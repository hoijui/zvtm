#!/bin/sh
#java -Xmx2048M -Xms512M -Dcom.sun.media.jai.disableMediaLib=true -cp .:target/zvtm-core-0.11.2-SNAPSHOT.jar:target/zvtm-fits-0.1.4-SNAPSHOT.jar:target/jsky-3.0.jar:target/hcompress-0.52.jar fr.inria.zvtm.fits.examples.JSkyFitsExample "$@"
#java -Xmx2048M -Xms512M -cp .:target/zvtm-core-0.11.2-SNAPSHOT.jar:target/zvtm-fits-0.1.4-SNAPSHOT.jar:target/jsky-3.0.jar:target/hcompress-0.52.jar fr.inria.zvtm.fits.examples.JSkyFitsExample "$@"
#java -Xmx2048M -Xms512M -Dcom.sun.media.jai.disableMediaLib=true -cp .:target/zvtm-core-0.11.2-SNAPSHOT.jar:target/zvtm-fits-0.1.4-SNAPSHOT.jar:target/jsky-3.0.jar:target/hcompress-0.52.jar fr.inria.zvtm.fits.examples.FitsExample "$@"

#java -Xmx2048M -Xms512M -cp .:target/zvtm-core-0.11.2-SNAPSHOT.jar:target/zvtm-fits-0.1.4-SNAPSHOT.jar:target/jsky-3.0.jar:target/hcompress-0.52.jar fr.inria.zvtm.fits.examples.FitsExample "$@"

java -Xmx2048M -Xms512M -Dcom.sun.media.jai.disableMediaLib=true -cp .:target/zvtm-core-0.11.2.jar:target/zvtm-fits-0.1.4-SNAPSHOT.jar:target/jsky-3.0.jar:target/hcompress-0.52.jar fr.inria.zvtm.fits.examples.JSkyFitsExample "$@"
