# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2010. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

# Usage example: GET http://data.wild.lri.fr/py/zuistServer/getTile?z=9&col=322&row=224

# If you get an error such as:
#
#    ImportError: libmapnik.so.0.6: cannot open shared object file: No such file or directory
#
# when executing this script, check http://trac.mapnik.org/wiki/InstallationTroubleshooting#Thelibmapniksharedlibraryisnotfound

# in /etc/apache2/envvars, add 
#   export MAPNIK_MAP_FILE=/home/osm/mapnik/osm.xml
#
# and maybe this, too:
#   export MAPNIK_WORLD_BOUNDARIES_DIR=/home/osm/mapnik/world_boundaries

from mod_python import apache
#http://www.modpython.org/live/current/doc-html/

from mapnik import *

import os, math

TILE_DIR = "/var/www/tiles"
CACHE_DIR = "%s/cache" % TILE_DIR

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
# MAIN
###############################################################################
def getTile(req, z=-1, col=-1, row=-1):
    try:
        req.log_error('handler')
        req.content_type = 'image/png'
        req.send_http_header()
        if int(z) < 10:
            # tiles for levels up to z=9 have been pre-rendered and stored
            req.sendfile("%s/%s/%s/%s.png" % (TILE_DIR, z, col, row))
        elif os.path.exists("%s/%s/%s/%s.png" % (CACHE_DIR, z, col, row)):
            req.sendfile("%s/%s/%s/%s.png" % (CACHE_DIR, z, col, row))            
        else:
            generateTile(req, int(z), int(col), int(row))
        return apache.OK
    except:
        return apache.HTTP_NOT_FOUND

###############################################################################
# Tile generation
###############################################################################
def generateTile(req, z=-1, col=-1, row=-1):
    map_path = "%s/%s/%s/%s.png" % (CACHE_DIR, z, col, row)
    if not os.path.exists("%s/%s/%s" % (CACHE_DIR, z, col)):
        if not os.path.exists("%s/%s" % (CACHE_DIR, z)):
            os.mkdir("%s/%s" % (CACHE_DIR, z))
        os.mkdir("%s/%s/%s" % (CACHE_DIR, z, col))
    m = Map(TS_I, TS_I)
    load_map(m, os.environ['MAPNIK_MAP_FILE'])
    prj = Projection(m.srs)
    gprj = GoogleProjection(MAX_ZOOM+1)
    # Calculate pixel positions of bottom-left & top-right
    p0 = (col * TS_I, (row + 1) * TS_I)
    p1 = ((col + 1) * TS_I, row * TS_I)
    # Convert to LatLong (EPSG:4326)
    l0 = gprj.fromPixelToLL(p0, z);
    l1 = gprj.fromPixelToLL(p1, z);
    # Convert to map projection (e.g. mercator co-ords EPSG:900913)
    c0 = prj.forward(Coord(l0[0],l0[1]))
    c1 = prj.forward(Coord(l1[0],l1[1]))
    # Bounding box for the tile
    bbox = Envelope(c0.x, c0.y, c1.x, c1.y)
    m.resize(TS_I, TS_I)
    m.zoom_to_box(bbox)
    m.buffer_size = 256
    im = Image(TS_I, TS_I)
    render(m, im)
    
    view = im.view(0, 0, TS_I, TS_I) # x,y,width,height
    view.save(map_path, 'png')
    #im.save(map_uri, 'png256')
    
    req.content_type = 'image/png'
    req.send_http_header()
    req.sendfile(map_path)
