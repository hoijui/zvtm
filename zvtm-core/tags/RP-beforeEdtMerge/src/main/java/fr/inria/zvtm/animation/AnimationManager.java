/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$ 
 */ 
package fr.inria.zvtm.animation;

import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.jcip.annotations.*;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.Interpolator;
import org.jdesktop.animation.timing.TimingSource;

//for active Camera animation
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Camera;

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
    public AnimationManager(VirtualSpaceManager vsm){
	pendingAnims = new LinkedList<Animation>();
	runningAnims = new LinkedList<Animation>();
	listsLock = new ReentrantLock();
	tickThread = new TickThread("tickThread");
	animationFactory = new AnimationFactory(this);
	started = new AtomicBoolean(false);

	//vsm is only useful for currentCamAnim
	currentCamAnim = new InteractiveCameraAnimation(vsm);
	Animation anim = createAnimation(Animator.INFINITE, 1d,
					 Animation.RepeatBehavior.LOOP,
					 currentCamAnim, //DUMMY subject: avoids conflicts
					 Animation.Dimension.POSITION,
					 currentCamAnim);
	startAnimation(anim, true);
    }

    Animation createAnimation(int duration, 
			      double repeatCount, 
			      Animation.RepeatBehavior repeatBehavior,
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

    Animation createAnimation(int duration, 
			      double repeatCount, 
			      Animation.RepeatBehavior repeatBehavior,
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
     * Starts this AnimationManager.
     * This method must be called for animations to start.
     * This method may be called multiple times from multiple threads;
     * only the first one will be taken in consideration, and subsequent 
     * calls will have no effect.
     */
    public void start(){
	if(started.compareAndSet(false, true)){
	    tickThread.start();
	}
    }

    /**
     * Stops this AnimationManager
     * After calling this method, all animations handled by this AnimationManager will stop.
     * This method must be called once and only once, otherwise AnimationManager
     * will complain loudly (i.e. throw an IllegalThreadStateException).
     * This AnimationManager should not be used after stop() has been called.
     *
     * @throws java.lang.IllegalStateException if called more than once
     */
    public void stop(){
	tickThread.requestStop();
    }

    /**
     * Returns the AnimationFactory associated with this AnimationManager
     */
    public AnimationFactory getAnimationFactory(){
	return animationFactory;
    }

    /**
     * Starts an animation.
     * @param anim animation to start (must have been created by 
     * calling createAnimation on the same AnimationManager).
     * @param force if true, any previously started and conflicting 
     * animations will be cancelled. Queued conflicting animations 
     * that did not yet start are cancelled. Two animations conflict if
     * they target the same subject and animate the same dimension.
     * Note that the end() action of a cancelled animation will <b>not</b>
     * be executed.
     */
    public void startAnimation(Animation anim, boolean force){
	listsLock.lock();
	try{
	    if(force){
		cancelConflictingAnimations(anim);
	    }

	    pendingAnims.add(anim);
	    
	    //*moves and starts* eligible animations
	    startEligibleAnimations();
	} finally {
	    listsLock.unlock();
	}
    }

    /**
     * Stops an animation.
     * The end() action of an animation will be executed if the animation has
     * been started beforehand.
     * @param anim animation to stop (must have been created by 
     * calling createAnimation on the same AnimationManager).
     */
    public void stopAnimation(Animation anim){
	listsLock.lock();
	try{
	    if(pendingAnims.remove(anim)){
		//XXX try to reunite with TimingInterceptor.end()?
		anim.handler.end(anim.subject, anim.dimension);
		return;
	    }

	    if(runningAnims.indexOf(anim) == -1)
		return;

	    anim.stop();
	    runningAnims.remove(anim);
	} finally {
	    listsLock.unlock();
	}
    }

    /**
     * Cancels an animation.
     * @param anim animation to cancel (must have been created by 
     * calling createAnimation on the same AnimationManager).
     * Note that the end() action of a cancelled animation will not
     * be executed.
     */
    public void cancelAnimation(Animation anim){
	listsLock.lock();
	try{
	    if(pendingAnims.remove(anim))
		return;

	    if(runningAnims.indexOf(anim) == -1)
		return;

	    anim.cancel();
	    runningAnims.remove(anim);
	    startEligibleAnimations();
	} finally {
	    listsLock.unlock();
	}
    }

    /**
     * Pauses an animation. An animation that is paused
     * still prevents conflicting animations to run.
     * @param anim animation to pause
     * @return true if the animation was actually paused (ie was running
     * prior to the user call), false otherwise.
     */
    public boolean pauseAnimation(Animation anim){
	listsLock.lock();
	try{
	    if(anim.isRunning()){
		anim.pause();
		return true;
	    }
	    return false;
	} finally {
	    listsLock.unlock();
	}
    }

    /**
     * Resumes an animation.
     * @param anim animation to resume
     * @return true if the animation was actually resumed, false otherwise.
     */
    public boolean resumeAnimation(Animation anim){
	listsLock.lock();
	try{
	    anim.resume();
	    return anim.isRunning();
	} finally {
	    listsLock.unlock();
	}
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

    /**
     * Sets the active camera X speed.
     * @param dx active camera X speed
     */
    public void setXspeed(double dx){
	currentCamAnim.setXspeed(dx);
    }

    /**
     * Sets the active camera Y speed
     * @param dy active camera Y speed
     */
    public void setYspeed(double dy){
	currentCamAnim.setYspeed(dy);
    }

    /**
     * Sets the active camera Z speed
     * @param dz active camera Z speed
     */
    public void setZspeed(float dz){
	currentCamAnim.setZspeed(dz);
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

    private void cancelConflictingAnimations(Animation anim){
	listsLock.lock();
	try{
	    //any number of pending animations may conflict with anim.
	    //remove them
	    List<Animation> remove = new LinkedList<Animation>();
	    for(Animation pending: pendingAnims){
		if(!anim.orthogonalWith(pending)){
		    remove.add(pending);
		}
	    }
	    pendingAnims.removeAll(remove);

	    //at most one running animation may conflict with anim
	    //(class invariant). cancel it.
	    Animation cancel = null;
	    for(Animation running: runningAnims){
		if(!anim.orthogonalWith(running)){
		    assert(null == cancel);
		    cancel = running;
		}
	    }
	    
	    if(null != cancel){
		cancel.cancel();
		runningAnims.remove(cancel);
	    }
	    
	} finally {
	    listsLock.unlock();
	}
    }

    //animations are added here when the user calls startAnimation()
    @GuardedBy("listsLock") private final List<Animation> pendingAnims;

    //animations are moved here when they are running. Paused animations
    //also stay here (ie a paused animation still prevents conflicting 
    //candidates from running)
    @GuardedBy("listsLock") private final List<Animation> runningAnims;

    private final Lock listsLock;

    private final TickThread tickThread;

    private final AnimationFactory animationFactory;

    //infinite animation that handles the current camera x-y-speed and altitude
    //typically used to implement user-directed camera movement (mouse handlers...)
    private final InteractiveCameraAnimation currentCamAnim; 

    private final AtomicBoolean started;

    /**
     * A class that represents an indefinite, interactive
     * camera animation. The user interacts with this 
     * animation by providing instantaneous camera speeds.
     */
    class InteractiveCameraAnimation extends DefaultTimingHandler {
	InteractiveCameraAnimation(VirtualSpaceManager vsm){
	    this.vsm = vsm;
	    dx = 0d;
	    dy = 0d;
	    dz = 0f;
	}

	@Override public void timingEvent(float fraction, 
					  Object subject, 
					  Animation.Dimension dim){
	    Camera cam = vsm.getActiveCamera();
	    if(null != cam){
		if((dx != 0) || (dy != 0)){
		    cam.move(dx, dy);
		}

		if(dz != 0){
		    cam.altitudeOffset(dz);
		}
	    }
	}

	public void setXspeed(double dx){
	    this.dx = dx;
	}

	public void setYspeed(double dy){
	    this.dy = dy;
	}

	public void setZspeed(float dz){
	    this.dz = dz;
	}

	public double getXspeed(){
	    return dx;
	}

	public double getYspeed(){
	    return dy;
	}

	public float getZspeed(){
	    return dz;
	}

	private final VirtualSpaceManager vsm;
	private volatile double dx;
	private volatile double dy;
	private volatile float dz;
    }

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

@ThreadSafe
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

