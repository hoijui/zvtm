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
 * Computes WCS coordinates from pixel location in a FITS image
 */
class PIX2WCS {

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
        jep.set("x", Double.parseDouble(args[1]));
        jep.set("y", Double.parseDouble(args[2]));
        jep.eval("wcscoords = w.wcs_pix2world(numpy.array([[x,y]], numpy.float_), 1)");
        double[] wcsCoords = ((NDArray<double[]>)jep.getValue("wcscoords")).getData();
        System.out.println(wcsCoords[0]+" "+wcsCoords[1]);
        jep.close();
    }
}
