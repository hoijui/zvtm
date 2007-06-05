/*   FILE: TLensDemoEventHandler.java
 *   DATE OF CREATION:  Sat Jun 10 18:06:19 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.demo;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import net.claribole.zvtm.engine.ViewEventHandler;

import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.glyphs.Glyph;

class TLensDemoEventHandler implements ViewEventHandler, ComponentListener {

    static final float MAIN_SPEED_FACTOR = 50.0f;
    static final float LENS_SPEED_FACTOR = 5.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 8.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 9.0f;

    float oldCameraAltitude = TLensDemo.START_ALTITUDE;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    long lastVX, lastVY;

    static final double DRAG_FACTOR = 50.0;

    static final int NO_LENS = 0;
    static final int ZOOMIN_LENS = 1;
    static final int ZOOMOUT_LENS = -1;
    int lensType = NO_LENS;

    static final short ZOOMING_IN = -1;
    static final short ZOOMING_OUT = 1;
    static final short NOT_ZOOMING = 0;
    short zoomDirection = NOT_ZOOMING;

    long[] wnes; //region seen through camera

    boolean cursorNearBorder = false;

    TLensDemo application;

    TLensDemoEventHandler(TLensDemo appli){
	application = appli;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.activeView.mouse.setSensitivity(false);
	lastJPX = jpx;
	lastJPY = jpy;
	v.setDrawDrag(true);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.animator.Xspeed = 0;
	application.vsm.animator.Yspeed = 0;
	application.vsm.animator.Aspeed = 0;
	application.vsm.activeView.mouse.setSensitivity(true);
	v.setDrawDrag(false);
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	lastVX = v.getMouse().vx;
	lastVY = v.getMouse().vy;
	if (lensType != NO_LENS){
	    application.zoomInPhase2(lastVX, lastVY);
	}
	else {
	    if (cursorNearBorder){// do not activate the lens when cursor is near the border
		return;
	    }
	    application.zoomInPhase1(jpx, jpy);
	}
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.activeView.mouse.setSensitivity(false);
	lastJPX = jpx;
	lastJPY = jpy;
	v.setDrawDrag(true);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.animator.Xspeed = 0;
	application.vsm.animator.Yspeed = 0;
	application.vsm.animator.Aspeed = 0;
	application.vsm.activeView.mouse.setSensitivity(true);
	v.setDrawDrag(false);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	lastVX = v.getMouse().vx;
	lastVY = v.getMouse().vy;
	if (lensType != NO_LENS){
	    application.zoomOutPhase2();
	}
	else {
	    if (cursorNearBorder){// do not activate the lens when cursor is near the border
		return;
	    }
	    application.zoomOutPhase1(jpx, jpy, lastVX, lastVY);
	}
    }

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
	if ((jpx-TLensDemo.LENS_R1) < 0){
	    jpx = TLensDemo.LENS_R1;
	    cursorNearBorder = true;
	}
	else if ((jpx+TLensDemo.LENS_R1) > application.panelWidth){
	    jpx = application.panelWidth - TLensDemo.LENS_R1;
	    cursorNearBorder = true;
	}
	else {
	    cursorNearBorder = false;
	}
	if ((jpy-TLensDemo.LENS_R1) < 0){
	    jpy = TLensDemo.LENS_R1;
	    cursorNearBorder = true;
	}
	else if ((jpy+TLensDemo.LENS_R1) > application.panelHeight){
	    jpy = application.panelHeight - TLensDemo.LENS_R1;
	    cursorNearBorder = true;
	}
	if (lensType != 0 && application.lens != null){
	    application.moveLens(jpx, jpy, e.getWhen());
	}
	//application.vsm.repaintNow();
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
 	if (buttonNumber != 2){
	    float a = (application.demoCamera.focal+Math.abs(application.demoCamera.altitude))/application.demoCamera.focal;
	    application.vsm.animator.Xspeed = (jpx-lastJPX)*(a/DRAG_FACTOR);
	    application.vsm.animator.Yspeed = (lastJPY-jpy)*(a/DRAG_FACTOR);
	    application.vsm.animator.Aspeed = 0;
 	}
	if (lensType != 0 && application.lens != null){
	    application.moveLens(jpx, jpy, e.getWhen());
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	if (lensType != 0 && application.lens != null){
	    if (wheelDirection  == ViewEventHandler.WHEEL_UP){
		application.magnifyFocus(application.WHEEL_MM_STEP, lensType, application.demoCamera);
	    }
	    else {
		application.magnifyFocus(-application.WHEEL_MM_STEP, lensType, application.demoCamera);
	    }
	}
    }

    public void enterGlyph(Glyph g){}

    public void exitGlyph(Glyph g){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
	// L1 lenses
	if (code == KeyEvent.VK_2){
	    application.lensFamily = TLensDemo.L1_Linear;
	    application.demoView.setTitle(TLensDemo.L1_Linear_Title);
	}
	else if (code == KeyEvent.VK_3){
	    application.lensFamily = TLensDemo.L1_InverseCosine;
	    application.demoView.setTitle(TLensDemo.L1_InverseCosine_Title);
	}
	else if (code == KeyEvent.VK_4){
	    application.lensFamily = TLensDemo.L1_Manhattan;
	    application.demoView.setTitle(TLensDemo.L1_Manhattan_Title);
	}
	else if (code == KeyEvent.VK_5){
	    application.lensFamily = TLensDemo.L1_Fresnel;
	    application.demoView.setTitle(TLensDemo.L1_Fresnel_Title);
	}
	// L2 lenses
	else if (code == KeyEvent.VK_Q){
	    application.lensFamily = TLensDemo.L2_Gaussian;
	    application.demoView.setTitle(TLensDemo.L2_Gaussian_Title);
	}
	else if (code == KeyEvent.VK_W){
	    application.lensFamily = TLensDemo.L2_Linear;
	    application.demoView.setTitle(TLensDemo.L2_Linear_Title);
	}
	else if (code == KeyEvent.VK_E){
	    application.lensFamily = TLensDemo.L2_InverseCosine;
	    application.demoView.setTitle(TLensDemo.L2_InverseCosine_Title);
	}
	else if (code == KeyEvent.VK_R){
	    application.lensFamily = TLensDemo.L2_Manhattan;
	    application.demoView.setTitle(TLensDemo.L2_Manhattan_Title);
	}
	else if (code == KeyEvent.VK_T){
	    application.lensFamily = TLensDemo.L2_Fresnel;
	    application.demoView.setTitle(TLensDemo.L2_Fresnel_Title);
	}
	else if (code == KeyEvent.VK_Y){
	    application.lensFamily = TLensDemo.L2_TLinear;
	    application.demoView.setTitle(TLensDemo.L2_TLinear_Title);
	}
	else if (code == KeyEvent.VK_U){
	    application.lensFamily = TLensDemo.L2_TGaussian;
	    application.demoView.setTitle(TLensDemo.L2_TGaussian_Title);
	}
	else if (code == KeyEvent.VK_I){
	    application.lensFamily = TLensDemo.L2_Fading;
	    application.demoView.setTitle(TLensDemo.L2_Fading_Title);
	}
	else if (code == KeyEvent.VK_O){
	    application.lensFamily = TLensDemo.L2_Scrambling;
	    application.demoView.setTitle(TLensDemo.L2_Scrambling_Title);
	}
	else if (code == KeyEvent.VK_P){
	    application.lensFamily = TLensDemo.L2_DLinear;
	    application.demoView.setTitle(TLensDemo.L2_DLinear_Title);
	}
	// L3 lenses
	else if (code == KeyEvent.VK_A){
	    application.lensFamily = TLensDemo.L3_Gaussian;
	    application.demoView.setTitle(TLensDemo.L3_Gaussian_Title);
	}
	else if (code == KeyEvent.VK_S){
	    application.lensFamily = TLensDemo.L3_Linear;
	    application.demoView.setTitle(TLensDemo.L3_Linear_Title);
	}
	else if (code == KeyEvent.VK_D){
	    application.lensFamily = TLensDemo.L3_InverseCosine;
	    application.demoView.setTitle(TLensDemo.L3_InverseCosine_Title);
	}
	else if (code == KeyEvent.VK_F){
	    application.lensFamily = TLensDemo.L3_Manhattan;
	    application.demoView.setTitle(TLensDemo.L3_Manhattan_Title);
	}
	else if (code == KeyEvent.VK_G){
	    application.lensFamily = TLensDemo.L3_Fresnel;
	    application.demoView.setTitle(TLensDemo.L3_Fresnel_Title);
	}
	else if (code == KeyEvent.VK_H){
	    application.lensFamily = TLensDemo.L3_TLinear;
	    application.demoView.setTitle(TLensDemo.L3_TLinear_Title);
	}
	// LInf lenses
	else if (code == KeyEvent.VK_Z){
	    application.lensFamily = TLensDemo.LInf_Gaussian;
	    application.demoView.setTitle(TLensDemo.LInf_Gaussian_Title);
	}
	else if (code == KeyEvent.VK_X){
	    application.lensFamily = TLensDemo.LInf_Linear;
	    application.demoView.setTitle(TLensDemo.LInf_Linear_Title);
	}
	else if (code == KeyEvent.VK_C){
	    application.lensFamily = TLensDemo.LInf_InverseCosine;
	    application.demoView.setTitle(TLensDemo.LInf_InverseCosine_Title);
	}
	else if (code == KeyEvent.VK_V){
	    application.lensFamily = TLensDemo.LInf_Manhattan;
	    application.demoView.setTitle(TLensDemo.LInf_Manhattan_Title);
	}
	else if (code == KeyEvent.VK_B){
	    application.lensFamily = TLensDemo.LInf_Fresnel;
	    application.demoView.setTitle(TLensDemo.LInf_Fresnel_Title);
	}
	else if (code == KeyEvent.VK_N){
	    application.lensFamily = TLensDemo.LInf_TLinear;
	    application.demoView.setTitle(TLensDemo.LInf_TLinear_Title);
	}
	else if (code == KeyEvent.VK_M){
	    application.lensFamily = TLensDemo.LInf_Fading;
	    application.demoView.setTitle(TLensDemo.LInf_Fading_Title);
	}
    }

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){
 	System.exit(0);
    }


    /*ComponentListener*/
    public void componentHidden(ComponentEvent e){}
    public void componentMoved(ComponentEvent e){}
    public void componentResized(ComponentEvent e){
	application.updatePanelSize();
    }
    public void componentShown(ComponentEvent e){}
    
}
