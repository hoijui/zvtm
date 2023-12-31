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
import fr.inria.zvtm.engine.portals.DraggableCameraPortal;
import fr.inria.zvtm.engine.portals.RoundCameraPortal;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.engine.Location;
import java.util.ArrayList;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.Color;

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


    //Sync cluster

    public interface AckReceiver {
        public void ackReceive ();
    }

    declare parents : VirtualSpaceManager implements AckReceiver;  

    private long VirtualSpaceManager.minDrawTime = 20000000L; //50fps
    private Timer VirtualSpaceManager.drawTimer;
    private long VirtualSpaceManager.lastDrawTime = 0;
    private boolean VirtualSpaceManager.drawAck = true;


    public void VirtualSpaceManager.setRefreshRate(int rr){
        minDrawTime = rr * 1000000L; // milli -> nano
    }

    public int VirtualSpaceManager.getRefreshRate(){
        return (int)(minDrawTime / 1000000L);
    }


    public void VirtualSpaceManager.ackReceive() {
        if (drawTimer!=null) drawTimer.stop();
        long remainingTime = 0;

        if (drawAck) {
            long currentTime = System.nanoTime();
            long ellapsedTime = currentTime - lastDrawTime; 

            if (ellapsedTime < minDrawTime)
                remainingTime = (minDrawTime-ellapsedTime) / 1000000; //in ms
        }
        
        System.out.println("remainingTime: " + remainingTime + "ms");

        drawTimer = new Timer(5000, taskPerformer);
        drawTimer.setInitialDelay((int)remainingTime);
        drawTimer.setRepeats(false);
        drawTimer.start();       

    }

    private ActionListener VirtualSpaceManager.taskPerformer = new ActionListener(){
        public void actionPerformed(ActionEvent evt){
            drawTimer.stop();
            if (drawAck) {
                drawAck = false;
                //Send Paint Delta
                System.out.println("Send Paint Delta");
                Delta paintDelta = new PaintCreateDelta();
                sendDeltaImmediatly(paintDelta); 
            }
            else {
                drawAck = true;
                //Send Draw Delta
                System.out.println("Send Draw Delta");
                lastDrawTime = System.nanoTime();
                Delta drawDelta = new DrawCreateDelta();
                sendDeltaImmediatly(drawDelta); 
            }
        }
    };

    public void VirtualSpaceManager.startPaintDeltaTimer()
    {
        System.out.println("startPaintDeltaTimer");

        drawTimer = new Timer(5000, taskPerformer);
        drawTimer.setRepeats(false);
        drawTimer.start();
    }


    public static class PaintCreateDelta implements Delta{
        public PaintCreateDelta(){}

        public void apply(SlaveUpdater updater){
            //Send draw call
            updater.paintAndAck();
        }

        @Override public String toString(){
            return "PaintCreateDelta";
        }
    }


    public static class DrawCreateDelta implements Delta{
        public DrawCreateDelta(){}

        public void apply(SlaveUpdater updater){
            //Send draw call
            updater.drawAndAck();
        }

        @Override public String toString(){
            return "DrawCreateDelta";
        }
    }    

   

   /** Add a portal to a clustered view. Only subclasses of CameraPortal (e.g.,
    * DraggableCameraPortal, RoundCameraPortal, OverviewPortal) are supported.
    *@param p Portal to be added
    *@param v owning View
    */
    public void VirtualSpaceManager.addClusteredPortal(Portal p, ClusteredView v) {
        if (!(p instanceof CameraPortal)) {
            System.out.println("Only subclasses of CameraPortal are supported");
        }
        else {
            Delta createDelta = new  PortalCreateDelta(p.getReplicator(),  p.getObjId());
            VirtualSpaceManager.INSTANCE.sendDelta(createDelta);
            p.setReplicated(true);  
        }
    }

    private static class CameraPortalReplicator implements PortalReplicator {
        protected final int x,y,w,h;
        protected float a;
        protected final ArrayList<ObjId<Camera>> camIds;

        CameraPortalReplicator(CameraPortal source){
            this.x = source.x;
            this.y = source.y;
            this.w = source.w;
            this.h = source.h;
            this.a = source.getTranslucencyValue();
            camIds = new ArrayList<ObjId<Camera>>(source.getCameras().length);
            for (Camera camera : source.getCameras()) {
            	this.camIds.add((ObjId<Camera>)camera.getObjId());
    		}
        }

        public Portal createPortal(SlaveUpdater updater) {
            Camera []cams = new Camera[this.camIds.size()];
            int i=0;
            for (ObjId<Camera> camId : this.camIds) {
            	cams[i++] = updater.getSlaveObject(camId);
            }
            Portal p = new CameraPortal(x, y, w, h, cams,a);
            updater.setPortalLocation(p,x, y, w, h);
            return p;
        }
    }

    private static class DraggableCameraPortalReplicator extends CameraPortalReplicator {

        DraggableCameraPortalReplicator(DraggableCameraPortal source){
           super(source);
        }

        public Portal createPortal(SlaveUpdater updater) {
            Camera []cams = new Camera[this.camIds.size()];
            int i=0;
            for (ObjId<Camera> camId : this.camIds) {
            	cams[i++] = updater.getSlaveObject(camId);
            }
            Portal p = new DraggableCameraPortal(x, y, w, h, cams);
            updater.setPortalLocation(p,x, y, w, h);
            return p;
        }
    }

    private static class RoundCameraPortalReplicator extends CameraPortalReplicator {

        RoundCameraPortalReplicator(RoundCameraPortal source){
           super(source);
        }

        public Portal createPortal(SlaveUpdater updater) {
            Camera cam = updater.getSlaveObject(this.camIds.get(0));
            Portal p = new RoundCameraPortal(x, y, w, h, cam);
            updater.setPortalLocation(p,x, y, w, h);
            return p;
        }
    }

    private static class OverviewPortalReplicator extends CameraPortalReplicator {
        protected final ObjId<Camera> obscamId;

        OverviewPortalReplicator(OverviewPortal source){
           super(source);
           obscamId = source.getObservedRegionCamera().getObjId();
        }

        public Portal createPortal(SlaveUpdater updater) {
            Camera cam = updater.getSlaveObject(this.camIds.get(0));
            Camera obsCam = updater.getSlaveObject(this.obscamId);
            Portal p = new OverviewPortal(x, y, w, h, cam, obsCam);
            updater.setPortalLocation(p,x, y, w, h);
            return p;
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
            VirtualSpaceManager.INSTANCE.destroyPortal(p);
            updater.removeSlaveObject(portalId); 
        }

        @Override public String toString(){
            return "PortalDestroyDelta";
        }
    }
} 

