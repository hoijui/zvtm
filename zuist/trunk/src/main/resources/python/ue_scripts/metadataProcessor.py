#!/usr/bin/python

# $Id$

# Usage:
#    metadataProcessor.py <src_dir> <stylesheet>

import os, sys
import math
import elementtree.ElementTree as ET

TRACE_LEVEL = 1

def processDirectory(directory, stylesheet):
    for f in os.listdir(directory):
        f_abs_path = "%s/%s" % (directory, f)
        if os.path.isdir(f_abs_path):
            processDirectory(f_abs_path, stylesheet)
        elif f.startswith("ACM") and f.endswith(".xml"):
            processMetadata(f_abs_path, stylesheet)

def processMetadata(f, stylesheet):
    log("Processing %s" % f, 1)
    command = "xsltproc -o %smetadata.xml %s %s" % (f[:f.index("ACM-")], stylesheet, f)
    log("Executing %s" % command, 2)
    os.popen(command)

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
    SRC_DIR = os.path.realpath(sys.argv[1])
    STYLESHEET_PATH = os.path.realpath(sys.argv[2])
    if len(sys.argv) > 3:
        TRACE_LEVEL = int(sys.argv[3])
else:
    sys.exit(0)
processDirectory(SRC_DIR, STYLESHEET_PATH)
