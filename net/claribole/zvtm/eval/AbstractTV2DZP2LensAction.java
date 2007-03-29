/*   FILE: TV2DZIP2LensAction.java
 *   DATE OF CREATION:  Wed May 24 08:51:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 
package net.claribole.zvtm.eval;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.Lens;

public class AbstractTV2DZP2LensAction implements PostAnimationAction {

    AbstractTrajectoryViewer2D application;
    
    public AbstractTV2DZP2LensAction(AbstractTrajectoryViewer2D application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (type == PostAnimationAction.LENS){
	    application.vsm.getOwningView(((Lens)target).getID()).setLens(null);
	    ((Lens)target).dispose();
	    application.setMagFactor(AbstractTrajectoryViewer2D.DEFAULT_MAG_FACTOR);
	    application.lens = null;
	    application.setLens(AbstractTaskEventHandler.NO_LENS);
	}
    }
    
}