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
    
    /**top-left horizontal coordinate of portal, in parent's JPanel coordinates*/
    public int x;
    /**top-left vertical coordinate of portal, in parent's JPanel coordinates*/
    public int y;
    /**Portal dimensions*/
    public Dimension size;
    /**View embedding this portal*/
    View owningView;

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
    
    /**set the portal's size*/
    public void setSize(Dimension d){
	this.size = d;
    }
    
    /**CALLED INTERNALLY - NOT FOR PUBLIC USE*/
    public void setOwningView(View v){
	this.owningView = v;
    }
    
    /**Get the view embedding this portal*/
    public View getOwningView(){
	return owningView;
    }

    public abstract void paint(Graphics2D g2d, int viewWidth, int viewHeight);

}
