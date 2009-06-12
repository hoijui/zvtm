#!/bin/sh
java -Xmx2048M -Xms512M -cp target/zuist4wild-0.1.0-SNAPSHOT.jar fr.inria.wild.zuist.Controller "$@" 
