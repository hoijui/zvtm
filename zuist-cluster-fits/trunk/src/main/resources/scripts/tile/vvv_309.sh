#!/bin/bash

./imageTiler.py ~/v20100411_00980_st_tl.fits ./980 -f -astropy -ts=500 -tileprefix=tile- -idprefix=980 -layer=SceneKsSpace -minvalue=-602.5 -maxvalue=58637.5 -shrink

./imageTiler.py ~/v20100411_00968_st_tl.fits ./968 -f -astropy -ts=500 -tileprefix=tile- -idprefix=968 -layer=SceneHSpace -minvalue=-420.0 -maxvalue=49582.0 -shrink

./imageTiler.py ~/v20100411_00992_st_tl.fits ./992 -f -astropy -ts=500 -tileprefix=tile- -idprefix=992 -layer=SceneJSpace -minvalue=-148.5 -maxvalue=67200.5 -shrink


