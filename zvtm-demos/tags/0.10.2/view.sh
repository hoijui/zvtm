#!/bin/sh
java -Xmx1024M -Xms512M -cp "target/classes:target/zvtm-0.10.0-SNAPSHOT.jar:target/timingframework-1.0.jar:target/zvtm-layout-0.2.0-SNAPSHOT.jar" net.claribole.zvtm.demo.ViewDemo "$@"

