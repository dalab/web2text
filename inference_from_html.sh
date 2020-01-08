#!/bin/bash
if [ "$1" != "" ]; then
    python3 src/main/python/nn.py --predict_from_html $1
else
    echo "Positional parameter is empty. Usage: ./inference.sh <http://my.url/article> <output_prefix>"
fi
