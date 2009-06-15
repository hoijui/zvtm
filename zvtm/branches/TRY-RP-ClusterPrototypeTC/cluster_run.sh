#!/bin/bash

function colNum {
  case "$1" in 
	  "c" ) return 0;;
	  "d" ) return 1;;
  esac
}

#start client nodes
for col in {c..d}
do
	for row in {1..4}
      do
		  colNum $col 
		  SLAVENUM1=`expr $? \* 8 + $row - 1`
		  SLAVENUM2=`expr $SLAVENUM1 + 4`
		  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave -DslaveNum=$SLAVENUM1 -DstartServer=false tc:run" &
		  ssh wild@$col$row.wild.lri.fr -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no "export DISPLAY=:0.0 && cd /home/wild/romain/zvtm_tc && mvn -DactiveNodes=slave -DslaveNum=$SLAVENUM2 -DstartServer=false tc:run" &

      done
done

