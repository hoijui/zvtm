/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.clustering;

import java.awt.Color;

import java.util.HashMap;
import java.util.Map;

import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.VText;

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
	//'private' attr made public for Jython (zvtm shell) purposes
	public ObjId Glyph.id = ObjIdFactory.next();
	public ObjId Glyph.getObjId(){ return id; }

	//augment Camera with an ObjId identifier (serializable)
	private ObjId Camera.id = ObjIdFactory.next();
	private ObjId Camera.getObjId(){ return id; }

	//test: augment VirtualSpace with an 'isSlave' attribute
	private boolean VirtualSpace.isSlave = false;
	boolean VirtualSpace.isSlave(){ return isSlave; }
	void VirtualSpace.setSlave(boolean b){ isSlave = b; }
	boolean Glyph.isOwnerSlave(){ return ((VirtualSpace)getOwner()).isSlave(); }

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
			retval.setOpt(Channel.LOCAL, Boolean.FALSE);
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
	//[commented out private mod for saving purposes, but should be left
	//private in a 'production' design (?)
	/* private */public CreateDelta Glyph.getCreateDelta(){
		return new NopDelta();
	}	

	//note the private @Override; no contradiction here because
	//private means "private within the aspect"
	@Override public CreateDelta VRectangle.getCreateDelta(){
		return new RectangleCreateDelta(getObjId(), 
				getLocation(), 
				getZindex(),
				getWidth(), getHeight(), 
				getColor());
	}
	@Override public CreateDelta VCircle.getCreateDelta(){
		return new CircleCreateDelta(getObjId(), 
				getLocation(), 
				getZindex(),
				(long)getSize(),
				getColor(),
				getBorderColor());
	}
	@Override public CreateDelta VSegment.getCreateDelta(){
		return new SegmentCreateDelta(getObjId(), 
				getEndPoints()[0], 
				getEndPoints()[1], 
				getZindex(),
				getColor(),
				getStrokeWidth());
	}
	@Override public CreateDelta VText.getCreateDelta(){
		return new TextCreateDelta(getObjId(),
				getLocation(),
				getZindex(),
				getColor(),
				getText(),
				getTextAnchor(),
				getScale()
				);
	}
	@Override public CreateDelta ClusteredImage.getCreateDelta(){
		return new ImageCreateDelta(getObjId(),
				getLocation(),
				getZindex(),
				getImageLocation(),
				getScale());
	}

	/* Section: Glyph-related pointcuts */

	//define pointcuts to catch Glyphs additions 
	//and removals to/from a VirtualSpace
	//make sure this join point fires only once even if addGlyph
	//overloads are chained together 	
	pointcut glyphAdd(Glyph glyph, VirtualSpace virtualSpace): 
		execution(public * VirtualSpace.addGlyph(Glyph, boolean, boolean)) 
		&& args(glyph, ..)
		&& this(virtualSpace); 

	pointcut glyphRemove(Glyph glyph, VirtualSpace virtualSpace): 
		execution(public * VirtualSpace.removeGlyph(Glyph, boolean)) 
		&& args(glyph, ..)
		&& this(virtualSpace);

	//!within(Glyph+) to avoid calling multiple times when chained overloads(is it the right approach?)
	pointcut glyphPosChange(Glyph glyph):
		(execution(public * Glyph+.moveTo(long, long)) || execution(public * Glyph+.move(long, long)))
		&& this(glyph);

	pointcut glyphColorChange(Glyph glyph):
		(execution(public * Glyph+.setColor(..)) || execution(public * Glyph+.setHSVColor(..)))
		 && this(glyph); //todo avoid method chaining (?)

	pointcut glyphStrokeWidthChange(Glyph glyph):
		execution(public * Glyph+.setStrokeWidth(..))
		&& this(glyph);

 	pointcut glyphVisibilityChange(Glyph glyph):
		execution(public * Glyph+.setVisible(boolean))
		&& this(glyph);

	/* Section: misc. VS-related pointcuts */
	pointcut removeAllGlyphs(VirtualSpace virtualSpace): 
		execution(public * VirtualSpace.removeAllGlyphs()) 
		&& this(virtualSpace);

	/* Section: CameraGroup-related pointcuts */
	pointcut groupPosChange(CameraGroup cameraGroup):
		execution(public * CameraGroup.setLocation(..))
		&& this(cameraGroup);

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
			if(virtualSpace.isSlave()){ return;}

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
			if(virtualSpace.isSlave()){ return;}
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
		if(null == glyph.getOwner()){ return; }
		if(glyph.isOwnerSlave()){ return;}

		LongPoint loc = glyph.getLocation();

		Delta delta = new GlyphPosDelta(glyph.getObjId(),
				loc.x, loc.y);

		Message msg = new Message(null, null, delta);
		try{
			VirtualSpace vs = (VirtualSpace)glyph.getOwner();

			retrieveChannel(vs.getName()).send(msg);
		} catch(Exception e){
			e.printStackTrace();
			throw new Error("Could not retrieve comm channel");
		}
	}

	after(Glyph glyph) returning: glyphColorChange(glyph){
		if(null == glyph.getOwner()){ return; }
		if(glyph.isOwnerSlave()){ return;}

		Color color = glyph.getColor();

		Delta delta = new GlyphColorDelta(glyph.getObjId(),
				color);

		Message msg = new Message(null, null, delta);
		try{
			VirtualSpace vs = (VirtualSpace)glyph.getOwner();

			retrieveChannel(vs.getName()).send(msg);
		} catch(Exception e){
			e.printStackTrace();
			throw new Error("Could not retrieve comm channel");
		}
	}

	after(Glyph glyph) returning: glyphStrokeWidthChange(glyph){
		if(null == glyph.getOwner()){ return; }
		if(glyph.isOwnerSlave()){ return;}

		float strokeWidth = glyph.getStrokeWidth();

		Delta delta = new GlyphStrokeWidthDelta(glyph.getObjId(),
				strokeWidth);

		Message msg = new Message(null, null, delta);
		try{
			VirtualSpace vs = (VirtualSpace)glyph.getOwner();

			retrieveChannel(vs.getName()).send(msg);
		} catch(Exception e){
			e.printStackTrace();
			throw new Error("Could not retrieve comm channel");
		}
	}

	after(Glyph glyph) returning: glyphVisibilityChange(glyph){
		if(null == glyph.getOwner()){ return; }
		if(glyph.isOwnerSlave()){ return;}

		boolean visible = glyph.isVisible();

		Delta delta = new GlyphVisibilityDelta(glyph.getObjId(),
				visible);

		Message msg = new Message(null, null, delta);
		try{
			VirtualSpace vs = (VirtualSpace)glyph.getOwner();

			retrieveChannel(vs.getName()).send(msg);
		} catch(Exception e){
			e.printStackTrace();
			throw new Error("Could not retrieve comm channel");
		}
	}

	/* Section: misc. VS-related advice */
	after(VirtualSpace virtualSpace): 
		removeAllGlyphs(virtualSpace) {
			if(virtualSpace.isSlave()){ return;}

			Delta delta = new RemoveAllGlyphsDelta();
			Message msg = new Message(null, null, delta);
			try{
				retrieveChannel(virtualSpace.getName()).send(msg);
			} catch(Exception e){
				e.printStackTrace();
				throw new Error("Could not retrieve comm channel");
			}

		}

	/* Section: CameraGroup-related advice */
	after(CameraGroup cameraGroup) returning: groupPosChange(cameraGroup){
		VirtualSpace vs = cameraGroup.getOwner();
		if(vs.isSlave()){ return; }

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

