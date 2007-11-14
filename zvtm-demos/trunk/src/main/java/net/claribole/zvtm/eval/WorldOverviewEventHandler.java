/*   FILE: AbstractTaskPZOEventHandler.java
 *   DATE OF CREATION:  Thu Sep 28 09:51:19 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.eval;

import java.awt.Color;

import net.claribole.zvtm.engine.*;

class WorldOverviewEventHandler implements PortalEventHandler {

    ZLWorldTask application;
    DMEventHandler eh;

    WorldOverviewEventHandler(ZLWorldTask app){
	this.application = app;
	eh = (DMEventHandler)this.application.eh;
    }

    /**cursor enters portal*/
    public void enterPortal(Portal p){
	eh.inOvPortal = true;
	((CameraPortal)p).setBorder(Color.WHITE);
	application.vsm.repaintNow();
    }

    /**cursor exits portal*/
    public void exitPortal(Portal p){
	eh.inOvPortal = false;
	((CameraPortal)p).setBorder(Color.RED);
	application.vsm.repaintNow();
    }

}