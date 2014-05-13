/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010.
 *  Licensed under the GNU GPL. For full terms see the file COPYING.
 *
 * $Id:$
 */ 
package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import fr.inria.zvtm.glyphs.projection.RProjectedCoordsP;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;

import jsky.coords.WCSTransform;
import jsky.image.fits.FITSKeywordProvider;
import jsky.image.fits.codec.FITSImage;
import jsky.image.ImageProcessor;
import jsky.image.ImageLookup;

//import jsky.image.ImageHistogram;
import javax.media.jai.Histogram;

import javax.media.jai.RenderedImageAdapter;

import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;

//Fits support provided by JSky instead of IVOA FITS
//Note: JSkyFitsImage requires JAI (Java Advanced Imaging)

public class JSkyFitsImage extends ClosedShape implements RectangularShape {

    private final FITSImage fitsImage;
    private final String imageLocation;
    private final WCSTransform wcsTransform;
    private final ImageProcessor proc;

    /** Width in virtual space */
    private double vw;
    /** Height in virtual space */
    private double vh;

    private double scale = 1;

    /** For internal use. Made public for easier outside package subclassing. */
    public boolean zoomSensitive = true;

    /** For internal use. Made public for easier outside package subclassing. */
    private RProjectedCoordsP[] pc;

    /** For internal use. Made public for easier outside package subclassing. */
    public AffineTransform at;

    /** For internal use. Made public for easier outside package subclassing. */
    public double scaleFactor = 1.0f;

    /** For internal use. Made public for easier outside package subclassing. */
    public double trueCoef = 1.0f;

    public boolean paintBorder = false;

    /** For internal use. Made public for easier outside package subclassing. */
    public Object interpolationMethod = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;


    public JSkyFitsImage(String fileOrUrl){
        try{
            fitsImage = new FITSImage(fileOrUrl);
        } catch(Exception e){
            //XXX change
            throw new Error("Could not create FitsImage: " + e);
        }
        

        imageLocation = fileOrUrl;
        vw = fitsImage.getWidth() * scale;
        vh = fitsImage.getHeight() * scale;
        RenderedImageAdapter ria = new RenderedImageAdapter(fitsImage);
        Rectangle2D.Double region = new Rectangle2D.Double(0,0, fitsImage.getWidth(), fitsImage.getHeight());
        //proc = new ImageProcessor(ria, new Rectangle2D.Double(0,0, fitsImage.getWidth(), fitsImage.getHeight()));
        proc = new ImageProcessor();
        proc.setSourceImage(ria, region);
        
        /*DEFAULT*/
        proc.setColorLookupTable("Standard");
        proc.setScaleAlgorithm(ImageLookup.LINEAR_SCALE);
        proc.update();
        VirtualSpaceManager.INSTANCE.repaint();

        wcsTransform = new WCSTransform(new FITSKeywordProvider(fitsImage));
    }


    /**
     * Returns the underlying FITSImage
     */
    public FITSImage getUnderlyingImage(){
        return fitsImage;
    }

    @Override public double getWidth(){
        return vw;
    }

    @Override public double getHeight(){
        return vh;
    }

    @Override public void setWidth(double w){
    }

    @Override public void setHeight(double h){
    }

    public String getImageLocation(){
        return imageLocation;
    }

    public double getMinValue(){
        return proc.getMinValue();
    }
    
    public double getMaxValue(){
        return proc.getMaxValue();
    }

    public float getScale(){
        return fitsImage.getScale();
    }


    /*
    public Histogram getHistogram(int size){
        return proc.getHistogram(size, new ROI(proc.getRescaledSourceImage()));
    }
    */

    /*
    public void setScale(float scaleFactor){
        //XXX
    }
    */

    /**
     * Sets the color lookup table.
     * Currently, accepted values are: "Background", "Blue", "Heat", "Isophot", "Light", "Pastel", "Ramp", "Real", "Smooth", "Staircase", "Standard".
     */
    public void setColorLookupTable(String tableName){
        proc.setColorLookupTable(tableName);
        proc.update();
        VirtualSpaceManager.INSTANCE.repaint();
    }

