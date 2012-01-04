/*
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2011-2012.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.tests;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.AgileGLCanvasFactory;
import fr.inria.zvtm.engine.AgileNewtCanvasFactory;
import fr.inria.zvtm.engine.AgileGLJPanelFactory;
import fr.inria.zvtm.event.ViewAdapter;

import fr.inria.zvtm.glyphs.*;


public class AgileTest implements Java2DPainter {
	
	VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
	VirtualSpace mSpace;
	View mView;
	Camera mCamera;
	
	public AgileTest(String vt){
		init(vt);
		populate();
		mView.getGlobalView(mCamera, 0);
	}
	
	void init(String vt){
		mSpace = vsm.addVirtualSpace(VirtualSpace.ANONYMOUS);
		mCamera = mSpace.addCamera();
		Vector cameras = new Vector(1);
		cameras.add(mCamera);
		System.out.println(vt);
        if (vt.equals(AgileGLJPanelFactory.AGILE_GLJ_VIEW)){
            System.out.println("Instantiating a GLJPanel-backed view");
            View.registerViewPanelFactory(AgileGLJPanelFactory.AGILE_GLJ_VIEW, new AgileGLJPanelFactory());
    		mView = vsm.addFrameView(cameras, View.ANONYMOUS, AgileGLJPanelFactory.AGILE_GLJ_VIEW, 1600, 1200, true);
        }
        else if (vt.equals(AgileNewtCanvasFactory.AGILE_NEWT_VIEW)){
            System.out.println("Instantiating a NewtCanvas-backed view");
            View.registerViewPanelFactory(AgileNewtCanvasFactory.AGILE_NEWT_VIEW, new AgileNewtCanvasFactory());
    		mView = vsm.addFrameView(cameras, View.ANONYMOUS, AgileNewtCanvasFactory.AGILE_NEWT_VIEW, 1600, 1200, true);
        }
        else {
            System.out.println("Instantiating a GLCanvas-backed view");
            View.registerViewPanelFactory(AgileGLCanvasFactory.AGILE_GLC_VIEW, new AgileGLCanvasFactory());
    		mView = vsm.addFrameView(cameras, View.ANONYMOUS, AgileGLCanvasFactory.AGILE_GLC_VIEW, 1600, 1200, true);
        }
		mView.setBackgroundColor(Color.LIGHT_GRAY);
		mView.setListener(new MainListener(this), 0);
		mView.setRefreshRate(1);
		mView.setJava2DPainter(this, Java2DPainter.FOREGROUND);
	}
	
	void populate(){
		float MAX = 100;
        for (int i=0;i<MAX;i++){
            for (int j=0;j<MAX;j++){
                mSpace.addGlyph(new VRectangle(i*20,j*20,0,18,18,Color.getHSBColor(i/MAX,j/MAX,1)));
            }
        }
	}
	
	public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	    float rr = 1000 / (float)(mView.getPanel().getDelay());
	    g2d.drawString(String.valueOf(rr), 10, 20);
	}
	
	public static void main(String[] args){
        System.out.println("-----------------");
        System.out.println("General information");
        System.out.println("JVM version: "+System.getProperty("java.vm.vendor")+" "+System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version"));
        System.out.println("OS type: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+"/"+System.getProperty("os.arch")+" "+System.getProperty("sun.cpu.isalist"));
        System.out.println("-----------------");
        System.out.println("Directory information");
        System.out.println("Java Classpath: "+System.getProperty("java.class.path"));	
        System.out.println("Java directory: "+System.getProperty("java.home"));
        System.out.println("Launching from: "+System.getProperty("user.dir"));
        System.out.println("-----------------");
        System.out.println("User informations");
        System.out.println("User name: "+System.getProperty("user.name"));
        System.out.println("User home directory: "+System.getProperty("user.home"));
        System.out.println("-----------------");
        new AgileTest((args.length > 0) ? args[0] : AgileGLCanvasFactory.AGILE_GLC_VIEW);
    }
    	
}

class MainListener extends ViewAdapter {
	
	AgileTest application;
	
	int lastJPX, lastJPY;
	
	MainListener(AgileTest app){
		this.application = app;
	}
	
	public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        v.setDrawDrag(true);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        application.mCamera.setXspeed(0);
        application.mCamera.setYspeed(0);
        application.mCamera.setZspeed(0);
        v.setDrawDrag(false);
    }
	
	static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (buttonNumber == 1){
            Camera c = application.mCamera;
            double a = (c.focal+Math.abs(c.altitude)) / c.focal;
            if (mod == SHIFT_MOD) {
                application.mCamera.setXspeed(0);
                application.mCamera.setYspeed(0);
                application.mCamera.setZspeed(((lastJPY-jpy)*(ZOOM_SPEED_COEF)));
                //50 is just a speed factor (too fast otherwise)
            }
            else {
                application.mCamera.setXspeed((c.altitude>0) ? ((jpx-lastJPX)*(a/PAN_SPEED_COEF)) : ((jpx-lastJPX)/(a*PAN_SPEED_COEF)));
                application.mCamera.setYspeed((c.altitude>0) ? ((lastJPY-jpy)*(a/PAN_SPEED_COEF)) : ((lastJPY-jpy)/(a*PAN_SPEED_COEF)));
                application.mCamera.setZspeed(0);
            }
        }
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        Camera c = application.mCamera;
        double a = (c.focal+Math.abs(c.altitude)) / c.focal;
        if (wheelDirection == WHEEL_UP){
            c.altitudeOffset(-a*5);
            VirtualSpaceManager.INSTANCE.repaint();
        }
        else {
            //wheelDirection == WHEEL_DOWN
            c.altitudeOffset(a*5);
            VirtualSpaceManager.INSTANCE.repaint();
        }
    }

    public void enterGlyph(Glyph g){
        g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
        g.highlight(false, null);
    }
    
}
