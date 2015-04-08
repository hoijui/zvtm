import json
import consumer

c = consumer.Consumer(host="192.168.1.213", virtual_host="/", userid="guest", password="guest")
c.declare_exchange(exchange_name='java')
c.declare_queue(queue_name='javais', routing_key='java')

while True:
	def callback(ch, method, properties, body):
		print " [x] Received %r" % (body)

		data = json.loads(body)
		#print data['name']
		#print data['x']
		#print data['y']

		print data

		ch.basic_ack(delivery_tag = method.delivery_tag)


	c.start_consuming(callback=callback)
	c.wait()


