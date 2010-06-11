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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.SwingUtilities;

//Network-related imports
import org.jgroups.ChannelException;
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
		} catch( ChannelException ce ){
			logger.error("Could not join network channel: " + ce);
		}
	}

	void stop(){
		networkDelegate.stop();
        appDelegate.stop();
	}

	void createLocalView(ClusteredView cv){
		appDelegate.createLocalView(cv);
	}

	void setCameraLocation(Location masterLoc,
			Camera slaveCamera){
		appDelegate.setCameraLocation(masterLoc, slaveCamera);
	}

    void setBackgroundColor(ClusteredView cv, Color bgColor){
        appDelegate.setBackgroundColor(cv, bgColor);
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
		void startOperation() throws ChannelException {
			channel = new JChannel();
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

