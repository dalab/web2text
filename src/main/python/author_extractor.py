from math import floor, ceil
import numpy as np
import os
import pandas as pd
import random
from sklearn.metrics import classification_report, confusion_matrix, precision_recall_fscore_support
import subprocess
import sys
import tensorflow as tf
from tensorflow import keras
import time
from transformers import pipeline
import wget

# BATCH_SIZE = 5000  # The number of training examples to use per training step
TRAINING_EPOCHS = 50
LEARNING_RATE = 0.0001
DROPOUT_RATE = 0.2
NUM_FEATURES = 141
NUM_FILES_TO_READ = 3000  # There are 76,968 files. 100 files take about 20 minutes using 5000 epochs
RANDOM_STATE = 1
SOURCE = "/Users/cesc/Desktop/hypefactors/AuthorExtractor"
MODEL_SAVE_FILE = '/public/trained_model_all_the_news/model.ckpt'
TRAIN_SIZE = 0.6
VALID_SIZE = 0.2
BATCH_SIZE = 50
FEATURE_COLUMNS = ["has_duplicate", "has_10_duplicates",
                   "n_same_class_path", "has_word", "log(n_words)",
                   "avg_word_length [3,15]runMain 2s", "has_stopword",
                   "contains_popular_name", "contains_author_particle",
                   "stopword_ratio", "log(n_characters) [2.5,5.5]",
                   "contains_punctuation", "n_punctuation [0,10]",
                   "log(punctuation_ratio)", "has_numeric",
                   "numeric_ratio", "log(avg_sentence_length) [2,5]",
                   "has_multiple_sentences", "relative_position",
                   "relative_position^2", "ends_with_punctuation",
                   "ends_with_question_mark", "contains_copyright",
                   "contains_email", "contains_url",
                   "contains_author_url", "contains_year",
                   "ratio_words_with_capital",
                   "ratio_words_with_capital^2",
                   "ratio_words_with_capital^3",
                   "contains_author", "has_p", "p_body_percentage",
                   "p_link_density", "p_avg_word_length [3,15]",
                   "p_has_stopword", "p_stopword_ratio",
                   "p_contains_popular_name",
                   "p_contains_author_particle",
                   "p_log(n_characters) [2.5,10]",
                   "p_log(punctuation_ratio)",
                   "p_has_numeric", "p_numeric_ratio",
                   "p_log(avg_sentence_length) [2,5]",
                   "p_ends_with_punctuation",
                   "p_ends_with_question_mark",
                   "p_contains_copyright", "p_contains_email",
                   "p_contains_url", "p_contains_author_url", "p_contains_year",
                   "p_ratio_words_with_capital",
                   "p_ratio_words_with_capital^2",
                   "p_ratio_words_with_capital^3",
                   "p_contains_form_element", "p_tag_td", "p_tag_div",
                   "p_tag_p", "p_tag_tr", "p_tag_table", "p_tag_body",
                   "p_tag_ul", "p_tag_span", "p_tag_li",
                   "p_tag_blockquote", "p_tag_b", "p_tag_small",
                   "p_tag_a", "p_tag_ol", "p_tag_ul (2)", "p_tag_i",
                   "p_tag_form", "p_tag_dl", "p_tag_strong",
                   "p_tag_pre", "has_gp", "gp_body_percentage",
                   "gp_link_density", "gp_avg_word_length [3,15]",
                   "gp_has_stopword", "gp_stopword_ratio",
                   "gp_contains_popular_name",
                   "gp_contains_author_particle",
                   "gp_log(n_characters) [2.5,10]",
                   "gp_log(punctuation_ratio)",
                   "gp_has_numeric", "gp_numeric_ratio",
                   "gp_log(avg_sentence_length) [2,5]",
                   "gp_ends_with_punctuation",
                   "gp_ends_with_question_mark",
                   "gp_contains_copyright",
                   "gp_contains_email", "gp_contains_url", "gp_contains_author_url",
                   "gp_contains_year", "gp_ratio_words_with_capital",
                   "gp_ratio_words_with_capital^2",
                   "gp_ratio_words_with_capital^3",
                   "gp_contains_form_element", "root_body_percentage",
                   "root_link_density", "root_avg_word_length [3,15]",
                   "root_has_stopword", "root_stopword_ratio",
                   "root_contains_popular_name",
                   "root_contains_author_particle",
                   "root_log(n_characters) [2.5,10]",
                   "root_log(punctuation_ratio)",
                   "root_has_numeric", "root_numeric_ratio",
                   "root_log(avg_sentence_length) [2,5]",
                   "root_ends_with_punctuation",
                   "root_ends_with_question_mark",
                   "root_contains_copyright", "root_contains_email",
                   "root_contains_url", "root_contains_author_url", "root_contains_year",
                   "root_ratio_words_with_capital",
                   "root_ratio_words_with_capital^2",
                   "root_ratio_words_with_capital^3",
                   "root_contains_form_element", "tag_a", "tag_p",
                   "tag_td", "tag_b", "tag_li", "tag_span", "tag_i",
                   "tag_tr", "tag_div", "tag_strong", "tag_em",
                   "tag_h3", "tag_h2", "tag_table", "tag_h4",
                   "tag_small", "tag_sup", "tag_h1", "tag_blockquote"]


