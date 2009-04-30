/*   FILE: Camera.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004. All Rights Reserved
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.claribole.zvtm.engine.CameraListener;
import net.claribole.zvtm.engine.Location;

import com.xerox.VTM.glyphs.Glyph;

  /**
   * a Camera is used to observe the virtual space which owns it - all cameras have unique IDs, as glyph - (x,y) coordinates, observation altitude and focal distance can be changed
   * @author Emmanuel Pietriga
   **/

public class Camera {

    public static final float DEFAULT_FOCAL = 100;

    /** camera ID */
    Integer ID;
    /** camera index (wrt the owning virtual space)*/
    int index; 
    /** Coordinates in virtual space.
	Directly assigning vlaues to posx,posy will work but will not propagate
	the translation to glyphs that may be sticked to the camera).
	IMPORTANT: do not forget to call updatePrecisePosition() after assigning values to posx and posy*/
    public long posx,posy;
    double dposx,dposy;
    /** altitude of observation*/
    public float altitude;
    /** focal distance*/
    public float focal;
    /**camera is enabled or not (disabling does not destroy)*/
    boolean enabled;
    /** virtual space to which this camera belongs to*/
    VirtualSpace parentSpace;
    /**View using this camera as one of its layer(s)*/
    View view;
    /**glyphs sticked to this one*/
    List<Glyph> stickedGlyphs = new ArrayList<Glyph>();
    /**glyphs sticked to this one*/
    Camera[] stickedCameras;
    /**tells whether camera altitude changes should be propagated to sticked cameras or not (in addition to x,y changes)*/
    boolean[] stickAltitude;

    /**Lazy camera, true if the camera only repaints when explicitely asked*/
    boolean eager=true;

    /**when in lazy mode, tells the camera to repaint next time the owning view goes through its paint loop*/
    boolean shouldRepaint=false;

    //"listeners" is traversed a lot more often than it is mutated.
    private final List<CameraListener> listeners = new CopyOnWriteArrayList<CameraListener>();
    
    /** 
     * @param x initial X coordinate
     * @param y initial Y coordinate
     * @param alt initial altitude
     * @param f initial focal distance
     * @param i camera index (wrt the owning virtual space)
     */
    Camera(long x,long y,float alt,float f,int i){
	this(x, y, alt, f, i, false);
    }

    /** 
     * @param x initial X coordinate
     * @param y initial Y coordinate
     * @param alt initial altitude
     * @param f initial focal distance
     * @param i camera index (wrt the owning virtual space)
     * @param l lazy camera, will only repaint when explicitely told to do so (default is false) 
     */
    Camera(long x,long y,float alt,float f,int i, boolean l){
	posx=x;
	posy=y;
	updatePrecisePosition();
	altitude=alt;
	focal=f;
	index=i;
	enabled=true;
	eager=!l;
    }

    /**This method must imperatively be called (if and) when assigning values to posx and posy manually*/
    public void updatePrecisePosition(){
	dposx = posx;
	dposy = posy;
    }

    /**
     * set camera position (absolute value) - will trigger a repaint, whereas directly assigning values to posx,posy will not
     *@deprecated As of zvtm 0.9.2, replaced by moveTo
     *@see #moveTo(long x,long y)
     */
    public void setLocation(long x,long y){
	propagateMove(x-posx, y-posy);  //take care of sticked glyphs
	posx = x;
	posy = y;
	updatePrecisePosition();
	if (view != null){
	    parentSpace.vsm.repaintNow(view);
	}
	notifyMoved();
    }

    /**relative translation (offset) - will trigger a repaint, whereas directly assigning values to posx, posy will not*/
    public void move(long x,long y){
	posx += x;
	posy += y;
	updatePrecisePosition();
	propagateMove(x, y);  //take care of sticked glyphs
	if (view != null){
	    parentSpace.vsm.repaintNow(view);
	}
	notifyMoved();
    }

