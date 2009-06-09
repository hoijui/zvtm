#!/bin/sh
java -Xmx1024M -Xms512M -jar target/zuist4wild-0.1.0-SNAPSHOT.jar -screen=0 ../zuist-engine/examples/hs-2007-16-a &
java -Xmx1024M -Xms512M -jar target/zuist4wild-0.1.0-SNAPSHOT.jar -screen=1 ../zuist-engine/examples/temple_cg &
