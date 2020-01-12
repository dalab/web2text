#!/bin/bash
if [ "$1" != "" ]; then
    python3 src/main/python/author_extractor.py --predict_from_url $1
else
    echo "Positional parameter is empty. Usage: ./inference.sh <http://my.url/article>"
fi
