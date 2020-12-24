import os
import sys
import time

import numpy as np
import tensorflow as tf
from forward import EDGE_VARIABLES, UNARY_VARIABLES, edge, loss, unary
from shuffle_queue import ShuffleQueue
from viterbi import viterbi

BATCH_SIZE = 128
PATCH_SIZE = 9
N_FEATURES = 128
N_EDGE_FEATURES = 25
TRAIN_STEPS = 5000
LEARNING_RATE = 1e-3
DROPOUT_KEEP_PROB = 0.8
REGULARIZATION_STRENGTH = 0.000
EDGE_LAMBDA = 1
CHECKPOINT_DIR = os.path.join(os.path.dirname(__file__), 'trained_model_cleaneval_split')

def main():
  if len(sys.argv) < 2:
    print("USAGE: python main.py [command]")
    sys.exit()
  if sys.argv[1] == 'train_unary':
    # Train the block classification network
    train_unary()
  elif sys.argv[1] == 'train_edge':
    # Train the edge classification network
    train_edge()
  elif sys.argv[1] == 'test_structured':
    # Run performance tests based on a trained network
    test_structured()
  elif sys.argv[1] == 'classify':
    # Classifies a single page
    # Takes {base}_edge_features.csv and {base}_block_feature.csv as produced by
    # ch.ethz.dalab.web2text.ExtractPageFeatures
    [_, _, file_base, labels_output_file] = sys.argv
    classify(file_base + '_block_features.csv', file_base + '_edge_features.csv', labels_output_file)
  else:
    raise ValueError('Unknown command ' + sys.argv[1])

def evaluate_unary(dataset, prediction_fn):
  fp, fn, tp, tn = 0, 0, 0, 0
  for doc in dataset:
    predictions = prediction_fn(doc[b'data'], doc[b'edge_data'])

    for i, lab in enumerate(doc[b'labels']):
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
    predictions = prediction_fn(doc[b'edge_data'])

    for i, lab in enumerate(doc[b'edge_labels']):
      if predictions[i] == lab:
        correct += 1
      else:
        incorrect += 1

  return float(correct) / (correct + incorrect)


def train_unary(conv_weight_decay = REGULARIZATION_STRENGTH):
  from data import cleaneval_test, cleaneval_train, cleaneval_validation
  training_queue = ShuffleQueue(cleaneval_train)

  data_shape = [BATCH_SIZE, PATCH_SIZE, 1, N_FEATURES]
  labs_shape = [BATCH_SIZE, PATCH_SIZE, 1, 1]
  train_features = tf.placeholder(tf.float32, shape=data_shape)
  train_labels   = tf.placeholder(tf.int64,   shape=labs_shape)

  logits = unary(train_features,
                 is_training=True,
                 conv_weight_decay=conv_weight_decay,
                 dropout_keep_prob=DROPOUT_KEEP_PROB)
  l = loss(tf.reshape(logits, [-1, 2]), tf.reshape(train_labels, [-1]))
  train_op = tf.train.AdamOptimizer(LEARNING_RATE).minimize(l)

  test_features = tf.placeholder(tf.float32)
  tf.get_variable_scope().reuse_variables()
  test_logits = unary(test_features, is_training=False)

  saver = tf.train.Saver(tf.get_collection(UNARY_VARIABLES))
  init_op = tf.global_variables_initializer()

  with tf.Session() as session:
    # Initialize
    session.run(init_op)

    def prediction(features, edge_features):
      features = features[np.newaxis, :, np.newaxis, :]
      logits = session.run(test_logits, feed_dict={test_features: features})
      return np.argmax(logits, axis=-1).flatten()

    BEST_VAL_SO_FAR = 0
    for step in range(TRAIN_STEPS+1):
      # Construct a bs-length numpy array
      features, _, labels, edge_labels = get_batch(training_queue)
      # Run a training step
      loss_val, _ = session.run(
        [l, train_op],
        feed_dict={train_features: features, train_labels: labels}
      )

      if step % 100 == 0:
        _,_,_,f1_validation = evaluate_unary(cleaneval_validation, prediction)
        _,_,_,f1_train = evaluate_unary(cleaneval_train, prediction)
        if f1_validation > BEST_VAL_SO_FAR:
          best = True
          saver.save(session, os.path.join(CHECKPOINT_DIR, 'unary.ckpt'))
          BEST_VAL_SO_FAR = f1_validation
        else:
          best = False
        print("%10d: train=%.4f, val=%.4f %s" % (step, f1_train, f1_validation, '*' if best else ''))
    # saver.save(session, os.path.join(CHECKPOINT_DIR, 'unary.ckpt'))
    return f1_validation


