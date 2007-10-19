#!/usr/bin/python

# $Id: cdataRemover.py,v 1.3 2007/08/22 08:27:52 pietriga Exp $

# Usage:
#    cdataRemover.py <src_dir>

import os, sys

TRACE_LEVEL = 1

def processDirectory(directory):
    for f in os.listdir(directory):
        f_abs_path = "%s/%s" % (directory, f)
        if os.path.isdir(f_abs_path):
            processDirectory(f_abs_path)
        elif f == "metadata.xml":
            processMetadata(f_abs_path)

def processMetadata(src):
    log("Processing %s" % src, 1)
    f1 = open(src, "r")
    f2 = open("%smetadata_nocdata.xml" % src[:-len("metadata.xml")], "w")
    for line in f1.readlines():
#        line = line.replace("<![CDATA[", "")
#        line = line.replace("]]>", "")
#        line = line.replace(" & ", "&amp;")
        line = line.replace("&amp;#", "&#")
        line = line.replace(" & ", "&amp;")
        f2.write(line)
    f1.close()
    f2.flush()
    f2.close()
    
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
    SRC_DIR = os.path.realpath(sys.argv[1])
    if len(sys.argv) > 2:
        TRACE_LEVEL = int(sys.argv[2])
else:
    sys.exit(0)
processDirectory(SRC_DIR)
