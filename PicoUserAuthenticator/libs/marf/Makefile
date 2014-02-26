# Global MARF Makefile
# Use with GNU make
#
# Copyright (C) 2003 The MARF Development Group
#
# $Header: /cvsroot/marf/marf/Makefile,v 1.2.2.1 2003/02/16 21:38:42 mokhov Exp $

VERSION=0.2.1

EXCLUDEFILES := architecture.txt CVS presentation.ppt proposal.txt

all clean:
	make -C src $@
	make -C doc $@

#
# Distro
#

distro: distro.exclude
	rm -rf distro
	mkdir -p distro
	mkdir -p distro/doc
	@echo "Making MARF source distro..."
	make -C src $@
	@echo "Making MARF docs distro..."
	make -C doc $@
	@echo "Cleaning up after builds..."
	make maintainer-clean
	@echo "Making MARF source bundle..."
	tar --exclude-from distro.exclude -cvf marf-src-$(VERSION).tar src doc HISTORY INSTALL README TODO marf.jpx Makefile ChangeLog COPYRIGHT
	gzip --best marf-src-$(VERSION).tar
	mv marf-src-$(VERSION).tar.gz distro
	@echo "Bundling up on-line documentation..."
	cp -r ChangeLog COPYRIGHT ../api ../api-dev distro/doc
	( \
		cd distro; \
		tar -cvf marf-doc-$(VERSION).tar doc; \
		gzip --best "marf-doc-$(VERSION).tar"; \
		rm -rf doc; \
	)
	rm -f distro.exclude
	@echo "Done building source and documentation distros for version $(VERSION)."

distro.exclude: $(EXCLUDEFILES)
	@echo "CVS" >> distro.exclude

$(EXCLUDEFILES):
	@echo "$@" >> distro.exclude


maintainer-clean:
	make -C src $@
	make -C doc $@
	rm -f *.diff

# EOF
