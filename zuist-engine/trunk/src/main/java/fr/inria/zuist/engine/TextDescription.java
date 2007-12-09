/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: TextDescription.java,v 1.11 2007/10/04 13:59:00 pietriga Exp $
 */

package fr.inria.zuist.engine;

import java.awt.Color;
import java.awt.Font;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VText;
import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.glyphs.VTextST;

/** Description of text objects to be loaded/unloaded in the scene.
 *@author Emmanuel Pietriga
 */

public class TextDescription extends ObjectDescription {

    public static final String _start = "start";
    public static final String _middle = "middle";
    public static final String _end = "end";

    /* necessary info about a text for instantiation */
    long vx, vy;
    int zindex;
    float scale;
    String text;
    short anchor = VText.TEXT_ANCHOR_MIDDLE;
    Font font;
    
    Color fillColor;
    
    VTextST glyph;

    TextDescription(String id, long x, long y, float s, String tx, Color c, Region pr){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.scale = s;
        this.text = tx;
        this.fillColor = c;
        this.parentRegion = pr;
    }

    TextDescription(String id, long x, long y, int z, float s, String tx, Color c, short ta, Region pr){
        this.id = id;
        this.vx = x;
        this.vy = y;
    	this.zindex = z;
        this.scale = s;
        this.text = tx;
        this.fillColor = c;
        this.parentRegion = pr;
        this.anchor = ta;
    }

    /** Called automatically by scene manager. But cam ne called by client application to force loading of objects not actually visible. */
    public synchronized void createObject(VirtualSpace vs, VirtualSpaceManager vsm, boolean fadeIn){
        if (glyph == null){
            if (fadeIn){
                glyph = new VTextST(vx, vy, zindex, fillColor, text, anchor, 0.0f, scale);
                if (font != null){((VText)glyph).setSpecialFont(font);}
                if (!sensitive){glyph.setSensitivity(false);}
                vsm.addGlyph(glyph, vs);
                vsm.animator.createGlyphAnimation(GlyphLoader.FADE_IN_DURATION, AnimManager.GL_COLOR_LIN,
                    GlyphLoader.FADE_IN_ANIM_DATA, glyph.getID());
            }
            else {
                glyph = new VTextST(vx, vy, zindex, fillColor, text, anchor, 1.0f, scale);
                if (font != null){((VText)glyph).setSpecialFont(font);}
                if (!sensitive){glyph.setSensitivity(false);}
                vsm.addGlyph(glyph, vs);
            }
            glyph.setOwner(this);
        }
        loadRequest = null;
    }

    /** Called automatically by scene manager. But cam ne called by client application to force unloading of objects still visible. */
    public synchronized void destroyObject(VirtualSpace vs, VirtualSpaceManager vsm, boolean fadeOut){
        if (glyph != null){
            if (fadeOut){
                vsm.animator.createGlyphAnimation(GlyphLoader.FADE_OUT_DURATION, AnimManager.GL_COLOR_LIN,
                    GlyphLoader.FADE_OUT_ANIM_DATA, glyph.getID(),
                    new TextHideAction(vs));
            }
            else {
                vs.destroyGlyph(glyph);
            }
            glyph = null;
        }
        unloadRequest = null;
    }

    public void setFont(Font f){
	this.font = f;
    }

    public Font getFont(){
	return font;
    }

    public Glyph getGlyph(){
	return glyph;
    }
    
    public String getText(){
        return text;
    }
    
    public float getScale(){
        return scale;
    }
    
    public static short getAnchor(String anchor){
	if (anchor.equals(_start)){return VText.TEXT_ANCHOR_START;}
	else if (anchor.equals(_end)){return VText.TEXT_ANCHOR_END;}
	else {return VText.TEXT_ANCHOR_MIDDLE;}
    }
    
    public long getX(){
        return vx;
    }
    
    public long getY(){
        return vy;
    }
    
}

class TextHideAction implements PostAnimationAction {
    
    VirtualSpace vs;
    
    TextHideAction(VirtualSpace vs){
	this.vs = vs;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	try {
	    vs.destroyGlyph((Glyph)target);
	}
	catch(ArrayIndexOutOfBoundsException ex){
	    System.err.println("Warning: attempt at destroying text " + ((Glyph)target).getID() + " failed. Trying one more time.");
	    recoverFailingAnimationEnded(target, type, dimension);
	}
    }

    public void recoverFailingAnimationEnded(Object target, short type, String dimension){
	try {
	    vs.destroyGlyph((Glyph)target);
	}
	catch(ArrayIndexOutOfBoundsException ex){
	    System.err.println("Warning: attempt at destroying text " + ((Glyph)target).getID() + " failed. Giving up.");
	    recoverFailingAnimationEnded(target, type, dimension);
	}
    }
    
}
