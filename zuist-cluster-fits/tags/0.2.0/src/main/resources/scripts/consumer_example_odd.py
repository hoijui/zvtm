
import consumer

c = consumer.Consumer(host='192.168.1.213', userid='fitsuc', password='wall.13')
c.declare_exchange(exchange_name='integers')
c.declare_queue(queue_name='odds', routing_key='odd')


def callback(ch, method, properties, body):
    print " [x] Received %r" % (body)
    ch.basic_ack(delivery_tag = method.delivery_tag)


c.start_consuming(callback=callback)
c.wait()
