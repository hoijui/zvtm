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

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.glyphs.VImageST;

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

    VImageST glyph;

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
    public synchronized void createObject(VirtualSpace vs, VirtualSpaceManager vsm, boolean fadeIn){
        if (glyph == null){
            Image i = (new ImageIcon(path)).getImage();
            int ih = i.getHeight(null);
            double sf = vh / ((double)ih);
            if (fadeIn){
                glyph = new VImageST(vx, vy, zindex, i, sf, 0.0f);
                if (strokeColor != null){
                    glyph.setBorderColor(strokeColor);
                    glyph.setDrawBorderPolicy(VImageST.DRAW_BORDER_ALWAYS);
                }
                if (!sensitive){glyph.setSensitivity(false);}
                glyph.setInterpolationMethod(interpolationMethod);
                vsm.addGlyph(glyph, vs);
                vsm.animator.createGlyphAnimation(GlyphLoader.FADE_IN_DURATION, AnimManager.GL_COLOR_LIN,
                    GlyphLoader.FADE_IN_ANIM_DATA, glyph.getID());
            }
            else {
                glyph = new VImageST(vx, vy, zindex, i, sf, 1.0f);
                if (strokeColor != null){
                    glyph.setBorderColor(strokeColor);
                    glyph.setDrawBorderPolicy(VImageST.DRAW_BORDER_ALWAYS);
                }
                if (!sensitive){glyph.setSensitivity(false);}
                glyph.setInterpolationMethod(interpolationMethod);
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
                    new ImageHideAction(vs));
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

class ImageHideAction implements PostAnimationAction {
    
    VirtualSpace vs;
    
    ImageHideAction(VirtualSpace vs){
	this.vs = vs;
    }
    
    public void animationEnded(Object target, short type, String dimension){
	try {
	    vs.removeGlyph((Glyph)target);
	    ((VImageST)target).getImage().flush();
	}
	catch(ArrayIndexOutOfBoundsException ex){
	    System.err.println("Warning: attempt at destroying image " + ((Glyph)target).getID() + " failed. Trying one more time.");
	    recoverFailingAnimationEnded(target, type, dimension);
	}
    }

    public void recoverFailingAnimationEnded(Object target, short type, String dimension){
	try {
	    vs.removeGlyph((Glyph)target);
	    ((VImageST)target).getImage().flush();
	}
	catch(ArrayIndexOutOfBoundsException ex){
	    System.err.println("Warning: attempt at destroying image " + ((Glyph)target).getID() + " failed. Giving up.");
	}	
    }
    
}
