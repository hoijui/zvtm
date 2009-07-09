#!/bin/bash
#execute after cluster nodes
java -Djava.net.preferIPv4Stack=true -cp target/zvtm-0.10.0-SNAPSHOT.jar:target/timingframework-1.0.jar:target/aspectjrt-1.5.4.jar:target/jgroups-2.7.0.GA.jar:target/commons-logging-1.1.jar:target/args4j-2.0.12.jar fr.inria.zvtm.clustering.TestColorRect -w 25000 -h 8000

