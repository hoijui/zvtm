#!/usr/bin/python
# -*- coding: UTF-8 -*-

import os, sys
import inspect
import exceptions

from time import sleep


SUCCEEDED_IMPORTING_NUMPY = True
SUCCEEDED_IMPORTING_ASTROPY = True

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


PARALLEL = True
OUTPUT = "newfits.fits"

#sys.exc_clear()
#sys.exc_traceback = sys.last_traceback = None

def header(src_path):
	#print "RESOURCE: %.2f" % (memory_usage_resource())
	print "header"
	hdulist = fits.open(src_path, mode="readonly", memmap=True)
	print "RESOURCE: %.2f" % (memory_usage_resource())
	if hdulist[0].header['NAXIS'] == 2:
		size = (hdulist[0].header['NAXIS1'], hdulist[0].header['NAXIS2'])
		print "size"
		im = hdulist[0].data
		print "im"
		wcsdata = wcs.WCS(hdulist[0].header)
		print "wcsdata"
		header = hdulist[0].header
		print "header"
	elif hdulist[1].header['NAXIS'] == 2:
		size = (hdulist[1].header['NAXIS1'], hdulist[1].header['NAXIS2'])
		print "size"
		im = hdulist[1].data
		print "im"
		wcsdata = wcs.WCS(hdulist[1].header)
		print "wcsdata"
		header = hdulist[1].header
		print "header"
	else:
		print("Naxis == %d" % (hdulist[0].header['NAXIS']) )
		hdulist.close()
		return

	hdulist.close()
	#print "RESOURCE: %.2f" % (memory_usage_resource())
	return {'header': header, 'im': im, 'wcsdata': wcsdata, 'size': size}


def displace(reference, concatenable):

	pointsRef = [[0,0], [0,reference['size'][1]], [reference['size'][0],0], [reference['size'][0],reference['size'][1]]]
	points = [[0,0], [0,concatenable['size'][1]], [concatenable['size'][0],0], [concatenable['size'][0],concatenable['size'][1]]]

	minx = 99999999
	miny = 99999999
	maxx = -99999999
	maxy = -99999999
	print "second"
	for p in points:
		ra, dec = concatenable['wcsdata'].wcs_pix2world(p[0], p[1], 0)
		ii, jj = reference['wcsdata'].wcs_world2pix(ra, dec, 0)
		print "ra:%f - dec:%f -- (%f, %f)" % (ra, dec, ii, jj)
		if ii < minx:
			minx = ii
		if jj < miny:
			miny = jj
		if ii > maxx:
			maxx = ii
		if jj > maxy:
			maxy = jj
	print "reference"
	for p in pointsRef:
		ra, dec = reference['wcsdata'].wcs_pix2world(p[0], p[1], 0)
		ii, jj = reference['wcsdata'].wcs_world2pix(ra, dec, 0)
		print "ra:%f - dec:%f -- (%f, %f)" % (ra, dec, ii, jj)
		if ii < minx:
			minx = ii
		if jj < miny:
			miny = jj
		if ii > maxx:
			maxx = ii
		if jj > maxy:
			maxy = jj

	disy = math.sqrt(miny*miny)
	disx = math.sqrt(minx*minx)

	print "displace y:%d x:%d" % (disy, disx)
	return {"minx": int(minx), "maxx": int(maxx), "miny": int(miny), "maxy": int(maxy), "disx": int(disx), "disy": int(disy)}


def size_im_without_ref(reference, concatenable):
	points = [[0,0], [0,concatenable['size'][1]], [concatenable['size'][0],0], [concatenable['size'][0],concatenable['size'][1]]]

	minx = 99999999
	miny = 99999999
	maxx = -99999999
	maxy = -99999999

	for p in points:
		ra, dec = concatenable['wcsdata'].wcs_pix2world(p[0], p[1], 0)
		ii, jj = reference['wcsdata'].wcs_world2pix(ra, dec, 0)
		if ii < minx:
			minx = ii
		if jj < miny:
			miny = jj
		if ii > maxx:
			maxx = ii
		if jj > maxy:
			maxy = jj

	sizex = maxx - minx
	sizey = maxy - miny

	disy = miny
	disx = minx

	return {"minx": int(minx), "maxx": int(maxx), "miny": int(miny), "maxy": int(maxy), "sizex": int(sizex), "sizey": int(sizey), "disx": int(disx), "disy": int(disy)}

