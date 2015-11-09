/*   Copyright (c) INRIA, 2010-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: FitsResourceHandler.java 5247 2014-12-02 20:22:41Z fdelcampo $
 */

package fr.inria.zuist.engine;

import java.net.URL;
import java.awt.Color;
import fr.inria.zvtm.glyphs.JSkyFitsImage;
import fr.inria.zuist.engine.SceneManager;

public class JSkyFitsResourceHandler implements ResourceHandler {
    public static final String RESOURCE_TYPE_FITS = "skyfits";
    private static final String SC_ID = "sc="; //scale factor in params
    private static final String SM_ID = "sm="; //scale method in params
    private static final String CF_ID = "cf="; //color filter in params
    private static final String MIN_VAL_ID = "minvalue="; //min value for rescale in params
    private static final String MAX_VAL_ID = "maxvalue="; //max value for rescale in params
    private static final String REF_ID = "reference"; // fits reference for wcs coordinates
    private static final String HIST_ID = "hist="; // histogram file

    public ResourceDescription createResourceDescription(
            double x, double y, String id, int zindex, Region region,
            URL resourceURL, boolean sensitivity, Color stroke, String params){

        float scaleFactor = 1;

        JSkyFitsImage.ScaleAlgorithm scaleMethod = JSkyFitsImage.ScaleAlgorithm.LINEAR;
        String colorLookupTable = "Heat";

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        boolean reference = false;

        String hist = "";

        if (params != null){
            String[] paramTokens = params.split(SceneManager.PARAM_SEPARATOR);
            for (int i=0;i<paramTokens.length;i++) {
                if (paramTokens[i].startsWith(SC_ID)){
                    scaleFactor = Float.parseFloat(paramTokens[i].substring(SC_ID.length()));
                }
                else if (paramTokens[i].startsWith(SM_ID)){
                    try{
                        scaleMethod = JSkyFitsImage.ScaleAlgorithm.valueOf(paramTokens[i].substring(SM_ID.length()));
                    } catch(IllegalArgumentException ignored){
                        System.err.println("Incorrect scale method, using default instead");
                    }
                }
                else if (paramTokens[i].startsWith(CF_ID)){
                    try{
                        colorLookupTable = paramTokens[i].substring(CF_ID.length());
                    } catch(IllegalArgumentException ignored){
                        System.err.println("Incorrect color filter, using default instead");
                    }
                }
                else if (paramTokens[i].startsWith(MIN_VAL_ID)){
                    try{
                        min = Double.parseDouble(paramTokens[i].substring(MIN_VAL_ID.length()));
                    } catch(IllegalArgumentException ignored){
                        System.err.println("Incorrect min value, using default instead");
                    }
                }
                else if (paramTokens[i].startsWith(MAX_VAL_ID)){
                    try{
                        max = Double.parseDouble(paramTokens[i].substring(MAX_VAL_ID.length()));
                    } catch(IllegalArgumentException ignored){
                        System.err.println("Incorrect max value, using default instead");
                    }
                }
                else if(paramTokens[i].startsWith(REF_ID)){
                    reference = true;
                }
                else if(paramTokens[i].startsWith(HIST_ID) ){
                    hist = paramTokens[i].substring(HIST_ID.length());
                }
                else {
                    System.err.println("Unknown type of resource parameter: "+paramTokens[i]);
                }
            }
        }
        
        JSkyFitsImageDescription desc;
        if (max != Double.MIN_VALUE && min != Double.MAX_VALUE){
            desc = new JSkyFitsImageDescription(
                id, x, y, zindex, resourceURL, region,
                scaleFactor, scaleMethod, colorLookupTable, min, max
            );
        } else {
            desc = new JSkyFitsImageDescription(
                id, x, y, zindex, resourceURL, region,
                scaleFactor, scaleMethod, colorLookupTable
            );  
        }
        if(reference){
            desc.setReference(reference);
        }

        desc.setHistogram(hist);
        
        region.addObject(desc);
        return desc;
    }


}

