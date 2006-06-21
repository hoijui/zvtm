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
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;

class AbstractRegion {
    
    int level;
    
    VRoundRect target;
    VRectangle[] distractors;
    
    // null if none
    AbstractRegion childRegion;
    
    AbstractRegion(int l){
	this.level = l;
    }
    
    void setTarget(VRoundRect g){
	this.target = g;
    }

    void setDistractors(VRectangle[] gl){
	this.distractors = gl;
    }

    void setChildRegion(AbstractRegion cr){
	childRegion = cr;
    }

    void addToVirtualSpace(VirtualSpaceManager vsm, VirtualSpace vs){
	vsm.addGlyph(target, vs);
	for (int i=0;i<distractors.length;i++){
	    vsm.addGlyph(distractors[i],vs);
	}
	if (childRegion != null){
	    childRegion.addToVirtualSpace(vsm, vs);
	}
    }

    void removeFromVirtualSpace(VirtualSpace vs){
	vs.destroyGlyph(target);
	for (int i=0;i<distractors.length;i++){
	    vs.destroyGlyph(distractors[i]);
	}
	if (childRegion != null){
	    childRegion.removeFromVirtualSpace(vs);
	}
    }

}