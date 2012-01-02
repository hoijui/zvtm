package fr.inria.zvtm.cluster;

import java.awt.Color;
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

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.PRectangle;

/**
 * Most of these Deltas are (faster) replacements for common operations that
 * used to be performed using the AutoReplay mechanism.
 */
aspect GlyphReplication {

    // Serializes the Paint of a PRectangle if it serializable 
    // or if it is an instance of LinearGradientPaint (or a 
    // "well-known" paint), otherwise ignores it.
    pointcut pRectPaint(PRectangle prect, Paint paint):
        execution(public void PRectangle.setPaint(Paint))
        && this(prect)
        && args(paint)
        && if(VirtualSpaceManager.INSTANCE.isMaster());
        
    after(PRectangle prect, Paint paint) returning:
        pRectPaint(prect, paint) &&
        !cflowbelow(pRectPaint(PRectangle, Paint)) &&
        if(prect.isReplicated()){
            Delta delta = makePaintDelta(prect.getObjId(), paint);
            if(delta != null){
                VirtualSpaceManager.INSTANCE.sendDelta(delta);
            }
        }

    pointcut dPathEdit(DPath dPath, Point2D.Double[] coords, boolean absolute): 
        execution(public void DPath.edit(Point2D.Double[], boolean))
		&& this(dPath)
        && args(coords, absolute)
		&& if(VirtualSpaceManager.INSTANCE.isMaster());

    after(DPath dPath, Point2D.Double[] coords, boolean absolute) returning:
	   dPathEdit(dPath, coords, absolute) && 
       !cflowbelow(dPathEdit(DPath, Point2D.Double[], boolean)) &&
       if(dPath.isReplicated()){
		   Delta delta = new DPathEditDelta(dPath.getObjId(),
                   coords, absolute);
		   VirtualSpaceManager.INSTANCE.sendDelta(delta);
	   }

    pointcut glyphMove(Glyph glyph):
        (execution(public void Glyph.moveTo(double, double)) ||
         execution(public void Glyph.move(double, double)))
		&& this(glyph)
		&& if(VirtualSpaceManager.INSTANCE.isMaster());

    after(Glyph glyph) returning:
        glyphMove(glyph) && 
        !cflowbelow(glyphMove(Glyph)) &&
        if(glyph.isReplicated()){
            Delta delta = new GlyphMoveDelta(glyph.getObjId(),
                    glyph.getLocation().x, glyph.getLocation().y);
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
	   }

    private static class PaintDelta implements Delta {
        private final ObjId<PRectangle> targetId;
        private final Paint paint;

        PaintDelta(ObjId<PRectangle> targetId, Paint paint){
            if(! (paint instanceof Serializable)){
                throw new IllegalArgumentException("Expected serializable Paint");
            }
            this.targetId = targetId;
            this.paint = paint;
        }

        public void apply(SlaveUpdater updater){
            PRectangle target = updater.getSlaveObject(targetId);
            target.setPaint(paint);
        }
    }

    // a serializable LinearGradientPaint
    private static class LinGradPaint implements Paint, Serializable {
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

    static final Paint wrapPaint(Paint orig){
        if(orig instanceof Serializable){
            return orig;
        } else if(orig instanceof LinearGradientPaint){
            return new LinGradPaint((LinearGradientPaint)orig);
        } else {
            return null;
        }
    }

    private static final PaintDelta makePaintDelta(ObjId<PRectangle> targetId, Paint paint){
        Paint wrapped = wrapPaint(paint);
        if(wrapped == null){
            return null;
        }
        return new PaintDelta(targetId, wrapped);
    }

    private static class DPathEditDelta implements Delta {
        private final ObjId<DPath> targetId;
        private final Point2D.Double[] coords;
        private final boolean absolute;

        DPathEditDelta(ObjId<DPath> targetId, Point2D.Double[] coords, boolean absolute){
            this.targetId = targetId;
            this.coords = coords;
            this.absolute = absolute;
        }

        public void apply(SlaveUpdater updater){
            DPath target = updater.getSlaveObject(targetId);
            target.edit(coords, absolute);
        }
    }

    private static class GlyphMoveDelta implements Delta {
        private final ObjId<Glyph> targetId;
        private final double x;
        private final double y;

        GlyphMoveDelta(ObjId<Glyph> targetId, double x, double y){
            this.targetId = targetId;
            this.x = x;
            this.y = y;
        }

        public void apply(SlaveUpdater updater){
            Glyph target = updater.getSlaveObject(targetId);
            target.moveTo(x, y);
        }
    }
}

