/*   FILE: AnimationDemo.java
 *   DATE OF CREATION:   Thu Mar 29 19:33 2007
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.glyphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.AlphaComposite;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VPath;
import net.claribole.zvtm.glyphs.projection.ProjectedCoords;
import net.claribole.zvtm.lens.Lens;

/**
 * Dynamic Path.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VPath
 *@see net.claribole.zvtm.glyphs.VPathST
 *@see com.xerox.VTM.glyphs.VQdCurve
 *@see com.xerox.VTM.glyphs.VCbCurve
 *@see com.xerox.VTM.glyphs.VSegment
 *@see com.xerox.VTM.glyphs.VSegmentST
 *@see com.xerox.VTM.engine.VCursor#intersectsVPath(VPath p)
 */

public class DPath extends Glyph {

    /** For internal use. Dot not tamper with. Made public for outside package subclassing. */
    public ProjectedCoords[] pc;

    public void initCams(int nbCam){
	pc = new ProjectedCoords[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i] = new ProjectedCoords();
	}
    }

    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjectedCoords[] ta = pc;
		pc = new ProjectedCoords[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i] = ta[i];
		}
		pc[pc.length-1] = new ProjectedCoords();
	    }
	    else {System.err.println("VPath:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc = new ProjectedCoords[1];
		pc[0] = new ProjectedCoords();
	    }
	    else {System.err.println("VPath:Error while adding camera "+verifIndex);}
	}
    }

    public void removeCamera(int index){
	pc[index] = null;
    }

    public void resetMouseIn(){
	for (int i=0;i<pc.length;i++){
	    resetMouseIn(i);
	}
    }

    public void resetMouseIn(int i){
	if (pc[i]!=null){pc[i].prevMouseIn = false;}
    }
    
    public void sizeTo(float factor){}

    public void reSize(float factor){}

    public void orientTo(float angle){}

    public float getSize(){
	return size;
    }

    public float getOrient(){return orient;}

    public boolean fillsView(long w,long h,int camIndex){
	return false;
    }

    public boolean coordInside(int x,int y,int camIndex){
	return false;
    }

    public short mouseInOut(int x,int y,int camIndex){
	return Glyph.NO_CURSOR_EVENT;
    }

    public void project(Camera c, Dimension d){
	int i = c.getIndex();
	coef = (float)(c.focal / (c.focal+c.altitude));
	pc[i].cx = (d.width/2) + Math.round((-c.posx)*coef);
	pc[i].cy = (d.height/2) - Math.round((-c.posy)*coef);
    }

    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i = c.getIndex();
	coef = (float)(c.focal / (c.focal+c.altitude)) * lensMag;
	pc[i].lcx = (lensWidth/2) + Math.round((-lensx)*coef);
	pc[i].lcy = (lensHeight/2) - Math.round((-lensy)*coef);
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){

    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){

    }

    /** Not implemented yet. */
    public Object clone(){
	return new DPath();
    }

    public void highlight(boolean b, Color selectedColor){}

}
