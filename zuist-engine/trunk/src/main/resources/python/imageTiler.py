#!/usr/bin/python
# -*- coding: UTF-8 -*-
# $Id$

import os, sys, math
from copy import copy
# http://www.pythonware.com/products/pil/
from PIL import Image
# http://effbot.org/zone/element-index.htm
import elementtree.ElementTree as ET

TRACE_LEVEL = 1

################################################################################
# Create target directory if it does not exist yet
################################################################################
def createTargetDir():
    if not os.path.exists(TGT_DIR):
        log("Creating target directory %s" % TGT_DIR, 2)
        os.mkdir(TGT_DIR)

################################################################################
# Create tiles and ZUIST XML scene from source image
################################################################################
def processSrcImg():
    outputSceneFile = "%s/img_scene.xml" % TGT_DIR
    # prepare the XML scene
    outputroot = ET.Element("scene")
    # serialize the XML tree
    tree = ET.ElementTree(outputroot)
    log("-----------------------------------\nWriting %s\n-----------------------------------" % outputSceneFile)
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
        TRACE_LEVEL = int(sys.argv[3])
else:
    sys.exit(0)

createTargetDir()
processSrcImg()
