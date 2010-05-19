package fr.inria.zuist.engine;

import java.net.URL;
import java.awt.Color;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zuist.engine.SceneManager;

public class FitsResourceHandler implements ResourceHandler {
    public static final String RESOURCE_TYPE_FITS = "fits";
    private static final String SC_ID = "sc="; //scale factor in params
    private static final String SM_ID = "sm="; //scale method in params
    private static final String CF_ID = "cf="; //color filter in params

    public ResourceDescription createResourceDescription(
            long x, long y, String id, int zindex, Region region, 
            URL resourceURL, boolean sensitivity, Color stroke, String params){
        float scaleFactor = 1;
        FitsImage.ScaleMethod scaleMethod = FitsImage.ScaleMethod.ASINH;
        FitsImage.ColorFilter colorFilter = FitsImage.ColorFilter.NOP;

        if (params != null){
            String[] paramTokens = params.split(SceneManager.PARAM_SEPARATOR);
            for (int i=0;i<paramTokens.length;i++) {
                if (paramTokens[i].startsWith(SC_ID)){
                    scaleFactor = Float.parseFloat(paramTokens[i].substring(SC_ID.length()));
                }
                else if (paramTokens[i].startsWith(SM_ID)){
                    try{
                        scaleMethod = FitsImage.ScaleMethod.valueOf(paramTokens[i].substring(SM_ID.length()));
                    } catch(IllegalArgumentException ignored){
                        System.err.println("Incorrect scale method, using default instead");
                    }
                }
                else if (paramTokens[i].startsWith(CF_ID)){
                    try{
                        colorFilter = FitsImage.ColorFilter.valueOf(paramTokens[i].substring(CF_ID.length()));
                    } catch(IllegalArgumentException ignored){
                        System.err.println("Incorrect color filter, using default instead");
                    }
                }
                else {
                    System.err.println("Unknown type of resource parameter: "+paramTokens[i]);
                }
            }            
        }

        FitsImageDescription desc = new FitsImageDescription(
                id, x, y, zindex, resourceURL, region, 
                scaleFactor, scaleMethod, colorFilter);
        region.addObject(desc);
        return desc;             
            }
}

