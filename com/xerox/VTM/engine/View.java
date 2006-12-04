/*   FILE: View.java
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
 * $Id: View.java,v 1.31 2006/06/01 07:56:30 epietrig Exp $
 */

package com.xerox.VTM.engine;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.JLabel;

import net.claribole.zvtm.engine.Java2DPainter;
import net.claribole.zvtm.engine.Portal;
import net.claribole.zvtm.engine.ViewEventHandler;
import net.claribole.zvtm.lens.Lens;
import net.claribole.zvtm.engine.RepaintListener;

import fr.lri.swingstates.sm.BasicInputStateMachine;

  /**
   * A view is a window and can be composed of one or several cameras superimposed - use EView or IView <BR>
   * A view is repainted on a regular basis when active - for inactive views, the default is to repaint only if the mouse is inside the view (but the frame is not selected) - this can be changed to repaint the view automatically even if it is not selected and if the mouse is not inside, using setRepaintPolicy()
   * @author Emmanuel Pietriga
   **/

public abstract class View {

    /**Standard ZVTM view, with no particular acceleration method*/
    public static final short STD_VIEW = 0;
    /**ZVTM view based on Volatile Images introduced with Java 1.4; may provide some acceleration*/
    public static final short VOLATILE_VIEW = 1;
    /**ZVTM view based on Java 5's OpenGL rendering pipeline; does accelerate rendering but requires a JVM 1.5 or later*/
    public static final short OPENGL_VIEW = 2;

    /**list of Camera objects used in this view*/
    Vector cameras;

    void initCameras(Vector c){
	cameras=c;
	for (int i=0;i<cameras.size();i++){
	    ((Camera)c.elementAt(i)).setOwningView(this);
	}
    }

    /**portals embedded in this view*/
    Portal[] portals = new Portal[0];
    
    /**add a portal to this view*/
    Portal addPortal(Portal p){
	Portal[] tmpP = new Portal[portals.length+1];
	System.arraycopy(portals, 0, tmpP, 0, portals.length);
	tmpP[portals.length] = p;
	portals = tmpP;
	p.setOwningView(this);
	return p;
    }

    /**remove a portal from this view*/
    void removePortal(Portal p){
	for (int i=0;i<portals.length;i++){
	    if (portals[i] == p){
		removePortalAtIndex(i);
		break;
	    }
	}
    }

    /**remove portal at index portalIndex in the list of portals*/
    void removePortalAtIndex(int portalIndex){
	Portal[] tmpP = new Portal[portals.length-1];
	System.arraycopy(portals, 0, tmpP, 0, portalIndex);
	System.arraycopy(portals, portalIndex+1, tmpP, portalIndex, portals.length-portalIndex-1);
	portals = tmpP;
	panel.resetCursorInsidePortals();
    }

    /**mouse glyph*/
    public VCursor mouse;

    /**the actual panel*/
    ViewPanel panel;

    /**enables detection of multiple full fills in one view repaint - for this specific view - STILL VERY BUGGY - ONLY SUPPORTS VRectangle and VCircle for now*/
    boolean detectMultipleFullFills;

    JLabel statusBar;

    /**View name*/
    protected String name;

    /**root VTM class*/
    VirtualSpaceManager parent;

//     /**font used in this view  (use setFont() to modify)*/
//     Font font=Glyph.defaultFont;

    /**triggers the mouseMoved method in ViewEventHandler when the mouse is moved - set to false by default because few applications will need this; it is therefore not necessary to overload other applications with these events*/
    boolean notifyMouseMoved=false;

    /**hooks for Java2D painting in ZVTM views (BACKGROUND, FOREGROUND, AFTER_DISTORTION, AFTER_PORTALS)*/
    Java2DPainter[] painters = new Java2DPainter[4];

    /**
     * get the ViewPanel associated with this view
     */
    public ViewPanel getPanel(){
	return panel;
    }

    /**destroy this view*/
    public abstract void destroyView();

    /**get the java.awt.Container for this view*/
    public abstract Container getFrame();

    /**used only in Internal Views to get focus in view for key events (called automatically when the mouse enters the (Acc)IView)*/
    public abstract void requestFocus();

