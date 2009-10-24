/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.Color;
import java.awt.Font;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;

/** Description of text objects to be loaded/unloaded in the scene.
 *@author Emmanuel Pietriga
 */

public class TextDescription extends ObjectDescription {

    public static final String _start = "start";
    public static final String _middle = "middle";
    public static final String _end = "end";

    /* necessary info about a text for instantiation */
    long vx, vy;
    float scale;
    String text;
    short anchor = VText.TEXT_ANCHOR_MIDDLE;
    Font font;
    
    Color fillColor;
    
    VText glyph;

    /** Constructs the description of an image (VTextST).
        *@param id ID of object in scene
        *@param x x-coordinate in scene
        *@param y y-coordinate in scene
        *@param z z-index (layer). Feed 0 if you don't know.
        *@param s scale factor
        *@param tx text label
        *@param c text color
        *@param pr parent Region in scene
        */
    TextDescription(String id, long x, long y, int z, float s, String tx, Color c, Region pr){
        this.id = id;
        this.vx = x;
        this.vy = y;
    	this.zindex = z;
        this.scale = s;
        this.text = tx;
        this.fillColor = c;
        this.parentRegion = pr;
    }

    /** Constructs the description of an image (VTextST).
        *@param id ID of object in scene
        *@param x x-coordinate in scene
        *@param y y-coordinate in scene
        *@param z z-index (layer). Feed 0 if you don't know.
        *@param s scale factor
        *@param tx text label
        *@param c text color
        *@param ta text alignment, one of VText.TEXT_ANCHOR_*
        *@param pr parent Region in scene
        */
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
    public synchronized void createObject(VirtualSpace vs, boolean fadeIn){
        if (glyph == null){
            if (fadeIn){
                glyph = new VText(vx, vy, zindex, fillColor, text, anchor, scale, 0.0f);
                if (font != null){((VText)glyph).setSpecialFont(font);}
                if (!sensitive){glyph.setSensitivity(false);}
                vs.addGlyph(glyph);
//                VirtualSpaceManager.INSTANCE.animator.createGlyphAnimation(GlyphLoader.FADE_IN_DURATION, AnimManager.GL_COLOR_LIN,
//                    GlyphLoader.FADE_IN_ANIM_DATA, glyph.getID());
                Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_IN_DURATION, glyph,
                    1.0f, false, IdentityInterpolator.getInstance(), null);
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
            }
            else {
                glyph = new VText(vx, vy, zindex, fillColor, text, anchor, scale, 1.0f);
                if (font != null){((VText)glyph).setSpecialFont(font);}
                if (!sensitive){glyph.setSensitivity(false);}
                vs.addGlyph(glyph);
            }
            glyph.setOwner(this);
        }
        loadRequest = null;
    }

    /** Called automatically by scene manager. But cam ne called by client application to force unloading of objects still visible. */
    public synchronized void destroyObject(VirtualSpace vs, boolean fadeOut){
        if (glyph != null){
            if (fadeOut){
//                VirtualSpaceManager.INSTANCE.animator.createGlyphAnimation(GlyphLoader.FADE_OUT_DURATION, AnimManager.GL_COLOR_LIN,
//                    GlyphLoader.FADE_OUT_ANIM_DATA, glyph.getID(),
//                    new TextHideAction(vs));
                Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_OUT_DURATION, glyph,
                    0.0f, false, IdentityInterpolator.getInstance(), new TextHideAction(vs));
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
            }
            else {
                vs.removeGlyph(glyph);
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

class TextHideAction implements EndAction {
    
    VirtualSpace vs;
    
    TextHideAction(VirtualSpace vs){
	this.vs = vs;
    }
    
    public void	execute(Object subject, Animation.Dimension dimension) {
        try {
            vs.removeGlyph((Glyph)subject);
        }
        catch(ArrayIndexOutOfBoundsException ex){
            System.err.println("Warning: attempt at destroying rectangle " + ((Glyph)subject).hashCode() + " failed. Trying one more time.");
            recoverFailingAnimationEnded(subject, dimension);
        }
    }

    public void recoverFailingAnimationEnded(Object subject, Animation.Dimension dimension){
        try {
            vs.removeGlyph((Glyph)subject);
        }
        catch(ArrayIndexOutOfBoundsException ex){
            System.err.println("Warning: attempt at destroying rectangle " + ((Glyph)subject).hashCode() + " failed. Giving up.");
            recoverFailingAnimationEnded(subject, dimension);
        }
    }
    
}
