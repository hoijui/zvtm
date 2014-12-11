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

import jsky.coords.WCSTransform;
import fr.inria.zvtm.fits.NomWcsKeywordProvider;
import edu.jhu.pha.sdss.fits.FITSImage; 
import java.awt.geom.Point2D;
import nom.tam.fits.FitsException;

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
    private double w;
    private double h;

    private WCSTransform wcsTransform;
    private String objectName; 

    private float alpha = 1f;
    private boolean isVisible = false;
    //private boolean isSensitive;

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

    private boolean isReference = false;

    private double angle = 0;

    private int layerIndex;

    private boolean createdWithGlobalData = false;

    private Region parentRegion;

    public FitsImageDescription(String id, double x, double y, int z, URL src,
            Region parentRegion, float scaleFactor, FitsImage.ScaleMethod scaleMethod,
            FitsImage.ColorFilter colorFilter){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.zindex = z;
        this.src = src;

        w = parentRegion.getWidth();
        h = parentRegion.getHeight();

        this.scaleFactor = scaleFactor;
        this.scaleMethod = scaleMethod;
        this.colorFilter = colorFilter;

        isVisible = true;

        layerIndex = parentRegion.getLayerIndex();

        this.parentRegion = parentRegion;

        /*
        try{
            FITSImage fitsImage = new FITSImage(src);
            NomWcsKeywordProvider wcsKeyProvider = new NomWcsKeywordProvider(fitsImage.getFits().getHDU(0).getHeader());
            wcsTransform = new WCSTransform(wcsKeyProvider);
            objectName = wcsKeyProvider.getStringValue("OBJECT");
        } catch(IOException ioe){
            wcsTransform = null;
            objectName = "";
        } catch (FitsException fe){
            wcsTransform = null;
            objectName = "";
        } catch(FITSImage.NoImageDataFoundException nidfe){
            wcsTransform = null;
            objectName = "";
        } catch(FITSImage.DataTypeNotSupportedException dtnse) {
            wcsTransform = null;
            objectName = "";
        }
        */

    }

    public FitsImageDescription(String id, double x, double y, int z, URL src,
            Region parentRegion, float scaleFactor, FitsImage.ScaleMethod scaleMethod,
            FitsImage.ColorFilter colorFilter, double min, double max){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.zindex = z;
        this.src = src;

        w = parentRegion.getWidth();
        h = parentRegion.getHeight();

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

        this.parentRegion = parentRegion;

        /*
        try{
            FITSImage fitsImage = new FITSImage(src);
            NomWcsKeywordProvider wcsKeyProvider = new NomWcsKeywordProvider(fitsImage.getFits().getHDU(0).getHeader());
            wcsTransform = new WCSTransform(wcsKeyProvider);
            objectName = wcsKeyProvider.getStringValue("OBJECT");
        } catch(IOException ioe){
            wcsTransform = null;
            objectName = "";
        } catch (FitsException fe){
            wcsTransform = null;
            objectName = "";
        } catch(FITSImage.NoImageDataFoundException nidfe){
            wcsTransform = null;
            objectName = "";
        } catch(FITSImage.DataTypeNotSupportedException dtnse) {
            wcsTransform = null;
            objectName = "";
        }
        */

    }

    public boolean isReference(){
        return isReference;
    }

    public void setReference(boolean isReference){
        this.isReference = isReference;
    }

    public URL getSrc(){
        return src;
    }

    public String getObjectName(){
        return objectName;
    }

    public String getID(){
        return id;
    }

    public int getLayerIndex(){
        return layerIndex;
    }

    public boolean isCreatedWithGlobalData(){
        return createdWithGlobalData;
    }


    public void orientTo(double angle){
        this.angle = angle;
        if (glyph != null) glyph.orientTo(angle);
    }
    /*
    public Region getParentRegion(){
        return parentRegion;
    }
    */

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
        //System.out.println("createObject");
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
        //glyph.setSensitivity(isSensitive);
        //System.out.println("glyph.setSensitivity("+isSensitive+")");
        glyph.setVisible(isVisible);
        glyph.setDrawBorder(false);
        glyph.setTranslucencyValue(alpha);

        glyph.setScaleMethod(scaleMethod);
        glyph.setColorFilter(colorFilter);

        if(isRescaleGlobal) glyph.rescale(gmin, gmax, gsigma);
        else glyph.rescale(lmin, lmax, lsigma);

        //System.out.println("localmin: " + lmin + " localmax: " + lmax);
        //System.out.println("globalmin: " + gmin + " globalmax: " + gmax);
        //System.out.println(glyph);
        glyph.orientTo(angle);

        vs.addGlyph(glyph,false);

    }

    public void destroyObject(final SceneManager sm, final VirtualSpace vs, boolean fadeOut){
        //System.out.println("destroyObject");
        if(glyph != null) vs.removeGlyph(glyph);
        glyph = null;
    }

    public double getWidth(){
        return w;
    }

    public double getHeight(){
        return h;
    }

    public double getWidthWithFactor(){
        return w/scaleFactor;
    }

    public double getHeightWithFactor(){
        return h/scaleFactor;
    }

    public double getFactor(){
        return scaleFactor;
    }

    @Override
    public Glyph getGlyph(){
        return glyph;
    }

    @Override
    public double getX(){
        //return glyph.vx;
        return vx;
    }

    @Override
    public double getY(){
        //return glyph.vy;
        return vy;
    }



    /**
     * Converts pixel coordinates to World Coordinates. Returns null if the WCSTransform is not valid.
     * @param x x-coordinates, in the FITS system: (0,0) lower left, x axis increases to the right, y axis increases upwards
     * @param y y-coordinates, in the FITS system: (0,0) lower left, x axis increases to the right, y axis increases upwards
     */
    /*
    public Point2D.Double pix2wcs(double x, double y){
        if(wcsTransform != null) return wcsTransform.pix2wcs(x, y);
        else return null;
    }
    */

    /**
     * Converts World Coordinates to pixel coordinates. Returns null if the WCSTransform is invalid, or if the WCS position does not fall within the image.
     */
    /*
    public Point2D.Double wcs2pix(double ra, double dec){
        if(wcsTransform != null) return wcsTransform.wcs2pix(ra, dec);
        else return null;
    }
    */

    @Override
    public void moveTo(double x, double y){
        if(glyph != null) glyph.moveTo(x, y);
        vx = x;
        vy = y;
        parentRegion.moveTo(x,y);
    }

    public void setTranslucencyValue(float alpha){
        this.alpha = alpha;
        if(glyph != null) glyph.setTranslucencyValue(alpha);
    }

    public void setVisible(boolean visible){
        this.isVisible = visible;
        if(glyph != null) glyph.setVisible(visible);
    }

    public boolean isVisible(){
        return isVisible;
    }

}

