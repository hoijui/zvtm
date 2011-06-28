/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Area;
import java.awt.Shape;
import java.awt.geom.Point2D;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.projection.BProjectedCoords;

public class VEclipse extends VCircle {
	
	float fraction = .5f;
	
	/**
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param d diameter in virtual space
     *@param f fraction of full eclipse (-1 to 1, with 0 = full eclipse)
     *@param c fill color
     *@param bc border color
     *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public VEclipse(double x, double y, int z, double d, float f, Color c, Color bc, float alpha){
		super(x, y, z, d, c, bc, alpha);
		this.fraction = f;
	}
	
	@Override
	public void initCams(int nbCam){
		pc = new ProjEclipse[nbCam];
		for (int i=0;i<nbCam;i++){
			pc[i] = new ProjEclipse();
		}
	}

	@Override
	public void addCamera(int verifIndex){
		if (pc!=null){
			if (verifIndex == pc.length){
				BProjectedCoords[] ta = pc;
				pc = new ProjEclipse[ta.length+1];
				for (int i=0;i<ta.length;i++){
					pc[i] = ta[i];
				}
				pc[pc.length-1] = new ProjEclipse();
			}
			else {System.err.println("VEclipse:Error while adding camera "+verifIndex);}
		}
		else {
			if (verifIndex == 0){
				pc = new ProjEclipse[1];
				pc[0] = new ProjEclipse();
			}
			else {System.err.println("VEclipse:Error while adding camera "+verifIndex);}
		}
	}
	
	@Override
    public void project(Camera c, Dimension d){
        int i = c.getIndex();
        coef = c.focal / (c.focal+c.altitude);
        //find coordinates of object's geom center wrt to camera center and project
        //translate in JPanel coords
        pc[i].cx = (int)Math.round((d.width/2)+(vx-c.vx)*coef);
        pc[i].cy = (int)Math.round((d.height/2)-(vy-c.vy)*coef);
 		pc[i].cr = (int)Math.round(size*coef/2d);
		ProjEclipse pe = (ProjEclipse)pc[i];
		pe.eclipsed.setFrame(pc[i].cx-pc[i].cr, pc[i].cy-pc[i].cr, 2*pc[i].cr, 2*pc[i].cr);
		pe.shadowSource.setFrame(pc[i].cx-pc[i].cr+2*pc[i].cr*fraction, pc[i].cy-pc[i].cr, 2*pc[i].cr, 2*pc[i].cr);
    }

	@Override
	public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
		if (alphaC != null && alphaC.getAlpha()==0){return;}
		ProjEclipse pe = (ProjEclipse)pc[i];
        if ((pe.eclipsed.getBounds().width>2) || (pe.eclipsed.getBounds().height>2)){
			Area eclipsed = new Area(pe.eclipsed);
			eclipsed.intersect(new Area(pe.shadowSource));
            if (alphaC != null){
                g.setComposite(alphaC);
                if (filled){
                    g.setColor(this.color);
                    g.translate(dx, dy);
                    g.fill(eclipsed);
                    g.translate(-dx, -dy);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke!=null){
                        g.setStroke(stroke);
                        g.translate(dx, dy);
                        g.draw(eclipsed);
                        g.translate(-dx, -dy);
                        g.setStroke(stdS);
                    }
                    else {
                        g.translate(dx, dy);
                        g.draw(eclipsed);
                        g.translate(-dx, -dy);
                    }
                }
                g.setComposite(acO);
            }
            else {
                if (filled){
                    g.setColor(this.color);
                    g.translate(dx, dy);
                    g.fill(eclipsed);
                    g.translate(-dx, -dy);
                }
                if (paintBorder){
                    g.setColor(borderColor);
                    if (stroke!=null){
                        g.setStroke(stroke);
                        g.translate(dx, dy);
                        g.draw(eclipsed);
                        g.translate(-dx, -dy);
                        g.setStroke(stdS);
                    }
                    else {
                        g.translate(dx, dy);
                        g.draw(eclipsed);
                        g.translate(-dx, -dy);
                    }
                }
            }
        }
        else {
            g.setColor(this.color);
            if (alphaC != null){
                g.setComposite(alphaC);
                g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
                g.setComposite(acO);
            }
            else {
                g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
            }
        }
	}
	
}

class ProjEclipse extends BProjectedCoords {

	Ellipse2D eclipsed = new Ellipse2D.Float();
	Ellipse2D shadowSource = new Ellipse2D.Float();
	
}
