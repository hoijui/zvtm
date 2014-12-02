#!/usr/bin/python
# -*- coding: UTF-8 -*-

SUCCEEDED_IMPORTING_NUMPY = True
SUCCEEDED_IMPORTING_ASTROPY = True

import os, sys

try:
	import numpy as np
	import math
except ImportError:
	SUCCEEDED_IMPORTING_NUMPY = False

# http://www.astropy.org/
try:
	from astropy.io import fits
	from astropy import wcs 
except ImportError:
	SUCCEEDED_IMPORTING_ASTROPY = False


def memory_usage_resource():
	import resource
	rusage_denom = 1024.
	if sys.platform == 'darwin':
		# ... it seems that in OSX the output is different units ...
		rusage_denom = rusage_denom * rusage_denom
	mem = resource.getrusage(resource.RUSAGE_SELF).ru_maxrss / rusage_denom
	return mem

def header(src_path):
	#print "RESOURCE: %.2f" % (memory_usage_resource())
	print "header"
	hdulist = fits.open(src_path, mode="readonly", memmap=True)
	print "RESOURCE: %.2f MB" % (memory_usage_resource())
	if hdulist[0].header['NAXIS'] == 2:
		size = (hdulist[0].header['NAXIS1'], hdulist[0].header['NAXIS2'])
		wcsdata = wcs.WCS(hdulist[0].header)
		header = hdulist[0].header
	elif hdulist[1].header['NAXIS'] == 2:
		size = (hdulist[1].header['NAXIS1'], hdulist[1].header['NAXIS2'])
		wcsdata = wcs.WCS(hdulist[1].header)
		header = hdulist[1].header
	else:
		print("Naxis == %d" % (hdulist[0].header['NAXIS']) )
		hdulist.close()
		return

	hdulist.close()
	#print "RESOURCE: %.2f" % (memory_usage_resource())
	return {'header': header, 'wcsdata': wcsdata, 'size': size}

def pix2world(wcsdata, x, y):
	return wcsdata.wcs_pix2world(x, y, 0)
def world2pix(wcsdata, ra, dec):
	return wcsdata.wcs_world2pix(ra, dec, 0)



# main
def main(argv):
	global REFERENCE

	if len(argv) > 1:
		SRC_PATH = os.path.realpath(argv[1])
	else:
		print "You need parameter of Fits image reference"
		return

	if not SUCCEEDED_IMPORTING_ASTROPY and not SUCCEEDED_IMPORTING_NUMPY:
		print "You need library Astropy and Numpy"
		return
	else:
		if os.path.isfile(SRC_PATH):
			REFERENCE = header(SRC_PATH)

		else:
			print "The file not exist"
			return



if __name__ == "__main__":
	main(argv=sys.argv)