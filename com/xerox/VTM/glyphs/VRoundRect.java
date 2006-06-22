/*   FILE: VRoundRect.java
 *   DATE OF CREATION:   Wed May 28 14:27:51 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: VRoundRect.java,v 1.8 2006/03/17 17:45:23 epietrig Exp $
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
 * Round Rectangle - cannot be reoriented - the corners are approximated to a plain rectangle for some operations like computing mouse enter/exit events (performance reasons)
 * @author Emmanuel Pietriga
 **/

public class VRoundRect extends Glyph implements RectangularShape  {

    /**half width and height in virtual space*/
    long vw,vh;
    /**aspect ratio (width divided by height)*/
    float ar;

    ProjRoundRect[] pc;

    /**
     * horizontal diameter of the arc at the four corners
     */
    int arcWidth;
    /**
     * vertical diameter of the arc at the four corners
     */
    int arcHeight;

    public VRoundRect(){
	vx=0;
	vy=0;
	vz=0;
	vw=10;
	vh=10;
	computeSize();
	ar=(float)vw/(float)vh;
	orient=0;
	setColor(Color.white);
	setBorderColor(Color.black);
	arcWidth=10;
	arcHeight=10;
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param w half width in virtual space
     *@param h half height in virtual space
     *@param c fill color
     *@param aw arc width in virtual space
     *@param ah arc height in virtual space
     */
    public VRoundRect(long x,long y,float z,long w,long h,Color c,int aw,int ah){
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
	arcWidth=aw;
	arcHeight=ah;
    }

    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public void initCams(int nbCam){
	pc=new ProjRoundRect[nbCam];
	for (int i=0;i<nbCam;i++){
	    pc[i]=new ProjRoundRect();
	}
    }

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
    public void addCamera(int verifIndex){
	if (pc!=null){
	    if (verifIndex==pc.length){
		ProjRoundRect[] ta=pc;
		pc=new ProjRoundRect[ta.length+1];
		for (int i=0;i<ta.length;i++){
		    pc[i]=ta[i];
		}
		pc[pc.length-1]=new ProjRoundRect();
	    }
	    else {System.err.println("VRoundRect:Error while adding camera "+verifIndex);}
	}
	else {
	    if (verifIndex==0){
		pc=new ProjRoundRect[1];
		pc[0]=new ProjRoundRect();
	    }
	    else {System.err.println("VRoundRect:Error while adding camera "+verifIndex);}
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
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /**set absolute half width*/
    public void setWidth(long w){ 
	vw=w;
	ar=(float)vw/(float)vh;
	computeSize();
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /**set absolute half height*/
    public void setHeight(long h){
	vh=h;
	ar=(float)vw/(float)vh;
	computeSize();
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /**multiply bounding circle radius by factor*/
    public void reSize(float factor){ //resizing factor
	size*=factor;
	vw=(long)Math.round((size*ar)/(Math.sqrt(Math.pow(ar,2)+1)));
	vh=(long)Math.round((size)/(Math.sqrt(Math.pow(ar,2)+1)));
	try{vsm.repaintNow();}catch(NullPointerException e){}
    }

    /**
     * set horizontal diameter of the arc at the four corners
     */
    public void setArcWidth(int w){
	arcWidth=(w>=0) ? w : 0;
    }

    /**
     * set vertical diameter of the arc at the four corners
     */
    public void setArcHeight(int h){
	arcHeight=(h>=0) ? h : 0;
    }

    /**
     * get horizontal diameter of the arc at the four corners
     */
    public int getArcWidth(){
	return arcWidth;
    }

    /**
     * get vertical diameter of the arc at the four corners
     */
    public int getArcHeight(){
	return arcHeight;
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
	//project width and height
	pc[i].cw=Math.round(vw*coef);
	pc[i].ch=Math.round(vh*coef);
	pc[i].aw=Math.round(arcWidth*coef);
	pc[i].ah=Math.round(arcHeight*coef);
    }

    /**project shape in camera coord sys prior to actual painting through the lens*/
    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
	int i=c.getIndex();
	coef=(float)(c.focal/(c.focal+c.altitude)) * lensMag;
	//find coordinates of object's geom center wrt to camera center and project
	//translate in JPanel coords
	pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
	pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
	//project width and height
	pc[i].lcw=Math.round(vw*coef);
	pc[i].lch=Math.round(vh*coef);
	pc[i].law=Math.round(arcWidth*coef);
	pc[i].lah=Math.round(arcHeight*coef);
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
		g.fillRoundRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw,2*pc[i].ch,pc[i].aw,pc[i].ah);
	    }
	    g.setColor(borderColor);
	    if (paintBorder){
		if (stroke!=null) {
		    if (((pc[i].cx-pc[i].cw)>0) || ((pc[i].cy-pc[i].ch)>0) || ((2*pc[i].cw-1)<vW) || ((2*pc[i].ch-1)<vH)){
			g.setStroke(stroke);  //change stroke there
			g.drawRoundRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1,pc[i].aw,pc[i].ah);
			g.setStroke(stdS);  //original stroke restored here
		    }
		}
		else {
		    g.drawRoundRect(dx+pc[i].cx-pc[i].cw,dy+pc[i].cy-pc[i].ch,2*pc[i].cw-1,2*pc[i].ch-1,pc[i].aw,pc[i].ah);
		}
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
		g.fillRoundRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw,2*pc[i].lch,pc[i].law,pc[i].lah);
	    }
	    g.setColor(borderColor);
	    if (paintBorder){
		if (stroke!=null) {
		    if (((pc[i].lcx-pc[i].lcw)>0) || ((pc[i].lcy-pc[i].lch)>0) || ((2*pc[i].lcw-1)<vW) || ((2*pc[i].lch-1)<vH)){
			g.setStroke(stroke);  //change stroke there
			g.drawRoundRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1,pc[i].law,pc[i].lah);
			g.setStroke(stdS);  //original stroke restored here
		    }
		}
		else {
		    g.drawRoundRect(dx+pc[i].lcx-pc[i].lcw,dy+pc[i].lcy-pc[i].lch,2*pc[i].lcw-1,2*pc[i].lch-1,pc[i].law,pc[i].lah);
		}
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
	VRoundRect res=new VRoundRect(vx,vy,0,vw,vh,color,arcWidth,arcHeight);
	res.borderColor=this.borderColor;
	res.selectedColor=this.selectedColor;
	res.mouseInsideColor=this.mouseInsideColor;
	res.bColor=this.bColor;
	return res;
    }

}
