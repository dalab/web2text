import os, sys
import data_queue
import numpy as np
from data_queue import DataQueue
BATCH_SIZE = 5

def generate_queue(path):
    file_list = [f for f in os.listdir(path) if os.path.isfile(os.path.join(path, f))]
    file_sizes_list = []
    accum = 0
    for f in file_list:
        fn = os.path.join(path, f)
        print("fn:" + fn)
        with open(fn) as fd:
            content = fd.readlines()
            count_training_examples = content[0].count(',') +1
            accum += count_training_examples
            file_sizes_list.append([f, count_training_examples, accum])
        fd.close()
    print(file_sizes_list)
    print(file_sizes_list[1][1])
    return file_sizes_list

if __name__ == '__main__':
    train_path = "./trained_model_all_the_news_split/train"
    #file_list = generate_queue(train_path)
    dq = DataQueue(train_path,BATCH_SIZE)
    #arr = dq.get_data("./trained_model_all_the_news_split/train/output_7__0a0a5bddf78b2ef2de13c71fbf737764bbf97449_block_features.csv", 0,5)
    #arr2 = dq.get_data("./trained_model_all_the_news_split/train/output_7__0a0a5bddf78b2ef2de13c71fbf737764bbf97449_block_features.csv", 6,11)
    #print("arr: " + str(arr.shape))
    #print("arr2: " + str(arr2.shape))
    #arr3 = np.vstack([arr, arr2])
    #print("arr3: " + str(arr3.shape))
    arr0 = dq.takeOne()
    print("arr0.shape: " + str(arr0.shape))
    print("-----")
    arr1 = dq.takeOne()
    print("arr1.shape: " + str(arr1.shape))
    print("-----")
    arr2 = dq.takeOne()
    print("arr2.shape: " + str(arr2.shape))
    print("-----")
    arr3 = dq.takeOne()
    print("arr3.shape: " + str(arr3.shape))
