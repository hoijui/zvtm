/*   FILE: GTranslation.java
 *   DATE OF CREATION:   Jul 12 2000
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

/**glyph animation: translation (X,Y)
 * @author Emmanuel Pietriga
 */

class GTranslation extends GAnimation{
    /** step values for (x,y) coords */
    LongPoint[] steps;

    /** 
     *@param g glyph to be animated
     *@param mgr Animation Manager
     *@param d duration in ms
     */
    GTranslation(Glyph g,AnimManager mgr,long d){
	started=false;
	target=g;	
	parent=mgr;	
	duration=d;
	type=AnimManager.GL_TRANS;
    }

    void start(){		
	now=new Date();
	startTime=now.getTime();
	started=true;
    }

    boolean animate() {//the returned boolean says will be used to know if anything happened here, i.e. if iot is necessary to repaint
	if (started){
	    now=new Date();
	    progression=(double)((now.getTime()-startTime)/(double)duration);
	    step=(int)Math.round(steps.length*progression);
	    if (step<steps.length) {
		target.moveTo(steps[step].x,steps[step].y);
	    }
	    else {
		target.moveTo(steps[steps.length-1].x,steps[steps.length-1].y);
		parent.killGAnim(this,type);
	    }
	    return true;
	}
	else return false;
    }

    protected void conclude(){
	target.moveTo(steps[steps.length-1].x,steps[steps.length-1].y);
    }

    public void postAnimAction(){
	if (paa != null){
	    paa.animationEnded(target, PostAnimationAction.GLYPH, AnimManager.GL_TRANS);
	}
    }

}
