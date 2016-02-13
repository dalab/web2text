# $Id: Makefile 382 2008-02-26 20:42:30Z michal $

all: wac3.dvi wac3.ps wac3.pdf bc.dvi bc.ps bc.pdf

wac3.dvi wac3.pdf: biblio.bib wac3.bst wac3.sty

bc.dvi bc.pdf: biblio.bib

%.dvi: %.tex
	rm -f $*.aux
	latex $*
	bibtex $*
	latex $*
	latex $*

.dvi.ps:
	dvips $<

%.pdf: %.tex
	rm -f $*.aux
	pdflatex $*
	bibtex $*
	pdflatex $*
	pdflatex $*


# graphics
bc.pdf: logo.pdf sigwac.png

bc.dvi: logo.eps sigwac.eps

logo.pdf: logo.eps
	epstopdf $<

logo.eps: logo-orig.eps
	eps2eps $< $@

TMPFILES = *.log *.aux *.out *.toc *.bbl *.blg logo.eps logo.pdf

tmp-clean:
	rm -f $(TMPFILES)

clean:
	rm -f $(TMPFILES) *.dvi *.pdf *.ps

.SUFFIXES: .dvi .ps

# a failed latex run leaves the .dvi file, so that a subsequent make
# won't run latex again
.DELETE_ON_ERROR:
