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
    levelCount = generateLevels(outputroot, SCENE_DEPTH)
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
    for d in os.listdir(SRC_TGT_DIR):
        if not os.path.isdir("%s/%s" % (SRC_TGT_DIR, d)):
            continue
        generateTilesAtLevel(parentEL, d, levelCount)
        
def generateTilesAtLevel(parentEL, leveldir, levelCount):
    level = int(leveldir[leveldir.find("_")+1:])
    # scale factor
    sc = 2**(levelCount-level-1)
    # region size at this level
    rs = TILE_SIZE * sc
    log("Generating level %d" % level, 2)
    cols = []
    for f in os.listdir("%s/l_%d" % (SRC_TGT_DIR, level)):
        if os.path.isdir("%s/l_%d/%s" % (SRC_TGT_DIR, level, f)):
            cols.append(f)
    cols.sort(strColSorter)
    vx = 0
    for colf in cols:
        vy = 0
        col = int(colf[colf.find("_")+1:])
        rows = []
        for f in os.listdir("%s/l_%d/%s" % (SRC_TGT_DIR, level, colf)):
            if f.startswith("tile_"):
                rows.append(f)
        rows.sort(strTileSorter)
        for rowf in rows:
            row = int(rowf[rowf.find("_")+1:rowf.find(".jpg")])
            regionEL = ET.SubElement(parentEL, "region")
            regionEL.set("id", "R-%s-%s-%s" % (level, col, row))
            regionEL.set("levels", "%d" % level)
            regionEL.set("x", "%d" % vx)
            regionEL.set("y", "%d" % vy)
            regionEL.set("w", "%d" % rs)
            regionEL.set("h", "%d" % rs)
            objectEL = ET.SubElement(regionEL, "resource")
            objectEL.set("id", "I-%s-%s-%s" % (level, col, row))
            objectEL.set("type", "img")
            objectEL.set("x", "%d" % vx)
            objectEL.set("y", "%d" % vy)
            objectEL.set("w", "%d" % rs)
            objectEL.set("h", "%d" % rs)
            objectEL.set("scale", "%d" % sc)
            objectEL.set("src", "%s/%s/%s" % (leveldir, colf, rowf))
            vy -= TILE_SIZE * sc
        vx += TILE_SIZE * sc

#################################################################################
## nums as strings sorter
#################################################################################
def strColSorter(sn1, sn2):
    n1 = int(sn1[sn1.find("_")+1:])
    n2 = int(sn2[sn2.find("_")+1:])
    if  n1 < n2:
        return -1
    elif n1 > n2:
        return 1
    else:
        return 0

#################################################################################
## nums as strings sorter
#################################################################################
def strTileSorter(sn1, sn2):
    n1 = int(sn1[sn1.find("_")+1:sn1.find(".jpg")])
    n2 = int(sn2[sn2.find("_")+1:sn2.find(".jpg")])
    if  n1 < n2:
        return -1
    elif n1 > n2:
        return 1
    else:
        return 0
                
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
