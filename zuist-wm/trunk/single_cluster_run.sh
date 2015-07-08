#!/bin/bash

JARS="target/commons-logging-1.1.jar"
JARS=$JARS":target/args4j-2.0.23.jar"
JARS=$JARS":target/aspectjrt-1.8.6.jar"
JARS=$JARS":target/jgroups-2.7.0.GA.jar"
JARS=$JARS":target/log4j-1.2.17.jar"
JARS=$JARS":target/slf4j-api-1.7.10.jar"
JARS=$JARS":target/slf4j-log4j12-1.7.10.jar"
JARS=$JARS":target/timingframework-1.0.jar"
JARS=$JARS":target/zuist-cluster-0.2.1-SNAPSHOT.jar"
JARS=$JARS":target/zuist-wm-0.2.0-SNAPSHOT.jar"

java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Xmx1g -cp $JARS fr.inria.zvtm.cluster.SlaveApp -n WildWorldExplorer -b 0 $* &
