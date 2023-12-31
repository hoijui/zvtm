#!/bin/bash
# SAMPLE run script. Edit and adapt to your needs.

java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="192.168.0.56" -Xmx2g -cp target/aspectjrt-1.6.5.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/xercesImpl-2.8.1.jar:target/xml-apis-1.3.03.jar:target/xmlParserAPIs-2.6.2.jar:target/zuist-cluster-0.2.0-SNAPSHOT.jar:target/zvtm-cluster-0.2.7-SNAPSHOT.jar:target/zvtm-svg-0.2.1.jar:target/commons-logging-1.1.jar:target/args4j-2.0.23.jar:target/javaSmarties-0.1.0-SNAPSHOT.jar  fr.inria.zuist.cluster.viewer.Viewer -r 4 -c 8 -bw 2760 -bh 1800 "$@"

