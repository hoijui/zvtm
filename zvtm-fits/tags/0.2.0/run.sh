#!/bin/sh
#java -Xmx2048M -Xms512M -Dcom.sun.media.jai.disableMediaLib=true -cp .:target/zvtm-core-0.11.2-SNAPSHOT.jar:target/zvtm-fits-0.1.4-SNAPSHOT.jar:target/jsky-3.0.jar:target/hcompress-0.52.jar fr.inria.zvtm.fits.examples.JSkyFitsExample "$@"
#java -Xmx2048M -Xms512M -cp .:target/zvtm-core-0.11.2-SNAPSHOT.jar:target/zvtm-fits-0.1.4-SNAPSHOT.jar:target/jsky-3.0.jar:target/hcompress-0.52.jar fr.inria.zvtm.fits.examples.JSkyFitsExample "$@"
#java -Xmx2048M -Xms512M -Dcom.sun.media.jai.disableMediaLib=true -cp .:target/zvtm-core-0.11.2-SNAPSHOT.jar:target/zvtm-fits-0.1.4-SNAPSHOT.jar:target/jsky-3.0.jar:target/hcompress-0.52.jar fr.inria.zvtm.fits.examples.FitsExample "$@"

#java -Xmx2048M -Xms512M -cp .:target/zvtm-core-0.11.2-SNAPSHOT.jar:target/zvtm-fits-0.1.4-SNAPSHOT.jar:target/jsky-3.0.jar:target/hcompress-0.52.jar fr.inria.zvtm.fits.examples.FitsExample "$@"

JARS="target/jsky-3.0.jar"
JARS=$JARS":target/args4j-2.0.29.jar"
JARS=$JARS":target/hcompress-0.52.jar"
JARS=$JARS":target/zvtm-core-0.12.0-SNAPSHOT.jar"
JARS=$JARS":target/zvtm-fits-0.2.0-SNAPSHOT.jar"
JARS=$JARS":target/jep-3.5.2.jar"

java -Xmx4096M -Xms1024M -Dcom.sun.media.jai.disableMediaLib=true -Djava.library.path=/Library/Python/2.7/site-packages/jep  -cp $JARS fr.inria.zvtm.fits.examples.JSkyFitsExample "$@"