# def load_data(file_list):
#    start_time = time.process_time()
#    df = load_csv(file_list)
#    df_x = df.drop(columns=['contains_author'])
#    df_y = pd.get_dummies(df['contains_author'])
#    print(f"Read {NUM_FILES_TO_READ} training and test files: {time.process_time() - start_time} seconds")
#    return df_x, df_y


def get_files(path, batch_load=True):
    if batch_load:
        file_list = [os.path.join(path, f) for f in os.listdir(path) if (os.path.isfile(os.path.join(path, f))
                                                                         and os.path.join(path, f).endswith(".csv"))]
        file_list = file_list[:NUM_FILES_TO_READ]
    else:
        file_list = [path]
    return file_list


def load_csv(file):
    #print(f"importing {file}")
    full_df = pd.read_csv(file, sep=",", header=None).transpose()
    full_df.columns = FEATURE_COLUMNS
    full_df_x = full_df.drop(columns=['contains_author'])
    full_df['contains_author'] = pd.Categorical(full_df['contains_author'], categories=[-1.0, 1.0])
    full_df_y = pd.get_dummies(full_df['contains_author'])
    full_df_y.columns = ["dummy_0", "dummy_1"]
    return full_df_x, full_df_y


def predict_from_html(html_file):
    path = SOURCE + "/public/inference"
    subprocess.run([SOURCE + '/extract_page_features.sh', html_file, path + "/test"])
    predict_from_csv(path + "/test.csv", html_file, path + "/predict")
    return


def predict_from_url(url):
    path = SOURCE + "/public/inference"
    os.system('rm ' + path + '/*')
    html_file = wget.download(url, out=path)
    predict_from_html(html_file)
    return


def get_html_chunk(dom_seq, html_file):
    with open("/Users/cesc/Desktop/hypefactors/AuthorExtractor/public/dom/dom.html") as fd:
        lines = fd.readlines()
        i = 0
        for line in lines:
            if "<dt>nChildrenDeep</dt><dd>0</dd>" in line:
                i += 1
            if "startPosition" in line:
                if i == dom_seq:
                    start_position = int(line.split("<dd>")[1].split("</dd>")[0])
            if "endPosition" in line:
                if i == dom_seq:
                    end_position = int(line.split("<dd>")[1].split("</dd>")[0])
    print(f"i={i}, start_position={start_position}, end_position={end_position}")
    i = 0
    chunk = ""
    with open(html_file) as fd:
        while True:
            c = fd.read(1)
            i += 1
            if not c:
                break
            else:
                if start_position < i <= end_position:
                    chunk += c
    return chunk


def predict_from_csv(csv_file, html_file, predict_suffix):
    # file_list = get_files(csv_file, True)
    pred_df,_ = load_csv(csv_file)
    #pred_df = pred_df.drop(columns=['contains_author'])
    model = tf.keras.models.load_model(SOURCE + MODEL_SAVE_FILE)
    pred_y = model.predict(pred_df)
    pred_y_argmax = np.argmax(pred_y, axis=1)
    pred_y_argmax_2 = np.argmax(pred_y_argmax, axis=0)
    print(f"Node predicted: {pred_y_argmax_2}")
    subprocess.run([SOURCE + '/extract_page_features.sh',
                    html_file, predict_suffix])
    if pred_y_argmax_2 == 0:
        print("No author name predicted")
    else:
        html_chunk = get_html_chunk(pred_y_argmax_2, html_file)
        ner = pipeline('ner')
        y = ner(html_chunk)
        # print(y)
        # for named_entity in y:
        #
        #
        print(f"The Author Name is: {html_chunk}. NER prediction: {y}")
    return


