#!/bin/bash

JARS="target/commons-logging-1.1.jar"
JARS=$JARS":target/args4j-2.0.29.jar"
JARS=$JARS":target/aspectjrt-1.8.6.jar"
JARS=$JARS":target/jgroups-2.7.0.GA.jar"
JARS=$JARS":target/log4j-1.2.17.jar"
JARS=$JARS":target/slf4j-api-1.7.10.jar"
JARS=$JARS":target/slf4j-log4j12-1.7.10.jar"
JARS=$JARS":target/timingframework-1.0.jar"
JARS=$JARS":target/zvtm-cluster-0.2.9-SNAPSHOT.jar"
JARS=$JARS":target/zvtm-cluster-calibrator-1.0.jar"

function colNum {
  case "$1" in
          "a" ) return 0;;
          "b" ) return 2;;
          "c" ) return 4;;
  esac
}

#start client nodes
for col in {a..c}
do
  for row in {1..4}
    do
      colNum $col
      SLAVENUM1=`expr $? \* 4 + $row - 1`
      SLAVENUM2=`expr $SLAVENUM1 + 4`
      ssh wall@$col$row.wall.inria.cl -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wall/zvtm-code/zvtm-cluster-calibrator/trunk && java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="\"$col$row.wall.inria.cl\"" -Xmx4g -cp \"$JARS\" fr.inria.zvtm.cluster.SlaveApp -n Calibrator -b $SLAVENUM1 -f -a" $* &
      sleep 1
      ssh wall@$col$row.wall.inria.cl -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.1 && cd /home/wall/zvtm-code/zvtm-cluster-calibrator/trunk && java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="\"$col$row.wall.inria.cl\"" -Xmx4g -cp \"$JARS\" fr.inria.zvtm.cluster.SlaveApp -n Calibrator -b $SLAVENUM2 -f -a" $* &
    done
done
