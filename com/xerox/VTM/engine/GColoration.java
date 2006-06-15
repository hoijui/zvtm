/*   FILE: GColoration.java
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
import com.xerox.VTM.glyphs.Transparent;
import net.claribole.zvtm.engine.PostAnimationAction;

/**glyph animation: coloration (H,S,V) and translucency
 * @author Emmanuel Pietriga
 */

class GColoration extends GAnimation{

    /**step values for color (H,S,V)*/
    float[][] steps;
    /**step values for border color (H,S,V)*/
    float[][] bsteps;
    /**step values for translucency*/
    float[] alphasteps;

    int nbSteps;
    
    /** 
     *@param g glyph to be animated
     *@param mgr Animation Manager
     *@param d duration in ms
     *@param b border or fill color
     */
    GColoration(Glyph g,AnimManager mgr,long d){
	started=false;
	target=g;	
	parent=mgr;	
	duration=d;
	type=AnimManager.GL_COLOR;
    }

    void start(){		
	now=new Date();
	startTime=now.getTime();
	started=true;
    }

    boolean animate() {//the returned boolean will be used to know if anything happened here,
	// i.e. if it is necessary to repaint
	if (started){
	    now=new Date();
	    progression=(double)((now.getTime()-startTime)/(double)duration);
	    step = (int)Math.round(nbSteps*progression);
	    if (step < nbSteps) {
		if (bsteps != null){
		    target.setHSVbColor(bsteps[step][0],bsteps[step][1],bsteps[step][2]);
		}
		if (steps != null){
		    target.setHSVColor(steps[step][0],steps[step][1],steps[step][2]);
		}
		try {
		    if (alphasteps != null){((Transparent)target).setTransparencyValue(alphasteps[step]);}
		}
		catch (IllegalArgumentException ex){
		    if (VirtualSpaceManager.debug){System.err.println("Error animating translucency of "+target.toString());}
		}
	    }
	    else {
		if (bsteps != null){
		    target.setHSVbColor(bsteps[bsteps.length-1][0],bsteps[bsteps.length-1][1],bsteps[bsteps.length-1][2]);
		}
		if (steps != null){
		    target.setHSVColor(steps[steps.length-1][0],steps[steps.length-1][1],steps[steps.length-1][2]);
		}
		try {
		    if (alphasteps != null){((Transparent)target).setTransparencyValue(alphasteps[alphasteps.length-1]);}
		}
		catch (IllegalArgumentException ex){
		    if (VirtualSpaceManager.debug){System.err.println("Error animating translucency of "+target.toString());}
		}
		parent.killGAnim(this,type);
	    }
	    return true;
	}
	else return false;
    }

    protected void conclude(){
	if (bsteps != null){
	    target.setHSVbColor(bsteps[bsteps.length-1][0],bsteps[bsteps.length-1][1],bsteps[bsteps.length-1][2]);
	}
	if (steps != null){
	    target.setHSVColor(steps[steps.length-1][0],steps[steps.length-1][1],steps[steps.length-1][2]);
	}
	if (alphasteps != null){
	    try {
		((Transparent)target).setTransparencyValue(alphasteps[alphasteps.length-1]);
	    }
	    catch (IllegalArgumentException ex){
		if (VirtualSpaceManager.debug){System.err.println("Error animating translucency of "+target.toString());}
	    }
	}
    }

    public void postAnimAction(){
	if (paa != null){
	    paa.animationEnded(target, PostAnimationAction.GLYPH, AnimManager.GL_COLOR);
	}
    }

}