    /**
     * Sets the scale algorithm.
     */
    public void setScaleAlgorithm(ScaleAlgorithm algorithm){
        proc.setScaleAlgorithm(algorithm.toJSkyValue());
        proc.update();
        VirtualSpaceManager.INSTANCE.repaint();
    }
    public void setScaleAlgorithm(int method){
        switch(method){
            case ImageLookup.LINEAR_SCALE:
                proc.setScaleAlgorithm(ImageLookup.LINEAR_SCALE);
                break;
            case ImageLookup.LOG_SCALE:
                proc.setScaleAlgorithm(ImageLookup.LOG_SCALE);
                break;
            case ImageLookup.HIST_EQ:
                proc.setScaleAlgorithm(ImageLookup.HIST_EQ);
                break;
            case ImageLookup.SQRT_SCALE:
                proc.setScaleAlgorithm(ImageLookup.SQRT_SCALE);
                break;

            default:
                proc.setScaleAlgorithm(ImageLookup.LINEAR_SCALE);
                break;
        }
        proc.update();
        VirtualSpaceManager.INSTANCE.repaint();
    }

    public ImageProcessor getImageProcessor(){
        return proc;
    }

/*
             LINEAR{
            @Override int toJSkyValue(){
                return ImageLookup.LINEAR_SCALE;
            }
        },
        LOG{
            @Override int toJSkyValue(){
                return ImageLookup.LOG_SCALE;
            }
        },
        HIST_EQ{
            @Override int toJSkyValue(){
                return ImageLookup.HIST_EQ;
            }
        },
        SQRT{
            @Override int toJSkyValue(){
                return ImageLookup.SQRT_SCALE;
        
    }
*/

    /**
     * Sets the cut levels for this image.
     */
    public void setCutLevels(double lowCut, double highCut){
        proc.setCutLevels(lowCut, highCut);
        proc.update();
        VirtualSpaceManager.INSTANCE.repaint();
    }

    /**
     * Returns an array containing [lowCut, highCut]
     */
    public double[] getCutLevels(){
        return new double[]{proc.getLowCut(), proc.getHighCut()};
    }

    /**
     * Sets the image cut levels automatically using median filtering on the given area of the image.
     */
    public void autoSetCutLevels(Rectangle2D.Double rect){
        proc.autoSetCutLevels(rect);
        proc.update();
    }

    /**
     * Sets the image cut levels automatically using median filtering. 
     */
    public void autoSetCutLevels(){
        autoSetCutLevels(new Rectangle2D.Double(0,0,fitsImage.getWidth(),fitsImage.getHeight()));
    }

    /**
     * Converts pixel coordinates to World Coordinates. Returns null if the WCSTransform is not valid.
     * @param x x-coordinates, in the FITS system: (0,0) lower left, x axis increases to the right, y axis increases upwards
     * @param y y-coordinates, in the FITS system: (0,0) lower left, x axis increases to the right, y axis increases upwards
     */
    public Point2D.Double pix2wcs(double x, double y){
        return wcsTransform.pix2wcs(x, y);
    }

    /**
     * Converts World Coordinates to pixel coordinates. Returns null if the WCSTransform is invalid, or if the WCS position does not fall within the image.
     */
    public Point2D.Double wcs2pix(double ra, double dec){
        return wcsTransform.wcs2pix(ra, dec);
    }

    /** 
     * Gets the bounding box of this Glyph in virtual space coordinates.
     * @return west, north, east and south bounds in virtual space.
     */
    @Override
        public double[] getBounds(){
            double[] res = {vx-vw/2d,vy+vh/2d,vx+vw/2d,vy-vh/2d};
            return res;
        }

    /**
     * {@inheritDoc}
     */
    @Override
        public boolean fillsView(double w, double h, int camIndex){
            return false; //safe option
        }

    /**
     * {@inheritDoc}
     */
    @Override
        public short mouseInOut(int jpx, int jpy, int camIndex, double cvx, double cvy){
            //XXX implement
            return NO_CURSOR_EVENT;
        }

    /**
     * {@inheritDoc}
     */
    @Override
        public void resetMouseIn(){
            //XXX ?
        }

    /**
     * {@inheritDoc}
     */
    @Override 
        public void resetMouseIn(int i){
            //XXX ?
        }

