/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.fits;

import javax.swing.SwingUtilities;
import java.io.File;

import jep.Jep;
import jep.JepException;
import jep.NDArray;

public class CoordConv {

    Jep astropyConverter;

    public CoordConv(){
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
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
        });
    }

    public void setFITSFile(final String fitsFilePath){
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                _setFITSFile(fitsFilePath);
            }
        });
    }

    public void _setFITSFile(String fitsFilePath){
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

    public void wcs2pix(final double ra, final double dec, final int[] res){
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                _wcs2pix(ra, dec, res);
            }
        });
    }

    // expects to be called from EDT
    public int[] _wcs2pix(double ra, double dec, int[] res){
        try {
            astropyConverter.set("ra", ra);
            astropyConverter.set("dec", dec);
            astropyConverter.eval("p_res = w.wcs_world2pix(numpy.array([[ra, dec]]), 1)");
            double[] p_res = ((NDArray<double[]>)astropyConverter.getValue("p_res")).getData();
            res[0] = (int)Math.round(p_res[0]);
            res[1] = (int)Math.round(p_res[1]);
        }
        catch (JepException ex){
            ex.printStackTrace();
            return null;
        }
        return res;
    }

    public void pix2wcs(final double x, final double y){
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                _pix2wcs(x, y);
            }
        });
    }

    // expects to be called from EDT
    public double[] _pix2wcs(double x, double y){
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

    public void close(){
        System.out.print("Shutting down astropy...");
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                astropyConverter.close();
            }
        });
        System.out.println("OK");
    }

}
