/*   FILE: VirtualSpaceManager.java
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
import java.awt.Font;
import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JFrame;

import net.claribole.zvtm.animation.Animation;
import net.claribole.zvtm.animation.AnimationManager;
import net.claribole.zvtm.animation.EndAction;
import net.claribole.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import net.claribole.zvtm.engine.Location;
import net.claribole.zvtm.engine.Portal;
import net.claribole.zvtm.engine.RepaintListener;
import net.claribole.zvtm.glyphs.CGlyph;
import net.claribole.zvtm.lens.Lens;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.RectangularShape;
import com.xerox.VTM.glyphs.VPath;
import com.xerox.VTM.glyphs.VText;

/**
 * Virtual space manager. This is the main entry point to the toolkit. Virtual spaces, cameras, glyphs and views are instanciated from here.
 * @author Emmanuel Pietriga
 **/

public class VirtualSpaceManager implements AWTEventListener {

    static Font mainFont=new Font("Dialog",0,10);

    /**return default font used by glyphs*/
    public static Font getMainFont(){return mainFont;}

    /**set default font used by glyphs*/
    public void setMainFont(Font f){
	mainFont=f;
	for (int i=0;i<allViews.length;i++){
	    allViews[i].updateFont();
	}
	Object g;
	for (Enumeration e=allGlyphs.elements();e.hasMoreElements();){
	    g=e.nextElement();
	    if (g instanceof VText){((VText)g).invalidate();}
	}
	repaintNow();
    }

    /**select only mouse sensitive and visible glyphs*/
    public static short VIS_AND_SENS_GLYPHS=0;
    /**select only visible glyphs*/ 
    public static short VISIBLE_GLYPHS=1;
    /**select only mouse sensitive glyphs*/
    public static short SENSITIVE_GLYPHS=2;
    /**select all glyphs in the region*/
    public static short ALL_GLYPHS=3;     

    /**print exceptions and warning*/
    static boolean debug = false;

    /**next glyph will have ID...*/
    private long nextID;
    /**next camera will have ID...*/
    private int nextcID;
    /**next lens will have ID...*/
    protected int nextlID;
    /**next portal will have ID...*/
    protected int nextpID;
    /**next cursor will have ID...*/
    private int nextmID;

    /**key is glyph ID (Long)*/
    protected Hashtable allGlyphs;
    /**key is camera ID  (Integer)*/
    protected Hashtable allCameras;
    /**key is portal ID  (Integer)*/
    protected Hashtable allPortals;
    /**key is lens ID  (Integer), value is a two-element Vector: 1st element is the Lens object, 2nd element is the owning view*/
    protected Hashtable allLenses;
    /**key is space name (String)*/
    protected Hashtable allVirtualSpaces;
    /**All views managed by this VSM*/
    protected View[] allViews;
    /**used to quickly retrieve a view by its name (gives its index position in the list of views)*/
    protected Hashtable name2viewIndex;

    /**View which has the focus (or which was the last to have it among all views)*/
    public View activeView;
    protected int activeViewIndex = -1;

    /**default policy for view repainting - true means all views are repainted even if ((not active) or (mouse not inside the view)) - false means only the active view and the view in which the mouse is currently located (if different) are repainted - default is true*/
    boolean generalRepaintPolicy=true;

    /**enables detection of multiple full fills in one view repaint - default value assigned to new views  - STILL VERY BUGGY - ONLY SUPPORTS VRectangle and VCircle for now - setting it to true will prevent some glyphs from being painted if they are not visible in the final rendering (because of occlusion). This can enhance performance (in configurations where occlusion does happen).*/
    boolean defaultMultiFill=false;

    /**value under which a VText is drawn as a segment instead of a text (considered too small to be read). Default is 0.5 - if you raise this value, text that was still displayed as a string will be displayed as a segment and inversely - of course, displaying a line instead of applying affine transformations to strings is faster*/
    float textAsLineCoef=0.5f;

    /**Animation Manager*/
    private final AnimationManager animationManager;

    /**allow negative camera altitudes (zoom beyond the standard size=magnification) ; this is actually a hack to decrease focal value automatically when the altitude is 0*/
    protected int zoomFloor=0;

    /**Constraint Manager*/
//     public ConstraintManager constMgr;

    Color mouseInsideColor=Color.white;

  public static final VirtualSpaceManager INSTANCE = new VirtualSpaceManager();
 
    /**
     * Set applet to true if you are calling ZVTM from inside an Applet
     */
    private VirtualSpaceManager(){
	if (debug){System.out.println("Debug mode ON");}
	nextID=1;
	nextcID=1;
	nextpID=1;
	nextlID=1;
	nextmID=1;
	animationManager = new AnimationManager(this);
	allGlyphs=new Hashtable();
	allCameras=new Hashtable();
	allPortals=new Hashtable();
	allLenses=new Hashtable();
	allVirtualSpaces=new Hashtable();
	allViews = new View[0];
	name2viewIndex = new Hashtable();
    }

    public static final void setApplet(){
	    java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(INSTANCE, AWTEvent.WINDOW_EVENT_MASK);
    }

    /**set debug mode ON or OFF*/
    public static void setDebug(boolean b){
	debug=b;
    }

    /**get debug mode state (ON or OFF)*/
    public static boolean debugModeON(){return debug;}

    /**
     * Returns a reference to the AnimationManager associated
     * with this VirtualSpaceManager.
     */
    public AnimationManager getAnimationManager(){
	return animationManager;
    }

    /**set policy for view repainting - true means all views are repainted even if ((not active) or (mouse not inside the view)) - policy is forwarded to all existing views (no matter its current policy) and will be applied to future ones (but it can be changed for each single view)*/
    public void setRepaintPolicy(boolean b){
	if (b!=generalRepaintPolicy){
	    generalRepaintPolicy=b;
	    for (int i=0;i<allViews.length;i++){
		allViews[i].setRepaintPolicy(generalRepaintPolicy);
	    }
	}
    }

