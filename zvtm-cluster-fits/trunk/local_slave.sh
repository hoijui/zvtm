#!/bin/bash
#sample slave run script. requires maven.

DEPS_CP=`mvn org.apache.maven.plugins:maven-dependency-plugin:2.0:build-classpath -DexcludeArtifactIds="zvtm-core" | grep jgroups`


echo $DEPS_CP
echo "----------"

echo $DEPS_CP > libs.txt


LIBS=.
LIBS=$LIBS:target/args4j-2.0.12.jar
LIBS=$LIBS:target/aspectjrt-1.6.2.jar
LIBS=$LIBS:target/commons-logging-1.1.jar
LIBS=$LIBS:target/com.springsource.javax.media.jai.codec-1.1.3.jar
LIBS=$LIBS:target/com.springsource.javax.media.jai.core-1.1.3.jar
LIBS=$LIBS:target/fits-0.3.0.jar
LIBS=$LIBS:target/flowstates-0.1.jar
LIBS=$LIBS:target/hcompress-0.52.jar
LIBS=$LIBS:target/jgroups-2.7.0.GA.jar
LIBS=$LIBS:target/jsky-3.0.jar
LIBS=$LIBS:target/log4j-1.2.14.jar
LIBS=$LIBS:target/slf4j-api-1.5.9-RC0.jar
LIBS=$LIBS:target/slf4j-log4j12-1.5.9-RC0.jar
LIBS=$LIBS:target/swingstates-0.1.jar
LIBS=$LIBS:target/timingframework-1.0.jar
LIBS=$LIBS:target/wildinputserver-0.1.jar
LIBS=$LIBS:target/zvtm-cluster-0.2.8-SNAPSHOT.jar
LIBS=$LIBS:target/zvtm-fits-0.1.4-SNAPSHOT.jar

JAR=target/zvtm-cluster-fits-0.1.2-SNAPSHOT.jar

#ZVTM_CLUSTER_VERSION="0.2.5-SNAPSHOT"
ZVTM_CLUSTER_VERSION="0.2.8-SNAPSHOT"

#java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -Dcom.sun.media.jai.disableMediaLib=true -cp .:$DEPS_CP:target/zvtm-cluster-fits-0.1.2-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -b 0 "$@" -n AstroRad &
#java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -Dcom.sun.media.jai.disableMediaLib=true -cp .:$DEPS_CP:target/zvtm-cluster-fits-0.1.2-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -b 1 "$@" -n AstroRad &

java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -Dcom.sun.media.jai.disableMediaLib=true -cp .:$LIBS:$JAR fr.inria.zvtm.cluster.SlaveApp -b 0 "$@" -n JSkyFitsImage &
java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -Dcom.sun.media.jai.disableMediaLib=true -cp .:$LIBS:$JAR fr.inria.zvtm.cluster.SlaveApp -b 1 "$@" -n JSkyFitsImage &
java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -Dcom.sun.media.jai.disableMediaLib=true -cp .:$LIBS:$JAR fr.inria.zvtm.cluster.SlaveApp -b 2 "$@" -n JSkyFitsImage &
java -Djgroups.bind_addr="127.0.0.1" -Djava.net.preferIPv4Stack=true -Dcom.sun.media.jai.disableMediaLib=true -cp .:$LIBS:$JAR fr.inria.zvtm.cluster.SlaveApp -b 3 "$@" -n JSkyFitsImage &
