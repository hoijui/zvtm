
#!/usr/bin/env python
import pika
import sys

class Producer(object):
	def __init__(self, exchange_name, host, virtual_host, userid, password):
		"""
		Constructor. Initiate connection with the RabbitMQ server.

		@param exchange_name name of the exchange to send messages to
		@param host RabbitMQ server host 
		@param userid RabbitMQ server username
		@param password RabbitMQ server user's password
		"""
		self.exchange_name = exchange_name
		#self.connection = amqp.Connection(host=host, userid=userid,
		#    password=password, virtual_host='/', insist=False)
		#self.channel = self.connection.channel()
		try:
			credentials = pika.PlainCredentials(userid, password)
			self.connection = pika.BlockingConnection(pika.ConnectionParameters(
	        	host=host, virtual_host=virtual_host, port=5672, credentials=credentials))
			self.channel = self.connection.channel()
		except pika.exceptions.ChannelClosed, e:
			print "ChannelClosed"
		self.channel.exchange_declare(exchange=exchange_name)
		#self.channel.queue_declare(queue='task_queue', durable=True)
		#print "Producer"
		#print self


	def publish(self, message, routing_key):
		"""
		Publish message to exchange using routing key

		@param text message to publish
		@param routing_key message routing key
		"""
		#msg = amqp.Message(message)
		#msg.properties["content_type"] = "text/plain"
		#msg.properties["delivery_mode"] = 2
		#self.channel.basic_publish(exchange=self.exchange_name,
		#                           routing_key=routing_key, msg=msg)

		#channel.queue_declare(queue='task_queue', durable=True)
		self.channel.queue_declare(queue="%ss" % routing_key, durable=True)
		try:
			self.channel.basic_publish(exchange=self.exchange_name,
			                      routing_key=routing_key,
			                      body=message#,
			                      #properties=pika.BasicProperties(
			                      #	content_type = 'application/json'
			                      #   delivery_mode = 2, # make message persistent
			                      )#)
			print " [x] Sent %r" % (message,)
		except pika.exceptions.ChannelClosed, e:
			print "ChannelClosed"
		#print "publish"
		

	def close(self):
		"""
		Close channel and connection
		"""
		#self.channel.queue_delete(queue='')
		self.channel.close()
		self.connection.close()
