/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.lri;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.Lens;

public class ZOP2LensAction implements PostAnimationAction {

    LRIExplorer application;
    
    public ZOP2LensAction(LRIExplorer application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (type == PostAnimationAction.LENS){
	    application.vsm.getOwningView(((Lens)target).getID()).setLens(null);
	    ((Lens)target).dispose();
	    application.setMagFactor(LRIExplorer.DEFAULT_MAG_FACTOR);
	    application.lens = null;
	    application.setLens(ExplorerEventHandler.NO_LENS);
	    application.altitudeChanged();
	}
    }
    
}