/*
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2014.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

class StopDelta implements Delta{
    public void apply(SlaveUpdater updater){
        updater.stop();
    }
}

