#!/usr/bin/python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2009. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id:  $

import os, sys, math
# http://effbot.org/zone/element-index.htm
import elementtree.ElementTree as ET

TRACE_LEVEL = 1

CMD_LINE_HELP = "OSM ZUIST Scene Script\n\nUsage:\n\n" + \
    " \tosm_generator <src_image_path> <target_dir> [options]\n\n" + \
    "Options:\n\n"+\
    "\t-tl=N\t\ttrace level (N in [0:3])\n"+\
    "\t-ts=N\t\ttile size (N in pixels)\n"+\
    "\t-idprefix=p\tcustom prefix for all region and objects IDs\n"

# camera focal distance
F = 100.0
# camera max altitude
MAX_ALT = "100000"

ID_PREFIX = ""
TILE_SIZE = 512

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
def generateLevels(rootEL):
	
	
	
	#XXX: count number of levels
	res = 1
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
# Create tiles and ZUIST XML scene from source image
################################################################################
def processOSMTiles():
    global maxTileCount
    outputSceneFile = "%s/scene.xml" % TGT_DIR
    # prepare the XML scene
    outputroot = ET.Element("scene")
    # source data
    log("Loading OSM tiles from %s" % SRC_PATH, 2)
    generateLevels(outputroot)
    #XXX:TODO
    
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
            if arg.startswith("-ts="):
                TILE_SIZE = int(arg[4:])
            elif arg.startswith("-tl="):
                TRACE_LEVEL = int(arg[4:])
            elif arg.startswith("-idprefix"):
                ID_PREFIX = arg[len("-idprefix="):]
else:
    log(CMD_LINE_HELP)
    sys.exit(0)

log("Tile size: %s" % TILE_SIZE)
createTargetDir()
processOSMTiles()
log("--------------------")
