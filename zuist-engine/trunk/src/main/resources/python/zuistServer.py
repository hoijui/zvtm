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

import os

TILE_DIR = "/var/www/tiles"

###############################################################################
# MAIN
###############################################################################
def getTile(req, z=-1, col=-1, row=-1):
    try:
        req.log_error('handler')
        if int(z) < 10:
            # tiles for levels up to z=9 have been pre-rendered and stored
            req.content_type = 'image/png'
            req.send_http_header()
            req.sendfile("%s/%s/%s/%s.png" % (TILE_DIR, z,col,row))
        else:
            generateTile(req, z, col, row)
        return apache.OK
    except:
        return apache.HTTP_NOT_FOUND

def generateTile(req, z=-1, col=-1, row=-1):
    ll = (-6.5, 49.5, -6.4, 51)
    z = 1
    imgx = 512 * z
    imgy = 512 * z
    m = Map(imgx,imgy)
    load_map(m, os.environ['MAPNIK_MAP_FILE'])
    prj = Projection("+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +no_defs +over")
    c0 = prj.forward(Coord(ll[0],ll[1]))
    c1 = prj.forward(Coord(ll[2],ll[3]))
    bbox = Envelope(c0.x,c0.y,c1.x,c1.y)
    m.zoom_to_box(bbox)
    im = Image(imgx,imgy)
    render(m, im)
    view = im.view(0,0,imgx,imgy) # x,y,width,height
    map_path = "%s/cache/%s" % (TILE_DIR, "image.png")
    view.save(map_path, 'png')
    req.content_type = 'image/png'
    req.send_http_header()
    req.sendfile(map_path)
