#!/usr/bin/env python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2009-2019. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

import os, sys, math
from copy import copy

# http://effbot.org/zone/element-index.htm
import xml.etree.ElementTree as ET

SUCCEEDED_IMPORTING_PIL = True
SUCCEEDED_IMPORTING_CG = True
GRAYSCALE = False

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
    " \timageTiler <src_image_path> <target_dir> [options]\n\n" + \
    "Options:\n\n"+\
    "\t-cg\t\tprocessing pipeline: CoreGraphics (Mac only)\n"+\
    "\t-im\t\tprocessing pipeline: PIL and ImageMagick (default)\n"+\
    "\t-gm\t\tprocessing pipeline: GraphicsMagick\n"+\
    "\t-ts=N\t\ttile size (N in pixels)\n"+\
    "\t-f\t\tforce tile generation\n"+\
    "\t-tl=N\t\ttrace level (N in [0:3])\n"+\
    "\t-idprefix=p\tcustom prefix for all region and objects IDs\n"+\
    "\t-dx=y\t\tx offset for all regions and objects\n"+\
    "\t-dy=x\t\ty offset for all regions and objects\n"+\
    "\t-dl=l\t\tlevel offset for all regions and objects\n"+\
    "\t-scale=s\ts scale factor w.r.t default size for PDF input\n"+\
    "\t-im=<i>\t\t<i> one of {bilinear,bicubic,nearestNeighbor}\n"+\
    "\t-format=t\tt output tiles in PNG (png), JPEG (jpg) or TIFF (tiff)\n"

TRACE_LEVEL = 1

FORCE_GENERATE_TILES = False

TILE_SIZE = 512

OUTPUT_TYPE_PNG = "png"
OUTPUT_TYPE_JPEG = "jpg"
OUTPUT_TYPE_TIFF = "tiff"
OUTPUT_TYPE = OUTPUT_TYPE_PNG

# OUTPUT_TYPE2CG = {
#     OUTPUT_TYPE_PNG: kCGImageFormatPNG,
#     OUTPUT_TYPE_JPEG: kCGImageFormatJPEG,
#     OUTPUT_TYPE_TIFF: kCGImageFormatTIFF
# }

# camera focal distance
F = 100.0
# camera max altitude
MAX_ALT = "100000"

# prefix for image tile files
TILE_FILE_PREFIX = "tile-"

PROGRESS = 0

# X offset
DX = 0
# Y offset
DY = 0
# level offset
DL = 0

INTERPOLATION = "nearestNeighbor"

ID_PREFIX = ""

PDF_SCALE_FACTOR = 5

COLOR_SPACE = None

