/*   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id$
 */

package net.claribole.eval.to;

import java.awt.Toolkit;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;


class BehaviorMVEventHandler extends BehaviorEventHandler {

    BehaviorMVEventHandler(BehaviorEval app){
	super(app);
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	if (!application.blm.trialStarted){
	    if (application.blm.sessionStarted && application.blm.im.clickOnStartButton(jpx, jpy)){
		application.blm.startTrial();
		return;
	    }
	    else {
		return;
	    }
	}
	if (mouseInsideOverview){
	    if (application.to.coordInsideObservedRegion(jpx, jpy)){
 		orStickedToMouse = true;
	    }
	}
	else {
	    mCameraStickedToMouse = true;
	    if (v.lastGlyphEntered() == application.blm.target){
		application.blm.endTrial();
	    }
	    else {
		application.blm.error();
	    }
	}
    }
   
}