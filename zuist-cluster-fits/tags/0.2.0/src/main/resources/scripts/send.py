#!/usr/bin/env python
import pika
import json

connection = pika.BlockingConnection(pika.ConnectionParameters(
        host='master.wall'))
channel = connection.channel()


channel.queue_declare(queue='java')

msg = {'msg': 'Hello World!'}

channel.basic_publish(exchange='java',
                      routing_key='java',
                      body=json.dumps(msg))
print " [x] Sent 'Hello World!'"
connection.close()
