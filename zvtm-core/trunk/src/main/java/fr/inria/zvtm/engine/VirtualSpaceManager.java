/*   FILE: VirtualSpaceManager.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2010. All Rights Reserved
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

package fr.inria.zvtm.engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JFrame;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.event.RepaintListener;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.lens.Lens;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;

/**
 * Virtual space manager. This is the main entry point to the toolkit. Virtual spaces and views are instanciated from here.
 * @author Emmanuel Pietriga
 **/

public class VirtualSpaceManager implements AWTEventListener {

	/** Called by VText to update default font. */
	public void onMainFontUpdated(){
		for (int i=0;i<allViews.length;i++){
			allViews[i].updateFont();
		}
		Object g;
		for (Enumeration<VirtualSpace> e=allVirtualSpaces.elements();e.hasMoreElements();){
    		for (Enumeration e2=e.nextElement().getAllGlyphs().elements();e2.hasMoreElements();){
    			g = e2.nextElement();
    			if (g instanceof VText){((VText)g).invalidate();}
    		}		    
		}
		repaint();
	}

	/** Select only glyphs that are visible and sensitive to the cursor. */
	public static short VIS_AND_SENS_GLYPHS=0;
	/** Select only glyphs that are visible. */ 
	public static short VISIBLE_GLYPHS=1;
	/** Select only glyphs that are sensitive to the cursor. */
    public static short SENSITIVE_GLYPHS=2;
    /** Select all glyphs in the region. */
    public static short ALL_GLYPHS=3;     

    /**print exceptions and warning*/
    static boolean debug = false;

    /**key is space name (String)*/
    protected Hashtable<String,VirtualSpace> allVirtualSpaces;
    /**All views managed by this VSM*/
    protected View[] allViews;
    /**used to quickly retrieve a view by its name (gives its index position in the list of views)*/
    protected Hashtable<String,Integer> name2viewIndex;

    /**View which has the focus (or which was the last to have it among all views)*/
    View activeView;
    protected int activeViewIndex = -1;

    /**default policy for view repainting - true means all views are repainted even if ((not active) or (mouse not inside the view)) - false means only the active view and the view in which the mouse is currently located (if different) are repainted - default is true*/
    boolean generalRepaintPolicy=true;

    /**enables detection of multiple full fills in one view repaint - default value assigned to new views  - STILL VERY BUGGY - ONLY SUPPORTS VRectangle and VCircle for now - setting it to true will prevent some glyphs from being painted if they are not visible in the final rendering (because of occlusion). This can enhance performance (in configurations where occlusion does happen).*/
    boolean defaultMultiFill=false;

    /**Animation Manager*/
    private final AnimationManager animationManager;
    
    public static final VirtualSpaceManager INSTANCE = new VirtualSpaceManager();
 