def writeFile(reference, concatenable, disx, disy):
	#print "RESOURCE: %.2f" % (memory_usage_resource())
	w = wcs.WCS(naxis=2)
	#ra, dec = reference['wcsdata'].wcs_pix2world(reference['size'][0]/2, reference['size'][1]/2, 0)
	#print "crpix [%d, %d]" % (reference['header']['CRPIX1'], reference['header']['CRPIX2'])
	ra, dec = reference['wcsdata'].wcs_pix2world(reference['header']['CRPIX1'], reference['header']['CRPIX2'], 0)
	w.wcs.crval = [ra, dec]
	w.wcs.ctype = reference['wcsdata'].wcs.ctype
	w.wcs.equinox = reference['wcsdata'].wcs.equinox
	w.wcs.dateobs = reference['wcsdata'].wcs.dateobs
	#w.wcs.crpix = [reference['size'][0]/2+disx, reference['size'][1]/2+disy]
	w.wcs.crpix = [reference['header']['CRPIX1']+disx, reference['header']['CRPIX2']+disy]
	
	if reference['wcsdata'].wcs.has_cd():
		cd = reference['wcsdata'].wcs.cd
		w.wcs.cd = cd
		w.wcs.cdelt = [np.sqrt(w.wcs.cd[0,0]*w.wcs.cd[0,0]+w.wcs.cd[1,0]*w.wcs.cd[1,0]), np.sqrt(w.wcs.cd[0,1]*w.wcs.cd[0,1]+w.wcs.cd[1,1]*w.wcs.cd[1,1])]
		w.wcs.pc = [[w.wcs.cd[0,0]/ w.wcs.cdelt[0], w.wcs.cd[0,1]/ w.wcs.cdelt[0]],[w.wcs.cd[1,0]/ w.wcs.cdelt[1], w.wcs.cd[1,1]/ w.wcs.cdelt[1]]]
	else:
		pc = reference['wcsdata'].wcs.pc
		w.wcs.pc = pc
		w.wcs.cdelt = reference['wcsdata'].wcs.cdelt

	header = w.to_header()

	header['OBJECT'] = reference['header']['OBJECT'] + " - " + concatenable['header']['OBJECT']

	hdu = fits.PrimaryHDU(header=header, data=IM)
	if os.path.isfile(OUTPUT):
		os.remove(OUTPUT)	
	#print "RESOURCE: %.2f" % (memory_usage_resource())
	hdu.writeto(OUTPUT)
	#print "RESOURCE: %.2f" % (memory_usage_resource())
	#fits.writeto(path+"_newfits.fits", im, header)



def init_project(reference, concatenable, disx, disy, xini, yini, lenght, width, height):
	global IM

	try:
		porcent = 0
		before = -1
		for k in range(yini * height + xini, yini * height + xini + lenght):

			porcent = int(float(k)/float(yini * height + xini +lenght)*100)
			if before != porcent:
				sys.stdout.write('\r')
				sys.stdout.write("[%-50s] %d%% RESOURCE: %.2f MB 	-- init_project" % ('='*(porcent/2), porcent, memory_usage_resource()))
				sys.stdout.flush()
				before = porcent

			i = (k/height)
			j = (k%height)
			if IM[j+disy,i+disx] < reference['im'][j,i]:
				IM[j+disy,i+disx] = reference['im'][j,i]

		print ""
	except IndexError:
		print "IndexError"
		print "disx: %f disy: %f xini: %d yini: %d lenght: %d width: %d height: %d IM: (%d, %d)" % (disx, disy, xini, yini, lenght, width, height, IM.shape[1], IM.shape[0])
		print "i: %d j: %d" % (i, j)
	except MemoryError:
		print "MemoryError"
		print "disx: %f disy: %f xini: %d yini: %d lenght: %d width: %d height: %d IM: (%d, %d)" % (disx, disy, xini, yini, lenght, width, height, IM.shape[1], IM.shape[0])
		print "i: %d j: %d" % (i, j)
		print "[%d, %d]" % (yini * height + xini, yini * height + xini + lenght)




def project(reference, concatenable, disx, disy, xini, yini, lenght, width, height):
	global IM

	try:
		notbackup = True
		porcent = 0
		before = -1
		for k in range(yini * height + xini, yini * height + xini + lenght):

			porcent = int(float(k)/float(yini * height + xini +lenght)*100)
			if before != porcent:
				sys.stdout.write('\r')
				sys.stdout.write("[%-50s] %d%% RESOURCE: %.2f MB 	-- project" % ('='*(porcent/2), porcent, memory_usage_resource()))
				sys.stdout.flush()
				before = porcent
				if not notbackup:
					writeFile(reference, concatenable, disx, disy)

			i = (k/height)
			j = (k%height)
			#print "k: %d i: %d j: %d" % (k, (k/width), (k%width))
			ra, dec = concatenable['wcsdata'].wcs_pix2world(i, j, 0)
			ii, jj = reference['wcsdata'].wcs_world2pix(ra, dec, 0)
			if IM[jj+disy,ii+disx] < concatenable['im'][j,i]:
				IM[jj+disy,ii+disx] = concatenable['im'][j,i]
				if notbackup:
					notbackup = False
			else:
				if not notbackup:
					notbackup = True
		print ""
	except IndexError:
		print "IndexError"
		print "disx: %f disy: %f xini: %d yini: %d lenght: %d width: %d height: %d IM: (%d, %d)" % (disx, disy, xini, yini, lenght, width, height, IM.shape[1], IM.shape[0])
		print "i: %d j: %d" % (i, j)
	except MemoryError:
		print "MemoryError"
		print "disx: %f disy: %f xini: %d yini: %d lenght: %d width: %d height: %d IM: (%d, %d)" % (disx, disy, xini, yini, lenght, width, height, IM.shape[1], IM.shape[0])
		print "i: %d j: %d" % (i, j)
		print "[%d, %d]" % (yini * height + xini, yini * height + xini + lenght)


