#!/usr/bin/env python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2009. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id: imageTiler.py 5012 2013-09-27 20:05:17Z epietrig $

###############################################################################
# Only 32-bit Python will work with CG bindings on Mac OS X 10.6 Snow Leopard
# make sure you

#    export VERSIONER_PYTHON_PREFER_32_BIT=yes

# before calling this script
###############################################################################

import os, sys, math
from copy import copy

# http://effbot.org/zone/element-index.htm
import xml.etree.ElementTree as ET

SUCCEEDED_IMPORTING_PIL = True
SUCCEEDED_IMPORTING_CG = True
SUCCEEDED_IMPORTING_ASTROPY = True

# http://developer.apple.com/documentation/GraphicsImaging/Conceptual/drawingwithquartz2d/dq_python/dq_python.html
try:
    from CoreGraphics import *
except ImportError:
    SUCCEEDED_IMPORTING_CG = False

# http://www.pythonware.com/products/pil/
try:
    from PIL import Image
except ImportError:
    SUCCEEDED_IMPORTING_PIL = False

# http://www.astropy.org/
try:
    from astropy.io import fits
    from astropy import wcs
    #from astropy.convolution import convolve #, convolve_fft
    import numpy as np

except ImportError:
    SUCCEEDED_IMPORTING_ASTROPY = False


# Tile IDs are generated following this pattern:
# ---------
# | 1 | 2 |
# ---------
# | 3 | 4 |
# ---------
TL = 1
TR = 2
BL = 3
BR = 4

MINVALUE = None
MAXVALUE = None


CMD_LINE_HELP = "ZUIST Image Tiling Script\n\nUsage:\n\n" + \
    " \timageTiler <src_image_path> <target_dir> [options]\n\n" + \
    "Options:\n\n"+\
    "\t-cg\t\tprocessing pipeline: CoreGraphics (Mac only)\n"+\
    "\t-im\t\tprocessing pipeline: PIL and ImageMagick (default)\n"+\
    "\t-gm\t\tprocessing pipeline: GraphicsMagick\n"+\
    "\t-fits\t\tprocessing fits image with astropy library\n"+\
    "\t-ts=N\t\ttile size (N in pixels)\n"+\
    "\t-f\t\tforce tile generation\n"+\
    "\t-tl=N\t\ttrace level (N in [0:3])\n"+\
    "\t-idprefix=p\tcustom prefix for all region and objects IDs\n"+\
    "\t-dx=y\t\tx offset for all regions and objects\n"+\
    "\t-dy=x\t\ty offset for all regions and objects\n"+\
    "\t-dl=l\t\tlevel offset for all regions and objects\n"+\
    "\t-scale=s\ts scale factor w.r.t default size for PDF input\n"+\
    "\t-layer=name\tname layer from the zuist\n"+\
    "\t-minvalue\tvalue minimum on fits images\n"+\
    "\t-maxvalue\tvalue maximum on fits images\n"+\
    "\t-onlyxml \tcreate only xml from the tiles\n"+\
    "\t-shrink \tdefault use natural neighbor. change set to shrink\n"+\
    "\t-notnewfile \tif exist file, not create new file\n"+\
    "\t-withbackground\tWithout the image of the level 0\n"+\
    "\t-maxneighborhood=N\tMaximum neighborhood\n"


TRACE_LEVEL = 1

FORCE_GENERATE_TILES = False

TILE_SIZE = 500

OUTPUT_FILE_EXT = "png"

# camera focal distance
F = 100.0
# camera max altitude
MAX_ALT = "100000"

IMG_SRC_PATH = ""

# prefix for image tile files
TILE_FILE_PREFIX = "tile-"

PROGRESS = 0

# X offset
DX = 0
# Y offset
DY = 0
# level offset
DL = 0

LAYER = None

ID_PREFIX = ""

PDF_SCALE_FACTOR = 5

COLOR_SPACE = None

USE_CG = False
USE_GRAPHICSMAGICK = False
USE_ASTROPY = False

ONLYXML = False
NATURAL_NEIGBOR = True
NEWFILE = True
BACKGROUND = False
MAX_NEIGBORHOOD = -1

