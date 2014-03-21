#!/bin/sh
java -Xmx2048M -Xms512M -classpath "target/classes:target/timingframework-1.0.jar:target/javaosc-core-0.2.jar" fr.inria.zvtm.tests.AllGlyphsTest "$@"
