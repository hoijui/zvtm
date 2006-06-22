/*   FILE: CGlyph.java
 *   DATE OF CREATION:   Oct 01 2002
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) E. Pietriga, 2002. All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 */

package net.claribole.zvtm.glyphs;

//import java.lang.Math;
//import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.lens.Lens;

  /**
   * Composite glyph (abstract glyph made of a primary shape and [optional] secondary shapes). A composite glyph has no visual representation of its own : it is just a means to tie glyphs between themselves. CGlyph only offers an higher level construction to group standard glyphs ; it entirely relies on lower level functions that are also available to the programmer ; so if you are not happy with the way CGlyph works, you can always create a modified version of it. <br> 
   * IMPORTANT : both CGlyphs AND and their components (standard glyphs) should be added to the virtual space<br>
   * The event firing policy can be changed using method setSensitivity(int) ; Note: the glyph sent as a parameter of the event triggering is the component, not the CGlyph ; the CGlyph can be retrieved by calling Glyph.getCGlyph()
   * @author Emmanuel Pietriga
   */

public class CGlyph extends Glyph implements Cloneable {

    /**Fire enter/exit Glyph events only when entering primary glyph*/
    public static short PRIMARY_GLYPH_ONLY=0;
    /**Fire enter/exit Glyph events when entering primary glyph and secondary glyphs*/
    public static short ALL_GLYPHS=1;

    short compSensit=CGlyph.ALL_GLYPHS;

    Glyph pGlyph; //primaryGlyph
    SGlyph[] sGlyphs;  //secondary glyphs

    /**
     *REMINDER : both CGlyphs AND and their components (standard glyphs) should be added to the virtual space
     *@param primary primary glyph in the composition
     *@param secondaries array of secondary glyphs (null if none)
     */
    public CGlyph(Glyph primary,SGlyph[] secondaries){
	setPrimaryGlyph(primary);
	if (secondaries!=null && secondaries.length>0){
	    sGlyphs=secondaries;
	    for (int i=0;i<sGlyphs.length;i++){
		sGlyphs[i].g.moveTo(pGlyph.vx+sGlyphs[i].xoffset,pGlyph.vy+sGlyphs[i].yoffset);
		sGlyphs[i].g.setCGlyph(this);
	    }
	}
    }

    /**called when glyph is created in order to create the initial set of projected coordinates wrt the number of cameras in the space
     *@param nbCam current number of cameras in the virtual space
     */
    public void initCams(int nbCam){}

    /**used internally to create new projected coordinates to use with the new camera
     *@param verifIndex camera index, just to be sure that the number of projected coordinates is consistent with the number of cameras
     */
    public void addCamera(int verifIndex){}

    /**if a camera is removed from the virtual space, we should delete the corresponding projected coordinates, but do not modify the array it self because we do not want to change other cameras' index - just point to null*/
    public void removeCamera(int index){}

    /**reset prevMouseIn for projected coordinates nb i*/
    public void resetMouseIn(int i){}

