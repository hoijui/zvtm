#!/usr/bin/python
# -*- coding: UTF-8 -*-
# $Id$

import os, sys, math
from copy import copy
# http://www.pythonware.com/products/pil/
from PIL import Image
# http://effbot.org/zone/element-index.htm
import elementtree.ElementTree as ET

CMD_LINE_HELP = "ZUIST Image Tiling Script\n\nUsage:\n\n" + \
    " \timageTyler <src_image_path> <target_dir> [options]\n\n" + \
    "Options:\n\n"+\
    "\t-tsN\ttile size (N in pixels)\n"+\
    "\t-f\tforce tile generation\n"+\
    "\t-tlN\ttrace level (N in [0:3])\n"

TRACE_LEVEL = 1

GENERATE_TILES = False

TILE_SIZE = 500

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
# Count number of levels in ZUIST scene (source image size from PIL, tile size)
################################################################################
def generateLevels(src_sz, parent):
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
    log("Will generate %s level(s)" % res, 2)
    # generate ZUIST levels
    altitudes = [-F+1,]
    for i in range(int(res)):
        depth = int(res-i-1)
        altitudes.append(int(F*math.pow(2,i+1)-F))
        level = ET.SubElement(parent, "level")
        level.set("depth", str(depth))
        level.set("floor", str(altitudes[-2]))
        level.set("ceiling", str(altitudes[-1]))
    # fix max scene altitude (for highest region)
    level.set("ceiling", MAX_ALT)
    return res

################################################################################
# 
################################################################################
def buildTiles(parentTileID, inc, level, levelCount, x, y, parent):
    if (level >= levelCount):
        return
    tileID = copy(parentTileID)
    tileID[level] = tileID[level] + inc
    scale = math.pow(2, levelCount-level-1)
    if (level != 0):
        # XXX: HAVE TO CHECK THAT WE ARE WITHIN BOUNDS (otherwise it does not make sense)
        # generate image except for level 0 where we use original image
        tileIDstr = "-".join([str(s) for s in tileID])
        tilePath = "%s/%s%s.jpg" % (TGT_DIR, TILE_FILE_PREFIX, tileIDstr)        
        if os.path.exists(tilePath) or not GENERATE_TILES:
            log("%s already exists (skipped)" % tilePath, 2)
        else:    
            log("----\nGenerating tile %s" % tileIDstr, 2)
            ccl = "convert %s -crop %sx%s+%s+%s -quality 95 %s" % (SRC_PATH, str(int(TILE_SIZE*scale)), str(int(TILE_SIZE*scale)), str(int(x)), str(int(y)), tilePath)
            os.system(ccl)
            log("Cropping: %s" % ccl, 3)
            if scale > 1.0:
                ccl = "convert %s -resize %sx%s -quality 95 %s" % (tilePath, str(int(TILE_SIZE)), str(int(TILE_SIZE)), tilePath)
                os.system(ccl)
                log("Rescaling %s" % ccl, 3)
    # call to lower level, top left
    buildTiles(tileID, 1, level+1, levelCount, x, y, parent)
    # call to lower level, top right
    buildTiles(tileID, 2, level+1, levelCount, x+TILE_SIZE*scale/2, y, parent)
    # call to lower level, bottom left
    buildTiles(tileID, 3, level+1, levelCount, x, y+TILE_SIZE*scale/2, parent)
    # call to lower level, bottom right
    buildTiles(tileID, 4, level+1, levelCount, x+TILE_SIZE*scale/2, y+TILE_SIZE*scale/2, parent)

    
################################################################################
# Create tiles and ZUIST XML scene from source image
################################################################################
def processSrcImg():
    outputSceneFile = "%s/scene.xml" % TGT_DIR
    # prepare the XML scene
    outputroot = ET.Element("scene")
    # source image
    log("Loading source image from %s" % SRC_PATH, 2)
    src_im = Image.open(SRC_PATH)
    src_sz = src_im.size
    levelCount = generateLevels(src_sz, outputroot)
    buildTiles([0 for i in range(int(levelCount))], 1, 0, levelCount, 0, 0, outputroot)
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
                GENERATE_TILES = True
                log("Force tile generation")
            elif arg.startswith("-tl"):
                TRACE_LEVEL = int(arg[3:])
else:
    log(CMD_LINE_HELP)
    sys.exit(0)

log("Tile Size: %sx%s" % (TILE_SIZE, TILE_SIZE), 1)
createTargetDir()
processSrcImg()
