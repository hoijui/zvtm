#!/usr/bin/python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2009-2016. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

import os, sys, math
from copy import copy
# http://developer.apple.com/documentation/GraphicsImaging/Conceptual/drawingwithquartz2d/dq_python/dq_python.html
from CoreGraphics import *
# http://effbot.org/zone/element-index.htm
import xml.etree.ElementTree as ET

CMD_LINE_HELP = "ZUIST PDF Page Tiler Script\n\nUsage:\n\n" + \
    " \tpdfPageTiler <src_image_path> <target_dir> [options]\n\n" + \
    "Options:\n\n"+\
    "\t-ts=N\t\ttile size (N in pixels)\n"+\
    "\t-tl=N\t\ttrace level (N in [0:3])\n"+\
    "\t-f\t\tforce tile generation\n"+\
    "\t-page=p\t\tpage number\n"+\
    "\t-format=t\tt output tiles in PNG (png), JPEG (jpg) or TIFF (tiff)\n"+\
    "\t-scale=s\ts scale factor w.r.t default size\n"

TRACE_LEVEL = 1

FORCE_GENERATE_TILES = False

TILE_SIZE = 500
PDF_SCALE_FACTOR = 1

PAGE_NUMBER = 1

OUTPUT_TYPE_PNG = "png"
OUTPUT_TYPE_JPEG = "jpg"
OUTPUT_TYPE_TIFF = "tiff"
OUTPUT_TYPE = OUTPUT_TYPE_PNG

OUTPUT_TYPE2CG = {
    OUTPUT_TYPE_PNG: kCGImageFormatPNG,
    OUTPUT_TYPE_JPEG: kCGImageFormatJPEG,
    OUTPUT_TYPE_TIFF: kCGImageFormatTIFF
}

COLOR_SPACE = CGColorSpaceCreateWithName(kCGColorSpaceGenericRGB)

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
def processSrcPDF():
    # source image
    log("Loading source image from %s" % SRC_PATH, 2)
    pdf_document = CGPDFDocumentCreateWithProvider(CGDataProviderCreateWithFilename(SRC_PATH))
    page = pdf_document.getPage(PAGE_NUMBER)
    page_rect = page.getBoxRect(kCGPDFCropBox)
    log("Default PDF page size: %d x %d" % (page_rect.getWidth(), page_rect.getHeight()), 1)
    page_width = int(page_rect.getWidth() * PDF_SCALE_FACTOR)
    page_height = int(page_rect.getHeight() * PDF_SCALE_FACTOR)
    log("Target bitmap size: %d x %d" % (page_width, page_height), 1)
    outputSceneFile = "%s/scene.xml" % TGT_DIR
    outputroot = ET.Element("scene")
    x = 0
    y = 0
    while y < page_height:
        x = 0
        while x < page_width:
            generateTile(pdf_document, page, PAGE_NUMBER, x, y, "%s/tile-%d-%d.%s" % (TGT_DIR, x/TILE_SIZE, y/TILE_SIZE, OUTPUT_TYPE))

            include = ET.SubElement(outputroot, "include")
            include.set("src", "tile-%d-%d/scene.xml" % (x/TILE_SIZE, y/TILE_SIZE))
            include.set("x", "%d" % x)
            include.set("y", "%d" % y)

            x += TILE_SIZE
        y += TILE_SIZE
    log("Writing scene file %s" % outputSceneFile, 1)
    tree = ET.ElementTree(outputroot)
    tree.write(outputSceneFile, encoding='utf-8')


def generateTile(pdf_document, page, page_number, x, y, tgtPath):
    if os.path.exists(tgtPath) and not FORCE_GENERATE_TILES:
        log("Tile %s already exists" % (tgtPath), 2)
        return
    log("Generating tile (%d,%d) (%d,%d)" % (x,y,x+TILE_SIZE,y+TILE_SIZE), 2)
    bitmap = CGBitmapContextCreateWithColor(TILE_SIZE, TILE_SIZE,\
                                            COLOR_SPACE, CGFloatArray(4))
    rect = page.getBoxRect(kCGPDFMediaBox)
    rect = rect.offset(-x,-y)
    rect.size.width = int(rect.size.width * PDF_SCALE_FACTOR)
    rect.size.height = int(rect.size.height * PDF_SCALE_FACTOR)
    bitmap.drawPDFDocument(rect, pdf_document, page_number)
    log("\tWriting bitmap to %s (page %s)" % (tgtPath, page_number), 2)
    bitmap.writeToFile(tgtPath, OUTPUT_TYPE2CG[OUTPUT_TYPE])


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
                TILE_SIZE = int(arg[len("-ts="):])
            elif arg == "-f":
                FORCE_GENERATE_TILES = True
                log("Force tile generation")
            elif arg.startswith("-tl="):
                TRACE_LEVEL = int(arg[len("-tl="):])
            elif arg.startswith("-scale="):
                PDF_SCALE_FACTOR = float(arg[len("-scale="):])
            elif arg.startswith("-format"):
                OUTPUT_TYPE = arg[len("-format="):]
            elif arg.startswith("-page"):
                PAGE_NUMBER = int(arg[len("-page="):])
else:
    log(CMD_LINE_HELP)
    sys.exit(0)

log("--------------------")
log("Tile Size: %dx%d" % (TILE_SIZE, TILE_SIZE), 1)
createTargetDir()
processSrcPDF()
log("--------------------")
