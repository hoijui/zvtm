#!/bin/bash

JARS="target/commons-logging-1.1.jar"
JARS=$JARS":target/args4j-2.0.29.jar"
JARS=$JARS":target/aspectjrt-1.6.5.jar"
JARS=$JARS":target/jgroups-2.7.0.GA.jar"
JARS=$JARS":target/log4j-1.2.17.jar"
JARS=$JARS":target/slf4j-api-1.7.10.jar"
JARS=$JARS":target/slf4j-log4j12-1.7.10.jar"
JARS=$JARS":target/timingframework-1.0.jar"
JARS=$JARS":target/zuist-cluster-0.2.1-SNAPSHOT.jar"

PROXY_SETTINGS="-Dhttp.proxyHost=http://foo.bar -Dhttp.proxyPort=3128"

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
	    ssh wild@192.168.2.$startIp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0 && cd /home2/wild/workspace/zvtm/zuist-cluster/trunk && java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="\"192.168.2.$startIp\"" -Xmx4g $PROXY_SETTINGS -cp $JARS fr.inria.zvtm.cluster.SlaveApp -n ZuistCluster -b $SLAVENUM -wb $BLOCKNB -f -a $*" &

      done
done
