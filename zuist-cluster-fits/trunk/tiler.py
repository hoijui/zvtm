#!/usr/bin/env python
#creates a tile pyramid
#images are assumed to be rectangular, and image dimensions
#should be powers of two. Image dimensions should be greater or
#equal than tile_size.

import subprocess

DEFAULT_LEVEL_COUNT=3
DEFAULT_TILE_SIZE=1024 #use power of two

#TODO complete
#TODO add dry-run option (only generate xml info, do not tile images)

def print_zuist_level_info():
    """
    Prints the <level> sections
    """
    print "<level ceiling=\"%d\" depth=\"%d\" floor=\"%d\"/>" % (0,0,0)

def tile_image(filename, image_width, image_height, tile_width, tile_height, levels):
    """
    Creates a pyramid from an image
    """
    for i in range(levels):
        print_zuist_level_info()

    for i in range(levels):
        print "<!-- starting level %d -->" % i
        tile_level(filename, image_width, image_height, tile_width, tile_height, 2**i)

def print_zuist_resource_info(tile_width, tile_height):
    """
    Prints the relevant <region> and <resource> sections.
    """
    print "<region h=\"%d\" w=\"%d\" id=\"%s\" layer=\"%s\" levels=\"%d\" w=\"%d\" x=\"%d\" y=\"%d\">" % (tile_height, tile_width, "id", "layer", 0, 0, 0, 0)
    print "<resource h=\"%d\" w=\"%d\" id=\"%s\" src=\"%s\" type=\"fits\"  x=\"%d\" y=\"%d\" z-index=\"0\"/>" % (tile_height, tile_width, "id", "src", 0, 0)
    print "</region>"

def tile_level(filename, image_width, image_height, tile_width, tile_height, skip_factor):
   """
   skip_factor should be a power of two (sf = 2^level), level 0
   has the resolution of the original image, level 1 has one fourth etc.
   called internally by tile_image. not for public consumption.
   """
   xcount = image_width / (tile_width * skip_factor) 
   ycount = image_height / (tile_height * skip_factor)
   
   for i in range(xcount):
       for j in range(ycount):
           print "echo fitscopy %s[%d:%d:%d,%d:%d:%d]" % (filename, i * tile_width * skip_factor + 1, (i + 1) * tile_width * skip_factor, skip_factor, j * tile_height * skip_factor + 1, (j + 1) * tile_height * skip_factor, skip_factor)
           #subprocess.Popen("echo fitscopy %s[%d:%d:%d,%d:%d:%d]" % (filename, i * tile_width * skip_factor + 1, (i + 1) * tile_width * skip_factor, skip_factor, j * tile_height * skip_factor + 1, (j + 1) * tile_height * skip_factor, skip_factor))
           print_zuist_resource_info()

if __name__ == "__main__":
   tile_image("foo", 8192, 8192, 1024, 1024, 3)

