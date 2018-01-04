import numpy as np

def softmax(x):
  x = np.exp(x - np.max(x, axis=-1, keepdims=True))
  x /= np.sum(x, axis=-1, keepdims=True)
  return x

def viterbi(unary, pairs, lam=1.0):
  # input: logits (n x 2 for unary and n-1 x 4 for pairs)

  unary = softmax(unary)
  pairs = softmax(pairs)

  n = unary.shape[0]
  P = np.zeros((n,2))
  C = np.zeros((n,2), dtype=np.int32)

  P[n-1,:] = np.log(unary[n-1,:]) # p(0), p(1)
  for i in range(n-2,-1,-1):
    t00, t01, t10, t11 = np.log(pairs[i,:])
    p0, p1             = np.log(unary[i,:])

    pc0 = [lam*t00 + P[i+1,0], lam*t01 + P[i+1,1]]
    pc1 = [lam*t10 + P[i+1,0], lam*t11 + P[i+1,1]]

    C[i,:] = (np.argmax(pc0), np.argmax(pc1)) # choices for the next one
    P[i,:] = (p0+np.max(pc0), p1+np.max(pc1)) # log probability

  res = np.zeros(n, dtype=np.int32)
  res[0] = np.argmax(P[0,:])
  next = C[0,res[0]]
  for i in range(1,n):
    res[i] = next
    next = C[i,next]
  return res

if __name__ == "__main__":
  # test
  unary = np.array([[1,1],[1,1]])
  pairwise = np.array([[2,1,0.6,0.1]])
  print(viterbi(unary, pairwise, lam=1))
