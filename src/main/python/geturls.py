<<<<<<< HEAD
import hashlib
import urllib
import csv
import sys

inputFileName = "../resources/all_the_news_dataset/articles3.csv"
    
i = 0
=======
import csv
import sys
import hashlib
import urllib

inputFileName = "../resources/all_the_news_dataset/articles3.csv"
>>>>>>> e60ffe74ab9be9ba60321cd5d22fd3b9dbddf67a
csv.field_size_limit(sys.maxsize)
with open(inputFileName, 'r') as f:
  reader = csv.reader(f)
  a_line_after_header = next(reader)
  for row in reader:
<<<<<<< HEAD
    i=i+1
    url = row[8]
    outputFileName =  hashlib.sha1(url).hexdigest()

    if len(url) > 7 and i >= 34203:
      outputFileName =  hashlib.sha1(url).hexdigest()
      print("URL: " + url + ", Hash: " + outputFileName + ", i=" + str(i))
=======
    url = row[8]
    if len(url) > 7:
      outputFileName =  hashlib.sha1(url).hexdigest()
      print("URL: " + url + ", Hash: " + outputFileName)
>>>>>>> e60ffe74ab9be9ba60321cd5d22fd3b9dbddf67a
      urllib.urlretrieve(url, filename="../resources/all_the_news_dataset/html/" + outputFileName + ".html")
