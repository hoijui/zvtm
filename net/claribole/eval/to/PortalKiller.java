/*   FILE: Eval.java
 *   DATE OF CREATION:  Fri Oct 20 10:28:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006-2007. All Rights Reserved
 *
 * $Id:  $
 */

package net.claribole.eval.to;

import net.claribole.zvtm.engine.PostAnimationAction;

public class PortalKiller implements PostAnimationAction {

    TOWApplication application;
    
    public PortalKiller(TOWApplication application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (type == PostAnimationAction.PORTAL){
	    application.killPortal();
	}
    }
    
}