/*   FILE: VirtualSpace.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
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
 *
 * $Id$
 */

package com.xerox.VTM.engine;

import java.util.Enumeration;
import java.util.Vector;

import com.xerox.VTM.glyphs.Glyph;

  /**
   * A virtual space contains glyphs and can be observed through multiple cameras
   * @author Emmanuel Pietriga
   **/

public class VirtualSpace {

    /**
     *computes the geometrical center of a set of glyphs (takes glyph sizes into account)  (0,0 if list is empty)
     *@param gl a list of Glyph instances
     */
    public static LongPoint getGlyphSetGeometricalCenter(Glyph[] gl){
	if (gl!=null && gl.length>0){
	    long[] tmpC=new long[4];
	    long size=(long)gl[0].getSize();
	    tmpC[0]=gl[0].vx-size;
	    tmpC[1]=gl[0].vy+size;
	    tmpC[2]=gl[0].vx+size;
	    tmpC[3]=gl[0].vy-size;
	    long tmp;
	    for (int i=1;i<gl.length;i++){
		size=(long)gl[i].getSize();
		tmp=gl[i].vx-size; if (tmp<tmpC[0]){tmpC[0]=tmp;}
		tmp=gl[i].vy+size; if (tmp>tmpC[1]){tmpC[1]=tmp;}
		tmp=gl[i].vx+size; if (tmp>tmpC[2]){tmpC[2]=tmp;}
		tmp=gl[i].vy-size; if (tmp<tmpC[3]){tmpC[3]=tmp;}
	    }
	    return new LongPoint((tmpC[2]+tmpC[0])/2,(tmpC[1]+tmpC[3])/2);
	}
	else {return new LongPoint(0,0);}
    }

    /**name of virtual space*/
    public String spaceName;
    
    /**hook to virtual space manager*/
    public VirtualSpaceManager vsm;

    /**camera manager for this virtual space*/
    CameraManager cm;

    /**all glyphs in this virtual space, visible or not*/
    Vector visualEnts;

    /**visible glyphs - order is important  (biggest index gets drawn on top)<br>
       shared by all cameras in the virtual space as it is the same for all of them*/
    Glyph[] drawingList;

    Vector[] camera2drawnList; //sharing drawnList was causing a problem ; we now have one for each camera

    /**
     *@param n virtual space name
     */
    VirtualSpace(String n){
	cm=new CameraManager(this);
	visualEnts=new Vector();
	camera2drawnList=new Vector[0];
  	drawingList = new Glyph[0];
	spaceName=n;
    }

    /**get virtual space name*/
    public String getName(){return spaceName;}

    /**get virtual space's i-th camera*/
    public Camera getCamera(int i){return cm.getCamera(i);}
    
    /**
     *@deprecated As of zvtm 0.9.0, replaced by getCameraListAsArray
     *@see #getCameraListAsArray()
     */
    public Vector getCameraList(){
	Vector res=new Vector();
	for (int i=0;i<cm.cameraList.length;i++){
	    res.add(cm.cameraList[i]);
	}
	return res;
    }

    /**returns the list of all cameras in this virtual space*/
    public Camera[] getCameraListAsArray(){return cm.cameraList;}

    /**create a new camera*/
    Camera createCamera(){
	Camera c=cm.addCamera();
	Vector[] newDrawnListList=new Vector[camera2drawnList.length+1];  //create a new drawnList for it
	System.arraycopy(camera2drawnList,0,newDrawnListList,0,camera2drawnList.length);
	newDrawnListList[camera2drawnList.length]=new Vector();
	camera2drawnList=newDrawnListList;
	c.setOwningSpace(this);
	for (Enumeration e=visualEnts.elements();e.hasMoreElements();){
	    Glyph g=(Glyph)e.nextElement();
	    g.addCamera(c.getIndex());
	}
	return c;
    }
    
    /**remove camera at index i
     * when a camera is destroyed, its index is not reused for another one - so if camera number #3 is removed and then a new camera is added it will be assigned number #4 even though there is no camera at index #3 any longer
     *@param i index of camera in virtual space
     */
    public void removeCamera(int i){
	if (cm.cameraList.length>i){
	    for (int j=0;j<vsm.allViews.length;j++){
		if (vsm.allViews[j].cameras.contains(cm.getCamera(i))){
		    vsm.allViews[j].destroyCamera(cm.getCamera(i));
		}
	    }
	    for (Enumeration e=visualEnts.elements();e.hasMoreElements();){
		Glyph g=(Glyph)e.nextElement();
		g.removeCamera(i);
	    }
	    cm.removeCamera(i);
	    camera2drawnList[i]=null;
	}
    }

    /**destroy this virtual space - call method in virtual space manager*/
    protected void destroy(){
	for (int i=0;i<cm.cameraList.length;i++){
	    this.removeCamera(i);
	}
	for (Enumeration e=visualEnts.elements();e.hasMoreElements();){
	    this.destroyGlyph((Glyph)e.nextElement());
	}
    }

    /**add glyph g to this space*/
    void addGlyph(Glyph g){
	g.initCams(cm.cameraList.length);
	visualEnts.add(g);
	addGlyphToDrawingList(g);
    }

