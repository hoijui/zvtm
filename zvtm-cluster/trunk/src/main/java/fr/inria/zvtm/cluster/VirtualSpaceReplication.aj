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
		   Delta delta = new CameraCreateDelta(vs.getObjId(),
				   cam.getObjId());
		   VirtualSpaceManager.INSTANCE.sendDelta(delta);
	   }

	pointcut cameraMoved(Camera camera) :
		this(camera) &&
		if(VirtualSpaceManager.INSTANCE.isMaster()) &&
		(
		 execution(public void Camera.altitudeOffset(float)) ||
		 execution(public void Camera.move(long, long)) ||
		 execution(public void Camera.move(double, double)) ||
		 execution(public void Camera.moveTo(long, long)) ||
		 execution(public void Camera.setAltitude(float)) ||
		 execution(public void Camera.setAltitude(float, boolean)) ||
		 execution(public void Camera.setLocation(Location)) 
		)
		;

	after(Camera camera) :
		cameraMoved(camera) &&
		!cflowbelow(cameraMoved(Camera)){
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
	   glyphAbove(vs, g1, g2) && !cflowbelow(glyphAbove(VirtualSpace, Glyph, Glyph)){
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
	   glyphBelow(vs, g1, g2) && !cflowbelow(glyphBelow(VirtualSpace, Glyph, Glyph)){
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
}

