#!/bin/bash
java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="127.0.0.1" -server -Xmx2g -cp target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/xercesImpl-2.8.1.jar:target/xml-apis-1.3.03.jar:target/xmlParserAPIs-2.6.2.jar:target/zuist-cluster-1.0-SNAPSHOT.jar:target/zvtm-cluster-0.1.0-SNAPSHOT.jar:target/zvtm-svg-0.1.1-SNAPSHOT.jar:target/commons-logging-1.1.jar fr.inria.zuist.cluster.viewer.Viewer ./dscene.xml