    /**get general policy for view repainting (this is the current default policy, but this does not guarantee that all views comply with it since the policy may be changed for each single view)*/
    public boolean getRepaintPolicy(){return generalRepaintPolicy;}

    /**enable/disable detection of multiple full fills in one view repaint - default value assigned to new views - default is false */
    public void setDefaultMultiFills(boolean b){
	defaultMultiFill=b;
    }

    /**get state of detection of multiple full fills in one view repaint - default value assigned to new views */
    public boolean getDefaultMultiFills(){
	return defaultMultiFill;
    }

    /**
     * set a zoom-in limit/maximum magnification  (like a floor the camera cannot go through)<br>
     * value 0 means that, at maximum magnification, the size of observed glyphs corresponds to their <i>real</i> size (e.g. if a circle has a declared radius of 50 in the virtual space, then its radius at max magnification is 50)<br>
     * if the floor is set to a negative value, you will be able to zoom in further (meaning that you will be able to magnify objects beyond their declared size)<br>
     * Note: there is no limit for zoom out (no so-called ceiling)
     *@param a the altitude of the floor - the default value is 0 (put a negative value if you want to be able to magnify objects beyond their normal size) 
     */
    public void setZoomLimit(int a){
	zoomFloor=a;
    }

    /**
     * get the zoom-in limit/maximum magnification  (like a floor the camera cannot go through)<br>
     * default value 0 means that, at maximum magnification, the size of observed glyphs corresponds to their <i>real</i> size (e.g. if a circle has a declared radius of 50 in the virtual space, then its radius at max magnification is 50)<br>
     * if the floor is set to a negative value, you will be able to zoom in further (meaning that you will be able to magnify objects beyond their declared size)<br>
     * Note: there is no limit for zoom out (no so-called ceiling)
     */
    public int getZoomLimit(){
	return zoomFloor;
    }

    /** Set border color of glyphs overlapped by mouse.
     * Not propagated to existing glyphs.
     *@see #setMouseInsideGlyphColor(Color c, boolean propagate)
     */
    public void setMouseInsideGlyphColor(Color c){
	setMouseInsideGlyphColor(c, false);
    }


    /** Set border color of glyphs overlapped by mouse.
     *@param propagate propagate changes to existing glyphs
     *@see #setMouseInsideGlyphColor(Color c)
     */
    public void setMouseInsideGlyphColor(Color c, boolean propagate){
	mouseInsideColor = c;
	if (propagate){
	    for (Enumeration e=allGlyphs.elements();e.hasMoreElements();){
		((Glyph)e.nextElement()).setMouseInsideHighlightColor(mouseInsideColor);
	    }
	}
    }

    /**add glyph g to virtual space whose name is vs
     *@param g glyph
     *@param vs virtual space name
     */
    public Glyph addGlyph(Glyph g, String vs){
	return addGlyph(g, vs, true);
    }

    /**add glyph g to virtual space vs
     *@param g glyph
     *@param vs virtual space
     */
    public Glyph addGlyph(Glyph g, VirtualSpace vs){
	return addGlyph(g, vs, true);
    }

    /**add glyph g to virtual space whose name is vs
     *@param g glyph
     *@param vs virtual space name
     *@param repaint false -> do not issue a repaint request for cameras associated with vs (default is true)
     */
    public Glyph addGlyph(Glyph g, String vs, boolean repaint){
	if (g!=null){
	    if (allVirtualSpaces.containsKey(vs)){
		VirtualSpace tvs = (VirtualSpace)allVirtualSpaces.get(vs);
		return addGlyph(g, tvs, true, repaint);
	    }
	    else {System.err.println("ZVTM Error:VirtualSpaceManager:addGlyph:unknown virtual space: "+vs);return null;}
	}
	else {System.err.println("ZVTM Error:VirtualSpaceManager:addGlyph:attempting to add a null Glyph in space: "+vs);return null;}
    }

    /**add glyph g to virtual space vs
     *@param g glyph
     *@param vs virtual space
     *@param repaint false -> do not issue a repaint request for cameras associated with vs (default is true)
     */
    public Glyph addGlyph(Glyph g, VirtualSpace vs, boolean repaint){
	return addGlyph(g, vs, true, repaint);
    }

    /**add glyph g to virtual space vs
     *@param g glyph
     *@param vs virtual space
     *@param initColors false -> do not initalize mouse inside and selected colors
     *@param repaint false -> do not issue a repaint request for cameras associated with vs (default is true)
     */
    public Glyph addGlyph(Glyph g, VirtualSpace vs, boolean initColors, boolean repaint){
	if (g!=null && vs!=null){
	    vs.addGlyph(g);
	    g.setID(new Long(nextID++));
	    if (initColors){
		g.setMouseInsideHighlightColor(this.mouseInsideColor);
	    }
	    allGlyphs.put(g.getID(),g);
	    if (repaint){repaintNow();}
	    return g;
	}
	else {System.err.println("ZVTM Error:VirtualSpaceManager:addGlyph:attempting to add a null Glyph in space: "+vs);return null;}
    }
    
    public void addGlyphs(Glyph[] gs, VirtualSpace vs, boolean repaint){
		if(vs != null){
			vs.addGlyphs(gs);

			for(Glyph glyph: gs){
				glyph.setID(new Long(nextID++));
				glyph.setMouseInsideHighlightColor(this.mouseInsideColor);
				allGlyphs.put(glyph.getID(), glyph);
			}
			if(repaint){repaintNow();}
		}
    }

