/*   Copyright (c) INRIA, 2010-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.io.IOException;
import java.net.URL;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zuist.engine.SceneManager;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.glyphs.Glyph;

import java.awt.image.ImageFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes a FITS images and creates / releases the corresponding
 * ZVTM glyph on demand.
 */
public class FitsImageDescription extends ResourceDescription {

    private static final Logger logger = LoggerFactory.getLogger(FitsImageDescription.class);

    private float scaleFactor = 1;
    private FitsImage.ScaleMethod scaleMethod;
    private FitsImage.ColorFilter colorFilter;
    private URL src;
    private String id;
    private double vx;
    private double vy;
    private int zindex;

    private float alpha = 1f;
    private boolean isVisible = false;

    private double gmin = Double.MAX_VALUE;
    private double gmax = Double.MIN_VALUE;
    private double gsigma;

    private boolean isRescaleGlobal = false;

    private double lmin = Double.MAX_VALUE;
    private double lmax = Double.MIN_VALUE;
    private double lsigma;

    private volatile boolean display = true;
    //private Color strokeColor = null;

    private FitsImage glyph; //the actual FITS image

    private int layerIndex;

    private boolean createdWithGlobalData = false;

    public FitsImageDescription(String id, double x, double y, int z, URL src,
            Region parentRegion,
            float scaleFactor, FitsImage.ScaleMethod scaleMethod,
            FitsImage.ColorFilter colorFilter){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.zindex = z;
        this.src = src;

        this.scaleFactor = scaleFactor;
        this.scaleMethod = scaleMethod;
        this.colorFilter = colorFilter;

        isVisible = true;

        layerIndex = parentRegion.getLayerIndex();

    }

    public FitsImageDescription(String id, double x, double y, int z, URL src,
            Region parentRegion,
            float scaleFactor, FitsImage.ScaleMethod scaleMethod,
            FitsImage.ColorFilter colorFilter, double min, double max){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.zindex = z;
        this.src = src;

        this.scaleFactor = scaleFactor;
        this.scaleMethod = scaleMethod;
        this.colorFilter = colorFilter;
        gmin = min;
        gmax = max;
        gsigma = min/2. + max/2.;

        isRescaleGlobal = true;
        createdWithGlobalData = true;

        isVisible = true;

        layerIndex = parentRegion.getLayerIndex();

    }

    public int getLayerIndex(){
        return layerIndex;
    }

    public boolean isCreatedWithGlobalData(){
        return createdWithGlobalData;
    }

    public String getType(){
        return FitsResourceHandler.RESOURCE_TYPE_FITS;
    }

    public void setColorFilter(FitsImage.ColorFilter colorFilter){
        this.colorFilter = colorFilter;
        if(glyph != null){
            glyph.setColorFilter(colorFilter);
        }
    }


    public void setScaleMethod(FitsImage.ScaleMethod scaleMethod){
        this.scaleMethod = scaleMethod;
        if(glyph != null){
            glyph.setScaleMethod(scaleMethod);
        }
    }

    public void setRescaleGlobal(double min, double max){
        gmin = (min < gmin) ? min : gmin;
        gmax = (max > gmax) ? max : gmax;
        gsigma = gmin/2. + gmax/2.;
    }

    public void setRescaleGlobal(boolean isGlobal){
        isRescaleGlobal = isGlobal;
    }

    public void rescale(double min, double max, double sigma){
        if(glyph != null) glyph.rescale(min, max, sigma);
    }

    public void rescaleGlobal(){
        //System.out.println("rescaleGlobal()");
        //System.out.println("glyph.rescale("+gmax+", "+gmin+", "+gsigma+")");
        if(glyph != null) glyph.rescale(gmin, gmax, gsigma);
    }

    public void rescaleLocal(){
        //System.out.println("rescaleLocal()");
        //System.out.println("glyph.rescale("+lmax+", "+lmin+", "+lsigma+")");
        if(glyph != null) glyph.rescale(lmin, lmax, lsigma);
    }

    public double[] getLocalScaleParams(){
        return new double[]{lmin, lmax};
    }

    //public void createObject(final VirtualSpace vs, final boolean fadeIn){
    public void createObject(final SceneManager sm, final VirtualSpace vs, final boolean fadeIn){
       
        System.out.println("createObject");
        try{
            //if(isRescaleGlobal) glyph = new FitsImage(vx,vy,zindex,src,scaleFactor,gmin, gmax);
            //else glyph = new FitsImage(vx,vy,zindex,src,scaleFactor);
            glyph = new FitsImage(vx,vy,zindex,src,scaleFactor);
            //if(gmin == Double.MAX_VALUE && gmax == Double.MIN_VALUE) glyph = new FitsImage(vx,vy,zindex,src,scaleFactor,false);
            //else glyph = new FitsImage(vx,vy,zindex,src,scaleFactor,gmin, gmax);
            //if(lmin == Double.MAX_VALUE) 
            lmin = glyph.getScaleParams()[0];
            //if(lmax == Double.MIN_VALUE) 
            lmax = glyph.getScaleParams()[1];
            
        } catch(Exception ioe){
            System.out.println("Could not create FitsImage");
            throw new Error("Could not create FitsImage");
        }
        
        //if(gmin != Double.MAX_VALUE && gmax != Double.MIN_VALUE) glyph.rescale(gmin, gmax, gmin/2. + gmax/2.);
        glyph.setVisible(isVisible);
        glyph.setDrawBorder(false);
        glyph.setTranslucencyValue(alpha);

        glyph.setScaleMethod(scaleMethod);
        glyph.setColorFilter(colorFilter);
        if(isRescaleGlobal) glyph.rescale(gmin, gmax, gsigma);
        else glyph.rescale(lmin, lmax, lsigma);

        System.out.println("localmin: " + lmin + " localmax: " + lmax);
        System.out.println("globalmin: " + gmin + " globalmax: " + gmax);
        System.out.println(glyph);

        vs.addGlyph(glyph,false);

    }

    public void destroyObject(final SceneManager sm, final VirtualSpace vs, boolean fadeOut){
        //System.out.println("destroyObject");
        if(glyph != null) vs.removeGlyph(glyph);
        glyph = null;
    }

    @Override
    public Glyph getGlyph(){
        return glyph;
    }

    @Override
    public double getX(){
        return glyph.vx;
    }

    @Override
    public double getY(){
        return glyph.vy;
    }

    @Override
    public void moveTo(double x, double y){
        glyph.moveTo(x, y);
    }

    public void setTranslucencyValue(float alpha){
        this.alpha = alpha;
        if(glyph != null) glyph.setTranslucencyValue(alpha);
    }

    public void setVisible(boolean visible){
        this.isVisible = visible;
        if(glyph != null) glyph.setVisible(visible);
    }

}

