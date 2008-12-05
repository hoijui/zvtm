#!/usr/bin/python

# -*- coding: utf-8 -*-

import sys, os
import random
import copy

# http://www.a2zwordfinder.com/cgi-bin/scrabble.cgi?Letters=&Pattern=________&MatchType=Exactly&MinLetters=8&SortBy=Alpha&SearchType=Scrabble
WORDS_08 = ["abducted","abounded","absorbed","accented",\
			"accepted","accorded","achieved","acquired",\
			"addicted","adjoined","adjusted","admitted",\
			"advanced","affected","affirmed","afforded",\
			"agitated","agonized","alighted","arboured",\
			"analyzed","anchored","animated","answered"]

# data obtained from http://wordnavigator.com/by-length/12m/
WORDS_12 = ["mainstreamed","manufactured","marginalized","masculinized",\
			"masterminded","materialized","mathematized","memorialized",\
			"micromanaged","multilayered","miniaturized","misaddressed",\
			"misassembled","misconceived","misconducted","misevaluated",\
			"misprogramed","mousetrapped","multifaceted","multicolored",\
			"matriculated","meanspirited","merchandized","microcracked"]

# 4a
#V = ["O", "T", "O", "T"]
#S = [[4,8,6,2], [6,2,8,4], [8,4,2,6], [2,6,4,8]]
# 4b
V = ["O", "O"]
S = [[4,8,6,2], [8,4,2,6]]

###############################################################################
def generateTrial(rank, visibility, word_pool):
	wp = copy.copy(word_pool)
	words = []
	while len(wp) > 0:
		word = random.choice(wp)
		words.append(word)
		wp.remove(word)
	print "%s;%s;%s;%s" % (visibility, rank, len(words[0]), ",".join(words))

###############################################################################
i = 0

for s in S:
	v = V[i]
	for r in s:
		generateTrial(r, v, WORDS_08)
	for r in s:
		generateTrial(r, v, WORDS_12)
	i += 1
