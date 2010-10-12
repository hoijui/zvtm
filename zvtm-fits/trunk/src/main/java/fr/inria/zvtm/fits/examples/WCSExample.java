package fr.inria.zvtm.fits.examples;

import edu.jhu.pha.sdss.fits.FITSImage;  
import java.net.URL;
import skyview.geometry.Transformer;
import skyview.geometry.WCS;

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
        WCS celestial2pixel = new WCS(fImg.getFits().getHDU(0).getHeader());
        Transformer pixel2celestial = celestial2pixel.inverse();
        System.out.println("celestial2pixel input dim: " + celestial2pixel.getInputDimension());
        System.out.println("celestial2pixel output dim: " + celestial2pixel.getOutputDimension());
        double[] celestialCoords = pixel2celestial.transform(new double[]{Double.parseDouble(args[1]), Double.parseDouble(args[2])});
        System.out.print("Celestial coords: ");
        for(double d: celestialCoords){
            System.out.print(d*180./Math.PI + " ");
        }
        System.out.println("");
    }
}

