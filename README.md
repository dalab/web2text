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

### Recipe: Extract Page Features

`./extract_page_features.sh`

### Recipe: Train Model

`./train_model.sh`

### Recipe: Inference

`./inference.sh`
