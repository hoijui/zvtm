#!/bin/sh
java -Xmx2048M -Xms512M -cp .:target/zvtm-core-0.11.0-SNAPSHOT.jar:target/zvtm-fits-0.1.3-SNAPSHOT.jar:target/jsky-3.0.jar:target/hcompress-0.52.jar fr.inria.zvtm.fits.examples.JSkyFitsExample "$@" 
