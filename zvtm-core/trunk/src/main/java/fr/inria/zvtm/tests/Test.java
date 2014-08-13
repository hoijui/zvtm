/*
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2011-2012.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.tests;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.ImageIcon;

import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.portals.CameraPortal;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.DefaultTimingHandler;
import fr.inria.zvtm.animation.interpolation.ConstantAccInterpolator;
import fr.inria.zvtm.event.ViewAdapter;

import fr.inria.zvtm.glyphs.*;


public class Test implements Java2DPainter {

	/* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1600;
    static int VIEW_MAX_H = 1200;
    int VIEW_W, VIEW_H;

	static final Font FPS_FONT = new Font("Arial", Font.PLAIN, 12);

	static final float DEFAULT_MAX = 40;
	float MAX = DEFAULT_MAX;

	VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
	VirtualSpace mSpace, sSpace;
	View mView;
	Camera mCamera, sCamera;

	public Test(String vt, float nbObj){
		init(vt);
		MAX = nbObj;
		populate();
		//mView.getGlobalView(mCamera, 0);
	}

    void init(String vt){
        windowLayout();
        mSpace = vsm.addVirtualSpace(VirtualSpace.ANONYMOUS);
        sSpace = vsm.addVirtualSpace(VirtualSpace.ANONYMOUS);
        mCamera = mSpace.addCamera();
        sCamera = sSpace.addCamera();
        Vector cameras = new Vector(2);
        cameras.add(mCamera);
        // cameras.add(sCamera);
        if (vt.equals("ogl")){
            System.out.println("Instantiating a regular Java2D view with Sun's OGL pipeline");
            mView = vsm.addFrameView(cameras, View.ANONYMOUS, View.OPENGL_VIEW, VIEW_W, VIEW_H, true);
        }
        else {
            System.out.println("Instantiating a regular Java2D view");
            mView = vsm.addFrameView(cameras, View.ANONYMOUS, View.STD_VIEW, VIEW_W, VIEW_H, true);
        }
        mView.setBackgroundColor(Color.LIGHT_GRAY);
        mView.setListener(new MainListener(this), 0);
        mView.setRefreshRate(1);
        mView.setJava2DPainter(this, Java2DPainter.FOREGROUND);
    }

	void windowLayout(){
        VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
        VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }

	void populate(){
        VCircle c = new VCircle(500, 100, 0, 10, Color.RED, Color.BLACK);
        mSpace.addGlyph(c);
        c = new VCircle(500, 200, 0, 10, Color.RED, Color.BLACK);
        mSpace.addGlyph(c);
        c = new VCircle(500, 300, 0, 10, Color.RED, Color.BLACK);
        mSpace.addGlyph(c);
        VText t = new VText(500, 100, 0, Color.BLACK, "TEXT_ALIGN_LEFT", VText.TEXT_ANCHOR_START);
        // t.setScaleIndependent(true);
        mSpace.addGlyph(t);
        t = new VText(500, 200, 0, Color.BLACK, "TEXT_ALIGN_MIDDLE", VText.TEXT_ANCHOR_MIDDLE);
        // t.setScaleIndependent(true);
        mSpace.addGlyph(t);
        t = new VText(500, 300, 0, Color.BLACK, "TEXT_ALIGN_RIGHT", VText.TEXT_ANCHOR_END);
        // t.setScaleIndependent(true);
        mSpace.addGlyph(t);
	}


	public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
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
        String vt = "";
        float nbObj = DEFAULT_MAX;
        for (String arg:args){
            if (arg.equals("ogl")){vt = arg;}
            else {
                try {
                    nbObj = Integer.parseInt(arg);
                }
                catch (NumberFormatException ex){
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
        }
	    if (vt.equals("ogl")){
	       System.setProperty("sun.java2d.opengl", "True");
	    }
        new Test(vt, nbObj);
    }

}

class MainListener extends ViewAdapter {

	Test application;

	int lastJPX, lastJPY;

	MainListener(Test app){
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

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

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

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
    }
    
    public void enterGlyph(Glyph g){
        g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
        g.highlight(false, null);
    }

    public void viewClosing(View v){
        System.exit(0);
    }

}
