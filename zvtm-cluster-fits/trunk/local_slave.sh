#!/bin/bash
#sample slave run script. requires maven.

DEPS_CP=`mvn org.apache.maven.plugins:maven-dependency-plugin:2.0:build-classpath -DexcludeArtifactIds="zvtm-core" | grep jgroups`

#ZVTM_CLUSTER_VERSION="0.2.5-SNAPSHOT"
ZVTM_CLUSTER_VERSION="0.2.8-SNAPSHOT"

java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -Dcom.sun.media.jai.disableMediaLib=true -cp .:$DEPS_CP:target/zvtm-cluster-fits-0.1.2-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -b 0 "$@" -n AstroRad&
java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -Dcom.sun.media.jai.disableMediaLib=true -cp .:$DEPS_CP:target/zvtm-cluster-fits-0.1.2-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -b 1 "$@" -n AstroRad &

