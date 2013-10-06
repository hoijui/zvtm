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

COLOR_SPACE = None

FORCE_GENERATE_TILES = False

# camera focal distance
F = 100.0
# camera max altitude
MAX_ALT = "100000"

################################################################################
# Create target directory if it does not exist yet
################################################################################
def createTargetDir():
    if not os.path.exists(TGT_DIR):
        log("Creating target directory %s" % TGT_DIR, 2)
        os.mkdir(TGT_DIR)

def createLevelDir(tileDir, level):
    if not os.path.exists(tileDir):
        log("Creating tile level directory %s" % tileDir, 3)
        levelDir = "%s/L%02d" % (TGT_DIR, level)
        if not os.path.exists(levelDir):
            os.mkdir(levelDir)
        os.mkdir(tileDir)

################################################################################
# Create target directory if it does not exist yet
################################################################################
def analyzeTree():
    # walk subtile dirs and generate pyramid
    TIFF_FILES = glob.glob("%s/*.tif" % SRC_DIR)
    TIFF_FILES.sort()
    initialized = False
    maxCol = minCol = 0
    maxRow = minRow = 0
    for tiffFile in TIFF_FILES:
        tokens = tiffFile.split("/")[-1].split("_")
        col = int(tokens[2])
        row = int(tokens[3])
        if initialized:
            if col > maxCol:
                maxCol = col
            if col < minCol:
                minCol = col
            if row > maxRow:
                maxRow = row
            if row < minRow:
                minRow = row
        else:
            maxCol = minCol = col
            maxRow = minRow = row
            initialized = True
    log("Processing Columns %s-%s x Rows %s-%s" % (minCol, maxCol, minRow, maxRow), 1)
    return (minCol, maxCol, minRow, maxRow)


################################################################################
# Create target directory if it does not exist yet
################################################################################
def processSrcDir(levelCount):
    # walk src dir and perform tile subtiling
    TIFF_FILES = glob.glob("%s/*.tif" % SRC_DIR)
    counter = 0
    for tiffFile in TIFF_FILES:
        counter = counter + 1
        log("Subtiling: %s" % tiffFile.split("/")[-1], 2)
        log("--- %3.1f%%" % (100 * counter/float(len(TIFF_FILES))), 2)
        tileTile(tiffFile, levelCount)


################################################################################
# Further tile original tiles
################################################################################
def tileTile(tiffFile, levelCount):
    tokens = tiffFile.split("_")
    col = int(tokens[-3])
    row = int(tokens[-2])
    # load tile
    im = CGImageImport(CGDataProviderCreateWithFilename(tiffFile))
    src_sz = (im.getWidth(), im.getHeight())
    if src_sz[0] != SRC_TILE_SIZE or src_sz[1] != SRC_TILE_SIZE:
        log("WARNING: unexpected tile dimensions: (%d,%d) for %s" % (src_sz[0], src_sz[1], tiffFile.split("/")[-1]))
    # split it in NB_SUBTILES x NB_SUBTILES tiles
    tileDir = "%s/L%02d/%d_%d" % (TGT_DIR, levelCount-1, col, row)
    createLevelDir(tileDir, levelCount-1)
    for i in range(NB_SUBTILES):
        for j in range(NB_SUBTILES):
            subTileName = "%d_%d-%d_%d" % (col, row, i, NB_SUBTILES-j-1)
            subTilePath = "%s/%s.%s" % (tileDir, subTileName, OUTPUT_FILE_EXT)
            if os.path.exists(subTilePath) and not FORCE_GENERATE_TILES:
                log("%s already exists (skipped)" % (subTilePath), 3)
            else:
                log("Generating tile %s" % (subTilePath), 3)
                log("Cropping at (%d,%d,%d,%d)" % (i*TGT_TILE_SIZE, j*TGT_TILE_SIZE, TGT_TILE_SIZE, TGT_TILE_SIZE), 3)
                cim = im.createWithImageInRect(CGRectMake(i*TGT_TILE_SIZE, j*TGT_TILE_SIZE, TGT_TILE_SIZE, TGT_TILE_SIZE))
                bitmap = CGBitmapContextCreateWithColor(TGT_TILE_SIZE, TGT_TILE_SIZE, COLOR_SPACE, CGFloatArray(4))
                bitmap.setInterpolationQuality(kCGInterpolationHigh)
                rect = CGRectMake(0, 0, TGT_TILE_SIZE, TGT_TILE_SIZE)
                bitmap.drawImage(rect, cim)
                bitmap.writeToFile(subTilePath, kCGImageFormatPNG)