    /**relative translation (offset) - will trigger a repaint, whereas directly assigning values to posx, posy will not*/
    public void move(double x,double y){
        dposx += x;
        dposy += y;
        posx = Math.round(dposx);
        posy = Math.round(dposy);
        propagateMove(posx, posy);  //take care of sticked glyphs
        if (view != null){
            parentSpace.vsm.repaintNow(view);
        }
	notifyMoved();
    }
    
    /**absolute translation - will trigger a repaint, whereas directly assigning values to posx, posy will not*/
    public void moveTo(long x,long y){
	posx = x;
	posy = y;
	updatePrecisePosition();
	propagateMove(x-posx, y-posy);  //take care of sticked glyphs
	if (view != null){
	    parentSpace.vsm.repaintNow(view);
	}
	notifyMoved();
    }

    /**
     * Set camera altitude (absolute value).
     * Do not automatically refresh associated view.
     *@param a new altitude value
     */
    public void setAltitude(float a){
	setAltitude(a, false);
    }

    /**
     * Set camera altitude (relative value).
     * Do not automatically refresh associated view.
     *@param a offset value
     */
    public void altitudeOffset(float a){
	altitudeOffset(a, false);
    }


    /**
     * Set camera altitude (absolute value).
     *@param a new altitude value
     *@param repaint refresh the associated view or not
     */
    public void setAltitude(float a, boolean repaint){
	float oldAlt = altitude;
	if (a>=parentSpace.vsm.zoomFloor){altitude=a;}  //test prevents incorrect altitudes
	else {altitude=parentSpace.vsm.zoomFloor;}
	propagateAltitudeChange(altitude - oldAlt);
	if (repaint && view != null){
	    parentSpace.vsm.repaintNow(view);
	}
	notifyMoved();
    }

    /**
     * Set camera altitude (relative value).
     *@param a offset value
     *@param repaint refresh the associated view or not
     */
    public void altitudeOffset(float a, boolean repaint){
	float oldAlt = altitude;
	if ((altitude+a)>parentSpace.vsm.zoomFloor){altitude+=a;}   //test prevents incorrect altitudes
	else {altitude=parentSpace.vsm.zoomFloor;}
	propagateAltitudeChange(altitude - oldAlt);
	if (repaint && view != null){
	    parentSpace.vsm.repaintNow(view);
	}
	notifyMoved();
    }

    /**
     * get camera altitude
     */
    public float getAltitude(){
	return altitude;
    }


    /**
     * Set camera location
     */
    public void setLocation(Location l){
        propagateMove(l.vx-posx, l.vy-posy);
        propagateAltitudeChange(l.alt - altitude);
        posx = l.vx;
        posy = l.vy;
        altitude = l.alt;
        updatePrecisePosition();
        if (view != null){
            parentSpace.vsm.repaintNow(view);
        }
	notifyMoved();
    }

    /**
     * get camera location
     */
    public Location getLocation(){
	return new Location(posx,posy,altitude);
    }
    
    /**
     * Registers a CameraListener for this Camera
     */
    public void addListener(CameraListener listener){
	listeners.add(listener);
    }

    /**
     * Un-registers a CameraListener for this Camera
     */
    public void removeListener(CameraListener listener){
	listeners.remove(listener);
    }

    /**
     * Sends a notification to camera listeners currently
     * observing this Camera instance of a camera movement
     */
    protected void notifyMoved(){
	for(CameraListener listener: listeners){
	    listener.cameraMoved(this, new LongPoint(posx, posy), altitude);
	}
    }

    /**
     * set camera focal distance (absolute value)
     */
    public void setFocal(float f){
	if (f<0) {f=0;}
	focal=f;
    }

    /**
     * get camera focal distance
     */
    public float getFocal(){
	return focal;
    }

    /** Propagate this camera's movement to all glyphs and cameras attached to it. */
    public void propagateMove(double x, double y){
        long lx = Math.round(x);
        long ly = Math.round(y);
		for(Glyph sticked: stickedGlyphs){
			sticked.move(lx,ly);
		}
        if (stickedCameras != null){
            for (int i=0;i<stickedCameras.length;i++){
                stickedCameras[i].moveTo(posx, posy);
            }
        }
    }

