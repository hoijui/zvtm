/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:$
 */
package fr.inria.zvtm.fits.examples;

import edu.jhu.pha.sdss.fits.FITSImage;  
import java.net.URL;
import jsky.coords.WCSTransform;
import jsky.coords.WorldCoords;

import fr.inria.zvtm.fits.NomWcsKeywordProvider;
import fr.inria.zvtm.glyphs.FitsImage;

/**
 * Computes WCS coordinates from a pixel location within a FITS image
 */
class WCSExample {
    public static void main(String[] args) throws Exception{
        if(args.length != 3){
            System.out.println("usage: WCSExample URL x y");
            System.out.println("(where x and y are pixel coordinates)");
            return;
        }
        FitsImage img = new FitsImage(0,0,0,new URL(args[0]),1);
        FITSImage fImg = img.getUnderlyingImage();
        NomWcsKeywordProvider provider = new NomWcsKeywordProvider(fImg.getFits().getHDU(0).getHeader());        
        WCSTransform transform = new WCSTransform(provider);
        System.out.println(new WorldCoords(transform.pix2wcs(Double.parseDouble(args[1]), Double.parseDouble(args[2]))));
    }
}

