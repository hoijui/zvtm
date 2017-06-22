/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.engine.portals.CameraPortal;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.VirtualSpaceManager;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.SwingUtilities;

//Network-related imports
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SlaveUpdater maintains a set of objects that may be identified
 * by their ObjectId, and applies Deltas to those objects.
 * Examples of such objects are Glyphs and Cameras.
 */
public class SlaveUpdater {
    protected final Logger logger =
        LoggerFactory.getLogger(SlaveUpdater.class);
    private final int slaveNumber;
    private final String appId;
    //examples of slave objects are glyphs and cameras
    private final Map<ObjId, Object> slaveObjects =
        new HashMap<ObjId, Object>();
    private final NetworkDelegate networkDelegate;
    //'SlaveApp' may be replaced by an interface containing
    //the essential operations. For now it's overkill.
    private SlaveApp appDelegate = null;

    private SyncView syncView = null;

    /**
     * Creates a new Slave updater.
     * SlaveUpdater maintains the state of a whole application,
     * not just a VirtualSpace
     */
    public SlaveUpdater(String appId, int slaveNumber){
        this.appId = appId;
        this.slaveNumber = slaveNumber;
        networkDelegate = new NetworkDelegate(appId);
    }

    public SlaveUpdater(){
        this("clusterApp", 0);
    }

    void setAppDelegate(SlaveApp appDelegate){
        this.appDelegate = appDelegate;
    }

    void setSyncView(SyncView syncView){
        this.syncView = syncView;
    }

    /**
     * Returns the object mapped to 'id', or
     * null if not present.
     * @return the value to which this SlaveUpdater maps the specified key,
     * or null if the SlaveUpdater contains no mapping for this key.
     */
    public <T> T getSlaveObject(ObjId<T> id){
        return (T)(slaveObjects.get(id));
    }

    /**
     * Returns an array containing the matching slave object for each
     * reference in the given array 'idarray'.
     * References to non-existing objects (ObjId instances that
     * do not have a mapping) translate to null values in the
     * returned array.
     */
    public <T> ArrayList<T> getSlaveObjectArrayList(final List<ObjId<T>> idList){
        ArrayList<T> retval = new ArrayList<T>();
        for(ObjId<T> id: idList){
            retval.add(getSlaveObject(id));
        }
        return retval;
    }

    /**
     * Associates 'id' with 'object' in this SlaveUpdater.
     * If a mapping for this key was present, the old value
     * is replaced.
     * @return previous value associated with specified key,
     * or null  if there was no mapping for key.
     */
    public <T> T putSlaveObject(ObjId<T> id, T object){
        return (T)slaveObjects.put(id, object);
    }

    /**
     * Removes the mapping for 'id' if it exists.
     */
    public Object removeSlaveObject(ObjId id){
        return slaveObjects.remove(id);
    }


    void startOperation(){
        try{
            networkDelegate.startOperation();
        } catch( Exception ce ){
            logger.error("Could not join network channel: " + ce);
        }
    }

    void stop(){
        networkDelegate.stop();
        appDelegate.stop();
    }

    void destroyLocalView(ClusteredView cv){
        appDelegate.destroyLocalView(cv);
    }

    void createLocalView(ClusteredView cv){
        appDelegate.createLocalView(cv);

        if (appDelegate.getViewType()==ClusteredViewPanelFactory.CLUSTER_VIEW) {
            ClusteredViewPanel clusterViewPanel = (ClusteredViewPanel)appDelegate.getView().getPanel();
            setSyncView(clusterViewPanel);
            clusterViewPanel.setSlaveUpdater(this);
        }        
    }

    public fr.inria.zvtm.engine.View getLocalView() {
        return appDelegate.getView();
    }

    public boolean ownsBlock(ClusteredView cv) {
        return appDelegate.ownsBlock(cv);
    }
    
    // not useful for now
    // void setOverlayCameras(Vector<Camera> cams, ClusteredView cv){
    //     appDelegate.setOverlayCameras(cams, cv);
    //  }
    // void removeOverlayCameras(ClusteredView cv){
    //     appDelegate.removeOverlayCameras(cv);
    // }

    void setCameraLocation(Location masterLoc,
            Camera slaveCamera){
        appDelegate.setCameraLocation(masterLoc, slaveCamera);
    }

    void setOverviewPortalObservedViewLocationAndSize(OverviewPortal slavePortal){
        appDelegate.setOverviewPortalObservedViewLocationAndSize(slavePortal);
    }

    void setPortalLocationAndSize(Portal slavePortal, int x, int y, int w, int h){
        appDelegate.setPortalLocationAndSize(slavePortal, x, y, w, h);
    }

    void setPortalLocation(Portal slavePortal, int x, int y){
        appDelegate.setPortalLocation(slavePortal, x, y);
    }
    void setPortalLocation(Portal slavePortal, int x, int y, int w, int h){
        appDelegate.setPortalLocation(slavePortal, x, y, w, h);
    }
    void setPortalSize(Portal slavePortal, int w, int h){
        appDelegate.setPortalSize(slavePortal, w, h);
    }

    void portalCameraUpdate(Portal slavePortal, double vx, double vy, double alt)
    {
        CameraPortal cp = (CameraPortal)slavePortal;
        //cp.getCamera().setLocation(new Location(vx, vy, alt));
        for(Camera cam : cp.getCameras()) {
            cam.setLocation(new Location(vx, vy, alt));
        }
    }

    void setBackgroundColor(ClusteredView cv, Color bgColor){
        appDelegate.setBackgroundColor(cv, bgColor);
    }



    public void drawAndAck() {
        if (syncView!=null) syncView.drawAndAck();
    }

    public void paintAndAck() {
        if (syncView!=null) syncView.paintAndAck();
    }   

    //Send the delta immediatly and wait for the answer
    void sendAckSync() throws Exception {
        Message msg = new Message(null, null, null);
        msg.setFlag(Message.RSVP);
        msg.setFlag(Message.DONT_BUNDLE);
        networkDelegate.channel.send(msg);
    }    

    class NetworkDelegate {
        private JChannel channel;
        private final String appName;
        NetworkDelegate(String appName){
            this.appName = appName;
        }

        //start listening on the appropriate channel,
        //handle incoming messages (optionnally post reply
        //or error messages)
        void startOperation() throws Exception {
            channel = ChannelFactory.makeChannel();
            channel.connect(appName);
            channel.setReceiver(new ReceiverAdapter(){
                @Override public void viewAccepted(View newView){
                    logger.info("new view: {}", newView);
                }

                @Override public void receive(Message msg){
                    if(!(msg.getObject() instanceof Delta)){
                        logger.warn("wrong message type (Delta expected)");
                        return;
                    }
                    final Delta delta = (Delta)msg.getObject();

                    SwingUtilities.invokeLater(new Runnable(){
                        public void run(){
                            //Do whatever needs to be done to update the
                            //state of the slave VirtualSpace (e.g. move a
                            //Camera, create a rectangle, ...)
                            //In other words, "apply the message"

                            delta.apply(SlaveUpdater.this);
                        }
                    });
                }
            });
        }




        void stop(){
            channel.close();
        }
    }
}

