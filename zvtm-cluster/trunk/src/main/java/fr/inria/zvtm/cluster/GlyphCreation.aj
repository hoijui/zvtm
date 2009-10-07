/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import java.awt.Color;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;

//replicates Glyph+ creation on slaves
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

	//advise VirtualSpace.removeGlyph

	//overloads for various Glyph subclasses
	@Override private Delta VRectangle.getCreateDelta(){
		return new VRectangleCreateDelta(this, 
				this.getParentSpace().getObjId());
	}

	private static class NopDelta implements Delta{
		public void apply(SlaveUpdater su){}

		@Override public String toString(){
			return "NopDelta";
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
			//Color.BLACK will be overwritten
			return new VRectangle(0,0,0,halfWidth,halfHeight,Color.BLACK);
		}

		@Override public String toString(){
			return "VRectangleCreateDelta, halfWidth=" + halfWidth
				+ ", halfHeight=" + halfHeight;
		}
	}
}

