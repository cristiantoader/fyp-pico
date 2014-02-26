# MARF LaTeX Documentation
# Use with GNU make
#
# Copyright (c) 2002-2003 The MARF Development Group
#
# $Header: /cvsroot/marf/marf/doc/src/tex/Makefile,v 1.13 2003/02/10 08:52:36 mokhov Exp $

#
# Settings
#

#PDFLATEXFLAGS = --interaction=batchmode --file-line-error-style
PDFLATEXFLAGS = --interaction=batchmode

APPDIR = ../../../../apps/SpeakerIdentApp
APPFILES = SpeakerIdentApp.tex SpeakersIdentDb.tex testing-sh.tex

#
# Build
#

all: report

report: report.tex \
        architecture.tex \
        preprocessing \
        feature-extraction \
        classification \
        references.tex \
        app \
        todo
	date > date.tex
	@echo "Compiling *.tex files..."
	pdflatex $(PDFLATEXFLAGS) report.tex
	@echo "Updating cross-references..."
	pdflatex $(PDFLATEXFLAGS) report.tex

preprocessing: \
        preprocessing.tex \
        fft-filter.tex \
        high-frequency-boost.tex \
        high-pass.tex \
        low-pass.tex \
        bandpass-filter.tex

feature-extraction: \
        feature-extraction.tex \
        fft.tex \
        lpc.tex \
        random-feature-extraction.tex

classification: \
        classification.tex \
        chebyshev-distance.tex \
        euclidean-distance.tex \
        minkowski-distance.tex \
        mahalanobis-distance.tex \
        random-classification.tex \
        nnet.tex

# Make up app's tex files from the source
# For the time being there's a hack to replace tabs with 4 spaces because
# LaTeX's verbatim mode seems to ignore them.

# NOTE: this requires presence of the application sources

app: $(APPDIR)/SpeakerIdentApp.java $(APPDIR)/SpeakersIdentDb.java $(APPDIR)/testing.sh
	@echo "Preparing application files for insertion..."
	@echo "\begin{verbatim}" > SpeakerIdentApp.tex
	@cat $(APPDIR)/SpeakerIdentApp.java | perl -e "while(<STDIN>){s/\t/    /g; print}" >> SpeakerIdentApp.tex
	@echo "\end{verbatim}" >> SpeakerIdentApp.tex
	@echo "\begin{verbatim}" > SpeakersIdentDb.tex
	@cat $(APPDIR)/SpeakersIdentDb.java | perl -e "while(<STDIN>){s/\t/    /g; print}" >> SpeakersIdentDb.tex
	@echo "\end{verbatim}" >> SpeakersIdentDb.tex
	@echo "\begin{verbatim}" > testing-sh.tex
	@cat $(APPDIR)/testing.sh | perl -e "while(<STDIN>){s/\t/    /g; print}" >> testing-sh.tex
	@echo "\end{verbatim}" >> testing-sh.tex

todo: ../../../TODO
	@echo "Preparing TODO list for insertion..."
	@echo "\begin{verbatim}" > TODO.tex
	@cat ../../../TODO | perl -e "while(<STDIN>){s/\t/    /g; print}" >> TODO.tex
	@echo "\end{verbatim}" >> TODO.tex


#
# Distro
#

distro: report
	cp report.pdf ../../../distro/doc
	# XXX: latex2html

#
# Clean up
#

clean:
	rm -f *.log *.pdf

maintainer-clean: clean
	rm -f *.dvi *.aux *.diff $(APPFILES) TODO.tex date.tex

# EOF
