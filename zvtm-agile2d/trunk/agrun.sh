#!/bin/sh
java -Djava.library.path=lib -cp target/classes:target/agile2d-0.0.1-SNAPSHOT.jar:target/gluegen-2.0.jar:target/jcip-annotations-1.0.jar:target/jogl.all-2.0.jar:target/junit-3.8.1.jar:target/nativewindow.all-2.0.jar:target/newt.all-2.0.jar:target/timingframework-1.0.jar:target/zvtm-agile2d-0.1.0-SNAPSHOT.jar:target/zvtm-core-0.11.0-SNAPSHOT.jar fr.inria.zvtm.tests.AllGlyphsTest
