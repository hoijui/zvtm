#!/usr/bin/python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2009. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

import os, sys, math
from copy import copy

# http://effbot.org/zone/element-index.htm
import elementtree.ElementTree as ET

TRACE_LEVEL = 1

NODES = {"a1.wild.lri.fr": [(-4,2), (-3,2)],
"a2.wild.lri.fr": [(-4,1), (-3,1)],
"a3.wild.lri.fr": [(-4,-1), (-3,-1)],
"a4.wild.lri.fr": [(-4,-2), (-3,-2)],

"b1.wild.lri.fr": [(-2,2), (-1,2)],
"b2.wild.lri.fr": [(-2,1), (-1,1)],
"b3.wild.lri.fr": [(-2,-1), (-1,-1)],
"b4.wild.lri.fr": [(-2,-2), (-1,-2)],

"c1.wild.lri.fr": [(1,2), (2,2)],
"c2.wild.lri.fr": [(1,1), (2,1)],
"c3.wild.lri.fr": [(1,-1), (2,-1)],
"c4.wild.lri.fr": [(1,-2), (2,-2)],

"d1.wild.lri.fr": [(3,2), (4,2)],
"d2.wild.lri.fr": [(3,1), (4,1)],
"d3.wild.lri.fr": [(3,-1), (4,-1)],
"d4.wild.lri.fr": [(3,-2), (4,-2)],
}

PORTS = [57110, 57111]

SCREEN_W = 2560
SCREEN_H = 1600

BEZEL_W = 96
BEZEL_H = 120

def getX(i):
    if i < 0:
        return (i+0.5) * (SCREEN_W + 2*BEZEL_W)
    elif i > 0:
        return (i-0.5) * (SCREEN_W + 2*BEZEL_W)
    else:
        return i

def getY(i):
    if i < 0:
        return (i+0.5) * (SCREEN_H + 2*BEZEL_H)
    elif i > 0:
        return (i-0.5) * (SCREEN_H + 2*BEZEL_H)
    else:
        return i

def generateConfig():
    outputSceneFile = TGT_PATH
    # prepare the XML scene
    outputroot = ET.Element("wall")
    for host in NODES.keys():
        nodeEL = ET.SubElement(outputroot, "node")
        nodeEL.set("name", host)
        i = 0
        for screen in NODES[host]:
            viewportEL = ET.SubElement(nodeEL, "viewport")
            viewportEL.set("device", "%d" % i)
            viewportEL.set("port", "%d" % PORTS[i])
            viewportEL.set("dx", "%d" % getX(screen[0]))
            viewportEL.set("dy", "%d" % getY(screen[1]))
            viewportEL.set("w", "%d" % SCREEN_W)
            viewportEL.set("h", "%d" % SCREEN_H)
            viewportEL.set("bw", "%d" % BEZEL_W)
            viewportEL.set("bh", "%d" % BEZEL_H)
            i += 1
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
if len(sys.argv) > 1:
    TGT_PATH = os.path.realpath(sys.argv[1])
else:
    log(CMD_LINE_HELP)
    sys.exit(0)

generateConfig()
