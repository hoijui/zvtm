#!/bin/bash

./imageTiler.py ~/coadd_gal_AIT.fits ./coadd_AIT_shrink -f -astropy -ts=500 -dx=0 -dy=0 -tileprefix=tile- -idprefix=coadd_AIT -layer=SceneKsSpace -minvalue=-10385.527 -maxvalue=50703.176 -shrink

./imageTiler.py ~/coadd_gal_TAN.fits ./coadd_TAN_shrink -f -astropy -ts=500 -tileprefix=tile- -idprefix=coadd_TAN -layer=SceneKsSpace -minvalue=-10626.094 -maxvalue=49248.715 -shrink