    /** Get all glyphs in this space, visible or not, sensitive or not.
     * IMPORTANT: Read-only. Do not temper with this data structure unless you know what you are doing.
     * It is highly recommended to clone it if you want to add/remove elements from it for your own purposes.
     */
    public Vector getAllGlyphs(){
	return visualEnts;
    }

    /**get all visible glyphs
     *@deprecated as of zvtm 0.9.2
     *@see #getDrawnGlyphs(int cameraIndex)
     */
    public Vector getVisibleGlyphs(){
	Vector res = new Vector();
	for (int i=0;i<drawingList.length;i++){
	    res.add(drawingList[i]);
	}
	return res;
    }

    /**get all visible glyphs*/
    public Glyph[] getVisibleGlyphList(){
	return drawingList;
    }

    /**
     *@deprecated as of zvtm 0.9.0
     *@see #getDrawnGlyphs(int cameraIndex)
     */
    public Vector getDrawnGlyphs(){
	if (camera2drawnList.length>0){
	    return camera2drawnList[0];
	}
	else return null;
    }

    /**
     *get all glyphs actually drawn for a given camera in this virtual space
     */
    public Vector getDrawnGlyphs(int cameraIndex){
	if (cameraIndex<camera2drawnList.length){
	    return camera2drawnList[cameraIndex];
	}
	else return null;
    }

    /*put glyph gl in the list of glyphs actually drawn (this list is used to compute the list of glyphs under mouse)*/
    protected void drewGlyph(Glyph gl,int cameraIndex){
	if (cameraIndex<camera2drawnList.length && camera2drawnList[cameraIndex]!=null){
	    camera2drawnList[cameraIndex].add(gl);
	}
    }

    /**get selected glyphs*/
    public Vector getSelectedGlyphs(){
	Vector v=new Vector();
	Glyph g;
	for (Enumeration e=visualEnts.elements();e.hasMoreElements();){
	    g=(Glyph)e.nextElement();
	    if (g.isSelected()){
		v.add(g);
	    }
	}
	return v;
    }

    /**select all glyphs*/
    public void selectAllGlyphs(){
	for (Enumeration e=visualEnts.elements();e.hasMoreElements();){
	    ((Glyph)e.nextElement()).select(true);
	}
    }

    /**unselect all glyphs*/
    public void unselectAllGlyphs(){
	for (Enumeration e=visualEnts.elements();e.hasMoreElements();){
	    ((Glyph)e.nextElement()).select(false);
	}
    }

    /**get all glyphs of type t - if t=="" then select all glyphs (means ANY type)*/
    public Vector getGlyphsOfType(String t){//
	Vector v=new Vector();
	Glyph g;
	for (Enumeration e=visualEnts.elements();e.hasMoreElements();){
	    g=(Glyph)e.nextElement();
	    if ((t.equals("")) || (g.getType().equals(t))){v.add(g);}
	}
	return v;
    }

    /**remove this glyph from this virtual space (should then be garbage-collected)
     *@param gID glyph's ID
     */
    public void destroyGlyph(Long gID){
	destroyGlyph(vsm.getGlyph(gID));
    }

    /**remove this glyph from this virtual space (should then be garbage-collected)*/
    public void destroyGlyph(Glyph g){
	try {
	    if (g.stickedTo!=null){
		if (g.stickedTo instanceof Glyph){((Glyph)g.stickedTo).unstick(g);}
		else if (g.stickedTo instanceof Camera){((Camera)g.stickedTo).unstick(g);}
		else {((VCursor)g.stickedTo).unstickSpecificGlyph(g);}
	    }
	    if (g.getCGlyph()!=null){//remove from composite glyph if was part of one
		g.getCGlyph().removeSecondaryGlyph(g);
	    }
	    for (int i=0;i<camera2drawnList.length;i++){
		if (camera2drawnList[i]!=null){//camera2drawnlist[i] can be null if camera i has been removed from 
		    camera2drawnList[i].remove(g);//the virtual space
		}
	    }
	    View v;
	    for (int i=0;i<cm.cameraList.length;i++){
		if (cm.cameraList[i] != null && cm.cameraList[i].view != null){
		    cm.cameraList[i].view.mouse.removeGlyphFromList(g);
		}
	    }
	    visualEnts.remove(g);
	    removeGlyphFromDrawingList(g);
	    vsm.allGlyphs.remove(g);
	    vsm.repaintNow();
	}
	catch (NullPointerException ex){System.err.println("ZVTM Error: VirtualSpace.destroyGlyph(): the glyph you are trying to delete might not be a member of this virtual space ("+spaceName+") or might be null");ex.printStackTrace();}
    }

    /**show Glyph g
     * <br>- use show() and hide() to change both the visibility and sensitivity of glyphs
     * <br>- use Glyph.setVisible() to only change the glyph's visibility
     *@see #hide(Glyph g)*/
    public void show(Glyph g){
	if (visualEnts.contains(g) && glyphIndexInDrawingList(g) == -1){addGlyphToDrawingList(g);}
	vsm.repaintNow();
    }

