#!/bin/bash
java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Xmx4g -cp target/commons-logging-1.0.3.jar:target/args4j-2.0.12.jar:target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zuist-cluster-0.2.0-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -b 0 $* &
