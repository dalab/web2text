import numpy as np
import os, sys

NUM_FEATURES = 140

class DataQueue:
    """Initialize a ShuffleQueue with a list, and from that point onwards you
       can take elements from it"""

    def get_size(self):
        return self.accum

    def get_num_files(self):
        return len(self.file_sizes_list)

    def __init__(self,path, batch_size):
        file_list = [os.path.join(path, f) for f in os.listdir(path) if os.path.isfile(os.path.join(path, f))]
        self.file_sizes_list = []
        self.file_num = 0
        self.batch_size = batch_size
        self.data_so_far = 0
        self.file_indx = 0
        self.accum = 0
        for f in file_list:
            with open(f) as fd:
                content = fd.readlines()
                count_training_examples = content[0].count(',') +1
                self.accum += count_training_examples
                self.file_sizes_list.append([f, count_training_examples, self.accum])
            fd.close()


    def get_data(self, file_name, idx_start, idx_end):
        is_first = True
        with open(file_name) as fd:
            content = fd.readlines()
            for line in content:
                line_split = line.replace("\n","").split(",")
                B= line_split[idx_start:idx_end]
                if is_first:
                    A = B
                    is_first = False
                else:
                    A = np.vstack([A, B])
        return np.transpose(A)

    def takeOne(self):
        first_part = True
        self.data_so_far=0
        data_to_return = np.zeros(NUM_FEATURES,)
        found = False
        while found == False:
            elem = self.file_sizes_list[self.file_num]
            if self.file_indx+self.batch_size<=elem[2]:
                i = self.batch_size-(self.data_so_far) % self.batch_size
                batch_data = self.get_data(elem[0], self.file_indx, self.file_indx+i)
                self.data_so_far+=i #self.batch_size
                self.file_indx+=self.batch_size
                print("(1) file_indx=" + str(self.file_indx))
                print("The shape is: " + str(batch_data.shape))
                found = True
            else:
                batch_data = self.get_data(elem[0],self.file_indx,elem[2]) # ?
                self.data_so_far+=elem[2]-self.file_indx
                self.file_indx=0
                print("(2) file_indx=" + str(self.file_indx))
                print("The shape is: " + str(batch_data.shape))
                self.file_num+=1
            if first_part:
                data_to_return = batch_data
                first_part = False
            else:
                data_to_return = np.vstack([data_to_return,batch_data])

        train_labels = data_to_return[:,122] # This row mark which node contains the author and is used for training
        train_labels = train_labels.reshape(train_labels.shape+(1,))
        train_data = np.delete(data_to_return,[29,53,97,120],1) # Data not used for training (containsAuthor at the parent, grandparent and root level)
        return train_data,train_labels