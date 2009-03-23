/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package net.claribole.zvtm.animation;

import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.Interpolator;
import org.jdesktop.animation.timing.TimingSource;

/**
 * A class that manages Animation instances.
 * @author Romain Primet
 */
public class AnimationManager {

    /**
     * Creates a new AnimationManager. Clients are expected to retrieve an existing AnimationManager
     * by calling the appropriate operation on their application's VirtualSpaceManager, and
     * not to create an AnimationManager themselves.
     */
    public AnimationManager(){
	pendingAnims = new LinkedList<Animation>();
	runningAnims = new LinkedList<Animation>();
	listsLock = new ReentrantLock();
	tickThread = new TickThread("tickThread");
    }

    /**
     * Starts this AnimationManager.
     * This method must be called for animations to start.
     * This method must be called once and only once, otherwise AnimationManager
     * will complain loudly (i.e. throw an IllegalThreadStateException).
     *
     * @throws java.lang.IllegalStateException if called more than once
     */
    public void start(){
	tickThread.start();
    }

    /**
     * Stops this AnimationManager
     * After calling this method, all animations handled by this AnimationManager will stop.
     * This method must be called once and only once, otherwise AnimationManager
     * will complain loudly (i.e. throw an IllegalThreadStateException).
     *
     * @throws java.lang.IllegalStateException if called more than once
     */
    public void stop(){
	tickThread.requestStop();
    }

    /**
     * Creates a new Animation object that will be handled 
     * by this AnimationManager.
     * @param duration duration of the animation, in milliseconds
     * @param subject object that will be animated
     * @param dimension dimension of the animation
     * @param handler timing handler that will receive callbacks 
     * for each animation event. The handler is responsible for 
     * implementing the animation code itself (e.g. move a Camera or
     * change the color of a Glyph).
     */
    public Animation createAnimation(int duration, 
				     Object subject,
				     Animation.Dimension dimension,
				     TimingHandler handler){
	return createAnimation(duration, 1.0, Animator.RepeatBehavior.LOOP,
			       subject, dimension, handler);
    }

    /**
     * Creates a new Animation object that will be handled 
     * by this AnimationManager.
     * @param duration duration of the animation, in milliseconds
     */
    public Animation createAnimation(int duration, 
				     double repeatCount, 
				     Animator.RepeatBehavior repeatBehavior,
				     Object subject,
				     Animation.Dimension dimension,
				     TimingHandler handler){
	Animation retval =  new Animation(this, duration, repeatCount,
					  repeatBehavior, subject,
					  dimension, handler);
	retval.setTimer(new TickSource(tickThread));
	return retval;
    }

    /**
     * Creates a new Animation object that will be handled 
     * by this AnimationManager.
     * @param duration duration of the animation, in milliseconds
     */
    public Animation createAnimation(int duration, 
				     double repeatCount, 
				     Animator.RepeatBehavior repeatBehavior,
				     Object subject,
				     Animation.Dimension dimension,
				     TimingHandler handler,
				     Interpolator interpolator){
	Animation retval = new Animation(this, duration, repeatCount,
					 repeatBehavior, subject,
					 dimension, handler);
	retval.setTimer(new TickSource(tickThread));
	retval.setInterpolator(interpolator);
	return retval;
    }

    /**
     * Starts an animation.
     * @param anim animation to start (must have been created by 
     * calling createAnimation on the same AnimationManager).
     * @param force if true, any previously started and conflicting 
     * animations will be cancelled. Queued conflicting animations 
     * that did not yet start are cancelled. Two animations conflict if
     * they target the same subject and animate the same dimension.
     */
    public void startAnimation(Animation anim, boolean force){
	//XXX implement forced startAnimation
	listsLock.lock();
	try{
	    pendingAnims.add(anim);
	    
	    //*moves and starts* eligible animations
	    startEligibleAnimations();
	} finally {
	    listsLock.unlock();
	}
    }

    //throws exception if animation was not previously added
    //the "end" action will be executed
    //No effect if "anim" is not running
    public void stopAnimation(Animation anim){
	listsLock.lock();
	try{
	    if(runningAnims.indexOf(anim) == -1)
		return;

	    anim.stop();
	    runningAnims.remove(anim);
	} finally {
	    listsLock.unlock();
	}
    }

    //cancel means the "end" action will not be executed
    public void cancelAnimation(Animation anim){
	listsLock.lock();
	try{
	    if(runningAnims.indexOf(anim) != -1)
		return;

	    anim.cancel();
	    runningAnims.remove(anim);
	    startEligibleAnimations();
	} finally {
	    listsLock.unlock();
	}
    }

