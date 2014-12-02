/*   Copyright (c) INRIA, 2010-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

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
    private static final String MIN_VAL_ID = "minvalue="; //min value for rescale in params
    private static final String MAX_VAL_ID = "maxvalue="; //max value for rescale in params
    private static final String REF_ID = "reference"; // fits reference for wcs coordinates

    public ResourceDescription createResourceDescription(
            double x, double y, String id, int zindex, Region region,
            URL resourceURL, boolean sensitivity, Color stroke, String params){

        float scaleFactor = 1;

        FitsImage.ScaleMethod scaleMethod = FitsImage.ScaleMethod.LINEAR;
        FitsImage.ColorFilter colorFilter = FitsImage.ColorFilter.HEAT;

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        boolean reference = false;

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
                else {
                    System.err.println("Unknown type of resource parameter: "+paramTokens[i]);
                }
            }
        }
        
        FitsImageDescription desc;
        if (max != Double.MIN_VALUE && min != Double.MAX_VALUE){
            desc = new FitsImageDescription(
                id, x, y, zindex, resourceURL, region,
                scaleFactor, scaleMethod, colorFilter, min, max
            );
        } else {
            desc = new FitsImageDescription(
                id, x, y, zindex, resourceURL, region,
                scaleFactor, scaleMethod, colorFilter
            );  
        }
        if(reference){
            desc.setReference(reference);
        }
        
        region.addObject(desc);
        return desc;
    }


}