    /**hide Glyph g
     * <br>- use show() and hide() to change both the visibility and sensitivity of glyphs
     * <br>- use Glyph.setVisible() to only change the glyph's visibility
     *@see #show(Glyph g)*/
    public void hide(Glyph g){
	removeGlyphFromDrawingList(g);
	g.resetMouseIn();
	View v;
	for (int i=0;i<cm.cameraList.length;i++){
	    if (cm.cameraList[i] != null && cm.cameraList[i].view != null){
		cm.cameraList[i].view.mouse.removeGlyphFromList(g);
	    }
	}
	vsm.repaintNow();
    }

    /**put this glyph on top of the drawing list (will be drawn last)*/
    public void onTop(Glyph g){
	if (glyphIndexInDrawingList(g) != -1){
	    removeGlyphFromDrawingList(g);
	    addGlyphToDrawingList(g);
	}
    }

    /**put this glyph at bottom of the drawing list (will be drawn first)*/
    public void atBottom(Glyph g){
	if (glyphIndexInDrawingList(g) != -1){
	    removeGlyphFromDrawingList(g);
	    insertGlyphInDrawingList(g,0);
	}
    }

    /**put glyph g1 just above glyph g2 in the drawing list (g1 painted after g2)*/
    public void above(Glyph g1,Glyph g2){
	if ((glyphIndexInDrawingList(g1) != -1) && (glyphIndexInDrawingList(g2) != -1)){
	    removeGlyphFromDrawingList(g1);
	    int i = glyphIndexInDrawingList(g2);
	    insertGlyphInDrawingList(g1,i+1);
	}
    }

    /**put glyph g1 just below glyph g2 in the drawing list (g1 painted before g2)*/
    public void below(Glyph g1,Glyph g2){
	if ((glyphIndexInDrawingList(g1) != -1) && (glyphIndexInDrawingList(g2) != -1)){
	    removeGlyphFromDrawingList(g1);
	    int i = glyphIndexInDrawingList(g2);
	    insertGlyphInDrawingList(g1,i);
	}
    }

    void setManager(VirtualSpaceManager v){this.vsm=v;}

    /**returns the leftmost Glyph x-pos, upmost Glyph y-pos, rightmost Glyph x-pos, downmost Glyph y-pos visible in this virtual space*/
    public long[] findFarmostGlyphCoords(){
	long[] res = new long[4];
	return findFarmostGlyphCoords(res);
    }
    
    /**returns the leftmost Glyph x-pos, upmost Glyph y-pos, rightmost Glyph x-pos, downmost Glyph y-pos visible in this virtual space*/
    public long[] findFarmostGlyphCoords(long[] res){
	Glyph[] gl = this.getVisibleGlyphList();
	if (gl.length > 0){
	    //init result with first glyph found
	    long size = (long)gl[0].getSize();
	    res[0] = gl[0].vx-size;
	    res[1] = gl[0].vy+size;
	    res[2] = gl[0].vx+size;
	    res[3] = gl[0].vy-size;
	    long tmp;
	    for (int i=1;i<gl.length;i++){
		size = (long)gl[i].getSize();
		tmp = gl[i].vx-size; if (tmp<res[0]){res[0] = tmp;}
		tmp = gl[i].vy+size; if (tmp>res[1]){res[1] = tmp;}
		tmp = gl[i].vx+size; if (tmp>res[2]){res[2] = tmp;}
		tmp = gl[i].vy-size; if (tmp<res[3]){res[3] = tmp;}
	    }
	    return res;
	}
	else {res[0] = 0;res[1] = 0;res[2] = 0;res[3] =  0;return res;}
    }

    protected void addGlyphToDrawingList(Glyph g){
	synchronized(drawingList){
	    Glyph[] newDrawingList = new Glyph[drawingList.length + 1];
	    System.arraycopy(drawingList, 0, newDrawingList, 0, drawingList.length);
	    newDrawingList[drawingList.length] = g;
	    drawingList = newDrawingList;
	}
    }

    protected void insertGlyphInDrawingList(Glyph g, int index){
	synchronized(drawingList){
	    Glyph[] newDrawingList = new Glyph[drawingList.length + 1];
	    System.arraycopy(drawingList, 0, newDrawingList, 0, index);
	    newDrawingList[index] = g;
	    System.arraycopy(drawingList, index, newDrawingList, index+1, drawingList.length-index);
	    drawingList = newDrawingList;
	}
    }

    protected void removeGlyphFromDrawingList(Glyph g){
	synchronized(drawingList){
	    for (int i=0;i<drawingList.length;i++){
		if (drawingList[i] == g){
		    Glyph[] newDrawingList = new Glyph[drawingList.length - 1];
		    System.arraycopy(drawingList, 0, newDrawingList, 0, i);
		    System.arraycopy(drawingList, i+1, newDrawingList, i, drawingList.length-i-1);
		    drawingList = newDrawingList;
		    break;
		}
	    }
	}
    }

    protected int glyphIndexInDrawingList(Glyph g){
	synchronized(drawingList){
	    for (int i=0;i<drawingList.length;i++){
		if (drawingList[i] == g){
		    return i;
		}
	    }
	}
	return -1;
    }

}
