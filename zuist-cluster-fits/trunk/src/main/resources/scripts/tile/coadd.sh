#!/bin/bash

./imageTiler.py ~/coadd_gal_AIT.fits ./coadd_AIT_neighbor -f -astropy -ts=500 -tl=6 -dx=-100000 -dy=-100000 -tileprefix=tile- -idprefix=coadd_AIT -layer=SceneKsSpace -minvalue=-10385.527 -maxvalue=50703.176 -maxneighborhood=2 -onlyxml

#./imageTiler.py ~/coadd_gal_TAN.fits ./coadd_TAN_neighbor -f -astropy -ts=500 -tl=6 -tileprefix=tile- -idprefix=coadd_TAN -layer=SceneKsSpace -minvalue=-10626.094 -maxvalue=49248.715

