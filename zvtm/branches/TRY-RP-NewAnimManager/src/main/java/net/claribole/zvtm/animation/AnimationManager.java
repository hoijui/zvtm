package net.claribole.zvtm.animation;

import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AnimationManager {

    //should be called by VSM only
    public AnimationManager(){
	inactiveAnims = new LinkedList();
	pendingAnims = new LinkedList();
	runningAnims = new LinkedList();
	listsLock = new ReentrantLock();
    }

    //our QueuedAnimations are Animators that are limited to 
    //a single target
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

    //throws exception if animation was not previously added
    //if "force" is true, the animation will cancel any previously running
    //animation that target the same dimension on the same object
    public void startAnimation(Animation anim, boolean force){
	listsLock.lock();
	try{
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
	    if(runningAnims.indexOf(anim) != -1)
		return;

	    anim.stop();
	    runningAnims.remove(anim);
	    startEligibleAnimations();
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

    private void startEligibleAnimations(){
	listsLock.lock();
	try{
	    //XXX lists may not be the best data structure for
	    //queued animations, perhaps a hash table is better

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