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

CMD_LINE_HELP = "ZUIST Meta Scene Script\n\nUsage:\n\n" + \
    " \tmetaScene <target_path> [options]\n\n" + \
    "Options:\n\n"+\
    "\t-ts=N\t\ttile size (N in pixels)\n"+\
    "\t-tl=N\t\ttrace level (N in [0:3])\n"

TRACE_LEVEL = 1

TILE_SIZE = 10000

IMG_W = 214414
IMG_H = 80571

################################################################################
# Create tiles and ZUIST XML scene from source image
################################################################################
def processSrc():
    # source image
    outputSceneFile = TGT_PATH
    outputroot = ET.Element("scene")
    x = 0
    y = 0
#    while y < IMG_W:
#        x = 0
#        while x < IMG_H:
#            include = ET.SubElement(outputroot, "include")
#            include.set("src", "tile-%d-%d/scene.xml" % (x/TILE_SIZE, y/TILE_SIZE))
#            include.set("x", "%d" % x)
#            include.set("y", "%d" % y)

    for i in range(9):
        x = 0
        for j in range(22):
            include = ET.SubElement(outputroot, "include")
            include.set("src", "r%04dc%04d/scene.xml" % (i,j))
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
if len(sys.argv) > 1:
    TGT_PATH = os.path.realpath(sys.argv[1])
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
