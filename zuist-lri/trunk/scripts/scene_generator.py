#!/usr/bin/python
# -*- coding: UTF-8 -*-
# $Id$

import os, sys
import math
import elementtree.ElementTree as ET

TRACE_LEVEL = 1

# paper page width and height, high resolution
PPW_H = 1224
PPH_H = 1584

PAPER_REGION_WIDTH = 0
PAPER_REGION_HEIGHT = 0
FIGURE_REGION_WIDTH = 0
FIGURE_REGION_HEIGHT = 0
L1_REGION_WIDTH = 0
L1_REGION_HEIGHT = 0

LEVEL0_CEILING = "8000000"
LEVEL0_FLOOR = "810000"
LEVEL1_CEILING = "810000"
LEVEL1_FLOOR = "45000"
TITLE_LEVEL_CEILING = "45000"
TITLE_LEVEL_FLOOR = "3500"
PAPER_LEVEL_CEILING = "3500"
PAPER_LEVEL_FLOOR = "0"

FADE_IN = "fadein"
FADE_OUT = "fadeout"
APPEAR = "appear"
DISAPPEAR = "disappear"

################################################################################
# METADATA
################################################################################
# paper id -> <reference/>
id2paper = {}
# paper year -> [paper id]
year2id = {}
# paper team -> [paper id]
team2id = {}
# paper author (authors/author/first+authors/author/last) -> [paper id]
author2id = {}
# paper directory names in lri4z/
availablePDFs = []

TEAM_FIXES = {"grand large": "grandlarge",
    "grand large": "PARALL",
    "proval": "DEMONS",
    "reseaux": "HIPERCOM",
    "hipercom-lri": "HIPERCOM",
    "tao": "I&A",
    "graph-comb": "GRAPHCOMB",
    "alchemy": "ARCHI",
    "gemo": "", # total (-1) overlap with IASI
    "aviz": "",
    "compsys": "",
    "caps": ""}

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
    global availablePDFs
    log("Loading metadata from %s" % metadataFile, 2)
    xmlMetadata = ET.parse(open(metadataFile, 'r'))
    # walk the metadata tree and generate multi-scale scene in dedicated XML format
    metadataRoot = xmlMetadata.getroot()
    for referencesEL in metadataRoot.findall(".//references"):
        for referenceEL in referencesEL.findall("./reference"):
            id2paper[referenceEL.get('key')] = referenceEL
    log("Found %s papers in metadata" % len(id2paper.keys()), 2)
    availablePDFs = os.listdir("%s/lri4z" % SRC_DIR)
    # directory contains one subdir per paper, plus scene.xml (which is obvisouly not a PDF)
    availablePDFs.remove("scene.xml")
    log ("Found %s papers in PDF directory" % len(availablePDFs), 2)
    # retrieve some global information from hierarchy to compute layout
    t = getMaxChildren()
    # maximum number of pages in a paper
    mpgCount = t[0]
    # maximum number of papers all categories
    mpallCount = t[1]
    # maximum number of papers per year
    mpyCount = t[2]
    # maximum number of papers per team
    mptCount = t[3]
    # maximum number of papers per author
    mpaCount = t[4]
    log("Maximum page count per paper: %s" % mpgCount, 0)
    log("Maximum paper count per year: %s" % mpyCount, 0)
    log("Maximum paper count per team: %s" % mptCount, 0)
    log("Maximum paper count per author: %s" % mpaCount, 0)
