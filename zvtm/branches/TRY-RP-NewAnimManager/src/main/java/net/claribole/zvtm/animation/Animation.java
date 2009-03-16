package net.claribole.zvtm.animation;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;

/**
 * An animator that has a single timing target.
 * Animations should be started, stopped or paused from the AnimationManager.
 */
public class Animation {
    
    public Animation(int duration, 
		     double repeatCount, 
		     Animator.RepeatBehavior repeatBehavior, 
		     TimingSubject subject){
	animator = new Animator(duration, repeatCount, repeatBehavior, subject);
    }

    public Animation(int duration, TimingSubject subject){
	animator = new Animator(duration, subject);
    }

    public boolean orthogonalWith(Animation other){
	return subject.orthogonalWith(other.subject);
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

    //note to self: do *not* provide addTarget, removeTarget
    //Also, start(), cancel(), stop()... should have package access

    private final Animator animator;
    private TimingSubject subject; //this implies some redundancy, so a better
    //solution should be found (...)
}
