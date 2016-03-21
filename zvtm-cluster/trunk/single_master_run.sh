#!/bin/bash

#IP="192.168.2.108"
#IP="192.168.1.14"
IP="129.175.5.15"

JARS="target/aspectjrt-1.8.9.jar"
JARS=$JARS":target/jgroups-3.6.6.Final.jar"
JARS=$JARS":target/log4j-1.2.17.jar"
JARS=$JARS":target/slf4j-api-1.7.10.jar"
JARS=$JARS":target/slf4j-log4j12-1.7.10.jar"
JARS=$JARS":target/timingframework-1.0.jar"
JARS=$JARS":target/commons-logging-1.1.jar"
JARS=$JARS":target/args4j-2.0.29.jar"
JARS=$JARS":target/zvtm-cluster-0.2.10-SNAPSHOT.jar"

java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=$IP -Xmx1g -cp .:$JARS fr.inria.zvtm.cluster.examples.VSegmentExample -r 1 -c 1 -bw 800 -bh 600 "$@"
