/*   FILE: ZPDFPage.java
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZPDFPage.java 2112 2009-06-23 15:04:47Z epietrig $
 */

package fr.inria.zvtm.glyphs;

import java.awt.Image;
import java.awt.Rectangle;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.RectangularShape;
import fr.inria.zvtm.glyphs.projection.RProjectedCoordsP;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import com.sun.pdfview.PDFPage;

/** Glyph encapsulating a PDFPage from <a href="https://pdf-renderer.dev.java.net/">SwingLabs' PDFRenderer</a>.
 *@author Emmanuel Pietriga
 */

public class ZPDFPageImg extends ZPDFPage {

	Image pageImage;

    /** For internal use. Made public for easier outside package subclassing. */
    public AffineTransform at;
    
	/** Instantiate a PDF page as a ZVTM glyph, rendered at a resolution that matches the default scale for that page.
	 *@param page the PDF page from pdf-renderer
	 */
	public ZPDFPageImg(long x, long y, int z, PDFPage page){
		vx = x;
		vy = y;
		vz = z;
		// get the width and height for the doc at the default zoom 
		Rectangle rect = new Rectangle(0, 0, (int)page.getBBox().getWidth(), (int)page.getBBox().getHeight());
		// generate the image
		pageImage = page.getImage(rect.width, rect.height,
		 	rect,   // clip rect
			null,   // null for the ImageObserver
			true,   // fill background with white
			true);  // block until drawing is done
		vw = Math.round(pageImage.getWidth(null)/2.0);
		vh = Math.round(pageImage.getHeight(null)/2.0);
		if (vw==0 && vh==0){ar = 1.0f;}
		else {ar = (float)vw/(float)vh;}
		computeSize();
		orient = 0;
	}

	/** Instantiate a PDF page as a ZVTM glyph, rendered at a resolution that matches the default scale for that page.
	 *@param page the PDF page from pdf-renderer
	 *@param detailFactor multiplication factor applied to compute the actual width and height of the bitmap image in which to render the page, taking the default rendering scale as a basis
     *@param scale scaleFactor in virtual space w.r.t specified image size (default is 1.0)
	 */
	public ZPDFPageImg(long x, long y, int z, PDFPage page, float detailFactor, double scale){
		vx = x;
		vy = y;
		vz = z;
		// get the width and height for the doc at the default zoom 
		Rectangle rect = new Rectangle(0, 0, (int)(detailFactor*page.getBBox().getWidth()), (int)(detailFactor*page.getBBox().getHeight()));
		// generate the image
		pageImage = page.getImage((int)(detailFactor*rect.width), (int)(detailFactor*rect.height),
		 	rect,   // clip rect
			null,   // null for the ImageObserver
			true,   // fill background with white
			true);  // block until drawing is done
		vw = Math.round(pageImage.getWidth(null)/2.0);
		vh = Math.round(pageImage.getHeight(null)/2.0);
		if (vw==0 && vh==0){ar = 1.0f;}
		else {ar = (float)vw/(float)vh;}
		computeSize();
		orient = 0;		
		scaleFactor = (float)scale;
	}

	/** Instantiate a PDF page as a ZVTM glyph, rendered at a resolution that matches the default scale for that page.
	 *@param page the PDF page from pdf-renderer
	 *@param w width of bitmap image in which to render the page
	 *@param h height of bitmap image in which to render the page
     *@param scale scaleFactor in virtual space w.r.t specified image size (default is 1.0)
	 */
	public ZPDFPageImg(long x, long y, int z, PDFPage page, int w, int h, double scale){
		vx = x;
		vy = y;
		vz = z;
		// get the width and height for the doc at the default zoom
		//XXX:TBW would probably be a good idea to throw some kind of warning if 
		//        w and h are smaller than default scale ? not sure... depends on how page.getImage() behaves in this case
		Rectangle rect = new Rectangle(0, 0, w, h);
		// generate the image
		pageImage = page.getImage(rect.width, rect.height,
		 	rect,   // clip rect
			null,   // null for the ImageObserver
			true,   // fill background with white
			true);  // block until drawing is done
		vw = Math.round(pageImage.getWidth(null)/2.0);
		vh = Math.round(pageImage.getHeight(null)/2.0);
		if (vw==0 && vh==0){ar = 1.0f;}
		else {ar = (float)vw/(float)vh;}
		computeSize();
		orient = 0;		
		scaleFactor = (float)scale;		
	}

	public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
		if ((pc[i].cw>1) && (pc[i].ch>1)){
			if (zoomSensitive){
				trueCoef = scaleFactor*coef;
			}
			else{
				trueCoef = scaleFactor;
			}
			//a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
			if (Math.abs(trueCoef-1.0f)<0.01f){trueCoef=1.0f;}
			if (trueCoef!=1.0f){
				// translate
				at = AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch);
				g.setTransform(at);
				// rescale and draw
				g.drawImage(pageImage,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
				g.setTransform(stdT);
				if (drawBorder==1){
					if (pc[i].prevMouseIn){
						g.setColor(borderColor);
						g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
					}
				}
				else if (drawBorder==2){
					g.setColor(borderColor);
					g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
				}
			}
			else {
				g.drawImage(pageImage, dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,null);
				if (drawBorder == 1){
					if (pc[i].prevMouseIn){
						g.setColor(borderColor);
						g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
					}
				}
				else if (drawBorder == 2){
					g.setColor(borderColor);
					g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
				}
			}
		}
		else {
			g.setColor(this.borderColor);
			g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
		}
	}

	public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
		if ((pc[i].lcw > 1) && (pc[i].lch > 1)){
			if (zoomSensitive){trueCoef=scaleFactor*coef;}
			else {trueCoef=scaleFactor;}
			if (Math.abs(trueCoef-1.0f)<0.01f){
				//a threshold greater than 0.01 causes jolts when zooming-unzooming around the 1.0 scale region
				trueCoef=1.0f;
			}
			if (trueCoef!=1.0f){
				g.setTransform(AffineTransform.getTranslateInstance(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch));
				g.drawImage(pageImage, AffineTransform.getScaleInstance(trueCoef,trueCoef), null);
				g.setTransform(stdT);
				if (drawBorder==1){
					if (pc[i].prevMouseIn){
						g.setColor(borderColor);
						g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
					}
				}
				else if (drawBorder==2){
					g.setColor(borderColor);
					g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
				}
			}
			else {
				g.drawImage(pageImage, dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, null);
				if (drawBorder == 1){
					if (pc[i].prevMouseIn){
						g.setColor(borderColor);
						g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
					}
				}
				else if (drawBorder == 2){
					g.setColor(borderColor);
					g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
				}
			}
		}
		else {
			g.setColor(this.borderColor);
			g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
		}
	}

	public Object clone(){
		return null;
	}

}