################################################################################
# Count number of levels in ZUIST scene
# (source image min/max cols/rows in IGN coords, elementtree XML root)
################################################################################
def generateLevels(cr_coords, rootEL):
    # number of horizontal tiles at lowest level
    # /10 because src tile IDs increment by 10
    hsz = ((cr_coords[1]-cr_coords[0])/10+1) * SRC_TILE_SIZE
    htc = hsz / TGT_TILE_SIZE
    # number of vertical tiles at lowest level
    # /10 because src tile IDs increment by 10
    vsz = ((cr_coords[3]-cr_coords[2])/10+1) * SRC_TILE_SIZE
    vtc = vsz / TGT_TILE_SIZE
    log("Scene size: %d x %d" % (hsz, vsz), 2)
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
# for all IGN tiles, generate pyramid, build regions and resource descriptions
# (source image min/max cols/rows in IGN coords, number of levels overall, elementtree XML root)
################################################################################
def buildScene(cr_coords, levelCount, rootEL):
    depth = levelCount-1
    counter = 0
    cols = range(cr_coords[0]/10,cr_coords[1]/10+1)
    rows = range(cr_coords[2]/10,cr_coords[3]/10+1)
    total = len(cols) * len(rows)
    for col in cols:
        # /10 because src tile IDs increment by 10
        for row in rows:
            col10 = col * 10
            row10 = row * 10
            counter = counter + 1
            log("--- %3.1f%%" % (100 * counter/float(total)), 2)
            for subcol in range(SRC_TILE_SIZE/TGT_TILE_SIZE):
                for subrow in range(SRC_TILE_SIZE/TGT_TILE_SIZE):
                    regionEL = ET.SubElement(rootEL, "region")
                    regionEL.set("id", "R%d_%d-%d_%d-%d" % (depth, col10, row10, subcol, subrow))
                    regionEL.set("containedIn", "R%d_%d-%d_%d-%d" % (depth-1, col10, row10, subcol/2, subrow/2))
                    #XXX FOR DEBUGGING
                    #regionEL.set("levels", "0;%d" % depth)
                    regionEL.set("levels", str(int(depth)))
                    x = col*SRC_TILE_SIZE + (subcol+.5)*TGT_TILE_SIZE
                    y = row*SRC_TILE_SIZE + (subrow+.5)*TGT_TILE_SIZE
                    regionEL.set("x", str(int(x)))
                    regionEL.set("y", str(int(y)))
                    regionEL.set("w", str(int(TGT_TILE_SIZE)))
                    regionEL.set("h", str(int(TGT_TILE_SIZE)))
                    objectEL = ET.SubElement(regionEL, "resource")
                    objectEL.set("id", "I%d_%d-%d_%d-%d" % (depth, col10, row10, subcol, subrow))
                    # make sure lowest res tile, visible on each level, is always drawn below higher-res tiles
                    objectEL.set("z-index", "10")
                    objectEL.set("type", "img")
                    objectEL.set("x", str(int(x)))
                    objectEL.set("y", str(int(y)))
                    objectEL.set("w", str(int(TGT_TILE_SIZE)))
                    objectEL.set("h", str(int(TGT_TILE_SIZE)))
                    objectEL.set("src", "L%02d/%d_%d/%d_%d-%d_%d.png" % (depth, col10, row10, col10, row10, subcol, subrow))
            buildUpperLevel(col10, row10, depth-1, 2, SRC_TILE_SIZE/TGT_TILE_SIZE/2, outputroot)


