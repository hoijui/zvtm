/*   FILE: TInverseCosineLens.java
 *   DATE OF CREATION:  Thu Oct 05 11:35:04 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */ 


package net.claribole.zvtm.lens;

import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

/**Translucent lens. Profile: linear - Distance metric: L(2) (circular shape)<br>Size expressed as an absolute value in pixels*/

public class TInverseCosineLens extends FixedSizeLens {

    /* gain function parameters (transition in translucence space) */
    protected float MMTc = 0.0f;
    protected float MMTf = 1.0f;
    protected float aT = 0;
    protected float bT = 0;
    protected float cT = 0;
    float[] gainT = new float[1];

    double d = 0;

    int[] BMlI, BMmI; // bit masks
    int[] BOlI, BOmI; // bit offsets
    int PlI, PmI;
    int RlI, GlI, BlI;
    int RmI, GmI, BmI, AmI;
    int RrI, GrI, BrI;

    /**
     * create a lens with a maximum magnification factor of 2.0
     */
    public TInverseCosineLens(){
	this.MM = 2.0f;
	updateMagBufferWorkingDimensions();
	aT = 1/(float)(LR1-LR2);
	bT = (float)LR2/(float)(LR2-LR1);
	cT = (2/(float)Math.PI)*(MMTf-MMTc);
    }

    /**
     * create a lens with a given maximum magnification factor
     *
     *@param mm magnification factor, mm in [0,+inf[
     */
    public TInverseCosineLens(float mm){
	this.MM = mm;
	updateMagBufferWorkingDimensions();
	aT = 1/(float)(LR1-LR2);
	bT = (float)LR2/(float)(LR2-LR1);
	cT = (2/(float)Math.PI)*(MMTf-MMTc);
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
	aT = 1/(float)(LR1-LR2);
	bT = (float)LR2/(float)(LR2-LR1);
	cT = (2/(float)Math.PI)*(MMTf-MMTc);
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
	aT = 1/(float)(LR1-LR2);
	bT = (float)LR2/(float)(LR2-LR1);
	cT = (2/(float)Math.PI)*(MMTf-MMTc);
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
	aT = 1/(float)(LR1-LR2);
	bT = (float)LR2/(float)(LR2-LR1);
	cT = (2/(float)Math.PI)*(MMTf-MMTc);
    }

    /**
     * set the lens' inner radius (beyond which maximum magnification is applied - inward)
     *
     *@param r radius in pixels
     */
    public void setInnerRadius(int r){
	super.setInnerRadius(r);
	aT = 1/(float)(LR1-LR2);
	bT = (float)LR2/(float)(LR2-LR1);
	cT = (2/(float)Math.PI)*(MMTf-MMTc);
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
	aT = 1/(float)(LR1-LR2);
	bT = (float)LR2/(float)(LR2-LR1);
	cT = (2/(float)Math.PI)*(MMTf-MMTc);
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
	aT = 1/(float)(LR1-LR2);
	bT = (float)LR2/(float)(LR2-LR1);
	cT = (2/(float)Math.PI)*(MMTf-MMTc);
    }

    public void setMaximumMagnification(float mm){
	this.setMaximumMagnification(mm, true);
    }

    public void setMaximumMagnification(float mm, boolean forceRaster){
	super.setMaximumMagnification(mm, forceRaster);
	aT = 1/(float)(LR1-LR2);
	bT = (float)LR2/(float)(LR2-LR1);
	cT = (2/(float)Math.PI)*(MMTf-MMTc);
    }

