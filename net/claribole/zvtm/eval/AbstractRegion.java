/*   FILE: Region.java
 *   DATE OF CREATION:  Tue Nov 22 09:36:06 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package net.claribole.zvtm.eval;

import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VRoundRect;

class AbstractRegion {
    
    int level;
    
    VRoundRect target;
    VRectangle[] distractors;
    
    AbstractRegion[] children;
    
    AbstractRegion(int l){
	this.level = l;
	
    }
    
    void setTarget(VRoundRect g){
	this.target = g;
    }

    void setDistractors(VRectangle[] gl){
	this.distractors = gl;
    }

}