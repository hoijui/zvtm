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

## Running an application

Running an application is done in two steps: launch the slave instances, then launch the master.

### Launching the slaves

Launching the slaves is usually done over ssh.

Important slave options:

### Launching the master

Launching the master is the easiest part. Wait until the slaves have initialized, then ...

### Killing the slaves

Normally, stopping the master should kill the slaves after a while. If it is not the case, you could write a kill script that would invoke e.g. `killall -9 java` on each wall machine (provided no important java-based daemon is running on the wall). On WILD, the `wildo` command can be used, e.g `wildo killall -9 java`.

## Gotchas

Under Linux, it may be useful to give an explicit bind address to the master, e.g. use `-Djgroups.bind_addr="123.44.55.66"` when launching the master (replace the address by the IP of the interface that you wish to use).


