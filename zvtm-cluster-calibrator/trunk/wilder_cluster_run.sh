#!/bin/bash

function colNum {
  case "$1" in
          "a" ) return 0;;
          "b" ) return 2;;
  esac
}

function colIP {
  case "$1" in
          "a" ) return 1;;
          "b" ) return 2;;
  esac
}

#start client nodes
for col in {a..b}
do
  for row in {1..5}
    do
      colNum $col
      SLAVENUM1=`expr $? \* 5 + $row - 1`
      SLAVENUM2=`expr $SLAVENUM1 + 5`
      colIP $col
      ssh wild@192.168.2.$?$row -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wild/zvtm-cluster-calibrator/trunk && java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="\"192.168.2.$?$row\"" -Xmx4g -cp target/commons-logging-1.1.jar:target/args4j-2.0.23.jar:target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zvtm-cluster-0.2.8-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -n Calibrator -b $SLAVENUM1 -f -a" $* &
      sleep 1
      colIP $col
      ssh wild@192.168.2.$?$row -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.1 && cd /home/wild/zvtm-cluster-calibrator/trunk && java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="\"192.168.2.$?$row\"" -Xmx4g -cp target/commons-logging-1.1.jar:target/args4j-2.0.23.jar:target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zvtm-cluster-0.2.8-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -n Calibrator -b $SLAVENUM2 -f -a" $* &
    done
done
