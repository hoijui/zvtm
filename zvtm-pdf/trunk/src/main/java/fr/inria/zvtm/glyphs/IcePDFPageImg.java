/*   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package fr.inria.zvtm.glyphs;

import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.RenderingHints;

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

import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;

/** Glyph encapsulating a PDF page from <a href="http://www.icepdf.org/">ICEpdf</a>.
 * ICEpdf API documentation available at <a href="http://www.icepdf.org/docs/v4_0_0/core/javadocs/index.html">http://www.icepdf.org/docs/v4_0_0/core/javadocs/index.html</a>.
 * ICEpdf developer's guide available at <a href="http://wiki.icefaces.org/display/PDF/ICEpdf+Developer's+Guide">http://wiki.icefaces.org/display/PDF/ICEpdf+Developer's+Guide</a>.
 *@author Emmanuel Pietriga
 */

public class IcePDFPageImg extends ZPDFPage {

	BufferedImage pageImage;

    /** For internal use. Made public for easier outside package subclassing. */
    public float trueCoef = 1.0f;

    /** For internal use. Made public for easier outside package subclassing. */
    public AffineTransform at;
    
	/** Instantiate a PDF page as a ZVTM glyph, rendered at a resolution that matches the default scale for that page.
	 *@param pdfDoc the PDF document from ICEpdf
	 *@param pageNumber page number starting from 0 (for page 1)
	 *@param detailFactor multiplication factor applied to compute the actual width and height of the bitmap image in which to render the page, taking the default rendering scale as a basis
     *@param scale scaleFactor in virtual space w.r.t specified image size (default is 1.0)
	 */
	public IcePDFPageImg(long x, long y, int z, Document pdfDoc, int pageNumber, float detailFactor, double scale){
		vx = x;
		vy = y;
		vz = z;
        pageImage = (BufferedImage)pdfDoc.getPageImage(pageNumber, GraphicsRenderingHints.SCREEN, Page.BOUNDARY_CROPBOX, 0f, (float)scale);
		vw = Math.round(detailFactor*pageImage.getWidth()*scale/2.0);
		vh = Math.round(detailFactor*pageImage.getHeight()*scale/2.0);
		if (vw==0 && vh==0){ar = 1.0f;}
		else {ar = (float)vw/(float)vh;}
		computeSize();
		orient = 0;
		scaleFactor = (float)scale;
	}
	
	/** For internal use. Made public for easier outside package subclassing. */
    public Object interpolationMethod = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
    
    /** Specify how image should be interpolated when drawn at a scale different from its original scale.
        *@param im one of java.awt.RenderingHints.{VALUE_INTERPOLATION_NEAREST_NEIGHBOR,VALUE_INTERPOLATION_BILINEAR,VALUE_INTERPOLATION_BICUBIC} ; default is VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        */
    public void setInterpolationMethod(Object im){
        interpolationMethod = im;
    }
    
    /** Get information about how image should be interpolated when drawn at a scale different from its original scale.
        *@return one of java.awt.RenderingHints.{VALUE_INTERPOLATION_NEAREST_NEIGHBOR,VALUE_INTERPOLATION_BILINEAR,VALUE_INTERPOLATION_BICUBIC} ; default is VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        */
    public Object getInterpolationMethod(){
        return interpolationMethod;
    }

	public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	    if (alphaC != null && alphaC.getAlpha()==0){return;}
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
				at = AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw, dy+pc[i].cy-pc[i].ch);
				g.setTransform(at);
				if (alphaC != null){
                    // translucent
                    g.setComposite(alphaC);
    				// rescale and draw				
    				if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(pageImage,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(pageImage,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                    }
    				g.setTransform(stdT);
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawRect(dx+pc[i].cx-pc[i].cw, dy+pc[i].cy-pc[i].ch, 2*pc[i].cw-1, 2*pc[i].ch-1);
                    }
                    g.setComposite(acO);
                }
                else {
                    // opaque
    				// rescale and draw				
    				if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(pageImage,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(pageImage,AffineTransform.getScaleInstance(trueCoef,trueCoef),null);
                    }
    				g.setTransform(stdT);
                    if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
                        g.setColor(borderColor);
                        g.drawRect(dx+pc[i].cx-pc[i].cw, dy+pc[i].cy-pc[i].ch, 2*pc[i].cw-1, 2*pc[i].ch-1);
                    }
                }
            }
			else {
			    if (alphaC != null){
                    // translucent
                    g.setComposite(alphaC);
    			    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(pageImage, dx+pc[i].cx-pc[i].cw, dy+pc[i].cy-pc[i].ch, null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(pageImage, dx+pc[i].cx-pc[i].cw, dy+pc[i].cy-pc[i].ch, null);
                    }
    				if ((drawBorder == 1 && pc[i].prevMouseIn) || drawBorder == 2){
    					g.setColor(borderColor);
    					g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
    				}
                    g.setComposite(acO);
                }
                else {
                    // opaque
    			    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(pageImage, dx+pc[i].cx-pc[i].cw, dy+pc[i].cy-pc[i].ch, null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(pageImage, dx+pc[i].cx-pc[i].cw, dy+pc[i].cy-pc[i].ch, null);
                    }
    				if ((drawBorder == 1 && pc[i].prevMouseIn) || drawBorder == 2){
    					g.setColor(borderColor);
    					g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
    				}
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
				
				if (alphaC != null){
                    // translucent
                    g.setComposite(alphaC);
                	if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(pageImage, AffineTransform.getScaleInstance(trueCoef,trueCoef), null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(pageImage, AffineTransform.getScaleInstance(trueCoef,trueCoef), null);
                    }
    				g.setTransform(stdT);
    				if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
    					g.setColor(borderColor);
    					g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
    				}
                    g.setComposite(acO);
                }
                else {
                 	if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
                        g.drawImage(pageImage, AffineTransform.getScaleInstance(trueCoef,trueCoef), null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
                        g.drawImage(pageImage, AffineTransform.getScaleInstance(trueCoef,trueCoef), null);
                    }
    				g.setTransform(stdT);
    				if ((drawBorder==1 && pc[i].prevMouseIn) || drawBorder==2){
    					g.setColor(borderColor);
    					g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
    				}
                }				
			}
			else {
			    if (alphaC != null){
                    // translucent
                    g.setComposite(alphaC);
                    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
        				g.drawImage(pageImage, dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
        				g.drawImage(pageImage, dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, null);
                    }
    				if ((drawBorder == 1 && pc[i].prevMouseIn) || drawBorder == 2){
    					g.setColor(borderColor);
    					g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
    				}
                    g.setComposite(acO);
                }
                else {
    			    if (interpolationMethod != RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR){
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, interpolationMethod);
        				g.drawImage(pageImage, dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, null);
                        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    }
                    else {
        				g.drawImage(pageImage, dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, null);
                    }
    				if ((drawBorder == 1 && pc[i].prevMouseIn) || drawBorder == 2){
    					g.setColor(borderColor);
    					g.drawRect(dx+pc[i].lcx-pc[i].lcw, dy+pc[i].lcy-pc[i].lch, 2*pc[i].lcw-1, 2*pc[i].lch-1);
    				}                    
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

	public void flush(){
	    if (pageImage != null){
	        pageImage.flush();
	        pageImage = null;
	    }
	}
	
	public BufferedImage getpageImage() {
		return pageImage;
	}
	
}
