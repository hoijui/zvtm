/*   FILE: ViewPanel.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
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
 * $Id: ViewPanel.java,v 1.30 2006/06/01 07:11:56 epietrig Exp $
 */

package com.xerox.VTM.engine;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.JPanel;

import net.claribole.zvtm.lens.Lens;
import net.claribole.zvtm.engine.ViewEventHandler;

import com.xerox.VTM.glyphs.Glyph;

/**
 * Each view runs in its own thread - uses double buffering
 * @author Emmanuel Pietriga
 **/
public abstract class ViewPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    /**draw no oval between point where we started dragging the mouse and current point*/
    public final static short NONE=0;
    /**draw an oval between point where we started dragging the mouse and current point*/
    public final static short OVAL=1;
    /**should a circle between point where we started dragging the mouse and current point*/
    public final static short CIRCLE=2;

    Thread runView;

    /**list of cameras used in this view*/
    public Camera[] cams;

    /**active layer in this view (corresponds to the index of a camera in cams[])*/
    public int activeLayer=0;

    /**view*/
    public View parent;

    /**mouse is inside this component*/
    boolean inside=false;

    /**active means that this view should be repainted on a regular basis*/
    boolean active=true;

    /**send events to this class (application side)*/
    ViewEventHandler evH;

    /**repaint only if necessary (when there are animations, when the mouse moves...)*/
    boolean repaintNow=true;

    /**only repaint mouse cursor (using XOR mode)*/
    boolean updateMouseOnly=false;

    /**should repaint this view on a regular basis or not (even if not activated, but does not apply to iconified views)*/
    boolean alwaysRepaintMe=false;

    /**for blank mode (methods to enter/exit blank mode are in View)*/
    boolean notBlank=true;
    Color blankColor=null;

    /**should compute the list of glyphs under mouse each time the view is repainted (default is false)
     * <br>this list is anyway computed each time the mouse is moved
     */
    boolean computeListAtEachRepaint=false;

    /**minimum time between two consecutive repaint+sleep   (refresh rate)*/
    int frameTime = 25;

    /**minimum time a view is put to sleep between after it has been painted<br>
     * (even if it took more than frameTime to repaint)
     */
    int minimumSleepTime = 10;

    /**If a view does not need to be repainted in the current loop,
     * go to sleep frameTime + noRepaintAdditionalTime before checking again
     */
    int noRepaintAdditionalTime = 20;

    /**when view is iconified/deactivated, go sleep much longer*/
    int deactiveTime = 500;

    /**absolute time at which the last painting loop has begun<br>used for an adaptative sleeping time in order to spend max. frameTime ms in each iteration*/
    public long loopStartTime;
    /**duration of the last painting loop<br>used for an adaptative sleeping time in order to spend max. frameTime ms in each iteration*/
    public long loopTotalTime;
    /**how long this thread should sleep in order to spend max. frameTime ms in each iteration*/
    long timeToSleep;

    /**view's backgorund color (default is lightGray)*/
    Color backColor=Color.lightGray;

    /**graphics2d's original stroke -passed to each glyph in case it needs to modifiy the stroke when painting itself*/
    Stroke standardStroke;

    /**graphics2d's original affine transform -passed to each glyph in case it needs to modifiy the affine transform when painting itself*/
    AffineTransform standardTransform;

    float uncoef;
    long viewW;
    long viewH;
    long viewWC, viewNC, viewEC, viewSC;

    int[] visibilityPadding = {0,0,0,0};

    Dimension size;

    //index of a camera (passed to drawMe())
    int camIndex;

    int beginAt=0; //index of first glyph that entirely fills the view (reset for each layer) when scanned in reverse order (list of drawnGlyphs)
    Vector drawnGlyphs;
    Glyph gl;
    Glyph[] gll;

    /**tells thread to update font*/
    boolean updateFont=false;

    /**tells thread to update antaliasing status*/
    boolean updateAntialias=true;

    /**should the view be antialiased*/
    boolean antialias=false;

    /**Previous coordinates of the mouse.
     * Used to erase old cursor before repainting in XOR mode.
     * Also used to resetMouseInsidePortals
     */
    protected int oldX=0;
    protected int oldY=0;

    /**drag-segment/rectangle coords*/
    protected int origDragx,origDragy,curDragx,curDragy;
    /**should we draw a line between point where we started dragging the mouse and current point*/
    boolean drawDrag=false;
    /**should we draw a rectangle between point where we started dragging the mouse and current point*/
    boolean drawRect=false;
    /**should we draw an oval between point where we started dragging the mouse and current point*/
    boolean drawOval=false;
    /**should the oval be a circle or any oval*/
    boolean circleOnly=false;
    
    /**VTM cursor is drawn only when AWT cursor is set to CUSTOM_CURSOR*/
    protected boolean drawVTMcursor=true;
    /**the AWT cursor*/
    protected Cursor awtCursor;

    /**Lens (fisheye, etc.)*/
    protected Lens lens;

    public abstract void stop();
    
    //graphics context used to draw the offscreen image
    Graphics2D g2;

    /**set application class to which events should be sent*/
    void setEventHandler(ViewEventHandler eh){
	evH = eh;
    }

    /* -------------------- PORTALS ------------------- */

    // if = 0, not inside any portal,
    // if = N > 0, inside N portals
    int cursorInsidePortals = 0;
    
    void resetCursorInsidePortals(){
	synchronized(this){
	    cursorInsidePortals = 0;
	    for (int i=0;i<parent.portals.length;i++){
		if (parent.portals[i].coordInside(oldX, oldY)){
		    cursorInsidePortals += 1;
		}
	    }
	}
    }

    void updateCursorInsidePortals(int x, int y){
	synchronized(this){
	    for (int i=0;i<parent.portals.length;i++){
		cursorInsidePortals += parent.portals[i].cursorInOut(x, y);
	    }
	}
    }

    /* -------------------- CURSOR ------------------- */
    
    /**Set the cursor.
     * Either the ZVTM cursor or one of the default AWT cursors.
     *@param cursorType any of the cursor type values declared in java.awt.Cursor, such as DEFAULT_CURSOR, CROSSHAIR_CURSOR HAND_CURSOR, etc. To get the ZVTM cursor, use Cursor.CUSTOM_CURSOR.
     */
    protected void setAWTCursor(int cursorType){
	if (cursorType == Cursor.CUSTOM_CURSOR){
	    /* custom cursor is used to designate the VTM cursor.
	       It is transparent (cursor is painted at the end of each
	       loop by the view itself (as a cross)) */
	    //create a BufferedImage with transparent background
	    BufferedImage cImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	    try {
		awtCursor = Toolkit.getDefaultToolkit().createCustomCursor(cImage,
									   new Point(0,0),
									   "zvtmCursor");
		drawVTMcursor = true;
	    }
	    catch(IndexOutOfBoundsException e){
		if (parent.parent.debug){
		    System.err.println("Error while creating custom cursor " + e);
		    awtCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
		    drawVTMcursor = false;
		}
	    }
	}
	else {
	    /* an AWT predefined cursor - forward the request to AWT and
	       signal ZVTM that it should not paint its own cursor */
	    drawVTMcursor = false;
	    try {
		awtCursor = Cursor.getPredefinedCursor(cursorType);
	    }
	    catch(IndexOutOfBoundsException e){
		if (parent.parent.debug){
		    System.err.println("Error while creating AWT cursor " + e);
		    awtCursor=new Cursor(Cursor.DEFAULT_CURSOR);
		}
	    }
	}
	this.setCursor(awtCursor);
    }

    /**Set the cursor.
     * Replaces the ZVTM cursor by a bitmap cursor similar to the default AWT cursors.
     *@param c an AWT cursor instantiated e.g. by calling java.awt.Toolkit.createCustomCursor(Image cursor, Point hotSpot, String name)
     */
    protected void setAWTCursor(Cursor c){
	awtCursor = c;
	drawVTMcursor = false;
	this.setCursor(awtCursor);
    }
    
    /**true will draw a segment between origin of drag and current cursor pos until drag is finished (still visible for backward compatibility reasons - should use setDrawSegment instead)*/
    public void setDrawDrag(boolean b){
	curDragx=origDragx;
	curDragy=origDragy;
	drawDrag=b;
	parent.repaintNow();
    }

    /**true will draw a segment between origin of drag and current cursor pos until drag is finished*/
    public void setDrawSegment(boolean b){
	curDragx=origDragx;
	curDragy=origDragy;
	drawDrag=b;
	parent.repaintNow();
    }

    /**true will draw a rectangle between origin of drag and current cursor pos until drag is finished*/
    public void setDrawRect(boolean b){
	curDragx=origDragx;
	curDragy=origDragy;
	drawRect=b;
	parent.repaintNow();
    }

    /**draw a circle between origin of drag and current cursor pos until drag is finished (drag segment represents the radius of the circle, not its diameter) - use OVAL for any oval, CIRCLE for circle, NONE to stop drawing it*/
    public void setDrawOval(short s){
	curDragx=origDragx;
	curDragy=origDragy;
	if (s==OVAL){drawOval=true;circleOnly=false;}
	else if (s==CIRCLE){drawOval=true;circleOnly=true;}
	else if (s==NONE){drawOval=false;}
	parent.repaintNow();
    }

    /**send event to application event handler*/
    public void mousePressed(MouseEvent e){
	if (evH == null){return;}
	int whichButton=e.getModifiers();
	origDragx=e.getX();origDragy=e.getY();  //store these anyway, since we have no way to know which button (if any) sets drawDrag mode
	if ((whichButton & InputEvent.BUTTON1_MASK)==InputEvent.BUTTON1_MASK){
	    if (e.isShiftDown()) {
		if (e.isControlDown()) {evH.press1(this,ViewEventHandler.CTRL_SHIFT_MOD,e.getX(),e.getY(), e);}
		else if (e.isMetaDown()) {evH.press1(this,ViewEventHandler.META_SHIFT_MOD,e.getX(),e.getY(), e);}
		else if (e.isAltDown()) {evH.press1(this,ViewEventHandler.ALT_SHIFT_MOD,e.getX(),e.getY(), e);}
		else {evH.press1(this,ViewEventHandler.SHIFT_MOD,e.getX(),e.getY(), e);}
	    }
	    else if (e.isControlDown()){
		evH.press1(this,ViewEventHandler.CTRL_MOD,e.getX(),e.getY(), e);
	    }
	    else {
		if (e.isMetaDown()) {evH.press1(this,ViewEventHandler.META_MOD,e.getX(),e.getY(), e);}
		else if (e.isAltDown()) {evH.press1(this,ViewEventHandler.ALT_MOD,e.getX(),e.getY(), e);}
		else {evH.press1(this,ViewEventHandler.NO_MODIFIER,e.getX(),e.getY(), e);}
	    }
	}
	else {
	    if ((whichButton & InputEvent.BUTTON2_MASK)==InputEvent.BUTTON2_MASK){
		if (e.isShiftDown()) {
		    if (e.isControlDown()) {evH.press2(this,ViewEventHandler.CTRL_SHIFT_MOD,e.getX(),e.getY(), e);}
		    else if (e.isMetaDown()) {evH.press2(this,ViewEventHandler.META_SHIFT_MOD,e.getX(),e.getY(), e);}
		    else if (e.isAltDown()) {evH.press2(this,ViewEventHandler.ALT_SHIFT_MOD,e.getX(),e.getY(), e);}
		    else {evH.press2(this,ViewEventHandler.SHIFT_MOD,e.getX(),e.getY(), e);}
		}
		else if (e.isControlDown()){
		    evH.press2(this,ViewEventHandler.CTRL_MOD,e.getX(),e.getY(), e);
		}
		else {
		    if (e.isMetaDown()) {evH.press2(this,ViewEventHandler.META_MOD,e.getX(),e.getY(), e);}
		    else if (e.isAltDown()) {evH.press2(this,ViewEventHandler.ALT_MOD,e.getX(),e.getY(), e);}
		    else {evH.press2(this,ViewEventHandler.NO_MODIFIER,e.getX(),e.getY(), e);}
		}
	    }
	    else {
		if ((whichButton & InputEvent.BUTTON3_MASK)==InputEvent.BUTTON3_MASK){
		    if (e.isShiftDown()) {
			if (e.isControlDown()) {evH.press3(this,ViewEventHandler.CTRL_SHIFT_MOD,e.getX(),e.getY(), e);}
			else if (e.isMetaDown()) {evH.press3(this,ViewEventHandler.META_SHIFT_MOD,e.getX(),e.getY(), e);}
			else if (e.isAltDown()) {evH.press3(this,ViewEventHandler.ALT_SHIFT_MOD,e.getX(),e.getY(), e);}
			else {evH.press3(this,ViewEventHandler.SHIFT_MOD,e.getX(),e.getY(), e);}
		    }
		    else if (e.isControlDown()){
			evH.press3(this,ViewEventHandler.CTRL_MOD,e.getX(),e.getY(), e);
		    }
		    else {
			if (e.isMetaDown()) {evH.press3(this,ViewEventHandler.META_MOD,e.getX(),e.getY(), e);}
			else if (e.isAltDown()) {evH.press3(this,ViewEventHandler.ALT_MOD,e.getX(),e.getY(), e);}
			else {evH.press3(this,ViewEventHandler.NO_MODIFIER,e.getX(),e.getY(), e);}
		    }
		}
	    }
	}
    }

    /**send event to application event handler*/
    public void mouseClicked(MouseEvent e){
	if (evH == null){return;}
	int whichButton=e.getModifiers();
	if ((whichButton & InputEvent.BUTTON1_MASK)==InputEvent.BUTTON1_MASK){
	    if (e.isShiftDown()) {
		if (e.isControlDown()) {evH.click1(this,ViewEventHandler.CTRL_SHIFT_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
		else if (e.isMetaDown()) {evH.click1(this,ViewEventHandler.META_SHIFT_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
		else if (e.isAltDown()) {evH.click1(this,ViewEventHandler.ALT_SHIFT_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
		else {evH.click1(this,ViewEventHandler.SHIFT_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
	    }
	    else if (e.isControlDown()) {
		evH.click1(this,ViewEventHandler.CTRL_MOD,e.getX(),e.getY(),e.getClickCount(), e);
	    }
	    else {
		if (e.isMetaDown()) {evH.click1(this,ViewEventHandler.META_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
		else if (e.isAltDown()) {evH.click1(this,ViewEventHandler.ALT_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
		else {evH.click1(this,ViewEventHandler.NO_MODIFIER,e.getX(),e.getY(),e.getClickCount(), e);}
	    }
	}
	else {
	    if ((whichButton & InputEvent.BUTTON2_MASK)==InputEvent.BUTTON2_MASK){
		if (e.isShiftDown()) {
		    if (e.isControlDown()) {evH.click2(this,ViewEventHandler.CTRL_SHIFT_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
		    else if (e.isMetaDown()) {evH.click2(this,ViewEventHandler.META_SHIFT_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
		    else if (e.isAltDown()) {evH.click2(this,ViewEventHandler.ALT_SHIFT_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
		    else {evH.click2(this,ViewEventHandler.SHIFT_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
		}
		else if (e.isControlDown()) {
		    evH.click2(this,ViewEventHandler.CTRL_MOD,e.getX(),e.getY(),e.getClickCount(), e);
		}
		else {
		    if (e.isMetaDown()) {evH.click2(this,ViewEventHandler.META_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
		    else if (e.isAltDown()) {evH.click2(this,ViewEventHandler.ALT_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
		    else {evH.click2(this,ViewEventHandler.NO_MODIFIER,e.getX(),e.getY(),e.getClickCount(), e);}
		}
	    }
	    else {
		if ((whichButton & InputEvent.BUTTON3_MASK)==InputEvent.BUTTON3_MASK){
		    if (e.isShiftDown()) {
			if (e.isControlDown()) {evH.click3(this,ViewEventHandler.CTRL_SHIFT_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
			else if (e.isMetaDown()) {evH.click3(this,ViewEventHandler.META_SHIFT_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
			else if (e.isAltDown()) {evH.click3(this,ViewEventHandler.ALT_SHIFT_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
			else {evH.click3(this,ViewEventHandler.SHIFT_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
		    }
		    else if (e.isControlDown()) {
			evH.click3(this,ViewEventHandler.CTRL_MOD,e.getX(),e.getY(),e.getClickCount(), e);
		    }
		    else {
			if (e.isMetaDown()) {evH.click3(this,ViewEventHandler.META_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
			else if (e.isAltDown()) {evH.click3(this,ViewEventHandler.ALT_MOD,e.getX(),e.getY(),e.getClickCount(), e);}
			else {evH.click3(this,ViewEventHandler.NO_MODIFIER,e.getX(),e.getY(),e.getClickCount(), e);}
		    }
		}
	    }
	}
    }

    /**send event to application event handler*/
    public void mouseReleased(MouseEvent e){
	if (evH == null){return;}
	int whichButton=e.getModifiers();
	if ((whichButton & InputEvent.BUTTON1_MASK)==InputEvent.BUTTON1_MASK){
	    if (e.isShiftDown()) {
		if (e.isControlDown()) {evH.release1(this,ViewEventHandler.CTRL_SHIFT_MOD,e.getX(),e.getY(), e);}
		else if (e.isMetaDown()) {evH.release1(this,ViewEventHandler.META_SHIFT_MOD,e.getX(),e.getY(), e);}
		else if (e.isAltDown()) {evH.release1(this,ViewEventHandler.ALT_SHIFT_MOD,e.getX(),e.getY(), e);}
		else {evH.release1(this,ViewEventHandler.SHIFT_MOD,e.getX(),e.getY(), e);}
	    }
	    else if (e.isControlDown()) {
		evH.release1(this,ViewEventHandler.CTRL_MOD,e.getX(),e.getY(), e);
	    }
	    else {
		if (e.isMetaDown()) {evH.release1(this,ViewEventHandler.META_MOD,e.getX(),e.getY(), e);}
		else if (e.isAltDown()) {evH.release1(this,ViewEventHandler.ALT_MOD,e.getX(),e.getY(), e);}
		else {evH.release1(this,ViewEventHandler.NO_MODIFIER,e.getX(),e.getY(), e);}
	    }
	}
	else {
	    if ((whichButton & InputEvent.BUTTON2_MASK)==InputEvent.BUTTON2_MASK){
		if (e.isShiftDown()) {
		    if (e.isControlDown()) {evH.release2(this,ViewEventHandler.CTRL_SHIFT_MOD,e.getX(),e.getY(), e);}
		    else if (e.isMetaDown()) {evH.release2(this,ViewEventHandler.META_SHIFT_MOD,e.getX(),e.getY(), e);}
		    else if (e.isAltDown()) {evH.release2(this,ViewEventHandler.ALT_SHIFT_MOD,e.getX(),e.getY(), e);}
		    else {evH.release2(this,ViewEventHandler.SHIFT_MOD,e.getX(),e.getY(), e);}
		}
		else if (e.isControlDown()) {
		    evH.release2(this,ViewEventHandler.CTRL_MOD,e.getX(),e.getY(), e);
		}
		else {
		    if (e.isMetaDown()) {evH.release2(this,ViewEventHandler.META_MOD,e.getX(),e.getY(), e);}
		    else if (e.isAltDown()) {evH.release2(this,ViewEventHandler.ALT_MOD,e.getX(),e.getY(), e);}
		    else {evH.release2(this,ViewEventHandler.NO_MODIFIER,e.getX(),e.getY(), e);}
		}
	    }
	    else {
		if ((whichButton & InputEvent.BUTTON3_MASK)==InputEvent.BUTTON3_MASK){
		    if (e.isShiftDown()) {
			if (e.isControlDown()) {evH.release3(this,ViewEventHandler.CTRL_SHIFT_MOD,e.getX(),e.getY(), e);}
			else if (e.isMetaDown()) {evH.release3(this,ViewEventHandler.META_SHIFT_MOD,e.getX(),e.getY(), e);}
			else if (e.isAltDown()) {evH.release3(this,ViewEventHandler.ALT_SHIFT_MOD,e.getX(),e.getY(), e);}
			else {evH.release3(this,ViewEventHandler.SHIFT_MOD,e.getX(),e.getY(), e);}
		    }
		    else if (e.isControlDown()) {
			evH.release3(this,ViewEventHandler.CTRL_MOD,e.getX(),e.getY(), e);
		    }
		    else {
			if (e.isMetaDown()) {evH.release3(this,ViewEventHandler.META_MOD,e.getX(),e.getY(), e);}
			else if (e.isMetaDown()) {evH.release3(this,ViewEventHandler.ALT_MOD,e.getX(),e.getY(), e);}
			else {evH.release3(this,ViewEventHandler.NO_MODIFIER,e.getX(),e.getY(), e);}
		    }
		}
	    }
	}
    }

    /**mouse entered this view*/
    public void mouseEntered(MouseEvent e){
	active=true; //make the view active any time the mouse enters it
	repaintNow=true;
	inside=true;
	parent.parent.setActiveView(this.parent);
	/* requesting parent focus was only used to get keyboard/mouse wheel events in IViews,
	   better to manage this explcitly when internal frames get selected as doing it
	   here has unwanted side effects in some UIs that mix internal and external frames */
	//parent.requestFocus();
    }

    /**mouse exited this view*/
    public void mouseExited(MouseEvent e){
	inside=false;
	if ((!parent.isSelected()) && (!alwaysRepaintMe)){active=false;}
    }


    public void mouseMoved(MouseEvent e){
	try {
	    if (parent.parent.mouseSync){
		synchronized(this){
		    parent.mouse.moveTo(e.getX(),e.getY());
		    parent.mouse.unProject(cams[activeLayer],this);  //we project the mouse cursor wrt the appropriate coord sys
		    updateMouseOnly=true;
		}
		parent.mouse.propagateMove();  //translate glyphs sticked to mouse
		// find out is the cursor is inside one (or more) portals
		updateCursorInsidePortals(e.getX(), e.getY());
		// forward mouseMoved event to View event handler
		if (evH != null){
		    if (parent.notifyMouseMoved){
			evH.mouseMoved(this, e.getX(), e.getY(), e);
		    }
		    if (parent.mouse.isSensitive()){
			if (parent.mouse.computeMouseOverList(evH, cams[activeLayer], this.lens)){
			    parent.repaintNow();
			}
		    }
		}
	    }
	}
	catch (NullPointerException ex) {if (parent.parent.debug){System.err.println("viewpanel.mousemoved "+ex);}}
    }
    
    /**send event to application event handler*/
    public void mouseDragged(MouseEvent e){
	int whichButton=e.getModifiers();
	int buttonNumber=0;
	try {
	    if (parent.parent.mouseSync){
 		parent.mouse.moveTo(e.getX(), e.getY());
 		parent.mouse.unProject(cams[activeLayer],this);  //we project the mouse cursor wrt the appropriate coord sys
 		parent.mouse.propagateMove();  //translate glyphs sticked to mouse
		// find out is the cursor is inside one (or more) portals
		updateCursorInsidePortals(e.getX(), e.getY());
		if (evH != null){
		    if ((whichButton & InputEvent.BUTTON1_MASK)==InputEvent.BUTTON1_MASK){buttonNumber=1;}
		    else {
			if ((whichButton & InputEvent.BUTTON2_MASK)==InputEvent.BUTTON2_MASK){buttonNumber=2;}
			else {
			    if ((whichButton & InputEvent.BUTTON3_MASK)==InputEvent.BUTTON3_MASK){buttonNumber=3;}
			}
		    }
		    if (e.isShiftDown()) {//event sent after unproject because we need to compute coord in virtual space
			if (e.isControlDown()) {evH.mouseDragged(this,ViewEventHandler.CTRL_SHIFT_MOD,buttonNumber,e.getX(),e.getY(), e);}
			else if (e.isMetaDown()){evH.mouseDragged(this,ViewEventHandler.META_SHIFT_MOD,buttonNumber,e.getX(),e.getY(), e);}
			else if (e.isAltDown()){evH.mouseDragged(this,ViewEventHandler.ALT_SHIFT_MOD,buttonNumber,e.getX(),e.getY(), e);}
			else {evH.mouseDragged(this,ViewEventHandler.SHIFT_MOD,buttonNumber,e.getX(),e.getY(), e);}
		    }
		    else if (e.isControlDown()){
			evH.mouseDragged(this,ViewEventHandler.CTRL_MOD,buttonNumber,e.getX(),e.getY(), e);
		    }
		    else {
			if (e.isMetaDown()) {evH.mouseDragged(this,ViewEventHandler.META_MOD,buttonNumber,e.getX(),e.getY(), e);}
			else if (e.isAltDown()) {evH.mouseDragged(this,ViewEventHandler.ALT_MOD,buttonNumber,e.getX(),e.getY(), e);}
			else {evH.mouseDragged(this,ViewEventHandler.NO_MODIFIER,buttonNumber,e.getX(),e.getY(), e);}
		    }
		}
		//assign anyway, even if the current drag command does not want to display a segment
		curDragx=e.getX();curDragy=e.getY();  
		parent.repaintNow();
		if (parent.mouse.isSensitive()){parent.mouse.computeMouseOverList(evH,cams[activeLayer],this.lens);}
	    }
	}	
	catch (NullPointerException ex) {if (parent.parent.debug){System.err.println("viewpanel.mousedragged "+ex);}}
    }

    /**send event to application event handler*/
    public void mouseWheelMoved(MouseWheelEvent e){
	if (evH != null){
	    try {
		evH.mouseWheelMoved(this, (e.getWheelRotation() < 0) ? ViewEventHandler.WHEEL_DOWN : ViewEventHandler.WHEEL_UP, e.getX(), e.getY(), e);
	    }
	    catch (NullPointerException ex) {if (parent.parent.debug){System.err.println("viewpanel.mousewheelmoved "+ex);}}
	}
    }

    /**get mouse as VCursor*/
    public VCursor getMouse(){return parent.mouse;}

    /**last glyph the mouse entered in  (for this view and current active layer)*/
    public Glyph lastGlyphEntered(){
	return parent.mouse.lastGlyphEntered;
    }

    /**get the list of glyphs currently under mouse (last entry is last glyph entered)
     * This returns a <em>copy</em> of the actual array managed by VCursor at the time the method is called
     * (in other words, the array returned by this method is not synchronized with the actual list over time)
     */
    public Glyph[] getGlyphsUnderMouseList(){
	return parent.mouse.getGlyphsUnderMouseList();
    }

    /**get the list of glyphs currently under mouse (last entry is last glyph entered)
     * This returns a <em>copy</em> of the actual array managed by VCursor at the time the method is called
     * (in other words, the array returned by this method is not synchronized with the actual list over time)
     *@deprecated As of zvtm 0.9.3, replaced by getGlyphsUnderMouseList()
     *@see #getGlyphsUnderMouseList()
     */
    public Vector getGlyphsUnderMouse(){
	return parent.mouse.getGlyphsUnderMouse();
    }

    //get the BufferedImage or VolatileImage for this view
    public abstract java.awt.image.BufferedImage getImage();

    /**Set the maximum view refresh rate by giving the minimum refresh period (below which ZVTM won't go even if it can) 
     *@param r positive integer in milliseconds
     */
    protected void setRefreshRate(int r){
	if (r>0){
	    frameTime=r;
	}
	else {
	    System.err.println("Error: trying to set a negative refresh rate : "+r+" ms");
	}
    }

    /**Get the maximum view refresh rate as the minimum refresh period (below which ZVTM won't go even if it can) 
     *@return positive integer in milliseconds
     */
    protected int getRefreshRate(){
	return frameTime;
    }

    /**set a lens for this view ; set to null to remove an existing lens*/
    protected Lens setLens(Lens l){
	if (l != null){
	    this.lens = l;
	    if (this.lens.getID() == null){
		this.lens.setID(new Integer(parent.parent.nextlID++));
		Vector v = new Vector();
		v.addElement(this.lens);
		v.addElement(parent);
		parent.parent.allLenses.put(this.lens.getID(), v);
	    }
	    this.lens.setLensBuffer(this);
	    
	    parent.repaintNow();
	    return this.lens;
	}
	else {//removing the lens set for this view
	    if (this.lens != null){
		parent.parent.allLenses.remove(this.lens.getID());
		this.lens = null;
		parent.repaintNow();
	    }
	    return null;
	}
    }

    /**return Lens cyrrently used by this view (null if none)*/
    protected Lens getLens(){
	return this.lens;
    }

    /** set a padding for customizing the region inside the view for which objects are actually visibles
     *@param wnesPadding padding values in pixels for the west, north, east and south borders
    */
    protected void setVisibilityPadding(int[] wnesPadding){
	visibilityPadding = wnesPadding;
    }

    /** get the padding values customizing the region inside the view for which objects are actually visibles
     *@return padding values in pixels for the west, north, east and south borders
    */
    protected int[] getVisibilityPadding(){
	return visibilityPadding;
    }

}
