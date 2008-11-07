#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys, os
import random

################################################################################
# SETTINGS
################################################################################
# separator char used in input CSV file (use \t for tab)
INPUT_CSV_SEPARATOR = ";"
# value separator char used in output CSV file (use \t for tab)
OUTPUT_CSV_SEPARATOR = ";"
# line separator char used in output CSV file (use \n for new line)
OUTPUT_LINE_SEPARATOR = "\n"
# suffix inserted in input file name to generate output file name when none is
# provided
DEFAULT_OUTPUT_SUFFIX = "-out"
# input encoding
INPUT_ENCODING = "utf-8"
# output encoding
OUTPUT_ENCODING = "utf-8"

################################################################################
# INTERNAL USE
################################################################################
# usage cmd line msg
USAGE_MSG = "No input file provided\nUsage:\n\tlatinsquare.py <input_file.ext> [<output_file.ext>]"
# error message at checking time
ERR_IN_RES = "The generated latin square is incorrect, probably because you messed up with the original program !"

################################################################################
# FUNCTIONS
################################################################################

# main method
# ifn = input file name (String)
# ofn = output file name (String)
def process(ifn, ofn):
   inputSequence = parse(ifn)
   standardSquare = generateStandardSquare(inputSequence)
   latinSquare = generateLatinSquare(standardSquare)
   check(latinSquare)
   serialize(latinSquare, ofn)


# parse input sequence
# ifn = input file name (String)
def parse(ifn):
   inputFile = open(ifn, 'r')
   res = inputFile.readline()[:-1].decode(INPUT_ENCODING).split(INPUT_CSV_SEPARATOR)
   inputFile.close()
   return res


# builds the standard square from the input sequence
# isq = input sequence (String[])
# returns a (String[][])
def generateStandardSquare(isq):
   res = []
   offset = len(isq)
   for i in range(len(isq)):
       row = isq[offset:]
       row.extend(isq[:offset])
       offset -= 1
       res += [row]
   return res


# column reordering
# sq = a square (String[][])
def reorderColumns(sq):
   res = []
   r_seq = range(len(sq))
   random.shuffle(r_seq)
   for i in range(len(sq)):
       row = []
       for j in r_seq:
           row += [sq[i][j]]
       res += [row]
   return res


# row reordering
# sq = a square (String[][])
def reorderRows(sq):
   random.shuffle(sq)
   return sq


# transforms the standard square in a latin square
# ss = start square (String[][])
# returns a (String[][])
def generateLatinSquare(ss):
   res = ss
   for i in range(2):
       res = reorderRows(reorderColumns(res))
   return res


# checks that the latin square is well-formed
# (just in case somebody tampers with the code)
# ls = latin square (String[][])
def check(ls):
   for i in range(len(ls)):
       for j in range(len(ls)):
           # check that each element only appears one in each row
           for remainingElemInRow in ls[i][j+1:]:
               if remainingElemInRow == ls[i][j]:
                   print ERR_IN_RES
           for k in range(i+1, len(ls)):
               if ls[k][j] == ls[i][j]:
                   print ERR_IN_RES


# serializes the final latin square data structure
# ls = latin square (String[][])
# ofn = output file name (String)
def serialize(ls, ofn):
   outputFile = open(ofn, 'w')
   for line in ls:
       eline = "%s%s" % (OUTPUT_CSV_SEPARATOR.join(line), OUTPUT_LINE_SEPARATOR)
       outputFile.write(eline.encode(OUTPUT_ENCODING))
       #print line
   outputFile.flush()
   outputFile.close() 


################################################################################
# MAIN
################################################################################
if len(sys.argv) == 3:
   inputFileName = sys.argv[1]
   outputFileName = sys.argv[2]
   process(inputFileName, outputFileName)
elif len(sys.argv) == 2:
   inputFileName = sys.argv[1]
   outputFileName = inputFileName[:-4] + DEFAULT_OUTPUT_SUFFIX + inputFileName[-4:]
   process(inputFileName, outputFileName)
else:
   print USAGE_MSG

