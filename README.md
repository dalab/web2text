# Web2Text

Source code for [Web2Text: Deep Structured Boilerplate Removal](https://arxiv.org/abs/1801.02607), full paper at ECIR '18 

## Introduction

This repository contains 

* Scala code to parse an (X)HTML document into a DOM tree, convert it to a CDOM tree, interpret tree leaves as a sequence of text blocks and extract features for each of these blocks. 

* Python code to train and evaluate unary and pairwise CNNs on top of these features. Inference on the hidden Markov model based on the CNN output potentials can be executed using the provided implementation of the Viterbi algorithm.

* The [CleanEval](https://cleaneval.sigwac.org.uk) dataset under `src/main/resources/cleaneval/`:
    - `orig`: raw pages
    - `clean`: reference clean pages
    - `aligned`: clean content aligned with the corresponding raw page on a per-character basis using the alignment algorithm described in our paper

* Output from various other webpage cleaners on CleanEval under `other_frameworks/output`:
    - [Body Text Extractor](https://www.researchgate.net/publication/2376126_Fact_or_fiction_Content_classification_for_digital_libraries) (Finn et al., 2001)
    - [Boilerpipe](https://github.com/janih/boilerpipe) (Kohlschütter et al., 2010): default-extractor, article-extractor, largestcontent-extractor
    - [Unfluff](https://github.com/ageitgey/node-unfluff) (Geitgey, 2014)
    - [Victor](https://pdfs.semanticscholar.org/5462/d15610592394a5cd305d44003cc89630f990.pdf) (Spousta et al., 2008)



## Installation

1. Install [Scala and SBT](http://www.scala-sbt.org/download.html). The code was tested with SBT 0.31.

2. Install Python 3 with Tensorflow and NumPy.


## Usage

### HTML to CDOM

In Scala:

```scala
import ch.ethz.dalab.web2text.cdom.CDOM
val cdom = CDOM.fromHTML("""
    <body>
        <h1>Header</h1>
        <p>Paragraph with an <i>Italic</i> section.</p>
    </body>
    """)
println(cdom)
```

### Feature extraction

Example:
```scala
import ch.ethz.dalab.web2text.features.{FeatureExtractor, PageFeatures}
import ch.ethz.dalab.web2text.features.extractor._

val unaryExtractor = 
    DuplicateCountsExtractor
    + LeafBlockExtractor
    + AncestorExtractor(NodeBlockExtractor + TagExtractor(mode="node"), 1)
    + AncestorExtractor(NodeBlockExtractor, 2)
    + RootExtractor(NodeBlockExtractor)
    + TagExtractor(mode="leaf")

val pairwiseExtractor = 
    TreeDistanceExtractor + 
    BlockBreakExtractor + 
    CommonAncestorExtractor(NodeBlockExtractor)

val extractor = FeatureExtractor(unaryExtractor, pairwiseExtractor)

val features: PageFeatures = extractor(cdom)

println(features)
```

### Aligning cleaned text with original source

@TODO

### Extracting features for CleanEval

@TODO

### Training the CNNs

@TODO

### Evaluating the CNN

@TODO
