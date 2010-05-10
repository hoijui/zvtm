/*   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008-2010. All Rights Reserved
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

/** Glyph encapsulating a PDF Page.
 *@author Emmanuel Pietriga
 */

public abstract class ZPDFPage extends ClosedShape implements RectangularShape {

	/** Page width in virtual space. */
	long vw;
	/** Page height in virtual space. */
	long vh;
	/** Aspect ratio: width divided by height (read-only). */
    public float ar;

    /** For internal use. Made public for easier outside package subclassing. */
    public RProjectedCoordsP[] pc;
	
	/** For internal use. Made public for easier outside package subclassing. */
    public boolean zoomSensitive = true;

    /** For internal use. Made public for easier outside package subclassing. */
    public float scaleFactor = 1.0f;
    
    /** Indicates when a border is drawn around the image (read-only).
     * One of DRAW_BORDER_*
     */
    public short drawBorder = VImage.DRAW_BORDER_ALWAYS;
    
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
	
	public abstract void flush();

}