    /**add composite glyph c to virtual space whose name is vs*/
    public CGlyph addCGlyph(CGlyph c,String vs){
	if (c!=null){
	    if (allVirtualSpaces.containsKey(vs)){
		c.setID(new Long(nextID++));
		allGlyphs.put(c.getID(),c);
		return c;
	    }
	    else {System.err.println("Error:VirtualSpaceManager:addCGlyph:unknown virtual space: "+vs);return null;}
	}
	else {System.err.println("Error:VirtualSpaceManager:addCGlyph:attempting to add a null composite glyph in space: "+vs);return null;}
    }

    /** Get glyph with ID id. */
    public Glyph getGlyph(Long id){
        return (Glyph)(allGlyphs.get(id));
    }
    
    /** Get all glyphs currently present in the various virtual spaces managed by this VSM. */
    public Enumeration getAllGlyphs(){
        return allGlyphs.elements();
    }

	/** Remove all glyphs from a virtual space.
		*@param spaceName name of that virtual space
		*@deprecated as of zvtm 0.9.8
		*@see #removeGlyphsFromSpace(String spaceName)
		*/
	public void destroyGlyphsInSpace(String spaceName){
		removeGlyphsFromSpace(spaceName);
	}

	/** Remove all glyphs from a virtual space.
		*@param spaceName name of that virtual space
		*/
	public void removeGlyphsFromSpace(String spaceName){
		VirtualSpace vs=getVirtualSpace(spaceName);
		Glyph g;
		Vector entClone=(Vector)vs.getAllGlyphs().clone();
		for (int i=0;i<entClone.size();i++){
			g = (Glyph)entClone.elementAt(i);
			vs.removeGlyph(g, false);
			allGlyphs.remove(g);
		}
		repaintNow();
	}

    /**add camera to space whose name is vs
     *@param vs owning virtual space's name
     */
    public Camera addCamera(String vs){
	return addCamera((VirtualSpace)allVirtualSpaces.get(vs));
    }

    /**add camera to space whose name is vs
     *@param vs owning virtual space 
     */
    public Camera addCamera(VirtualSpace vs){
	Camera c = vs.createCamera();
	c.setID(new Integer(nextcID++));
	allCameras.put(c.getID(), c);
	return c;
    }

    /**add camera to space whose name is vs
     *@param vs owning virtual space's name
     *@param lazy true if this is to be a lazy camera (false otherwise)
     */
    public Camera addCamera(String vs, boolean lazy){
	return addCamera((VirtualSpace)allVirtualSpaces.get(vs), lazy);
    }

    /**
     *add camera to space whose name is vs
     *@param vs owning virtual space
     *@param lazy true if this is to be a lazy camera (false otherwise)
     */
    public Camera addCamera(VirtualSpace vs, boolean lazy){
	Camera c = vs.createCamera();
	c.setLaziness(lazy);
	c.setID(new Integer(nextcID++));
	allCameras.put(c.getID(),c);
	return c;
    }

    /**get camera whose ID is id*/
    public Camera getCamera(Integer id){
	return (Camera)(allCameras.get(id));
    }

    /**get active camera (in focused view) - null if no view is active*/
    public Camera getActiveCamera(){
	return (activeView != null) ? activeView.getActiveCamera() : null;
    }

    /* -------------- PORTALS ------------------ */

    /**add a portal to view v
     *@param p portal
     *@param v owning view
     */
    public Portal addPortal(Portal p, View v){
	p.setID(new Integer(nextpID++));
	allPortals.put(p.getID(), p);
	return v.addPortal(p);
    }

    /**destroy a portal*/
    public void destroyPortal(Portal p){
	View v = p.getOwningView();
	v.removePortal(p);
	allPortals.remove(p.getID());
    }

    /**get portal whose ID is id*/
    public Portal getPortal(Integer id){
	return (Portal)(allPortals.get(id));
    }

    /* ----------------- LENSES ---------------- */

    /**get lens whose ID is id*/
    public Lens getLens(Integer id){
	try {
	    return (Lens)(((Vector)allLenses.get(id)).elementAt(0));
	}
	catch (NullPointerException ex){return null;}
    }

    /**get view to which lens whose ID is id belongs to*/
    public View getOwningView(Integer id){
	try {
	    return (View)(((Vector)allLenses.get(id)).elementAt(1));
	}
	catch (NullPointerException ex){return null;}
    }

    /**create a new external view
     *@param c vector of cameras making this view (if more than one camera, cameras will be superimposed on different layers)
     *@param name view name
     *@param viewType one of View.STD_VIEW, View.VOLATILE_VIEW, View.OPENGL_VIEW - determines the type of view and acceleration method
     *@param w width of window in pixels
     *@param h height of window in pixels
     *@param bar true -&gt; add a status bar to this view (below main panel)
     *@param visible should the view be made visible automatically or not
     */
    public View addExternalView(Vector c, String name, short viewType, int w, int h, boolean bar, boolean visible){
	return addExternalView(c, name, viewType, w, h, bar, visible, null);
    }

    /**Create a new external view.<br>
     * The use of OPENGL_VIEW requires the following Java property: -Dsun.java2d.opengl=true
     *@param c vector of cameras making this view (if more than one camera, cameras will be superimposed on different layers)
     *@param name view name
     *@param viewType one of View.STD_VIEW, View.VOLATILE_VIEW, View.OPENGL_VIEW - determines the type of view and acceleration method
     *@param w width of window in pixels
     *@param h height of window in pixels
     *@param bar true -&gt; add a status bar to this view (below main panel)
     *@param visible should the view be made visible automatically or not
     *@param mnb a menu bar (null if none), already configured with ActionListeners already attached to items (it is just added to the view)
     *@see #addExternalView(Vector c, String name, short viewType, int w, int h, boolean bar, boolean visible, boolean decorated, JMenuBar mnb)
     */
    public View addExternalView(Vector c, String name, short viewType, int w, int h,
				boolean bar, boolean visible, JMenuBar mnb){
	return addExternalView(c, name, viewType, w, h, bar, visible, true, mnb);
    }
    
