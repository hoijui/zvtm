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
import fr.inria.zvtm.glyphs.CircleNR;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.RectangleNR;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.VText;
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
	
	//overrides for various Glyph subclasses
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

	@Override private Delta VSegment.getCreateDelta(){
		return new VSegmentCreateDelta(this,
				this.getParentSpace().getObjId());
	}

	@Override private Delta VText.getCreateDelta(){
		return new VTextCreateDelta(this,
				this.getParentSpace().getObjId());
	}

	@Override private Delta RectangleNR.getCreateDelta(){
		return new RectangleNRCreateDelta(this,
				this.getParentSpace().getObjId());
	}

	@Override private Delta CircleNR.getCreateDelta(){
		return new CircleNRCreateDelta(this,
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

		void stateTransferHook(Glyph glyph){
			//left empty. subclasses may use this to transfer
			//additional state information to the glyph.
			//'glyph' may be downcast to the type of the object
			//that was passed to the AbstractGlyphCreateDelta ctor
			//Important: overrides should chain to their parents
		}

		public final void apply(SlaveUpdater su){
			//template method pattern: calls abstract method.
			//In principle, not meant for inheritance
			Glyph glyph = createGlyph();
			baseAttr.moveAttributesToGlyph(glyph);
			stateTransferHook(glyph);

			su.putSlaveObject(glyphId, glyph);
			VirtualSpace vs = 
				(VirtualSpace)(su.getSlaveObject(virtualSpaceId));
			vs.addGlyph(glyph);
		}
	}

	private static abstract class ClosedShapeCreateDelta extends AbstractGlyphCreateDelta {
		private final Color borderColor;
		private final Color mouseInsideFillColor;
		private final boolean filled;
		private final boolean borderDrawn;

		ClosedShapeCreateDelta(ClosedShape source, ObjId virtualSpaceId){
			super(source, virtualSpaceId);
			this.borderColor = source.getDefaultBorderColor();
			this.mouseInsideFillColor = source.mouseInsideFColor;
			this.filled = source.isFilled();
			this.borderDrawn = source.isBorderDrawn(); 
		}

		@Override void stateTransferHook(Glyph glyph){
			//note that overrides should chain to their parent
			super.stateTransferHook(glyph);
			ClosedShape dest = (ClosedShape)glyph;
			dest.bColor = borderColor;
			dest.setMouseInsideFillColor(mouseInsideFillColor);
			dest.setFilled(filled);
			dest.setDrawBorder(borderDrawn);
		}
	}

	private static class VRectangleCreateDelta extends ClosedShapeCreateDelta {
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

	private static class VCircleCreateDelta extends ClosedShapeCreateDelta {
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
	
	private static class VTriangleOrCreateDelta extends ClosedShapeCreateDelta {
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

	private static class VSegmentCreateDelta extends AbstractGlyphCreateDelta {
		private final long halfWidth;
		private final long halfHeight;

		VSegmentCreateDelta(VSegment source, ObjId virtualSpaceId){
			super(source, virtualSpaceId);
			this.halfWidth = source.getWidth();
			this.halfHeight = source.getHeight();
		}

		Glyph createGlyph(){
			//beware of z-index
			return new VSegment(0,0,0,halfWidth,halfHeight,Color.BLACK);
		}
	}

	private static class VTextCreateDelta extends AbstractGlyphCreateDelta {
		private final String text;
		private final float scaleFactor;
		private final short textAnchor;

		VTextCreateDelta(VText source, ObjId virtualSpaceId){
			super(source, virtualSpaceId);
			this.text = source.getText();
			this.scaleFactor = source.getScale();
			this.textAnchor = source.getTextAnchor();
		}

		Glyph createGlyph(){
			//beware of z-index
			return new VText(0,0,0,Color.BLACK,text,textAnchor,scaleFactor);
		}
	}

	private static class RectangleNRCreateDelta extends ClosedShapeCreateDelta {
		private final long halfWidth;
		private final long halfHeight;

		RectangleNRCreateDelta(RectangleNR source, ObjId virtualSpaceId){
			super(source, virtualSpaceId);
			this.halfWidth = source.getWidth();
			this.halfHeight = source.getHeight();
		}

		Glyph createGlyph(){
			return new RectangleNR(0,0,0,halfWidth,halfHeight,Color.BLACK);
		}
	}

	private static class CircleNRCreateDelta extends ClosedShapeCreateDelta {
		private final long radius;
		
		CircleNRCreateDelta(CircleNR source, ObjId virtualSpaceId){
			super(source, virtualSpaceId);
			this.radius = (long)(source.getSize());
		}

		Glyph createGlyph(){
			return new CircleNR(0,0,0,radius,Color.BLACK);
		}
	}
}

