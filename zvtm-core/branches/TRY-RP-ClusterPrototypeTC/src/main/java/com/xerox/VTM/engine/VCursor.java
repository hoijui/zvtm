/*   FILE: VCursor.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
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
import java.awt.geom.GeneralPath;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Arrays;
import java.util.Set;

import net.claribole.zvtm.engine.ViewEventHandler;
import net.claribole.zvtm.lens.Lens;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VPath;
import net.claribole.zvtm.glyphs.DPath;
import com.xerox.VTM.glyphs.VSegment;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.Translucent;

/* For DynaSpot */
import java.util.Timer;
import java.util.TimerTask;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Area;
import net.claribole.zvtm.engine.DynaSpotListener;
import net.claribole.zvtm.engine.SelectionListener;
import net.claribole.zvtm.glyphs.Translucency;
import java.awt.Point;

/**
 * Glyph representing mouse cursor
 * @author Emmanuel Pietriga
 *
 * <h4>Using DynaSpot</h4>
 * <p>The DynaSpot behavior must be activated in VCursor, calling</p>
 * <ul><li>VCursor.activateDynaSpot(boolean b)</li></ul>
 * 
 * <p>In your ViewEventHandler, simply call VCursor.dynaPick(Camera c) wherever this makes sense. Usually this will be mouseMoved(...):</p>
 * <ul>
 *  <li>v.getMouse().dynaPick(c); // where c is the active camera</li>
 * </ul>
 * <p>This updates the list of glyphs intersected by the DynaSpot disc, and
 *    identifies the one glyph actually selected (which is returned). The method
 *    also takes care of highlighting/unhighlighting the selected glyph.</p>
 * <p><strong>Note:</strong> dynaPick() also gets called internally when DynaSpot's size changes.</p>
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
        computeDynaSpotParams();
        setSelectionListener(new DefaultSelectionAction());
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
				if ((glyph instanceof DPath) && intersectsPath((DPath)glyph, tolerance, cursorX, cursorY)){res.add(glyph);}
			}
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

    /**tells if the mouse is above DPath p
     *@param p DPath instance to be tested
     *@param tolerance the rectangular area's half width/height considered as the cursor intersecting region, in virtual space units (default tolerance is 5)
     *@param cursorX cursor X coordinate in associated virtual space (if camera is not the active one)
     *@param cursorY cursor Y coordinate in associated virtual space (if camera is not the active one)
     *@see #intersectsPath(DPath p)
     */
	public boolean intersectsPath(DPath p, int tolerance, long cursorX, long cursorY){
		if (!p.coordsInsideBoundingBox(cursorX, cursorY)){return false;}
		int dtol = tolerance * 2;
		GeneralPath gp = p.getJava2DGeneralPath();
		return gp.intersects(cursorX-dtol, cursorY-dtol, dtol, dtol) && !p.getJava2DGeneralPath().contains(cursorX-tolerance, cursorY-tolerance, dtol, dtol);
	}

    /**tells if the mouse is above DPath p (default tolerance, 5)
     *@param p DPath instance to be tested
     *@param tolerance the rectangular area's half width/height considered as the cursor intersecting region, in virtual space units (default tolerance is 5)
     *@see #intersectsPath(DPath p, int tolerance, long cursorX, long cursorY)
     */
    public boolean intersectsPath(DPath p, int tolerance){
		return intersectsPath(p, tolerance, vx, vy);
    }

    /**tells if the mouse is above DPath p (default tolerance, 5)
     *@param p DPath instance to be tested
     *@see #intersectsPath(DPath p, int tolerance, long cursorX, long cursorY)
     */
    public boolean intersectsPath(DPath p){
		return intersectsPath(p, 5, vx, vy);
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
		return p.getJava2DGeneralPath().intersects(cursorX-dtol, -cursorY-dtol, dtol, dtol) &&
			   p.getJava2DGeneralPath().contains(cursorX-tolerance, -cursorY-tolerance, tolerance, tolerance);
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

	/** Get a list of all Glyphs (including texts and paths) under the mouse cursor.
		* This method is especially useful when the camera of interest is not the active camera for the associated view (i.e. another layer is active).
		* Beware of the fact that this method returns glyphs of any kind, not just ClosedShape instances.
		* It can thus be much more computationaly expensive than getGlyphsUnderMouseList()
		*@param c a camera (the active camera can be obtained by VirtualSpaceManager.getActiveCamera())
		*@return a list of glyphs under the mouse cursor, sorted by drawing order; null if no object under the cursor.
		*@see #getGlyphsUnderMouseList()
		*/
	public Vector getIntersectingGlyphs(Camera c){
		synchronized(this){
			Vector res=new Vector();
			Vector glyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
			Glyph glyph;
			for (int i=0;i<glyphs.size();i++){
				glyph = (Glyph)glyphs.elementAt(i);
				if (glyph.coordInside(mx, my, c.getIndex(), vx, vy)){
					res.add(glyph);
				}
				else if (glyph instanceof VSegment && intersectsSegment((VSegment)glyph, 2, c.getIndex())){
					res.add(glyph);
				}
				else if (glyph instanceof VText && intersectsVText((VText)glyph, c.getIndex())){
					res.add(glyph);
				}
				else if (glyph instanceof VPath && intersectsVPath((VPath)glyph)){
					res.add(glyph);
				}
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
            Glyph[] gl = vs.getDrawingList();
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
     *@see #getIntersectingGlyphs(Camera c)
     */
    public Vector getGlyphsUnderMouse(){
	Vector res=new Vector();
	for (int i=0;i<=maxIndex;i++){
	    res.add(glyphsUnderMouse[i]);
	}
	return res;
    }

    /** Get the list of glyphs currently under the cursor. Last entry is last glyph entered.
     * This returns a <em>copy</em> of the actual array managed by VCursor at the time the method is called.
     * In other words, the array returned by this method is not synchronized with the actual list over time.
     *@return an empty array if the cursor is not over any object.
	 *@see #getIntersectingGlyphs(Camera c)
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
    
    /** Compute list of glyphs currently overlapped by the mouse. */
    boolean computeMouseOverList(ViewEventHandler eh,Camera c, int x, int y){
        boolean res=false;
        Vector drawnGlyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
        synchronized(drawnGlyphs){
            synchronized(this.glyphsUnderMouse){
                try {
                    for (int i=0;i<drawnGlyphs.size();i++){
                        tmpGlyph = (Glyph)drawnGlyphs.elementAt(i);
                        if (tmpGlyph.isSensitive() && checkGlyph(eh, c, x, y)){
                            res = true;
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

    boolean checkGlyph(ViewEventHandler eh,Camera c, int jpx, int jpy){
        tmpRes = tmpGlyph.mouseInOut(jpx, jpy, c.getIndex(), vx, vy);
        if (tmpRes == Glyph.ENTERED_GLYPH){
            //we've entered this glyph
            tmpID = tmpGlyph.getID();
            maxIndex = maxIndex + 1;
            if (maxIndex >= glyphsUnderMouse.length){doubleCapacity();}
            glyphsUnderMouse[maxIndex] = tmpGlyph;
            lastGlyphEntered = tmpGlyph;
            eh.enterGlyph(tmpGlyph);
            return true;
        }
        else if (tmpRes == Glyph.EXITED_GLYPH){
            //we've exited it
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
			switch(dynaSpotVisibility){
				case DYNASPOT_VISIBILITY_VISIBLE:{g.setComposite(dsST);break;}
				case DYNASPOT_VISIBILITY_FADEIN:{g.setComposite(Translucency.acs[(int)Math.round((1-opacity) * DYNASPOT_MAX_TRANSLUCENCY * Translucency.ACS_ACCURACY)]);break;}
				case DYNASPOT_VISIBILITY_FADEOUT:{g.setComposite(Translucency.acs[(int)Math.round(opacity * DYNASPOT_MAX_TRANSLUCENCY * Translucency.ACS_ACCURACY)]);break;}
			}
			g.fillOval(mx-dynaSpotRadius, my-dynaSpotRadius, 2*dynaSpotRadius, 2*dynaSpotRadius);
			g.setComposite(Translucent.acO);
		}
	}

	/* ---- DynaSpot implementation ---- */
	
	/* Glyphs in DynaSpot area */
	Hashtable gida = new Hashtable(20);
	
	Color DYNASPOT_COLOR = Color.LIGHT_GRAY;
	float DYNASPOT_MAX_TRANSLUCENCY = 0.3f;
	AlphaComposite dsST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)DYNASPOT_MAX_TRANSLUCENCY);
	
	/** The DynaSpot area is never displayed. */
	public static final short DYNASPOT_VISIBILITY_INVISIBLE = 0;
	/** The DynaSpot area is always displayed. */
	public static final short DYNASPOT_VISIBILITY_VISIBLE = 1;
	/** The DynaSpot area is invisible when the cursor is still, and gradually fades in when the cursor moves. */
	public static final short DYNASPOT_VISIBILITY_FADEIN = 2;
	/** The DynaSpot area is visible when the cursor is still, and gradually fades out when the cursor moves. */
	public static final short DYNASPOT_VISIBILITY_FADEOUT = 3;
	
	short dynaSpotVisibility = DYNASPOT_VISIBILITY_VISIBLE;
	
	/** Set the visibility and visual behaviour of the DynaSpot.
	 *@param v one of DYNASPOT_VISIBILITY_*
	 */
	public void setDynaSpotVisibility(short v){
		dynaSpotVisibility = v;
		showDynarea = dynaSpotVisibility != DYNASPOT_VISIBILITY_INVISIBLE;
	}

	/** Set the color of the dynaspot area.
        *@param c color of dynaspot area
        */	
	public void setDynaSpotColor(Color c){
	    DYNASPOT_COLOR = c;
	}
	
	/** Get the color of the dynaspot area.
        */	
	public Color getDynaSpotColor(){
	    return DYNASPOT_COLOR;
	}

	/** Set the translucence level of the dynaspot area.
        *@param a alpha value in [0.0-1.0]
        */	
	public void setDynaSpotTranslucence(float a){
	    DYNASPOT_MAX_TRANSLUCENCY = a;
	    dsST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)DYNASPOT_MAX_TRANSLUCENCY);
	}
	
	/** Get the translucence level of the dynaspot area.
        *@return an alpha value in [0.0-1.0]
        */	
	public float getDynaSpotTranslucence(){
	    return DYNASPOT_MAX_TRANSLUCENCY;
	}
	
	int DYNASPOT_MAX_RADIUS = 16;

	int LAG_TIME = 120;
	int REDUC_TIME = 180;
	
	public void setDynaSpotLagTime(int t){
	    LAG_TIME = t;
	}

	public int getDynaSpotLagTime(){
	    return LAG_TIME;
	}

	public void setDynaSpotReducTime(int t){
	    REDUC_TIME = t;
	    computeDynaSpotParams();
	}

	public int getDynaSpotReducTime(){
	    return REDUC_TIME;
	}
	
	int MIN_SPEED = 100;
	int MAX_SPEED = 300;
	
	/* dynaspot parameters */
	float ds_aa;
	float ds_ab;
	float ds_ra;
	float ds_rb;
	
	void computeDynaSpotParams(){
	    ds_aa = DYNASPOT_MAX_RADIUS / (float)(MAX_SPEED-MIN_SPEED);
    	ds_ab = -DYNASPOT_MAX_RADIUS * MIN_SPEED / (float)(MAX_SPEED-MIN_SPEED);
    	// linear drop-off
//    	ds_ra = -DYNASPOT_MAX_RADIUS / (float)REDUC_TIME;
        // co-exponential drop-off
    	ds_ra = -DYNASPOT_MAX_RADIUS / (float)Math.pow(REDUC_TIME,2);
    	ds_rb = DYNASPOT_MAX_RADIUS;
	}
	
	int dynaSpotRadius = 0;
		
	boolean dynaSpotActivated = false;
	
	boolean showDynarea = true;
	
	Timer dstimer;
	DynaSpotTimer dynaspotTimer;

	double opacity = 1.0f;

	long[] dynawnes = new long[4];
	
	Ellipse2D dynaspotVSshape = new Ellipse2D.Double(0, 0, 1, 1);
	
	void initDynaSpotTimer(){
		dstimer = new Timer();
		dynaspotTimer = new DynaSpotTimer(this);
		dstimer.scheduleAtFixedRate(dynaspotTimer, 40, 20);
	}
	
	static final int NB_SPEED_POINTS = 4;
	
	long[] cursor_time = new long[NB_SPEED_POINTS];
	int[] cursor_x = new int[NB_SPEED_POINTS];
	int[] cursor_y = new int[NB_SPEED_POINTS];

	float[] speeds = new float[NB_SPEED_POINTS-1];
	
	float mean_speed = 0;
	
	boolean dynaspot_triggered = false;
	
	long lastTimeAboveMinSpeed = -1;
	
	boolean reducing = false;
	long reducStartTime = 0;

	public synchronized void updateDynaSpot(long currentTime){
		// compute mean speed over last 3 points
		for (int i=1;i<NB_SPEED_POINTS;i++){
			cursor_time[i-1] = cursor_time[i];
			cursor_x[i-1] = cursor_x[i];
			cursor_y[i-1] = cursor_y[i];
		}
		cursor_time[NB_SPEED_POINTS-1] = currentTime;
		cursor_x[NB_SPEED_POINTS-1] = this.mx;
		cursor_y[NB_SPEED_POINTS-1] = this.my;
		for (int i=0;i<speeds.length;i++){
			speeds[i] = (float)Math.sqrt(Math.pow(cursor_x[i+1]-cursor_x[i],2)+Math.pow(cursor_y[i+1]-cursor_y[i],2)) / (float)(cursor_time[i+1]-cursor_time[i]);
		}
		mean_speed = 0;
		for (int i=0;i<speeds.length;i++){
			mean_speed += speeds[i];
		}
		mean_speed = mean_speed / (float)speeds.length * 1000;
		// adapt dynaspot area accordingly
		if (dynaspot_triggered){
		 	if (mean_speed > MIN_SPEED){
				lastTimeAboveMinSpeed = System.currentTimeMillis();
				if (mean_speed > MAX_SPEED){
					if (dynaSpotRadius < DYNASPOT_MAX_RADIUS){
						updateDynaSpotArea(DYNASPOT_MAX_RADIUS);
					}				
				}
			}
			else {
				if (lastTimeAboveMinSpeed > 0 && currentTime - lastTimeAboveMinSpeed >= LAG_TIME){
					lastTimeAboveMinSpeed = -1;
					reducing = true;
					reducStartTime = currentTime;
					dynaspot_triggered = false;
				}
			}		
		}
		else {
		 	if (mean_speed > MIN_SPEED){
				lastTimeAboveMinSpeed = System.currentTimeMillis();
				dynaspot_triggered = true;
				if (mean_speed > MAX_SPEED){
					if (dynaSpotRadius < DYNASPOT_MAX_RADIUS){
						updateDynaSpotArea(DYNASPOT_MAX_RADIUS);
					}				
				}
				else {
					updateDynaSpotArea(Math.round(ds_aa*mean_speed+ds_ab));
				}
			}
			else if (reducing){
				if (currentTime-reducStartTime >= REDUC_TIME){
					updateDynaSpotArea(0);
					reducing = false;
				}
				else {
				    // linear drop-off
//					updateDynaSpotArea(Math.round(ds_ra*(currentTime-reducStartTime)+ds_rb));
                    // co-exponential drop-off
					updateDynaSpotArea(Math.round(ds_ra*(float)Math.pow(currentTime-reducStartTime,2)+ds_rb));
				}
			}
		}
		owningView.repaintNow();
    }
    
	void updateDynaSpotArea(int r){
		dynaSpotRadius = r;
		dynaPick();
		if (dsl != null){
			dsl.spotSizeChanged(this, dynaSpotRadius);
		}
	}
	
	public int getDynaSpotRadius(){
	    return dynaSpotRadius;
	}
	
	DynaSpotListener dsl;

	public void setDynaSpotListener(DynaSpotListener dsl){
		this.dsl = dsl;
	}
	
	public DynaSpotListener getDynaSpotListener(){
		return dsl;
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
	
	public boolean isDynaSpotActivated(){
	    return dynaSpotActivated;
	}

	/** Set maximum size of DynaSpot selection region. */
	public void setDynaSpotMaxRadius(int r){
		DYNASPOT_MAX_RADIUS = (r < 0) ? 0 : r;
		computeDynaSpotParams();
	}

	/** Get maximum size of DynaSpot selection region. */
	public int getDynaSpotMaxRadius(){
		return DYNASPOT_MAX_RADIUS;
	}

    Camera refToCam4DynaPick = null;

	/** Compute the list of glyphs picked by the dynaspot cursor.
	 * The best picked glyph is returned.
	 *@see #dynaPick(Camera c)
	 */
	void dynaPick(){
        dynaPick(refToCam4DynaPick);
    }
    
    Glyph lastDynaPicked = null;
    
    SelectionListener sl;
    
    /** Set a Selection Listener callback triggered when a glyph gets selected/unselected by DynaSpot.
        *@param sl set to null to remove
        */
    public void setSelectionListener(SelectionListener sl){
        this.sl = sl;
    }
    
    /** Get the Selection Listener callback triggered when a glyph gets selected/unselected by DynaSpot.
        *@return null if none set.
        */
    public SelectionListener getSelectionListener(){
        return this.sl;
    }
    
	/** Compute the list of glyphs picked by the dynaspot cursor.
	 * The best picked glyph is returned.
	 *@return null if the dynaspot cursor does not pick anything.
     *@see #dynaPick()
	 */
	public Glyph dynaPick(Camera c){
	    if (c == null){
	        return null;
	    }
	    refToCam4DynaPick = c;
		Vector drawnGlyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
		Glyph selectedGlyph = null;
	    // initialized at -1 because we don't know have any easy way to compute some sort of "initial" distance for comparison
	    // when == 0, means that the cursor's hotspot is actually inside the glyph
	    // if > 0 at the end of the loop, dynaspot intersects at least one glyph (but cursor hotspot is not inside any glyph)
	    // if == -1, nothing is intersected by the dynaspot area
		double distanceToSelectedGlyph = -1;
		Glyph g;
		int gumIndex = -1;
		int cgumIndex = -1;
	    long unprojectedDSRadius = Math.round((((double)c.focal+(double)c.altitude) / (double)c.focal) * dynaSpotRadius);
		dynawnes[0] = vx - unprojectedDSRadius; // west bound
		dynawnes[1] = vy + unprojectedDSRadius; // north bound
		dynawnes[2] = vx + unprojectedDSRadius; // east bound
		dynawnes[3] = vy - unprojectedDSRadius; // south bound
		dynaspotVSshape.setFrame(dynawnes[0], dynawnes[3], 2*unprojectedDSRadius, 2*unprojectedDSRadius);
		for (int i=0;i<drawnGlyphs.size();i++){
			g = (Glyph)drawnGlyphs.elementAt(i);
			if (!g.isSensitive()){
			    continue;
			}
			// check if cursor hotspot is inside glyph
			// if hotspot in several glyphs, selected glyph will be the last glyph entered (according to glyphsUnderMouse)
			cgumIndex = Utilities.indexOfGlyph(glyphsUnderMouse, g, maxIndex+1);
			if (cgumIndex > -1){
				if (cgumIndex > gumIndex){
					gumIndex = cgumIndex;
					selectedGlyph = g;
					distanceToSelectedGlyph = 0;
				}
				gida.put(g.getID(), g);
			}
			// if cursor hotspot is not inside the glyph, check bounding boxes (Glyph's and DynaSpot's),
			// if they do intersect, peform a finer-grain chec with Areas
			else if (g.visibleInRegion(dynawnes[0], dynawnes[1], dynawnes[2], dynawnes[3], c.getIndex()) &&
			 	g.visibleInDisc(vx, vy, unprojectedDSRadius, dynaspotVSshape, c.getIndex(), mx, my, dynaSpotRadius)){
                // glyph intersects dynaspot area    
                gida.put(g.getID(), g);
                double d = Math.sqrt(Math.pow(g.vx-vx,2)+Math.pow(g.vy-vy,2));
                if (distanceToSelectedGlyph == -1 || d < distanceToSelectedGlyph){
                    selectedGlyph = g;
                    distanceToSelectedGlyph = d;
                }
			}
			else {
			    // glyph does not intersect dynaspot area
			    if (gida.containsKey(g.getID())){
    		        gida.remove(g.getID());
    		        if (sl != null){
    		            sl.glyphSelected(lastDynaPicked, false);
    		        }
			    }
			}
		}
        if (selectedGlyph != null && sl != null){
            sl.glyphSelected(selectedGlyph, true);
        }
        if (lastDynaPicked != null && selectedGlyph != lastDynaPicked && sl != null){
            sl.glyphSelected(lastDynaPicked, false);
        }
        lastDynaPicked = selectedGlyph;
        return selectedGlyph;
	}

	/** Get the set of glyphs intersected by the cursor's dynaspot region.
	 *@return a set of Glyph IDs
	 *@see #dynaPick(Camera c)
	 */
	public Set getGlyphsInDynaSpotRegion(Camera c){
		return gida.keySet();
	}

}

class DynaSpotTimer extends TimerTask{

	VCursor c;
	
	DynaSpotTimer(VCursor c){
		super();
		this.c = c;
	}
	
	public void run(){
		c.updateDynaSpot(System.currentTimeMillis());
	}
	
}

class DefaultSelectionAction implements SelectionListener {
    
    public void glyphSelected(Glyph g, boolean b){
        g.highlight(b, null);        
    }
    
}
