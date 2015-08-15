#!/usr/bin/env python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2015. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

import os, sys, math, random
import urllib2
from copy import copy

# http://effbot.org/zone/element-index.htm
import xml.etree.ElementTree as ET

CMD_LINE_HELP = "IGN Map Tiling Script\n\nUsage:\n\n" + \
    " \tignMapTiler <target_dir> [options]\n\n" + \
    "Options:\n\n"+\
    "\t-ts=S\t\ttile size (S in pixels)\n"+\
    "\t-ext=<ext>\t<ext> one of {png,jpg}\n"+\
    "\t-rt=z-x-y\troot tile (zoom-x-y) for this scene\n"+\
    "\t-zd=N\t\tzoom depth from root tile specified in -rt (N in [0,19])\n"+\
    "\t-mfd=N\t\tmaximum depth of scene fragments (0 to generate a single scene no matter the total depth)\n"+\
    "\t-dt=N-N\t\tdownload and save tiles for levels in the specified range (N in [0,19])\n"+\
    "\t-user=<u>\t\t<u> HTTPS authentication (user)\n"+\
    "\t-password=<p>\t\t<p> HTTPS authentication (password)\n"+\
    "\t-key=<p>\t\t<p> IGN API key\n"+\
    "\t-im=<i>\t\t<i> one of {bilinear,bicubic,nearestNeighbor}\n"+\
    "\t-tl=N\t\ttrace level (N in [0:2])\n"

TRACE_LEVEL = 0

TILE_SIZE = 256

# browse http://www.maptiler.org/google-maps-coordinates-tile-bounds-projection/
# to find the coordinates of your root tile
# use Zoom Z and Google: (X,Y)
ZOOM_DEPTH = 3
ROOT_TILE = (0,0,0) # zoom 0, x 0, y 0

MAX_FRAG_DEPTH = 0

# Download tiles and reference local tiles
# for levels in this range (expressed in original tileset's zoom level values)
DOWNLOAD_LEVEL_RANGE = (-1,-1)

TILE_EXT = "png"

INTERPOLATION = "bilinear"

# camera focal distance
F = 100.0
# camera max altitude
MAX_ALT = "100000"

# prefix for image tile files
TILE_FILE_PREFIX = "t-"

USER = ""
PASSWD = ""
API_KEY = ""
SERVER = "wxs.ign.fr"

################################################################################
# TMS URL
#
# Make sure SERVER above matches the server name for the URL returned by getTMSURL()
#
################################################################################

def getTMSURL(z, x, y, k):
    # ORTHO
    ## EXT: jpg
    #return "https://wxs.ign.fr/%s/geoportail/wmts?LAYER=ORTHOIMAGERY.ORTHOPHOTOS&EXCEPTIONS=text/xml&FORMAT=image/jpeg&SERVICE=WMTS&VERSION=1.0.0&REQUEST=GetTile&STYLE=normal&TILEMATRIXSET=PM&TILEMATRIX=%d&TILEROW=%d&TILECOL=%d" % (k,z,x,y)
    # SCAN CLASSIQUE
    ## EXT: jpg
    #return "https://wxs.ign.fr/%s/geoportail/wmts?LAYER=GEOGRAPHICALGRIDSYSTEMS.MAPS.SCAN-EXPRESS.CLASSIQUE&EXCEPTIONS=text/xml&FORMAT=image/jpeg&SERVICE=WMTS&VERSION=1.0.0&REQUEST=GetTile&STYLE=normal&TILEMATRIXSET=PM&TILEMATRIX=%d&TILEROW=%d&TILECOL=%d" % (k,z,x,y)
    # SCAN STANDARD
    ## EXT: jpg
    return "https://wxs.ign.fr/%s/geoportail/wmts?LAYER=GEOGRAPHICALGRIDSYSTEMS.MAPS.SCAN-EXPRESS.STANDARD&EXCEPTIONS=text/xml&FORMAT=image/jpeg&SERVICE=WMTS&VERSION=1.0.0&REQUEST=GetTile&STYLE=normal&TILEMATRIXSET=PM&TILEMATRIX=%d&TILEROW=%d&TILECOL=%d" % (k,z,x,y)