WCSDATA = ""
OBJECT = ""
HEADER = ""
SIZE = [0, 0]

################################################################################
# Create target directory if it does not exist yet
################################################################################
def createTargetDir():
    if not os.path.exists(TGT_DIR):
        log("Creating target directory %s" % TGT_DIR, 2)
        os.mkdir(TGT_DIR)

################################################################################
# Compute number of tiles
################################################################################
def computeMaxTileCount(i, sum):
    if i > 0:
        return computeMaxTileCount(i-1, sum+math.pow(4,i))
    else:
        return sum + 1

################################################################################
# Count number of levels in ZUIST scene
# (source image size from PIL, parent XML element)
################################################################################
def generateLevels(src_sz, rootEL):
    # number of horizontal tiles at lowest level
    htc = src_sz[0] / TILE_SIZE
    if src_sz[0] % TILE_SIZE > 0:
        htc += 1
    # number of vertical tiles at lowest level
    vtc = src_sz[1] / TILE_SIZE
    if src_sz[1] % TILE_SIZE > 0:
        vtc += 1
    # number of levels
    res = math.ceil(max(math.log(vtc,2)+1, math.log(htc,2)+1))
    log("Will generate %d level(s)" % res, 2)
    # generate ZUIST levels
    altitudes = [0,]
    for i in range(int(res)):
        depth = int(res-i-1)
        altitudes.append(int(F*math.pow(2,i+1)-F))
        level = ET.SubElement(rootEL, "level")
        level.set("depth", str(depth+DL))
        level.set("floor", str(altitudes[-2]))
        level.set("ceiling", str(altitudes[-1]))
    if DL == 0:
        # fix max scene altitude (for highest region)
        level.set("ceiling", MAX_ALT)
    else:
        # log2 slice of unused space between declared MAX_ALT and actual max alt
        # for empty levels (exist if DL > 0)
        faltitudes = [int(MAX_ALT),]
        for i in range(DL):
            faltitudes.append((faltitudes[-1]-altitudes[-1])/2)
            level = ET.SubElement(rootEL, "level")
            level.set("depth", str(i))
            level.set("floor", str(faltitudes[-1]))
            level.set("ceiling", str(faltitudes[-2]))
    return res

