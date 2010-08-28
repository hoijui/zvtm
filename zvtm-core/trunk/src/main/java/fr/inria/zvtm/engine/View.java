/*   FILE: View.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.GridBagConstraints;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.JLabel;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.RepaintListener;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.lens.Lens;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.RectangularShape;
import fr.inria.zvtm.engine.portals.Portal;

  /**
   * A view is a window and can be composed of one or several cameras superimposed - use EView or IView <BR>
   * A view is repainted on a regular basis when active - for inactive views, the default is to repaint only if the mouse is inside the view (but the frame is not selected) - this can be changed to repaint the view automatically even if it is not selected and if the mouse is not inside, using setRepaintPolicy()
   * @author Emmanuel Pietriga
   **/

public abstract class View {

    /**Standard ZVTM view, with no particular acceleration method*/
    public static final short STD_VIEW = 0;
    /**ZVTM view based on Java 5's OpenGL rendering pipeline; does accelerate rendering but requires a JVM 1.5 or later*/
    public static final short OPENGL_VIEW = 1;

    /**list of Camera objects used in this view*/
    Vector<Camera> cameras;

    void initCameras(Vector<Camera> c){
        cameras = c;
        for (int i=0;i<cameras.size();i++){
            c.elementAt(i).setOwningView(this);
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

	/** Get this View's cursor object. */
	public VCursor getCursor(){
		return mouse;
	}

    /**the actual panel*/
    ViewPanel panel;

    /**enables detection of multiple full fills in one view repaint - for this specific view - STILL VERY BUGGY - ONLY SUPPORTS VRectangle and VCircle for now*/
    boolean detectMultipleFullFills;

    JLabel statusBar;

    /**View name*/
    protected String name;

    /**triggers the mouseMoved method in ViewEventHandler when the mouse is moved - set to false by default because few applications will need this; it is therefore not necessary to overload other applications with these events*/
    boolean notifyCursorMoved = true;

    /**hooks for Java2D painting in ZVTM views (BACKGROUND, FOREGROUND, AFTER_DISTORTION, AFTER_PORTALS)*/
    Java2DPainter[] painters = new Java2DPainter[4];

    /**
     * Get the ViewPanel associated with this View.
     */
    public ViewPanel getPanel(){
	    return panel;
    }

    /** Destroy this view. */
    public abstract void destroyView();

    /** Get the java.awt.Container for this View. */
    public abstract Container getFrame();

    /**Set the cursor for this View.
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
   
    /** Set application class instance to which events are sent for all layers in this view.
     *@param eh client application implementation of ViewEventHandler
     */
    public void setEventHandler(ViewEventHandler eh){
	    for (int i=0;i<cameras.size();i++){
    	    setEventHandler(eh, i);	        
	    }
    }

    /** Set application class instance to which events are sent for a given layer.
    *@param eh client application implementation of ViewEventHandler
     *@param layer depth of layer to which the event handler should be associated.
     */
    public void setEventHandler(ViewEventHandler eh, int layer){
	    panel.setEventHandler(eh, layer);
    }

    /** Sets whether the mouseMoved callback in ViewEventHandler is triggered when the cursor moves.
     * Set to true by default. Applications that do not care about this callback can disable notification
     * about these events to avoid unnecessary callbacks (an event each sent each time the cursor moves).
     */
    public void setNotifyCursorMoved(boolean b){
	    notifyCursorMoved=b;
    }

    /** Tells whether the mouseMoved callback in ViewEventHandler is triggered when the mouse is moved.
     * Set to true by default.*/
    public boolean getNotifyCursorMoved(){return notifyCursorMoved;}

    /** Set status bar text. */
    public void setStatusBarText(String s){
	    if (statusBar!=null){if (s.equals("")){statusBar.setText(" ");}else{{statusBar.setText(s);}}}
    }

    /** Set font used in status bar text. */
    public void setStatusBarFont(Font f){
	    if (statusBar!=null){statusBar.setFont(f);}
    }

    /** Set color used for status bar text. */
    public void setStatusBarForeground(Color c){
	    if (statusBar!=null){statusBar.setForeground(c);}
    }

    /** Enable/disable detection of multiple full fills in one view repaint for this View.
     * Off by default.
     * If enabled, all glyphs below the higest glyph in the drawing stack that fills the viewport will not be painted, as they will be invisible anyway.
     * This computation has a cost. Assess its usefulness and evaluate performance (there is tradeoff).
     *@see #getDetectMultiFills()
     */
    public void setDetectMultiFills(boolean b){
	    detectMultipleFullFills=b;
    }

    /** Tells whether detection of multiple full fills in one view repaint is enabled or not for this View.
     * Off by default.
     *@see #setDetectMultiFills(boolean b)
     */
    public boolean getDetectMultiFills(){
	    return detectMultipleFullFills;
    }

    /** Get bounds of rectangular region of the VirtualSpace seen through a camera.
     *@param c camera
     *@return boundaries in VirtualSpace coordinates {west,north,east,south}
     */
    public double[] getVisibleRegion(Camera c){
	    return getVisibleRegion(c, new double[4]);
    }

    /** Get bounds of rectangular region of the VirtualSpace seen through a camera.
     *@param c camera
     *@param res array which will contain the result
     *@return boundaries in VirtualSpace coordinates {west,north,east,south}
     */
    public double[] getVisibleRegion(Camera c, double[] res){
        if (cameras.contains(c)){
            //compute region seen from this view through camera
            double uncoef = (c.focal+c.altitude) / c.focal;
            Dimension panelSize = panel.getSize();
            res[0] = c.vx-(panelSize.width/2-panel.visibilityPadding[0])*uncoef;
            res[1] = c.vy+(panelSize.height/2-panel.visibilityPadding[1])*uncoef;
            res[2] = c.vx+(panelSize.width/2-panel.visibilityPadding[2])*uncoef;
            res[3] = c.vy-(panelSize.height/2-panel.visibilityPadding[3])*uncoef;
            return res;
        }
        return null;
    }

    /** Get width of rectangular region of the VirtualSpace seen through a camera.
     *@param c camera
     *@return width in VirtualSpace coordinates (from west to east boundaries)
     */
    public double getVisibleRegionWidth(Camera c){
	    return panel.getSize().width * ((c.focal+c.altitude) / c.focal);
    }

    /** Get height of rectangular region of the VirtualSpace seen through a camera.
     *@param c camera
     *@return width in VirtualSpace coordinates (from north to south boundaries)
     */
    public double getVisibleRegionHeight(Camera c){
	    return panel.getSize().height * ((c.focal+c.altitude) / c.focal);
    }

    /** Set which layer (camera) is currently active (getting events).
     * @param i layer index. 0 is the deepest layer (first camera given in the Vector at construction time).
     */
    public void setActiveLayer(int i){
        Camera c = (Camera)cameras.elementAt(i);
        mouse.unProject(c, panel);
        mouse.resetGlyphsUnderMouseList(c.parentSpace,
            c.getIndex());
        panel.activeLayer=i;
    }

    /** Get index of layer (camera) currently active (getting events).
     *@return layer index. 0 is the deepest layer (first camera given in the Vector at construction time).
     */
    public int getActiveLayer(){
	    return panel.activeLayer;
    }
    
    /** Get the number of layers (cameras) in this View. */
    public int getLayerCount(){
	    return cameras.size();
    }
    
    /** Update default font used in this View. */
    public void updateFont(){panel.updateFont=true;}

    /** Set antialias rendering hint for this View. */
    public void setAntialiasing(boolean b){
        if (b!=panel.antialias){
            panel.antialias=b;
            panel.updateAntialias=true;
            repaint();
        }
    }

    /** Get the value of the antialias rendering hint for this View. */
    public boolean getAntialiasing(){
	    return panel.antialias;
    }

    /** Get camera for layer i.
     *@param i layer index. 0 is the deepest layer (first camera given in the Vector at construction time).
     */
    public Camera getCameraNumber(int i){
        if (cameras.size()>i){return (Camera)cameras.elementAt(i);}
        else return null;
    }

    /** Get camera corresponding to layer currently active (getting events).
     */
    public Camera getActiveCamera(){
	return panel.cams[panel.activeLayer];
    }

    void destroyCamera(Camera c){
        for (int i=0;i<panel.cams.length;i++){
            if (panel.cams[i]==c){
                panel.cams[i]=null;
                if (i==panel.activeLayer){
                    //if the camera we remove was associated to the active layer, make active another non-null layer
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

    /** Set background color for this View. */
    public void setBackgroundColor(Color c){
	    panel.backColor = c;
    }

    /** Get background color of this view. */
    public Color getBackgroundColor(){
	    return panel.backColor;
    }

    public abstract boolean isSelected();

    /** Sets the title for this frame to the specified string.
     *@param t - the title to be displayed in the frame's border. A null value is treated as an empty string, "".
     */
    public abstract void setTitle(String t);

    /** Moves this component to a new location.
     * The top-left corner of the new location is specified by the x and y parameters in the coordinate space of this component's parent.
     *@param x the x-coordinate of the new location's top-left corner in the parent's coordinate space
     *@param y the y-coordinate of the new location's top-left corner in the parent's coordinate space
     */
    public abstract void setLocation(int x,int y);

    /** Resizes this component so that it has width width and height height. 
     *@param width - the new width of this component in pixels
     *@param height - the new height of this component in pixels
     */
    public abstract void setSize(int width, int height);

    /** Get the dimensions of the ZVTM panel embedded in this View. */
    public Dimension getPanelSize(){
	    return panel.size;
    }

    /** Sets whether this View is resizable by the user.
     *@param resizable - true if this frame is resizable; false otherwise.
     */
    public abstract void setResizable(boolean resizable);

    /** Shows or hides this View depending on the value of parameter b. */
    public abstract void setVisible(boolean b);

    /** Set this View's refresh rate - default is 20.
     *@param rr positive integer (refresh rate in milliseconds)
     */
    public void setRefreshRate(int rr){
	    panel.setRefreshRate(rr);
    }

    /** Get this View's refresh rate - default is 20.*/
    public int getRefreshRate(){
	    return panel.getRefreshRate();
    }

    /*XXX:should repaint this view on a regular basis or not (even if not activated, but does not apply to iconified views)*/
    public void setRepaintPolicy(boolean b){
        panel.alwaysRepaintMe=b;
        if (b){panel.active=true;}
        else {if ((!isSelected()) && (!panel.inside)){panel.active=false;}}
    }

    /**
     * Make a view blank. The view is erased and filled with a uniform color.
     *@param c blank color (will fill the entire view) - pass null to exit blank mode.
     */
    public void setBlank(Color c){
        if (c==null){
            panel.blankColor=null;
            panel.notBlank=true;
            repaint();
        }
        else {
            panel.blankColor=c;
            panel.notBlank=false;
            repaint();
        }
    }

    /**
     * Says whether a view is in blank mode or not.
     *@return the fill color if in blank mode, null if not in blank mode.
     */
    public Color isBlank(){
        if (!panel.notBlank){
            return panel.blankColor;
        }
        else return null;
    }

    /** Activating the view means that it will be repainted. */
    public void activate(){
        VirtualSpaceManager.INSTANCE.setActiveView(this);
        panel.active=true;
        if (panel.evHs[panel.activeLayer]!=null){panel.evHs[panel.activeLayer].viewActivated(this);}
    }
    
    /** Deactivating the view (will not be repainted unless setRepaintPolicy(true) or mouse inside the view)*/
    public void deactivate(){
        if ((!panel.alwaysRepaintMe) && (!panel.inside)){panel.active=false;}
        if (panel.evHs[panel.activeLayer]!=null){panel.evHs[panel.activeLayer].viewDeactivated(this);}
    }

    /** Called from the window listener when the window is iconified - repaint is automatically disabled. */
    void iconify(){
        panel.active=false;
        if (panel.evHs[panel.activeLayer]!=null){panel.evHs[panel.activeLayer].viewIconified(this);}
    }

    /** Called from the window listener when the window is deiconified - repaint is automatically re-enabled. */
    void deiconify(){
        panel.active=true;
        if (panel.evHs[panel.activeLayer]!=null){panel.evHs[panel.activeLayer].viewDeiconified(this);}
    }

    /** Called from the window listener when the window is closed. */
    protected void close(){
	    if (panel.evHs[panel.activeLayer]!=null){panel.evHs[panel.activeLayer].viewClosing(this);}
    }

    /** Ask for the view to be repainted. This is an asynchronous call.
        *@see #repaint(RepaintListener rl)
        */
    public void repaint(){
        panel.repaintASAP = true;
    }

    /** Ask for the view to be repainted. This is an asynchronous call.
     *@param rl a repaint listener, to get notified when this repaint cycle is completed. The listener must be removed manually
     * if you are not interested in being notified about following repaint cycles.
     *@see #repaint()
     *@see #removeRepaintListener()
     */
    public void repaint(RepaintListener rl){
        panel.repaintListener = rl;
        repaint();
    }

    /** Remove the repaint listener associated with this view.
     *@see #repaint(RepaintListener rl)
     */
    public void removeRepaintListener(){
	    panel.repaintListener = null;
    }

    /** Get access to the panel's AWT Graphics object.
     * This can be useful in some cases, for instance to compute the bounds of a text string
       that has not yet been added to any virtual space.
       This instance of Graphics should not be tampered with (this will be at your own risks).
     */
    public Graphics getGraphicsContext(){
	    return panel.stableRefToBackBufferGraphics;
    }

    /** Get an image of what is visible in this view, by calling getImage on the JComponent.
     *@return a copy of the original offscreen buffer.
     *@see #rasterize(int w, int h)
     *@see #rasterize(int w, int h, Vector layers)
     */
    public BufferedImage getImage(){
        BufferedImage res=null;
        BufferedImage i=panel.getImage();
        if (i!=null){
            res = new BufferedImage(i.getWidth(),i.getHeight(),i.getType());
            Graphics2D resg2d = res.createGraphics();
            resg2d.drawImage(i,null,0,0);
        }
        return res;
    }

    /** Ask for a bitmap rendering of this view and return the resulting BufferedImage.
     *@param w width of rendered image
     *@param h height of rendered image
     *@return the resulting buffered image which can then be manipulated and serialized
     *@see #rasterize(int w, int h, Vector layers)
     *@see #getImage()
     */
    public BufferedImage rasterize(int w, int h){
	    return rasterize(w, h, (Vector)null);
    }

    /** Ask for a bitmap rendering of this view and return the resulting BufferedImage.
     *@param w width of rendered image
     *@param h height of rendered image
     *@param layers Vector of cameras : what layers (represented by cameras) of this view should be rendered (you can pass null for all layers)
     *@return the resulting buffered image which can then be manipulated and serialized
     *@see #rasterize(int w, int h)
     *@see #getImage()
     */
    public BufferedImage rasterize(int w, int h, Vector<Camera> layers){
        Dimension panelSize = panel.getSize();
        float mFactor = 1/Math.min(w / ((float)panelSize.getWidth()),
            h / ((float)panelSize.getHeight()));
        Camera c, nc;
        Vector<Camera> clones= new Vector<Camera>();
        Vector<Camera> cams = (layers != null) ? layers : cameras;
        for (int i=0;i<cams.size();i++){
            c = cams.elementAt(i);
            nc = c.parentSpace.addCamera();
            nc.vx = c.vx;
            nc.vy = c.vy;
            /*change this altitude to compensate for the w/h change what we
            want is to get the same view at a higher (or lower) resolution*/
            nc.focal = c.focal;
            nc.altitude = (c.altitude + c.focal) * mFactor - c.focal;
            clones.add(nc);
        }
        BufferedImage img = (new OffscreenViewPanel(clones)).rasterize(w, h, panel.backColor);
        for (int i=0;i<clones.size();i++){
            nc = clones.elementAt(i);
            VirtualSpaceManager.INSTANCE.getVirtualSpace(nc.parentSpace.spaceName).removeCamera(nc.index);
        }
        return img;
    }

    /** Set a paint method (containing Java2D paint instructions) that will be called each time the view is repainted.
     *@param p the paint method encapsulated in an object implementing the Java2DPainter interface (pass null to unset an existing one)
     *@param g one of Java2DPainter.BACKGROUND, Java2DPainter.FOREGROUND, Java2DPainter.AFTER_DISTORTION, Java2DPainter.AFTER_PORTALS depending on whether the method should be called before or after ZVTM glyphs have been painted, after distortion by a lens (FOREGROUND and AFTER_DISTORTION are equivalent in the absence of lens), or after portals have been painted
     */
    public void setJava2DPainter(Java2DPainter p, short g){
        painters[g] = p;
        repaint();
    }

    /** Get the implementation of the paint method (containing Java2D paint instructions) that will be called each time the view is repainted.
     *@param g one of Java2DPainter.BACKGROUND, Java2DPainter.FOREGROUND, Java2DPainter.AFTER_DISTORTION, Java2DPainter.AFTER_PORTALS depending on whether the method should be called before or after ZVTM glyphs have been painted, after distortion by a lens (FOREGROUND and AFTER_DISTORTION are equivalent in the absence of lens), or after portals have been painted
     *@return p the paint method encapsulated in an object implementing the Java2DPainter interface (null if not set)
     */
    public Java2DPainter getJava2DPainter(short g){
	    return painters[g];
    }
    
    /** Get the View's name. */
    public String getName(){
	    return name;
    }

    /** Set a padding for customizing the region inside the view for which objects are actually visible.
     *@param wnesPadding padding values in pixels for the west, north, east and south borders
     */
    public void setVisibilityPadding(int[] wnesPadding){
	    panel.setVisibilityPadding(wnesPadding);
    }

    /** Get the padding values customizing the region inside the view for which objects are actually visible.
     *@return padding values in pixels for the west, north, east and south borders
     */
    public int[] getVisibilityPadding(){
	    return panel.getVisibilityPadding();
    }

    /* for gridbagconstraint layout */
    static void buildConstraints(GridBagConstraints gbc, int gx,int gy,int gw,int gh,int wx,int wy){
        gbc.gridx=gx;
        gbc.gridy=gy;
        gbc.gridwidth=gw;
        gbc.gridheight=gh;
        gbc.weightx=wx;
        gbc.weighty=wy;
    }
    
    /* --------------------- LENSES -------------------------- */

    /** Activate a lens in this view. This only works with regular views (not OpengGL views - GLEView).
     *@param l the lens instance. Pass null to remove an existing lens.
     */
    public Lens setLens(Lens l){
        Lens res = panel.setLens(l);
        return res;
    }

    /** Get Lens currently active in this view
     *@return null if none
     */
    public Lens getLens(){
        return panel.getLens();
    }
    
    /* ----------------- Navigation ------------------ */
    
    /** Get the location from which a camera will see all glyphs visible in the associated virtual space.
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
     *@param c camera considered (will not be moved)
     *@param mFactor magnification factor - 1.0 (default) means that the glyphs will occupy the whole screen. mFactor &gt; 1 will zoom out from this default location. mFactor &lt; 1 will do the opposite
     *@return the location to which the camera should go, null if the camera is not associated with this view.
     *@see #getGlobalView(Camera c, int d)
     *@see #getGlobalView(Camera c)
     *@see #getGlobalView(Camera c, int d, float mFactor)
     */
    public Location getGlobalView(Camera c, float mFactor){
        if (c.getOwningView() != this){return null;}
        //wnes=west north east south
        double[] wnes = c.parentSpace.findFarmostGlyphCoords();
        //new coords where camera should go
        double dx = (wnes[2]+wnes[0])/2d;
        double dy = (wnes[1]+wnes[3])/2d;
        double[] regBounds = this.getVisibleRegion(c);
        /*region that will be visible after translation, but before zoom/unzoom (need to
        compute zoom) ; we only take left and down because we only need horizontal and
        vertical ratios, which are equals for left and right, up and down*/
        double[] trRegBounds = {regBounds[0]+dx-c.vx, regBounds[3]+dy-c.vy};
        double currentAlt = c.getAltitude()+c.getFocal();
        double ratio = 0;
        //compute the mult factor for altitude to see all stuff on X
        if (trRegBounds[0]!=0){ratio = (dx-wnes[0]) / (dx-trRegBounds[0]);}
        //same for Y ; take the max of both
        if (trRegBounds[1]!=0){
            double tmpRatio = (dy-wnes[3]) / (dy-trRegBounds[1]);
            if (tmpRatio>ratio){ratio = tmpRatio;}
        }
        ratio *= mFactor;
        return new Location(dx, dy, currentAlt*Math.abs(ratio)-c.getFocal());
    }
    
    /** Translates and (un)zooms a camera in order to see everything visible in the associated virtual space.
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
     *@param c Camera to be moved (will actually be moved)
     *@param d duration of the animation in ms
     *@return the final camera location, null if the camera is not associated with this view.
     *@see #getGlobalView(Camera c)
     *@see #getGlobalView(Camera c, int d, float mFactor)
     *@see #getGlobalView(Camera c, float mFactor)
     */
    public Location getGlobalView(Camera c, int d){
        return getGlobalView(c, d, 1.0f);
    }
    
    /** Translates and (un)zooms a camera in order to see everything visible in the associated virtual space.
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
     *@param c Camera to be moved (will actually be moved)
     *@param d duration of the animation in ms
     *@param mFactor magnification factor - 1.0 (default) means that the glyphs will occupy the whole screen. mFactor &gt; 1 will zoom out from this default location. mFactor &lt; 1 will do the opposite
     *@return the final camera location, null if the camera is not associated with this view.
     *@see #getGlobalView(Camera c)
     *@see #getGlobalView(Camera c, int d)
     *@see #getGlobalView(Camera c, float mFactor)
     */
	public Location getGlobalView(Camera c, int d, float mFactor){
		Location l = this.getGlobalView(c, mFactor);
		if (l != null){
		    Animation trans = 
			VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraTranslation(d,c,
										       new Point2D.Double(l.vx,l.vy),
										       false,
										       SlowInSlowOutInterpolator.getInstance(),
										       null);
		    
		    Animation alt = 
			VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraAltAnim(d,c,
										   l.alt,
										   false,
										   SlowInSlowOutInterpolator.getInstance(),
										   null);
		    
		    VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);
		    VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(alt, false);
		}
		return l;
	}
	
	/** Get the location from which a camera will see everything visible in the associated virtual space.
	 * The camera must be used in this view. Otherwise, the method returns null and does nothing.
	 *@param c camera considered (will not be moved)
	 *@return the location to which the camera should go, null if the camera is not associated with this view.
	 *@see #getGlobalView(Camera c, int d)
	 *@see #getGlobalView(Camera c, int d, float mFactor)
	 *@see #getGlobalView(Camera c, float mFactor)
	 */
	public Location getGlobalView(Camera c){
		return getGlobalView(c, 1.0f);
	}

    /** Translates and (un)zooms a camera in order to focus on glyph g
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
        *@param g Glyph of interest
        *@param c Camera to be moved
        *@param d duration of the animation in ms
        *@param z if false, do not (un)zoom, just translate (default is true)
        *@param mFactor magnification factor: 1.0 (default) means that the glyph will occupy the whole screen. mFactor < 1 will make the glyph smaller (zoom out). mFactor > 1 will make the glyph appear bigger (zoom in)
        *@param endAction end action to execute after camera reaches its final position
        *@return the final camera location, null if the camera is not associated with this view.
        */
    public Location centerOnGlyph(Glyph g, Camera c, int d, boolean z, float mFactor, EndAction endAction){
        if (c.getOwningView() != this){return null;}
        double dx;
        double dy;
        if (g instanceof VText){
            VText t=(VText)g;
            Point2D.Double p = t.getBounds(c.getIndex());
            if (t.getTextAnchor()==VText.TEXT_ANCHOR_START){
                dx=g.vx+p.x/2-c.vx;
                dy=g.vy+p.y/2-c.vy;
            }
            else if (t.getTextAnchor()==VText.TEXT_ANCHOR_MIDDLE){
                dx=g.vx-c.vx;
                dy=g.vy-c.vy;
            }
            else {
                dx=g.vx-p.x/2-c.vx;
                dy=g.vy-p.y/2-c.vy;
            }
        }
        else {
            dx=g.vx-c.vx;
            dy=g.vy-c.vy;
        }
        //relative translation
        Animation trans = 
            VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
            createCameraTranslation(d, c,
            new Point2D.Double(dx,dy),
            true,
            SlowInSlowOutInterpolator.getInstance(),
            endAction);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);

        double currentAlt=c.getAltitude()+c.getFocal();
        if (z){
            double[] regBounds = this.getVisibleRegion(c);
            // region that will be visible after translation, but before zoom/unzoom  (need to compute zoom) ;
            // we only take left and down because ratios are equals for left and right, up and down
            double[] trRegBounds = {regBounds[0]+dx, regBounds[3]+dy};
            double ratio = 0;
            //compute the mult factor for altitude to see glyph g entirely
            if (trRegBounds[0]!=0){
                if (g instanceof VText){
                    ratio = (((VText)g).getBounds(c.getIndex()).x) / (g.vx-trRegBounds[0]);
                }
                else if (g instanceof RectangularShape){
                    ratio = (((RectangularShape)g).getWidth()/2d) / (g.vx-trRegBounds[0]);
                }
                else {
                    ratio = g.getSize() / 2d / (g.vx-trRegBounds[0]);
                }
            }
            //same for Y ; take the max of both
            if (trRegBounds[1]!=0){
                double tmpRatio;
                if (g instanceof VText){
                    tmpRatio = (((VText)g).getBounds(c.getIndex()).y) / (g.vy-trRegBounds[1]);
                }
                else if (g instanceof RectangularShape){
                    tmpRatio = (((RectangularShape)g).getHeight()/2d) / (g.vy-trRegBounds[1]);
                }
                else {
                    tmpRatio = g.getSize() / 2d / (g.vy-trRegBounds[1]);
                }
                if (tmpRatio>ratio){ratio=tmpRatio;}
            }
            ratio *= mFactor;
            double newAlt=currentAlt*Math.abs(ratio);

            Animation altAnim = 
                VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
                createCameraAltAnim(d, c, 
                newAlt, false,
                SlowInSlowOutInterpolator.getInstance(),
                null);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(altAnim, false);

            return new Location(g.vx,g.vy,newAlt);
        }
        else {
            return new Location(g.vx,g.vy,currentAlt);
        }
    }
 
    /** Translates and (un)zooms a camera in order to focus on glyph g
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
     *@param g Glyph of interest
     *@param c Camera to be moved
     *@param d duration of the animation in ms
     *@return the final camera location, null if the camera is not associated with this view.
     */
    public Location centerOnGlyph(Glyph g,Camera c,int d){
	    return this.centerOnGlyph(g,c,d,true);
    }

    /** Translates and (un)zooms a camera in order to focus on glyph g
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
     *@param g Glyph of interest
     *@param c Camera to be moved
     *@param d duration of the animation in ms
     *@param z if false, do not (un)zoom, just translate (default is true)
     *@return the final camera location, null if the camera is not associated with this view.
     */
    public Location centerOnGlyph(Glyph g, Camera c, int d, boolean z){
	    return this.centerOnGlyph(g, c, d, z, 1.0f);
    }

    /** Translates and (un)zooms a camera in order to focus on glyph g
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
     *@param g Glyph of interest
     *@param c Camera to be moved
     *@param d duration of the animation in ms
     *@param z if false, do not (un)zoom, just translate (default is true)
     *@param mFactor magnification factor - 1.0 (default) means that the glyph will occupy the whole screen. mFactor < 1 will make the glyph smaller (zoom out). mFactor > 1 will make the glyph appear bigger (zoom in)
     *@return the final camera location, null if the camera is not associated with this view.
     */
    public Location centerOnGlyph(Glyph g, Camera c, int d, boolean z, float mFactor){
	    return this.centerOnGlyph(g, c, d, z, mFactor, null);
    }
    
    

    /** Translates and (un)zooms a camera in order to focus on a specific rectangular region
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
		*@param c Camera to be moved
		*@param d duration of the animation in ms (pass 0 to go there instantanesouly)
		*@param x1 x coord of first point
		*@param y1 y coord of first point
		*@param x2 x coord of opposite point
		*@param y2 y coord of opposite point
		*@return the final camera location, null if the camera is not associated with this view.
		*/
	public Location centerOnRegion(Camera c, int d, double x1, double y1, double x2, double y2){
	    return centerOnRegion(c, d, x1, y1, x2, y2, null);
    }
    
	/** Translates and (un)zooms a camera in order to focus on a specific rectangular region
     * The camera must be used in this view. Otherwise, the method returns null and does nothing.
		*@param c Camera to be moved
		*@param d duration of the animation in ms (pass 0 to go there instantanesouly)
		*@param x1 x coord of first point
		*@param y1 y coord of first point
		*@param x2 x coord of opposite point
		*@param y2 y coord of opposite point
		*@param ea action to be performed at end of animation
		*@return the final camera location, null if the camera is not associated with this view.
		*/
	public Location centerOnRegion(Camera c, int d, double x1, double y1, double x2, double y2, EndAction ea){
        if (c.getOwningView() != this){return null;}
        double minX = Math.min(x1,x2);
        double minY = Math.min(y1,y2);
        double maxX = Math.max(x1,x2);
        double maxY = Math.max(y1,y2);
        //wnes=west north east south
        double[] wnes = {minX, maxY, maxX, minY};
        //new coords where camera should go
        double dx = (wnes[2]+wnes[0]) / 2d; 
        double dy = (wnes[1]+wnes[3]) / 2d;
        // new alt to fit horizontally
		Dimension panelSize = this.getPanel().getSize();
        double nah = (wnes[2]-dx) * 2 * c.getFocal() / panelSize.width - c.getFocal();
        // new alt to fit vertically
        double nav = (wnes[1]-dy) * 2 * c.getFocal() / panelSize.height - c.getFocal();
        // take max of both
        double na = Math.max(nah, nav);
        if (d > 0){
            Animation trans =
                VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
                createCameraTranslation(d, c, new Point2D.Double(dx, dy), false,
                SlowInSlowOutInterpolator.getInstance(),
                ea);
            Animation altAnim = 
                VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
                createCameraAltAnim(d, c, na, false,
                SlowInSlowOutInterpolator.getInstance(),
                null);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(altAnim, false);			        
        }
        else {
            c.setAltitude(na);
            c.moveTo(dx, dy);
        }
        return new Location(dx, dy, na);
    }

    /** Get glyphs whose hotspot is in region delimited by rectangle (x1,y1,x2,y2) in VirtualSpace vs.
     * Coordinates of the mouse cursor in virtual space are available in instance variables vx and vy of class VCursor.
     * The selection rectangle can be drawn on screen by using ViewPanel.setDrawRect(true) (e.g. call when mouse button is pressed)/ViewPanel.setDrawRect(false) (e.g. call when mouse button is released).
     *@return null if empty. 
     *@param x1 x coord of first point
     *@param y1 y coord of first point
     *@param x2 x coord of opposite point
     *@param y2 y coord of opposite point
     *@param vsn name of virtual space
     *@param wg which glyphs in the region should be returned (among VIS_AND_SENS_GLYPHS (default), VISIBLE_GLYPHS, SENSIBLE_GLYPHS, ALL_GLYPHS)
     */
    public Vector<Glyph> getGlyphsInRegion(double x1,double y1,double x2,double y2,String vsn,int wg){
        Vector<Glyph> res = new Vector<Glyph>();
        VirtualSpace vs = VirtualSpaceManager.INSTANCE.getVirtualSpace(vsn);
        double minX = Math.min(x1,x2);
        double minY = Math.min(y1,y2);
        double maxX = Math.max(x1,x2);
        double maxY = Math.max(y1,y2);
        if (vs!=null){
            Vector<Glyph> allG = vs.getAllGlyphs();
            Glyph g;
            for (int i=0;i<allG.size();i++){
                g=(Glyph)allG.elementAt(i);
                if ((g.vx>=minX) && (g.vy>=minY) && (g.vx<=maxX) && (g.vy<=maxY)){
                    if ((wg==VirtualSpaceManager.VIS_AND_SENS_GLYPHS) && g.isSensitive() && g.isVisible()){res.add(g);}
                    else if ((wg==VirtualSpaceManager.VISIBLE_GLYPHS) && g.isVisible()){res.add(g);}
                    else if ((wg==VirtualSpaceManager.SENSITIVE_GLYPHS) && g.isSensitive()){res.add(g);}
                    else if (wg==VirtualSpaceManager.ALL_GLYPHS){res.add(g);}
                }
            }
        }
        if (res.isEmpty()){res=null;}
        return res;
    }
   
}

