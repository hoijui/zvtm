#!/usr/bin/python

## $Id:  $

DEBUG = 1

print "    /* called each time the camera moves - detects what regions are visible depending "
print "       on the level of details and calls methods to load/unload maps accordingly */"
print "    void updateVisibleMaps(long[] wnes, boolean onlyIfSameLevel, short actualLevel){"
print "	       if ((onlyIfSameLevel && (actualLevel != lod)) || !adaptMaps){"
print "	           /* do not update visible maps if the actual level is different from the"
print "	              level the app believes it is at (happens when a lens is active, as"
print "	              updateMapLevel call are temporarily freezed)*/"
print "	           /*do not update them if user disabled updates explicitly either*/"
print "	           return;"
print "	       }"
print "        if (lod == LEVEL_3){// if dealing with maps at level of detail = 1"
print "            updateVisibleMapsL3(wnes);"
print "        }"
print "        else if (lod == LEVEL_2){// if dealing with maps at level of detail = 1"
print "            updateVisibleMapsL2(wnes);"
print "        }"
print "        else if (lod == LEVEL_1){// if dealing with maps at level of detail = 1"
print "            updateVisibleMapsL1(wnes);"
print "        }"
print "        /* else if lod == LEVEL_0 there is nothing to do as this is the main map\n           (always visible);level 1 removal delt with through updateMapLevel */"
print "    }\n"

################################################################################
print "    void updateVisibleMapsL1(long[] wnes){"
for i in [1,2,3,4,5,6,7,8]:
    print "        // Level 1 - M1%s00" % (i)
    index = i-1
    print "        if (M1N00im[%s] == null){" %(index)
    print "            if (intersectsRegion(wnes, MapData.M1%s00region)){showMapL1(MapData.M1%s00, %s);}" % (i, i, index)
    print "            else if (M1N00lrq[%s] != null && !M1N00lrqs[%s]){cancelRequest(M1N00lrq[%s].ID, M1N00lrq, %s);}" % (index,index,index,index)
    print "        }"
    print "        else {"
    print "            if (!intersectsRegion(wnes, MapData.M1%s00region)){hideMapL1(MapData.M1%s00, %s);}" % (i, i, index)
    print "        }"
print "    }\n"

################################################################################
print "    void updateVisibleMapsL2(long[] wnes){"
for i in [1,2,3,4,5,6,7,8]:
    print "        // Level 2 - M1%sN0" % (i)
    print "        if (intersectsRegion(wnes, MapData.M1%s00region)){" % (i)
    for j in [1,2,3,4]:
        index = (i-1)*4 + j - 1
        print "            if (M1NN0im[%s] == null){" % (index)
        print "                if (intersectsRegion(wnes, MapData.M1%s%s0region)){showMapL2(MapData.M1%s%s0, %s);}" % (i,j,i,j,index)
        print "                else if (M1NN0lrq[%s] != null && !M1NN0lrqs[%s]){cancelRequest(M1NN0lrq[%s].ID, M1NN0lrq, %s);}" % (index,index,index,index)
        print "            }"
        print "            else {"
        print "                if (!intersectsRegion(wnes, MapData.M1%s%s0region)){hideMapL2(MapData.M1%s%s0, %s);}" % (i,j,i,j,index)
        print "            }"
    print "        }"
    print "        else {"
    for j in [1,2,3,4]:
        index = (i-1)*4 + j - 1
        print "            if (M1NN0im[%s] != null){hideMapL2(MapData.M1%s%s0, %s);}" % (index,i,j,index)
        print "            else if (M1NN0lrq[%s] != null && !M1NN0lrqs[%s]){cancelRequest(M1NN0lrq[%s].ID, M1NN0lrq, %s);}" % (index,index,index,index)
    print "        }"
print "    }\n"

################################################################################
print "    void updateVisibleMapsL3(long[] wnes){"
for i in [1,2,3,4,5,6,7,8]:
    print "        if (intersectsRegion(wnes, MapData.M1%s00region)){" % (i)
    for j in [1,2,3,4]:
        print "            if (intersectsRegion(wnes, MapData.M1%s%s0region)){" % (i,j)
        for k in [1,2,3,4]:
            index = (i-1)*16 + (j-1)*4 + k - 1
            print "		if (M1NNNim[%s] == null){" % (index)
            print "		    if (intersectsRegion(wnes, MapData.M1%s%s%sregion)){showMapL3(MapData.M1%s%s%s, %s);}" % (i,j,k,i,j,k,index)
            print "                 else if (M1NNNlrq[%s] != null && !M1NNNlrqs[%s]){cancelRequest(M1NNNlrq[%s].ID, M1NNNlrq, %s);}" % (index,index,index,index)
            print "		}"
            print "		else {"
            print "		    if (!intersectsRegion(wnes, MapData.M1%s%s%sregion)){hideMapL3(MapData.M1%s%s%s, %s);}" % (i,j,k,i,j,k,index)
            print "		}"
        print "            }"
        print "            else {"
        for k in [1,2,3,4]:
            index = (i-1)*16 + (j-1)*4 + k - 1
            print "              if (M1NNNim[%s] != null){hideMapL3(MapData.M1%s%s%s, %s);}" % (index,i,j,k,index)
            print "              else if (M1NNNlrq[%s] != null && !M1NNNlrqs[%s]){cancelRequest(M1NNNlrq[%s].ID, M1NNNlrq, %s);}" % (index,index,index,index)
        print "            }"
    print "        }"
    print "        else {"
    for j in [1,2,3,4]:
        for k in [1,2,3,4]:
            index = (i-1)*16 + (j-1)*4 + k - 1
            print "            if (M1NNNim[%s] != null){hideMapL3(MapData.M1%s%s%s, %s);}" % (index,i,j,k,index)
            print "            else if (M1NNNlrq[%s] != null && !M1NNNlrqs[%s]){cancelRequest(M1NNNlrq[%s].ID, M1NNNlrq, %s);}" % (index,index,index,index)
    print "        }"
print "    }\n"
