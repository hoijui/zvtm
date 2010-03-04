/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import java.lang.reflect.Method;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import fr.inria.zvtm.engine.VirtualSpaceManager;

/**
 * Define methods that will be replayed automatically
 * on remote virtual spaces.
 * Autoreplay is a quick way of propagating changes to
 * remote objects without writing Delta classes.
 * Use only for "atomic" operations (change one attribute at a time).
 */
public abstract aspect AbstractAutoReplay {
    abstract pointcut autoReplayMethods(Identifiable replayTarget);

    pointcut masterAutoReplay(Identifiable replayTarget) :
        autoReplayMethods(replayTarget) && 
        if(VirtualSpaceManager.INSTANCE.isMaster());

    after(Identifiable replayTarget) :
        masterAutoReplay(replayTarget) &&
        !cflowbelow(masterAutoReplay(Identifiable)){
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

        Delta delta = new GenericDelta(target,
                method.getName(),
                method.getParameterTypes(),
                args);

        VirtualSpaceManager.INSTANCE.sendDelta(delta);
    }
}

