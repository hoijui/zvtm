CLASSPATH="../../../target/test-classes"
CLASSNAME="fr.inria.zvtm.cluster.tests.ClusteredViewBenchmark"
CLUSTERPATH="/media/data/Olivier/zvtm/target"

#!/bin/bash

function colNum {
  case "$1" in
          "a" ) return 0;;
          "b" ) return 2;;
  esac
}

function colIP {
  case "$1" in
          "a" ) return 0;;
          "b" ) return 1;;
  esac
}

function startId {
  case "$1" in 
	  "a" ) return 0;;
	  "b" ) return 40;;
  esac
}


function blockNum {
  case "$1" in 
	  "a" ) return 8;;
	  "b" ) return 7;;
  esac
}

for col in {a..b}
do
	for row in {1..5}
	do
		SLAVENUM=`expr $row - 1`
		startId $col
		SLAVENUM=`expr $? + $SLAVENUM`
		colIP $col
		startIp=`expr $? + 1`
		startIp=`expr $startIp \* 10`
		startIp=`expr $startIp + $row` 
		blockNum $col
		BLOCKNB=$?
		echo "-Djgroups.bind_addr=\"192.168.2.$startIp\" Slavenum: $SLAVENUM $BLOCKNB"
      	ssh wild@$col$row -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=localhost:0.0 ; cd $CLUSTERPATH ; java -server -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=\"192.168.2.$startIp\" -cp ./args4j-2.0.29.jar:./aspectjrt-1.8.6.jar:./log4j-1.2.17.jar:./slf4j-api-1.7.10.jar:./slf4j-log4j12-1.7.10.jar:./jgroups-3.6.6.Final.jar:./timingframework-1.0.jar:./zvtm-cluster-0.2.10-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -b $SLAVENUM -wb $BLOCKNB -fps -s -u -f -g -n Benchmark" $* &
      	sleep 1
      done
done

java -Djgroups.bind_addr="192.168.2.2" -Djava.net.preferIPv4Stack=true -cp "$CLASSPATH:../../../target/*" $CLASSNAME --glyph-number 500 --block-width 960 --block-height 960 --num-rows 5 --num-cols 15"$@" 


walldo killall java