    /**Create a new external view.<br>
     * The use of OPENGL_VIEW requires the following Java property: -Dsun.java2d.opengl=true
     *@param c vector of cameras making this view (if more than one camera, cameras will be superimposed on different layers)
     *@param name view name
     *@param viewType one of View.STD_VIEW, View.VOLATILE_VIEW, View.OPENGL_VIEW - determines the type of view and acceleration method
     *@param w width of window in pixels
     *@param h height of window in pixels
     *@param bar true -&gt; add a status bar to this view (below main panel)
     *@param visible should the view be made visible automatically or not
     *@param decorated should the view be decorated with the underlying window manager's window frame or not
     *@param mnb a menu bar (null if none), already configured with ActionListeners already attached to items (it is just added to the view)
     *@see #addExternalView(Vector c, String name, short viewType, int w, int h, boolean bar, boolean visible, JMenuBar mnb)
     */
    public View addExternalView(Vector c, String name, short viewType, int w, int h,
				boolean bar, boolean visible, boolean decorated, JMenuBar mnb){
	View v = null;
	switch(viewType){
	case View.STD_VIEW:{
	    v = (mnb != null) ? new EView(c, name, w, h, bar, visible, decorated, mnb) : new EView(c, name, w, h, bar, visible, decorated);
	    v.mouse.setID(new Long(nextmID++));
	    addView(v);
	    v.setRepaintPolicy(generalRepaintPolicy);
	    break;
	}
	case View.OPENGL_VIEW:{
	    v = (mnb != null) ? new GLEView(c, name, w, h, bar, visible, mnb) : new GLEView(c, name, w, h, bar, visible);
	    v.mouse.setID(new Long(nextmID++));
	    addView(v);
	    v.setRepaintPolicy(generalRepaintPolicy);
	    break;
	}
	case View.VOLATILE_VIEW:{
	    v = (mnb != null) ? new AccEView(c, name, w, h, bar, visible, mnb) : new AccEView(c, name, w, h, bar, visible);
	    v.mouse.setID(new Long(nextmID++));
	    addView(v);
	    v.setRepaintPolicy(generalRepaintPolicy);
	    break;
	}   
	}
	return v;
    }

    /**create a new view embedded in a JPanel, suitable for inclusion in other Components including JApplet
     *@param c vector of cameras superimposed in this view
     *@param name view name
     *@param w width of window in pixels
     *@param h height of window in pixels
     */
    public JPanel addPanelView(Vector c,String name,int w,int h){
        PView tvi = new PView(c, name, w, h);
        tvi.mouse.setID(new Long(nextmID++));
        addView(tvi);
        tvi.setRepaintPolicy(generalRepaintPolicy);
        return tvi.panel;
    }

    /**
     * Adds a newly created view to the list of existing views
     * Side-effect: attempts to start the animation manager
     */
    protected void addView(View v){
	View[] tmpA = new View[allViews.length+1];
	System.arraycopy(allViews, 0, tmpA, 0, allViews.length);
	tmpA[allViews.length] = v;
	allViews = tmpA;
	name2viewIndex.put(v.name, new Integer(allViews.length-1));
	animationManager.start(); //starts animationManager if not already running
    }

     /**
       * Creates an external view which presents itself
       * in a JPanel in a window (JFrame) provided by the client application (and which can contain other components).
	 * @param cameraList vector of cameras superimposed in this view
	 * @param name	View name. Since this view is
	 * not itself a window, this does not affect the
	 * window's title: use setTitle() for that.
	 * @param panelWidth	width of panel in pixels
	 * @param panelHeight	width of panel in pixels
	 * @param visible	should the view be made visible automatically or not
	 * @param decorated	should the view be decorated with the underlying window manager's window frame or not
	 * @param viewType	One of <code>View.STD_VIEW</code>,
	 * <code>View.OPENGL_VIEW</code>,
	 * or <code>View.VOLATILE_VIEW</code>.
	 * @param parentPanel	This is the parent panel for this view. A JPanel
	 * presenting this view will be created as a child of this panel.
	 * If the parent is <code>null</code>, the frame's content panel
	 * will be used as the parent.
	 * @param frame	The frame in which this panel will be created.
	 * (This is to be compatible with the <code>View</code> API.)
	 * @return	View	The created view.
	 */
    public View addExternalView(Vector cameraList, String name, int panelWidth, int panelHeight,
				boolean visible, boolean decorated, short viewType,
				JPanel parentPanel, JFrame frame) {
    	View v = new JPanelView(cameraList, name, panelWidth, panelHeight,
				visible, decorated, viewType,
				parentPanel, frame);
	v.mouse.setID(new Long(nextmID++));
	addView(v);
	v.setRepaintPolicy(generalRepaintPolicy);
	return v;
     }

    /**Get view whose name is n (-1 if view does not exist).*/
    protected int getViewIndex(String n){
	try {
	    return ((Integer)name2viewIndex.get(n)).intValue();
	}
	catch (NullPointerException ex){return -1;}
    }

    /**Get view whose name is n (null if no match).*/
    public View getView(String n){
	int index = getViewIndex(n);
	if (index != -1){
	    return allViews[index];
	}
	else {
	    return null;
	}
    }

    /**Destroy a view identified by its index in the list of views.*/
    protected void destroyView(int i){
	View[] tmpA = new View[allViews.length-1];
	if (tmpA.length > 0){
	    System.arraycopy(allViews, 0, tmpA, 0, i);
	    System.arraycopy(allViews, i+1, tmpA, i, allViews.length-i-1);
	}
	allViews = tmpA;
	updateViewIndex();
    }

