#!/usr/bin/python

# -*- coding: utf-8 -*-

import sys, os
import random
import copy

WORDS_08 = ["abducted","abounded","absorbed","accented",\
			"accepted","accorded","achieved","acquired",\
			"addicted","adjoined","adjusted","admitted",\
			"advanced","affected","affirmed","afforded",\
			"agitated","agonized","alighted"]

WORDS_12 = ["mainstreamed","manufactured","marginalized","masculinized",\
			"masterminded","materialized","mathematized","memorialized",\
			"micromanaged","multilayered","miniaturized","misaddressed",\
			"misassembled","misconceived","misconducted"]

V = ["O", "T", "O", "T"]
S = [[4,8,6,2], [6,2,8,4], [8,4,2,6], [2,6,4,8]]

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
