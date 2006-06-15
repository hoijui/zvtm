#!/sw/bin/python

## $Id: mapManagerGenerator.py,v 1.6 2006/04/05 06:24:16 epietrig Exp $


print "/*   FILE: MapData.java\n *   DATE OF CREATION:  Tue Mar 07 18:26:11 2006\n *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)\n *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)\n *   Copyright (c) INRIA, 2006. All Rights Reserved\n *   Licensed under the GNU LGPL. For full terms see the file COPYING.\n *\n * $Id: mapManagerGenerator.py,v 1.6 2006/04/05 06:24:16 epietrig Exp $\n */ \n\n"

print "/*------------- THIS FILE IS GENERATED AUTOMATICALLY -------------*/\n/*           by a Python script: mapManagerGenerator.py           */\n"

print "package net.claribole.zvtm.eval;\n\nimport java.awt.Image;\nimport javax.swing.ImageIcon;\n\nimport java.util.Hashtable;\nimport java.util.Vector;\n\nimport com.xerox.VTM.engine.LongPoint;\nimport com.xerox.VTM.glyphs.VImage;\n\n"

print "public class MapData {\n"

################################################################################
print "\n    /* ----------------- level 0 (8000x4000, 1 map)  ------------------------ */\n\n    static final Double MN000factor = new Double(8.0);\n    static final String M1000 = \"1000\";\n    static final String M1000path = \"images/world/1000.png\";\n    static final long M1000x = 0;\n    static final long M1000y = 0;"

################################################################################
print "\n    /* ----------------- level 1 (4000x4000, 8 maps) ------------------------ */\n    static final Double MNN00factor = new Double(4.0);"

################################################################################
for i in [1,2,3,4,5,6,7,8]:
    print "    static final String M1%s00 = \"1%s00\";" % (i,i)

################################################################################
for i in [1,2,3,4,5,6,7,8]:
    print "    static final String M1%s00path = \"images/world/1%s00.png\";" % (i,i)

################################################################################
STEP = 16000
x = -24000
y = 8000
for i in [1,2,3,4]:
    print "    static final long M1%s00x = %s;" % (i,x)
    print "    static final long M1%s00y = %s;" % (i,y)
    x = x + STEP
x = -24000
y = -8000
for i in [5,6,7,8]:
    print "    static final long M1%s00x = %s;" % (i,x)
    print "    static final long M1%s00y = %s;" % (i,y)
    x = x + STEP

################################################################################
w = -32000
n = 16000
for i in [1,2,3,4]:
    print "    static final long[] M1%s00region = {%s,%s,%s,%s};" % (i,w,n,w+STEP,n-STEP)
    w = w + STEP
w = -32000
n = 0
for i in [5,6,7,8]:
    print "    static final long[] M1%s00region = {%s,%s,%s,%s};" % (i,w,n,w+STEP,n-STEP)
    w = w + STEP

################################################################################
print "\n    /* ----------------- level 2 (4000x4000, 32 maps) ----------------------- */\n\n    static final Double MNNN0factor = new Double(2.0f);"

################################################################################
for i in [1,2,3,4,5,6,7,8]:
    for j in [1,2,3,4]:
        print "    static final String M1%s%s0 = \"1%s%s0\";" % (i,j,i,j)
            
################################################################################
for i in [1,2,3,4,5,6,7,8]:
    for j in [1,2,3,4]:
        print "    static final String M1%s%s0path = \"images/world/1%s%s0.png\";" % (i,j,i,j)
            
################################################################################
STEP = 8000
x = -28000
y = 12000
for i in [1,2,3,4]:
    for j in [1,2]:
        print "    static final long M1%s%s0x = %s;" % (i,j,x)
        print "    static final long M1%s%s0y = %s;" % (i,j,y)
        x = x + STEP
x = -28000
y = 4000
for i in [1,2,3,4]:
    for j in [3,4]:
        print "    static final long M1%s%s0x = %s;" % (i,j,x)
        print "    static final long M1%s%s0y = %s;" % (i,j,y)
        x = x + STEP
x = -28000
y = -4000
for i in [5,6,7,8]:
    for j in [1,2]:
        print "    static final long M1%s%s0x = %s;" % (i,j,x)
        print "    static final long M1%s%s0y = %s;" % (i,j,y)
        x = x + STEP