    /**relative translation (offset)*/
    public void move(long x,long y){
	vx+=x;
	vy+=y;
	pGlyph.move(x,y);
	if (sGlyphs!=null){
	    for (int i=0;i<sGlyphs.length;i++){
		sGlyphs[i].g.move(x,y);
	    }
	}
	propagateMove(x,y);  //take care of sticked glyphs
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**absolute translation*/
    public void moveTo(long x,long y){
	propagateMove(x-vx,y-vy);  //take care of sticked glyphs
	pGlyph.moveTo(x,y);
	if (sGlyphs!=null){
	    double teta=(double)-getOrient();
	    long x2,y2;
	    for (int i=0;i<sGlyphs.length;i++){
		if ((sGlyphs[i].rotationPolicy==SGlyph.FULL_ROTATION) 
		    || (sGlyphs[i].rotationPolicy==SGlyph.ROTATION_POSITION_ONLY)){
		    x2=Math.round((sGlyphs[i].xoffset*Math.cos(teta)+sGlyphs[i].yoffset*Math.sin(teta)));
		    y2=Math.round((sGlyphs[i].yoffset*Math.cos(teta)-sGlyphs[i].xoffset*Math.sin(teta)));
		    sGlyphs[i].g.moveTo(pGlyph.vx+x2,pGlyph.vy+y2);
		}
		else {
		    sGlyphs[i].g.moveTo(x+sGlyphs[i].xoffset,y+sGlyphs[i].yoffset);
		}
	    }
	}
	this.vx=pGlyph.vx;
	this.vy=pGlyph.vy;
	try{vsm.repaintNow();}catch(NullPointerException e){/*System.err.println("VSM null in Glyph "+e);*/}
    }

    /**returns orientation of primary glyph*/
    public float getOrient(){
	if (pGlyph!=null){return pGlyph.getOrient();}
	else {return 0;}
    }

    /**set composite glyph orientation - the rotation policy is set for each secondary glyph in the corresponding SGlyph*/
    public void orientTo(float angle){
	try {
	    pGlyph.orientTo(angle);
	    if (sGlyphs!=null){
		long x2,y2;
		double teta=(double)-angle;
		for (int i=0;i<sGlyphs.length;i++){
		    if ((sGlyphs[i].rotationPolicy==SGlyph.FULL_ROTATION) 
			|| (sGlyphs[i].rotationPolicy==SGlyph.ROTATION_ANGLE_ONLY)){
			sGlyphs[i].g.orientTo(angle+sGlyphs[i].aoffset);
		    }
		    if ((sGlyphs[i].rotationPolicy==SGlyph.FULL_ROTATION) 
			|| (sGlyphs[i].rotationPolicy==SGlyph.ROTATION_POSITION_ONLY)){
			x2=Math.round(sGlyphs[i].xoffset*Math.cos(teta)+sGlyphs[i].yoffset*Math.sin(teta));
			y2=Math.round(sGlyphs[i].yoffset*Math.cos(teta)-sGlyphs[i].xoffset*Math.sin(teta));
			sGlyphs[i].g.moveTo(pGlyph.vx+x2,pGlyph.vy+y2);
		    }
		}
	    }
	}
	catch(NullPointerException e){}
    }

    /**returns size of primary glyph*/
    public float getSize(){
	if (pGlyph!=null){return pGlyph.getSize();}
	else {return 0;}
    }

    /**set size of primary glyph - the resizing policy is set for each secondary glyph in the corresponding SGlyph*/
    public void sizeTo(float radius){
	if (sGlyphs!=null){
	    float ratio=radius/getSize();
	    double teta=(double)-getOrient();
	    long x2,y2;
	    for (int i=0;i<sGlyphs.length;i++){
		sGlyphs[i].xoffset=Math.round(sGlyphs[i].xoffset*ratio);
		sGlyphs[i].yoffset=Math.round(sGlyphs[i].yoffset*ratio);
		if ((sGlyphs[i].rotationPolicy==SGlyph.FULL_ROTATION) 
		    || (sGlyphs[i].rotationPolicy==SGlyph.ROTATION_POSITION_ONLY)){
		    x2=Math.round((sGlyphs[i].xoffset*Math.cos(teta)+sGlyphs[i].yoffset*Math.sin(teta)));
		    y2=Math.round((sGlyphs[i].yoffset*Math.cos(teta)-sGlyphs[i].xoffset*Math.sin(teta)));
		    sGlyphs[i].g.moveTo(pGlyph.vx+x2,pGlyph.vy+y2);
		}
		else {
		    sGlyphs[i].g.moveTo(pGlyph.vx+sGlyphs[i].xoffset,pGlyph.vy+sGlyphs[i].yoffset);
		}
		if (sGlyphs[i].sizePolicy==SGlyph.RESIZE){
		    sGlyphs[i].g.reSize(ratio);
		}
	    }
	}
	pGlyph.sizeTo(radius);
    }

    public void reSize(float factor){}

    /**used to find out if glyph completely fills the view (in which case it is not necessary to repaint objects at a lower altitude)*/
    public boolean fillsView(long w,long h,int camIndex){//would be too complex: just say no
	return false;
    }

    /**detects whether the given point is inside this glyph or not 
     *@param x EXPECTS PROJECTED JPanel COORDINATE
     *@param y EXPECTS PROJECTED JPanel COORDINATE
     */
    public boolean coordInside(int x,int y,int camIndex){
	return false;
    }

    /**returns 1 if mouse has entered the glyph, -1 if it has exited the glyph, 0 if nothing has changed (meaning it was already inside or outside it)*/
    public int mouseInOut(int x,int y,int camIndex){
	return 0;
    }

    /**project shape in camera coord sys prior to actual painting*/
    public void project(Camera c, Dimension d){}

    /**project shape in camera coord sys prior to actual painting through the lens*/
    public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){}

    /**not defined (no text can be associated to a composite glyph)
     *@param i camera index in the virtual space
     */
    void textDraw(Graphics2D g,int i){}

