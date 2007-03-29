/*   FILE: TInverseCosineLens.java
 *   DATE OF CREATION:  Thu Oct 05 11:35:04 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 


package net.claribole.zvtm.lens;

import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

/**Translucent lens. Profile: inverse cosine - Distance metric: L(2) (circular shape)<br>Size expressed as an absolute value in pixels*/

public class TInverseCosineLens extends TLens {

    /* gain function parameters (transition in translucence space) */
    protected float aT = 0;
    protected float bT = 0;
    protected float cT = 0;

    /**
     * create a lens with a maximum magnification factor of 2.0
     */
    public TInverseCosineLens(){
	this.MM = 2.0f;
	updateMagBufferWorkingDimensions();
	computeDropoffFactors();
    }

    /**
     * create a lens with a given maximum magnification factor
     *
     *@param mm magnification factor, mm in [0,+inf[
     */
    public TInverseCosineLens(float mm){
	this.MM = mm;
	updateMagBufferWorkingDimensions();
	computeDropoffFactors();
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm magnification factor, mm in [0,+inf[
     *@param tc translucency value (at junction between transition and context), tc in [0,1.0]
     *@param tf translucency value (at junction between transition and focus), tf in [0,1.0]
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public TInverseCosineLens(float mm, float tc, float tf, int outerRadius, int innerRadius){
	this.MM = mm;
	this.LR1 = outerRadius;
	this.LR2 = innerRadius;
	this.MMTc = tc;
	this.MMTf = tf;
	updateMagBufferWorkingDimensions();
	computeDropoffFactors();
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm magnification factor, mm in [0,+inf[
     *@param tc translucency value (at junction between transition and context), tc in [0,1.0]
     *@param tf translucency value (at junction between transition and focus), tf in [0,1.0]
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     *@param x horizontal coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     *@param y vertical coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     */
    public TInverseCosineLens(float mm, float tc, float tf, int outerRadius, int innerRadius, int x, int y){
	this.MM = mm;
	this.LR1 = outerRadius;
	this.LR2 = innerRadius;
	this.MMTc = tc;
	this.MMTf = tf;
	updateMagBufferWorkingDimensions();
	computeDropoffFactors();
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
	computeDropoffFactors();
    }

    /**
     * set the lens' inner radius (beyond which maximum magnification is applied - inward)
     *
     *@param r radius in pixels
     */
    public void setInnerRadius(int r){
	super.setInnerRadius(r);
	computeDropoffFactors();
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
	computeDropoffFactors();
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
	computeDropoffFactors();
    }

    public void setMaximumMagnification(float mm){
	this.setMaximumMagnification(mm, true);
    }

    public void setMaximumMagnification(float mm, boolean forceRaster){
	super.setMaximumMagnification(mm, forceRaster);
	computeDropoffFactors();
    }
    
    void computeDropoffFactors(){
	aT = 1/(float)(LR1-LR2);
	bT = (float)LR2/(float)(LR2-LR1);
	cT = (2/(float)Math.PI)*(MMTf-MMTc);
    }

    public void gfT(float x, float y, float[] g){
	d = Math.sqrt(Math.pow(x-sw-lx,2) + Math.pow(y-sh-ly,2));
	if (d <= LR2)
	    g[0] = MMTf;
	else if (d <= LR1)
	    g[0] = MMTf-cT*(float)Math.acos(Math.pow(d*aT+bT-1,2));
	else
	    g[0] = MMTc;
    }

}