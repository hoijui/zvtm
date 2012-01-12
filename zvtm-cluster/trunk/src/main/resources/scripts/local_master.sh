#!/bin/bash
#sample master run script. requires maven.

DEPS_CP=`mvn org.apache.maven.plugins:maven-dependency-plugin:2.0:build-classpath -DexcludeArtifactIds="zvtm" | grep jgroups`

ZVTM_CLUSTER_VERSION="0.2.7-SNAPSHOT"
CLASSNAME="fr.inria.zvtm.cluster.examples.VImageExample"

java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -cp .:target/zvtm-cluster-"$ZVTM_CLUSTER_VERSION".jar:$DEPS_CP "$CLASSNAME" "$@"