    /**update mapping between view name and view index in the list of views when
     * complex changes are made to the list of views (like removing a view)*/
    protected void updateViewIndex(){
	name2viewIndex.clear();
	for (int i=0;i<allViews.length;i++){
	    name2viewIndex.put(allViews[i].name, new Integer(i));
	}
    }

    /**Destroy a view.*/
    protected void destroyView(View v){
	for (int i=0;i<allViews.length;i++){
	    if (allViews[i] == v){
		destroyView(i);
		break;
	    }
	}
    }

    /**Destroy a view. 
     * Used internally - not available outside from package, you should call the method directly on the view itself*/
    protected void destroyView(String viewName){
	destroyView(getView(viewName));
    }

    /**Call this if you want to repaint all views at once.
     * In some cases it is not possible to detect graphical changes so repaint
     * calls have to be issued manually (unless you are willing to wait for
     * another event to trigger repaint).
     *@see #repaintNow(View v)
     *@see #repaintNow(View v, RepaintListener rl)
     */
    public void repaintNow(){
	for (int i=0;i<allViews.length;i++){
	    allViews[i].repaintNow();
	}
    }

    /**Call this if you want to repaint a given view at once.
     * In some cases it is not possible to detect graphical changes so repaint
     * calls have to be issued manually (unless you are willing to wait for
     * another event to trigger repaint).
     *@see #repaintNow()
     *@see #repaintNow(View v, RepaintListener rl)
     */
    public void repaintNow(View v){
	v.repaintNow();
    }

    /**Call this if you want to repaint a given view at once.
     * In some cases it is not possible to detect graphical changes so repaint
     * calls have to be issued manually (unless you are willing to wait for
     * another event to trigger repaint).
     *@param v the view to repaint
     *@param rl a repaint listener to be notified when this repaint cycle is completed (it must be removed manually if you are not interested in being notified about following repaint cycles)
     *@see #repaintNow(View v)
     *@see View#removeRepaintListener()
     */
    public void repaintNow(View v, RepaintListener rl){
	v.repaintNow(rl);
    }

    /**Call this if you want to repaint a given view at once. Internal use.
     * In some cases it is not possible to detect graphical changes so repaint
     * calls have to be issued manually (unless you are willing to wait for
     * another event to trigger repaint).
     *@param i view index in list of views
     */
    protected void repaintNow(int i){
	repaintNow(allViews[i]);
    }

    /**create a new virtual space with name n*/
    public VirtualSpace addVirtualSpace(String n){
	VirtualSpace tvs=new VirtualSpace(n);
	allVirtualSpaces.put(n,tvs);
	return tvs;
    }

    /**destroy a virtual space*/
    public void destroyVirtualSpace(String n){
	if (allVirtualSpaces.containsKey(n)){
	    VirtualSpace vs=(VirtualSpace)(allVirtualSpaces.get(n));
	    vs.destroy();
	    allVirtualSpaces.remove(n);
	}
    }

    /**returns the virtual space owning glyph g*/
    public VirtualSpace getOwningSpace(Glyph g){
	VirtualSpace vs;
	for (Enumeration e=allVirtualSpaces.elements();e.hasMoreElements();){
	    vs = (VirtualSpace)e.nextElement();
	    if (vs.getAllGlyphs().contains(g)){return vs;}
	}
	return null;
    }

    /**get virtual space whose name is n*/
    public VirtualSpace getVirtualSpace(String n){
	return (VirtualSpace)(allVirtualSpaces.get(n));
    }

    /**get active virtual space (i.e., the space owning the camera currently active), null if no view is active*/
    public VirtualSpace getActiveSpace(){
	return (activeView != null) ? activeView.getActiveCamera().getOwningSpace() : null;
    }

    /**manually set active view*/
    public void setActiveView(View v){
	activeView=v;
	activeViewIndex = getViewIndex(v.getName());
    }

    /**get active view*/
    public View getActiveView(){
	return activeView;
    }

    /**stick glyph whose ID is id to mouse (to drag it) - glyph is automatically made unsensitive to mouse events*/
    public void stickToMouse(Long id){
	stickToMouse(getGlyph(id));
    }

    /**stick glyph g to mouse (to drag it) - glyph is automatically made unsensitive to mouse events*/
    public void stickToMouse(Glyph g){
	activeView.mouse.stick(g);
    }

    /**unstick ONLY LAST glyph sticked to mouse - glyph is automatically made sensitive to mouse events - the number of glyphs sticked to the mouse can be obtained by calling VCursor.getStickedGlyphsNumber()*/
    public void unstickFromMouse(){
	activeView.mouse.unstick();
    }
    
    /**stick glyph whose ID is id1 to glyph whose ID is id2 (behaves like a one-way constraint)*/
    public void stickToGlyph(Long id1,Long id2){
	stickToGlyph(getGlyph(id1),getGlyph(id2));
    }

    /**stick glyph g1 to glyph g2 (behaves like a one-way constraint)*/
    public void stickToGlyph(Glyph g1,Glyph g2){
	g2.stick(g1);
    }

    /**unstick glyph whose ID is id1 from glyph whose ID is id2*/
    public void unstickFromGlyph(Long id1,Long id2){
	getGlyph(id2).unstick(getGlyph(id1));
    }

    /**unstick glyph g1 from glyph g2*/
    public void unstickFromGlyph(Glyph g1,Glyph g2){
	g2.unstick(g1);
    }

    /**stick glyph whose ID is id1 to camera whose ID is id2 (behaves like a one-way constraint)*/
    public void stickToCamera(Long id1, Integer id2){
	stickToCamera(getGlyph(id1), getCamera(id2));
    }

    /**stick glyph g to camera c (behaves like a one-way constraint)*/
    public void stickToCamera(Glyph g, Camera c){
	c.stick(g);
    }

    /**unstick all glyphs sticked to Glyph g*/
    public void unstickAllGlyphs(Glyph g){
	g.unstickAllGlyphs();
    }

