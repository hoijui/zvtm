#!/usr/bin/python
# -*- coding: UTF-8 -*-

# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2010. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

import math
from subprocess import call
import sys, os
from Queue import Queue
import mapnik
import threading
import shutil
import hashlib

CMD_LINE_HELP = "OSM ZUIST Scene Generator for WILD\n\nUsage:\n\n" + \
    " \tosm2zuist [options]\n\n" + \
    "Options:\n\n"+\
    "\t-tl=N\t\ttrace level (N in [0:2])\n"+\
    "\t-log\t\tlog log exec to osm2zuist.log\n"+\
    "\t-min=N\t\tmin zoom level (N in [0:maxN])\n"+\
    "\t-max=N\t\tmax zoom level (N in [minN:18])\n"
    
DEG_TO_RAD = math.pi/180
RAD_TO_DEG = 180/math.pi

# Default number of rendering threads to spawn, should be roughly equal to number of CPU cores available
NUM_THREADS = 4

TS_F = 512.0
TS_I = 512

# camera focal distance
F = 100.0
# camera max altitude
MAX_ALT = "100000000"

# OSM zoom levels to be generated [0..18]
MIN_ZOOM = 0
MAX_ZOOM = 6
HIGHEST_ZOOM = 18

EMPTY_SEA_TILES_512 = ["5429c11f64f842fa1ef2bdd78c0e91ae",]
EMPTY_LAND_TILES_512 = ["19321692408961898d45d97d70be7313", "053d9da21fca556bc30cf5f375a892ea"]

LOG_FILE_NAME = "osm2zuist.log"
LOG_FILE = None
TRACE_LEVEL = 1

INTERPOLATION = "bilinear"

FRAG_DEPTH = 6

et = {}

################################################################################
# Utilities
################################################################################
def sumfile(fobj):
    m = hashlib.md5()
    while True:
        d = fobj.read(8096)
        if not d:
            break
        m.update(d)
    return m.hexdigest()

def minmax (a,b,c):
    a = max(a,b)
    a = min(a,c)
    return a
    
def hasEmptyAncestor(z, x, y, et_set):
    if z <= 1:
        return False
    else:
        pID = "%s-%s-%s" % (z-1, x/2, y/2)
        if et_set.has_key(pID):
            return True
        else:
            return hasEmptyAncestor(z-1, x/2, y/2, et_set)


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


