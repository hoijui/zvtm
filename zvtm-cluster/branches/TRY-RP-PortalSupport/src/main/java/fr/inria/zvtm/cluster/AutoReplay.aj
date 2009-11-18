/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.CameraPortal;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.RectangularShape;
import fr.inria.zvtm.glyphs.VText;

import java.lang.reflect.Method;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import java.awt.Color;

/**
 * Define methods that will be replayed automatically
 * on remote virtual spaces.
 * Autoreplay is a quick way of propagating changes to
 * remote objects without writing Delta classes.
 * Use only for "atomic" operations (change one attribute at a time).
 */
public aspect AutoReplay {
	//Rules to observe in order to modify this pointcut:
	// - only add execution join points
	// - every parameter of every method join point must be
	// serializable (primitive types are okay)
	// - exercise caution when adding non-public methods to the 
	// join points, because these methods will be invoked reflectively.
	pointcut glyphAutoReplayMethods(Glyph glyph) : 
		this(glyph) &&
		if(VirtualSpaceManager.INSTANCE.isMaster()) &&
		(
		 execution(public void Glyph.move(long, long))	||
		 execution(public void Glyph.moveTo(long, long))	||
		 execution(public void Glyph.setStrokeWidth(float))	||
		 execution(public void Glyph.setMouseInsideHighlightColor(Color)) ||
		 execution(public void Glyph.setVisible(boolean)) ||
		 execution(public void Glyph.orientTo(float)) ||
		 execution(public void Glyph.setSensitivity(boolean)) ||
		 execution(public void VText.setText(String)) || 
		 execution(public void VText.setScale(float)) || 
		 execution(public void ClosedShape.setDrawBorder(boolean)) || 
		 execution(public void ClosedShape.setFilled(boolean)) || 
		 execution(public void DPath.addSegment(long, long, boolean)) ||  
		 execution(public void DPath.addCbCurve(long, long, long, long, long, long, boolean)) ||  
		 execution(public void DPath.addQdCurve(long, long, long, long, boolean)) ||  
		 execution(public void RectangularShape.setHeight(long)) ||  
		 execution(public void RectangularShape.setWidth(long))   
		)
		;

	after(Glyph glyph) returning: 
		glyphAutoReplayMethods(glyph) && 
		!cflowbelow(glyphAutoReplayMethods(Glyph)){
			sendGenericDelta(glyph, thisJoinPoint);
		}

		pointcut cameraAutoReplayMethods(Camera camera) :
		this(camera) &&
		if(VirtualSpaceManager.INSTANCE.isMaster()) &&
		(
		 execution(public void Camera.setZoomFloor(float))
		)
		;

	after(Camera camera) :
		cameraAutoReplayMethods(camera) &&
        !cflowbelow(cameraAutoReplayMethods(Camera)){
            sendGenericDelta(camera, thisJoinPoint);
        }

    pointcut genericAutoReplayMethods(Identifiable replayTarget) :
        this(replayTarget) &&
        if(VirtualSpaceManager.INSTANCE.isMaster()) &&
        (
         execution(public void CameraPortal.setBackgroundColor(Color))
        )
        ;

    after(Identifiable replayTarget) :
        genericAutoReplayMethods(replayTarget) &&
        !cflowbelow(genericAutoReplayMethods(Identifiable)){
            sendGenericDelta(replayTarget, thisJoinPoint);
        }

	private static void sendGenericDelta(Identifiable target, 
			JoinPoint joinPoint){
		Signature sig = joinPoint.getStaticPart().getSignature();
		//We want to create a generic, serializable proxy that
		//calls a remote method. Hence, we catch method invocations.
		//If this assert fires, chances are that the definition of
		//the related pointcuts are incorrect.
		assert(sig instanceof MethodSignature);
		Method method = ((MethodSignature)sig).getMethod();
		Object[] args = joinPoint.getArgs();

		GenericDelta glyphDelta = new GenericDelta(target,
				method.getName(),
				method.getParameterTypes(),
				args);

		VirtualSpaceManager.INSTANCE.sendDelta(glyphDelta);
	}
}

