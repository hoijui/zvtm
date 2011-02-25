/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.demo;

import java.awt.Color;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.Container;
import java.awt.GridLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.inria.zvtm.engine.*;
import fr.inria.zvtm.event.*;
import fr.inria.zvtm.glyphs.*;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

public class DynaSpotDemo {

    VirtualSpaceManager vsm;
    VirtualSpace vs;
    ViewListener eh;   //class that receives the events sent from views (include mouse click, entering object,...)

    View demoView;

    DynaSpotDemo(short ogl){
        vsm = VirtualSpaceManager.INSTANCE;
        vsm.setDebug(true);
        initTest(ogl);
    }

    public void initTest(short ogl){
        eh=new DynaSpotDemoEvtHdlr(this);
        vs = vsm.addVirtualSpace("src");
        vs.addCamera();
        Vector cameras=new Vector();
        cameras.add(vsm.getVirtualSpace("src").getCamera(0));
        vsm.getVirtualSpace("src").getCamera(0).setZoomFloor(-90);
        short vt = View.STD_VIEW;
        switch(ogl){
            case View.OPENGL_VIEW:{vt = View.OPENGL_VIEW;break;}
        }
        demoView = vsm.addFrameView(cameras, "DynaSpot Demo", vt, 800, 600, false, true);
        demoView.setBackgroundColor(Color.WHITE);
        demoView.setListener(eh);
        demoView.setNotifyCursorMoved(true);
        vsm.getVirtualSpace("src").getCamera(0).setAltitude(50);
		vs.addGlyph(new VCircle(-300,0,0,8,Color.BLACK));
		vs.addGlyph(new VCircle(300,0,0,8,Color.BLACK));
        vsm.repaint();
		demoView.getCursor().getDynaPicker().activateDynaSpot(true);
    }

	void setDynaSpotVisibility(short v){
		demoView.getCursor().getDynaPicker().setDynaSpotVisibility(v);
	}
    
    public static void main(String[] args){
        System.out.println("-----------------");
        System.out.println("General information");
        System.out.println("JVM version: "+System.getProperty("java.vm.vendor")+" "+System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version"));
        System.out.println("OS type: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+"/"+System.getProperty("os.arch")+" "+System.getProperty("sun.cpu.isalist"));
        System.out.println("-----------------");
        new DynaSpotDemo((args.length > 0) ? Short.parseShort(args[0]) : 0);
    }
    
}

class DynaSpotDemoEvtHdlr implements ViewListener {

    DynaSpotDemo application;

    double lastX,lastY;
    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)

    DynaSpotDemoEvtHdlr(DynaSpotDemo appli){
        application = appli;
    }

    double x1,x2,y1,y2;

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){

    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

    }

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
    }

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
    }

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        //application.vsm.setSync(false);
        lastJPX=jpx;
        lastJPY=jpy;
        //application.vsm.animator.setActiveCam(v.cams[0]);
        v.setDrawDrag(true);
        application.vsm.getActiveView().mouse.setSensitivity(false);
        //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        v.cams[0].setXspeed(0);
        v.cams[0].setYspeed(0);
        v.cams[0].setZspeed(0);
        v.setDrawDrag(false);
        application.vsm.getActiveView().mouse.setSensitivity(true);
        /*Camera c=v.cams[0];
        application.vsm.getAnimationManager().createCameraAnimation(500,2,new LongPoint(lastX-application.vsm.mouse.vx,lastY-application.vsm.mouse.vy),c.getID());*/
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        v.getVCursor().getDynaPicker().dynaPick(v.cams[0]);
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
            Camera c=application.vsm.getActiveCamera();
            double a=(c.focal+Math.abs(c.altitude))/c.focal;
            if (mod == META_SHIFT_MOD) {
                v.cams[0].setXspeed(0);
                v.cams[0].setYspeed(0);
                v.cams[0].setZspeed((c.altitude>0) ? (lastJPY-jpy)*(a/50.0f) : (lastJPY-jpy)/(a*50));
                //50 is just a speed factor (too fast otherwise)
            }
            else {
                v.cams[0].setXspeed((c.altitude>0) ? (jpx-lastJPX)*(a/50.0f) : (jpx-lastJPX)/(a*50));
                v.cams[0].setYspeed((c.altitude>0) ? (lastJPY-jpy)*(a/50.0f) : (lastJPY-jpy)/(a*50));
                v.cams[0].setZspeed(0);
            }
        }
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        Camera c=application.vsm.getActiveCamera();
        double a=(c.focal+Math.abs(c.altitude))/c.focal;
        if (wheelDirection == WHEEL_UP){
            c.altitudeOffset(-a*5);
            application.vsm.repaint();
        }
        else {
            //wheelDirection == WHEEL_DOWN
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
    
    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}
    
    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

}
