/*   FILE: BaseEventHandler.java
 *   DATE OF CREATION:   Mon Nov 27 08:30:31 2006
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *   $Id:  $
 */ 

package net.claribole.zgrviewer;

import net.claribole.zvtm.engine.Portal;
import net.claribole.zvtm.engine.PortalEventHandler;

public class BaseEventHandler implements PortalEventHandler {

    
    boolean zoomingInRegion=false;
    boolean manualLeftButtonMove=false;
    boolean manualRightButtonMove=false;

    /*speed-dependant autozoom data*/
    boolean autoZooming = false;
    double dragValue;

    boolean toolPaletteIsActive = false;

    /* DragMag interaction */
    boolean inZoomWindow = false;
    boolean inMagWindow = false;
    boolean draggingMagWindow = false;
    boolean draggingZoomWindow = false;
    boolean draggingZoomWindowContent = false;
    

    /**cursor enters portal*/
    public void enterPortal(Portal p){
	inZoomWindow = true;
    }

    /**cursor exits portal*/
    public void exitPortal(Portal p){
	inZoomWindow = false;
    }

    void resetDragMagInteraction(){
	inMagWindow = false;
	inZoomWindow = false;
	draggingZoomWindow = false;
	draggingZoomWindowContent = false;
    }

}