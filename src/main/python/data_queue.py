import numpy as np
import os, sys

class DataQueue:
    """Initialize a ShuffleQueue with a list, and from that point onwards you
       can take elements from it"""

    def get_size(self):
        return self.accum

    def get_num_files(self):
        return len(self.file_sizes_list)

    def get_headers(self):
        return self.headers

    def load_headers(self):
        headers = {}
        with open("column_names.txt") as fd:
            i=0
            content = fd.readlines()
            for lin in content:
                headers[lin.replace("\n", "")] = i
                i+=1
        fd.close
        return headers

    def __init__(self,path, num_features, batch_size, skip_files, num_files):
        file_list = [os.path.join(path, f) for f in os.listdir(path) if os.path.isfile(os.path.join(path, f))]
        self.file_sizes_list = []
        self.file_num = 0
        self.batch_size = batch_size
        self.data_so_far = 0
        self.file_indx = 0
        self.accum = 0
        self.num_features=num_features
        self.skip_files=skip_files
        self.num_files=num_files
        self.headers = self.load_headers()
        i=0
        for f in file_list:
            i+=1
            if skip_files < i <= skip_files+num_files:
                with open(f) as fd:
                        content = fd.readlines()
                        count_training_examples = content[0].count(',') +1
                        self.accum += count_training_examples
                        self.file_sizes_list.append([f, count_training_examples, self.accum])
                        print(f"i={i}, acum={self.accum}, file={f}")
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

        #if self.file_num >= self.num_files:
        #    return np.zeros(self.num_features,), np.zeros(self.num_features,)
        found = False
        while not found:
            elem = self.file_sizes_list[self.file_num]
            if self.file_indx+self.batch_size-self.data_so_far<elem[1]:
                #i = self.batch_size-(self.data_so_far) % self.batch_size
                batch_data = self.get_data(elem[0], self.file_indx, self.file_indx+self.batch_size-self.data_so_far)
                self.data_so_far+=self.batch_size
                self.file_indx+=self.batch_size
                #print(f"(1) file_num={self.file_num},{elem[0]},file_indx={self.file_indx},data_so_far={self.data_so_far},batch_data.shape={batch_data.shape}")
            elif self.file_indx+self.batch_size-self.data_so_far>=elem[1]:
                batch_data = self.get_data(elem[0], self.file_indx, elem[1]-self.data_so_far)
                self.data_so_far+=self.batch_size
                self.file_indx=0
                self.file_num+=1
                #print(f"(2) file_num={self.file_num},{elem[0]},file_indx={self.file_indx},data_so_far={self.data_so_far},batch_data.shape={batch_data.shape}")
            if self.data_so_far==self.batch_size or self.data_so_far>=self.get_size() or self.file_num>=self.num_files:
                found = True
            if first_part:
                data_to_return = batch_data
                first_part = False
            else:
                #print(f"going to vstack {data_to_return.shape} with {batch_data.shape}")
                data_to_return = np.vstack((data_to_return,batch_data))
            #print(f"data_to_return.shape={data_to_return.shape}")

        #print(f"headers: {self.headers}")
        train_labels = data_to_return[:,self.headers["contains_author"]]
        train_labels = train_labels.reshape(train_labels.shape+(1,))
        train_data = np.delete(data_to_return,[self.headers["contains_author"]],1)
        return train_data,train_labels