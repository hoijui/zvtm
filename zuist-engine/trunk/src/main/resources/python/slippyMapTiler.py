#!/usr/bin/env python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2015. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

import os, sys, math, random
import urllib
from copy import copy

# http://effbot.org/zone/element-index.htm
import xml.etree.ElementTree as ET

CMD_LINE_HELP = "Slippy Map Tiling Script\n\nUsage:\n\n" + \
    " \tslippyMapTiler <target_dir> [options]\n\n" + \
    "Options:\n\n"+\
    "\t-ts=S\t\ttile size (S in pixels)\n"+\
    "\t-ext=<ext>\t<ext> one of {png,jpg}\n"+\
    "\t-rt=z-x-y\troot tile (zoom-x-y) for this scene\n"+\
    "\t-zd=N\t\tzoom depth from root (N in [0,19])\n"+\
    "\t-dt=N-N\t\tdownload and save tiles for levels in the specified range (N in [0,19])\n"+\
    "\t-xy\t\tinvert x and y in slippy tile URL coordinates system\n"+\
    "\t-im=<i>\t\t<i> one of {bilinear,bicubic,nearestNeighbor}\n"+\
    "\t-tl=N\t\ttrace level (N in [0:2])\n"

TRACE_LEVEL = 1

TILE_SIZE = 256

# browse http://www.maptiler.org/google-maps-coordinates-tile-bounds-projection/
# to find the coordinates of your root tile
# use Zoom Z and Google: (X,Y)
ZOOM_DEPTH = 3
ROOT_TILE = (0,0,0) # zoom 0, x 0, y 0

# Download tiles and reference local tiles
# for levels in this range (expressed in original tileset's zoom level values)
DOWNLOAD_LEVEL_RANGE = (-1,-1)

INVERT_X_Y = False

TILE_EXT = "png"

INTERPOLATION = "bilinear"

# camera focal distance
F = 100.0
# camera max altitude
MAX_ALT = "100000"

# prefix for image tile files
TILE_FILE_PREFIX = "t-"

TILE_SERVER_LETTER_PREFIXES = ["a", "b", "c", "d"]

################################################################################
# TMS URL
################################################################################

def getTMSURL():
    ### OSM
    #return "http://%s.tile.openstreetmap.org/" % TILE_SERVER_LETTER_PREFIXES[int(math.floor(random.random()*3))]
    ### ArcGIS orthoimagery, use with -yx
    #return "http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/"
    ### Stamen maps
    return "http://%s.tile.stamen.com/watercolor/" % TILE_SERVER_LETTER_PREFIXES[int(math.floor(random.random()*4))]
    #return "http://%s.tile.stamen.com/terrain/" % TILE_SERVER_LETTER_PREFIXES[int(math.floor(random.random()*4))]
    #return "http://%s.tile.stamen.com/terrain-background/" % TILE_SERVER_LETTER_PREFIXES[int(math.floor(random.random()*4))]
    #return "http://%s.tile.stamen.com/toner/" % TILE_SERVER_LETTER_PREFIXES[int(math.floor(random.random()*4))]
    ### Mapquest
    #return "http://otile%d.mqcdn.com/tiles/1.0.0/sat/" % math.ceil(random.random()*4)
    #return "http://otile%d.mqcdn.com/tiles/1.0.0/osm/" % math.ceil(random.random()*4)
    ### Mapbox
    #return "http://%s.tiles.mapbox.com/v3/examples.xqwfusor/" % TILE_SERVER_LETTER_PREFIXES[int(math.floor(random.random()*4))]
    ### More examples at http://homepage.ntlworld.com/keir.clarke/leaflet/leafletlayers.htm

################################################################################
# Create target directory if it does not exist yet
################################################################################
def createTargetDir():
    if not os.path.exists(TGT_DIR):
        log("Creating target directory %s" % TGT_DIR, 2)
        os.mkdir(TGT_DIR)

################################################################################
# Generate levels for ZUIST scene
# (root level, depth, zuist level offset, parent XML element)
################################################################################
def generateLevels(lroot, ldepth, zloffset, rootEL):
    res = ldepth
    log("Will generate %d level(s)" % res, 2)
    # generate ZUIST levels
    altitudes = [str(int(F*math.pow(2,zloffset)-F)),]
    for i in range(zloffset, zloffset+res):
        depth = res-i-1
        altitudes.append(str(int(F*math.pow(2,i+1)-F)))
        level = ET.SubElement(rootEL, "level")
        level.set("depth", str(depth))
        level.set("floor", altitudes[-2])
        level.set("ceiling", altitudes[-1])
    log("Altitudes: %s" % ", ".join(altitudes), 3)
    return res

################################################################################
# Create tiles and ZUIST XML scene from source image
# root tile (z,x,y), zoom depth
################################################################################
def generateTree(rt, zd):
    outputSceneFile = "%s/scene.xml" % TGT_DIR
    # prepare the XML scene
    outputroot = ET.Element("scene")
    # source image
    levelCount = generateLevels(rt[0], zd, 0, outputroot)
    log("TMS URL prefix: %s" % getTMSURL(), 1)
    log("Interpolation method: %s" % INTERPOLATION)
    ox = -int(math.pow(2,levelCount-1)) * TILE_SIZE / 2
    oy = int(math.pow(2,levelCount-1)) * TILE_SIZE / 2
    for l in range(levelCount):
        buildRegionsAtLevel(rt, l, levelCount, outputroot, ox, oy)
    # serialize the XML tree
    tree = ET.ElementTree(outputroot)
    log("Writing %s" % outputSceneFile)
    tree.write(outputSceneFile, encoding='utf-8')

