#!/bin/bash

function colNum {
  case "$1" in
	  "a" ) return 0;;
	  "b" ) return 1;;
	  "c" ) return 2;;
  esac
}

#start client nodes
for col in {a..c}
do
	for row in {1..4}
      do
		  colNum $col
		  SLAVENUM1=`expr $? \* 8 + $row - 1`
		  SLAVENUM2=`expr $SLAVENUM1 + 4`
		  colNum $col
		  X1=`expr $? \* 400`
		  colNum $col
		  X2=`expr $? \* 400 + 200`
		  Y=`expr $row \* 112`
		  java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Xms256m -Xmx512m -cp target/commons-logging-1.0.3.jar:target/args4j-2.0.23.jar:target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zvtm-cluster-0.2.8-SNAPSHOT.jar:target/zuist-cluster-0.2.1-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -n WildWorldExplorer -u -b $SLAVENUM1 -x $X1 -y $Y -a $* &
		  sleep .2
		  java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Xms256m -Xmx512m -cp target/commons-logging-1.0.3.jar:target/args4j-2.0.23.jar:target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zvtm-cluster-0.2.8-SNAPSHOT.jar:target/zuist-cluster-0.2.1-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -n WildWorldExplorer -u -b $SLAVENUM2 -x $X2 -y $Y -a $* &
		  sleep .2
      done
done
