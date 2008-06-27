/*   FILE: VCursor.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2008. All Rights Reserved
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;

import java.util.Vector;
import java.util.Arrays;

import net.claribole.zvtm.engine.ViewEventHandler;
import net.claribole.zvtm.lens.Lens;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VPath;
import com.xerox.VTM.glyphs.VSegment;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.Translucent;

/* For DynaSpot */
import java.util.Timer;
import java.util.TimerTask;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Area;
import net.claribole.zvtm.engine.LowPassFilter;
import java.awt.Point;

/**
 * Glyph representing mouse cursor
 * @author Emmanuel Pietriga
 *
 * <h4>Using DynaSpot</h4>
 * <p>The DynaSpot behavior must be activated in VCursor, calling</p>
 * <ul><li>activateDynaSpot(boolean b)</li></ul>
 * 
 * <p>In your ViewEventHandler's mouseMoved() method, call:</p>
 * <ol>
 *  <li>v.getMouse().updateDynaSpotFrequency(e.getWhen());</li>
 *  <li>v.getMouse().updateDynaSpot(jpx, jpy);</li>
 * </ol>
 * 
 */

public class VCursor {

    Long ID;

    /**cursor color*/
    Color color;

    /**color of geometrical hints associated with cursor (drag segment, selection rectangle, etc.)*/
    Color hcolor;

    /**tells whether a cross should be drawn at cursor pos or not*/
    boolean isVisible=true;

    /**tells whether we should detect entry/exit in glyphs*/
    boolean sensit=true;

    /**sync VTM cursor and system cursor if true*/
    boolean sync;

    /**coord in camera space*/
    int cx,cy;
    /**coord in virtual space*/
    public long vx,vy;
    /**previous coords in virtual space*/
    long pvx,pvy;
    /**coords in JPanel*/
    int mx,my;
    /**gain for cursor unprojection w.r.t lens (if any lens is set)*/
    float[] gain = new float[2];

    Glyph tmpGlyph;  //used in computeMouseOverGlyph
    short tmpRes;      //used in computeMouseOverGlyph
    Long tmpID;      //used in computeMouseOverGlyph

    int maxIndex=-1;  //used in computeMouseOverGlyph
    
    /** List of glyphs overlapped by mouse. Last entry is last glyph entered.
        IMPORTANT: elements beyond maxIndex might not be up to date. Do not trust the value, especially if not null.*/
    public Glyph[] glyphsUnderMouse=new Glyph[50];  //50 is default, will grow if not enough

    /**last glyph the mouse entered in*/
    public Glyph lastGlyphEntered=null;

    /**glyphs sticked to the mouse cursor*/
    Glyph[] stickedGlyphs;

    /**view to which this cursor belongs*/
    View owningView;

    /* crosshair size */
    int size = 10;

    VCursor(View v){
        this.owningView=v;
        vx=0;pvx=0;
        vy=0;pvy=0;
        cx=0;
        cy=0;
        mx=0;
        my=0;
        color=Color.black;
        hcolor = Color.black;
        stickedGlyphs = new Glyph[0];
        sync=true;
    }

    /**Set size of cursor (crosshair length).*/
    public void setSize(int s){
	this.size = s;
    }

    /**Get size of cursor (crosshair length).*/
    public int getSize(){
	return size;
    }

    /**get mouse cursor ID*/
    public Long getID(){
	return ID;
    }

    /**set mouse cursor ID - should always be 0*/
    public void setID(Long ident){
	ID=ident;
    }

    /**get the mouse location in virtual space (active layer)*/
    public LongPoint getLocation(){return new LongPoint(vx,vy);}

    /**get the view to which this cursor belongs*/
    public View getOwningView(){return owningView;}

    /**synchronize real mouse cursor and this glyph*/
    public void setSync(boolean b){
	sync=b;
    }

    /**set the mouse cursor color*/
    public void setColor(Color c){
	this.color=c;
    }

    /**set the color of elements associated with cursor (drag segment, selection rectangle, etc.)*/
    public void setHintColor(Color c){
	this.hcolor = c;
    }
    
    /**move mouse cursor
     *@param x EXPECTS JPanel coord
     *@param y EXPECTS JPanel coord
     */
    public void moveTo(int x,int y){
	if (sync){
	    mx=x;
	    my=y;
	}
    }

    /**propagate mouse cursor movement to sticked glyphs*/
    public void propagateMove(){
	for (int i=0;i<stickedGlyphs.length;i++){
	    stickedGlyphs[i].move(vx-pvx, vy-pvy);
	}
    }