    /**
     * Automatic instantiation as a singleton. THere is always a single VSM per application.
     */
    private VirtualSpaceManager(){
		if (debug){System.out.println("Debug mode ON");}
		animationManager = new AnimationManager(this);
		allVirtualSpaces=new Hashtable<String,VirtualSpace>();
		allViews = new View[0];
		name2viewIndex = new Hashtable<String,Integer>();
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

    /** Sets the policy for repainting views.
     *@param b true means all views are repainted even if ((not active) or (mouse not inside the view)).
     * Policy is forwarded to all existing views (no matter its current policy) and will be applied to future ones (but it can be changed for each single view).
     */
    public void setRepaintPolicy(boolean b){
        if (b!=generalRepaintPolicy){
            generalRepaintPolicy=b;
            for (int i=0;i<allViews.length;i++){
                allViews[i].setRepaintPolicy(generalRepaintPolicy);
            }
        }
    }

    /** Get the policy for repainting views.
     *@return true means all views are repainted even if ((not active) or (mouse not inside the view)).
     * Policy is forwarded to all existing views (no matter its current policy) and will be applied to future ones (but it can be changed for each single view).
     */
    public boolean getRepaintPolicy(){return generalRepaintPolicy;}

    /** Enable/disable detection of multiple full fills in one view repaint for this View.
     * Off by default.
     * If enabled, all glyphs below the higest glyph in the drawing stack that fills the viewport will not be painted, as they will be invisible anyway.
     * This computation has a cost. Assess its usefulness and evaluate performance (there is tradeoff).
     *@see #getDefaultDetectMultiFills()
     */
    public void setDefaultDetectMultiFills(boolean b){
	    defaultMultiFill=b;
    }

    /** Tells whether detection of multiple full fills in one view repaint for this View is enabled or disabled.
     * Off by default.
     * If enabled, all glyphs below the higest glyph in the drawing stack that fills the viewport will not be painted, as they will be invisible anyway.
     * This computation has a cost. Assess its usefulness and evaluate performance (there is tradeoff).
     *@see #setDefaultDetectMultiFills(boolean b)
     */
    public boolean getDefaultDetectMultiFills(){
	    return defaultMultiFill;
    }

    /* -------------- Active entities ------------------ */

    Object activeJFrame = null;
    
    public void eventDispatched(AWTEvent e){
	    if (e.getID() == WindowEvent.WINDOW_ACTIVATED){activeJFrame=e.getSource();}
    }

    /** Manually set what view is active. */
    public void setActiveView(View v){
        activeView=v;
        activeViewIndex = getViewIndex(v.getName());
    }

    /** Get currently active view. */
    public View getActiveView(){
        return activeView;
    }

    /** Get active camera (in focused view).
     *@return null if no view is active
     */
    public Camera getActiveCamera(){
	    return (activeView != null) ? activeView.getActiveCamera() : null;
    }

    /* -------------- PORTALS ------------------ */

    /** Add a portal to a View.
     *@param p Portal to be added
     *@param v owning View
     */
    public Portal addPortal(Portal p, View v){
		return v.addPortal(p);
	}

    /** Destroy a portal (remove it from the View). */
    public void destroyPortal(Portal p){
		View v = p.getOwningView();
		v.removePortal(p);
	}

    /* ----------------- VIEWS ---------------- */

    /** Create a new External View.<br>
     *@param c vector of cameras making this view (if more than one camera, cameras will be superimposed on different layers)
     *@param name view name
     *@param viewType one of View.STD_VIEW, View.VOLATILE_VIEW, View.OPENGL_VIEW - determines the type of view and acceleration method
     *@param w width of window in pixels
     *@param h height of window in pixels
     *@param bar true -&gt; add a status bar to this view (below main panel)
     *@param visible should the view be made visible automatically or not
     */
    public View addFrameView(List<Camera> c, String name, short viewType, int w, int h, boolean bar, boolean visible){
	    return addFrameView(new Vector<Camera>(c), name, viewType, w, h, bar, visible, null);
    }

    /** Create a new External View.<br>
     * The use of OPENGL_VIEW requires the following Java property: -Dsun.java2d.opengl=true
     *@param c vector of cameras making this view (if more than one camera, cameras will be superimposed on different layers)
     *@param name view name
     *@param viewType one of View.STD_VIEW, View.VOLATILE_VIEW, View.OPENGL_VIEW - determines the type of view and acceleration method
     *@param w width of window in pixels
     *@param h height of window in pixels
     *@param bar true -&gt; add a status bar to this view (below main panel)
     *@param visible should the view be made visible automatically or not
     *@param mnb a menu bar (null if none), already configured with ActionListeners already attached to items (it is just added to the view)
     *@see #addFrameView(Vector c, String name, short viewType, int w, int h, boolean bar, boolean visible, boolean decorated, JMenuBar mnb)
     */
    public View addFrameView(List<Camera> c, String name, short viewType, int w, int h,
				boolean bar, boolean visible, JMenuBar mnb){
	    return addFrameView(new Vector<Camera>(c), name, viewType, w, h, bar, visible, true, mnb);
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
     *@see #addFrameView(Vector c, String name, short viewType, int w, int h, boolean bar, boolean visible, JMenuBar mnb)
     */
    public View addFrameView(List<Camera> c, String name, short viewType, int w, int h,
				boolean bar, boolean visible, boolean decorated, JMenuBar mnb){
        View v = null;
        switch(viewType){
            case View.STD_VIEW:{
                v = (mnb != null) ? new EView(new Vector<Camera>(c), name, w, h, bar, visible, decorated, mnb) : new EView(new Vector<Camera>(c), name, w, h, bar, visible, decorated);
                addView(v);
                v.setRepaintPolicy(generalRepaintPolicy);
                break;
            }
            case View.OPENGL_VIEW:{
                v = (mnb != null) ? new GLEView(new Vector<Camera>(c), name, w, h, bar, visible, decorated, mnb) : new GLEView(new Vector<Camera>(c), name, w, h, bar, visible, decorated);
                addView(v);
                v.setRepaintPolicy(generalRepaintPolicy);
                break;
            }
        }
        return v;
    }

    /**Create a new view embedded in a JPanel, suitable for inclusion in any Swing component hierarchy, including a JApplet.
     *@param c vector of cameras superimposed in this view
     *@param name view name
     *@param w width of window in pixels
     *@param h height of window in pixels
     */
    public JPanel addPanelView(List<Camera> c,String name,int w,int h){
        PView tvi = new PView(new Vector<Camera>(c), name, w, h);
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
    public View addFrameView(List<Camera> cameraList, String name, int panelWidth, int panelHeight,
				boolean visible, boolean decorated, short viewType,
				JPanel parentPanel, JFrame frame) {
    	View v = new JPanelView(new Vector<Camera>(cameraList), name, panelWidth, panelHeight,
            visible, decorated, viewType,
            parentPanel, frame);
        addView(v);
        v.setRepaintPolicy(generalRepaintPolicy);
        return v;
    }

    /** Get index of View whose name is n (-1 if view does not exist) .*/
    protected int getViewIndex(String n){
        try {
            return name2viewIndex.get(n).intValue();
        }
        catch (NullPointerException ex){return -1;}
    }

    /** Get View whose name is n.
     *@return null if no match
     */
    public View getView(String n){
        int index = getViewIndex(n);
        if (index != -1){
            return allViews[index];
        }
        else {
            return null;
        }
    }

    /** Destroy a View identified by its index in the list of views.*/
    protected void destroyView(int i){
        View[] tmpA = new View[allViews.length-1];
        if (tmpA.length > 0){
            System.arraycopy(allViews, 0, tmpA, 0, i);
            System.arraycopy(allViews, i+1, tmpA, i, allViews.length-i-1);
        }
        allViews = tmpA;
        updateViewIndex();
    }

    /** Update mapping between view name and view index in the list of views when
     * complex changes are made to the list of views (like removing a view).
     */
    protected void updateViewIndex(){
        name2viewIndex.clear();
        for (int i=0;i<allViews.length;i++){
            name2viewIndex.put(allViews[i].name, new Integer(i));
        }
    }

    /** Destroy a view. */
    protected void destroyView(View v){
        for (int i=0;i<allViews.length;i++){
            if (allViews[i] == v){
                destroyView(i);
                break;
            }
        }
    }

    /** Destroy a view. */
    protected void destroyView(String viewName){
	    destroyView(getView(viewName));
    }

    /** Ask for all Views to be repainted. This is an asynchronous call.
     * In some cases it is not possible to detect graphical changes so repaint
     * calls have to be issued manually (unless you are willing to wait for
     * another event to trigger repaint).
     *@see #repaint(View v)
     *@see #repaint(View v, RepaintListener rl)
     */
    public void repaint(){
        for (int i=0;i<allViews.length;i++){
            allViews[i].repaint();
        }
    }

    /** Ask for View v to be repainted. This is an asynchronous call.
     * In some cases it is not possible to detect graphical changes so repaint
     * calls have to be issued manually (unless you are willing to wait for
     * another event to trigger repaint).
     *@see #repaint()
     *@see #repaint(View v, RepaintListener rl)
     */
    public void repaint(View v){
	    v.repaint();
    }

    /** Ask for View v to be repainted. This is an asynchronous call.
     * In some cases it is not possible to detect graphical changes so repaint
     * calls have to be issued manually (unless you are willing to wait for
     * another event to trigger repaint).
     *@param v the view to repaint
     *@param rl a repaint listener to be notified when this repaint cycle is completed (it must be removed manually if you are not interested in being notified about following repaint cycles)
     *@see #repaint(View v)
     *@see View#removeRepaintListener()
     */
    public void repaint(View v, RepaintListener rl){
	    v.repaint(rl);
    }
    
    /* ----------- VIRTUAL SPACE --------------- */

    /** Create a new virtual space.
     *@param n name of this virtual space
     *@return the new virtual space
     */
    public VirtualSpace addVirtualSpace(String n){
        VirtualSpace tvs=new VirtualSpace(n);
        allVirtualSpaces.put(n,tvs);
        return tvs;
    }

    /** Destroy a virtual space.
     *@param n name of this virtual space
     */
    public void destroyVirtualSpace(String n){
        if (allVirtualSpaces.containsKey(n)){
            allVirtualSpaces.get(n).destroy();
            allVirtualSpaces.remove(n);
        }
    }

    /** Destroy a virtual space.
     *@param vs virtual space to destroy
     */
    public void destroyVirtualSpace(VirtualSpace vs){
        vs.destroy();
        String n = vs.getName();
        if (allVirtualSpaces.containsKey(n)){
            allVirtualSpaces.remove(n);
        }
    }

    /** Get the virtual space owning Glyph g. */
    public VirtualSpace getOwningSpace(Glyph g){
        VirtualSpace vs;
        for (Enumeration<VirtualSpace> e=allVirtualSpaces.elements();e.hasMoreElements();){
            vs = e.nextElement();
            if (vs.getAllGlyphs().contains(g)){return vs;}
        }
        return null;
    }

    /** Get virtual space named n.
     *@return null if no virtual space named n
     */
    public VirtualSpace getVirtualSpace(String n){
	    return allVirtualSpaces.get(n);
    }

    /** Get active virtual space, i.e., the space owning the camera currently active.
     *@return null if no camera/view is active
     */
    public VirtualSpace getActiveSpace(){
	    return (activeView != null) ? activeView.getActiveCamera().getOwningSpace() : null;
    }

}
