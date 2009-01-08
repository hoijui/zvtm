#!/usr/bin/python
# -*- coding: UTF-8-*-
# $Id$

import os, sys
import math, string
import elementtree.ElementTree as ET
import unicodedata
import csv

TRACE_LEVEL = 1
DEBUG_SCENE = 0

XML_SCENE_FILE_NAME = "scene.xml"

# paper page width and height
PPW = 1224
PPH = 1584

PAPER_REGION_WIDTH = 0
PAPER_REGION_HEIGHT = 0
META_REGION_WIDTH = 0
META_REGION_HEIGHT = 0
YEAR_REGION_WIDTH = 0
YEAR_REGION_HEIGHT = 0
CAT_REGION_WIDTH = 0
CAT_REGION_HEIGHT = 0

L0_CEILING = "800000000"
L0_FLOOR = "300000000"
L1_CEILING = "300000000"
TREEMAP_CEILING = "70000000"
CAT_LEVEL_CEILING = "10000000"
CAT_LEVEL_FLOOR = "800000"
YEAR_LEVEL_CEILING = "800000"
YEAR_LEVEL_FLOOR = "150000"
META_LEVEL_CEILING = "150000"
META_LEVEL_FLOOR = "50000"
PAPER_LEVEL_CEILING = "50000"
PAPER_LEVEL_FLOOR = "0"

L0_LABEL_SCALE_FACTOR = "20000000"
TEAM_LABEL_SCALE_FACTOR = "6000000"
AUTHOR_LABEL_SCALE_FACTOR = "700000"
AUTHOR_TEAM_LABEL_SCALE_FACTOR = "600000"
CATEGORY_LABEL_SCALE_FACTOR = "150000"
YEAR_LABEL_SCALE_FACTOR = "30000"
TITLE_LABEL_SCALE_FACTOR = "2000"
SUBTITLE_LABEL_SCALE_FACTOR = "1200"
NOPDF_LABEL_SCALE_FACTOR = "100"

MAIN_LABEL_COLOR = "#777"
MISSING_PAPER_TITLE_COLOR = "#AAA"
MISSING_PAPER_COLOR = "red"

FADE_IN = "fadein"
FADE_OUT = "fadeout"
APPEAR = "appear"
DISAPPEAR = "disappear"

invalid_id_chars = ":/&'"
replacement_id_chars  =   "----"
id_trans = string.maketrans(invalid_id_chars, replacement_id_chars)

################################################################################
# METADATA
################################################################################
# paper id -> <reference/>
id2paper = {}
# paper team -> {cat:{year:[paper id]}}
tcy2id = {}
# paper author (authors/author/first + authors/author/last) -> {cat:{year:[paper id]}}
acy2id = {}
# paper directory names in lri4z/
availablePDFs = []
# author canonical name -> (first name, last name)
canonical_authors = {}
# author key -> author canonical name
author2canonical_author = {}
# author canonical name -> None (contains only authors from LRI)
authors_LRI = {}
# author canonical name -> team name
author2team = {}
# paper id -> [(author canonical name, 1 or 0 depending on whether author is from LRI or not)]
paper2authors = {}

################################################################################
# EXCEPTIONS
################################################################################
# order is important, lookup for replacements in a string of teams occurs as declared here
TEAM_FIXES = [("alchemy archi", "ARCHI"),
    ("gemo i&a iasi tao", "iasi IAO"),
    ("iasi", "IASI"),
    ("ia", "IAO"),
    ("i&a", "IAO"),
    ("grandlarge graphcomb parall", "graphcomb parall"),
    ("grandlarge parall", "PARALL"),
    ("grand large parall", "PARALL"),
    ("grand large", "PARALL"),
    ("grandlarge", "PARALL"),
    ("demons ext proval", "DEMONS EXT"),
    ("demons proval", "DEMONS"),
    ("proval demons", "DEMONS"),
    ("proval", "DEMONS"),
    ("hipercom reseaux", "HIPERCOM"),
    ("hipercom hipercom-lri", "HIPERCOM"),
    ("reseaux", "HIPERCOM"),    
    ("hipercom-lri", "HIPERCOM"),
    ("hipercom-", "HIPERCOM"),
    ("tao", "IAO"),
    ("graph-comb", "GRAPHCOMB"),
    ("alchemy", "ARCHI"),
    ("gemo", ""), # total (-1) overlap with IASI
    ("aviz", ""),
    ("compsys", ""),
    ("caps", ""),
    ("lri", "")]

TITLE_ELEMS = ['bibentry/title', 'bibentry/title', 'xtradata/description']
    
################################################################################
# TRANSLATIONS
# from zbib/bib2xml/moulinette/lib/bibliography/aeres.py
################################################################################
CATEGORIES_LRI_SEPTEMBRE_2008 = {
    u"1":u"Articles dans des revues internationales ou nationales avec comité de lecture répertoriées dans les bases de données internationales", # Titre
    u"1.1":u"Revues internationales majeures avec comité de lecture",
    u"1.2":u"Autres revues avec comité de lecture",
    # -----------------
    u"2":u"Conférences données à l’invitation du Comité d’organisation dans un congrès national ou international",
    # -----------------
    u"3":u"Communications avec actes dans un congrès international ou national", # Titre
    u"3.1":u"Conférences et workshops internationaux majeurs avec actes et comité de lecture",
    u"3.2":u"Conférences et workshops nationaux majeurs avec actes et comité de lecture",
    u"3.3":u"Autres conférences et workshops",
    # -----------------
    u"4":u"Communications orales et par affiche, autres communications dans les conférences et workshops",
    # -----------------
    u"5":u"Ouvrages scientifiques (ou chapitres de ces ouvrages)",
    # -----------------
    u"6":u"Directions d’ouvrages",
    # -----------------
    u"7":u"Ouvrages de vulgarisation (ou chapitres de ces ouvrages), diffusion de la connaissance",
    # -----------------
    u"8":u"Autres publications",
    # -----------------
    u"9":u"Thèses et habilitations soutenues",
    # -----------------
    u"xx":u"Type et/ou support non renseigné(s)" # Important: garder "xx" pour texreport
    }