    /**attach glyph to mouse*/
    void stick(Glyph g){
	if (g!=null){
	    //make it unsensitive (was automatically disabled when glyph was sticked to mouse)
	    //because false enter/exit events can be generated when moving the mouse too fast
	    //in small glyphs   (I did not find a way to correct this bug yet)
	    g.setSensitivity(false);
	    Glyph[] newStickList = new Glyph[stickedGlyphs.length + 1];
	    System.arraycopy(stickedGlyphs, 0, newStickList, 0, stickedGlyphs.length);
	    newStickList[stickedGlyphs.length] = g;
	    stickedGlyphs = newStickList;
	    g.stickedTo = this;
	}
    }

    /**detach last glyph from mouse*/
    void unstick(){
	if (stickedGlyphs.length>0){
	    Glyph g = stickedGlyphs[stickedGlyphs.length - 1];
	    g.setSensitivity(true);  //make it sensitive again (was automatically disabled when glyph was sticked to mouse)
	    g.stickedTo = null;
	    Glyph[] newStickList = new Glyph[stickedGlyphs.length - 1];
	    System.arraycopy(stickedGlyphs, 0, newStickList, 0, stickedGlyphs.length - 1);
	    stickedGlyphs = newStickList;
	}
    }

    /**get the number of glyphs sticked to the mouse*/
    public int getStickedGlyphsNumber(){return stickedGlyphs.length;}

    /**detach specific glyph from mouse*/
    void unstickSpecificGlyph(Glyph g){
	for (int i=0;i<stickedGlyphs.length;i++){
	    if (stickedGlyphs[i] == g){
		g.stickedTo = null;
		Glyph[] newStickList = new Glyph[stickedGlyphs.length - 1];
		System.arraycopy(stickedGlyphs, 0, newStickList, 0, i);
		System.arraycopy(stickedGlyphs, i+1, newStickList, i, stickedGlyphs.length-i-1);
		stickedGlyphs = newStickList;
		break;
	    }
	}
    }

    /**get glyphs sticked to mouse*/
    public Glyph[] getStickedGlyphArray(){
	return stickedGlyphs;
    }

    /**get glyphs sticked to mouse
     *@deprecated As of zvtm 0.9.2, replaced by getStickedGlyphArray
     *@see #getStickedGlyphArray()
     */
    public Vector getStickedGlyphs(){
	Vector res = new Vector();
	for (int i=0;i<stickedGlyphs.length;i++){
	    res.add(stickedGlyphs[i]);
	}
	return res;
    }

    /**tells whether a cross should be drawn at cursor pos or not*/
    public void setVisibility(boolean b){
	isVisible=b;
    }

    /**tells whether we should detect entry/exit in glyphs*/
    public void setSensitivity(boolean b){
	sensit=b;
    }

    /**tells whether mouse sends events related to entry/exit in glyphs or not*/
    public boolean isSensitive(){return sensit;}

    /**returns a list of all VPaths under the mouse cursor - returns null if none
     *@param c should be the active camera (can be obtained by VirtualSpaceManager.getActiveCamera())
     *@param tolerance the rectangular area's half width/height considered as the cursor intersecting region, in virtual space units (default tolerance is 5)
     *@param cursorX cursor X coordinate in associated virtual space (if camera is not the active one)
     *@param cursorY cursor Y coordinate in associated virtual space (if camera is not the active one)
     *@see #getIntersectingPaths(Camera c)
     */
    public Vector getIntersectingPaths(Camera c, int tolerance, long cursorX, long cursorY){
	synchronized(this){
	    Vector res=new Vector();
	    Vector glyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
	    Object glyph;
	    for (int i=0;i<glyphs.size();i++){
		glyph = glyphs.elementAt(i);
		if ((glyph instanceof VPath) && intersectsVPath((VPath)glyph, tolerance, cursorX, cursorY)){res.add(glyph);}
	    }
	    if (res.isEmpty()){res=null;}
	    return res;
	}
    }
    
    /**returns a list of all VPaths under the mouse cursor (default tolerance, 5) - returns null if none
     *@param c should be the active camera (can be obtained by VirtualSpaceManager.getActiveCamera())
     *@see #getIntersectingPaths(Camera c, int tolerance, long cursorX, long cursorY)
     */
    public Vector getIntersectingPaths(Camera c){
	return getIntersectingPaths(c, 5, vx, vy);
    }

    /**returns a list of all VPaths under the mouse cursor (default tolerance, 5) - returns null if none
     *@param c should be the active camera (can be obtained by VirtualSpaceManager.getActiveCamera())
     *@param tolerance the rectangular area's half width/height considered as the cursor intersecting region, in virtual space units (default tolerance is 5)
     *@see #getIntersectingPaths(Camera c, int tolerance, long cursorX, long cursorY)
     */
    public Vector getIntersectingPaths(Camera c, int tolerance){
	return getIntersectingPaths(c, tolerance, vx, vy);
    }

