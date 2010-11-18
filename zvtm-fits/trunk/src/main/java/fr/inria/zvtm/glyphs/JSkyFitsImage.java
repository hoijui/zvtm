/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010.
 *  Licensed under the GNU GPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.projection.RProjectedCoordsP;

import jsky.coords.WCSTransform;
import jsky.image.fits.FITSKeywordProvider;
import jsky.image.fits.codec.FITSImage;
import jsky.image.ImageProcessor;
import jsky.image.ImageLookup;

import javax.media.jai.RenderedImageAdapter;

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

    private RProjectedCoordsP[] pc;


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
        proc = new ImageProcessor(new RenderedImageAdapter(fitsImage), new Rectangle2D.Double(0,0, fitsImage.getWidth(), fitsImage.getHeight()));
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

    public void setScale(float scaleFactor){
        //XXX
    }

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
            double trueCoef = 1; //scaleFactor * coef
            AffineTransform at = AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
            g.setTransform(at);
            if(alphaC != null){
                g.setComposite(alphaC);
            }
            g.drawRenderedImage(proc.getDisplayImage(), AffineTransform.getScaleInstance(trueCoef,trueCoef));
            g.setComposite(acO);
            g.setTransform(stdT);
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
            pc[i].cw = (int)Math.round(vw/2d);
            pc[i].ch = (int)Math.round(vh/2d);
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
        public void move(double dx, double dy){
            vx += dx;
            vy += dy;
        }

    @Override
        public void moveTo(double x, double y){
            move(x - vx, y - vy);
        }

    @Override
        public boolean visibleInRegion(double wb, double nb, double eb, double sb, int i){
           return true;
           // double vw = (bbox[2] - bbox[0])/2d;
           // double vh = (bbox[1] - bbox[3])/2d; 
           // double cx = bbox[0] + vw;
           // double cy = bbox[3] + vh; 
           // return ((cx-vw)<=eb) && ((cx+vw)>=wb) && 
           //     ((cy-vh)<=nb) && ((cy+vh)>=sb);
        }

    @Override 
        public double getSize(){
            //return (Math.sqrt((bbox[1] - bbox[3])*(bbox[1] - bbox[3]) + 
            //            (bbox[2] - bbox[0])*(bbox[2] - bbox[0])));
            return 0;
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
}

