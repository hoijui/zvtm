#!/bin/bash

#Usage example:

# wildo sudo mkdir -p  /opt/zuist-cluster/smarties/target/

# linux:
# wildo sudo chown -R wild.wild /opt/zuist-cluster/smarties
# mac:
# wildo sudo chown -R wild /opt/zuist-cluster/smarties
# wildo sudo chown -R wild /home/olivier/bigimages/zuist
# ./deploy.sh
# linux
# wildo sudo chown -R root.root /opt/zuist-cluster/smarties
# mac
# wildo sudo chown -R root /opt/zuist-cluster/smarties

for col in {a..d}
do
	for row in {1..4}
      do
		  #scp -r  /home/olivier/src/zvtm/zuist-cluster/branches/Smarties/target wild@$col$row.wild.lri.fr:/opt/zuist-cluster/smarties
	  scp -r  /home/olivier/bigimages/zuist/ssc2008-11 wild@$col$row.wild.lri.fr:/usr/local/share/bigimages/zuist
      done
done

