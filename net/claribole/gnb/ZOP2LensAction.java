/*   FILE: ZOP2LensAction.java
 *   DATE OF CREATION:  Mon Oct 23 08:56:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 

package net.claribole.gnb;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.Lens;

public class ZOP2LensAction implements PostAnimationAction {

    GeonamesBrowser application;
    
    public ZOP2LensAction(GeonamesBrowser application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (type == PostAnimationAction.LENS){
	    application.vsm.getOwningView(((Lens)target).getID()).setLens(null);
	    ((Lens)target).dispose();
	    application.setMagFactor(GeonamesBrowser.DEFAULT_MAG_FACTOR);
	    application.lens = null;
	    application.setLens(GNBEventHandler.NO_LENS);
	    application.altitudeChanged();
	}
    }
    
}