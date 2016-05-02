#!/bin/sh
java -cp target/zvtm-fits-0.2.0-SNAPSHOT.jar:target/zvtm-core-0.11.2-SNAPSHOT.jar:target/timingframework-1.0.jar fr.inria.zvtm.fits.filters.FilterVisualizer "$@" 