def create_model():
    model = tf.keras.models.Sequential([
        keras.layers.Dense(NUM_FEATURES-1, activation='relu', input_shape=(NUM_FEATURES-1,)),
        keras.layers.Dropout(DROPOUT_RATE),
        keras.layers.Dense(2, activation='softmax')]
    )
    model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])
    return model


def batch_generator(files_list):
    pd.set_option('display.max_rows', None)
    pd.set_option('display.max_columns', None)
    while True:
        for i in range(len(files_list)):
            x, y = load_csv(files_list[i])
            if i % BATCH_SIZE == 0:
                df_x = x
                df_y = y
            else:
                df_x = pd.concat([df_x, x])
                df_y = pd.concat([df_y, y])
            if i % BATCH_SIZE == BATCH_SIZE - 1 or i == len(files_list) - 1:
                # print(f"Let's see,  df_y={df_y}, columns of y={list(df_y.columns)}")
                yield df_x, df_y


def load_data(files_list):
    for i in range(len(files_list)):
        x, y = load_csv(files_list[i])
        if i == 0:
            df_x, df_y = x, y
        else:
            df_x = pd.concat([df_x, x])
            df_y = pd.concat([df_y, y])
    return df_x, df_y


def train_keras(csv_folder):
    file_list = get_files(csv_folder)
    train_cnt = floor(len(file_list) * TRAIN_SIZE)
    valid_cnt = floor(len(file_list) * VALID_SIZE)
    print(f"train_cnt={train_cnt}, valid_cnt={valid_cnt}")
    file_list_train = file_list[0:train_cnt]
    file_list_valid = file_list[train_cnt:train_cnt + valid_cnt]
    file_list_test = file_list[train_cnt + valid_cnt:len(file_list)]
    model = create_model()
    # (train_x.shape[1])
    model.summary()
    cp_callback = tf.keras.callbacks.ModelCheckpoint(filepath=SOURCE + MODEL_SAVE_FILE,
                                                     save_weights_only=False,
                                                     verbose=1)
    train_generator = batch_generator(file_list_train)
    valid_generator = batch_generator(file_list_valid)
    _ = model.fit(x=train_generator, epochs=TRAINING_EPOCHS,
                  steps_per_epoch=len(file_list_train)//BATCH_SIZE,
                  workers=1, use_multiprocessing=False, validation_data=valid_generator,
                  validation_steps=len(file_list_valid)//BATCH_SIZE, callbacks=[cp_callback])
    test_x, test_y = load_data(file_list_test)
    loss, accuracy = model.evaluate(test_x, test_y)
    print(f"Loss={loss}, Accuracy={100 * accuracy}")
    pred_y = model.predict(test_x)
    rep = classification_report(np.argmax(test_y.to_numpy(), axis=1), np.argmax(pred_y, axis=1), digits=4)
    print(rep)
    print(confusion_matrix(np.argmax(test_y.to_numpy(), axis=1), np.argmax(pred_y, axis=1)))
    model.save(SOURCE + MODEL_SAVE_FILE)
    return


def main():
    os.environ['PYTHONHASHSEED'] = str(RANDOM_STATE)
    random.seed(RANDOM_STATE)
    np.random.seed(RANDOM_STATE)
    tf.random.set_seed(RANDOM_STATE)
    if len(sys.argv) < 2:
        exit(0)
    elif len(sys.argv) == 4:
        if sys.argv[1] == '--predict_from_csv':
            predict_from_csv(sys.argv[2], sys.argv[3], sys.argv[4])
            exit()
    elif sys.argv[1] == '--train_from_folder':
        train_keras(sys.argv[2])
        exit()
    elif sys.argv[1] == '--predict_from_url':
        predict_from_url(sys.argv[2])
        exit()
    elif sys.argv[1] == '--predict_from_html':
        predict_from_html(sys.argv[2])
        exit()
    return


if __name__ == '__main__':
    main()
