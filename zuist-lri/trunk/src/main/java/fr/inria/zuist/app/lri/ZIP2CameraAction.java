/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZIP2CameraAction.java,v 1.1 2007/06/01 06:29:03 pietriga Exp $
 */

package fr.inria.zuist.app.lri;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.Lens;

public class ZIP2CameraAction implements PostAnimationAction {

    LRIExplorer application;
    
    public ZIP2CameraAction(LRIExplorer application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (type == PostAnimationAction.CAMERA){
	    application.altitudeChanged();
	}
    }
    
}