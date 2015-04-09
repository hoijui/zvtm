#!/bin/bash



mkdir fitsUC

## Ks
mkdir fitsUC/01052
./imageTiler.py ~/zuist-scenes-local/fits/Ks/v20100411_01052_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-38321 -dy=0 -tileprefix=01052/tile- -idprefix=01052 -layer=SceneKsSpace -minvalue=-3107.0 -maxvalue=70118.5
mv fitsUC/scene.xml fitsUC/scene_01052.xml

mkdir fitsUC/00980
./imageTiler.py ~/zuist-scenes-local/fits/Ks/v20100411_00980_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-25546 -dy=0 -tileprefix=00980/tile- -idprefix=00980 -layer=SceneKsSpace -minvalue=-3107.0 -maxvalue=70118.5
mv fitsUC/scene.xml fitsUC/scene_00980.xml

mkdir fitsUC/00944
./imageTiler.py ~/zuist-scenes-local/fits/Ks/v20100411_00944_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-12774 -dy=0 -tileprefix=00944/tile- -idprefix=00944 -layer=SceneKsSpace -minvalue=-3107.0 -maxvalue=70118.5
mv fitsUC/scene.xml fitsUC/scene_00944.xml

mkdir fitsUC/00872
./imageTiler.py ~/zuist-scenes-local/fits/Ks/v20100411_00872_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -tileprefix=00872/tile- -idprefix=00872 -layer=SceneKsSpace -minvalue=-3107.0 -maxvalue=70118.5
mv fitsUC/scene.xml fitsUC/scene_00872.xml


mkdir fitsUC/00370
./imageTiler.py ~/zuist-scenes-local/fits/Ks/v20100420_00370_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-38321 -dy=-15654 -tileprefix=00370/tile- -idprefix=00370 -layer=SceneKsSpace -minvalue=-3107.0 -maxvalue=70118.5
mv fitsUC/scene.xml fitsUC/scene_00370.xml

mkdir fitsUC/01016
./imageTiler.py ~/zuist-scenes-local/fits/Ks/v20100411_01016_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-25546 -dy=-15658 -tileprefix=01016/tile- -idprefix=01016 -layer=SceneKsSpace -minvalue=-3107.0 -maxvalue=70118.5
mv fitsUC/scene.xml fitsUC/scene_01016.xml

mkdir fitsUC/00908
./imageTiler.py ~/zuist-scenes-local/fits/Ks/v20100411_00908_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-12774 -dy=-15662 -tileprefix=00908/tile- -idprefix=00908 -layer=SceneKsSpace -minvalue=-3107.0 -maxvalue=70118.5
mv fitsUC/scene.xml fitsUC/scene_00908.xml

mkdir fitsUC/00412
./imageTiler.py ~/zuist-scenes-local/fits/Ks/v20110508_00412_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dy=-15654 -tileprefix=00412/tile- -idprefix=00412 -layer=SceneKsSpace -minvalue=-3107.0 -maxvalue=70118.5
mv fitsUC/scene.xml fitsUC/scene_00412.xml

cat fitsUC/scene_01052.xml fitsUC/scene_00980.xml fitsUC/scene_00944.xml fitsUC/scene_00872.xml fitsUC/scene_00370.xml fitsUC/scene_01016.xml fitsUC/scene_00908.xml fitsUC/scene_00412.xml > fitsUC/scene_Ks.xml


## H
mkdir fitsUC/01040
./imageTiler.py ~/zuist-scenes-local/fits/H/v20100411_01040_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-38321 -dy=0 -tileprefix=01040/tile- -idprefix=01040 -layer=SceneHSpace -minvalue=-2190.0 -maxvalue=68264.0
mv fitsUC/scene.xml fitsUC/scene_01040.xml

mkdir fitsUC/00968
./imageTiler.py ~/zuist-scenes-local/fits/H/v20100411_00968_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-25546 -dy=0 -tileprefix=00968/tile- -idprefix=00968 -layer=SceneHSpace -minvalue=-2190.0 -maxvalue=68264.0
mv fitsUC/scene.xml fitsUC/scene_00968.xml

mkdir fitsUC/00932
./imageTiler.py ~/zuist-scenes-local/fits/H/v20100411_00932_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-12774 -dy=0 -tileprefix=00932/tile- -idprefix=00932 -layer=SceneHSpace -minvalue=-2190.0 -maxvalue=68264.0
mv fitsUC/scene.xml fitsUC/scene_00932.xml

mkdir fitsUC/00860
./imageTiler.py ~/zuist-scenes-local/fits/H/v20100411_00860_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -tileprefix=00860/tile- -idprefix=00860 -layer=SceneHSpace -minvalue=-2190.0 -maxvalue=68264.0
mv fitsUC/scene.xml fitsUC/scene_00860.xml