USE_CG = False
USE_GRAPHICSMAGICK = False

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
    tileFileName = "%s%s.%s" % (TILE_FILE_PREFIX, tileIDstr, OUTPUT_TYPE)
    levelTilePath = "%s/%s" % (TGT_DIR, level)
    relTilePath = "%s/%s" % (level, tileFileName)
    fullTilePath = "%s/%s" % (levelTilePath, tileFileName)
    if not os.path.exists(levelTilePath):
        log("Creating dir %s" % levelTilePath, 2)
        os.mkdir(levelTilePath)
    aw = ah = TILE_SIZE*scale
    if x + TILE_SIZE*scale > src_sz[0]:
        aw = int(src_sz[0] - x)
    if y + TILE_SIZE*scale > src_sz[1]:
        ah = int(src_sz[1] - y)
    if aw == 0 or ah == 0:
        return
    if os.path.exists(fullTilePath) and not FORCE_GENERATE_TILES:
        log("---- %.2f%%\n%s already exists (skipped)" % (PROGRESS/float(maxTileCount)*100, fullTilePath), 1)
    else:
        log("---- %.2f%%\nGenerating tile %s" % (PROGRESS/float(maxTileCount)*100, tileIDstr), 1)
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
            bitmap.writeToFile(fullTilePath, OUTPUT_TYPE2CG[OUTPUT_TYPE])
        else:
            ccl = "convert %s -crop %dx%d+%d+%d -quality 95 %s" % (SRC_PATH, aw, ah, x, y, fullTilePath)
            if USE_GRAPHICSMAGICK:
                ccl = "%s %s" % ("gm", ccl)
            os.system(ccl)
            log("Cropping: %s" % ccl, 3)
            if scale > 1.0:
                ccl = "convert %s -resize %dx%d -quality 95 %s" % (fullTilePath, aw/scale, ah/scale, fullTilePath)
                if USE_GRAPHICSMAGICK:
                    ccl = "%s %s" % ("gm", ccl)
                os.system(ccl)
                log("Rescaling %s" % ccl, 3)
    # generate ZUIST region and image object
    regionEL = ET.SubElement(rootEL, "region")
    regionEL.set("id", "R%s-%s" % (ID_PREFIX, tileIDstr))
    objectEL = ET.SubElement(regionEL, "resource")
    if parentRegionID is None:
        regionEL.set("levels", "0;%d" % (levelCount-1+DL))
        # make sure lowest res tile, visible on each level, is always drawn below higher-res tiles
        objectEL.set("z-index", "0")
    else:
        regionEL.set("containedIn", parentRegionID)
        regionEL.set("levels", str(level+DL))
        # make sure lowest res tile, visible on each level, is always drawn below higher-res tiles
        objectEL.set("z-index", "1")
    regionEL.set("x", str(int(DX+x+aw/2)))
    regionEL.set("y", str(int(DY-y-ah/2)))
    regionEL.set("w", str(int(aw)))
    regionEL.set("h", str(int(ah)))
    objectEL.set("id", "I%s-%s" % (ID_PREFIX, tileIDstr))
    objectEL.set("type", "img")
    objectEL.set("x", str(int(DX+x+aw/2)))
    objectEL.set("y", str(int(DY-y-ah/2)))
    objectEL.set("w", str(int(aw)))
    objectEL.set("h", str(int(ah)))
    objectEL.set("src", relTilePath)
    objectEL.set("params", "im=%s" % INTERPOLATION)
    objectEL.set("sensitive", "false")
    log("Image in scene: scale=%.4f, w=%d, h=%d" % (scale, aw, ah), 2)
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
                                                    COLOR_SPACE, CGFloatArray(4))
            log("\tRescaling bitmap", 3)
            page_rect.size.width = page_width
            page_rect.size.height = page_height
            log("\tDrawing PDF to bitmap", 3)
            bitmap.drawPDFDocument(page_rect, pdf_document, 1)
            log("\tWriting bitmap to temp file", 3)
            bitmap.writeToFile(IMG_SRC_PATH, OUTPUT_TYPE2CG[OUTPUT_TYPE])
            deleteTmpFile = True
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
    log("Writing %s" % outputSceneFile, 1)
    tree.write(outputSceneFile, encoding='utf-8')
    if deleteTmpFile:
        log("Deleting temp file %s" % IMG_SRC_PATH, 3)
        os.remove(IMG_SRC_PATH)

################################################################################
# Trace exec on std output
################################################################################
def log(msg, level=0):
    if level <= TRACE_LEVEL:
        print(msg)

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
                if not SUCCEEDED_IMPORTING_CG:
                    log("CoreGraphics not available")
                    sys.exit(0)
            elif arg == "-im":
                USE_CG = False
                if not SUCCEEDED_IMPORTING_PIL:
                    log("PIL not available")
                    sys.exit(0)
            elif arg == "-gs":
                GRAYSCALE = True
            elif arg.startswith("-idprefix"):
                ID_PREFIX = arg[len("-idprefix="):]
            elif arg.startswith("-dx"):
                DX = int(arg[len("-dx="):])
            elif arg.startswith("-dy"):
                DY = int(arg[len("-dy="):])
            elif arg.startswith("-dl"):
                DL = int(arg[len("-dl="):])
            elif arg.startswith("-format"):
                OUTPUT_TYPE = arg[len("-format="):]
            elif arg.startswith("-scale="):
                PDF_SCALE_FACTOR = float(arg[len("-scale="):])
            elif arg.startswith("-im="):
                INTERPOLATION = arg[len("-im="):]
else:
    log(CMD_LINE_HELP)
    sys.exit(0)

if USE_CG:
    log("--------------------\nUsing Core Graphics")
    if GRAYSCALE:
        COLOR_SPACE = CGColorSpaceCreateWithName(kCGColorSpaceGenericGray)
        log("Grayscale mode")
    else:
        COLOR_SPACE = CGColorSpaceCreateWithName(kCGColorSpaceGenericRGB)
        log("RGB mode")
elif USE_GRAPHICSMAGICK:
    log("--------------------\nUsing GraphicsMagick")
else:
    log("--------------------\nUsing PIL + ImageMagick")
log("Tile Size: %dx%d" % (TILE_SIZE, TILE_SIZE), 1)
createTargetDir()
processSrcImg()
log("--------------------")
