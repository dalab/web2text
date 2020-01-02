import csv
import sys
import hashlib
import urllib

inputFileName = "../resources/all_the_news_dataset/articles3.csv"
csv.field_size_limit(sys.maxsize)
with open(inputFileName, 'r') as f:
  reader = csv.reader(f)
  a_line_after_header = next(reader)
  for row in reader:

    url = row[8]
    if len(url) > 7:
      outputFileName =  hashlib.sha1(url).hexdigest()
      print("URL: " + url + ", Hash: " + outputFileName)
      urllib.urlretrieve(url, filename="../resources/all_the_news_dataset/html/" + outputFileName + ".html")
