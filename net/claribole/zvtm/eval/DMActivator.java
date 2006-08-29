/*   FILE: DMActivator.java
 *   DATE OF CREATION:  2006/03/15 13:12:32
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: DMActivator.java,v 1.2 2006/04/11 10:10:37 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.Lens;

public class DMActivator implements PostAnimationAction {

    ZLAbstractTask application;
    int cursorX, cursorY;

    public DMActivator(ZLAbstractTask application, int x, int y){
	this.application = application;
	cursorX = x;
	cursorY = y;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	if (((AbstractTaskDMEventHandler)application.eh).cursorHasNotMovedYet){
	    application.robot.mouseMove(cursorX+2, cursorY);
	    application.robot.mouseMove(cursorX, cursorY);
	}
    }
    
}