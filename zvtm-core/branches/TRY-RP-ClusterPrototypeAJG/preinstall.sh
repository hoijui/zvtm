#!/bin/sh
#install pdf-renderer (http://pdf-renderer.dev.java.net) into local
#maven repository as it is currently not provided by any big online repository.

wget https://pdf-renderer.dev.java.net/demos/latest/PDFRenderer.jar && mvn install:install-file -Dfile=PDFRenderer.jar -DgroupId=com.sun.pdfrenderer -DartifactId=pdfRenderer -Dversion=1.0 -Dpackaging=jar	&& rm PDFRenderer.jar 

