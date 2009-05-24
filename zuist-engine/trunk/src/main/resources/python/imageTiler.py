#!/usr/bin/python
# -*- coding: UTF-8 -*-
# $Id$

import os, sys, math
from copy import copy
# http://effbot.org/zone/element-index.htm
import elementtree.ElementTree as ET

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
    "\t-tsN\ttile size (N in pixels)\n"+\
    "\t-f\tforce tile generation\n"+\
    "\t-tlN\ttrace level (N in [0:3])\n"

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
    log("Will generate %s level(s)" % int(res), 2)
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
#  top left coords, size of source image, parent XML element)
################################################################################
def buildTiles(parentTileID, pos, level, levelCount, x, y, src_sz, rootEL, parentRegionID):
    if (level >= levelCount):
        return
    tileID = copy(parentTileID)
    tileID[level] = tileID[level] + pos
    tileIDstr = "-".join([str(s) for s in tileID])
    if x > src_sz[0] or y > src_sz[1]:
        log("Ignoring tile %s (out of bounds)" % tileIDstr, 3)
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
    if os.path.exists(tilePath) and not FORCE_GENERATE_TILES:
        log("%s already exists (skipped)" % tilePath, 2)
    else:    
        log("----\nGenerating tile %s" % tileIDstr, 2)
        if USE_CG:
            print scale
            from CoreGraphics import *
            w = h = int(TILE_SIZE)
            im = CGImageImport(CGDataProviderCreateWithFilename(SRC_PATH))
            log("Cropping at (%s,%s,%s,%s)" % (int(x), int(y), int(aw), int(ah)), 3)
            cim = im.createWithImageInRect(CGRectMake(int(x), int(y), int(aw), int(ah)))
            log("Resizing to (%s, %s)" % (int(aw/scale), int(ah/scale)), 3)
            bitmap = CGBitmapContextCreateWithColor(int(aw/scale), int(ah/scale), CGColorSpaceCreateWithName(kCGColorSpaceGenericRGB), (0,0,0,1))
            bitmap.setInterpolationQuality(kCGInterpolationHigh)
            rect = CGRectMake(0, 0, int(aw/scale), int(ah/scale))
            bitmap.drawImage(rect, cim)
            bitmap.writeToFile(tilePath, kCGImageFormatPNG)
        else:
            ccl = "convert %s -crop %sx%s+%s+%s -quality 95 %s" % (SRC_PATH, str(int(TILE_SIZE*scale)), str(int(TILE_SIZE*scale)), str(int(x)), str(int(y)), tilePath)
            os.system(ccl)
            log("Cropping: %s" % ccl, 3)
            if scale > 1.0:
                ccl = "convert %s -resize %sx%s -quality 95 %s" % (tilePath, str(int(TILE_SIZE)), str(int(TILE_SIZE)), tilePath)
                os.system(ccl)
                log("Rescaling %s" % ccl, 3)
    # generate ZUIST region and image object
    regionEL = ET.SubElement(rootEL, "region")
    regionEL.set("id", "R%s" % tileIDstr)
    if parentRegionID is not None:
        regionEL.set("containedIn", parentRegionID)
    regionEL.set("levels", str(level))
    regionEL.set("x", str(int(x+aw/2)))
    regionEL.set("y", str(int(-y-ah/2)))
    regionEL.set("w", str(int(aw)))
    regionEL.set("h", str(int(ah)))
    objectEL = ET.SubElement(regionEL, "object")
    objectEL.set("id", "I%s" % tileIDstr)
    objectEL.set("type", "image")
    objectEL.set("x", str(int(x+aw/2)))
    objectEL.set("y", str(int(-y-ah/2)))
    objectEL.set("w", str(int(aw)))
    objectEL.set("h", str(int(ah)))
    objectEL.set("src", tileFileName)
    objectEL.set("sensitive", "false")
    # call to lower level, top left
    buildTiles(tileID, TL, level+1, levelCount, x, y, src_sz, rootEL, regionEL.get("id"))
    # call to lower level, top right
    buildTiles(tileID, TR, level+1, levelCount, x+TILE_SIZE*scale/2, y, src_sz, rootEL, regionEL.get("id"))
    # call to lower level, bottom left
    buildTiles(tileID, BL, level+1, levelCount, x, y+TILE_SIZE*scale/2, src_sz, rootEL, regionEL.get("id"))
    # call to lower level, bottom right
    buildTiles(tileID, BR, level+1, levelCount, x+TILE_SIZE*scale/2, y+TILE_SIZE*scale/2, src_sz, rootEL, regionEL.get("id"))
    
################################################################################
# Create tiles and ZUIST XML scene from source image
################################################################################
def processSrcImg():
    outputSceneFile = "%s/scene.xml" % TGT_DIR
    # prepare the XML scene
    outputroot = ET.Element("scene")
    # source image
    log("Loading source image from %s" % SRC_PATH, 2)
    if USE_CG:
        # http://developer.apple.com/documentation/GraphicsImaging/Conceptual/drawingwithquartz2d/dq_python/dq_python.html
        from CoreGraphics import CGImageImport, CGDataProviderCreateWithFilename
        im = CGImageImport(CGDataProviderCreateWithFilename(SRC_PATH))
        src_sz = (im.getWidth(), im.getHeight())
    else:
        # http://www.pythonware.com/products/pil/
        from PIL import Image
        im = Image.open(SRC_PATH)
        src_sz = im.size
    levelCount = generateLevels(src_sz, outputroot)
    buildTiles([0 for i in range(int(levelCount))], TL, 0, levelCount, 0, 0, src_sz, outputroot, None)
    # serialize the XML tree
    tree = ET.ElementTree(outputroot)
    log("Writing %s" % outputSceneFile)
    tree.write(outputSceneFile, encoding='utf-8')

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
            if arg.startswith("-ts"):
                TILE_SIZE = int(arg[3:])
            elif arg == "-f":
                FORCE_GENERATE_TILES = True
                log("Force tile generation")
            elif arg.startswith("-tl"):
                TRACE_LEVEL = int(arg[3:])
            elif arg == "-cg":
                USE_CG = True
else:
    log(CMD_LINE_HELP)
    sys.exit(0)

if USE_CG:
    log("--------------------\nUsing Core Graphics")
else:
    log("--------------------\nUsing PIL + ImageMagick")
log("Tile Size: %sx%s" % (TILE_SIZE, TILE_SIZE), 1)
createTargetDir()
processSrcImg()
log("--------------------")
