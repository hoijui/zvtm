/*   FILE: BooleanOps.java
 *   DATE OF CREATION:   Oct 03 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Thu Jan 24 10:29:38 2002 by Emmanuel Pietriga
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
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
 */

package com.xerox.VTM.glyphs;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * class used to store a boolean operation to be run on an appropriate glyph - right now we only support RectangularShape derivatives (Ellipse, Rectangle)
 * @author Emmanuel Pietriga
 */

public class BooleanOps {    

    /** virtual distance to main shape's center (vx,vy) */
    public long ox,oy;
    /** size along x and y axis */
    public long szx,szy;
    /** 1=ellipse 2=rectangle */
    public int shapeType;
    /** 1=union 2=subtraction 3=intersection 4=exclusive OR */
    public int opType;

    /** the actual projected area (used in the boolean operation before drawing, called by project) */
    Area ar;
    Area lar;
    
    /** 
     *@param x virtual horizontal distance to main shape's center in virtual space
     *@param y virtual vertical distance to main shape's center in virtual space
     *@param sx size along X axis
     *@param sy size along Y axis
     *@param t shape type 1=ellipse 2=rectangle
     *@param o boolean operation type 1=union 2=subtraction 3=intersection 4=exclusive OR
     */
    public BooleanOps(long x,long y,long sx,long sy,int t,int o){
	ox=x;
	oy=y;
	szx=sx;
	szy=sy;
	shapeType=t;
	opType=o;
    }

    void project(float coef,long cx,long cy){//cx and cy are projected coordinates of main area
	switch (shapeType) {
	case 1:{//ellipse
	    ar=new Area(new Ellipse2D.Float(cx+(ox-szx/2)*coef,cy-(oy+szy/2)*coef,szx*coef,szy*coef));
	    break;
	}
	case 2:{//rectangle
	    ar=new Area(new Rectangle2D.Float(cx+(ox-szx/2)*coef,cy-(oy+szy/2)*coef,szx*coef,szy*coef));
	    break;
	}
	default:{//ellipse as default
	    ar=new Area(new Ellipse2D.Float(cx+(ox-szx/2)*coef,cy-(oy+szy/2)*coef,szx*coef,szy*coef));
	}
	}
    }

    void projectForLens(float coef,long cx,long cy){//cx and cy are projected coordinates of main area
	switch (shapeType) {
	case 1:{//ellipse
	    lar=new Area(new Ellipse2D.Float(cx+(ox-szx/2)*coef,cy-(oy+szy/2)*coef,szx*coef,szy*coef));
	    break;
	}
	case 2:{//rectangle
	    lar=new Area(new Rectangle2D.Float(cx+(ox-szx/2)*coef,cy-(oy+szy/2)*coef,szx*coef,szy*coef));
	    break;
	}
	default:{//ellipse as default
	    lar=new Area(new Ellipse2D.Float(cx+(ox-szx/2)*coef,cy-(oy+szy/2)*coef,szx*coef,szy*coef));
	}
	}
    }

}
