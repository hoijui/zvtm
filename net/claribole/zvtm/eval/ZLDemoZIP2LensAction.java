/*   FILE: ZLDemoZIP2LensAction.java
 *   DATE OF CREATION:  2006/03/15 13:12:32
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZLDemoZIP2LensAction.java,v 1.1 2006/05/13 07:45:23 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.Lens;

public class ZLDemoZIP2LensAction implements PostAnimationAction {

    ZLWorldDemo application;
    
    public ZLDemoZIP2LensAction(ZLWorldDemo application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (type == PostAnimationAction.LENS){
	    application.vsm.getOwningView(((Lens)target).getID()).setLens(null);
	    ((Lens)target).dispose();
	    application.setMagFactor(ZLWorldDemo.DEFAULT_MAG_FACTOR);
	    application.lens = null;
	    application.setLens(WorldTaskEventHandler.NO_LENS);
	}
    }
    
}