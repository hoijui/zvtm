/*   FILE: Test.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2004-2011.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.tests;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;

import java.util.Vector;

import fr.inria.zvtm.engine.*;
import fr.inria.zvtm.animation.*;
import fr.inria.zvtm.animation.interpolation.*;
import fr.inria.zvtm.glyphs.*;
import fr.inria.zvtm.event.*;

import java.awt.font.*;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;

public class Test implements Java2DPainter {

    VirtualSpaceManager vsm;
    VirtualSpace vs;
    ViewListener eh;

    View testView;
    Camera cam;
    
    Test(String vt){
        vsm=VirtualSpaceManager.INSTANCE;
        vsm.setDebug(true);
        initTest(vt);
    }

	VCircle c = new VCircle(0, 100, 0, 20, Color.GREEN, Color.BLACK);
	VEclipse e = new VEclipse(0, 100, 0, 20, -.5f, Color.BLACK, Color.BLACK, .5f);

    public void initTest(String vt){
        eh=new EventHandlerTest(this);
        vs = vsm.addVirtualSpace(VirtualSpace.ANONYMOUS);
        cam = vs.addCamera();
        Vector cameras=new Vector();
        cameras.add(vs.getCamera(0));
        vs.getCamera(0).setZoomFloor(-90f);
        testView = vsm.addFrameView(cameras, View.ANONYMOUS, View.OPENGL_VIEW, 800, 600, false, true, true, null);
        testView.setBackgroundColor(Color.LIGHT_GRAY);
        testView.setListener(eh);
        testView.setAntialiasing(true);
		testView.setJava2DPainter(this, Java2DPainter.FOREGROUND);

		vs.addGlyph(c);
		vs.addGlyph(e);
		
		
        vsm.repaint();
    }

	void animateS(){
		Animation a1 = vsm.getAnimationManager().getAnimationFactory().createGlyphSizeAnim(
			1000, c, 2*c.getSize(), false, SlowInSlowOutInterpolator.getInstance(), null);
		vsm.getAnimationManager().startAnimation(a1, false);
		Animation a2 = vsm.getAnimationManager().getAnimationFactory().createGlyphSizeAnim(
			1000, e, 2*e.getSize(), false, SlowInSlowOutInterpolator.getInstance(), null);
		vsm.getAnimationManager().startAnimation(a2, false);
	}

	void animateR(){
		Animation a1 = vsm.getAnimationManager().getAnimationFactory().createGlyphOrientationAnim(
			1000, c, Math.PI, true, SlowInSlowOutInterpolator.getInstance(), null);
		vsm.getAnimationManager().startAnimation(a1, false);
		Animation a2 = vsm.getAnimationManager().getAnimationFactory().createGlyphOrientationAnim(
			1000, e, Math.PI, true, SlowInSlowOutInterpolator.getInstance(), null);
		vsm.getAnimationManager().startAnimation(a2, false);
	}

	public void paint(Graphics2D g2d, int viewWidth, int viewHeight){}
	
    
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
        new Test((args.length > 0) ? args[0] : View.STD_VIEW);
    }
    
}

class EventHandlerTest implements ViewListener {

    Test application;

    //remember last mouse coords to compute translation  (dragging)
    long lastJPX,lastJPY;

    EventHandlerTest(Test appli){
        application=appli;
    }
    
    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){application.animateR();}

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){application.animateS();}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX=jpx;
        lastJPY=jpy;
        v.setDrawDrag(true);
        application.vsm.getActiveView().mouse.setSensitivity(false);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        application.cam.setXspeed(0);
        application.cam.setYspeed(0);
        application.cam.setZspeed(0);
        v.setDrawDrag(false);
        application.vsm.getActiveView().mouse.setSensitivity(true);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
		if (buttonNumber == 3){
            Camera c=application.vsm.getActiveCamera();
            double a=(c.focal+Math.abs(c.altitude))/c.focal;
            if (mod == META_SHIFT_MOD) {
                application.cam.setXspeed(0);
                application.cam.setYspeed(0);
                application.cam.setZspeed(((lastJPY-jpy)*(ZOOM_SPEED_COEF)));
                //50 is just a speed factor (too fast otherwise)
            }
            else {
                application.cam.setXspeed((c.altitude>0) ? ((jpx-lastJPX)*(a/PAN_SPEED_COEF)) : ((jpx-lastJPX)/(a*PAN_SPEED_COEF)));
                application.cam.setYspeed((c.altitude>0) ? ((lastJPY-jpy)*(a/PAN_SPEED_COEF)) : ((lastJPY-jpy)/(a*PAN_SPEED_COEF)));
                application.cam.setZspeed(0);
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
    
    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code==KeyEvent.VK_C){}
	}
    
    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

}
