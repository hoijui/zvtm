/*   FILE: AbstractTaskZIP2CameraAction.java
 *   DATE OF CREATION:  2006/03/15 13:12:32
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: AbstractTaskZIP2CameraAction.java,v 1.1 2006/03/15 14:52:55 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.Lens;

public class AbstractTaskZIP2CameraAction implements PostAnimationAction {

    ZLAbstractTask application;
    
    public AbstractTaskZIP2CameraAction(ZLAbstractTask application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (type == PostAnimationAction.CAMERA){
	    application.altitudeChanged();
	}
    }
    
}