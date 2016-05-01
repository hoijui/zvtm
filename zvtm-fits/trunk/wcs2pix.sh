#!/bin/sh

JARS=":target/zvtm-fits-0.2.0-SNAPSHOT.jar"
JARS=$JARS":target/jep-3.5.2.jar"

java -Djava.library.path=/Library/Python/2.7/site-packages/jep -cp $JARS fr.inria.zvtm.fits.examples.WCS2PIX "$@"
