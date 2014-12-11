#!/usr/bin/python

import sys

import Queue
import threading
import time

import numpy as np

import wcsCoordinates

import json


'''
def producer(name, x, y):

	import producer
	p = producer.Producer(exchange_name='python', host='192.168.1.213', virtual_host='/', userid='guest', password='guest')
	#time.sleep(1)

	obj = {'name': name, 'x': x, 'y': y}

	p.publish( json.dumps(obj) , 'producer')

'''


def consumer():

	import producer
	p = producer.Producer(exchange_name='python', host='192.168.1.213', virtual_host='/', userid='guest', password='guest')

	import consumer
	c = consumer.Consumer(host='192.168.1.213', virtual_host='/', userid='guest', password='guest')
	c.declare_exchange(exchange_name='java')
	c.declare_queue(queue_name=('%ss')%('java'), routing_key='java')

	kill = False

	def callback(ch, method, properties, body):
		print " [x] Received %r" % (body)

		#print json.dumps(body)

		data = json.loads(body)
		print data['name']

		if data['name'] != 'end':
			methodToCall = getattr(wcsCoordinates, data['name'])
			p_x, p_y = methodToCall(wcsCoordinates.REFERENCE['wcsdata'], data['x'], data['y'])
			lat, lon = wcsCoordinates.icrs2galactic(p_x, p_y)
			galactic = wcsCoordinates.worldgalactic(p_x, p_y)
			ecuatorial = wcsCoordinates.worldecuatorial(p_x, p_y)
			obj = {'name': data['name'], 'x': float(p_x), 'y': float(p_y), 'lat': float(lat), 'lon': float(lon), 'ecuatorial': ecuatorial, 'galactic': galactic}

			p.publish( json.dumps(obj) , 'python')

		else:
			kill = True

		ch.basic_ack(delivery_tag = method.delivery_tag)

	if kill:
		print "Goodbye!"
		c.close()
		p.close()
		return 0

	c.start_consuming(callback=callback)
	c.wait()




def main(argv=sys.argv):

	

	ini = time.time()

	wcsCoordinates.main(argv)


	consumer()
	

	end = time.time()

	print "Init %s -- End %s" % (time.asctime( time.localtime( ini ) ), time.asctime( time.localtime(end) ))
	print "Time: %f seconds" % (end-ini)

	print "Exiting Main Thread"


if __name__ == "__main__":
	main(sys.argv)





