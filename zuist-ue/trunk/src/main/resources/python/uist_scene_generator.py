#!/usr/bin/python
# -*- coding: UTF-8 -*-
# $Id$

import os, sys
import math
import elementtree.ElementTree as ET
from PIL import Image

TRACE_LEVEL = 1

# paper page width and height, low resolution
PPW_L = 612
PPH_L = 792
# paper page width and height, high resolution
PPW_H = 1224
PPH_H = 1584

PAPER_REGION_WIDTH = 0
PAPER_REGION_HEIGHT = 0
FIGURE_REGION_WIDTH = 0
FIGURE_REGION_HEIGHT = 0
L1_REGION_WIDTH = 0
L1_REGION_HEIGHT = 0

LEVEL0_CEILING = "40000000"
LEVEL0_FLOOR = "8000000"
LEVEL1_CEILING = "8000000"
LEVEL1_FLOOR = "810000"
LEVEL1b_CEILING = "810000"
LEVEL1b_FLOOR = "45000"
FIG_LEVEL_CEILING = "45000"
FIG_LEVEL_FLOOR = "3500"
PAPER_LEVEL_CEILING = "3500"
PAPER_LEVEL_FLOOR = "0"

L0_LABEL_SCALE_FACTOR = "400000"
L1_LETTER_SCALE_FACTOR = "100000"
L1_YEAR_LABEL_SCALE_FACTOR = "80000"
L1_AUTHOR_LABEL_SCALE_FACTOR = "8000"
L1_COAUTHOR_ACTION_SCALE_FACTOR = "2000"
L2_TITLE_LABEL_SCALE_FACTOR = 150
L2_SUBTITLE_LABEL_SCALE_FACTOR = 120
# when the matrix has 7 columns (max), L2 scale factors are as above
# when it has 1 column (min), L2 scale factors are TSFA * x = TSFB, x being above values
# basically, rescaling varies between 1 and 3.5
TSFM = 3.5
TSFA = (1-TSFM) / 6.0
TSFB = (7*TSFM-1) / 6.0
L2_AUTHORS_LABEL_SCALE_FACTOR = 80
L1_ATOM_LABEL_SCALE_FACTOR = 3 * 13000
L1_ATOM_CHAR_WIDTH = int (8 * L1_ATOM_LABEL_SCALE_FACTOR)
# ratio is probably around 8 * L1_ATOM_LABEL_SCALE_FACTOR
L1_ATOM_CHAR_HEIGHT = int (2 * L1_ATOM_CHAR_WIDTH)
# consider that chars are approx. 200% their width

FADE_IN = "fadein"
FADE_OUT = "fadeout"
APPEAR = "appear"
DISAPPEAR = "disappear"

MAX_CHARS_PER_TITLE_LINE = 45
MAX_CHARS_PER_SUBTITLE_LINE = 65

MIN_KWA_OCC = 3

MAX_KWE_PER_ATOM_LINE = 6

# maximum number of characters for a paper's title
# (when appearing as a ZUIST region's title)
MAX_CHAR_IN_PAPER_REGION_TITLE = 50

################################################################################
# EXCEPTIONS
################################################################################
PROBLEMATIC_PAPER_IDs = {'1991#p155-vander_zanden': '1991#p155-zanden',\
    '1994#p157-vander_zanden': '1994#p157-vanderzanden',\
    '1996#p137-vander_zanden': '1996#p137-vanderzanden',\
#    '1999#p83-vander_zanden': '1999#p83-vanderzanden',\
#    '2001#p41-mcintyre': '2001#p41-macintyre',\
#    '2001#p153-beaudouin_lafon': '2001#p153-beaudouin-lafon',\
#    '2005#p241-laio': '2005#p241-liao',\
    '2006#p277-millan': '2006#p277-delr.millan'}

FIXED_PAPER_IDs = {'1991#p155-vander_zanden': '1991#p155-zanden',\
    '1994#p157-vander_zanden': '1994#p157-vanderzanden',\
    '1996#p137-vander_zanden': '1996#p137-vanderzanden',\
    '1999#p83-vander_zanden': '1999#p83-vanderzanden',\
    '2001#p41-mcintyre': '2001#p41-macintyre',\
    '2001#p153-beaudouin_lafon': '2001#p153-beaudouin-lafon',\
    '2005#p241-laio': '2005#p241-liao',\
    '2006#p277-millan': '2006#p277-delr.millan'}

XML_ID2FILE_ID = {'1991#p155-zanden': '1991#p155-vander_zanden',\
    '1994#p157-vanderzanden': '1994#p157-vander_zanden',\
    '1996#p137-vanderzanden': '1996#p137-vander_zanden',\
    '1999#p83-vanderzanden': '1999#p83-vander_zanden',\
    '2001#p41-macintyre': '2001#p41-mcintyre',\
    '2001#p153-beaudouin-lafon': '2001#p153-beaudouin_lafon',\
    '2005#p241-liao': '2005#p241-laio',\
    '2006#p277-delr.millan': '2006#p277-millan'}
    
FORCED_ATOM_WIDTHS = {}
    
################################################################################
# METADATA
################################################################################
# paper id -> <article/>
id2paper = {}
# year -> [<article/>]  (paper list should already be sorted by first page)
year2papers = {}
# author id -> (canonical name, [paper ids])
author2papers =  {}
# keyword id -> [(keyword, [paper ids])]
kw2papers = {}
# keyword atom -> ([keyword ids], total number of papers using this atom in keywords)
atom2kws = {}
# list of keyword atoms used at L1
SORTED_KW_ATOMS = []

