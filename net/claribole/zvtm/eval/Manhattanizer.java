/*   FILE: Manhattanizer.java
 *   DATE OF CREATION:  Tue Nov 22 09:36:06 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Manhattanizer.java,v 1.1 2006/06/15 07:39:47 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.Lens;
import com.xerox.VTM.engine.AnimManager;

public class Manhattanizer implements PostAnimationAction {
    
    ZLWorldDemo application;
    
    static final int[] rdOffsets = {0, ZLWorldDemo.LENS_R1 - ZLWorldDemo.LENS_R2};

    Manhattanizer(ZLWorldDemo app){
	this.application = app;
    }

    public void animationEnded(Object target, short type, String dimension){
	application.vsm.animator.createLensAnimation(ZLWorldDemo.LENS_ANIM_TIME, AnimManager.LS_RD_LIN,
						     rdOffsets, ((Lens)target).getID(), null);
    }
    
    
}