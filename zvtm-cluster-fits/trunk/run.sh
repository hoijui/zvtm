#!/bin/sh

libs=target/zvtm-cluster-0.2.8-SNAPSHOT.jar
libs=$libs:target/fits-0.3.0.jar
libs=$libs:target/zvtm-cluster-fits-0.1.2-SNAPSHOT.jar
libs=$libs:target/args4j-2.0.12.jar
libs=$libs:target/jgroups-2.7.0.GA.jar
libs=$libs:target/aspectjrt-1.6.2.jar
libs=$libs:target/commons-logging-1.1.jar
libs=$libs:target/log4j-1.2.14.jar
libs=$libs:target/slf4j-log4j12-1.5.9-RC0.jar
libs=$libs:target/timingframework-1.0.jar
libs=$libs:target/slf4j-api-1.5.9-RC0.jar

java -cp $libs fr.inria.zvtm.cluster.examples.FitsViewer

