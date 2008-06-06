/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */

package net.claribole.zvtm.glyphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.Shape;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.VSlice;
import net.claribole.zvtm.glyphs.projection.ProjRing;

/**
 * Like a slice, but with iner and outer arcs (slice of a ring instead of slice of a pie)
 * @author Emmanuel Pietriga
 *@see com.xerox.VTM.glyphs.VSlice
 *@see com.xerox.VTM.glyphs.VSliceST
 */

public class VRing extends VSlice {

	ProjRing[] pr;

	/** Radius of inner ring, from center of ring.*/
	long virr;
    
    /** Construct a slice by giving its 3 vertices
        *@param v array of 3 points representing the absolute coordinates of the slice's vertices. The first element must be the point that is not an endpoint of the arc 
        *@param irr inner ring radius in virtual space (in rad)
		*@param z z-index (pass 0 if you do not use z-ordering)
        *@param c fill color
        *@param bc border color
        */
    public VRing(LongPoint[] v, long irr, int z, Color c, Color bc){
		initCoordArray(4);
        vx = v[0].x;
        vy = v[0].y;
        vz = z;
		virr = irr;
        computeSize();
        computeOrient();
        computeAngle();
        setColor(c);
        setBorderColor(bc);
    }

    /** Construct a slice by giving its size, angle and orientation
        *@param x x-coordinate in virtual space of vertex that is not an arc endpoint
        *@param y y-coordinate in virtual space of vertex that is not an arc endpoint
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param vs arc radius in virtual space
        *@param ag arc angle in virtual space (in rad)
        *@param irr inner ring radius in virtual space
        *@param or slice orientation in virtual space (interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc) (in rad)
        *@param c fill color
        *@param bc border color
        */
    public VRing(long x, long y, int z, long vs, double ag, long irr, double or, Color c, Color bc){
		initCoordArray(4);	
        vx = x;
        vy = y;
        vz = z;
        size = (float)vs;
        vr = vs;
		virr = irr;
        orient = or;
        orientDeg = (int)Math.round(orient * RAD2DEG_FACTOR);
        angle = ag;
        angleDeg = (int)Math.round(angle * RAD2DEG_FACTOR);
        setColor(c);
        setBorderColor(bc);
    }

    /** Construct a slice by giving its size, angle and orientation
        *@param x x-coordinate in virtual space of vertex that is not an arc endpoint
        *@param y y-coordinate in virtual space of vertex that is not an arc endpoint 
        *@param z z-index (pass 0 if you do not use z-ordering)
        *@param vs arc radius in virtual space
        *@param ag arc angle in virtual space (in degrees)
        *@param irr inner ring radius in virtual space
        *@param or slice orientation in virtual space (interpreted as the orientation of the segment linking the vertex that is not an arc endpoint to the middle of the arc)  (in degrees)
        *@param c fill color
        *@param bc border color
        */
    public VRing(long x, long y, int z, long vs, int ag, long irr, int or, Color c, Color bc){
		initCoordArray(4);	
        vx = x;
        vy = y;
        vz = z;
        size = (float)vs;
        vr = vs;
		virr = irr;
        orient = or * DEG2RAD_FACTOR;
        orientDeg = or;
        angle = ag * DEG2RAD_FACTOR;
        angleDeg = ag;
        setColor(c);
        setBorderColor(bc);
    }

	public void initCams(int nbCam){
		pc = new ProjRing[nbCam];
		for (int i=0;i<nbCam;i++){
			pc[i] = new ProjRing();
		}
		pr = (ProjRing[])pc;
	}

	public void addCamera(int verifIndex){
		if (pc != null){
			if (verifIndex == pc.length){
				ProjRing[] ta = (ProjRing[])pc;
				pc = new ProjRing[ta.length+1];
				for (int i=0;i<ta.length;i++){
					pc[i] = ta[i];
				}
				pc[pc.length-1] = new ProjRing();
				pr = (ProjRing[])pc;
			}
			else {System.err.println("VRing:Error while adding camera "+verifIndex);}
		}
		else {
			if (verifIndex == 0){
				pc = new ProjRing[1];
				pc[0] = new ProjRing();
			}
			else {System.err.println("VRing:Error while adding camera "+verifIndex);}
		}
	}

	public boolean coordInside(int x,int y,int camIndex){
		if (Math.sqrt(Math.pow(x-pc[camIndex].cx, 2)+Math.pow(y-pc[camIndex].cy, 2)) <= pc[camIndex].innerCircleRadius){
			if (ring.contains(x,y)){
				return true;
			}
		}
		return false;
	}
    
