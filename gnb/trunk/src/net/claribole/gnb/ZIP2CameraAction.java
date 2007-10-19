/*   FILE: ZIP2CameraAction.ajav
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

public class ZIP2CameraAction implements PostAnimationAction {

    GeonamesBrowser application;
    
    public ZIP2CameraAction(GeonamesBrowser application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (type == PostAnimationAction.CAMERA){
	    application.altitudeChanged();
	}
    }
    
}