    //pausing a non-running animation has no effect
    /**
     * Pauses an animation. An animation that is paused
     * still prevents conflicting animations to run.
     */
    public void pauseAnimation(Animation anim){
	anim.pause();
    }

    //resuming an animation that is not paused has no effect
    /**
     * Resumes an animation.
     */
    public void resumeAnimation(Animation anim){
	anim.resume();
    }

    /**
     * Sets the timing events period, in milliseconds. 
     * This is not a guaranteed resolution but rather a minimal value.
     * @param resolution minimal tick interval, in milliseconds.
     * All animations handled by this AnimationManager share the same
     * resolution.
     */
    public void setResolution(int resolution){
	tickThread.setResolution(resolution);
    }

    void onAnimationEnded(Animation anim){
	listsLock.lock();
	try{
	    //remove animation from the running list
	    assert(runningAnims.indexOf(anim) != -1);
	    runningAnims.remove(anim);

	    startEligibleAnimations();
	} finally {
	    listsLock.unlock();
	}
    }

    private void startEligibleAnimations(){
	listsLock.lock();
	try{  
	    //XXX lists may not be the best data structure for
	    //queued animations, perhaps a hash table is better
	    List<Animation> transfer = new LinkedList<Animation>();
	    for(Animation pending: pendingAnims){
		boolean conflicts = false;
		for(Animation running: runningAnims){
		    if(!pending.orthogonalWith(running))
			conflicts = true;
		}
		for(Animation willRun: transfer){
		    if(!pending.orthogonalWith(willRun))
			conflicts = true;
		}
		if(!conflicts){
		    transfer.add(pending);
		}
	    }

	    pendingAnims.removeAll(transfer);
	    runningAnims.addAll(transfer);
	    for(Animation a: transfer){
		a.start();
	    }

	} finally {
	    listsLock.unlock();
	}
    }

    //animations are added here when the user calls startAnimation()
    private final List<Animation> pendingAnims;

    //animations are moved here when they are running. Paused animations
    //also stay here (ie a paused animation still prevents conflicting 
    //candidates from running)
    private final List<Animation> runningAnims;

    private final Lock listsLock;

    private final TickThread tickThread;
}

//A custom tick source to replace the one provided by the Timing Framework.
//NOTE: setResolution() has no effect (the resolution will always be the one supplied by the TickThread)
//Every animation has its own TickSource (that way they can be started or stopped independently)
//but all tick sources share the same TickThread (enforced by AnimationManager)
//@ThreadSafe
class TickSource extends TimingSource {
    private TickThread tickThread;
    
    public TickSource(TickThread tickThread){
	this.tickThread = tickThread;
    }
    
    public void setResolution(int resolution){
	//This has purposely no effect. Animation resolution is set globally,
	//see AnimationManager#setResolution.
    }

    public void setStartDelay(int delay){
	//This has purposely no effect. We do not currently provide a start
	//delay for animations, altough this could be done.
    }

    public void start(){
	tickThread.addSubscriber(this);
    }

    public void stop(){
	tickThread.removeSubscriber(this);
    }

    //called by TickThread instance, which needs at least package acccess
    void tick(){
	timingEvent();
    }
}

//@ThreadSafe
class TickThread extends Thread{
    private volatile boolean stopped = false;
    private AtomicInteger resolution; //milliseconds

    //receivers is traversed a *lot* more often than it is mutated
    private final List<TickSource> receivers = new CopyOnWriteArrayList<TickSource>();

    public TickThread(String name){
	super(name);

	resolution = new AtomicInteger(25);
    }

    public void setResolution(int res){
	resolution.set(res);
    }

    public void addSubscriber(TickSource ts){
	receivers.add(ts);
    }

    public void removeSubscriber(TickSource ts){
	receivers.remove(ts);
    }
	
    public void requestStop(){
	stopped = true;
    }

    public void run(){
	long startEventProcessing;
	long endEventProcessing;
	long NS_IN_MS = 1000000; //nanoseconds in a millisecond
	while(!stopped){
	    try{
		startEventProcessing = System.nanoTime();

		for(TickSource ts: receivers){
		    ts.tick();
		}

		endEventProcessing = System.nanoTime();

		sleep(Math.max(0,
			       resolution.get() - (endEventProcessing - startEventProcessing)/NS_IN_MS));
	    } catch(InterruptedException ex){
		//Swallowing this exception should be okay
		//because TickThread is only ever used by
		//AnimationManager, which will not require its
		//interruption (method stop provides this).
	    }
	}
    }
}