################################################################################
# Walk the hierarchy of UIST proceedings and generate XML scene description
################################################################################
def buildScene(metadataFile, outputSceneFile):
    global PAPER_REGION_WIDTH
    global PAPER_REGION_HEIGHT
    global FIGURE_REGION_WIDTH
    global FIGURE_REGION_HEIGHT
    global L1_REGION_WIDTH
    global L1_REGION_HEIGHT
    global id2paper
    global year2papers
    global author2papers
    global kw2papers
    global atom2kws
    global SORTED_AUTHORS
    global SORTED_KW_ATOMS
    global FORCED_ATOM_WIDTHS
    xmlMetadata = ET.parse(open(metadataFile, 'r'))
    # walk the metadata tree and generate multi-scale scene in dedicated XML format
    metadataRoot = xmlMetadata.getroot()
    for proceedingsEL in metadataRoot.findall(".//proceedings"):
        year = proceedingsEL.get('year')
        articles = proceedingsEL.findall("./article")
        year2papers[year] = articles
        for articleEL in articles:
            id2paper[articleEL.get('id')] = articleEL
    PAPERS_WITHOUT_A_FIGURE = []
    PAPERS_WITHOUT_A_PDF = []
    # check for non-existing figures/papers
    for pid in id2paper.keys():
        if pid in XML_ID2FILE_ID:
            pid = XML_ID2FILE_ID[pid]
        sid = pid.split("#")
        year = sid[0]
        if not os.path.exists("%s/figures/%s/%s.png" % (SRC_DIR, year, sid[1])):
            PAPERS_WITHOUT_A_FIGURE.append(pid)
        if not os.path.exists("%s/papers/%s/%s" % (SRC_DIR, year, sid[1])):
            PAPERS_WITHOUT_A_PDF.append(pid)
    # get author data from metadata
    for authorEL in metadataRoot.findall("./allAuthors/author"):
        pids = []
        for articleEL in authorEL.findall("./article"):
            pids.append(articleEL.get("idref"))
        author2papers[authorEL.get('id')] = (authorEL.findtext("canonicalName"), pids)
    # get keyword data from metadata
    for kwEL in metadataRoot.findall("./allKeywords/keywords/keyword"):
        kw2papers[kwEL.get('id')] = (kwEL.text, kwEL.get('papers').split(","))
    for kwaEL in metadataRoot.findall("./allKeywords/keywordAtoms/keywordAtom"):
        tnbo = int(kwaEL.get('totalnbo'))
        if tnbo >= MIN_KWA_OCC:
            # only keep atoms used at least in MIN_KWA_OCC paper keyword expressions
            atom2kws[kwaEL.text] = (kwaEL.get('idrefs').split(","), tnbo)
    SORTED_KW_ATOMS = atom2kws.keys()
    SORTED_KW_ATOMS.sort(kwAtomSorter)
    FORCED_ATOM_WIDTHS["3D"] = int(2 * math.pow(atom2kws.get("3D")[1], 1/2.9) * L1_ATOM_CHAR_WIDTH * len("3D"))
    FORCED_ATOM_WIDTHS["Web"] = int(1.5 * math.pow(atom2kws.get("Web")[1], 1/2.9) * L1_ATOM_CHAR_WIDTH * len("Web"))
    FORCED_ATOM_WIDTHS["user"] = int(1.1 * math.pow(atom2kws.get("user")[1], 1/2.9) * L1_ATOM_CHAR_WIDTH * len("user"))
    # retrieve some global information from hierarchy to compute layout
    t = getMaxChildren(xmlMetadata)
    # maximum number of pages in a paper
    mpgCount = t[0]
    # maximum number of papers in a proceedings vol.
    mppCount = t[1]
    # total number of proceedings
    prCount = t[2]
    # maximum number of papers per author
    mpaCount = t[3]
    # maximum number of papers per keyword
    mpkwCount = t[4]
    ########################## PROCEEDINGS
    log("Maximum page count per paper: %s" % mpgCount, 0)
    log("Maximum paper count per proceedings: %s" % mppCount, 0)
    log("Number of proceedings (years): %s" % prCount, 0)
    # compute largest matrix of figures for proceedings
    nbColRow = matrixLayout(mppCount)
    nbFigColProc = nbColRow[0]
    nbFigRowProc = nbColRow[1]
    log("Largest matrix of figures (proceedings): %sx%s" % (nbFigColProc, nbFigRowProc), 0)
    ########################## AUTHORS
    authCount = len(author2papers.keys())
    log("Number of authors: %s" % authCount, 0)
    log("Maximum paper count per author: %s" % mpaCount, 0)
    # compute largest matrix of figures for authors
    nbColRow = matrixLayout(mpaCount)
    nbFigColAu = nbColRow[0]
    nbFigRowAu = nbColRow[1]
    log("Largest matrix of figures (authors): %sx%s" % (nbFigColAu, nbFigRowAu), 0)
    ########################## KEYWORDS
    kwCount = len(SORTED_KW_ATOMS)
    log("Number of keywords: %s" % kwCount, 0)
    log("Maximum paper count per keyword: %s" % mpkwCount, 0)
    # compute largest matrix of figures for keywords
    nbColRow = matrixLayout(mpkwCount)
    nbFigColKw = nbColRow[0]
    nbFigRowKw = nbColRow[1]
    log("Largest matrix of figures (keywords): %sx%s" % (nbFigColKw, nbFigRowKw), 0)
    # maximum number of papers per higher level of abstraction, whatever that is
    mpCount = max(mppCount, mpaCount, mpkwCount)
    # compute dimensions of a region containing the pages of a paper
    # leave an empty space between pages that is .1 a page in size
    # same before first page and after last page (hence the +.1)
    PAPER_REGION_WIDTH = int(mpgCount * PPW_H * 1.1)
    PAPER_REGION_HEIGHT = int(PPH_H * 1.1)
    # compute dimensions of a region containing the figures associated with
    # an higher-level entity (proceedings, author, keyword)
    # base computation on largest matrix for all higher-level entities
    nbFigColMax = max(nbFigColProc, nbFigColAu, nbFigColKw)
    nbFigRowMax = max(nbFigRowProc, nbFigRowAu, nbFigRowKw)
    # leave an empty space between figures that is equal to a figure in size
    FIGURE_REGION_WIDTH = int(PAPER_REGION_WIDTH * (2 * nbFigColMax))
	# consider width instead of height as we lay out objects in a square matrix
    FIGURE_REGION_HEIGHT = int(PAPER_REGION_WIDTH * (2 * nbFigRowMax))
    # compute L1 from author matrix as there are much more authors than years
    nbAuthColRow = matrixLayout(authCount)
    # last * 2 to make harmonize sizes of keywords, years, authors
    L1_REGION_WIDTH = int(FIGURE_REGION_WIDTH * (2 * nbAuthColRow[0])) * 2
    L1_REGION_HEIGHT = int(FIGURE_REGION_HEIGHT * (2 * nbAuthColRow[1])) * 2
    
    # generate the XML scene
    outputroot = ET.Element("scene")
    level_entry = ET.SubElement(outputroot, "level")
    level_entry.set("depth", "0")
    level_entry.set("ceiling", LEVEL0_CEILING)
    level_entry.set("floor", LEVEL0_FLOOR)
    level1 = ET.SubElement(outputroot, "level")
    level1.set("depth", "1")
    level1.set("ceiling", LEVEL1_CEILING)
    level1.set("floor", LEVEL1_FLOOR)
    level1b = ET.SubElement(outputroot, "level")
    level1b.set("depth", "2")
    level1b.set("ceiling", LEVEL1b_CEILING)
    level1b.set("floor", LEVEL1b_FLOOR)
    level_figs = ET.SubElement(outputroot, "level")
    level_figs.set("depth", "3")
    level_figs.set("ceiling", FIG_LEVEL_CEILING)
    level_figs.set("floor", FIG_LEVEL_FLOOR)
    level_lpapers = ET.SubElement(outputroot, "level")
    level_lpapers.set("depth", "4")
    level_lpapers.set("ceiling", PAPER_LEVEL_CEILING)
    level_lpapers.set("floor", PAPER_LEVEL_FLOOR)
    root_region_el = ET.SubElement(outputroot, "region")
    root_region_el.set('id', 'root')
    root_region_el.set("levels", '0')
    root_region_el.set("tful", FADE_IN)
    root_region_el.set("tfll", FADE_IN)
    root_region_el.set("ttul", FADE_OUT)
    root_region_el.set("ttll", FADE_OUT)
    root_region_el.set('x', '0')
    root_region_el.set('y', '0')
    root_region_el.set('w', str(int(4 * L1_REGION_WIDTH)))
    root_region_el.set('h', str(int(4 * L1_REGION_HEIGHT)))
    root_region_el.set('stroke', "white")
    root_region_el.set('sensitive', "true")
    object_el = ET.SubElement(root_region_el, "object")
    object_el.set('id', "entryYearLb")
    object_el.set('type', "text")
    object_el.set('x', str(-L1_REGION_WIDTH))
    object_el.set('y', str(L1_REGION_HEIGHT/3))
    object_el.set('scale', L0_LABEL_SCALE_FACTOR)
    object_el.set('sensitive', "false")
    object_el.text = "By year"
    object_el = ET.SubElement(root_region_el, "object")
    object_el.set('id', "yearsLb")
    object_el.set('type', "text")
    object_el.set('x', str(-L1_REGION_WIDTH))
    object_el.set('y', str(L1_REGION_HEIGHT))
    object_el.set('scale', str(int(int(L0_LABEL_SCALE_FACTOR) / 1.5)))
    object_el.set('sensitive', "false")
    object_el.set("fill", "#AAA")
    object_el.text = "1988 ... 2007"
    object_el = ET.SubElement(root_region_el, "object")
    object_el.set('id', "entryAuthorLb")
    object_el.set('type', "text")
    object_el.set('x', str(L1_REGION_WIDTH))
    object_el.set('y', str(L1_REGION_HEIGHT/3))
    object_el.set('sensitive', "false")
    object_el.set('scale', L0_LABEL_SCALE_FACTOR)
    object_el.text = "By author"
    object_el = ET.SubElement(root_region_el, "object")
    object_el.set('id', "kwAuthorLb")
    object_el.set('type', "text")
    object_el.set('x', "0")
    object_el.set('y', str(int(-1.75 * L1_REGION_HEIGHT)))
    object_el.set('scale', L0_LABEL_SCALE_FACTOR)
    object_el.set('sensitive', "false")
    object_el.text = "By keyword"
    
    kwrw = int(3 * L1_REGION_WIDTH/2.0 * 16 / 12)
    kwrh = int(3 * L1_REGION_HEIGHT/2.0)
    x = 0
    y = int(-1.5 * L1_REGION_HEIGHT/2.0)
    # f_path = "%s/kwss.png" % (SRC_DIR)
    # im = Image.open(f_path)
    # sz = im.size
    # mw = kwrw / sz[0]
    # mh = kwrh / sz[1]
    # minmf = min([mw, mh])
    # w = int(sz[0] * minmf * 0.992)
    # h = int(sz[1] * minmf * 0.992)
    # object_el = ET.SubElement(root_region_el, "object")
    # object_el.set('id', "kwss")
    # object_el.set('type', "image")
    # object_el.set('x', str(x))
    # object_el.set('y', str(y))
    # object_el.set('w', str(w))
    # object_el.set('h', str(h))
    # object_el.set('src', "kwss.png")
    # object_el.set('sensitive', "false")

    L0KWs = ["interface", "interaction", "user", "input", "visualization", "information", "virtual", "device"]
    L0KWsPos = [(x, y), (int(x+kwrw/3.0), int(y-kwrh/3.0)), (int(x-kwrw/3.0), int(y+kwrh/6.0)),\
                (int(x-kwrw/6.0), int(y+kwrh/2.8)), (int(x+kwrw/4.0), int(y-kwrh/6.7)), (int(x-kwrw/2.4), int(y-kwrh/2.2)),\
                (int(x+kwrw/3.2), int(y+kwrh/2.9)), (int(x-kwrw/4.4), int(y-kwrh/8.0))]
    L0LSF = int(L0_LABEL_SCALE_FACTOR)
    L0KWsScale = [int(L0LSF), int(L0LSF/1.4), int(L0LSF/1.6),\
                  int(L0LSF/1.8), int(L0LSF/2.0), int(L0LSF/2.2),\
                  int(L0LSF/2.4), int(L0LSF/2.6), ]
    
    i = 0
    for kw in L0KWs:
        object_el = ET.SubElement(root_region_el, "object")
        object_el.set('id', "L0KW-%s" % i)
        object_el.set('type', "text")
        object_el.set('x', str(L0KWsPos[i][0]))
        object_el.set('y', str(L0KWsPos[i][1]))
        object_el.set('scale', str(L0KWsScale[i]))
        object_el.set('sensitive', "false")
        object_el.set('fill', "#AAA")
        object_el.text = kw
        i += 1    
    
    object_el = ET.SubElement(root_region_el, "object")
    object_el.set('id', "aboutLb")
    object_el.set('type', "text")
    object_el.set('x', str(int(1.8*L1_REGION_WIDTH)))
    object_el.set('y', str(int(-1.75 * L1_REGION_HEIGHT)))
    object_el.set('scale', str(int(int(L0_LABEL_SCALE_FACTOR)/2)))
    object_el.set('sensitive', "true")
    object_el.set('fill', "#AAA")
    object_el.text = "About..."
    
    buildYearTree(outputroot, metadataRoot, matrixLayout(prCount))
    buildAuthorTree(outputroot, metadataRoot, matrixLayout(authCount))
    buildKeywordTree(outputroot, metadataRoot, x, y, kwrw, kwrh)
    
    # serialize the tree
    tree = ET.ElementTree(outputroot)
    log("Writing %s" % outputSceneFile)
    tree.write(outputSceneFile, encoding='utf-8') # was iso-8859-1