#    # compute largest matrix of figures for proceedings
#    nbColRow = matrixLayout(mppCount)
#    nbFigColProc = nbColRow[0]
#    nbFigRowProc = nbColRow[1]
#    log("Largest matrix of figures (proceedings): %sx%s" % (nbFigColProc, nbFigRowProc), 0)
#    # maximum number of papers per higher level of abstraction, whatever that is
#    mpCount = max(mppCount, mpaCount, mpkwCount)
#    # compute dimensions of a region containing the pages of a paper
#    # leave an empty space between pages that is .1 a page in size
#    # same before first page and after last page (hence the +.1)
#    PAPER_REGION_WIDTH = int(mpgCount * PPW_H * 1.1)
#    PAPER_REGION_HEIGHT = int(PPH_H * 1.1)
#    # compute dimensions of a region containing the figures associated with
#    # an higher-level entity (proceedings, author, keyword)
#    # base computation on largest matrix for all higher-level entities
#    nbFigColMax = max(nbFigColProc, nbFigColAu, nbFigColKw)
#    nbFigRowMax = max(nbFigRowProc, nbFigRowAu, nbFigRowKw)
#    # leave an empty space between figures that is equal to a figure in size
#    FIGURE_REGION_WIDTH = int(PAPER_REGION_WIDTH * (2 * nbFigColMax))
#	# consider width instead of height as we lay out objects in a square matrix
#    FIGURE_REGION_HEIGHT = int(PAPER_REGION_WIDTH * (2 * nbFigRowMax))
#    # compute L1 from author matrix as there are much more authors than years
#    nbAuthColRow = matrixLayout(authCount)
#    # last * 2 to make harmonize sizes of keywords, years, authors
#    L1_REGION_WIDTH = int(FIGURE_REGION_WIDTH * (2 * nbAuthColRow[0])) * 2
#    L1_REGION_HEIGHT = int(FIGURE_REGION_HEIGHT * (2 * nbAuthColRow[1])) * 2
#    
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
    level_figs = ET.SubElement(outputroot, "level")
    level_figs.set("depth", "2")
    level_figs.set("ceiling", TITLE_LEVEL_CEILING)
    level_figs.set("floor", TITLE_LEVEL_FLOOR)
    level_lpapers = ET.SubElement(outputroot, "level")
    level_lpapers.set("depth", "3")
    level_lpapers.set("ceiling", PAPER_LEVEL_CEILING)
    level_lpapers.set("floor", PAPER_LEVEL_FLOOR)
#    root_region_el = ET.SubElement(outputroot, "region")
#    root_region_el.set('id', 'root')
#    root_region_el.set("levels", '0')
#    root_region_el.set("tful", FADE_IN)
#    root_region_el.set("tfll", FADE_IN)
#    root_region_el.set("ttul", FADE_OUT)
#    root_region_el.set("ttll", FADE_OUT)
#    root_region_el.set('x', '0')
#    root_region_el.set('y', '0')
#    root_region_el.set('w', str(int(4 * L1_REGION_WIDTH)))
#    root_region_el.set('h', str(int(4 * L1_REGION_HEIGHT)))
#    root_region_el.set('stroke', "white")
#    root_region_el.set('sensitive', "true")
#    object_el = ET.SubElement(root_region_el, "object")
#    object_el.set('id', "entryYearLb")
#    object_el.set('type', "text")
#    object_el.set('x', str(-L1_REGION_WIDTH))
#    object_el.set('y', str(L1_REGION_HEIGHT/3))
#    object_el.set('scale', L0_LABEL_SCALE_FACTOR)
#    object_el.set('sensitive', "false")
#    object_el.text = "By year"
#    object_el = ET.SubElement(root_region_el, "object")
#    object_el.set('id', "yearsLb")
#    object_el.set('type', "text")
#    object_el.set('x', str(-L1_REGION_WIDTH))
#    object_el.set('y', str(L1_REGION_HEIGHT))
#    object_el.set('scale', str(int(int(L0_LABEL_SCALE_FACTOR) / 1.5)))
#    object_el.set('sensitive', "false")
#    object_el.set("fill", "#AAA")
#    object_el.text = "1988 ... 2007"
#    object_el = ET.SubElement(root_region_el, "object")
#    object_el.set('id', "entryAuthorLb")
#    object_el.set('type', "text")
#    object_el.set('x', str(L1_REGION_WIDTH))
#    object_el.set('y', str(L1_REGION_HEIGHT/3))
#    object_el.set('sensitive', "false")
#    object_el.set('scale', L0_LABEL_SCALE_FACTOR)
#    object_el.text = "By author"
#    object_el = ET.SubElement(root_region_el, "object")
#    object_el.set('id', "kwAuthorLb")
#    object_el.set('type', "text")
#    object_el.set('x', "0")
#    object_el.set('y', str(int(-1.75 * L1_REGION_HEIGHT)))
#    object_el.set('scale', L0_LABEL_SCALE_FACTOR)
#    object_el.set('sensitive', "false")
#    object_el.text = "By keyword"
#
#    buildTeamTree(outputroot, metadataRoot, matrixLayout(prCount))
#    buildAuthorTree(outputroot, metadataRoot, matrixLayout(prCount))
    
    # serialize the tree
    tree = ET.ElementTree(outputroot)
    log("Writing %s" % outputSceneFile)
    tree.write(outputSceneFile, encoding='utf-8') # was iso-8859-1

