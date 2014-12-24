#!/bin/bash

java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -Xmx4096M -Dcom.sun.media.jai.disableMediaLib=true -cp target/commons-logging-1.1.jar:target/args4j-2.0.23.jar:target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zuist-cluster-fits-0.2.0-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -n JSkyFitsViewer -b 0
