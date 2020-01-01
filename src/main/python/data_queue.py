import numpy as np
import os, sys

BATCH_SIZE = 7 # The number of training examples to use per training step.
NUM_FEATURES = 140

class DataQueue:
    """Initialize a ShuffleQueue with a list, and from that point onwards you
       can take elements from it"""

    def __init__(self,path, batch_size):
        file_list = [os.path.join(path, f) for f in os.listdir(path) if os.path.isfile(os.path.join(path, f))]
        self.file_sizes_list = []
        self.file_num = 0
        self.batch_size = BATCH_SIZE
        self.data_so_far = 0
        self.file_indx = 0
        accum = 0
        for f in file_list:
            with open(f) as fd:
                content = fd.readlines()
                count_training_examples = content[0].count(',') +1
                accum += count_training_examples
                self.file_sizes_list.append([f, count_training_examples, accum])
            fd.close()

    def get_data(self, file_name, idx_start, idx_end):
        #A = np.zeros((idx_end-idx_start,))
        is_first = True
        with open(file_name) as fd:
            content = fd.readlines()
            for line in content:
                line_split = line.split(",")
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
        print("hello. data_so_far=" + str(self.data_so_far) + ", file_num=" + str(self.file_num))
        found = False
        #while self.data_so_far <self.batch_size*(self.file_num+1):
        while found == False:
            elem = self.file_sizes_list[self.file_num]
            print("Vamos por el bloque " + str(self.file_num) + ", file:" + elem[0] +\
                  ", file size=" + str(elem[2])  + ", self.batch_size=" +\
                  str(self.batch_size))
            #if self.data_so_far+self.batch_size<elem[2]:
            if self.file_indx+self.batch_size<=elem[2]:
                print("caso 1 - Llegamos al final en este mismo bloque")

                #if self.file_num==0:
                #if self.file_indx==0 or self.file_num==0:
                #    i = self.batch_size
                #else:
                #    i = 1+(self.data_so_far +self.batch_size) % self.batch_size
                i = self.batch_size-(self.data_so_far) % self.batch_size
                batch_data = self.get_data(elem[0], self.file_indx, self.file_indx+i)
                self.data_so_far+=i #self.batch_size
                self.file_indx+=self.batch_size
                print("(1) file_indx=" + str(self.file_indx))
                print("The shape is: " + str(batch_data.shape))
                found = True
            else:
                print("caso 2 - El bloque no da.., hay que ir al siguiente.")
                batch_data = self.get_data(elem[0],self.file_indx,elem[2]) # ?
                self.data_so_far+=elem[2]-self.file_indx
                self.file_indx=0
                print("(2) file_indx=" + str(self.file_indx))
                print("The shape is: " + str(batch_data.shape))
                self.file_num+=1
            if first_part:
                print("es un first_part")
                data_to_return = batch_data
                first_part = False
            else:
                print("Pepe - data_to_return.shape=" + str(data_to_return.shape) + ", batch_data.shape=" + str(batch_data.shape))
                data_to_return = np.vstack([data_to_return,batch_data])
        return data_to_return