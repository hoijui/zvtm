#!/bin/bash
java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="192.168.0.49" -Xmx2g -cp target/args4j-2.0.12.jar:target/aspectjrt-1.6.2.jar:target/commons-logging-1.1.jar:target/dazboard-0.1-SNAPSHOT.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/zvtm-cluster-0.1.0-SNAPSHOT.jar:target/javaOSC-20060402.jar:target/timingframework-1.0.jar fr.inria.zvtm.dazibao.DazBoard -v -bh 1740 -bw 2760 -c 8 -r 4 -n 3 -cv 8 -rv 4

