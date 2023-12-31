/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
//import java.awt.RadialGradientPaint;
//import java.awt.MultipleGradientPaint.CycleMethod;

public class GPath extends DPath {
    
    Color[] gradientColors = {new Color(118,98,252), Color.WHITE};
    float[] gradientDist = {0.0f, 1.0f};
    Point2D gradientCenter = new Point2D.Float();
    //RadialGradientPaint p;
    
    public GPath(){
		super();
	}

	/**
		*@param x start coordinate in virtual space
		*@param y start coordinate in virtual space
		*@param z z-index (pass 0 if you do not use z-ordering)
		*@param c color
		*/
	public GPath(long x, long y, int z, Color c){
	    super(x, y, z, c);
    }
    
	/**
		*@param x start coordinate in virtual space
		*@param y start coordinate in virtual space
		*@param z z-index (pass 0 if you do not use z-ordering)
		*@param c color
		*@param alpha alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
		*/
	public GPath(long x, long y, int z, Color c, float alpha){
		super(x, y, z, c, alpha);
	}

    /**
	 *@param pi PathIterator describing this path (virtual space coordinates)
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param c color
     */
    public GPath(PathIterator pi, int z, Color c){
        super(pi, z, c);
    }
    
    /**
	 *@param pi PathIterator describing this path (virtual space coordinates)
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param c color
     *@param alpha alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public GPath(PathIterator pi, int z, Color c, float alpha){
		super(pi, z, c, alpha);
    }
    
    public void setGradient(Color[] gc, float[] gd){
        this.gradientColors = gc;
        this.gradientDist = gd;
    }
    
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha() == 0){return;}
        //gradientCenter.setLocation(pc[i].cx, pc[i].cy);
        //p = new RadialGradientPaint(gradientCenter,
        //                            (float)Math.sqrt(Math.pow(elements[elements.length-1].getX(i)-pc[i].cx,2) + Math.pow(elements[elements.length-1].getY(i)-pc[i].cy,2)),
        //                            gradientDist, gradientColors, CycleMethod.NO_CYCLE);
        //g.setPaint(p);
        g.setColor(this.color);
        if (stroke!=null) {
            g.setStroke(stroke);
            g.translate(dx,dy);
            if (alphaC != null){
                // translucent
                g.setComposite(alphaC);
                for (int j=0;j<elements.length;j++){
                    if (elements[j].type == GPath.MOV){continue;}
                    g.draw(elements[j].getShape(i));		
                }
                g.setComposite(acO);
            }
            else {
                // opaque
                for (int j=0;j<elements.length;j++){
                    if (elements[j].type == GPath.MOV){continue;}
                    g.draw(elements[j].getShape(i));		
                }
            }
            g.translate(-dx,-dy);
            g.setStroke(stdS);
        }
        else {
            g.translate(dx,dy);
            if (alphaC != null){
                // translucent
                g.setComposite(alphaC);
                for (int j=0;j<elements.length;j++){
                    if (elements[j].type == GPath.MOV){continue;}
                    g.draw(elements[j].getShape(i));
                }
                g.setComposite(acO);
            }
            else {
                // opaque
                for (int j=0;j<elements.length;j++){
                    if (elements[j].type == GPath.MOV){continue;}
                    g.draw(elements[j].getShape(i));
                }
            }
            g.translate(-dx,-dy);
        }
    }
    
    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha() == 0){return;}
        //gradientCenter.setLocation(pc[i].lcx, pc[i].lcy);
        //p = new RadialGradientPaint(gradientCenter,
        //                            (float)Math.sqrt(Math.pow(elements[elements.length-1].getlX(i)-pc[i].lcx,2) + Math.pow(elements[elements.length-1].getlY(i)-pc[i].lcy,2)),
        //                            gradientDist, gradientColors, CycleMethod.NO_CYCLE);
        //g.setPaint(p);
        g.setColor(this.color);
        if (stroke!=null) {
            g.setStroke(stroke);
            g.translate(dx,dy);
            if (alphaC != null){
                // translucent
                g.setComposite(alphaC);
                for (int j=0;j<elements.length;j++){
                    if (elements[j].type == DPath.MOV){continue;}
                    g.draw(elements[j].getlShape(i));
                }
                g.setComposite(acO);
            }
            else {
                // opaque
                for (int j=0;j<elements.length;j++){
                    if (elements[j].type == DPath.MOV){continue;}
                    g.draw(elements[j].getlShape(i));
                }
            }
            g.translate(-dx,-dy);
            g.setStroke(stdS);
        }
        else {
            g.translate(dx,dy);
            if (alphaC != null){
                // translucent
                g.setComposite(alphaC);
                for (int j=0;j<elements.length;j++){
                    if (elements[j].type == DPath.MOV){continue;}
                    g.draw(elements[j].getlShape(i));
                }
                g.setComposite(acO);
            }
            else {
                // opaque
                for (int j=0;j<elements.length;j++){
                    if (elements[j].type == DPath.MOV){continue;}
                    g.draw(elements[j].getlShape(i));
                }
            }
            g.translate(-dx,-dy);
        }
    }
    
}