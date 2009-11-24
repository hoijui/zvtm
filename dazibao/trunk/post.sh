#!/bin/bash
#name of the generated file is received as the first script argument
#convert cups-pdf generated file into one PNG image per document page
#make an HTTP request to the master Dazibao program (DazBoard) for
#each PNG file.

#Dazibao uses two HTTP servers:
# - one server embedded in DazBoard (ZVTMCluster master)
# - one that serves the contents of OUT_IMG_DIR for the slaves' benefit
DAZBOARD_SERVER=127.0.0.1
DAZBOARD_PORT=3444
IMGDATA_SERVER=127.0.0.1
IMGDATA_PORT=4555

OUT_IMG_DIR=/tmp/dazibao/images
IMG_NAME_PREFIX=`whoami`_`date +%s`_`basename $1 .pdf`

convert $1 $OUT_IMG_DIR/${IMG_NAME_PREFIX}_%04d.png

#make an HTTP request per output file
for file in `ls $OUT_IMG_DIR/${IMG_NAME_PREFIX}*`
do
    wget --post-data image=http://$IMGDATA_SERVER:$IMGDATA_PORT/images/`basename $file` http://$DAZBOARD_SERVER:$DAZBOARD_PORT/addpage
done

