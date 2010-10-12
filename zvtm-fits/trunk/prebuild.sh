#!/bin/bash

#Download and add the ivoa.fits archive to the local 
#maven repository.

cd `mktemp -d` &&\
wget http://skyservice.pha.jhu.edu/develop/vo/ivoafits/ivoafits-0.3.jar &&\
mvn install:install-file -DgroupId=ivoa.fits -DartifactId=fits -Dversion=0.3.0 -Dpackaging=jar -Dfile=ivoafits-0.3.jar &&\
wget http://skyview.gsfc.nasa.gov/jar/skyview.jar &&\
mvn install:install-file -DgroupId=skyview -DartifactId=skyview -Dversion=2.4b -Dpackaging=jar -Dfile=skyview.jar

