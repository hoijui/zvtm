/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.lens;

import java.awt.Color;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import java.util.Timer;
import java.util.TimerTask;

import com.xerox.VTM.glyphs.Translucent;
import net.claribole.zvtm.glyphs.Translucency;
import net.claribole.zvtm.engine.LowPassFilter;

/**Profile: inverse cosine - Distance metric: L(2) (circular shape) - Flattens itself when moving fast<br>Size expressed as an absolute value in pixels*/

public class DInverseCosineLens extends FSInverseCosineLens implements TemporalLens {

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
    DICTrailingTimer mouseStillUpdater;

    double cutoffParamA = 0.2;   // 0.8
    double cutoffParamB = 0.001;  // 0.1 to make it more difficult to acquire

    /** Dynamic magnification factor. */
    float dMM = MM;

    /**
     * create a lens with a maximum magnification factor of 2.0
     */
    public DInverseCosineLens(){
	super();
	initTimer();
    }

    /**
     * create a lens with a given maximum magnification factor
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     */
    public DInverseCosineLens(float mm){
	super(mm);
	dMM = MM;
	initTimer();
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public DInverseCosineLens(float mm, int outerRadius, int innerRadius){
	super(mm, outerRadius, innerRadius);
	dMM = MM;
	initTimer();
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
    public DInverseCosineLens(float mm, int outerRadius, int innerRadius, int x, int y){
	super(mm, outerRadius, innerRadius, x, y);
	dMM = MM;
	initTimer();
    }

    void initTimer(){
	timer = new Timer();
	mouseStillUpdater = new DICTrailingTimer(this);
	timer.scheduleAtFixedRate(mouseStillUpdater, 40, 10);
    }

    /**set the position of the lens inside the view
     *@param ax lens's center horizontal coordinate expressed as an absolute position within the view (JPanel coordinate system)
     *@param ay lens's center vertical coordinate expressed as an absolute position within the view (JPanel coordinate system)
     *@param absTime time at which this event is occuring (in ms, as can be obtained e.g. by System.currentTimeMillis())
     */
    public synchronized void setAbsolutePosition(int ax, int ay, long absTime){
	synchronized(this){
	    super.setAbsolutePosition(ax, ay);
	    updateFrequency(absTime);
	    updateTimeBasedParams(ax, ay);
	}
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

    public void updateTimeBasedParams(int cx, int cy){
	parentPos.setLocation(cx, cy);
	updateTimeBasedParams();
    }

    float mindMM = 1.0f;

    public void updateTimeBasedParams(){
	synchronized(this){
	    targetPos.setLocation(parentPos.getX() + xOffset, parentPos.getY() + yOffset);
	    double distAway = targetPos.distance(currentPos);
	    double opacity = 1.0 - Math.min(1.0, distAway / maxDist);
	    filter.setCutOffFrequency(((1.0 - opacity) * cutoffParamA) +  cutoffParamB);
	    currentPos = filter.apply(targetPos, frequency);
	    int tx = (int)Math.round(currentPos.getX());
	    int ty = (int)Math.round(currentPos.getY());
	    tx = Math.max(tx, w/2);
	    ty = Math.min(ty, owningView.parent.getPanelSize().height - h/2);
	    float nMM = ((float)opacity) * (MM-mindMM) + mindMM;
	    if (Math.abs(dMM - nMM) > 0.1f){// avoid unnecesarry repaint requests
		// make the lens almost flat when making big moves
		dMM = nMM;
		this.setDynamicMagnification();
		owningView.parent.repaintNow();
	    }
	}
    }

    void setDynamicMagnification(){
	c = (2/(float)Math.PI)*(dMM-1);
	owningView.parent.repaintNow();
    }

    public void setCutoffFrequencyParameters(double a, double b){
	cutoffParamA = a;
	cutoffParamB = b;
    }

    public void setNoUpdateWhenMouseStill(boolean b){
	mouseStillUpdater.setEnabled(!b);
    }

    public void dispose(){
	super.dispose();
	timer.cancel();
    }
    
    public void gf(float x, float y, float[] g){
	d = Math.sqrt(Math.pow(x-sw-lx,2) + Math.pow(y-sh-ly,2));
	if (d <= LR2)
	    g[0] = g[1] = dMM;
	else if (d <= LR1)
	    g[0] = g[1] = dMM-c*(float)Math.acos(Math.pow(d*a+b-1,2));
	else
	    g[0] = g[1] = 1;
    }

    /**for internal use*/
    public void drawBoundary(Graphics2D g2d){
	// get the alpha composite from a precomputed list of values
	// (we don't want to instantiate a new AlphaComposite at each repaint request)
	g2d.setComposite(Translucency.acs[Math.round((dMM/((float)(1-MM)) + MM/((float)(MM-1)))*Translucency.ACS_ACCURACY)-1]);
	if (r1Color != null){
	    g2d.setColor(r1Color);
	    g2d.drawOval(lx+w/2-LR1, ly+h/2-LR1, 2*LR1, 2*LR1);
	}
	if (r2Color != null){
	    int r2 = Math.round(dMM/((float)MM) * LR2);
	    g2d.setColor(r2Color);
	    g2d.drawOval(lx+w/2-r2, ly+h/2-r2, 2*r2, 2*r2);
	}
	g2d.setComposite(Translucent.acO);
    }

    public float getActualMaximumMagnification(){
	return dMM;
    }

}


class DICTrailingTimer extends TimerTask {

    TemporalLens lens;
    private boolean enabled = true;

    DICTrailingTimer(TemporalLens l){
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
	    lens.updateTimeBasedParams();
	}
    }

}
