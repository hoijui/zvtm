/*   FILE: AnimChoreographer.java
 *   DATE OF CREATION:  Mon Feb 14 11:45:46 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: AnimChoreographer.java,v 1.5 2005/12/21 12:22:44 epietrig Exp $
 */

package com.xerox.VTM.engine;

import java.util.LinkedList;
import java.util.Vector;

public class AnimChoreographer {

    VirtualSpaceManager vsm;
    AnimManager am;
    
    public AnimChoreographer(VirtualSpaceManager v){
	this.vsm = v;
	this.am = this.vsm.animator;
    }
    
    /**animate a camera along a spline passing by a sequence of LongPoints (contributed by Olivier Garaud and Thomas Maitre)
     *@param duration in milliseconds
     *@param data a vector of LongPoints by which the trajectory's spline should pass
     *@param cID ID of camera to be animated
     */
    public void createCameraSplineAnimation(long duration, Object data,Integer cID){
	if (this.am.animatedCameras.containsKey(cID) &&
	    (((int[])this.am.animatedCameras.get(cID))[0] == 1)){
	    this.am.putAsPendingCAnimation(cID, AnimManager.CA_TRANS, duration, AnimManager.CA_TRANS_LIN, data, null);
	}
	else {
	    if (this.am.animatedCameras.containsKey(cID)){
		((int[])this.am.animatedCameras.get(cID))[0] = 1;
	    }
	    else {
		int[] tmpA = {1,0};
		this.am.animatedCameras.put(cID,tmpA);
	    }
	    double nbSteps = Math.round((float)(duration/AnimManager.frameTime)); // number of steps
	    if (nbSteps > 0){
		Camera c=vsm.getCamera(cID);
		CTranslation an = new CTranslation(c, this.am, duration);
		double matrice[][] = {{-0.5, 1.5,-1.5, 0.5},
				      { 1.0,-2.5, 2.0,-0.5},
				      {-0.5, 0.0, 0.5, 0.0},
				      { 0.0, 1.0, 0.0, 0.0}};
		int nbPts = ((Vector)data).size();
		nbPts++; // the camera
		int n = (int)nbSteps/nbPts;
		an.steps = new LongPoint[n*(nbPts-1)];
		//XXX: to be enhanced
		LongPoint cam = new LongPoint(c.posx, c.posy);
		LinkedList ptsTmp = new LinkedList((Vector)data);
		ptsTmp.addFirst(cam);
		ptsTmp.addFirst(cam);
		ptsTmp.addLast(ptsTmp.getLast());
		LongPoint pts[] = new LongPoint[ptsTmp.size()];
		ptsTmp.toArray(pts);
		double x,y ;
		double[] tt = new double[4];
		double[] ttt = new double[4];
		for (int i=0;i<nbPts-1;i++ ){
		    tt = new double[4];
		    ttt = new double[4];
		    for (int j=0;j<n;j++){
			float t = (float)j/(n-1);
			tt[0] = t*t*t;
			tt[1] = t*t;
			tt[2] = t;
			tt[3] = 1;
			for (int k=0;k<4;k++){
			    ttt[k] = 0;
			    for (int l=0;l<4;l++){
				ttt[k] += tt[l] * matrice[l][k];
			    }
			}
			x = y = 0;
			for (int k=0;k<4;k++){
			    x += ttt[k] * pts[k+i].x;
			    y += ttt[k] * pts[k+i].y;
			}
			an.steps[i*n+j] = new LongPoint((long)x, (long)y);
		    }
		}
		this.am.animCameraBag.add(an);
		an.start();
	    }
	}
    }

}