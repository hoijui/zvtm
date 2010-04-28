#!/bin/bash

#utility script:
#tile the SIMU_8K image in 1024x1024 blocks
for i in {0..7} 
do
    for j in {0..7}
    do
        fitscopy SIMU_8k_8k.fits[`expr $i \* 1024 + 1`:`expr \( $i + 1 \) \* 1024`,`expr $j \* 1024 + 1`:`expr \( $j + 1 \) \* 1024`] tiles/SIMU_tiles_"$i"_"$j".fits
    done
done
