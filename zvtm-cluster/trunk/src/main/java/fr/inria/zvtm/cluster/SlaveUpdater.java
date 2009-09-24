package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpace;

import java.util.Map;
import java.util.HashMap;

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
	private final VirtualSpace virtualSpace;
	//examples of slave objects are glyphs and cameras
	private final Map<ObjId, Object> slaveObjects =
		new HashMap<ObjId, Object>();

	public SlaveUpdater(VirtualSpace virtualSpace){
		this.virtualSpace = virtualSpace;
	}

	/**
	 * Returns the object mapped to 'id', or 
	 * null if not present.
	 * @return the value to which this SlaveUpdater maps the specified key, 
	 * or null if the SlaveUpdater contains no mapping for this key.
	 */
	Object getSlaveObject(ObjId id){
		return slaveObjects.get(id);
	}

	/**
	 * Associates 'id' with 'object' in this SlaveUpdater.
	 * If a mapping for this key was present, the old value 
	 * is replaced.
	 * @return previous value associated with specified key, 
	 * or null  if there was no mapping for key.
	 */
	Object putSlaveObject(ObjId id, Object object){
		return slaveObjects.put(id, object);
	}

	/**
	 * Removes the mapping for 'id' if it exists.
	 */
	Object removeSlaveObject(ObjId id){
		return slaveObjects.remove(id);
	}

	class NetworkDelegate {
		private final JChannel channel;
		private final String spaceName;
		NetworkDelegate(String spaceName) throws ChannelException {
			this.spaceName = spaceName;
			channel = new JChannel();
		}

		//start listening on the appropriate channel,
		//handle incoming messages (optionnally post reply
		//or error messages)
		void startOperation() throws ChannelException {
			channel.connect(spaceName);
			channel.setReceiver(new ReceiverAdapter(){
				@Override public void viewAccepted(View newView){
					logger.info("new view: {}", newView);
				}

				@Override public void receive(Message msg){
					if(!(msg.getObject() instanceof Delta)){
						logger.warn("wrong message type (Delta expected)");
						return;
					}
					Delta delta = (Delta)msg.getObject();

					//Do whatever needs to be done to update the
					//state of the slave VirtualSpace (e.g. move a 
					//Camera, create a rectangle, ...)
					//In other words, "apply the message"
					delta.apply(SlaveUpdater.this); 
				}
			});
		}
	}
}