################################################################################
# Create target directory if it does not exist yet
################################################################################
def createTargetDir(tgtDir):
    if not os.path.exists(tgtDir):
        log("Creating target directory %s" % tgtDir, 2)
        os.makedirs(tgtDir)

################################################################################
# Generate levels for ZUIST scene
# (root level, depth, zuist level offset, parent XML element)
################################################################################
def generateLevels(lroot, ldepth, zloffset, rootEL):
    res = ldepth
    log("Will generate %d level(s) in total (across fragments)" % res, 2)
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
# root tile (z,x,y), zoom depth, fragment top level, target dir
################################################################################
def generateTree(rt, zd, ftl, ox, oy, tgtDir):
    outputSceneFile = "%s/scene.xml" % tgtDir
    # prepare the XML scene
    outputroot = ET.Element("scene")
    totalLevelCount = generateLevels(rt[0], zd, 0, outputroot)
    if MAX_FRAG_DEPTH == 0:
        levelRangeInFrag = range(ftl, ftl+totalLevelCount)
    else:
        ffl = ftl+MAX_FRAG_DEPTH if ftl+MAX_FRAG_DEPTH < totalLevelCount else totalLevelCount
        levelRangeInFrag = range(ftl, ffl)
    for l in levelRangeInFrag:
        buildRegionsAtLevel(rt, l, ftl, totalLevelCount, outputroot, ox, oy, tgtDir)
    # serialize the XML tree
    tree = ET.ElementTree(outputroot)
    log("Writing %s" % outputSceneFile, 1)
    tree.write(outputSceneFile, encoding='utf-8')

################################################################################
# Build all regions/tiles for a given zoom level
################################################################################
def buildRegionsAtLevel(rt, level, ftl, totalLevelCount, rootEL, ox, oy, tgtDir):
    # should call generateTree to create scene fragments for levels below this one
    generateFrags = (level-ftl+1 == MAX_FRAG_DEPTH) and (level + 1 < totalLevelCount)
    tcal = int(math.pow(2,level-ftl)) # tile count at level
    sf = int(math.pow(2,totalLevelCount-level-1)) # tile scale factor
    log("level: %d, tile count: %dx%d, tile scale factor: %d" % (level, tcal, tcal, sf), 2)
    for x in range(tcal):
        for y in range(tcal):
            awh = int(sf*TILE_SIZE)
            vx = ox + x*awh + awh/2
            vy = oy - y*awh - awh/2
            vw = awh
            vh = awh
            regionEL = ET.SubElement(rootEL, "region")
            tileID = "%d-%d-%d" % (level-ftl+rt[0], x+tcal*rt[1], y+tcal*rt[2])
            objectEL = ET.SubElement(regionEL, "resource")
            regionEL.set("x", str(int(vx)))
            regionEL.set("y", str(int(vy)))
            regionEL.set("w", str(int(vw)))
            regionEL.set("h", str(int(vh)))
            objectEL.set("x", str(int(vx)))
            objectEL.set("y", str(int(vy)))
            if generateFrags:
                regionID = "X%s" % tileID
                regionEL.set("id", regionID)
                # generate scene fragment
                parentTileID = "R%d-%d-%d" % (rt[0]-ftl+level-1,(x+tcal*rt[1])/2,(y+tcal*rt[2])/2)
                regionEL.set("containedIn", parentTileID)
                bottomLevel = level+MAX_FRAG_DEPTH if level+MAX_FRAG_DEPTH < totalLevelCount else totalLevelCount-1
                regionEL.set("levels", "%d;%d" % (level,bottomLevel))
                objectEL.set("id", "F%s" % tileID)
                objectEL.set("type", "scn")
                nTgtDir = "%s/%d/%d/%d" % (tgtDir, rt[0]+level-ftl, x+tcal*rt[1], y+tcal*rt[2])
                createTargetDir(nTgtDir)
                fragPath = "%d/%d/%d/scene.xml" % (rt[0]+level-ftl, x+tcal*rt[1], y+tcal*rt[2])
                objectEL.set("src", fragPath)
                nrt = (rt[0]+level-ftl, x+tcal*rt[1], y+tcal*rt[2])
                generateTree(nrt, totalLevelCount, level, vx-awh/2, vy+awh/2, nTgtDir)
            else:
                regionID = "R%s" % tileID
                regionEL.set("id", regionID)
                # generate the tile
                objectEL.set("w", str(int(vw)))
                objectEL.set("h", str(int(vh)))
                objectEL.set("id", "T%s" % tileID)
                objectEL.set("type", "img")
                objectEL.set("sensitive", "false")
                tileURL = getTMSURL(level-ftl+rt[0],y+tcal*rt[2],x+tcal*rt[1], API_KEY)
                if level-ftl+rt[0] in range(DOWNLOAD_LEVEL_RANGE[0],DOWNLOAD_LEVEL_RANGE[1]+1):
                    log("Fetching tile %s" % tileURL, 3)
                    tilePath = fetchTile(tileURL, level-ftl+rt[0], x+tcal*rt[1], y+tcal*rt[2], tgtDir)
                    objectEL.set("src", tilePath)
                else:
                    objectEL.set("src", "%s" % tileURL)
                objectEL.set("z-index", str(rt[0]+level-ftl))
                objectEL.set("params", "im=%s" % INTERPOLATION)
                if level == 0:
                    regionEL.set("levels", "0;%d" % (totalLevelCount-1))
                else:
                    parentTileID = "R%d-%d-%d" % (rt[0]-ftl+level-1,(x+tcal*rt[1])/2,(y+tcal*rt[2])/2)
                    regionEL.set("containedIn", parentTileID)
                    regionEL.set("levels", str(level))
    return

