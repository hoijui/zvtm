/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.Image;
import java.awt.Color;
import java.awt.RenderingHints;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.glyphs.VRectProgress;

/** Description of image objects to be loaded/unloaded in the scene.
 *@author Emmanuel Pietriga
 */

public class ImageDescription extends ResourceDescription {

    public static final String RESOURCE_TYPE_IMG = "img";

    /* necessary info about an image for instantiation */
    long vw, vh;
    Color strokeColor;
    Object interpolationMethod = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

    volatile VImage glyph;

    /** Constructs the description of an image (VImageST).
        *@param id ID of object in scene
        *@param x x-coordinate in scene
        *@param y y-coordinate in scene
        *@param z z-index (layer). Feed 0 if you don't know.
        *@param w width in scene
        *@param h height in scene
        *@param p path to bitmap resource (any valid absolute URL)
        *@param sc border color
        *@param pr parent Region in scene
        */
    ImageDescription(String id, long x, long y, int z, long w, long h, URL p, Color sc, Region pr){
        this(id,x,y,z,w,h,p,sc,null,pr);
    }
    
    /** Constructs the description of an image (VImageST).
        *@param id ID of object in scene
        *@param x x-coordinate in scene
        *@param y y-coordinate in scene
        *@param z z-index (layer). Feed 0 if you don't know.
        *@param w width in scene
        *@param h height in scene
        *@param p path to bitmap resource (any valid absolute URL)
        *@param sc border color
        *@param im one of java.awt.RenderingHints.{VALUE_INTERPOLATION_NEAREST_NEIGHBOR,VALUE_INTERPOLATION_BILINEAR,VALUE_INTERPOLATION_BICUBIC} ; default is VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        *@param pr parent Region in scene
        */
    ImageDescription(String id, long x, long y, int z, long w, long h, URL p, Color sc, Object im, Region pr){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.zindex = z;
        this.vw = w;
        this.vh = h;
		this.setURL(p);
        this.strokeColor = sc;
        this.interpolationMethod = (im != null) ? im : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        this.parentRegion = pr;
    }
    
    /** Type of resource.
	 *@return type of resource.
	 */
	public String getType(){
	    return RESOURCE_TYPE_IMG;
	}

    /** Called automatically by scene manager. But cam ne called by client application to force loading of objects not actually visible. */
    public void createObject(final VirtualSpace vs, final boolean fadeIn){
        if (glyph == null){
            // open connection to data
            if (showFeedbackWhenFetching){
                final VRectProgress vrp = new VRectProgress(vx, vy, zindex, vw / 2 , vh / 80, bgColor, barColor, percentFontColor, vs);
                vs.addGlyph(vrp);
                 try {
                            URLConnection uc = src.openConnection();
                            int dataLength = uc.getContentLength();
                            byte[] imgData = new byte[dataLength];
                            BufferedInputStream bis = new BufferedInputStream(uc.getInputStream());
                            int bytesRead = 0;
                            while (bytesRead < dataLength-1){
                                int av = bis.available();
                                if (av > 0){
                                    bis.read(imgData, bytesRead, av);
                                    bytesRead += av;
                                    vrp.setProgress(bytesRead, dataLength);                                    
                                }
                            }
                            finishCreatingObject(vs, (new ImageIcon(imgData)).getImage(), vrp, fadeIn);
                        }
                        catch(IOException e){
                            System.err.println("Error fetching Image resource "+src.toString());
                            e.printStackTrace();
                        }
            }
            else {
                    finishCreatingObject(vs, (new ImageIcon(src)).getImage(), null, fadeIn);                    
                }
        }                
    }
    
    private void finishCreatingObject(final VirtualSpace vs, Image i, VRectProgress vrp, boolean fadeIn){
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
            if (showFeedbackWhenFetching){
                // remove visual feedback about loading (smoothly)
                Animation a2 = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_OUT_DURATION, vrp,
                    1.0f, false, IdentityInterpolator.getInstance(), new FeedbackHideAction(vs));
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a2, false);                    
            }
            // smoothly fade glyph in
            Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_IN_DURATION, glyph,
                1.0f, false, IdentityInterpolator.getInstance(), null);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
        }
        else {
            if (showFeedbackWhenFetching){
                vs.removeGlyph(vrp);
            }
            glyph = new VImage(vx, vy, zindex, i, sf, 1.0f);
            if (strokeColor != null){
                glyph.setBorderColor(strokeColor);
                glyph.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
            }
            if (!sensitive){glyph.setSensitivity(false);}
            glyph.setInterpolationMethod(interpolationMethod);
        }
	SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                vs.addGlyph(glyph);
                glyph.setOwner(ImageDescription.this);
            }
	});
    }

    /** Called automatically by scene manager. But cam ne called by client application to force unloading of objects still visible. */
    public void destroyObject(final VirtualSpace vs, boolean fadeOut){
        if (glyph != null){
            if (fadeOut){
                Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_OUT_DURATION, glyph,
                    0.0f, false, IdentityInterpolator.getInstance(), new ImageHideAction(vs));
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
                glyph = null;
            }
            else {
                SwingUtilities.invokeLater(new Runnable(){
                public void run(){
	            vs.removeGlyph(glyph);
                glyph.getImage().flush();
                glyph = null;
                }
	        });
            }
        }
    }

    public Glyph getGlyph(){
	    return glyph;
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

class FeedbackHideAction implements EndAction {
    
    VirtualSpace vs;
    
    FeedbackHideAction(VirtualSpace vs){
	    this.vs = vs;
    }
    
    public void execute(Object subject, Animation.Dimension dimension){
        try {
            vs.removeGlyph((Glyph)subject);
        }
        catch(ArrayIndexOutOfBoundsException ex){
            System.err.println("Warning: attempt at destroying label " + ((Glyph)subject).hashCode() + " failed. Trying one more time.");
            recoverFailingAnimationEnded(subject, dimension);
        }
    }

    public void recoverFailingAnimationEnded(Object subject, Animation.Dimension dimension){
        try {
            vs.removeGlyph((Glyph)subject);
        }
        catch(ArrayIndexOutOfBoundsException ex){
            System.err.println("Warning: attempt at destroying label " + ((Glyph)subject).hashCode() + " failed. Giving up.");
        }	
    }

}
