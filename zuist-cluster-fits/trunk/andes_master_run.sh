#!/bin/bash
#java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="master.wall.inria.cl" -Xmx1g -cp target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/xercesImpl-2.8.1.jar:target/xml-apis-1.3.03.jar:target/xmlParserAPIs-2.6.2.jar:target/zuist-cluster-fits.0.1.2-SNAPSHOT.jar:target/zuist-cluster-0.2.1-SNAPSHOT.jar:target/zvtm-cluster-0.2.8-SNAPSHOT.jar:target/zvtm-svg-0.2.1.jar:target/commons-logging-1.1.jar:target/args4j-2.0.23.jar:target/zuist-cluster-fits-0.1.2-SNAPSHOT.jar fr.inria.zuist.cluster.viewer.FitsViewer -r 4 -c 6 -bw 2020 -bh 1180 "$@"

#java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="192.168.1.213" -Xmx1g -cp target/jgroups-2.7.0.GA.jar:target/aspectjrt-1.6.2.jar:target/args4j-2.0.12.jar:target/log4j-1.2.14.jar:target/zvtm-fits-0.1.4-SNAPSHOT.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/commons-collections-2.1.jar:target/commons-logging-1.1.jar:target/zvtm-svg-0.2.1.jar:target/zuist-cluster-fits-0.1.2-SNAPSHOT.jar fr.inria.zuist.viewer.FitsViewer -r 4 -c 6 -bw 2020 -bh 1180 "$@"

#LIB=target/agile2d-3.0.2-SNAPSHOT.jar
LIB=$LIB:target/args4j-2.0.23.jar
LIB=$LIB:target/aspectjrt-1.6.2.jar
LIB=$LIB:target/commons-logging-1.1.jar
#LIB=$LIB:target/com.springsource.javax.media.jai.codec-1.1.3.jar
#LIB=$LIB:target/com.springsource.javax.media.jai.core-1.1.3.jar
#LIB=$LIB:target/ehcache-core-2.4.1.jar
#LIB=$LIB:target/fits-0.3.0.jar
#LIB=$LIB:target/gluegen-rt-2.0-rc9.jar
#LIB=$LIB:target/hcompress-0.52.jar
#LIB=$LIB:target/javaosc-core-0.2.jar
#LIB=$LIB:target/javaSmarties-1.1.0-SNAPSHOT.jar
#LIB=$LIB:target/jcip-annotations-1.0.jar
LIB=$LIB:target/jgroups-2.7.0.GA.jar
#LIB=$LIB:target/jogl-all-2.0-rc9.jar
#LIB=$LIB:target/jsky-3.0.jar
#LIB=$LIB:target/json-20090211.jar
LIB=$LIB:target/log4j-1.2.14.jar
LIB=$LIB:target/slf4j-api-1.5.9-RC0.jar
LIB=$LIB:target/slf4j-log4j12-1.5.9-RC0.jar
#LIB=$LIB:target/slf4j-simple-1.6.1.jar
LIB=$LIB:target/timingframework-1.0.jar
#LIB=$LIB:target/tuio-1.4.jar
#LIB=$LIB:target/xercesImpl-2.8.1.jar
#LIB=$LIB:target/xml-apis-1.3.03.jar
#LIB=$LIB:target/xmlParserAPIs-2.6.2.jar
#LIB=$LIB:target/zuist-cluster-0.2.1-SNAPSHOT.jar
#LIB=$LIB:target/zuist-engine-0.4.0-SNAPSHOT.jar
#LIB=$LIB:target/zvtm-agile2d-0.2.0-SNAPSHOT.jar
#LIB=$LIB:target/zvtm-cluster-0.2.8-SNAPSHOT.jar
#LIB=$LIB:target/zvtm-core-0.11.2-SNAPSHOT.jar
LIB=$LIB:target/zvtm-fits-0.1.4-SNAPSHOT.jar
LIB=$LIB:target/zvtm-svg-0.2.1.jar

JAR=target/zuist-cluster-fits-0.2.0-SNAPSHOT.jar

rm target/zvtm-core-0.11.2-SNAPSHOT.jar
rm target/slf4j-simple-1.6.1.jar

ADDR=192.168.1.213


java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="$ADDR" -Xmx2048M -cp $LIB:$JAR fr.inria.zuist.viewer.FitsViewer -r 4 -c 6 -bw 2020 -bh 1180 "@"

#target/commons-collections-2.1.jar: fr.inria.zuist.viewer.FitsViewer -r 4 -c 6 -bw 2020 -bh 1180 "$@"

