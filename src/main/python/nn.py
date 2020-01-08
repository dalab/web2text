from math import floor, ceil
import numpy as np
import os
import pandas as pd
from sklearn.metrics import classification_report, confusion_matrix, precision_recall_fscore_support
import subprocess
import sys
import tensorflow as tf
from tensorflow import keras
from transformers import pipeline
import time

NUM_CLASSES = 2
BATCH_SIZE = 5000  # The number of training examples to use per training step.
TRAINING_EPOCHS = 50
LEARNING_RATE = 0.0001
KEEP_PROB = 0.8
NUM_FEATURES = 136
NUM_HIDDEN = NUM_FEATURES
TRAIN_SIZE = 0.8
NUM_FILES_TO_READ = 5000  # There are 76,968 files. 100 files take about 20 minutes using 5000 epochs
RANDOM_STATE = 1
MODEL_SAVE_FILE = '/Users/cesc/Desktop/Hypefactors/AuthorExtractor/public/trained_model_all_the_news/model.ckpt'


def load_csv(path, batch_load=True):
    if batch_load:
        file_list = [os.path.join(path, f) for f in os.listdir(path) if os.path.isfile(os.path.join(path, f))]
        num_files = NUM_FILES_TO_READ
    else:
        file_list = [path]
        num_files = 1
    first = True
    for i in range(num_files):
        file = file_list[i]
        page_df = pd.read_csv(file, sep=",").transpose()
        if len(page_df.columns) != NUM_FEATURES+1:
            print(f"Error in file:{file}. Ignoring it")
            continue
        page_df.columns = \
            ["has_duplicate", "has_10_duplicates", "n_same_class_path",
             "has_word", "log(n_words)", "avg_word_length [3,15]runMain 2s",
             "has_stopword", "contains_popular_name", "contains_author_particle",
             "stopword_ratio", "log(n_characters) [2.5,5.5]", "contains_punctuation",
             "n_punctuation [0,10]", "log(punctuation_ratio)", "has_numeric",
             "numeric_ratio", "log(avg_sentence_length) [2,5]", "has_multiple_sentences",
             "relative_position", "relative_position^2", "ends_with_punctuation",
             "ends_with_question_mark", "contains_copyright", "contains_email",
             "contains_url", "contains_year", "ratio_words_with_capital",
             "ratio_words_with_capital^2", "ratio_words_with_capital^3",
             "contains_author", "has_p", "p_body_percentage", "p_link_density",
             "p_avg_word_length [3,15]", "p_has_stopword", "p_stopword_ratio",
             "p_contains_popular_name", "p_contains_author_particle",
             "p_log(n_characters) [2.5,10]", "p_log(punctuation_ratio)",
             "p_has_numeric", "p_numeric_ratio", "p_log(avg_sentence_length) [2,5]",
             "p_ends_with_punctuation", "p_ends_with_question_mark",
             "p_contains_copyright", "p_contains_email", "p_contains_url",
             "p_contains_year", "p_ratio_words_with_capital",
             "p_ratio_words_with_capital^2", "p_ratio_words_with_capital^3",
             "p_contains_form_element", "p_tag_td", "p_tag_div", "p_tag_p", "p_tag_tr",
             "p_tag_table", "p_tag_body", "p_tag_ul", "p_tag_span", "p_tag_li",
             "p_tag_blockquote", "p_tag_b", "p_tag_small", "p_tag_a", "p_tag_ol",
             "p_tag_ul (2)", "p_tag_i", "p_tag_form", "p_tag_dl", "p_tag_strong",
             "p_tag_pre", "has_gp", "gp_body_percentage", "gp_link_density",
             "gp_avg_word_length [3,15]", "gp_has_stopword", "gp_stopword_ratio",
             "gp_contains_popular_name", "gp_contains_author_particle",
             "gp_log(n_characters) [2.5,10]", "gp_log(punctuation_ratio)",
             "gp_has_numeric", "gp_numeric_ratio", "gp_log(avg_sentence_length) [2,5]",
             "gp_ends_with_punctuation", "gp_ends_with_question_mark",
             "gp_contains_copyright", "gp_contains_email", "gp_contains_url",
             "gp_contains_year", "gp_ratio_words_with_capital",
             "gp_ratio_words_with_capital^2", "gp_ratio_words_with_capital^3",
             "gp_contains_form_element", "root_body_percentage", "root_link_density",
             "root_avg_word_length [3,15]", "root_has_stopword", "root_stopword_ratio",
             "root_contains_popular_name", "root_contains_author_particle",
             "root_log(n_characters) [2.5,10]", "root_log(punctuation_ratio)",
             "root_has_numeric", "root_numeric_ratio", "root_log(avg_sentence_length) [2,5]",
             "root_ends_with_punctuation", "root_ends_with_question_mark",
             "root_contains_copyright", "root_contains_email", "root_contains_url",
             "root_contains_year", "root_ratio_words_with_capital",
             "root_ratio_words_with_capital^2", "root_ratio_words_with_capital^3",
             "root_contains_form_element", "tag_a", "tag_p", "tag_td", "tag_b",
             "tag_li", "tag_span", "tag_i", "tag_tr", "tag_div", "tag_strong", "tag_em",
             "tag_h3", "tag_h2", "tag_table", "tag_h4", "tag_small", "tag_sup", "tag_h1",
             "tag_blockquote"]
        if first:
            full_df = page_df
            first = False
        else:
            full_df = pd.concat([full_df, page_df])
    return full_df