################################################################################
# Walk the hierarchy of UIST proceedings and generate XML scene description
################################################################################
def buildScene(metadataFile, authorsFile, outputSceneFile):
    global PAPER_REGION_WIDTH
    global PAPER_REGION_HEIGHT
    global META_REGION_WIDTH
    global META_REGION_HEIGHT
    global YEAR_REGION_WIDTH
    global YEAR_REGION_HEIGHT
    global CAT_REGION_WIDTH
    global CAT_REGION_HEIGHT
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
    if availablePDFs.count(XML_SCENE_FILE_NAME):
        availablePDFs.remove(XML_SCENE_FILE_NAME)
    log ("Found %s papers in PDF directory" % len(availablePDFs), 2)
    # get canonical authors
    parseAuthors(authorsFile)
    # retrieve some global information from hierarchy to compute layout
    t = generateAbstractTree()
    # maximum number of pages in a paper
    mpgCount = t[0]
    # maximum paper count per year per category per team/author
    mppCount = t[1]
    # maximum year count per category per team/author
    mpyCount = t[2]
    # maximum category count per team/author
    mpcCount = t[3]
    log("Maximum page count per paper: %s" % mpgCount, 0)
    log("Maximum paper count per year per category per team/author: %s" % mppCount, 0)
    log("Maximum year count per category per team/author: %s" % mpyCount, 0)
    log("Maximum category count per team/author: %s" % mpcCount, 0)
    
    # compute dimensions of a region containing the pages of a paper
    nbColRow = matrixLayout(mpgCount)
    log("Largest matrix of pages will be %sx%s" % nbColRow)
    # leave an empty space between pages that is .1 a page in size
    # same before first page and after last page (hence the +.1)
    PAPER_REGION_WIDTH = int(nbColRow[0] * PPW * 1.1)
    PAPER_REGION_HEIGHT = int(nbColRow[1] * PPH * 1.1)
    
    # compute dimensions of a region containing all papers for a given year
    META_REGION_HEIGHT = int(PAPER_REGION_HEIGHT * (mppCount+1) * 1.1)
    META_REGION_WIDTH = int(META_REGION_HEIGHT * 1.5)
    log("Meta region: %s x %s" % (META_REGION_WIDTH, META_REGION_HEIGHT), 2)
    
    # compute dimensions of a region containing all years for a given category
    # (all years put on a single column ; there should be only four for the 2005-2008 version)
    YEAR_REGION_WIDTH = int(META_REGION_WIDTH * mpyCount * 1.1)
    YEAR_REGION_HEIGHT = int(META_REGION_HEIGHT * mpyCount * 1.1)
    log("Year region: %s x %s" % (YEAR_REGION_WIDTH, YEAR_REGION_HEIGHT), 2)
    
    # compute dimensions of a region containing all categories for a given team/author
    CAT_REGION_WIDTH = int(YEAR_REGION_WIDTH * mpcCount * 1.1)
    CAT_REGION_HEIGHT = int(YEAR_REGION_HEIGHT * mpcCount * 1.1)
    log("Category region: %s x %s" % (CAT_REGION_WIDTH, CAT_REGION_HEIGHT), 2)
    
    # generate the XML scene
    outputroot = ET.Element("scene")
    # levels
    level_0 = ET.SubElement(outputroot, "level")
    level_0.set("depth", "0")
    level_0.set("ceiling", L0_CEILING)
    level_0.set("floor", L0_FLOOR)
    level_1 = ET.SubElement(outputroot, "level")
    level_1.set("depth", "1")
    level_1.set("ceiling", L1_CEILING)
    level_1.set("floor", TREEMAP_CEILING)
    level_tm = ET.SubElement(outputroot, "level")
    level_tm.set("depth", "2")
    level_tm.set("ceiling", TREEMAP_CEILING)
    level_tm.set("floor", CAT_LEVEL_CEILING)
    level_cat = ET.SubElement(outputroot, "level")
    level_cat.set("depth", "3")
    level_cat.set("ceiling", CAT_LEVEL_CEILING)
    level_cat.set("floor", CAT_LEVEL_FLOOR)
    level_year = ET.SubElement(outputroot, "level")
    level_year.set("depth", "4")
    level_year.set("ceiling", YEAR_LEVEL_CEILING)
    level_year.set("floor", YEAR_LEVEL_FLOOR)
    level_meta = ET.SubElement(outputroot, "level")
    level_meta.set("depth", "5")
    level_meta.set("ceiling", META_LEVEL_CEILING)
    level_meta.set("floor", META_LEVEL_FLOOR)
    level_paper = ET.SubElement(outputroot, "level")
    level_paper.set("depth", "6")
    level_paper.set("ceiling", PAPER_LEVEL_CEILING)
    level_paper.set("floor", PAPER_LEVEL_FLOOR)
    # high level regions
    root_region_el = ET.SubElement(outputroot, "region")
    root_region_el.set('id', 'root')
    root_region_el.set("levels", '0')
    root_region_el.set("tful", FADE_IN)
    root_region_el.set("tfll", FADE_IN)
    root_region_el.set("ttul", FADE_OUT)
    root_region_el.set("ttll", FADE_OUT)
    root_region_el.set('x', '0')
    root_region_el.set('y', '0')
    root_region_el.set('stroke', getStroke("red"))
    root_region_el.set('sensitive', "true")
    L0_RW = int(50 * CAT_REGION_HEIGHT)
    L0_RH = L0_RW * 3 / 5
    root_region_el.set('w', str(int(2.2*L0_RW)))
    root_region_el.set('h', str(int(1.2*L0_RH)))
    object_el = ET.SubElement(root_region_el, "object")
    object_el.set('id', "entryAuthorLb")
    object_el.set('type', "text")
    object_el.set('x', str(int(L0_RW*0.55)))
    object_el.set('y', str(0))
    object_el.set('fill', MAIN_LABEL_COLOR)
    object_el.set('scale', L0_LABEL_SCALE_FACTOR)
    object_el.set('sensitive', "false")
    object_el.text = "by author"
    object_el = ET.SubElement(root_region_el, "object")
    object_el.set('id', "entryTeamLb")
    object_el.set('type', "text")
    object_el.set('x', str(int(-L0_RW*0.55)))
    object_el.set('y', str(0))
    object_el.set('fill', MAIN_LABEL_COLOR)
    object_el.set('scale', L0_LABEL_SCALE_FACTOR)
    object_el.set('sensitive', "false")
    object_el.text = "by team"
    object_el = ET.SubElement(root_region_el, "object")
    object_el.set('id', "mainLb")
    object_el.set('type', "text")
    object_el.set('x', str(int(-L0_RW*0.6)))
    object_el.set('y', str(int(L0_RH/1.2)))
    object_el.set('fill', MAIN_LABEL_COLOR)
    object_el.set('scale', L0_LABEL_SCALE_FACTOR)
    object_el.set('sensitive', "false")
    object_el.set('anchor', "start")
    object_el.text = "Browse LRI publications (2005-2008)"
    # team tree
    buildTeamTree(outputroot, -L0_RW*0.55, 0, L0_RW, L0_RH)
    # author tree
    buildAuthorTree(outputroot, L0_RW*0.55, 0, L0_RW, L0_RH)
    # serialize the tree
    tree = ET.ElementTree(outputroot)
    log("Writing %s" % outputSceneFile)
    tree.write(outputSceneFile, encoding='utf-8')