def project_without_reference(reference, concatenable, disx, disy, xini, yini, lenght, width, height):
	global IM

	try:
		notbackup = True
		porcent = 0
		before = -1
		for k in range(yini * height + xini, yini * height + xini + lenght):

			porcent = int(float(k)/float(yini * height + xini +lenght)*100)
			if before != porcent:
				sys.stdout.write('\r')
				sys.stdout.write("[%-50s] %d%% RESOURCE: %.2f MB 	-- project_without_reference" % ('='*(porcent/2), porcent, memory_usage_resource()))
				sys.stdout.flush()
				before = porcent
				if not notbackup:
					writeFile(reference, concatenable, disx, disy)

			i = (k/height)
			j = (k%height)

			#print "k: %d i: %d j: %d" % (k, (k/width), (k%width))
			ra, dec = concatenable['wcsdata'].wcs_pix2world(i, j, 0)
			ii, jj = reference['wcsdata'].wcs_world2pix(ra, dec, 0)

			rara, decdec = reference['wcsdata'].wcs_pix2world(ii, jj, 0)
			iii, jjj = concatenable['wcsdata'].wcs_world2pix(rara, decdec, 0)
			'''
			if ii-disx <= 0 or jj-disy <= 0 or ii-disx >= width or jj-disy >= height :
				print "(%d, %d) dis (%d, %d) (%d, %d)" % (ii-i, jj-j, disx, disy, ii-disx, jj-disy)
			'''
			print '(%d, %d) <-- (%d, %d) [%f]' % (iii, jjj, i,j,concatenable['im'][j,i])
			print IM.shape
			
			if IM[jjj+1, iii+1] < concatenable['im'][j,i]:
				IM[jjj+1, iii+1] = concatenable['im'][j,i]
				if notbackup:
					notbackup = False
			else:
				if not notbackup:
					notbackup = True
			

		print ""
	except IndexError:
		print "IndexError"
		print "disx: %f disy: %f xini: %d yini: %d lenght: %d width: %d height: %d IM: (%d, %d)" % (disx, disy, xini, yini, lenght, width, height, IM.shape[1], IM.shape[0])
		print "i: %d j: %d" % (i, j)
	except MemoryError:
		print "MemoryError"
		print "disx: %f disy: %f xini: %d yini: %d lenght: %d width: %d height: %d IM: (%d, %d)" % (disx, disy, xini, yini, lenght, width, height, IM.shape[1], IM.shape[0])
		print "i: %d j: %d" % (i, j)
		print "[%d, %d]" % (yini * height + xini, yini * height + xini + lenght)


def memory_usage_resource():
	import resource
	rusage_denom = 1024.
	if sys.platform == 'darwin':
		# ... it seems that in OSX the output is different units ...
		rusage_denom = rusage_denom * rusage_denom
	mem = resource.getrusage(resource.RUSAGE_SELF).ru_maxrss / rusage_denom
	return mem


# main

