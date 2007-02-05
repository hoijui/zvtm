/*   FILE: TOWApplication.java
 *   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id:  $
 */

package net.claribole.eval.to;

import java.awt.Toolkit;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.engine.*;


abstract class AcquireBaseEventHandler implements ViewEventHandler, ComponentListener {//, ObservedRegionListener {

    static final float MAIN_SPEED_FACTOR = 50.0f;
    static final float LENS_SPEED_FACTOR = 5.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    AcquireEval application;

    boolean mouseInsideOverview = false;

    VCursor cursor;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    long lastVX, lastVY;

    float projCoef, alt, oldCameraAltitude; // for efficiency

    /* main camera is being dragged */
    boolean mCameraStickedToMouse = false;
    /* overview camera is being dragged */
    boolean oCameraStickedToMouse = false;
    /* observed region in overview is being dragged */
    boolean orStickedToMouse = false;

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}

    public void click1(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void release2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void click2(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void release3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void click3(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, MouseEvent e){}

    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e){
	// DO NOT ALLOW ALTITUDE CHANGES, IT DOES NOT MAKE SENSE WITH A STYLUS
// 	if (!application.alm.trialStarted){return;}
// 	projCoef = (application.mCamera.focal+Math.abs(application.mCamera.altitude))/application.mCamera.focal;
// 	if (wheelDirection  == WHEEL_UP){// zooming in
// 	    application.mCamera.altitudeOffset(-projCoef*WHEEL_ZOOMIN_FACTOR);
// 	    cameraMoved();
// 	    application.vsm.repaintNow();
// 	}
// 	else {//wheelDirection == WHEEL_DOWN, zooming out
// 	    application.mCamera.altitudeOffset(projCoef*WHEEL_ZOOMOUT_FACTOR);
// 	    cameraMoved();
// 	    application.vsm.repaintNow();
// 	}
    }

    public void enterGlyph(Glyph g){
	if (!application.alm.trialStarted){return;}
	if (g.mouseInsideFColor != null){g.color = g.mouseInsideFColor;}
	if (g.mouseInsideColor != null){g.borderColor = g.mouseInsideColor;}
    }

    public void exitGlyph(Glyph g){
	if (!application.alm.trialStarted){return;}
	if (g.isSelected()){
	    g.borderColor = (g.selectedColor != null) ? g.selectedColor : g.bColor;
	}
	else {
	    if (g.mouseInsideFColor != null){g.color = g.fColor;}
	    if (g.mouseInsideColor != null){g.borderColor = g.bColor;}
	}
    }

    public void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e){
	if (!application.alm.trialStarted){return;}
	if (code == KeyEvent.VK_SPACE){application.alm.validateTarget();}
    }
           
    public void Krelease(ViewPanel v, char c, int code, int mod, KeyEvent e){
	if (code == KeyEvent.VK_S){application.alm.startSession();}
	else if (mod == CTRL_MOD && code == KeyEvent.VK_Q){application.exit();}
	else if (code == KeyEvent.VK_F12){application.alm.nextTarget();}
    }
           
    public void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e){}
    
    public void viewActivated(View v){}
    
    public void viewClosing(View v){}
    
    public void viewDeactivated(View v){}
    
    public void viewDeiconified(View v){}
    
    public void viewIconified(View v){}

    /* ComponentListener */
    public void componentHidden(ComponentEvent e){}

    public void componentMoved(ComponentEvent e){}

    public void componentResized(ComponentEvent e){
	application.updatePanelSize();
    }

    public void componentShown(ComponentEvent e){}

    void cameraMoved(){
// 	alt = application.mCamera.getAltitude();
// 	if (alt != oldCameraAltitude){
// 	    oldCameraAltitude = alt;
// 	    application.updateOverview();
// 	}
    }

//     long translationSpeed;

//     public void intersectsParentRegion(long[] wnes){
// 	if (orStickedToMouse){
// 	    translationSpeed = Math.round((application.oCamera.altitude+application.oCamera.focal)/application.oCamera.focal);
// 	    if (wnes[0] < 0 && wnes[2] < 0){// intersection west border
// 		application.oCamera.move(-translationSpeed, 0);
//  		application.mCamera.move(-translationSpeed, 0);
// 	    }
// 	    else if (wnes[0] > 0 && wnes[2] > 0){// intersection east border
// 		application.oCamera.move(translationSpeed, 0);
// 		application.mCamera.move(translationSpeed, 0);
// 	    }
// 	    if (wnes[1] > 0 && wnes[3] > 0){// intersection north border
// 		application.oCamera.move(0, translationSpeed);
// 		application.mCamera.move(0, translationSpeed);
// 	    }
// 	    else if (wnes[1] < 0 && wnes[3] < 0){// intersection south border
// 		application.oCamera.move(0, -translationSpeed);
// 		application.mCamera.move(0, -translationSpeed);
// 	    }
// 	}
//     }
   
}