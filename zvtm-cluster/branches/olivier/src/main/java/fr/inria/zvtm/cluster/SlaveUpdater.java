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
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.engine.portals.CameraPortal;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.SyncPaintImmediately;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.SwingUtilities;

//Network-related imports
//CE import org.jgroups.ChannelException;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.stack.IpAddress;
import org.jgroups.Address;
import org.jgroups.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.BasicConfigurator;

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
    private boolean doSync = false;
    private long paintCount = 0;
    private SlaveSyncPaintImmediately sspi = null;

    /**
     * Creates a new Slave updater.
     * SlaveUpdater maintains the state of a whole application,
     * not just a VirtualSpace
     */
    public SlaveUpdater(String appId, int slaveNumber){
        BasicConfigurator.configure();
        this.appId = appId;
        this.slaveNumber = slaveNumber;
        networkDelegate = new NetworkDelegate(appId);
        sspi = new SlaveSyncPaintImmediately();
    }

    public SlaveUpdater(){
        this("clusterApp", 0);
    }

    void setAppDelegate(SlaveApp appDelegate){
        this.appDelegate = appDelegate;
        //appDelegate.getView().setSyncPaintImmediately(sspi);
    }

    void setSyncPaintImmediately(){
        appDelegate.getView().setSyncPaintImmediately(sspi);
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
        } catch( Exception ce) { //CE ChannelException ce ){
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
    }

    public fr.inria.zvtm.engine.View getLocalView() {
        return appDelegate.getView();
    }

    public boolean ownsBlock(ClusteredView cv) {
        return appDelegate.ownsBlock(cv);
    }

    void setCameraLocation(Location masterLoc,
            Camera slaveCamera){
        appDelegate.setCameraLocation(masterLoc, slaveCamera);
    }

    void setOverlayCamera(Camera c, ClusteredView cv){
        appDelegate.setOverlayCamera(c, cv);
    }
    void destroyOverlayCamera(ClusteredView cv){
        appDelegate.destroyOverlayCamera(cv);
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

    void setPortalSize(Portal slavePortal, int w, int h){
        appDelegate.setPortalSize(slavePortal, w, h);
    }

    void portalCameraUpdate(Portal slavePortal, double vx, double vy, double alt)
    {
        CameraPortal cp = (CameraPortal)slavePortal;
        for(Camera cam : cp.getCameras()) {
            cam.setLocation(new Location(vx, vy, alt));
        }
    }

    void setBackgroundColor(ClusteredView cv, Color bgColor){
        appDelegate.setBackgroundColor(cv, bgColor);
    }

    // ----------------------------------------------
    // sync stuff

    private long lastRepainCountScheduled = -1;
    private long lastRepaintCountPainted = -1;
    private long lastPaintTime = 0;

    void setSyncronous(ClusteredView cv, boolean b){
        if (!ownsBlock(cv)) { return; }
        System.out.println("slave setSyncronous "+b);
        if (!b){
            if (doSync){
                sendMessage("Bye");
                appDelegate.getView().setPaintLocked(false);
                appDelegate.paintImmediately();
            }
            doSync = false;
            lastRepainCountScheduled = -1;
            return;
        }
        doSync = true;
        sendMessage("Hello");
    }

    void setPaintLock(boolean b){
        appDelegate.getView().setPaintLocked(b);
    }

    class SlaveSyncPaintImmediately implements SyncPaintImmediately {
        public void SlaveSyncPaintImmediately() {}
        public void paint(){
            if (doSync) {
                Long lpc = appDelegate.getView().getRepaintCount();
                if (lastRepainCountScheduled != -1){
                    // this happen of course if we do not lock below
                    //System.out.println("!!! WILL PAINT IN THE FUTUR !!! " + lpc + " " + lastRepainCountScheduled);
                    // return;
                }
                appDelegate.getView().setPaintLocked(true);
                lastRepainCountScheduled = lpc;
                networkDelegate.sendMsg(new Long(lastRepainCountScheduled));
            }
            else{
                appDelegate.paintImmediately();
                long ct = System.currentTimeMillis();
                //System.out.println("PaintDelay (US): "+ SlaveUpdater.this +" "+(ct-lastPaintTime));
                lastPaintTime = ct;
            }
        }
    }

    public void paintImmediately(long maxRepaintCount){
        if (lastRepainCountScheduled == -1){
            // FIXME: several clustered views
            //System.out.println("Sync paint not usful msg... " + this);
            return;
        }
        if (maxRepaintCount == 0) {
            paintImmediately();
            return;
        }
        boolean late = false;
        if (lastRepaintCountPainted == maxRepaintCount){
            // already painted ... might happen            
            //System.out.println("ALREADY PAINTED");
            lastRepainCountScheduled = -1; 
            appDelegate.getView().setPaintLocked(false);
            return;
        }
        else if (lastRepaintCountPainted > maxRepaintCount){
            // should not happen with the lock
            //System.out.println("SHOULD NOT HAPPEN WITH LOCK");
            appDelegate.getView().setPaintLocked(false);
            return;
        }
        else if (maxRepaintCount > lastRepainCountScheduled){
            // we are late
            late = true;
            //System.out.println("WE ARE LATE me:" + lastRepainCountScheduled +", max: "+ maxRepaintCount+" me now: "+appDelegate.getView().getRepaintCount() +" "+this);
        }
        else if (maxRepaintCount == lastRepainCountScheduled){
            // good
            //System.out.println("WE ARE GOOD " + this);
        }
        else {
            // what ? MUST NOT HAPPEN
            System.out.println("WHAT ????");
        }
        appDelegate.paintImmediately();
        long ct = System.currentTimeMillis();
        //System.out.println("PaintDelay (Sync): "+this+" "+(ct-lastPaintTime));
        lastPaintTime = ct;
        lastRepaintCountPainted = lastRepainCountScheduled;
        lastRepainCountScheduled = -1; 
        appDelegate.getView().setPaintLocked(false);
        if (late){
            // do something... not clear that this is a good idea
            //do not do that at least ... sspi.paint();
        }
    }

    public void paintImmediately(){
        appDelegate.paintImmediately();
        long ct = System.currentTimeMillis();
        //System.out.println("DIRECT / PaintDelay: "+this+" "+(ct-lastPaintTime));
        lastPaintTime = ct;
        appDelegate.getView().setPaintLocked(false);
    }

    // ----------------------------------------------
    // network

    public void sendMessage(String str){
         networkDelegate.sendMsg(str);
    }

    class NetworkDelegate {
        private JChannel channel;
        private final String appName;
        Address masterAddress = null;

        NetworkDelegate(String appName){
            this.appName = appName;
        }

        void sendMsg(String str) {
            if (masterAddress == null){
                //System.err.println("slave updater do not have masterAddress");
                return;
            }
            try {
                Message msg=new Message(masterAddress, null, str);
                channel.send(msg);
            } catch(Exception e) {
                System.err.println("slave updater fail to send msg");
                logger.error("Could not send msg: " + e);
            }
        }

        void sendMsg(Long l) {
            if (masterAddress == null){ 
                // System.err.println("slave updater do not have masterAddress");
                return;
            }
            try {
                Message msg=new Message(masterAddress, null, l);
                // do bundle for sync ....
                //msg.setFlag(Message.DONT_BUNDLE);
                channel.send(msg);
            } catch(Exception e) {
                System.err.println("slave updater fail to send msg");
                logger.error("Could not send msg: " + e);
            }
        }

        //start listening on the appropriate channel,
        //handle incoming messages (optionnally post reply
        //or error messages)
        void startOperation() throws Exception { //CE throws ChannelException {
            channel = ChannelFactory.makeChannel();
            //CE channel.setOpt(Channel.LOCAL, Boolean.FALSE);
            channel.setDiscardOwnMessages(true);
            channel.connect(appName);
            
            // Address ad =    channel.getAddress();
            // System.out.println("Slave address: "+ ad.toString());

            channel.setReceiver(new ReceiverAdapter(){
                @Override public void viewAccepted(View newView){
                     System.err.println("slave new view: {}"+ newView);
                    logger.info("slave new view: {}", newView);
                }

                @Override public void receive(Message msg){
                    if(!(msg.getObject() instanceof Delta)){
                        logger.warn("wrong message type (Delta expected)");
                        System.err.println("wrong message type (Delta expected)");
                        return;
                    }

                    if (masterAddress == null){
                        masterAddress = (msg.getSrc());
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

