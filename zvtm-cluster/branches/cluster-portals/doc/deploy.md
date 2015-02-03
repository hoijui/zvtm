# Deploying zvtm-cluster

Installing zvtm-cluster on a new wall requires the following steps:

* Obtain zvtm-core and zvtm-cluster
* Build zvtm-core and zvtm-cluster
* Deploy zvtm-cluster on the wall
* Create a run script for the slaves

Running an application on a display wall requires the following steps:

* Launch the slaves
* Launch the master

## Obtaining zvtm and zvtm-cluster

It is advised to work from the SVN trunk, both for `zvtm-core` and `zvtm-cluster`.

Grab zvtm-core from the repository: 

`svn co https://zvtm.svn.sourceforge.net/svnroot/zvtm/zvtm-core/trunk zvtm-core`

Grab zvtm-cluster from the repository:

`svn co https://zvtm.svn.sourceforge.net/svnroot/zvtm/zvtm-cluster/trunk zvtm-cluster`

Build and install zvtm-core:

`mvn clean install`

Build zvtm-cluster:

`mvn clean package`

## Setting up access to the wall machines

You should be able to access each wall machine through `ssh`. Two steps require `ssh`: deploying the slaves, and launching the slave executables.

> You should set up a password-less access to the wall machines, otherwise manipulations will be tedious. Use public key authentication for instance (and possibly execute `ssh-add` to add a key to your ssh agent on the master machine). 

## A simple deployment script

Deploying zvtm-cluster can be done by copying the zvtm-cluster and dependencies binaries on each wall machine (that is, the content of the `zvtm-cluster/trunk` directory, with the exception of the `zvtm-core` jar -- more on this in the section "Gotchas - Troubleshooting").

There is a simple deployment script example bundled with zvtm-cluster.
It lives in `src/main/resources/scripts/deploy.sh`

## zvtm-cluster screen layout

When writing the slave startup script, you should give each slave a block id. Blocks are laid column-wise. Each slave application infers its position within the walls from its block id. Block id is passed to `SlaveApp` by the `-b` option. The following figure illustrates the zvtm-cluster screen layout.

![Cluster screen layout](cv.png "Cluster screen layout")

## Running an application

Running an application is done in two steps: launch the slave instances, then launch the master.

### Launching the slaves

Launching the slaves is usually done over ssh. There is an example launch script in `src/main/resources/scripts/cluster_run.sh`. I usually copy it to the zvtm-cluster root on the master machine and modify it to suit the application/wall config.

Important slave options:

`-b` block id

`-f` fullscreen

`-d` device (screen name, when fullscreen is enabled)

`-u` undecorated (to simulate full screen if needed)

`-n` group name (should match the string given in the master application `setMaster()` call)

### Launching the master

Launching the master is the easiest part. Wait until the slaves have initialized, then launch the application as for local development. The only change is the bind address if you specified a local one (see Gotchas). There is an example run script in `src/main/resources/scripts/master_run.sh`.

### Killing the slaves

Normally, stopping the master should kill the slaves after a while. If it is not the case, you could write a kill script that would invoke e.g. `killall -9 java` on each wall machine (provided no important java-based daemon is running on the wall). On WILD, the `wildo` command can be used, e.g `wildo killall -9 java`.

### Borders

If you want to take the screen bezels into account, add the bezel width and height to your `blockWidth` and `blockHeight` dimensions.

## Gotchas - Troubleshooting

Under Linux, it may be useful to give an explicit bind address to the master, e.g. use `-Djgroups.bind_addr="123.44.55.66"` when launching the master (replace the address by the IP of the interface that you wish to use).

By default, some traces are not shown. You can get richer traces by putting a log4j configuration file on your classpath. There is an example configuration file in `src/main/resources/config/log4j.properties`.
