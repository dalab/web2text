# Code from https://github.com/jasonbaldridge/try-tf

#import tensorflow.python.platform
import numpy as np
import tensorflow.compat.v1 as tf

#import plot_boundary_on_data
import os, sys
from data_queue import DataQueue

# TODO: Make use of Tensorfow 2 functionality
tf.disable_v2_behavior()

# Global variables.
NUM_LABELS = 1  # The number of labels.
BATCH_SIZE = 10 # The number of training examples to use per training step.

TRAIN_FILE_PATH = 'trained_model_all_the_news_split/train/'
TEST_FILE_PATH = 'trained_model_all_the_news_split/test/'
TRAIN_SIZE = 100
NUM_HIDDEN = 10
NUM_FEATURES = 136

#tf.flags.DEFINE_string('train', "\\trained_model_web2text_split\\train\\output_7__0a0a5bddf78b2ef2de13c71fbf737764bbf97449_block_features.csv",
#                           'File containing the training data (labels & features).')
#tf.flags.DEFINE_string('test',  "\\trained_model_web2text_split\\test\\output_7__0a0a5bddf78b2ef2de13c71fbf737764bbf97449_block_features.csv",
#                           'File containing the test data (labels & features).')
tf.flags.DEFINE_string('train', "trained_model_all_the_news_split/train", 'Path containing the training data (labels & features).')
tf.flags.DEFINE_string('test',  "trained_model_all_the_news_split/test", 'Path containing the test data (labels & features).')
tf.flags.DEFINE_integer('num_epochs', 1,
                            'Number of passes over the training data.')
tf.flags.DEFINE_integer('num_hidden', NUM_HIDDEN,
                            'Number of nodes in the hidden layer.')
tf.flags.DEFINE_boolean('verbose', True, 'Produce verbose output.')
tf.flags.DEFINE_boolean('plot', False, 'Plot the final decision boundary on the data.')

FLAGS = tf.flags.FLAGS

def extract_test_data(filename):
    dq_test    = DataQueue(TEST_FILE_PATH,10)
    test_size = dq_test.get_size()
    test_num_files = dq_test.get_num_files()

    print(f"The test_size:{test_size}, test_num_files:{test_num_files}")
    for i in range(test_size):
        get_data, get_labels = dq_test.takeOne()
        if i ==0:
            test_data, test_labels = get_data, get_labels
        else:
            test_data = np.vstack([test_data,get_data])
            test_labels = np.hstack([test_labels,get_labels])
    #test_labels = test_labels.reshape(test_labels.shape+(1,))
    test_labels = test_labels.reshape((test_size,1))
    return test_data,test_labels

# Extract numpy representations of the labels and features given rows consisting of:
#   label, feat_0, feat_1, ..., feat_n
def extract_data(filename):

    # Arrays to hold the labels and feature vectors.
    labels = []
    fvecs = []

    # Iterate over the rows, splitting the label from the features. Convert labels
    # to integers and features to floats.
    for line in file(filename):
        row = line.split(",")
        labels.append(int(row[0]))
        fvecs.append([float(x) for x in row[1:]])

    # Convert the array of float arrays into a numpy float matrix.
    fvecs_np = np.matrix(fvecs).astype(np.float32)

    # Convert the array of int labels into a numpy array.
    labels_np = np.array(labels).astype(dtype=np.uint8)

    # Convert the int numpy array into a one-hot matrix.
    labels_onehot = (np.arange(NUM_LABELS) == labels_np[:, None]).astype(np.float32)

    # Return a pair of the feature matrix and the one-hot label matrix.
    return fvecs_np,labels_onehot

# Init weights method. (Lifted from Delip Rao: http://deliprao.com/archives/100)
def init_weights(shape, init_method='xavier', xavier_params = (None, None)):
    if init_method == 'zeros':
        return tf.Variable(tf.zeros(shape, dtype=tf.float32))
    elif init_method == 'uniform':
        return tf.Variable(tf.random_normal(shape, stddev=0.01, dtype=tf.float32))
    else: #xavier
        (fan_in, fan_out) = xavier_params
        low = -4*np.sqrt(6.0/(fan_in + fan_out)) # {sigmoid:4, tanh:1}
        high = 4*np.sqrt(6.0/(fan_in + fan_out))
        return tf.Variable(tf.random_uniform(shape, minval=low, maxval=high, dtype=tf.float32))

