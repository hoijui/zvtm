#!/usr/bin/env python
import pika
import json

connection = pika.BlockingConnection(pika.ConnectionParameters(
        host='master.wall'))
channel = connection.channel()


channel.queue_declare(queue='java')

print ' [*] Waiting for messages. To exit press CTRL+C'

def callback(ch, method, properties, body):
    print " [x] Received %s" % (body,)

    data = json.loads(body)
    print data['msg']

channel.basic_consume(callback,
                      queue='java',
                      no_ack=True)

channel.start_consuming()
