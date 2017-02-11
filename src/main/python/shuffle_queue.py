from copy import copy
import numpy as np
import random

class ShuffleQueue:
  """Initialize a ShuffleQueue with a list, and from that point onwards you
     can take elements from it, receiving them in random order.
     When the list is exhausted, it is reshuffled."""

  def __init__(self, list):
    self.length = len(list)
    if self.length == 0:
      raise ValueError("Please pas a non-empty list to ShuffleQueue")
    self.data = copy(list)
    self.reshuffle()
    self.pos = 0

  def reshuffle(self):
    random.shuffle(self.data)

  def takeOne(self):
    elem = self.data[self.pos]
    self.pos = self.pos + 1
    if self.pos >= self.length:
      self.pos -= self.length
      self.reshuffle()
    return elem

  def take(self, count=1):
    elems = [None]*count
    for i in xrange(count):
      elems[i] = (self.takeOne())
    return elems
