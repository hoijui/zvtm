#!/bin/bash

#Download and add the ivoa.fits archive to the local 
#maven repository.

cd `mktemp -d` &&\
wget http://skyservice.pha.jhu.edu/develop/vo/ivoafits/ivoafits-0.3.jar &&\
mvn install:install-file -DgroupId=ivoa.fits -DartifactId=fits -Dversion=0.3.0 -Dpackaging=jar -Dfile=ivoafits-0.3.jar 

