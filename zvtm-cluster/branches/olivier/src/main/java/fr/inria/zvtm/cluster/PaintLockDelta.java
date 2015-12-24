/*
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2014.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

class PaintLockDelta implements Delta {
	private final boolean dolock;

	PaintLockDelta(boolean b){
		dolock = b;
	}

    public void apply(SlaveUpdater updater){
        updater.setPaintLock(dolock);
    }
}

