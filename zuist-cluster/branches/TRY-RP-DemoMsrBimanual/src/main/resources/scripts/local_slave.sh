#!/bin/bash
java -server -Xmx4g -Dsun.java2d.opengl=True -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="127.0.0.1" -cp .:target/zuist-cluster-msrdemo-biman-0.1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.zvtm.cluster.SlaveApp -b 14 -u -n ZuistCluster &

