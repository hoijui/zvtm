package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

// Various serializable paints

class Paints {
    //disallow instanciation
    private Paints(){}
    // wraps well-known Paint types into corresponding serializable types
    static final Paint wrapPaint(Paint orig){
        if(orig instanceof Serializable){
            return orig;
        } else if (orig instanceof GradientPaint){
            return new GradPaint((GradientPaint)orig);
        } else if(orig instanceof LinearGradientPaint){
            return new LinGradPaint((LinearGradientPaint)orig);
        } else {
            return null;
        }
    }
}

// A serializable LinearGradientPaint
// XXX transparency
class LinGradPaint implements Paint, Serializable {
    private transient LinearGradientPaint paint;
    private final float startX;
    private final float startY;
    private final float endX;
    private final float endY;
    private final float[] fractions;
    private final Color[] colors;
    private final MultipleGradientPaint.CycleMethod cycleMethod;
    private final MultipleGradientPaint.ColorSpaceType colorSpace;
    private final AffineTransform gradientTransform;

    LinGradPaint(LinearGradientPaint paint){
        this.paint = paint;
        startX = (float)(paint.getStartPoint().getX());
        startY = (float)(paint.getStartPoint().getY());
        endX = (float)(paint.getEndPoint().getX());
        endY = (float)(paint.getEndPoint().getY());
        fractions = Arrays.copyOf(paint.getFractions(), paint.getFractions().length);
        colors = Arrays.copyOf(paint.getColors(), paint.getColors().length);
        cycleMethod = paint.getCycleMethod();
        colorSpace = paint.getColorSpace();
        gradientTransform = paint.getTransform();
    }

    public PaintContext createContext(ColorModel cm,
            Rectangle deviceBounds,
            Rectangle2D userBounds,
            AffineTransform xform,
            RenderingHints hints){
        return paint.createContext(cm, deviceBounds, userBounds, xform, hints);
    }

    public int getTransparency(){
        return paint.getTransparency();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        paint = new LinearGradientPaint(new Point2D.Double(startX, startY), new Point2D.Double(endX, endY), fractions, colors, cycleMethod, colorSpace, gradientTransform);
    }
}

// a serializable GradientPaint
class GradPaint implements Paint, Serializable {
    private transient GradientPaint paint;
    private final float x1;
    private final float y1;
    private final float x2;
    private final float y2;
    private final Color color1;
    private final Color color2;
    private final boolean cyclic;

    GradPaint(GradientPaint paint){
        this.paint = paint;
        x1 = (float)paint.getPoint1().getX();
        y1 = (float)paint.getPoint1().getY();
        x2 = (float)paint.getPoint2().getX();
        y2 = (float)paint.getPoint2().getY();
        color1 = paint.getColor1();
        color2 = paint.getColor2();
        cyclic = paint.isCyclic();
    }
    public PaintContext createContext(ColorModel cm,
            Rectangle deviceBounds,
            Rectangle2D userBounds,
            AffineTransform xform,
            RenderingHints hints){
        return paint.createContext(cm, deviceBounds, userBounds, xform, hints);
    }

    public int getTransparency(){
        return paint.getTransparency();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        paint = new GradientPaint(x1, y1, color1, x2, y2, color2, cyclic);
    }
}

