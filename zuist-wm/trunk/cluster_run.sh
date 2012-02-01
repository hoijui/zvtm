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
		  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "cd /Users/wild/sandboxes/epietrig/zuist-cluster/trunk && java -XX:+DoEscapeAnalysis -Xmx4g -cp target/commons-logging-1.1.jar:target/args4j-2.0.12.jar:target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zuist-cluster-0.2.0-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -b $SLAVENUM1 -f -d "'\\Display0' $* &
                                                                                                                                                                   
		  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "cd /Users/wild/sandboxes/epietrig/zuist-cluster/trunk && java -XX:+DoEscapeAnalysis -Xmx4g -cp target/commons-logging-1.1.jar:target/args4j-2.0.12.jar:target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zuist-cluster-0.2.0-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -b $SLAVENUM2 -f -d "'\\Display1' $* &
      done
done
