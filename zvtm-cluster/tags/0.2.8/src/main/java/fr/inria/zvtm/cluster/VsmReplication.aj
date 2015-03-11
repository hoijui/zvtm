/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.engine.portals.CameraPortal;


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

    public void VirtualSpaceManager.addClusteredView(ClusteredView cv){
        sendDelta(new ClusteredViewCreateDelta(cv));

        //make sure slave receive camera positions once
        for(Camera cam: cv.peekCameras()){
            cam.setLocation(cam.getLocation());
        }
    }

    public void VirtualSpaceManager.destroyClusteredView(ClusteredView cv){
        sendDelta(new ClusteredViewDestroyDelta(cv));
    }
    

   private static class CameraPortalReplicator implements PortalReplicator {
        protected final int x,y,w,h;
        protected float a;
        private final ObjId<Camera> camId;

        CameraPortalReplicator(CameraPortal source){
            this.x = source.x;
            this.y = source.y;
            this.w = source.w;
            this.h = source.h;
            this.a = source.getTranslucencyValue();
            this.camId = source.getCamera().getObjId();
        }

        public Portal createPortal(SlaveUpdater updater) {
            Camera overviewCam = updater.getSlaveObject(this.camId);
            Portal p = new CameraPortal(x, y, w, h, overviewCam,a);
            updater.setPortalLocation(p,x, y, w, h);
            return p;
        }
    }


   /** Add a portal to a clustered view. Only CameraPortal are supported for now.
    *@param p Portal to be added
    *@param v owning View
    */
   
    public void VirtualSpaceManager.addClusteredPortal(Portal p, ClusteredView v) {
        if (!(p instanceof CameraPortal)) {
            System.out.println("Only CameraPortal are supported");
        }
        else {
            CameraPortalReplicator replicator = new CameraPortalReplicator((CameraPortal)p);
            VirtualSpaceManager.INSTANCE.sendDelta(new PortalCreateDelta(replicator,p.getObjId()));    
        }
    }



    private static class PortalCreateDelta implements Delta{
        private final PortalReplicator replicator;
        private final ObjId<Portal> portalId;

        PortalCreateDelta(PortalReplicator replicator, ObjId<Portal> portalId){
            this.replicator = replicator;
            this.portalId = portalId;
        }

        public void apply(SlaveUpdater updater){
            //Create Portal Camera
            Portal p = replicator.createPortal(updater);
            
            VirtualSpaceManager.INSTANCE.addPortal(p, updater.getLocalView());
           

            updater.putSlaveObject(portalId, p);
        }

        @Override public String toString(){
            return "PortalCreateDelta";
        }
    }


    pointcut portalMoved(Portal portal) :
    this(portal) &&
    if(VirtualSpaceManager.INSTANCE.isMaster()) &&
    (
     execution(public void Portal.sizeTo(int, int)) ||
     execution(public void Portal.resize(int, int)) ||
     execution(public void Portal.move(int, int)) ||
     execution(public void Portal.moveTo(int, int)) 
    )
    ;

    after(Portal portal) :
        portalMoved(portal) {
            Delta delta = new PortalLocationDelta(portal.getObjId(), 
                portal.x, 
                portal.y, 
                portal.w, 
                portal.h);
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class PortalLocationDelta implements Delta{
        private final ObjId<Portal> portalId;
        private final int x,y,w,h;


        PortalLocationDelta(ObjId<Portal> portalId, int x, int y, int w, int h){
            this.portalId = portalId;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public void apply(SlaveUpdater updater){
            Portal p = updater.getSlaveObject(portalId);
            updater.setPortalLocation(p,x, y, w, h);
        }

        @Override public String toString(){
            return "PortalLocationDelta";
        }
    }    

    pointcut portalsetVisible(Portal portal) :
    this(portal) &&
    if(VirtualSpaceManager.INSTANCE.isMaster()) &&
    (
     execution(public void Portal.setVisible(boolean))
    )
    ;

    after(Portal portal) :
        portalsetVisible(portal) {
            Delta delta = new PortalVisibleDelta(portal.getObjId(), 
                portal.isVisible());
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class PortalVisibleDelta implements Delta{
        private final ObjId<Portal> portalId;
        private final boolean isVisible;

        PortalVisibleDelta(ObjId<Portal> portalId, boolean isVisible){
            this.portalId = portalId;
            this.isVisible = isVisible;
        }

        public void apply(SlaveUpdater updater){
            Portal p = updater.getSlaveObject(portalId);
            updater.setPortalVisible(p,isVisible);
        }

        @Override public String toString(){
            return "PortalVisibleDelta";
        }
    }   

} 

