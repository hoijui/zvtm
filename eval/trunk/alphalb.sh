#!/bin/sh
java -Xmx1024M -Xms512M -classpath "target/classes:target/zvtm-0.9.8-SNAPSHOT.jar" net.claribole.eval.alphalens.EvalAcqLabel "$@"
