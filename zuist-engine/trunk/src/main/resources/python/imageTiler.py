#!/usr/bin/python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2009. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

###############################################################################
# Only 32-bit Python will work with CG bindings on Mac OS X 10.6 Snow Leopard
# make sure you

#    export VERSIONER_PYTHON_PREFER_32_BIT=yes

# before calling this script
###############################################################################

import os, sys, math
from copy import copy

# http://effbot.org/zone/element-index.htm
import elementtree.ElementTree as ET

SUCCEEDED_IMPORTING_PIL = True
SUCCEEDED_IMPORTING_CG = True

# http://developer.apple.com/documentation/GraphicsImaging/Conceptual/drawingwithquartz2d/dq_python/dq_python.html
try:
    from CoreGraphics import *
except ImportError:
    SUCCEEDED_IMPORTING_CG = False

# http://www.pythonware.com/products/pil/
try:
    from PIL import Image
except ImportError:
    SUCCEEDED_IMPORTING_PIL = True

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

CMD_LINE_HELP = "ZUIST Image Tiling Script\n\nUsage:\n\n" + \
    " \timageTyler <src_image_path> <target_dir> [options]\n\n" + \
    "Options:\n\n"+\
    "\t-cg\t\tprocessing pipeline: CoreGraphics (Mac only)\n"+\
    "\t-im\t\tprocessing pipeline: PIL and ImageMagick (default)\n"+\
    "\t-ts=N\t\ttile size (N in pixels)\n"+\
    "\t-f\t\tforce tile generation\n"+\
    "\t-tl=N\t\ttrace level (N in [0:3])\n"+\
    "\t-idprefix=p\tcustom prefix for all region and objects IDs\n"+\
    "\t-dx=y\t\tx offset for all regions and objects\n"+\
    "\t-dy=x\t\ty offset for all regions and objects\n"+\
    "\t-scale=s\ts scale factor w.r.t default size for PDF input\n"

TRACE_LEVEL = 1

FORCE_GENERATE_TILES = False
USE_CG = False

TILE_SIZE = 500

OUTPUT_FILE_EXT = "png"

# camera focal distance
F = 100.0
# camera max altitude
MAX_ALT = "100000"

# prefix for image tile files
TILE_FILE_PREFIX = "tile-"

PROGRESS = 0

DX = 0
DY = 0

ID_PREFIX = ""

PDF_SCALE_FACTOR = 5

COLOR_SPACE = CGColorSpaceCreateWithName(kCGColorSpaceGenericRGB)

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
        level.set("depth", str(depth))
        level.set("floor", str(altitudes[-2]))
        level.set("ceiling", str(altitudes[-1]))
    # fix max scene altitude (for highest region)
    level.set("ceiling", MAX_ALT)
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
    tilePath = "%s/%s" % (TGT_DIR, tileFileName)
    aw = ah = TILE_SIZE*scale
    if x + TILE_SIZE*scale > src_sz[0]:
        aw = int(src_sz[0] - x)
    if y + TILE_SIZE*scale > src_sz[1]:
        ah = int(src_sz[1] - y)
    if aw == 0 or ah == 0:
        return
    if os.path.exists(tilePath) and not FORCE_GENERATE_TILES:
        log("---- %.2f%%\n%s already exists (skipped)" % (PROGRESS/float(maxTileCount)*100, tilePath), 2)
    else:    
        log("---- %.2f%%\nGenerating tile %s" % (PROGRESS/float(maxTileCount)*100, tileIDstr), 2)
        if USE_CG:
            # this will work only with a Mac
            w = h = int(TILE_SIZE)
            log("Cropping at (%d,%d,%d,%d)" % (x, y, aw, ah), 3)
            cim = im.createWithImageInRect(CGRectMake(int(x), int(y), int(aw), int(ah)))
            log("Resizing to (%d, %d)" % (aw/scale, ah/scale), 3)
            bitmap = CGBitmapContextCreateWithColor(int(aw/scale), int(ah/scale), COLOR_SPACE, (0,0,0,1))
            bitmap.setInterpolationQuality(kCGInterpolationHigh)
            rect = CGRectMake(0, 0, int(aw/scale), int(ah/scale))
            bitmap.drawImage(rect, cim)
            bitmap.writeToFile(tilePath, kCGImageFormatPNG)
        else:
            ccl = "convert %s -crop %dx%d+%d+%d -quality 95 %s" % (SRC_PATH, aw, ah, x, y, tilePath)
            os.system(ccl)
            log("Cropping: %s" % ccl, 3)
            if scale > 1.0:
                ccl = "convert %s -resize %dx%d -quality 95 %s" % (tilePath, aw/scale, ah/scale, tilePath)
                os.system(ccl)
                log("Rescaling %s" % ccl, 3)
    # generate ZUIST region and image object
    regionEL = ET.SubElement(rootEL, "region")
    regionEL.set("id", "R%s-%s" % (ID_PREFIX, tileIDstr))
    if parentRegionID is None:
        regionEL.set("levels", "0;%d" % (levelCount-1))
    else:
        regionEL.set("containedIn", parentRegionID)
        regionEL.set("levels", str(level))
    regionEL.set("x", str(int(DX+x+aw/2)))
    regionEL.set("y", str(int(DY-y-ah/2)))
    regionEL.set("w", str(int(aw)))
    regionEL.set("h", str(int(ah)))
    objectEL = ET.SubElement(regionEL, "resource")
    objectEL.set("id", "I%s-%s" % (ID_PREFIX, tileIDstr))
    objectEL.set("type", "image")
    objectEL.set("x", str(int(DX+x+aw/2)))
    objectEL.set("y", str(int(DY-y-ah/2)))
    objectEL.set("w", str(int(aw)))
    objectEL.set("h", str(int(ah)))
    objectEL.set("src", tileFileName)
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
            elif arg.startswith("-tl="):
                TRACE_LEVEL = int(arg[4:])
            elif arg == "-cg":
                USE_CG = True
                if not SUCCEEDED_IMPORTING_CG:
                    log("CoreGraphics not available")
                    sys.exit(0)
            elif arg == "-im":
                USE_CG = False
                if not SUCCEEDED_IMPORTING_PIL:
                    log("PIL not available")
                    sys.exit(0)
            elif arg.startswith("-idprefix"):
                ID_PREFIX = arg[len("-idprefix="):]
            elif arg.startswith("-dx"):
                DX = int(arg[len("-dx="):])
            elif arg.startswith("-dy"):
                DY = int(arg[len("-dy="):])
            elif arg.startswith("-scale"):
                PDF_SCALE_FACTOR = float(arg[len("-scale="):])
else:
    log(CMD_LINE_HELP)
    sys.exit(0)

if USE_CG:
    log("--------------------\nUsing Core Graphics")
else:
    log("--------------------\nUsing PIL + ImageMagick")
log("Tile Size: %dx%d" % (TILE_SIZE, TILE_SIZE), 1)
createTargetDir()
processSrcImg()
log("--------------------")
