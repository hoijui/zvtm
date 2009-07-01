package fr.inria.zvtm.clustering;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import fr.inria.zvtm.engine.VirtualSpace;

/**
 * A class that applies incoming messages
 * to a VirtualSpace in order to keep it in sync
 * with a master reference.
 */
public class SlaveUpdater {
	private final VirtualSpace virtualSpace;
	private final JChannel channel = new JChannel();

	public SlaveUpdater(VirtualSpace vs) throws Exception {
		this.virtualSpace = vs;
		
		//use slave virtual space name to search for master group
		channel.connect(vs.getName());
		channel.setReceiver(new ReceiverAdapter(){
			@Override public void viewAccepted(View newView){
				System.out.println("** view: " + newView);
			}

			@Override public void receive(Message msg){
				System.out.println("received message: " + msg.getObject());
			}
		});
	}
}

