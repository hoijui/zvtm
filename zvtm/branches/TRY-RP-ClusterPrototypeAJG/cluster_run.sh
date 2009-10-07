#!/bin/bash

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
		  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "cd /Users/wild/sandboxes/romain/zvtm-ajg2 && java -Xmx2g -cp target/zvtm-cluster-proto-ajg-0.1.0-SNAPSHOT.jar:target/timingframework-1.0.jar:target/aspectjrt-1.5.4.jar:target/jgroups-2.7.0.GA.jar:target/commons-logging-1.1.jar:target/args4j-2.0.12.jar:target/pdfRenderer-1.0.jar fr.inria.zvtm.clustering.AJTestSlave -b $SLAVENUM1 -w 2760 -h 1840 -r 4 -c 8 -f -d "'\\Display0' &

		  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "cd /Users/wild/sandboxes/romain/zvtm-ajg2 && java -Xmx2g -cp target/zvtm-cluster-proto-ajg-0.1.0-SNAPSHOT.jar:target/timingframework-1.0.jar:target/aspectjrt-1.5.4.jar:target/jgroups-2.7.0.GA.jar:target/commons-logging-1.1.jar:target/args4j-2.0.12.jar:target/pdfRenderer-1.0.jar fr.inria.zvtm.clustering.AJTestSlave -b $SLAVENUM2 -w 2760 -h 1840 -r 4 -c 8 -f -d "'\\Display1' &
      done
done

