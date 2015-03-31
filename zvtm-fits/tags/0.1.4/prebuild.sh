#!/bin/bash

#Download and add the ivoa.fits archive to the local 
#maven repository.
PREBUILD_ROOT=`pwd`

cd `mktemp -d` &&\
wget http://skyservice.pha.jhu.edu/develop/vo/ivoafits/ivoafits-0.3.jar &&\
mvn install:install-file -DgroupId=ivoa.fits -DartifactId=fits -Dversion=0.3.0 -Dpackaging=jar -Dfile=ivoafits-0.3.jar &&\
mvn install:install-file -DgroupId=jsky -DartifactId=jsky -Dversion=3.0 -Dpackaging=jar -Dfile=$PREBUILD_ROOT/lib/jsky-3.0.jar &&
mvn install:install-file -DgroupId=hcompress -DartifactId=hcompress -Dversion=0.52 -Dpackaging=jar -Dfile=$PREBUILD_ROOT/lib/hcompress-0.52.jar

