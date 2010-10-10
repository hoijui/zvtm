#!/usr/bin/python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2010. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

import os, sys

def generateJavaClass(lut_file, src_dir, tgt_dir):
    className = lut_file[:-4]
    className = "%s%sGradient" % (className[0:1].upper(), className[1:])
    ifp = "%s/%s" % (src_dir, lut_file)
    ofp = "%s/%s.java" % (tgt_dir, className)
    print "%s -> %s" % (ifp, ofp)
    
    return

if len(sys.argv) > 1:
    SRC_DIR = os.path.realpath(sys.argv[1])
    if not os.path.isdir(SRC_DIR):
        print "%s not a directory" % SRC_DIR
    if len(sys.argv) > 2:
        TGT_DIR = os.path.realpath(sys.argv[2])
    else:
        TGT_DIR = SRC_DIR
    for f in os.listdir(SRC_DIR):
        if f.lower().endswith(".lut"):
            generateJavaClass(f, SRC_DIR, TGT_DIR)
