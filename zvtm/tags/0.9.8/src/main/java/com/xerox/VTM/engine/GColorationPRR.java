/*   FILE: GColorationPRR.java
 *   DATE OF CREATION:   Mar 28 2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 */

package com.xerox.VTM.engine;

import java.util.Date;

import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.engine.PostAnimationAction;

/**glyph animation: coloration (H,S,V)  (parameterable refresh rate)
 * @author Emmanuel Pietriga
 */

class GColorationPRR extends GColoration {

    int rate;
    int count;

    /** 
     *@param g glyph to be animated
     *@param mgr Animation Manager
     *@param d duration in ms
     *@param r refresh rate
     */
    GColorationPRR(Glyph g,AnimManager mgr,long d,int r){
	super(g,mgr,d);
	rate=r;
	count=1;
    }

    boolean animate() {//the returned boolean says will be used to know if anything happened here, i.e. if iot is necessary to repaint
	if (started){
	    now=new Date();
	    progression=(double)((now.getTime()-startTime)/(double)duration);
	    step = (int)Math.round(nbSteps*progression);
	    if (step < nbSteps) {
		if (count==rate){
		    if (bsteps != null){target.setHSVbColor(bsteps[step][0],bsteps[step][1],bsteps[step][2]);}
		    if (steps != null){target.setHSVColor(steps[step][0],steps[step][1],steps[step][2]);}
		    count=1;
		    return true;
		}
		else {count++;return false;}
	    }
	    else {
		if (bsteps != null){
		    target.setHSVbColor(bsteps[bsteps.length-1][0],bsteps[bsteps.length-1][1],bsteps[bsteps.length-1][2]);
		}
		if (steps != null){
		    target.setHSVColor(steps[steps.length-1][0],steps[steps.length-1][1],steps[steps.length-1][2]);
		}
		parent.killGAnim(this,type);
		return true;
	    }
	}
	else return false;
    }

    public void postAnimAction(){
	if (paa != null){
	    paa.animationEnded(target, PostAnimationAction.GLYPH, AnimManager.GL_COLOR);
	}
    }

}