    /**tells if the mouse is above VPath p
     *@param p VPath instance to be tested
     *@param tolerance the rectangular area's half width/height considered as the cursor intersecting region, in virtual space units (default tolerance is 5)
     *@param cursorX cursor X coordinate in associated virtual space (if camera is not the active one)
     *@param cursorY cursor Y coordinate in associated virtual space (if camera is not the active one)
     *@see #intersectsVPath(VPath p)
     */
    public boolean intersectsVPath(VPath p, int tolerance, long cursorX, long cursorY){
	int dtol = tolerance * 2;
	boolean res = p.getJava2DGeneralPath().intersects(cursorX-dtol, -cursorY-dtol, dtol, dtol);
	//XXX: why the hell did I do that? there is probably a reason... there ought to be a reason...
	if (p.getJava2DGeneralPath().contains(cursorX-tolerance, -cursorY-tolerance, tolerance, tolerance)){res=false;}
	return res;
    }

    /**tells if the mouse is above VPath p (default tolerance, 5)
     *@param p VPath instance to be tested
     *@param tolerance the rectangular area's half width/height considered as the cursor intersecting region, in virtual space units (default tolerance is 5)
     *@see #intersectsVPath(VPath p, int tolerance, long cursorX, long cursorY)
     */
    public boolean intersectsVPath(VPath p, int tolerance){
	return intersectsVPath(p, tolerance, vx, vy);
    }

    /**tells if the mouse is above VPath p (default tolerance, 5)
     *@param p VPath instance to be tested
     *@see #intersectsVPath(VPath p, int tolerance, long cursorX, long cursorY)
     */
    public boolean intersectsVPath(VPath p){
	return intersectsVPath(p, 5, vx, vy);
    }

    /**returns a list of all VTexts under the mouse cursor - returns null if none<br>
     * (mouse cursor coordinates are taken from the active layer's camera space)
     *@param c should be the active camera (can be obtained by VirtualSpaceManager.getActiveCamera())
     *@see #getIntersectingTexts(Camera c, long cursorX, long cursorY)
     */
    public Vector getIntersectingTexts(Camera c){
	return getIntersectingTexts(c, vx, vy);
    }

    /**returns a list of all VTexts under the mouse cursor - returns null if none
     *@param c camera
     *@param cursorX cursor X coordinate in associated virtual space (if camera is not the active one)
     *@param cursorY cursor Y coordinate in associated virtual space (if camera is not the active one)
     *@see #getIntersectingTexts(Camera c)
     */
    public Vector getIntersectingTexts(Camera c, long cursorX, long cursorY){
	synchronized(this){
	    Vector res=new Vector();
	    int index=c.getIndex();
	    Vector glyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
	    Object glyph;
	    for (int i=0;i<glyphs.size();i++){
		glyph = glyphs.elementAt(i);
		if ((glyph instanceof VText) && (intersectsVText((VText)glyph, index, cursorX, cursorY))){res.add(glyph);}
	    }
	    if (res.isEmpty()){res=null;}
	    return res;
	}
    }

    /**tells if the mouse is above VText t<br>
     * camera is supposed to be the active one (mouse cursor coordinates are taken from the active layer's camera space)
     *@param camIndex should be the active camera's index (active camera can be obtained by VirtualSpaceManager.getActiveCamera(), available through Camera.getIndex())
     *@see #intersectsVText(VText t,int camIndex, long cursorX, long cursorY)
     */
    public boolean intersectsVText(VText t,int camIndex){
	return intersectsVText(t, camIndex, vx, vy);
    }

    /**tells if the mouse is above VText t.
     *@param camIndex the camera's index (available through Camera.getIndex())
     *@param cursorX cursor X coordinate in associated virtual space (if camera is not the active one)
     *@param cursorY cursor Y coordinate in associated virtual space (if camera is not the active one)
     *@see #intersectsVText(VText t,int camIndex)
     */
    public boolean intersectsVText(VText t,int camIndex, long cursorX, long cursorY){
	boolean res=false;
	LongPoint p=t.getBounds(camIndex);
	switch (t.getTextAnchor()){
	case VText.TEXT_ANCHOR_START:{
	    if ((cursorX>=t.vx) && (cursorY>=t.vy) && (cursorX<=(t.vx+p.x)) && (cursorY<=(t.vy+p.y))){res=true;}
	    break;
	}
	case VText.TEXT_ANCHOR_MIDDLE:{
	    if ((cursorX>=t.vx-p.x/2) && (cursorY>=t.vy) && (cursorX<=(t.vx+p.x/2)) && (cursorY<=(t.vy+p.y))){res=true;}
	    break;
	}
	default:{
	    if ((cursorX<=t.vx) && (cursorY>=t.vy) && (cursorX>=(t.vx-p.x)) && (cursorY<=(t.vy+p.y))){res=true;}
	}
	}
	return res;
    }