    /**Set the cursor for this view.
     * Either the ZVTM cursor or one of the default AWT cursors.
     *@param cursorType any of the cursor type values declared in java.awt.Cursor, such as DEFAULT_CURSOR, CROSSHAIR_CURSOR HAND_CURSOR, etc. To get the ZVTM cursor, use Cursor.CUSTOM_CURSOR.
     *@see #setCursorIcon(Cursor c)
     */
    public void setCursorIcon(int cursorType){
	panel.setAWTCursor(cursorType);
    }

    /**Set the cursor for this view.
     * Replaces the ZVTM cursor by a bitmap cursor similar to the default AWT cursors.
     *@param c an AWT cursor instantiated e.g. by calling java.awt.Toolkit.createCustomCursor(Image cursor, Point hotSpot, String name)
     *@see #setCursorIcon(int cursorType)
     */
    public void setCursorIcon(Cursor c){
	panel.setAWTCursor(c);
    }
   
    /**set application class to which events are sent*/
    public void setEventHandler(ViewEventHandler eh){
	panel.setEventHandler(eh);
    }

    /**sets whether the mouseMoved method in ViewEventHandler is triggered when the mouse is moved - set to false by default because few applications will need this; it is therefore not necessary to overload other applications with these events*/
    public void setNotifyMouseMoved(boolean b){
	notifyMouseMoved=b;
    }

    /**get state of notifyMouseMoved for this view*/
    public boolean getNotifyMouseMoved(){return notifyMouseMoved;}

    /**set status bar text*/
    public void setStatusBarText(String s){
	if (statusBar!=null){if (s.equals("")){statusBar.setText(" ");}else{{statusBar.setText(s);}}}
    }

    /**set font used in status bar text*/
    public void setStatusBarFont(Font f){
	if (statusBar!=null){statusBar.setFont(f);}
    }

    /**set color used for status bar text*/
    public void setStatusBarForeground(Color c){
	if (statusBar!=null){statusBar.setForeground(c);}
    }

    /**enable/disable detection of multiple full fills in one view repaint - for this specific view */
    public void setDetectMultiFills(boolean b){
	detectMultipleFullFills=b;
    }

    /**get state of detection of multiple full fills in one view repaint - for this specific view*/
    public boolean getDetectMultiFills(){
	return detectMultipleFullFills;
    }

    /**returns bounds of rectangle representing virtual space's region seen through camera c [west,north,east,south]*/
    public long[] getVisibleRegion(Camera c){
	return getVisibleRegion(c, new long[4]);
    }

    /**returns bounds of rectangle representing virtual space's region seen through camera c [west,north,east,south]
     *@param c camera
     *@param res array which will contain the result */
    public long[] getVisibleRegion(Camera c, long[] res){
	if (cameras.contains(c)){
	    float uncoef=(float)((c.focal+c.altitude)/c.focal);  //compute region seen from this view through camera
	    res[0] = (long)(c.posx-(panel.viewW/2-panel.visibilityPadding[0])*uncoef);
	    res[1] = (long)(c.posy+(panel.viewH/2-panel.visibilityPadding[1])*uncoef);
	    res[2] = (long)(c.posx+(panel.viewW/2-panel.visibilityPadding[2])*uncoef);
	    res[3] = (long)(c.posy-(panel.viewH/2-panel.visibilityPadding[3])*uncoef);
	    return res;
	}
	return null;
    }

    public long getVisibleRegionWidth(Camera c){
	return (long)(panel.getSize().width * ((c.focal+c.altitude) / c.focal));
    }

    public long getVisibleRegionHeight(Camera c){
	return (long)(panel.getSize().height * ((c.focal+c.altitude) / c.focal));
    }

    /**returns a BufferedImage representation of this view (this is actually a COPY of the original) that can be used for instance with ImageIO.ImageWriter*/
    public BufferedImage getImage(){
	BufferedImage res=null;
	synchronized(this.getClass()){
	    BufferedImage i=panel.getImage();
	    if (i!=null){
		//this is the old method for doing this, which eventually stopped working on POSIX systems  (hangs at i.copyData())
// 		java.awt.image.WritableRaster wr=Raster.createWritableRaster(i.getSampleModel(),new java.awt.Point(0,0));
// 		res=new BufferedImage(i.getColorModel(),i.copyData(wr),false,null);
		//new way of doing things
		res=new BufferedImage(i.getWidth(),i.getHeight(),i.getType());
		Graphics2D resg2d=res.createGraphics();
		resg2d.drawImage(i,null,0,0);
	    }
	}
	return res;
    }

