package fr.inria.zvtm.cluster;

import fr.inria.zvtm.glyphs.Glyph;

import java.awt.Color;
import java.io.Serializable;
import java.lang.reflect.Method;

import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.Signature;

/**
 * Define methods that will be replayed automatically
 * on remote virtual spaces.
 * Autoreplay is a quick way of propagating changes to
 * remote objects without writing Delta classes.
 * Use only for "atomic" operations (change one attribute at a time).
 * Likely candidates are Glyph.setStrokeWidth(float),
 * Glyph.setMouseInsideHighlightColor(Color).
 * Bad candidates includes Glyph.setLocation(Location)
 */
public aspect AutoReplay {
	pointcut glyphAutoReplayMethods(Glyph glyph, Serializable arg) : 
		this(glyph) && 
		args(arg) && //capture first argument *only*
		(
		execution(public * Glyph+.setStrokeWidth(float))	||
		execution(public * Glyph+.setMouseInsideHighlightColor(Color)) ||
		execution(public * Glyph+.setVisible(boolean))
		)
		;

	after(Glyph glyph, Serializable arg) : 
		glyphAutoReplayMethods(glyph, arg) {
		System.out.println("inside glyphAutoReplayMethods");
		//create a delta message
		Signature sig = thisJoinPoint.getStaticPart().getSignature();
		assert(sig instanceof MethodSignature);
		Method method = ((MethodSignature)sig).getMethod();

		GenericGlyphDelta glyphDelta = new GenericGlyphDelta(glyph,
				method.getName(),
				method.getParameterTypes(),
				arg);

		glyphDelta.execute();
	}

	//DRAFT generic proxy, for debug purposes
	private class GenericGlyphDelta implements Serializable {
		private final ObjId objId;
		private final String methodName;
		private final Class[] parameterTypes;
		private final Serializable argument;

		GenericGlyphDelta(Glyph target, String methodName,
				Class[] parameterTypes,
				Serializable argument){
			this.objId = target.getObjId();
			this.methodName = methodName;
			this.parameterTypes = parameterTypes;
			this.argument = argument;
		}

		void execute(){
			System.out.println("Were I a real Delta, I would now be invoking method " + methodName + "(" + parameterTypes + ") with the argument " + argument);
		}
	}
}

