/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.net.URL;
import javax.swing.ImageIcon;
import java.util.ArrayList;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.SICircle;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.Composite;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.MultilineText;
import fr.inria.zvtm.glyphs.PRectangle;
import fr.inria.zvtm.glyphs.SIRectangle;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VEllipse;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VPoint;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VRectangleOr;
import fr.inria.zvtm.glyphs.VRing;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VTextOr;

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
            glyph.setReplicated(true);
		}

	//advise VirtualSpace.addGlyph
	after(Glyph glyph, VirtualSpace virtualSpace) returning: 
		glyphAdd(glyph, virtualSpace) &&
        if(glyph.isReplicated()) &&
		!cflowbelow(glyphAdd(Glyph, VirtualSpace)){
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
			// We set the target glyph to non-replicated since
			// we replicate glyphs when they are added to the virtual space
			// (and *destroy* the slave glyphs when they are removed from the 
			// virtual space).
			// Hence, we should not propagate modifications to any glyph that has
			// been removed from a space, because it does not have a slave counterpart.
			glyph.setReplicated(false);
		}
	
	//overrides for various Glyph subclasses
	@Override GlyphReplicator VRectangle.getReplicator(){
		return new VRectangleReplicator(this);
	}

    @Override GlyphReplicator PRectangle.getReplicator(){
        return new PRectangleReplicator(this);
    }
	
	@Override GlyphReplicator VRectangleOr.getReplicator(){
		return new VRectangleOrReplicator(this);
	}

	@Override GlyphReplicator VCircle.getReplicator(){
		return new VCircleReplicator(this);
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

	@Override GlyphReplicator SIRectangle.getReplicator(){
		return new SIRectangleReplicator(this);
	}

	@Override GlyphReplicator SICircle.getReplicator(){
		return new SICircleReplicator(this);
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

    @Override GlyphReplicator VPolygon.getReplicator(){
        return new VPolygonReplicator(this);
    }

    @Override GlyphReplicator Composite.getReplicator(){
        return new CompositeReplicator(this);
    }

    @Override GlyphReplicator MultilineText.getReplicator(){
        return new MultilineTextReplicator(this);
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

	static abstract class AbstractGlyphReplicator implements GlyphReplicator {
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

	static class VRectangleReplicator extends ClosedShapeReplicator {
		protected final double width;
		protected final double height;

		VRectangleReplicator(VRectangle source){
			super(source);
			this.width = source.getWidth();
			this.height = source.getHeight();
		}

		public Glyph doCreateGlyph(){
			return new VRectangle(0d,0d,0,width,height,Color.BLACK);
		}

		@Override public String toString(){
			return "VRectangleReplicator, width=" + width
				+ ", height=" + height;
		}
	}

    private static class PRectangleReplicator extends VRectangleReplicator {
        protected final Paint paint;

        PRectangleReplicator(PRectangle source){
            super(source);
            paint = Paints.wrapPaint(source.getPaint());
        }

        public Glyph doCreateGlyph(){
            PRectangle retval = new PRectangle(0d,0d,0,width,height,paint);
            return retval;
        }
    }
	
	private static class VRectangleOrReplicator extends VRectangleReplicator {

		VRectangleOrReplicator(VRectangleOr source){
			super(source);
		}

		public Glyph doCreateGlyph(){
			return new VRectangleOr(0d,0d,0,width,height,Color.BLACK,0);
		}

		@Override public String toString(){
			return "VRectangleOrReplicator, width=" + width
				+ ", height=" + height;
		}
	}

	private static class VCircleReplicator extends ClosedShapeReplicator {
		private final double radius;

		VCircleReplicator(VCircle source){
			super(source);
			this.radius = source.getSize();
		}

		public Glyph doCreateGlyph(){
			return new VCircle(0d,0d,0,radius,Color.BLACK);
		}

		@Override public String toString(){
			return "VCircleReplicator, radius=" + radius;
		}
	}
	
	private static class VSegmentReplicator extends AbstractGlyphReplicator {
		private final double halfWidth;
		private final double halfHeight;

		VSegmentReplicator(VSegment source){
			super(source);
			this.halfWidth = source.getWidth();
			this.halfHeight = source.getHeight();
		}

		public Glyph doCreateGlyph(){
			return new VSegment(0d,0d,halfWidth,halfHeight,0,Color.BLACK);
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
			return new VText(0d,0d,0,Color.BLACK,text,textAnchor,scaleFactor);
		}
	}

    private static class MultilineTextReplicator extends VTextReplicator {
        protected final double widthConstraint;
        protected final double heightConstraint;

        MultilineTextReplicator(MultilineText source){
            super(source);
            this.widthConstraint = source.getWidthConstraint();
            this.heightConstraint = source.getHeightConstraint();
        }

        public Glyph doCreateGlyph(){
            MultilineText retval =  new MultilineText(text);
            retval.setWidthConstraint(widthConstraint);
            retval.setHeightConstraint(heightConstraint);
            return retval;
        }
    }

    private static class VTextOrReplicator extends VTextReplicator{
        VTextOrReplicator(VTextOr source){
            super(source);
        }

        @Override public Glyph doCreateGlyph(){
            return new VTextOr(0d,0d,0,Color.BLACK,text,0f,textAnchor,scaleFactor);
        }
    }

	private static class SIRectangleReplicator extends ClosedShapeReplicator {
		private final double halfWidth;
		private final double halfHeight;

		SIRectangleReplicator(SIRectangle source){
			super(source);
			this.halfWidth = source.getWidth();
			this.halfHeight = source.getHeight();
		}

		public Glyph doCreateGlyph(){
			return new SIRectangle(0d,0d,0,halfWidth,halfHeight,Color.BLACK);
		}
	}

	private static class SICircleReplicator extends ClosedShapeReplicator {
		private final double radius;
		
		SICircleReplicator(SICircle source){
			super(source);
			this.radius = source.getSize();
		}

		public Glyph doCreateGlyph(){
			return new SICircle(0d,0d,0,radius,Color.BLACK);
		}
	}

    static class VImageReplicator extends ClosedShapeReplicator {
        //Note that serialized ImageIcon instances (as most AWT objects)
        //are not guaranteed to be portable across toolkits.
        //If this becomes a practical concern, then another serialization 
        //mechanism is to be used.
        protected final ImageIcon serImage; 
        protected final double scaleFactor;

        VImageReplicator(VImage source){
            super(source);
            this.serImage = new ImageIcon(source.getImage());
            this.scaleFactor = source.scaleFactor;
        }

        public Glyph doCreateGlyph(){
            return new VImage(0d,0d,0,serImage.getImage(), scaleFactor);
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
			return new ClusteredImage(0d,0d,0,imageLocation,scaleFactor);
		}
	}

    static class DPathReplicator extends AbstractGlyphReplicator {
		//note that GeneralPath is only serializable
		//starting with Java 1.6
		protected final GeneralPath path;

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
        private final double sx;
        private final double sy;
        
        VEllipseReplicator(VEllipse source){
            super(source);
            this.sx = source.getWidth();
            this.sy = source.getHeight();
        }

        public Glyph doCreateGlyph(){
            return new VEllipse(0d,0d,0,sx,sy,Color.BLACK);
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
        private final double arcDiameter;
        private final double arcAngle;
        private final float irRad; //inner ring radius (%outer ring radius)
        private final double sliceOrient;

        VRingReplicator(VRing source){
            super(source);
            this.arcDiameter = source.getSize();
            this.arcAngle = source.angle;
            this.irRad = source.getInnerRatio();
            this.sliceOrient = source.orient;
        }

        public Glyph doCreateGlyph(){
            return new VRing(0d,0d,0,arcDiameter,arcAngle,irRad,sliceOrient,
                    Color.BLACK, Color.BLACK);
        }
    }

    private static class VPolygonReplicator extends ClosedShapeReplicator {
        private final Point2D.Double[] coords;

        VPolygonReplicator(VPolygon source){
            super(source);
            this.coords = source.getAbsoluteVertices();
        }

        public Glyph doCreateGlyph(){
            return new VPolygon(coords, 0, Color.BLACK);
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