    /**set the layer (camera) active in this view
     * @param i i-th layer 0 is the deepest layer
     */
    public void setActiveLayer(int i){
	Camera c = (Camera)cameras.elementAt(i);
	mouse.unProject(c, panel);
	mouse.resetGlyphsUnderMouseList(c.parentSpace,
					c.getIndex());
	panel.activeLayer=i;
    }

    /**get the active layer in this view (0 is deepest)*/
    public int getActiveLayer(){
	return panel.activeLayer;
    }

    /**update font used in this view (for all cameras) (should be automatically called when changing the VSM's main font)*/
    public void updateFont(){panel.updateFont=true;}

//     /**get font used in this view*/
//     public Font getFont(){return font;}

    /**set antialias rendering hint for this view*/
    public void setAntialiasing(boolean b){
	if (b!=panel.antialias){
	    panel.antialias=b;
	    panel.updateAntialias=true;
	    repaintNow();
	}
    }

    /**get the value of the antialias rendering hint for this view*/
    public boolean getAntialiasing(){
	return panel.antialias;
    }

    /**get camera number i (corresponds to layer)*/
    public Camera getCameraNumber(int i){
	if (cameras.size()>i){return (Camera)cameras.elementAt(i);}
	else return null;
    }

    /**get active camera (associated with active layer)*/
    public Camera getActiveCamera(){
	return panel.cams[panel.activeLayer];
    }

    void destroyCamera(Camera c){
	for (int i=0;i<panel.cams.length;i++){
	    if (panel.cams[i]==c){
		panel.cams[i]=null;
		if (i==panel.activeLayer){//if the camera we remove was associated to the active layer, make active another non-null layer
		    for (int j=0;j<panel.cams.length;j++){
			if (panel.cams[j]!=null){
			    panel.activeLayer=j;
			    break;
			}
		    }
		}
		break;
	    }
	}
	cameras.remove(c);
    }

    /**set background color for this view*/
    public void setBackgroundColor(Color c){
	panel.backColor=c;
    }

    /**get background color of this view*/
    public Color getBackgroundColor(){
	return panel.backColor;
    }

    /**tells whether this frame is selected or not*/
    public abstract boolean isSelected();

    /**set the window title*/
    public abstract void setTitle(String t);

    /**set the window location*/
    public abstract void setLocation(int x,int y);

    /**set the window size*/
    public abstract void setSize(int x,int y);

    /**get the dimensions of the ZVTM panel embedded in this view*/
    public Dimension getPanelSize(){
	return panel.size;
    }

    /**can the window be resized or not*/
    public abstract void setResizable(boolean b);

    /**Shows or hides this view*/
    public abstract void setVisible(boolean b);

    /**Brings this window to the front. Places this window at the top of the stacking order and shows it in front of any other windows*/
    public abstract void toFront();

    /**Sends this window to the back. Places this window at the bottom of the stacking order and makes the corresponding adjustment to other visible windows*/
    public abstract void toBack();

    /**Set this view's refresh rate - default is 20
     *@param r positive integer (refresh rate in milliseconds)
     */
    public void setRefreshRate(int r){
	panel.setRefreshRate(r);
    }

    /**Set this view's refresh rate - default is 20*/
    public int getRefreshRate(){
	return panel.getRefreshRate();
    }

    /**should repaint this view on a regular basis or not (even if not activated, but does not apply to iconified views)*/
    public void setRepaintPolicy(boolean b){
	panel.alwaysRepaintMe=b;
	if (b){panel.active=true;}
	else {if ((!isSelected()) && (!panel.inside)){panel.active=false;}}
    }

    /**
     * make a view blank (the view is erased and filled with a uniform color)
     *@param c blank color (will fill the entire view) - put null to exit blank mode
     */
    public void setBlank(Color c){
	if (c==null){
	    panel.blankColor=null;
	    panel.notBlank=true;
	    repaintNow();
	}
	else {
	    panel.blankColor=c;
	    panel.notBlank=false;
	    repaintNow();
	}
    }

