#!/bin/sh
rm *.aux *.blg *.bbl flightlogger.pdf *.log
pdflatex flightlogger 
bibtex flightlogger 
pdflatex flightlogger
pdflatex flightlogger
