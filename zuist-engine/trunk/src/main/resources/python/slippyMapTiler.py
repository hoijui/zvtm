#!/usr/bin/env python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2015. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id:  $

import os, sys, math, random
from copy import copy

# http://effbot.org/zone/element-index.htm
import xml.etree.ElementTree as ET

CMD_LINE_HELP = "Slippy Map Tiling Script\n\nUsage:\n\n" + \
    " \tslippyMapTiler <TMS_URL> <target_dir> [options]\n\n" + \
    "Options:\n\n"+\
    "\t-ts=N\t\ttile size (N in pixels)\n"+\
    "\t-mzl=N\t\tmaximum zoom level (N in [0,19])\n"+\
    "\t-tl=N\t\ttrace level (N in [0:2])\n"

TRACE_LEVEL = 1

TILE_SIZE = 256
MAX_ZOOM_LEVEL = 3

TMS_URL_PREFIX = "http://a.tile.openstreetmap.org/"

OUTPUT_FILE_EXT = "png"

# camera focal distance
F = 100.0
# camera max altitude
MAX_ALT = "100000"

# prefix for image tile files
TILE_FILE_PREFIX = "t-"

PROGRESS = 0

################################################################################
# TMS URL getter/setter
################################################################################
def setTMSURL(url_p):
    if (url_p.endswith("/")):
        TMS_URL_PREFIX = url_p
    else:
        TMS_URL_PREFIX = "%s/" % url_p

def getTMSURL():
    #return "http://tile.stamen.com/terrain-background/"
    #return "http://otile%d.mqcdn.com/tiles/1.0.0/sat/" % math.ceil(random.random()*4)
    #return "http://otile%d.mqcdn.com/tiles/1.0.0/osm/" % math.ceil(random.random()*4)
    return TMS_URL_PREFIX

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
def generateLevels(mzl, rootEL):
    # number of levels
    res = mzl + 1
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
    return res

################################################################################
# Create tiles and ZUIST XML scene from source image
################################################################################
def generateTree():
    outputSceneFile = "%s/scene.xml" % TGT_DIR
    # prepare the XML scene
    outputroot = ET.Element("scene")
    # source image
    levelCount = generateLevels(MAX_ZOOM_LEVEL, outputroot)
    log("TMS URL prefix: "+getTMSURL())
    #########buildTiles([0 for i in range(int(levelCount))], TL, 0, levelCount, 0, 0, src_sz, outputroot, im, None)
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
if len(sys.argv) > 1:
    TGT_DIR = os.path.realpath(sys.argv[1])
    if len(sys.argv) > 2:
        for arg in sys.argv[2:]:
            if arg.startswith("-ts="):
                TILE_SIZE = int(arg[4:])
            elif arg.startswith("-url="):
                TMS_URL = arg[5:]
            elif arg.startswith("-mzl="):
                MAX_ZOOM_LEVEL = int(arg[5:])
            elif arg.startswith("-tl="):
                TRACE_LEVEL = int(arg[4:])
else:
    log(CMD_LINE_HELP)
    sys.exit(0)

log("Tile Size: %dx%d" % (TILE_SIZE, TILE_SIZE), 1)
setTMSURL(TMS_URL)
createTargetDir()
generateTree()
log("--------------------")
