#!/bin/bash

#start the TC server
ssh wild@d2.wild.lri.fr "export DISPLAY=:0.0 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=master tc:run" &

