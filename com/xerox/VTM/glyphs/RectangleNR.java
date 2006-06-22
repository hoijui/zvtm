/*   FILE: RectangleNR.java
 *   DATE OF CREATION:   Thu Dec 05 13:53:36 2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: RectangleNR.java,v 1.8 2006/03/17 17:45:22 epietrig Exp $
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.Camera;
import net.claribole.zvtm.lens.Lens;

/**
 * RectangleNR - not sensitive to zoom (will keep its size no matter the camera's altitude) - cannot be reoriented <br> used for instance to create resizing handles
 * @author Emmanuel Pietriga
 **/

public class RectangleNR extends Glyph implements RectangularShape,Cloneable {

    /**half width and height in virtual space*/
    long vw,vh;
    /**aspect ratio (width divided by height)*/
    float ar;

    RProjectedCoords[] pc;

    public RectangleNR(){
	vx=0;
	vy=0;
	vz=0;
	vw=5;
	vh=5;
	computeSize();
	ar=(float)vw/(float)vh;
	orient=0;
	setColor(Color.black);
	setBorderColor(Color.black);
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     */
    public RectangleNR(long x,long y,float z,long w,long h,Color c){
	vx=x;
	vy=y;
	vz=z;
	vw=w;
	vh=h;
	computeSize();
	if (vw==0 && vh==0){ar=1.0f;}
	else {ar=(float)vw/(float)vh;}
	//if (vh!=0){ar=vw/vh;}else{ar=0;}
	orient=0;
	setColor(c);
	setBorderColor(Color.black);
    }

    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public void initCams(int nbCam){
	pc=new RProjectedCoords[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new RProjectedCoords();
	    pc[i].cw=(int)vw;
	    pc[i].ch=(int)vh;
	}
    }

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		RProjectedCoords[] ta=pc;
		pc=new RProjectedCoords[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new RProjectedCoords();
		pc[pc.length-1].cw=(int)vw;
		pc[pc.length-1].ch=(int)vh;
	    }
	    else {System.err.println("RectangleNR:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new RProjectedCoords[1];
		pc[0]=new RProjectedCoords();
		pc[0].cw=(int)vw;
		pc[0].ch=(int)vh;
	    }
	    else {System.err.println("RectangleNR:Error while adding camera "+verifIndex);}
	}
    }

    /**if a camera is removed from the virtual space, we should delete the corresponding projected coordinates, but do not modify the array it self because we do not want to change other cameras' index - just point to null*/
    public void removeCamera(int index){
	pc[index]=null;
    }

    /**reset prevMouseIn for projected coordinates nb i*/
    public void resetMouseIn(int i){
	if (pc[i]!=null){pc[i].prevMouseIn=false;}
    }

    /**get orientation*/
    public float getOrient(){return 0;}

    /**set orientation (absolute) - has no effect*/
    public void orientTo(float angle){}

    /**get size (bounding circle radius)*/
    public float getSize(){return size;}

    /**get half width*/
    public long getWidth(){return vw;}

    /**get half height*/
    public long getHeight(){return vh;}

    /**compute size (bounding circle radius)*/
    void computeSize(){
	size=(float)Math.sqrt(Math.pow(vw,2)+Math.pow(vh,2));
    }

