/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ImageDescription.java 5516 2015-04-27 15:09:27Z epietrig $
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
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.InvocationTargetException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.glyphs.VRectProgress;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.util.EntityUtils;

import java.security.ProtectionDomain;
import java.net.URLDecoder;

/** Description of bitmap image objects to be loaded/unloaded in the scene.
 *@author Emmanuel Pietriga
 */

public class ImageDescription extends ResourceDescription {

    public static final String RESOURCE_TYPE_IMG = "img";

    /* necessary info about an image for instantiation */
    double vw, vh;
    Color strokeColor;
    float alpha;
    Object interpolationMethod = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

    private HashMap<VirtualSpace,VImage> glyphsMap;
    private Image img = null; 

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
            // System.out.println("ImageLoadTask " + glyphsMap.size() + " "  + " "+ id);
            Glyph glyph = (glyphsMap.size() > 0)? glyphsMap.get(vs):null;
            if (glyph != null) {
                if (SceneManager.getDebugMode()){
                    System.out.println("GLYPH ALREADY LOADED FOR THIS VS!!! "+ glyphsMap.size() + " "  + " "+ id);
                }
            }
            //else 
            {
                String protocol = src.getProtocol();
                if (protocol.startsWith(ImageDescription.HTTP_PROTOCOL) || protocol.startsWith(ImageDescription.HTTPS_PROTOCOL)){
                    Glyph vrp = null;
                    // if (showFeedbackWhenFetching){
                    //     vrp = new VRectProgress(vx, vy, zindex, vw, vh / 40);
                    //     vs.addGlyph(vrp);
                    // }
                    // open connection to data
                    CloseableHttpClient httpclient;
                    if (SceneManager.getHTTPUser() != null && SceneManager.getHTTPPassword() != null){
                        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                        credentialsProvider.setCredentials(AuthScope.ANY,
                                                           new UsernamePasswordCredentials(SceneManager.getHTTPUser(), SceneManager.getHTTPPassword()));
                        httpclient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
                    }
                    else {
                        httpclient = HttpClients.createDefault();
                    }
                    try {
                        CloseableHttpResponse response = null;
                        try {
                            HttpGet httpget = new HttpGet(src.toURI());
                            response = httpclient.execute(httpget);
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                if (img == null){
                                    byte[] imgData = EntityUtils.toByteArray(entity);
                                    img = (new ImageIcon(imgData)).getImage();
                                }
                                finishCreatingObject(sm, vs, img, vrp, fadeIn);
                            }
                        }
                        catch(URISyntaxException usex){
                            if (SceneManager.getDebugMode()){
                                System.err.println("Error fetching Image resource (malformed URI) "+src.toString());
                                usex.printStackTrace();
                            }
                        }
                        finally {
                            response.close();
                            httpclient.getConnectionManager().shutdown();
                        }
                    }
                    catch(IOException ioex){
                        if (SceneManager.getDebugMode()){
                            System.err.println("Error fetching Image resource "+src.toString());
                            ioex.printStackTrace();
                        }
                    }
                }
                else if (src.toString().startsWith(JAR_HEADER)){
                    if (img == null){
                        img = (new ImageIcon(this.getClass().getResource(src.toString().substring(JAR_HEADER.length())))).getImage();
                    }
                    finishCreatingObject(sm, vs, img, null, fadeIn);
                }
                else {
                    if (img == null){
                        img =  (new ImageIcon(src)).getImage();
                    }
                    finishCreatingObject(sm, vs, img, null, fadeIn);
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
            //System.out.println("ImageUnloadTask " + glyphsMap.size() +" "+ id);
            VImage glyph = glyphsMap.get(vs);
            if (glyph != null){
                if (fadeOut){
                    glyphsMap.remove(vs);
                    if ( glyphsMap.size() == 0) {
                        img = null; // flushed in ImageHideAction
                    }
                    Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_OUT_DURATION, glyph,
                        0.0f, false, IdentityInterpolator.getInstance(),
                        new ImageHideAction(sm, vs, glyphsMap.size()));
                    VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
                }
                else {
                    assert(!SwingUtilities.isEventDispatchThread());
                    try {
                        SwingUtilities.invokeAndWait(new Runnable(){
                        public void run(){
                            VImage g = glyphsMap.get(vs);
                            vs.removeGlyph(g);
                            glyphsMap.remove(vs);
                            if (glyphsMap.size() == 0) {
                                img.flush();
                                img = null;
                            }
                            sm.objectDestroyed(ImageDescription.this);
                        }
                        });
                    } catch(InterruptedException ie){ /*ie.printStackTrace();*/ }
                    catch(InvocationTargetException ite){ /*ite.printStackTrace();*/ }
                }
            }
        }
    }

    ///** Constructs the description of an image (VImageST).
    //    *@param id ID of object in scene
    //    *@param x x-coordinate in scene
    //    *@param y y-coordinate in scene
    //    *@param z z-index (layer). Feed 0 if you don't know.
    //    *@param w width in scene
    //    *@param h height in scene
    //    *@param p path to bitmap resource (any valid absolute URL)
    //    *@param sc border color
    //    *@param pr parent Region in scene
    //    */
    //ImageDescription(String id, double x, double y, int z, double w, double h, URL p, Color sc, Region pr){
    //    this(id,x,y,z,w,h,p,sc,1f,null,pr);
    //}

    /** Constructs the description of an image (VImageST).
        *@param id ID of object in scene
        *@param x x-coordinate in scene
        *@param y y-coordinate in scene
        *@param z z-index (layer). Feed 0 if you don't know.
        *@param w width in scene
        *@param h height in scene
        *@param p path to bitmap resource (any valid absolute URL)
        *@param sc border color
        *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
        *@param im one of java.awt.RenderingHints.{VALUE_INTERPOLATION_NEAREST_NEIGHBOR,VALUE_INTERPOLATION_BILINEAR,VALUE_INTERPOLATION_BICUBIC} ; default is VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        *@param pr parent Region in scene
        */
    ImageDescription(String id, double x, double y, int z, double w, double h, URL p, Color sc, float alpha, Object im, Region pr){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.zindex = z;
        this.vw = w;
        this.vh = h;
        this.setURL(p);
        this.strokeColor = sc;
        this.alpha = alpha;
        this.interpolationMethod = (im != null) ? im : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        this.parentRegion = pr;
        // System.out.println("ImageDescription " + interpolationMethod);
        glyphsMap = new HashMap<VirtualSpace,VImage>();
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

    private void finishCreatingObject(final SceneManager sm, final VirtualSpace vs, Image i, Glyph vrp, boolean fadeIn){
        loadCount++;  // = 1
        // fit image in declared "bounding box"
        double sf = Math.min(vw / ((double)i.getWidth(null)), vh / ((double)i.getHeight(null)));
        if (fadeIn){
            VImage glyph = new VImage(vx, vy, zindex, i, sf, 0.0f);
            //System.out.println("new VImage "+glyph);
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
            glyphsMap.put(vs,glyph);
            if (showFeedbackWhenFetching){
                // remove visual feedback about loading (smoothly)
                Animation a2 = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_OUT_DURATION, vrp,
                    alpha, false, IdentityInterpolator.getInstance(), new FeedbackHideAction(vs));
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a2, false);
            }
            // smoothly fade glyph in
            Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_IN_DURATION, glyph,
                alpha, false, IdentityInterpolator.getInstance(), null);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
        }
        else {
            if (showFeedbackWhenFetching){
                vs.removeGlyph(vrp);
            }
            VImage glyph = new VImage(vx, vy, zindex, i, sf, alpha);
            //System.out.println("new VImage "+glyph);
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
            glyphsMap.put(vs,glyph);
        }
        assert(!SwingUtilities.isEventDispatchThread());
        try{
            SwingUtilities.invokeAndWait(new Runnable(){
                public void run(){
                    VImage g = glyphsMap.get(vs);
                    vs.addGlyph(g);
                    //g.setInterpolationMethod(interpolationMethod);
                    g.setOwner(ImageDescription.this);
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
        //return glyph;
        for(Map.Entry<VirtualSpace,VImage> entry: glyphsMap.entrySet()){
            VirtualSpace vs = entry.getKey();
            return glyphsMap.get(vs);
        }
        return null;
    }

    @Override
    public void moveTo(double x, double y){
        super.moveTo(x, y);
        for(Map.Entry<VirtualSpace,VImage> entry: glyphsMap.entrySet()){
            VirtualSpace vs = entry.getKey();
            VImage glyph = glyphsMap.get(vs);
            if (glyph != null){
                glyph.moveTo(vx, vy);
            }
        }
    }

    public float getTranslucencyValue(){
        return alpha;
    }

    @Override
    public boolean coordInside(double pvx, double pvy){
        return (vx >= pvx-vw/2d && vx <= pvx+vw/2d && vy >= pvy-vw/2d && vy <= pvy+vw/2d);
    }

}

class ImageHideAction implements EndAction {

    SceneManager sm;
    VirtualSpace vs;
    int loadCount;

    ImageHideAction(SceneManager sm, VirtualSpace vs, int lc){
        this.sm = sm;
        this.vs = vs;
        this.loadCount = lc;
    }

    public void execute(Object subject, Animation.Dimension dimension){
        try {
            vs.removeGlyph((Glyph)subject);
            if (loadCount == 0) {
                ((VImage)subject).getImage().flush();
            }
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
            if (loadCount == 0) {
               ((VImage)subject).getImage().flush();
            }
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
