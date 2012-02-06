package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.io.Serializable;

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

    pointcut setStroke(Glyph glyph, Stroke stroke):
        execution(public void Glyph.setStroke(Stroke))
        && this(glyph)
        && args(stroke)
        && if(VirtualSpaceManager.INSTANCE.isMaster());

    after(Glyph glyph, Stroke stroke) returning:
        setStroke(glyph, stroke) &&
        !cflowbelow(setStroke(Glyph, Stroke)) &&
        if(glyph.isReplicated()){
            Delta delta = makeStrokeDelta(glyph.getObjId(), stroke);
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

    /**
     * Returns a PaintDelta given a target Glyph and a Paint if
     * the Paint is or can be made Serializable, <code>null</code>
     * otherwise.
     */
    private static final PaintDelta makePaintDelta(ObjId<PRectangle> targetId, Paint paint){
        Paint wrapped = Paints.wrapPaint(paint);
        if(wrapped == null){
            return null;
        }
        return new PaintDelta(targetId, wrapped);
    }

    private static class StrokeDelta implements Delta {
        private final ObjId<Glyph> targetId;
        private final Stroke stroke;

        StrokeDelta(ObjId<Glyph> targetId, Stroke stroke){
            if(! (stroke instanceof Serializable)){
                throw new IllegalArgumentException("Expected serializable Stroke");
            }
            this.targetId = targetId;
            this.stroke = stroke;
        }

        public void apply(SlaveUpdater updater){
            Glyph target = updater.getSlaveObject(targetId);
            target.setStroke(stroke);
        }
    }

    /**
     * Returns a StrokeDelta given a target Glyph and a Stroke if
     * the Stroke is or can be made Serializable, <code>null</code>
     * otherwise.
     */
    private static final StrokeDelta makeStrokeDelta(ObjId<Glyph> targetId, Stroke stroke){
        Stroke wrapped = Strokes.wrapStroke(stroke);
        if(wrapped == null){
            return null;
        }
        return new StrokeDelta(targetId, wrapped);
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

