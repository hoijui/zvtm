/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010-2011. All Rights Reserved
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
import java.awt.RadialGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;

import fr.inria.zvtm.nodetrix.ProjectColors;

/** A path whose smoothly color changes from one color to another one. Approximated using a radial gradient paint. */

public class GPath extends DPath {
    
    private Color[] gradientColors = new Color[2];
    float[] gradientDist = {0.2f, 0.8f};
    Point2D gradientCenter = new Point2D.Float();
    RadialGradientPaint p;
    
    public GPath(){
		super();
        gradientColors[0] = ProjectColors.EXTRA_COLOR_GRADIENT_START[ProjectColors.COLOR_SCHEME];
        gradientColors[1] = ProjectColors.EXTRA_COLOR_GRADIENT_END[ProjectColors.COLOR_SCHEME];;
    }
    
	/**
		*@param x start coordinate in virtual space
		*@param y start coordinate in virtual space
		*@param z z-index (pass 0 if you do not use z-ordering)
		*@param c color
		*/
	public GPath(double x, double y, int z, Color c){
	    super(x, y, z, c);
    }
    
	/**
		*@param x start coordinate in virtual space
		*@param y start coordinate in virtual space
		*@param z z-index (pass 0 if you do not use z-ordering)
		*@param c color
		*@param alpha alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
		*/
	public GPath(double x, double y, int z, Color c, float alpha){
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
    
    /** Set colors defining gradient.
     *@param gc array containing colors defining the gradient.
     *@see #setGradientDist(float[] gd)
     *@see #setGradient(Color[] gc, float[] gd)
     */
    public void setGradientColors(Color[] gc){
        this.gradientColors = gc;        
    }
    
    @Override
    /** Is just a <i>delegate</i> for <code>setGradientColors</code>. The one color passed
     * to this method is the second gradient value, whether the first one is a darker 
     * version of the second one. Hence the edge goes gray->color.
     * @author benjamin bach
     * @param Color c
     */
    public void setColor(Color c){
    	this.gradientColors = new Color[2];
    	this.gradientColors[0] = ProjectColors.EXTRA_COLOR_GRADIENT_START[ProjectColors.COLOR_SCHEME];
        this.gradientColors[1] = c;        
    }
    
    /** Set gradient distribution.
     *@param gd array containing as many floats in [0;1.0] as there are colors defining the gradient.
     *@see #setGradientColors(Color[] gc)
     *@see #setGradient(Color[] gc, float[] gd)
     */
    public void setGradientDist(float[] gd){
        this.gradientDist = gd;        
    }
    
    /** Set gradient color and distribution.
     *@param gc array containing colors defining the gradient.
     *@param gd array containing as many floats in [0;1.0] as there are colors defining the gradient.
     *@see #setGradientDist(float[] gd)
     *@see #setGradientColors(Color[] gc)
     */
    public void setGradient(Color[] gc, float[] gd){
        this.gradientColors = gc;
        this.gradientDist = gd;
    }
    
    @Override
    public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha() == 0){return;}
        gradientCenter.setLocation(pc[i].cx, pc[i].cy);
        p = new RadialGradientPaint(gradientCenter,
                                    (float)Math.sqrt(Math.pow(elements[elements.length-1].getX(i)-pc[i].cx,2) + Math.pow(elements[elements.length-1].getY(i)-pc[i].cy,2)),
                                    gradientDist, gradientColors, CycleMethod.NO_CYCLE);
        g.setPaint(p);
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
    
    @Override
    public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
        if (alphaC != null && alphaC.getAlpha() == 0){return;}
        gradientCenter.setLocation(pc[i].lcx, pc[i].lcy);
        p = new RadialGradientPaint(gradientCenter,
                                    (float)Math.sqrt(Math.pow(elements[elements.length-1].getlX(i)-pc[i].lcx,2) + Math.pow(elements[elements.length-1].getlY(i)-pc[i].lcy,2)),
                                    gradientDist, gradientColors, CycleMethod.NO_CYCLE);
        g.setPaint(p);
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