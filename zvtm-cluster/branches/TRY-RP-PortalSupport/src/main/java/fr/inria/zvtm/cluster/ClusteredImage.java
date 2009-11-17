/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import java.net.URL;

import java.io.IOException;

import javax.imageio.ImageIO;

import fr.inria.zvtm.engine.Camera;
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
public class ClusteredImage extends VImage {
	private final URL location;
	private static final Image DEFAULT_IMAGE;

	static { 
		DEFAULT_IMAGE = new BufferedImage(10,10,BufferedImage.TYPE_INT_ARGB);
	}

	public ClusteredImage(long x, long y, int z, URL location, double scale){
		super(x,y,z,DEFAULT_IMAGE,scale);

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
		setImage(img);
	}

	public final URL getImageLocation(){ return location; }
	public final double getScale(){ return scaleFactor; }
}