################################################################################
# compute smallest almost square matrix that can accomodate a given number
# of items
################################################################################
def matrixLayout(nbItems):
    # compute number of columns and rows to accomodate nbItems
    nbRow = int(math.floor(math.sqrt(nbItems)))
    nbCol = nbRow
    while nbCol * nbRow < nbItems:
        nbCol += 1
    return (nbCol, nbRow)

################################################################################
# Build scene subtree that corresponds to browsing papers by proceedings
################################################################################
def buildYearTree(outputParent, metadataRoot, colRow):
    log("Building proceedings tree", 1)
    nbCol = colRow[0]
    nbRow = colRow[1]
    xc = int(-L1_REGION_WIDTH)
    yc = int(L1_REGION_HEIGHT)
    region_el = ET.SubElement(outputParent, "region")
    region_el.set("x", str(xc))
    region_el.set("y", str(yc))
    region_el.set("w", str(L1_REGION_WIDTH))
    region_el.set("h", str(L1_REGION_HEIGHT))
    region_el.set("levels", "1")
    region_el.set("id", "proceedings")
    region_el.set("title", "Proceedings")
    region_el.set("containedIn", "root")
    region_el.set("stroke", "#AAA")
    region_el.set("tful", FADE_IN)
    region_el.set("tfll", FADE_IN)
    region_el.set("ttul", FADE_OUT)
    region_el.set("ttll", FADE_OUT)
    region_el.set('sensitive', "true")
    yi = -1
    years = year2papers.keys()
    years.sort()
    dx = int(L1_REGION_WIDTH / (nbCol * 2))
    xo = int(-L1_REGION_WIDTH*1.5 - dx)
    dy = int(L1_REGION_HEIGHT / (nbRow * 2))
    # 1.52 instead of 1.5 to slightly offset covers
    # so that last row's labels fit in
    y = int(L1_REGION_HEIGHT*1.52 + dy)
    cover_width = int(dx * 1.45)
    cover_height = int(dy * 1.45)
    for row in range(nbRow):
        y -= 2 * dy
        x = xo
        for col in range(nbCol):
            x += 2 * dx
            yi += 1
            if yi < len(years):
                year = years[yi]
                log("Processing year %s" % year, 2)
                f = "UIST%s.png" % year
                f_path = "%s/covers/%s" % (SRC_DIR, f)
                im = Image.open(f_path)
                sz = im.size
                mw = cover_width / sz[0]
                mh = cover_height / sz[1]
                minmf = min([mw, mh])
                w = sz[0] * minmf
                h = sz[1] * minmf
                # image
                object_el = ET.SubElement(region_el, "object")
                object_el.set('id', "cover-%s" % year)
                object_el.set('type', "image")
                object_el.set('x', str(x))
                object_el.set('y', str(y))
                object_el.set('w', str(w))
                object_el.set('h', str(h))
                object_el.set('src', "covers/%s" % f)
                figRegID = "procFigs-%s" % year
                object_el.set('takesToRegion', figRegID)
                # label
                object_el = ET.SubElement(region_el, "object")
                object_el.set('id', "yearLb-%s" % year)
                object_el.set('type', "text")
                object_el.set('x', str(x))
                object_el.set('y', str(int(y-dy)))
                object_el.set('scale', L1_YEAR_LABEL_SCALE_FACTOR)
                object_el.text = year
                object_el.set('fill', "#AAA")
                object_el.set('takesToRegion', figRegID)
                articles = year2papers.get(year)
                layoutFigures(articles, figRegID, region_el.get('id'),\
                              "proc", x, y, outputParent, year, False)
            else:
                break

