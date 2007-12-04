/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.Color;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.engine.PostAnimationAction;
import com.xerox.VTM.glyphs.VPolygonST;

/** Description of polygon objects to be loaded/unloaded in the scene.
 *@author Emmanuel Pietriga
 */

public class PolygonDescription extends ObjectDescription {

    /* necessary info about a rectangle for instantiation */
    LongPoint[] vertices;
    Color strokeColor, fillColor;

    VPolygonST glyph;

    PolygonDescription(String id, LongPoint[] vertices, Color fc, Color sc, Region pr){
        this.id = id;
        this.vertices = vertices;
        this.strokeColor = sc;
        this.fillColor = fc;
        this.parentRegion = pr;
    }

    /** Called automatically by scene manager. But can be called by client application to force loading of objects not actually visible. */
    public synchronized void createObject(VirtualSpace vs, VirtualSpaceManager vsm, boolean fadeIn){
        if (glyph == null){
            if (fadeIn){
                glyph = new VPolygonST(vertices, (fillColor!=null) ? fillColor : Color.BLACK, (strokeColor!=null) ? strokeColor : Color.WHITE, 0.0f);
                if (!sensitive){glyph.setSensitivity(false);}
                if (fillColor == null){glyph.setFilled(false);}
                if (strokeColor == null){glyph.setDrawBorder(false);}
                vsm.addGlyph(glyph, vs);
                vsm.animator.createGlyphAnimation(GlyphLoader.FADE_IN_DURATION, AnimManager.GL_COLOR_LIN,
                    GlyphLoader.FADE_IN_ANIM_DATA, glyph.getID());
            }
            else {
                glyph = new VPolygonST(vertices, (fillColor!=null) ? fillColor : Color.BLACK, (strokeColor!=null) ? strokeColor : Color.WHITE, 1.0f);
                if (!sensitive){glyph.setSensitivity(false);}
                if (fillColor == null){glyph.setFilled(false);}
                if (strokeColor == null){glyph.setDrawBorder(false);}
                vsm.addGlyph(glyph, vs);
            }
            glyph.setOwner(this);
        }
        loadRequest = null;
    }

    /** Called automatically by scene manager. But can be called by client application to force unloading of objects still visible. */
    public synchronized void destroyObject(VirtualSpace vs, VirtualSpaceManager vsm, boolean fadeOut){
        if (glyph != null){
            if (fadeOut){
                vsm.animator.createGlyphAnimation(GlyphLoader.FADE_OUT_DURATION, AnimManager.GL_COLOR_LIN,
                    GlyphLoader.FADE_OUT_ANIM_DATA, glyph.getID(),
                    new PolygonHideAction(vs));
            }
            else {
                vs.destroyGlyph(glyph);
            }
            glyph = null;
        }
        unloadRequest = null;
    }

    public Glyph getGlyph(){
	return glyph;
    }
    
}

class PolygonHideAction implements PostAnimationAction {
    
    VirtualSpace vs;
    
    PolygonHideAction(VirtualSpace vs){
        this.vs = vs;
    }
    
    public void animationEnded(Object target, short type, String dimension){
        try {
            vs.destroyGlyph((Glyph)target);
        }
        catch(ArrayIndexOutOfBoundsException ex){
            System.err.println("Warning: attempt at destroying rectangle " + ((Glyph)target).getID() + " failed. Trying one more time.");
            recoverFailingAnimationEnded(target, type, dimension);
        }
    }

    public void recoverFailingAnimationEnded(Object target, short type, String dimension){
        try {
            vs.destroyGlyph((Glyph)target);
        }
        catch(ArrayIndexOutOfBoundsException ex){
            System.err.println("Warning: attempt at destroying rectangle " + ((Glyph)target).getID() + " failed. Giving up.");
            recoverFailingAnimationEnded(target, type, dimension);
        }
    }

}
