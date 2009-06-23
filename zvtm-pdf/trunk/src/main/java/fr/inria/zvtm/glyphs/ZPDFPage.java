/*   FILE: ZPDFPage.java
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
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

public class ZPDFPage extends ClosedShape implements RectangularShape {

	Image pageImage;

	/** Page width in virtual space. */
	long vw;
	/** Page height in virtual space. */
	long vh;
	/** Aspect ratio: width divided by height (read-only). */
    public float ar;

    /** For internal use. Made public for easier outside package subclassing. */
    public AffineTransform at;

    /** For internal use. Made public for easier outside package subclassing. */
    public RProjectedCoordsP[] pc;
	
	/** For internal use. Made public for easier outside package subclassing. */
    public boolean zoomSensitive = true;

    /** For internal use. Made public for easier outside package subclassing. */
    public float scaleFactor = 1.0f;
    
    /** For internal use. Made public for easier outside package subclassing. */
    public float trueCoef = 1.0f;

    /** Indicates when a border is drawn around the image (read-only).
     * One of DRAW_BORDER_*
     */
    public short drawBorder = VImage.DRAW_BORDER_ALWAYS;
    
	/** Instantiate a PDF page as a ZVTM glyph, rendered at a resolution that matches the default scale for that page.
	 *@param page the PDF page from pdf-renderer
	 */
	public ZPDFPage(long x, long y, int z, PDFPage page){
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
	public ZPDFPage(long x, long y, int z, PDFPage page, float detailFactor, double scale){
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
	public ZPDFPage(long x, long y, int z, PDFPage page, int w, int h, double scale){
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

	public void initCams(int nbCam){
		pc=new RProjectedCoordsP[nbCam];
		for (int i=0;i<nbCam;i++){
			pc[i]=new RProjectedCoordsP();
		}
	}

	public void addCamera(int verifIndex){
		if (pc!=null){
			if (verifIndex==pc.length){
				RProjectedCoordsP[] ta=pc;
				pc=new RProjectedCoordsP[ta.length+1];
				for (int i=0;i<ta.length;i++){
					pc[i]=ta[i];
				}
				pc[pc.length-1]=new RProjectedCoordsP();
			}
			else {System.err.println("ZPDFPage: Error while adding camera "+verifIndex);}
		}
		else {
			if (verifIndex==0){
				pc=new RProjectedCoordsP[1];
				pc[0]=new RProjectedCoordsP();
			}
			else {System.err.println("ZPDFPage: Error while adding camera "+verifIndex);}
		}
	}

	public void removeCamera(int index){
		pc[index]=null;
	}

    /** For internal use. */
    public void computeSize(){
        size=(float)Math.sqrt(Math.pow(vw,2)+Math.pow(vh,2));
    }

    /** Get glyph's size (radius of bounding circle). */
    public float getSize(){return size;}

    /** Set glyph's size by setting its bounding circle's radius.
     *@see #reSize(float factor)
     */
    public void sizeTo(float radius){/*XXX:TBW*/}

    /** Set glyph's size by multiplying its bounding circle radius by a factor. 
     *@see #sizeTo(float radius)
     */
    public void reSize(float factor){/*XXX:TBW*/}

    /** Get the glyph's orientation. */
    public float getOrient(){/*XXX:TBW*/return 0;}

    /** Set the glyph's absolute orientation.
     *@param angle in [0:2Pi[ 
     */
    public void orientTo(float angle){/*XXX:TBW*/}

	public void highlight(boolean b, Color selectedColor){}

	public void setWidth(long w){}

	public void setHeight(long h){}

	public long getWidth(){return vw;}

	public long getHeight(){return vh;}

	/** Set to false if the image should not be scaled according to camera's altitude. Its size can still be changed, but its apparent size will always be the same, no matter the camera's altitude.
		*@see #isZoomSensitive()
		*/
	public void setZoomSensitive(boolean b){
		if (zoomSensitive!=b){
			zoomSensitive=b;
			VirtualSpaceManager.INSTANCE.repaintNow();
		}
	}

	/** Indicates whether the image is scaled according to camera's altitude.
		*@see #setZoomSensitive(boolean b)
		*/
	public boolean isZoomSensitive(){
		return zoomSensitive;
	}

	/** Should a border be drawn around the bitmap image.
		*@param p one of DRAW_BORDER_*
		*/
	public void setDrawBorderPolicy(short p){
		if (drawBorder!=p){
			drawBorder=p;
			VirtualSpaceManager.INSTANCE.repaintNow();
		}
	}
	
	public boolean fillsView(long w,long h,int camIndex){
		//can contain transparent pixel (we have no way of knowing without analysing the image data -could be done when constructing the object or setting the image)
		return false; 
	}

	public boolean coordInside(int jpx, int jpy, int camIndex, long cvx, long cvy){
		if ((jpx>=(pc[camIndex].cx-pc[camIndex].cw)) && (jpx<=(pc[camIndex].cx+pc[camIndex].cw)) &&
		    (jpy>=(pc[camIndex].cy-pc[camIndex].ch)) && (jpy<=(pc[camIndex].cy+pc[camIndex].ch))){return true;}
		else {return false;}
	}

	public short mouseInOut(int jpx, int jpy, int camIndex, long cvx, long cvy){
		if (coordInside(jpx, jpy, camIndex, cvx, cvy)){
			//if the mouse is inside the glyph
			if (!pc[camIndex].prevMouseIn){
				//if it was not inside it last time, mouse has entered the glyph
				pc[camIndex].prevMouseIn=true;
				return Glyph.ENTERED_GLYPH;
			}
			else {
				//if it was inside last time, nothing has changed
				return Glyph.NO_CURSOR_EVENT;
			}
		}
		else{
			//if the mouse is not inside the glyph
			if (pc[camIndex].prevMouseIn){
				//if it was inside it last time, mouse has exited the glyph
				pc[camIndex].prevMouseIn=false;
				return Glyph.EXITED_GLYPH;
			}
			else {
				//if it was not inside last time, nothing has changed
				return Glyph.NO_CURSOR_EVENT;
			}
		}
	}

	public void resetMouseIn(){
		for (int i=0;i<pc.length;i++){
			resetMouseIn(i);
		}
	}

	public void resetMouseIn(int i){
		if (pc[i]!=null){pc[i].prevMouseIn=false;}
		borderColor = bColor;
	}
	
	public void project(Camera c, Dimension d){
		int i = c.getIndex();
		coef = (float)(c.focal/(c.focal+c.altitude));
		//find coordinates of object's geom center wrt to camera center and project
		//translate in JPanel coords
		pc[i].cx = (d.width/2)+Math.round((vx-c.posx)*coef);
		pc[i].cy = (d.height/2)-Math.round((vy-c.posy)*coef);
		//project width and height
		if (zoomSensitive){
			pc[i].cw = Math.round(vw*coef);
			pc[i].ch = Math.round(vh*coef);
		}
		else{
			pc[i].cw = (int)vw;
			pc[i].ch = (int)vh;
		}
	}

	public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
		int i = c.getIndex();
		coef = ((float)(c.focal/(c.focal+c.altitude))) * lensMag;
		//find coordinates of object's geom center wrt to camera center and project
		//translate in JPanel coords
		pc[i].lcx = lensWidth/2 + Math.round((vx-lensx)*coef);
		pc[i].lcy = lensHeight/2 - Math.round((vy-lensy)*coef);
		//project width and height
		if (zoomSensitive){
			pc[i].lcw = Math.round(vw*coef);
			pc[i].lch = Math.round(vh*coef);
		}
		else {
			pc[i].lcw = (int)vw;
			pc[i].lch = (int)vh;
		}
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
