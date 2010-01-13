#!/bin/bash
java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="192.168.0.49" -cp target/args4j-2.0.12.jar:target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zvtm-cluster-0.1.0-SNAPSHOT.jar:target/commons-logging-1.1.jar:target/antlr-2.7.7.jar:target/xercesImpl-2.8.1.jar:target/xml-apis-1.3.03.jar:target/xmlParserAPIs-2.6.2.jar:target/zgrviewer-0.9.0-SNAPSHOT.jar net.claribole.zgrviewer.ZGRViewer  

