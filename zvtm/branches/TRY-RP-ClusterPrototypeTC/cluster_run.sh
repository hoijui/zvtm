#!/bin/bash

#start client nodes
#ssh wild@c1.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave0 -DstartServer=false tc:run" &
#ssh wild@c1.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.1 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave4 -DstartServer=false  tc:run" &
#ssh wild@c2.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave1 -DstartServer=false  tc:run" &
#ssh wild@c2.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.1 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave5 -DstartServer=false  tc:run" &
#ssh wild@c3.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave2 -DstartServer=false  tc:run" &
#ssh wild@c3.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.1 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave6 -DstartServer=false  tc:run" &
#ssh wild@c4.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave3 -DstartServer=false  tc:run" &
#ssh wild@c4.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.1 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave7 -DstartServer=false  tc:run" &

ssh wild@d1.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave8 -DstartServer=false  tc:run" &
ssh wild@d1.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.1 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave12 -DstartServer=false  tc:run" &
ssh wild@d2.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave9 -DstartServer=false  tc:run" &
ssh wild@d2.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.1 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave13 -DstartServer=false  tc:run" &
ssh wild@d3.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave10 -DstartServer=false  tc:run" &
ssh wild@d3.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.1 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave14 -DstartServer=false  tc:run" &
ssh wild@d4.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave11 -DstartServer=false  tc:run" &
ssh wild@d4.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.1 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave15 -DstartServer=false  tc:run" &

