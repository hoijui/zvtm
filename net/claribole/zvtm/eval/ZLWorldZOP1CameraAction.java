/*   FILE: ZLWorldZOP1CameraAction.java
 *   DATE OF CREATION:  2006/03/15 13:12:32
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZLWorldZOP1CameraAction.java,v 1.3 2006/05/05 07:39:21 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.Lens;

public class ZLWorldZOP1CameraAction implements PostAnimationAction {

    ZLWorldTask application;
    
    public ZLWorldZOP1CameraAction(ZLWorldTask application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (type == PostAnimationAction.CAMERA){
	    application.updateLabels(application.demoCamera.getAltitude());
	    /* disabled following call because we do not want to update maps
	       in the middle of a zoom out operation. Disabling this call
	       does not have any bad side effect on the update of visible maps
	       (e.g. visible maps might have been updated at the wrong level)
	       because the update of visible maps is temporarily freezed
	       when a lens is active*/
	    //application.ewmm.updateMapLevel(application.demoCamera.getAltitude());
	}
    }
    
}