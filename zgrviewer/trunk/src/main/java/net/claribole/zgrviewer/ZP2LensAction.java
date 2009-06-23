/*   FILE: ZP2LensAction.java
 *   DATE OF CREATION:  2006/03/15 13:12:32
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zgrviewer;

import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.lens.Lens;

public class ZP2LensAction implements EndAction {

    GraphicsManager grMngr;
    
    public ZP2LensAction(GraphicsManager gm){
	    this.grMngr = gm;
    }
    
    public void	execute(Object subject, Animation.Dimension dimension){
        grMngr.vsm.getOwningView(((Lens)subject).getID()).setLens(null);
        ((Lens)subject).dispose();
        grMngr.setMagFactor(GraphicsManager.DEFAULT_MAG_FACTOR);
        grMngr.lens.dispose();
        grMngr.lens = null;
        grMngr.setLens(GraphicsManager.NO_LENS);
    }
    
}
