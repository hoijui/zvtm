package com.xerox.VTM.engine;

import java.util.Date;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.glyphs.DPath;

import com.xerox.VTM.glyphs.Glyph;

public class DPTransformation extends GAnimation {

    /** First index is step number and second index is number of LongPoint coordinates in this step.
     * For example steps[0][4] means that this is 5th point of 1st step*/
    LongPoint[][] steps;
    
    DPTransformation(Glyph g, AnimManager mgr, long d){
	target = g;
	parent = mgr;
	duration = d;
	started = false;
	type = AnimManager.GL_TRANS;
    }
    
    boolean animate() {
	if (started){
	    now = new Date();
	    progression = (double)((now.getTime() - startTime) / (double)duration);
	    step = (int)Math.round(steps.length*progression);
	    if (step < steps.length) {
		((DPath)target).edit(steps[step], true);
	    }
	    else {
		((DPath)target).edit(steps[steps.length - 1], true);
		parent.killGAnim(this,type);
	    }
	    return true;
	}
	else return false;
    }

    protected void conclude() {
	((DPath)target).edit(steps[step-1], true);
    }

    public void postAnimAction() {
	if (paa != null){
	    paa.animationEnded(target, PostAnimationAction.GLYPH, AnimManager.GL_TRANS);
	}
    }

    void start() {
	now = new Date();
	startTime = now.getTime();
	started = true;
    }

}
