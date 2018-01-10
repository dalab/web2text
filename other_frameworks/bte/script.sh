#!/bin/bash

PROJECT=...

regex="[^/]*$"
for f in `ls $PROJECT/data/cleaneval/orig/*`
do
  # filename=`echo $f | rev | cut -d"/" -f1 | rev`
  ./bte.py $f
  echo $f
done


