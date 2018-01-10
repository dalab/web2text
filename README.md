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

```scala
import ch.ethz.dalab.web2text.alignment.Alignment
val reference = "keep this"
val source = "You should keep this text"
val alignment: String = Alignment.alignment(source, reference) 
println(alignment) // □□□□□□□□□□□keep this□□□□□
```
### Extracting features for CleanEval

```scala
import ch.ethz.dalab.web2text.utilities.Util
import ch.ethz.dalab.web2text.cleaneval.CleanEval
import ch.ethz.dalab.web2text.output.CsvDatasetWriter

val data = Util.time{ CleanEval.dataset(fe) }

// Write block_features.csv and edge_features.csv
// Format of a row: page id, groundtruth label (1/0), features ...
CsvDatasetWriter.write(data, "./src/main/python/data")

// Print the names of the exported features in order
println("# Block features")
fe.blockExtractor.labels.foreach(println)
println("# Edge features")
fe.edgeExtractor.labels.foreach(println)
```

### Training the CNNs

Code related to the CNNs lives in the `src/main/python` directory. 

To train the CNNs:

1. Set the `CHECKPOINT_DIR` variable in `main.py`.
2. Make sure the files `block_features.csv` and `edge_features.csv` are in the `src/main/python/data` directory. Use the example from the previous section for this.
3. Convert the CSV files to `.npy` with `data/convert_scala_csv.py`.
3. Train the unary CNN with `python3 main.py train_unary`.
4. Train the pairwise CNN with `python3 main.py train_edge`.

### Evaluating the CNN

To evaluate the CNN:

1. Set the `CHECKPOINT_DIR` variable in `main.py` to point to a directory with trained weights. We provide trained weights based on the cleaneval split and a custom web2text split (with more training data.)
2. Run `python3 main.py test_structured` to test performance on the CleanEval test set.

The performance of other networks is computed in Scala:

```scala
import ch.ethz.dalab.web2text.Main
Main.evaluateOthers()
```
