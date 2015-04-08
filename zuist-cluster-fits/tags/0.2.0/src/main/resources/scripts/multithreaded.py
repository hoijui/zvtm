#!/usr/bin/python

import sys

import Queue
import threading
import time


import numpy as np


import projection

exitFlag = 0

class myThread (threading.Thread):
	def __init__(self, threadID, name, q):
		threading.Thread.__init__(self)
		self.threadID = threadID
		self.name = name
		self.q = q
	def run(self):
		print "Starting " + self.name
		process_data(self.name, self.q)
		print "Exiting " + self.name

def process_data(threadName, q):
	global queueLock, exitFlag
	while not exitFlag:
		queueLock.acquire()
		if not workQueue.empty():
			data = q.get()
			queueLock.release()
			print "%s processing %s" % (threadName, data)
			#project(xini=data['ini']['x'], yini=data['ini']['y'], lenght=data['lenght'], width=4, height=4)
			methodToCall = getattr(projection, data['name'])
			methodToCall(reference=projection.REFERENCE, concatenable=projection.CONCATENABLE, disx=projection.DISP['disx'], disy=projection.DISP['disy'], 
						xini=data['ini']['x'], yini=data['ini']['y'], lenght=data['lenght'], width=data['width'], height=data['height'])
		else:
			queueLock.release()
		time.sleep(1)



def prepareWorks(countWorks, name, width, height):
	worksList = []
	counts = width*height/countWorks
	print "counts: %d" % (counts)
	for i in range(0, countWorks):
		worksList.append({	"ini": {"x": (counts * i)/width, "y": (counts * i) % width}, 
							"lenght": counts, "name": name, "width": width, "height": height})

	if countWorks*counts < width*height:
		worksList.append({	"ini": {"x": (counts * countWorks)/width, "y": (counts * countWorks) % width}, 
							"lenght": width*height-countWorks*counts, "name": name, "width": width, "height": height})
	return worksList




threadList = ["Thread-1", "Thread-2"]#, "Thread-3", "Thread-4"]#, "Thread-5", "Thread-6", "Thread-7", "Thread-8", "Thread-9", "Thread-10", "Thread-11", "Thread-12", "Thread-13", "Thread-14", "Thread-15", "Thread-16", "Thread-17", "Thread-18"]


threads = []
threadID = 1


def main(argv=sys.argv):

	global threadID, threads, queueLock, workQueue, exitFlag

	ini = time.time()

	projection.main(argv)

	workList = prepareWorks(countWorks=len(threadList), name="init_project", width=projection.REFERENCE['size'][0], height=projection.REFERENCE['size'][1])
	workList.extend(prepareWorks(countWorks=len(threadList), name="project", width=projection.CONCATENABLE['size'][0], height=projection.CONCATENABLE['size'][1]))

	#projection.init_project(projection.REFERENCE, projection.CONCATENABLE, projection.DISP['disx'], projection.DISP['disy'], 0, 0, projection.REFERENCE['size'][0]*projection.REFERENCE['size'][1], projection.REFERENCE['size'][0], projection.REFERENCE['size'][1])
	#projection.project(projection.REFERENCE, projection.CONCATENABLE, projection.DISP['disx'], projection.DISP['disy'], 0, 0, projection.REFERENCE['size'][0]*projection.REFERENCE['size'][1], projection.REFERENCE['size'][0], projection.REFERENCE['size'][1])

	
	queueLock = threading.Lock()
	workQueue = Queue.Queue( len(workList) )

	# Create new threads
	for tName in threadList:
		thread = myThread(threadID, tName, workQueue)
		thread.start()
		threads.append(thread)
		threadID += 1

	# Fill the queue
	queueLock.acquire()
	for work in workList:
		workQueue.put(work)
	queueLock.release()

	# Wait for queue to empty
	while not workQueue.empty():
		pass

	# Notify threads it's time to exit
	exitFlag = 1

	# Wait for all threads to complete
	for t in threads:
		t.join()

	

	projection.writeFile(projection.REFERENCE, projection.CONCATENABLE, projection.DISP['disx'], projection.DISP['disy'])

	end = time.time()

	print "Init %s -- End %s" % (time.asctime( time.localtime( ini ) ), time.asctime( time.localtime(end) ))
	print "Time: %f seconds" % (end-ini)

	print "Exiting Main Thread"


if __name__ == "__main__":
	main(sys.argv)