#!/bin/bash



mkdir AIT

## -10385.527, 50703.176
./imageTiler.py /home/wall/zuist-scenes/astro/fitsMaren2/coadd_gal_AIT.fits AIT/ -f -astropy -ts=500 -tl=8 -dx=0 -dy=0 -tileprefix=tile- -idprefix=AIT -layer=SceneKsSpace -minvalue=-10385.527 -maxvalue=50703.176


mkdir TAN

## [-10626.094, 49248.715]
./imageTiler.py /home/wall/zuist-scenes/astro/fitsMaren2/coadd_gal_TAN.fits TAN/ -f -astropy -ts=500 -tl=8 -dx=0 -dy=0 -tileprefix=tile- -idprefix=TAN -layer=SceneKsSpace -minvalue=-10626.094 -maxvalue=49248.715

