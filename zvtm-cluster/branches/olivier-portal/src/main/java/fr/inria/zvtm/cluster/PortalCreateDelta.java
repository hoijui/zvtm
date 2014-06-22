package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.portals.MorOverviewPortal;

import fr.inria.zvtm.engine.Camera;

class PortalCreateDelta implements Delta {
    private final ObjId<MorOverviewPortal> objId = ObjIdFactory.next();
    private final ObjId<ClusteredView> cvObjId;
    // Portal
    private final int x, y, w, h;
    // CameraPortal
    private final ObjId<Camera> camRef;
    // OverviewPortal
    private final ObjId<Camera>[] camORRefs;

    PortalCreateDelta(MorOverviewPortal p, ClusteredView cv){
        cvObjId = cv.getObjId();

	x = p.x; y = p.y; w = p.w; h = p.h;
	camRef = p.getCamera().getObjId();
	camORRefs = makeCamRefs(p.getObservedRegionCameras());
    }

    private static final ObjId<Camera>[] makeCamRefs(Camera[] cameras)
    {
	    ObjId<Camera>[] retval = new ObjId[cameras.length];
	    for (int i = 0; i < cameras.length; i++)
	    {
		    retval[i] = cameras[i].getObjId();
            }
            return retval;
        }

    private final Camera[] refsToCameras(SlaveUpdater su)
    {
            Camera[] retval = new Camera[camORRefs.length];
	    for (int i = 0; i < camORRefs.length; i++)
	    {
		    retval[i] = su.getSlaveObject(camORRefs[i]);
            }
            return retval;
    }

    public void apply(SlaveUpdater updater)
    {
	    MorOverviewPortal portal = new MorOverviewPortal(
		    x,y,w,h, (Camera)updater.getSlaveObject(camRef), (Camera[])refsToCameras(updater), null);
	    updater.putSlaveObject(objId, portal);
	    ClusteredView cv = updater.getSlaveObject(cvObjId);
	    if(cv != null){
		    updater.addPortal(portal, cv);
	    } else {
		    System.err.println(
			    "warning [PortalCreateDelta]: trying to access a non-existent  ClusterView");
	    }
    }

    @Override public String toString(){
	    return "PortalCreateDelta, portal id: " + objId;
    }
}

