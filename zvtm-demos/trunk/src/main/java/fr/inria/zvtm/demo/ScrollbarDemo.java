/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.demo;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.event.CameraListener;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.SwingWorker;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.event.ViewAdapter;
import fr.inria.zvtm.engine.ScrollLayer;
import fr.inria.zvtm.event.ScrollingListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;

public class ScrollbarDemo implements CameraListener {

    VirtualSpaceManager vsm;

    static final String demoSpaceName = "demoSpace";
    static final String demoViewName = "Scrollbar Demo";
    VirtualSpace demoSpace;
    View demoView;
    Camera mCamera;

    ViewListener meh;
    ScrollLayer sl;
    ViewListener sbeh;

    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;
    static final int ANIM_MOVE_LENGTH = 250;

    ScrollbarDemo(){
	    vsm = VirtualSpaceManager.INSTANCE;
	init();
    }

    public void init(){
	demoSpace = vsm.addVirtualSpace(demoSpaceName);
	mCamera = demoSpace.addCamera();
	mCamera.setZoomFloor(0);
	sl = new ScrollLayer(mCamera);
	Vector cameras = new Vector();
	cameras.add(mCamera);
	cameras.add(sl.getWidgetCamera());
	demoView = vsm.addFrameView(cameras, demoViewName, View.STD_VIEW, 800, 600, true);
	demoView.setBackgroundColor(Color.WHITE);
	meh = new ScrollbarDemoEventHandler(this, 0, 1);
	demoView.setListener(meh, 0);
	sbeh = new ViewAdapter(){
		public void viewClosing(View v){System.exit(0);}
	    };
	demoView.setListener(sbeh, 1);
	demoView.setNotifyCursorMoved(true);
	sl.setView(demoView);
	mCamera.addListener(this);
	final SwingWorker worker = new SwingWorker(){
		public Object construct(){
		    buildGlyphs();
		    return null; 
		}
	    };
	worker.start();
    }
    
    void buildGlyphs(){
	VRectangle r;
	double cw = 45;
	double ch = 45;
	double tw = 90;
	double th = 90;
	for (int i=0;i<30;i++){
	    for (int j=0;j<30;j++){
		r = new VRectangle(2*i*cw, 2*j*ch, 0, tw, th, Color.getHSBColor((float)((i*j)/900.0), 1.0f,1.0f));
		r.setDrawBorder(false);
		demoSpace.addGlyph(r, false);
	    }
	}
	sl.virtualSpaceUpdated();
	vsm.repaint();
	demoView.getGlobalView(demoSpace.getCamera(0), 400);
    }

    void translateView(short direction){
	Camera c = demoView.getCameraNumber(0);
	Point2D.Double trans;
	double[] rb = demoView.getVisibleRegion(c);
	if (direction==MOVE_UP){
	    double qt=(rb[1]-rb[3])/2.4;
	    trans=new Point2D.Double(0,qt);
	}
	else if (direction==MOVE_DOWN){
	    double qt=(rb[3]-rb[1])/2.4;
	    trans=new Point2D.Double(0,qt);
	}
	else if (direction==MOVE_RIGHT){
	    double qt=(rb[2]-rb[0])/2.4;
	    trans=new Point2D.Double(qt,0);
	}
	else {//MOVE_LEFT
	    double qt=(rb[0]-rb[2])/2.4;
	    trans=new Point2D.Double(qt,0);
	}

	Animation anim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraTranslation(ScrollbarDemo.ANIM_MOVE_LENGTH, c, trans, true,
				     SlowInSlowOutInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(anim, true);
    }

    void getGlobalView(){
	demoView.getGlobalView(vsm.getActiveCamera(), ScrollbarDemo.ANIM_MOVE_LENGTH);
    }

    void getHigherView(){
	Camera c = demoView.getCameraNumber(0);
	double alt = c.getAltitude()+c.getFocal();
	
	Animation anim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraAltAnim(ScrollbarDemo.ANIM_MOVE_LENGTH, c, alt, true,
				 SlowInSlowOutInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(anim, true);
    }
    
    void getLowerView(){
	Camera c = demoView.getCameraNumber(0);
	double alt = -(c.getAltitude()+c.getFocal())/2.0f;
	
	Animation anim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraAltAnim(ScrollbarDemo.ANIM_MOVE_LENGTH, c, alt, true,
				 SlowInSlowOutInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(anim, true);
    }

    public void cameraMoved(Camera cam, Point2D.Double coord, double alt){
	sl.cameraUpdated();
    }

    void exit(){
	System.exit(0);
    }

    public static void main(String[] args){
	new ScrollbarDemo();
    }
    
}

class ScrollbarDemoEventHandler implements ViewListener {

    ScrollbarDemo application;

    ScrollLayer sl;
    int mli;
    int sli;

    double lastX,lastY;
    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)

    ScrollbarDemoEventHandler(ScrollbarDemo appli, int mainLayerIndex, int scrollLayerIndex){
	application = appli;
	sl = application.sl;
	mli = 0;
	sli = 1;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	lastJPX = jpx;
	lastJPY = jpy;
	v.setDrawDrag(true);
	application.demoView.mouse.setSensitivity(false);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	v.cams[0].setXspeed(0);
	v.cams[0].setYspeed(0);
	v.cams[0].setZspeed(0);
	v.setDrawDrag(false);
	application.demoView.mouse.setSensitivity(true);
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}
    
    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        if (sl.cursorInside(jpx, jpy)){
            // if the cursor is in the scroll bar area
            if (v.parent.getActiveLayer() == mli){
                // and if the scroll layer is not the active one, make it active
                v.parent.setActiveLayer(sli);
            }
        }
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (buttonNumber == 1){
	    Camera c = application.demoView.getCameraNumber(0);
	    double a = (c.focal+Math.abs(c.altitude))/c.focal;
	    if (mod == SHIFT_MOD) {
		v.cams[0].setXspeed(0);
		v.cams[0].setYspeed(0);
		v.cams[0].setZspeed((c.altitude>0) ? (lastJPY-jpy)*(a/50.0f) : (lastJPY-jpy)/(a*50));  //50 is just a speed factor (too fast otherwise)
	    }
	    else {
		v.cams[0].setXspeed((c.altitude>0) ? (jpx-lastJPX)*(a/50.0f) : (jpx-lastJPX)/(a*50));
		v.cams[0].setYspeed((c.altitude>0) ? (lastJPY-jpy)*(a/50.0f) : (lastJPY-jpy)/(a*50));
		v.cams[0].setZspeed(0);
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	Camera c = application.demoView.getCameraNumber(0);
	double a = (c.focal+Math.abs(c.altitude))/c.focal;
	if (wheelDirection == WHEEL_UP){
	    c.altitudeOffset(-a*5);
	    application.vsm.repaint();
	}
	else {//wheelDirection == WHEEL_DOWN
	    c.altitudeOffset(a*5);
	    application.vsm.repaint();
	}
    }

    public void enterGlyph(Glyph g){
	g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
	g.highlight(false, null);
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
	else if (code==KeyEvent.VK_HOME){application.getGlobalView();}
	else if (code==KeyEvent.VK_UP){application.translateView(ScrollbarDemo.MOVE_UP);}
	else if (code==KeyEvent.VK_DOWN){application.translateView(ScrollbarDemo.MOVE_DOWN);}
	else if (code==KeyEvent.VK_LEFT){application.translateView(ScrollbarDemo.MOVE_LEFT);}
	else if (code==KeyEvent.VK_RIGHT){application.translateView(ScrollbarDemo.MOVE_RIGHT);}
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

}
