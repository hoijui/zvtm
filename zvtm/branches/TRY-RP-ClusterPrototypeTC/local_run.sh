#!/bin/bash

# Run a clustered demo on a local machine, with 
# one master application and 3 slaves

# Run master
mvn -DactiveNodes=master tc:run&

# Run slaves
for sn in {0..3}
do
  mvn -DactiveNodes=slave -DstartServer=false -DslaveNum=$sn tc:run&
done