    /** Returns a list of all VSegment instances under the mouse cursor.
     * Mouse cursor coordinates are taken from the active layer's camera space.
     *@param c camera observing the segments of interest
     *@param tolerance the segment's abstract thickness (w.r.t picking) in pixels, not virtual space units (we consider a narrow rectangular region, not an actual segment)
     *@return null if none
     *@see #getIntersectingSegments(Camera c, int jpx, int jpy, int tolerance)
     *@see #intersectsSegment(VSegment s, int tolerance, int camIndex)
     *@see #intersectsSegment(VSegment s, int jpx, int jpy, int tolerance, int camIndex)
     */
    public Vector getIntersectingSegments(Camera c, int tolerance){
	return getIntersectingSegments(c, mx, my, tolerance);
    }

    /** Returns a list of all VSegment instances under the mouse cursor.
     *@param c camera observing the segments of interest
     *@param tolerance the segment's abstract thickness (w.r.t picking) in pixels, not virtual space units (we consider a narrow rectangular region, not an actual segment)
     *@return null if none
     *@see #getIntersectingSegments(Camera c, int tolerance)
     *@see #intersectsSegment(VSegment s, int tolerance, int camIndex)
     *@see #intersectsSegment(VSegment s, int jpx, int jpy, int tolerance, int camIndex)
     */
    public Vector getIntersectingSegments(Camera c, int jpx, int jpy, int tolerance){
	synchronized(this){
	    Vector res = new Vector();
	    int index = c.getIndex();
	    Vector glyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
	    Object glyph;
	    for (int i=0;i<glyphs.size();i++){
		glyph = glyphs.elementAt(i);
		if ((glyph instanceof VSegment) && (intersectsSegment((VSegment)glyph, jpx, jpy, tolerance, index))){res.add(glyph);}
	    }
	    if (res.isEmpty()){res = null;}
	    return res;
	}
    }

    /** Indicates if the mouse cursor is above VSegment s.
     *@param camIndex indes of camera observing the segments of interest (available through Camera.getIndex())
     *@param tolerance the segment's abstract thickness (w.r.t picking) in pixels, not virtual space units (we consider a narrow rectangular region, not an actual segment)
     *@see com.xerox.VTM.engine.Camera#getIndex()
     *@see #intersectsSegment(VSegment s, int jpx, int jpy, int tolerance, int camIndex)
     *@see #getIntersectingSegments(Camera c, int jpx, int jpy, int tolerance)
     *@see #getIntersectingSegments(Camera c, int tolerance)
     */
    public boolean intersectsSegment(VSegment s, int tolerance, int camIndex){
	return intersectsSegment(s, mx, my, camIndex, tolerance);
    }

    /** Indicates if the mouse cursor is above VSegment s.
     *@param tolerance the segment's abstract thickness (w.r.t picking) in pixels, not virtual space units (we consider a narrow rectangular region, not an actual segment)
     *@see #intersectsSegment(VSegment s, int tolerance, int camIndex)
     *@see #getIntersectingSegments(Camera c, int jpx, int jpy, int tolerance)
     *@see #getIntersectingSegments(Camera c, int tolerance)
     */
    public boolean intersectsSegment(VSegment s, int jpx, int jpy, int tolerance, int camIndex){
	return s.intersects(jpx, jpy, tolerance, camIndex);
    }

    /**returns a list of all Glyphs under the mouse cursor - returns null if none<br>
     * This method is especially useful when the camera of interest is not the active camera for the associated view (i.e. another layer is active)
     *@param c should be the active camera (can be obtained by VirtualSpaceManager.getActiveCamera())
     *@see #getGlyphsUnderMouseList()
     */
    public Vector getIntersectingGlyphs(Camera c){
	synchronized(this){
	    Vector res=new Vector();
	    Vector glyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
	    Glyph glyph;
	    for (int i=0;i<glyphs.size();i++){
		glyph = (Glyph)glyphs.elementAt(i);
		if (glyph.coordInside(mx, my, c.getIndex())){res.add(glyph);}
	    }
	    if (res.isEmpty()){res = null;}
	    return res;
	}
    }

    /**double capacity of array containing glyphs under mouse*/
    void doubleCapacity(){
	Glyph[] tmpArray=new Glyph[glyphsUnderMouse.length*2];
	System.arraycopy(glyphsUnderMouse,0,tmpArray,0,glyphsUnderMouse.length);
	glyphsUnderMouse=tmpArray;
    }

