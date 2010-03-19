#!/bin/bash

#Download and add the nom.tam.fits archive to the local 
#maven repository.

cd `mktemp -d`
wget http://heasarc.gsfc.nasa.gov/docs/heasarc/fits/java/v1.0/v1.04.0/fits.jar
mvn install:install-file -DgroupId=nom.tam.fits -DartifactId=fits -Dversion=1.04.0 -Dpackaging=jar -Dfile=fits.jar
cd -

