from os import listdir
from os.path import isfile, join

def file_len(fname):
    with open(fname) as f:
        for i, l in enumerate(f):
            pass
    return i + 1

mypath = "/Users/cesc/Desktop/hypefactors/AuthorExtractor/src/main/resources/all_the_news_dataset/html"

onlyfiles = [f for f in listdir(mypath) if isfile(join(mypath, f))]

for f in onlyfiles:
 print(f + ", " + str(file_len(mypath + "/" + f)))



