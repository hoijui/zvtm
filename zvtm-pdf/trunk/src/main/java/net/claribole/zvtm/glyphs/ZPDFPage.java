/*   FILE: DPath.java
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.glyphs;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.ClosedShape;
import com.xerox.VTM.glyphs.RectangularShape;
import net.claribole.zvtm.glyphs.projection.RProjectedCoordsP;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/** Glyph encapsulating a PDFPage from <a href="https://pdf-renderer.dev.java.net/">SwingLabs' PDFRenderer</a>.
	*@author Emmanuel Pietriga
	*/

public class ZPDFPage extends ClosedShape implements RectangularShape {

	/** Page width in virtual space. */
	long vw;
	/** Page height in virtual space. */
	long vh;

    public void initCams(int nbCam){}

    public void addCamera(int verifIndex){}

    public void removeCamera(int index){}

    /** Get glyph's size (radius of bounding circle). */
    public float getSize(){/*XXX:TBW*/return 0;}

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
	
	public boolean fillsView(long w, long h, int camIndex){
		//XXX:TBW
		return false;		
	}
	
	public boolean coordInside(int x,int y,int camIndex){
		//XXX:TBW
		return false;
	}
	
	public void resetMouseIn(){
//		for (int i=0;i<pc.length;i++){
//			resetMouseIn(i);
//		}
	}

	public void resetMouseIn(int i){
//  	if (pc[i]!=null){pc[i].prevMouseIn=false;}
//  	borderColor = bColor;
	}

	public short mouseInOut(int x,int y,int camIndex){
		//XXX:TBW
		return Glyph.NO_CURSOR_EVENT;
	}
	
	public void project(Camera c, Dimension d){}

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){}

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){}

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){}

	public Object clone(){
		return null;
	}

}
