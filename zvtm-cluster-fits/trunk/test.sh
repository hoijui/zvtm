#!/bin/sh

killall -9 java
sleep 1
mvn -P wis clean package
sleep 1
./local_slave.sh
sleep 5
#./local_master.sh -url http://chandra.harvard.edu/photo/2009/e0102/fits/e0102_200-750eV.fits -d 
./local_master.sh -file ../../zvtm-fits/trunk/data/igrj11014-6103_broad.fits
#../../zvtm-fits/trunk/data/g19_5-7.5keV.fits 