def main(argv=None):
    # Be verbose?
    verbose = FLAGS.verbose

    # Plot?
    plot = FLAGS.plot

    # Get the data.
    train_data_filename = FLAGS.train
    test_data_filename = FLAGS.test

    # Get the file queues
    dq_train  = DataQueue(train_data_filename,BATCH_SIZE)
    dq_test    = DataQueue(test_data_filename,BATCH_SIZE)

    # Extract test data into numpy array.
    test_data, test_labels = extract_test_data(test_data_filename)

    print(f"test_data.shape: {test_data.shape}, test_labels.shape= {test_labels.shape}")
    # Get the shape of the training data.
    #train_size,num_features = train_data.shape

    # Get the number of epochs for training.
    num_epochs = FLAGS.num_epochs

    # Get the size of layer one.
    num_hidden = FLAGS.num_hidden

    # This is where training samples and labels are fed to the graph.
    # These placeholder nodes will be fed a batch of training data at each
    # training step using the {feed_dict} argument to the Run() call below.
    x = tf.placeholder("float", shape=[None, NUM_FEATURES])
    y_ = tf.placeholder("float", shape=[None, NUM_LABELS])

    # For the test data, hold the entire dataset in one constant node.
    #test_data_node = tf.constant(test_data)

    # Define and initialize the network.

    # Initialize the hidden weights and biases.
    w_hidden = init_weights(
        [NUM_FEATURES, num_hidden],
        'xavier',
        xavier_params=(NUM_FEATURES, num_hidden))

    b_hidden = init_weights([1,num_hidden],'zeros')

    # The hidden layer.
    hidden = tf.nn.tanh(tf.matmul(x,w_hidden) + b_hidden)

    # Initialize the output weights and biases.
    w_out = init_weights(
        [num_hidden, NUM_LABELS],
        'xavier',
        xavier_params=(num_hidden, NUM_LABELS))

    b_out = init_weights([1,NUM_LABELS],'zeros')

    # The output layer.
    y = tf.nn.softmax(tf.matmul(hidden, w_out) + b_out)

    # Optimization.
    cross_entropy = -tf.reduce_sum(y_*tf.log(y))
    train_step = tf.train.GradientDescentOptimizer(0.01).minimize(cross_entropy)

    # Evaluation.
    predicted_class = tf.argmax(y,1)
    correct_prediction = tf.equal(tf.argmax(y,1), tf.argmax(y_,1))
    accuracy = tf.reduce_mean(tf.cast(correct_prediction, "float"))

    # Create a local session to run this computation.
    with tf.Session() as s:
        # Run all the initializers to prepare the trainable parameters.
        tf.initialize_all_variables().run()
        if verbose:
            print('Initialized!')
            print('Training.')

        # Iterate and train.
        valor = num_epochs * TRAIN_SIZE // BATCH_SIZE
        print(f"El entrenamiento es de steps={valor}, num_epochs={num_epochs}, batch_size={BATCH_SIZE}, TRAIN_SIZE={TRAIN_SIZE}")
        for step in range(num_epochs * TRAIN_SIZE // BATCH_SIZE):
            if verbose:
                print(step)

            #offset = (step * BATCH_SIZE) % TRAIN_SIZE
            #batch_data, batch_labels, offset, offset_latest_file = get_next_batch(offset,offset_latest_file,train_file_list)
            # train_data[offset:(offset + BATCH_SIZE), :]
            #batch_labels = train_labels[offset:(offset + BATCH_SIZE)]
            batch_data, batch_labels = dq_train.takeOne()
            #batch_labels_trans = np.transpose(batch_labels) # remove
            #batch_labels = batch_labels.reshape(())
            #test_labels = test_labels.reshape((1,test_size))
            train_step.run(feed_dict={x: batch_data, y_: batch_labels})
            #if verbose and offset >= TRAIN_SIZE-BATCH_SIZE:
            #    print
        print(f"batch_data.shape={batch_data.shape}, batch_labels_trans.shape={batch_labels.shape}")
        print(f"test_data.shape={test_data.shape}, test_labels.shape={test_labels.shape}")
        print("Accuracy:", accuracy.eval(feed_dict={x: test_data, y_: test_labels}))

        if plot:
            eval_fun = lambda X: predicted_class.eval(feed_dict={x:X});
            plot_boundary_on_data.plot(test_data, test_labels, eval_fun)

if __name__ == '__main__':
    tf.app.run()