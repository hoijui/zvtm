/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic Delta that replays a method call remotely.
 * Method target must be Identifiable and must exist in the remote
 * address space.
 * If a method argument is an instance of Identifiable, this
 * argument will be converted to the corresponding ObjectId
 * (and the counterpart object will be retrieved on Delta execution).
 */
public class GenericDelta implements Delta {
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

        for(int i=0; i<arguments.length; ++i){
            if(arguments[i] instanceof Identifiable){
                arguments[i] = ((Identifiable)arguments[i]).getObjId();
            }
        }
	}

	public void apply(SlaveUpdater updater){
		try{
			Object target = updater.getSlaveObject(objId);
			Method method = target.getClass().getMethod(methodName, 
					parameterTypes);
            for(int i=0; i<arguments.length; ++i){
                if(arguments[i] instanceof ObjId){
                    arguments[i] = updater.getSlaveObject((ObjId)arguments[i]);
                }
            }
			method.invoke(target, arguments);
		} catch (Exception e){
            logger.error("Could not invoke remote method: {}. methodName: {}",
                    e, methodName);
		}
	}

	@Override public String toString(){
		return "GenericDelta, target=" + objId + 
			", method=" + methodName + ", args=" + 
			Arrays.toString(arguments);
	}
}

