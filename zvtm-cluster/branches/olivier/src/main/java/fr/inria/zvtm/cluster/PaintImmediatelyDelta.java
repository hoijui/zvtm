/*
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2014.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

class PaintImmediatelyDelta implements Delta{
	private final long maxPaintCount;

	PaintImmediatelyDelta(long mpc){
		maxPaintCount = mpc;
	}

    public void apply(SlaveUpdater updater){
        updater.paintImmediately(maxPaintCount);
    }
}

