/*   FILE: PortalKiller.java
 *   DATE OF CREATION:  2006/07/05 13:12:32
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */ 

package net.claribole.zvtm.demo;

import net.claribole.zvtm.engine.PostAnimationAction;

public class PortalKiller implements PostAnimationAction {

    PortalWorldDemo application;
    
    public PortalKiller(PortalWorldDemo application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (type == PostAnimationAction.PORTAL){
	    application.killPortal();
	}
    }
    
}