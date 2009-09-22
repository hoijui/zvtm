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
	pointcut glyphAutoReplayMethods(Glyph glyph) : 
		this(glyph) && 
		(
		execution(public * Glyph+.setStrokeWidth(float))	||
		execution(public * Glyph+.setMouseInsideHighlightColor(Color)) ||
		execution(public * Glyph+.setVisible(boolean))
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

		GenericGlyphDelta glyphDelta = new GenericGlyphDelta(glyph,
				method.getName(),
				method.getParameterTypes(),
				args);

		glyphDelta.execute();
	}

	//DRAFT generic proxy, for debug purposes
	private static class GenericGlyphDelta implements Serializable {
		private final ObjId objId;
		private final String methodName;
		private final Class[] parameterTypes;
		private final Object[] arguments;

		GenericGlyphDelta(Glyph target, String methodName,
				Class[] parameterTypes,
				Object[] arguments){

			this.objId = target.getObjId();
			this.methodName = methodName;
			this.parameterTypes = parameterTypes;
			this.arguments = arguments;
		}

		private static String printArgs(Object[] args){
			String result = "";
			for(Object obj: args){
				result += obj;
			}
			return result;
		}

		void execute(){
			System.out.println("Were I a real Delta, I would now be invoking method " + methodName + "(" + parameterTypes + ") with arguments " + printArgs(arguments));
		}
	}
}

