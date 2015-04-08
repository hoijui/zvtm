#!/bin/bash

#PATH=/home/wall/zuist-scenes/astro/fits
PATH=/home/fdelcampo/zuist-scenes-local/fits

## J
PROC=J

./projection.py $PATH/$PROC/v20100411_00956_st_tl.fit $PATH/$PROC/v20100411_00992_st_tl.fit 0956_0992.fits noproc &
./projection.py $PATH/$PROC/v20100411_00920_st_tl.fit $PATH/$PROC/v20100411_01028_st_tl.fit 0920_1028.fits noproc &
./projection.py $PATH/$PROC/v20100420_00382_st_tl.fit $PATH/$PROC/v20100411_01064_st_tl.fit 0382_1064.fits noproc &
./projection.py $PATH/$PROC/v20100411_00884_st_tl.fit $PATH/$PROC/v20110508_00424_st_tl.fit 0884_0424.fits noproc 


./projection.py 0956_0992.fits 0920_1028.fits 0956_0992_0920_1028.fits nopro

./projection.py 0956_0992_0920_1028.fits 0382_1064.fits 0956_0992_0920_1028_0382_1064.fits noproc

#./projection.py 0956_0992_0920_1028_0382_1064.fits 0884_0424.fits 0956_0992_0920_1028_0382_1064_0884_0424.fits
