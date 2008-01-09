#!/usr/bin/python
# -*- coding: ISO-8859-1 -*-
# $Id$

# Usage:
#    metadataProcessor.py <src_dir> <stylesheet>

import os, sys
import re, string
import csv
import elementtree.ElementTree as ET

without_diacritics = "aaaaaaaceeeeiiiinoooooouuuuy"
with_diacritics  =   "àáâãäåæçèéêëìíîïñòóôõöøùúûüý"
diacritics_trans = string.maketrans(with_diacritics, without_diacritics)

DECENT2UCHAR = [("&#225;", "á"), ("&#227;", "ã"), ("&#228;", "ä"), ("&#230;", "ae"), ("&#231;", "ç"), ("&#232;", "è"), ("&#233;", "é"), ("&#235;", "ë"), ("&#243;", "ó"), ("&#246;", "ö"), ("&#248;", "ø"), ("&#252;", "ü")]
DECENT2ACHAR = [("&#225;", "a"), ("&#227;", "a"), ("&#228;", "a"), ("&#230;", "ae"), ("&#231;", "c"), ("&#232;", "e"), ("&#233;", "e"), ("&#235;", "e"), ("&#243;", "o"), ("&#246;", "o"), ("&#248;", "o"), ("&#252;", "u")]


TRACE_LEVEL = 1

PROBLEMATIC_NAMES = [("Begole, James ", "Begole, James"), (u"Hornbæk, Kasper", "Hornbaek, Kasper"), ("del R. Mill", "Millan, Jose del R.")]

# author ID to (canonicalname, [synonyms], [papers], [coauthors])
id2author = {}
# all author names (including synonyms) to corresponding ID - key is canonical name without diacritics
author2id = {}

# all keyword atoms to ([keyword ids], [count per keyword], total count))
kwa2kws = {}
# all keywords to associated (keyword id, [papers])
kw2papers = {}
# keywords that got fixed by Michel
kw2fixed = {}
# paper DOI to companion video
doi2video = {}

