/*   FILE: ZCircle.java
 *   DATE OF CREATION:  Wed Jan 24 10:45:06 2007
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
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
 * $Id:  $
 */

package com.xerox.VTM.glyphs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.Camera;
import net.claribole.zvtm.lens.Lens;

/**
 * Circle. Same as VCircle but with a parameterable minimum apparent size.
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VCircle
 **/

public class ZCircle extends VCircle {

    public ZCircle(){
	super();
    }

    /**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z altitude
     *@param r radius in virtual space
     *@param c fill color
     */
    public ZCircle(long x,long y,float z,long r,Color c){
	super(x, y, z, r, c);
    }

    int minProjSize = 1;

    /** Set the minimum projected/apparent size of this circle.
     * If the actual projected size if less than this value, the glyph will be drawn with this value.
     *@see #getMimimumProjectedSize()
     */
    public void setMinimumProjectedSize(int s){
	minProjSize = s;
    }

    /** Get the minimum projected/apparent size of this circle.
     *@see #setMinimumProjectedSize(int s)
     */
    public int getMimimumProjectedSize(){
	return minProjSize;
    }

    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (pc[i].cr >= minProjSize){
	    if (filled){
		g.setColor(this.color);
		g.fillOval(dx+pc[i].cx-pc[i].cr,dy+pc[i].cy-pc[i].cr,2*pc[i].cr,2*pc[i].cr);
	    }
	    if (paintBorder){
		g.setColor(borderColor);
		if (stroke!=null) {
		    g.setStroke(stroke);
		    g.drawOval(dx+pc[i].cx-pc[i].cr,dy+pc[i].cy-pc[i].cr,2*pc[i].cr,2*pc[i].cr);
		    g.setStroke(stdS);
		}
		else {
		    g.drawOval(dx+pc[i].cx-pc[i].cr,dy+pc[i].cy-pc[i].cr,2*pc[i].cr,2*pc[i].cr);
		}
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].cx, dy+pc[i].cy, minProjSize, minProjSize);
	}
    }

    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
	if (pc[i].lcr >= minProjSize){
	    if (filled){
		g.setColor(this.color);
		g.fillOval(dx+pc[i].lcx-pc[i].lcr,dy+pc[i].lcy-pc[i].lcr,2*pc[i].lcr,2*pc[i].lcr);
	    }
	    if (paintBorder){
		g.setColor(borderColor);
		if (stroke!=null) {
		    g.setStroke(stroke);
		    g.drawOval(dx+pc[i].lcx-pc[i].lcr,dy+pc[i].lcy-pc[i].lcr,2*pc[i].lcr,2*pc[i].lcr);
		    g.setStroke(stdS);
		}
		else {
		    g.drawOval(dx+pc[i].lcx-pc[i].lcr,dy+pc[i].lcy-pc[i].lcr,2*pc[i].lcr,2*pc[i].lcr);
		}
	    }
	}
	else {
	    g.setColor(this.color);
	    g.fillRect(dx+pc[i].lcx, dy+pc[i].lcy, minProjSize, minProjSize);
	}
    }

    public Object clone(){
	ZCircle res = new ZCircle(vx,vy,0,vr,color);
	res.borderColor = this.borderColor;
	res.mouseInsideColor = this.mouseInsideColor;
	res.bColor = this.bColor;
	res.setMinimumProjectedSize(minProjSize);
	return res;
    }

}
