#!/bin/sh
java -Xmx1024M -Xms512M -cp "target/classes:target/zvtm-core-0.11.0-SNAPSHOT.jar:target/timingframework-1.0.jar" fr.inria.zvtm.demo.ViewDemo "$@"
