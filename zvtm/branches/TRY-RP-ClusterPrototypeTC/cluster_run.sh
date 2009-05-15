#!/bin/bash

#start the TC server
#start client nodes
ssh wild@d1.wild.lri.fr "export DISPLAY=:0.0 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave0 -DstartServer=false tc:run" &
ssh wild@d1.wild.lri.fr "export DISPLAY=:0.1 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave3 -DstartServer=false tc:run" &
ssh wild@d2.wild.lri.fr "export DISPLAY=:0.0 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave1 -DstartServer=false tc:run" &
ssh wild@d2.wild.lri.fr "export DISPLAY=:0.1 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave4 -DstartServer=false tc:run" &
ssh wild@d3.wild.lri.fr "export DISPLAY=:0.0 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave2 -DstartServer=false tc:run" &
ssh wild@d3.wild.lri.fr "export DISPLAY=:0.1 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave5 -DstartServer=false tc:run" &

