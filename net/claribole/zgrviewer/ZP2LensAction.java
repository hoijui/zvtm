/*   FILE: ZP2LensAction.java
 *   DATE OF CREATION:  2006/03/15 13:12:32
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZP2LensAction.java,v 1.1 2006/06/14 13:23:21 epietrig Exp $
 */ 

package net.claribole.zgrviewer;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.Lens;

public class ZP2LensAction implements PostAnimationAction {

    ZGRViewer application;
    
    public ZP2LensAction(ZGRViewer application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (type == PostAnimationAction.LENS){
	    application.vsm.getOwningView(((Lens)target).getID()).setLens(null);
	    ((Lens)target).dispose();
	    application.setMagFactor(ZGRViewer.DEFAULT_MAG_FACTOR);
	    application.lens.dispose();
	    application.lens = null;
	    application.setLens(ZgrvEvtHdlr.NO_LENS);
	}
    }
    
}