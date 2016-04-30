#!/bin/sh

JARS="target/jsky-3.0.jar"
JARS=$JARS":target/args4j-2.0.29.jar"
JARS=$JARS":target/hcompress-0.52.jar"
JARS=$JARS":target/zvtm-core-0.12.0-SNAPSHOT.jar"
JARS=$JARS":target/zvtm-fits-0.2.0-SNAPSHOT.jar"

java -cp $JARS fr.inria.zvtm.fits.examples.PIX2WCS "$@"
