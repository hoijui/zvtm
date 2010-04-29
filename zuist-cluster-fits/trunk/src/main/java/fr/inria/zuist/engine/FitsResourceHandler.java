package fr.inria.zuist.engine;

import java.net.URL;
import java.awt.Color;

public class FitsResourceHandler implements ResourceHandler {
    public static final String RESOURCE_TYPE_FITS = "fits";

    public ResourceDescription createResourceDescription(
            long x, long y, String id, int zindex, Region region, 
            URL resourceURL, boolean sensitivity, Color stroke, String params){
        return null;
            }
}