x = -28000
y = -12000
for i in [5,6,7,8]:
    for j in [3,4]:
        print "    static final long M1%s%s0x = %s;" % (i,j,x)
        print "    static final long M1%s%s0y = %s;" % (i,j,y)
        x = x + STEP
        
################################################################################
w = -32000
n = 16000
for i in [1,2,3,4]:
    for j in [1,2]:
        print "    static final long[] M1%s%s0region = {%s,%s,%s,%s};" % (i,j,w,n,w+STEP,n-STEP)
        w = w + STEP
w = -32000
n = 8000
for i in [1,2,3,4]:
    for j in [3,4]:
        print "    static final long[] M1%s%s0region = {%s,%s,%s,%s};" % (i,j,w,n,w+STEP,n-STEP)
        w = w + STEP
w = -32000
n = 0
for i in [5,6,7,8]:
    for j in [1,2]:
        print "    static final long[] M1%s%s0region = {%s,%s,%s,%s};" % (i,j,w,n,w+STEP,n-STEP)
        w = w + STEP
w = -32000
n = -8000
for i in [5,6,7,8]:
    for j in [3,4]:
        print "    static final long[] M1%s%s0region = {%s,%s,%s,%s};" % (i,j,w,n,w+STEP,n-STEP)
        w = w + STEP

################################################################################
print "    /* ----------------- level 3 (4000x4000, 128 maps) ----------------------- */\n    static final Double MNNNNfactor = new Double(1.0f);"

################################################################################
for i in [1,2,3,4,5,6,7,8]:
    for j in [1,2,3,4]:
        for k in [1,2,3,4]:
            print "    static final String M1%s%s%s = \"1%s%s%s\";" % (i,j,k,i,j,k)
            
################################################################################
for i in [1,2,3,4,5,6,7,8]:
    for j in [1,2,3,4]:
        for k in [1,2,3,4]:
            print "    static final String M1%s%s%spath = \"images/world/1%s%s%s.png\";" % (i,j,k,i,j,k)

################################################################################
STEP = 4000
x = -30000
y = 14000
for i in [1,2,3,4]:
    for j in [1,2]:
        for k in [1,2]:
            print "    static final long M1%s%s%sx = %s;" % (i,j,k,x)
            print "    static final long M1%s%s%sy = %s;" % (i,j,k,y)
            x = x + STEP
x = -30000
y = 10000
for i in [1,2,3,4]:
    for j in [1,2]:
        for k in [3,4]:
            print "    static final long M1%s%s%sx = %s;" % (i,j,k,x)
            print "    static final long M1%s%s%sy = %s;" % (i,j,k,y)
            x = x + STEP
x = -30000
y = 6000
for i in [1,2,3,4]:
    for j in [3,4]:
        for k in [1,2]:
            print "    static final long M1%s%s%sx = %s;" % (i,j,k,x)
            print "    static final long M1%s%s%sy = %s;" % (i,j,k,y)
            x = x + STEP
x = -30000
y = 2000
for i in [1,2,3,4]:
    for j in [3,4]:
        for k in [3,4]:
            print "    static final long M1%s%s%sx = %s;" % (i,j,k,x)
            print "    static final long M1%s%s%sy = %s;" % (i,j,k,y)
            x = x + STEP
x = -30000
y = -2000
for i in [5,6,7,8]:
    for j in [1,2]:
        for k in [1,2]:
            print "    static final long M1%s%s%sx = %s;" % (i,j,k,x)
            print "    static final long M1%s%s%sy = %s;" % (i,j,k,y)
            x = x + STEP
x = -30000
y = -6000
for i in [5,6,7,8]:
    for j in [1,2]:
        for k in [3,4]:
            print "    static final long M1%s%s%sx = %s;" % (i,j,k,x)
            print "    static final long M1%s%s%sy = %s;" % (i,j,k,y)
            x = x + STEP
x = -30000
y = -10000
for i in [5,6,7,8]:
    for j in [3,4]:
        for k in [1,2]:
            print "    static final long M1%s%s%sx = %s;" % (i,j,k,x)
            print "    static final long M1%s%s%sy = %s;" % (i,j,k,y)
            x = x + STEP
x = -30000
y = -14000
for i in [5,6,7,8]:
    for j in [3,4]:
        for k in [3,4]:
            print "    static final long M1%s%s%sx = %s;" % (i,j,k,x)
            print "    static final long M1%s%s%sy = %s;" % (i,j,k,y)
            x = x + STEP

