package fr.inria.zuist.engine;

import java.net.URL;
import java.awt.Color;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zuist.engine.SceneManager;

public class FitsResourceHandler implements ResourceHandler {
    public static final String RESOURCE_TYPE_FITS = "fits";
    private static final String SC_ID = "sc="; //scale factor in params
    private static final String SM_ID = "sm="; //scale method in params

    public ResourceDescription createResourceDescription(
            long x, long y, String id, int zindex, Region region, 
            URL resourceURL, boolean sensitivity, Color stroke, String params){
        //TODO scaleMethod (hardcoded to ASINH)
        float scaleFactor = 1;
        if (params != null){
            String[] paramTokens = params.split(SceneManager.PARAM_SEPARATOR);
            for (int i=0;i<paramTokens.length;i++) {
                if (paramTokens[i].startsWith(SC_ID)){
                    scaleFactor = Float.parseFloat(paramTokens[i].substring(SC_ID.length()));
                }
                else if (paramTokens[i].startsWith(SM_ID)){
                    //TODO 
                }
                else {
                    System.err.println("Uknown type of resource parameter: "+paramTokens[i]);
                }
            }            
        }

        FitsImageDescription desc = new FitsImageDescription(
                id, x, y, zindex, resourceURL, region, 
                scaleFactor, FitsImage.ScaleMethod.ASINH);
        region.addObject(desc);
        return desc;             
            }
}

