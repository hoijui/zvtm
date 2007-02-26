/*   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id:  $
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


class BehaviorTWEventHandler extends BehaviorEventHandler {

    BehaviorTWEventHandler(BehaviorEval app){
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
		exitPortal(application.to);
		application.blm.endTrial();
	    }
	    else {
		application.blm.error();
	    }
	}
	else {
	    mCameraStickedToMouse = true;
	}
    }

//     public void press3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
// 	sx = v.getMouse().vx;
// 	sy = v.getMouse().vy;
// 	VRectangle r = new VRectangle(sx, sy, 0, 5, 5, BehaviorEval.DISTRACTOR_COLOR);
// 	application.vsm.addGlyph(r, application.mSpace);
// 	r.setMouseInsideBorderColor(java.awt.Color.RED);
// 	System.err.println(sx+" "+sy);
//     }

//     long sx,sy;
//     long sx1,sy1;
//     long sx2,sy2;

//     public void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e){
// 	if (code == KeyEvent.VK_F1){
// 	    application.switchPortal(currentJPX, currentJPY);
// 	    application.to.updateFrequency(e.getWhen());
// 	    application.to.updateWidgetLocation(currentJPX, currentJPY);
// 	}
// 	else if (code == KeyEvent.VK_T){
// 	    sx1 = v.getMouse().vx;
// 	    sy1 = v.getMouse().vy;
// 	}
// 	else if (code == KeyEvent.VK_H){
// 	    sx2 = v.getMouse().vx;
// 	    sy2 = v.getMouse().vy;
// 	    application.vsm.addGlyph(new VSegment(sx1, sy1, 0, java.awt.Color.BLACK, sx2, sy2), application.mSpace);
// 	    System.err.println(sx1+" "+sy1+" "+sx2+" "+sy2);
// 	}
//     }

}