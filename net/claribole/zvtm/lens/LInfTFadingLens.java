/*   FILE: LInfTFadingLens.java
 *   DATE OF CREATION:  Fri Oct 06 08:41:04 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */ 


package net.claribole.zvtm.lens;

import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.geom.Point2D;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

import java.util.Timer;
import java.util.TimerTask;

import com.xerox.VTM.glyphs.Translucent;

/**Translucent lens. Lens that fades away when moving fast - Distance metric: L(Inf) (rectangular shape)<br>Size expressed as an absolute value in pixels*/

public class LInfTFadingLens extends TFadingLens {

    /**
     * create a lens with a maximum magnification factor of 2.0
     */
    public LInfTFadingLens(){
	super();
    }

    /**
     * create a lens with a given maximum magnification factor
     *
     *@param mm magnification factor, mm in [0,+inf[
     */
    public LInfTFadingLens(float mm){
	super(mm);
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm magnification factor, mm in [0,+inf[
     *@param minT translucency value (at junction between transition and focus), in [0,1.0]
     *@param maxT translucency value (at junction between transition and focus), in [0,1.0]
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public LInfTFadingLens(float mm, float minT, float maxT, int innerRadius){
	super(mm, minT, maxT, innerRadius);
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm magnification factor, mm in [0,+inf[
     *@param minT translucency value (at junction between transition and focus), in [0,1.0]
     *@param maxT translucency value (at junction between transition and focus), in [0,1.0]
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     *@param x horizontal coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     *@param y vertical coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     */
    public LInfTFadingLens(float mm, float minT, float maxT, int innerRadius, int x, int y){
	super(mm, minT, maxT, innerRadius, x, y);
    }

    public void gfT(float x, float y, float[] g){
        d = Math.max(Math.abs(x-sw-lx), Math.abs(y-sh-ly));
	if (d <= LR2)
	    g[0] = MMTf;
	else
	    g[0] = 0.0f;
    }

    public void drawBoundary(Graphics2D g2d){
	if (bColor != null){
	    g2d.setColor(bColor);
	    g2d.drawRect(lx+w/2-lensWidth/2, ly+h/2-lensHeight/2, lensWidth, lensHeight);
	}
	if (rColor != null){
	    //XXX: the following two values could actually be computed less frequently, just when MM of lens{Width,Height} change
	    lensProjectedWidth = Math.round(lensWidth/MM);
	    lensProjectedHeight = Math.round(lensHeight/MM);
	    g2d.setColor(rColor);
	    // get the alpha composite from a precomputed list of values
	    // (we don't want to instantiate a new AlphaComposite at each repaint request)
	    g2d.setComposite(acs[Math.round((1.0f-MMTf)*ACS_ACCURACY)-1]);  
	    g2d.drawRect(lx+w/2-lensProjectedWidth/2, ly+h/2-lensProjectedHeight/2, lensProjectedWidth, lensProjectedHeight);
	    g2d.setComposite(Translucent.acO);
	}
    }

}