    /**unstick all glyphs sticked to Camera c*/
    public void unstickAllGlyphs(Camera c){
	c.unstickAllGlyphs();
    }

	/**returns the location from which a camera will see everything visible in the associated virtual space
		*@param c camera considered (will not be moved)
		*@return the location to which the camera should go
		*@see #getGlobalView(Camera c, int d)
		*@see #getGlobalView(Camera c, int d, float mFactor)
		*@see #getGlobalView(Camera c, float mFactor)
		*/
	public Location getGlobalView(Camera c){
		return getGlobalView(c, 1.0f);
	}

	/** Get the location from which a camera will see all glyphs visible in the associated virtual space.
		*@param c camera considered (will not be moved)
     	*@param mFactor magnification factor - 1.0 (default) means that the glyphs will occupy the whole screen. mFactor &gt; 1 will zoom out from this default location. mFactor &lt; 1 will do the opposite
		*@return the location to which the camera should go
		*@see #getGlobalView(Camera c, int d)
		*@see #getGlobalView(Camera c)
		*@see #getGlobalView(Camera c, int d, float mFactor)
		*/
	public Location getGlobalView(Camera c, float mFactor){
		View v = null;
		try {
			v = c.getOwningView();
			if (v!=null){
				long[] wnes = findFarmostGlyphCoords(c.parentSpace);  //wnes=west north east south
				long dx = (wnes[2]+wnes[0])/2;  //new coords where camera should go
				long dy = (wnes[1]+wnes[3])/2;
				long[] regBounds = v.getVisibleRegion(c);
				/*region that will be visible after translation, but before zoom/unzoom (need to
					compute zoom) ; we only take left and down because we only need horizontal and
					vertical ratios, which are equals for left and right, up and down*/
					long[] trRegBounds = {regBounds[0]+dx-c.posx, regBounds[3]+dy-c.posy};
				float currentAlt = c.getAltitude()+c.getFocal();
				float ratio = 0;
				//compute the mult factor for altitude to see all stuff on X
				if (trRegBounds[0]!=0){ratio = (dx-wnes[0])/((float)(dx-trRegBounds[0]));}
				//same for Y ; take the max of both
				if (trRegBounds[1]!=0){
					float tmpRatio = (dy-wnes[3])/((float)(dy-trRegBounds[1]));
					if (tmpRatio>ratio){ratio = tmpRatio;}
				}
				ratio *= mFactor;
				return new Location(dx, dy, currentAlt*Math.abs(ratio)-c.getFocal());
			}
			else return null;
		}
		catch (NullPointerException e){
			System.err.println("Error:VirtualSpaceManager:getGlobalView: ");
			System.err.println("Camera c="+c);
			System.err.println("View v="+v);
			if (debug){e.printStackTrace();}
			else {System.err.println(e);}
			return null;
		}
	}

	/**translates and (un)zooms a camera in order to see everything visible in the associated virtual space
		*@param c Camera to be moved (will actually be moved)
		*@param d duration of the animation in ms
		*@return the final camera location
		*@see #getGlobalView(Camera c)
		*@see #getGlobalView(Camera c, int d, float mFactor)
		*@see #getGlobalView(Camera c, float mFactor)
		*/
    public Location getGlobalView(Camera c, int d){
		return getGlobalView(c, d, 1.0f);
	}

	/**translates and (un)zooms a camera in order to see everything visible in the associated virtual space
		*@param c Camera to be moved (will actually be moved)
		*@param d duration of the animation in ms
     	*@param mFactor magnification factor - 1.0 (default) means that the glyphs will occupy the whole screen. mFactor &gt; 1 will zoom out from this default location. mFactor &lt; 1 will do the opposite
		*@return the final camera location
		*@see #getGlobalView(Camera c)
		*@see #getGlobalView(Camera c, int d)
		*@see #getGlobalView(Camera c, float mFactor)
		*/
	public Location getGlobalView(Camera c, int d, float mFactor){
		Location l = getGlobalView(c, mFactor);
		if (l != null){
		    Animation trans = 
			animationManager.getAnimationFactory().createCameraTranslation(d,c,
										       new LongPoint(l.vx,l.vy),
										       false,
										       SlowInSlowOutInterpolator.getInstance(),
										       null);
		    
		    Animation alt = 
			animationManager.getAnimationFactory().createCameraAltAnim(d,c,
										   l.alt,
										   false,
										   SlowInSlowOutInterpolator.getInstance(),
										   null);
		    
		    animationManager.startAnimation(trans, false);
		    animationManager.startAnimation(alt, false);
		}
		return l;
	}
    
    /**returns the leftmost Glyph x-pos, upmost Glyph y-pos, rightmost Glyph x-pos, downmost Glyph y-pos visible in virtual space s*/
    public static long[] findFarmostGlyphCoords(VirtualSpace s){
	return findFarmostGlyphCoords(s, new long[4]);
    }

    /**returns the leftmost Glyph x-pos, upmost Glyph y-pos, rightmost Glyph x-pos, downmost Glyph y-pos visible in virtual space s*/
    public static long[] findFarmostGlyphCoords(VirtualSpace s, long[] res){
	if (s!=null){
	    return s.findFarmostGlyphCoords(res);
	}
	else return null;
    }

    /**translates and (un)zooms a camera in order to focus on glyph g
     *@param g Glyph of interest
     *@param c Camera to be moved
     *@param d duration of the animation in ms
     *@return the final camera location
     */
    public Location centerOnGlyph(Glyph g,Camera c,int d){
	return this.centerOnGlyph(g,c,d,true);
    }

    /**translates and (un)zooms a camera in order to focus on glyph g
     *@param g Glyph of interest
     *@param c Camera to be moved
     *@param d duration of the animation in ms
     *@param z if false, do not (un)zoom, just translate (default is true)
     *@return the final camera location
     */
    public Location centerOnGlyph(Glyph g, Camera c, int d, boolean z){
	return this.centerOnGlyph(g, c, d, z, 1.0f);
    }

