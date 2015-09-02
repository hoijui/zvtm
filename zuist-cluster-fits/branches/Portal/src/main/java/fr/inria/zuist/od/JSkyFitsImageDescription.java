/*   Copyright (c) INRIA, 2010-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: FitsImageDescription.java 5249 2014-12-11 19:33:30Z fdelcampo $
 */

package fr.inria.zuist.od;

import java.io.IOException;
import java.net.URL;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.Region;
import fr.inria.zvtm.glyphs.JSkyFitsImage;
import fr.inria.zvtm.glyphs.Glyph;

import java.awt.image.ImageFilter;

import fr.inria.zuist.engine.JSkyFitsResourceHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import jsky.coords.WCSTransform;
import fr.inria.zvtm.fits.NomWcsKeywordProvider;
//import edu.jhu.pha.sdss.fits.FITSImage; 
import java.awt.geom.Point2D;
//import nom.tam.fits.FitsException;

/**
 * Describes a FITS images and creates / releases the corresponding
 * ZVTM glyph on demand.
 */
public class JSkyFitsImageDescription extends ResourceDescription {

    private static final Logger logger = LoggerFactory.getLogger(JSkyFitsImageDescription.class);

    private float scaleFactor = 1;
    private JSkyFitsImage.ScaleAlgorithm scaleMethod;
    private String colorLookupTable;
    private URL src;
    private String id;
    //private double vx;
    //private double vy;
    private int zindex;
    private double vw;
    private double vh;

    public static short GLOBAL = 0;
    public static short LOCAL = 1;
    public static short CUSTOM = 2;

    //private WCSTransform wcsTransform;
    private String objectName; 

    private float alpha = 1f;
    private boolean isVisible = false;
    //private boolean isSensitive;

    private double gmin = Double.MAX_VALUE;
    private double gmax = Double.MIN_VALUE;
    //private double gsigma;

    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;

    //private boolean isRescaleGlobal = false;
    //private boolean isRescaleLocal = false;

    private double lmin = Double.MAX_VALUE;
    private double lmax = Double.MIN_VALUE;

    private short mode;
    //private double lsigma;

    private volatile boolean display = true;
    //private Color strokeColor = null;

    private JSkyFitsImage glyph;

    private boolean isReference = false;

    private String hist;

    private double angle = 0;

    //private String[] tags;

    private boolean createdWithGlobalData = false;

    private Region parentRegion;

