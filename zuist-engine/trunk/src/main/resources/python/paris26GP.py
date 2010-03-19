#!/usr/bin/python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2010. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

import os, sys, math
# http://effbot.org/zone/element-index.htm
import elementtree.ElementTree as ET

CMD_LINE_HELP = "ZUIST Paris 26GP Scene Script\n\nUsage:\n\n" + \
    " \tparis26GP <target_path> [options]\n\n" + \
    "Options:\n\n"+\
    "\t-ts=N\t\ttile size (N in pixels)\n"+\
    "\t-tl=N\t\ttrace level (N in [0:3])\n"

TRACE_LEVEL = 1

TILE_SIZE = 1024
SCENE_DEPTH = 8

# camera focal distance
F = 100.0
# camera max altitude
MAX_ALT = 1000000

################################################################################
# Create tiles and ZUIST XML scene from source image
################################################################################
def processSrc():
    # source image
    outputSceneFile = "%s/scene.xml" % SRC_TGT_DIR
    outputroot = ET.Element("scene")
    generateLevels(outputroot, SCENE_DEPTH)
    generateTilePyramid(outputroot, SCENE_DEPTH)
    log("Writing scene file %s" % outputSceneFile, 1)
    tree = ET.ElementTree(outputroot)
    tree.write(outputSceneFile, encoding='utf-8')


################################################################################
# Generate ZUIST levels
################################################################################
def generateLevels(parentEL, levelCount):
    altitudes = [0,]
    for i in range(levelCount):
        depth = int(levelCount-i-1)
        altitudes.append(int(F*math.pow(2,i+1)-F))
        if i == levelCount-1:
            ceiling = MAX_ALT
        else:
            ceiling = altitudes[-1]
        floor = altitudes[-2]
        levelEL = ET.SubElement(parentEL, "level")
        levelEL.set("depth", "%d" % depth)
        levelEL.set("ceiling", "%d" % ceiling)
        levelEL.set("floor", "%d" % floor)

################################################################################
# Generate tile pyramid
################################################################################
def generateTilePyramid(parentEL, levelCount):
    return
            
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
    for arg in sys.argv[1:]:
        if arg.startswith("-ts="):
            TILE_SIZE = int(arg[len("-ts="):])
            if TILE_SIZE == 256:
                SCENE_DEPTH = 9
        elif arg.startswith("-tl="):
            TRACE_LEVEL = int(arg[len("-tl="):])
        elif arg.startswith("-h"):
            log(CMD_LINE_HELP)
            sys.exit(0)
        else:
            SRC_TGT_DIR = os.path.realpath(arg)
            
log("--------------------")
log("Saving to %s" % SRC_TGT_DIR, 1)
log("Tile Size: %dx%d" % (TILE_SIZE, TILE_SIZE), 1)
processSrc()
log("--------------------")
