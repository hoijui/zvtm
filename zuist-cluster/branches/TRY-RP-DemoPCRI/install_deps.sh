#!/bin/sh
mvn install:install-file -DgroupId=com.feketeries -DartifactId=jdom -Dversion=1.0 -Dpackaging=jar -Dfile=deps/jdom-1.0.jar &&\
mvn install:install-file -DgroupId=com.feketeries -DartifactId=intset -Dversion=1.0 -Dpackaging=jar -Dfile=deps/intset-1.0.jar &&\
mvn install:install-file -DgroupId=com.feketeries -DartifactId=infovis -Dversion=1.0 -Dpackaging=jar -Dfile=deps/infovis-1.0.jar &&\
mvn install:install-file -DgroupId=com.feketeries -DartifactId=beanutils -Dversion=1.0 -Dpackaging=jar -Dfile=deps/beanutils-1.0.jar &&\
mvn install:install-file -DgroupId=fr.inria -DartifactId=WildInputServer -Dversion=1.0 -Dpackaging=jar -Dfile=deps/WildInputServer-1.0.jar &&\
mvn install:install-file -DgroupId=com.feketeries -DartifactId=colt -Dversion=1.0 -Dpackaging=jar -Dfile=deps/colt-1.0.jar &&\
mvn install:install-file -DgroupId=fr.inria.zvtm -DartifactId=javaOSC -Dversion=20060402 -Dpackaging=jar -Dfile=deps/javaOSC-20060402.jar
