# ZVTM Cluster documentation

## Building zvtm-cluster
### Basic build
The recommended way of obtaining zvtm-cluster is from the SVN trunk. To check out zvtm-cluster:

` svn co https://zvtm.svn.sourceforge.net/svnroot/zvtm/zvtm-cluster/trunk `

The build process is managed by [Maven](http://maven.apache.org/). Assuming a recent version of maven2 is installed on your machine, executing

` mvn clean package `

at the project root will build the software and

` mvn install ` 

will install it in your local repository.

### Build options
It is possible to build a jar containing the library, plus every dependency.
To do so, execute
` mvn assembly:assembly `

This will generate `zvtm-cluster-x.y.z-jar-with-dependencies.jar`

### Agile2d pipeline
zvtm-cluster can use the Agile2d pipeline through zvtm-agile2d.
To build zvtm-cluster with Agile2d support, run

` mvn -Pagile2d package `

The generic slave ` Agile2dSlaveApp ` should be used instead of ` SlaveApp `

##Launching an executable with maven
` mvn exec:java -Dexec.mainClass="com.example.Main" [-Dexec.args="argument1"] ... `

For instance, you may launch the generic slave with 

` mvn exec:java -Dexec.mainClass="fr.inria.zvtm.cluster.SlaveApp" `

You may also try with

` mvn exec:exec -Dexec.executable="java"  -Dexec.args="-Xmx2g -cp %classpath fr.inria.zvtm.cluster.SlaveApp" `

## Features (supported/unsupported)
### Supported
* Most glyphs (however `VImage` instances should be replaced by `ClusteredImage` instances for speed - `VImage` is supported but will usually be slower)
* Multiple virtual spaces
* Multiple views and cameras
* Animation framework

### Unsupported
* Portals (portal support is planned, for `CameraPortal` only at first)
* Lenses (due to their implementation, supporting lenses on large display would be too memory-heavy)
*  `VCursor` (we are investigating `VCursor` support, or a suitable replacement)

## Porting an application to zvtm-cluster
Porting an application is done in three steps:
* If you use Maven, declare a dependency to ZVTM-cluster in your pom file
 
    <dependency>
      <groupId>fr.inria.zvtm</groupId>
      <artifactId>zvtm-cluster</artifactId>
      <version>0.2.6-SNAPSHOT</version>
    </dependency>

* At the very beginning of your program, call ` VirtualSpaceManager.INSTANCE.setMaster() `
* Create one or more ClusteredView instances

Examples are located in the directory ` src/main/java/fr/inria/zvtm/cluster/examples ` . Studying the examples is a good way to get started.

## API additions to ZVTM
* VirtualSpaceManager.setMaster()
* VirtualSpaceManager.addClusteredView()
* VirtualSpaceManager.stop()
* ClusterGeometry class
* ClusteredView class
* ClusteredImage class

## Exclusions
XXX not terribly elegant.
It may be necessary to declare your dependency on zvtm-cluster in the following 
fashion:
 
    <dependency>
       <groupId>fr.inria.zvtm</groupId>
       <artifactId>zvtm-cluster</artifactId>
       <version>0.2.0-SNAPSHOT</version>
       <exclusions>
          <exclusion>
             <groupId>fr.inria.zvtm</groupId> 
             <artifactId>zvtm</artifactId>
          </exclusion>
       </exclusions>
    </dependency>
 

This is because zvtm-cluster depends on zvtm, but is not an add-on. Rather, it replaces zvtm and augments it with clustering. Since you want to avoid linking to the non-clustered classes in a clustered project, you may need to declare this exclusion.

## About clustered views
A clustered view extends the concept of a ZVTM view (i.e. a window) to display wall. A clustered view is divided into blocks. A block is displayed by a single slave instance.

## About clustered images
There are two possibilities for using images: `ClusteredImage` and the normal zvtm `VImage`.
`ClusteredImage` is defined in the `fr.inria.zvtm.cluster` package.
These images are constructed by providing an URL. When transmitting these images to the slaves, only a pointer (URL) is passed, and the slave is given the task of obtaining the actual image (over an http connection, a shared drive, a common naming scheme...).

`VImage` instances are transmitted fully to the slaves (i.e. the bitmap is serialized). This is especially useful for resource images (icons) or images that cannot be put on a web server / shared drive etc.

## Deploying on WILD
See the example script ` deploy.sh ` located in ` src/main/resources/scripts `.
Deploying a pure-java build of zvtm-cluster (the default) is as simple as copying your build to the target machines; using native libraries, however, requires more effort (the correct objects for the wall machines should be deployed and referenced from the ` java.library.path ` variable).

## Running Applications
zvtm-cluster applications are split in two parts:

* a master application (the one that you write)
* a generic slave application `fr.inria.zvtm.cluster.SlaveApp`, one instance of which is typically run for each screen of the display wall.

> All slave instances should be running before the master application is launched.

## Properties
These are the system properties currently understood by zvtm-cluster. Define them in the standard way, i.e. by using ` -D ` on the command line, e.g. 
` -Dzvtm.cluster.channel_conf="foo.xml" `

* ` zvtm.cluster.channel_conf `

> TODO: isolate properties in a package?

## Slave client options
A summary of the options supported by SlaveApp can be displayed by executing

` java fr.inria.zvtm.cluster.SlaveApp --help `

This summary is reproduced here:

    Usage: SlaveApp [options] where options are:
     -b (--block) N         : view block number (slave index)
     -d (--device-name) VAL : use chosen device (fullscreen only)
     -f (--fullscreen)      : open in full screen mode
     -g (--debug)           : show ZVTM debug information
     -h (--help)            : print this help message and exit
     -n (--app-name) VAL    : application name (should match master program)
     -o                     : enable OpenGL acceleration
     -u (--undecorated)     : remove window decorations (borders)
     -x (--x-offset) N      : window x offset (ignored if fullscreen set)
     -y (--y-offset) N      : window y offset (ignored if fullscreen set)


> Note about OpenGL acceleration: for the acceleration to be enabled, you must define sun.java2d.opengl in addition to passing the ` -o ` option to SlaveApp, i.e., ` java -Dsun.java2d.opengl=True fr.inria.zvtm.cluster.SlaveApp -o (...)`. This only works under Linux and Windows (the OGL pipeline is not yet available for Mac OS X).

## OpenGL information and troubleshooting

1. The example ` OGLClusterExample.java ` is used to trace the OpenGL rendering capabilities with zvtm-cluster under Linux. This example contains paths, simple polygons, text, and image samples and therefore we can check if opengl calls are made for the drawing operations associated to each of those graphical entities.

2. On each slave machine, we launch two slave applications through the script : `/local_slave_ogl.sh -n OGLClusterExample -u -d :0.0 -b 0`, where "d" indicates the screen name id and "u" indicates undecorated windows (since we couldn't get fullscreen working simultaneously on two screens).

3. In `local_slave_ogl.sh`, when calling the java `SlaveApp`, we must supply the arguments  

`-Dsun.java2d.opengl=True` (enabling trace of opengl operations) and 

`-Dsun.java2d.opengl.fbobject=false` (deactivating the use of `FrameBufferObject`, otherwise the rendering fails).

4. On frontal-1, we  launch the script `./local_master_ogl.sh -bw 2560 -bh 1600 -r 1 -c 2`, where "bw" and "bh" indicate the dimensions of the window of each slave view).

5. The configuration of the slave machine used in the tests: Ubuntu 9.10, NVIDIA  GeForce 8800 GT/PCI/SSE2, OpenGL version 3.0.0, driver version NVIDIA 185.18.36, GLX version 1.4.

6. Trace messages (we get the same output on both slaveapps): 

` sun.java2d.loops.FillRect::FillRect(AnyColor, SrcNoEa, AnyInt)`

` sun.java2d.loops.Blit::Blit(IntRgb, SrcNoEa, IntRgb)`

` sun.java2d.loops.DrawRect::DrawRect(AnyColor, SrcNoEa, AnyInt)`

` sun.java2d.loops.FillRect::FillRect(AnyColor, SrcNoEa, AnyInt)`

` sun.java2d.loops.DrawRect::DrawRect(AnyColor, SrcNoEa, AnyInt)`

` sun.java2d.loops.FillPath::FillPath(AnyColor, SrcNoEa, AnyInt)`

` sun.java2d.loops.DrawPath::DrawPath(AnyColor, SrcNoEa, AnyInt)`

` sun.java2d.loops.DrawGlyphList::DrawGlyphList(AnyColor, SrcNoEa, AnyInt)`

` sun.java2d.opengl.OGLSwToSurfaceBlit::Blit(IntRgb, AnyAlpha, "OpenGL Surface")`

` sun.java2d.opengl.OGLSurfaceToSurfaceBlit::Blit("OpenGL Surface", AnyAlpha, "OpenGL Surface")`

7. On WILD slaves (as of Feb 2011), the argument `-Dsun.java2d.opengl.fbobject=true` works and the last line of the above trace output becomes:

`sun.java2d.opengl.OGLRTTSurfaceToSurfaceBlit::Blit("OpenGL Surface (render-to-texture)", AnyAlpha, "OpenGL Surface")`

## Graceful shutdown
The method ` VirtualSpaceManager.stop() ` has been added to try to initiate an orderly shutdown of the whole cluster.

## Logging: configuring log4j
Since we do not want to force a particular logging library on zvtm-cluster users, logging is done through [slf4j](http://www.slf4j.org/). At the moment, we use the log4j back-end to actually perform the work. A sample properties file `log4j.properties` is provided with zvtm-cluster.
Modify it to suit your purposes. 

> This properties file should be in your classpath.

## Quirks and troubleshooting
* When launching an application, the slave views do not display anything

Check the application name property: the cluster name for your application and the generic slaves should match. That is, if you called

` VirtualSpaceManager.INSTANCE.setMaster("FooApp") `

in your application, you should start the generic slaves using

` java fr.inria.zvtm.cluster.SlaveApp -n FooApp `

(classpath and other options omitted).

* `DPath` clustering is only available when using java 1.6+. This is because `GeneralPath` was not serializable before java 1.6.

## Generic Java troubleshooting links and tips:

[Java 6 quick troubleshooting tips](http://java.sun.com/javase/6/webnotes/trouble/other/matrix6-Unix.html)

[hprof](http://java.sun.com/developer/technicalArticles/Programming/HPROF.html)

Discover hprof options by running:
`
java -Xrunhprof:help
`

Start VM with `-XX:+HeapDumpOnOutOfMemoryError`; if OutOfMemoryError is thrown, VM generates a heap dump. 

##Developer tips/best practices
###  Branches
When creating a branch (for example a throwaway branch for a demo),
rename it (change the artifact id in the maven project file). This way, you will
avoid deploying the wrong code by mistake. Of course, if the branch is a feature
branch that you intend to merge back into the trunk, change back the artifact id when merging.

###  Version numbers
We are still at 0.something. When moving to 1.0 it may be useful to adopt
[semantic versioning](http://semver.org/).

## Instrumenting zvtm-cluster for performance measurement
###  Running with YourKit Java Profiler
[YJP](http://www.yourkit.com) is a commercial java profiler. In-Situ owns a licence for this software.
[Here](http://www.yourkit.com/docs/80/help/agent.jsp) are the instructions needed to run programs under YJP.
It mostly boils down to running 
`
java -agentlib:yjpagent FooClass
`
after the yjp agent library directory has been added to your `LD_LIBRARY_PATH`.
One interesting feature of YJP is that it allows profiling of remote applications.
Connecting to a remote application is described [here](http://www.yourkit.com/docs/80/help/connect.jsp).

### Running with VisualVM
[VisualVM](https://visualvm.dev.java.net/) allows monitoring of the Java virtual machine that runs an application (heap size, occupancy, thread states, ...) as well as basic CPU and memory profiling. To install VisualVM under Debian, run

`apt-get install visualvm `

Then run VisualVM with

` jvisualvm `

VisualVM allows monitoring remote applications using jstatd, but not remote profiling.
This is described [here](https://visualvm.dev.java.net/gettingstarted.html).

## Network monitoring with JMX and JGroups
[JMX support for JGroups](http://community.jboss.org/wiki/JMX)

## Resources
[ZVTM website (sourceforge)](http://zvtm.sf.net). Before using zvtm-cluster, you should have basic familiarity with ZVTM.

[zvtm-cluster](ZVTMCluster) presentation page on this wiki. 

[JGroups wiki](http://community.jboss.org/wiki/JGroups)

## ToDo List
* portal support
* picking and VCursor support
* better dependency handling (maven)
* change cluster geometry dynamically

## Glossary
### Block 
The part of a clustered view that can be displayed by a single machine 
(typically the area covered by one or two screens).

### Camera 
A Camera is a zvtm concept. A Camera has a position in 3d space, but always looks straight down. Camera creation and movements are propagated to slaves, but with an offset applied to match the slave's position within the wall.

### Cluster Geometry  
Describes a display wall. The most important pieces of information contained in a ClusterGeometry are the number of screens (rows and columns) and the size of each screen (in pixels).

### Clustered View  
A clustered view extends the concept of a ZVTM view (i.e. a window) to display wall.
A clustered view is divided into blocks. A block is displayed by a single slave instance.

### Delta  
A Delta represents a change from an application value (state) to the next.
Examples of Deltas are: creating a camera, creating a virtual space, adding a glyph to a virtual space, changing a glyph's border color...

### Glyph  
Graphical object. Examples of glyphs are an image, a rectangle, a circle, a path.
Glyphs can be manipulated in a homogeneous fashion (i.e. resize() resizes a Circle or an image, moveTo() moves any glyph...)

### Identifiable  
An Identifiable is an object that may be replicated across the cluster.

### Master  
A Master is a (bespoke) ZVTM application which declares itself as a master.
By doing so, it broadcasts changes to its state to all machines on the cluster. 

### Slave  
A Slave is a generic display application that displays part of a clustered view.
A Slave is identified by its block id.

### Virtual Space
An infinite plane on which Glyphs are laid.
An arbitrary number of virtual spaces can be observed in a View (through the use of Cameras).

