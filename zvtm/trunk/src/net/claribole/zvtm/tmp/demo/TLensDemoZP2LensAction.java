/*   FILE: TLensDemoZP2LensAction.java
 *   DATE OF CREATION:  2006/03/15 13:12:32
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.demo;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.Lens;

public class TLensDemoZP2LensAction implements PostAnimationAction {

    TLensDemo application;
    
    public TLensDemoZP2LensAction(TLensDemo application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (type == PostAnimationAction.LENS){
	    application.vsm.getOwningView(((Lens)target).getID()).setLens(null);
	    ((Lens)target).dispose();
	    application.setMagFactor(TLensDemo.DEFAULT_MAG_FACTOR);
	    application.lens = null;
	    application.setLens(TLensDemoEventHandler.NO_LENS);
	}
    }
    
}