    /**empty the list of glyphs under mouse*/
    void resetGlyphsUnderMouseList(VirtualSpace vs,int camIndex){
        synchronized(this){
            for (int i=0;i<glyphsUnderMouse.length;i++){
                glyphsUnderMouse[i] = null;
                maxIndex =- 1;
            }
            lastGlyphEntered = null;
            Glyph[] gl = vs.getVisibleGlyphList();
            synchronized(gl){
                for (int i=0;i<gl.length;i++){
                    try {
                        gl[i].resetMouseIn(camIndex);
                    }
                    catch (NullPointerException ex){
                        if (VirtualSpaceManager.debugModeON()){
                            System.err.println("Recovered from error when resetting list of glyphs under mouse");
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**get the list of glyphs currently under mouse (last entry is last glyph entered)
     * This returns a <em>copy</em> of the actual array managed by VCursor at the time the method is called
     * (in other words, the array returned by this method is not synchronized with the actual list over time)
     *@deprecated As of zvtm 0.9.3, replaced by getGlyphsUnderMouseList()
     *@see #getGlyphsUnderMouseList()
     */
    public Vector getGlyphsUnderMouse(){
	Vector res=new Vector();
	for (int i=0;i<=maxIndex;i++){
	    res.add(glyphsUnderMouse[i]);
	}
	return res;
    }

    /**get the list of glyphs currently under mouse (last entry is last glyph entered)
     * This returns a <em>copy</em> of the actual array managed by VCursor at the time the method is called
     * (in other words, the array returned by this method is not synchronized with the actual list over time)
     */
    public Glyph[] getGlyphsUnderMouseList(){
	if (maxIndex >= 0){
	    Glyph[] res = new Glyph[maxIndex+1];
	    System.arraycopy(glyphsUnderMouse, 0, res, 0, maxIndex+1);
	    return res;
	}
	else return new Glyph[0];
    }

    /**remove glyph g in list of glyphs under mouse if it is present (called when destroying a glyph)*/
    void removeGlyphFromList(Glyph g){
	synchronized(this){
	    int i=0;
	    boolean present=false;
	    while (i<=maxIndex){
		if (glyphsUnderMouse[i++]==g){present=true;break;}
	    }
	    while (i<=maxIndex){
		glyphsUnderMouse[i-1]=glyphsUnderMouse[i];
		i++;
	    }
	    if (present){
		maxIndex = maxIndex - 1;
		if (maxIndex<0){lastGlyphEntered=null;maxIndex=-1;}
		else {lastGlyphEntered=glyphsUnderMouse[maxIndex];}
	    }
	}
    }

    /**compute list of glyphs currently overlapped by the mouse*/
    boolean computeMouseOverList(ViewEventHandler eh,Camera c){
	return this.computeMouseOverList(eh, c, mx, my);
    }

    /**compute list of glyphs currently overlapped by the mouse (take into account lens l when unprojecting)*/
    boolean computeMouseOverList(ViewEventHandler eh,Camera c, Lens l){
	if (l != null){
	    return this.computeMouseOverList(eh, c, Math.round((((float)mx-l.sw)/gain[0])+l.sw), Math.round((((float)my-l.sh)/gain[1])+l.sh));
	}
	else {
	    return this.computeMouseOverList(eh, c, mx, my);
	}
    }
    
    /**compute list of glyphs currently overlapped by the mouse*/
    boolean computeMouseOverList(ViewEventHandler eh,Camera c, int x, int y){
	boolean res=false;
	Vector drawnGlyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
 	synchronized(drawnGlyphs){
	    synchronized(this.glyphsUnderMouse){
		try {
		    for (int i=0;i<drawnGlyphs.size();i++){
			tmpGlyph = (Glyph)drawnGlyphs.elementAt(i);
			if (tmpGlyph.isSensitive()){
			    if (checkGlyph(eh, c, x, y)){
				res = true;
			    }
			}
		    }
		}
		catch (java.util.NoSuchElementException e){
		    if (owningView.parent.debug){
			System.err.println("vcursor.computemouseoverlist "+e);
			e.printStackTrace();
		    }
		}
		catch (NullPointerException e2){
		    if (owningView.parent.debug){
			System.err.println("vcursor.computemouseoverlist null "+e2+
					   " (This might be caused by an error in enterGlyph/exitGlyph in your event handler)");
			e2.printStackTrace();
		    }
		}
	    }
 	}
	return res;
    }

    boolean checkGlyph(ViewEventHandler eh,Camera c, int x, int y){
	tmpRes = tmpGlyph.mouseInOut(x, y, c.getIndex());
	if (tmpRes == Glyph.ENTERED_GLYPH){//we've entered this glyph
	    tmpID = tmpGlyph.getID();
	    maxIndex = maxIndex + 1;
	    if (maxIndex >= glyphsUnderMouse.length){doubleCapacity();}
	    glyphsUnderMouse[maxIndex] = tmpGlyph;
	    lastGlyphEntered = tmpGlyph;
	    eh.enterGlyph(tmpGlyph);
	    return true;
	}
	else if (tmpRes == Glyph.EXITED_GLYPH){//we've exited it
	    tmpID = tmpGlyph.getID();
	    int j = 0;
	    while (j <= maxIndex){
		if (glyphsUnderMouse[j++] == tmpGlyph){break;}
	    }
	    while (j <= maxIndex){
		glyphsUnderMouse[j-1] = glyphsUnderMouse[j];
		j++;
	    }
	    maxIndex = maxIndex - 1;
	    /*required because list can be reset because we change layer and then we exit a glyph*/
	    if (maxIndex<0){lastGlyphEntered = null;maxIndex = -1;}
	    else {lastGlyphEntered = glyphsUnderMouse[maxIndex];}
	    eh.exitGlyph(tmpGlyph);
	    return true;
	}
	return false;
    }


    //for debug purpose
    public void printList(){
	System.err.print("[");
	for (int i=0;i<=maxIndex;i++){
	    System.err.print(glyphsUnderMouse[i].getID().toString()+",");
	}
	System.err.println("]");
    }

    /**project mouse cursor IN VIRTUAL SPACE wrt camera info and change origin -> JPanel coords*/
    void unProject(Camera c,ViewPanel v){
	if (sync){
	    //translate from JPanel coords
	    if (v.lens != null){//take lens into account (if set)
		v.lens.gf(mx,my,gain);
		cx = mx-(v.getSize().width/2);
		cy = (v.getSize().height/2)-my;
		v.lens.gf(cx, cy, gain);
		cx *= gain[0];
		cy *= gain[1];
//  		cx = Math.round((((float)mx-v.lens.sw)/gain[0])+v.lens.sw)-(v.getSize().width/2);
//  		cy = (v.getSize().height/2)-Math.round((((float)my-v.lens.sh)/gain[1])+v.lens.sh);
	    }
	    else {
		cx = mx-(v.getSize().width/2);
		cy = (v.getSize().height/2)-my;
	    }
	    double coef=(((double)c.focal+(double)c.altitude)/(double)c.focal);
	    //find coordinates of object's geom center wrt to camera center and project IN VIRTUAL SPACE
	    pvx=vx;
	    pvy=vy;
	    vx=Math.round((cx*coef)+c.posx);
	    vy=Math.round((cy*coef)+c.posy);
	}
    }

    public LongPoint getVSCoordinates(Camera c, ViewPanel v){
	//translate from JPanel coords
	int cx, cy;
	if (v.lens != null){//take lens into account (if set)
	    v.lens.gf(mx,my,gain);
	    cx = mx-(v.getSize().width/2);
	    cy = (v.getSize().height/2)-my;
	    v.lens.gf(cx, cy, gain);
	    cx *= gain[0];
	    cy *= gain[1];
// 	    cx = Math.round((((float)mx-v.lens.sw)/gain[0])+v.lens.sw) - (v.getSize().width/2);
// 	    cy = (v.getSize().height/2) - Math.round((((float)my-v.lens.sh)/gain[1])+v.lens.sh);
	}
	else {
	    cx = mx - (v.getSize().width/2);
	    cy = (v.getSize().height/2) - my;
	}
	double coef=(((double)c.focal+(double)c.altitude)/(double)c.focal);
	//find coordinates of object's geom center wrt to camera center and project IN VIRTUAL SPACE
	return new LongPoint(Math.round((cx*coef)+c.posx),
			     Math.round((cy*coef)+c.posy));
    }

    /**returns the cursor's X JPanel coordinate*/
    public int getPanelXCoordinate(){
	return mx;
    }

    /**returns the cursor's Y JPanel coordinate*/
    public int getPanelYCoordinate(){
	return my;
    }

    /**draw mouse cursor*/
    public void draw(Graphics2D g){
        if (isVisible){
            g.setColor(this.color);
            g.drawLine(mx-size,my,mx+size,my);
            g.drawLine(mx,my-size,mx,my+size);
        }
		if (dynaSpotActivated && showDynarea){
			g.setColor(DYNASPOT_COLOR);
			g.setComposite(DYNASPOT_TRANSLUCENCY);
			g.fillOval(mx-dynaSpotRadius, my-dynaSpotRadius, 2*dynaSpotRadius, 2*dynaSpotRadius);
			g.setComposite(Translucent.acO);
		}
	}

	/* ---- DynaSpot implementation ---- */
	
	Color DYNASPOT_COLOR = Color.RED;
	AlphaComposite DYNASPOT_TRANSLUCENCY = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
	
	int DYNASPOT_MAX_RADIUS = 40;
	int dynaSpotRadius = 0;
	
	boolean dynaSpotActivated = false;
	
	boolean showDynarea = true;
	
	/* Low-pass filter variables */
	double frequency = -1;
	long mLastSampleTime = -1;
	int xOffset = 10;
	int yOffset = 10;
	double maxDist = 2 * Math.abs(xOffset);
	LowPassFilter filter = new LowPassFilter();
	Point2D currentPos = new Point2D.Double(0, 0);
	Point2D parentPos = new Point2D.Double(0, 0);
	Point2D targetPos = new Point2D.Double(0, 0);
	Timer dstimer;
	DynaSpotTimer cursorStillDSUpdater;
	double cutoffParamA = 3; // decrease to make the region stay at max radius longer before shrinking
	double cutoffParamB = 0.0001;
	double distAway = 0;	 

	long[] dynawnes = new long[4];
	
	void initDynaSpotTimer(){
		dstimer = new Timer();
		cursorStillDSUpdater = new DynaSpotTimer(this);
		dstimer.scheduleAtFixedRate(cursorStillDSUpdater, 40, 20);
	}
	
	public void updateDynaSpotFrequency(){
		updateDynaSpotFrequency(System.currentTimeMillis());
	}

	public void updateDynaSpotFrequency(long currentTime){
		if (frequency == -1){
			frequency = 1;
		}
		else {
			if (currentTime != mLastSampleTime){
				frequency = 1000.0 / ((double)(currentTime - mLastSampleTime));
			}
		}
		mLastSampleTime = currentTime;
	}
	
	public void updateDynaSpot(int cx, int cy){
		parentPos.setLocation(cx, cy);
	}
	
	public void updateDynaSpot(){
		targetPos.setLocation(parentPos.getX() + xOffset, parentPos.getY() + yOffset);
		distAway = targetPos.distance(currentPos);
		double maxDist = 2 * Math.abs(xOffset);
		double opacity = 1.0 - Math.min(1.0, distAway / maxDist);
		filter.setCutOffFrequency(((1.0 - opacity) * cutoffParamA) + cutoffParamB);
		currentPos = filter.apply(targetPos, frequency);
		dynaSpotRadius = (int)Math.round(DYNASPOT_MAX_RADIUS * (1.0-opacity));
		owningView.repaintNow();
	}
	
	/** Set to true if the dynaspot selection region should be updated when the cursor does not move. Default is true. */
	public void setDynaSpotUpdateWhenCursorStill(boolean b){
		cursorStillDSUpdater.setEnabled(b);
	}

	/** Activate or deactivate DynaSpot behavior. */
	public void activateDynaSpot(boolean b){
		dynaSpotActivated = b;
		if (dynaSpotActivated){
			if (dstimer != null){
				dstimer.cancel();
			}
			initDynaSpotTimer();
		}
		else {
			try {
				dstimer.cancel();
				dstimer = null;
			}
			catch (NullPointerException ex){}
		}
	}

	/** Set maximum size of DynaSpot selection region. */
	public void setDynaSpotMaxRadius(int r){
		DYNASPOT_MAX_RADIUS = (r < 0) ? 0 : r;
	}

	/** Get maximum size of DynaSpot selection region. */
	public int getDynaSpotMaxRadius(){
		return DYNASPOT_MAX_RADIUS;
	}
	
	/** Set the low-pass filter's parameters. */
	public void setCutoffFrequencyParameters(double a, double b){
		cutoffParamA = a;
		cutoffParamB = b;
	}
	
	public double getCutoffFrequencyParameterA(){
		return cutoffParamA;
	}

	public double getCutoffFrequencyParameterB(){
		return cutoffParamB;
	}
	
	/** Higher values make it more difficult to reach the max radius. Speed of cursor has to be higher.*/
	public void setOffsets(int x, int y){
		xOffset = x;
		yOffset = y;
	}
	
	public Point getOffsets(){
		return new Point(xOffset, yOffset);
	}
	
	public void setDynaSpotAreaVisible(boolean b){
		showDynarea = b;
	}
	
	public boolean isDynaSpotAreaVisible(){
		return showDynarea;
	}
	
	/** Get the glyph picked by the dynaspot cursor.
	 * If several glyphs are picked by the dynaspot cursor, the best picked glyph is returned.
	 *@return null if the dynaspot cursor does not pick anything.
     *@see #getGlyphsInDynaSpotRegion(Glyph[] res, Camera c)
	 */
	public Glyph dynaPick(Camera c, int x, int y){
		Vector drawnGlyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
		Glyph res = null;
		Glyph g;
		int gumIndex = -1;
		int cgumIndex = -1;
		for (int i=0;i<drawnGlyphs.size();i++){
			g = (Glyph)drawnGlyphs.elementAt(i);
			// check if cursor hotspot is inside glyph
			// if hotspot in several glyphs, take last glyph entered (according to glyphsUnderMouse)
			cgumIndex = Utilities.indexOfGlyph(glyphsUnderMouse, g, maxIndex+1);
			if (cgumIndex > -1){
				if (cgumIndex > gumIndex){
					gumIndex = cgumIndex;
					res = g;
					if (gumIndex == maxIndex){
						// minor optimization: don't look at remaining drawnGlyphs if the one just tested
						// positive is the last glyph entered (won't be overridden anyway)
						break;
					}
				}
			}
		}
		if (res == null){
			// if cursor not inside any glyph look at dynaspot area
		    long unprojectedDSRadius = Math.round((((double)c.focal+(double)c.altitude) / (double)c.focal) * dynaSpotRadius);
			dynawnes[0] = vx - unprojectedDSRadius; // west bound
			dynawnes[1] = vy + unprojectedDSRadius; // north bound
			dynawnes[2] = vx + unprojectedDSRadius; // east bound
			dynawnes[3] = vy - unprojectedDSRadius; // south bound
			for (int i=0;i<drawnGlyphs.size();i++){
				g = (Glyph)drawnGlyphs.elementAt(i);
				// first check bounding boxes intersect (both Glyph's and DynaSpot's), and if positive perform
				// a finer grain chec with Areas
				if (g.visibleInRegion(dynawnes[0], dynawnes[1], dynawnes[2], dynawnes[3], c.getIndex()) &&
				 	g.visibleInDisc(vx, vy, unprojectedDSRadius, c.getIndex())){
						// return something only if dynapost area intersects only one glyph
						if (res == null){
							res = g;
						}
						// if dynapost area intersects more than one glyph, selection is ambiguous
						// our current policy is to select nothing
						// (hence the check of the entire array of drawnGlyphs without any break condition)
						else {
							return null;
						}
				}
			}
		}		
		return res;
	}

	/** Get the set of glyphs intersected by the cursor's dynaspot region.
	 *@param res the array to be filled with glyphs interesecting the region.
	 * If len(res) &gt; count(glyphs), then the last len(res)-count(glyphs) cells are empty.
	 * If len(res) &lt; count(glyphs), only the first len(res) glyphs are returned (meaning that some interesecting glyphs are not returned).
	 * If res is null, an array of adequate length is instantiated and returned.
	 *@return an empty array if the DynaSpot is not activated.
	 *@see #dynaPick(Camera c, int x, int y)
	 */
	public Glyph[] getGlyphsInDynaSpotRegion(Glyph[] res, Camera c){
		Vector drawnGlyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
		Glyph g;
	    long unprojectedDSRadius = Math.round((((double)c.focal+(double)c.altitude) / (double)c.focal) * dynaSpotRadius);
		dynawnes[0] = vx - unprojectedDSRadius; // west bound
		dynawnes[1] = vy + unprojectedDSRadius; // north bound
		dynawnes[2] = vx + unprojectedDSRadius; // east bound
		dynawnes[3] = vy - unprojectedDSRadius; // south bound
		if (res != null){
			int gCount = 0;
			for (int i=0;i<drawnGlyphs.size();i++){
				g = (Glyph)drawnGlyphs.elementAt(i);
				// first check bounding boxes intersect (both Glyph's and DynaSpot's)
				if (g.visibleInRegion(dynawnes[0], dynawnes[1], dynawnes[2], dynawnes[3], c.getIndex())){
					// then check circle and actual object shape
					if (g.visibleInDisc(vx, vy, unprojectedDSRadius, c.getIndex())){
						res[gCount++] = g;
					}
				}
				if (gCount >= res.length){
					// if the provided array has been filled, ignore remaining glyphs
					// (won't be returned anyway)
					break;
				}
			}
			if (gCount < res.length){
				// nullify unfilled slots (if any)
				Arrays.fill(res, gCount, res.length-1, null);
			}
		}
		else {
			Vector tres = new Vector();
			for (int i=0;i<drawnGlyphs.size();i++){
				g = (Glyph)drawnGlyphs.elementAt(i);
				// first check bounding boxes intersect (both Glyph's and DynaSpot's)
				if (g.visibleInRegion(dynawnes[0], dynawnes[1], dynawnes[2], dynawnes[3], c.getIndex())){
					// then check circle and actual object shape
					if (g.visibleInDisc(vx, vy, unprojectedDSRadius, c.getIndex())){
						tres.add(g);
					}
				}				
			}
			res = (Glyph[])tres.toArray(new Glyph[tres.size()]);
		}		
		return res;
	}

}

class DynaSpotTimer extends TimerTask{

	VCursor c;
	private boolean enabled = true;
	
	DynaSpotTimer(VCursor c){
		super();
		this.c = c;
	}
	
	public void setEnabled(boolean b){
		enabled = b;
	}
	
	public boolean isEnabled(){
		return enabled;
	}
	
	public void run(){
		if (enabled){
			c.updateDynaSpot();
		}
	}
	
}
