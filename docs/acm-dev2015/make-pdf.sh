#!/bin/sh
rm *.aux *.blg *.bbl acm-dev.pdf *.log
pdflatex acm-dev 
bibtex acm-dev 
pdflatex acm-dev
pdflatex acm-dev
