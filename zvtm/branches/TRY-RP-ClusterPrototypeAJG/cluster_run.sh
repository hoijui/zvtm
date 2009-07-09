#
#wildo {a..d}{1..4} "java -cp target/zvtm-0.10.0-SNAPSHOT.jar:targetimingframework-1.0.jar:target/aspectjrt-1.5.4.jar:target/jgroups-2.7.0.GA.jar:target/commons-logging-1.1.jar:target/args4j-2.0.12.jar fr.inria.zvtm.clustering.AJTestSlave &"

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
		  SLAVENUM1=`expr $? \* 4 + $row - 1`
		  SLAVENUM2=`expr $SLAVENUM1 + 4`
		  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "cd /Users/wild/sandboxes/romain/zvtm-ajgcluster && java -cp target/zvtm-0.10.0-SNAPSHOT.jar:targetimingframework-1.0.jar:target/aspectjrt-1.5.4.jar:target/jgroups-2.7.0.GA.jar:target/commons-logging-1.1.jar:target/args4j-2.0.12.jar fr.inria.zvtm.clustering.AJTestSlave -b $SLAVENUM -w 2560 -h 1600 -r 4 -c 8 -xo 0" &

		  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "cd /Users/wild/sandboxes/romain/zvtm-ajgcluster && java -cp target/zvtm-0.10.0-SNAPSHOT.jar:targetimingframework-1.0.jar:target/aspectjrt-1.5.4.jar:target/jgroups-2.7.0.GA.jar:target/commons-logging-1.1.jar:target/args4j-2.0.12.jar fr.inria.zvtm.clustering.AJTestSlave -b $SLAVENUM2 -w 2560 -h 1600 -r 4 -c 8 -xo 2560" &
		  
      done
done

