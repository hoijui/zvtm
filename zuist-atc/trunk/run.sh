#!/bin/sh

java -Xms512M -Xmx1024M -classpath "lib/JFontChooser.jar:lib/antlr.jar:\
lib/colt.jar:\
lib/commons-beanutils-1.7.0.jar:lib/commons-logging-1.0.3.jar:lib/concurrent-1.3.4.jar:lib/concurrent.jar:lib/geoapi-nogenerics-2.1.0.jar:lib/gt2-api-2.4.3.jar:lib/gt2-epsg-wkt-2.4.3.jar:lib/gt2-main-2.4.3.jar:lib/gt2-metadata-2.4.3.jar:lib/gt2-referencing-2.4.3.jar:lib/gt2-shapefile-2.4.3.jar:lib/infovis.jar:lib/intset.jar:lib/jdom-1.0.jar:lib/jsr108-0.01.jar:lib/jts-1.8.jar:lib/linkslider.jar:lib/log4j.jar:lib/swing-worker.jar:lib/vecmath-1.3.1.jar:lib/velocity-1.4.jar:lib/xercesImpl-2.8.1.jar:lib/xml-apis-1.3.03.jar:lib/xml-writer.jar:lib/xmlParserAPIs-2.6.2.jar:lib/xp.jar:lib/xpp3_min-1.1.3.4.O.jar:lib/xstream-1.2.2.jar:lib/zuist-atc.jar:lib/zuist-engine-0.1.1.jar:lib/zvtm-0.9.8.jar" fr.inria.zuist.app.atc.ATCExplorer "$@"
