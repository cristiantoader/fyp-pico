pdflatex main -output-directory _output
bibtex _output/main
pdflatex main -output-directory _output
pdflatex main -output-directory _output

pause