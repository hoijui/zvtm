#!/bin/sh
java -Xmx1024M -Xms512M -Dsun.java2d.opengl=True -jar target/zuist-wm-0.1.1-SNAPSHOT.jar "$@" 

