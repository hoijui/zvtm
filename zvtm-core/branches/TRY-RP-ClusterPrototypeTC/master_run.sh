#!/bin/bash

#start the TC server
#ssh wild@d2.wild.lri.fr "export DISPLAY=:0.0 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=master tc:run" &

ssh wild@d2.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "cd /home/wild/romain/zvtm_tc && mvn tc:start" &
sleep 4
mvn -DstartServer=false -DactiveNodes=master tc:run&

