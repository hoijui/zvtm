#!/bin/bash

IP="192.168.1.23"

JARS="target/aspectjrt-1.6.5.jar"
JARS=$JARS":target/jgroups-2.7.0.GA.jar"
JARS=$JARS":target/log4j-1.2.17.jar"
JARS=$JARS":target/slf4j-api-1.7.10.jar"
JARS=$JARS":target/slf4j-log4j12-1.7.10.jar"
JARS=$JARS":target/timingframework-1.0.jar"
JARS=$JARS":target/xercesImpl-2.8.1.jar"
JARS=$JARS":target/xml-apis-1.3.03.jar"
JARS=$JARS":target/xmlParserAPIs-2.6.2.jar"
JARS=$JARS":target/commons-logging-1.1.jar"
JARS=$JARS":target/args4j-2.0.23.jar"
JARS=$JARS":target/zuist-cluster-0.2.1-SNAPSHOT.jar"
JARS=$JARS":target/zuist-wm-0.2.0-SNAPSHOT.jar"

java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=$IP -Xmx1g -cp $JARS fr.inria.zuist.app.wm.WildWorldExplorer -r 1 -c 1 -bw 800 -bh 600 "$@"
