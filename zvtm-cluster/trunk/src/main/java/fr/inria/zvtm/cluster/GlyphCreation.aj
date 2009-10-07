package fr.inria.zvtm.cluster;

import java.awt.Color;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;

//replicates Glyph+ creation on slaves
aspect GlyphCreation {
	//introduce Glyph.getCreateDelta
	public Delta Glyph.getCreateDelta(){
		String poison = System.getProperty("poisonNopDelta");
		if((poison != null) && (poison.toLowerCase().equals("true"))){
			throw new Error("NopDelta not allowed");
		}
		return new NopDelta();
	}
	
	//advise VirtualSpace.addGlyph
	//advise VirtualSpace.removeGlyph
	
	//overloads for various Glyph subclasses
	public Delta VRectangle.getCreateDelta(){
		return new NopDelta();
		//XXX replace by VRectCreateDelta 
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
	}
}

