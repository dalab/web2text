#!/bin/bash
if [ "$1" != "" ]; then
    python3 src/main/python/author_extractor.py --predict_from_html $1
else
    echo "Positional parameter is empty. Usage: ./inference.sh <full_path_to_html_file.html>
fi