    /**
     * {@inheritDoc}
     */
    @Override
        public boolean coordInside(int jpx, int jpy, int camIndex, double cvx, double cvy){
            //XXX implement
            return true;
        }

    @Override 
        public void removeCamera(int index){
            pc[index]=null;
        }

    @Override 
        public void addCamera(int verifIndex){
            if (pc!=null){
                if (verifIndex==pc.length){
                    RProjectedCoordsP[] ta=pc;
                    pc=new RProjectedCoordsP[ta.length+1];
                    for (int i=0;i<ta.length;i++){
                        pc[i]=ta[i];
                    }
                    pc[pc.length-1]=new RProjectedCoordsP();
                }
                else {System.err.println("JSkyFitsImage:Error while adding camera "+verifIndex);}
            }
            else {
                if (verifIndex==0){
                    pc=new RProjectedCoordsP[1];
                    pc[0]=new RProjectedCoordsP();
                }
                else {System.err.println("JSkyFitsImage:Error while adding camera "+verifIndex);}
            }
        }

    @Override
        public void initCams(int nbCam){
            pc=new RProjectedCoordsP[nbCam];
            for (int i=0;i<nbCam;i++){
                pc[i]=new RProjectedCoordsP();
            }
        }

    @Override
        public void drawForLens(Graphics2D g,
                int vW,
                int vH,
                int i,
                Stroke stdS,
                AffineTransform stdT,
                int dx,
                int dy){
            draw(g, vW, vH, i, stdS, stdT, dx, dy);
        }

