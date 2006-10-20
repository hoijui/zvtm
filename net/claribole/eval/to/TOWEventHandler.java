/*   FILE: TOWEventHandler.java
 *   DATE OF CREATION:  Thu Oct 12 12:08:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *
 * $Id:  $
 */ 

package net.claribole.eval.to;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;

import com.xerox.VTM.engine.*;
import net.claribole.zvtm.engine.*;

class TOWEventHandler extends BaseEventHandler implements PortalEventHandler {

    static final int PORTAL_EXPANSION_TIME = 200;

    int currentJPX, currentJPY;

    boolean inTOW = false;
    boolean delayedTOWExit = false;

    TOWEventHandler(Eval app){
	this.application = app;
    }

    public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e){
	if (!inTOW && application.to != null){
	    application.to.updateFrequency(e.getWhen());
	    application.to.updateWidgetLocation(jpx, jpy);
	}
	currentJPX = jpx;
	currentJPY = jpy;
    }


    public void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e){
	if (code == KeyEvent.VK_SPACE){
	    application.switchPortal(currentJPX, currentJPY);
	    application.to.updateFrequency(e.getWhen());
	    application.to.updateWidgetLocation(currentJPX, currentJPY);
	}
    }

    /**cursor enters portal*/
    public void enterPortal(Portal p){
	if (delayedTOWExit){delayedTOWExit = false;return;}
	inTOW = true;
// 	stickPortal();
	application.vsm.repaintNow();
    }

    /**cursor exits portal*/
    public void exitPortal(Portal p){
// 	if (regionStickedToMouse){
// 	    delayedTOWExit = true;
// 	}
// 	else {
	    portalExitActions();
// 	}
    }

    void portalExitActions(){
	inTOW = false;
	delayedTOWExit = false;
// 	unstickPortal();
	application.vsm.repaintNow();
    }

}