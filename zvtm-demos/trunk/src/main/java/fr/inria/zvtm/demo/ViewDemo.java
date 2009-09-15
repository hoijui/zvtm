/*   FILE: ViewDemo.java
 *   DATE OF CREATION:  Fri Aug 26 15:12:06 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package fr.inria.zvtm.demo;

import java.awt.Color;
import java.util.Vector;
import javax.swing.SwingUtilities;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.ViewEventHandler;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.SwingWorker;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VSegment;

public class ViewDemo {

    VirtualSpaceManager vsm;

    VirtualSpace vs;

    String mainSpaceName = "demoSpace";
    String mainViewName = "View";

    ViewEventHandler eh;

    static short MOVE_UP=0;
    static short MOVE_DOWN=1;
    static short MOVE_LEFT=2;
    static short MOVE_RIGHT=3;
    static int ANIM_MOVE_LENGTH = 500;

    View demoView;
    Camera mCamera;

    short translucentMode = 0;
    short viewType = 0;

    String tms, vts;

    ViewDemo(short vt, short translucent){
	    vsm = VirtualSpaceManager.INSTANCE;
	translucentMode = translucent;
	if (translucentMode == 1){
	    tms = "Translucency: ON";
	}
	else {
	    tms = "Translucency: OFF";
	}
	// get View type from command line argument
	viewType = View.STD_VIEW;
	vts = "View type: Standard";
	switch(vt){
	case View.OPENGL_VIEW:{viewType = View.OPENGL_VIEW;vts = "View type: OpenGL";break;}
	}
	init();
    }

    ProgFrame pf;

    public void init(){
	eh=new ViewDemoEventHandler(this);
	vs = vsm.addVirtualSpace(mainSpaceName);
	Vector cameras=new Vector();
	mCamera = vs.addCamera();
	mCamera.setZoomFloor(-90);
	cameras.add(mCamera);
	demoView = vsm.addFrameView(cameras, mainViewName, viewType, 800, 600, false, true);
	demoView.setBackgroundColor(Color.WHITE);
	demoView.setEventHandler(eh);
	final SwingWorker worker = new SwingWorker(){
		public Object construct(){
		    buildGlyphs();
		    return null; 
		}
	    };
	worker.start();
    }
    
    void buildGlyphs(){
	pf = new ProgFrame("Building 10,000 objects...", "ViewDemo");
	VRectangle r;
	long cw = 50;
	long ch = 50;
	long tw = 60;
	long th = 60;
	for (int i=0;i<100;i++){
	    for (int j=0;j<100;j++){
		if (translucentMode == 1){
		    r = new VRectangle(2*i*cw, 2*j*ch, 0, tw, th, Color.getHSBColor((float)((i*j)/10000.0), 1.0f,1.0f), Color.BLACK, 0.5f);
		}
		else {
		    r = new VRectangle(2*i*cw, 2*j*ch, 0, tw, th, Color.getHSBColor((float)((i*j)/10000.0), 1.0f,1.0f));
		}
		r.setDrawBorder(false);
		vs.addGlyph(r, false);
		pf.setPBValue(i+j/100);
	    }
	}
	vsm.repaintNow();
	pf.destroy();
	demoView.getGlobalView(vsm.getVirtualSpace(mainSpaceName).getCamera(0), 400);
    }

    void translateView(short direction){
	Camera c = vsm.getView(mainViewName).getCameraNumber(0);
	LongPoint trans;
	long[] rb = vsm.getView(mainViewName).getVisibleRegion(c);
	if (direction==MOVE_UP){
	    long qt=Math.round((rb[1]-rb[3])/2.4);
	    trans=new LongPoint(0,qt);
	}
	else if (direction==MOVE_DOWN){
	    long qt=Math.round((rb[3]-rb[1])/2.4);
	    trans=new LongPoint(0,qt);
	}
	else if (direction==MOVE_RIGHT){
	    long qt=Math.round((rb[2]-rb[0])/2.4);
	    trans=new LongPoint(qt,0);
	}
	else {//MOVE_LEFT
	    long qt=Math.round((rb[0]-rb[2])/2.4);
	    trans=new LongPoint(qt,0);
	}
	Animation transAnim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraTranslation(ViewDemo.ANIM_MOVE_LENGTH, c, trans, 
				     true, SlowInSlowOutInterpolator.getInstance(), null);

	vsm.getAnimationManager().startAnimation(transAnim, true);	
    }

    void getGlobalView(){
	demoView.getGlobalView(vsm.getActiveCamera(), ViewDemo.ANIM_MOVE_LENGTH);
    }

    void getHigherView(){
	Camera c = vsm.getView(mainViewName).getCameraNumber(0);
	float alt = c.getAltitude()+c.getFocal();

	Animation altAnim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraAltAnim(ViewDemo.ANIM_MOVE_LENGTH, c, alt, true, 
				 SlowInSlowOutInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(altAnim, true);
    }
    
    void getLowerView(){
	Camera c = vsm.getView(mainViewName).getCameraNumber(0);
	float alt = -(c.getAltitude()+c.getFocal())/2.0f;

	Animation altAnim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraAltAnim(ViewDemo.ANIM_MOVE_LENGTH, c, alt, true, 
				 SlowInSlowOutInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(altAnim, true);
    }

    void exit(){
	System.exit(0);
    }

    static String startMsg = "-----------------\nJVM version: " + System.getProperty("java.vm.vendor") + " " + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version") + 
	"\nOS type: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "\nArchitecture: " + System.getProperty("os.arch") + " " + System.getProperty("sun.cpu.isalist") +
	"\n-----------------\nDirectory information\nJava Classpath: " + System.getProperty("java.class.path") +
	"\nJava directory: " + System.getProperty("java.home") +
	"\nLaunching from: " + System.getProperty("user.dir") +
	"\n-----------------\n\nThe ViewDemo gives an indication of the refresh rate under extreme conditions:\n10,000 (possibly translucent) objects are displayed. The refresh rate (given in\nframes per second) corresponds to the approximate theoretical rate ZVTM could\nproduce. In practive, ZVTM limits the refresh rate to 40 fps maximum, as this\nis already beyond human perception capabilities.\n\n-----------------\nThe OpenGL-based View demo requires the use of a Java Virtual Machine\nv1.5.0 or later\n-----------------\nDrag mouse to move camera, Shift + vertical mouse drag to zoom in/out.\n-----------------";

    public static void main(final String[] args){
        System.out.println(startMsg);
        new ViewDemo((args.length > 0) ? Short.parseShort(args[0]) : 0, (args.length > 1) ? Short.parseShort(args[1]) : 0);
    }
    
}


class ViewDemoEventHandler implements ViewEventHandler {

    ViewDemo application;

    boolean manualLeftButtonMove = false;
    boolean manualRightButtonMove=false;
    boolean zoomingInRegion=false;

    long lastX,lastY,lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    long jpxD, jpyD;
    float tfactor;
    float cfactor=50.0f;
    long x1,y1,x2,y2;                    //remember last mouse coords to display selection rectangle (dragging)

    VSegment navSeg;

    Camera activeCam;

    ViewDemoEventHandler(ViewDemo app){
	this.application = app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (mod == NO_MODIFIER || mod == SHIFT_MOD || mod == META_MOD || mod == META_SHIFT_MOD){
	    manualLeftButtonMove=true;
	    lastJPX=jpx;
	    lastJPY=jpy;
	    v.setDrawDrag(true);
	    application.vsm.activeView.mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
	    activeCam=application.vsm.getActiveCamera();
	}
	else if (mod == ALT_MOD){
	    zoomingInRegion=true;
	    x1=v.getMouse().vx;
	    y1=v.getMouse().vy;
	    v.setDrawRect(true);
	}
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (zoomingInRegion){
	    v.setDrawRect(false);
	    x2=v.getMouse().vx;
	    y2=v.getMouse().vy;
	    if ((Math.abs(x2-x1)>=4) && (Math.abs(y2-y1)>=4)){
		application.demoView.centerOnRegion(application.vsm.getActiveCamera(),ViewDemo.ANIM_MOVE_LENGTH,x1,y1,x2,y2);
	    }
	    zoomingInRegion=false;
	}
	else if (manualLeftButtonMove){
	    application.vsm.getAnimationManager().setXspeed(0);
	    application.vsm.getAnimationManager().setYspeed(0);
	    application.vsm.getAnimationManager().setZspeed(0);
	    v.setDrawDrag(false);
	    application.vsm.activeView.mouse.setSensitivity(true);
	    manualLeftButtonMove=false;
	}
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy, int clickNumber, MouseEvent e){
	LongPoint lp = new LongPoint(v.getMouse().vx - v.cams[0].posx, v.getMouse().vy - v.cams[0].posy);

	Animation transAnim = application.vsm.getAnimationManager().getAnimationFactory()
	    .createCameraTranslation(ViewDemo.ANIM_MOVE_LENGTH, v.cams[0], lp, true, 
				     SlowInSlowOutInterpolator.getInstance(), null);
	application.vsm.getAnimationManager().startAnimation(transAnim,true);
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	lastJPX=jpx;
	lastJPY=jpy;
	v.setDrawDrag(true);
	application.vsm.activeView.mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
	activeCam=application.vsm.getActiveCamera();
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.getAnimationManager().setXspeed(0);
	application.vsm.getAnimationManager().setYspeed(0);
	application.vsm.getAnimationManager().setZspeed(0);
	v.setDrawDrag(false);
	application.vsm.activeView.mouse.setSensitivity(true);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy, int clickNumber, MouseEvent e){
	LongPoint lp = new LongPoint(v.getMouse().vx - v.cams[0].posx, v.getMouse().vy - v.cams[0].posy);

	Animation transAnim = application.vsm.getAnimationManager().getAnimationFactory()
	    .createCameraTranslation(ViewDemo.ANIM_MOVE_LENGTH, v.cams[0], lp, true, 
				     SlowInSlowOutInterpolator.getInstance(), null);
	application.vsm.getAnimationManager().startAnimation(transAnim,true);
    }

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (mod != ALT_MOD && (buttonNumber == 1 || buttonNumber == 3)){
	    tfactor=(activeCam.focal+Math.abs(activeCam.altitude))/activeCam.focal;
	    if (mod == SHIFT_MOD || mod == META_SHIFT_MOD){
		application.vsm.getAnimationManager().setXspeed(0);
		application.vsm.getAnimationManager().setYspeed(0);
		application.vsm.getAnimationManager().setZspeed((activeCam.altitude>0) ? (long)((lastJPY-jpy)*(tfactor/cfactor)) : (long)((lastJPY-jpy)/(tfactor*cfactor)));
		//50 is just a speed factor (too fast otherwise)
	    }
	    else {
		jpxD = jpx-lastJPX;
		jpyD = lastJPY-jpy;
		application.vsm.getAnimationManager().setXspeed((activeCam.altitude>0) ? (long)(jpxD*(tfactor/cfactor)) : (long)(jpxD/(tfactor*cfactor)));
		application.vsm.getAnimationManager().setYspeed((activeCam.altitude>0) ? (long)(jpyD*(tfactor/cfactor)) : (long)(jpyD/(tfactor*cfactor)));
		application.vsm.getAnimationManager().setZspeed(0);
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	Camera c=application.vsm.getActiveCamera();
	float a=(c.focal+Math.abs(c.altitude))/c.focal;
	if (wheelDirection == WHEEL_UP){
	    c.altitudeOffset(a*10);
	}
	else {//wheelDirection == WHEEL_DOWN
	    c.altitudeOffset(-a*10);
	}
    }

    public void enterGlyph(Glyph g){}
    
    public void exitGlyph(Glyph g){}

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
	else if (code==KeyEvent.VK_HOME){application.getGlobalView();}
	else if (code==KeyEvent.VK_UP){application.translateView(ViewDemo.MOVE_UP);}
	else if (code==KeyEvent.VK_DOWN){application.translateView(ViewDemo.MOVE_DOWN);}
	else if (code==KeyEvent.VK_LEFT){application.translateView(ViewDemo.MOVE_LEFT);}
	else if (code==KeyEvent.VK_RIGHT){application.translateView(ViewDemo.MOVE_RIGHT);}
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){application.exit();}

}
