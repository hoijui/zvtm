#!/bin/sh
# Run the generic slave using JMX monitoring
java -Dcom.sun.management.jmxremote -Xmx2g -cp target/zvtm-cluster-proto-ajg-0.1.0-SNAPSHOT.jar:target/timingframework-1.0.jar:target/aspectjrt-1.5.4.jar:target/jgroups-2.7.0.GA.jar:target/commons-logging-1.1.jar:target/args4j-2.0.12.jar fr.inria.zvtm.clustering.AJTestSlave -jmx

