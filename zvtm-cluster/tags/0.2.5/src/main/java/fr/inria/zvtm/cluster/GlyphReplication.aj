package fr.inria.zvtm.cluster;

import java.awt.geom.Point2D;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.Glyph;

/**
 * Most of these Deltas are (faster) replacements for common operations that
 * used to be performed using the AutoReplay mechanism.
 */
aspect GlyphReplication {

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

