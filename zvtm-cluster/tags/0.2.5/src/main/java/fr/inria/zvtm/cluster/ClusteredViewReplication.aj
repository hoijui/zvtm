/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;

import java.awt.Color;

/**
 * Replicates changes made to a ClusteredView
 * (adding a ClusteredView to a VirtualSpaceManager is 
 * handled by VsmReplication)
 */
aspect ClusteredViewReplication {	
    pointcut colorChange(ClusteredView clusteredView):
        execution(public void ClusteredView.setBackgroundColor(Color)) 
        && this(clusteredView)
        && if(VirtualSpaceManager.INSTANCE.isMaster());

    after(ClusteredView cv, Color bgColor) returning:
        colorChange(cv) && !cflowbelow(colorChange(ClusteredView))
        && args(bgColor){
            Delta delta = new BgColorDelta(cv.getObjId(),
                    bgColor); 
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class BgColorDelta implements Delta {
        private final ObjId<ClusteredView> cvId;
        private final Color bgColor;

        BgColorDelta(ObjId<ClusteredView> cvId, Color bgColor){
            this.cvId = cvId;
            this.bgColor = bgColor;
        }

        public void apply(SlaveUpdater updater){
            //do not change state of remote clustered views.
            //instead, see if the clustered view owns the local
            //zvtm view and set its color if it does
            ClusteredView cv = updater.getSlaveObject(cvId);
            if(cv == null){
                return;
            }
            updater.setBackgroundColor(cv, bgColor);
        }
    }
}

