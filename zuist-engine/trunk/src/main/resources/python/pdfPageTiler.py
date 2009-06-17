#!/usr/bin/python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2009. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id:  $

import os, sys, math
from copy import copy
# http://developer.apple.com/documentation/GraphicsImaging/Conceptual/drawingwithquartz2d/dq_python/dq_python.html
from CoreGraphics import *

CMD_LINE_HELP = "ZUIST PDF Page Tiler Script\n\nUsage:\n\n" + \
    " \tpdfPageTiler <src_image_path> <target_dir> [options]\n\n" + \
    "Options:\n\n"+\
    "\t-ts=N\t\ttile size (N in pixels)\n"+\
    "\t-f\t\tforce tile generation\n"+\
    "\t-tl=N\t\ttrace level (N in [0:3])\n"

TRACE_LEVEL = 1

FORCE_GENERATE_TILES = False

TILE_SIZE = 500

OUTPUT_FILE_EXT = "png"

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
    #pdf_document = CGPDFDocumentCreateWithProvider(CGDataProviderCreateWithFilename(SRC_PATH))
    #page_rect = pdf_document.getPage(1).getBoxRect(1)
    #log("Default PDF page size: %d x %d" % (page_rect.getWidth(), page_rect.getHeight()), 1)
    #page_width = int(page_rect.getWidth() * PDF_SCALE_FACTOR)
    #page_height = int(page_rect.getHeight() * PDF_SCALE_FACTOR)
    #log("Target bitmap size: %d x %d" % (page_width, page_height), 1)
    #bitmap = CGBitmapContextCreateWithColor(page_width, page_height,\
    #                                        COLOR_SPACE, (1,1,1,1))
    #log("\tRescaling bitmap", 3)
    #bitmap.scaleCTM(PDF_SCALE_FACTOR, PDF_SCALE_FACTOR)
    #log("\tDrawing PDF to bitmap", 3)
    #bitmap.drawPDFDocument(page_rect, pdf_document, 1)
    #log("\tWriting bitmap to temp file", 3)
    #bitmap.writeToFile(IMG_SRC_PATH, kCGImageFormatPNG)

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
            elif arg == "-f":
                FORCE_GENERATE_TILES = True
                log("Force tile generation")
            elif arg.startswith("-tl="):
                TRACE_LEVEL = int(arg[4:])
else:
    log(CMD_LINE_HELP)
    sys.exit(0)

log("--------------------")
log("Tile Size: %dx%d" % (TILE_SIZE, TILE_SIZE), 1)
createTargetDir()
processSrcPDF()
log("--------------------")
