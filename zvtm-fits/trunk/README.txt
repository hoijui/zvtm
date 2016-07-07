JEP is not available in any Maven repository for obvious reasons.

* Get it from https://github.com/mrj0/jep

* python setup.py build install

* install the JAR files in your local repository using the following command line:
    mvn install:install-file -Dfile=jep-3.5.3.jar -DgroupId=jep -DartifactId=jep -Dversion=3.5.3 -Dpackaging=jar

Update your JVM launch scripts to point to JEP with, e.g.:

-Djava.library.path=/Library/Python/2.7/site-packages/jep


Mac OS X:

* link to it (LD_LIBRARY_PATH, or symbolic link like:
  ln -sf libjep.so /Library/JavaExtensions/libjep.jnilib)

Linux:

Set environment variable LD_PRELOAD with the value returned by the following command:

ldd libjep.so | grep libpython | awk '{ print "export LD_PRELOAD="$3}'

libjep.so will typically be in:
  /usr/local/lib/python2.7/dist-packages/jep/libjep.so

Note: code tested mainly with Python 2.7, but that should be compatible with Python 3.x
