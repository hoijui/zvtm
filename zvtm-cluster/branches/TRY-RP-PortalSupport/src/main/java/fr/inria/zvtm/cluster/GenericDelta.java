/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GenericDelta implements Delta {
	private final ObjId objId;
	private final String methodName;
	private final Class[] parameterTypes;
	private final Object[] arguments;

	static final Logger logger = 
		LoggerFactory.getLogger(GenericDelta.class);

	GenericDelta(Identifiable target, String methodName,
			Class[] parameterTypes,
			Object[] arguments){

		this.objId = target.getObjId();
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		this.arguments = arguments;
	}

	public void apply(SlaveUpdater updater){
		try{
			Object target = updater.getSlaveObject(objId);
			Method method = target.getClass().getMethod(methodName, 
					parameterTypes);
			method.invoke(target, arguments);
		} catch (Exception e){
			logger.error("Could not invoke remove method", e);
		}
	}

	@Override public String toString(){
		return "GenericDelta, target=" + objId + 
			", method=" + methodName + ", args=" + 
			Arrays.toString(arguments);
	}
}

