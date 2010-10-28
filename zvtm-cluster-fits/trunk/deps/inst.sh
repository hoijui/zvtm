#!/bin/sh
mvn install:install-file -Dfile=WILDInputServer.jar -DgroupId=insitu -DartifactId=wildinputserver -Dversion=0.1 -Dpackaging=jar
mvn install:install-file -Dfile=swingstates.jar -DgroupId=insitu -DartifactId=swingstates -Dversion=0.1 -Dpackaging=jar
mvn install:install-file -Dfile=flowstates.jar -DgroupId=insitu -DartifactId=flowstates -Dversion=0.1 -Dpackaging=jar
