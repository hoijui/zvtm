package fr.inria.zvtm.cluster;

import java.awt.Point;
import java.awt.image.BufferedImage;

import fr.inria.zvtm.glyphs.Glyph;

import fr.inria.zvtm.common.compositor.MetisseWindow;

public aspect MetisseGlyphCreation {
	@Override GlyphReplicator MetisseWindow.getReplicator(){
		return new MetisseWindowReplicator(this);
	}

	private static class MetisseWindowReplicator extends GlyphCreation.VImageReplicator {
		//Note that serialized ImageIcon instances (as most AWT objects)
		//are not guaranteed to be portable across toolkits.
		//If this becomes a practical concern, then another serialization 
		//mechanism is to be used.
		private final double x_offset;
		private final double y_offset;
		private final double xParentOffset;
		private final double yParentOffset;
		private final double parentScaleFactor;
		private final double x;//coordinates in the x server
		private final double y;
		private final int windowNumber;
		private final double width;
		private final double height;
		private final double rootxorigin;
		private final double rootyorigin;
		private final double rootw;
		private final double rooth;
		private final boolean isroot;
		private final boolean isResizing;
		public final boolean isRescaling;

		MetisseWindowReplicator(MetisseWindow source){
			super(source);
			this.x_offset = source.getX_offset();
			this.y_offset = source.getY_offset();
			this.xParentOffset = source.getXparentOffset();
			this.yParentOffset = source.getYparentOffset();
			this.parentScaleFactor = source.getParentScaleFactor();
			this.windowNumber = source.getId();
			this.height = source.getHeight();
			this.width = source.getWidth();
			this.x = source.getX();
			this.y = source.getY();
			this.isroot = source.isRoot();
			this.isResizing = source.isResizing();
			this.isRescaling = source.isRescaling();
			if(!isroot){
				this.rootxorigin = source.getRootLocation().x;
				this.rootyorigin = source.getRootLocation().y;
				this.rootw = source.getRootSize().x;
				this.rooth = source.getRootSize().y;
			}
			else{
				this.rootxorigin = 0;
				this.rootyorigin = 0;
				this.rootw = 0;
				this.rooth = 0;
			}	

		}

		public Glyph doCreateGlyph(){
			MetisseWindow mw = new MetisseWindow(false,windowNumber,0,0,(int)width,(int)height);
			BufferedImage bImg = (BufferedImage)(mw.getImage());
			bImg.createGraphics().drawImage(serImage.getImage(), null, null);
			mw.setX_offset(x_offset);
			mw.setY_offset(y_offset);
			mw.setXparentOffset(xParentOffset);
			mw.setYparentOffset(yParentOffset);
			mw.setParentScaleFactor(parentScaleFactor);
			mw.setX(x);
			mw.setY(y);
			mw.setHeight(height);
			mw.setWidth(width);
			mw.setIsroot(isroot);
			mw.setResizing(isResizing);
			mw.setRescaling(isRescaling);
			if(!isroot){
				mw.setRootSize(new Point.Double(rootw,rooth));
				mw.setRootLocation(new Point.Double(rootxorigin,rootyorigin));
			}
			mw.setScaleFactor(scaleFactor);

			return mw;
		}
	}
}

