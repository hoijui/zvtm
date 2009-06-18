#!/usr/bin/python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2009. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id:  $

import os, sys, math
from copy import copy
# http://effbot.org/zone/element-index.htm
import elementtree.ElementTree as ET

CMD_LINE_HELP = "ZUIST PDF Page Tiler Script\n\nUsage:\n\n" + \
    " \tpdfPageTiler <src_image_path> <target_dir> [options]\n\n" + \
    "Options:\n\n"+\
    "\t-ts=N\t\ttile size (N in pixels)\n"+\
    "\t-tl=N\t\ttrace level (N in [0:3])\n"+\
    "\t-f\t\tforce tile generation\n"+\
    "\t-scale=s\ts scale factor w.r.t default size\n"

TRACE_LEVEL = 1

TILE_SIZE = 500

IMG_W = 214414
IMG_H = 80571

################################################################################
# Create tiles and ZUIST XML scene from source image
################################################################################
def processSrc():
    # source image
    log("Loading source image from %s" % SRC_PATH, 2)
    outputSceneFile = "%s/scene.xml" % TGT_DIR
    outputroot = ET.Element("scene")
    x = 0
    y = 0
    while y < IMG_W:
        x = 0
        while x < IMG_H:
            #generateTile(pdf_document, page, x, y, "%s/tile-%d-%d.%s" % (TGT_DIR, x/TILE_SIZE, y/TILE_SIZE, OUTPUT_FILE_EXT))
            
            include = ET.SubElement(outputroot, "include")
            include.set("src", "tile-%d-%d/scene.xml" % (x/TILE_SIZE, y/TILE_SIZE))
            include.set("x", "%d" % x)
            include.set("y", "%d" % y)
            
            x += TILE_SIZE
        y += TILE_SIZE
    log("Writing scene file %s" % outputSceneFile, 1)
    tree = ET.ElementTree(outputroot)
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
    TGT_DIR = os.path.realpath(sys.argv[2])
    if len(sys.argv) > 3:
        for arg in sys.argv[3:]:
            if arg.startswith("-ts="):
                TILE_SIZE = int(arg[len("-ts="):])
            elif arg.startswith("-tl="):
                TRACE_LEVEL = int(arg[len("-tl="):])
else:
    log(CMD_LINE_HELP)
    sys.exit(0)

log("--------------------")
log("Tile Size: %dx%d" % (TILE_SIZE, TILE_SIZE), 1)
processSrc()
log("--------------------")