################################################################################
# for a given IGN tile, generate lower levels from subtiles
# (IGN tile coords, number of levels overall, elementtree XML root)
################################################################################
def buildUpperLevel(col10, row10, levelDepth, scale, subdivisions, rootEL):
    log("Aggregating tiles (level %d) for tile %02d_%02d" % (levelDepth, col10, row10), 2)
    tileDir = "%s/L%02d/%d_%d" % (TGT_DIR, levelDepth, col10, row10)
    createLevelDir(tileDir, levelDepth)
    for agcol in range(subdivisions):
        for agrow in range(subdivisions):
            aggregateTiles(col10, row10, levelDepth, scale, agcol, agrow, rootEL)
    if subdivisions >= 2:
        buildUpperLevel(col10, row10, levelDepth-1, scale*2, subdivisions/2, rootEL)

def aggregateTiles(col10, row10, levelDepth, scale, agcol, agrow, rootEL):
    # lower level tile dir
    lltDir = "%s/L%02d/%d_%d" % (TGT_DIR, levelDepth+1, col10, row10)
    agTilePath = "%s/L%02d/%d_%d/%d_%d-%d_%d.png" % (TGT_DIR, levelDepth, col10, row10, col10, row10, agcol, agrow)
    if os.path.exists(agTilePath) and not FORCE_GENERATE_TILES:
        log("%s already exists (skipped)" % (agTilePath), 3)
    else:
        log("Aggregating tile %s" % agTilePath, 3)
        bitmap = CGBitmapContextCreateWithColor(TGT_TILE_SIZE, TGT_TILE_SIZE, COLOR_SPACE, CGFloatArray(4))
        bitmap.setInterpolationQuality(kCGInterpolationHigh)
        thsz = TGT_TILE_SIZE/2
        # load upper left, upper right, lower left amd lower right tiles to be aggregated into a single tile
        for i in range(2):
            for j in range(2):
                im = CGImageImport(CGDataProviderCreateWithFilename("%s/%d_%d-%d_%d.png" % (lltDir, col10, row10, 2*agcol+i, 2*agrow+j)))
                rect = CGRectMake(i*thsz, j*thsz, thsz, thsz)
                bitmap.drawImage(rect, im)
        bitmap.writeToFile(agTilePath, kCGImageFormatPNG)
    regionEL = ET.SubElement(rootEL, "region")
    regionEL.set("id", "R%d_%d-%d_%d-%d" % (levelDepth, col10, row10, agcol, agrow))
    regionEL.set("containedIn", "R%d_%d-%d_%d-%d" % (levelDepth-1, col10, row10, agcol/2, agrow/2))
    regionEL.set("levels", str(int(levelDepth)))
    scsz = TGT_TILE_SIZE*scale
    x = col10/10*SRC_TILE_SIZE + (agcol+.5)*scsz
    y = row10/10*SRC_TILE_SIZE + (agrow+.5)*scsz
    regionEL.set("x", str(int(x)))
    regionEL.set("y", str(int(y)))
    regionEL.set("w", str(int(scsz)))
    regionEL.set("h", str(int(scsz)))
    objectEL = ET.SubElement(regionEL, "resource")
    objectEL.set("id", "I%d_%d-%d_%d-%d" % (levelDepth, col10, row10, agcol, agrow))
    # make sure lowest res tile, visible on each level, is always drawn below higher-res tiles
    objectEL.set("z-index", "10")
    objectEL.set("type", "img")
    objectEL.set("x", str(int(x)))
    objectEL.set("y", str(int(y)))
    objectEL.set("w", str(int(scsz)))
    objectEL.set("h", str(int(scsz)))
    objectEL.set("src", agTilePath)

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
log("--------------------------------------------------------------------------")
log("Tile Size: %dx%d" % (TGT_TILE_SIZE, TGT_TILE_SIZE), 1)
createTargetDir()
outputSceneFile = "%s/scene.xml" % TGT_DIR
outputroot = ET.Element("scene")
# subtile tiles
cr_coords = analyzeTree()
# generate ZUIST levels
levelCount = generateLevels(cr_coords, outputroot)
log("---------------------------- Subtiling -----------------------------------")
processSrcDir(levelCount)
# build ZUIST pyramid
log("------------------ Build scene (aggregate tiles) -------------------------")
buildScene(cr_coords, levelCount, outputroot)
# serialize the XML tree
tree = ET.ElementTree(outputroot)
log("Writing %s" % outputSceneFile)
tree.write(outputSceneFile, encoding='utf-8')
log("--------------------")
