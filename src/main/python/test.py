from data_queue import DataQueue
BATCH_SIZE = 5

if __name__ == '__main__':
    train_path = "./trained_model_all_the_news_split/train"
    dq = DataQueue(train_path,BATCH_SIZE)
    size = dq.get_size()

    print("acum: " + str(size))

    arr0_data, arr0_labels = dq.takeOne()
    print(f"arr0_data.shape: {arr0_data.shape}, arr0_labels: {arr0_labels.shape}")
    print("arr0_labels:" + str(arr0_labels))

    arr1_data, arr1_labels = dq.takeOne()
    print(f"arr1_data.shape: {arr1_data.shape}, arr1_labels: {arr1_labels.shape}")

    arr2_data, arr2_labels = dq.takeOne()
    print(f"arr2_data.shape: {arr2_data.shape}, arr2_labels: {arr2_labels.shape}")
    print("arr2_labels:" + str(arr2_labels))

    arr3_data, arr3_labels = dq.takeOne()
    print(f"arr3_data.shape: {arr3_data.shape}, arr3_labels: {arr3_labels.shape}")