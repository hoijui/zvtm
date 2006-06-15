/*   FILE: CinematicInfo.java
 *   DATE OF CREATION:  Wed May 24 08:51:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: CinematicInfo.java,v 1.4 2006/06/02 09:05:42 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import com.xerox.VTM.engine.Camera;

class CinematicInfo {

    static final short NO_LENS = 0;
    static final short ZOOMIN_LENS = 1;
    static final short ZOOMOUT_LENS = 2;

    int trialNumber = 0;
    short lensStatus = NO_LENS;
    long cx = 0;
    long cy = 0;
    float ca = 0;
    int lensX;
    int lensY;
    long lensVX;
    long lensVY;
    long lensVR;
    String lensMM = "4.0";
    int time;

    /*view half width and height (computed from latitude and viewport size)*/
    long viewHW = 1024 / 2;
    long viewHH = 668 / 2;

    CinematicInfo(String tn, String la, String cx, String cy, String cz,
		  String lx, String ly, String lmm, int vpw, int vph, String tm){
	trialNumber = Integer.parseInt(tn);
	lensStatus = Short.parseShort(la);
	this.cx = Long.parseLong(cx);
	this.cy = Long.parseLong(cy);
	ca = Float.parseFloat(cz);
	lensX = (lx.equals(LogManager.NaN)) ? 0 : Integer.parseInt(lx);
	lensY = (ly.equals(LogManager.NaN)) ? 0 : Integer.parseInt(ly);
	lensMM = lmm;  // not casting it to float because we only display it on screen for now
	double uncoef = (Camera.DEFAULT_FOCAL + ca) / Camera.DEFAULT_FOCAL;
	viewHW = Math.round((vpw * uncoef) / 2.0);
	viewHH = Math.round((vph * uncoef) / 2.0);
	lensVX = this.cx + Math.round(lensX * uncoef);
	lensVY = this.cy - Math.round(lensY * uncoef);
	lensVR = Math.round(ZLWorldTask.LENS_R1 * uncoef);
	time = Integer.parseInt(tm);
    }

}