package fr.inria.zvtm.clustering;

import fr.inria.zvtm.clustering.ObjId;
import fr.inria.zvtm.clustering.ObjIdFactory;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;

/**
 * A VirtualSpace that can be replicated over a cluster
 */
public aspect MasterVirtualSpace {
	
	//augment Glyph with an ObjId identifier
	private ObjId Glyph.id = ObjIdFactory.next();
	private ObjId Glyph.getObjId(){ return id; }

	//multicast group, named after the virtual space 
	
	//define pointcuts to catch Glyphs additions 
	//and removals to/from a VirtualSpace
	//make sure this join point fires only once even if addGlyph
	//overloads are chained together (!within VirtualSpaceManager...)
	pointcut glyphAdd(Glyph glyph): 
		call(public Glyph VirtualSpaceManager.addGlyph(..)) 
		&& args(glyph, ..)
		&&!within(VirtualSpaceManager)
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

	
	after(Glyph glyph) returning: glyphAdd(glyph) {
		//information needed: the glyph's ID (that we get through
		//a method injection) and parent virtualspace (that we can
		//obtain by instrumenting the right method in VS rather than VSM)
		System.out.println("glyph add: send add message");
	}

	after(Glyph glyph, VirtualSpace virtualSpace) returning: 
		glyphRemove(glyph, virtualSpace){
		//information needed: the glyph's ID (that we get through
		//a method injection) and parent virtualspace (target)
		System.out.println("glyph remove: send remove message");
	}

	after(Glyph glyph) returning: glyphPosChange(glyph) {
		LongPoint loc = glyph.getLocation();
		System.out.println("Glyph moved, destination (" +
		loc.x + "," + loc.y + ")");	
	}


}

