package net.claribole.zvtm.animation;

import java.util.List;

public class AnimationManager {

    //our QueuedAnimations are Animators that are limited to 
    //a single target
    public void addAnimation(Animation anim){
    }

    //throws exception if animation was not previously added
    //if "force" is true, the animation will cancel any previously running
    //animation that target the same dimension on the same object
    public void startAnimation(Animation anim, boolean force){
    }

    //throws exception if animation was not previously added
    //the "end" action will be executed
    public void stopAnimation(Animation anim){
    }

    //cancel means the "end" action will not be executed
    public void cancelAnimation(Animation anim){
    }

    public void pauseAnimation(Animation anim){
    }

    public void resumeAnimation(Animation anim){
    }

    //new animations are added here
    //the key (instance of Object) is the subject of the animation
    //(i.e. Glyph, Camera, Portal, DPath...). It is only used for identity.
    private List<Animation> inactiveAnims;

    //animations are moved here when the user calls startAnimation()
    private List<Animation> pendingAnims;

    //animations are moved here when they are running. Paused animations
    //also stay here (ie a paused animation still prevents candidates 
    //from running)
    private List<Animation> runningAnims;

    //add a lock (?)

}