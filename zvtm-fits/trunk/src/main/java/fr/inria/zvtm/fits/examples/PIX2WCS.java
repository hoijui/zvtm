/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: WCSExample.java 5441 2015-03-31 10:37:57Z epietrig $
 */

package fr.inria.zvtm.fits.examples;

import java.awt.geom.Point2D;

import java.net.URL;

import jsky.coords.WCSTransform;
import jsky.coords.WorldCoords;

import fr.inria.zvtm.glyphs.JSkyFitsImage;
import jsky.image.fits.codec.FITSImage;
import jsky.image.fits.FITSKeywordProvider;

/**
 * Computes WCS coordinates from a pixel location within a FITS image
 */
class PIX2WCS {
    public static void main(String[] args) throws Exception{
        if(args.length != 3){
            System.out.println("usage: PIX2WCS URL x y");
            System.out.println("(where x and y are pixel coordinates)");
            return;
        }
        JSkyFitsImage jfi = new JSkyFitsImage(new URL(args[0]));
        WCSTransform transform = new WCSTransform(new FITSKeywordProvider(jfi.getRawFITSImage()));
        Point2D.Double p = transform.pix2wcs(Double.parseDouble(args[1]), Double.parseDouble(args[2]));
        System.out.println(p.x+" "+p.y);
        System.out.println(new WorldCoords(p));
    }
}
