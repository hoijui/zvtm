#!/usr/bin/env python
#creates a tile pyramid
#images are assumed to be rectangular, and image dimensions
#should be powers of two. Image dimensions should be greater or
#equal than tile_size.

import os.path
import subprocess

DEFAULT_LEVEL_COUNT=3
DEFAULT_TILE_SIZE=1024 #use power of two

#TODO complete
#TODO add dry-run option (only generate xml info, do not tile images)

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

def tile_image(filename, image_width, image_height, tile_width, tile_height, levels):
    """
    Creates a pyramid from an image
    levels: number of levels in the pyramid (1 for a flat pyramid)
    """
    for i in reversed(range(levels)):
        print_zuist_level_info(levels, i)

    for i in reversed(range(levels)):
        print "<!-- starting level %d -->" % i
        tile_level(filename, image_width, image_height, tile_width, tile_height, i)

def print_zuist_resource_info(source_name, i, j, tile_width, tile_height, level):
    """
    Prints the relevant <region> and <resource> sections.
    i: tile index in the x-axis
    j: tile index in the y-axis
    """
    id="tile_level%d_%d_%d" % (level, i, j)
    print "<region h=\"%d\" w=\"%d\" id=\"%s\" layer=\"%s\" levels=\"%d\" x=\"%d\" y=\"%d\">" % (tile_height, tile_width, id, "imageLayer", level, (i - 0.5) * tile_width, -(j - 0.5) * tile_height)
    print "<resource h=\"%d\" w=\"%d\" id=\"%s\" src=\"%s\" type=\"fits\" x=\"%d\" y=\"%d\" z-index=\"0\"/>" % (tile_height, tile_width, id, source_name, i * tile_width, -j * tile_height)
    print "</region>"

def tile_level(filename, image_width, image_height, tile_width, tile_height, level):
   """
   skip_factor should be a power of two (sf = 2^level), level 0
   has the resolution of the original image, level 1 has one fourth etc.
   called internally by tile_image. not for public consumption.
   """
   skip_factor = 2**level
   xcount = image_width / (tile_width * skip_factor) 
   ycount = image_height / (tile_height * skip_factor)
   
   for i in range(xcount):
       for j in range(ycount):
           tile_name = ""
           print "fitscopy %s[%d:%d:%d,%d:%d:%d]" % (filename, i * tile_width * skip_factor + 1, (i + 1) * tile_width * skip_factor, skip_factor, j * tile_height * skip_factor + 1, (j + 1) * tile_height * skip_factor, skip_factor)
           #subprocess.Popen("fitscopy %s[%d:%d:%d,%d:%d:%d]" % (filename, i * tile_width * skip_factor + 1, (i + 1) * tile_width * skip_factor, skip_factor, j * tile_height * skip_factor + 1, (j + 1) * tile_height * skip_factor, skip_factor))
           print_zuist_resource_info(os.path.basename(filename), i, j, tile_width, tile_height, level)

if __name__ == "__main__":
   tile_image("foo", 8192, 8192, 1024, 1024, 3)

