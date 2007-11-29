#!/usr/bin/python
# -*- coding: UTF-8 -*-
# $Id$

import os, sys, math
from copy import copy
from PIL import Image
import elementtree.ElementTree as ET

TRACE_LEVEL = 1

GENERATE_TILES = True

SRC_TILE_NAMES = ["A1", "B1", "C1", "D1",\
              "A2", "B2", "C2", "D2"]

# SRC_TILE_SIZE
STS = 21600
# SRC_TILE_COORDS
STC = {"A1":(-2*STS,STS), "B1":(-STS,STS), "C1":(0,STS), "D1":(STS,STS),\
    "A2":(-2*STS,0), "B2":(-STS,0), "C2":(0,0), "D2":(STS,0)}

TILE_SIZES_PER_LEVEL = [1350, 1350, 1350, 1350, 1350]

TILE_SUBDIVS_PER_LEVEL = [1, 2, 2, 2]
#TILE_SUBDIVS_PER_LEVEL = [1, 2, 2, 2, 2]

NB_LEVELS = len(TILE_SUBDIVS_PER_LEVEL)

XML_LEVELS = []
LEVEL_FLOORS = []
LEVEL_CEILINGS = []

ALTS = [("1000000","2800"), ("2800","600"), ("600","200"), ("200","0")]

def createTargetDir():
    if not os.path.exists(TGT_DIR):
        log("Creating target directory %s" % TGT_DIR, 2)
        os.mkdir(TGT_DIR)
    
def processSrcDir():
    srcFiles = os.listdir(SRC_DIR)
    outputSceneFile = "%s/wm_scene.xml" % TGT_DIR
    # prepare the XML scene
    outputroot = ET.Element("scene")
    createXMLLevels(outputroot)
    # process images and populate scene
    for f in srcFiles:
        if f.endswith(".jpg"):
            checkTile(f, outputroot)
    # serialize the XML tree
    tree = ET.ElementTree(outputroot)
    log("-----------------------------------\nWriting %s\n-----------------------------------" % outputSceneFile)
    tree.write(outputSceneFile, encoding='utf-8')

def createXMLLevels(outputParent):
    i = 0
    for nbSubDivs in TILE_SUBDIVS_PER_LEVEL:
        level = ET.SubElement(outputParent, "level")
        XML_LEVELS.append(level)
        LEVEL_CEILINGS.append(str(ALTS[i][0]))
        LEVEL_FLOORS.append(str(ALTS[i][1]))
        level.set("depth", str(i))
        level.set("ceiling", LEVEL_CEILINGS[i])
        level.set("floor", LEVEL_FLOORS[i])
        i += 1
            
def checkTile(tileFile, rootEL):
    tileName = tileFile[-6:-4]
    if tileName in SRC_TILE_NAMES:
        processTile("%s/%s" % (SRC_DIR, tileFile), tileName, rootEL)
        
def processTile(srcTilePath, tileName, rootEL):
    im = Image.open(srcTilePath)
    sz = im.size
    log("Processing %s (%sx%s)" % (srcTilePath, sz[0], sz[1]), 1)
    generateLevel(0, 0, 0, im, tileName, srcTilePath, [0 for i in range(len(TILE_SIZES_PER_LEVEL))], None, rootEL)
    
def generateLevel(level, x, y, im, tileName, srcTilePath, parentTileID, parentRegion, rootEL):
    log("Generating level %s for tile %s" % (level, tileName))
    tileID = copy(parentTileID)
    subDivFactor = TILE_SUBDIVS_PER_LEVEL[0]
    for v in TILE_SUBDIVS_PER_LEVEL[1:level+1]:
        subDivFactor *= v
    doCrop = (subDivFactor != 1.0)
    cw = int(im.size[0] / subDivFactor)
    ch = int(im.size[1] / subDivFactor)
    orig = STC.get(tileName)
    for i in range(TILE_SUBDIVS_PER_LEVEL[level]):
        for j in range(TILE_SUBDIVS_PER_LEVEL[level]):
            tx = x + j * cw
            ty = y + i * ch
            tileID[level] = tileID[level] + 1
            strID = "%s-%s" % (tileName, "-".join([str(s) for s in tileID]))
            tileFileName = "%s.jpg" % strID
            tilePath = "%s/%s" % (TGT_DIR, tileFileName)
            # populate XML scene
            regionEL = ET.SubElement(rootEL, "region")
            regionEL.set("id", "R%s" % strID)
            if parentRegion is not None:
                regionEL.set("containedIn", parentRegion.get("id"))
            regionEL.set("depth", str(level))
            regionEL.set("x", str(orig[0]+tx+cw/2))
            regionEL.set("y", str(orig[1]-ty-ch/2))
            regionEL.set("w", str(cw))
            regionEL.set("h", str(ch))
            regionEL.set("stroke", "blue")
            objectEL = ET.SubElement(regionEL, "object")
            objectEL.set("id", "I%s" % strID)
            objectEL.set("type", "image")
            objectEL.set("x", str(orig[0]+tx+cw/2))
            objectEL.set("y", str(orig[1]-ty-ch/2))
            objectEL.set("w", str(cw))
            objectEL.set("h", str(ch))
            objectEL.set("src", tileFileName)
            # calls to ImageMagick
            if os.path.exists(tilePath) or not GENERATE_TILES:
                log("%s already exists (skipped)" % tilePath, 2)
            else:
                if doCrop:
                    log("Cropping %s" % tileFileName, 2)
                    ccl = "convert %s -crop %sx%s+%s+%s -quality 95 %s" % (srcTilePath, cw, ch, tx, ty, tilePath)
                    log("%s" % ccl, 3)
                    os.system(ccl)
                    if cw != TILE_SIZES_PER_LEVEL[level] or ch != TILE_SIZES_PER_LEVEL[level]:
                        log("Resizing %s" % tileFileName, 2)
                        ccl = "convert %s -resize %sx%s -quality 85 %s" % (tilePath, TILE_SIZES_PER_LEVEL[level], TILE_SIZES_PER_LEVEL[level], tilePath)
                        log("%s" % ccl, 3)
                        os.system(ccl)
                else:
                    log("Resizing %s" % tileFileName, 2)
                    ccl = "convert %s -resize %sx%s -quality 85 %s" % (srcTilePath, TILE_SIZES_PER_LEVEL[level], TILE_SIZES_PER_LEVEL[level], tilePath)
                    log("%s" % ccl, 3)
                    os.system(ccl)
            if level < NB_LEVELS-1:
                generateLevel(level+1, tx, ty, im, tileName, srcTilePath, tileID, regionEL, rootEL)

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
    if len(sys.argv) > 3:
        TRACE_LEVEL = int(sys.argv[3])
else:
    sys.exit(0)

createTargetDir()
processSrcDir()
