/*   FILE: ViewDemo.java
 *   DATE OF CREATION:  Fri Aug 26 15:12:06 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.demo;

import java.awt.Color;
import java.util.Vector;
import javax.swing.SwingUtilities;

import net.claribole.zvtm.animation.Animation;
import net.claribole.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import net.claribole.zvtm.engine.Java2DPainter;
import net.claribole.zvtm.engine.ViewEventHandler;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.SwingWorker;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VRectangleST;

public class ViewDemo {

    VirtualSpaceManager vsm;

    String mainSpaceName = "demoSpace";
    String mainViewName = "View";

    ViewEventHandler eh;

    static short MOVE_UP=0;
    static short MOVE_DOWN=1;
    static short MOVE_LEFT=2;
    static short MOVE_RIGHT=3;
    static int ANIM_MOVE_LENGTH = 500;

    FrameRateIndicator fri;

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
	vsm.addVirtualSpace(mainSpaceName);
	Vector cameras=new Vector();
	mCamera = vsm.addCamera(mainSpaceName);
	mCamera.setZoomFloor(-90);
	cameras.add(mCamera);
	demoView = vsm.addExternalView(cameras, mainViewName, viewType, 800, 600, false, true);
	demoView.setBackgroundColor(Color.WHITE);
	demoView.setEventHandler(eh);
	fri = new FrameRateIndicator(this);
	demoView.setJava2DPainter(fri, Java2DPainter.FOREGROUND);
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
		    r = new VRectangleST(2*i*cw, 2*j*ch, 0, tw, th, Color.getHSBColor((float)((i*j)/10000.0), 1.0f,1.0f));
		}
		else {
		    r = new VRectangle(2*i*cw, 2*j*ch, 0, tw, th, Color.getHSBColor((float)((i*j)/10000.0), 1.0f,1.0f));
		}
		r.setDrawBorder(false);
		vsm.addGlyph(r, mainSpaceName, false);
		pf.setPBValue(i+j/100);
	    }
	}
	vsm.repaintNow();
	pf.destroy();
	vsm.getGlobalView(vsm.getVirtualSpace(mainSpaceName).getCamera(0), 400);
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
	vsm.getGlobalView(vsm.getActiveCamera(), ViewDemo.ANIM_MOVE_LENGTH);
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

class FrameRateIndicator implements Java2DPainter {

    ViewDemo application;

    FrameRateIndicator(ViewDemo app){
	application = app;
    }

    long d1,d2;
    
    public void paint(java.awt.Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setColor(Color.black);
	d1 = application.demoView.getPanel().loopTotalTime;
	if (d1 > 0){ d2 = 1000/d1;}
	g2d.drawString(Long.toString(d2) + " fps", viewWidth-50, 30);
	g2d.drawString(viewWidth + " x " + viewHeight, 20, viewHeight - 70);
	g2d.drawString(application.vts, 20, viewHeight - 50);
	g2d.drawString(application.tms, 20, viewHeight - 30);
    }
    
}