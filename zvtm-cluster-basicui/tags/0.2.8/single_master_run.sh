#!/bin/bash

IP="192.168.1.23"

JARS="target/aspectjrt-1.6.5.jar"
JARS=$JARS":target/args4j-2.0.29.jar"
JARS=$JARS":target/jgroups-2.7.0.GA.jar"
JARS=$JARS":target/log4j-1.2.17.jar"
JARS=$JARS":target/slf4j-api-1.7.10.jar"
JARS=$JARS":target/slf4j-log4j12-1.7.10.jar"
JARS=$JARS":target/timingframework-1.0.jar"
JARS=$JARS":target/commons-logging-1.1.jar"
JARS=$JARS":target/zvtm-cluster-0.2.8.jar"
JARS=$JARS":target/zvtm-cluster-basicui-0.2.8.jar"

java -Xmx1g -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=$IP -XX:+UseConcMarkSweepGC -cp $JARS fr.inria.zvtm.basicui.WallViewer -r 1 -c 1 -bw 800 -bh 600 "$@"
