#!/bin/bash

./imageTiler.py ~/coadd_gal_AIT.fits ./coadd_AIT -f -astropy -ts=500 -tl=6 -dx=0 -dy=0 -tileprefix=tile- -idprefix=coadd_AIT -layer=SceneKsSpace -minvalue=-10385.527 -maxvalue= 50703.176
mv scene.xml coadd_AIT/

./imageTiler.py ~/coadd_gal_TAN.fits ./coadd_TAN -f -astropy -ts=500 -tl=6 -dx=0 -dy=0 -tileprefix=tile- -idprefix=coadd_TAN -layer=SceneKsSpace -minvalue=-10626.094 -maxvalue=49248.715
mv scene.xml coadd_TAN/

