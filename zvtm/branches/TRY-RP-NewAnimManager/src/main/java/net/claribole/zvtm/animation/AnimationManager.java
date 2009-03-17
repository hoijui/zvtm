/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package net.claribole.zvtm.animation;

import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jdesktop.animation.timing.Animator;

public class AnimationManager {

    //should be called by VSM only, but for the time being
    //clients will create it manually (until we merge that change
    //with the trunk)
    public AnimationManager(){
	inactiveAnims = new LinkedList<Animation>();
	pendingAnims = new LinkedList<Animation>();
	runningAnims = new LinkedList<Animation>();
	listsLock = new ReentrantLock();
    }

    /**
     * Creates a new Animation object that will be handled 
     * by this AnimationManager.
     * @param duration duration of the animation, in milliseconds
     */
    public Animation createAnimation(int duration, 
				     double repeatCount, //opt
				     Animator.RepeatBehavior repeatBehavior, //opt
				     Object subject,
				     Animation.Dimension dimension,
				     TimingHandler handler){//interpolator opt
	return new Animation(this, duration, repeatCount,
			     repeatBehavior, subject,
			     dimension, handler);
    }

    /**
     * Enqueues an animation, needs to be done before starting it
     */
    public void addAnimation(Animation anim){
	listsLock.lock();
	try{
	    if((inactiveAnims.indexOf(anim) != -1) ||
	       (pendingAnims.indexOf(anim) != -1) ||
	       (runningAnims.indexOf(anim) != -1)) 
		return;

	    inactiveAnims.add(anim);
	} finally {
	    listsLock.unlock();
	}
    }

    //if "force" is true, the animation will cancel any previously running
    //animation that target the same dimension on the same object
    public void startAnimation(Animation anim, boolean force){
	//!!! force
	listsLock.lock();
	try{
	    if(inactiveAnims.indexOf(anim) == -1)
		return;
	    inactiveAnims.remove(anim);
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
    public void pauseAnimation(Animation anim){
	anim.pause();
    }

    //resuming an animation that is not paused has no effect
    public void resumeAnimation(Animation anim){
	anim.resume();
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

    //new animations are added here
    //the key (instance of Object) is the subject of the animation
    //(i.e. Glyph, Camera, Portal, DPath...). It is only used for identity.
    private final List<Animation> inactiveAnims;

    //animations are moved here when the user calls startAnimation()
    private final List<Animation> pendingAnims;

    //animations are moved here when they are running. Paused animations
    //also stay here (ie a paused animation still prevents candidates 
    //from running)
    private final List<Animation> runningAnims;

    private final Lock listsLock;

}