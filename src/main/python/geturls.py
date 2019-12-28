import hashlib
import urllib
import csv
import sys

inputFileName = "../resources/all_the_news_dataset/articles3.csv"
    
i = 0
csv.field_size_limit(sys.maxsize)
with open(inputFileName, 'r') as f:
  reader = csv.reader(f)
  a_line_after_header = next(reader)
  for row in reader:
    i=i+1
    url = row[8]
    outputFileName =  hashlib.sha1(url).hexdigest()

    if len(url) > 7 and i >= 34203:
      outputFileName =  hashlib.sha1(url).hexdigest()
      print("URL: " + url + ", Hash: " + outputFileName + ", i=" + str(i))
      urllib.urlretrieve(url, filename="../resources/all_the_news_dataset/html/" + outputFileName + ".html")