    /**draw glyph - does not do anything each component paints itself independently
     *@param i camera index in the virtual space
     */
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){}

    /**draw this glyph through the lens
     *@param g graphic context in which the glyph should be drawn 
     *@param vW associated view width (used to determine if border should be drawn)
     *@param vH associated view height (used to determine if border should be drawn)
     * right now only VRectangle and VRectangleOr(/Or=0) use this
     *@param i camera index in the virtual space
     */
    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){}

    /**set sensitivity of this glyph - use either PRIMARY_GLYPH_ONLY or ALL_GLYPHS - this does not override the setSensitivity(boolean) setting - Note: the glyph sent as a parameter of the event triggering is the component, not the CGlyph ; the CGlyph can be retrieved by calling Glyph.getCGlyph()*/
    public void setSensitivity(short s){
	if (s!=compSensit){
	    if (s==PRIMARY_GLYPH_ONLY && sGlyphs!=null){
		for (int i=0;i<sGlyphs.length;i++){
		    sGlyphs[i].g.setSensitivity(false);
		}
	    }
	    else if (s==ALL_GLYPHS && sGlyphs!=null){
		for (int i=0;i<sGlyphs.length;i++){
		    sGlyphs[i].g.setSensitivity(true);
		}
	    }
	    compSensit=s;
	}
    }

    /**
     *@param g change primary glyph in the composition
     */
    public void setPrimaryGlyph(Glyph g){
	pGlyph=g;
	g.setCGlyph(this);
	this.vx=pGlyph.vx;
	this.vy=pGlyph.vy;
    }

    /**
     *@param g Glyph to be added in the composition
     *@param rx relative position w.r.t primary glyph's center 
     *@param ry relative position w.r.t primary glyph's center
     */
    public void addSecondaryGlyph(Glyph g,long rx,long ry){
	if (sGlyphs==null){
	    sGlyphs=new SGlyph[1];
	    sGlyphs[0]=new SGlyph(g,rx,ry);
	    sGlyphs[0].g.moveTo(pGlyph.vx+sGlyphs[0].xoffset,pGlyph.vy+sGlyphs[0].yoffset);
	}
	else {
	    SGlyph[] tmpA=new SGlyph[sGlyphs.length+1];
	    System.arraycopy(sGlyphs,0,tmpA,0,sGlyphs.length);
	    tmpA[tmpA.length-1]=new SGlyph(g,rx,ry);
	    sGlyphs=tmpA;
	    sGlyphs[sGlyphs.length-1].g.moveTo(pGlyph.vx+sGlyphs[sGlyphs.length-1].xoffset,pGlyph.vy+sGlyphs[sGlyphs.length-1].yoffset);
	}
	g.setCGlyph(this);
    }

    /**
     *@param sGlyph SGlyph to be added in the composition
     */
    public void addSecondaryGlyph(SGlyph sGlyph){
	if (sGlyphs==null){
	    sGlyphs=new SGlyph[1];
	    sGlyphs[0]= sGlyph;
	    sGlyphs[0].g.moveTo(pGlyph.vx+sGlyphs[0].xoffset,pGlyph.vy+sGlyphs[0].yoffset);
	    sGlyphs[0].g.setCGlyph(this);
	}
	else {
	    SGlyph[] tmpA=new SGlyph[sGlyphs.length+1];
	    System.arraycopy(sGlyphs,0,tmpA,0,sGlyphs.length);
	    tmpA[tmpA.length-1] = sGlyph;
	    sGlyphs=tmpA;	    
	    sGlyphs[sGlyphs.length-1].g.moveTo(pGlyph.vx+sGlyphs[sGlyphs.length-1].xoffset,pGlyph.vy+sGlyphs[sGlyphs.length-1].yoffset);
	    sGlyphs[sGlyphs.length-1].g.setCGlyph(this);
	}
    }

    /**
     *@param g Glyph to be removed from the composition (this does not remove the glyph from the virtual space)
     */
    public void removeSecondaryGlyph(Glyph g){
	if (sGlyphs!=null){
	    for (int i=0;i<sGlyphs.length;i++){
		if (sGlyphs[i].g==g){
		    g.setCGlyph(null);
		    SGlyph[] tmpA=new SGlyph[sGlyphs.length-1];
		    System.arraycopy(sGlyphs,0,tmpA,0,i);
		    System.arraycopy(sGlyphs,i+1,tmpA,i,sGlyphs.length-i-1);
		    sGlyphs=tmpA;
		    break;
		}
	    }
	    if (sGlyphs.length==0){sGlyphs=null;}
	}
    }

    /**
     * get the secondary glyph encapsulating the glyph provided as parameter
     */
    public SGlyph getSGlyph(Glyph gl){
	SGlyph res=null;
	if (sGlyphs!=null){
	    for (int i=0;i<sGlyphs.length;i++){
		if (sGlyphs[i].g==gl){res=sGlyphs[i];break;}
	    }
	}
	return res;
    }

    /**
     * get all secondary glyphs associated with this CGlyph
     */
    public SGlyph[] getSecondaryGlyphs(){
	return sGlyphs;
    }

    /**
     * get primary glyph associated with this CGlyph
     */
    public Glyph getPrimaryGlyph(){
	return pGlyph;
    }

    /**not implemented yet*/
    public Object clone(){return null;}

}
