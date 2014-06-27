#!/bin/bash

mkdir v20100411

mkdir v20100411/01052
./imageTiler.py ~/zuist-scenes-tmp/fits/v20100411_01052_st_tl.fit v20100411/ -astropy -ts=500 -tl=6 -f -dx=-38321 -dy=0 -tileprefix=01052/tile- -idprefix=01052 -f
mv v20100411/scene.xml v20100411/scene_01052.xml

mkdir v20100411/00980
./imageTiler.py ~/zuist-scenes-tmp/fits/v20100411_00980_st_tl.fit v20100411/ -astropy -ts=500 -tl=6 -f -dx=-25546 -dy=0 -tileprefix=00980/tile- -idprefix=00980 -f
mv v20100411/scene.xml v20100411/scene_00980.xml

mkdir v20100411/00944
./imageTiler.py ~/zuist-scenes-tmp/fits/v20100411_00944_st_tl.fit v20100411/ -astropy -ts=500 -tl=6 -f -dx=-12774 -dy=0 -tileprefix=00944/tile- -idprefix=00944 -f
mv v20100411/scene.xml v20100411/scene_00944.xml

mkdir v20100411/00872/
./imageTiler.py ~/zuist-scenes-tmp/fits/v20100411_00872_st_tl.fit v20100411/ -astropy -ts=500 -tl=6 -f -tileprefix=00872/tile- -idprefix=00872 -f
mv v20100411/scene.xml v20100411/scene_00872.xml


mkdir v20100411/00370
./imageTiler.py ~/zuist-scenes-tmp/fits/v20100420_00370_st_tl.fit v20100411/ -astropy -ts=500 -tl=6 -f -dx=-38321 -dy=-15654 -tileprefix=00370/tile- -idprefix=00370 -f
mv v20100411/scene.xml v20100411/scene_00370.xml

mkdir v20100411/01016
./imageTiler.py ~/zuist-scenes-tmp/fits/v20100411_01016_st_tl.fit v20100411/ -astropy -ts=500 -tl=6 -f -dx=-25546 -dy=-15658 -tileprefix=01016/tile- -idprefix=01016 -f
mv v20100411/scene.xml v20100411/scene_01016.xml

mkdir v20100411/00908
./imageTiler.py ~/zuist-scenes-tmp/fits/v20100411_00908_st_tl.fit v20100411/ -astropy -ts=500 -tl=6 -f -dx=-12774 -dy=-15662 -tileprefix=00908/tile- -idprefix=00908 -f
mv v20100411/scene.xml v20100411/scene_00908.xml

mkdir v20100411/00412/
./imageTiler.py ~/zuist-scenes-tmp/fits/v20110508_00412_st_tl.fit v20100411/ -astropy -ts=500 -tl=6 -f -dy=-15654 -tileprefix=00412/tile- -idprefix=00412
mv v20100411/scene.xml v20100411/scene_00412.xml

