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
import com.xerox.VTM.glyphs.ZRoundRect;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;

class AbstractRegion {
    
    int level;
    
    ZRoundRect bounds;  // rectangle representing the region itself (same Glyph as the target of parent region)
    ZRoundRect target;  // rectangle representing the target within this region (same Glyph as the bounds of the child region)
    VRectangle[] distractors; // false targets populating the region
    
    // null if none
    AbstractRegion childRegion;
    
    boolean visibleChildren = true;

    AbstractRegion(int l){
	this.level = l;
    }
    
    void setTarget(ZRoundRect g){
	this.target = g;
    }

    void setDistractors(VRectangle[] gl){
	this.distractors = gl;
    }

    void setBounds(ZRoundRect b){
	bounds = b;
	if (childRegion != null){
	    childRegion.setBounds(target);
	}
    }

    void setChildRegion(AbstractRegion cr){
	childRegion = cr;
    }

    void addToVirtualSpace(VirtualSpaceManager vsm, VirtualSpace vs, Glyph parent){
	vsm.addGlyph(target, vs);
	if (parent != null){vs.above(target, parent);}
	else {vs.atBottom(target);}
	for (int i=0;i<distractors.length;i++){
	    vsm.addGlyph(distractors[i], vs);
	    if (parent != null){vs.above(distractors[i], parent);}
	}
	if (childRegion != null){
	    childRegion.addToVirtualSpace(vsm, vs, target);
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

    ZRoundRect getDeepestTarget(){
	return (childRegion != null) ? childRegion.getDeepestTarget() : target;
    }

    int updateWorld(long[] visibleRegion, short altChange){
	int res = level;
	if (containsVisibleRegion(visibleRegion)){
	    if (!visibleChildren){
		target.setVisible(true);
		for (int i=0;i<distractors.length;i++){
		    distractors[i].setVisible(true);
		}		
		visibleChildren = true;		
	    }
	    if (childRegion != null){
		return childRegion.updateWorld(visibleRegion, altChange);
	    }
	    else {
		return level;
	    }
	}
	else {
	    if (visibleChildren){
		target.setVisible(false);
		for (int i=0;i<distractors.length;i++){
		    distractors[i].setVisible(false);
		}
		visibleChildren = false;
	    }
	    if (childRegion != null){
		childRegion.updateWorld(visibleRegion, altChange);
	    }
	    return level-1;
	}
    }

    static final double visFactor = 1.4;
    
    boolean containsVisibleRegion(long[] wnes){
	return ((wnes[2]-wnes[0]) < Math.round(2 * bounds.getWidth() * visFactor) ||
		(wnes[1]-wnes[3]) < Math.round(2 * bounds.getHeight() * visFactor));
    }

}