################################################################################
# generate tiles (rec calls)
# (tile ID of parent, position in parent tile, current level, number of levels
#  top left coords, size of source image, parent XML element,
#  src image object [a PIL image or a CoreImage depending on pipeline],
#  ID of parent region [None for 1st level])
################################################################################
def buildTiles(parentTileID, pos, level, levelCount, x, y, src_sz, rootEL, im, parentRegionID):
    if (level >= levelCount):
        return
    global PROGRESS
    PROGRESS += 1
    tileID = copy(parentTileID)
    tileID[level] = tileID[level] + pos
    tileIDstr = "-".join([str(s) for s in tileID])
    if x > src_sz[0] or y > src_sz[1]:
        log("---- %.2f%%\nIgnoring tile %s (out of bounds)" % (PROGRESS/float(maxTileCount)*100, tileIDstr), 3)
        return
    scale = math.pow(2, levelCount-level-1)
    # generate image tile
    # generate image except for level 0 where we use original image
    tileFileName = "%s%s.%s" % (TILE_FILE_PREFIX, tileIDstr, OUTPUT_FILE_EXT)
    tilePath = "%s/%s/%s" % (TGT_DIR, level, tileFileName)

    if not os.path.exists("%s/%s" % (TGT_DIR, level) ):
        log("Creating target directory %s/%s" % (TGT_DIR, level), 2)
        os.mkdir("%s/%s" % (TGT_DIR, level))

    aw = ah = TILE_SIZE*scale
    if x + TILE_SIZE*scale > src_sz[0]:
        aw = int(src_sz[0] - x)
    if y + TILE_SIZE*scale > src_sz[1]:
        ah = int(src_sz[1] - y)
    if aw == 0 or ah == 0:
        return
    if os.path.exists(tilePath) and not FORCE_GENERATE_TILES:
        log("---- %.2f%%\n%s already exists (skipped)" % (PROGRESS/float(maxTileCount)*100, tilePath), 2)
    elif not ONLYXML:
        log("---- %.2f%%\nGenerating tile %s" % (PROGRESS/float(maxTileCount)*100, tileIDstr), 2)
        if USE_CG:
            # this will work only with a Mac
            w = h = int(TILE_SIZE)
            log("Cropping at (%d,%d,%d,%d)" % (x, y, aw, ah), 3)
            cim = im.createWithImageInRect(CGRectMake(int(x), int(y), int(aw), int(ah)))
            log("Resizing to (%d, %d)" % (aw/scale, ah/scale), 3)
            bitmap = CGBitmapContextCreateWithColor(int(aw/scale), int(ah/scale), COLOR_SPACE, CGFloatArray(4))
            bitmap.setInterpolationQuality(kCGInterpolationHigh)
            rect = CGRectMake(0, 0, int(aw/scale), int(ah/scale))
            bitmap.drawImage(rect, cim)
            bitmap.writeToFile(tilePath, kCGImageFormatPNG)
        elif USE_ASTROPY:
            
            if NEWFILE or (not os.path.exists(tilePath) and not NEWFILE):

                #wcsdata = wcs.WCS(IMG_SRC_PATH)
                #log("wcsdata:")
                #log(WCSDATA)
                '''
                Check correctly wcs coordinates
                '''

                log("Cropping at (%d,%d,%d,%d)" % (x, y, aw, ah))
                #data = im[y:y+ah, x:x+aw]
                #data = im[SIZE[1]-y-ah/2: SIZE[1]-y+ah/2, x:x+aw]
                data = im[SIZE[1]-y-ah: SIZE[1]-y, x:x+aw]

                log(data.shape)
                
                if scale > 1.:
                    log("Resizing to (%d, %d)" % (aw/scale, ah/scale), 3)
                    
                    if NATURAL_NEIGBOR:
                        resizedata = natural_neighbor(data, ah, aw, ah/scale, aw/scale)
                    else:
                        resizedata = shrink(data, ah, aw, ah/scale, aw/scale)
                    
                    log(resizedata.shape)
                

                '''
                #header = WCSDATA.to_header()
                w = wcs.WCS(naxis=2, relax=False)#relax=wcs.WCSHDR_RADESYS)#, relax=(wcs.WCSHDR_RADECSYS | wcs.WCSHDR_EPOCHa | wcs.WCSHDR_CD00i00j))#, relax=wcs.WCSHDR_CD00i00j) #wcs.WCSHDR_CD00i00j RADESYS

                #'
                ra, dec = WCSDATA.wcs_pix2world(WCSDATA.wcs.crpix[0], WCSDATA.wcs.crpix[1] , 0)
                log("ra: %f - dec: %f" % (ra, dec) )
                px, py = WCSDATA.wcs_world2pix(WCSDATA.wcs.crval[0], WCSDATA.wcs.crval[1], 0)
                log("px: %f - py: %f" % (px, py) )
                #'

                ra, dec = WCSDATA.wcs_pix2world(x+aw/2, SIZE[1]-y-ah/2, 0)
                
                w.wcs.crval = [ra, dec]
                w.wcs.ctype = WCSDATA.wcs.ctype
                w.wcs.equinox = WCSDATA.wcs.equinox
                w.wcs.dateobs = WCSDATA.wcs.dateobs
                w.wcs.crpix = [aw/scale/2, ah/scale/2]
                #if WCSDATA.wcs.has_cd:
                #    cd = WCSDATA.wcs.cd
                #    w.wcs.cd = cd*scale


                if WCSDATA.wcs.has_cd():
                    cd = WCSDATA.wcs.cd
                    w.wcs.cd = cd*scale
                    w.wcs.cdelt = [np.sqrt(w.wcs.cd[0,0]*w.wcs.cd[0,0]+w.wcs.cd[1,0]*w.wcs.cd[1,0]), np.sqrt(w.wcs.cd[0,1]*w.wcs.cd[0,1]+w.wcs.cd[1,1]*w.wcs.cd[1,1])]
                    w.wcs.pc = [[w.wcs.cd[0,0]/ w.wcs.cdelt[0], w.wcs.cd[0,1]/ w.wcs.cdelt[0]],[w.wcs.cd[1,0]/ w.wcs.cdelt[1], w.wcs.cd[1,1]/ w.wcs.cdelt[1]]]
                elif WCSDATA.wcs.has_pc():
                    w.wcs.pc = WCSDATA.wcs.pc
                    cdelt = WCSDATA.wcs.cdelt
                    print "cdelt"
                    print cdelt
                    print scale
                    w.wcs.cdelt = cdelt*scale
                    print w.wcs.cdelt

                '''

                header = HEADER.copy()

                '''

                ra, dec = WCSDATA.wcs_pix2world(x+aw/2, SIZE[1]-y-ah/2, 0)


                #header.set('CRVAL1', #(float)(ra) )
                #header.set('CRVAL2', #(float)(dec) )
                header.set('CRPIX1', float(x) )#(float)(aw/scale/2) )
                header.set('CRPIX2', float(SIZE[1]-y) )#(float)(ah/scale/2) )
                if WCSDATA.wcs.has_cd():
                    cd = WCSDATA.wcs.cd
                    header.set('CD1_1', cd[0,0]*scale )
                    header.set('CD1_2', cd[0,1]*scale )
                    header.set('CD2_1', cd[1,0]*scale )
                    header.set('CD2_2', cd[1,1]*scale )
                elif WCSDATA.wcs.has_pc():
                    pc = WCSDATA.wcs.get_pc()
                    cdelt = WCSDATA.wcs.cdelt
                    header.set('PC1_1', pc[0,0] )
                    header.set('PC1_2', pc[0,1] )
                    header.set('PC2_1', pc[1,0] )
                    header.set('PC2_2', pc[1,1] )
                    header.set('CDELT1', cdelt[0]*scale)
                    header.set('CDELT2', cdelt[1]*scale)

                '''

                #header['CD1_1'] = header['CD1_1']*scale
                #header['CD1_2'] = header['CD1_2']*scale
                #header['CD2_1'] = header['CD2_1']*scale
                #header['CD2_2'] = header['CD1_1']*scale

                #pc = WCSDATA.wcs.get_pc()
                #w.wcs.pc = pc*scale
                #w.wcs.cdelt = [np.sqrt(w.wcs.cd[0,0]*w.wcs.cd[0,0]+w.wcs.cd[1,0]*w.wcs.cd[1,0]), np.sqrt(w.wcs.cd[0,1]*w.wcs.cd[0,1]+w.wcs.cd[1,1]*w.wcs.cd[1,1])]
                #w.wcs.cdelt = [1, 1]

                #w.wcs.cd = [[w.wcs.pc[0,0]* w.wcs.cdelt[0], w.wcs.pc[0,1]* w.wcs.cdelt[0]],[w.wcs.pc[1,0]* w.wcs.cdelt[1], w.wcs.pc[1,1]* w.wcs.cdelt[1]]]
                #log("cdelt: ")
                #log(w.wcs.cdelt)

                #log("cd")
                #log(w.wcs.cd)

                #log("pc")
                #log(w.wcs.pc)

                #del w.wcs.pc
                #del w.wcs.cdelt

                #w.wcs.pc = [[0,0],[0,0]]
                #w.wcs.cdelt = [0,0]
                
                #w.wcs.print_contents()

                
                #header = w.to_header(relax=True)
                #header['OBJECT'] = OBJECT

                #log("header wcs")
                #log(header)
                #hdu = fits.PrimaryHDU(newData)
                #log("new file: %s" % (tilePath))
                tilePathn = tilePath
                tileFileNamen = tileFileName
                n = 1
                while os.path.exists(tilePathn) and NEWFILE:
                    tilePathn = "%s(%d)" % (tilePath, n)
                    tileFileNamen = "%s(%d)" % (tileFileName, n)
                    n = n+1
                #hdu.writeto(tilePathn)
                
                if scale > 1.0 and not os.path.exists(tilePathn):
                    fits.writeto(tilePathn, resizedata, header)
                elif not os.path.exists(tilePathn):
                    fits.writeto(tilePathn, data, header)

                
                tileFileName = tileFileNamen
                tilePath = tilePathn

                log("x: %d - y: %d - aw: %f - ah: %f - scale: %f" % (x , y, aw, ah , scale) )

        else:
            ccl = "convert %s -crop %dx%d+%d+%d -quality 95 %s" % (SRC_PATH, aw, ah, x, y, tilePath)
            if USE_GRAPHICSMAGICK:
                ccl = "%s %s" % ("gm", ccl)
            os.system(ccl)
            log("Cropping: %s" % ccl, 3)
            if scale > 1.0:
                ccl = "convert %s -resize %dx%d -quality 95 %s" % (tilePath, aw/scale, ah/scale, tilePath)
                if USE_GRAPHICSMAGICK:
                    ccl = "%s %s" % ("gm", ccl)
                os.system(ccl)
                log("Rescaling %s" % ccl, 3)
    # generate ZUIST region and image object
    regionEL = ET.SubElement(rootEL, "region")
    regionEL.set("id", "R%s-%s" % (ID_PREFIX, tileIDstr))
    objectEL = ET.SubElement(regionEL, "resource")
    if parentRegionID is None:
        if BACKGROUND:
            regionEL.set("levels", "0;%d" % (levelCount-1+DL))
        else:
            regionEL.set("levels", "0")
        # make sure lowest res tile, visible on each level, is always drawn below higher-res tiles
        objectEL.set("z-index", "0")
    else:
        regionEL.set("containedIn", parentRegionID)
        regionEL.set("levels", str(level+DL))
        # make sure lowest res tile, visible on each level, is always drawn below higher-res tiles
        objectEL.set("z-index", "1")
    #regionEL.set("stroke", "red")
    regionEL.set("x", str(int(DX+x+aw/2)))
    regionEL.set("y", str(int(DY-y-ah/2)))
    regionEL.set("w", str(int(aw)))
    regionEL.set("h", str(int(ah)))
    if LAYER:
        regionEL.set("layer", str(LAYER))
    objectEL.set("id", "I%s-%s" % (ID_PREFIX, tileIDstr))
    if USE_ASTROPY:
        #objectEL.set("type", "fits")
        objectEL.set("type", "skyfits")
    else:
        objectEL.set("type", "img")
    objectEL.set("x", str(int(DX+x+aw/2)))
    objectEL.set("y", str(int(DY-y-ah/2)))
    objectEL.set("w", str(int(aw)))
    objectEL.set("h", str(int(ah)))
    if USE_ASTROPY:
        if parentRegionID is None:
            if MINVALUE and MAXVALUE:
                objectEL.set("params", str("reference;sc=%d;minvalue=%f;maxvalue=%f" % (scale, MINVALUE, MAXVALUE) ))
            else:
                objectEL.set("params", str("reference;sc=%d" % (scale) ))
        else:
            if MINVALUE and MAXVALUE:
                objectEL.set("params", str("sc=%d;minvalue=%f;maxvalue=%f" % (scale, MINVALUE, MAXVALUE) ))
            else:
                objectEL.set("params", str("sc=%d" % (scale) ))
    objectEL.set("src", "%d/%s" % (level, tileFileName) )
    if USE_ASTROPY:
        objectEL.set("sensitive", "true")    
    else:
        objectEL.set("sensitive", "false")
    log("Image in scene: scale=%.4f, w=%d, h=%d" % (scale, aw, ah))
    # call to lower level, top left
    buildTiles(tileID, TL, level+1, levelCount, x, y, src_sz, rootEL, im, regionEL.get("id"))
    # call to lower level, top right
    buildTiles(tileID, TR, level+1, levelCount, x+TILE_SIZE*scale/2, y, src_sz, rootEL, im, regionEL.get("id"))
    # call to lower level, bottom left
    buildTiles(tileID, BL, level+1, levelCount, x, y+TILE_SIZE*scale/2, src_sz, rootEL, im, regionEL.get("id"))
    # call to lower level, bottom right
    buildTiles(tileID, BR, level+1, levelCount, x+TILE_SIZE*scale/2, y+TILE_SIZE*scale/2, src_sz, rootEL, im, regionEL.get("id"))