    /**Propagate this camera's altitude change to all cameras attached to it.*/
    public void propagateAltitudeChange(float alt){
        if (stickedCameras != null && alt != 0){
            for (int i=0;i<stickedCameras.length;i++){
                if (stickAltitude[i]){
                    stickedCameras[i].setAltitude(altitude);
                }
            }
        }
    }

    /**
     * get camera index (w.r.t owning virtual space)
     */
    public int getIndex(){
	return index;
    }

    /**
     * get camera ID
     */
    public Integer getID(){
	return ID;
    }

    /**
     * set new ID for this camera (make sure there is no conflict)
     */
    public void setID(Integer ident){
	ID=ident;
    }

    /**
     * set virtual space owning this camera
     */
    protected void setOwningSpace(VirtualSpace vs){
	parentSpace=vs;
    }

    /**
     * get virtual space owning this camera
     */
    public VirtualSpace getOwningSpace(){
	return parentSpace;
    }

    /**
     * set view owning this camera.
     * CALLED INTERNALLY - NOT FOR PUBLIC USE
     */
    public void setOwningView(View vi){
	view=vi;
    }

    /**
     * get view owning this camera
     */
    public View getOwningView(){
	return view;
    }

    /**
     *attach glyph to this camera
     *@param g glyph to be attached to this camera
     *@see #unstick(Glyph g)
     *@see #unstickAllGlyphs()
     */
    public void stick(Glyph g){
		if(stickedGlyphs.contains(g)){
			if (parentSpace.vsm.debugModeON()){
				System.err.println("Warning: trying to stick Glyph "+g+" to Camera "+this+" while they are already sticked.");
			}
			return;
		}
		stickedGlyphs.add(g);
		g.stickedTo = this;
    }

    /**
     *detach glyph from this camera
     *@param g glyph to be detached
     *@see #stick(Glyph g)
     *@see #unstickAllGlyphs()
     */
    public void unstick(Glyph g){
		if(stickedGlyphs.contains(g)){
			stickedGlyphs.remove(g);
			g.stickedTo = null;
		}
	}

   /**
    *detach all glyphs attached to this camera
    *@see #unstick(Glyph g)
    *@see #stick(Glyph g)
    */
    public void unstickAllGlyphs(){
		for(Glyph sticked: stickedGlyphs){
			sticked.stickedTo = null;
		}
		stickedGlyphs.clear();
	}

    /**return the list of glyphs sticked to this camera (null if none)*/
    public Glyph[] getStickedGlyphArray(){
		return stickedGlyphs.toArray(new Glyph[0]);
    }


    /**
     * Attach a camera to this camera. Both cameras will have the same location.
     *@param c camera to be attached to this camera
     *@see #unstick(Camera c)
     *@see #unstickAllCameras()
     */
    public void stick(Camera c){
	stick(c, true);
    }

    /**
     * Attach a camera to this camera. Both cameras will have the same location.
     *@param c camera to be attached to this camera.
     *@param stickAlt also propagate altitude changes, in addition to translations.
     *@see #unstick(Camera c)
     *@see #unstickAllCameras()
     */
    public void stick(Camera c, boolean stickAlt){
	if (stickedCameras == null){
	    stickedCameras = new Camera[1];
	    stickedCameras[0] = c;
	    stickAltitude = new boolean[1];
	    stickAltitude[0] = stickAlt;
	}
	else {
	    boolean alreadySticked = false;
	    for (int i=0;i<stickedCameras.length;i++){
		if (stickedCameras[i] == c){
		    alreadySticked = true;
		    break;
		}
	    }
	    if (!alreadySticked){
		Camera[] newStickList = new Camera[stickedCameras.length + 1];
		System.arraycopy(stickedCameras, 0, newStickList, 0, stickedCameras.length);
		newStickList[stickedCameras.length] = c;
		stickedCameras = newStickList;
		boolean[] newStickAltList = new boolean[stickAltitude.length + 1];
		System.arraycopy(stickAltitude, 0, newStickAltList, 0, stickAltitude.length);
		newStickAltList[stickAltitude.length] = stickAlt;
		stickAltitude = newStickAltList;
	    }
	    else {
		if (parentSpace.vsm.debugModeON()){System.err.println("Warning: trying to stick Camera "+c+" to Camera "+this+" while they are already sticked.");}
	    }
	}
    }

