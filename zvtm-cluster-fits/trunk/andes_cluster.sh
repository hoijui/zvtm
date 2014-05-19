#!/bin/bash

function colNum {
  case "$1" in
          "a" ) return 0;;
          "b" ) return 2;;
          "c" ) return 4;;
  esac
}


#DEPS_CP=`mvn org.apache.maven.plugins:maven-dependency-plugin:2.0:build-classpath -DexcludeArtifactIds="zvtm-core" | grep jgroups`


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



#start client nodes
for col in {a..c}
do
  for row in {1..4}
    do
      colNum $col
      SLAVENUM1=`expr $? \* 4 + $row - 1`
      SLAVENUM2=`expr $SLAVENUM1 + 4`
      ssh wall@$col$row.wall.inria.cl -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wall/zvtm-code/zvtm-cluster-fits/trunk && java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="\"$col$row.wall.inria.cl\"" -Xmx4g -Dcom.sun.media.jai.disableMediaLib=true -cp .:$LIBS:$JAR fr.inria.zvtm.cluster.SlaveApp -n JSkyFitsImage -b $SLAVENUM1 -f -a " $* &
      sleep 1
      ssh wall@$col$row.wall.inria.cl -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.1 && cd /home/wall/zvtm-code/zvtm-cluster-fits/trunk && java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="\"$col$row.wall.inria.cl\"" -Xmx4g -Dcom.sun.media.jai.disableMediaLib=true -cp .:$LIBS:$JAR fr.inria.zvtm.cluster.SlaveApp -n JSkyFitsImage -b $SLAVENUM2 -f -a " $* &
    done
done




