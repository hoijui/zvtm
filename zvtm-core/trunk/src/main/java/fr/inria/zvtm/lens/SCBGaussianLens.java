/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: SCBGaussianLens.java 4912 2013-02-07 20:16:30Z epietrig $
 */

package fr.inria.zvtm.lens;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.Color;

import java.util.Timer;
import java.util.TimerTask;

import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.glyphs.Translucency;
import fr.inria.zvtm.engine.LowPassFilter;
import fr.inria.zvtm.engine.SpeedCoupling;

/**Profile: gaussian - Distance metric: L(2) (circular shape) - Dissapear when moving fast<br>Size expressed as an absolute value in pixels*/

public class SCBGaussianLens extends BGaussianLens implements TemporalLens {

    protected double frequency = -1;
    protected long mLastSampleTime = -1;
    protected int xOffset = -10;
    protected int yOffset = 10;
    protected double maxDist = 2 * Math.abs(xOffset);
    protected LowPassFilter filter = new LowPassFilter();
    protected Point2D currentPos = new Point2D.Double(0, 0);
    protected Point2D parentPos = new Point2D.Double(0, 0);
    protected Point2D targetPos = new Point2D.Double(0, 0);
    protected Timer timer;
    protected DGTrailingTimer mouseStillUpdater;

    protected double cutoffParamA = 0.01;   // decrease to increase time before starts to go back to rest position
    protected double cutoffParamB = 0.05;  // increase to lower time to go back to rest position

    protected SpeedCoupling speedCoupling = new SpeedCoupling();

    //protected SpeedCoupling speedCoupling = null;

    /** Dynamic magnification factor. */
    protected float dMM = MM;

    protected float maxTranslucency = 1;

    /** bracelet pseudo dMM **/
    protected float scRingRadius = LR2;
    protected boolean doRing = false;
    protected boolean doDrawRing = false;

    TemporalParamListener tpl;

    float a = 1;
    float b = 0;

    /**
     * create a lens with a maximum magnification factor of 2.0
     */
    public SCBGaussianLens(){
    super();
    initTimer();
    }

