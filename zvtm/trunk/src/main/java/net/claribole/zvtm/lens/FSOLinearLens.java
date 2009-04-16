/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.lens;

/**Profile: linear - Distance metric: L(2) (circular shape)<br>Size expressed as an absolute value in pixels. Parameterable focus offset.*/

public class FSOLinearLens extends FSOLens {

    //gain function parameters
    protected float a = 0;
    protected float b = 0;

    double d = 0;

    /**
     * create a lens with a maximum magnification factor of 2.0
     */
    public FSOLinearLens(){
	    this(2.0f, 100, 50, 0, 0);
    }

    /**
     * create a lens with a given maximum magnification factor
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     */
    public FSOLinearLens(float mm){
	    this(mm, 100, 50, 0, 0);
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public FSOLinearLens(float mm, int outerRadius, int innerRadius){
	    this(mm, outerRadius, innerRadius, 0, 0);
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     *@param x horizontal coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     *@param y vertical coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     */
    public FSOLinearLens(float mm, int outerRadius, int innerRadius, int x, int y){
	this.MM = mm;
	this.LR1 = outerRadius;
	this.LR2 = innerRadius;
	updateMagBufferWorkingDimensions();
	a = (1-MM)/(float)(LR1 - LR2);
	b = (MM*LR1-LR2)/(float)(LR1 - LR2);
	lx = x;
	ly = y;
    }

    /**
     * set the lens' outer radius (beyond which no magnification is applied - outward)
     *
     *@param r radius in pixels
     */
    public void setOuterRadius(int r){
	super.setOuterRadius(r);
	a = (1-MM)/(float)(LR1 - LR2);
	b = (MM*LR1-LR2)/(float)(LR1 - LR2);
    }

    /**
     * set the lens' inner radius (beyond which maximum magnification is applied - inward)
     *
     *@param r radius in pixels
     */
    public void setInnerRadius(int r){
	super.setInnerRadius(r);
	a = (1-MM)/(float)(LR1 - LR2);
	b = (MM*LR1-LR2)/(float)(LR1 - LR2);
    }

    /**
     * set the lens' radii
     *
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public void setRadii(int outerRadius, int innerRadius){
	this.setRadii(outerRadius, innerRadius, true);
    }

    /**
     * set the lens' radii
     *
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public void setRadii(int outerRadius, int innerRadius, boolean forceRaster){
	super.setRadii(outerRadius, innerRadius, forceRaster);
	a = (1-MM)/(float)(LR1 - LR2);
	b = (MM*LR1-LR2)/(float)(LR1 - LR2);
    }


    /**
     * set the lens' radii and maximum magnification
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public void setMMandRadii(float mm, int outerRadius, int innerRadius){
	this.setMMandRadii(mm, outerRadius, innerRadius, true);
    }

    /**
     * set the lens' radii and maximum magnification
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public void setMMandRadii(float mm, int outerRadius, int innerRadius, boolean forceRaster){
	super.setMMandRadii(mm, outerRadius, innerRadius, forceRaster);
	a = (1-MM)/(float)(LR1 - LR2);
	b = (MM*LR1-LR2)/(float)(LR1 - LR2);
    }

    public void setMaximumMagnification(float mm){
	this.setMaximumMagnification(mm, true);
    }

    public void setMaximumMagnification(float mm, boolean forceRaster){
	super.setMaximumMagnification(mm, forceRaster);
	a = (1-MM)/(float)(LR1 - LR2);
	b = (MM*LR1-LR2)/(float)(LR1 - LR2);
    }

    public void gf(float x, float y, float[] g){
	d = Math.sqrt(Math.pow(x-sw-lx,2) + Math.pow(y-sh-ly,2));
	if (d <= LR2)
	    g[0] = g[1] = MM;
	else if (d <= LR1)
	    g[0] = g[1] = a * (float)d + b;
	else
	    g[0] = g[1] = 1;
    }

}