#!/usr/bin/python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2009. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

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
# Generate all zoom levels
################################################################################
def generateLevels(rootEL):
	# making the assumption that src dir only contains tile level directories
	# (which is the case when coming out of the mapnik tile generation script)
	res = len(os.listdir(SRC_PATH))
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
	level_dirs = os.listdir(SRC_PATH)
	level_dirs.sort(strNumSorter)
	for d in level_dirs:
	    processLevel(d, rootEL)

################################################################################
# Generate regions and tiles for a given level
################################################################################
def processLevel(level_dir, rootEL):
    level = int(level_dir)
    log("Processing level %s" % level, 2)
    col_dirs = os.listdir("%s/%s" % (SRC_PATH, level_dir))
    col_dirs.sort(strNumSorter)
    for col_dir in col_dirs:
        row_files = os.listdir("%s/%s/%s" % (SRC_PATH, level_dir, col_dir))
        row_files.sort(pngRowSorter)
        for row_file in row_files:
            processTile(int(level_dir), int(col_dir), int(row_file[:-4]), rootEL)    

################################################################################
# Generate one specific region/resource for a given level/column/row
################################################################################
def processTile(level, col, row, rootEL):
    log("Processing %s %s %s" % (level, col, row))
    
    
################################################################################
# level/col/row sorter
################################################################################
def strNumSorter(l1, l2):
    n1 = int(l1)
    n2 = int(l2)
    if  n1 < n2:
        return -1
    elif n1 > n2:
        return 1
    else:
        return 0
        
def pngRowSorter(l1, l2):
    n1 = int(l1[:-4])
    n2 = int(l2[:-4])
    if  n1 < n2:
        return -1
    elif n1 > n2:
        return 1
    else:
        return 0


################################################################################
# Create levels for ZUIST scene from source OSM tile hierarchy
################################################################################
def processOSMTiles():
    global maxTileCount
    outputSceneFile = "%s/scene.xml" % TGT_DIR
    # prepare the XML scene
    outputroot = ET.Element("scene")
    # source data
    log("Loading OSM tiles from %s" % SRC_PATH, 2)
    generateLevels(outputroot)    
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
