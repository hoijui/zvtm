package fr.inria.zuist.engine;

import java.net.URL;
import java.awt.Color;
import fr.inria.zvtm.glyphs.FitsImage;

public class FitsResourceHandler implements ResourceHandler {
    public static final String RESOURCE_TYPE_FITS = "fits";

    public ResourceDescription createResourceDescription(
            long x, long y, String id, int zindex, Region region, 
            URL resourceURL, boolean sensitivity, Color stroke, String params){
        //TODO scaleFactor (hardcoded to 1 at the moment)
        //TODO scaleMethod (hardcoded to ASINH)
        FitsImageDescription desc = new FitsImageDescription(
                id, x, y, zindex, resourceURL, region, 
                1, FitsImage.ScaleMethod.ASINH);
        region.addObject(desc);
        return desc;             
            }
}

