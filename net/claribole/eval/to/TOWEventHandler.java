/*   FILE: TOWEventHandler.java
 *   DATE OF CREATION:  Thu Oct 12 12:08:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *
 * $Id:  $
 */ 

package net.claribole.eval.to;

import java.awt.Toolkit;
import java.util.Vector;

import com.xerox.VTM.engine.*;
import net.claribole.zvtm.engine.*;

class TOWEventHandler extends BaseEventHandler {

    TOWEventHandler(Eval app){
	this.application = app;
    }

}