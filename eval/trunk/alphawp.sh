#!/bin/sh
java -Xmx1024M -Xms512M -classpath "target/classes:target/zvtm-0.9.6-SNAPSHOT.jar" net.claribole.eval.alphalens.EvalWorldPointing "$@" 

