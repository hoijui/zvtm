#!/bin/bash
# Downloads javaOSC, unpacks it and extracts it into the
# local maven repository (javaOSC is not managed by maven...)
MY_TMPDIR=`mktemp -d`
cd $MY_TMPDIR
wget http://www.illposed.com/software/dls/javaosc.zip
unzip javaosc.zip
mvn install:install-file -DgroupId=fr.inria.zvtm -DartifactId=javaOSC -Dversion=20060402 -Dpackaging=jar -Dfile=javaosc/lib/javaosc.jar
cd -
rm -rf $MY_TMPDIR

