/*   FILE: ESCamera.java
 *   DATE OF CREATION:  Sat Jan 29 09:38:04 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package com.xerox.VTM.engine;

import net.claribole.zvtm.engine.MotionListener;

  /**
   * a camera to which a motion listener can be attached
   * @author Emmanuel Pietriga
   **/

public class ESCamera extends Camera {

    MotionListener ml = null;

    /** 
     * @param x initial X coordinate
     * @param y initial Y coordinate
     * @param alt initial altitude
     * @param f initial focal distance
     * @param i camera index (wrt the owning virtual space)
     */
    ESCamera(long x,long y,float alt,float f,int i){ 
	super(x, y, alt, f, i);
    }

    /** 
     * @param x initial X coordinate
     * @param y initial Y coordinate
     * @param alt initial altitude
     * @param f initial focal distance
     * @param i camera index (wrt the owning virtual space)
     * @param l lazy camera, will only repaint when explicitely told to do so (default is false) 
     */
    ESCamera(long x,long y,float alt,float f,int i, boolean l){
	super(x, y, alt, f, i, l);
    }

    /**associate a motion listener with this camera (null to unset)*/
    public void setMotionListener(MotionListener ml){
	this.ml = ml;
    }

    /**get the motion listener associated with this camera*/
    public MotionListener getMotionListener(){
	return this.ml;
    }

    /**
     * set camera position (absolute value) - will trigger a repaint, whereas directly assigning values to posx,posy will not
     *@deprecated As of zvtm 0.9.2, replaced by moveTo
     *@see #moveTo(long x,long y)
     */
    public void setLocation(long x,long y){
	super.setLocation(x, y);
	if (ml != null){ml.translation(this);}
    }

    /**relative translation (offset) - will trigger a repaint, whereas directly assigning values to posx, posy will not*/
    public void move(long x,long y){
	super.move(x, y);
	if (ml != null){ml.translation(this);}
    }

    /**absolute translation - will trigger a repaint, whereas directly assigning values to posx, posy will not*/
    public void moveTo(long x,long y){
	super.moveTo(x, y);
	if (ml != null){ml.translation(this);}
    }

    /**
     * set camera altitude (absolute value)
     */
    public void setAltitude(float a){
	super.setAltitude(a);
	if (ml != null){ml.zoom(this);}
    }

    /**
     * set camera altitude (relative value)
     */
    public void altitudeOffset(float a){
	super.altitudeOffset(a);
	if (ml != null){ml.zoom(this);}
    }

    /**
     * set camera focal distance (absolute value)
     */
    public void setFocal(float f){
	super.setFocal(f);
	if (ml != null){ml.zoom(this);}
    }

    /**
     * returns a String with ID, position, altitude and focal distance
     */
    public String toString() {
	return new String("ESCamera "+ID+" position ("+posx+","+posy+") alt "+altitude+" focal "+focal+" motion listener "+ml);
    }

}
