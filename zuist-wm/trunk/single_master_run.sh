#!/bin/bash
# SAMPLE run script. Edit and adapt to your needs.

java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="192.168.2.122" -Xmx1g -cp target/args4j-2.0.23.jar:target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zuist-cluster-0.2.1-SNAPSHOT.jar:target/commons-logging-1.0.3.jar:target/zuist-wm-0.2.0-SNAPSHOT.jar fr.inria.zuist.app.wm.WildWorldExplorer -r 1 -c 1 -bw 800 -bh 600 "$@"
