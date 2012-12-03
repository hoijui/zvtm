/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;

/**
 * Replicate interesting events on virtual spaces
 */
aspect VirtualSpaceReplication {
    pointcut cameraAdd(VirtualSpace virtualSpace):
        execution(public Camera VirtualSpace.addCamera())
        && this(virtualSpace)
        && if(VirtualSpaceManager.INSTANCE.isMaster());

    after(VirtualSpace vs) returning(Camera cam):
       cameraAdd(vs) && !cflowbelow(cameraAdd(VirtualSpace)){
           cam.setReplicated(true);
           Delta delta = new CameraCreateDelta(vs.getObjId(),
                   cam.getObjId());
           VirtualSpaceManager.INSTANCE.sendDelta(delta);
       }

    pointcut cameraMoved(Camera camera) :
        this(camera) &&
        if(VirtualSpaceManager.INSTANCE.isMaster()) &&
        (
         execution(public void Camera.altitudeOffset(double)) ||
         execution(public void Camera.move(double, double)) ||
         execution(public void Camera.move(double, double)) ||
         execution(public void Camera.moveTo(double, double)) ||
         execution(public void Camera.setAltitude(double)) ||
         execution(public void Camera.setAltitude(double, boolean)) ||
         execution(public void Camera.setLocation(Location))
        )
        ;

    after(Camera camera) :
        cameraMoved(camera) {
        //XXX stop-gap measure: disabled !cflowbelow to allow
        //camera sticking
        //&& !cflowbelow(cameraMoved(Camera)){
            Delta delta = new CameraLocationDelta(camera.getObjId(),
                    camera.getLocation());
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }


    private static class CameraCreateDelta implements Delta{
        private final ObjId<VirtualSpace> vsId;
        private final ObjId<Camera> camId;

        CameraCreateDelta(ObjId<VirtualSpace> vsId, ObjId<Camera> camId){
            this.vsId = vsId;
            this.camId = camId;
        }

        public void apply(SlaveUpdater updater){
            VirtualSpace vs = updater.getSlaveObject(vsId);
            Camera cam = vs.addCamera();
            updater.putSlaveObject(camId, cam);
        }

        @Override public String toString(){
            return "CameraCreateDelta";
        }
    }

    private static class CameraLocationDelta implements Delta{
        private final ObjId<Camera> camId;
        private final Location newLoc;

        CameraLocationDelta(ObjId<Camera> camId, Location newLoc){
            this.camId = camId;
            this.newLoc = newLoc;
        }

        public void apply(SlaveUpdater updater){
            Camera cam = updater.getSlaveObject(camId);
            updater.setCameraLocation(newLoc, cam);
        }

        @Override public String toString(){
            return "CameraLocationDelta";
        }
    }

    pointcut glyphAbove(VirtualSpace virtualSpace, Glyph g1, Glyph g2):
        execution(public void VirtualSpace.above(Glyph, Glyph))
        && this(virtualSpace)
        && args(g1, g2)
        && if(VirtualSpaceManager.INSTANCE.isMaster());

    after(VirtualSpace vs, Glyph g1, Glyph g2) returning:
       glyphAbove(vs, g1, g2) &&
       !cflowbelow(glyphAbove(VirtualSpace, Glyph, Glyph)) &&
       if(g1.isReplicated()){
           Delta delta = new GlyphAboveDelta(vs.getObjId(),
                   g1.getObjId(), g2.getObjId());
           VirtualSpaceManager.INSTANCE.sendDelta(delta);
       }

    pointcut glyphBelow(VirtualSpace virtualSpace, Glyph g1, Glyph g2):
        execution(public void VirtualSpace.below(Glyph, Glyph))
        && this(virtualSpace)
        && args(g1, g2)
        && if(VirtualSpaceManager.INSTANCE.isMaster());

    after(VirtualSpace vs, Glyph g1, Glyph g2) returning:
       glyphBelow(vs, g1, g2) &&
       !cflowbelow(glyphBelow(VirtualSpace, Glyph, Glyph)) &&
       if(g1.isReplicated()){
           Delta delta = new GlyphBelowDelta(vs.getObjId(),
                   g1.getObjId(), g2.getObjId());
           VirtualSpaceManager.INSTANCE.sendDelta(delta);
       }

    private abstract static class ZIndexDelta implements Delta {
        protected final ObjId<VirtualSpace> space;
        protected final ObjId<Glyph> g1;
        protected final ObjId<Glyph> g2;

        ZIndexDelta(ObjId<VirtualSpace> space, ObjId<Glyph> g1, ObjId<Glyph> g2){
            this.space = space;
            this.g1 = g1;
            this.g2 = g2;
        }

        public abstract void apply(SlaveUpdater updater);
    }

    private static class GlyphAboveDelta extends ZIndexDelta {
        GlyphAboveDelta(ObjId<VirtualSpace> space, ObjId<Glyph> g1, ObjId<Glyph> g2){
            super(space,g1,g2);
        }

        @Override public void apply(SlaveUpdater updater){
            VirtualSpace vs = updater.getSlaveObject(space);
            Glyph gl1 = updater.getSlaveObject(g1);
            Glyph gl2 = updater.getSlaveObject(g2);
            vs.above(gl1, gl2);
        }
    }

    private static class GlyphBelowDelta extends ZIndexDelta {
        GlyphBelowDelta(ObjId<VirtualSpace> space, ObjId<Glyph> g1, ObjId<Glyph> g2){
            super(space,g1,g2);
        }

        @Override public void apply(SlaveUpdater updater){
            VirtualSpace vs = updater.getSlaveObject(space);
            Glyph gl1 = updater.getSlaveObject(g1);
            Glyph gl2 = updater.getSlaveObject(g2);
            vs.below(gl1, gl2);
        }
    }

    pointcut glyphOnTop(VirtualSpace virtualSpace, Glyph glyph):
        execution(public void VirtualSpace.onTop(Glyph))
        && this(virtualSpace)
        && args(glyph)
        && if(VirtualSpaceManager.INSTANCE.isMaster());

    after(VirtualSpace vs, Glyph glyph) returning:
       glyphOnTop(vs, glyph) &&
       !cflowbelow(glyphOnTop(VirtualSpace, Glyph)) &&
       if(glyph.isReplicated()){
           Delta delta = new GlyphOnTopDelta(vs.getObjId(), glyph.getObjId());
           VirtualSpaceManager.INSTANCE.sendDelta(delta);
       }

    pointcut glyphOnTopIndex(VirtualSpace virtualSpace, Glyph glyph, int index):
        execution(public void VirtualSpace.onTop(Glyph, int))
        && this(virtualSpace)
        && args(glyph, index)
        && if(VirtualSpaceManager.INSTANCE.isMaster());

    after(VirtualSpace vs, Glyph glyph, int index) returning:
       glyphOnTopIndex(vs, glyph, index) &&
       !cflowbelow(glyphOnTopIndex(VirtualSpace, Glyph, int)) &&
       if(glyph.isReplicated()){
           Delta delta = new GlyphOnTopDelta(vs.getObjId(), glyph.getObjId(), index);
           VirtualSpaceManager.INSTANCE.sendDelta(delta);
       }

    private static class GlyphOnTopDelta implements Delta {
        static final int NO_INDEX = -1;
        private final ObjId<VirtualSpace> space;
        private final ObjId<Glyph> glyph;
        private final int index;

        GlyphOnTopDelta(ObjId<VirtualSpace> space, ObjId<Glyph> glyph,
                int index){
            this.space = space;
            this.glyph = glyph;
            this.index = index;
        }

        GlyphOnTopDelta(ObjId<VirtualSpace> space, ObjId<Glyph> glyph){
            this(space, glyph, NO_INDEX);
        }

        public void apply(SlaveUpdater updater){
            VirtualSpace vs = updater.getSlaveObject(space);
            Glyph g = updater.getSlaveObject(glyph);
            if(index == NO_INDEX){
                vs.onTop(g);
            } else {
                vs.onTop(g, index);
            }
        }
    }

    pointcut glyphAtBottom(VirtualSpace virtualSpace, Glyph glyph):
        execution(public void VirtualSpace.atBottom(Glyph))
        && this(virtualSpace)
        && args(glyph)
        && if(VirtualSpaceManager.INSTANCE.isMaster());

    after(VirtualSpace vs, Glyph glyph) returning:
       glyphAtBottom(vs, glyph) &&
       !cflowbelow(glyphAtBottom(VirtualSpace, Glyph)) &&
       if(glyph.isReplicated()){
           Delta delta = new GlyphAtBottomDelta(vs.getObjId(), glyph.getObjId());
           VirtualSpaceManager.INSTANCE.sendDelta(delta);
       }

    pointcut glyphAtBottomIndex(VirtualSpace virtualSpace, Glyph glyph, int index):
        execution(public void VirtualSpace.atBottom(Glyph, int))
        && this(virtualSpace)
        && args(glyph, index)
        && if(VirtualSpaceManager.INSTANCE.isMaster());

    after(VirtualSpace vs, Glyph glyph, int index) returning:
       glyphAtBottomIndex(vs, glyph, index) &&
       !cflowbelow(glyphAtBottomIndex(VirtualSpace, Glyph, int)) &&
       if(glyph.isReplicated()){
           Delta delta = new GlyphAtBottomDelta(vs.getObjId(), glyph.getObjId(), index);
           VirtualSpaceManager.INSTANCE.sendDelta(delta);
       }

    private static class GlyphAtBottomDelta implements Delta {
        static final int NO_INDEX = -1;
        private final ObjId<VirtualSpace> space;
        private final ObjId<Glyph> glyph;
        private final int index;

        GlyphAtBottomDelta(ObjId<VirtualSpace> space, ObjId<Glyph> glyph,
                int index){
            this.space = space;
            this.glyph = glyph;
            this.index = index;
        }

        GlyphAtBottomDelta(ObjId<VirtualSpace> space, ObjId<Glyph> glyph){
            this(space, glyph, NO_INDEX);
        }

        public void apply(SlaveUpdater updater){
            VirtualSpace vs = updater.getSlaveObject(space);
            Glyph g = updater.getSlaveObject(glyph);
            if(index == NO_INDEX){
                vs.atBottom(g);
            } else {
                vs.atBottom(g, index);
            }
        }
    }
}

