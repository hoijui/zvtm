#!/usr/bin/env python
# -*- coding: UTF-8 -*-

import os, sys
import inspect

SUCCEEDED_IMPORTING_NUMPY = True
SUCCEEDED_IMPORTING_ASTROPY = True

try:
	import numpy as np
except ImportError:
	SUCCEEDED_IMPORTING_NUMPY = False

# http://www.astropy.org/
try:
	from astropy.io import fits
	from astropy import wcs 
except ImportError:
	SUCCEEDED_IMPORTING_ASTROPY = False




OBJECT = ""
SIZE = [0, 0]

def log(msg):
	print msg

def header(SRC_PATH):

	global OBJECT
	global SIZE
	hdulist = fits.open(SRC_PATH)

	#log("--------------------")
	#log(hdulist.info())
	#log("--------------------\n")

	if hdulist[0].header['NAXIS'] == 2:
		SIZE = (hdulist[0].header['NAXIS1'], hdulist[0].header['NAXIS2'])
		im = hdulist[0].data
		wcsdata = wcs.WCS(hdulist[0].header)
		HEADER = hdulist[0].header
		OBJECT = hdulist[0].header['OBJECT']
	elif hdulist[1].header['NAXIS'] == 2:
		SIZE = (hdulist[1].header['NAXIS1'], hdulist[1].header['NAXIS2'])
		im = hdulist[1].data
		wcsdata = wcs.WCS(hdulist[1].header)
		HEADER = hdulist[1].header
		OBJECT = hdulist[1].header['OBJECT']
	else:
		log("Naxis == %d" % (hdulist[0].header['NAXIS']) )
		return

	aw = SIZE[0]
	ah = SIZE[1]
	
	scale = 1

	path = os.path.dirname(os.path.abspath(inspect.getfile(inspect.currentframe())))
	name = "wcs_"
	ext = "fits"

	#create_fits("%s%s%d.%s" % (path, name, 0, ext), wcsdata, im, x, y, aw, ah, scale)
	x = 0
	#y = ah/2+1
	y = 0
	create_fits("%s%s%d.%s" % (path, name, 1, ext), wcsdata, im, x, y, aw/2, ah/2, scale)
	x = aw/2+1
	create_fits("%s%s%d.%s" % (path, name, 2, ext), wcsdata, im, x, y, aw/2, ah/2, scale)
	y = ah/2+1
	x = 0
	create_fits("%s%s%d.%s" % (path, name, 3, ext), wcsdata, im, x, y, aw/2, ah/2, scale)
	x = aw/2+1
	create_fits("%s%s%d.%s" % (path, name, 4, ext), wcsdata, im, x, y, aw/2, ah/2, scale)


def create_fits(tilepath, wcsdata, im, x, y, aw, ah, scale):

	lon, lat = wcsdata.all_pix2world(aw/2, SIZE[1]-y-ah/2, 0)
	print(lon, lat)

	w = wcs.WCS(naxis=2)
	ra, dec = wcsdata.wcs_pix2world(x+aw/2, SIZE[1]-y-ah/2, 0)
	w.wcs.crval = [ra, dec]
	w.wcs.ctype = wcsdata.wcs.ctype
	w.wcs.equinox = wcsdata.wcs.equinox
	w.wcs.dateobs = wcsdata.wcs.dateobs
	w.wcs.crpix = [aw/2, ah/2]
	if wcsdata.wcs.has_cd:
		cd = wcsdata.wcs.cd
		w.wcs.cd = cd*scale

	header = w.to_header()
	header['OBJECT'] = OBJECT

	data = im[y:y+ah, x:x+aw]

	fits.writeto(tilepath, data, header)
	log("created %s" % (tilepath))

# main

if len(sys.argv) > 1:
    SRC_PATH = os.path.realpath(sys.argv[1])

if not SUCCEEDED_IMPORTING_ASTROPY and not SUCCEEDED_IMPORTING_NUMPY:
	print "You need library Astropy and Numpy"
else:
	header(SRC_PATH)