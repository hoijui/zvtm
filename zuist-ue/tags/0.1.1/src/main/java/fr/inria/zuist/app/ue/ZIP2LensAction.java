/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.ue;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.Lens;

public class ZIP2LensAction implements PostAnimationAction {

    UISTExplorer application;
    
    public ZIP2LensAction(UISTExplorer application){
	this.application = application;
    }
    
    public void animationEnded(Object target, short type, String dimension){
        if (type == PostAnimationAction.LENS){
            application.disposeOfLens();
        }
    }
    
}