/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.Color;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.ClosedShape;
import com.xerox.VTM.glyphs.Translucent;
import net.claribole.zvtm.engine.PostAnimationAction;

/** Description of any ZVTM glyph to be loaded/unloaded in the scene.
 *@author Emmanuel Pietriga
 */

public class ClosedShapeDescription extends ObjectDescription {

    ClosedShape glyph;
    boolean inSpace = false;

    /** Constructs the description of an image (VImageST).
        *@param id ID of object in scene
        *@param g any ClosedShape. It must implement com.xerox.VTM.glyphs.Translucent if fade in/out transitions are used in the parent region.
        *@param pr parent Region in scene
        */
    ClosedShapeDescription(String id, ClosedShape g, Region pr, boolean sensitive){
        this.id = id;
        this.glyph = g;
        this.parentRegion = pr;
        this.sensitive = sensitive;
    }

    /** Called automatically by scene manager. But can be called by client application to force loading of objects not actually visible. */
    public synchronized void createObject(VirtualSpace vs, VirtualSpaceManager vsm, boolean fadeIn){
        if (!inSpace){
            if (fadeIn){
                ((Translucent)glyph).setTranslucencyValue(0.0f);
                if (!sensitive){glyph.setSensitivity(false);}
                vsm.addGlyph(glyph, vs);
                //XXX:TBW FADE_ANIM_DATA should actually have a translucency value that equals the glyph's original value,
                //        not necessarily 1.0f
                vsm.animator.createGlyphAnimation(GlyphLoader.FADE_IN_DURATION, AnimManager.GL_COLOR_LIN,
                    GlyphLoader.FADE_IN_ANIM_DATA, glyph.getID());
            }
            else {
                if (!sensitive){glyph.setSensitivity(false);}
                vsm.addGlyph(glyph, vs);
            }
            inSpace = true;
            glyph.setOwner(this);
        }
        loadRequest = null;
    }

    /** Called automatically by scene manager. But can be called by client application to force unloading of objects still visible. */
    public synchronized void destroyObject(VirtualSpace vs, VirtualSpaceManager vsm, boolean fadeOut){
        if (inSpace){
            if (fadeOut){
                vsm.animator.createGlyphAnimation(GlyphLoader.FADE_OUT_DURATION, AnimManager.GL_COLOR_LIN,
                    GlyphLoader.FADE_OUT_ANIM_DATA, glyph.getID(),
                    new ClosedShapeHideAction(vs));
            }
            else {
                vs.removeGlyph(glyph);
            }
            inSpace = false;
        }
        unloadRequest = null;
    }

    public Glyph getGlyph(){
	    return glyph;
    }
    
    public long getX(){
        return glyph.vx;
    }
    
    public long getY(){
        return glyph.vy;
    }
    
}

class ClosedShapeHideAction implements PostAnimationAction {
    
    VirtualSpace vs;
    
    ClosedShapeHideAction(VirtualSpace vs){
	this.vs = vs;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	try {
	    vs.removeGlyph((Glyph)target);
	}
	catch(ArrayIndexOutOfBoundsException ex){
	    System.err.println("Warning: attempt at destroying rectangle " + ((Glyph)target).getID() + " failed. Trying one more time.");
	    recoverFailingAnimationEnded(target, type, dimension);
	}
    }

    public void recoverFailingAnimationEnded(Object target, short type, String dimension){
	try {
	    vs.removeGlyph((Glyph)target);
	}
	catch(ArrayIndexOutOfBoundsException ex){
	    System.err.println("Warning: attempt at destroying rectangle " + ((Glyph)target).getID() + " failed. Giving up.");
	    recoverFailingAnimationEnded(target, type, dimension);
	}
    }
    
}