################################################################################
# Create tiles and ZUIST XML scene from source image
################################################################################
def processSrcImg():
    global maxTileCount
    global OUTPUT_FILE_EXT
    global IMG_SRC_PATH
    global WCSDATA
    global OBJECT
    global HEADER
    global MINVALUE
    global MAXVALUE
    global SIZE
    outputSceneFile = "%s/scene.xml" % TGT_DIR
    # prepare the XML scene
    outputroot = ET.Element("scene")
    # source image
    log("Loading source image from %s" % SRC_PATH, 2)
    deleteTmpFile = False
    if USE_CG:
        if SRC_PATH.lower().endswith(".pdf"):
            IMG_SRC_PATH = "%s.png" % SRC_PATH
            log("Generating bitmap from PDF, stored temporarily in %s" % IMG_SRC_PATH, 2)
            pdf_document = CGPDFDocumentCreateWithProvider(CGDataProviderCreateWithFilename(SRC_PATH))
            page_rect = pdf_document.getPage(1).getBoxRect(kCGPDFCropBox)
            log("Default PDF page size: %d x %d" % (page_rect.getWidth(), page_rect.getHeight()), 1)
            page_width = int(page_rect.getWidth() * PDF_SCALE_FACTOR)
            page_height = int(page_rect.getHeight() * PDF_SCALE_FACTOR)
            log("Target bitmap size: %d x %d" % (page_width, page_height), 1)
            bitmap = CGBitmapContextCreateWithColor(page_width, page_height,\
                                                    COLOR_SPACE, (1,1,1,1))
            log("\tRescaling bitmap", 3)
            bitmap.scaleCTM(PDF_SCALE_FACTOR, PDF_SCALE_FACTOR)
            log("\tDrawing PDF to bitmap", 3)
            bitmap.drawPDFDocument(page_rect, pdf_document, 1)
            log("\tWriting bitmap to temp file", 3)
            bitmap.writeToFile(IMG_SRC_PATH, kCGImageFormatPNG)
            deleteTmpFile = True
            return
        else:
            IMG_SRC_PATH = SRC_PATH
        im = CGImageImport(CGDataProviderCreateWithFilename(IMG_SRC_PATH))
        src_sz = (im.getWidth(), im.getHeight())
    elif USE_ASTROPY:
        if SRC_PATH.lower().endswith(".fit") or SRC_PATH.lower().endswith(".fits"):
            IMG_SRC_PATH = SRC_PATH

            hdulist = fits.open(IMG_SRC_PATH, do_not_scale_image_data=True)

            hdulist.info()

            #print hdulist.header

            print hdulist[0].data

            #print hdulist[0].data.dtype
            #print hdulist[0].data.dtype.name

            if hdulist[0].header['NAXIS'] == 2:
                src_sz = (hdulist[0].header['NAXIS1'], hdulist[0].header['NAXIS2'])
                im = hdulist[0].data
                WCSDATA = wcs.WCS(hdulist[0].header)
                HEADER = hdulist[0].header
                OBJECT = hdulist[0].header['OBJECT']
                SIZE = src_sz
            elif hdulist[1].header['NAXIS'] == 2:
                src_sz = (hdulist[1].header['NAXIS1'], hdulist[1].header['NAXIS2'])
                im = hdulist[1].data
                WCSDATA = wcs.WCS(hdulist[1].header)
                HEADER = hdulist[1].header
                OBJECT = hdulist[1].header['OBJECT']
                SIZE = src_sz
            else:
                log("Naxis == %d" % (hdulist[0].header['NAXIS']) )
                return

            hdulist.close()

            #print im
            #print im.dtype
            #print im.dtype.name

            if im.dtype.name == "float64":
                log("Changed data type to float32")
                im = im.astype(np.float32)

            minvalue = np.amin(im)
            maxvalue = np.amax(im)
            if MINVALUE > minvalue:
                MINVALUE = minvalue
            if MAXVALUE < maxvalue:
                MAXVALUE = maxvalue

            OUTPUT_FILE_EXT = "fits"
        else:
            log("It isn't .fits")
            return
    else:
        im = Image.open(SRC_PATH)
        src_sz = im.size
    levelCount = generateLevels(src_sz, outputroot)
    maxTileCount = computeMaxTileCount(levelCount-1, 0)
    log("Maximum number of tiles to be generated: %d" % maxTileCount, 3)
    log("Scene offset (%s,%s)" % (DX, DY), 2)
    log("ID Prefix: %s" % ID_PREFIX, 2)
    buildTiles([0 for i in range(int(levelCount))], TL, 0, levelCount, 0, 0, src_sz, outputroot, im, None)
    # serialize the XML tree
    tree = ET.ElementTree(outputroot)
    log("Writing %s" % outputSceneFile)
    tree.write(outputSceneFile, encoding='utf-8')
    #if deleteTmpFile:
    #    log("Deleting temp file %s" % IMG_SRC_PATH)
    #    os.remove(IMG_SRC_PATH)


