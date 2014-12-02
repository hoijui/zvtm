#!/usr/bin/env python
# -*- coding: UTF-8 -*-

#
# Simple example which uses a pool of workers to carry out some tasks.
#
# Notice that the results will probably not come out of the output
# queue in the same in the same order as the corresponding tasks were
# put on the input queue.  If it is important to get the results back
# in the original order then consider using `Pool.map()` or
# `Pool.imap()` (which will save on the amount of code needed anyway).
#
# Copyright (c) 2006-2008, R Oudkerk
# All rights reserved.
#

IMPORT_ERROR = False
try:
    import time
    import random

    from multiprocessing import Process, Queue, current_process, freeze_support
except ImportError:
    IMPORT_ERROR = True


import numpy as np

import projection

#
# Function run by worker processes
#

def worker(input, output):
    for func, args in iter(input.get, 'STOP'):
        result = calculate(func, args)
        output.put(result)

#
# Function used to calculate result
#

def calculate(func, args):
    print func
    print args
    result = func(*args)
    return '%s says that %s%s = %s' % \
        (current_process().name, func.__name__, args, result)

#
# Functions referenced by tasks
#

def mul(a, b):
    time.sleep(0.5*random.random())
    return a * b

def plus(a, b):
    time.sleep(0.5*random.random())
    return a + b


def project(disx, disy, xini, yini, lenght, width, height):
    global IM

    print "ini: (%d, %d) len: %d width: %d height:%d" % (xini, yini, lenght, width, height)

    try:
        for k in range(yini * height + xini, yini * height + xini + lenght):
            i = (k/height)
            j = (k%height)
            #print "k: %d i: %d j: %d" % (k, (k/width), (k%width))
            #ra, dec = concatenable['wcsdata'].wcs_pix2world(i, j, 0)
            #ii, jj = reference['wcsdata'].wcs_world2pix(ra, dec, 0)
            #if IM[jj+disy,ii+disx] < concatenable['im'][j,i]:
                #IM[jj+disy,ii+disx] = concatenable['im'][j,i]
    except IndexError:
        print "disx: %f disy: %f xini: %d yini: %d lenght: %d width: %d height: %d IM: (%d, %d)" % (disx, disy, xini, yini, lenght, width, height, IM.shape[1], IM.shape[0])
        print "i: %d j: %d" % (i, j)



#
#
#

def test():
    NUMBER_OF_PROCESSES = 4

    width = 10
    height = 20
    disx = 2
    disy = 4
    lenght = width*height/NUMBER_OF_PROCESSES
    reference = np.zeros([width, height], dtype=np.float32)
    concatenable = np.ones([width, height], dtype=np.float32)

    TASKS1 = [(project, (disx, disy, i*lenght/width, i*lenght%width, lenght, width, height)) for i in range(NUMBER_OF_PROCESSES*10)]
    TASKS2 = [(plus, (i, 8)) for i in range(10)]


    # Create queues
    task_queue = Queue()
    done_queue = Queue()

    # Submit tasks
    for task in TASKS1:
        task_queue.put(task)

    # Start worker processes
    for i in range(NUMBER_OF_PROCESSES):
        Process(target=worker, args=(task_queue, done_queue)).start()

    # Get and print results
    print 'Unordered results:'
    for i in range(len(TASKS1)):
        print '\t', done_queue.get()

    # Add more tasks using `put()`
    for task in TASKS2:
        task_queue.put(task)

    # Get and print some more results
    for i in range(len(TASKS2)):
        print '\t', done_queue.get()

    # Tell child processes to stop
    for i in range(NUMBER_OF_PROCESSES):
        task_queue.put('STOP')


if __name__ == '__main__':
    if IMPORT_ERROR:
        print "Problem import lib"
    else:
        print "Before freeze_support"
        freeze_support()
        print "Before test"
        test()