    public JSkyFitsImageDescription(String id, double x, double y, int z, URL src,
            Region parentRegion, float scaleFactor, JSkyFitsImage.ScaleAlgorithm scaleMethod,
            String colorLookupTable){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.zindex = z;
        this.src = src;

        this.vw = parentRegion.getWidth();
        this.vh = parentRegion.getHeight();

        this.scaleFactor = scaleFactor;
        this.scaleMethod = scaleMethod;
        this.colorLookupTable = colorLookupTable;

        mode = LOCAL;

        isVisible = true;

        //tags = parentRegion.getTags(); //parentRegion.getLayerIndex()-1;  XXX: change layer to tag

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

    public JSkyFitsImageDescription(String id, double x, double y, int z, URL src,
            Region parentRegion, float scaleFactor, JSkyFitsImage.ScaleAlgorithm scaleMethod,
            String colorLookupTable, double min, double max){
        this.id = id;
        this.vx = x;
        this.vy = y;
        this.zindex = z;
        this.src = src;

        this.vw = parentRegion.getWidth();
        this.vh = parentRegion.getHeight();


        this.scaleFactor = scaleFactor;
        this.scaleMethod = scaleMethod;
        this.colorLookupTable = colorLookupTable;

        gmin = min;
        gmax = max;

        this.min = min;
        this.max = max;
        //gsigma = min/2. + max/2.;

        //isRescaleGlobal = true;
        
        createdWithGlobalData = true;
        mode = GLOBAL;

        isVisible = true;

        //tags; //layerIndex = 1;//parentRegion.getLayerIndex()-1; XXX: Layer Index to Tag

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

    public boolean hasTag(String tag){
        return parentRegion.hasTag(tag);
    }

    public void setHistogram(String file){
        hist = file;
    }

    public String getHistogram(){
        return hist;
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

    /*
    public int getLayerIndex(){
        return layerIndex;
    }
    */

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
        return JSkyFitsResourceHandler.RESOURCE_TYPE_FITS;
    }

    public void setColorLookupTable(String colorLookupTable, boolean updateDisplay){
        this.colorLookupTable = colorLookupTable;
        if(glyph != null){
            glyph.setColorLookupTable(colorLookupTable, updateDisplay);
        }
    }


    public void setScaleAlgorithm(JSkyFitsImage.ScaleAlgorithm algorithm, boolean updateDisplay){
        this.scaleMethod = algorithm;
        if(glyph != null){
            glyph.setScaleAlgorithm(algorithm, updateDisplay);
        }
    }

    public void setRescaleGlobal(double min, double max){
        gmin = (min < gmin) ? min : gmin;
        gmax = (max > gmax) ? max : gmax;
    }

    /*
    public void setRescaleGlobal(boolean isGlobal){
        isRescaleGlobal = isGlobal;
        if(isGlobal)
            mode = GLOBAL;
    }

    public void setRescaleLocal(boolean isLocal){
        isRescaleLocal = isLocal;
    }
    */

    public void changeMode(short mode){
        this.mode = mode;
    }

    public void rescale(double min, double max, boolean updateDisplay){
        mode = CUSTOM;
        this.min = min;
        this.max = max;
        if(glyph != null) glyph.setCutLevels(min, max, updateDisplay);
    }

    public void rescaleGlobal(){
        mode = GLOBAL;
        if(glyph != null) glyph.setCutLevels(gmin, gmax, true);
    }

    public void rescaleLocal(){
        mode = LOCAL;
        if(glyph != null) glyph.setCutLevels(lmin, lmax, true);
    }

    public double[] getLocalScaleParams(){
        return new double[]{lmin, lmax};
    }

    public double[] getGlobalScaleParams(){
        return new double[]{gmin, gmax};
    }

    //public void createObject(final VirtualSpace vs, final boolean fadeIn){
    public void createObject(final SceneManager sm, final VirtualSpace vs, final boolean fadeIn){
        //System.out.println("createObject");
        try{

            glyph = new JSkyFitsImage(vx,vy,zindex,src,scaleFactor);

            lmin = glyph.getCutLevels()[0];
            lmax = glyph.getCutLevels()[1];
            
        } catch(Exception ioe){
            System.out.println("Could not create FitsImage");
            throw new Error("Could not create FitsImage");
        }
        
        //if(gmin != Double.MAX_VALUE && gmax != Double.MIN_VALUE) glyph.rescale(gmin, gmax, gmin/2. + gmax/2.);
        //glyph.setSensitivity(isSensitive);
        //System.out.println("glyph.setSensitivity("+isSensitive+")");
        glyph.setVisible(isVisible);
        //glyph.setDrawBorder(false);
        glyph.setTranslucencyValue(alpha);

        glyph.setScaleAlgorithm(scaleMethod, false);
        glyph.setColorLookupTable(colorLookupTable, false);

        if(mode == GLOBAL) glyph.setCutLevels(gmin, gmax, true);
        else if(mode == LOCAL) glyph.setCutLevels(lmin, lmax, true);
        else glyph.setCutLevels(min, max, true);

        //System.out.println("localmin: " + lmin + " localmax: " + lmax);
        //System.out.println("globalmin: " + gmin + " globalmax: " + gmax);
        //System.out.println(glyph);
        glyph.orientTo(angle);

        vs.addGlyph(glyph, false);

    }

    public void destroyObject(final SceneManager sm, final VirtualSpace vs, boolean fadeOut){
        //System.out.println("destroyObject");
        if(glyph != null) vs.removeGlyph(glyph);
        glyph = null;
    }

    public double getWidth(){
        return vw;
    }

    public double getHeight(){
        return vh;
    }

    public double getWidthWithFactor(){
        return vw/scaleFactor;
    }

    public double getHeightWithFactor(){
        return vh/scaleFactor;
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
        super.moveTo(x,y);
        if(glyph != null) glyph.moveTo(x, y);
        //vx = x;
        //vy = y;
        //parentRegion.moveTo(x,y);
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

    @Override
    public boolean coordInside(double pvx, double pvy){
        return (vx >= pvx-vw/2d && vx <= pvx+vw/2d && vy >= pvy-vw/2d && vy <= pvy+vw/2d);
    }
    
}

