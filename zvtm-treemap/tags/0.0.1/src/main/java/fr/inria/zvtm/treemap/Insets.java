package fr.inria.zvtm.treemap;

public class Insets {
    public double left = 0;
    public double right = 0;
    public double top = 0;
    public double bottom = 0;
    
    public Insets(double left, double top, double right, double bottom){
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }
    
    Rect applyTo(Rect r){
        Rect retval = new Rect(r);
        retval.x += left;
        retval.y += bottom;
        retval.w -= (left + right);
        retval.h -= (top + bottom);
        return retval;
    }

    Rect applyHoriz(Rect r){
        Rect retval = new Rect(r);
        retval.x += left;
        retval.w -= (left + right);
        return retval;
    }

    Rect applyVert(Rect r){
        Rect retval = new Rect(r);
        retval.y += bottom;
        retval.h -= (top + bottom);
        return retval;
    }
}

