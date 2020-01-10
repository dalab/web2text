# AuthorExtractor

Source code for [Extracting the author of news stories with Machine Learning and DOM-based segmentation](https://medium.com/p/69225ea0e5c2/) 

## Introduction

This repository contains 

* Source code for [Web2Text](https://github.com/dalab/web2text), including additional features specific to the Author Extraction task
* Feature representations for +70,000 news articles from [All The News](https://www.kaggle.com/snapcrack/all-the-news) under `public/train_and_test` (23.39GB), in CSV format. The corresponding HTML files were not uploaded because it contains copyrighted material
* Train Model Task
* Weights from a pre-trained model with the above dataset (under `public/trained_model_all_the_news`)
* Inference Task

## Installation

1. Install [Scala and SBT](http://www.scala-sbt.org/download.html). The code was tested with SBT 0.31. You can also use Docker image `hseeberger/scala-sbt:8u222_1.3.3_2.13.1`.

2. Install Python 3 with Tensorflow, Keras, NumPy and HuggingFace Transformers. Running an Anaconda instance is recommended.

## Usage 
### Recipe: Extract Page Features of a single local HTML file:
`./extract_page_features.sh <html_file.html>`
(This will generate a CSV file)

### Recipe: Extract Page Features of an entire Corpus
Extract feature representations of all HTML files located in `public/html`: 
`./extract_corpus_features.sh`
(This will generate CSV files and store them under `public/train_and_test`)

Both the page and the corpus feature extraction generate a file named `/public/DOM/dom.html` which contains a visual DOM tree. This file is used for troubleshooting during implementation and on inference time. 

### Recipe: Train Model
Train the model with all the feature representations located in `public/train_and_test`:
`./train_model.sh`
The true labels are expected to be in `public/authors.csv`. The syntax of this file is (URL Hash, author name).
For clarity, all URLs of news articles are hashed.
This generates model files located under `public/trained_model_all_the_news`.

### Recipe: Inference from a local HTML file:
`./inference_from_csv <html_file.html>`

### Recipe: Inference from a local CSV file:
`./inference_from_html <csv_file.csv>`

### Recipe: Inference from a URL:
`./inference_from_url.sh <URL>`

All inference scripts are slow to run (about 3 minutes) due to the fact that there are several steps involved (load weights, load Tensorflow and BERT libraries) and a suboptimal switch back and forth between Scala and Python. 
