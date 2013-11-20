#!/bin/bash

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
      ssh wall@$col$row.wall.inria.cl -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "sudo sysctl -w net.core.wmem_max=660000"
      ssh wall@$col$row.wall.inria.cl -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "sudo sysctl -w net.core.rmem_max=24000000"
      ssh wall@$col$row.wall.inria.cl -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wall/zvtm-code/zvtm-cluster-basicui/trunk && java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="\"$col$row.wall.inria.cl\"" -Xmx4g -cp target/args4j-2.0.12.jar:target/aspectjrt-1.6.2.jar:target/commons-logging-1.1.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zvtm-cluster-0.2.8-SNAPSHOT.jar:target/zvtm-cluster-basicui-0.2.8.jar fr.inria.zvtm.cluster.SlaveApp -n WildViewer -b $SLAVENUM1 -f" $* &
      sleep 1
      ssh wall@$col$row.wall.inria.cl -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.1 &&  cd /home/wall/zvtm-code/zvtm-cluster-basicui/trunk && java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="\"$col$row.wall.inria.cl\"" -Xmx4g -cp target/args4j-2.0.12.jar:target/aspectjrt-1.6.2.jar:target/commons-logging-1.1.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zvtm-cluster-0.2.8-SNAPSHOT.jar:target/zvtm-cluster-basicui-0.2.8.jar fr.inria.zvtm.cluster.SlaveApp -n WildViewer -b $SLAVENUM2 -f" $* &
    done
done
