#!/usr/bin/python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2010. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

import mapnik

import sys, os, math

TILE_DIR = "/home/osm/tiles"

DEG_TO_RAD = math.pi / 180
RAD_TO_DEG = 180 / math.pi

TS_I = 512

MAX_ZOOM = 18

################################################################################
# Projection
################################################################################
class GoogleProjection:
    def __init__(self,levels=18):
        self.Bc = []
        self.Cc = []
        self.zc = []
        self.Ac = []
        c = TS_I
        for d in range(0,levels):
            e = c/2;
            self.Bc.append(c/360.0)
            self.Cc.append(c/(2 * math.pi))
            self.zc.append((e,e))
            self.Ac.append(c)
            c *= 2
                
    def fromLLtoPixel(self,ll,zoom):
         d = self.zc[zoom]
         e = round(d[0] + ll[0] * self.Bc[zoom])
         f = minmax(math.sin(DEG_TO_RAD * ll[1]),-0.9999,0.9999)
         g = round(d[1] + 0.5*math.log((1+f)/(1-f))*-self.Cc[zoom])
         return (e,g)
     
    def fromPixelToLL(self,px,zoom):
         e = self.zc[zoom]
         f = (px[0] - e[0])/self.Bc[zoom]
         g = (px[1] - e[1])/-self.Cc[zoom]
         h = RAD_TO_DEG * ( 2 * math.atan(math.exp(g)) - 0.5 * math.pi)
         return (f,h)

###############################################################################
# Tile generation
###############################################################################
def generateTile(z=-1, col=-1, row=-1):
    map_path = "%s/%s/%s/%s.png" % (TILE_DIR, z, col, row)
    if not os.path.exists("%s/%s/%s" % (TILE_DIR, z, col)):
        if not os.path.exists("%s/%s" % (TILE_DIR, z)):
            os.mkdir("%s/%s" % (TILE_DIR, z))
        os.mkdir("%s/%s/%s" % (TILE_DIR, z, col))
    m = mapnik.Map(TS_I, TS_I)
    try:
        mapfile = os.environ['MAPNIK_MAP_FILE']
    except KeyError:
        mapfile = "/home/osm/mapnik/zuist_osm.xml"
    prj = mapnik.Projection(m.srs)
    gprj = GoogleProjection(MAX_ZOOM+1)
    # Calculate pixel positions of bottom-left & top-right
    p0 = (col * TS_I, (row + 1) * TS_I)
    p1 = ((col + 1) * TS_I, row * TS_I)
    # Convert to LatLong (EPSG:4326)
    l0 = gprj.fromPixelToLL(p0, z);
    l1 = gprj.fromPixelToLL(p1, z);
    # Convert to map projection (e.g. mercator co-ords EPSG:900913)
    c0 = prj.forward(mapnik.Coord(l0[0],l0[1]))
    c1 = prj.forward(mapnik.Coord(l1[0],l1[1]))
    # Bounding box for the tile
    if hasattr(mapnik,'mapnik_version') and mapnik.mapnik_version() >= 800:
        bbox = mapnik.Box2d(c0.x,c0.y, c1.x,c1.y)
    else:
        bbox = mapnik.Envelope(c0.x,c0.y, c1.x,c1.y)    
    m.resize(TS_I, TS_I)
    m.zoom_to_box(bbox)
    m.buffer_size = 256
    im = mapnik.Image(TS_I, TS_I)
    print im
    mapnik.render(m, im)
    #view = im.view(0, 0, TS_I, TS_I) # x,y,width,height
    #print map_path
    #view.save(map_path, 'png')
    print map_path
    im.save(map_path, 'png256')

###############################################################################
# MAIN
###############################################################################
if __name__ == "__main__":
    col = sys.argv[2]
    row = sys.argv[3]
    z = sys.argv[1]
    print "z=%s x=%s y=%s" % (z, col, row)
    generateTile(int(z), int(col), int(row))
