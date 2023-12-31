/*   FILE: BaseEventHandler.java
 *   DATE OF CREATION:   Mon Nov 27 08:30:31 2006
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *   $Id$
 */ 

package net.claribole.zgrviewer;

import java.awt.Point;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.VSegment;

import fr.inria.zvtm.engine.Portal;
import fr.inria.zvtm.engine.PortalEventHandler;

public abstract class BaseEventHandler implements PortalEventHandler {

    static final float WHEEL_ZOOMOUT_FACTOR = 21.0f;
    static final float WHEEL_ZOOMIN_FACTOR = 22.0f;
    
    static final float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static final float PAN_SPEED_FACTOR = 50.0f;

    Camera activeCam;
    VSegment navSeg;

    boolean cursorNearBorder = false;

    // remember last mouse coords to compute translation  (dragging)
    int lastJPX,lastJPY;
    long lastVX, lastVY;
    long jpxD, jpyD;
    float tfactor;
    // remember last mouse coords to display selection rectangle (dragging)
    long x1,y1,x2,y2;
    
    // lens optimization
    int lx, ly;

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
    
    /* Link Sliding */
	long LS_SX, LS_SY;
	Point relative;

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
