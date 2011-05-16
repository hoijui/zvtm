#!/bin/bash

# usage example: ./cluster_run.sh -n AnimCircles will run slaves for
# each cluster screen, joining the application named 'AnimCircles'

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
		  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "sudo sysctl -w kern.ipc.maxsockbuf=80000000"
		  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "sudo sysctl -w net.inet.tcp.recvspace=40000000"
		  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "sudo sysctl -w net.inet.tcp.sendspace=40000000"
		  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "cd /Users/wild/sandboxes/jaltieri/zmetisse && java -server -XX:+DoEscapeAnalysis -Xmx4g -cp .:args4j-2.0.12.jar:aspectjrt-1.6.2.jar:commons-logging-1.1.jar:jgroups-2.7.0.GA.jar:log4j-1.2.14.jar:slf4j-api-1.5.9-RC0.jar:slf4j-log4j12-1.5.9-RC0.jar:timingframework-1.0.jar:zvtm-cluster-0.2.6-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -b $SLAVENUM1 -f -d "'\\Display0' $* &

		  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "cd /Users/wild/sandboxes/jaltieri/zmetisse && java -server -XX:+DoEscapeAnalysis -Xmx4g -cp .:args4j-2.0.12.jar:aspectjrt-1.6.2.jar:commons-logging-1.1.jar:jgroups-2.7.0.GA.jar:log4j-1.2.14.jar:slf4j-api-1.5.9-RC0.jar:slf4j-log4j12-1.5.9-RC0.jar:timingframework-1.0.jar:zvtm-cluster-0.2.6-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -b $SLAVENUM2 -f -d "'\\Display1' $* &
      done
done