################################################################################
# Render Thread
################################################################################
class RenderThread:
    def __init__(self, tile_dir, mapfile, q, printLock, xmlLock, maxZoom, zf):
        self.tile_dir = tile_dir
        self.sea_path = "%ssea.png" % tile_dir
        self.lnd_path = "%slnd.png" % tile_dir
        self.q = q
        self.m = mapnik.Map(TS_I, TS_I)
        self.printLock = printLock
        self.xmlLock = xmlLock
        self.zf = zf
        # Load style XML
        mapnik.load_map(self.m, mapfile, True)
        # Obtain <Map> projection
        self.prj = mapnik.Projection(self.m.srs)
        # Projects between tile pixel co-ordinates and LatLong (EPSG:4326)
        self.tileproj = GoogleProjection(maxZoom+1)

    def render_tile(self, tile_uri, x, y, z):
        # Calculate pixel positions of bottom-left & top-right
        p0 = (x * TS_I, (y + 1) * TS_I)
        p1 = ((x + 1) * TS_I, y * TS_I)

        # Convert to LatLong (EPSG:4326)
        l0 = self.tileproj.fromPixelToLL(p0, z);
        l1 = self.tileproj.fromPixelToLL(p1, z);

        # Convert to map projection (e.g. mercator co-ords EPSG:900913)
        c0 = self.prj.forward(mapnik.Coord(l0[0],l0[1]))
        c1 = self.prj.forward(mapnik.Coord(l1[0],l1[1]))

        # Bounding box for the tile
        if hasattr(mapnik,'mapnik_version') and mapnik.mapnik_version() >= 800:
            bbox = mapnik.Box2d(c0.x,c0.y, c1.x,c1.y)
        else:
            bbox = mapnik.Envelope(c0.x,c0.y, c1.x,c1.y)
        render_size = TS_I
        self.m.resize(render_size, render_size)
        self.m.zoom_to_box(bbox)
        self.m.buffer_size = 256 #128

        # Render image with default Agg renderer
        im = mapnik.Image(render_size, render_size)
        mapnik.render(self.m, im)
        im.save(tile_uri, 'png256')

    def loop(self):
        global et
        while True:
            #Fetch a tile from the queue and render it
            r = self.q.get()
            if (r == None):
                self.q.task_done()
                break
            else:
                (name, tile_uri, x, y, z) = r

            ID = "%s-%s-%s" % (z, x, y)
            parentID = "%s-%s-%s" % (z-1, x/2, y/2)
            if hasEmptyAncestor(z, x, y, et):
                self.printLock.acquire()
                log("Descendant of an empty land/sea tile, ignoring %s" % ID, 1)
                self.printLock.release()
            else:
                if os.path.exists(tile_uri):
                    self.printLock.acquire()
                    log("%s exists" % ID, 1)
                    self.printLock.release()
                else:
                    self.printLock.acquire()
                    log("Rendering %s" % ID, 1)
                    self.printLock.release()
                    self.render_tile(tile_uri, x, y, z)
                # compute coords and size of this tile
                ilc = HIGHEST_ZOOM - MIN_ZOOM + 1 - z
                ts = TS_I * math.pow(2, ilc-1)
                vx = (x*ts + ts/2.0)
                vy = -(y*ts + ts/2.0)
                w = h = ts
                # z-index
                if z == 0:
                    zi = 0
                else:
                    zi = 1                
                bytes = os.stat(tile_uri)[6]
                if bytes == 126:
                    # empty tile
                    md5sum = sumfile(open(tile_uri, 'rb'))
                    # sea tile
                    if md5sum in EMPTY_SEA_TILES_512:
                        self.printLock.acquire()
                        log("Instantiating top level sea tile %s" % ID, 2)
                        self.printLock.release()
                        if not os.path.exists(self.sea_path):
                            self.printLock.acquire()
                            log("Creating generic sea tile at %s" % self.sea_path, 2)
                            self.printLock.release()
                            shutil.move(tile_uri, self.sea_path)
                        else:
                            os.remove(tile_uri)
                        levels = "%s;%s" % (z, HIGHEST_ZOOM)
                        self.xmlLock.acquire()
                        self.zf.write("  <region id=\"R%s\" containedIn=\"R%s\" levels=\"%s\" x=\"%d\" y=\"%d\" w=\"%d\" h=\"%d\">\n" % (ID, parentID, levels, vx, vy, w, h))
                        self.zf.write("    <resource type=\"img\" id=\"T%s\" src=\"sea.png\" params=\"im=nearestNeighbor\" x=\"%d\" y=\"%d\" w=\"%d\" h=\"%d\" z-index=\"%d\"/>\n" % (ID, vx, vy, w, h, zi))
                        self.zf.write("  </region>\n")
                        self.xmlLock.release()
                        et[ID] = None
                    # land tile
                    elif md5sum in EMPTY_LAND_TILES_512:
                        self.printLock.acquire()
                        log("Instantiating top level land tile %s" % ID, 2)
                        self.printLock.release()
                        if not os.path.exists(self.lnd_path):
                            self.printLock.acquire()
                            log("Creating generic land tile at %s" % self.lnd_path, 2)
                            self.printLock.release()
                            shutil.move(tile_uri, self.lnd_path)
                        else:
                            os.remove(tile_uri)
                        levels = "%s;%s" % (z, HIGHEST_ZOOM)
                        self.xmlLock.acquire()
                        self.zf.write("  <region id=\"R%s\" containedIn=\"R%s\" levels=\"%s\" x=\"%d\" y=\"%d\" w=\"%d\" h=\"%d\">\n" % (ID, parentID, levels, vx, vy, w, h))
                        self.zf.write("    <resource type=\"img\" id=\"T%s\" src=\"lnd.png\" params=\"im=nearestNeighbor\" x=\"%d\" y=\"%d\" w=\"%d\" h=\"%d\" z-index=\"%d\"/>\n" % (ID, vx, vy, w, h, zi))
                        self.zf.write("  </region>\n")
                        self.xmlLock.release()
                        et[ID] = None
                    # unknown...
                    else:
                        self.printLock.acquire()
                        log("WARNING: Unkown empty tile %s" % ID)
                        self.printLock.release()
                else:
                    # non-empty tile
                    src = tile_uri[len(tile_dir):]
                    if z == 0:
                        levels = "0;%s" % HIGHEST_ZOOM
                        ci = ""
                    else :
                        levels = z
                        ci = "containedIn=\"R%s\"" % parentID
                    self.xmlLock.acquire()
                    self.zf.write("  <region id=\"R%s\" %s levels=\"%s\" x=\"%d\" y=\"%d\" w=\"%d\" h=\"%d\">\n" % (ID, ci, levels, vx, vy, w, h))
                    self.zf.write("    <resource type=\"img\" id=\"T%s\" src=\"%s\" params=\"im=%s\" x=\"%d\" y=\"%d\" w=\"%d\" h=\"%d\" z-index=\"%d\"/>\n" % (ID, src, INTERPOLATION, vx, vy, w, h, zi))
                    if z == MAX_ZOOM:
                        frag_src = "%ssf%s.xml" % (src[:src.rfind("/")+1], src[src.rfind("/")+1:src.rfind(".")])
                        self.zf.write("    <resource type=\"scn\" id=\"F%s\" src=\"%s\" x=\"%d\" y=\"%d\" />\n" % (ID, frag_src, vx, vy))
                    self.zf.write("  </region>\n")
                    self.xmlLock.release()
                    if z == MAX_ZOOM:
                        # frag_src will have been initialized in the above same test
                        generateSceneFragment("%s%s" % (tile_dir, frag_src), x, y, MAX_ZOOM+1)
            self.zf.flush()
            self.q.task_done()

