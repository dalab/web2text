import tensorflow.python.platform
#import numpy as np
import tensorflow as tf
#import plot_boundary_on_data
#import os, sys
#from data_queue import DataQueue

# Global variables.
NUM_LABELS = 1  # The number of labels.
BATCH_SIZE = 1 # The number of training examples to use per training step.

TRAIN_FILE_PATH = 'trained_model_all_the_news_split/train/'
TEST_FILE_PATH = 'trained_model_all_the_news_split/test/'
TRAIN_SIZE = 100
NUM_HIDDEN = 10
NUM_FEATURES = 140

def main(argv=None):
    print("Hello, this is a test")