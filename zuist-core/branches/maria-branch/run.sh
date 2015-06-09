#!/bin/sh
java -Xmx2048M -Xms512M -Djava.library.path=target/lib -Djogamp.gluegen.UseTempJarCache=false -jar target/zuist-engine-0.4.0-SNAPSHOT.jar "$@"
 
