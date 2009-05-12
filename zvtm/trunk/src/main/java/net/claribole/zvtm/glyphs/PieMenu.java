/*   FILE: PieMenu.java
 *   DATE OF CREATION:  Thu Aug 25 14:14:50 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.glyphs;

import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.ClosedShape;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VTextOr;

import net.claribole.zvtm.animation.Animation;
import net.claribole.zvtm.animation.EndAction;
import net.claribole.zvtm.animation.interpolation.IdentityInterpolator;

public abstract class PieMenu {

    boolean orientText = false;

    /**glyphs used to represent menu items*/
    ClosedShape[] items;
    /***/
    Glyph boundary;
    /**glyphs to represent the labels of menu items*/
    VTextOr[] labels;

    /**Virtual space the menu will appear in*/
    VirtualSpace vs;

    /**destroy the pie menu (remove glyphs from virtual space)*/
    public void destroy(){
	destroy(0);
    }

	/**destroy the pie menu (remove glyphs from virtual space)
	 *@param animLength duration of collapse animation in ms (0 if no animation)
	 */
	public void destroy(int animLength){
		vs.removeGlyph(boundary);
		for (int i=0;i<labels.length;i++){
			if (labels[i] != null){
				vs.removeGlyph(labels[i]);
			}   
		}
		if (animLength > 0){
			for (int i=0;i<items.length;i++){
				if (items[i] != null){
				    Animation sizeAnim = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory()
					.createGlyphSizeAnim(animLength, 
							     items[i], 
							     0.1f, 
							     false,
							     IdentityInterpolator.getInstance(),
							     new EndAction(){
								 public void execute(Object subject,
										     Animation.Dimension dimension){
								     vs.removeGlyph((Glyph)subject);
								 }
							     });
				    VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(sizeAnim, false);
				}   
			}
		}
		else {
			for (int i=0;i<items.length;i++){
				if (items[i] != null){
					vs.removeGlyph(items[i]);
				}   
			}
		}
	}

    /**returns the menu's items counter clockwise,
     * starting with the element placed at the start angle.<br>
     * This is useful to associate owners (and events) with items.*/
    public Glyph[] getItems(){
	return items;
    }

    /**returns the menu items' labels counter clockwise,
       starting with the element placed at the start angle*/
    public VText[] getLabels(){
	return labels;
    }
    
    /**returns the menu's invisible (but sensitive) boundary*/
    public Glyph getBoundary(){
	return boundary;
    }

    /**return the index of the provided glyph in the list of menu items.<br>
     * Menu items are sorted counter clockwise, starting with the element
     * placed at the start angle.
     *@param g a glyph representing one of the menu's items
     */
    public int getItemIndex(Glyph g){
	for (int i=0;i<items.length;i++){
	    if (items[i] == g){return i;}
	}
	return -1;
    }

    public void setSensitivity(boolean b){
	for (int i=0;i<items.length;i++){
	    items[i].setSensitivity(b);
	}
	boundary.setSensitivity(b);
    }

    public boolean getSensitivity(){
	return boundary.isSensitive();
    }

}
