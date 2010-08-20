#!/bin/sh
CLASSPATH=/opt/local/share/java/jython.jar:target/args4j-2.0.12.jar:target/aspectjrt-1.5.4.jar:target/commons-logging-1.1.jar:target/jcip-annotations-1.0.jar:target/jgroups-2.7.0.GA.jar:target/timingframework-1.0.jar:target/zvtm-0.10.0-SNAPSHOT.jar java -Dpython.security.respectJavaAccessibility=false org.python.util.jython -i context.py $*