################################################################################
# Max.
################################################################################
def getMaxChildren():
    global year2id
    global team2id
    global author2id
    # pages per paper, papers per year/team/author
    max_page_count = 0
    missing_team_paper_ids = []
    for pid in id2paper.keys():
        referenceEL = id2paper.get(pid)
        if pid in availablePDFs:
            page_count = len(os.listdir("%s/lri4z/%s" % (SRC_DIR, pid)))
            if page_count > max_page_count:
                max_page_count = page_count
        else:
            log("Warning: could not find a PDF for %s" % pid, 3)
        # year
        year = referenceEL.find('bibentry/year').text
        if year in year2id.keys():
            year2id[year].append(referenceEL)
        else:
            year2id[year] = [referenceEL,]
        # team
        teamsEL = referenceEL.find('bibentry/x-equipes')
        if teamsEL is None:
            log("Warning: could not find any team for %s" % pid, 3)
            missing_team_paper_ids.append(pid)
        else:
            for team in normalizeTeams(teamsEL.text):
                team = team.upper()
                if team in team2id.keys():
                    team2id[team].append(referenceEL)
                else:
                    team2id[team] = [referenceEL,]
        # authors
        for authorEL in referenceEL.findall('bibentry/authors/author'):
            if not authorEL.find('first') is None:
                author = authorEL.find('first').text.lower()
            if not authorEL.find('last') is None:
                author = "%s %s" % (author, authorEL.find('last').text.lower())
            if author in author2id.keys():
                author2id[author].append(referenceEL)
            else:
                author2id[author] = [referenceEL,]
    
    max_paper_per_year = 0
    for v in year2id.values():
        if len(v) > max_paper_per_year:
            max_paper_per_year = len(v)
    max_paper_per_team = 0
    # ignoring some teams generates entries with key ""
    del team2id[""]
    for k in team2id.keys():
        print "%s %s" % (k, len(team2id[k]))
        if k == "EXT":
            continue
        if len(team2id[k]) > max_paper_per_team:
            max_paper_per_team = len(team2id[k])
    max_paper_per_author = 0
    for v in author2id.values():
        if len(v) > max_paper_per_author:
            max_paper_per_author = len(v)
    max_paper_count = max(max_paper_per_year, max_paper_per_team, max_paper_per_author)
    print (max_page_count, max_paper_count, max_paper_per_year, max_paper_per_team, max_paper_per_author)
    return (max_page_count, max_paper_count, max_paper_per_year, max_paper_per_team, max_paper_per_author)

################################################################################
# fix inconsistencies in team names
################################################################################
def normalizeTeams(teams):
    teams = teams.lower()
    for tf in TEAM_FIXES.keys():
        if teams.find(tf) != -1:
            teams = teams.replace(tf, TEAM_FIXES[tf])
    return teams.split(" ")
    
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
# Build scene subtree that corresponds to browsing papers per team/support/year
################################################################################
def buildTeamTree(outputParent, metadataRoot, colRow):
    log("Building team tree", 1)
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

METADATA_FILE = "%s/%s" % (SRC_DIR, "bibdata.xml")
XML_SCENE_FILE = "%s/%s" % (SRC_DIR, "lri4z/scene.xml")
buildScene(METADATA_FILE, XML_SCENE_FILE)
