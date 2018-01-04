import tensorflow as tf
import numpy as np
import time
import sys

from data import cleaneval_train, cleaneval_test
from forward import loss, unary, edge, EDGE_VARIABLES, UNARY_VARIABLES
from shuffle_queue import ShuffleQueue
from viterbi import viterbi

BATCH_SIZE = 128
PATCH_SIZE = 9
N_FEATURES = 128
N_EDGE_FEATURES = 25


def evaluate_unary(dataset, prediction_fn):
  fp, fn, tp, tn = 0, 0, 0, 0
  for doc in dataset:
    predictions = prediction_fn(doc['data'], doc['edge_data'])

    for i, lab in enumerate(doc['labels']):
      if predictions[i] == 1 and lab == 1:
        tp += 1
      elif predictions[i] == 1 and lab == 0:
        fp += 1
      elif predictions[i] == 0 and lab == 1:
        fn += 1
      else:
        tn += 1

  n = fp+fn+tp+tn
  accuracy = float(tp+tn) / n
  precision = float(tp) / (tp+fp)
  recall = float(tp) / (tp+fn)
  f1 = 2*precision*recall/(precision+recall)
  return accuracy, precision, recall, f1


def evaluate_edge(dataset, prediction_fn):
  correct, incorrect = 0, 0
  for doc in dataset:
    predictions = prediction_fn(doc['edge_data'])

    for i, lab in enumerate(doc['edge_labels']):
      if predictions[i] == lab:
        correct += 1
      else:
        incorrect += 1

  return float(correct) / (correct + incorrect)


def train_unary(conv_weight_decay = 0.0001):

  training_queue = ShuffleQueue(cleaneval_train)

  data_shape = [BATCH_SIZE, PATCH_SIZE, 1, N_FEATURES]
  labs_shape = [BATCH_SIZE, PATCH_SIZE, 1, 1]
  train_features = tf.placeholder(tf.float32, shape=data_shape)
  train_labels   = tf.placeholder(tf.int64,   shape=labs_shape)

  logits = unary(train_features,
                 is_training=True,
                 conv_weight_decay=conv_weight_decay,
                 dropout_keep_prob=0.75)
  l = loss(tf.reshape(logits, [-1, 2]), tf.reshape(train_labels, [-1]))
  train_op = tf.train.AdamOptimizer().minimize(l)

  test_features = tf.placeholder(tf.float32)
  tf.get_variable_scope().reuse_variables()
  test_logits = unary(test_features, is_training=False)

  saver = tf.train.Saver(tf.get_collection(UNARY_VARIABLES))
  init_op = tf.initialize_all_variables()

  with tf.Session() as session:
    # Initialize
    session.run(init_op)

    def prediction(features, edge_features):
      features = features[np.newaxis, :, np.newaxis, :]
      logits = session.run(test_logits, feed_dict={test_features: features})
      return np.argmax(logits, axis=-1).flatten()

    for step in xrange(2001):
      # Construct a bs-length numpy array
      features, _, labels, edge_labels = get_batch(training_queue)
      # Run a training step
      loss_val, _ = session.run(
        [l, train_op],
        feed_dict={train_features: features, train_labels: labels}
      )


      if step % 100 == 0:
        _,_,_,f1_test = evaluate_unary(cleaneval_test, prediction)
        _,_,_,f1_train = evaluate_unary(cleaneval_train, prediction)
        print("%.4f, %.4f" % (f1_train, f1_test))
    saver.save(session, 'trained_model/unary.ckpt')
    return f1_test


def train_edge(conv_weight_decay = 0.0001):

  training_queue = ShuffleQueue(cleaneval_train)

  data_shape = [BATCH_SIZE, PATCH_SIZE-1, 1, N_EDGE_FEATURES]
  labs_shape = [BATCH_SIZE, PATCH_SIZE-1, 1, 1]
  train_features = tf.placeholder(tf.float32, shape=data_shape)
  train_labels   = tf.placeholder(tf.int64,   shape=labs_shape)

  logits = edge(train_features,
                is_training=True,
                conv_weight_decay=conv_weight_decay,
                dropout_keep_prob=0.8)
  l = loss(tf.reshape(logits, [-1, 4]), tf.reshape(train_labels, [-1]))
  train_op = tf.train.AdamOptimizer().minimize(l)

  test_features = tf.placeholder(tf.float32)
  tf.get_variable_scope().reuse_variables()
  test_logits = edge(test_features, is_training=False)

  saver = tf.train.Saver(tf.get_collection(EDGE_VARIABLES))
  init_op = tf.initialize_all_variables()

  with tf.Session() as session:
    # Initialize
    session.run(init_op)

    def prediction(features):
      features = features[np.newaxis, :, np.newaxis, :]
      logits = session.run(test_logits, feed_dict={test_features: features})
      return np.argmax(logits, axis=-1).flatten()

    for step in xrange(2001):
      # Construct a bs-length numpy array
      _, edge_features, labels, edge_labels = get_batch(training_queue)
      # Run a training step
      loss_val, _ = session.run(
        [l, train_op],
        feed_dict={train_features: edge_features, train_labels: edge_labels}
      )


      if step % 100 == 0:
        accuracy_test = evaluate_edge(cleaneval_test, prediction)
        accuracy_train = evaluate_edge(cleaneval_train, prediction)
        print("%.4f, %.4f" % (accuracy_train, accuracy_test))
    saver.save(session, 'trained_model/edge.ckpt')
    return accuracy_test


