#!/bin/sh
cd target/site
tar -cvf api.tar apidocs/*
gzip -c api.tar > api.tgz
scp api.tgz epietrig,zvtm@web.sourceforge.net:/home/groups/z/zv/zvtm/htdocs/
rm api.tgz
rm api.tar

