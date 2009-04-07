/*   FILE: GTranslationPRR.java
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

/**glyph animation: translation (X,Y)  (parameterable refresh rate)
 * @author Emmanuel Pietriga
 */

class GTranslationPRR extends GTranslation {
    int rate;
    int count;

    /** 
     *@param g glyph to be animated
     *@param mgr Animation Manager
     *@param d duration in ms
     *@param r refresh rate
     */
    GTranslationPRR(Glyph g,AnimManager mgr,long d,int r){
	super(g,mgr,d);
	rate=r;
	count=1;
    }

    boolean animate() {//the returned boolean says will be used to know if anything happened here, i.e. if iot is necessary to repaint
	if (started){
	    now=new Date();
	    progression=(double)((now.getTime()-startTime)/(double)duration);
	    step=(int)Math.round(steps.length*progression);
	    if (step<steps.length) {
		if (count==rate){
		    target.moveTo(steps[step].x,steps[step].y);
		    count=1;
		    return true;
		}
		else {count++;return false;}
	    }
	    else {
		target.moveTo(steps[steps.length-1].x,steps[steps.length-1].y);
		parent.killGAnim(this,type);
		return true;
	    }
	}
	else return false;
    }

    public void postAnimAction(){
	if (paa != null){
	    paa.animationEnded(target, PostAnimationAction.GLYPH, AnimManager.GL_TRANS);
	}
    }

}
