/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.fits;

import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;
import java.io.File;

import jep.Jep;
import jep.JepException;
import jep.NDArray;

import fr.inria.zvtm.glyphs.JSkyFitsImage;

public class CoordConv {

    Jep astropyConverter;

    JSkyFitsImage img;

    public CoordConv(){
        try {
            astropyConverter = new Jep(false);
            astropyConverter.eval("from __future__ import division # confidence high");
            astropyConverter.eval("import numpy");
            astropyConverter.eval("from astropy import wcs");
            astropyConverter.eval("from astropy.io import fits");
        }
        catch (JepException ex){
            ex.printStackTrace();
        }
    }

    public void setFITSFile(JSkyFitsImage img, String fitsFilePath){
        this.img = img;
        // should be an absolute file path
        try {
            astropyConverter.set("fitsFilePath", fitsFilePath);
            astropyConverter.eval("hdulist = fits.open(fitsFilePath)");
            astropyConverter.eval("w = wcs.WCS(hdulist[0].header)");
            System.out.println("Set FITS file to " + fitsFilePath);
        }
        catch (JepException ex){
            ex.printStackTrace();
        }
    }

    // expects to be called from EDT
    public double[] wcs2pix(double ra, double dec, double[] res){
        try {
            astropyConverter.set("ra", ra);
            astropyConverter.set("dec", dec);
            astropyConverter.eval("p_res = w.wcs_world2pix(numpy.array([[ra, dec]]), 1)");
            return ((NDArray<double[]>)astropyConverter.getValue("p_res")).getData();
        }
        catch (JepException ex){
            ex.printStackTrace();
            return null;
        }
    }

    // expects to be called from EDT
    public double[] pix2wcs(double x, double y){
        if (x < 0 || y < 0 ||
            x > img.getRawFITSImage().getWidth() || y > img.getRawFITSImage().getHeight()){
            return null;
        }
        double[] res;
        try {
            astropyConverter.set("x", x);
            astropyConverter.set("y", y);
            astropyConverter.eval("wcscoords = w.wcs_pix2world(numpy.array([[x,y]], numpy.float_), 1)");
            res = ((NDArray<double[]>)astropyConverter.getValue("wcscoords")).getData();
        }
        catch (JepException ex){
            ex.printStackTrace();
            return null;
        }
        return res;
    }

    /** Converts from World Coordinates to VirtualSpace coordinates.
     *@param ra right ascension (in degrees).
     *@param dec declination (in degrees).
     *@return null if coords are outside the image
     */
    public Point2D.Double wcs2vs(double ra, double dec){
        double[] pix = wcs2pix(ra, dec, new double[2]);
        // got FITS image coords, now convert them to virtual space coords
        return new Point2D.Double(
            pix[0]*img.getScale()+img.vx-img.getWidth()/2d,
            pix[1]*img.getScale()+img.vy-img.getHeight()/2d);
    }


    public Point2D.Double vs2wcs(double pvx, double pvy){
        // convert to FITS image coords
        if (pvx < img.vx-img.getWidth()/2d || pvx > img.vx+img.getWidth()/2d ||
            pvy < img.vy-img.getHeight()/2d || pvy > img.vy+img.getHeight()/2d){
            return null;
        }
        double x = (pvx-img.vx+img.getWidth()/2d)/img.getScale();
        double y = (pvy-img.vy+img.getHeight()/2d)/img.getScale();
        double[] tres = pix2wcs(x, y);
        return new Point2D.Double(tres[0], tres[1]);
    }

    public void close(){
        System.out.print("Shutting down astropy...");
        astropyConverter.close();
        System.out.println("OK");
    }

}
