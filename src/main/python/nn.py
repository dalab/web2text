import tensorflow  as tf
#from data_queue import DataQueue
import numpy as np
import pandas as pd
import os
import time
from math import floor, ceil
import sklearn as sk
from sklearn.metrics import confusion_matrix

NUM_LABELS = 1
BATCH_SIZE = 1000 # The number of training examples to use per training step.
TRAINING_EPOCHS = 5000
DISPLAY_STEP = 100
LEARNING_RATE=0.0001
KEEP_PROB=0.8
NUM_FEATURES = 137
NUM_HIDDEN = NUM_FEATURES
TRAIN_AND_TEST_FILE_PATH = '../../../public/train_and_test/'
TRAIN_SIZE = 0.8
NUM_FILES_TO_READ = 20 # There are 76,968 files. 100 files take about 20 minutes with 5000 epochs
RANDOM_STATE = 1

def encode(series):
    return pd.get_dummies(series.astype(str))

def read_csvs(path):
    file_list = [os.path.join(path, f) for f in os.listdir(path) if os.path.isfile(os.path.join(path, f))]
    first = True
    for i in range(NUM_FILES_TO_READ):
        #print(f"i={i}")
        file = file_list[i]
        #print(f"reading file:{file}")
        page_df = pd.read_csv(file, sep=",").transpose()
        page_df.columns =\
            ["has_duplicate","has_10_duplicates","n_same_class_path",
            "has_word","log(n_words)","avg_word_length [3,15]runMain 2s",
            "has_stopword","contains_popular_name","contains_author_particle",
            "stopword_ratio","log(n_characters) [2.5,5.5]","contains_punctuation",
            "n_punctuation [0,10]","log(punctuation_ratio)","has_numeric",
            "numeric_ratio","log(avg_sentence_length) [2,5]","has_multiple_sentences",
            "relative_position","relative_position^2","ends_with_punctuation",
            "ends_with_question_mark","contains_copyright","contains_email",
            "contains_url","contains_year","ratio_words_with_capital",
            "ratio_words_with_capital^2","ratio_words_with_capital^3",
            "contains_author","has_p","p_body_percentage","p_link_density",
            "p_avg_word_length [3,15]","p_has_stopword","p_stopword_ratio",
            "p_contains_popular_name","p_contains_author_particle",
            "p_log(n_characters) [2.5,10]","p_log(punctuation_ratio)",
            "p_has_numeric","p_numeric_ratio","p_log(avg_sentence_length) [2,5]",
            "p_ends_with_punctuation","p_ends_with_question_mark",
            "p_contains_copyright","p_contains_email","p_contains_url",
            "p_contains_year","p_ratio_words_with_capital",
            "p_ratio_words_with_capital^2","p_ratio_words_with_capital^3",
            "p_contains_form_element","p_tag_td","p_tag_div","p_tag_p","p_tag_tr",
            "p_tag_table","p_tag_body","p_tag_ul","p_tag_span","p_tag_li",
            "p_tag_blockquote","p_tag_b","p_tag_small","p_tag_a","p_tag_ol",
            "p_tag_ul (2)","p_tag_i","p_tag_form","p_tag_dl","p_tag_strong",
            "p_tag_pre","has_gp","gp_body_percentage","gp_link_density",
            "gp_avg_word_length [3,15]","gp_has_stopword","gp_stopword_ratio",
            "gp_contains_popular_name","gp_contains_author_particle",
            "gp_log(n_characters) [2.5,10]","gp_log(punctuation_ratio)",
            "gp_has_numeric","gp_numeric_ratio","gp_log(avg_sentence_length) [2,5]",
            "gp_ends_with_punctuation","gp_ends_with_question_mark",
            "gp_contains_copyright","gp_contains_email","gp_contains_url",
            "gp_contains_year","gp_ratio_words_with_capital",
            "gp_ratio_words_with_capital^2","gp_ratio_words_with_capital^3",
            "gp_contains_form_element","root_body_percentage","root_link_density",
            "root_avg_word_length [3,15]","root_has_stopword","root_stopword_ratio",
            "root_contains_popular_name","root_contains_author_particle",
            "root_log(n_characters) [2.5,10]","root_log(punctuation_ratio)",
            "root_has_numeric","root_numeric_ratio","root_log(avg_sentence_length) [2,5]",
            "root_ends_with_punctuation","root_ends_with_question_mark",
            "root_contains_copyright","root_contains_email","root_contains_url",
            "root_contains_year","root_ratio_words_with_capital",
            "root_ratio_words_with_capital^2","root_ratio_words_with_capital^3",
            "root_contains_form_element","tag_a","tag_p","tag_td","tag_b",
            "tag_li","tag_span","tag_i","tag_tr","tag_div","tag_strong","tag_em",
            "tag_h3","tag_h2","tag_table","tag_h4","tag_small","tag_sup","tag_h1",
            "tag_blockquote"]
        if first:
            full_df = page_df
            first = False
        else:
            full_df = pd.concat([full_df, page_df])
    return full_df

