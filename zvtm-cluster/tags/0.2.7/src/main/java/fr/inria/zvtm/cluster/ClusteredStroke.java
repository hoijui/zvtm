package fr.inria.zvtm.cluster;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

//simple serializable wrapper around BasicStroke
public class ClusteredStroke implements Stroke, Serializable {
    private float width;
    private int cap;
    private int join;
    private float miterlimit;
    private float[] dash;
    private float dash_phase;
    private transient BasicStroke bs;

    public ClusteredStroke(float width, int cap, int join, float miterlimit, float[] dash, float dash_phase){
        this.width = width;
        this.cap = cap;
        this.join = join;
        this.miterlimit = miterlimit;
        this.dash = dash;
        this.dash_phase = dash_phase;
        bs = new BasicStroke(width, cap, join, miterlimit, dash, dash_phase);
    }

    public ClusteredStroke(BasicStroke bs){
        this(bs.getLineWidth(), bs.getEndCap(), bs.getLineJoin(), bs.getMiterLimit(), bs.getDashArray(),
                bs.getDashPhase());
    }

    public Shape createStrokedShape(Shape s){
        return bs.createStrokedShape(s);
    }

    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        bs = new BasicStroke(width, cap, join, miterlimit, dash, dash_phase);
    }
}

