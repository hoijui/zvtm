#!/bin/sh
java -Xmx1024M -Xms512M -cp target/geonames-1.0.jar:target/zuist-wm-0.2.0-SNAPSHOT.jar fr.inria.zuist.app.wm.GeoNamesParser "$@"