# ('ref letter for horizontal positioning', next to its west (0) or east (2) bound,
#  'ref letter for vertical positioning', next to its north (1) or south (3) bound,
#  horizontal offset, vertical offset factors * dx or dy)
RPIF = [
# A
('fake'),
# B
('A', 2, 'A', 1, 1, 0),
# C
('B', 2, 'A', 1, 1, 0),
# D
('C', 2, 'C', 1, 1, 0),
# E
('D', 0, 'D', 3, 0, -1),
# F
('A', 0, 'B', 3, 0, -1),
# G
('F', 2, 'F', 1, 1, 0),
# H
('G', 2, 'G', 1, 1, 0),
# I
('H', 2, 'E', 3, 1, -1),
# J
('I', 2, 'I', 1, 1, 0),
# K
('A', 0, 'G', 3, 0, -1),
# L
('K', 2, 'H', 3, 1, -1),
# M
('L', 2, 'H', 3, 1, -1),
# N
('M', 2, 'J', 3, 1, -1),
# O
('N', 2, 'N', 1, 1, 0),
# P
('M', 2, 'N', 3, 2, -1),
# Q
('P', 2, 'P', 1, 1, 0),
# R
('A', 0, 'K', 3, 0, -1),
# S
('R', 2, 'M', 3, 1, -1),
# T
('S', 2, 'S', 1, 1, 0),
# U
('T', 0, 'T', 3, 0, -1),
# V
('T', 2, 'P', 3, 1, -1),
# W
('A', 0, 'R', 3, 0, -1),
# X
('W', 2, 'S', 3, 1, -1),
# Y
('U', 2, 'V', 3, 1, -1),
# Z
('Y', 2, 'Y', 1, 1, 0),
# fake
('Z', 0, 'Z', 0, 0, 0)
]

REGION_BOUNDS_IN_FLOW = {}

