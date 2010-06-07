#!/bin/bash
java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="127.0.0.1" -server -Xmx2g -cp .:target/zuist-cluster-msrdemo-biman-0.1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.zuist.cluster.viewer.Viewer "$@"

