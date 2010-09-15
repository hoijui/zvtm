#!/bin/bash
#sample slave run script. requires maven.

DEPS_CP=`mvn org.apache.maven.plugins:maven-dependency-plugin:2.0:build-classpath -DexcludeArtifactIds="zvtm-core" | grep jgroups`

ZVTM_CLUSTER_VERSION="0.2.5-SNAPSHOT"

java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -cp .:$DEPS_CP:target/zvtm-cluster-fits-0.1.1-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -b 0 "$@" &
java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -cp .:$DEPS_CP:target/zvtm-cluster-fits-0.1.1-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -b 1 "$@" &