################################################################################
# Build scene subtree that corresponds to browsing papers by authors
################################################################################
def buildAuthorTree(outputParent, metadataRoot, colRow):
    log("Building author tree", 1)
    nbCol = colRow[0]
    nbRow = colRow[1]
    x = int(L1_REGION_WIDTH)
    y = int(L1_REGION_HEIGHT)
    region_el = ET.SubElement(outputParent, "region")
    region_el.set("x", str(x))
    region_el.set("y", str(y))
    region_el.set("w", str(L1_REGION_WIDTH))
    region_el.set("h", str(L1_REGION_HEIGHT))
    region_el.set("levels", "1")
    region_el.set("id", "authors")
    region_el.set("title", "Authors")
    region_el.set("containedIn", "root")
    region_el.set("stroke", "#AAA")
    region_el.set("tful", FADE_IN)
    region_el.set("tfll", FADE_IN)
    region_el.set("ttul", FADE_OUT)
    region_el.set("ttll", FADE_OUT)
    region_el.set('sensitive', "true")
    region_el.set("requestOrdering", "decl")
    authors = author2papers.keys()
    # sorts by author ID, which should correspond to sorting by author name
    authors.sort()
    lastFirstChar = ""
    # [('A', [authorID1, ...]), ('B', [authorIDN, ...]), ...]
    authors_by_letter = []
    for authorID in authors:
        authorName = author2papers.get(authorID)[0]
        firstChar = authorName[0].lower()
        if firstChar == lastFirstChar:
            authors_by_letter[-1][1].append(authorID)
        else:
            authors_by_letter.append((firstChar.upper(), [authorID]))
            lastFirstChar = firstChar
    # compute distance between names as if they were to be laid out in a single square matrix
    dx = int(L1_REGION_WIDTH / (2 * nbCol))
    dy = int(L1_REGION_HEIGHT / (2 * nbRow))
    xo = int(L1_REGION_WIDTH*0.5 -dx)
    yo = int(L1_REGION_HEIGHT*1.5 + dy)
    mx = xo + dx + L1_REGION_WIDTH / 6
    my = yo - dy - L1_REGION_HEIGHT / 6
    li = 0
    pi = 0
    # gray = False
    for letter in authors_by_letter:
        # gray = not gray
        nbColRowForL = matrixLayout(len(letter[1]))
        nbColForL = nbColRowForL[0]
        nbRowForL = nbColRowForL[1]
        ai = -1
        x = mx
        west = mx
        north = my
        lregion_el = ET.SubElement(outputParent, "region")
        lregion_el.set("levels", "2")
        lregion_el.set("id", "ABLr-%s" % letter[0])
        lregion_el.set("title", "Authors - %s" % letter[0])
        lregion_el.set("containedIn", "authors")
        lregion_el.set("stroke", "#AAA")
        # if gray:
        #     lregion_el.set("fill", "#CCC")
        lregion_el.set("tful", APPEAR)
        lregion_el.set("tfll", APPEAR)
        lregion_el.set("ttul", DISAPPEAR)
        lregion_el.set("ttll", DISAPPEAR)
        lregion_el.set('sensitive', "true")
        fareast = mx
        farsouth = my
        for col in range(nbColForL):
            x += dx
            y = my
            if x > fareast:
                fareast = x
            for row in range(nbRowForL):
                y -= dy
                if y < farsouth:
                    farsouth = y
                ai += 1
                if ai < len(letter[1]):
                    pi += 1
                    authorID = letter[1][ai]
                    data = author2papers.get(authorID)
                    log("Processing author %s" % authorID, 3)
                    dname = data[0].split(",", 1)
                    # surname on a first line
                    object_el = ET.SubElement(lregion_el, "object")
                    object_el.set('id', "authorLb-%s" % authorID)
                    object_el.set('type', "text")
                    object_el.set('x', str(x))
                    object_el.set('y', str(y))
                    object_el.set('scale', L1_AUTHOR_LABEL_SCALE_FACTOR)
                    figRegID = "authFigs-%s" % authorID
                    object_el.set('takesToRegion', figRegID)
                    object_el.text = dname[0]
                    # first name and middle name on a second line
                    object_el = ET.SubElement(lregion_el, "object")
                    object_el.set('id', "authorLb-%sfn" % authorID)
                    object_el.set('type', "text")
                    object_el.set('x', str(x))
                    object_el.set('y', str(y+dy/6))
                    object_el.set('scale', L1_AUTHOR_LABEL_SCALE_FACTOR)
                    object_el.set('takesToRegion', figRegID)
                    object_el.text = dname[1]
                    articles = []
                    for paperID in data[1]:
                     article = id2paper.get(paperID)
                     if article:
                         articles.append(article)
                     elif paperID in FIXED_PAPER_IDs.keys():
                         paperID = FIXED_PAPER_IDs.get(paperID)
                         article = id2paper.get(paperID)
                         if article:
                             articles.append(article)
                         else:
                             log("Failed to load paper %s for author %s, even after ID correction" % (paperID, authorID), 2)
                     else:
                         log("Failed to load paper %s for author %s" % (paperID, authorID), 2)
                    if articles:
                     layoutFigures(articles, figRegID, region_el.get('id'),\
                                   "au%s" % authorID, x, y, outputParent, data[0], True)                                   
                    object_el = ET.SubElement(lregion_el, "object")
                    object_el.set('id', "authorCAA-%s" % authorID)
                    object_el.set('type', "text")
                    object_el.set('x', str(int(x+dx/4)))
                    object_el.set('y', str(int(y-dy/6)))
                    object_el.set('scale', L1_COAUTHOR_ACTION_SCALE_FACTOR)
                    object_el.set("fill", "#999")
                    object_el.text = "All Coauthors"
                    if pi and pi % 100 == 0:
                       log("Processed %s authors" % pi, 2)
                else:
                    break
        east = fareast + dx
        south = farsouth - dy
        rcx = int((west+east)/2.0)
        rcy = int((north+south)/2.0)
        rcw = int(east-west)
        rch = int(north-south)
        lregion_el.set("x", str(rcx))
        lregion_el.set("y", str(rcy))
        lregion_el.set("w", str(rcw))
        lregion_el.set("h", str(rch))
        object_el = ET.SubElement(region_el, "object")
        object_el.set('id', "ABLo-%s" % letter[0])
        object_el.set('type', "text")
        object_el.set('x', str(rcx))
        object_el.set('y', str(int(rcy-0.7*dy)))
        object_el.set('scale', L1_LETTER_SCALE_FACTOR)
        object_el.set('takesToRegion', "ABLr-%s" % letter[0])
        object_el.text = letter[0]
        REGION_BOUNDS_IN_FLOW[letter[0]] = (west, north, east, south)
        li += 1
        RPIFe = RPIF[li]
        mx = REGION_BOUNDS_IN_FLOW.get(RPIFe[0])[RPIFe[1]] + RPIFe[4] * dx
        my = REGION_BOUNDS_IN_FLOW.get(RPIFe[2])[RPIFe[3]] + RPIFe[5] * dy
        
