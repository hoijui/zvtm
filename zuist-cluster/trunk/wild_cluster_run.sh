#!/bin/bash

JARS="target/commons-logging-1.1.jar"
JARS=$JARS":target/args4j-2.0.29.jar"
JARS=$JARS":target/aspectjrt-1.8.6.jar"
JARS=$JARS":target/jgroups-2.7.0.GA.jar"
JARS=$JARS":target/log4j-1.2.17.jar"
JARS=$JARS":target/slf4j-api-1.7.10.jar"
JARS=$JARS":target/slf4j-log4j12-1.7.10.jar"
JARS=$JARS":target/timingframework-1.0.jar"
JARS=$JARS":target/zraildar-0.2.jar"

function colNum {
  case "$1" in
          "a" ) return 0;;
          "b" ) return 1;;
          "c" ) return 2;;
          "d" ) return 3;;
  esac
}

#start client nodes
for col in {a..d}
do
  for row in {1..4}
    do
      colNum $col
      SLAVENUM1=`expr $? \* 8 + $row - 1`
      SLAVENUM2=`expr $SLAVENUM1 + 4`
      colNum $col
      IP=`expr 32 + $? \* 4 + $row - 1`
      ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /opt/zuist-cluster/zraildar/ && java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="192.168.0.$IP"  -Xmx4g -cp $JARS fr.inria.zvtm.cluster.SlaveApp -n WallZRaildar -b $SLAVENUM1 -f -a -o -d :0.0 $*" &
      sleep 1
      ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.1 && cd /opt/zuist-cluster/zraildar/ && java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="192.168.0.$IP"  -Xmx4g -cp $JARS fr.inria.zvtm.cluster.SlaveApp -n WallZRaildar -b $SLAVENUM2 -f -a -o -d :0.1 $*" &
    done
done
