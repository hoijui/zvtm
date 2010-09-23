package fr.inria.zvtm.cluster;

import fr.inria.zvtm.glyphs.Glyph;

class AstroUtil {
    private AstroUtil(){}

    static boolean isInside(Glyph glyph, double x, double y){
        double[] wnes = glyph.getBounds(); 
        return((x >= wnes[0]) && (x <= wnes[2]) &&
                (y >= wnes[3]) && (y <= wnes[1]));
    }

}

