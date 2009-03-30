/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package net.claribole.zvtm.animation;

import java.awt.Point;

import net.jcip.annotations.*;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.Interpolator;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.Translucent;

import net.claribole.zvtm.engine.Portal;

/**
 * A class that provides creation methods for animations.
 * The createAnimation() methods are generic and flexible, while the
 * create[ObjectType][AnimationType] method are pre-made and easier to use
 * (but offer less flexibility and only cover the most common animation 
 * cases).
 */
@Immutable
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
     * @param camera camera to animate
     * @param data animation data, interpreted according to the
     * 'relative' boolean argument. If 'relative' is false, then
     * 'data' will be interpreted as absolute target coordinates, 
     * otherwise it will be interpreted as an offset.
     * @param endAction a functor that will be executed when the animation
     * ends. May be set to null, in which case it is ignored.
     */
    public Animation createCameraTranslation(final int duration, final Camera camera, 
					     final LongPoint data, final boolean relative,
					     final Interpolator interpolator,
					     final EndAction endAction){
	return createAnimation(duration, 1f, Animator.RepeatBehavior.LOOP,
			       camera,
			       Animation.Dimension.POSITION,
			       new DefaultTimingHandler(){
				   private final long startX = camera.getLocation().vx;
				   private final long startY = camera.getLocation().vy;
				   private final long endX = 
				       relative? camera.getLocation().vx + data.x : data.x;
				   private final long endY = 
				       relative? camera.getLocation().vy + data.y : data.y;

				   @Override
				   public void end(Object subject, Animation.Dimension dim){
				       if(null != endAction){
					   endAction.execute(subject, dim);
				       }
				   }

				   @Override
				   public void timingEvent(float fraction, 
							   Object subject, Animation.Dimension dim){
				       camera.moveTo(startX + (long)(fraction*(endX - startX)),
						     startY + (long)(fraction*(endY - startY)));
				   }
			       },
			       interpolator);
    }
    
    /**
     * Creates and returns a linear camera altitude animation
     * that will not repeat.
     * @param duration duration of the animation, in milliseconds.
     * @param camera camera to animate
     * @param data animation data, interpreted according to the
     * 'relative' boolean argument. If 'relative' is false, then
     * 'data' will be interpreted as an absolute target altitude, 
     * otherwise it will be interpreted as an offset.
     * @param endAction a functor that will be executed when the animation
     * ends. May be set to null, in which case it is ignored.
     */
    public Animation createCameraAltAnim(final int duration, final Camera camera, 
					 final float data, final boolean relative,
					 final Interpolator interpolator,
					 final EndAction endAction){
	
	return createAnimation(duration, 1f, Animator.RepeatBehavior.LOOP,
			       camera,
			       Animation.Dimension.ALTITUDE,
			       new DefaultTimingHandler(){
				   private final float startZ = camera.getAltitude();
				   private final float endZ = 
				       relative? camera.getAltitude() + data : data;
		
				   @Override
				   public void end(Object subject, Animation.Dimension dim){
				       if(null != endAction){
					   endAction.execute(subject, dim);
				       }
				   }

				   @Override
				   public void timingEvent(float fraction, 
							   Object subject, Animation.Dimension dim){
				       camera.setAltitude(startZ + fraction*(endZ - startZ));
				   }
			       },
			       interpolator);
     }

    /**
     * Creates and returns a linear glyph translation
     * that will not repeat.
     * @param duration duration of the animation, in milliseconds.
     * @param glyph glyph to animate
     * @param data animation data, interpreted according to the
     * 'relative' boolean argument. If 'relative' is false, then
     * 'data' will be interpreted as absolute target coordinates, 
     * otherwise it will be interpreted as an offset.
     * @param endAction a functor that will be executed when the animation
     * ends. May be set to null, in which case it is ignored.
     */
    public Animation createGlyphTranslation(final int duration, final Glyph glyph,
					    final LongPoint data, final boolean relative,
					    final Interpolator interpolator,
					    final EndAction endAction){
	 return createAnimation(duration, 1f, Animator.RepeatBehavior.LOOP,
				glyph,
				Animation.Dimension.POSITION,
				new DefaultTimingHandler(){
				    private final long startX = glyph.getLocation().x;
				    private final long startY = glyph.getLocation().y;
				    private final long endX = 
					relative? glyph.getLocation().x + data.x : data.x;
				    private final long endY = 
					relative? glyph.getLocation().y + data.y : data.y;
			
				    @Override
				    public void end(Object subject, Animation.Dimension dim){
					if(null != endAction){
					    endAction.execute(subject, dim);
					}
				    }
				    
				    @Override
				    public void timingEvent(float fraction, 
							    Object subject, Animation.Dimension dim){
					glyph.moveTo(startX + (long)(fraction*(endX - startX)),
						     startY + (long)(fraction*(endY - startY)));
				    }
				    
				},
				interpolator);
     }

    public Animation createGlyphSizeAnim(final int duration, final Glyph glyph,
					 final float data, final boolean relative,
					 final Interpolator interpolator,
					 final EndAction endAction){
	final float startSize = glyph.getSize();
	final float endSize = relative? glyph.getSize() + data : data;
	
	//throw if this animation would cause the size to become negative
	if(endSize < 0f){
	    throw new IllegalArgumentException("Cannot animate a Glyph size to a negative value");
	}

	return createAnimation(duration, 1f, Animator.RepeatBehavior.LOOP,
			       glyph,
			       Animation.Dimension.SIZE,
			       new DefaultTimingHandler(){
				   @Override
				   public void end(Object subject, Animation.Dimension dim){
				       if(null != endAction){
					   endAction.execute(subject, dim);
				       }
				   }

				   @Override
				   public void timingEvent(float fraction, 
							   Object subject, Animation.Dimension dim){
				       glyph.sizeTo(startSize + fraction*(endSize - startSize));
				   }
			       },
			       interpolator);
     }

    public Animation createGlyphOrientationAnim(final int duration, final Glyph glyph,
						final float data, final boolean relative,
						final Interpolator interpolator,
						final EndAction endAction){
	return createAnimation(duration, 1f, Animator.RepeatBehavior.LOOP,
			       glyph,
			       Animation.Dimension.ORIENTATION,
			       new DefaultTimingHandler(){
				   private final float startAngle = glyph.getOrient();
				   private final float endAngle = 
				       relative? glyph.getOrient() + data : data;

				   @Override
				   public void end(Object subject, Animation.Dimension dim){
				       if(null != endAction){
					   endAction.execute(subject, dim);
				       }
				   }

				   @Override
				   public void timingEvent(float fraction, 
							   Object subject, Animation.Dimension dim){
				       glyph.orientTo(startAngle + fraction*(endAngle - startAngle));
				   }
			       });
	
    }

    public Animation createGlyphFillColorAnim(final int duration, final Glyph glyph,
					      final float[] data, final boolean relative,
					      final Interpolator interpolator,
					      final EndAction endAction){
	final float[] startColor = glyph.getHSVColor();
	return createAnimation(duration, 1f, Animator.RepeatBehavior.LOOP,
			       glyph,
			       Animation.Dimension.FILLCOLOR,
			       new DefaultTimingHandler(){
				   private final float startH = startColor[0];
				   private final float startS = startColor[1];
				   private final float startV = startColor[2];
				   private final float endH = 
				       relative? startH + data[0] : data[0];
				   private final float endS = 
				       relative? startS + data[1] : data[1];
				   private final float endV = 
				       relative? startV + data[2]: data[2];

				   @Override
				   public void end(Object subject, Animation.Dimension dim){
				       if(null != endAction){
					   endAction.execute(subject, dim);
				       }
				   }

				   @Override
				   public void timingEvent(float fraction, 
							   Object subject, Animation.Dimension dim){
				       glyph.setHSVColor(startH + fraction*(endH - startH),
							 startS + fraction*(endS - startS),
							 startV + fraction*(endV - startV));
				   }
			       });
    }

    public Animation createGlyphBorderColorAnim(final int duration, final Glyph glyph,
						final float[] data, final boolean relative,
						final Interpolator interpolator,
						final EndAction endAction){
	final float[] startColor = glyph.getHSVbColor();
	return createAnimation(duration, 1f, Animator.RepeatBehavior.LOOP,
			       glyph,
			       Animation.Dimension.BORDERCOLOR,
			       new DefaultTimingHandler(){
				   private final float startH = startColor[0];
				   private final float startS = startColor[1];
				   private final float startV = startColor[2];
				   private final float endH = 
				       relative? startH + data[0] : data[0];
				   private final float endS = 
				       relative? startS + data[1] : data[1];
				   private final float endV = 
				       relative? startV + data[2] : data[2];

				   @Override
				   public void end(Object subject, Animation.Dimension dim){
				       if(null != endAction){
					   endAction.execute(subject, dim);
				       }
				   }

				   @Override
				   public void timingEvent(float fraction, 
							   Object subject, Animation.Dimension dim){
				       glyph.setHSVbColor(startH + fraction*(endH - startH),
							  startS + fraction*(endS - startS),
							  startV + fraction*(endV - startV));
				   }
			       });
    }


    public Animation createTranslucencyAnim(final int duration, final Translucent translucent,
					    final float data, final boolean relative,
					    final Interpolator interpolator,
					    final EndAction endAction){
	return createAnimation(duration, 1f, Animator.RepeatBehavior.LOOP,
			       translucent,
			       Animation.Dimension.TRANSLUCENCY,
			       new DefaultTimingHandler(){
				   private final float startA = translucent.getTranslucencyValue();
				   private final float endA = relative? startA + data : data;

				   @Override
				   public void end(Object subject, Animation.Dimension dim){
				       if(null != endAction){
					   endAction.execute(subject, dim);
				       }
				   }

				   @Override
				   public void timingEvent(float fraction, 
							   Object subject, Animation.Dimension dim){
				       translucent.setTranslucencyValue(startA + fraction*(endA - startA));
				   }
			       });
    }

    public Animation createPortalTranslation(final int duration, final Portal portal,
					     final Point data, final boolean relative,
					     final Interpolator interpolator,
					     final EndAction endAction){
	return createAnimation(duration, 1f, Animator.RepeatBehavior.LOOP,
			       portal,
			       Animation.Dimension.POSITION,
			       new DefaultTimingHandler(){
				   private final int startX = portal.x;
				   private final int startY = portal.y;
				   private final int endX = 
				       relative? portal.x + data.x : data.x;
				   private final int endY = 
				       relative? portal.y + data.y : data.y;

				   @Override
				    public void end(Object subject, Animation.Dimension dim){
					if(null != endAction){
					    endAction.execute(subject, dim);
					}
				    }
				    
				    @Override
				    public void timingEvent(float fraction, 
							    Object subject, Animation.Dimension dim){
					portal.moveTo(startX + (int)(fraction*(endX - startX)),
						      startY + (int)(fraction*(endY - startY)));
				    }
				   
			       },
			       interpolator);
    }
    
    public Animation createPortalSizeAnim(final int duration, final Portal portal,
					  final int wdata, final int hdata, 
					  final boolean relative,
					  final Interpolator interpolator,
					  final EndAction endAction){
	return createAnimation(duration, 1f, Animator.RepeatBehavior.LOOP,
			       portal,
			       Animation.Dimension.SIZE,
			       new DefaultTimingHandler(){
				   private final int startW = portal.w;
				   private final int startH = portal.h;
				   private final int endW = 
				       relative? startW + wdata : wdata;
				   private final int endH = 
				       relative? startH + hdata : hdata;

				   @Override
				   public void end(Object subject, Animation.Dimension dim){
				       if(null != endAction){
					   endAction.execute(subject, dim);
				       }
				   }
				   
				   @Override
				   public void timingEvent(float fraction, 
							   Object subject, Animation.Dimension dim){
				       portal.sizeTo(startW + (int)(fraction*(endW - startW)),
						     startH + (int)(fraction*(endH - startH)));
				   }
				   
			       },
			       interpolator);
    }
    

    private final AnimationManager animationManager;
}