    /**
     *detach camera from this camera
     *@param c camera to be detached
     *@see #stick(Camera c)
     *@see #unstickAllCameras()
     */
    public void unstick(Camera c){
	if (stickedCameras != null){
	    for (int i=0;i<stickedCameras.length;i++){
		if (stickedCameras[i] == c){
		    Camera[] newStickList = new Camera[stickedCameras.length - 1];
		    System.arraycopy(stickedCameras, 0, newStickList, 0, i);
		    System.arraycopy(stickedCameras, i+1, newStickList, i, stickedCameras.length-i-1);
		    stickedCameras = newStickList;
		    boolean[] newStickAltList = new boolean[stickAltitude.length - 1];
		    System.arraycopy(stickAltitude, 0, newStickAltList, 0, i);
		    System.arraycopy(stickAltitude, i+1, newStickAltList, i, stickAltitude.length-i-1);
		    stickAltitude = newStickAltList;
		    break;
		}
	    }
	    if (stickedCameras.length == 0){
		stickedCameras = null;
		stickAltitude = null;
	    }
	}
    }

   /**
    *detach all cameras attached to this camera
    *@see #unstick(Camera c)
    *@see #stick(Camera c)
    */
    public void unstickAllCameras(){
	if (stickedCameras != null){
	    for (int i=0;i<stickedCameras.length;i++){
		stickedCameras[i] = null;
	    }
	    stickedCameras = null;
	    stickAltitude = null;
	}
    }

    /**return the list of cameras sticked to this camera (null if none)*/
    public Camera[] getStickedCameraArray(){
	return stickedCameras;
    }

    /**
     * Enable camera. What is seen through the camera will be painted in the View.
     *@deprecated As of zvtm 0.9.6, replaced by setEnabled.
     *@see #setEnabled(boolean b)
     */
    public void enable(){
        setEnabled(true);
    }

    /**
     * Disable camera. What is seen through the camera will not be painted in the View.
      *@deprecated As of zvtm 0.9.6, replaced by setEnabled.
      *@see #setEnabled(boolean b)
      */
    public void disable(){
        setEnabled(false);
    }
    
    /**
     * Enable or disable camera. What is seen through the camera will (or will not) be painted in the View.
     *@see #isEnabled()
     */
    public void setEnabled(boolean b){
        if (b != enabled){
            enabled = b;
            parentSpace.vsm.repaintNow(view);
        }
    }

    /**
     * Tells whether the camera is enabled or disabled.
     *@see #setEnabled(boolean b)
     */
    public boolean isEnabled(){
        return enabled;
    }
    
    /**
     * set eager or lazy mode
     * @param b true=lazy, false=eager
     */
    public void setLaziness(boolean b){eager=!b;}

    /**
     * get camera repaint mode (eager or lazy)
     * @return true=lazy, false=eager
     */
    public boolean getLaziness(){return !eager;}

    /**
     * the content seen through this camera will be repainted in the next owning view's paint loop
     */
    public void repaintNow(){
	shouldRepaint=true;
    }

    protected boolean shouldRepaint(){
	if (shouldRepaint){
	    shouldRepaint=false;
	    return true;
	}
	else {return false;}
    }

    /**
     * returns a String with ID, position, altitude and focal distance
     */
    public String toString() {
	return new String("Camera "+ID+" position ("+posx+","+posy+") alt "+altitude+" focal "+focal);
    }

}
