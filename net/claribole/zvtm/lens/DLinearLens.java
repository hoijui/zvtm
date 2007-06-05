/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package net.claribole.zvtm.lens;

import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;

import net.claribole.zvtm.engine.LowPassFilter;

/**Profile: linear - Distance metric: L(2) (circular shape)<br>Size expressed as an absolute value in pixels*/

public class DLinearLens extends FSLinearLens implements TemporalLens {

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
    DTrailingTimer mouseStillUpdater;

    double cutoffParamA = 1.5;   // 0.8
    double cutoffParamB = 0.1;  // 0.1 to make it more difficult to acquire

    float lMM;
    float TMM = 4.0f;

    /**
     * create a lens with a maximum magnification factor of 2.0
     */
    public DLinearLens(){
	super();
	TMM = MM;
	initTimer();
    }

    /**
     * create a lens with a given maximum magnification factor
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     */
    public DLinearLens(float mm, float tmm){
	super(mm);
	lMM = MM;
	TMM = tmm;
	initTimer();
    }

    /**
     * create a lens with a given maximum magnification factor, inner and outer radii
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     *@param outerRadius outer radius (beyond which no magnification is applied - outward)
     *@param innerRadius inner radius (beyond which maximum magnification is applied - inward)
     */
    public DLinearLens(float mm, float tmm, int outerRadius, int innerRadius){
	super(mm, outerRadius, innerRadius);
	lMM = MM;
	TMM = tmm;
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
    public DLinearLens(float mm, float tmm, int outerRadius, int innerRadius, int x, int y){
	super(mm, outerRadius, innerRadius, x, y);
	lMM = MM;
	TMM = tmm;
	initTimer();
    }

    void initTimer(){
	timer = new Timer();
	mouseStillUpdater = new DTrailingTimer(this);
	timer.scheduleAtFixedRate(mouseStillUpdater, 40, 40);
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
	System.err.println("++"+TMM);
	float nMM = ((float)opacity) * TMM-1 + 1;
	if (Math.abs(lMM - nMM) > 0.01f){// avoid unnecesarry repaint requests
	    // make the lens almost flat when making big moves
	    System.err.println(nMM);
	    this.setMaximumMagnification(nMM, false);
	    lMM = nMM;
	    owningView.parent.repaintNow();
	}
	}
    }

    public void setNoUpdateWhenMouseStill(boolean b){
	mouseStillUpdater.setEnabled(!b);
    }

    public void dispose(){
	super.dispose();
	timer.cancel();
    }
    
    public void setTrueMaximumMagnification(float tmm){
	this.TMM = tmm;
    }

}


class DTrailingTimer extends TimerTask {

    DLinearLens lens;
    private boolean enabled = true;

    DTrailingTimer(DLinearLens l){
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
