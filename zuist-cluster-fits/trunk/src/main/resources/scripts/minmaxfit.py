#!/usr/bin/python
# -*- coding: UTF-8 -*-

import os, sys

SUCCEEDED_IMPORTING_ASTROPY = True

# http://www.astropy.org/
try:
	from astropy.io import fits
	import numpy as np
except ImportError:
	SUCCEEDED_IMPORTING_ASTROPY = False



def openFits(SRC_PATH):

	#print SRC_PATH
	hdulist = fits.open(SRC_PATH)


	if hdulist[0].header['NAXIS'] == 2:
		im = hdulist[0].data
	elif hdulist[1].header['NAXIS'] == 2:
		im = hdulist[1].data
	else:
		return

	minvalue = np.amin(im)
	maxvalue = np.amax(im)
	return [minvalue, maxvalue]


###############################################################################
# main
################################################################################

path_Ks = {
	"/home/fdelcampo/zuist-scenes-local/fits/Ks/v20100411_01052_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/Ks/v20100411_00980_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/Ks/v20100411_00944_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/Ks/v20100411_00872_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/Ks/v20100420_00370_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/Ks/v20100411_01016_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/Ks/v20100411_00908_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/Ks/v20110508_00412_st_tl.fit"
}

path_H = {
	"/home/fdelcampo/zuist-scenes-local/fits/H/v20100411_01040_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/H/v20100411_00968_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/H/v20100411_00932_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/H/v20100411_00860_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/H/v20100420_00358_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/H/v20100411_01004_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/H/v20100411_00896_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/H/v20110508_00400_st_tl.fit"
}

path_J = {
	"/home/fdelcampo/zuist-scenes-local/fits/J/v20100411_01064_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/J/v20100411_00992_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/J/v20100411_00956_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/J/v20100411_00884_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/J/v20100420_00382_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/J/v20100411_01028_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/J/v20100411_00920_st_tl.fit",
	"/home/fdelcampo/zuist-scenes-local/fits/J/v20110508_00424_st_tl.fit"
}

if not SUCCEEDED_IMPORTING_ASTROPY:
	print "ASTROPY not available"
	sys.exit(0)

gmin_ks = sys.float_info.max
gmax_ks = sys.float_info.min

for path in path_Ks:
	#SRC_PATH = os.path.realpath(path)
	minmax = openFits(path)
	#print minmax
	if minmax[0] < gmin_ks:
		gmin_ks = minmax[0]
	if minmax[1] > gmax_ks:
		gmax_ks = minmax[1]
print "global Ks"
print [gmin_ks, gmax_ks]

gmin_h = sys.float_info.max
gmax_h = sys.float_info.min

for path in path_H:
	#SRC_PATH = os.path.realpath(path)
	minmax = openFits(path)
	#print minmax
	if minmax[0] < gmin_h:
		gmin_h = minmax[0]
	if minmax[1] > gmax_h:
		gmax_h = minmax[1]
print "global H"
print [gmin_h, gmax_h]
	
gmin_j = sys.float_info.max
gmax_j = sys.float_info.min

for path in path_J:
	#SRC_PATH = os.path.realpath(path)
	minmax = openFits(path)
	#print minmax
	if minmax[0] < gmin_j:
		gmin_j = minmax[0]
	if minmax[1] > gmax_j:
		gmax_j = minmax[1]
print "global J"
print [gmin_j, gmax_j]