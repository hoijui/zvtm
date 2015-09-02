#!/bin/bash

PATH=/home/wall/zuist-scenes/astro/fits

## Ks
PROC=Ks

./projection.py $PATH/$PROC/v20100411_00944_st_tl.fit $PATH/$PROC/v20100411_00980_st_tl.fit 0944_0980.fits &
./projection.py $PATH/$PROC/v20100411_00908_st_tl.fit $PATH/$PROC/v20100411_01016_st_tl.fit 0908_1016.fits &
./projection.py $PATH/$PROC/v20100420_00370_st_tl.fit $PATH/$PROC/v20100411_01052_st_tl.fit 0370_1052.fits &
./projection.py $PATH/$PROC/v20100411_00872_st_tl.fit $PATH/$PROC/v20110508_00412_st_tl.fit 0872_0412.fits &

wait

./projection.py 0944_0980.fits 0908_1016.fits 0944_0980_0908_1016.fits
rm 0944_0980.fits 0908_1016.fits

./projection.py 0944_0980_0908_1016.fits 0370_1052.fits 0944_0980_0908_1016_0370_1052.fits
rm 0370_1052.fits 0944_0980_0908_1016.fits

./projection.py 0944_0980_0908_1016_0370_1052.fits 0872_0412.fits 0944_0980_0908_1016_0370_1052_0872_0412.fits
rm 0944_0980_0908_1016_0370_1052.fits 0872_0412.fits


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


## J
PROC=J

./projection.py $PATH/$PROC/v20100411_00956_st_tl.fit $PATH/$PROC/v20100411_00992_st_tl.fit 0956_0992.fits &
./projection.py $PATH/$PROC/v20100411_00920_st_tl.fit $PATH/$PROC/v20100411_01028_st_tl.fit 0920_1028.fits &
./projection.py $PATH/$PROC/v20100420_00382_st_tl.fit $PATH/$PROC/v20100411_01064_st_tl.fit 0382_1064.fits &
./projection.py $PATH/$PROC/v20100411_00884_st_tl.fit $PATH/$PROC/v20110508_00424_st_tl.fit 0884_0424.fits &

wait

./projection.py 0956_0992.fits 0920_1028.fits 0956_0992_0920_1028.fits
rm 0956_0992.fits 0920_1028.fits

./projection.py 0956_0992_0920_1028.fits 0382_1064.fits 0956_0992_0920_1028_0382_1064.fits
rm 0956_0992_0920_1028.fits 0382_1064.fits

./projection.py 0956_0992_0920_1028_0382_1064.fits 0884_0424.fits 0956_0992_0920_1028_0382_1064_0884_0424.fits
rm 0956_0992_0920_1028_0382_1064.fits 0884_0424.fits

