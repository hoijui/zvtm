package fr.inria.zvtm.clustering;

import java.util.HashMap;
import java.util.Map;

import org.jgroups.JChannel;
import org.jgroups.Message;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;

/**
 * A VirtualSpace that can be replicated over a cluster
 */
public aspect MasterVirtualSpace {
	//misc.
	declare parents : LongPoint implements java.io.Serializable;

	//augment Glyph with an ObjId identifier (serializable)
	private ObjId Glyph.id = ObjIdFactory.next();
	private ObjId Glyph.getObjId(){ return id; }

	//master communication channel, indexed by
	//virtual space name (XXX find better solution?)
	private Map<String, JChannel> channels = 
		new HashMap<String, JChannel>(); 

	/**
	 * Gets a channel to the VirtualSpace cluster,
	 * creating it if it does not exist yet
	 */
	private JChannel retrieveChannel(String vsName) throws Exception{
		JChannel retval = channels.get(vsName);
		if(null == retval){
			retval = new JChannel();
			retval.connect(vsName);
			channels.put(vsName, retval);
		}
		return retval;
	}

	//augment Glyph with a getCreateDelta method
	//that should be overriden by all glyph classes
	//(default produces a nopDelta)
	//!!!! check signification of private for aspects (we WANT overrides)!!!!
	private Delta Glyph.getCreateDelta(){
		return new NopDelta();
	}	

	@Override private Delta VRectangle.getCreateDelta(){
		return new RectangleCreateDelta(getObjId(), 
				getLocation(), 
				getZindex(),
				getWidth(), getHeight(), 
				getColor());
	}

	//multicast group, named after the virtual space 
	
	//define pointcuts to catch Glyphs additions 
	//and removals to/from a VirtualSpace
	//make sure this join point fires only once even if addGlyph
	//overloads are chained together (!within VirtualSpaceManager... -- is that the right way of doing it?)
	pointcut glyphAdd(Glyph glyph, VirtualSpace virtualSpace): 
		call(* VirtualSpace.addGlyph(..)) 
		&& args(glyph, ..)
		&& target(virtualSpace)
		&&!within(VirtualSpace)
		&&!within(Delta+); //Delta manages updates to slaves

	pointcut glyphRemove(Glyph glyph, VirtualSpace virtualSpace): 
		call(public * VirtualSpace.removeGlyph(..)) 
		&& args(glyph, ..)
		&& target(virtualSpace)
		&&!within(VirtualSpace)
		&&!within(Delta+); //Delta manages updates to slaves

	//!within(Glyph+) to avoid calling multiple times when chained overloads(is it the right approach?)
	pointcut glyphPosChange(Glyph glyph):
		(call(public * Glyph+.moveTo(..)) || call(public * Glyph+.move(..)))
		&& target(glyph)
		&&!within(Glyph+)
		&&!within(Delta+); //Delta manages updates to slaves


	after(Glyph glyph, VirtualSpace virtualSpace) returning: 
		glyphAdd(glyph, virtualSpace) {

			System.out.println("glyph add: send add message");
			Delta delta = glyph.getCreateDelta();
			Message msg = new Message(null, null, delta);
			try{
				retrieveChannel(virtualSpace.getName()).send(msg);
			} catch(Exception e){
				System.out.println("Could not retrieve comm channel");
			}
		}

	after(Glyph glyph, VirtualSpace virtualSpace) returning: 
		glyphRemove(glyph, virtualSpace){

		System.out.println("glyph remove: send remove message");
		Delta delta = new GlyphRemoveDelta(glyph.getObjId());
		Message msg = new Message(null, null, delta);
		try{
			retrieveChannel(virtualSpace.getName()).send(msg);
		} catch(Exception e){
			System.out.println("Could not retrieve comm channel");
		}
	}

	after(Glyph glyph) returning: glyphPosChange(glyph) {
		LongPoint loc = glyph.getLocation();
		System.out.println("Glyph moved, destination (" +
		loc.x + "," + loc.y + ")");	

		Delta delta = new GlyphPosDelta(glyph.getObjId(),
				loc.x, loc.y);
	}


}