	public void project(Camera c, Dimension d){
		int i = c.getIndex();
		coef = (float)(c.focal / (c.focal + c.altitude));
		//find coordinates of object's geom center wrt to camera center and project
		//translate in JPanel coords
		int hw = d.width/2;
		int hh = d.height/2;
		pc[i].cx = hw + Math.round((vx-c.posx) * coef);
		pc[i].cy = hh - Math.round((vy-c.posy) * coef);
		pc[i].innerCircleRadius = Math.round(size * coef);
		pr[i].innerRingRadius = Math.round(virr * coef);
	}

	public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, long lensx, long lensy){
		int i = c.getIndex();
		coef = (float)(c.focal / (c.focal + c.altitude)) * lensMag;
		//find coordinates of object's geom center wrt to camera center and project
		//translate in JPanel coords
		int hw = lensWidth/2;
		int hh = lensHeight/2;
		pc[i].lcx = hw + Math.round((vx-lensx) * coef);
		pc[i].lcy = hh - Math.round((vy-lensy) * coef);
		pc[i].linnerCircleRadius = Math.round(size * coef);
		pr[i].linnerRingRadius = Math.round(virr * coef);
	}
	
	Arc2D outerSlice = new Arc2D.Double(Arc2D.PIE);
	Arc2D innerSlice = new Arc2D.Double(Arc2D.PIE);
    Area ring, subring;
	
	public void draw(Graphics2D g, int vW, int vH, int i, Stroke stdS, AffineTransform stdT, int dx, int dy){
		if (pc[i].innerCircleRadius > 2){
			if (isFilled()){
				// larger pie slice
				outerSlice.setArc(dx+pc[i].cx - pc[i].innerCircleRadius, dy+pc[i].cy - pc[i].innerCircleRadius,
					2 * pc[i].innerCircleRadius, 2 * pc[i].innerCircleRadius,
					(int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1, Arc2D.PIE);
				// smaller pie slice to remove to create the ring
				innerSlice.setArc(dx+pc[i].cx - pr[i].innerRingRadius, dy+pc[i].cy - pr[i].innerRingRadius,
					2 * pr[i].innerRingRadius, 2 * pr[i].innerRingRadius,
					(int)Math.round(orientDeg-angleDeg/2.0), angleDeg, Arc2D.PIE);
				// actually combine both to create the ring (subtraction)
				ring = new Area(outerSlice);
				subring = new Area(innerSlice);
				ring.subtract(subring);
				// draw that area
				g.setColor(this.color);
				g.fill(ring);
			}
			if (isBorderDrawn()){
				g.setColor(borderColor);
				if (stroke != null){
					g.setStroke(stroke);
				  	g.draw(ring);
					g.setStroke(stdS);
				}
				else {
				  g.draw(ring);
				}
			}
		}
		else {
			//paint a dot if too small
			g.setColor(this.color);
			g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
		}
	}

	public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
		if (pc[i].linnerCircleRadius > 2){
			if (isFilled()){
				// larger pie slice
				outerSlice.setArc(dx+pc[i].lcx - pc[i].linnerCircleRadius, dy+pc[i].lcy - pc[i].linnerCircleRadius,
					2 * pc[i].linnerCircleRadius, 2 * pc[i].linnerCircleRadius,
					(int)Math.round(orientDeg-angleDeg/2.0), angleDeg-1, Arc2D.PIE);
				// smaller pie slice to remove to create the ring
				innerSlice.setArc(dx+pc[i].lcx - pr[i].linnerRingRadius, dy+pc[i].lcy - pr[i].linnerRingRadius,
					2 * pr[i].linnerRingRadius, 2 * pr[i].linnerRingRadius,
					(int)Math.round(orientDeg-angleDeg/2.0), angleDeg, Arc2D.PIE);
				// actually combine both to create the ring (subtraction)
				ring = new Area(outerSlice);
				subring = new Area(innerSlice);
				ring.subtract(subring);
				// draw that area
				g.setColor(this.color);
				g.fill(ring);
			}
			if (isBorderDrawn()){
				g.setColor(borderColor);
				if (stroke != null){
					g.setStroke(stroke);
				  	g.draw(ring);
					g.setStroke(stdS);
				}
				else {
				  g.draw(ring);
				}
			}
		}
		else {
			//paint a dot if too small
			g.setColor(this.color);
			g.fillRect(dx+pc[i].lcx,dy+pc[i].lcy,1,1);
		}
	}

    /** Not implement yet. */
    public Object clone(){
	//XXX: TBW
	return null;
    }

}