def train_edge(conv_weight_decay = REGULARIZATION_STRENGTH):
  from data import cleaneval_test, cleaneval_train, cleaneval_validation
  training_queue = ShuffleQueue(cleaneval_train)

  data_shape = [BATCH_SIZE, PATCH_SIZE-1, 1, N_EDGE_FEATURES]
  labs_shape = [BATCH_SIZE, PATCH_SIZE-1, 1, 1]
  train_features = tf.placeholder(tf.float32, shape=data_shape)
  train_labels   = tf.placeholder(tf.int64,   shape=labs_shape)

  logits = edge(train_features,
                is_training=True,
                conv_weight_decay=conv_weight_decay,
                dropout_keep_prob=DROPOUT_KEEP_PROB)
  l = loss(tf.reshape(logits, [-1, 4]), tf.reshape(train_labels, [-1]))
  train_op = tf.train.AdamOptimizer(LEARNING_RATE).minimize(l)

  test_features = tf.placeholder(tf.float32)
  tf.get_variable_scope().reuse_variables()
  test_logits = edge(test_features, is_training=False)

  saver = tf.train.Saver(tf.get_collection(EDGE_VARIABLES))
  init_op = tf.global_variables_initializer()

  with tf.Session() as session:
    # Initialize
    session.run(init_op)

    def prediction(features):
      features = features[np.newaxis, :, np.newaxis, :]
      logits = session.run(test_logits, feed_dict={test_features: features})
      return np.argmax(logits, axis=-1).flatten()

    BEST_VAL_SO_FAR = 0
    for step in range(TRAIN_STEPS+1):
      # Construct a bs-length numpy array
      _, edge_features, labels, edge_labels = get_batch(training_queue)
      # Run a training step
      loss_val, _ = session.run(
        [l, train_op],
        feed_dict={train_features: edge_features, train_labels: edge_labels}
      )


      if step % 100 == 0:
        accuracy_validation = evaluate_edge(cleaneval_validation, prediction)
        accuracy_train = evaluate_edge(cleaneval_train, prediction)
        if accuracy_validation > BEST_VAL_SO_FAR:
          best = True
          saver.save(session, os.path.join(CHECKPOINT_DIR, 'edge.ckpt'))
          BEST_VAL_SO_FAR = accuracy_validation
        else:
          best = False
        print("%10d: train=%.4f, val=%.4f %s" % (step, accuracy_train, accuracy_validation, '*' if best else ''))
    # saver.save(session, os.path.join(CHECKPOINT_DIR, 'edge.ckpt'))
    return accuracy_validation


def test_structured(lamb=EDGE_LAMBDA):
  from data import cleaneval_test, cleaneval_train, cleaneval_validation
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

  init_op = tf.global_variables_initializer()

  with tf.Session() as session:
    session.run(init_op)
    unary_saver.restore(session, os.path.join(CHECKPOINT_DIR, "unary.ckpt"))
    edge_saver.restore(session, os.path.join(CHECKPOINT_DIR, "edge.ckpt"))

    from time import time

    start = time()
    def prediction_structured(features, edge_feat):
      features  = features[np.newaxis, :, np.newaxis, :]
      edge_feat = edge_feat[np.newaxis, :, np.newaxis, :]

      unary_lgts = session.run(unary_logits, feed_dict={unary_features: features})
      edge_lgts = session.run(edge_logits, feed_dict={edge_features: edge_feat})

      return viterbi(unary_lgts.reshape([-1,2]), edge_lgts.reshape([-1,4]), lam=lamb)

    def prediction_unary(features, _):
      features = features[np.newaxis, :, np.newaxis, :]
      logits = session.run(unary_logits, feed_dict={unary_features: features})
      return np.argmax(logits, axis=-1).flatten()

    accuracy, precision, recall, f1 = evaluate_unary(cleaneval_test, prediction_structured)
    accuracy_u, precision_u, recall_u, f1_u = evaluate_unary(cleaneval_test, prediction_unary)
    end = time()
    print('duration', end-start)
    print('size', len(cleaneval_test))
    print("Structured: Accuracy=%.5f, precision=%.5f, recall=%.5f, F1=%.5f" % (accuracy, precision, recall, f1))
    print("Just unary: Accuracy=%.5f, precision=%.5f, recall=%.5f, F1=%.5f" % (accuracy_u, precision_u, recall_u, f1_u))