    @Override
        public void draw(Graphics2D g,
                int vW,
                int vH,
                int i,
                Stroke stdS,
                AffineTransform stdT,
                int dx,
                int dy){
            //XXX change to make image zoomable
            /*
            double trueCoef = 1; //scaleFactor * coef
            AffineTransform at = AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
            g.setTransform(at);
            if(alphaC != null){
                g.setComposite(alphaC);
            }
            g.drawRenderedImage(proc.getDisplayImage(), AffineTransform.getScaleInstance(trueCoef,trueCoef));
            g.setComposite(acO);
            g.setTransform(stdT);
            */
            //XXX

            // draw from the VImage
            if (alphaC != null && alphaC.getAlpha()==0){return;}
            if ((pc[i].cw>=1) || (pc[i].ch>=1)){
                if (zoomSensitive){
                    trueCoef = scaleFactor*coef;
                }
                else{
                    trueCoef = scaleFactor;
                }
                //a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
                if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;}
                if (trueCoef!=1.0f){
                    // translate
                    at = AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
                    g.setTransform(at);
                    // rescale and draw
                    if (alphaC != null){
                        // translucent
                        g.setComposite(alphaC);
                        if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                            //g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                            g.drawRenderedImage(proc.getDisplayImage(), AffineTransform.getScaleInstance(trueCoef,trueCoef));
                            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                        }
                        else {
                            //g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                            g.drawRenderedImage(proc.getDisplayImage(), AffineTransform.getScaleInstance(trueCoef,trueCoef));
                        }
                        g.setTransform(stdT);
                        if (paintBorder){
                            g.setColor(borderColor);
                            if (stroke!=null) {
                                g.setStroke(stroke);
                                g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
                                g.setStroke(stdS);
                            }
                            else {
                                g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
                            }
                        }
                        g.setComposite(acO);
                    }
                    else {
                        // opaque
                        if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                            //g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                            g.drawRenderedImage(proc.getDisplayImage(), AffineTransform.getScaleInstance(trueCoef,trueCoef));
                            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                        }
                        else {
                            //g.drawImage(image,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                            g.drawRenderedImage(proc.getDisplayImage(), AffineTransform.getScaleInstance(trueCoef,trueCoef));
                        }
                        g.setTransform(stdT);
                        if (paintBorder){
                            g.setColor(borderColor);
                            if (stroke!=null) {
                                g.setStroke(stroke);
                                g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
                                g.setStroke(stdS);
                            }
                            else {
                                g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
                            }
                        }
                    }
                }
                else {
                    if (alphaC != null){
                        // translucent
                        g.setComposite(alphaC);
                        //g.drawImage(image,dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,null);
                        g.drawRenderedImage(proc.getDisplayImage(), AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw, dy+pc[i].cy-pc[i].ch));
                        if (paintBorder){
                            g.setColor(borderColor);
                            if (stroke!=null) {
                                g.setStroke(stroke);
                                g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
                                g.setStroke(stdS);
                            }
                            else {
                                g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
                            }
                        }
                        g.setComposite(acO);
                    }
                    else {
                        // opaque
                        //g.drawImage(image,dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,null);
                        g.drawRenderedImage(proc.getDisplayImage(), AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw, dy+pc[i].cy-pc[i].ch));
                        if (paintBorder){
                            g.setColor(borderColor);
                            if (stroke!=null) {
                                g.setStroke(stroke);
                                g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
                                g.setStroke(stdS);
                            }
                            else {
                                g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
                            }
                        }
                    }
                }
            }
            else {
                g.setColor(this.borderColor);
                g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
            }

        }

    @Override
        public void projectForLens(Camera c,
                int lensWidth,
                int lensHeight,
                float lensMag,
                double lensx,
                double lensy){
           //XXX 
        }

    @Override
        public void project(Camera c, Dimension d){
            int i=c.getIndex();
            coef = c.focal/(c.focal+c.altitude);
            //find coordinates of object's geom center wrt to camera center and project
            //translate in JPanel coords
            pc[i].cx = (int)Math.round((d.width/2d)+(vx-c.vx)*coef);
            pc[i].cy = (int)Math.round((d.height/2d)-(vy-c.vy)*coef);

            //project width and height
            if (zoomSensitive){
            pc[i].cw = (int)Math.round(vw/2d*coef);
            pc[i].ch = (int)Math.round(vh/2d*coef);
            }
            else{
                pc[i].cw = (int)Math.round(vw/2d);
                pc[i].ch = (int)Math.round(vh/2d);
            }
        }

    @Override
        public void highlight(boolean b,
                Color selectedColor){
            
        }

    @Override
        public void orientTo(double angle){
        }

    @Override
        public double getOrient(){
            return 0f;
        }

    @Override 
        public void reSize(double factor){
        }

    @Override
        public void sizeTo(double newSize){
            reSize(newSize/getSize()); 
        }

    @Override
        public boolean visibleInRegion(double wb, double nb, double eb, double sb, int i){
           return true;
        }

    @Override 
        public double getSize(){
            return Math.sqrt(getWidth() * getWidth() + getHeight() * getHeight());
        }

   	@Override
   	public Shape getJava2DShape(){
   		return new Rectangle2D.Double(vx-vw/2.0, vy-vh/2.0, vw, vh);
   	}

    public enum ScaleAlgorithm {
        LINEAR{
            @Override int toJSkyValue(){
                return ImageLookup.LINEAR_SCALE;
            }
        },
        LOG{
            @Override int toJSkyValue(){
                return ImageLookup.LOG_SCALE;
            }
        },
        HIST_EQ{
            @Override int toJSkyValue(){
                return ImageLookup.HIST_EQ;
            }
        },
        SQRT{
            @Override int toJSkyValue(){
                return ImageLookup.SQRT_SCALE;
            }
        };
        abstract int toJSkyValue();
    }

    /**
     * Rescales the image.
     * @param min minimum value - image values below the minimum will be shown
     * in black.
     * @param max maximum value - image values above the maximum will be 
     * saturated.
     */
    /*
    public void rescale(double min, double max, double sigma){
        try{
            fitsImage.rescale(min, max, sigma);
            recreateDisplayImage();
        } catch(Exception ex){
            throw new Error(ex);
        }
    }

    private void recreateDisplayImage(){
        ImageProducer producer = fitsImage.getSource();
        producer = new FilteredImageSource(producer, filter);
        Image filteredImage = Toolkit.getDefaultToolkit().createImage(producer);
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
            getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage compatibleImage = gc.createCompatibleImage(fitsImage.getWidth(),
                fitsImage.getHeight(), fitsImage.getTransparency());
        Graphics g = compatibleImage.getGraphics();
        g.drawImage(filteredImage,0,0,null);
        g.dispose();
        setImage(compatibleImage);
    }
    */
}

