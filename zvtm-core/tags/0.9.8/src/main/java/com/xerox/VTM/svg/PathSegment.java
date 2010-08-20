/*   FILE: PathSegment.java
 *   DATE OF CREATION:   Thu Jan 16 17:24:56 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu Jan 16 17:25:02 2003 by Emmanuel Pietriga
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package com.xerox.VTM.svg;

import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

class PathSegment {
    
    int type;
    double[] cds=new double[6];

    PathSegment(double[] c,int t){
	type=t;
	cds[0]=c[0];
	cds[1]=-c[1];
	cds[2]=c[2];
	cds[3]=-c[3];
	cds[4]=c[4];
	cds[5]=-c[5];
    }

    int getType(){return type;}

    double[] getCoords(){return cds;}

    Point2D getMainPoint(){
	if (type==PathIterator.SEG_MOVETO || type==PathIterator.SEG_LINETO){
	    return new Point2D.Double(cds[0],cds[1]);
	}
	else if (type==PathIterator.SEG_QUADTO){
	    return new Point2D.Double(cds[2],cds[3]);
	}
	else if (type==PathIterator.SEG_CUBICTO){
	    return new Point2D.Double(cds[4],cds[5]);
	}
	return null;
    }

    void setMainPoint(Point2D p){
	if (type==PathIterator.SEG_MOVETO || type==PathIterator.SEG_LINETO){
	    cds[0]=p.getX();
	    cds[1]=p.getY();
	}
	else if (type==PathIterator.SEG_QUADTO){
	    cds[2]=p.getX();
	    cds[3]=p.getY();
	}
	else if (type==PathIterator.SEG_CUBICTO){
	    cds[4]=p.getX();
	    cds[5]=p.getY();
	}
    }
    
}
