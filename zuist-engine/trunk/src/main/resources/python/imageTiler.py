#!/usr/bin/python
# -*- coding: UTF-8 -*-
# $Id$

import os, sys, math
from copy import copy
# http://www.pythonware.com/products/pil/
from PIL import Image
# http://effbot.org/zone/element-index.htm
import elementtree.ElementTree as ET

CMD_LINE_HELP = "ZUIST Image Tiling Script\n\nUsage:\n\n\timageTyler <src_image_path> <target_dir> [tile_size] [trace_level]\n"

TRACE_LEVEL = 1

TILE_SIZE = 500

# camera focal distance
F = 100.0
# camera max altitude
MAX_ALT = "100000"

# prefix for image tile files
IMG_PREFIX = "tile-"

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
def generateLevels(src_sz, tile_sz, parent):
    # number of horizontal tiles at lowest level
    htc = src_sz[0] / tile_sz
    if src_sz[0] % tile_sz > 0:
        htc += 1
    # number of vertical tiles at lowest level
    vtc = src_sz[1] / tile_sz
    if src_sz[1] % tile_sz > 0:
        vtc += 1
    res = math.ceil(max(math.log(vtc,2)+1, math.log(htc,2)+1))
    log("Will generate %s level(s)" % res, 2)
    altitudes = [0,]
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
    levelCount = generateLevels(src_sz, TILE_SIZE, outputroot)
    
    
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
        TILE_SIZE = int(sys.argv[3])
        if len(sys.argv) > 4:
            TRACE_LEVEL = int(sys.argv[4])
else:
    log(CMD_LINE_HELP)
    sys.exit(0)

log("Tile Size: %sx%s" % (TILE_SIZE, TILE_SIZE), 1)
createTargetDir()
processSrcImg()