def resizeBilinear(data, w, h, aw, ah):
    log("resizeBilinear(%f, %f, %f, %f)" % (w, h, aw, ah))
    w2 = (int)(aw)
    h2 = (int)(ah)
    newdata = np.zeros((w2, h2) )#, dtype=type(data))
    #(np.ndarray)
    x_ratio = (h-1.)/h2
    y_ratio = (w-1.)/w2
    #log("x_ratio: %f - y_ratio: %f" % (x_ratio, y_ratio))
    for i in range(int(w2)):
        for j in range(int(h2)):
            x = x_ratio * i
            y = y_ratio * j
            if x > w2:
                x = w2 - 1
            if y > h2:
                y = h2 - 1
            x_diff = x_ratio * i - x
            y_diff = y_ratio * j - y
            #log("(%d, %d) (%d, %d)" % (x, y, w2, h2))
            a = data[x, y]
            b = data[x + 1, y]
            c = data[x, y + 1]
            d = data[x + 1, y + 1]
            newdata[i, j] = a * ( 1 - x_diff) * (1 - y_diff) + b * (x_diff) * (1 - y_diff) + x * (y_diff) * (1 - x_diff) + d * (x_diff * y_diff) 

    return newdata

def shrink(data, w, h, aw, ah):
    w2 = (int)(aw)
    h2 = (int)(ah)
    newdata = np.zeros((w2, h2), dtype=data.dtype )
    log("newdata: (%f, %f)" % (newdata.shape) )
    log("data: (%f, %f)" % (data.shape) )
    log("scale: %f" % (aw/w) )
    for i in range(w2):
        for j in range(h2):
            idi = i * (w-1)/ w2
            idj = j * (h-1)/ h2
            #log("idx1: %f idx2: %f" % (idx1, idx2))
            try:
                newdata[i, j] = data[idi, idj]
            except IndexError:
                log("IndexError:  i: %d - j: %d - idi: %d - idj: %d" % (i, j, idi, idj))
    return newdata

