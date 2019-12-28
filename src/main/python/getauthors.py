import hashlib
import csv
import sys

inputFileName = "../resources/all_the_news_dataset/articles2.csv"
i = 0
csv.field_size_limit(sys.maxsize)
with open(inputFileName, 'r') as f:
  reader = csv.reader(f)
  a_line_after_header = next(reader)
  print("Hash , Author")
  for row in reader:
    i=i+1
    url = row[8]
    author = row[4]
    if len(url) > 7 and len(author) > 3: # and i >= 34203:
      outputFileName =  hashlib.sha1(url).hexdigest()
      print(outputFileName + ", " + author)