################################################################################
# Generate ZUIST levels
################################################################################
def generateLevels(zf):
    levelCount = HIGHEST_ZOOM - MIN_ZOOM + 1
    altitudes = [0,]
    for i in range(levelCount):
        depth = int(levelCount-i-1)
        altitudes.append(int(F*math.pow(2,i+1)-F))
        if i == levelCount-1:
            ceiling = MAX_ALT
        else:
            ceiling = altitudes[-1]
        floor = altitudes[-2]
        zf.write("  <level depth=\"%s\" ceiling=\"%s\" floor=\"%s\"/>\n" % (str(depth), str(ceiling), str(floor)))
    

def generateSceneFragment(src, x, y, z):
    log("Generating scene fragment %s" % src, 2)
    ff = open(src, 'w')
    ff.write("<?xml version=\"1.0\"?>\n")
    ff.write("<scene background=\"white\">\n")
    generateLevels(ff)
    ff.write("</scene>\n")
    ff.flush()
    ff.close()
    
################################################################################
#
################################################################################
def render_tiles(bbox, mapfile, tile_dir, minZoom=1,maxZoom=18, name="unknown", num_threads=NUM_THREADS):
    log("Rendering levels %s to %s ..." % (MIN_ZOOM, MAX_ZOOM))
    # Launch rendering threads
    queue = Queue(32)
    printLock = threading.Lock()
    xmlLock = threading.Lock()
    renderers = {}
    if not os.path.isdir(tile_dir):
         os.mkdir(tile_dir)
    # ZUIST XML scene file
    zf = open("%sscene.xml" % tile_dir, 'w')
    for i in range(num_threads):
        renderer = RenderThread(tile_dir, mapfile, queue, printLock, xmlLock, maxZoom, zf)
        render_thread = threading.Thread(target=renderer.loop)
        render_thread.start()
        #print "Started render thread %s" % render_thread.getName()
        renderers[i] = render_thread


    gprj = GoogleProjection(maxZoom+1) 

    ll0 = (bbox[0],bbox[3])
    ll1 = (bbox[2],bbox[1])
    
    zf.write("<?xml version=\"1.0\"?>\n")
    zf.write("<scene background=\"white\">\n")
    generateLevels(zf)

    for z in range(minZoom,maxZoom + 1):
        px0 = gprj.fromLLtoPixel(ll0,z)
        px1 = gprj.fromLLtoPixel(ll1,z)

        # check if we have directories in place
        zoom = "%s" % z
        if not os.path.isdir(tile_dir + zoom):
            os.mkdir(tile_dir + zoom)
        for x in range(int(px0[0]/TS_F),int(px1[0]/TS_F)+1):
            # Validate x co-ordinate
            if (x < 0) or (x >= 2**z):
                continue
            # check if we have directories in place
            str_x = "%s" % x
            if not os.path.isdir(tile_dir + zoom + '/' + str_x):
                os.mkdir(tile_dir + zoom + '/' + str_x)
            for y in range(int(px0[1]/TS_F),int(px1[1]/TS_F)+1):
                # Validate x co-ordinate
                if (y < 0) or (y >= 2**z):
                    continue
                str_y = "%s" % y
                tile_uri = tile_dir + zoom + '/' + str_x + '/' + str_y + '.png'
                # Submit tile to be rendered into the queue
                t = (name, tile_uri, x, y, z)
                queue.put(t)

    # Signal render threads to exit by sending empty request to queue
    for i in range(num_threads):
        queue.put(None)
    # wait for pending rendering jobs to complete
    queue.join()
    for i in range(num_threads):
        renderers[i].join()
    
    zf.write("</scene>\n")
    zf.flush()
    zf.close()
    if LOG_FILE is not None:
        LOG_FILE.flush()
        LOG_FILE.close()    

################################################################################
# Trace exec on std output and log file
################################################################################
def log(msg, level=0):
    if level <= TRACE_LEVEL:
        print msg
        if LOG_FILE is not None:
            LOG_FILE.write(msg+"\n")
            LOG_FILE.flush()
        
def initLogFile():
    global LOG_FILE
    LOG_FILE = open(LOG_FILE_NAME, 'w')
        
################################################################################
#
################################################################################
if __name__ == "__main__":
    # cmd line args
    if len(sys.argv) > 1:
        for arg in sys.argv[1:]:
            if arg.startswith("-tl="):
                TRACE_LEVEL = int(arg[4:])
            elif arg.startswith("-min="):
                MIN_ZOOM = int(arg[5:])
            elif arg.startswith("-max="):
                MAX_ZOOM = int(arg[5:])
            elif arg.startswith("-log"):
                initLogFile()
            elif arg.startswith("-h"):
                log(CMD_LINE_HELP)
                sys.exit(0)
    
    home = os.environ['HOME']
    try:
        mapfile = os.environ['MAPNIK_MAP_FILE']
    except KeyError:
        mapfile = home + "/mapnik/zuist_osm.xml"
    try:
        tile_dir = os.environ['MAPNIK_TILE_DIR']
    except KeyError:
        tile_dir = home + "/zuist/tiles/"
    
    bbox = (-180.0,-90.0, 180.0,90.0)
    render_tiles(bbox, mapfile, tile_dir, MIN_ZOOM, MAX_ZOOM, "World")
