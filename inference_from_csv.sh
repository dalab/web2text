#!/bin/bash
if [ "$1" != "" ]; then
    python3 src/main/python/author_extractor.py --predict_from_csv $1
else
    echo "Positional parameter is empty. Usage: ./inference.sh <full_path_to_csv_file.csv>
fi
