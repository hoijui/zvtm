#!/usr/bin/python
# -*- coding: UTF-8 -*-
# $Id$

from PIL import Image

TRACE_LEVEL = 1

def processSrcMap():
    

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
    TGT_DIR = os.path.realpath(sys.argv[2])    
    if len(sys.argv) > 3:
        TRACE_LEVEL = int(sys.argv[3])
else:
    sys.exit(0)

processSrcMap()
