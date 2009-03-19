/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package net.claribole.zvtm.animation;

import org.jdesktop.animation.timing.interpolation.Interpolator;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;

/**
 * An animator that has a single timing target.
 * Animations should be started, stopped or paused from the AnimationManager.
 * An Animation has a subject (object that will be animated e.g. Glyph,
 * Camera, Portal...) and a dimension (characteristic that will be animated
 * e.g. position, color, altitude...)
 *
 * @see TimingHandler
 * @author Romain Primet
 */
public class Animation {

    public static enum Dimension {POSITION, ALTITUDE, SIZE,                  
	    BORDERCOLOR, FILLCOLOR, TRANSLUCENCY, PATH};    
    
    //package-level ctor, to be used from AnimationManager
    //(not publicly visible)
    Animation(AnimationManager parent,
	      int duration, 
	      double repeatCount, 
	      Animator.RepeatBehavior repeatBehavior, 
	      Object subject,
	      Dimension dimension,
	      TimingHandler handler){
	this.parent = parent;
	this.subject = subject;
	this.dimension = dimension;
	this.handler = handler;

	timingInterceptor = new TimingInterceptor(this);
	animator = new Animator(duration, repeatCount, repeatBehavior, timingInterceptor);
    }

    public void setInterpolator(Interpolator interpolator){
	animator.setInterpolator(interpolator);
    }

    /**
     * Sets the initial fraction at which the first animation cycle will begin. The default value is 0.
     * @param startFraction initial fraction.
     * @throws IllegalArgumentException if startFraction is less than 0 or greater than 1 
     * @throws IllegalStateException if animation is already running; this parameter may only 
     * be changed prior to starting the animation or after the animation has ended
     */
    public void setStartFraction(float startFraction){
	animator.setStartFraction(startFraction);
    }
    
    /**
     * Two or more Animations can be run concurrently if and only if they
     * are orthogonal. Two Animations are said to be orthogonal if they either
     * target different subjects or have different dimensions.
     *
     * Non-orthogonal animations get queued and run in the order in which
     * they were started.
     */
    boolean orthogonalWith(Animation other){                      
     return ( !((subject == other.subject) &&                            
                (dimension == other.dimension)) );                       
    }   

    void start(){
	animator.start();
    }

    void stop(){
	animator.stop();
    }

    void cancel(){
	animator.cancel();
    }

    void pause(){
	animator.pause();
    }

    void resume(){
	animator.resume();
    }

    //called back from timingInterceptor, propagate to parent AnimManager
    void onAnimationEnded(){
	parent.onAnimationEnded(this);
    }

    //note to self: do *not* provide addTarget, removeTarget
    //Also, start(), cancel(), stop()... should have package access
    private final AnimationManager parent;

    private final Animator animator;

    //intercepts TimingTarget events and propagates them to
    //TimingHandlers and to the parent class for queuing and
    //housekeeping tasks
    private final TimingTarget timingInterceptor; 

    //real, destination handler that is provided by client code
    TimingHandler handler;

    //object that gets animated, e.g. Glyph, Camera, Portal...
    final Object subject;

    //characteristic of the animation e.g. color, altitude, position, size...
    final Dimension dimension;
}

//This class is not really conceptually separate from "Animation",
//it is mailny a trick to avoid exposing its TimingTarget inheritance
//to clients (and providing public callback methods, et caetera).
class TimingInterceptor implements TimingTarget {
    TimingInterceptor(Animation parent){
	this.parent = parent;
    }

    public void begin(){
	parent.handler.begin(parent.subject, parent.dimension);
    }

    public void end(){
	parent.handler.end(parent.subject, parent.dimension);

	//should do useful stuff like queue management
	//note that the order of call is important: the end action of
	//the animation should have ended before we process the animation
	//queue, and potentially run a new animation
	parent.onAnimationEnded();
    }

    public void repeat(){
	parent.handler.repeat(parent.subject, parent.dimension);
    }

    public void timingEvent(float fraction){
	parent.handler.timingEvent(fraction, parent.subject, parent.dimension);
    }

    private Animation parent;
}