def test_structured(lamb=0.1):
  unary_features = tf.placeholder(tf.float32)
  edge_features  = tf.placeholder(tf.float32)

  # hack to get the right shape weights
  _ = unary(tf.placeholder(tf.float32, shape=[1,PATCH_SIZE,1,N_FEATURES]), False)
  _ = edge(tf.placeholder(tf.float32, shape=[1,PATCH_SIZE,1,N_EDGE_FEATURES]), False)

  tf.get_variable_scope().reuse_variables()
  unary_logits = unary(unary_features, is_training=False)
  edge_logits  = edge(edge_features, is_training=False)

  unary_saver = tf.train.Saver(tf.get_collection(UNARY_VARIABLES))
  edge_saver  = tf.train.Saver(tf.get_collection(EDGE_VARIABLES))

  init_op = tf.initialize_all_variables()

  with tf.Session() as session:
    session.run(init_op)
    unary_saver.restore(session, "trained_model/unary.ckpt")
    edge_saver.restore(session, "trained_model/edge.ckpt")

    print(session.run(tf.train.get_or_create_global_step()))

    def prediction(features, edge_feat):
      features  = features[np.newaxis, :, np.newaxis, :]
      edge_feat = edge_feat[np.newaxis, :, np.newaxis, :]

      unary_lgts = session.run(unary_logits, feed_dict={unary_features: features})
      edge_lgts = session.run(edge_logits, feed_dict={edge_features: edge_feat})

      return viterbi(unary_lgts.reshape([-1,2]), edge_lgts.reshape([-1,4]), lam=lamb)

    accuracy, precision, recall, f1 = evaluate_unary(cleaneval_test, prediction)
    print("Accuracy=%.5f, precision=%.5f, recall=%.5f, F1=%.5f" % (accuracy, precision, recall, f1))


def get_batch(q, batch_size=BATCH_SIZE, patch_size=PATCH_SIZE):
  """Takes a batch from a ShuffleQueue of documents"""
  # Define empty matrices for the return data

  batch       = np.zeros((BATCH_SIZE, PATCH_SIZE, 1, N_FEATURES), dtype = np.float32)
  labels      = np.zeros((BATCH_SIZE, PATCH_SIZE, 1, 1), dtype = np.float32)
  edge_batch  = np.zeros((BATCH_SIZE, PATCH_SIZE-1, 1, N_EDGE_FEATURES), dtype = np.float32)
  edge_labels = np.zeros((BATCH_SIZE, PATCH_SIZE-1, 1, 1), dtype = np.int64)


  for entry in xrange(BATCH_SIZE):
    # Find an entry that is long enough (at least one patch size)
    while True:
      doc = q.takeOne()
      length = doc['data'].shape[0]
      if length > PATCH_SIZE+1:
        break;

    # Select a random patch
    i = np.random.random_integers(length-PATCH_SIZE-1)

    # Add it to the tensors
    batch[entry,:,0,:]       = doc['data'][i:i+PATCH_SIZE,:]
    edge_batch[entry,:,0,:]  = doc['edge_data'][i:i+PATCH_SIZE-1,:]
    labels[entry,:,0,0]      = doc['labels'][i:i+PATCH_SIZE] # {0,1}
    edge_labels[entry,:,0,0] = doc['edge_labels'][i:i+PATCH_SIZE-1] # {0,1,2,3} = {00,01,10,11}

  return batch, edge_batch, labels, edge_labels


if __name__ == '__main__':
  if len(sys.argv) < 2:
    print("USAGE: python main.py [command]")
    sys.exit()
  if sys.argv[1] == 'train_unary':
    train_unary()
  elif sys.argv[1] == 'train_edge':
    train_edge()
  elif sys.argv[1] == 'test_structured':
    test_structured()
