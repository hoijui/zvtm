#!/bin/bash
java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="127.0.0.1" -Xmx4g -cp .:target/vit-0.1.2-SNAPSHOT.jar:target/javaOSC-20060402.jar:target/args4j-2.0.12.jar:target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zvtm-cluster-0.2.6-SNAPSHOT.jar:target/commons-logging-1.1.jar:target/zvtm-cluster-fits-0.1.1-SNAPSHOT.jar:target/hcompress-0.52.jar:target/zvtm-fits-0.1.3-SNAPSHOT.jar fr.inria.zvtm.cluster.AstroRad "$@" 

