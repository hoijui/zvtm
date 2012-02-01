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
		  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "cd /Users/wild/sandboxes/epietrig/zvtm/zuist-wm/trunk && java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Xmx4g -cp target/agile2d-3.0.jar:target/antlr-2.7.7.jar:target/antlr-runtime-3.2.jar:target/args4j-2.0.16.jar:target/aspectjrt-1.6.2.jar:target/commons-beanutils-1.7.0.jar:target/commons-logging-1.1.jar:target/commons-pool-1.5.3.jar:target/ehcache-core-2.4.1.jar:target/geoapi-2.3-M1.jar:target/geoapi-pending-2.3-M1.jar:target/geonames-1.0.jar:target/gluegen-rt-v2.0-rc5.jar:target/gt-api-2.6.5.jar:target/gt-epsg-hsql-2.6.5.jar:target/gt-main-2.6.5.jar:target/gt-metadata-2.6.5.jar:target/gt-referencing-2.6.5.jar:target/gt-shapefile-2.6.5.jar:target/hsqldb-1.8.0.7.jar:target/jcip-annotations-1.0.jar:target/jdom-1.0.jar:target/jgroups-2.7.0.GA.jar:target/jogl.all-v2.0-rc5.jar:target/jsr-275-1.0-beta-2.jar:target/jts-1.10.jar:target/junit-3.8.1.jar:target/log4j-1.2.14.jar:target/opencsv-2.1.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/slf4j-simple-1.6.1.jar:target/stringtemplate-3.2.jar:target/timingframework-1.0.jar:target/vecmath-1.3.2.jar:target/xercesImpl-2.8.1.jar:target/xml-apis-1.3.03.jar:target/xmlParserAPIs-2.6.2.jar:target/zuist-cluster-0.2.0-SNAPSHOT.jar:target/zuist-engine-0.4.0-SNAPSHOT.jar:target/zuist-wm-0.2.0-SNAPSHOT.jar:target/zvtm-agile2d-0.1.0.jar:target/zvtm-cluster-0.2.7-SNAPSHOT.jar:target/zvtm-core-0.11.0-SNAPSHOT.jar:target/zvtm-svg-0.2.1-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -n WildWorldExplorer -b $SLAVENUM1 -f -d "'\\Display0' $* &
                                                                                                                                                                   
		  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "cd /Users/wild/sandboxes/epietrig/zvtm/zuist-wm/trunk && java -XX:+DoEscapeAnalysis -XX:+UseConcMarkSweepGC -Xmx4g -cp target/agile2d-3.0.jar:target/antlr-2.7.7.jar:target/antlr-runtime-3.2.jar:target/args4j-2.0.16.jar:target/aspectjrt-1.6.2.jar:target/commons-beanutils-1.7.0.jar:target/commons-logging-1.1.jar:target/commons-pool-1.5.3.jar:target/ehcache-core-2.4.1.jar:target/geoapi-2.3-M1.jar:target/geoapi-pending-2.3-M1.jar:target/geonames-1.0.jar:target/gluegen-rt-v2.0-rc5.jar:target/gt-api-2.6.5.jar:target/gt-epsg-hsql-2.6.5.jar:target/gt-main-2.6.5.jar:target/gt-metadata-2.6.5.jar:target/gt-referencing-2.6.5.jar:target/gt-shapefile-2.6.5.jar:target/hsqldb-1.8.0.7.jar:target/jcip-annotations-1.0.jar:target/jdom-1.0.jar:target/jgroups-2.7.0.GA.jar:target/jogl.all-v2.0-rc5.jar:target/jsr-275-1.0-beta-2.jar:target/jts-1.10.jar:target/junit-3.8.1.jar:target/log4j-1.2.14.jar:target/opencsv-2.1.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/slf4j-simple-1.6.1.jar:target/stringtemplate-3.2.jar:target/timingframework-1.0.jar:target/vecmath-1.3.2.jar:target/xercesImpl-2.8.1.jar:target/xml-apis-1.3.03.jar:target/xmlParserAPIs-2.6.2.jar:target/zuist-cluster-0.2.0-SNAPSHOT.jar:target/zuist-engine-0.4.0-SNAPSHOT.jar:target/zuist-wm-0.2.0-SNAPSHOT.jar:target/zvtm-agile2d-0.1.0.jar:target/zvtm-cluster-0.2.7-SNAPSHOT.jar:target/zvtm-core-0.11.0-SNAPSHOT.jar:target/zvtm-svg-0.2.1-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -n WildWorldExplorer -b $SLAVENUM2 -f -d "'\\Display1' $* &
      done
done

