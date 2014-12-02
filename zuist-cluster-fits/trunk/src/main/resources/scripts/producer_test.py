import json
import random, time
import producer
p = producer.Producer(exchange_name="python", host="192.168.1.213", virtual_host="/", userid="guest", password="guest")

key = "python"
workLists = []
workLists.append({"name": "pix2world", "x": 0, "y": 0})
workLists.append({"name": "pix2world", "x": 250, "y": 0})
workLists.append({"name": "pix2world", "x": 250, "y": 250})
workLists.append({"name": "pix2world", "x": 0, "y": 250})
workLists.append({"name": "world2pix", "x": 273.012341, "y": -25.327799})
workLists.append({"name": "world2pix", "x": 272.678927, "y": -24.298804})
workLists.append({"name": "world2pix", "x": 272.999999, "y": -24.859000})
workLists.append({"name": "world2pix", "x": 0, "y": 0})
workLists.append({"name": "end"})
#p.publish(str(workLists), key)


for workList in workLists:
    p.publish(json.dumps(workList), key)
    print "publish: %r" % (workList) 
    time.sleep(1)
p.close()

