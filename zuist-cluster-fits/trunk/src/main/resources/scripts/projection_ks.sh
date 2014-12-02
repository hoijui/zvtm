#!/bin/bash

PATH=/home/wall/zuist-scenes/astro/fits


## Ks
PROC=Ks

#./projection.py $PATH/$PROC/v20100411_00944_st_tl.fit $PATH/$PROC/v20100411_00980_st_tl.fit 0944_0980.fits &
#./projection.py $PATH/$PROC/v20100411_00908_st_tl.fit $PATH/$PROC/v20100411_01016_st_tl.fit 0908_1016.fits &
#./projection.py $PATH/$PROC/v20100420_00370_st_tl.fit $PATH/$PROC/v20100411_01052_st_tl.fit 0370_1052.fits &
./projection.py $PATH/$PROC/v20100411_00872_st_tl.fit $PATH/$PROC/v20110508_00412_st_tl.fit 0872_0412.fits 


wait

#./projection.py 0944_0980.fits 0908_1016.fits 0944_0980_0908_1016.fits
#rm 0944_0980.fits 0908_1016.fits

#./projection.py 0944_0980_0908_1016.fits 0370_1052.fits 0944_0980_0908_1016_0370_1052.fits
#rm 0944_0980_0908_1016.fits 0370_1052.fits

./projection.py 0944_0980_0908_1016_0370_1052.fits 0872_0412.fits 0944_0980_0908_1016_0370_1052_0872_0412.fits
rm 0944_0980_0908_1016_0370_1052.fits 0872_0412.fits