nears = dict()

def natural_neighbor(data, w, h, aw, ah):
    log("natural_neighbor")
    w2 = (int)(aw)
    h2 = (int)(ah)
    newdata = np.zeros((w2, h2), dtype=data.dtype )
    scale = (int)((w/w2 + h/h2)/2)
    numbrother = scale/2
    if MAX_NEIGBORHOOD > 0 and numbrother > MAX_NEIGBORHOOD:
        numbrother = MAX_NEIGBORHOOD

    for i in range(w2):
        for j in range(h2):
            idi = i * (w-1)/ w2
            idj = j * (h-1)/ h2
            key = "%d,%d,%d,%d,%d" % (idi, idj, int(w), int(h), numbrother)
            if nears.has_key(key):
                near = nears[key]
            else:
                near = neighborhood(idi, idj, int(w), int(h), numbrother)
                nears[key] = near
            value = 0
            for val in near:
                value = value + data[ val[0], val[1] ] * val[2]
            try:
                newdata[i, j] = value
            except IndexError:
                log("IndexError:  i: %d - j: %d" % (i, j))

    return newdata

def neighborhood(i, j, aw, ah, scale):
    ls = []
    result = []
    for ii in range(int(i-scale), int(i+scale+1)):
        if ii >= 0 and ii < aw:
            for jj in range(int(j-scale), int(j+scale+1)):
                if jj >= 0 and jj < ah:
                    distance = math.sqrt( (ii-i)*(ii-i)+(jj-j)*(jj-j) )
                    if distance == 0:
                        factor = 1
                    else:
                        factor = 1/distance
                    #print "(%i, %i) - distance = %f - factor = %f" % (ii, jj, distance, factor)
                    if factor > 0.01:
                        ls.append([ii,jj,factor])
    sumfac = 0
    for l in ls:
        sumfac = sumfac + l[2]
    for l in range(0, len(ls)):
        ls[l][2] = ls[l][2]/sumfac
        if ls[l][2] > 0.01:
            result.append(ls[l])
    return result




