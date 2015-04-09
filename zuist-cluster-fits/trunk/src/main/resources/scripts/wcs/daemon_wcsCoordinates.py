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
		#data['name'] = 'test'

		if data['name'] != 'end':
			methodToCall = getattr(wcsCoordinates, data['name'])
			
			ctype_x, ctype_y = wcsCoordinates.REFERENCE['ctype']
			print ctype_x, ctype_y

			p_x, p_y = methodToCall(wcsCoordinates.REFERENCE['wcsdata'], data['x'], data['y'])
			if "GLON" in ctype_x and "GLAT" in ctype_y:
				l = p_x
				b = p_y
				ra, dec = wcsCoordinates.galactic2icrs(l, b)

			elif "RA" in ctype_x and "DEC" in ctype_y:
				ra = p_x
				dec = p_y
				l, b = wcsCoordinates.icrs2galactic(ra, dec)
			else:
				print "ctype not correctly"
				c.close()
				p.close()
				return 0

			galactic = wcsCoordinates.worldgalactic(l, b)
			ecuatorial = wcsCoordinates.worldecuatorial(ra, dec)
			obj = {'name': data['name'], 'ra': float(ra), 'dec': float(dec), 'l': float(l), 'b': float(b), 'ecuatorial': ecuatorial, 'galactic': galactic}
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





