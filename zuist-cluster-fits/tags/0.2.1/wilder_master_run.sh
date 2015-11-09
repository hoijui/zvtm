#!/bin/bash

IP="192.168.2.2"

LIB="target/aspectjrt-1.8.6.jar"
LIB=$LIB":target/args4j-2.0.29.jar"
LIB=$LIB":target/jgroups-2.7.0.GA.jar"
LIB=$LIB":target/log4j-1.2.17.jar"
LIB=$LIB":target/slf4j-api-1.7.10.jar"
LIB=$LIB":target/slf4j-log4j12-1.7.10.jar"
LIB=$LIB":target/timingframework-1.0.jar"
LIB=$LIB":target/commons-logging-1.1.jar"
LIB=$LIB":target/zvtm-fits-0.1.4-SNAPSHOT.jar"
LIB=$LIB":target/zvtm-svg-0.2.2-SNAPSHOT.jar"

JAR="target/zuist-cluster-fits-0.2.0-SNAPSHOT.jar"

java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=$IP -Xmx4096M -Xms2048M -Dcom.sun.media.jai.disableMediaLib=true -cp $LIB:$JAR fr.inria.zuist.viewer.JSkyFitsViewer -r 5 -c 15 -bw 960 -bh 960 "$@"