    /**translates and (un)zooms a camera in order to focus on glyph g
     *@param g Glyph of interest
     *@param c Camera to be moved
     *@param d duration of the animation in ms
     *@param z if false, do not (un)zoom, just translate (default is true)
     *@param mFactor magnification factor - 1.0 (default) means that the glyph will occupy the whole screen. mFactor < 1 will make the glyph smaller (zoom out). mFactor > 1 will make the glyph appear bigger (zoom in)
     *@return the final camera location
     */
    public Location centerOnGlyph(Glyph g, Camera c, int d, boolean z, float mFactor){
	return this.centerOnGlyph(g, c, d, z, mFactor, null);
    }

    /**translates and (un)zooms a camera in order to focus on glyph g
        *@param g Glyph of interest
        *@param c Camera to be moved
        *@param d duration of the animation in ms
        *@param z if false, do not (un)zoom, just translate (default is true)
        *@param mFactor magnification factor: 1.0 (default) means that the glyph will occupy the whole screen. mFactor < 1 will make the glyph smaller (zoom out). mFactor > 1 will make the glyph appear bigger (zoom in)
        *@param endAction end action to execute after camera reaches its final position
        *@return the final camera location
        */
    public Location centerOnGlyph(Glyph g, Camera c, int d, boolean z, float mFactor, EndAction endAction){
        View v=null;
        try {
            v=c.getOwningView();
            if (v!=null){
                long dx;
                long dy;
                if (g instanceof VText){
                    VText t=(VText)g;
                    LongPoint p=t.getBounds(c.getIndex());
                    if (t.getTextAnchor()==VText.TEXT_ANCHOR_START){
                        dx=g.vx+p.x/2-c.posx;
                        dy=g.vy+p.y/2-c.posy;
                    }
                    else if (t.getTextAnchor()==VText.TEXT_ANCHOR_MIDDLE){
                        dx=g.vx-c.posx;
                        dy=g.vy-c.posy;
                    }
                    else {
                        dx=g.vx-p.x/2-c.posx;
                        dy=g.vy-p.y/2-c.posy;
                    }
                }
                else if (g instanceof VPath){
                    VPath p=(VPath)g;
                    dx=p.realHotSpot.x-c.posx;
                    dy=p.realHotSpot.y-c.posy;
                }
                else {
                    dx=g.vx-c.posx;
                    dy=g.vy-c.posy;
                }

		//relative translation
		Animation trans = 
		    animationManager.getAnimationFactory().
		    createCameraTranslation(d, c,
					    new LongPoint(dx,dy),
					    true,
					    SlowInSlowOutInterpolator.getInstance(),
					    endAction);
		animationManager.startAnimation(trans, false);

                float currentAlt=c.getAltitude()+c.getFocal();
                if (z){
                    long[] regBounds=v.getVisibleRegion(c);
                    // region that will be visible after translation, but before zoom/unzoom  (need to compute zoom) ;
                    // we only take left and down because ratios are equals for left and right, up and down
                    long[] trRegBounds={regBounds[0]+dx,regBounds[3]+dy};
                    float ratio=0;
                    //compute the mult factor for altitude to see glyph g entirely
                    if (trRegBounds[0]!=0){
                        if (g instanceof VText){
                            ratio = ((float)(((VText)g).getBounds(c.getIndex()).x)) / ((float)(g.vx-trRegBounds[0]));
                        }
                        else if (g instanceof RectangularShape){
                            ratio = ((float)(((RectangularShape)g).getWidth())) / ((float)(g.vx-trRegBounds[0]));
                        }
                        else {
                            ratio = g.getSize() / ((float)(g.vx-trRegBounds[0]));
                        }
                    }
                    //same for Y ; take the max of both
                    if (trRegBounds[1]!=0){
                        float tmpRatio;
                        if (g instanceof VText){
                            tmpRatio = ((float)(((VText)g).getBounds(c.getIndex()).y)) / ((float)(g.vy-trRegBounds[1]));
                        }
                        else if (g instanceof RectangularShape){
                            tmpRatio = ((float)(((RectangularShape)g).getHeight())) / ((float)(g.vy-trRegBounds[1]));
                        }
                        else {
                            tmpRatio = (g.getSize())/((float)(g.vy-trRegBounds[1]));
                        }
                        if (tmpRatio>ratio){ratio=tmpRatio;}
                    }
                    ratio *= mFactor;
                    float newAlt=currentAlt*Math.abs(ratio);
		 
		    Animation altAnim = 
			animationManager.getAnimationFactory().
			createCameraAltAnim(d, c, 
					    newAlt, false,
					    SlowInSlowOutInterpolator.getInstance(),
					    null);
		    animationManager.startAnimation(altAnim, false);

                    return new Location(g.vx,g.vy,newAlt);
                }
                else {
		    return new Location(g.vx,g.vy,currentAlt);
                }
            }
            else return null;
        }
        catch (NullPointerException e){
            System.err.println("Error:VirtualSpaceManager:centerOnGlyph: ");
            System.err.println("Glyph g="+g);
            System.err.println("Camera c="+c);
            System.err.println("View v="+v);
            if (debug){e.printStackTrace();}
            else {System.err.println(e);}
            return null;
        }
    }

