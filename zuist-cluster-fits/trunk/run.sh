#!/bin/bash
java -cp target/zuist-cluster-fits-0.1.1-SNAPSHOT.jar:target/jgroups-2.7.0.GA.jar:target/aspectjrt-1.6.2.jar:target/args4j-2.0.12.jar:target/log4j-1.2.14.jar:target/zvtm-fits-0.1.1-SNAPSHOT.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/commons-collections-2.1.jar:target/commons-logging-1.1.jar:target/zvtm-svg-0.1.1-SNAPSHOT.jar fr.inria.zuist.cluster.viewer.Viewer "$@" 