################################################################################
# Initialize HTTPS login/password
################################################################################
def initializeHTTPS():
    passman = urllib2.HTTPPasswordMgrWithDefaultRealm()
    passman.add_password(None, SERVER, USER, PASSWD)
    authhandler = urllib2.HTTPBasicAuthHandler(passman)
    opener = urllib2.build_opener(authhandler)
    urllib2.install_opener(opener)

################################################################################
# Trace exec on std output
################################################################################
def fetchTile(tileURL, z, x, y, tgtDir):
    tileDir = "%s/%d/%d" % (tgtDir, z, x)
    if not os.path.exists(tileDir):
        log("Creating target directory %s" % tileDir, 3)
        os.makedirs(tileDir)
    relPath = "%d/%d/%d.%s" % (z, x, y, TILE_EXT)
    absPath = "%s/%s" % (tgtDir, relPath)
    if os.path.exists(absPath):
        log("Tile already fetched: %s" % absPath, 3)
    else:
        log("Saving tile to %s" % absPath, 3)
        tile = urllib2.urlopen(tileURL)
        tilef = open(absPath, 'wb')
        tilef.write(tile.read())
        tilef.close()
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
            elif arg.startswith("-mfd="):
                MAX_FRAG_DEPTH = int(arg[5:])
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
            elif arg.startswith("-user="):
                USER = arg[6:]
            elif arg.startswith("-password="):
                PASSWD = arg[10:]
            elif arg.startswith("-key="):
                API_KEY = arg[5:]
            elif arg.startswith("-h"):
                log(CMD_LINE_HELP)
                sys.exit(0)
else:
    log(CMD_LINE_HELP)
    sys.exit(0)

log("Tile Size: %dx%d" % (TILE_SIZE, TILE_SIZE), 1)
log("Maximum levels in scene fragments: %d" % MAX_FRAG_DEPTH, 1)
log("TMS URL prefix: %s" % getTMSURL(0,0,0,API_KEY), 1)
log("Interpolation method: %s" % INTERPOLATION, 1)
createTargetDir(TGT_DIR)
initializeHTTPS()
sox = -int(math.pow(2,ZOOM_DEPTH-1)) * TILE_SIZE / 2
soy = int(math.pow(2,ZOOM_DEPTH-1)) * TILE_SIZE / 2
generateTree(ROOT_TILE, ZOOM_DEPTH, 0, sox, soy, TGT_DIR)
