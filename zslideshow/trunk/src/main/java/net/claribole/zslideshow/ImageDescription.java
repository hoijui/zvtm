/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zslideshow;

import java.awt.Image;
import java.awt.Color;
import javax.swing.ImageIcon;

import java.io.File;
import javax.imageio.ImageIO;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.glyphs.VImageST;
import net.claribole.zvtm.glyphs.RImage;

/** Description of image objects to be loaded/unloaded in the slideshow.
 *@author Emmanuel Pietriga
 */

public class ImageDescription {

    String imageFile;

    Integer loadRequest, unloadRequest;

    VImageST glyph;

    int index = -1;

    boolean inVirtualSpace = false;

    ImageDescription(File imf, int idx){
        this.imageFile = imf.getAbsolutePath();
        this.index = idx;
    }

    /** Called automatically by scene manager. But cam ne called by client application to force loading of objects not actually visible. */
    public synchronized void loadObject(VirtualSpace vs, VirtualSpaceManager vsm, boolean fadeIn, boolean show){
        if (glyph == null){
            // On Mac OS X, ImageIcon returns an OSXImage, while ImageIO.read() returns a BufferedImage
            // OSXImage is correctly garbage-collected. BufferedImage is not
            // (at least with just an IMage.flush() and removal of all references to it)
            Image i = (new ImageIcon(imageFile)).getImage();
//            Image i=null;
//            try{
//              i = ImageIO.read(new File(imageFile));
//            
//            }
//            catch(java.io.IOException ex){}
/*            int ih = i.getHeight(null);
            double sf = vh / ((double)ih);
*/            double sf = 1.0f;
            float alpha = (fadeIn) ? 0.0f : 1.0f;
            glyph = new VImageST(0, 0, 0, i, sf, alpha);
            glyph.setBorderColor(ZSlideShow.IMAGE_BORDER_COLOR);
            glyph.setDrawBorderPolicy(VImageST.DRAW_BORDER_ALWAYS);
        }
        if (show){
            if (!inVirtualSpace){
                vsm.addGlyph(glyph, vs);
                inVirtualSpace = true;
            }
        }
        else {
            if (inVirtualSpace){
                vs.destroyGlyph(glyph);
                inVirtualSpace = false;
            }
        }
        loadRequest = null;
    }

    /** Called automatically by scene manager. But cam ne called by client application to force unloading of objects still visible. */
    public synchronized void unloadObject(VirtualSpace vs, VirtualSpaceManager vsm, boolean fadeOut){
        if (glyph != null){
            if (inVirtualSpace){
                if (fadeOut){
                    vsm.animator.createGlyphAnimation(GlyphLoader.FADE_OUT_DURATION, AnimManager.GL_COLOR_LIN,
                        GlyphLoader.FADE_OUT_ANIM_DATA, glyph.getID(),
                        new ImageHideAction(vs));
                }
                else {
                    vs.destroyGlyph(glyph);
                    glyph.getImage().flush();
                }
                inVirtualSpace = false;
            }
            else {
                glyph.getImage().flush();
            }
            glyph = null;
        }
        unloadRequest = null;
    }

    public Glyph getGlyph(){
        return glyph;
    }
    
    public String toString(){
        return "ID "+index;
    }
    
}

class ImageHideAction implements PostAnimationAction {
    
    VirtualSpace vs;

    ImageHideAction(VirtualSpace vs){
        this.vs = vs;
    }

    public void animationEnded(Object target, short type, String dimension){
        try {
            vs.destroyGlyph((Glyph)target);
            ((VImageST)target).getImage().flush();
        }
        catch(ArrayIndexOutOfBoundsException ex){
            System.err.println("Warning: attempt at destroying image " + ((Glyph)target).getID() + " failed. Trying one more time.");
            recoverFailingAnimationEnded(target, type, dimension);
        }
    }

    public void recoverFailingAnimationEnded(Object target, short type, String dimension){
        try {
            vs.destroyGlyph((Glyph)target);
            ((VImageST)target).getImage().flush();
        }
        catch(ArrayIndexOutOfBoundsException ex){
            System.err.println("Warning: attempt at destroying image " + ((Glyph)target).getID() + " failed. Giving up.");
        }	
    }
    
}
