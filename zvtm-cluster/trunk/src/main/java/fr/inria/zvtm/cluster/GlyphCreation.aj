/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import java.net.URL;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.CircleNR;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.RectangleNR;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VDiamond;
import fr.inria.zvtm.glyphs.VEllipse;
import fr.inria.zvtm.glyphs.VPoint;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VRing;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.VSlice;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VTextOr;
import fr.inria.zvtm.glyphs.VTriangleOr;

//Replicates Glyph subtypes creation on slaves
//(in fact, waits for the glyphs to be added to a virtual
//space to replicate them on slaves)
public aspect GlyphCreation {
	//introduce Glyph.getCreateDelta
	Delta Glyph.getCreateDelta(){
		String poison = System.getProperty("poisonNopDelta");
		if((poison != null) && (poison.toLowerCase().equals("true"))){
			throw new Error("NopDelta not allowed");
		}
		return new NopDelta();
	}

	public pointcut glyphAdd(Glyph glyph, VirtualSpace virtualSpace): 
		execution(public * VirtualSpace.addGlyph(Glyph, boolean, boolean)) 
		&& if(VirtualSpaceManager.INSTANCE.isMaster())
		&& args(glyph, ..)
		&& this(virtualSpace);
        
	pointcut glyphRemove(Glyph glyph, VirtualSpace virtualSpace): 
		(execution(public * VirtualSpace.removeGlyph(Glyph, boolean)) ||
         execution(public * VirtualSpace.removeGlyph(Glyph)))
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
		glyphAdd(glyph, virtualSpace) &&
		!cflowbelow(glyphAdd(Glyph, VirtualSpace)){
            glyph.setReplicated(true);
			Delta createDelta = glyph.getCreateDelta();
			VirtualSpaceManager.INSTANCE.sendDelta(createDelta);
		}

	//advise VirtualSpace.removeGlyph
	after(Glyph glyph, VirtualSpace virtualSpace) returning:
		glyphRemove(glyph, virtualSpace) &&
        if(glyph.isReplicated()) &&
		!cflowbelow(glyphRemove(Glyph, VirtualSpace)){
			Delta delta = new GlyphRemoveDelta(glyph.getObjId(),
					virtualSpace.getObjId());	
			VirtualSpaceManager.INSTANCE.sendDelta(delta);
		}
	
	//overrides for various Glyph subclasses
	@Override Delta VRectangle.getCreateDelta(){
		return new VRectangleCreateDelta(this, 
				this.getParentSpace().getObjId());
	}

	@Override Delta VCircle.getCreateDelta(){
		return new VCircleCreateDelta(this,
				this.getParentSpace().getObjId());
	}

	@Override Delta VTriangleOr.getCreateDelta(){
		return new VTriangleOrCreateDelta(this,
				this.getParentSpace().getObjId());
	}

	@Override Delta VSegment.getCreateDelta(){
		return new VSegmentCreateDelta(this,
				this.getParentSpace().getObjId());
	}

	@Override Delta VText.getCreateDelta(){
		return new VTextCreateDelta(this,
				this.getParentSpace().getObjId());
	}

    @Override Delta VTextOr.getCreateDelta(){
        return new VTextOrCreateDelta(this,
                this.getParentSpace().getObjId());
    }

	@Override Delta RectangleNR.getCreateDelta(){
		return new RectangleNRCreateDelta(this,
				this.getParentSpace().getObjId());
	}

	@Override Delta CircleNR.getCreateDelta(){
		return new CircleNRCreateDelta(this,
				this.getParentSpace().getObjId());
	}

	@Override Delta ClusteredImage.getCreateDelta(){
		return new ClusteredImageCreateDelta(this,
				this.getParentSpace().getObjId());
	}

	@Override Delta DPath.getCreateDelta(){
		return new DPathCreateDelta(this,
				this.getParentSpace().getObjId());
	}

    @Override Delta VEllipse.getCreateDelta(){
        return new VEllipseCreateDelta(this,
                this.getParentSpace().getObjId());
    }

    @Override Delta VPoint.getCreateDelta(){
        return new VPointCreateDelta(this,
                this.getParentSpace().getObjId());
    }

    @Override Delta VRing.getCreateDelta(){
        return new VRingCreateDelta(this,
                this.getParentSpace().getObjId());
    }

    @Override Delta VSlice.getCreateDelta(){
        return new VSliceCreateDelta(this,
                this.getParentSpace().getObjId());
    }

    @Override Delta VPolygon.getCreateDelta(){
        return new VPolygonCreateDelta(this,
                this.getParentSpace().getObjId());
    }

    @Override Delta VDiamond.getCreateDelta(){
        return new VDiamondCreateDelta(this,
                this.getParentSpace().getObjId());
    }

	private static class NopDelta implements Delta {
		public void apply(SlaveUpdater su){}

		@Override public String toString(){
			return "NopDelta";
		}
	}

	private static class GlyphRemoveDelta implements Delta {
		private final ObjId<Glyph> glyphId;
		private final ObjId<VirtualSpace> spaceId;

		GlyphRemoveDelta(ObjId<Glyph> glyphId, ObjId<VirtualSpace> spaceId){
			this.glyphId = glyphId;
			this.spaceId = spaceId;
		}	

		public void apply(SlaveUpdater su){
			Glyph glyph = su.getSlaveObject(glyphId);
			VirtualSpace virtualSpace = su.getSlaveObject(spaceId);
			su.removeSlaveObject(glyphId);
			virtualSpace.removeGlyph(glyph);
		}

	}

	private static abstract class AbstractGlyphCreateDelta implements Delta {
		private final GlyphAttributes baseAttr;
		private final ObjId<Glyph> glyphId;
		private final ObjId<VirtualSpace> virtualSpaceId;

		AbstractGlyphCreateDelta(Glyph source, ObjId<VirtualSpace> virtualSpaceId){
			this.baseAttr = GlyphAttributes.fromGlyph(source);
			this.glyphId = source.getObjId();
			this.virtualSpaceId = virtualSpaceId;	
		}

		abstract Glyph createGlyph();

		protected void stateTransferHook(Glyph glyph){
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
			VirtualSpace vs = su.getSlaveObject(virtualSpaceId);
			vs.addGlyph(glyph);
		}
	}

	static abstract class ClosedShapeCreateDelta extends AbstractGlyphCreateDelta {
		private final Color borderColor;
		private final Color mouseInsideFillColor;
		private final boolean filled;
		private final boolean borderDrawn;

		ClosedShapeCreateDelta(ClosedShape source, ObjId<VirtualSpace> virtualSpaceId){
			super(source, virtualSpaceId);
			this.borderColor = source.getDefaultBorderColor();
			this.mouseInsideFillColor = source.mouseInsideFColor;
			this.filled = source.isFilled();
			this.borderDrawn = source.isBorderDrawn(); 
		}

		@Override protected void stateTransferHook(Glyph glyph){
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

		VRectangleCreateDelta(VRectangle source, ObjId<VirtualSpace> virtualSpaceId){
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

		VCircleCreateDelta(VCircle source, ObjId<VirtualSpace> virtualSpaceId){
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

		VTriangleOrCreateDelta(VTriangleOr source, ObjId<VirtualSpace> virtualSpaceId){
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

		VSegmentCreateDelta(VSegment source, ObjId<VirtualSpace> virtualSpaceId){
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
		protected final String text;
		protected final float scaleFactor;
		protected final short textAnchor;

		VTextCreateDelta(VText source, ObjId<VirtualSpace> virtualSpaceId){
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

    private static class VTextOrCreateDelta extends VTextCreateDelta{
        VTextOrCreateDelta(VTextOr source, ObjId<VirtualSpace> virtualSpaceId){
            super(source, virtualSpaceId);
        }

        @Override Glyph createGlyph(){
            return new VTextOr(0,0,0,Color.BLACK,text,0f,textAnchor,scaleFactor);
        }
    }

	private static class RectangleNRCreateDelta extends ClosedShapeCreateDelta {
		private final long halfWidth;
		private final long halfHeight;

		RectangleNRCreateDelta(RectangleNR source, ObjId<VirtualSpace> virtualSpaceId){
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
		
		CircleNRCreateDelta(CircleNR source, ObjId<VirtualSpace> virtualSpaceId){
			super(source, virtualSpaceId);
			this.radius = (long)(source.getSize());
		}

		Glyph createGlyph(){
			return new CircleNR(0,0,0,radius,Color.BLACK);
		}
	}

	private static class ClusteredImageCreateDelta extends ClosedShapeCreateDelta {
		private final double scaleFactor;
		private final URL imageLocation;

		ClusteredImageCreateDelta(ClusteredImage source, ObjId<VirtualSpace> virtualSpaceId){
			super(source, virtualSpaceId);
			this.scaleFactor = source.scaleFactor;
			this.imageLocation = source.getImageLocation();
		}

		Glyph createGlyph(){
			return new ClusteredImage(0,0,0,imageLocation,scaleFactor);
		}
	}

	private static class DPathCreateDelta extends AbstractGlyphCreateDelta {
		//note that GeneralPath is only serializable
		//starting with Java 1.6
		private final GeneralPath path;

		DPathCreateDelta(DPath source, ObjId<VirtualSpace> virtualSpaceId){
			super(source, virtualSpaceId);
			this.path = source.getJava2DGeneralPath();
		}

		Glyph createGlyph(){
			return new DPath(path.getPathIterator(null), 
					0, Color.BLACK);
		}
	}

    private static class VEllipseCreateDelta extends ClosedShapeCreateDelta {
        private final long sx;
        private final long sy;
        
        VEllipseCreateDelta(VEllipse source, ObjId<VirtualSpace> virtualSpaceId){
            super(source, virtualSpaceId);
            this.sx = source.getWidth();
            this.sy = source.getHeight();
        }

        Glyph createGlyph(){
            return new VEllipse(0,0,0,sx,sy,Color.BLACK);
        }
    }

    private static class VPointCreateDelta extends AbstractGlyphCreateDelta {

        VPointCreateDelta(VPoint source, ObjId<VirtualSpace> virtualSpaceId){
            super(source, virtualSpaceId);
        }

        Glyph createGlyph(){
            return new VPoint();
        }
    }

    private static class VRingCreateDelta extends ClosedShapeCreateDelta {
        private final long arcRadius;
        private final double arcAngle;
        private final float irRad; //inner ring radius (%outer ring radius)
        private final double sliceOrient;

        VRingCreateDelta(VRing source, ObjId<VirtualSpace> virtualSpaceId){
            super(source, virtualSpaceId);
            this.arcRadius = source.vr;
            this.arcAngle = source.angle;
            this.irRad = source.getInnerRatio();
            this.sliceOrient = source.orient;
        }

        Glyph createGlyph(){
            return new VRing(0,0,0,arcRadius,arcAngle,irRad,sliceOrient,
                    Color.BLACK, Color.BLACK);
        }
    }

    private static class VSliceCreateDelta extends ClosedShapeCreateDelta {
        private final long arcRadius;
        private final double arcAngle;
        private final double sliceOrient; 

        VSliceCreateDelta(VSlice source, ObjId<VirtualSpace> virtualSpaceId){
            super(source, virtualSpaceId);
            this.arcRadius = source.vr;
            this.arcAngle = source.angle;
            this.sliceOrient = source.orient;
        }

        Glyph createGlyph(){
            return new VSlice(0,0,0,arcRadius,arcAngle,sliceOrient,
                    Color.BLACK,Color.BLACK);
        }
    }

    private static class VPolygonCreateDelta extends ClosedShapeCreateDelta {
        private final LongPoint[] coords;

        VPolygonCreateDelta(VPolygon source, ObjId<VirtualSpace> virtualSpaceId){
            super(source, virtualSpaceId);
            this.coords = source.getAbsoluteVertices();
        }

        Glyph createGlyph(){
            return new VPolygon(coords, 0, Color.BLACK);
        }
    }

    private static class VDiamondCreateDelta extends ClosedShapeCreateDelta {
        private final long size; //width = height

        VDiamondCreateDelta(VDiamond source, ObjId<VirtualSpace> virtualSpaceId){
            super(source, virtualSpaceId);
            this.size = (long)source.getSize();
        }

        Glyph createGlyph(){
            return new VDiamond(0,0,0,size,Color.BLACK);
        }
    }
}