################################################################################
# Author metadata generated @ insitu by Michel
################################################################################
def processAuthors(f):
    csvFile = open(f, 'r')
    csvreader = csv.reader(csvFile, dialect="excel", delimiter=';', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    for row in csvreader:
        authorCanonicalName = resolveCharacterEntities(row[1], DECENT2ACHAR)
        uauthorCanonicalName = resolveCharacterEntities(row[1], DECENT2UCHAR)
        if int(row[2]) > 0:
            id2author[row[0]] = (uauthorCanonicalName, processSynonyms(row[3:-1], row[0]))
        else:
            id2author[row[0]] = (uauthorCanonicalName, [])
        author2id[authorCanonicalName] = row[0]
    nbAuthors = len(id2author.keys())
    log("Found %s authors, and %s synonyms" % (nbAuthors, len(author2id.keys())-nbAuthors))
    csvFile.close()
    
def processSynonyms(syns, authorID):
    res = []
    for syn in syns:
        resolvedName = resolveCharacterEntities(syn, DECENT2UCHAR)
        res.append(resolvedName)
        author2id[resolvedName] = authorID
    return res
    
def resolveCharacterEntities(s, table):
    for c in table:
        s = s.replace(c[0], c[1])
    return s

def processPaperAuthors(f):
    csvFile = open(f, 'r')
    csvreader = csv.reader(csvFile, dialect="excel", delimiter=';', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    for row in csvreader:
        id2author[row[0]] = id2author[row[0]] + (row[3:],)
    csvFile.close()

def processCoauthors(f):
    csvFile = open(f, 'r')
    csvreader = csv.reader(csvFile, dialect="excel", delimiter=';', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    for row in csvreader:
        ida = row[0]
        nbca = int(row[2])
        i = 3
        res = []
        while (i < len(row)):
            res.append(processCoauthor(row[i:i+4]))
            i += 4
        id2author[ida] = id2author[ida] + (res,)
    csvFile.close()
    
def processCoauthor(cadata):
    idca = cadata[0]
    papers = cadata[3].split(",")
    return (idca, papers)
    
def xmlizeAuthors(authorsEL):
    for ida in id2author.keys():
        authordata = id2author[ida]
        authorEL = ET.SubElement(authorsEL, "author")
        authorEL.set("id", ida)
        cnEL = ET.SubElement(authorEL, "canonicalName")
        cnEL.text = authordata[0]
        for syn in authordata[1]:
            synEL = ET.SubElement(authorEL, "otherName")
            synEL.text = syn
        for paper in authordata[2]:
            paperEL = ET.SubElement(authorEL, "article")
            paperEL.set('idref', paper)
        if (len(authordata) > 3):
            for coauthor in authordata[3]:
                coauthorEL = ET.SubElement(authorEL, "coauthor")
                coauthorEL.set('idref', coauthor[0])
                for paper in coauthor[1]:
                    paperEL = ET.SubElement(coauthorEL, "coauthoredArticle")
                    paperEL.set('idref', paper)

################################################################################
# Keyword metadata generated @ insitu by Michel
################################################################################
def processKeywords(f1, f2, f3):
    nextID = 0
    csvFile = open(f2, 'r')
    csvreader = csv.reader(csvFile, dialect="excel", delimiter=';', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    for row in csvreader:
        kw2papers[row[0]] = ("kw%04d" % nextID, row[2:])
        nextID += 1
    csvFile.close()
    csvFile = open(f1, 'r')
    csvreader = csv.reader(csvFile, dialect="excel", delimiter=';', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    for row in csvreader:
        kws = []
        kwscard = []
        i = 2
        while i < len(row):
            kws.append(kw2papers[row[i+1]][0])
            kwscard.append(row[i])
            i += 2
        kwa2kws[row[0]] = (kws, kwscard, row[1])
        nextID += 1
    csvFile.close()
    csvFile = open(f3, 'r')
    csvreader = csv.reader(csvFile, dialect="excel", delimiter=';', quotechar='"', quoting=csv.QUOTE_MINIMAL)
    for row in csvreader:
        kw2fixed[row[0]] = row[1]
    csvFile.close()

def xmlizeKeywords(allkeywordsEL):
    keywordsEL = ET.SubElement(allkeywordsEL, "keywords")
    for kw in kw2papers.keys():
        kwdata = kw2papers[kw]
        kwEL = ET.SubElement(keywordsEL, "keyword")
        kwEL.set('id', kwdata[0])
        kwEL.text = kw
        kwEL.set('papers', ",".join(kwdata[1]))
    atomsEL = ET.SubElement(allkeywordsEL, "keywordAtoms")
    for atom in kwa2kws.keys():
        atomdata = kwa2kws[atom]
        atomEL = ET.SubElement(atomsEL, "keywordAtom")
        atomEL.set('idrefs', ",".join(atomdata[0]))
        atomEL.set('nbopkwe', ",".join(atomdata[1]))
        atomEL.set('totalnbo', atomdata[2])
        atomEL.text = atom
        
################################################################################
# Videos metadata generated @ insitu by Michel
################################################################################
def processVideos(f):
    csvFile = open(f, 'r')
    csvreader = csv.reader(csvFile, delimiter="\t", quoting=csv.QUOTE_MINIMAL)
    for row in csvreader:
        doi2video[row[3]] = row[4]
    csvFile.close()
    
################################################################################
# Metadata from ACM DL
################################################################################
def processProceedingsMetadata(directory, proceedingsEL):
    for f in os.listdir(directory):
        f_abs_path = "%s/%s" % (directory, f)
        if os.path.isdir(f_abs_path):
            processProceedingsMetadata(f_abs_path, proceedingsEL)
        elif f == "metadata_nocdata.xml":
            processDLMetadata(f_abs_path, proceedingsEL)

def processDLMetadata(f, proceedingsEL):
    log("Processing %s" % f, 1)
    xmlFile = open(f, 'r')
    DLmetadata = ET.parse(xmlFile)
    proceedingEL = ET.SubElement(proceedingsEL, "proceedings")
    year = DLmetadata.getroot().get('year')
    proceedingEL.set("year", year)
    city = DLmetadata.find("//city").text
    state = DLmetadata.find("//state").text
    country = DLmetadata.find("//country").text
    location = ""
    if city:
        location += city
    if state:
        if city:
            location += ", %s" % state
        else:
            location = state
    if country:
        if country.lower().startswith("united states"):
            country = "USA"
        location += ", %s" % country
    proceedingEL.set("location", location)
    proceedingEL.append(DLmetadata.find("//chairs"))
    for article in DLmetadata.findall("//article"):
        aln = article.find("authors/author/last_name").text.encode("iso-8859-1").translate(diacritics_trans).lower().replace(" ", "")
        if aln == "delr.millan":
            aln = "millan"
        article.set('id', "%s#p%s-%s" % (year, article.findtext("firstPage"), aln))
        proceedingEL.append(article)
        # get rid of escaped <p></p> in <par>
        pars = article.findall(".//abstract/par")
        for par in pars:
            if par.text and par.text.startswith("<p>") and par.text.startswith("<p>"):
                par.text = par.text[3:-4]
        # add keyword id for each keyword expression
        kws = article.findall(".//kw")
        for kw in kws:
            if kw.text:
                kwdata = kw2papers.get(kw.text)
                if kwdata:
                    kw.set('idref', kwdata[0])
                elif kw2fixed[kw.text]:
                    kwdata = kw2papers.get(kw2fixed[kw.text])
                    kw.set('idref', kwdata[0]) 
                else:
                    log("Error: could not find a match for '%s' in paper %s" % (kw.text, article.get('id')), 0)
        # add author id for each author
        authors = article.findall(".//author")
        for author in authors:
            first_name = author.findtext("first_name")
            middle_name = author.findtext("middle_name")
            last_name = author.findtext("last_name")
            suffix = author.findtext("suffix")
            name = "%s, %s" % (last_name, first_name)
            if middle_name:
                name += " %s" % (middle_name)
            if suffix:
                name += " %s" % (suffix)
            name = unicode(name)
            isProblematic = 0
            for pn in PROBLEMATIC_NAMES:
                if name.startswith(pn[0]):
                    name = pn[1]
                    isProblematic = 1
                    break
            if not isProblematic:
                name = name.encode('iso-8859-1').translate(diacritics_trans)
            ida = author2id.get(name)
            if not ida:
                log("Error: could not find \"%s\" in list of authors" % name)
            else:
                author.set('idref', ida)
        # add companion video information
        doi = article.get("doi")
        if doi and doi2video.has_key(doi):
            article.set("video", doi2video.get(doi))
    xmlFile.close()

################################################################################
# XML serialization
################################################################################
def serialize(treeRoot, osf):
    log("Writing %s" % osf)
    tree = ET.ElementTree(treeRoot)
    tree.write(osf, encoding='utf-8')

################################################################################
# Trace exec on std output
################################################################################
def log(msg, level=0):
    if level <= TRACE_LEVEL:
        print msg

################################################################################
# main
################################################################################
if len(sys.argv) > 8:
    SRC_DIR = os.path.realpath(sys.argv[1])
    AUTHOR_PATH = os.path.realpath(sys.argv[2])
    COAUTHORS_PATH = os.path.realpath(sys.argv[3])
    PAPERS_BY_AUTHOR_PATH = os.path.realpath(sys.argv[4])
    KWA2KWS_PATH = os.path.realpath(sys.argv[5])
    KW2_PAPER_PATH = os.path.realpath(sys.argv[6])
    KW2FIXEDKW = os.path.realpath(sys.argv[7])
    VIDEOS_PATH = os.path.realpath(sys.argv[8])
    if len(sys.argv) > 9:
        TRACE_LEVEL = int(sys.argv[9])
else:
    sys.exit(0)

rootEL = ET.Element("UISTmetadata")
authorsEL = ET.SubElement(rootEL, "allAuthors")
keywordsEL = ET.SubElement(rootEL, "allKeywords")
proceedingsEL = ET.SubElement(rootEL, "allProceedings")
processAuthors(AUTHOR_PATH)
processPaperAuthors(PAPERS_BY_AUTHOR_PATH)
processCoauthors(COAUTHORS_PATH)
xmlizeAuthors(authorsEL)
processKeywords(KWA2KWS_PATH, KW2_PAPER_PATH, KW2FIXEDKW)
xmlizeKeywords(keywordsEL)
processVideos(VIDEOS_PATH)
processProceedingsMetadata(SRC_DIR, proceedingsEL)

# serialize the tree
XML_OUTPUT_FILE = "%s/%s" % (SRC_DIR, "UISTmetadata.xml")
serialize(rootEL, XML_OUTPUT_FILE)
