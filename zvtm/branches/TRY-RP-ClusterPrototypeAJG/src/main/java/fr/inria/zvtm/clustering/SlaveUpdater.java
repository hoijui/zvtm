/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.clustering;

import java.util.HashMap;
import java.util.Map;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import fr.inria.zvtm.clustering.ObjId;
import fr.inria.zvtm.clustering.ObjIdFactory;
import fr.inria.zvtm.engine.Camera;
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
	private final JChannel channel = new JChannel(new java.io.File("chan_conf.xml"));
	
	//data structure for easy retrieval of a Glyph 
	//given its ObjId: either a BStree or a hash table
	private final Map<ObjId, Glyph> glyphMap = new HashMap<ObjId, Glyph>();

	//if we have to do that for more than glyphs and cameras,
	//consider refactoring
	private final Map<ObjId, Camera> cameraMap = 
		new HashMap<ObjId, Camera>();

	//retrieve a glyph according to its (cluster) id
	//overloading Glyph.equals is probably more elegant (and would
	//allow us to use a set instead of a map) but I am not comfortable
	//with doing that from AspectJ yet.
	Glyph getGlyphById(ObjId id){
		return glyphMap.get(id);
	}

	//XXX if multithreaded context, should be atomic (?)
	void addGlyph(ObjId id, Glyph glyph){
		//XXX ObjId consistency (glyph.getObjId().equals(id) ???)
		//XXX solve by yet another weaving?
		virtualSpace.addGlyph(glyph);
		glyphMap.put(id, glyph);
	}

	//XXX if multithreaded context, should be atomic (?)
	void removeGlyph(ObjId id){
		Glyph glyph = glyphMap.get(id);
		if(null == glyph){
			System.out.println("Attempting to remove a non-existent Glyph.");
			return;
		}
		virtualSpace.removeGlyph(glyph);
		glyphMap.remove(id);
	}

	Camera getCameraById(ObjId id){
		return cameraMap.get(id);
	}

	void addCamera(ObjId id){
		Camera cam = VirtualSpaceManager.INSTANCE.addCamera(virtualSpace);
		cameraMap.put(id, cam);
	}
	
	void removeCamera(ObjId id){
		Camera cam = cameraMap.get(id);
		if(null == cam){
			System.out.println("Attempting to remove a non-exitent Camera");
			return;
		}
		virtualSpace.removeCamera(cam.getIndex());
		cameraMap.remove(id);
	}

	CameraGroup getCameraGroup(){
		return virtualSpace.getCameraGroup();
	}

	void removeAllGlyphs(){
		virtualSpace.removeAllGlyphs();
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
				if(!(msg.getObject() instanceof Delta)){
					System.out.println("wrong message type (Delta expected)");
					return;
				}
				Delta delta = (Delta)msg.getObject();
				
				//Do whatever needs to be done to update the
				//state of the slave VirtualSpace (e.g. move a 
				//Camera, create a rectangle, ...)
				//In other words, "apply the message"
				delta.apply(SlaveUpdater.this); 

				//...
				VirtualSpaceManager.INSTANCE.repaintNow();
			}
		});
	}
}

