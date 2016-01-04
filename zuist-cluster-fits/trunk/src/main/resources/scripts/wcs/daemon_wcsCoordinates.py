#!/usr/bin/python

import sys

import Queue
import threading
import time

import numpy as np

import wcsCoordinates

import json

import pika


'''
def producer(name, x, y):

	import producer
	p = producer.Producer(exchange_name='python', host='192.168.1.213', virtual_host='/', userid='guest', password='guest')
	#time.sleep(1)

	obj = {'name': name, 'x': x, 'y': y}

	p.publish( json.dumps(obj) , 'producer')

'''

PRODUCER_ROUTINGKEY = 'python'
#HOST = '192.168.1.213'
HOST = '127.0.0.1'
VIRTUAL_HOST = '/'
USER_ID = 'guest'
PASSWORD = 'guest'
CONSUMER_ROUTINGKEY = 'java'


def consumer():

	try:

		import producer
		#p = producer.Producer(exchange_name='python', host='192.168.1.213', virtual_host='/', userid='guest', password='guest')
		p = producer.Producer(exchange_name=PRODUCER_ROUTINGKEY, host=HOST, virtual_host=VIRTUAL_HOST, userid=USER_ID, password=PASSWORD)

		import consumer
		#c = consumer.Consumer(host='192.168.1.213', virtual_host='/', userid='guest', password='guest')
		c = consumer.Consumer(host=HOST, virtual_host=VIRTUAL_HOST, userid=USER_ID, password=PASSWORD)
		#c.declare_exchange(exchange_name='java')
		c.declare_exchange(exchange_name=CONSUMER_ROUTINGKEY)
		#c.declare_queue(queue_name=('%ss')%('java'), routing_key='java')
		c.declare_queue(queue_name=('%ss')%(CONSUMER_ROUTINGKEY), routing_key=CONSUMER_ROUTINGKEY)
		

		kill = False

		def callback(ch, method, properties, body):
			print " [x] Received %r" % (body)

			#print json.dumps(body)

			data = json.loads(body)
			print data['name']
			#data['name'] = 'test'

			if data['name'] != 'end' and data['name'] == 'pix2world':
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
				obj = {'name': data['name'], 'ra': float(ra), 'dec': float(dec), 'l': float(l), 'b': float(b), 'ecuatorial': ecuatorial, 'galactic': galactic, 'id': data['id'], 'x': data['x'], 'y': data['y']}
				#p.publish( json.dumps(obj) , 'python')
				p.publish( json.dumps(obj) , PRODUCER_ROUTINGKEY)

			elif data['name'] != 'end' and data['name'] == 'world2pix':

				methodToCall = getattr(wcsCoordinates, data['name'])

				p_x, p_y = methodToCall(wcsCoordinates.REFERENCE['wcsdata'], data['ra'], data['dec'])


				obj = {'name': data['name'], 'x': float(p_x), 'y': float(p_y), 'id': data['id'], 'ra': data['ra'], 'dec': data['dec']}
				p.publish( json.dumps(obj) , PRODUCER_ROUTINGKEY)

			elif data['name'] != 'end' and data['name'] == 'set_reference':
				print "call set_reference()"
				methodToCall = getattr(wcsCoordinates, data['name'])

				wcsCoordinates.set_reference(data['src_path'])


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

	except pika.exceptions.AMQPConnectionError, e:

		print "Not found RabbitMQ connection"
		return 0



def main(argv=sys.argv):

	

	ini = time.time()

	wcsCoordinates.main(argv)
	print "wcsCoordinates"

	consumer()


	end = time.time()

	print "Init %s -- End %s" % (time.asctime( time.localtime( ini ) ), time.asctime( time.localtime(end) ))
	print "Time: %f seconds" % (end-ini)

	print "Exiting Main Thread"


if __name__ == "__main__":
	main(sys.argv)





