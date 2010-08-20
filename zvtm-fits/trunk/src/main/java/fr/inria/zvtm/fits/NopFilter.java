package fr.inria.zvtm.fits;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

public class NopFilter extends RGBImageFilter {
    public NopFilter(){
        canFilterIndexColorModel = true;
    }

    public int filterRGB(int x, int y, int rgb) {
        return rgb;
    }
}

