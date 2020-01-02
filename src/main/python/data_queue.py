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

        # Line 31: Author Name Label
        train_labels = data_to_return[:,30] # This row mark which node contains the author and is used for training
        train_labels = train_labels.reshape(train_labels.shape+(1,))
        #train_data = np.delete(data_to_return,[23,29,53,97,120],1) # Data not used for training (containsAuthor at the parent, grandparent and root level)
        train_data = np.delete(data_to_return,[30],1) # Data not used for training (containsAuthor at the parent, grandparent and root level)
        return train_data,train_labels

"""
#Duplicate counts
    1, "has_duplicate",
    2, "has_10_duplicates",
    3, "n_same_class_path"
#Leaf
    4, "has_word",
    5, "log(n_words)",
    6, "avg_word_length [3,15]",
    7, "has_stopword",
    8, "contains_popular_name",
    9, "contains_author_particle",
    10, "stopword_ratio",
    11, "contains_popular_name",
    12, "log(n_characters) [2.5,5.5]",
    13, "contains_punctuation",
    14, "n_punctuation [0,10]",
    15, "log(punctuation_ratio)",
    16, "has_numeric",
    17, "numeric_ratio",
    18, "log(avg_sentence_length) [2,5]",
    19, "has_multiple_sentences",
    20, "relative_position",
    21, "relative_position^2",
    22, "ends_with_punctuation",
    23, "ends_with_question_mark",
    24, "contains_copyright",
    25, "contains_email",
    26, "contains_url",
    27, "contains_year",
    28, "ratio_words_with_capital",
    29, "ratio_words_with_capital^2",
    30, "ratio_words_with_capital^3",
    31, "contains_author"
#Ancestor
    32, "body_percentage",
    33, "link_density",
    34, "avg_word_length [3,15]",
    35, "has_stopword",
    36, "stopword_ratio",
    37, "contains_popular_name",
    38, "contains_author_particle",
    39, "log(n_characters) [2.5,10]",
    40, "log(punctuation_ratio)",
    41, "has_numeric",
    42, "numeric_ratio",
    43, "log(avg_sentence_length) [2,5]",
    44, "ends_with_punctuation",
    45, "ends_with_question_mark",
    46, "contains_copyright",
    47, "contains_email",
    48, "contains_url",
    49, "contains_year",
    50, "ratio_words_with_capital",
    51, "ratio_words_with_capital^2",
    52, "ratio_words_with_capital^3",
    53, "contains_form_element",
    
    
"""