mkdir fitsUC/00358
./imageTiler.py ~/zuist-scenes-local/fits/H/v20100420_00358_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-38321 -dy=-15654 -tileprefix=00358/tile- -idprefix=00358 -layer=SceneHSpace -minvalue=-2190.0 -maxvalue=68264.0
mv fitsUC/scene.xml fitsUC/scene_00358.xml

mkdir fitsUC/01004
./imageTiler.py ~/zuist-scenes-local/fits/H/v20100411_01004_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-25546 -dy=-15658 -tileprefix=01004/tile- -idprefix=01004 -layer=SceneHSpace -minvalue=-2190.0 -maxvalue=68264.0
mv fitsUC/scene.xml fitsUC/scene_01004.xml

mkdir fitsUC/00896
./imageTiler.py ~/zuist-scenes-local/fits/H/v20100411_00896_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-12774 -dy=-15662 -tileprefix=00896/tile- -idprefix=00896 -layer=SceneHSpace -minvalue=-2190.0 -maxvalue=68264.0
mv fitsUC/scene.xml fitsUC/scene_00896.xml

mkdir fitsUC/00400
./imageTiler.py ~/zuist-scenes-local/fits/H/v20110508_00400_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dy=-15654 -tileprefix=00400/tile- -idprefix=00400 -layer=SceneHSpace -minvalue=-2190.0 -maxvalue=68264.0
mv fitsUC/scene.xml fitsUC/scene_00400.xml

cat fitsUC/scene_01040.xml fitsUC/scene_00968.xml fitsUC/scene_00932.xml fitsUC/scene_00860.xml fitsUC/scene_00358.xml fitsUC/scene_01004.xml fitsUC/scene_00896.xml fitsUC/scene_00400.xml > fitsUC/scene_H.xml


## J
mkdir fitsUC/01064
./imageTiler.py ~/zuist-scenes-local/fits/J/v20100411_01064_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-38321 -dy=0 -tileprefix=01064/tile- -idprefix=01064 -layer=SceneJSpace -minvalue=-1114.0 -maxvalue=70728.75
mv fitsUC/scene.xml fitsUC/scene_01064.xml

mkdir fitsUC/00992
./imageTiler.py ~/zuist-scenes-local/fits/J/v20100411_00992_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-25546 -dy=0 -tileprefix=00992/tile- -idprefix=00992 -layer=SceneJSpace -minvalue=-1114.0 -maxvalue=70728.75
mv fitsUC/scene.xml fitsUC/scene_00992.xml

mkdir fitsUC/00956
./imageTiler.py ~/zuist-scenes-local/fits/J/v20100411_00956_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-12774 -dy=0 -tileprefix=00956/tile- -idprefix=00956 -layer=SceneJSpace -minvalue=-1114.0 -maxvalue=70728.75
mv fitsUC/scene.xml fitsUC/scene_00956.xml

mkdir fitsUC/00884
./imageTiler.py ~/zuist-scenes-local/fits/J/v20100411_00884_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -tileprefix=00884/tile- -idprefix=00884 -layer=SceneJSpace -minvalue=-1114.0 -maxvalue=70728.75
mv fitsUC/scene.xml fitsUC/scene_00884.xml


mkdir fitsUC/00382
./imageTiler.py ~/zuist-scenes-local/fits/J/v20100420_00382_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-38321 -dy=-15654 -tileprefix=00382/tile- -idprefix=00382 -layer=SceneJSpace -minvalue=-1114.0 -maxvalue=70728.75
mv fitsUC/scene.xml fitsUC/scene_00382.xml

mkdir fitsUC/01028
./imageTiler.py ~/zuist-scenes-local/fits/J/v20100411_01028_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-25546 -dy=-15658 -tileprefix=01028/tile- -idprefix=01028 -layer=SceneJSpace -minvalue=-1114.0 -maxvalue=70728.75
mv fitsUC/scene.xml fitsUC/scene_01028.xml

mkdir fitsUC/00920
./imageTiler.py ~/zuist-scenes-local/fits/J/v20100411_00920_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dx=-12774 -dy=-15662 -tileprefix=00920/tile- -idprefix=00920 -layer=SceneJSpace -minvalue=-1114.0 -maxvalue=70728.75
mv fitsUC/scene.xml fitsUC/scene_00920.xml

mkdir fitsUC/00424
./imageTiler.py ~/zuist-scenes-local/fits/J/v20110508_00424_st_tl.fit fitsUC/ -f -astropy -ts=500 -tl=6 -dy=-15654 -tileprefix=00424/tile- -idprefix=00424 -layer=SceneJSpace -minvalue=-1114.0 -maxvalue=70728.75
mv fitsUC/scene.xml fitsUC/scene_00424.xml

cat fitsUC/scene_01064.xml fitsUC/scene_00992.xml fitsUC/scene_00956.xml fitsUC/scene_00884.xml fitsUC/scene_00382.xml fitsUC/scene_01028.xml fitsUC/scene_00920.xml fitsUC/scene_00424.xml > fitsUC/scene_J.xml