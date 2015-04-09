#!/bin/bash

PATH=/home/wall/zuist-scenes/astro/fits


## H
PROC=H


./projection.py $PATH/$PROC/v20100411_00932_st_tl.fit $PATH/$PROC/v20100411_00968_st_tl.fit 0932_0968.fits &
./projection.py $PATH/$PROC/v20100411_00896_st_tl.fit $PATH/$PROC/v20100411_01004_st_tl.fit 0896_1004.fits &
./projection.py $PATH/$PROC/v20100420_00358_st_tl.fit $PATH/$PROC/v20100411_01040_st_tl.fit 0358_1040.fits &
./projection.py $PATH/$PROC/v20100411_00860_st_tl.fit $PATH/$PROC/v20110508_00400_st_tl.fit 0860_0400.fits &

wait

./projection.py 0932_0968.fits 0896_1004.fits 0932_0968_0896_1004.fits
rm 0932_0968.fits 0896_1004.fits

./projection.py 0932_0968_0896_1004.fits 0358_1040.fits 0932_0968_0896_1004_0358_1040.fits
rm 0358_1040.fits 0932_0968_0896_1004.fits

./projection.py 0932_0968_0896_1004_0358_1040.fits 0860_0400.fits 0932_0968_0896_1004_0358_1040_0860_0400.fits
rm 0932_0968_0896_1004_0358_1040.fits 0860_0400.fits