################################################################################
# Trace exec on std output
################################################################################
def log(msg, level=0):
    if level <= TRACE_LEVEL:
        print msg

################################################################################
# main
################################################################################
if len(sys.argv) > 2:
    SRC_PATH = os.path.realpath(sys.argv[1])
    TGT_DIR = os.path.realpath(sys.argv[2])
    if len(sys.argv) > 3:
        for arg in sys.argv[3:]:
            if arg.startswith("-ts="):
                TILE_SIZE = int(arg[4:])
            elif arg == "-f":
                FORCE_GENERATE_TILES = True
                log("Force tile generation")
            elif arg == "-gm":
                USE_GRAPHICSMAGICK = True
            elif arg.startswith("-tl="):
                TRACE_LEVEL = int(arg[4:])
            elif arg == "-cg":
                USE_CG = True
                if SUCCEEDED_IMPORTING_CG:
                    COLOR_SPACE = CGColorSpaceCreateWithName(kCGColorSpaceGenericRGB)
                else:
                    log("CoreGraphics not available")
                    sys.exit(0)
            elif arg == "-im":
                USE_CG = False
                if not SUCCEEDED_IMPORTING_PIL:
                    log("PIL not available")
                    sys.exit(0)
            elif arg == "-astropy":
                USE_ASTROPY = True
                if not SUCCEEDED_IMPORTING_ASTROPY:
                    log("ASTROPY not available")
                    sys.exit(0)
            elif arg.startswith("-idprefix"):
                ID_PREFIX = arg[len("-idprefix="):]
            elif arg.startswith("-dx"):
                DX = int(arg[len("-dx="):])
            elif arg.startswith("-dy"):
                DY = int(arg[len("-dy="):])
            elif arg.startswith("-dl"):
                DL = int(arg[len("-dl="):])
            elif arg.startswith("-scale"):
                PDF_SCALE_FACTOR = float(arg[len("-scale="):])
            elif arg.startswith("-tileprefix"):
                TILE_FILE_PREFIX = str(arg[len("-tileprefix="):])
            elif arg.startswith("-layer"):
                LAYER = str(arg[len("-layer="):])
            elif arg.startswith("-minvalue"):
                MINVALUE = float(arg[len("-minvalue="):])
            elif arg.startswith("-maxvalue"):
                MAXVALUE = float(arg[len("-maxvalue="):])
            elif arg.startswith("-onlyxml"):
                ONLYXML = True
            elif arg.startswith("-shrink"):
                NATURAL_NEIGBOR = False
            elif arg.startswith("-notnewfile"):
                NEWFILE = False
            elif arg.startswith("-withbackground"):
                BACKGROUND = True
            elif arg.startswith("-maxneighborhood"):
                MAX_NEIGBORHOOD = int(arg[len("-maxneighborhood="):])
            else:
                log("Parameters incorrect");
                log(CMD_LINE_HELP);
		sys.exit(0);
            

else:
    log(CMD_LINE_HELP)
    sys.exit(0)

if USE_CG:
    log("--------------------\nUsing Core Graphics")
elif USE_GRAPHICSMAGICK:
    log("--------------------\nUsing GraphicsMagick")
elif USE_ASTROPY:
    log("--------------------\nUsing Astropy library for fits images")
else:
    log("--------------------\nUsing PIL + ImageMagick")
log("Tile Size: %dx%d" % (TILE_SIZE, TILE_SIZE), 1)
createTargetDir()
processSrcImg()
log("--------------------")