################################################################################
# Build scene subtree that corresponds to browsing papers by keywords
################################################################################
def buildKeywordTree(outputParent, metadataRoot, rx, ry, kwrw, kwrh):
    # make the keywords fit in a region that is more or less
    # the aspect ratio of a wide screen display
    # /2.0 because it was *2 for other regions to fit this one
    KW_REGION_WIDTH = kwrw
    KW_REGION_HEIGHT = kwrh
    x = rx
    y = ry
    region_el = ET.SubElement(outputParent, "region")
    region_el.set("x", str(x))
    region_el.set("y", str(y))
    region_el.set("w", str(KW_REGION_WIDTH))
    region_el.set("h", str(KW_REGION_HEIGHT))
    region_el.set("levels", "1")
    region_el.set("id", "keywords")
    region_el.set("title", "Keywords")
    region_el.set("containedIn", "root")
    region_el.set("stroke", "#AAA")
    region_el.set("tful", FADE_IN)
    region_el.set("tfll", FADE_IN) 
    region_el.set("ttul", FADE_OUT)
    region_el.set("ttll", FADE_OUT)
    region_el.set('sensitive', "true")
    ai = 0
    # theoretical start of a line
    xo = int(-KW_REGION_WIDTH / 2.15)
    # true when beginning a new line
    lineStart = True
    maxAtomHeightOnLine = 0
    # [[(atom label, atom x pos, atom scale factor)]]
    atomLines = []
    for atom in SORTED_KW_ATOMS:
        atomData = atom2kws.get(atom)
        atomSWeight = math.pow(atomData[1], 1/2.6)
        atomWWeight = math.pow(atomData[1], 1/2.9)
        atomHWeight = math.pow(atomData[1], 1/3.8)
        atomWidth = int(atomWWeight * L1_ATOM_CHAR_WIDTH * len(atom))
        # 1.2 to leave some space between lines
        atomHeight = int(atomHWeight * L1_ATOM_CHAR_HEIGHT * 1.4)
        if atomHeight > maxAtomHeightOnLine:
            maxAtomHeightOnLine = atomHeight
        if lineStart:
            x = xo
            line = []
            lineStart = False
        else:
            x += lastAtomWidth
        line.append((atom, x, int(atomSWeight * L1_ATOM_LABEL_SCALE_FACTOR)))
        ai += 1
        # reached end of line
        if x+atomWidth >= KW_REGION_WIDTH/2.45:
            atomLines.append((line, maxAtomHeightOnLine))
            maxAtomHeightOnLine = 0
            lineStart = True
        # last atom in the list
        elif ai >= len(SORTED_KW_ATOMS):
            atomLines.append((line, maxAtomHeightOnLine))
        if atom in FORCED_ATOM_WIDTHS:
            lastAtomWidth = FORCED_ATOM_WIDTHS.get(atom)
        else:
            lastAtomWidth = atomWidth
    log("Generating %s keyword lines" % len(atomLines), 2)
    y = 0 #int(-L1_REGION_HEIGHT * 0.5)
    ai = 0
    for line in atomLines:
        lineHeight = line[1]
        y -= lineHeight
        for atom in line[0]:
            object_el = ET.SubElement(region_el, "object")
            object_el.set('id', "atomLb-%03d" % ai)
            object_el.set('type', "text")
            object_el.set('x', str(atom[1]))
            object_el.set('y', str(y))
            object_el.set('scale', str(atom[2]))
            object_el.set('anchor', "start")
            object_el.text = atom[0]
            # get keyword expressions using this atom
            x = atom[1]
            kwIDs = atom2kws.get(atom[0])[0]
            if len(kwIDs) > 66:
                log("Split figures on five lines for atom %s" % atom[0], 3)
                kwLines = [kwIDs[:len(kwIDs)/5], kwIDs[len(kwIDs)/5:2*len(kwIDs)/5],\
                           kwIDs[2*len(kwIDs)/5:3*len(kwIDs)/5], kwIDs[3*len(kwIDs)/5:4*len(kwIDs)/5],
                           kwIDs[4*len(kwIDs)/5:]]
                ty = int(y + 4 * 1.5 * FIGURE_REGION_HEIGHT)
            elif len(kwIDs) > 24:
                log("Split figures on four lines for atom %s" % atom[0], 3)
                kwLines = [kwIDs[:len(kwIDs)/4], kwIDs[len(kwIDs)/4:len(kwIDs)/2],\
                           kwIDs[len(kwIDs)/2:3*len(kwIDs)/4], kwIDs[3*len(kwIDs)/4:]]
                ty = int(y + 3 * 1.5 * FIGURE_REGION_HEIGHT)
            elif len(kwIDs) > 12:
                log("Split figures on three lines for atom %s" % atom[0], 3)
                kwLines = [kwIDs[:len(kwIDs)/3], kwIDs[len(kwIDs)/3:2*len(kwIDs)/3], kwIDs[2*len(kwIDs)/3:]]
                ty = int(y + 2 * 1.5 * FIGURE_REGION_HEIGHT)
            elif len(kwIDs) > 6:
                log("Split figures on two lines for atom %s" % atom[0], 3)
                kwLines = [kwIDs[:len(kwIDs)/2], kwIDs[len(kwIDs)/2:]]
                ty = int(y + 1.5 * FIGURE_REGION_HEIGHT)
            else:
                kwLines = [kwIDs]
                ty = y
            for kwLine in kwLines:
                for kwID in kwLine:
                    kdata = kw2papers.get(kwID)
                    articles = []
                    for paperID in kdata[1]:
                        article = id2paper.get(paperID)
                        if article:
                            articles.append(article)
                        elif paperID in FIXED_PAPER_IDs.keys():
                            paperID = FIXED_PAPER_IDs.get(paperID)
                            article = id2paper.get(paperID)
                            if article:
                                articles.append(article)
                            else:
                                log("Failed to load paper %s for keyword %s, even after ID correction" % (paperID, kwe), 2)
                        else:
                            log("Failed to load paper %s for keyword %s" % (paperID, kwe), 2)
                    if articles:
						# title of figure region will be "atom , kwe"
                        layoutFigures(articles, "atom%03d%s" % (ai, kwID), region_el.get('id'),\
 									  "atom%03d%s-" % (ai, kwID), x, ty, outputParent, "%s , %s" % (atom[0], kdata[0]), True)
                        x += int( 1.5 * FIGURE_REGION_WIDTH)
                ty -= int(1.5 * FIGURE_REGION_HEIGHT)
                x = atom[1]
            ai += 1
            if ai and ai % 50 == 0:
               log("Processed %s keyword atoms" % ai, 2)

