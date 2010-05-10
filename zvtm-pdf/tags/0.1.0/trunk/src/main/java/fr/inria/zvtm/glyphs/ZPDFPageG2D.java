/*   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.glyphs;

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
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

/** Glyph encapsulating a PDFPage from <a href="https://pdf-renderer.dev.java.net/">SwingLabs' PDFRenderer</a>.
 *@author Emmanuel Pietriga
 */

public class ZPDFPageG2D extends ZPDFPage {
    
    PDFPage page;
    PDFRenderer renderer;
    Rectangle imageBounds;

	/** Instantiate a PDF page as a ZVTM glyph, rendered at a resolution that matches the default scale for that page.
	 *@param page the PDF page from pdf-renderer
     */
	public ZPDFPageG2D(long x, long y, int z, PDFPage page){
        this(x, y, z, page, 1);
    }

	/** Instantiate a PDF page as a ZVTM glyph, rendered at a resolution that matches the default scale for that page.
	 *@param page the PDF page from pdf-renderer
     */
	public ZPDFPageG2D(long x, long y, int z, PDFPage page, float detailFactor){
	    this.page = page;
		vx = x;
		vy = y;
		vz = z;
		imageBounds = new Rectangle(0, 0, (int)Math.round(detailFactor*page.getBBox().getWidth()), (int)Math.round(detailFactor*page.getBBox().getHeight()));
		vw = Math.round(imageBounds.width/2.0);
		vh = Math.round(imageBounds.height/2.0);
		if (vw==0 && vh==0){ar = 1.0f;}
		else {ar = (float)vw/(float)vh;}
		computeSize();
		orient = 0;
	}
	
	public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	    if (alphaC != null){
            // translucent
            g.setComposite(alphaC);
        }
	    if (renderer == null){
	        renderer = new PDFRenderer(page, g, imageBounds, null, Color.RED);
	        try {page.waitForFinish();}
	        catch(InterruptedException ex){ex.printStackTrace();}
	    }
        if ((pc[i].cw>1) && (pc[i].ch>1)){
            // translate
            AffineTransform at = AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw, dy+pc[i].cy-pc[i].ch);
            at.concatenate(AffineTransform.getScaleInstance(coef, coef));
            g.setTransform(at);
            renderer.run();
            g.setTransform(stdT);
            if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                g.setColor(borderColor);
                g.drawRect(dx+pc[i].cx-pc[i].cw, dy+pc[i].cy-pc[i].ch, 2*pc[i].cw-1, 2*pc[i].ch-1);
            }
        }
		else {
			g.setColor(this.borderColor);
			g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
		}
		if (alphaC != null){
            // translucent - restore
            g.setComposite(acO);
        }
	}

	public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	    /*TBW*/
	}

	public Object clone(){
		return null;
	}
	
	public void flush(){
	    if (renderer != null){
	        renderer.stop();
	        renderer = null;
	    }
	}
	
}
