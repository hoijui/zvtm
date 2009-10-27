/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import java.awt.Color;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VTriangleOr;

//Replicates Glyph subtypes creation on slaves
//(in fact, waits for the glyphs to be added to a virtual
//space to replicate them on slaves)
aspect GlyphCreation {
	//introduce Glyph.getCreateDelta
	private Delta Glyph.getCreateDelta(){
		String poison = System.getProperty("poisonNopDelta");
		if((poison != null) && (poison.toLowerCase().equals("true"))){
			throw new Error("NopDelta not allowed");
		}
		return new NopDelta();
	}

	pointcut glyphAdd(Glyph glyph, VirtualSpace virtualSpace): 
		execution(public * VirtualSpace.addGlyph(Glyph, boolean, boolean)) 
		&& if(VirtualSpaceManager.INSTANCE.isMaster())
		&& args(glyph, ..)
		&& this(virtualSpace); 

	pointcut glyphRemove(Glyph glyph, VirtualSpace virtualSpace): 
		execution(public * VirtualSpace.removeGlyph(Glyph, boolean)) 
		&& if(VirtualSpaceManager.INSTANCE.isMaster())
		&& args(glyph, ..)
		&& this(virtualSpace);

	before(Glyph glyph, VirtualSpace virtualSpace):
		glyphAdd(glyph, virtualSpace){
			if(glyph == null){
				return;
			}
			glyph.setParentSpace(virtualSpace);
		}

	//advise VirtualSpace.addGlyph
	after(Glyph glyph, VirtualSpace virtualSpace) returning: 
		glyphAdd(glyph, virtualSpace) {
			Delta createDelta = glyph.getCreateDelta();
			VirtualSpaceManager.INSTANCE.sendDelta(createDelta);
		}

	//advise VirtualSpace.removeGlyph
	after(Glyph glyph, VirtualSpace virtualSpace) returning:
		glyphRemove(glyph, virtualSpace){
			Delta delta = new GlyphRemoveDelta(glyph.getObjId(),
					virtualSpace.getObjId());	
		}
	
	//overloads for various Glyph subclasses
	@Override private Delta VRectangle.getCreateDelta(){
		return new VRectangleCreateDelta(this, 
				this.getParentSpace().getObjId());
	}

	@Override private Delta VCircle.getCreateDelta(){
		return new VCircleCreateDelta(this,
				this.getParentSpace().getObjId());
	}

	@Override private Delta VTriangleOr.getCreateDelta(){
		return new VTriangleOrCreateDelta(this,
				this.getParentSpace().getObjId());
	}

	private static class NopDelta implements Delta {
		public void apply(SlaveUpdater su){}

		@Override public String toString(){
			return "NopDelta";
		}
	}

	private static class GlyphRemoveDelta implements Delta {
		private final ObjId glyphId;
		private final ObjId spaceId;

		GlyphRemoveDelta(ObjId glyphId, ObjId spaceId){
			this.glyphId = glyphId;
			this.spaceId = spaceId;
		}	

		public void apply(SlaveUpdater su){
			Glyph glyph = (Glyph)su.getSlaveObject(glyphId);
			VirtualSpace virtualSpace = 
				(VirtualSpace)su.getSlaveObject(spaceId);
			su.removeSlaveObject(glyphId);
			virtualSpace.removeGlyph(glyph);
		}

	}

	private static abstract class AbstractGlyphCreateDelta implements Delta {
		private final GlyphAttributes baseAttr;
		private final ObjId glyphId;
		private final ObjId virtualSpaceId;

		AbstractGlyphCreateDelta(Glyph source, ObjId virtualSpaceId){
			this.baseAttr = GlyphAttributes.fromGlyph(source);
			this.glyphId = source.getObjId();
			this.virtualSpaceId = virtualSpaceId;	
		}

		abstract Glyph createGlyph();

		public final void apply(SlaveUpdater su){
			//template method pattern: calls abstract method.
			//In principle, not meant for inheritance
			Glyph glyph = createGlyph();
			baseAttr.moveAttributesToGlyph(glyph);

			su.putSlaveObject(glyphId, glyph);
			VirtualSpace vs = 
				(VirtualSpace)(su.getSlaveObject(virtualSpaceId));
			vs.addGlyph(glyph);
		}
	}

	private static class VRectangleCreateDelta extends AbstractGlyphCreateDelta {
		private final long halfWidth;
		private final long halfHeight;

		VRectangleCreateDelta(VRectangle source, ObjId virtualSpaceId){
			super(source, virtualSpaceId);
			this.halfWidth = source.getWidth();
			this.halfHeight = source.getHeight();
		}

		Glyph createGlyph(){
			//beware of z-index
			return new VRectangle(0,0,0,halfWidth,halfHeight,Color.BLACK);
		}

		@Override public String toString(){
			return "VRectangleCreateDelta, halfWidth=" + halfWidth
				+ ", halfHeight=" + halfHeight;
		}
	}

	private static class VCircleCreateDelta extends AbstractGlyphCreateDelta {
		private final long radius;

		VCircleCreateDelta(VCircle source, ObjId virtualSpaceId){
			super(source, virtualSpaceId);
			this.radius = (long)(source.getSize());
		}

		Glyph createGlyph(){
			//beware of z-index
			return new VCircle(0,0,0,radius,Color.BLACK);
		}

		@Override public String toString(){
			return "VCircleCreateDelta, radius=" + radius;
		}
	}
	
	private static class VTriangleOrCreateDelta extends AbstractGlyphCreateDelta {
		private final long height;

		VTriangleOrCreateDelta(VTriangleOr source, ObjId virtualSpaceId){
			super(source, virtualSpaceId);
			this.height = (long)(source.getSize());
		}

		Glyph createGlyph(){
			//beware of z-index
			return new VTriangleOr(0,0,0,height,Color.BLACK, 0);
		}
	}
}