    public void gf(float x, float y, float[] g){
	d = Math.sqrt(Math.pow(x-sw-lx,2) + Math.pow(y-sh-ly,2));
	if (d <= LR2)
	    g[0] = g[1] = MM;
	else
	    g[0] = g[1] = 1;
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

    synchronized void transformI(WritableRaster iwr, WritableRaster ewr){
	synchronized(this){
	    if (BMlI == null){// || BMmI == null || BOlI == null || BOmI == null
		// initialization of raster band configuration (should only occur once)
		SinglePixelPackedSampleModel SMl = (SinglePixelPackedSampleModel)ewr.getSampleModel();
		SinglePixelPackedSampleModel SMm = (SinglePixelPackedSampleModel)iwr.getSampleModel();
		BMlI = SMl.getBitMasks();
		BMmI = SMm.getBitMasks();
		BOlI = SMl.getBitOffsets();
		BOmI = SMm.getBitOffsets();
	    }
	    // get source pixels in an array
	    iwr.getDataElements(lurd[0], lurd[1], lensWidth, lensHeight, oPixelsI);
	    // get magnified source pixels in a second array
	    ewr.getDataElements(0, 0, mbw, mbh, mPixelsI);
	    // transfer them to the target array taking the gain function into account
	    if (BMlI.length == 4){// the sample model features four bands
		for (int x=lurd[0];x<lurd[2];x++){
		    for (int y=lurd[1];y<lurd[3];y++){
			//this.gf(x,y,gain);
			// get pixel from lens raster
			PlI = mPixelsI[Math.round(((y-lurd[1]) * MM - mbh/2.0f) / MM + mbh/2.0f)*mbw+Math.round(((x-lurd[0]) * MM - mbw/2.0f) / MM + mbw/2.0f)];
			RlI = (PlI & BMlI[0]) >>> BOlI[0];
			GlI = (PlI & BMlI[1]) >>> BOlI[1];
			BlI = (PlI & BMlI[2]) >>> BOlI[2];
			// get pixel from main raster
			PmI = oPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])];
			RmI = (PmI & BMmI[0]) >>> BOmI[0];
			GmI = (PmI & BMmI[1]) >>> BOmI[1];
			BmI = (PmI & BMmI[2]) >>> BOmI[2];
			AmI = (PmI & BMmI[3]) >>> BOmI[3];
			// compute contribution from each pixel, for each band
			// Use the Porter-Duff Source Atop Destination rule to achieve our effect.
			// Fs = Ad and Fd = (1-As), thus:
			//   Cd = Cs*Ad + Cd*(1-As)
			//   Ad = As*Ad + Ad*(1-As) = Ad
			this.gfT(x,y,gainT);
			RrI = Math.round(RlI*gainT[0] + RmI*(1-gainT[0]));
			GrI = Math.round(GlI*gainT[0] + GmI*(1-gainT[0]));
			BrI = Math.round(BlI*gainT[0] + BmI*(1-gainT[0]));
			// set new pixel value in target raster
			tPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])] = (RrI << BOmI[0]) | (GrI << BOlI[1]) | (BrI << BOlI[2]) | (AmI << BOlI[3]);
		    }
		}
	    }
	    else {// the sample model probably features 3 bands
		for (int x=lurd[0];x<lurd[2];x++){
		    for (int y=lurd[1];y<lurd[3];y++){
			//this.gf(x,y,gain);
			// get pixel from lens raster
			PlI = mPixelsI[Math.round(((y-lurd[1]) * MM - mbh/2.0f) / MM + mbh/2.0f)*mbw+Math.round(((x-lurd[0]) * MM - mbw/2.0f) / MM + mbw/2.0f)];
			RlI = (PlI & BMlI[0]) >>> BOlI[0];
			GlI = (PlI & BMlI[1]) >>> BOlI[1];
			BlI = (PlI & BMlI[2]) >>> BOlI[2];
			// get pixel from main raster
			PmI = oPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])];
			RmI = (PmI & BMmI[0]) >>> BOmI[0];
			GmI = (PmI & BMmI[1]) >>> BOmI[1];
			BmI = (PmI & BMmI[2]) >>> BOmI[2];
			// compute contribution from each pixel, for each band
			// Use the Porter-Duff Source Atop Destination rule to achieve our effect.
			// Fs = Ad and Fd = (1-As), thus:
			//   Cd = Cs*Ad + Cd*(1-As)
			//   Ad = As*Ad + Ad*(1-As) = Ad
			this.gfT(x,y,gainT);
			RrI = Math.round(RlI*gainT[0] + RmI*(1-gainT[0]));
			GrI = Math.round(GlI*gainT[0] + GmI*(1-gainT[0]));
			BrI = Math.round(BlI*gainT[0] + BmI*(1-gainT[0]));
			// set new pixel value in target raster
			tPixelsI[(y-lurd[1])*(lensWidth)+(x-lurd[0])] = (RrI << BOmI[0]) | (GrI << BOlI[1]) | (BrI << BOlI[2]);
		    }
		}
	    }
	    // transfer pixels in the target array back to the raster
	    iwr.setDataElements(lurd[0], lurd[1], lensWidth, lensHeight, tPixelsI);
	}
    }

    synchronized void transformS(WritableRaster iwr, WritableRaster ewr){System.err.println("Error: translucent lens: Sample model not supported yet");}

    synchronized void transformB(WritableRaster iwr, WritableRaster ewr){System.err.println("Error: translucent lens: Sample model not supported yet");}

}