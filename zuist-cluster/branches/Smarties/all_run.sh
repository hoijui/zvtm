#!/bin/bash

# 

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


		ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "cd /opt/zuist-cluster/smarties/target/ && DISPLAY=:0.0 java  -Xmx2g -server -XX:+DoEscapeAnalysis -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="192.168.0.$IP" -cp .:aspectjrt-1.6.5.jar:jgroups-2.7.0.GA.jar:log4j-1.2.14.jar:slf4j-api-1.5.9-RC0.jar:slf4j-log4j12-1.5.9-RC0.jar:timingframework-1.0.jar:xercesImpl-2.8.1.jar:xml-apis-1.3.03.jar:xmlParserAPIs-2.6.2.jar:zuist-cluster-0.2.0-SNAPSHOT.jar:zvtm-cluster-0.2.7-SNAPSHOT.jar:zvtm-svg-0.2.1.jar:commons-logging-1.1.jar:args4j-2.0.23.jar fr.inria.zvtm.cluster.SlaveApp -b $SLAVENUM1  -r 5  -x -100 -y -100 -d :0.0 -n ZuistCluster" &
		sleep 1
		ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "cd /opt/zuist-cluster/smarties/target/ && DISPLAY=:0.1 java  -Xmx2g -server -XX:+DoEscapeAnalysis -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="192.168.0.$IP" -cp .:aspectjrt-1.6.5.jar:jgroups-2.7.0.GA.jar:log4j-1.2.14.jar:slf4j-api-1.5.9-RC0.jar:slf4j-log4j12-1.5.9-RC0.jar:timingframework-1.0.jar:xercesImpl-2.8.1.jar:xml-apis-1.3.03.jar:xmlParserAPIs-2.6.2.jar:zuist-cluster-0.2.0-SNAPSHOT.jar:zvtm-cluster-0.2.7-SNAPSHOT.jar:zvtm-svg-0.2.1.jar:commons-logging-1.1.jar:args4j-2.0.23.jar fr.inria.zvtm.cluster.SlaveApp -b $SLAVENUM2  -r 5  -x -100 -y -100 -d :0.1 -n ZuistCluster" &
      done
done

sleep 3

java -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr="192.168.0.56" -Xmx2g  -cp "target/aspectjrt-1.6.5.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/xercesImpl-2.8.1.jar:target/xml-apis-1.3.03.jar:target/xmlParserAPIs-2.6.2.jar:target/zuist-cluster-0.2.0-SNAPSHOT.jar:target/zvtm-cluster-0.2.7-SNAPSHOT.jar:target/zvtm-svg-0.2.1.jar:target/commons-logging-1.1.jar:target/args4j-2.0.23.jar:target/javaSmarties-1.1.0.jar"  fr.inria.zuist.cluster.viewer.Viewer -r 4 -c 8 -bw 2760 -bh 1800 $*

wildo killall java
