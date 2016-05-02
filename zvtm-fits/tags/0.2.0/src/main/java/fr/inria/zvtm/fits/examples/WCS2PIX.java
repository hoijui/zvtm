/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.fits.examples;

import java.io.File;

import jep.Jep;
import jep.NDArray;

/**
 * Computes pixel location in a FITS image from WCS coordinates
 */
class WCS2PIX {

    public static void main(String[] args) throws Exception{
        File fitsFile = new File(args[0]);
        Jep jep = new Jep(false);
        jep.eval("from __future__ import division # confidence high");
        jep.eval("import numpy");
        jep.eval("from astropy import wcs");
        jep.eval("from astropy.io import fits");
        jep.set("fitsFilePath", fitsFile.getAbsolutePath());
        jep.eval("hdulist = fits.open(fitsFilePath)");
        jep.eval("w = wcs.WCS(hdulist[0].header)");
        jep.set("ra", Double.parseDouble(args[1]));
        jep.set("dec", Double.parseDouble(args[2]));
        jep.eval("pixcoords = w.wcs_world2pix(numpy.array([[ra, dec]]), 1)");
        double[] pixCoords = ((NDArray<double[]>)jep.getValue("pixcoords")).getData();
        int x = (int)Math.round(pixCoords[0]);
        int y = (int)Math.round(pixCoords[1]);
        System.out.println(x+" "+y);
        jep.close();
    }
}
