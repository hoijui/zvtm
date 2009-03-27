/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package net.claribole.zvtm.animation;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.Interpolator;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;

/**
 * A class that provides creation methods for animations.
 * The createAnimation() methods are generic and flexible, while the
 * create[ObjectType][AnimationType] method are pre-made and easier to use
 * (but offer less flexibility and only cover the most common animation 
 * cases).
 */
public class AnimationFactory {

    //AnimationFactories are created by AnimationManagers
    AnimationFactory(AnimationManager am){
	animationManager = am;
    }

    /**
     * Creates a new Animation object that will be handled 
     * by the associated AnimationManager.
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
     * by the associated AnimationManager.
     * @param duration duration of the animation, in milliseconds
     * @param repeatCount the number of times this Animation will
     * be repeated. This is not necessarily an integer, i.e. an animation may
     * be repeated 2.5 times
     * @param repeatBehavior controls whether an animation loops or reverse
     * when repeating
     * @param subject object that will be animated
     * @param dimension dimension of the animation
     * @param handler timing handler that will receive callbacks 
     * for each animation event. The handler is responsible for 
     * implementing the animation code itself (e.g. move a Camera or
     * change the color of a Glyph).
     */
    public Animation createAnimation(int duration, 
				     double repeatCount, 
				     Animator.RepeatBehavior repeatBehavior,
				     Object subject,
				     Animation.Dimension dimension,
				     TimingHandler handler){
	Animation retval =  new Animation(animationManager, duration, repeatCount,
					  repeatBehavior, subject,
					  dimension, handler);
	return animationManager.createAnimation(duration, repeatCount, repeatBehavior,
						subject, dimension, handler);
    }

    /**
     * Creates a new Animation object that will be handled 
     * by this AnimationManager.
     * @param duration duration of the animation, in milliseconds
     * @param repeatCount the number of times this Animation will
     * be repeated. This is not necessarily an integer, i.e. an animation may
     * be repeated 2.5 times
     * @param repeatBehavior controls whether an animation loops or reverse
     * when repeating
     * @param subject object that will be animated
     * @param dimension dimension of the animation
     * @param handler timing handler that will receive callbacks 
     * for each animation event. The handler is responsible for 
     * implementing the animation code itself (e.g. move a Camera or
     * change the color of a Glyph).
     * @param interpolator an Interpolator, ie a functor that takes a float between 0 and 1
     * and returns a float between 0 and 1. By default a linear Interpolator is
     * used, but spline interpolators may be used to provide different animation
     * behaviors: slow in/slow out, fast in/slow out et caetera. You may also
     * provide your own interpolator.
     */
    public Animation createAnimation(int duration, 
				     double repeatCount, 
				     Animator.RepeatBehavior repeatBehavior,
				     Object subject,
				     Animation.Dimension dimension,
				     TimingHandler handler,
				     Interpolator interpolator){

	return animationManager.createAnimation(duration, repeatCount, repeatBehavior,
						subject, dimension, handler, interpolator);
    }

    /**
     * Creates and returns a linear camera translation that will
     * not repeat.
     * @param duration duration of the animation, in milliseconds.
     * @param subject camera to animate
     * @param data animation data, interpreted according to the
     * 'relative' boolean argument. If 'relative' is false, then
     * 'data' will be interpreted as absolute target coordinates, 
     * otherwise it will be interpreted as an offset.
     * @param endAction a functor that will be executed when the animation
     * ends. May be set to null, in which case it is ignored.
     */
    public Animation createCameraTranslation(final int duration, final Camera subject, 
					     final LongPoint data, final boolean relative,
					     final Interpolator interpolator,
					     final EndAction endAction){
	return createAnimation(duration, 1f, Animator.RepeatBehavior.LOOP,
			       subject,
			       Animation.Dimension.POSITION,
			       new DefaultTimingHandler(){
				   private final long startX = subject.getLocation().vx;
				   private final long startY = subject.getLocation().vy;
				   private final long endX = 
				       relative? subject.getLocation().vx + data.x : data.x;
				   private final long endY = 
				       relative? subject.getLocation().vy + data.y : data.y;

				   @Override
				   public void end(Object subject, Animation.Dimension dim){
				       if(null != endAction){
					   endAction.execute(subject, dim);
				       }
				   }

				   @Override
				   public void timingEvent(float fraction, 
							   Object subject, Animation.Dimension dim){
				       Camera cam = (Camera)subject;
				       cam.moveTo(startX + (long)(fraction*(endX - startX)),
						  startY + (long)(fraction*(endY - startY)));
				   }
			       },
			       interpolator);
    }
    
    /**
     * Creates and returns a linear camera altitude animation
     * that will not repeat.
     * @param duration duration of the animation, in milliseconds.
     * @param subject camera to animate
     * @param data animation data, interpreted according to the
     * 'relative' boolean argument. If 'relative' is false, then
     * 'data' will be interpreted as an absolute target altitude, 
     * otherwise it will be interpreted as an offset.
     * @param endAction a functor that will be executed when the animation
     * ends. May be set to null, in which case it is ignored.
     */
    public Animation createCameraAltAnim(final int duration, final Camera subject, 
					 final float data, final boolean relative,
					 final Interpolator interpolator,
					 final EndAction endAction){
	
	return createAnimation(duration, 1f, Animator.RepeatBehavior.LOOP,
			       subject,
			       Animation.Dimension.ALTITUDE,
			       new DefaultTimingHandler(){
				   private final float startZ = subject.getAltitude();
				   private final float endZ = 
				       relative? subject.getAltitude() + data : data;
				   private final Camera cam = subject;

				   @Override
				   public void end(Object subject, Animation.Dimension dim){
				       if(null != endAction){
					   endAction.execute(subject, dim);
				       }
				   }

				   @Override
				   public void timingEvent(float fraction, 
							   Object subject, Animation.Dimension dim){
				       cam.setAltitude(startZ + fraction*(endZ - startZ));
				   }
			       },
			       interpolator);
     }

//     public Animation createGlyphTranslation(){
// 	return null;
//     }

//     public Animation createGlyphSizeAnim(){
// 	//throw if this animation would cause the size to become negative
// 	return null;
//     }

//     public Animation createGlyphOrientationAnim(){
// 	return null;
//     }

    private final AnimationManager animationManager;
}