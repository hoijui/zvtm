#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys, os

################################################################################
# SETTINGS
################################################################################
# line separator char used in output CSV file (use \n for new line)
OUTPUT_LINE_SEPARATOR = "\n"
# output encoding
OUTPUT_ENCODING = "utf-8"

# usage cmd line msg
USAGE_MSG = "No output file provided\nUsage:\n\tconcat.py <output_file.ext>"

# ofn = output file name (String)
def process(ofn):
	outputFile = open(ofn, 'w')
	firstLine = 0
	for f in os.listdir("."):
		if f.find("-trials.csv") != -1:
		    inputFile = open(f, 'r')
		    print "Procesing %s" % (f,)
		    for l in inputFile.readlines()[firstLine:]:
				outputFile.write(l)
		    inputFile.close()
		firstLine = 1
	outputFile.flush()
	outputFile.close() 

################################################################################
# MAIN
################################################################################
if len(sys.argv) == 2:
   outputFileName = sys.argv[1]
   process(outputFileName)
else:
   print USAGE_MSG