################################################################################
# Layout all figures associated with a sequence of papers (matrix)
################################################################################
def layoutFigures(articles, regionID, parentRegionID, idPrefix, xc, yc,\
 				  outputParent, regionTitle, showYear):
    region_el = ET.SubElement(outputParent, "region")
    region_el.set("x", str(xc))
    region_el.set("y", str(yc))
    region_el.set("w", str(FIGURE_REGION_WIDTH))
    region_el.set("h", str(FIGURE_REGION_HEIGHT))
    region_el.set("levels", "3")
    region_el.set("id", regionID)
    if regionTitle:
        region_el.set("title", regionTitle)
    region_el.set("containedIn", parentRegionID)
    region_el.set("tful", FADE_IN)
    region_el.set("tfll", FADE_IN)
    region_el.set("ttul", FADE_OUT)
    region_el.set("ttll", FADE_OUT)
    #region_el.set("stroke", "blue")
    nbColRow = matrixLayout(len(articles))
    nbCol = nbColRow[0]
    nbRow = nbColRow[1]
    ai = -1
    dx = int(FIGURE_REGION_WIDTH / (nbCol * 2))
    xo = int(xc - FIGURE_REGION_WIDTH/2 - dx)
    dy = int(FIGURE_REGION_HEIGHT / (nbRow * 2))
    y = int(yc + FIGURE_REGION_HEIGHT/2 + dy)
    # matrix dependent scale factor
    mdsf = TSFA * nbCol + TSFB
    for row in range(nbRow):
        y -= 2 * dy
        x = xo
        for col in range(nbCol):
            x += 2 * dx
            ai += 1
            if ai < len(articles):
                article = articles[ai]
                pID = article.get('id')
                if pID in XML_ID2FILE_ID:
                    pID = XML_ID2FILE_ID[pID]
                year = pID.split("#")[0]
                prefix = pID.split("#")[1]
                figureFile = "%s/%s.png" % (year, prefix)
                figureSrc = "figures/%s" % figureFile
                figure_path = "%s/%s" % (SRC_DIR, figureSrc)
                stroke = "#AAA"
                # handle case where no figure is available
                if not os.path.exists(figure_path):
                    figureFile = "%s/%s/H/%s_p1.png" % (year, prefix, prefix)
                    figureSrc = "papers/%s" % figureFile
                    figure_path = "%s/%s" % (SRC_DIR, figureSrc)
                    stroke = "black"
                    if os.path.exists(figure_path):
                        log("Warning: no figure for %s, using 1st page from article" % pID, 3)
                    else:
                        log("Warning: no figure or paper for %s, ignoring article" % pID, 3)
                        continue #XXX: should actually generate a placeholder here, because
                        #              this creates a hole in the matrix
                paperRegionID = "%sPapers%s%s" % (idPrefix, year, prefix.split('-')[0])
                # process representative image
                im = Image.open(figure_path)
                sz = im.size
                # have figures fill FIGURE_REGION_WIDTH in an optmial manner
                # depending on number of papers in region
                mw = int(1.5*dx / sz[0])
                mh = int(1.5*dy / sz[1])
                minmf = min([mw, mh])
                w = sz[0] * minmf
                h = sz[1] * minmf
                # image
                object_el = ET.SubElement(region_el, "object")
                object_el.set('id', "%sFig%s%s" % (idPrefix, year, prefix.split('-')[0]))
                object_el.set('type', "image")
                object_el.set('x', str(x))
                object_el.set('y', str(y))
                object_el.set('w', str(w))
                object_el.set('h', str(h))
                object_el.set('src', figureSrc)
                object_el.set('stroke', stroke)
                object_el.set('takesToRegion', paperRegionID)
                # paper title
                title = article.findtext("title")
                title = title.replace("<italic>", "").replace("</italic>", "")
                title = title.replace("<i>", "").replace("</i>", "")
                title = title.replace("<sup>", "").replace("</sup>", "")
                titleLines = splitInLines(title, MAX_CHARS_PER_TITLE_LINE)
                subtitle = article.findtext("subtitle")
                if subtitle:
                    subtitleLines = splitInLines(subtitle, MAX_CHARS_PER_SUBTITLE_LINE)
                    # add a colon at the end of main title if there is a subtitle
                    if titleLines:
                        titleLines[-1] += ":"
                    else:
                        titleLines = subtitleLines
                        subtitleLines = []
                else:
                    subtitleLines = []
                # title
                i = 0
                dty = PAPER_REGION_WIDTH / 14 * mdsf
                #ty = y - 1.5*dy/2 + dty/5
                ty = y-h/2
                for titleLine in titleLines:
                    i += 1
                    ty -= dty
                    object_el = ET.SubElement(region_el, "object")
                    object_el.set("type", "text")
                    object_el.set("id", "%sFig%s%sTL%s" % (idPrefix, year, prefix.split('-')[0], i))
                    object_el.text = titleLine
                    object_el.set("x", str(x))
                    object_el.set("y", str(int(ty)))
                    object_el.set("scale", str(int(L2_TITLE_LABEL_SCALE_FACTOR*mdsf)))
                i = 0
                # subtitle
                dty = PAPER_REGION_WIDTH / 16 * mdsf
                for subtitleLine in subtitleLines:
                    i += 1
                    ty -= dty
                    object_el = ET.SubElement(region_el, "object")
                    object_el.set("type", "text")
                    object_el.set("id", "%sFig%s%sSTL%s" % (idPrefix, year, prefix.split('-')[0], i))
                    object_el.text = subtitleLine
                    object_el.set("x", str(x))
                    object_el.set("y", str(int(ty)))
                    object_el.set("scale", str(int(L2_SUBTITLE_LABEL_SCALE_FACTOR*mdsf)))
                if showYear:
                    ty -= dty
                    object_el = ET.SubElement(region_el, "object")
                    object_el.set("type", "text")
                    object_el.set("id", "%sFig%s%sYEAR" % (idPrefix, year, prefix.split('-')[0]))
                    object_el.text = "(%s)" % year
                    object_el.set("x", str(x))
                    object_el.set("y", str(int(ty)))
                    object_el.set("scale", str(int(L2_SUBTITLE_LABEL_SCALE_FACTOR*mdsf)))
                    object_el.set("stroke", "#AAA")
                # put actions above figure
                ty = y + h/2 + dty
                object_el = ET.SubElement(region_el, "object")
                object_el.set("type", "text")
                object_el.set("id", "%sFig%s%sAUA%s" % (idPrefix, year, prefix.split('-')[0], i))
                object_el.text = "Authors"
                object_el.set("x", str(x-w/3))
                object_el.set("y", str(int(ty)))
                object_el.set("scale", str(int(L2_TITLE_LABEL_SCALE_FACTOR*mdsf)))
                object_el.set("fill", "#AAA")
                object_el = ET.SubElement(region_el, "object")
                object_el.set("type", "text")
                object_el.set("id", "%sFig%s%sKWA%s" % (idPrefix, year, prefix.split('-')[0], i))
                object_el.text = "Keywords"
                object_el.set("x", str(x+w/3))
                object_el.set("y", str(int(ty)))
                object_el.set("scale", str(int(L2_TITLE_LABEL_SCALE_FACTOR*mdsf)))
                object_el.set("fill", "#AAA")
                # # paper authors
                # dty = PAPER_REGION_WIDTH / 20
                # ty -= dty
                # object_el = ET.SubElement(region_el, "object")
                # object_el.set("type", "text")
                # object_el.set("id", "%sFig%s%sAU" % (idPrefix, year, prefix.split('-')[0]))
                # object_el.text = getAuthors(article)
                # object_el.set("x", str(x))
                # object_el.set("y", str(int(ty)))
                # object_el.set("scale", str(L2_AUTHORS_LABEL_SCALE_FACTOR))
                paperRegionTitle = title
                if subtitle:
                    paperRegionTitle += ": %s" % subtitle
                if len(paperRegionTitle) > MAX_CHAR_IN_PAPER_REGION_TITLE:
                    paperRegionTitle = "%s..." % paperRegionTitle[:MAX_CHAR_IN_PAPER_REGION_TITLE]
                layoutPaper(article, paperRegionID, region_el.get('id'), idPrefix, x, y,\
                            outputParent, paperRegionTitle)

