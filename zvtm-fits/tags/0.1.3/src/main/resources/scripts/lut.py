#!/usr/bin/python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2010. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

import os, sys

def generateJavaClass(lut_file, src_dir, tgt_dir):
    className = lut_file[:-4]
    className = "%s%sFilter" % (className[0:1].upper(), className[1:])
    ifp = "%s/%s" % (src_dir, lut_file)
    ofp = "%s/%s.java" % (tgt_dir, className)
    print "%s -> %s" % (ifp, ofp)
    lf = open(ifp, 'r')
    jf = open(ofp, 'w')
    # header
    jf.write("/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */\n\n")
    jf.write("package fr.inria.zvtm.fits.filters;\n\n")
    jf.write("import java.awt.Color;\n")
    jf.write("import java.awt.image.RGBImageFilter;\n\n")
    jf.write("import java.awt.LinearGradientPaint;\n\n")
    jf.write("public class %s extends RGBImageFilter implements ColorGradient {\n\n" % className)
    jf.write("    private static final Color[] map = new Color[128];\n\n")
    #values
    jf.write("    static {\n")
    i = 0
    for line in lf.readlines():
        # rgb components separated by one or more white spaces in .lut file
        rgb = line.split()
        jf.write("        map[%s] = new Color(%sf, %sf, %sf);\n" % (i, rgb[0], rgb[1], rgb[2]))
        i += 1
    #footer
    jf.write("    }\n\n")

    jf.write("    public %s(){}\n\n" % className)

    jf.write("    public int filterRGB(int x, int y, int rgb){\n")
    jf.write("        return map[(rgb & 0xff)/2].getRGB();\n")
    jf.write("    }\n\n")
    
    jf.write("    public LinearGradientPaint getGradient(float w){\n")
    jf.write("        return getGradientS(w);\n")
    jf.write("    }\n\n")

    jf.write("    public static LinearGradientPaint getGradientS(float w){\n")
    jf.write("        float[] fractions = new float[map.length];\n")
    jf.write("        for (int i=0;i<fractions.length;i++){\n")
    jf.write("            fractions[i] = i / (float)fractions.length;\n")
    jf.write("        }\n")
    jf.write("        return new LinearGradientPaint(0, 0, w, 0, fractions, map);\n")
    jf.write("    }\n\n")
    
    jf.write("}\n")

    jf.flush()
    jf.close()
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
