/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZIP2LensAction.java,v 1.2 2007/10/03 07:05:46 pietriga Exp $
 */

package fr.inria.zuist.app.lri;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.Lens;

public class ZIP2LensAction implements PostAnimationAction {

    LRIExplorer application;
    
    public ZIP2LensAction(LRIExplorer application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
        if (type == PostAnimationAction.LENS){
            application.disposeOfLens();
        }
    }
    
}