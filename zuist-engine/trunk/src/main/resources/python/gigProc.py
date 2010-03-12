#!/usr/bin/python
# -*- coding: UTF-8 -*-

import os

for i in range(9):
    for j in range(22):
        cmd = "/Users/epietrig/projects/zuist-engine/src/main/resources/python/imageTiler.py /Volumes/EP2To/glacier/r%04d_c%04d.tiff /Volumes/EP2To/glacier/zuist/r%04dc%04d -im -ts=500 -tl=3 -idprefix=r%04dc%04d" % (i,j,i,j,i,j)
        print cmd
        os.system(cmd)
