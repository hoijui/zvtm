#!/usr/bin/python
# -*- coding: UTF-8 -*-

# $Id$

import os, sys
import elementtree.ElementTree as ET

# value separator char used in output CSV file (use \t for tab)
OUTPUT_CSV_SEPARATOR = ";"
# line separator char used in output CSV file (use \n for new line)
OUTPUT_LINE_SEPARATOR = "\n"
# output encoding
OUTPUT_ENCODING = "utf-8"

def buildCSV(SRC_DIR, OUTPUT_FILE):
	lines = []
	for f in os.listdir(SRC_DIR):
		if f.startswith("airports_") and f.endswith(".xml"):
			xml = ET.parse(open(f, 'r'))
			log("Processing %s" % f)
			processXML(xml, lines)
	serialize(OUTPUT_FILE, lines)

def processXML(xmlDocument, lines):
	xmlRoot = xmlDocument.getroot()
	for geonameEL in xmlRoot.findall(".//geoname"):
		anELs = geonameEL.findall("./alternateName")
		for anEL in anELs:
			name = geonameEL.findtext("name")
			if anEL.attrib["lang"] == "iata":
				# only output airports with an IATA code
				lat = geonameEL.findtext("lat")
				lng = geonameEL.findtext("lng")
				iata = anEL.text
				lines += [[iata,name,lat,lng]]
			else:
				log("Warning: no IATA code for %s" % name.encode("utf-8"), 2)

def serialize(ofn, lines):
	outputFile = open(ofn, 'w')
	for line in lines:
		outputFile.write(("%s%s" % (OUTPUT_CSV_SEPARATOR.join(line), OUTPUT_LINE_SEPARATOR)).encode(OUTPUT_ENCODING))
	outputFile.flush()
	outputFile.close()
	log("Saved %s airports in %s" % (len(lines), ofn))

################################################################################
# Trace exec on std output
################################################################################
def log(msg, level=0):
    if level <= TRACE_LEVEL:
        print msg

################################################################################
# main
################################################################################
if len(sys.argv) > 1:
    SRC_DIR = os.path.realpath(sys.argv[1])
    if len(sys.argv) > 2:
        OUTPUT_FILE = os.path.realpath(sys.argv[2])
    	if len(sys.argv) > 3:
        	TRACE_LEVEL = int(sys.argv[3])
else:
    sys.exit(0)

buildCSV(SRC_DIR, OUTPUT_FILE)
