#import re
import csv
import sys

inputFileName = "./articles2.csv"
outputFileName = "./names.txt"

exceptionList = ["The New York Times Magazine", "The Associated Press", "The New York Times", "Breitbart News",\
                 "Breitbart Jerusalem", "Breitbart London", "AP", "Breitbart TV", "The Editors"]

treatmentList = ["Dr.", "Dr ", "Judge ", "Ph.D.", "Ph D ", " M D", ", MD", " (R-TX)", "(R-KY)", "Lt. ", "Gen. ", "(Ret.)", "Sen. ", "Assemblyman ", "Congressman ", "Senator ", "President "]
#TODO - Code to split with this
#authorSplitList = [", ", " and", " with", " &amp"]

csv.field_size_limit(sys.maxsize)

with open(inputFileName, 'r') as f:
  reader = csv.reader(f)
  i=0
  for row in reader:
    i = i+1
    if i > 0 and i < 60000:
        authors1 = row[4].split(", ")
        for auth1 in authors1:
          authors2 = auth1.split("and ")
          for auth2 in authors2:
            authors3 = auth2.split("with ")
            for auth3 in authors3:
              if not auth3 in exceptionList:
                auth4 = auth3
                for treatment in treatmentList:
                  auth4 = auth4.replace(treatment, "")
                if len(auth4) > 2 and " " in auth4:
                  #Remove leading and trailing spaces
                  print auth4.strip()
