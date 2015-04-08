
import random, time
import producer
p = producer.Producer(exchange_name='integers', host='192.168.1.213', virtual_host='/fitsuc', userid='fitsuc', password='wall.13')
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

