package fr.inria.zvtm.fits;

import java.awt.Color;
import java.util.List;
import fr.inria.zvtm.glyphs.Composite;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;

/**
 * Graphical representation of an histogram
 */
public class FitsHistogram extends Composite {
    private static final int DEFAULT_BIN_WIDTH = 6;

    public FitsHistogram(int[] data){
        int i = 0;
        for(int val: data){
            VRectangle bar = new VRectangle(i, val, 0, DEFAULT_BIN_WIDTH/2, val, 
                        new Color(0,0,255,127));
            bar.setBorderColor(new Color(0,0,255,180));
            addChild(bar);
            i += DEFAULT_BIN_WIDTH;
        }
    }

}

