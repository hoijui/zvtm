package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;
import java.awt.LinearGradientPaint;

public class NopFilter extends RGBImageFilter implements ColorGradient {
    
    public NopFilter(){
        canFilterIndexColorModel = true;
    }

    public int filterRGB(int x, int y, int rgb) {
        return rgb;
    }
    
    public LinearGradientPaint getGradient(float w){
        return getGradientS(w);
    }
    
    public static LinearGradientPaint getGradientS(float w){
        float[] fractions = {0, 1};
        Color[] map = {Color.BLACK, Color.BLACK};
        return new LinearGradientPaint(0, 0, w, 0, fractions, map);
    }
    
}

