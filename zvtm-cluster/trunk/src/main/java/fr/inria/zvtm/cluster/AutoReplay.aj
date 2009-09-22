package fr.inria.zvtm.cluster;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;

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

		GenericGlyphDelta glyphDelta = new GenericGlyphDelta(glyph,
				method.getName(),
				method.getParameterTypes(),
				args);

		glyphDelta.execute();
	}

	//dummy generic proxy, for debug purposes
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
			//get Glyph associated with ID
			//retrieve method
			//execute method
		}
	}
}

