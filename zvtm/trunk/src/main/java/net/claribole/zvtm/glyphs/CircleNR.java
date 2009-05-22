/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
 
package net.claribole.zvtm.glyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.glyphs.VCircle;

public class CircleNR extends VCircle {

	public CircleNR(){
		super();
	}

	/**
		*@param x coordinate in virtual space
		*@param y coordinate in virtual space
		*@param z z-index (pass 0 if you do not use z-ordering)
		*@param r radius in virtual space
		*@param c fill color
		*/
	public CircleNR(long x,long y, int z,long r,Color c){
		super(x, y, z, r, c);
	}

	/**
		*@param x coordinate in virtual space
		*@param y coordinate in virtual space
		*@param z z-index (pass 0 if you do not use z-ordering)
		*@param r radius in virtual space
		*@param c fill color
		*@param bc border color
		*/
	public CircleNR(long x, long y, int z, long r, Color c, Color bc){
		super(x, y, z, r, c, bc);
	}

	/**
		*@param x coordinate in virtual space
		*@param y coordinate in virtual space
		*@param z z-index (pass 0 if you do not use z-ordering)
		*@param r radius in virtual space
		*@param c fill color
		*@param bc border color
		*@param alpha alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
		*/
	public CircleNR(long x, long y, int z, long r, Color c, Color bc, float alpha){
		super(x, y, z, r, c, bc, alpha);
	}
	
	public void project(Camera c, Dimension d){
		int i=c.getIndex();
		coef=(float)(c.focal/(c.focal+c.altitude));
		//find coordinates of object's geom center wrt to camera center and project
		//translate in JPanel coords
		pc[i].cx=(d.width/2)+Math.round((vx-c.posx)*coef);
		pc[i].cy=(d.height/2)-Math.round((vy-c.posy)*coef);
		//project height and construct polygon
		pc[i].cr=Math.round(vr);
	}

	public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
		int i = c.getIndex();
		coef = ((float)(c.focal/(c.focal+c.altitude))) * lensMag;
		//find coordinates of object's geom center wrt to camera center and project
		//translate in JPanel coords
		pc[i].lcx = (lensWidth/2) + Math.round((vx-(lensx))*coef);
		pc[i].lcy = (lensHeight/2) - Math.round((vy-(lensy))*coef);
		//project height and construct polygon
		pc[i].lcr = Math.round(vr);
	}

	public Object clone(){
		CircleNR res=new CircleNR(vx,vy,0,vr,color);
		res.borderColor=this.borderColor;
		res.mouseInsideColor=this.mouseInsideColor;
		res.bColor=this.bColor;
		return res;
	}
}
