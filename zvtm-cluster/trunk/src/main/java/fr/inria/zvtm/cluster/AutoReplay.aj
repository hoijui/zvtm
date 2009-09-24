/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;

import java.lang.reflect.Method;
import org.aspectj.lang.reflect.MethodSignature;
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
		(
		execution(public void Glyph.setStrokeWidth(float))	||
		execution(public void Glyph.setMouseInsideHighlightColor(Color)) ||
		execution(public void Glyph.setVisible(boolean)) ||
		execution(public void Glyph.orientTo(float)) ||
		execution(public void Glyph.setSensitivity(boolean)) ||
		execution(public void VText.setText(String)) || 
		execution(public void VText.setScale(float))  
		)
		;

	after(Glyph glyph) : 
		glyphAutoReplayMethods(glyph) {

		Signature sig = thisJoinPoint.getStaticPart().getSignature();
		//We want to create a generic, serializable proxy that
		//calls a remote method. Hence, we catch method invocations.
		//If this assert fires, chances are that the definition of
		//the above pointcut "glyphAutoReplayMethods" is incorrect.
		assert(sig instanceof MethodSignature);
		Method method = ((MethodSignature)sig).getMethod();
		Object[] args = thisJoinPoint.getArgs();

		GenericDelta glyphDelta = new GenericDelta(glyph,
				method.getName(),
				method.getParameterTypes(),
				args);

		glyphDelta.apply(null);
	}
}

