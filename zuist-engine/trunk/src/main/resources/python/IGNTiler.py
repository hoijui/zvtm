#!/usr/bin/env python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2013. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

import os, sys, math, glob
from copy import copy

# http://effbot.org/zone/element-index.htm
import xml.etree.ElementTree as ET

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
    SUCCEEDED_IMPORTING_PIL = False

CMD_LINE_HELP = "ZUIST IGN Tiling Script\n\nUsage:\n\n" + \
    " \tIGNtiler <src_tiles_path> <target_dir> [options]\n\n" + \
    "Options:\n\n"+\
    "\t-f\t\tforce tile generation\n"+\
    "\t-tl=N\t\ttrace level (N in [0:3])\n"

TRACE_LEVEL = 1

SRC_TILE_SIZE = 4000
TGT_TILE_SIZE = 500
NB_SUBTILES = SRC_TILE_SIZE / TGT_TILE_SIZE

OUTPUT_FILE_EXT = "png"

PROGRESS = 0

COLOR_SPACE = None

FORCE_GENERATE_TILES = False

################################################################################
# Create target directory if it does not exist yet
################################################################################
def createTargetDir():
    if not os.path.exists(TGT_DIR):
        log("Creating target directory %s" % TGT_DIR, 2)
        os.mkdir(TGT_DIR)

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
# Create target directory if it does not exist yet
################################################################################
def processSrcDir():
    outputSceneFile = "%s/scene.xml" % TGT_DIR
    # prepare the XML scene
    outputroot = ET.Element("scene")
    # walk src dir
    TIFF_FILES = glob.glob("%s/*.tif" % SRC_DIR)
    tokens = TIFF_FILES[0].split("_")
    # seek min/max cols/rows
    minCol = int(tokens[-3])
    maxCol = int(tokens[-3])
    minRow = int(tokens[-2])
    maxRow = int(tokens[-2])
    for tiffFile in TIFF_FILES[1:]:
        tokens = tiffFile.split("_")
        col = int(tokens[-3])
        if col > maxCol:
            maxCol = col
        if col < minCol:
            minCol = col
        row = int(tokens[-2])
        if row > maxRow:
            maxRow = row
        if row < minRow:
            minRow = row
    log("Processing Columns %s-%s x Rows %s-%s" % (minCol, maxCol, minRow, maxRow), 1)
    counter = 0
    for tiffFile in TIFF_FILES:
        counter = counter + 1
        log("Subtiling: %s" % tiffFile.split("/")[-1], 2)
        log("--- %3.1f%%" % (100 * counter/float(len(TIFF_FILES))), 2)
        tileTile(tiffFile)
    # serialize the XML tree
    tree = ET.ElementTree(outputroot)
    log("Writing %s" % outputSceneFile)
    tree.write(outputSceneFile, encoding='utf-8')

################################################################################
# Further tile original tiles
################################################################################
def tileTile(tiffFile):
    tokens = tiffFile.split("_")
    col = int(tokens[-3])
    row = int(tokens[-2])
    # load tile
    im = CGImageImport(CGDataProviderCreateWithFilename(tiffFile))
    src_sz = (im.getWidth(), im.getHeight())
    if src_sz[0] != SRC_TILE_SIZE or src_sz[1] != SRC_TILE_SIZE:
        log("WARNING: unexpected tile dimensions: (%d,%d) for %s" % (src_sz[0], src_sz[1], tiffFile.split("/")[-1]))
    # split it in NB_SUBTILES x NB_SUBTILES tiles
    tileDir = "%s/%d_%d" % (TGT_DIR, col, row)
    if not os.path.exists(tileDir):
        log("Creating tile directory %s" % tileDir, 3)
        os.mkdir(tileDir)
    for i in range(NB_SUBTILES):
        for j in range(NB_SUBTILES):
            subTileName = "%d_%d-%d_%d" % (col, row, i, NB_SUBTILES-j-1)
            subTilePath = "%s/%s.%s" % (tileDir, subTileName, OUTPUT_FILE_EXT)
            if os.path.exists(subTilePath) and not FORCE_GENERATE_TILES:
                log("%s already exists (skipped)" % (subTilePath), 2)
            else:
                log("Generating tile %s" % (subTilePath), 2)
                log("Cropping at (%d,%d,%d,%d)" % (i*TGT_TILE_SIZE, j*TGT_TILE_SIZE, TGT_TILE_SIZE, TGT_TILE_SIZE), 3)
                cim = im.createWithImageInRect(CGRectMake(i*TGT_TILE_SIZE, j*TGT_TILE_SIZE, TGT_TILE_SIZE, TGT_TILE_SIZE))
                bitmap = CGBitmapContextCreateWithColor(TGT_TILE_SIZE, TGT_TILE_SIZE, COLOR_SPACE, CGFloatArray(4))
                bitmap.setInterpolationQuality(kCGInterpolationHigh)
                rect = CGRectMake(0, 0, TGT_TILE_SIZE, TGT_TILE_SIZE)
                bitmap.drawImage(rect, cim)
                bitmap.writeToFile(subTilePath, kCGImageFormatPNG)

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
    SRC_DIR = os.path.realpath(sys.argv[1])
    TGT_DIR = os.path.realpath(sys.argv[2])
    if SUCCEEDED_IMPORTING_CG:
        COLOR_SPACE = CGColorSpaceCreateWithName(kCGColorSpaceGenericRGB)
    else:
        log("CoreGraphics not available")
        sys.exit(0)
    if len(sys.argv) > 3:
        for arg in sys.argv[3:]:
            if arg == "-f":
                FORCE_GENERATE_TILES = True
                log("Force tile generation")
            elif arg.startswith("-tl="):
                TRACE_LEVEL = int(arg[4:])
else:
    log(CMD_LINE_HELP)
    sys.exit(0)

log("Tile Size: %dx%d" % (TGT_TILE_SIZE, TGT_TILE_SIZE), 1)
createTargetDir()
processSrcDir()
log("--------------------")