def classify(block_features_file, edge_features_file, labels_output_file, lamb=EDGE_LAMBDA):
  block_features = np.genfromtxt(block_features_file, delimiter=',')
  edge_features = np.genfromtxt(edge_features_file, delimiter=',')

  # Reshape
  block_features = block_features.T[np.newaxis, :, np.newaxis, :].astype(np.float32)
  edge_features = edge_features.T[np.newaxis, :, np.newaxis, :].astype(np.float32)

  unary_features = tf.constant(block_features)
  edge_features  = tf.constant(edge_features)

  unary_logits = unary(unary_features, is_training=False)
  edge_logits  = edge(edge_features, is_training=False)

  unary_saver = tf.train.Saver(tf.get_collection(UNARY_VARIABLES))
  edge_saver  = tf.train.Saver(tf.get_collection(EDGE_VARIABLES))

  init_op = tf.global_variables_initializer()

  with tf.Session() as session:
    session.run(init_op)
    unary_saver.restore(session, os.path.join(CHECKPOINT_DIR, "unary.ckpt"))
    edge_saver.restore(session, os.path.join(CHECKPOINT_DIR, "edge.ckpt"))

    from time import time
    start = time()

    unary_lgts = session.run(unary_logits)
    edge_lgts = session.run(edge_logits)

    labels = viterbi(unary_lgts.reshape([-1,2]), edge_lgts.reshape([-1,4]), lam=lamb).astype(np.int32)

    duration = time() - start
    print("Done. Classification took %.2f seconds " % duration)
    with open(labels_output_file, 'w') as fp:
      fp.write(",".join('%d' % label for label in labels))
    print('CSV labels written to %s' % labels_output_file)



def get_batch(q, batch_size=BATCH_SIZE, patch_size=PATCH_SIZE):
  """Takes a batch from a ShuffleQueue of documents"""
  # Define empty matrices for the return data

  batch       = np.zeros((BATCH_SIZE, PATCH_SIZE, 1, N_FEATURES), dtype = np.float32)
  labels      = np.zeros((BATCH_SIZE, PATCH_SIZE, 1, 1), dtype = np.float32)
  edge_batch  = np.zeros((BATCH_SIZE, PATCH_SIZE-1, 1, N_EDGE_FEATURES), dtype = np.float32)
  edge_labels = np.zeros((BATCH_SIZE, PATCH_SIZE-1, 1, 1), dtype = np.int64)


  for entry in range(BATCH_SIZE):
    # Find an entry that is long enough (at least one patch size)
    while True:
      doc = q.takeOne()
      length = doc[b'data'].shape[0]
      if length > PATCH_SIZE+1:
        break

    # Select a random patch
    i = np.random.random_integers(length-PATCH_SIZE-1)

    # Add it to the tensors
    batch[entry,:,0,:]       = doc[b'data'][i:i+PATCH_SIZE,:]
    edge_batch[entry,:,0,:]  = doc[b'edge_data'][i:i+PATCH_SIZE-1,:]
    labels[entry,:,0,0]      = doc[b'labels'][i:i+PATCH_SIZE] # {0,1}
    edge_labels[entry,:,0,0] = doc[b'edge_labels'][i:i+PATCH_SIZE-1] # {0,1,2,3} = {00,01,10,11}

  return batch, edge_batch, labels, edge_labels

if __name__ == '__main__':
  main()
