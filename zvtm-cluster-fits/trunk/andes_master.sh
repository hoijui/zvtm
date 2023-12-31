#!/bin/bash

LIBS=.
LIBS=$LIBS:target/javaOSC-20060402.jar
LIBS=$LIBS:target/args4j-2.0.12.jar
LIBS=$LIBS:target/aspectjrt-1.6.2.jar
LIBS=$LIBS:target/jgroups-2.7.0.GA.jar
LIBS=$LIBS:target/log4j-1.2.14.jar
LIBS=$LIBS:target/slf4j-api-1.5.9-RC0.jar
LIBS=$LIBS:target/slf4j-log4j12-1.5.9-RC0.jar
LIBS=$LIBS:target/timingframework-1.0.jar
#LIBS=$LIBS:target/zvtm-cluster-0.2.6.jar
LIBS=$LIBS:target/zvtm-cluster-0.2.8-SNAPSHOT.jar
LIBS=$LIBS:target/commons-logging-1.1.jar
LIBS=$LIBS:target/zvtm-cluster-fits-0.1.2-SNAPSHOT.jar
LIBS=$LIBS:target/hcompress-0.52.jar
LIBS=$LIBS:target/zvtm-fits-0.1.4-SNAPSHOT.jar

JAR=target/zvtm-cluster-fits-0.1.2-SNAPSHOT.jar

#java -Djava.net.preferIPv4Stack=true -Dcom.sun.media.jai.disableMediaLib=true -Djgroups.bind_addr="127.0.0.1" -Xmx4g -cp $libs fr.inria.zvtm.cluster.AstroRad "$@" 

java -Djava.net.preferIPv4Stack=true -Dcom.sun.media.jai.disableMediaLib=true -Djgroups.bind_addr="192.168.1.213" -Xmx4g -cp $LIBS:$JAR fr.inria.zvtm.cluster.andes.JSkyFitsExample -r 4 -c 6 -bw 2020 -bh 1180 "$@"

