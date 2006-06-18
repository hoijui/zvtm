/*   FILE: Portal.java
 *   DATE OF CREATION:  Sat Jun 17 07:19:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.engine;

import java.awt.Graphics2D;
import java.awt.Dimension;

import com.xerox.VTM.engine.View;

public abstract class Portal {
    
    /** portal ID */
    Integer ID;

    /** top-left horizontal coordinate of portal, in parent's JPanel coordinates */
    public int x;
    /** top-left vertical coordinate of portal, in parent's JPanel coordinates */
    public int y;
    /** Portal dimensions width*/
    public int w;
    /** Portal dimensions width*/
    public int h;
    Dimension size = new Dimension(0,0);
    /** View embedding this portal */
    View owningView;
    /** handles events occuring inside the portal */
    public PortalEventHandler pevH;
    /**remembers wether cursor was inside portal or not last time it moved*/
    public boolean cursorInside = false;

    /**move the portal by dx and dy inside the view (JPanel coordinates)*/
    public void move(int dx, int dy){
	x += dx;
	y += dy;
    }

    /**move the portal by dx and dy inside the view (JPanel coordinates)*/
    public void moveTo(int x, int y){
	this.x = x;
	this.y = y;
    }    
    
    /**set the portal's size (offset)*/
    public void resize(int dw, int dh){
	w += dw;
	h += dh;
	updateDimensions();
    }

    /**set the portal's size (absolute value)*/
    public void sizeTo(int w, int h){
	this.w = w;
	this.h = h;
	updateDimensions();
    }

    public void updateDimensions(){
	size.setSize(w, h);
    }
    
    /**CALLED INTERNALLY - NOT FOR PUBLIC USE*/
    public void setOwningView(View v){
	this.owningView = v;
    }
    
    /**Get the view embedding this portal*/
    public View getOwningView(){
	return owningView;
    }

    /**Set an event handler for mouse and keyboard events occuring inside the portal*/
    public void setPortalEventHandler(PortalEventHandler peh){
	this.pevH = peh;
    }

    /**Get the event handler for mouse and keyboard events occuring inside the portal (null if none)*/
    public PortalEventHandler getPortalEventHandler(){
	return this.pevH;
    }

    /**
     * get portal ID
     */
    public Integer getID(){
	return ID;
    }

    /**
     * set new ID for this portal
     */
    public void setID(Integer ident){
	ID = ident;
    }

    /**detects whether the given point is inside this portal or not 
     *@param cx horizontal cursor coordinate (JPanel)
     *@param cy vertical cursor coordinate (JPanel)
     */
    public abstract boolean coordInside(int cx, int cy);


    /**Detects cursor entry/exit in/from the portal.
     *@param cx horizontal cursor coordinate (JPanel)
     *@param cy vertical cursor coordinate (JPanel)
     *@return 1 if cursor has entered the portal, -1 if it has exited the portal, 0 if nothing has changed (meaning it was already inside or outside it)
     */
    public int cursorInOut(int cx,int cy){
	if (coordInside(cx, cy)){// if the cursor is inside the portal
	    if (!cursorInside){// if it was not inside it last time, cursor has entered the portal
		cursorInside = true;
		if (pevH != null){pevH.enterPortal(this);}
		return 1;
	    }
	    else {return 0;}   // if it was inside last time, nothing has changed
	}
	else{// if the cursor is not inside the portal
	    if (cursorInside){// if it was inside it last time, cursor has exited the portal
		cursorInside = false;
		if (pevH != null){pevH.exitPortal(this);}
		return -1;
	    }
	    else {return 0;}  // if it was not inside last time, nothing has changed
	}
    }

    public abstract void paint(Graphics2D g2d, int viewWidth, int viewHeight);

}