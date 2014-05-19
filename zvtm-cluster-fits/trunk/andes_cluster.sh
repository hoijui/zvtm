#!/bin/bash

function colNum {
  case "$1" in
          "a" ) return 0;;
          "b" ) return 2;;
          "c" ) return 4;;
  esac
}


DEPS_CP=`mvn org.apache.maven.plugins:maven-dependency-plugin:2.0:build-classpath -DexcludeArtifactIds="zvtm-core" | grep jgroups`

#start client nodes
for col in {a..c}
do
  for row in {1..4}
    do
      colNum $col
      SLAVENUM1=`expr $? \* 4 + $row - 1`
      SLAVENUM2=`expr $SLAVENUM1 + 4`
      ssh wall@$col$row.wall.inria.cl -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wall/zvtm-code/zvtm-cluster-fits/trunk && java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="\"$col$row.wall.inria.cl\"" -Xmx4g -Dcom.sun.media.jai.disableMediaLib=true -cp .:$DEPS_CP:target/zvtm-cluster-fits-0.1.2-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -n JSkyFitsImage -b $SLAVENUM1 -f -a" $* &
      sleep 1
      ssh wall@$col$row.wall.inria.cl -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.1 && cd /home/wall/zvtm-code/zvtm-cluster-fits/trunk && java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="\"$col$row.wall.inria.cl\"" -Xmx4g -Dcom.sun.media.jai.disableMediaLib=true -cp .:$DEPS_CP:target/zvtm-cluster-fits-0.1.2-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -n JSkyFitsImage -b $SLAVENUM2 -f -a" $* &
    done
done