################################################################################
# Layout all pages associated with a paper (one row)
################################################################################
def layoutPaper(article, regionID, parentRegionID, idPrefix, xc, yc,\
                outputParent, regionTitle):
    region_el = ET.SubElement(outputParent, "region")
    region_el.set("x", str(xc))
    region_el.set("y", str(yc))
    region_el.set("w", str(PAPER_REGION_WIDTH))
    region_el.set("h", str(PAPER_REGION_HEIGHT))
    region_el.set("levels", "4")
    region_el.set("id", regionID)
    region_el.set("containedIn", parentRegionID)
    region_el.set("tful", FADE_IN)
    region_el.set("tfll", APPEAR)
    region_el.set("ttul", FADE_OUT)
    region_el.set("ttll", DISAPPEAR)
    # region_el.set("stroke", "red")
    region_el.set("requestOrdering", "dist")
    if regionTitle:
        region_el.set("title", regionTitle)
    pID = article.get('id')
    if pID in XML_ID2FILE_ID:
        pID = XML_ID2FILE_ID[pID]
    year = pID.split("#")[0]
    prefix = pID.split("#")[1]
    paperSrc = "papers/%s/%s" % (year, prefix)
    paper_path = "%s/%s" % (SRC_DIR, paperSrc)
    if not os.path.exists(paper_path):
        log("Warning: failed to load paper %s" % paper_path, 2)
    else:
        pages = os.listdir("%s/H" % paper_path)
        PAGE_WIDTH = PAPER_REGION_WIDTH / (len(pages) + 0.2 * (len(pages)+1))
        x = int(xc - PAPER_REGION_WIDTH/2 + 0.7 * PAGE_WIDTH)
        dx = PAGE_WIDTH
        i = 1
        for page in pages:
            pageSrc = "%s/H/%s" % (paperSrc, page)
            page_path = "%s/%s" % (SRC_DIR, pageSrc)
            im = Image.open(page_path)
            sz = im.size
            mw = PAGE_WIDTH / float(sz[0])
            pageWidth = int(sz[0] * mw)
            pageHeight = int(sz[1] * mw)
            object_el = ET.SubElement(region_el, "object")
            object_el.set('id', "%sPage%s%s_%sH" % (idPrefix, year, prefix.split('-')[0], i))
            object_el.set('type', "image")
            object_el.set('x', str(x))
            object_el.set('y', str(yc))
            object_el.set('w', str(pageWidth))
            object_el.set('h', str(pageHeight))
            object_el.set('src', "%s/H/%s_p%s.png" % (paperSrc, prefix, i))
            object_el.set('stroke', "#000")
            x += int(pageWidth * 1.2)
            i += 1

################################################################################
# Keyword atom sorter, case insensitive
################################################################################
def kwAtomSorter(a1, a2):
    a1l = a1.lower()
    a2l = a2.lower()
    if  a1l < a2l:
        return -1
    elif a1l > a2l:
        return 1
    else:
        return 0

################################################################################
# get authors of an article as a string
################################################################################
def getAuthors(article):
    authors = []
    for author in article.findall("authors/author"):
        fn = author.findtext("first_name")
        mn = author.findtext("middle_name")
        ln = author.findtext("last_name")
        sf = author.findtext("suffix")
        if mn:
            name = "%s %s %s" % (fn, mn, ln)
        else:
            name = "%s %s" % (fn, ln)
        if sf:
            name = "%s %s" % (name, sf)
        authors.append(name)
    return ", ".join(authors)

################################################################################
# split a string on several lines
################################################################################
def splitInLines(s, maxCharsPerLine):
    res = []
    sbw = s.split(None)
    i = 0
    line = ""
    while i < len(sbw):
        if len(line)+len(sbw[i])+1 < maxCharsPerLine:
            line += "%s " % sbw[i]
            i += 1
            if i == len(sbw):
                res.append(line[:-1])
        else:
            res.append(line[:-1])
            line = ""
    return res

################################################################################
# Max.
################################################################################
def getMaxChildren(xmlMetadata):
    global id2paper
    global year2papers
    global author2papers
    global kw2papers
#    global PPW_L
#    global PPH_L
#    global PPW_H
#    global PPH_H
    # pages per paper, papers per year
    max_page_count = 0
    max_paper_count = 0
    nbYears = 0
    for f in os.listdir("%s/papers" % SRC_DIR):
        year_dir_abspath = "%s/%s" % (os.path.realpath("%s/papers" % SRC_DIR), f)
        if os.path.isdir(year_dir_abspath):
            year = f
            paper_count = 0
            for f2 in os.listdir(year_dir_abspath):
                paper_dir_abspath = "%s/%s" % (year_dir_abspath, f2)
                if os.path.isdir(paper_dir_abspath):
                    paper_count += 1
                    page_count = 0
                    for f3 in os.listdir("%s/%s" % (paper_dir_abspath, "H")):
                        # L could be H, does not matter as both directories
                        # contain the same PNG images at different resolutions
                        if f3.endswith(".png"):
                            page_count += 1
                            # uncomment following lines to update values of page
                            # resolutions from actual values in file hierarchy
#                           # store max dimension at high resolution
#                           im = Image.open("%s/H/%s" % (paper_dir_abspath, f3))
#                            img_size = im.size
#                            if img_size[0] > PPW_H:
#                                PPW_H = img_size[0]
#                            if img_size[1] > PPH_H:
#                                PPH_H = img_size[1]
#                            # store max dimension at low resolution
#                           im = Image.open("%s/L/%s" % (paper_dir_abspath, f3))
#                            img_size = im.size
#                            if img_size[0] > PPW_L:
#                                PPW_L = img_size[0]
#                            if img_size[1] > PPH_L:
#                                PPH_L = img_size[1]
                    # if paper contains more pages than any other before it, update max count
                    if page_count > max_page_count:
                        max_page_count = page_count
            nbMissingPapers = len(year2papers[year]) - paper_count
            if nbMissingPapers != 0:
                log("Warning: %s paper(s) missing in %s" % (nbMissingPapers, year), 1)
            if paper_count > max_paper_count:
                max_paper_count = paper_count
            nbYears += 1
    # years
    nbMissingYears = len(year2papers.keys()) - nbYears
    if nbMissingYears != 0:
        log("Warning: %s year(s) missing" % nbMissingYears, 1)
    # papers per author
    max_paper_per_author = 0
    for author in author2papers.keys():
        nb_paper = len(author2papers.get(author)[1])
        if nb_paper > max_paper_per_author:
            max_paper_per_author = nb_paper
    # papers per keyword expression
    max_paper_per_keyword = 0
    for kw in kw2papers.keys():
        nb_paper = len(kw2papers.get(kw)[1])
        if nb_paper > max_paper_per_keyword:
            max_paper_per_keyword = nb_paper
    return (max_page_count, max_paper_count, nbYears, max_paper_per_author, max_paper_per_keyword)
            

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
        TRACE_LEVEL = int(sys.argv[2])
else:
    sys.exit(0)

METADATA_FILE = "%s/%s" % (SRC_DIR, "UISTmetadata.xml")
XML_SCENE_FILE = "%s/%s" % (SRC_DIR, "scene.xml")
buildScene(METADATA_FILE, XML_SCENE_FILE)
