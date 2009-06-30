package fr.inria.zvtm.clustering;

import java.util.HashMap;
import java.util.Map;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;

/**
 * A VirtualSpace that can be replicated over a cluster
 */
public aspect MasterVirtualSpace {
	//data structure for easy retrieval of a Glyph 
	//given its key: either a BStree or a hash table
	private final Map<ObjId, Glyph> VirtualSpace.idMap = new HashMap<ObjId, Glyph>();

	//multicast group, named after the virtual space 
	
	//define pointcuts to catch Glyphs additions 
	//and removals to/from a VirtualSpace
	//make sure this join point fires only once even if addGlyph
	//overloads are chained together (!within VirtualSpaceManager...)
	pointcut glyphAdd(Glyph glyph): 
		call(public Glyph VirtualSpaceManager.addGlyph(..)) 
		&& args(glyph, ..)
		&&!within(VirtualSpaceManager);

	//here we need to capture the Glyph argument
	pointcut glyphRemove(Glyph glyph): 
		call(public * VirtualSpace.removeGlyph(..)) 
		&& args(glyph, ..)
		&&!within(VirtualSpace); 

	//!within(Glyph+) to avoid calling multiple times when chained overloads(is it the right approach?)
	pointcut glyphPosChange(Glyph glyph):
		(call(public * Glyph+.moveTo(..)) || call(public * Glyph+.move(..)))
		&& target(glyph)
		&&!within(Glyph+);

	//
	after(Glyph glyph) returning: glyphAdd(glyph) {
		System.out.println("glyph add: send add message");
		//idMap.put();
	}

	after(Glyph glyph) returning: glyphRemove(glyph){
		System.out.println("glyph remove: send remove message");
	}

	after(Glyph glyph) returning: glyphPosChange(glyph) {
		LongPoint loc = glyph.getLocation();
		System.out.println("Glyph moved, destination (" +
		loc.x + "," + loc.y + ")");	
	}

	//retrieve a glyph according to its (cluster) id
	//overloading Glyph.equals is probably more elegant (and would
	//allow us to use a set instead of a map) but I am not comfortable
	//with doing that from AspectJ yet.
	public Glyph VirtualSpace.getGlyphById(ObjId id){
		return idMap.get(id);
	}
}

