#!/bin/bash
java -Xmx1g -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="192.168.2.122" -XX:+UseConcMarkSweepGC -cp target/args4j-2.0.12.jar:target/aspectjrt-1.6.2.jar:target/commons-logging-1.1.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zvtm-cluster-0.2.8-SNAPSHOT.jar:target/zvtm-cluster-basicui-0.2.8.jar fr.inria.zvtm.basicui.WildViewer -bw 2760 -bh 1740 -r 4 -c 8 -w 28000 -n 50 "$@"
