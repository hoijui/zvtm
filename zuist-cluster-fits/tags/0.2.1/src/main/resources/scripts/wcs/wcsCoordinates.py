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
	from astropy import units as u
	from astropy.coordinates import SkyCoord

except ImportError:
	SUCCEEDED_IMPORTING_ASTROPY = False

SRC_PATH = ""


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

	print wcsdata.printwcs()

	hdulist.close()
	#print "RESOURCE: %.2f" % (memory_usage_resource())
	return {'header': header, 'wcsdata': wcsdata, 'size': size, 'ctype': wcsdata.wcs.ctype}

def set_reference(src_path):
	global REFERENCE, SRC_PATH

	if not src_path is SRC_PATH:

		if os.path.isfile(src_path):
			REFERENCE = header(src_path)
			SRC_PATH = src_path
			return True
		else:
			print "The file not exist"
			return False
	else:
		return True


def pix2world(wcsdata, x, y):
	print "pix2world"
	print "x: %f -- y: %f" % (x, y)
	ra, dec = wcsdata.wcs_pix2world(x, y, 1)
	print "ra: %f -- dec: %f" % (ra, dec)
	xx, yy = wcsdata.wcs_world2pix(ra, dec, 0)
	print "xx: %f -- yy: %f" % (xx, yy)
	return wcsdata.wcs_pix2world(x, y, 1)

def world2pix(wcsdata, ra, dec):
	print "world2pix"
	print "ra: %f -- dec: %f" % (ra, dec)
	ctype_x, ctype_y = wcsdata.wcs.ctype
	print wcsdata.wcs.ctype
	
	if "GLON" in ctype_x and "GLAT" in ctype_y:
		l, b = icrs2galactic(ra, dec)
		px, py = wcsdata.wcs_world2pix(l, b, 0)
	elif "RA" in ctype_x and "DEC" in ctype_y:
		px, py = wcsdata.wcs_world2pix(ra, dec, 0)

	print "x: %f -- y: %f" % (px, py)
	rara, decdec = wcsdata.wcs_pix2world(px, py, 1)
	print "rara: %f -- decdec: %f" % (rara, decdec)

	return [px, py]

def icrs2galactic(ra, dec):
	print "ra: %f - dec: %f" % (ra, dec)
	ecuatorial = SkyCoord(ra=ra, dec=dec, frame='icrs', unit='deg')
	print ecuatorial
	galactic = ecuatorial.galactic
	print galactic
	return [galactic.l.deg, galactic.b.deg]

def galactic2icrs(l, b):
	print "l: %f - b: %f" % (l, b)
	galactic = SkyCoord(l=l, b=b, frame='galactic', unit='deg')
	print galactic
	ecuatorial = galactic.icrs
	print ecuatorial
	return [ecuatorial.ra.deg, ecuatorial.dec.deg]


def worldgalactic(l, b):
	galactic = SkyCoord(l=l, b=b, frame='galactic', unit='deg')
	return galactic.to_string('dms')

def worldecuatorial(ra, dec):
	ecuatorial = SkyCoord(ra=ra, dec=dec, frame='icrs', unit='deg')
	return ecuatorial.to_string('hmsdms')


# main
def main(argv):
	global REFERENCE

	if not SUCCEEDED_IMPORTING_ASTROPY and not SUCCEEDED_IMPORTING_NUMPY:
		print "You need library Astropy and Numpy"
		return
	else:
		if len(argv) > 1:
			SRC_PATH = os.path.realpath(argv[1])
			set_reference(SRC_PATH)
		else:
			SRC_PATH = ""
		



if __name__ == "__main__":
	main(argv=sys.argv)

	'''
	print "icrs2galactic"
	print "pix2world"
	[ra, dec] = pix2world(REFERENCE['wcsdata'], 0,0)
	print "ra: %f - dec: %f" % (ra, dec)
	[l, b] = icrs2galactic(ra, dec)
	print "l: %f, b: %f" % (l, b)
	[ra2, dec2] = galactic2icrs(l, b)
	print "ra: %f - dec: %f" % (ra2, dec2)
	'''
