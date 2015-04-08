#!/usr/bin/python

import sys

import Queue
import threading
import time

import wcsCoordinates


exitFlag = 0

id_producer = 0

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
	global queueLock, exitFlag, id_producer
	while not exitFlag:
		queueLock.acquire()
		if not workQueue.empty():
			data = q.get()
			queueLock.release()
			print "%s processing %s" % (threadName, data)

			if data['name'] == 'end':
				# Notify threads it's time to exit
				exitFlag = 1
				return

			methodToCall = getattr(wcsCoordinates, data['name'])
			#point = methodToCall(wcsCoordinates.REFERENCE['wcsdata'] , data['x'], data['y'])
			#print "(%f, %f)" % (point[0], point[1])
			#producer(id_producer, data['name'], point[0], point[1])
			#id_producer = id_producer + 1
			#return point

		else:
			queueLock.release()
		time.sleep(1)


def producer(id, method, x, y):

	import random, time
	import producer
	p = producer.Producer(exchange_name='python', host='192.168.1.213', virtual_host='/', userid='guest', password='guest')
	#time.sleep(1)
	p.publish( "{'id': %d, 'method': %s, 'x': %d, 'y': %d}" % (id, method, x, y) , 'producer')
	print 'python: %d - %s (%f, %f)' % (id, method, x, y)



def consumer():

	import producer
	p = producer.Producer(exchange_name='python', host='192.168.1.213', virtual_host='/', userid='guest', password='guest')

	import consumer
	c = consumer.Consumer(host='192.168.1.213', userid='guest', password='guest')
	#c.declare_exchange(exchange_name='java')
	c.declare_queue(queue_name=('%ss')%('consumer'), routing_key='consumer')

	def callback(ch, method, properties, body):
	    print " [x] Received %r" % (body)


	    # switch body
	    #workList.append(body)

	    producer(1,'test', 0, 0)

	    #p.publish(body, 'producer')

	    ch.basic_ack(delivery_tag = method.delivery_tag)

	c.start_consuming(callback=callback)
	c.wait()



threadList = ["producer", "consumer"]

threads = []
threadID = 1


def main(argv=sys.argv):

	global threadID, threads, queueLock, workQueue, exitFlag

	ini = time.time()

	wcsCoordinates.main(argv)

	workList = []
	'''
	workList.append({"name": "pix2world", "x": 0, "y": 0})
	workList.append({"name": "pix2world", "x": 250, "y": 0})
	workList.append({"name": "pix2world", "x": 250, "y": 250})
	workList.append({"name": "pix2world", "x": 0, "y": 250})
	workList.append({"name": "world2pix", "x": 273.012341, "y": -25.327799})
	workList.append({"name": "world2pix", "x": 272.678927, "y": -24.298804})
	workList.append({"name": "world2pix", "x": 272.999999, "y": -24.859000})
	workList.append({"name": "world2pix", "x": 0, "y": 0})
	workList.append({"name": "end"})
	'''

	consumer()
	
	queueLock = threading.Lock()
	workQueue = Queue.Queue( 10 )

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
	#exitFlag = 1

	# Wait for all threads to complete
	for t in threads:
		t.join()

	

	end = time.time()

	print "Init %s -- End %s" % (time.asctime( time.localtime( ini ) ), time.asctime( time.localtime(end) ))
	print "Time: %f seconds" % (end-ini)

	print "Exiting Main Thread"


if __name__ == "__main__":
	main(sys.argv)





