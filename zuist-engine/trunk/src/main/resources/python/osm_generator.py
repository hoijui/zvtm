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
	    processLevel(d, res, rootEL)

################################################################################
# Generate regions and tiles for a given level
################################################################################
def processLevel(level_dir, level_count, rootEL):
    level = int(level_dir)
    log("Processing level %s" % level, 2)
    col_dirs = os.listdir("%s/%s" % (SRC_PATH, level_dir))
    col_dirs.sort(strNumSorter)
    for col_dir in col_dirs:
        row_files = os.listdir("%s/%s/%s" % (SRC_PATH, level_dir, col_dir))
        row_files.sort(pngRowSorter)
        for row_file in row_files:
            processTile(int(level_dir), int(col_dir), int(row_file[:-4]),\
                        level_count, rootEL)    

################################################################################
# Generate one specific region/resource for a given level/column/row
################################################################################
def processTile(level, col, row, level_count, rootEL):
    log("Processing %s %s %s" % (level, col, row))
    ilc = level_count - level
    ts = TILE_SIZE * math.pow(2, ilc-2)
    x = (col*ts + ts/2.0)
    y = -(row*ts + ts/2.0)
    w = h = ts
    # region
    regionEL = ET.SubElement(rootEL, "region")
    regionEL.set("id", "ID-%d-%d-%d" % (level, col, row))
    regionEL.set("x", "%d" % x)
    regionEL.set("y", "%d" % y)
    regionEL.set("w", "%d" % w)
    regionEL.set("h", "%d" % h)
    regionEL.set("levels", "%d" % level)
    if level > 0:
        pass
        #regionEL.set("containedIn", "ID-%d-%d-%d" % (level-1, , ))
    # image
    resourceEL = ET.SubElement(regionEL, "resource")
    resourceEL.set("id", "T-%d-%d-%d" % (level, col, row))
    resourceEL.set("x", "%d" % x)
    resourceEL.set("y", "%d" % y)
    resourceEL.set("w", "%d" % w)
    resourceEL.set("h", "%d" % h)
    resourceEL.set("type", "img")
    resourceEL.set("src", "tiles/%s/%s/%s.png" % (level,col,row))

################################################################################
# level/col/row sorters
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

################################################################################
# The following code might be useful in the future
# Taken from http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames

# lon/lat to tile numbers

# import math
# def deg2num(lat_deg, lon_deg, zoom):
#   lat_rad = lat_deg * math.pi / 180.0
#   n = 2.0 ** zoom
#   xtile = int((lon_deg + 180.0) / 360.0 * n)
#   ytile = int((1.0 - math.log(math.tan(lat_rad) + (1 / math.cos(lat_rad))) / math.pi) / 2.0 * n)
#   return(xtile, ytile)

# tile numbers to lon/lat

# import math
# def num2deg(xtile, ytile, zoom):
#   n = 2.0 ** zoom
#   lon_deg = xtile / n * 360.0 - 180.0
#   lat_rad = math.atan(math.sinh(math.pi * (1 - 2 * ytile / n)))
#   lat_deg = lat_rad * 180.0 / math.pi
#   return(lat_deg, lon_deg)

# This returns the NW-corner of the square. Use the function with xtile+1
# and/or ytile+1 to get the other corners. With xtile+0.5 & ytile+0.5
# it will return the center of the tile.
################################################################################
