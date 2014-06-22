package fr.inria.zvtm.cluster;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.io.Serializable;

class Strokes {
    //disallow instanciation
    private Strokes(){}

    // Returns a serializable Stroke, or null
    static final Stroke wrapStroke(Stroke orig){
        if(orig instanceof Serializable){
            return orig;
        } else if(orig instanceof BasicStroke){
            return new ClusteredStroke((BasicStroke)orig);
        } else {
            return null;
        }
    }
}

