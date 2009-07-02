package fr.inria.zvtm.clustering;

import java.util.HashMap;
import java.util.Map;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import fr.inria.zvtm.clustering.ObjId;
import fr.inria.zvtm.clustering.ObjIdFactory;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;

/**
 * A class that applies incoming messages
 * to a VirtualSpace in order to keep it in sync
 * with a master reference.
 */
public class SlaveUpdater {
	private final VirtualSpace virtualSpace;
	private final JChannel channel = new JChannel();
	
	//data structure for easy retrieval of a Glyph 
	//given its ObjId: either a BStree or a hash table
	private final Map<ObjId, Glyph> idMap = new HashMap<ObjId, Glyph>();
	
	//retrieve a glyph according to its (cluster) id
	//overloading Glyph.equals is probably more elegant (and would
	//allow us to use a set instead of a map) but I am not comfortable
	//with doing that from AspectJ yet.
	Glyph getGlyphById(ObjId id){
		return idMap.get(id);
	}

	//XXX if multithreaded context, should be atomic (?)
	void addGlyph(ObjId id, Glyph glyph){
		//XXX ObjId consistency (glyph.getObjId().equals(id) ???)
		//XXX solve by yet another weaving?
		VirtualSpaceManager.INSTANCE.addGlyph(glyph, virtualSpace);
		idMap.put(id, glyph);
	}

	//XXX if multithreaded context, should be atomic (?)
	void removeGlyph(ObjId id){
		Glyph glyph = getGlyphById(id);
		if(null == glyph){
			System.out.println("Oops. tried to remove a non-existent Glyph. Something fishy is happening");
			return;
		}
		virtualSpace.removeGlyph(glyph);
		idMap.remove(id);
	}

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

				if(!(msg.getObject() instanceof Delta)){
					System.out.println("wrong message type (Delta expected)");
					return;
				}

				Delta delta = (Delta)msg.getObject();
				delta.apply(SlaveUpdater.this); 
			}
		});
	}
}

