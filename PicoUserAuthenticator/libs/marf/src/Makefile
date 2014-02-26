# MARF Build Makefile
# Use with GNU make
#
# Copyright (c) 2002-2003 The MARF Development Group
#
# $Header: /cvsroot/marf/marf/src/Makefile,v 1.35.2.2 2003/02/17 21:04:02 mokhov Exp $

VERSION=0.2.1

#
# Dirs of MARF modules
#

MARFDIRS := marf \
            marf/Preprocessing \
            marf/Preprocessing/Dummy \
            marf/Preprocessing/Endpoint \
            marf/Preprocessing/FFTFilter \
            marf/FeatureExtraction \
            marf/FeatureExtraction/FFT \
            marf/FeatureExtraction/LPC \
            marf/FeatureExtraction/F0 \
            marf/FeatureExtraction/Cepstral \
            marf/FeatureExtraction/Segmentation \
            marf/FeatureExtraction/RandomFeatureExtraction \
            marf/Classification \
            marf/Classification/Markov \
            marf/Classification/NeuralNetwork \
            marf/Classification/Stochastic \
            marf/Classification/Distance \
            marf/Classification/RandomClassification \
            marf/Stats \
            marf/Storage \
            marf/Storage/Loaders \
            marf/gui \
            marf/util

EXTDIR = /mnt/nettemp/jaxp:~/marf/jaxp:/mnt/nettemp/step/jaxp
INSTALLDIR = /mnt/nettemp/marf
MARFDIR = /mnt/nettemp/marf:~/marf/marf

# Lists of all *.java and *.class files
JAVAFILES := $(MARFDIRS:%=%/*.java)
CLASSES := $(MARFDIRS:%=%/*.class)

# Devel
DIFFS := $(MARFDIRS:%=%/*.diff)


# Java version
JAVAC = javac
ALTJAVAC = /pkg/j2sdk/bin/javac

#JFLAGS = -g -verbose -extdirs ${EXTDIR}

#JFLAGS = -g -verbose
#JFLAGS = -g -verbose -extdirs $(INSTALLDIR):$(EXTDIR) -sourcepath .
#JFLAGS = -g -verbose -classpath .:$(INSTALLDIR):$(EXTDIR) -sourcepath .
#JFLAGS = -g -verbose -classpath .:$(EXTDIR) -sourcepath .
#JFLAGS = -g -verbose -extdirs $(EXTDIR) -sourcepath .
JFLAGS = -verbose -classpath .:$(EXTDIR) -extdirs .:$(EXTDIR) -sourcepath .

JVM = java

#
# Java Doc for API doc generation
#

JAVADOC = javadoc
#JAVADOCFLAGS = \
               -sourcepath $(MARFDIRS) \
               -classpath $(MARFDIRS) \
               -verbose -use -version -author \
               -stylesheetfile ../doc/src/stylesheet.css

JAVADOCFLAGS = \
               -verbose -use -version -author \
               -stylesheetfile ../doc/src/sgml/stylesheet.css

#
# Build
#

all: marf/MARF.class
	jar cvf marf.jar marf
	@echo "(-: MARF build has been successful :-)"

altall: $(JAVAFILES)
	mkdir -p release
	$(ALTJAVAC) $(JFLAGS) -d release test.java
	(cd release;\
	rm -f test.class;\
	jar cvf marf.jar marf;)
	@echo "(-: Alternative MARF build has been successful :-)"

marf/MARF.class: $(JAVAFILES)
	$(JAVAC) -g $(JFLAGS) test.java

# Quick hack for optimized build
# Faster but class files are larger, no debug info included
optimize: $(JAVAFILES)
	$(JAVAC) -g:none -O $(JFLAGS) test.java
	jar cvf marf.jar marf

install: all
	cp marf.jar $(INSTALLDIR)
	chmod go+r $(INSTALLDIR)/marf.jar
#	$(JAVAC) $(JFLAGS) -d $(INSTALLDIR) test.java
#	rm -f $(INSTALLDIR)/test.class


#
# For packaging and distribution
# XXX: make versionning of .jar automatic
#

distro: $(JAVAFILES) api
	@echo "MARF: Making binary marf distro..."
	$(JAVAC) -g:none -O $(JFLAGS) -d ../distro test.java
	(cd ../distro; \
	rm -f test.class; \
	jar cvf marf-$(VERSION).jar marf; \
	rm -rf marf)

#
# API docs in javadoc
#

# API Documentation for MARF users
apidoc: $(JAVAFILES)
	$(JAVADOC) $(JAVADOCFLAGS) -package -d ../../api $(JAVAFILES)

# API Documentation for MARF developers
apidoc-dev: $(JAVAFILES)
	$(JAVADOC) $(JAVADOCFLAGS) -private -d ../../api-dev $(JAVAFILES)

# Both APIs
api: apidoc apidoc-dev

#
# Clean up
#

clean:
	rm -f $(CLASSES) test.class

maintainer-clean: clean
	rm -f *.diff *.jar *log $(DIFFS)

# EOF
