#!/bin/bash

LIB="target/aspectjrt-1.6.5.jar"
LIB=$LIB":target/args4j-2.0.29.jar"
LIB=$LIB":target/jgroups-2.7.0.GA.jar"
LIB=$LIB":target/log4j-1.2.17.jar"
LIB=$LIB":target/slf4j-api-1.7.10.jar"
LIB=$LIB":target/slf4j-log4j12-1.7.10.jar"
LIB=$LIB":target/timingframework-1.0.jar"
LIB=$LIB":target/commons-logging-1.1.jar"
LIB=$LIB":target/zvtm-fits-0.1.4.jar"
LIB=$LIB":target/zvtm-svg-0.2.2-SNAPSHOT.jar"

JAR="target/zuist-cluster-fits-0.2.0.jar"

rm target/zvtm-core-0.11.2.jar

IP=192.168.1.213

java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="$IP" -Xmx4096M -Xms2048M -Dcom.sun.media.jai.disableMediaLib=true -cp $LIB:$JAR fr.inria.zuist.viewer.JSkyFitsViewer -r 4 -c 6 -bw 2020 -bh 1180 "@"
