#!/bin/sh
java -Xmx1024M -Xms512M -cp target/gt2-api-2.4.3.jar:target/gt2-epsg-wkt-2.4.3.jar:target/gt2-main-2.4.3.jar:target/gt2-metadata-2.4.3.jar:target/gt2-referencing-2.4.3.jar:target/gt2-shapefile-2.4.3.jar:target/zuist-wm-0.2.0-SNAPSHOT.jar fr.inria.zuist.app.wm.AWTTest "$@"
