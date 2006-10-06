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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

import java.util.Timer;
import java.util.TimerTask;

import net.claribole.zvtm.engine.LowPassFilter;

/**Translucent lens. Profile: inverse cosine - Distance metric: L(2) (circular shape)<br>Size expressed as an absolute value in pixels*/

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
     *@param tf translucency value (at junction between transition and focus), tf in [0,1.0]
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public LInfTFadingLens(float mm, float tf, int innerRadius){
	super(mm, tf, innerRadius);
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm magnification factor, mm in [0,+inf[
     *@param tf translucency value (at junction between transition and focus), tf in [0,1.0]
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     *@param x horizontal coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     *@param y vertical coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     */
    public LInfTFadingLens(float mm, float tf, int innerRadius, int x, int y){
	super(mm, tf, innerRadius, x, y);
    }

    public void gfT(float x, float y, float[] g){
        d = Math.max(Math.abs(x-sw-lx), Math.abs(y-sh-ly));
	if (d <= LR2)
	    g[0] = MMTf;
	else
	    g[0] = 0.0f;
    }

    public void drawBoundary(Graphics2D g2d){
	g2d.setColor(Color.BLACK);
	g2d.drawRect(lx+w/2-lensWidth/2, ly+h/2-lensHeight/2, lensWidth, lensHeight);
    }

}

