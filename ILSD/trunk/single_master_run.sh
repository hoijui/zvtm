#!/bin/bash

IP="172.18.32.98"

JARS="target/aspectjrt-1.6.5.jar"
JARS=$JARS":target/jgroups-2.7.0.GA.jar"
JARS=$JARS":target/log4j-1.2.17.jar"
JARS=$JARS":target/slf4j-api-1.7.10.jar"
JARS=$JARS":target/slf4j-log4j12-1.7.10.jar"
JARS=$JARS":target/timingframework-1.0.jar"
JARS=$JARS":target/xercesImpl-2.8.1.jar"
JARS=$JARS":target/xml-apis-1.3.03.jar"
JARS=$JARS":target/xmlParserAPIs-2.6.2.jar"
JARS=$JARS":target/zvtm-svg-0.2.1.jar"
JARS=$JARS":target/commons-logging-1.1.jar"
JARS=$JARS":target/args4j-2.0.29.jar"
JARS=$JARS":target/ilsd-1.0.jar"

java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=$IP -Xmx1g -cp .:$JARS fr.inria.ilda.ilsd.ILSD -r 1 -c 1 -bw 800 -bh 600 "$@"