def main(argv):
	global SRC_PATH
	global SRC_PATH2
	global OUTPUT
	global IM
	global DISP
	global REFERENCE
	global CONCATENABLE


	if len(argv) > 3:
		SRC_PATH = os.path.realpath(argv[1])
		SRC_PATH2 = os.path.realpath(argv[2])
		OUTPUT = argv[3]
	elif len(argv) > 2:
		SRC_PATH = os.path.realpath(argv[1])
		SRC_PATH2 = os.path.realpath(argv[2])
	elif len(argv) > 1:
		SRC_PATH = os.path.realpath(argv[1])
	else:
		print "You need parameters"

	if not SUCCEEDED_IMPORTING_ASTROPY and not SUCCEEDED_IMPORTING_NUMPY:
		print "You need library Astropy and Numpy"
	else:

		if not os.path.isfile(OUTPUT):
			REFERENCE = header(SRC_PATH)
			
		else:
			print "header OUTPUT"
			REFERENCE = header(OUTPUT)


		print "Object ref: %s" % (REFERENCE['header']['OBJECT'])
		CONCATENABLE = header(SRC_PATH2)
		print "Object concatenable: %s" % (CONCATENABLE['header']['OBJECT'])
		
		
		DISP = displace(REFERENCE, CONCATENABLE)

		if not os.path.isfile(OUTPUT):
			IM = np.zeros( [DISP['maxy'] - DISP['miny']+1, DISP['maxx'] - DISP['minx']+1], dtype=np.float32 )
		else:
			IM = REFERENCE['im']
		
		print "RESOURCE: %.2f" % (memory_usage_resource())

		if WITHOUTREF:

			SIZE = size_im_without_ref(REFERENCE, CONCATENABLE)
			print "size: (%d, %d) min: (%d,%d) max: (%d, %d) " % (SIZE['sizex'], SIZE['sizey'], SIZE['minx'], SIZE['miny'], SIZE['maxx'], SIZE['maxy'])
			IM = np.zeros([SIZE['sizey']+1, SIZE['sizex']+1], dtype=np.float32 )
			project_without_reference(REFERENCE, CONCATENABLE, SIZE['disx'], SIZE['disy'], 0, 0, CONCATENABLE['size'][0]*CONCATENABLE['size'][1], CONCATENABLE['size'][1], CONCATENABLE['size'][0])
			#project_without_reference(REFERENCE, CONCATENABLE, DISP['disx'], DISP['disy'], 0, 0, REFERENCE['size'][0]*REFERENCE['size'][1], REFERENCE['size'][0], REFERENCE['size'][1])
			writeFile(REFERENCE, CONCATENABLE, SIZE['disx'], SIZE['disy'])


		if not PARALLEL:
			#init_project(REFERENCE, CONCATENABLE, DISP['disx'], DISP['disy'], 0, 0, REFERENCE['size'][0]*REFERENCE['size'][1], REFERENCE['size'][1], REFERENCE['size'][0])
			#project(REFERENCE, CONCATENABLE, DISP['disx'], DISP['disy'], 0, 0, CONCATENABLE['size'][0]*CONCATENABLE['size'][1], CONCATENABLE['size'][1], CONCATENABLE['size'][0])
			if not os.path.isfile(OUTPUT):
				init_project(REFERENCE, CONCATENABLE, DISP['disx'], DISP['disy'], 0, 0, REFERENCE['size'][0]*REFERENCE['size'][1], REFERENCE['size'][0], REFERENCE['size'][1])
				writeFile(REFERENCE, CONCATENABLE, DISP['disx'], DISP['disy'])

			project(REFERENCE, CONCATENABLE, DISP['disx'], DISP['disy'], 0, 0, CONCATENABLE['size'][0]*CONCATENABLE['size'][1], CONCATENABLE['size'][0], CONCATENABLE['size'][1])
			writeFile(REFERENCE, CONCATENABLE, DISP['disx'], DISP['disy'])
	


if __name__ == "__main__":
	PARALLEL = True
	WITHOUTREF = True
	main(argv=sys.argv)

'''

if len(sys.argv) > 3:
	SRC_PATH = os.path.realpath(sys.argv[1])
	SRC_PATH2 = os.path.realpath(sys.argv[2])
	OUTPUT = sys.argv[3]
elif len(argv) > 2:
	SRC_PATH = os.path.realpath(sys.argv[1])
	SRC_PATH2 = os.path.realpath(sys.argv[2])
elif len(argv) > 1:
	SRC_PATH = os.path.realpath(sys.argv[1])
else:
	print "You need parameters"

if not SUCCEEDED_IMPORTING_ASTROPY and not SUCCEEDED_IMPORTING_NUMPY:
	print "You need library Astropy and Numpy"
else:


	REFERENCE = header(SRC_PATH)
	print "Object ref: %s" % (REFERENCE['header']['OBJECT'])
	CONCATENABLE = header(SRC_PATH2)
	print "Object concatenable: %s" % (CONCATENABLE['header']['OBJECT'])
	
	
	DISP = displace(REFERENCE, CONCATENABLE)

	IM = np.zeros( [DISP['maxy'] - DISP['miny']+1, DISP['maxx'] - DISP['minx']+1], dtype=np.float32 )
	
	init_project(REFERENCE, CONCATENABLE, DISP['disx'], DISP['disy'], 0, 0, REFERENCE['size'][0]*REFERENCE['size'][1], REFERENCE['size'][0], REFERENCE['size'][1])
	project(REFERENCE, CONCATENABLE, DISP['disx'], DISP['disy'], 0, 0, CONCATENABLE['size'][0]*CONCATENABLE['size'][1], CONCATENABLE['size'][0], CONCATENABLE['size'][1])
	writeFile(REFERENCE, CONCATENABLE, DISP['disx'], DISP['disy'])

'''