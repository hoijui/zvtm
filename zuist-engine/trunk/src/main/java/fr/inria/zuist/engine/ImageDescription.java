/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2010. All Rights Reserved
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.InvocationTargetException;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.glyphs.VRectProgress;

/** Description of bitmap image objects to be loaded/unloaded in the scene.
 *@author Emmanuel Pietriga
 */

public class ImageDescription extends ResourceDescription {

    public static final String RESOURCE_TYPE_IMG = "img";

    /* necessary info about an image for instantiation */
    double vw, vh;
    Color strokeColor;
    Object interpolationMethod = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

    private volatile VImage glyph;
    private static final ThreadPoolExecutor imageLoader, imageUnloader;
    private Future loadTask;
    private volatile boolean display = true;
    private static int N_THREADS = 10;
    private static int CAPACITY = 2000;

    static {   
        imageLoader = new ThreadPoolExecutor(5, N_THREADS, 
                10000L, TimeUnit.MILLISECONDS, 
                new LinkedBlockingQueue<Runnable>(CAPACITY));
        imageLoader.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        imageUnloader = new ThreadPoolExecutor(5, N_THREADS, 
                10000L, TimeUnit.MILLISECONDS, 
                new LinkedBlockingQueue<Runnable>(CAPACITY));
        imageUnloader.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private class ImageLoadTask implements Runnable {
        private final VirtualSpace vs;
        private final boolean fadeIn;
        private final SceneManager sm;

        ImageLoadTask(SceneManager sm, VirtualSpace vs, boolean fadeIn){
            this.sm = sm;
            this.vs = vs;
            this.fadeIn = fadeIn;
        }

        public void run(){
            if (glyph == null){
                // open connection to data
                if (showFeedbackWhenFetching){
                    final VRectProgress vrp = new VRectProgress(vx, vy, zindex, vw, vh / 40);
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
                        finishCreatingObject(sm, vs, (new ImageIcon(imgData)).getImage(), vrp, fadeIn);
                    }
                    catch(IOException e){
                        if (SceneManager.getDebugMode()){
                            System.err.println("Error fetching Image resource "+src.toString());
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    finishCreatingObject(sm, vs, (new ImageIcon(src)).getImage(), null, fadeIn);                    
                }
            }     
        }
    }

    private class ImageUnloadTask implements Runnable {
        private final SceneManager sm;
        private final VirtualSpace vs;
        private final boolean fadeOut;

        ImageUnloadTask(SceneManager sm, VirtualSpace vs, boolean fadeOut){
            this.sm = sm;
            this.vs = vs;
            this.fadeOut = fadeOut;
        }

        public void run(){
            display = false;
            try {
                loadTask.get();
            } 
            catch(InterruptedException ie){ /*ie.printStackTrace();*/ }
            catch(ExecutionException ee){ /*ee.printStackTrace();*/ }
            if (glyph != null){
                if (fadeOut){
                    Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_OUT_DURATION, glyph,
                        0.0f, false, IdentityInterpolator.getInstance(), new ImageHideAction(sm, vs));
                    VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
                    glyph = null;
                }
                else {
                    assert(!SwingUtilities.isEventDispatchThread());
                    try {
                        SwingUtilities.invokeAndWait(new Runnable(){
                        public void run(){
    	                    vs.removeGlyph(glyph);
                            glyph.getImage().flush();
                            glyph = null;
                            sm.objectDestroyed(ImageDescription.this);
                        }
                        });
                    } catch(InterruptedException ie){ /*ie.printStackTrace();*/ }
                    catch(InvocationTargetException ite){ /*ite.printStackTrace();*/ }
                }
            }
        }
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
        *@param pr parent Region in scene
        */
    ImageDescription(String id, double x, double y, int z, double w, double h, URL p, Color sc, Region pr){
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
    ImageDescription(String id, double x, double y, int z, double w, double h, URL p, Color sc, Object im, Region pr){
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
    
    @Override
	public String getType(){
	    return RESOURCE_TYPE_IMG;
	}

    @Override
    public void createObject(final SceneManager sm, final VirtualSpace vs, final boolean fadeIn){
        display = true;
        loadTask = imageLoader.submit(new ImageLoadTask(sm, vs, fadeIn));
    }
    
    private void finishCreatingObject(final SceneManager sm, final VirtualSpace vs, Image i, VRectProgress vrp, boolean fadeIn){
        // fit image in declared "bounding box"
        double sf = Math.min(vw / ((double)i.getWidth(null)), vh / ((double)i.getHeight(null)));
        if (fadeIn){
            glyph = new VImage(vx, vy, zindex, i, sf, 0.0f);
            if(!display){
                glyph.setVisible(false);
            }
            if (strokeColor != null){
                glyph.setBorderColor(strokeColor);
            }
            else {
                glyph.setDrawBorder(false);
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
            if(!display){
                glyph.setVisible(false);
            }
            if (strokeColor != null){
                glyph.setBorderColor(strokeColor);
            }
            else {
                glyph.setDrawBorder(false);
            }
            if (!sensitive){glyph.setSensitivity(false);}
            glyph.setInterpolationMethod(interpolationMethod);
        }
        assert(!SwingUtilities.isEventDispatchThread());
        try{
            SwingUtilities.invokeAndWait(new Runnable(){
                public void run(){
                    vs.addGlyph(glyph);
                    glyph.setOwner(ImageDescription.this);
                    sm.objectCreated(ImageDescription.this);
                }
                });
        } catch(InterruptedException ie){ /*ie.printStackTrace();*/} 
        catch(InvocationTargetException ite){ /*ite.printStackTrace();*/}
    }

    @Override
    public void destroyObject(final SceneManager sm, final VirtualSpace vs, boolean fadeOut){
        imageUnloader.submit(new ImageUnloadTask(sm, vs, fadeOut));        
    }
    
    @Override
    public Glyph getGlyph(){
	    return glyph;
    }
    
    @Override
    public void moveTo(double x, double y){
        super.moveTo(x, y);
        if (glyph != null){
            glyph.moveTo(vx, vy);
        }
    }
    
}

class ImageHideAction implements EndAction {
    
    SceneManager sm;
    VirtualSpace vs;
    
    ImageHideAction(SceneManager sm, VirtualSpace vs){
	    this.sm = sm;
	    this.vs = vs;
    }
    
    public void execute(Object subject, Animation.Dimension dimension){
        try {
            vs.removeGlyph((Glyph)subject);
            ((VImage)subject).getImage().flush();
            sm.objectDestroyed((ImageDescription)((Glyph)subject).getOwner());
        }
        catch(ArrayIndexOutOfBoundsException ex){
            if (SceneManager.getDebugMode()){System.err.println("Warning: attempt at destroying image " + ((Glyph)subject).hashCode() + " failed. Trying one more time.");}
            recoverFailingAnimationEnded(subject, dimension);
        }
    }

    public void recoverFailingAnimationEnded(Object subject, Animation.Dimension dimension){
        try {
            vs.removeGlyph((Glyph)subject);
            ((VImage)subject).getImage().flush();                
            sm.objectDestroyed((ImageDescription)((Glyph)subject).getOwner());
        }
        catch(ArrayIndexOutOfBoundsException ex){
            if (SceneManager.getDebugMode()){System.err.println("Warning: attempt at destroying image " + ((Glyph)subject).hashCode() + " failed. Giving up.");}
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
            if (SceneManager.getDebugMode()){System.err.println("Warning: attempt at destroying label " + ((Glyph)subject).hashCode() + " failed. Trying one more time.");}
            recoverFailingAnimationEnded(subject, dimension);
        }
    }

    public void recoverFailingAnimationEnded(Object subject, Animation.Dimension dimension){
        try {
            vs.removeGlyph((Glyph)subject);
        }
        catch(ArrayIndexOutOfBoundsException ex){
            if (SceneManager.getDebugMode()){System.err.println("Warning: attempt at destroying label " + ((Glyph)subject).hashCode() + " failed. Giving up.");}
        }	
    }

}
