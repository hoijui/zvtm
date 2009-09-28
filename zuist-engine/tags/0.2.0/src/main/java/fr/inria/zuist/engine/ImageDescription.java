/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ImageDescription.java,v 1.9 2007/10/04 13:59:00 pietriga Exp $
 */

package fr.inria.zuist.engine;

import java.awt.Image;
import java.awt.Color;
import java.awt.RenderingHints;
import javax.swing.ImageIcon;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;

/** Description of image objects to be loaded/unloaded in the scene.
 *@author Emmanuel Pietriga
 */

public class ImageDescription extends ObjectDescription {

    /* necessary info about an image for instantiation */
    long vx, vy;
    int zindex;
    long vw, vh;
    String path;
    Color strokeColor;
    Object interpolationMethod = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

    VImage glyph;

    /** Constructs the description of an image (VImageST).
        *@param id ID of object in scene
        *@param x x-coordinate in scene
        *@param y y-coordinate in scene
        *@param z z-index (layer)
        *@param w width in scene
        *@param h height in scene
        *@param p path to bitmap resource
        *@param sc border color
        *@param pr parent Region in scene
        */
    ImageDescription(String id, long x, long y, int z, long w, long h, String p, Color sc, Region pr){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.zindex = z;
        this.vw = w;
        this.vh = h;
        this.path = p;
        this.strokeColor = sc;
        this.parentRegion = pr;
    }
    
    /** Constructs the description of an image (VImageST).
        *@param id ID of object in scene
        *@param x x-coordinate in scene
        *@param y y-coordinate in scene
        *@param z z-index (layer)
        *@param w width in scene
        *@param h height in scene
        *@param p path to bitmap resource
        *@param sc border color
        *@param im one of java.awt.RenderingHints.{VALUE_INTERPOLATION_NEAREST_NEIGHBOR,VALUE_INTERPOLATION_BILINEAR,VALUE_INTERPOLATION_BICUBIC} ; default is VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        *@param pr parent Region in scene
        */
    ImageDescription(String id, long x, long y, int z, long w, long h, String p, Color sc, Object im, Region pr){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.zindex = z;
        this.vw = w;
        this.vh = h;
        this.path = p;
        this.strokeColor = sc;
        this.interpolationMethod = (im != null) ? im : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        this.parentRegion = pr;
    }

    /** Called automatically by scene manager. But cam ne called by client application to force loading of objects not actually visible. */
    public synchronized void createObject(VirtualSpace vs, boolean fadeIn){
        if (glyph == null){
            Image i = (new ImageIcon(path)).getImage();
            int ih = i.getHeight(null);
            double sf = vh / ((double)ih);
            if (fadeIn){
                glyph = new VImage(vx, vy, zindex, i, sf, 0.0f);
                if (strokeColor != null){
                    glyph.setBorderColor(strokeColor);
                    glyph.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
                }
                if (!sensitive){glyph.setSensitivity(false);}
                glyph.setInterpolationMethod(interpolationMethod);
                vs.addGlyph(glyph);
//                VirtualSpaceManager.INSTANCE.animator.createGlyphAnimation(GlyphLoader.FADE_IN_DURATION, AnimManager.GL_COLOR_LIN,
//                    GlyphLoader.FADE_IN_ANIM_DATA, glyph.getID());
                Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_IN_DURATION, glyph,
                    1.0f, false, IdentityInterpolator.getInstance(), null);
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
            }
            else {
                glyph = new VImage(vx, vy, zindex, i, sf, 1.0f);
                if (strokeColor != null){
                    glyph.setBorderColor(strokeColor);
                    glyph.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
                }
                if (!sensitive){glyph.setSensitivity(false);}
                glyph.setInterpolationMethod(interpolationMethod);
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
//                    new ImageHideAction(vs));
                Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_OUT_DURATION, glyph,
                    0.0f, false, IdentityInterpolator.getInstance(), new ImageHideAction(vs));
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
            }
            else {
                vs.removeGlyph(glyph);
                glyph.getImage().flush();
            }
            glyph = null;
        }
        unloadRequest = null;
    }

    public Glyph getGlyph(){
	    return glyph;
    }

    public long getX(){
        return vx;
    }
    
    public long getY(){
        return vy;
    }
        
}

class ImageHideAction implements EndAction {
    
    VirtualSpace vs;
    
    ImageHideAction(VirtualSpace vs){
	this.vs = vs;
    }
    
    public void execute(Object subject, Animation.Dimension dimension){
        try {
            vs.removeGlyph((Glyph)subject);
            ((VImage)subject).getImage().flush();
        }
        catch(ArrayIndexOutOfBoundsException ex){
            System.err.println("Warning: attempt at destroying image " + ((Glyph)subject).hashCode() + " failed. Trying one more time.");
            recoverFailingAnimationEnded(subject, dimension);
        }
    }

    public void recoverFailingAnimationEnded(Object subject, Animation.Dimension dimension){
        try {
            vs.removeGlyph((Glyph)subject);
            ((VImage)subject).getImage().flush();
        }
        catch(ArrayIndexOutOfBoundsException ex){
            System.err.println("Warning: attempt at destroying image " + ((Glyph)subject).hashCode() + " failed. Giving up.");
        }	
    }

}
