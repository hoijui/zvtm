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
import java.io.Serializable;
import java.net.URL;
import javax.swing.ImageIcon;
import java.util.ArrayList;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.CircleNR;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.Composite;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.RectangleNR;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VDiamond;
import fr.inria.zvtm.glyphs.VEllipse;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VPoint;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VRing;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.VSlice;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VTextOr;
import fr.inria.zvtm.glyphs.VTriangleOr;

interface GlyphReplicator extends Serializable {
   public Glyph createGlyph();
}

//Replicates Glyph subtypes creation on slaves
//(in fact, waits for the glyphs to be added to a virtual
//space to replicate them on slaves)
public aspect GlyphCreation {
        //introduce Glyph.getReplicator
	GlyphReplicator Glyph.getReplicator(){
		String poison = System.getProperty("poisonNopReplicator");
		if((poison != null) && (poison.toLowerCase().equals("true"))){
			throw new Error("NopReplicator not allowed");
		}
		return new NopReplicator();
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
            Delta createDelta = new GlyphCreateDelta(glyph.getReplicator(),
                    glyph.getObjId(), glyph.getParentSpace().getObjId());
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
	@Override GlyphReplicator VRectangle.getReplicator(){
		return new VRectangleReplicator(this);
	}

	@Override GlyphReplicator VCircle.getReplicator(){
		return new VCircleReplicator(this);
	}

	@Override GlyphReplicator VTriangleOr.getReplicator(){
		return new VTriangleOrReplicator(this);
	}

	@Override GlyphReplicator VSegment.getReplicator(){
		return new VSegmentReplicator(this);
	}

	@Override GlyphReplicator VText.getReplicator(){
		return new VTextReplicator(this);
	}

    @Override GlyphReplicator VTextOr.getReplicator(){
        return new VTextOrReplicator(this);
    }

	@Override GlyphReplicator RectangleNR.getReplicator(){
		return new RectangleNRReplicator(this);
	}

	@Override GlyphReplicator CircleNR.getReplicator(){
		return new CircleNRReplicator(this);
	}

	@Override GlyphReplicator VImage.getReplicator(){
		return new VImageReplicator(this);
	}

	@Override GlyphReplicator ClusteredImage.getReplicator(){
		return new ClusteredImageReplicator(this);
	}

	@Override GlyphReplicator DPath.getReplicator(){
		return new DPathReplicator(this);
	}

    @Override GlyphReplicator VEllipse.getReplicator(){
        return new VEllipseReplicator(this);
    }

    @Override GlyphReplicator VPoint.getReplicator(){
        return new VPointReplicator(this);
    }

    @Override GlyphReplicator VRing.getReplicator(){
        return new VRingReplicator(this);
    }

    @Override GlyphReplicator VSlice.getReplicator(){
        return new VSliceReplicator(this);
    }

    @Override GlyphReplicator VPolygon.getReplicator(){
        return new VPolygonReplicator(this);
    }

    @Override GlyphReplicator VDiamond.getReplicator(){
        return new VDiamondReplicator(this);
    }

    @Override GlyphReplicator Composite.getReplicator(){
        return new CompositeReplicator(this);
    }

	private static class NopReplicator implements GlyphReplicator {
        public Glyph createGlyph(){
            return null;
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

    private static class GlyphCreateDelta implements Delta {
        private final GlyphReplicator replicator;
        private final ObjId<Glyph> glyphId;
        private final ObjId<VirtualSpace> parentSpaceId;

        GlyphCreateDelta(GlyphReplicator replicator, ObjId<Glyph> glyphId, ObjId<VirtualSpace> parentSpaceId){
            this.replicator = replicator;
            this.glyphId = glyphId;
            this.parentSpaceId = parentSpaceId;
        }

        public void apply(SlaveUpdater su){
            Glyph glyph = replicator.createGlyph();

            su.putSlaveObject(glyphId, glyph);
            VirtualSpace vs = su.getSlaveObject(parentSpaceId);
            vs.addGlyph(glyph);
        }
    }

	private static abstract class AbstractGlyphReplicator implements GlyphReplicator {
		private final GlyphAttributes baseAttr;

		AbstractGlyphReplicator(Glyph source){
			this.baseAttr = GlyphAttributes.fromGlyph(source);
		}

		protected void stateTransferHook(Glyph glyph){
			//left empty. subclasses may use this to transfer
			//additional state information to the glyph.
			//'glyph' may be downcast to the type of the object
			//that was passed to the AbstractGlyphReplicator ctor
			//Important: overrides should chain to their parents
            //(i.e. call super.stateTransferHook())
		}

        public final Glyph createGlyph(){
            Glyph glyph = doCreateGlyph();
            baseAttr.moveAttributesToGlyph(glyph);
            stateTransferHook(glyph);
            return glyph;
        }
        
        abstract Glyph doCreateGlyph();
	}

	static abstract class ClosedShapeReplicator extends AbstractGlyphReplicator {
		private final Color borderColor;
		private final Color cursorInsideFillColor;
		private final boolean filled;
		private final boolean borderDrawn;

		ClosedShapeReplicator(ClosedShape source){
			super(source);
			this.borderColor = source.getDefaultBorderColor();
			this.cursorInsideFillColor = source.cursorInsideFColor;
			this.filled = source.isFilled();
			this.borderDrawn = source.isBorderDrawn(); 
		}

		@Override protected void stateTransferHook(Glyph glyph){
			//note that overrides should chain to their parent
			super.stateTransferHook(glyph);
			ClosedShape dest = (ClosedShape)glyph;
			dest.bColor = borderColor;
			dest.setCursorInsideFillColor(cursorInsideFillColor);
			dest.setFilled(filled);
			dest.setDrawBorder(borderDrawn);
		}
	}

	private static class VRectangleReplicator extends ClosedShapeReplicator {
		private final long halfWidth;
		private final long halfHeight;

		VRectangleReplicator(VRectangle source){
			super(source);
			this.halfWidth = source.getWidth();
			this.halfHeight = source.getHeight();
		}

		public Glyph doCreateGlyph(){
			//beware of z-index
			return new VRectangle(0,0,0,halfWidth,halfHeight,Color.BLACK);
		}

		@Override public String toString(){
			return "VRectangleReplicator, halfWidth=" + halfWidth
				+ ", halfHeight=" + halfHeight;
		}
	}

	private static class VCircleReplicator extends ClosedShapeReplicator {
		private final long radius;

		VCircleReplicator(VCircle source){
			super(source);
			this.radius = (long)(source.getSize());
		}

		public Glyph doCreateGlyph(){
			//beware of z-index
			return new VCircle(0,0,0,radius,Color.BLACK);
		}

		@Override public String toString(){
			return "VCircleReplicator, radius=" + radius;
		}
	}
	
	private static class VTriangleOrReplicator extends ClosedShapeReplicator {
		private final long height;

		VTriangleOrReplicator(VTriangleOr source){
			super(source);
			this.height = (long)(source.getSize());
		}

		public Glyph doCreateGlyph(){
			//beware of z-index
			return new VTriangleOr(0,0,0,height,Color.BLACK, 0);
		}
	}

	private static class VSegmentReplicator extends AbstractGlyphReplicator {
		private final long halfWidth;
		private final long halfHeight;

		VSegmentReplicator(VSegment source){
			super(source);
			this.halfWidth = source.getWidth();
			this.halfHeight = source.getHeight();
		}

		public Glyph doCreateGlyph(){
			//beware of z-index
			return new VSegment(0,0,0,halfWidth,halfHeight,Color.BLACK);
		}
	}

	private static class VTextReplicator extends AbstractGlyphReplicator {
		protected final String text;
		protected final float scaleFactor;
		protected final short textAnchor;

		VTextReplicator(VText source){
			super(source);
			this.text = source.getText();
			this.scaleFactor = source.getScale();
			this.textAnchor = source.getTextAnchor();
		}

		public Glyph doCreateGlyph(){
			//beware of z-index
			return new VText(0,0,0,Color.BLACK,text,textAnchor,scaleFactor);
		}
	}

    private static class VTextOrReplicator extends VTextReplicator{
        VTextOrReplicator(VTextOr source){
            super(source);
        }

        @Override public Glyph doCreateGlyph(){
            return new VTextOr(0,0,0,Color.BLACK,text,0f,textAnchor,scaleFactor);
        }
    }

	private static class RectangleNRReplicator extends ClosedShapeReplicator {
		private final long halfWidth;
		private final long halfHeight;

		RectangleNRReplicator(RectangleNR source){
			super(source);
			this.halfWidth = source.getWidth();
			this.halfHeight = source.getHeight();
		}

		public Glyph doCreateGlyph(){
			return new RectangleNR(0,0,0,halfWidth,halfHeight,Color.BLACK);
		}
	}

	private static class CircleNRReplicator extends ClosedShapeReplicator {
		private final long radius;
		
		CircleNRReplicator(CircleNR source){
			super(source);
			this.radius = (long)(source.getSize());
		}

		public Glyph doCreateGlyph(){
			return new CircleNR(0,0,0,radius,Color.BLACK);
		}
	}

    private static class VImageReplicator extends ClosedShapeReplicator {
        //Note that serialized ImageIcon instances (as most AWT objects)
        //are not guaranteed to be portable across toolkits.
        //If this becomes a practical concern, then another serialization 
        //mechanism is to be used.
        private final ImageIcon serImage; 
        private final double scaleFactor;

        VImageReplicator(VImage source){
            super(source);
            this.serImage = new ImageIcon(source.getImage());
            this.scaleFactor = source.scaleFactor;
        }

        public Glyph doCreateGlyph(){
            return new VImage(0,0,0,serImage.getImage(), scaleFactor);
        }
    }

	private static class ClusteredImageReplicator extends ClosedShapeReplicator {
		private final double scaleFactor;
		private final URL imageLocation;

		ClusteredImageReplicator(ClusteredImage source){
			super(source);
			this.scaleFactor = source.scaleFactor;
			this.imageLocation = source.getImageLocation();
		}

		public Glyph doCreateGlyph(){
			return new ClusteredImage(0,0,0,imageLocation,scaleFactor);
		}
	}

	private static class DPathReplicator extends AbstractGlyphReplicator {
		//note that GeneralPath is only serializable
		//starting with Java 1.6
		private final GeneralPath path;

		DPathReplicator(DPath source){
			super(source);
			this.path = source.getJava2DGeneralPath();
		}

		public Glyph doCreateGlyph(){
			return new DPath(path.getPathIterator(null), 
					0, Color.BLACK);
		}
	}

    private static class VEllipseReplicator extends ClosedShapeReplicator {
        private final long sx;
        private final long sy;
        
        VEllipseReplicator(VEllipse source){
            super(source);
            this.sx = source.getWidth();
            this.sy = source.getHeight();
        }

        public Glyph doCreateGlyph(){
            return new VEllipse(0,0,0,sx,sy,Color.BLACK);
        }
    }

    private static class VPointReplicator extends AbstractGlyphReplicator {

        VPointReplicator(VPoint source){
            super(source);
        }

        public Glyph doCreateGlyph(){
            return new VPoint();
        }
    }

    private static class VRingReplicator extends ClosedShapeReplicator {
        private final long arcRadius;
        private final double arcAngle;
        private final float irRad; //inner ring radius (%outer ring radius)
        private final double sliceOrient;

        VRingReplicator(VRing source){
            super(source);
            this.arcRadius = source.vr;
            this.arcAngle = source.angle;
            this.irRad = source.getInnerRatio();
            this.sliceOrient = source.orient;
        }

        public Glyph doCreateGlyph(){
            return new VRing(0,0,0,arcRadius,arcAngle,irRad,sliceOrient,
                    Color.BLACK, Color.BLACK);
        }
    }

    private static class VSliceReplicator extends ClosedShapeReplicator {
        private final long arcRadius;
        private final double arcAngle;
        private final double sliceOrient; 

        VSliceReplicator(VSlice source){
            super(source);
            this.arcRadius = source.vr;
            this.arcAngle = source.angle;
            this.sliceOrient = source.orient;
        }

        public Glyph doCreateGlyph(){
            return new VSlice(0,0,0,arcRadius,arcAngle,sliceOrient,
                    Color.BLACK,Color.BLACK);
        }
    }

    private static class VPolygonReplicator extends ClosedShapeReplicator {
        private final LongPoint[] coords;

        VPolygonReplicator(VPolygon source){
            super(source);
            this.coords = source.getAbsoluteVertices();
        }

        public Glyph doCreateGlyph(){
            return new VPolygon(coords, 0, Color.BLACK);
        }
    }

    private static class VDiamondReplicator extends ClosedShapeReplicator {
        private final long size; //width = height

        VDiamondReplicator(VDiamond source){
            super(source);
            this.size = (long)source.getSize();
        }

        public Glyph doCreateGlyph(){
            return new VDiamond(0,0,0,size,Color.BLACK);
        }
    }

    private static class CompositeReplicator extends AbstractGlyphReplicator{
		private final GlyphAttributes baseAttr;
        private final ArrayList<GlyphReplicator> replicators;

        CompositeReplicator(Composite source){
            super(source);
			baseAttr = GlyphAttributes.fromGlyph(source);
            replicators = new ArrayList<GlyphReplicator>();
            for(Glyph glyph: source.peekAtChildren()){
                replicators.add(glyph.getReplicator());
            } 
        }
        
        public Glyph doCreateGlyph(){
            Composite retval = new Composite();
            for(GlyphReplicator rep: replicators){
                retval.addChild(rep.createGlyph());
            }
            return retval;
        }
    }
}

