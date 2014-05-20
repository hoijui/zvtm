#!/usr/bin/env python
#creates a tile pyramid
#images are assumed to be rectangular, and image dimensions
#should be powers of two. Image dimensions should be greater or
#equal than tile_size.
#this script depends on fitscopy (http://heasarc.nasa.gov/docs/software/fitsio/cexamples.html)

import os.path
import subprocess
import sys
from optparse import OptionParser

DEFAULT_LEVEL_COUNT=3
DEFAULT_TILE_SIZE=1024 #use power of two

parser = OptionParser()
parser.add_option("-d", "--dry-run", action="store_true", dest="dry_run", default=False, help="dry run (generate the scene xml description, but do not tile image)")
parser.add_option("-b", "--draw-border", action="store_true", dest="draw_border", default=False, help="draw red borders around regions (for debug purposes)")
options = None
args = None

def print_zuist_level_info(levels, level, cam_focal=100, max_ceiling=100000):
    """
    Prints the <level> sections
    levels: total number of levels (at least 1)
    level: current level, 0-based
    """
    ceiling = cam_focal * (2**(levels - level) - 1)
    floor = cam_focal * (2**(levels - level - 1) - 1)
    if(level == 0):
        ceiling = max_ceiling

    print "<level ceiling=\"%d\" depth=\"%d\" floor=\"%d\"/>" % (ceiling,level,floor)

def tile_image(srcpath, image_width, image_height, tile_width, tile_height, levels):
    """
    Creates a pyramid from an image
    levels: number of levels in the pyramid (1 for a flat pyramid)
    """
    print "<?xml version=\"1.0\"?>"
    print "<scene>"
    for i in reversed(range(levels)):
        print_zuist_level_info(levels, i)

    for i in reversed(range(levels)):
        print "<!-- starting level %d -->" % i
        tile_level(srcpath, image_width, image_height, tile_width, tile_height, levels, i)
    print "</scene>"

def print_zuist_resource_info(source_name, i, j, tile_width, tile_height, levels, level):
    """
    Prints the relevant <region> and <resource> sections.
    i: tile index in the x-axis
    j: tile index in the y-axis
    """
    skip_factor = 2**(levels - level - 1)
    stroke = options.draw_border and "stroke=red" or ""
    id="tile_level%d_%d_%d" % (level, i, j)
    print "<region %s h=\"%d\" w=\"%d\" id=\"%s\" levels=\"%d\" x=\"%d\" y=\"%d\">" % (stroke, tile_height * skip_factor, tile_width * skip_factor, id, level, (i + 0.5) * tile_width * skip_factor, (-j - 0.5) * tile_height * skip_factor)
    print "<resource h=\"%d\" w=\"%d\" id=\"%s\" src=\"%s\" type=\"fits\" x=\"%d\" y=\"%d\" z-index=\"0\" params=\"sc=%d;sm=ASINH;cf=HEAT\"/>" % (tile_height, tile_width, id, source_name, (i+0.5) * tile_width * skip_factor, (-j - 0.5) * tile_height * skip_factor, skip_factor)
    print "</region>"

def tile_level(srcpath, image_width, image_height, tile_width, tile_height, levels, level):
   """
   skip_factor should be a power of two (sf = 2^level), level 0
   has the resolution of the original image, level 1 has one fourth etc.
   called internally by tile_image. not for public consumption.
   """
   skip_factor = 2**(levels - level - 1)
   xcount = image_width / (tile_width * skip_factor) 
   ycount = image_height / (tile_height * skip_factor)
   
   for i in range(xcount):
       for j in range(ycount):
           tile_name = "%s_level%d_%d_%d.fits" % (os.path.splitext(os.path.basename(srcpath))[0], level, i, j)
           command = "fitscopy %s[%d:%d:%d,%d:%d:%d] tiles/%s" % (srcpath, i * tile_width * skip_factor + 1, (i + 1) * tile_width * skip_factor, skip_factor, j * tile_height * skip_factor + 1, (j + 1) * tile_height * skip_factor, skip_factor, tile_name) 
           #print command 
           if not options.dry_run:
                subprocess.Popen(command, shell=True)
           print_zuist_resource_info(tile_name, i, j, tile_width, tile_height, levels, level)

if __name__ == "__main__":
    options, args = parser.parse_args()
    tile_image(args[0], 8192, 8192, 1024, 1024, 3)

