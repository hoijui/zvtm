/*   FILE: AbstractTaskZIP2LensAction.java
 *   DATE OF CREATION:  2006/03/15 13:12:32
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: AbstractTaskZIP2LensAction.java,v 1.2 2006/04/11 10:10:37 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.Lens;

public class AbstractTaskZIP2LensAction implements PostAnimationAction {

    ZLAbstractTask application;
    
    public AbstractTaskZIP2LensAction(ZLAbstractTask application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (type == PostAnimationAction.LENS){
	    application.vsm.getOwningView(((Lens)target).getID()).setLens(null);
	    ((Lens)target).dispose();
	    application.setMagFactor(ZLAbstractTask.DEFAULT_MAG_FACTOR);
	    application.lens = null;
	    application.setLens(AbstractTaskEventHandler.NO_LENS);
	}
    }
    
}