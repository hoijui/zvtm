package fr.inria.zvtm.cluster;

import fr.inria.zvtm.cluster.Identifiable;

import fr.inria.zvtm.common.compositor.MetisseWindow;

/**
 * Add methods that should be replay by the generic Delta here.
 * See the AbstractAutoReplay aspect in ZVTM-cluster for more details.
 * @see fr.inria.zvtm.AbstractAutoReplay
 */
aspect MetisseAutoReplay extends AbstractAutoReplay {
	public pointcut autoReplayMethods(Identifiable replayTarget) :
		this(replayTarget) &&
		if(replayTarget.isReplicated()) &&
		(
		 execution(public void MetisseWindow.fbUpdate(byte[], int, int, int, int)) ||
		 execution(public void MetisseWindow.configure(int, int, int, int))  ||
		 execution(public void MetisseWindow.endResize())  ||
		 execution(public void MetisseWindow.endRescale())  ||
		 execution(public void MetisseWindow.endMove()) ||
		 execution(public void MetisseWindow.setScaleFactor(double))  ||
		 execution(public void MetisseWindow.moveGlyphOf(double,double))  ||
		 execution(public void MetisseWindow.refreshMaster(MetisseWindow))  ||
		 execution(public void MetisseWindow.resetTransform())  
		);
}

