/*   FILE: ZLDemoZOP2LensAction.java
 *   DATE OF CREATION:  2006/03/15 13:12:32
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZLDemoZOP2LensAction.java,v 1.2 2006/06/10 16:15:51 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.Lens;

public class ZLDemoZOP2LensAction implements PostAnimationAction {

    ZLWorldDemo application;
    
    public ZLDemoZOP2LensAction(ZLWorldDemo application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (type == PostAnimationAction.LENS){
	    application.vsm.getOwningView(((Lens)target).getID()).setLens(null);
	    ((Lens)target).dispose();
	    application.setMagFactor(ZLWorldDemo.DEFAULT_MAG_FACTOR);
	    application.lens = null;
	    application.setLens(ZLDemoEventHandler.NO_LENS);
	    application.altitudeChanged();
	}
    }
    
}