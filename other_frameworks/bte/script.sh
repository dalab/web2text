#!/bin/bash

regex="[^/]*$"
for f in `ls /Users/thijs/dev/boilerplate2/src/main/resources/cleaneval/orig/*`
do
  # filename=`echo $f | rev | cut -d"/" -f1 | rev`
  ./bte.py $f
  echo $f
done