    /**
     *tells if a view is in blank mode (returns the fill color) or not (returns null)
     */
    public Color isBlank(){
	if (!panel.notBlank){
	    return panel.blankColor;
	}
	else return null;
    }

    /**if true, compute the list of glyphs under mouse each time the view is repainted (default is false) - note that this list is computed each time the mouse is moved inside the view, no matter the policy*/
    public void setComputeMouseOverListPolicy(boolean b){
	panel.computeListAtEachRepaint=b;
    }

    /**activate the view means that it will be repainted*/
    public void activate(){
	parent.setActiveView(this);
	panel.active=true;
	if (panel.evH!=null){panel.evH.viewActivated(this);}
    }
    
    /**deactivate the view (will not be repainted unless setRepaintPolicy(true) or mouse inside the view)*/
    public void deactivate(){
	if ((!panel.alwaysRepaintMe) && (!panel.inside)){panel.active=false;}
	if (panel.evH!=null){panel.evH.viewDeactivated(this);}
    }

    /**called from the window listener when the window is iconified - repaint is automatically disabled*/
    void iconify(){
	panel.active=false;
	if (panel.evH!=null){panel.evH.viewIconified(this);}
    }

    /**called from the window listener when the window is deiconified - repaint is automatically re-enabled*/
    void deiconify(){
	panel.active=true;
	if (panel.evH!=null){panel.evH.viewDeiconified(this);}
    }

    /**called from the window listener when the window is closed*/
    protected void close(){
	if (panel.evH!=null){panel.evH.viewClosing(this);}
    }

    /**Call this if you want to repaint this view at once.
     *@see #repaintNow(RepaintListener rl)
     */
    public void repaintNow(){
	panel.repaintNow=true;
    }

    /**Call this if you want to repaint this view at once.
     *@param rl a repaint listener to be notified when this repaint cycle is completed (it must be removed manually if you are not interested in being notified about following repaint cycles)
     *@see #repaintNow()
     *@see #removeRepaintListener()     */
    public void repaintNow(RepaintListener rl){
	panel.repaintListener = rl;
	panel.repaintNow = true;
    }

    /**Remove the repaint listener associated with this view.
     *@see #repaintNow(RepaintListener rl)
     */
    public void removeRepaintListener(){
	panel.repaintListener = null;
    }

    /**gives access to the panel's Graphics object - can be useful in some cases, for instance to compute the bounds of a text string that has not yet been added to any virtual space. SHOULD NOT BE TAMPERED WITH. USE AT YOUR OWN RISKS!*/
    public Graphics getGraphicsContext(){
	return panel.g2;
    }

    /**set a lens for this view ; set to null to remove an existing lens<br/>Only works with standard view (has no effect when set on accelereated views)<br>
     * Important: Distortion lenses cannot be associated with VolatileImage-based or OpenGL-based views*/
    public Lens setLens(Lens l){
	return panel.setLens(l);
    }

    /**return Lens currently used by this view (null if none)*/
    public Lens getLens(){
	return panel.getLens();
    }

    /**ask for a bitmap rendering of this view and encode it in a PNG file
     *@param w width of rendered image
     *@param h height of rendered image
     *@param vsm the current VirtualSpaceManager
     *@param f the location of the resulting PNG file
     */
    public void rasterize(int w, int h, VirtualSpaceManager vsm, java.io.File f){
	javax.imageio.ImageWriter writer = (javax.imageio.ImageWriter)javax.imageio.ImageIO.getImageWritersByFormatName("png").next();
	try {
	    writer.setOutput(javax.imageio.ImageIO.createImageOutputStream(f));
	    java.awt.image.BufferedImage bi = this.rasterize(w, h, vsm);
	    if (bi != null){
		writer.write(bi);
		writer.dispose();
	    }
	}
	catch (java.io.IOException ex){ex.printStackTrace();}
    }

