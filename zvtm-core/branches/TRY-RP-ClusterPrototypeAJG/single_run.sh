#!/bin/sh
#sample run script for a single host

#run slave
java -Xmx2g -cp target/zvtm-cluster-proto-ajg-0.1.0-SNAPSHOT.jar:target/timingframework-1.0.jar:target/aspectjrt-1.5.4.jar:target/jgroups-2.7.0.GA.jar:target/commons-logging-1.1.jar:target/args4j-2.0.12.jar:target/pdfRenderer-1.0.jar fr.inria.zvtm.clustering.AJTestSlave -b 0 -w 600 -h 400 -r 2 -c 2 &
java -Xmx2g -cp target/zvtm-cluster-proto-ajg-0.1.0-SNAPSHOT.jar:target/timingframework-1.0.jar:target/aspectjrt-1.5.4.jar:target/jgroups-2.7.0.GA.jar:target/commons-logging-1.1.jar:target/args4j-2.0.12.jar:target/pdfRenderer-1.0.jar fr.inria.zvtm.clustering.AJTestSlave -b 1 -w 600 -h 400 -r 2 -c 2 &
java -Xmx2g -cp target/zvtm-cluster-proto-ajg-0.1.0-SNAPSHOT.jar:target/timingframework-1.0.jar:target/aspectjrt-1.5.4.jar:target/jgroups-2.7.0.GA.jar:target/commons-logging-1.1.jar:target/args4j-2.0.12.jar:target/pdfRenderer-1.0.jar fr.inria.zvtm.clustering.AJTestSlave -b 2 -w 600 -h 400 -r 2 -c 2 &

#we don't yet provide master/slave coordination,
#so let's just use this ugly hack for now
sleep 7

#run master
java -cp target/zvtm-cluster-proto-ajg-0.1.0-SNAPSHOT.jar:target/timingframework-1.0.jar:target/aspectjrt-1.5.4.jar:target/jgroups-2.7.0.GA.jar:target/commons-logging-1.1.jar:target/args4j-2.0.12.jar fr.inria.zvtm.clustering.examples.TestColorRect -w 2500 -h 800 -x 50 -y 20
#java -cp target/zvtm-cluster-proto-ajg-0.1.0-SNAPSHOT.jar:target/timingframework-1.0.jar:target/aspectjrt-1.5.4.jar:target/jgroups-2.7.0.GA.jar:target/commons-logging-1.1.jar:target/args4j-2.0.12.jar:target/pdfRenderer-1.0.jar fr.inria.zvtm.clustering.examples.PDFViewer /tmp/aspectj.pdf 

