/*   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2013. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.glyphs;

import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Camera;
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

/** Glyph encapsulating a PDF document from <a href="http://www.icepdf.org/">ICEpdf</a>.
 * ICEpdf API documentation available at <a href="http://www.icepdf.org/docs/v4_0_0/core/javadocs/index.html">http://www.icepdf.org/docs/v4_0_0/core/javadocs/index.html</a>.
 * ICEpdf developer's guide available at <a href="http://wiki.icefaces.org/display/PDF/ICEpdf+Developer's+Guide">http://wiki.icefaces.org/display/PDF/ICEpdf+Developer's+Guide</a>.
 *@author Emmanuel Pietriga
 */

public class BrowsableDocument extends IcePDFPageImg {

    Document doc;
    int currentPage;
    float detailFactor = 1f;

    /** Instantiate a PDF page as a ZVTM glyph, rendered at a resolution that matches the default scale for that page.
	 *@param pdfDoc the PDF document from ICEpdf
	 *@param currentPage page number starting from 0 (for page 1)
	 */
	public BrowsableDocument(Document pdfDoc, int currentPage){
		this(0, 0, 0, pdfDoc, currentPage, 1f, 1f);
	}

    /** Instantiate a PDF page as a ZVTM glyph, rendered at a resolution that matches the default scale for that page.
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
	 *@param pdfDoc the PDF document from ICEpdf
	 *@param currentPage page number starting from 0 (for page 1)
	 */
	public BrowsableDocument(double x, double y, int z, Document pdfDoc, int currentPage){
		this(x, y, z, pdfDoc, currentPage, 1f, 1f);
	}

	/** Instantiate a PDF page as a ZVTM glyph, rendered at a resolution that matches the default scale for that page multiplied by detailFactor.
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
	 *@param pdfDoc the PDF document from ICEpdf
	 *@param currentPage page number starting from 0 (for page 1)
	 *@param detailFactor Multiplication factor applied to compute the actual width and height of the bitmap image in which to render the page, taking the default rendering scale as a basis (1.0f).
	                      This has a direct impact of the PDF page rendering quality. &gt; 1.0 will create higher quality renderings, &lt; will create lower quality renderings.
     *@param scaleFactor glyph size multiplication factor in virtual space w.r.t specified image size (default is 1.0). This has not impact on the PDF page rendering quality (a posteriori rescaling in ZVTM).
	 */
	public BrowsableDocument(double x, double y, int z, Document pdfDoc, int currentPage, float detailFactor, double scaleFactor){
        super(x, y, z, scaleFactor);
        this.doc = pdfDoc;
        this.detailFactor = detailFactor;
        setPage(currentPage);
	}

    public void setPage(int pageNumber){
        flush();
        this.currentPage = pageNumber;
        synchronized(doc){
            setPageImage((BufferedImage)doc.getPageImage(this.currentPage, GraphicsRenderingHints.SCREEN, Page.BOUNDARY_CROPBOX, 0f, detailFactor));
        }
    }

    /** Get the underlying IcePDF document. */
    public Document getDocument(){
        return this.doc;
    }

    /** Get the number (starting from 0 for page 1) of the page currently displayed. */
    public int getCurrentPageNumber(){
        return this.currentPage;
    }

    /** Cloning this PDF page glyph. Uses the same bitmap resource as the original.
     *
     */
	@Override
	public Object clone(){
	    BrowsableDocument res = new BrowsableDocument(vx, vy, vz, doc, currentPage, detailFactor, scaleFactor);
		return res;
	}

}