    /**ask for a bitmap rendering of this view and return the resulting BufferedImage
     *@param w width of rendered image
     *@param h height of rendered image
     *@param vsm the current VirtualSpaceManager
     *@return the resulting buffered image which can then be manipulated and serialized
     */
    public BufferedImage rasterize(int w, int h, VirtualSpaceManager vsm){
	Dimension panelSize = panel.getSize();
	float mFactor = 1/Math.min(w / ((float)panelSize.getWidth()),
				   h / ((float)panelSize.getHeight()));
	Camera c, nc;
	Vector clones= new Vector();
	for (int i=0;i<cameras.size();i++){
	    c = (Camera)cameras.elementAt(i);
	    nc = vsm.addCamera(c.parentSpace.spaceName);
	    nc.posx = c.posx;
	    nc.posy = c.posy;
	    /*change this altitude to compensate for the w/h change what we
	      want is to get the same view at a higher (or lower) resolution*/
	    nc.focal = c.focal;
	    nc.altitude = (c.altitude + c.focal) * mFactor - c.focal;
	    clones.add(nc);
	}
	BufferedImage img = (new OffscreenViewPanel(clones)).rasterize(w, h);
	for (int i=0;i<clones.size();i++){
	    nc = (Camera)clones.elementAt(i);
	    vsm.getVirtualSpace(nc.parentSpace.spaceName).removeCamera(nc.index);
	}
	return img;
    }

    //we have to specify the layer too (I think)     write this later
//     /**show/hide in this view the region seen through a camera (as a rectangle)
//      *@param c the camera to be displayed (should not be one of the cameras composing this view)
//      */
//     public void showCamera(Camera c,boolean b,Color col){
    
//     }

    /** set a paint method (containing Java2D paint instructions) that will be called each time the view is repainted
     *@param p the paint method encapsulated in an object implementing the Java2DPainter interface (pass null to unset an existing one)
     *@param g one of Java2DPainter.BACKGROUND, Java2DPainter.FOREGROUND, Java2DPainter.AFTER_DISTORTION, Java2DPainter.AFTER_PORTALS depending on whether the method should be called before or after ZVTM glyphs have been painted, after distortion by a lens (FOREGROUND and AFTER_DISTORTION are equivalent in the absence of lens), or after portals have been painted
     */
    public void setJava2DPainter(Java2DPainter p, short g){
	painters[g] = p;
	repaintNow();
    }

    /** get the paint method (containing Java2D paint instructions) that will be called each time the view is repainted
     *@param g one of Java2DPainter.BACKGROUND, Java2DPainter.FOREGROUND, Java2DPainter.AFTER_DISTORTION, Java2DPainter.AFTER_PORTALS depending on whether the method should be called before or after ZVTM glyphs have been painted, after distortion by a lens (FOREGROUND and AFTER_DISTORTION are equivalent in the absence of lens), or after portals have been painted
     *@return p the paint method encapsulated in an object implementing the Java2DPainter interface (null if not set)
     */
    public Java2DPainter getJava2DPainter(short g){
	return painters[g];
    }
    
    /**get the name of this view*/
    public String getName(){
	return name;
    }

    /** set a padding for customizing the region inside the view for which objects are actually visibles
     *@param wnesPadding padding values in pixels for the west, north, east and south borders
    */
    public void setVisibilityPadding(int[] wnesPadding){
	panel.setVisibilityPadding(wnesPadding);
    }

    /** get the padding values customizing the region inside the view for which objects are actually visibles
     *@return padding values in pixels for the west, north, east and south borders
    */
    public int[] getVisibilityPadding(){
	return panel.getVisibilityPadding();
    }

    /**Add a state machine that listens to mouse and keyboard events in this view.
     *@param sm the state machine to be dissociated from this view
     */
    public void addSMEventHandler(BasicInputStateMachine sm){
	panel.addMouseListener(sm);
	panel.addMouseMotionListener(sm);
	panel.addMouseWheelListener(sm);
    }

    /**Remove a state machine that was listening to mouse and keyboard events in this view.
     *@param sm the state machine to be dissociated from this view
     */
    public void removeSMEventHandler(BasicInputStateMachine sm){
	panel.removeMouseListener(sm);
	panel.removeMouseMotionListener(sm);
	panel.removeMouseWheelListener(sm);
    }

    void buildConstraints(GridBagConstraints gbc, int gx,int gy,int gw,int gh,int wx,int wy){
	gbc.gridx=gx;
	gbc.gridy=gy;
	gbc.gridwidth=gw;
	gbc.gridheight=gh;
	gbc.weightx=wx;
	gbc.weighty=wy;
    }

}
