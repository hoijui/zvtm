#!/usr/bin/python

import sys

import Queue
import threading
import time



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
			#methodToCall = getattr(self, data['name'])
			#methodToCall(x=data['x'], y=data['y'])
			#print methodToCall
			#methodToCall()
			if data['name'] == 'producer':
				producer()
			elif data['name'] == 'consumer_even':
				consumer('even')
			elif data['name'] == 'consumer_odd':
				consumer('odd')

		else:
			queueLock.release()
		time.sleep(1)

def producer():

	import random, time
	import producer
	p = producer.Producer(exchange_name='integers', host='192.168.1.213', virtual_host='/fitsuc', userid='guest', password='guest')
	while True:
		# generate a random integer between 1 and 100 included
		i = random.randint(1, 100)
		if i % 2 == 0:
			key = 'even'
		else:
			key = 'odd'
		p.publish(str(i), key)
		print 'integer: %d' % i 
		time.sleep(1)
	p.close()


def consumer(routing_key):

	import consumer

	c = consumer.Consumer(host='192.168.1.213', userid='guest', password='guest')
	c.declare_exchange(exchange_name='integers')
	c.declare_queue(queue_name=('%ss')%(routing_key), routing_key=routing_key)

	def callback(ch, method, properties, body):
	    print " [x] Received %r" % (body)
	    ch.basic_ack(delivery_tag = method.delivery_tag)

	c.start_consuming(callback=callback)
	c.wait()


threadList = ["producer", "consumer_even", "consumer_odd"]

threads = []
threadID = 1


def main(argv=sys.argv):

	global threadID, threads, queueLock, workQueue, exitFlag

	ini = time.time()

	workList = []
	for thread in threadList:
		workList.append({'name': thread, 'x': 0, 'y': 0})

	
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

	

	end = time.time()

	print "Init %s -- End %s" % (time.asctime( time.localtime( ini ) ), time.asctime( time.localtime(end) ))
	print "Time: %f seconds" % (end-ini)

	print "Exiting Main Thread"


if __name__ == "__main__":
	main(sys.argv)