################################################################################
w = -32000
n = 16000
for i in [1,2,3,4]:
    for j in [1,2]:
        for k in [1,2]:
            print "    static final long[] M1%s%s%sregion = {%s,%s,%s,%s};" % (i,j,k,w,n,w+STEP,n-STEP)
            w = w + STEP
w = -32000
n = 12000
for i in [1,2,3,4]:
    for j in [1,2]:
        for k in [3,4]:
            print "    static final long[] M1%s%s%sregion = {%s,%s,%s,%s};" % (i,j,k,w,n,w+STEP,n-STEP)
            w = w + STEP
w = -32000
n = 8000
for i in [1,2,3,4]:
    for j in [3,4]:
        for k in [1,2]:
            print "    static final long[] M1%s%s%sregion = {%s,%s,%s,%s};" % (i,j,k,w,n,w+STEP,n-STEP)
            w = w + STEP
w = -32000
n = 4000
for i in [1,2,3,4]:
    for j in [3,4]:
        for k in [3,4]:
            print "    static final long[] M1%s%s%sregion = {%s,%s,%s,%s};" % (i,j,k,w,n,w+STEP,n-STEP)
            w = w + STEP
w = -32000
n = 0
for i in [5,6,7,8]:
    for j in [1,2]:
        for k in [1,2]:
            print "    static final long[] M1%s%s%sregion = {%s,%s,%s,%s};" % (i,j,k,w,n,w+STEP,n-STEP)
            w = w + STEP
w = -32000
n = -4000
for i in [5,6,7,8]:
    for j in [1,2]:
        for k in [3,4]:
            print "    static final long[] M1%s%s%sregion = {%s,%s,%s,%s};" % (i,j,k,w,n,w+STEP,n-STEP)
            w = w + STEP
w = -32000
n = -8000
for i in [5,6,7,8]:
    for j in [3,4]:
        for k in [1,2]:
            print "    static final long[] M1%s%s%sregion = {%s,%s,%s,%s};" % (i,j,k,w,n,w+STEP,n-STEP)
            w = w + STEP
w = -32000
n = -12000
for i in [5,6,7,8]:
    for j in [3,4]:
        for k in [3,4]:
            print "    static final long[] M1%s%s%sregion = {%s,%s,%s,%s};" % (i,j,k,w,n,w+STEP,n-STEP)
            w = w + STEP

################################################################################

print "\n    /*put all this stuff in a hashtable for generalized access*/\n    static Hashtable mapInfo;\n    static {"
print "        mapInfo = new Hashtable();"
print "        // Level 0 - M1000"
print "        Vector v = new Vector();"
print "        v.add(M1000path);"
print "        v.add(new LongPoint(M1000x, M1000y));"
print "        v.add(MN000factor);"
print "        mapInfo.put(M1000, v);"
print "        // Level 1 - M1N00"
for i in [1,2,3,4,5,6,7,8]:
    id = "%s" % (i)
    print "        v = new Vector();"
    print "        v.add(M1%s00path);" % (id)
    print "        v.add(new LongPoint(M1%s00x, M1%s00y));" % (id,id)
    print "        v.add(MNN00factor);"
    print "        mapInfo.put(M1%s00, v);" % (id)
print "        // Level 2 - M1NN0"
for i in [1,2,3,4,5,6,7,8]:
    for j in [1,2,3,4]:
        id = "%s%s" % (i,j)
        print "        v = new Vector();"
        print "        v.add(M1%s0path);" % (id)
        print "        v.add(new LongPoint(M1%s0x, M1%s0y));" % (id,id)
        print "        v.add(MNNN0factor);"
        print "        mapInfo.put(M1%s0, v);" % (id)
print "        // Level 3 - M1NNN"
for i in [1,2,3,4,5,6,7,8]:
    for j in [1,2,3,4]:
        for k in [1,2,3,4]:
            id = "%s%s%s" % (i,j,k)
            print "        v = new Vector();"
            print "        v.add(M1%spath);" % (id)
            print "        v.add(new LongPoint(M1%sx, M1%sy));" % (id,id)
            print "        v.add(MNNNNfactor);"
            print "        mapInfo.put(M1%s, v);" % (id)

print "    }\n}"
