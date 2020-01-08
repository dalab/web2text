#!/bin/bash
if [ "$1" != "" ] && [ "$2" != "" ] && [ "$3" != "" ]; then
    python3 src/main/python/nn.py --predict_from_csv $1 $2 $3
else
    echo "Positional parameter is empty. Usage: ./inference.sh <full_path_to_feature_file.csv> <full_path_to_original_file.html> <output_prefix>"
fi
