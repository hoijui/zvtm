package fr.inria.zvtm.clustering;

import java.awt.Color;

import java.util.HashMap;
import java.util.Map;

import org.jgroups.JChannel;
import org.jgroups.Message;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;

/**
 * Intercepts VirtualSpace state changes and propagates
 * them to slaves. 
 */
public aspect VirtualSpaceReplication {
	//misc.
	declare parents : LongPoint implements java.io.Serializable;
	declare parents : Location implements java.io.Serializable;
	
	//master communication channel, indexed by
	//virtual space name (XXX find better solution?)
	private Map<String, JChannel> channels = 
		new HashMap<String, JChannel>(); 

	//XXX move Glyph and Camera to implement a new interface "VSObject"
	//(would allow to query ObjID, retrieve parent space
	//if existing...)
	
	//augment Glyph with an ObjId identifier (serializable)
	private ObjId Glyph.id = ObjIdFactory.next();
	private ObjId Glyph.getObjId(){ return id; }

	//augment Camera with an ObjId identifier (serializable)
	private ObjId Camera.id = ObjIdFactory.next();
	private ObjId Camera.getObjId(){ return id; }

	//augment VirtualSpace with a CameraGroup
	private final CameraGroup VirtualSpace.cameraGroup = 
		new CameraGroup(this);

	/**
	 * Gets a channel to the VirtualSpace cluster,
	 * creating it if it does not exist yet
	 */
	private JChannel retrieveChannel(String vsName) throws Exception{
		JChannel retval = channels.get(vsName);
		if(null == retval){
			retval = new JChannel(new java.io.File("chan_conf.xml"));
			retval.connect(vsName);
			channels.put(vsName, retval);
		}
		return retval;
	}

	public CameraGroup VirtualSpace.getCameraGroup(){
		return cameraGroup;
	}

	/* Section: Glyph-related delta creation methods */

	//augment Glyph with a getCreateDelta method
	//that should be overriden by all glyph classes
	//(default produces a nopDelta)
	private Delta Glyph.getCreateDelta(){
		return new NopDelta();
	}	

	//note the private @Override; no contradiction here because
	//private means "private within the aspect"
	@Override private Delta VRectangle.getCreateDelta(){
		return new RectangleCreateDelta(getObjId(), 
				getLocation(), 
				getZindex(),
				getWidth(), getHeight(), 
				getColor());
	}

	/* Section: Glyph-related pointcuts */

	//define pointcuts to catch Glyphs additions 
	//and removals to/from a VirtualSpace
	//make sure this join point fires only once even if addGlyph
	//overloads are chained together (!within VirtualSpaceManager... -- is that the right way of doing it?)
	pointcut glyphAdd(Glyph glyph, VirtualSpace virtualSpace): 
		call(public * VirtualSpace.addGlyph(..)) 
		&& args(glyph, ..)
		&& target(virtualSpace)
		&& !within(VirtualSpace)
		&& !within(SlaveUpdater);

	pointcut glyphRemove(Glyph glyph, VirtualSpace virtualSpace): 
		call(public * VirtualSpace.removeGlyph(..)) 
		&& args(glyph, ..)
		&& target(virtualSpace)
		&& !within(VirtualSpace)
		&& !within(SlaveUpdater);

	//!within(Glyph+) to avoid calling multiple times when chained overloads(is it the right approach?)
	pointcut glyphPosChange(Glyph glyph):
		(call(public * Glyph+.moveTo(..)) || call(public * Glyph+.move(..)))
		&& target(glyph)
		&& !within(Glyph+)
		&& !within(Delta+); //Delta manages updates to slaves

	pointcut glyphColorChange(Glyph glyph):
		(call(public * Glyph+.setColor(..)) || call(public * Glyph+.setHSVColor(..)))
		 && target(glyph)
		 && !within(Glyph+)
		 && !within(Delta+);
	 
	/* Section: Camera-related pointcuts */
//	pointcut cameraAdd(Camera camera):
//		call(public * VirtualSpaceManager.addCamera(..))
//		&& args(camera, ..)
//		&& !within(VirtualSpaceManager);
//
//	pointcut cameraRemove(Camera camera):
//		call(public * VirtualSpace.removeCamera(..))
//		&& args(camera, ..)
//		&& !within(VirtualSpace);
//
//	/* not yet in use */
//	pointcut cameraPosChange(Camera camera):
//		(call(public * Camera.moveTo(..)) 
//		  || call(public * Camera.move(..))
//		  || call (public * Camera.setAltitude(..))
//		  || call (public * Camera.setLocation(..)))
//		&& target(camera)
//		&& !within(Camera)
//		&& !within(Delta+);

	/* Section: CameraGroup-related pointcuts */
	pointcut groupPosChange(CameraGroup cameraGroup):
		call(public * CameraGroup.setLocation(..))
		&& target(cameraGroup)
		&& !within(CameraGroup)
		&& !within(Delta+);

	/* Section: Glyph-related advice */

	//We want a link from a Glyph to its parent VirtualSpace,
	//so we will just hijack the 'owner' attribute of Glyph
	//XXX ugly hack
	before(Glyph glyph, VirtualSpace virtualSpace): 
		glyphAdd(glyph, virtualSpace) {
			if(null == glyph){ return; }
			glyph.setOwner(virtualSpace);
		}

	after(Glyph glyph, VirtualSpace virtualSpace) returning: 
		glyphAdd(glyph, virtualSpace) {
			if(null == glyph){ return; }//XXX add dbg trace?

			Delta delta = glyph.getCreateDelta();
			Message msg = new Message(null, null, delta);
			try{
				retrieveChannel(virtualSpace.getName()).send(msg);
			} catch(Exception e){
				e.printStackTrace();
				throw new Error("Could not retrieve comm channel");
			}
		}

	after(Glyph glyph, VirtualSpace virtualSpace) returning: 
		glyphRemove(glyph, virtualSpace){

		Delta delta = new GlyphRemoveDelta(glyph.getObjId());
		Message msg = new Message(null, null, delta);
		try{
			retrieveChannel(virtualSpace.getName()).send(msg);
		} catch(Exception e){
			e.printStackTrace();
			throw new Error("Could not retrieve comm channel");
		}
	}

	after(Glyph glyph) returning: glyphPosChange(glyph) {
		LongPoint loc = glyph.getLocation();

		Delta delta = new GlyphPosDelta(glyph.getObjId(),
				loc.x, loc.y);

		Message msg = new Message(null, null, delta);
		try{
			//XXX using the 'owner' attribute is an ugly hack
			if(null == glyph.getOwner()){ return; }
			VirtualSpace vs = (VirtualSpace)glyph.getOwner();

			retrieveChannel(vs.getName()).send(msg);
		} catch(Exception e){
			e.printStackTrace();
			throw new Error("Could not retrieve comm channel");
		}
	}

	after(Glyph glyph) returning: glyphColorChange(glyph){
		Color color = glyph.getColor();

		Delta delta = new GlyphColorDelta(glyph.getObjId(),
				color);

		Message msg = new Message(null, null, delta);
		try{
			//XXX using the 'owner' attribute is an ugly hack
			if(null == glyph.getOwner()){ return; }
			VirtualSpace vs = (VirtualSpace)glyph.getOwner();

			retrieveChannel(vs.getName()).send(msg);
		} catch(Exception e){
			e.printStackTrace();
			throw new Error("Could not retrieve comm channel");
		}
	}

	/* Section: CameraGroup-related advice */
	after(CameraGroup cameraGroup) returning: groupPosChange(cameraGroup){
		VirtualSpace vs = cameraGroup.getOwner();
		Delta delta = new GroupLocDelta(cameraGroup.getLocation()); 

		Message msg = new Message(null, null, delta);
		try{
			retrieveChannel(vs.getName()).send(msg);
		} catch(Exception e){
			e.printStackTrace();
			throw new Error("Could not retrieve comm channel");
		}
	}

}

