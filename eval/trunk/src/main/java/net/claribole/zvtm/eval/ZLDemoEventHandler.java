/*   FILE: ZLDemoEventHandler.java
 *   DATE OF CREATION:  Tue Nov 22 09:51:19 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.eval;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import net.claribole.zvtm.engine.AnimationListener;
import net.claribole.zvtm.engine.ViewEventHandler;

import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.glyphs.Glyph;

class ZLDemoEventHandler implements ViewEventHandler, AnimationListener, ComponentListener {

    static final float MAIN_SPEED_FACTOR = 50.0f;
    static final float LENS_SPEED_FACTOR = 5.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 8.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 9.0f;

    float oldCameraAltitude = ZLWorldTask.START_ALTITUDE;

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

    ZLWorldDemo application;

    ZLDemoEventHandler(ZLWorldDemo appli){
	application = appli;
	application.vsm.animator.setAnimationListener(this);
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
	application.ewmm.updateVisibleMaps(application.demoView.getVisibleRegion(application.demoCamera), false, (short)0);
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
// 	System.err.println(v.getMouse().vx+" "+v.getMouse().vy);
	if ((jpx-ZLWorldDemo.LENS_R1) < 0){
	    jpx = ZLWorldDemo.LENS_R1;
	    cursorNearBorder = true;
	}
	else if ((jpx+ZLWorldDemo.LENS_R1) > application.panelWidth){
	    jpx = application.panelWidth - ZLWorldDemo.LENS_R1;
	    cursorNearBorder = true;
	}
	else {
	    cursorNearBorder = false;
	}
	if ((jpy-ZLWorldDemo.LENS_R1) < 0){
	    jpy = ZLWorldDemo.LENS_R1;
	    cursorNearBorder = true;
	}
	else if ((jpy+ZLWorldDemo.LENS_R1) > application.panelHeight){
	    jpy = application.panelHeight - ZLWorldDemo.LENS_R1;
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
	else {
	    Camera c = application.demoCamera;
	    float a = (c.focal+Math.abs(c.altitude))/c.focal;
	    if (wheelDirection == WHEEL_UP){
		c.altitudeOffset(-a*5);
		cameraMoved();
		application.vsm.repaintNow();
	    }
	    else {//wheelDirection == WHEEL_DOWN
		c.altitudeOffset(a*5);
		cameraMoved();
		application.vsm.repaintNow();
	    }
	}
    }

    public void enterGlyph(Glyph g){}

    public void exitGlyph(Glyph g){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (c == '+'){application.showGridLevel(application.currentLevel+1);}
	else if (c == '-'){application.showGridLevel(application.currentLevel-1);}
	else if (c == '8'){application.F_V -= 0.1f;}
	else if (c == '9'){application.F_V += 0.1f;}
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (mod == SHIFT_MOD && code==KeyEvent.VK_F4){application.ewmm.switchAdaptMaps();}
	else if (mod == SHIFT_MOD && code==KeyEvent.VK_F3){
	    application.switchManhattanizer();
	}
	else if (mod == SHIFT_MOD && code==KeyEvent.VK_F2){
	    application.SHOW_MEMORY_USAGE = !application.SHOW_MEMORY_USAGE;
	    application.vsm.repaintNow();
	}
	else if (mod == SHIFT_MOD && code==KeyEvent.VK_F1){application.gc();}
	// L1 lenses
	else if (code == KeyEvent.VK_2){
	    application.lensFamily = ZLWorldDemo.L1_Linear;
	    application.demoView.setTitle(ZLWorldDemo.L1_Linear_Title);
	}
	else if (code == KeyEvent.VK_3){
	    application.lensFamily = ZLWorldDemo.L1_InverseCosine;
	    application.demoView.setTitle(ZLWorldDemo.L1_InverseCosine_Title);
	}
	else if (code == KeyEvent.VK_4){
	    application.lensFamily = ZLWorldDemo.L1_Manhattan;
	    application.demoView.setTitle(ZLWorldDemo.L1_Manhattan_Title);
	}
	else if (code == KeyEvent.VK_5){
	    application.lensFamily = ZLWorldDemo.L1_Fresnel;
	    application.demoView.setTitle(ZLWorldDemo.L1_Fresnel_Title);
	}
	// L2 lenses
	else if (code == KeyEvent.VK_Q){
	    application.lensFamily = ZLWorldDemo.L2_Gaussian;
	    application.demoView.setTitle(ZLWorldDemo.L2_Gaussian_Title);
	}
	else if (code == KeyEvent.VK_W){
	    application.lensFamily = ZLWorldDemo.L2_Linear;
	    application.demoView.setTitle(ZLWorldDemo.L2_Linear_Title);
	}
	else if (code == KeyEvent.VK_E){
	    application.lensFamily = ZLWorldDemo.L2_InverseCosine;
	    application.demoView.setTitle(ZLWorldDemo.L2_InverseCosine_Title);
	}
	else if (code == KeyEvent.VK_R){
	    application.lensFamily = ZLWorldDemo.L2_Manhattan;
	    application.demoView.setTitle(ZLWorldDemo.L2_Manhattan_Title);
	}
	else if (code == KeyEvent.VK_T){
	    application.lensFamily = ZLWorldDemo.L2_Fresnel;
	    application.demoView.setTitle(ZLWorldDemo.L2_Fresnel_Title);
	}
	else if (code == KeyEvent.VK_Y){
	    application.lensFamily = ZLWorldDemo.L2_TLinear;
	    application.demoView.setTitle(ZLWorldDemo.L2_TLinear_Title);
	}
	else if (code == KeyEvent.VK_U){
	    application.lensFamily = ZLWorldDemo.L2_TGaussian;
	    application.demoView.setTitle(ZLWorldDemo.L2_TGaussian_Title);
	}
	else if (code == KeyEvent.VK_I){
	    application.lensFamily = ZLWorldDemo.L2_Fading;
	    application.demoView.setTitle(ZLWorldDemo.L2_Fading_Title);
	}
	else if (code == KeyEvent.VK_O){
	    application.lensFamily = ZLWorldDemo.L2_Scrambling;
	    application.demoView.setTitle(ZLWorldDemo.L2_Scrambling_Title);
	}
	else if (code == KeyEvent.VK_P){
	    application.lensFamily = ZLWorldDemo.L2_DLinear;
	    application.demoView.setTitle(ZLWorldDemo.L2_DLinear_Title);
	}
	else if (code == KeyEvent.VK_OPEN_BRACKET){
	    application.lensFamily = ZLWorldDemo.L2_XGaussian;
	    application.demoView.setTitle(ZLWorldDemo.L2_XGaussian_Title);
	}
	// L3 lenses
	else if (code == KeyEvent.VK_A){
	    application.lensFamily = ZLWorldDemo.L3_Gaussian;
	    application.demoView.setTitle(ZLWorldDemo.L3_Gaussian_Title);
	}
	else if (code == KeyEvent.VK_S){
	    application.lensFamily = ZLWorldDemo.L3_Linear;
	    application.demoView.setTitle(ZLWorldDemo.L3_Linear_Title);
	}
	else if (code == KeyEvent.VK_D){
	    application.lensFamily = ZLWorldDemo.L3_InverseCosine;
	    application.demoView.setTitle(ZLWorldDemo.L3_InverseCosine_Title);
	}
	else if (code == KeyEvent.VK_F){
	    application.lensFamily = ZLWorldDemo.L3_Manhattan;
	    application.demoView.setTitle(ZLWorldDemo.L3_Manhattan_Title);
	}
	else if (code == KeyEvent.VK_G){
	    application.lensFamily = ZLWorldDemo.L3_Fresnel;
	    application.demoView.setTitle(ZLWorldDemo.L3_Fresnel_Title);
	}
	else if (code == KeyEvent.VK_H){
	    application.lensFamily = ZLWorldDemo.L3_TLinear;
	    application.demoView.setTitle(ZLWorldDemo.L3_TLinear_Title);
	}
	// LInf lenses
	else if (code == KeyEvent.VK_Z){
	    application.lensFamily = ZLWorldDemo.LInf_Gaussian;
	    application.demoView.setTitle(ZLWorldDemo.LInf_Gaussian_Title);
	}
	else if (code == KeyEvent.VK_X){
	    application.lensFamily = ZLWorldDemo.LInf_Linear;
	    application.demoView.setTitle(ZLWorldDemo.LInf_Linear_Title);
	}
	else if (code == KeyEvent.VK_C){
	    application.lensFamily = ZLWorldDemo.LInf_InverseCosine;
	    application.demoView.setTitle(ZLWorldDemo.LInf_InverseCosine_Title);
	}
	else if (code == KeyEvent.VK_V){
	    application.lensFamily = ZLWorldDemo.LInf_Manhattan;
	    application.demoView.setTitle(ZLWorldDemo.LInf_Manhattan_Title);
	}
	else if (code == KeyEvent.VK_B){
	    application.lensFamily = ZLWorldDemo.LInf_Fresnel;
	    application.demoView.setTitle(ZLWorldDemo.LInf_Fresnel_Title);
	}
	else if (code == KeyEvent.VK_N){
	    application.lensFamily = ZLWorldDemo.LInf_TLinear;
	    application.demoView.setTitle(ZLWorldDemo.LInf_TLinear_Title);
	}
	else if (code == KeyEvent.VK_M){
	    application.lensFamily = ZLWorldDemo.LInf_Fading;
	    application.demoView.setTitle(ZLWorldDemo.LInf_Fading_Title);
	}
	
	else if (code == KeyEvent.VK_F1){
	    application.lensFamily = ZLWorldDemo.LP_Gaussian;
	    application.demoView.setTitle(ZLWorldDemo.LP_Gaussian_Title);
	}
	else if (code == KeyEvent.VK_F2){
	    application.setDistanceMetrics(-1);
	}
	else if (code == KeyEvent.VK_F3){
	    application.setDistanceMetrics(1);
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
    
    public void cameraMoved(){
	// region seen through camera
	wnes = application.demoView.getVisibleRegion(application.demoCamera);
	// update grid depth
	float alt = application.demoCamera.getAltitude();
	if (alt != oldCameraAltitude){
	    if (lensType == NO_LENS){
		//XXX: why did I comment this out?
		//application.ewmm.updateVisibleMaps(wnes, false, (short)0);
	    }
	    oldCameraAltitude = alt;
	}
	else {// camera movement was a simple translation
	    // update detailed map(s) depending on what region is currently observed
	    // only if camera is not being moved as a result of lens activation/deactivation
	    application.ewmm.updateVisibleMaps(wnes, true, application.ewmm.getMapLevel(alt));
	}
	// stop camera if going too far away from map
	if (cameraFarFromMapH(wnes, application.vsm.animator.Xspeed < 0)){
	    application.vsm.animator.Xspeed = 0;
	}
	if (cameraFarFromMapV(wnes, application.vsm.animator.Yspeed < 0)){
	    application.vsm.animator.Yspeed = 0;
	}
    }

    boolean cameraFarFromMapH(long[] wnes, boolean goingEast){// false if going west
	if (wnes[0] > ZLWorldTask.HALF_MAP_WIDTH && goingEast ||
	    wnes[2] < -ZLWorldTask.HALF_MAP_WIDTH && !goingEast){
	    return true;
	}
	else {
	    return false;
	}
    }

    boolean cameraFarFromMapV(long[] wnes, boolean goingNorth){// false if going south
	if (wnes[1] < -ZLWorldTask.HALF_MAP_HEIGHT && !goingNorth ||
	    wnes[3] > ZLWorldTask.HALF_MAP_HEIGHT && goingNorth){
	    return true;
	}
	else {
	    return false;
	}
    }

}
