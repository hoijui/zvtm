/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import java.util.ArrayList;
import java.util.Vector;
import java.awt.Color;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.engine.portals.CameraPortal;
import fr.inria.zvtm.engine.portals.DraggableCameraPortal;
import fr.inria.zvtm.engine.portals.RoundCameraPortal;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.engine.Location;

/**
 * Replicates 'interesting' calls made on the Virtual Space Manager
 * (virtual space creation, destruction, global vsm operations)
 */
aspect VsmReplication {

    //advise virtual space creation and destruction
    after(VirtualSpaceManager vsm) returning(VirtualSpace vs):
        target(vsm) && execution(VirtualSpace addVirtualSpace(String))
        && if(VirtualSpaceManager.INSTANCE.isMaster()) {
            vs.setReplicated(true);
            vsm.sendDelta(
                    new VsCreateDelta(vs.getName(), vs.getObjId())
                    );
        }

    //Note that VirtualSpaceManager being a singleton, we do
    //not need to transport an object reference to it.

    private static class VsCreateDelta implements Delta {
        private final String spaceName;
        private final ObjId spaceId;

        VsCreateDelta(String spaceName, ObjId spaceId){
            this.spaceName = spaceName;
            this.spaceId = spaceId;
        }

        public void apply(SlaveUpdater su){
            VirtualSpace vs =
                VirtualSpaceManager.INSTANCE.addVirtualSpace(spaceName);
            su.putSlaveObject(spaceId, vs);
        }

        @Override public String toString(){
            return String.format(
                    "VsCreateDelta, virtualSpace name: %s id: %s",
                    spaceName,
                    spaceId
                    );
        }
    }

    private static class VsDestroyDelta implements Delta {
        private final String spaceName;
        private final ObjId spaceId;

        VsDestroyDelta(String spaceName, ObjId spaceId){
            this.spaceName = spaceName;
            this.spaceId = spaceId;
        }

        public void apply(SlaveUpdater su){
            su.removeSlaveObject(spaceId);
            VirtualSpaceManager.INSTANCE.destroyVirtualSpace(spaceName);
        }

        @Override public String toString(){
            return String.format(
                    "VsDestroyDelta, virtualSpace name: %s id: %s",
                    spaceName,
                    spaceId
                    );
        }
    }

    ClusteredView[] VirtualSpaceManager.allClusteredViews = new ClusteredView[0];

    private void VirtualSpaceManager._addClusteredView(ClusteredView v){
        ClusteredView[] tmpA = new ClusteredView[allClusteredViews.length+1];
        System.arraycopy(allClusteredViews, 0, tmpA, 0, allClusteredViews.length);
        tmpA[allClusteredViews.length] = v;
        allClusteredViews = tmpA;
    }

    private void VirtualSpaceManager._destroyClusteredView(int i){
        ClusteredView[] tmpA = new ClusteredView[allClusteredViews.length-1];
        if (tmpA.length > 0){
            System.arraycopy(allClusteredViews, 0, tmpA, 0, i);
            System.arraycopy(allClusteredViews, i+1, tmpA, i, allClusteredViews.length-i-1);
        }
        allClusteredViews = tmpA;
    }
    
    private void VirtualSpaceManager._destroyClusteredView(ClusteredView v){
        for (int i=0;i<allClusteredViews.length;i++){
            if (allClusteredViews[i] == v){
                _destroyClusteredView(i);
                break;
            }
        }
    }

    public void VirtualSpaceManager.addClusteredView(ClusteredView cv){
        sendDelta(new ClusteredViewCreateDelta(cv));
        _addClusteredView(cv);
        //make sure slave receive camera positions once
        for(Camera cam: cv.peekCameras()){
            cam.setLocation(cam.getLocation());
        }
    }

    public void VirtualSpaceManager.destroyClusteredView(ClusteredView cv){
        sendDelta(new ClusteredViewDestroyDelta(cv));
        _destroyClusteredView(cv);
    }
    
    /** */
    public void VirtualSpaceManager.setClusteredOverlayCamera(Camera cov, ClusteredView cv){
        sendDelta(new SetClusteredOverlayDelta(cov.getObjId(), cv.getObjId()));
    }

    private static class SetClusteredOverlayDelta implements Delta {
        private final ObjId<Camera> camId;
        private final ObjId<ClusteredView> cvId;

        SetClusteredOverlayDelta(ObjId<Camera> camId, ObjId<ClusteredView> cvId){
            this.camId = camId;
            this.cvId = cvId;
        }

        public void apply(SlaveUpdater su){
            Camera c = su.getSlaveObject(this.camId);
            ClusteredView cv = su.getSlaveObject(this.cvId);
            su.setOverlayCamera(c, cv);
        }

        @Override public String toString(){
            return String.format("SetClusteredOverlayDelta");
        }
    }

    public void VirtualSpaceManager.destroyClusteredOverlayCamera(ClusteredView cv){
        sendDelta(new DestroyClusteredOverlayDelta(cv.getObjId()));
    }

    private static class DestroyClusteredOverlayDelta implements Delta {
        private final ObjId<ClusteredView> cvId;

        DestroyClusteredOverlayDelta(ObjId<ClusteredView> cvId){
             this.cvId = cvId;
        }

        public void apply(SlaveUpdater su){
            ClusteredView cv = su.getSlaveObject(this.cvId);
            su.destroyOverlayCamera(cv);
        }

        @Override public String toString(){
            return String.format("DestroyClusteredOverlayDelta");
        }
    }

   /** Add a portal to a clustered view. Only subclasses of CameraPortal (e.g.,
    * DraggableCameraPortal, RoundCameraPortal, OverviewPortal) are supported.
    *@param p Portal to be added
    *@param v owning View
    */
    public void VirtualSpaceManager.addClusteredPortal(Portal p, ClusteredView cv) {
        if (!(p instanceof CameraPortal)) {
            System.out.println("Only subclasses of CameraPortal are supported");
        }
        else {
            Delta createDelta = new  PortalCreateDelta(p.getReplicator(),  p.getObjId(), cv.getObjId());
            VirtualSpaceManager.INSTANCE.sendDelta(createDelta);
            p.setReplicated(true);  
        }
    }

    private static class CameraPortalReplicator implements PortalReplicator {
        protected final int x,y,w,h;
        protected float a;
        //protected final ObjId<Camera> camId;
        protected final ArrayList<ObjId<Camera>> camIds;

        CameraPortalReplicator(CameraPortal source){
            this.x = source.x;
            this.y = source.y;
            this.w = source.w;
            this.h = source.h;
            this.a = source.getTranslucencyValue();
            //this.camId = source.getCamera().getObjId();
            this.camIds =  makeCamRefs(source.getCameras());
        }

        public Portal createPortal(SlaveUpdater updater) {
            //Camera cam = updater.getSlaveObject(this.camId);
            //System.out.println("Create Portal !!!");
            Portal p = new CameraPortal(x, y, w, h, refsToCameras(updater, camIds), a);
            updater.setPortalLocationAndSize(p, x, y, w, h);
            return p;
        }
    }

    private static final ArrayList<ObjId<Camera>>
        makeCamRefs(Vector<Camera> cameras){
            ArrayList<ObjId<Camera>> retval = new ArrayList<ObjId<Camera>>();
            for(Camera cam: cameras){
                retval.add(cam.getObjId());
            }
            return retval;
        }

    private static final Vector<Camera>
        refsToCameras(SlaveUpdater su, ArrayList<ObjId<Camera>> camRefs){
            Vector<Camera> retval = new Vector<Camera>();
            for(ObjId<Camera> camRef: camRefs){
                retval.add(su.getSlaveObject(camRef));
            }
            return retval;
        }

    private static class DraggableCameraPortalReplicator extends CameraPortalReplicator {

        DraggableCameraPortalReplicator(DraggableCameraPortal source){
           super(source);
        }

        public Portal createPortal(SlaveUpdater updater) {
            //Camera cam = updater.getSlaveObject(this.camId);
            Portal p = new DraggableCameraPortal(x, y, w, h, refsToCameras(updater, camIds));
            updater.setPortalLocationAndSize(p,x, y, w, h);
            return p;
        }
    }

    private static class RoundCameraPortalReplicator extends CameraPortalReplicator {

        RoundCameraPortalReplicator(RoundCameraPortal source){
           super(source);
        }

        public Portal createPortal(SlaveUpdater updater) {
            //Camera cam = updater.getSlaveObject(this.camId);
            Portal p = new RoundCameraPortal(x, y, w, h, refsToCameras(updater, camIds));
            updater.setPortalLocationAndSize(p,x, y, w, h);
            return p;
        }
    }

    private static class OverviewPortalReplicator extends CameraPortalReplicator {
        //protected final ObjId<Camera> obscamId;
        protected final ArrayList<ObjId<Camera>> obsCamIds;

        OverviewPortalReplicator(OverviewPortal source){
           super(source);
           //obscamId = source.getObservedRegionCamera().getObjId();
           obsCamIds = makeCamRefs(source.getObservedRegionCameras());
        }

        public Portal createPortal(SlaveUpdater updater) {
            //Camera cam = updater.getSlaveObject(this.camId);
            //OverviewPortal p = new OverviewPortal(x, y, w, h, refsToCameras(updater, camIds), obsCam);
            OverviewPortal p = new OverviewPortal(x, y, w, h, refsToCameras(updater, camIds), refsToCameras(updater, obsCamIds));
            updater.setOverviewPortalObservedViewLocationAndSize(p);
            updater.setPortalLocationAndSize(p,x, y, w, h);
            return (Portal)p;
        }
    }

    public PortalReplicator Portal.getReplicator(){
        return null;
    }

    public PortalReplicator CameraPortal.getReplicator(){
        return new CameraPortalReplicator(this);
    }

    @Override public PortalReplicator DraggableCameraPortal.getReplicator(){
        return new DraggableCameraPortalReplicator(this);
    }

    @Override public PortalReplicator RoundCameraPortal.getReplicator(){
        return new RoundCameraPortalReplicator(this);
    }

    @Override public PortalReplicator OverviewPortal.getReplicator(){
        return new OverviewPortalReplicator(this);
    }

    private static class PortalCreateDelta implements Delta{
        private final PortalReplicator replicator;
        private final ObjId<Portal> portalId;
        private final ObjId<ClusteredView> cvId;

        PortalCreateDelta(PortalReplicator replicator, ObjId<Portal> portalId, ObjId<ClusteredView> cvId){
            this.replicator = replicator;
            this.portalId = portalId;
            this.cvId = cvId;
        }

        public void apply(SlaveUpdater updater){
            //Create Portal Camera
            ClusteredView cv = updater.getSlaveObject(this.cvId);
            if (updater.ownsBlock(cv)){ 
                Portal p = replicator.createPortal(updater);
                VirtualSpaceManager.INSTANCE.addPortal(p, updater.getLocalView());
                updater.putSlaveObject(portalId, p);
            }
        }

        @Override public String toString(){
            return "PortalCreateDelta";
        }
    }

    // portal Moved
    pointcut portalMoved(Portal portal) :
    this(portal) &&
    if(VirtualSpaceManager.INSTANCE.isMaster()) &&
    (
     execution(public void Portal.move(int, int)) ||
     execution(public void Portal.moveTo(int, int)) 
    )
    ;
    after(Portal portal) :
        portalMoved(portal) {
            Delta delta = new PortalMovedDelta(portal.getObjId(), 
                portal.x, 
                portal.y
                );
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class PortalMovedDelta implements Delta{
        private final ObjId<Portal> portalId;
        private final int x,y;

        PortalMovedDelta(ObjId<Portal> portalId, int x, int y){
            this.portalId = portalId;
            this.x = x;
            this.y = y;
        }

        public void apply(SlaveUpdater updater){
            Portal p = updater.getSlaveObject(portalId);
            if (p != null){
                updater.setPortalLocation(p,x, y);
            }
        }

        @Override public String toString(){
            return "PortalLocationDelta";
        }
    }

    // portal Resized
    pointcut portalResized(Portal portal) :
    this(portal) &&
    if(VirtualSpaceManager.INSTANCE.isMaster()) &&
    (
     execution(public void Portal.sizeTo(int, int)) ||
     execution(public void Portal.resize(int, int))
    )
    ;
    after(Portal portal) :
        portalResized(portal) {
            Delta delta = new PortalResizedDelta(portal.getObjId(), 
                portal.w, 
                portal.h);
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class PortalResizedDelta implements Delta{
        private final ObjId<Portal> portalId;
        private final int w,h;

        PortalResizedDelta(ObjId<Portal> portalId, int w, int h){
            this.portalId = portalId;
            this.w = w;
            this.h = h;
        }

        public void apply(SlaveUpdater updater){
            Portal p = updater.getSlaveObject(portalId);
            if (p != null){
                updater.setPortalSize(p, w, h);
            }
        }

        @Override public String toString(){
            return "PortalResizedDelta";
        }
    }
    // see AutoReplay.aj for setVisible, setColor, setBackgroundColor, etc.

    /** Remove a portal to a clustered view.
    * @param p Portal to be removed
    */
    public void VirtualSpaceManager.destroyClusteredPortal(Portal p) {
        Delta destroyDelta = new  PortalDestroyDelta(p.getObjId());
        VirtualSpaceManager.INSTANCE.sendDelta(destroyDelta);
        p.setReplicated(false);
    }

    private static class PortalDestroyDelta implements Delta{
        private final ObjId<Portal> portalId;

        PortalDestroyDelta(ObjId<Portal> portalId){
            this.portalId = portalId;
        }

        public void apply(SlaveUpdater updater){
            Portal p = updater.getSlaveObject(portalId);
            if (p != null){
                VirtualSpaceManager.INSTANCE.destroyPortal(p);
                updater.removeSlaveObject(portalId);
            } 
        }

        @Override public String toString(){
            return "PortalDestroyDelta";
        }
    }

    /** Put a portal in front of all portals
    * @param p the portal
    */
    public void VirtualSpaceManager.stackClusteredPortalFront(Portal p) {
        Delta frontDelta = new  PortalFrontDelta(p.getObjId());
        VirtualSpaceManager.INSTANCE.sendDelta(frontDelta);
    }

    private static class PortalFrontDelta implements Delta{
        private final ObjId<Portal> portalId;

        PortalFrontDelta(ObjId<Portal> portalId){
            this.portalId = portalId;
        }

        public void apply(SlaveUpdater updater){
            Portal p = updater.getSlaveObject(portalId);
            if (p != null){
                VirtualSpaceManager.INSTANCE.stackPortalFront(p);
            }
        }

        @Override public String toString(){
            return "PortalFrontDelta";
        }
    }

    /** Put a portal at the back of all portals
    * @param p the portal
    */
    public void VirtualSpaceManager.stackClusteredPortalBack(Portal p) {
        Delta frontDelta = new  PortalFrontDelta(p.getObjId());
        VirtualSpaceManager.INSTANCE.sendDelta(frontDelta);
    }

    private static class PortalBackDelta implements Delta{
        private final ObjId<Portal> portalId;

        PortalBackDelta(ObjId<Portal> portalId){
            this.portalId = portalId;
        }

        public void apply(SlaveUpdater updater){
            Portal p = updater.getSlaveObject(portalId);
            if (p!=null){
                VirtualSpaceManager.INSTANCE.stackPortalBack(p); 
            }
        }

        @Override public String toString(){
            return "PortalFrontDelta";
        }
    }
} 