def predict_from_csv(csv_file, html_file):
    pred_df = load_csv(csv_file, False).drop(columns=['contains_author'])
    model = tf.keras.models.load_model(MODEL_SAVE_FILE)
    model.summary()
    pred_y = model.predict(pred_df)
    pred_y_argmax = np.argmax(pred_y, axis=1)
    print(f"pred_y_argmax.shape={pred_y.shape}")
    print(f"pred_y_argmax={pred_y_argmax}")
    pred_y_argmax_2 = np.argmax(pred_y_argmax, axis=0)
    print(f"pred_y_argmax_2={pred_y_argmax_2}")
    print(f"pred_y={pred_y}")
    subprocess.run(['sbt', '"runMain ch.ethz.dalab.web2text.ExtractPageFeatures ' + html_file + ' ../../../public/train_and_test/predict_"'])
    if np.sum(pred_y_argmax) == 0:
        print("No author name predicted")
    else:
        print("Author name:")

    # print(x)
    # ner = pipeline('ner')
    # y = ner('Should be coming around the Montserrat River when John comes')
    # print(y)
    return


def create_model(n_input):
    model = tf.keras.models.Sequential([
        keras.layers.Dense(n_input, activation='relu', input_shape=(NUM_HIDDEN,)),
        keras.layers.Dropout(KEEP_PROB),
        keras.layers.Dense(2, activation='softmax')]
    )
    model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])
    return model


def train_keras(csv_folder):
    train_x, train_y, test_x, test_y = load_data(csv_folder)
    model = create_model(train_x.shape[1])
    model.summary()
    # Create a callback that saves the model's weights
    cp_callback = tf.keras.callbacks.ModelCheckpoint(filepath=MODEL_SAVE_FILE, save_weights_only=False, verbose=1)
    _ = model.fit(train_x, train_y, epochs=TRAINING_EPOCHS,
                  batch_size=BATCH_SIZE, validation_split=0.33, callbacks=[cp_callback])
    loss, accuracy = model.evaluate(test_x, test_y, verbose=0)
    print(f"loss={loss}, accuracy={100*accuracy}")
    pred_y = model.predict(test_x)
    print(f"pred_y.shape={pred_y.shape}, test_y.shape={test_y.shape}")
    rep = classification_report(np.argmax(test_y.to_numpy(), axis=1), np.argmax(pred_y, axis=1), digits=4)
    print(rep)
    conf = confusion_matrix(np.argmax(test_y.to_numpy(), axis=1), np.argmax(pred_y, axis=1))
    print(conf)
    prfs = precision_recall_fscore_support(np.argmax(test_y.to_numpy(), axis=1), np.argmax(pred_y, axis=1))
    print(prfs)
    model.save(MODEL_SAVE_FILE)
    return


def load_data(csv_folder):
    np.random.seed(RANDOM_STATE)
    start_time = time.process_time()
    train_and_test_df = load_csv(csv_folder, True)
    train_cnt = floor(train_and_test_df.shape[0] * TRAIN_SIZE)
    train_df = train_and_test_df.iloc[0:train_cnt]
    test_df = train_and_test_df.iloc[train_cnt:]
    train_x = train_df.drop(columns=['contains_author'])
    train_y = pd.get_dummies(train_df['contains_author'])
    test_x = test_df.drop(columns=['contains_author'])
    test_y = pd.get_dummies((test_df['contains_author']))
    print(f"train_x.shape: {train_x.shape}, train_y.shape={train_y.shape}")
    print(f"test_x.shape: {test_x.shape}, test_y.shape={test_y.shape}")
    print(f"Read {NUM_FILES_TO_READ} training and test files: {time.process_time() - start_time} seconds")
    start_time = time.process_time() # time.clock()
    return train_x, train_y, test_x, test_y


def main():
    if len(sys.argv) < 2:
        exit(0)
    elif len(sys.argv) == 4:
        if sys.argv[1] == '--predict_from_csv':
            predict_from_csv(sys.argv[2], sys.argv[3])
    elif sys.argv[1] == '--train_from_folder':
            train_keras(sys.argv[2])
            exit()
    elif sys.argv[1] == '--predict_from_url':
        predict_from_url(sys.argv[2])
    elif sys.argv[1] == '--predict_from_html':
        predict_from_html(sys.argv[2])
    return


if __name__ == '__main__':
    main()
