#!/bin/bash

./imageTiler.py ~/NGFS_tile1_g_INRIA.fits ./g -f -astropy -ts=500 -tileprefix=tile- -idprefix=NGFS_G -layer=G -minvalue=-1750.3 -maxvalue=23464.5 -shrink

./imageTiler.py ~/NGFS_tile1_i_INRIA.fits ./i -f -astropy -ts=500 -tileprefix=tile- -idprefix=NGFS_I -layer=I -minvalue=-1083.5 -maxvalue=162782.7 -shrink

./imageTiler.py ~/NGFS_tile1_u_INRIA.fits ./u -f -astropy -ts=500 -tileprefix=tile- -idprefix=NGFS_U -layer=U -minvalue=-31.200001 -maxvalue=11218.2 -shrink


