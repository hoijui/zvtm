/*   FILE: LensKillAction.java
 *   DATE OF CREATION:  Thu Dec 22 17:25:28 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: LensKillAction.java,v 1.1 2005/12/22 16:54:41 epietrig Exp $
 */ 

package net.claribole.zvtm.engine;

import com.xerox.VTM.engine.VirtualSpaceManager;
import net.claribole.zvtm.lens.Lens;

public class LensKillAction implements PostAnimationAction {

    VirtualSpaceManager vsm;
    
    public LensKillAction(VirtualSpaceManager vsm){
	this.vsm = vsm;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (type == PostAnimationAction.LENS){
	    this.vsm.getOwningView(((Lens)target).getID()).setLens(null);
	    ((Lens)target).dispose();
	}
    }
    
}