################################################################################
# Canonical authors (list built manually)
################################################################################
def parseAuthors(authorsFile):
    global canonical_authors
    global author2canonical_author
    global authors_LRI
    global author2team
    firstLine = 1
    csvFile = open(authorsFile, 'r')
    csvreader = csv.reader(csvFile, dialect="excel", delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    i = 0
    for author in csvreader:
        # first line is the table's headers
        if firstLine:
            firstLine = False
            continue
        if len(author[1]) > 0:
            uauthor0 = unicode(author[0], 'utf-8')
            uauthor1 = unicode(author[1], 'utf-8')
            # name is not canonical, store mapping to canonical name
            author2canonical_author[unicodedata.normalize('NFD', uauthor0)] = unicodedata.normalize('NFD', uauthor1)
            # author is from an LRI team
            if len(author[7]) > 0:
                authors_LRI[unicodedata.normalize('NFD', uauthor1)] = None
                author2team[unicodedata.normalize('NFD', uauthor1)] = author[7]
        else:
            # name is a canonical one
            i += 1
            uauthor = unicode(author[0], 'utf-8')
            canonical_authors[unicodedata.normalize('NFD', uauthor)] = (author[3], author[2])
            # author is from an LRI team
            if len(author[7]) > 0:
                authors_LRI[unicodedata.normalize('NFD', uauthor)] = None
                author2team[unicodedata.normalize('NFD', uauthor)] = author[7]
    log("Found %s authors, among which %s are from LRI" % (len(canonical_authors.keys()), len(authors_LRI.keys())), 0)
    log("%s author names have been canonicalized" % len(author2canonical_author.keys()), 2)
    
################################################################################
# Abstract tree that will be wlaked to generate the actual ZUIST scene
################################################################################
def generateAbstractTree():
    global paper2authors
    # pages per paper, papers per year/team/author
    max_page_count = 0
    missing_team_paper_ids = []
    i = 0
    for pid in id2paper.keys():
        referenceEL = id2paper.get(pid)
        if pid in availablePDFs:
            page_count = len(os.listdir("%s/lri4z/%s" % (SRC_DIR, pid)))
            if page_count > max_page_count:
                max_page_count = page_count
        else:
            log("Warning: could not find a PDF for %s" % pid, 4)
        # team / cat / year
        teamsEL = referenceEL.find('bibentry/x-equipes')
        if teamsEL is None:
            log("Warning: could not find any team for %s" % pid, 3)
            missing_team_paper_ids.append(pid)
        else:
            for team in normalizeTeams(teamsEL.text):
                team = team.upper()
                if team != "" and team != "EXT":
                    storeTeamPaper(team, referenceEL)
        # author / cat / year
        ordered_authors = []
        for authorEL in referenceEL.findall('bibentry/authors/author'):
            fn = authorEL.find('first')
            ln = authorEL.find('last')
            if not fn is None and not ln is None:
                author = "%s %s" % (fn.text.lower(), ln.text.lower())
                ordered_authors.append(storeAuthorPaper(author, referenceEL))
            elif not ln is None:
                author = ln.text.lower()
                ordered_authors.append(storeAuthorPaper(author, referenceEL))
            elif not fn is None:
                author = fn.text.lower()
                ordered_authors.append(storeAuthorPaper(author, referenceEL))
        paper2authors[referenceEL.get('key')] = ordered_authors
    # max population per level
    max_paper_per_tcy = 0
    max_year_per_tc = 0
    max_cat_per_t = 0
    for categories in tcy2id.values():
        catvals = categories.values()
        if len(catvals) > max_cat_per_t:
            max_cat_per_t = len(catvals)
        for years in catvals:
            yearvals = years.values()
            if len(yearvals) > max_year_per_tc:
                max_year_per_tc = len(yearvals)
            for year in yearvals:
                if len(year) > max_paper_per_tcy:
                    max_paper_per_tcy = len(year)
    max_paper_per_acy = 0
    max_year_per_ac = 0
    max_cat_per_a = 0
    for categories in acy2id.values():
        catvals = categories.values()
        if len(catvals) > max_cat_per_t:
            max_cat_per_t = len(catvals)
        for years in catvals:
            yearvals = years.values()
            if len(yearvals) > max_year_per_tc:
                max_year_per_tc = len(yearvals)
            for year in yearvals:
                if len(year) > max_paper_per_acy:
                    max_paper_per_acy = len(year)
    max_paper_count = max(max_paper_per_tcy, max_paper_per_acy)
    max_year_count = max(max_year_per_tc, max_year_per_ac)
    max_cat_count = max(max_cat_per_t, max_cat_per_a)
    return (max_page_count, max_paper_count, max_year_count, max_cat_count)

def storeTeamPaper(team, refEL):
    global tcy2id
    category = refEL.find('xtradata/category').text
    year = refEL.find('bibentry/year').text
    aa = refEL.get('key')
    if tcy2id.has_key(team):
        categories = tcy2id[team]
        if categories.has_key(category):
            years = categories[category]
            if years.has_key(year):
                years.get(year).append(refEL.get('key'))
            else:
                years[year] = [refEL.get('key'),]
        else:
            categories[category] = {year: [refEL.get('key'),]}
    else:
        tcy2id[team] = {category:{year:[refEL.get('key'),]}}

# returns (author, lri_member)
def storeAuthorPaper(author, refEL):
    global acy2id
    author = unicodedata.normalize('NFD', unicode(author))
    canonical_author = author
    if canonical_authors.has_key(author):
        pass
    elif author2canonical_author.has_key(author):
        canonical_author = author2canonical_author.get(author)
    else:
        log("Missing author %s" % author.encode('utf-8'), 3)
        return
    if authors_LRI.has_key(canonical_author):
        category = refEL.find('xtradata/category').text
        year = refEL.find('bibentry/year').text
        if acy2id.has_key(canonical_author):
            categories = acy2id[canonical_author]
            if categories.has_key(category):
                years = categories[category]
                if years.has_key(year):
                    years.get(year).append(refEL.get('key'))
                else:
                    years[year] = [refEL.get('key'),]
            else:
                categories[category] = {year: [refEL.get('key'),]}
        else:
            acy2id[canonical_author] = {category:{year:[refEL.get('key'),]}}
        return (canonical_author, 1)
    else:
        return (canonical_author, 0)

################################################################################
# Build scene subtree that corresponds to browsing papers per team/categ./year
################################################################################
def buildTeamTree(outputParent, xc, yc, w, h):
    log("Building team tree", 1)
    region_el = ET.SubElement(outputParent, "region")
    region_el.set("x", str(int(xc)))
    region_el.set("y", str(int(yc)))
    region_el.set("w", str(int(w)))
    region_el.set("h", str(int(h)))
    region_el.set("levels", "1;2")
    region_el.set("id", "teams")
    region_el.set("title", "Teams")
    region_el.set("containedIn", "root")
    region_el.set("stroke", MAIN_LABEL_COLOR)
    region_el.set("tful", FADE_IN)
    region_el.set("tfll", FADE_IN)
    region_el.set("ttul", FADE_OUT)
    region_el.set("ttll", FADE_OUT)
    region_el.set("sensitive", "true")
    teams = tcy2id.keys()
    teams.sort()
    y = yc + CAT_REGION_HEIGHT * 2.5 * (len(teams)-1) / 2
    for team in teams:
        log("Processing team %s" % team, 2)
        # label
        object_el = ET.SubElement(region_el, "object")
        object_el.set('id', "teamLb-%s" % team.lower())
        object_el.set('type', "text")
        object_el.set('x', str(int(xc)))
        object_el.set('y', str(int(y)))
        object_el.set('scale', TEAM_LABEL_SCALE_FACTOR)
        object_el.text = team
        object_el.set('fill', MAIN_LABEL_COLOR)
        catRegID = "R-%s-categories" % (team)
        object_el.set('takesToRegion', catRegID)
        categories = tcy2id.get(team)
        layoutCategories(categories, catRegID, region_el.get('id'),\
                         "%s-categories" % team, xc, y, outputParent, "%s / Categories" % team)
        y -= 2.5 * CAT_REGION_HEIGHT

################################################################################
# Build scene subtree that corresponds to browsing papers per author/categ./year
################################################################################
# ('ref letter for horizontal positioning', next to its west (0) or east (2) bound,
#  'ref letter for vertical positioning', next to its north (1) or south (3) bound,
#  horizontal offset, vertical offset factors * dx or dy)
RPIF = [
# A
('fake'),
# B
('A', 2, 'A', 1, .5, 0),
# C
('B', 2, 'B', 1, .5, 0),
# D
('C', 2, 'C', 1, .5, 0),
# E
('A', 0, 'A', 3, 0, -1.2),
# F
('E', 0, 'E', 3, 0, -1.2),
# G
('F', 2, 'B', 3, .5, -1.2),
# H
('G', 2, 'G', 1, .5, 0.5),
# I
('D', 0, 'D', 3, 0, -1.0),
# J
('I', 2, 'D', 3, 0.5, -1.0),
# K
('I', 0, 'I', 3, 0, -1.0),
# L
('F', 0, 'G', 3, 0, -1.2),
# M
('L', 2, 'G', 3, .5, -1.2),
# N
('M', 2, 'H', 3, .5, -1.2),
# O
('N', 0, 'N', 3, 0, -1.2),
# P
('N', 2, 'K', 3, 0.5, -0.8),
# Q
('O', 2, 'P', 3, .5, -0.8),
# R
('L', 0, 'L', 3, 0, -1.2),
# S
('R', 2, 'M', 3, .5, -1.2),
# T
('S', 2, 'O', 3, .5, -0.8),
# V
('T', 2, 'T', 1, .5, 0),
# W
('Q', 2, 'P', 3, 0.5, -0.8),
# X
('W', 0, 'W', 3, 0, -0.8),
# Y
('X', 2, 'X', 1, 0.5, 0),
# Z
('X', 0, 'X', 3, 0, -0.8),
# fake
('Z', 0, 'Z', 0, 0, 0)
]

REGION_BOUNDS_IN_FLOW = {}

def buildAuthorTree(outputParent, xc, yc, w, h):
    log("Building author tree", 1)
    region_el = ET.SubElement(outputParent, "region")
    region_el.set("x", str(int(xc)))
    region_el.set("y", str(int(yc)))
    region_el.set("w", str(int(w)))
    region_el.set("h", str(int(h)))
    region_el.set("levels", "1")
    region_el.set("id", "authors")
    region_el.set("title", "Authors")
    region_el.set("containedIn", "root")
    region_el.set("stroke", MAIN_LABEL_COLOR)
    region_el.set("tful", FADE_IN)
    region_el.set("tfll", FADE_IN)
    region_el.set("ttul", FADE_OUT)
    region_el.set("ttll", FADE_OUT)
    region_el.set("sensitive", "true")
    authors = authors_LRI.keys()
    authors.remove("XXX")
    authors.remove("xxx")
    authors.sort(authorSorter)
    lastFirstChar = ""
    # [('A', [authorID1, ...]), ('B', [authorIDN, ...]), ...]
    authors_by_letter = []
    for authorID in authors:
        authorName = canonical_authors.get(authorID)
        if len(authorName[0]) == 0 and len(authorName[1]) == 0:
            continue
        firstChar = authorName[1][0].lower()
        if firstChar == lastFirstChar:
            authors_by_letter[-1][1].append(authorID)
        else:
            authors_by_letter.append((firstChar.upper(), [authorID]))
            lastFirstChar = firstChar
    colRow = matrixLayout(len(authors))
    # compute distance between names as if they were to be laid out in a single square matrix
    dx = int(w / (1.6 * (colRow[0])))
    dy = int(h / (1.6 * (colRow[1])))
    mx = int(xc - w/2 + dx)
    my = int(yc + h/2 - dy/2)
    li = 0
    pi = 0
    # gray = False
    for letter in authors_by_letter:
        nbColRowForL = matrixLayout(len(letter[1]))
        nbColForL = nbColRowForL[0]
        nbRowForL = nbColRowForL[1]
        ai = -1
        x = mx
        west = mx
        north = my
        lregion_el = ET.SubElement(outputParent, "region")
        lregion_el.set("levels", "2")
        lregion_el.set("id", "R-letter-%s" % letter[0])
        lregion_el.set("title", "Authors - %s" % letter[0])
        lregion_el.set("containedIn", "authors")
        lregion_el.set("stroke", "white")
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
                    log(("Processing author %s" % authorID).encode('utf-8'), 4)
                    fnln = canonical_authors.get(authorID)
                    # surname on a first line
                    objectln_el = ET.SubElement(lregion_el, "object")
                    # take the rank of the author in the sorted list of authors
                    # as a basis for computing a unique ID, so as to avoid having
                    # to deal with utf-8 char escaping in XML ID attributes
                    authorID4id = "author%s" % authors.index(authorID)
                    objectln_el.set('id', "authorLbLN-%s" % authorID4id)
                    objectln_el.set('type', "text")
                    objectln_el.set('x', str(int(x)))
                    objectln_el.set('y', str(int(y)))
                    objectln_el.set('scale', AUTHOR_LABEL_SCALE_FACTOR)
                    objectln_el.text = unicode(fnln[1], 'utf-8')
                    # firstname on a second line
                    objectfn_el = ET.SubElement(lregion_el, "object")
                    objectfn_el.set('id', "authorLbFN-%s" % authorID4id)
                    objectfn_el.set('type', "text")
                    objectfn_el.set('x', str(int(x)))
                    objectfn_el.set('y', str(int(y+dy/8)))
                    objectfn_el.set('scale', AUTHOR_LABEL_SCALE_FACTOR)
                    objectfn_el.text = unicode(fnln[0], 'utf-8')
                    # team on yet another line
                    objecttm_el = ET.SubElement(lregion_el, "object")
                    objecttm_el.set('id', "authorTeamLb-%s" % authorID4id)
                    objecttm_el.set('type', "text")
                    objecttm_el.set('x', str(int(x)))
                    objecttm_el.set('y', str(int(y-dy/8)))
                    objecttm_el.set('scale', AUTHOR_TEAM_LABEL_SCALE_FACTOR)
                    objecttm_el.set('takesToObject', "teamLb-%s" % author2team[authorID].lower())
                    objecttm_el.text = unicode("(%s)" % author2team[authorID], 'utf-8')
                    # papers
                    categories = acy2id.get(authorID)
                    if categories is None:
                        log("Warning: no paper for author %s" % authorID.encode('utf-8'), 2)
                    else:
                        catRegID = "R-%s-categories" % authorID4id
                        objectfn_el.set('takesToRegion', catRegID)
                        objectln_el.set('takesToRegion', catRegID)
                        layoutCategories(categories, catRegID, lregion_el.get('id'),\
                                         "%s-categories" % authorID4id, x, y, outputParent, "%s / Categories" % authorID4id)
                    if pi and pi % 50 == 0:
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
        object_el.set('id', "letterLb-%s" % letter[0])
        object_el.set('type', "text")
        object_el.set('x', str(rcx))
        object_el.set('y', str(int(rcy-0.5*dy)))
        object_el.set('scale', TEAM_LABEL_SCALE_FACTOR)
        object_el.set('takesToRegion', "R-letter-%s" % letter[0])
        object_el.set('sensitive', "false")
        object_el.text = letter[0]
        object_el = ET.SubElement(region_el, "object")
        object_el.set('id', "letterBB-%s" % letter[0])
        object_el.set('type', "rect")
        object_el.set("x", str(rcx))
        object_el.set("y", str(rcy))
        object_el.set("w", str(rcw))
        object_el.set("h", str(rch))
        object_el.set('stroke', "#AAA")
        object_el.set('sensitive', "false")
        REGION_BOUNDS_IN_FLOW[letter[0]] = (west, north, east, south)
        li += 1
        RPIFe = RPIF[li]
        mx = REGION_BOUNDS_IN_FLOW.get(RPIFe[0])[RPIFe[1]] + RPIFe[4] * dx
        my = REGION_BOUNDS_IN_FLOW.get(RPIFe[2])[RPIFe[3]] + RPIFe[5] * dy

################################################################################
# Build scene subtree that corresponds to categories
# (contains papers organized by year)
################################################################################
def layoutCategories(categories, regionID, parentRegionID, idPrefix, xc, yc,\
         			 outputParent, regionTitle):
    region_el = ET.SubElement(outputParent, "region")
    region_el.set("x", str(int(xc)))
    region_el.set("y", str(int(yc)))
    region_el.set("w", str(CAT_REGION_WIDTH))
    region_el.set("h", str(CAT_REGION_HEIGHT))
    region_el.set("levels", "3")
    region_el.set("id", regionID)
    region_el.set("title", regionTitle)
    region_el.set("containedIn", parentRegionID)
    region_el.set("stroke", getStroke("green"))
    region_el.set("tful", FADE_IN)
    region_el.set("tfll", FADE_IN)
    region_el.set("ttul", FADE_OUT)
    region_el.set("ttll", FADE_OUT)
    region_el.set("sensitive", "false")
    categoryKeys = categories.keys()
    categoryKeys.sort()
    x = int(xc-CAT_REGION_WIDTH/2.2)
    y = yc + YEAR_REGION_HEIGHT * len(categoryKeys) / 2
    for ck in categoryKeys:
        object_el = ET.SubElement(region_el, "object")
        object_el.set('id', "catLb-%s%s" % (idPrefix, ck.replace(" ", "")))
        object_el.set('type', "text")
        object_el.set('anchor', 'start')
        object_el.set('x', str(x))
        object_el.set('y', str(int(y)))
        object_el.set('scale', CATEGORY_LABEL_SCALE_FACTOR)
        object_el.text = "%s (%s)" % (CATEGORIES_LRI_SEPTEMBRE_2008[ck.split(" ")[-1]], countItemsPerCat(categories[ck]))
        object_el.set('fill', MAIN_LABEL_COLOR)
        catRegID = "R-%s-%s" % (idPrefix, ck.replace(" ", ""))
        object_el.set('takesToRegion', catRegID)
        layoutYears(categories[ck], catRegID, region_el.get('id'),\
                    "%s-%s-years" % (idPrefix, ck.replace(" ", "")), int(x+YEAR_REGION_WIDTH), y,\
                    outputParent, "%s / %s / Years" % (idPrefix, ck))
        y -= 1.1 * YEAR_REGION_HEIGHT
        
################################################################################
# Build scene subtree that corresponds to years
################################################################################
def layoutYears(years, regionID, parentRegionID, idPrefix, xc, yc,\
         		outputParent, regionTitle):
    region_el = ET.SubElement(outputParent, "region")
    region_el.set("x", str(int(xc)))
    region_el.set("y", str(int(yc)))
    region_el.set("w", str(YEAR_REGION_WIDTH))
    region_el.set("h", str(YEAR_REGION_HEIGHT))
    region_el.set("levels", "4")
    region_el.set("id", regionID)
    region_el.set("title", regionTitle)
    region_el.set("containedIn", parentRegionID)
    region_el.set("stroke", getStroke("orange"))
    region_el.set("tful", FADE_IN)
    region_el.set("tfll", FADE_IN)
    region_el.set("ttul", FADE_OUT)
    region_el.set("ttll", FADE_OUT)
    region_el.set("sensitive", "false")
    yearKeys = years.keys()
    yearKeys.sort()
    x = xc - (META_REGION_WIDTH * 1.1 * (len(yearKeys)-1)) / 2
    for yk in yearKeys:
        object_el = ET.SubElement(region_el, "object")
        object_el.set('id', "yearLb-%s%s" % (idPrefix, yk))
        object_el.set('type', "text")
        object_el.set('x', str(int(x)))
        object_el.set('y', str(int(yc)))
        object_el.set('scale', YEAR_LABEL_SCALE_FACTOR)
        object_el.text = yk
        object_el.set('fill', MAIN_LABEL_COLOR)
        yearRegID = "R-%s-%s" % (idPrefix, yk)
        object_el.set('takesToRegion', yearRegID)
        layoutPapers(years[yk], yearRegID, region_el.get('id'),\
                     "%s-%s-papers" % (idPrefix, yk), x, yc, outputParent, "%s / %s / Papers" % (idPrefix, yk))
        x += 1.1 * META_REGION_WIDTH

################################################################################
# Build scene subtree that corresponds to papers
################################################################################
def layoutPapers(paperIDs, regionID, parentRegionID, idPrefix, xc, yc,\
         		 outputParent, regionTitle):
    region_el = ET.SubElement(outputParent, "region")
    region_el.set("x", str(int(xc)))
    region_el.set("y", str(int(yc)))
    region_el.set("w", str(META_REGION_WIDTH))
    region_el.set("h", str(META_REGION_HEIGHT))
    region_el.set("levels", "5")
    region_el.set("id", regionID)
    region_el.set("title", regionTitle)
    region_el.set("containedIn", parentRegionID)
    region_el.set("stroke", getStroke("blue"))
    region_el.set("tful", FADE_IN)
    region_el.set("tfll", FADE_IN)
    region_el.set("ttul", FADE_OUT)
    region_el.set("ttll", FADE_OUT)
    region_el.set("sensitive", "false")
    colRow = matrixLayout(len(paperIDs))
    nbCol = colRow[0]
    nbRow = colRow[1]
    x = int(xc-META_REGION_WIDTH/2.2)
    y = yc + PAPER_REGION_HEIGHT * (len(paperIDs) / 2 + 1)
    paperIDs.sort(paperSorter)
    for paperID in paperIDs:
        titleEL = getTitle(paperID)
        if titleEL is None:
            log("Error: could not find a title for paper %s" % paperID)
        else:
            object_el = ET.SubElement(region_el, "object")
            object_el.set('id', "titleLb%s%s" % (idPrefix, paperID.translate(id_trans)))
            object_el.set('type', "text")
            object_el.set('x', str(int(x)))
            object_el.set('y', str(int(y)))
            object_el.set('anchor', 'start')
            object_el.set('scale', TITLE_LABEL_SCALE_FACTOR)
            object_el.text = titleEL.text
            authors = getAuthors(paperID)
            sobject_el = None
            if authors is not None:
                sobject_el = ET.SubElement(region_el, "object")
                sobject_el.set('id', "titleAuthLb%s%s" % (idPrefix, paperID.translate(id_trans)))
                sobject_el.set('type', "text")
                sobject_el.set('x', str(int(x)))
                sobject_el.set('y', str(int(y-PAPER_REGION_HEIGHT * .45)))
                sobject_el.set('anchor', 'start')
                sobject_el.set('scale', SUBTITLE_LABEL_SCALE_FACTOR)
                sobject_el.text = unicode(authors, 'utf-8')
            paperRegID = "R-%s-%s" % (idPrefix, paperID.translate(id_trans))
            # Layout region slightly on the left w.r.t title to avoid
            # seeing one or two huge chars from the paper's title for a few moments.
            # This happens when the system takes some time processing all PDF page load requests,
            # which delays the processing of the upper level's unload requests.
            firstPageID = layoutPages(paperID, paperRegID, region_el.get('id'),\
                                      "%s-%s-pages" % (idPrefix, paperID.translate(id_trans)), x-PAPER_REGION_WIDTH, y,\
                                      outputParent, "%s / %s / Pages" % (idPrefix, paperID.translate(id_trans)))
            if len(firstPageID):
                # if PDF is available, clicking on title takes to first page of paper
                object_el.set('takesToObject', firstPageID)
                object_el.set('fill', MAIN_LABEL_COLOR)
                if sobject_el is not None:
                    sobject_el.set('fill', MAIN_LABEL_COLOR)
            else:
                # if not, takes to the region containing all (missing) pages,
                # which contains a msg indicating that the PDF is missing
                object_el.set('takesToRegion', paperRegID)
                object_el.set('fill', MISSING_PAPER_TITLE_COLOR)
                if sobject_el is not None:
                    sobject_el.set('fill', MISSING_PAPER_TITLE_COLOR)
        y -= 1.1 * PAPER_REGION_HEIGHT
    
################################################################################
# Build scene subtree that corresponds to pages
################################################################################
def layoutPages(paperID, regionID, parentRegionID, idPrefix, xc, yc,\
         		outputParent, regionTitle):
    paperDir = "%s/lri4z/%s" % (SRC_DIR, paperID)
    if os.path.exists(paperDir):
        PNGfiles = os.listdir(paperDir)
        PNGfiles.sort(pageSorter)
        dx = int(PPW * 1.1)
        if len(PNGfiles) <= 10:
            region_el = ET.SubElement(outputParent, "region")
            region_el.set("x", str(int(xc)))
            region_el.set("y", str(int(yc)))
            region_el.set("w", str(PAPER_REGION_WIDTH))
            region_el.set("h", str(PAPER_REGION_HEIGHT))
            region_el.set("levels", "6")
            region_el.set("id", regionID)
            region_el.set("title", regionTitle)
            region_el.set("containedIn", parentRegionID)
            region_el.set("stroke", getStroke("red"))
            region_el.set("tful", FADE_IN)
            region_el.set("tfll", FADE_IN)
            region_el.set("ttul", FADE_OUT)
            region_el.set("ttll", FADE_OUT)
            region_el.set("sensitive", "false")
            x = xc - PPW * 1.1 * len(PNGfiles) / 2
            i = 0
            for PNGfile in PNGfiles:
                i += 1
                pageSrc = "%s/%s" % (paperID, PNGfile)
                object_el = ET.SubElement(region_el, "object")
                object_el.set('id', "%s_p%s_%s" % (idPrefix, i, len(PNGfiles)))
                object_el.set('type', "image")
                object_el.set('x', str(int(x)))
                object_el.set('y', str(int(yc)))
                object_el.set('w', str(PPW))
                object_el.set('h', str(PPH))
                object_el.set('src', pageSrc)
                object_el.set('stroke', "#AAA")
                x += dx
        else:
            colRow = matrixLayout(len(PNGfiles))
            nbCol = colRow[0]
            nbRow = colRow[1]
            yi = -1
            dx = int(PPW * 1.1)
            xo = int(xc-PAPER_REGION_WIDTH/2+dx/2)
            dy = int(PPH * 1.1)
            y = int(yc+PAPER_REGION_HEIGHT/2-dy/2)
            for row in range(nbRow):
                x = xo
                y -= dy
                region_el = ET.SubElement(outputParent, "region")
                region_el.set("x", str(int(xo+dx*(nbCol+1)/2)))
                region_el.set("y", str(int(y)))
                region_el.set("w", str(dx * (nbCol)))
                region_el.set("h", str(PPH))
                region_el.set("levels", "6")
                region_el.set("id", "%s-line%s" % (regionID, yi))
                region_el.set("title", regionTitle)
                region_el.set("containedIn", parentRegionID)
                region_el.set("stroke", getStroke("orange"))
                region_el.set("tful", FADE_IN)
                region_el.set("tfll", FADE_IN)
                region_el.set("ttul", FADE_OUT)
                region_el.set("ttll", FADE_OUT)
                region_el.set("sensitive", "false")
                for col in range(nbCol):
                    x += dx
                    yi += 1
                    if yi < len(PNGfiles):
                        pageSrc = "%s/%s" % (paperID, PNGfiles[yi])
                        object_el = ET.SubElement(region_el, "object")
                        object_el.set('id', "%s_p%s_%s" % (idPrefix, yi+1, len(PNGfiles)))
                        object_el.set('type', "image")
                        object_el.set('x', str(int(x)))
                        object_el.set('y', str(int(y)))
                        object_el.set('w', str(PPW))
                        object_el.set('h', str(PPH))
                        object_el.set('src', pageSrc)
                        object_el.set('stroke', "#AAA")
                    else:
                        break
        return "%s_p1_%s" % (idPrefix, len(PNGfiles))
    else:
        log("Warning: could not find a PNG directory for paper %s" % paperID, 4)
        region_el = ET.SubElement(outputParent, "region")
        region_el.set("x", str(int(xc)))
        region_el.set("y", str(int(yc)))
        region_el.set("w", str(PAPER_REGION_WIDTH))
        region_el.set("h", str(PAPER_REGION_HEIGHT))
        region_el.set("levels", "6")
        region_el.set("id", regionID)
        region_el.set("title", regionTitle)
        region_el.set("containedIn", parentRegionID)
        region_el.set("stroke", getStroke("red"))
        region_el.set("tful", FADE_IN)
        region_el.set("tfll", FADE_IN)
        region_el.set("ttul", FADE_OUT)
        region_el.set("ttll", FADE_OUT)
        region_el.set("sensitive", "false")
        object_el = ET.SubElement(region_el, "object")
        object_el.set('id', "%s_NOPDF" % idPrefix)
        object_el.set('type', "text")
        object_el.set('x', str(int(xc)))
        object_el.set('y', str(int(yc)))
        object_el.set('scale', NOPDF_LABEL_SCALE_FACTOR)
        object_el.text = "PDF not available"
        object_el.set('fill', MISSING_PAPER_COLOR)
        return ""

#################################################################################
## split title, first at <sep>, then for each split line when it is > 50 chars
#################################################################################
#def splitTitleInLines(title, sep, keepSep):
#    sepI = title.find(sep)
#    lines = [title,]
#    if sepI != -1:
#        lines = title.split(sep)
#        if keepSep:
#            for i in range(len(lines)-1):
#                lines[i] = "%s%s" % (lines[i], sep)
#    res = []
#    for line in lines:
#        if len(line) > 50:
#            splitI = line.find(" ", len(line)/2)
#            if splitI != -1:
#                res.append(line[:splitI])
#                res.append(line[splitI+1:])
#        else:
#            res.append(line)
#    return res

################################################################################
# Return the title of a paper
################################################################################
def getTitle(paperID):
    j = 0
    titleEL = None
    while titleEL is None and j < len(TITLE_ELEMS):
        titleEL = id2paper[paperID].find(TITLE_ELEMS[j])
        if (j > 0 and titleEL is None):
            log("Warning: could not find a %s for paper %s, attempting fallback" % (TITLE_ELEMS[j], paperID), 2)
        j += 1
    return titleEL
    
################################################################################
# Return the authors of a paper
################################################################################
def getAuthors(paperID):
    authors = ""
    if paper2authors.has_key(paperID):
        for author in paper2authors[paperID]:
            if author is not None:
                if authors_LRI.has_key(author[0]) and canonical_authors.has_key(author[0]):
                    # take canonical names for people from LRI
                    fnln = canonical_authors[author[0]]
                    authors += "%s %s, " % (fnln[0], fnln[1])
                else:
                    # use the only information we have from bibtex for other people
                    authors += "%s, " % author[0].encode('utf-8')
        if len(authors) > 0:
            authors = authors[:-2]
        else:
            authors = None
    else:
        authors = None
        log("Error, could not find any author for %s" % paperID, 1)
    return authors

################################################################################
# Get the number of papers for a given category (for a given team/author)
################################################################################
def countItemsPerCat(years):
    count = 0
    for year in years.values():
        count += len(year)
    return count

################################################################################
# Author sorter
################################################################################
def authorSorter(a1, a2):
    a1l = canonical_authors[a1]
    a2l = canonical_authors[a2]
    if  a1l[1].lower() < a2l[1].lower():
        return -1
    elif a1l[1].lower() > a2l[1].lower():
        return 1
    else:
        if  a1l[0].lower() < a2l[0].lower():
            return -1
        elif a1l[0].lower() > a2l[0].lower():
            return 1
        else:
            return 0
        
################################################################################
# Page sorter
################################################################################
def pageSorter(a1, a2):
    a1l = int(a1.split("_p")[-1][:-4])
    a2l = int(a2.split("_p")[-1][:-4])
    if  a1l < a2l:
        return -1
    elif a1l > a2l:
        return 1
    else:
        return 0

################################################################################
# Paper sorter
################################################################################
def paperSorter(a1, a2):
    a1t = getTitle(a1)
    a2t = getTitle(a2)
    if a1t is None:
        a1t = ""
    else:
        a1t = a1t.text.lower()
    if a2t is None:
        a2t = ""
    else:
        a2t = a2t.text.lower()
    if  a1t < a2t:
        return -1
    elif a1t > a2t:
        return 1
    else:
        return 0

################################################################################
# fix inconsistencies in team names
################################################################################
def normalizeTeams(teams):
    teams = teams.lower()
    for tf in TEAM_FIXES:
        if teams.find(tf[0]) != -1:
            teams = teams.replace(tf[0], tf[1])
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
# compute smallest almost square matrix that can accomodate a given number
# of items
################################################################################
def getStroke(color):
    if DEBUG_SCENE:
        return color
    else:
        return "white"

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
        if len(sys.argv) > 3:
            DEBUG_SCENE = int(sys.argv[3])
else:
    sys.exit(0)

METADATA_FILE = "%s/%s" % (SRC_DIR, "bibdata.xml")
AUTHORS_FILE = "%s/%s" % (SRC_DIR, "authors.csv")
XML_SCENE_FILE = "%s/%s" % (SRC_DIR, "lri4z/%s" % XML_SCENE_FILE_NAME)
buildScene(METADATA_FILE, AUTHORS_FILE, XML_SCENE_FILE)