    /**set absolute size by setting bounding circle radius*/
    public void sizeTo(float radius){  //new bounding circle radius
	size=radius;
	vw=(long)Math.round((size*ar)/(Math.sqrt(Math.pow(ar,2)+1)));
	vh=(long)Math.round((size)/(Math.sqrt(Math.pow(ar,2)+1)));
	updateProjectedWH();
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /**set absolute half width*/
    public void setWidth(long w){ 
	vw=w;
	computeSize();
	updateProjectedWH();
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /**set absolute half height*/
    public void setHeight(long h){
	vh=h;
	computeSize();
	updateProjectedWH();
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /**multiply bounding circle radius by factor*/
    public void reSize(float factor){//resizing factor
	size*=factor;
	vw=(long)Math.round((size*ar)/(Math.sqrt(Math.pow(ar,2)+1)));
	vh=(long)Math.round((size)/(Math.sqrt(Math.pow(ar,2)+1)));
	updateProjectedWH();
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    private void updateProjectedWH(){
	if (pc!=null){
	    for (int i=0;i<pc.length;i++){
		try {
		    pc[i].cw=(int)vw;
		    pc[i].ch=(int)vh;
		}//some pc[i] might be null (if cameras were deleted from the virtual space)
		catch (NullPointerException e){}
	    }
	}
    }

    /**used to find out if it is necessary to project and draw the glyph in the current view or through the lens in the current view
     *@param wb west region boundary (virtual space coordinates)
     *@param nb north region boundary (virtual space coordinates)
     *@param eb east region boundary (virtual space coordinates)
     *@param sb south region boundary (virtual space coordinates)
     *@param i camera index (useuful only for some glyph classes redefining this method)
     */
    public boolean visibleInRegion(long wb, long nb, long eb, long sb, int i){
	if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
	    /* Glyph hotspot is in the region. The glyph is obviously visible */
	    return true;
	}
	else {
	    if (((vx-pc[i].cw)<=eb) && ((vx+pc[i].cw)>=wb) && ((vy-pc[i].ch)<=nb) && ((vy+pc[i].ch)>=sb)){
		/* Glyph is at least partially in region.
		   We approximate using the glyph bounding circle, meaning that some glyphs not
		   actually visible can be projected and drawn (but they won't be displayed)) */
		return true;  
	    }
	}
	return false;
    }

    /**used to find out if it is necessary to project and draw the glyph through the lens in the current view
     *@param wb west region boundary (virtual space coordinates)
     *@param nb north region boundary (virtual space coordinates)
     *@param eb east region boundary (virtual space coordinates)
     *@param sb south region boundary (virtual space coordinates)
     *@param i camera index (useuful only for some glyph classes redefining this method)
     */
    /**used to find out if it is necessary to project and draw the glyph in the current view*/
    public boolean containedInRegion(long wb, long nb, long eb, long sb, int i){
	if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
	    /* Glyph hotspot is in the region.
	       There is a good chance the glyph is contained in the region, but this is not sufficient. */
	    if (((vx+pc[i].cw)<=eb) && ((vx-pc[i].cw)>=wb) && ((vy+pc[i].ch)<=nb) && ((vy-pc[i].ch)>=sb)){
		return true;
	    }
	    else return false;   //otherwise the glyph is not visible
	}
	return false;
    }

    /**used to find out if glyph completely fills the view (in which case it is not necessary to repaint objects at a lower altitude)*/
    public boolean fillsView(long w,long h,int camIndex){//width and height of view - pc[i].c? are JPanel coords
	if ((w<=pc[camIndex].cx+pc[camIndex].cw) && (0>=pc[camIndex].cx-pc[camIndex].cw) && (h<=pc[camIndex].cy+pc[camIndex].ch) && (0>=pc[camIndex].cy-pc[camIndex].ch)){return true;}
	else {return false;}
    }

    /**detects whether the given point is inside this glyph or not 
     *@param x EXPECTS PROJECTED JPanel COORDINATE
     *@param y EXPECTS PROJECTED JPanel COORDINATE
     */
    public boolean coordInside(int x,int y,int camIndex){
	if ((x>=(pc[camIndex].cx-pc[camIndex].cw)) && (x<=(pc[camIndex].cx+pc[camIndex].cw)) && (y>=(pc[camIndex].cy-pc[camIndex].ch)) && (y<=(pc[camIndex].cy+pc[camIndex].ch))){return true;}
	else {return false;}
    }

    /**returns 1 if mouse has entered the glyph, -1 if it has exited the glyph, 0 if nothing has changed (meaning it was already inside or outside it)*/
    public int mouseInOut(int x,int y,int camIndex){
	if (coordInside(x,y,camIndex)){//if the mouse is inside the glyph
	    if (!pc[camIndex].prevMouseIn){//if it was not inside it last time, mouse has entered the glyph
		pc[camIndex].prevMouseIn=true;
		return 1;
	    }
	    else {return 0;}  //if it was inside last time, nothing has changed
	}
	else{//if the mouse is not inside the glyph
	    if (pc[camIndex].prevMouseIn){//if it was inside it last time, mouse has exited the glyph
		pc[camIndex].prevMouseIn=false;
		return -1;
	    }
	    else {return 0;}  //if it was not inside last time, nothing has changed
	}
    }

    /**project shape in camera coord sys prior to actual painting*/
    public void project(Camera c, Dimension d){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude));
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
	pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
    }

    /**project shape in camera coord sys prior to actual painting through the lens*/
    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
	pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
    }

    /**draw glyph 
     *@param i camera index in the virtual space
     *@param vW view width - used to determine if contour should be drawn or not (when it is dashed and object too big)
     *@param vH view height - used to determine if contour should be drawn or not (when it is dashed and object too big)
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].cw>1) && (pc[i].ch>1)) {//repaint only if object is visible
	    if (filled) {
		g.setColor(this.color);
		g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch);
	    }
	    g.setColor(borderColor);
	    if (paintBorder){
		g.drawRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1);
	    }
	}
	else if ((pc[i].cw<=1) ^ (pc[i].ch<=1)) {//repaint only if object is visible  (^ means xor)
	    g.setColor(this.color);
	    if (pc[i].cw<=1){
		g.fillRect(dx+pc[i].cx,dy+pc[i].cy-pc[i].ch,1,2*pc[i].ch);
	    }
	    else if (pc[i].ch<=1){
		g.fillRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy,2*pc[i].cw,1);
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if ((pc[i].lcw>1) && (pc[i].lch>1)) {//repaint only if object is visible
	    if (filled) {
		g.setColor(this.color);
		g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch);
	    }
	    g.setColor(borderColor);
	    if (paintBorder){
		g.drawRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1);
	    }
	}
	else if ((pc[i].lcw<=1) ^ (pc[i].lch<=1)) {//repaint only if object is visible  (^ means xor)
	    g.setColor(this.color);
	    if (pc[i].lcw<=1){
		g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy-pc[i].lch,1,2*pc[i].lch);
	    }
	    else if (pc[i].lch<=1){
		g.fillRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy,2*pc[i].lcw,1);
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
	}
    }

    /**returns a clone of this object (only basic information is cloned for now: shape, orientation, position, size)*/
    public Object clone(){
	RectangleNR res=new RectangleNR(vx,vy,0,vw,vh,color);
	res.borderColor=this.borderColor;
	res.selectedColor=this.selectedColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
