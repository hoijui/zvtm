#!/usr/bin/python

# $Id$

import os, sys
from CoreGraphics import CGColorSpaceCreateWithName, kCGColorSpaceGenericRGB,\
    CGDataProviderCreateWithFilename, CGPDFDocumentCreateWithProvider,\
    CGBitmapContextCreateWithColor, kCGImageFormatPNG, kCGPDFMediaBox

USAGE_MSG = """
Usage: pdf2pngpages.py <src_dir> <dst_dir> [trace_level]

    src_dir = Name of source directory containing PDF documents to be processed
    dst_dir = Name of destination directory in which PNG pages will be stored
    trace_level = 0 (only warnings and errors)
                  1 (main steps)
                  2 (detailed execution)
"""

PDF_EXT = ".pdf"

TRACE_LEVEL = 1
# CGPDFDocument.getMediaBox returns an instance of CGRect with a given size,
# that seems to be computed to have a PDF page fit on screen (about 600x800)
# set SCALE_FACTOR higher to get a higher resolution rendering of each page
DEFAULT_RES_SCALE_FACTOR = 1
HIGHER_RES_SCALE_FACTOR = 2

################################################################################
# Walk source hiearchy and mirror it in destination folder
################################################################################
def processDirectory(d_src, d_dst):
    for f in os.listdir(d_src):
        # compute child path in src tree
        src_dir_abspath = "%s/%s" % (os.path.realpath(d_src), f)
        # compute child path in dst tree
        dst_dir_abspath = "%s/%s" % (os.path.realpath(d_dst), f)
        if os.path.isdir(src_dir_abspath):
            if not os.path.exists(dst_dir_abspath):
                log("Creating %s" % dst_dir_abspath, 2)
                os.mkdir(dst_dir_abspath)
            processDirectory(src_dir_abspath, dst_dir_abspath)
        elif f.endswith(PDF_EXT):
            processDocument(d_src, d_dst, os.path.basename(src_dir_abspath))

################################################################################
# PDF document processing
################################################################################
def processDocument(parent_dir_src, parent_dir_dst, document_name):
    document_dir_basename = document_name[:-len(PDF_EXT)]
    document_dir_abspath = "%s/%s" % (parent_dir_dst, document_dir_basename)
    if not os.path.exists(document_dir_abspath):
        log("Creating %s" % document_dir_abspath, 2)
        os.mkdir(document_dir_abspath)
        # os.mkdir("%s/%s" % (document_dir_abspath, "L"))
        os.mkdir("%s/%s" % (document_dir_abspath, "H"))
    document_abspath = "%s/%s" % (parent_dir_src, document_name)
    # parse input PDF document
    pdf_document =\
        CGPDFDocumentCreateWithProvider(CGDataProviderCreateWithFilename(\
            document_abspath))
    if pdf_document is None:
		log("Error reading PDF document %s" % document_abspath)
    pageCount = pdf_document.getNumberOfPages()
    log("Processing %s [%s pages]" % (document_abspath, pageCount), 1)
    # page number index is 1-based
    for page_number in range(1, pageCount+1):
        # writePage(pdf_document, page_number, DEFAULT_RES_SCALE_FACTOR, document_dir_basename, parent_dir_dst, "L")
        writePage(pdf_document, page_number, HIGHER_RES_SCALE_FACTOR, document_dir_basename, parent_dir_dst, "H")

def writePage(pdf_document, page_number, scaleFactor, document_dir_basename, parent_dir_dst, res_dir_name):
    # for each page, create a bitmap
    page_rect = pdf_document.getPage(page_number).getBoxRect(kCGPDFMediaBox)
    # above replaced the following deprecated instruction: pdf_document.getMediaBox(page_number)
    page_width = int(page_rect.getWidth() * scaleFactor)
    page_height = int(page_rect.getHeight() * scaleFactor)
    bitmap = CGBitmapContextCreateWithColor(page_width, page_height,\
                                            COLOR_SPACE, (1,1,1,1))
    bitmap.scaleCTM(scaleFactor, scaleFactor)
    # draw the PDF page on it
    bitmap.drawPDFDocument(page_rect, pdf_document, page_number)
    # encode it in PNG and save it in a file
    page_abspath = "%s/%s/%s/%s_p%d.png" % (parent_dir_dst,\
                                            document_dir_basename,\
                                            res_dir_name,\
                                            document_dir_basename, page_number)
    log("Writing page %s [%dx%d]" % (page_number, page_width, page_height),\
        2)
    bitmap.writeToFile(page_abspath, kCGImageFormatPNG)
    
        
################################################################################
# Trace exec on std output
################################################################################
def log(msg, level=0):
    if level <= TRACE_LEVEL:
        print msg

################################################################################
# main
################################################################################
if len(sys.argv) > 3:
    TRACE_LEVEL = int(sys.argv[3])
    if len(sys.argv) > 4:
        SCALE_FACTOR = int(sys.argv[4])
elif len(sys.argv) < 3:
    print USAGE_MSG
    sys.exit(0)
# expand paths
SRC_DIR = os.path.realpath(sys.argv[1])
DST_DIR = os.path.realpath(sys.argv[2])
COLOR_SPACE = CGColorSpaceCreateWithName(kCGColorSpaceGenericRGB)
# create target directory if necesary
if not os.path.exists(DST_DIR):
    log("Creating %s" % DST_DIR, 2)
    os.mkdir(DST_DIR)
# process source tree starting at root
if os.path.exists(SRC_DIR):
    processDirectory(SRC_DIR, DST_DIR)
