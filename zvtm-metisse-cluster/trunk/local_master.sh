#!/bin/sh
#bind_addr must be the local network address of the master 
java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=True -cp .:target/args4j-2.0.12.jar:target/aspectjrt-1.6.2.jar:target/commons-logging-1.1.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zvtm-cluster-0.2.6-SNAPSHOT.jar:target/zvtm-metisse-cluster-0.0.1-SNAPSHOT.jar fr.inria.zvtm.cluster.MetisseMain 1 0

