#!/bin/bash                                                                    

# SAMPLE run script. Edit and adapt to your needs.

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
                  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "sudo sysctl -w net.core.wmem_max=660000"
                  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "sudo sysctl -w net.core.rmem_max=24000000"
                  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wild/sandboxes/romain/zvtm-zuist && java -server -Xmx4g -Dsun.java2d.opengl=True -Djava.net.preferIPv4Stack=true -cp .:target/commons-logging-1.1.jar:target/args4j-2.0.12.jar:target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zvtm-cluster-0.2.0-SNAPSHOT.jar:target/zuist-cluster-1.0-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -b $SLAVENUM1 -f -o -d "':0.0' -n ZuistCluster $* &

                  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.1 && cd /home/wild/sandboxes/romain/zvtm-zuist && java -server -Xmx4g -Dsun.java2d.opengl=True -Djava.net.preferIPv4Stack=true -cp .:target/commons-logging-1.1.jar:target/args4j-2.0.12.jar:target/aspectjrt-1.6.2.jar:target/jgroups-2.7.0.GA.jar:target/log4j-1.2.14.jar:target/slf4j-api-1.5.9-RC0.jar:target/slf4j-log4j12-1.5.9-RC0.jar:target/timingframework-1.0.jar:target/zvtm-cluster-0.2.0-SNAPSHOT.jar:target/zuist-cluster-1.0-SNAPSHOT.jar fr.inria.zvtm.cluster.SlaveApp -b $SLAVENUM2 -f -o -d "':0.1' -n ZuistCluster $* &
      done
done