	/**translates and (un)zooms a camera in order to focus on a specific rectangular region
		*@param c Camera to be moved
		*@param d duration of the animation in ms
		*@param x1 x coord of first point
		*@param y1 y coord of first point
		*@param x2 x coord of opposite point
		*@param y2 y coord of opposite point
		*@return the final camera location
		*/
	public Location centerOnRegion(Camera c,int d,long x1,long y1,long x2,long y2){
		View v=null;
		try {
			v=c.getOwningView();
			if (v!=null){
				long minX=Math.min(x1,x2);
				long minY=Math.min(y1,y2);
				long maxX=Math.max(x1,x2);
				long maxY=Math.max(y1,y2);
				long[] wnes={minX,maxY,maxX,minY};  //wnes=west north east south
				long dx=(wnes[2]+wnes[0])/2;  //new coords where camera should go
				long dy=(wnes[1]+wnes[3])/2;
				long[] regBounds=v.getVisibleRegion(c);
				// region that will be visible after translation, but before zoom/unzoom  (need to compute zoom) ;
				// we only take left and down because we only need horizontal and vertical ratios, which are equals for left and right, up and down
				long[] trRegBounds={regBounds[0]+dx-c.posx,regBounds[3]+dy-c.posy};
				float currentAlt=c.getAltitude()+c.getFocal();
				float ratio=0;
				//compute the mult factor for altitude to see all stuff on X
				if (trRegBounds[0]!=0){ratio=(dx-wnes[0])/((float)(dx-trRegBounds[0]));}
				//same for Y ; take the max of both
				if (trRegBounds[1]!=0){
					float tmpRatio=(dy-wnes[3])/((float)(dy-trRegBounds[1]));
					if (tmpRatio>ratio){ratio=tmpRatio;}
				}
				float newAlt=currentAlt*Math.abs(ratio);
			
				Animation trans = 
				    animationManager.getAnimationFactory().
				    createCameraTranslation(d, c, new LongPoint(dx, dy), false,
							    SlowInSlowOutInterpolator.getInstance(),
							    null);
				
				Animation altAnim = 
				    animationManager.getAnimationFactory().
				    createCameraAltAnim(d, c, newAlt, false,
							SlowInSlowOutInterpolator.getInstance(),
							null);

				animationManager.startAnimation(trans, false);
				animationManager.startAnimation(altAnim, false);
				
				return new Location(dx,dy,newAlt);
			}
			else return null;
		}
		catch (NullPointerException e){
			System.err.println("Error:VirtualSpaceManager:centerOnRegion: ");
			System.err.println("Camera c="+c);
			System.err.println("View v="+v);
			if (debug){e.printStackTrace();}
			else {System.err.println(e);}
			return null;
		}
	}

    /** returns a vector of glyphs whose hotspot is in region delimited by rectangle (x1,y1,x2,y2) in virtual space vs (returns null if empty). Coordinates of the mouse cursor in virtual space are available in instance variables vx and vy of class VCursor. The selection rectangle can be drawn on screen by using ViewPanel.setDrawRect(true) (e.g. call when mouse button is pressed)/ViewPanel.setDrawRect(false) (e.g. call when mouse button is released)
     *@param x1 x coord of first point
     *@param y1 y coord of first point
     *@param x2 x coord of opposite point
     *@param y2 y coord of opposite point
     *@param vsn name of virtual space
     *@param wg which glyphs in the region should be returned (among VIS_AND_SENS_GLYPHS (default), VISIBLE_GLYPHS, SENSIBLE_GLYPHS, ALL_GLYPHS)
     */
    public Vector getGlyphsInRegion(long x1,long y1,long x2,long y2,String vsn,int wg){
	Vector res=new Vector();
	VirtualSpace vs=getVirtualSpace(vsn);
	long minX=Math.min(x1,x2);
	long minY=Math.min(y1,y2);
	long maxX=Math.max(x1,x2);
	long maxY=Math.max(y1,y2);
	if (vs!=null){
	    Vector allG=vs.getAllGlyphs();
	    Glyph g;
	    for (int i=0;i<allG.size();i++){
		g=(Glyph)allG.elementAt(i);
		if ((g.vx>=minX) && (g.vy>=minY) && (g.vx<=maxX) && (g.vy<=maxY)){
		    if ((wg==VIS_AND_SENS_GLYPHS) && g.isSensitive() && g.isVisible()){res.add(g);}
		    else if ((wg==VISIBLE_GLYPHS) && g.isVisible()){res.add(g);}
		    else if ((wg==SENSITIVE_GLYPHS) && g.isSensitive()){res.add(g);}
		    else if (wg==ALL_GLYPHS){res.add(g);}
		}
	    }
	}
	if (res.isEmpty()){res=null;}
	return res;
    }

    /**set the value under which a VText is drawn as a point instead of a text (considered too small to be read). Default is 0.5 (it is compared to the product of the font size by the projection value) - if you raise this value, more text that was still displayed as a string will be displayed as a segment and inversely - of course, displaying a line instead of applying affine transformations to strings is faster*/
    public void setTextDisplayedAsSegCoef(float f){
	textAsLineCoef=f;
    }

    /**set the value under which a VText is drawn as a point instead of a text (considered too small to be read). Default is 0.5 (it is compared to the product of the font size by the projection value)*/
    public float getTextDisplayedAsSegCoef(){
	return textAsLineCoef;
    }

    /**should not be used by applications - public because accessed by Glyphs themselves when made unsensitive*/
    public void removeGlyphFromUnderMouseLists(Glyph g){
	VirtualSpace vs=null;
	try {
	    for (Enumeration e=allVirtualSpaces.elements();e.hasMoreElements();){
		vs=(VirtualSpace)e.nextElement();
		if (vs.getAllGlyphs().contains(g)){break;}
	    }
	    Camera[] cl = vs.getCameraListAsArray();
	    for (int i=0;i<cl.length;i++){
		((View)(cl[i].getOwningView())).mouse.removeGlyphFromList(g);
	    }
	}
	catch (NullPointerException ex){}
    }
    
    Object activeJFrame=null;
    
    public void eventDispatched(AWTEvent e){
	if (e.getID() == WindowEvent.WINDOW_ACTIVATED){activeJFrame=e.getSource();}
    }

}



