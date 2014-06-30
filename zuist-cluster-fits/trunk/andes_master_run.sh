#!/bin/bash
#java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="master.wall.inria.cl" -Xmx1g -cp target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/xercesImpl-2.8.1.jar:target/xml-apis-1.3.03.jar:target/xmlParserAPIs-2.6.2.jar:target/zuist-cluster-fits.0.1.2-SNAPSHOT.jar:target/zuist-cluster-0.2.1-SNAPSHOT.jar:target/zvtm-cluster-0.2.8-SNAPSHOT.jar:target/zvtm-svg-0.2.1.jar:target/commons-logging-1.1.jar:target/args4j-2.0.23.jar:target/zuist-cluster-fits-0.1.2-SNAPSHOT.jar fr.inria.zuist.cluster.viewer.FitsViewer -r 4 -c 6 -bw 2020 -bh 1180 "$@"

rm target/zvtm-core-0.11.2-SNAPSHOT.jar
rm target/slf4j-simple-1.6.1.jar

java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="127.0.0.1" -Xmx1g -cp target/commons-logging-1.1.jar:target/args4j-2.0.23.jar:target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zuist-cluster-fits-0.1.2-SNAPSHOT.jar fr.inria.zuist.viewer.FitsViewer -r 4 -c 6 -bw 2020 -bh 1180 "$@"
