package fr.inria.zvtm.clustering;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import java.net.URL;

import java.io.IOException;

import javax.imageio.ImageIO;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;

/**
 * Clustered image.
 * Images are a bit different from other glyphs, in that
 * we do not want to serialize images and send them over the
 * wire using the same "delta" protocol. Rather, we will
 * share metadata and trust slaves to create a matching image
 * on the other side. We expect that images will be shared 
 * using a network drive or a similar solution.
 */
public class ClusteredImage extends Glyph {
	private final VImage image;
	private final URL location;
	//private static final Image DEFAULT_IMAGE;

	static { /* init default (not found or error) image */ }

	public ClusteredImage(long x, long y, int z, URL location, double scale){
		this.location = location;
		Image img = null;

		try{
			img = ImageIO.read(location);
		} catch (IOException e){
			System.out.println("Could not open image, using default");
		}

		if(img == null){
			//img = DEFAULT_IMAGE;
			throw new Error("Could not create image");
		}
		image = new VImage(x,y,z,img,scale);
	}

	public final URL getImageLocation(){ return location; }
	public final double getScale(){ return image.scaleFactor; }

	/* delegate everything else to VImage instance */

	//TODO: is it possible to express this delegation 
	//concisely using AOP?
	
	@Override public void addCamera(int verifIndex){
		image.addCamera(verifIndex);
	}
	
	@Override public Object clone(){
		throw new Error("not implemented");
	}

	@Override public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
		return image.coordInside(jpx, jpy, camIndex, cvx, cvy);
	}

	@Override public void draw(Graphics2D g, int vW, int vH, int i, Stroke stdS, AffineTransform stdT, int dx, int dy){
		image.draw(g, vW, vH, i, stdS, stdT, dx, dy);
	}

	@Override public void drawForLens(Graphics2D g, int vW, int vH, int i, Stroke stdS, AffineTransform stdT, int dx, int dy){
		image.drawForLens(g, vW, vH, i, stdS, stdT, dx, dy);
	}

	@Override public boolean fillsView(long w, long h, int camIndex){
		return image.fillsView(w,h,camIndex);
	}

	@Override public float getOrient(){
		return image.getOrient();
	}

	@Override public float getSize(){
		return image.getSize();
	}

	@Override public void highlight(boolean b, Color selectedColor){
		image.highlight(b, selectedColor);
	}

	@Override public void initCams(int nbCam){
		image.initCams(nbCam);
	}

	@Override public short mouseInOut(int jpx, int jpy, int camIndex, long cvx, long cvy){
		return image.mouseInOut(jpx, jpy, camIndex, cvx, cvy);
	}

	@Override public void orientTo(float angle){
		image.orientTo(angle);
	}

	@Override public void project(Camera c, Dimension d){
		image.project(c,d);
	}

	@Override public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
		image.projectForLens(c, lensWidth, lensHeight, lensMag, lensx, lensy);
	}

	@Override public void removeCamera(int index){
		image.removeCamera(index);
	}

	@Override public void resetMouseIn(){
		image.resetMouseIn();
	}

	@Override public void resetMouseIn(int i){
		image.resetMouseIn(i);
	}

	@Override public void reSize(float factor){
		image.reSize(factor);
	}

	@Override public void sizeTo(float radius){
		image.sizeTo(radius);
	}
}