def multilayer_perceptron(x, weights, biases, keep_prob):
    layer_1 = tf.add(tf.matmul(x, weights['h1']), biases['b1'])
    layer_1 = tf.nn.relu(layer_1)
    layer_1 = tf.nn.dropout(layer_1, keep_prob)
    out_layer = tf.matmul(layer_1, weights['out']) + biases['out']
    return out_layer

if __name__ == '__main__':
    np.random.seed(RANDOM_STATE)
    tf.set_random_seed(RANDOM_STATE)
    start_time = time.clock()
    train_and_test_df = read_csvs(TRAIN_AND_TEST_FILE_PATH)
    train_cnt = floor(train_and_test_df.shape[0] * TRAIN_SIZE)
    train_df = train_and_test_df.iloc[0:train_cnt]
    test_df = train_and_test_df.iloc[train_cnt:]
    train_x = train_df.drop(columns=['contains_author'])
    train_y = encode(train_df['contains_author'])
    test_x = test_df.drop(columns=['contains_author'])
    test_y = encode(test_df['contains_author'])
    print(f"train_x.shape: {train_x.shape}, train_y.shape={train_y.shape}")
    print(f"test_x.shape: {test_x.shape}, test_y.shape={test_y.shape}")
    print(f"Read {NUM_FILES_TO_READ} training and test files: {time.clock() - start_time} seconds")
    start_time = time.clock()
    n_hidden_1 = NUM_HIDDEN
    n_input = train_x.shape[1]
    n_classes = 2 # train_y.shape[1]
    print(f"n_input={n_input}, n_classes={n_classes}, n_hidden_1={n_hidden_1}")
    weights = {
        'h1': tf.Variable(tf.random_normal([n_input, n_hidden_1])),
        'out': tf.Variable(tf.random_normal([n_hidden_1, n_classes]))
    }
    biases = {
        'b1': tf.Variable(tf.random_normal([n_hidden_1])),
        'out': tf.Variable(tf.random_normal([n_classes]))
    }
    keep_prob = tf.placeholder("float", name="keep_prob")
    x = tf.placeholder("float", [None, n_input], name="x")
    y = tf.placeholder("float", [None, n_classes], name="y")
    predictions = multilayer_perceptron(x, weights, biases, keep_prob)
    cost = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits_v2(logits=predictions, labels=y))
    optimizer = tf.train.AdamOptimizer(learning_rate=LEARNING_RATE).minimize(cost)
    saver = tf.train.Saver()
    with tf.Session() as sess:
        sess.run(tf.global_variables_initializer())
        for epoch in range(TRAINING_EPOCHS):
            avg_cost = 0.0
            total_batch = int(len(train_x) / BATCH_SIZE)
            x_batches = np.array_split(train_x, total_batch)
            y_batches = np.array_split(train_y, total_batch)
            for i in range(total_batch):
                batch_x, batch_y = x_batches[i], y_batches[i]
                _, c = sess.run([optimizer, cost],
                                feed_dict={
                                    x: batch_x,
                                    y: batch_y,
                                    keep_prob: KEEP_PROB
                                })
                avg_cost += c / total_batch
            if epoch % DISPLAY_STEP == 0:
                print("Epoch:", '%04d' % (epoch+1), "cost=", \
                      "{:.9f}".format(avg_cost), " time=", "{:.2f}".format(time.clock() - start_time), " sec.")

        print(f"Training time: {time.clock() - start_time} seconds")
        print("Optimization Finished!")

        save_path = saver.save(sess, "trained_model_all_the_news_split/model.ckpt")

        correct_prediction = tf.equal(tf.argmax(predictions, 1), tf.argmax(y, 1))
        accuracy = tf.reduce_mean(tf.cast(correct_prediction, "float"))
        print("Accuracy:", accuracy.eval({x: test_x, y: test_y, keep_prob: 1.0}))

        y_p = tf.argmax(predictions, 1)
        val_accuracy, y_pred = sess.run([accuracy, y_p], feed_dict={x:test_x, y:test_y, keep_prob: 1.0})

        print(f"Test set Accuracy: {val_accuracy}")
        y_true = np.argmax(test_y,1)
        print(f"Precision: {sk.metrics.precision_score(y_true, y_pred)}")
        print(f"Recall: {sk.metrics.recall_score(y_true, y_pred)}")
        print(f"F1 Score: {sk.metrics.f1_score(y_true, y_pred)}")
        print("Confusion Matrix")
        print(sk.metrics.confusion_matrix(y_true, y_pred))
        fpr, tpr, tresholds = sk.metrics.roc_curve(y_true, y_pred)
