/*   FILE: TFadingLens.java
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

/**Translucent lens. Lens that fades away when moving fast - Distance metric: L(2) (circular shape)<br>Size expressed as an absolute value in pixels*/

public class TFadingLens extends TLens {

    double frequency = -1;
    long mLastSampleTime = -1;
    int xOffset = -10;
    int yOffset = 10;
    double maxDist = 2 * Math.abs(xOffset);
    LowPassFilter filter = new LowPassFilter();
    Point2D currentPos = new Point2D.Double(0, 0);
    Point2D parentPos = new Point2D.Double(0, 0);
    Point2D targetPos = new Point2D.Double(0, 0);
    Timer timer;
    TrailingTimer mouseStillUpdater;

    /**Lens boundary color (default is black)*/
    Color bColor = Color.BLACK;

    // MMTf is used to hold the current translucence
    // a and b are used to convert the filter's opacity to values in the [minT, maxT] range
    float a = 1;
    float b = 0;

    /**
     * create a lens with a maximum magnification factor of 2.0
     */
    public TFadingLens(){
	this.MM = 2.0f;
	updateMagBufferWorkingDimensions();
	computeOpacityFactors(0, 1);
	initTimer();
    }

    /**
     * create a lens with a given maximum magnification factor
     *
     *@param mm magnification factor, mm in [0,+inf[
     */
    public TFadingLens(float mm){
	this.MM = mm;
	updateMagBufferWorkingDimensions();
	computeOpacityFactors(0, 1);
	initTimer();
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm magnification factor, mm in [0,+inf[
     *@param minT translucency value (at junction between transition and focus), in [0,1.0]
     *@param maxT translucency value (at junction between transition and focus), in [0,1.0]
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public TFadingLens(float mm, float minT, float maxT, int innerRadius){
	this.MM = mm;
	this.LR2 = innerRadius;
	this.MMTf = maxT;
	updateMagBufferWorkingDimensions();
	computeOpacityFactors(minT, maxT);
	initTimer();
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
    public TFadingLens(float mm, float minT, float maxT, int innerRadius, int x, int y){
	this.MM = mm;
	this.LR2 = innerRadius;
	this.MMTf = maxT;
	updateMagBufferWorkingDimensions();
	computeOpacityFactors(minT, maxT);
	lx = x;
	ly = y;
	initTimer();
    }

    void computeOpacityFactors(float minT, float maxT){
	a = maxT - minT;
	b = minT;
    }

    void initTimer(){
	timer = new Timer();
	mouseStillUpdater = new TrailingTimer(this);
	timer.scheduleAtFixedRate(mouseStillUpdater, 40, 40);
    }

    /**
     * set the lens' inner radius (beyond which maximum magnification is applied - inward)
     *
     *@param r radius in pixels
     */
    public void setInnerRadius(int r){
	super.setInnerRadius(r);
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
    }

    public void setMaximumMagnification(float mm){
	this.setMaximumMagnification(mm, true);
    }

    public void setMaximumMagnification(float mm, boolean forceRaster){
	super.setMaximumMagnification(mm, forceRaster);
    }

    public void gfT(float x, float y, float[] g){
	d = Math.sqrt(Math.pow(x-sw-lx,2) + Math.pow(y-sh-ly,2));
	if (d <= LR2)
	    g[0] = MMTf;
	else
	    g[0] = 0.0f;
    }

    public void updateFrequency() {
	updateFrequency(System.currentTimeMillis());
    }

    public void updateFrequency(long currentTime) {
	if (frequency == -1){
	    frequency = 1;
	}
	else {
	    if (currentTime != mLastSampleTime){
		frequency = 1000.0 / ((double)(currentTime - mLastSampleTime));
	    }
	}
	mLastSampleTime = currentTime;
    }

    public void updateAlpha(int cx, int cy){
	parentPos.setLocation(cx, cy);
	updateAlpha();
    }

    public void updateAlpha(){
	targetPos.setLocation(parentPos.getX() + xOffset, parentPos.getY() + yOffset);
	double distAway = targetPos.distance(currentPos);
	double opacity = 1.0 - Math.min(1.0, distAway / maxDist);
	filter.setCutOffFrequency(((1.0 - opacity) * 0.8) + 0.1);
	currentPos = filter.apply(targetPos, frequency);
	int tx = (int)Math.round(currentPos.getX());
	int ty = (int)Math.round(currentPos.getY());
	tx = Math.max(tx, w/2);
 	ty = Math.min(ty, owningView.parent.getPanelSize().height - h/2);
	float nMMTf = ((float)opacity) * a + b;
	if (Math.abs(MMTf - nMMTf) > 0.01f){// avoid unnecesarry repaint requests
	    // make the lens almost disappear when making big moves
	    MMTf = nMMTf;
	    owningView.parent.repaintNow();
	}
    }

    public void setNoUpdateWhenMouseStill(boolean b){
	mouseStillUpdater.setEnabled(!b);
    }

    public void dispose(){
	super.dispose();
	timer.cancel();
    }

    /**set the position of the lens inside the view
     *@param ax lens's center horizontal coordinate expressed as an absolute position within the view (JPanel coordinate system)
     *@param ay lens's center vertical coordinate expressed as an absolute position within the view (JPanel coordinate system)
     *@param absTime time at which this event is occuring (in ms, as can be obtained e.g. by System.currentTimeMillis())
     */
    public synchronized void setAbsolutePosition(int ax, int ay, long absTime){
	super.setAbsolutePosition(ax, ay);
	updateFrequency(absTime);
	updateAlpha(ax, ay);
    }

    /**Set the color used to draw the lens' boundaries (default is black)
     *@param bc color of the boundary (set to null if you do not want to draw the border)
     */
    public void setBoundaryColor(Color bc){
	bColor = bc;
    }

    /**for internal use*/
    public void drawBoundary(Graphics2D g2d){
	if (bColor != null){
	    g2d.setColor(bColor);
	    g2d.drawOval(lx+w/2-lensWidth/2, ly+h/2-lensHeight/2, lensWidth, lensHeight);
	}
    }

}

class TrailingTimer extends TimerTask {

    TFadingLens lens;
    private boolean enabled = true;

    TrailingTimer(TFadingLens l){
	super();
	this.lens = l;
    }

    public void setEnabled(boolean b){
	enabled = b;
    }

    public boolean isEnabled(){
	return enabled;
    }

    public void run(){
	if (enabled){
	    lens.updateAlpha();
	}
    }

}
