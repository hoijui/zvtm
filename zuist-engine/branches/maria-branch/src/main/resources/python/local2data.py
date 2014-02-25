#!/usr/bin/python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2010. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

import sys

def translate(i,o):
    fi = open(i, 'r')
    fo = open(o, 'w')
    for line in fi.readlines():
        srcStart = line.find("src=")
        if srcStart != -1:
            srcStartQuote = srcStart + 4
            srcEndQuote = line.find("\"", srcStartQuote+1)
            src = line[srcStartQuote+1:srcEndQuote]
            if src == "lnd.png":
                src = "http://data.wild.lri.fr/tiles/lnd.png"
            elif src == "sea.png":
                src = "http://data.wild.lri.fr/tiles/sea.png"                
            else:
                src = src.split("/")
                src = "http://data.wild.lri.fr/py/zuistServer/getTile?z=%s&amp;col=%s&amp;row=%s" % (src[0], src[1], src[2][:-4])
            line = "%ssrc=\"%s\"%s" % (line[:srcStart],src,line[srcEndQuote+1:])
        fo.write(line)
    fo.flush()
    fo.close()

if __name__ == "__main__":
    SRC_FILE = sys.argv[1]
    TGT_FILE = sys.argv[2]
    translate(SRC_FILE, TGT_FILE)
