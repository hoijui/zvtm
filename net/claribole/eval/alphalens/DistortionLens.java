/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.eval.alphalens;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;

import net.claribole.zvtm.engine.LowPassFilter;

import com.xerox.VTM.glyphs.Translucent;
import net.claribole.zvtm.glyphs.Translucency;
import net.claribole.zvtm.lens.FSGaussianLens;
import net.claribole.zvtm.lens.TemporalLens;

/**Profile: gaussian - Distance metric: L(2) (circular shape)<br>Size expressed as an absolute value in pixels*/

public class DistortionLens extends FSGaussianLens implements TemporalLens {

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
    DLTrailingTimer mouseStillUpdater;

    double cutoffParamA = 1;
    double cutoffParamB = 0.1;

    // MMTf is used to hold the current translucence
    // a and b are used to convert the filter's opacity to values in the [minT, maxT] range
    float a = 1;
    float b = 0;

    protected float MMTf = 1.0f;

    Color rColor = Color.RED;

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     *@param x horizontal coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     *@param y vertical coordinate of the lens' center (as an offset w.r.t the view's center coordinates)
     */
    public DistortionLens(float mm, int outerRadius, int innerRadius, int x, int y){
	super(mm, outerRadius, innerRadius, x, y);
 	computeOpacityFactors(0, 1);
	initTimer();
    }

    void initTimer(){
	timer = new Timer();
	mouseStillUpdater = new DLTrailingTimer(this);
	timer.scheduleAtFixedRate(mouseStillUpdater, 40, 40);
    }

    void computeOpacityFactors(float minT, float maxT){
	a = maxT - minT;
	b = minT;
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

    public void updateTimeBasedParams(){
	targetPos.setLocation(parentPos.getX() + xOffset, parentPos.getY() + yOffset);
	double distAway = targetPos.distance(currentPos);
	double opacity = 1.0 - Math.min(1.0, distAway / maxDist);
	filter.setCutOffFrequency(((1.0 - opacity) * cutoffParamA) +  cutoffParamB);
	currentPos = filter.apply(targetPos, frequency);
	int tx = (int)Math.round(currentPos.getX());
	int ty = (int)Math.round(currentPos.getY());
	tx = Math.max(tx, w/2);
 	ty = Math.min(ty, getOwningView().getPanelSize().height - h/2);
	float nMMTf = ((float)opacity) * a + b;
	if (Math.abs(MMTf - nMMTf) > 0.01f){// avoid unnecesarry repaint requests
	    // make the lens almost disappear when making big moves
	    MMTf = nMMTf;
	    getOwningView().repaintNow();
	}
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

    /**set the position of the lens inside the view
     *@param ax lens's center horizontal coordinate expressed as an absolute position within the view (JPanel coordinate system)
     *@param ay lens's center vertical coordinate expressed as an absolute position within the view (JPanel coordinate system)
     *@param absTime time at which this event is occuring (in ms, as can be obtained e.g. by System.currentTimeMillis())
     */
    public synchronized void setAbsolutePosition(int ax, int ay, long absTime){
	super.setAbsolutePosition(ax, ay);
	updateFrequency(absTime);
	updateTimeBasedParams(ax, ay);
    }

    /**for internal use*/
    public void drawBoundary(Graphics2D g2d){
	if (rColor != null){
	    g2d.setColor(rColor);
	    // get the alpha composite from a precomputed list of values
	    // (we don't want to instantiate a new AlphaComposite at each repaint request)
	    g2d.setComposite(Translucency.acs[Math.round((1.0f-MMTf)*Translucency.ACS_ACCURACY)-1]);  
	    g2d.drawOval(lx+w/2-LR2, ly+h/2-LR2, 2*LR2, 2*LR2);
	    g2d.setComposite(Translucent.acO);
	}
    }

}

class DLTrailingTimer extends TimerTask {

    TemporalLens lens;
    private boolean enabled = true;

    DLTrailingTimer(TemporalLens l){
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