################################################################################
# Build all regions/tiles for a given zoom level
################################################################################
def buildRegionsAtLevel(rt, level, levelCount, rootEL, ox, oy):
    tcal = int(math.pow(2,level)) # tile count at level
    sf = int(math.pow(2,levelCount-level-1)) # tile scale factor
    log("level: %d, tile count: %dx%d, tile scale factor: %d" % (level, tcal, tcal, sf), 2)
    for x in range(tcal):
        for y in range(tcal):
            regionEL = ET.SubElement(rootEL, "region")
            tileID = "%d-%d-%d" % (level, x, y)
            regionID = "R%s" % tileID
            regionEL.set("id", regionID)
            objectEL = ET.SubElement(regionEL, "resource")
            objectEL.set("id", "T%s" % tileID)
            objectEL.set("type", "img")
            objectEL.set("sensitive", "false")
            if INVERT_X_Y:
                tileURL = "%s%d/%d/%d.%s" % (getTMSURL(),level+rt[0],y+tcal*rt[2],x+tcal*rt[1],TILE_EXT)
            else:
                tileURL = "%s%d/%d/%d.%s" % (getTMSURL(),level+rt[0],x+tcal*rt[1],y+tcal*rt[2],TILE_EXT)
            if level+rt[0] in range(DOWNLOAD_LEVEL_RANGE[0],DOWNLOAD_LEVEL_RANGE[1]+1):
                log("Fetching tile %s" % tileURL, 3)
                tilePath = fetchTile(tileURL, level+rt[0], x+tcal*rt[1], y+tcal*rt[2])
                objectEL.set("src", tilePath)
            else:
                objectEL.set("src", tileURL)
            objectEL.set("z-index", str(level))
            objectEL.set("params", "im=%s" % INTERPOLATION)
            awh = int(sf*TILE_SIZE)
            vx = ox + x*awh + awh/2
            vy = oy - y*awh - awh/2
            vw = awh
            vh = awh
            regionEL.set("x", str(int(vx)))
            regionEL.set("y", str(int(vy)))
            regionEL.set("w", str(int(vw)))
            regionEL.set("h", str(int(vh)))
            objectEL.set("x", str(int(vx)))
            objectEL.set("y", str(int(vy)))
            objectEL.set("w", str(int(vw)))
            objectEL.set("h", str(int(vh)))
            if level == 0:
                regionEL.set("levels", "0;%d" % (levelCount-1))
            else:
                parentTileID = "R%d-%d-%d" % (level-1,x/2,y/2)
                regionEL.set("containedIn", parentTileID)
                regionEL.set("levels", str(level))
    return

################################################################################
# Trace exec on std output
################################################################################
def fetchTile(tileURL, z, x, y):
    directories = ["%s/%d" % (TGT_DIR, z), "%s/%d/%d" % (TGT_DIR, z, x)]
    for d in directories:
        if not os.path.exists(d):
            log("Creating target directory %s" % d, 3)
            os.mkdir(d)
    relPath = "%d/%d/%d.%s" % (z, x, y, TILE_EXT)
    absPath = "%s/%s" % (TGT_DIR, relPath)
    if os.path.exists(absPath):
        log("Tile already fetched: %s" % absPath, 3)
    else:
        log("Saving tile to %s" % absPath, 3)
        urllib.urlretrieve(tileURL, absPath)
    return relPath

################################################################################
# Trace exec on std output
################################################################################
def log(msg, level=0):
    if level <= TRACE_LEVEL:
        print msg

################################################################################
# main
################################################################################
if len(sys.argv) > 1 and not sys.argv[1].startswith("-"):
    TGT_DIR = os.path.realpath(sys.argv[1])
    if len(sys.argv) > 2:
        for arg in sys.argv[2:]:
            if arg.startswith("-ts="):
                TILE_SIZE = int(arg[4:])
            elif arg.startswith("-zd="):
                ZOOM_DEPTH = int(arg[4:])
            elif arg.startswith("-rt="):
                tokens = arg[4:].split("-")
                ROOT_TILE = (int(tokens[0]), int(tokens[1]), int(tokens[2]))
            elif arg.startswith("-dt="):
                tokens = arg[4:].split("-")
                DOWNLOAD_LEVEL_RANGE = (int(tokens[0]), int(tokens[1]))
            elif arg.startswith("-ext="):
                TILE_EXT = arg[5:]
            elif arg.startswith("-im="):
                INTERPOLATION = arg[4:]
            elif arg.startswith("-tl="):
                TRACE_LEVEL = int(arg[4:])
            elif arg.startswith("-yx"):
                INVERT_X_Y = True
            elif arg.startswith("-h"):
                log(CMD_LINE_HELP)
                sys.exit(0)
else:
    log(CMD_LINE_HELP)
    sys.exit(0)

log("Tile Size: %dx%d" % (TILE_SIZE, TILE_SIZE), 1)
createTargetDir()
generateTree(ROOT_TILE, ZOOM_DEPTH)
log("")
