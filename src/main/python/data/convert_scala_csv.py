#!/usr/bin/env python

"""Converts Scala output for block_features to Python binary format"""

import click
import numpy as np


@click.command()
@click.argument("block_csv", type=click.Path(exists=True), default='block_features.csv')
@click.argument("edge_csv", type=click.Path(exists=True), default='edge_features.csv')
@click.argument("npy_output", type=click.Path(exists=False), default='block_features.npy')

def cli(block_csv, edge_csv, npy_output):

  click.echo("Converting '%s' and '%s' to '%s'" % (block_csv, edge_csv, npy_output))

  data = np.genfromtxt(block_csv, delimiter=',')
  edge_data = np.genfromtxt(edge_csv, delimiter=',')
  click.echo("- CSV file loaded")

  def docs(data):
    i = 0
    n = data.shape[0]
    cur_doc_id = data[i,0]
    start = 0
    edge_start = 0
    while i < n:
      if i+1 == n or data[i+1,0] != cur_doc_id:
        labs = data[start:i+1,1] # 0 / 1
        edge_labs = np.zeros(i-start)
        for j in range(0,i-start):
          edge_labs[j] = {
            (0., 0.): 0,
            (0., 1.): 1,
            (1., 0.): 2,
            (1., 1.): 3
          }[labs[j], labs[j+1]]

        yield {
          'data': data[start:i+1, 2:],
          'edge_data': edge_data[edge_start:edge_start+i-start, 2:],
          'labels': labs,
          'edge_labels': edge_labs
        }
        cur_doc_id = data[i+1,0] if i+1 < n else 0.0
        edge_start += i-start
        start = i+1
      i += 1

  documents = np.array(list(docs(data)), dtype=object)
  click.echo("- Data converted into document format: list({data, edge_data, labels, edge_labels})")

  np.save(npy_output, documents)
  click.echo("- Done")


if __name__ == '__main__':
  cli()