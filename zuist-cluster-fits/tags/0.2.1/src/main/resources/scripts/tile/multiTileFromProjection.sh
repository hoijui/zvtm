#!/bin/bash



mkdir fitsUC

## Ks
mkdir fitsUC/01052_0370
./imageTiler.py ~/zuist-scenes-local/fits/0370_1052.fits fitsUC/ -f -astropy -ts=500 -tl=7 -dx=-0 -dy=0 -tileprefix=01052_0370/tile- -idprefix=01052_0370 -layer=SceneKsSpace -minvalue=-3107.0 -maxvalue=70118.5
mv fitsUC/scene.xml fitsUC/scene_01052_0370.xml