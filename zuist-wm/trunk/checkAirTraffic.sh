#!/bin/sh
java -cp target/classes:target/antlr-runtime-3.2.jar fr.inria.zuist.app.wm.GMLParser data/airports/airtraffic_2004.gml