    /**
     * create a lens with a given maximum magnification factor
     *
     *@param mm maximum magnification factor, mm in [0,+inf[
     */
    public SCBGaussianLens(float mm){
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
    public SCBGaussianLens(float mm, float tc, float tf, int outerRadius, int innerRadius){
    super(mm, tc, tf, outerRadius, innerRadius);
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
    public SCBGaussianLens(float mm, float tc, float tf, int outerRadius, int innerRadius, int x, int y){
    super(mm, tc, tf, outerRadius, innerRadius, x, y);
    dMM = MM;
    initTimer();
    }

    void initTimer(){
    speedCoupling.setCoefParameters(0.15f,0f);
    speedCoupling.setSpeedParameters(75f);
    speedCoupling.setTimeParameters(200,500);
	timer = new Timer();
	mouseStillUpdater = new DGTrailingTimer(this);
	timer.scheduleAtFixedRate(mouseStillUpdater, 40, 10);
    }

    void computeOpacityFactors(float minT, float maxT){
    a = maxT - minT;
    b = minT;
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
	    if (speedCoupling != null)
	    {
		speedCoupling.addPoint(ax, ay, absTime);
	    }
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

    protected float mindMM = 1.0f;

    public void updateTimeBasedParams(){
	synchronized(this){
	    double opacity;
        if (speedCoupling != null)
        {
            opacity = 1.0 - (double)speedCoupling.getCoef();
        }
        else
        {
            targetPos.setLocation(parentPos.getX() + xOffset, parentPos.getY() + yOffset);
            double distAway = targetPos.distance(currentPos);
            opacity = 1.0 - Math.min(1.0, distAway / maxDist);
            filter.setCutOffFrequency(((1.0 - opacity) * cutoffParamA) +  cutoffParamB);
            currentPos = filter.apply(targetPos, frequency);
            int tx = (int)Math.round(currentPos.getX());
            int ty = (int)Math.round(currentPos.getY());
            tx = Math.max(tx, w/2);
            ty = Math.min(ty, owningView.parent.getPanelSize().height - h/2);
        }
        float nMMTf = ((float)opacity) * a + b;
        if (Math.abs(MMTf - nMMTf) > 0.01f){
            // avoid unnecesarry repaint requests
            // make the lens almost disappear when making big moves
            MMTf = nMMTf;
            this.setDynamicTranslucency();
            owningView.parent.repaint();
            if (tpl != null){tpl.parameterUpdated();}
        }

        dMM = ((float)opacity) * (MM-mindMM) + mindMM;
        if (doRing) {
            float bR = Math.min(LR2, ((float)opacity) * (LR2) + 1.0f);
            if (Math.abs(bR - scRingRadius) > 1.0f){
                scRingRadius = bR;
                owningView.parent.repaint();
            }
        }
	}
    }

    void setDynamicTranslucency(){
	cT = (MMTf-MMTc)/2;
    eT = (MMTf+MMTc)/2;
	owningView.parent.repaint();
    }

    public void setCutoffFrequencyParameters(double a, double b){
	cutoffParamA = a;
	cutoffParamB = b;
    }

    public void setSpeedCoupling(SpeedCoupling sc){
	speedCoupling = sc;
    }

    public void setNoUpdateWhenMouseStill(boolean b){
	mouseStillUpdater.setEnabled(!b);
    }

    public void dispose(){
	super.dispose();
	timer.cancel();
    }

    public void gf(float x, float y, float[] g){
	d = Math.sqrt((x-sw-lx)*(x-sw-lx) + (y-sh-ly)*(y-sh-ly));
	if (d <= LR2)
	    g[0] = g[1] = dMM;
	else if (d <= LR1)
	    g[0] = g[1] = (float)(cT * Math.cos(aT*d+bT) + eT);
	else
	    g[0] = g[1] = 1;
    }

    /**
     * set the minimum magnification factor (1.0 by default)
     *
     *@param minMagFac minimum magnification factor, in [1.0,MaxMag]
     */
    public void setMinMagFactor(float minMagFac){
	minMagFac = (minMagFac < 1.0f) ? 1.0f:minMagFac;
	mindMM = minMagFac;
    }

    public void setMaxTranslucency(float maxAlpha)
    {
        computeOpacityFactors(0, maxAlpha);
        maxTranslucency = maxAlpha;
    }

    public void setDoRing(boolean b){
	doRing = b;
    }
    public boolean getDoRing(){
	return doRing;
    }
    public void setDoDrawRing(boolean b){
	doDrawRing = b;
    }

    boolean doDrawMaxFlatTop = false;
    /**
     * allows to ask to draw the max flat top (not rawn by default)
     *
     *@param b if true ask to draw the max flat top
     */
    public void setDrawMaxFlatTop(boolean b){
	doDrawMaxFlatTop = b;
    }

    boolean doSpeedBlendOuterRadius = true;
    boolean doSpeedBlendFlatTop = true;
    /**
     * allows to ask to draw the max flat top
     *
     *@param bft if true speed blend the flat top
     *@param bor if true speed blend the outer radius
     */
    public void setSpeedBlendRadii(boolean bft, boolean bor){
	doSpeedBlendFlatTop = bft;
	doSpeedBlendOuterRadius = bor;
    }

    /**for internal use*/
    public void drawBoundary(Graphics2D g2d){
        // get the alpha composite from a precomputed list of values
        // (we don't want to instantiate a new AlphaComposite at each repaint request)
        Color bColor = null;
        Color rColor = null;
	   if (bColor != null){
            g2d.setColor(bColor);
            g2d.drawOval(lx+w/2-lensWidth/2, ly+h/2-lensHeight/2, lensWidth, lensHeight);
        }
        if (rColor != null){
            //XXX: the following two values could actually be computed less frequently, just when MM of lens{Width,Height} change
            lensProjectedWidth = Math.round(lensWidth/MM);
            lensProjectedHeight = Math.round(lensHeight/MM);
            g2d.setColor(rColor);
            // get the alpha composite from a precomputed list of values
            // (we don't want to instantiate a new AlphaComposite at each repaint request)
            g2d.setComposite(Translucency.acs[Math.round((1.0f-MMTf)*Translucency.ACS_ACCURACY)]);
            g2d.drawOval(lx+w/2-lensProjectedWidth/2, ly+h/2-lensProjectedHeight/2, lensProjectedWidth, lensProjectedHeight);
            g2d.setComposite(Translucent.acO);
        }
    if (doRing && rColor != null) {
        g2d.setColor(rColor);
        float dbr = Math.max(lensProjectedWidth/2.0f,scRingRadius-2.0f);
            g2d.drawOval(lx+w/2 - (int)dbr, ly+h/2 - (int)dbr,
             2*(int)dbr, 2*(int)dbr);
    }
    }

    public float getActualMaximumMagnification(){
	return dMM;
    }

    public float getActualRingRadius(){
	return scRingRadius;
    }

    /** To be notified about updates to MM due to speed-coupling. */
    public void setTemporalParamListener(TemporalParamListener tpl){
        this.tpl = tpl;
    }

    public TemporalParamListener getTemporalParamListener(){
        return this.tpl;
    }

}


class BGTrailingTimer extends TimerTask {

    TemporalLens lens;
    private boolean enabled = true;

    BGTrailingTimer(TemporalLens l){
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
