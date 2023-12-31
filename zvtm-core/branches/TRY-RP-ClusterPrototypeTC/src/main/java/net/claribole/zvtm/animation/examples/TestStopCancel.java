/*   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Romain Primet
 *   Copyright (c) INRIA, 2009
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

package net.claribole.zvtm.animation.examples;

import java.awt.*;
import javax.swing.*;

import java.util.Vector;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;
import net.claribole.zvtm.glyphs.*;
import net.claribole.zvtm.widgets.*;

import net.claribole.zvtm.animation.*;
import net.claribole.zvtm.animation.interpolation.*;
import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.timing.interpolation.*;

// Checking that animation stopping is handled correctly
// Also, check stop vs cancel behavior in different situations
public class TestStopCancel {

    VirtualSpaceManager vsm;
    VirtualSpace vs;
    ViewEventHandler eh;   //class that receives the events sent from views (include mouse click, entering object,...)

    View testView;

    TestStopCancel(){
        vsm=new VirtualSpaceManager();
        vsm.setDebug(true);
    }

    public void startAnim(short ogl){
        eh=new TestStopCancel.MyEventHandler(this);
        vs = vsm.addVirtualSpace("src");
        vsm.setZoomLimit(-90);
        vsm.addCamera("src");
        Vector cameras=new Vector();
        cameras.add(vsm.getVirtualSpace("src").getCamera(0));
        short vt = View.STD_VIEW;
        switch(ogl){
	case View.OPENGL_VIEW:{vt = View.OPENGL_VIEW;break;}
	case View.VOLATILE_VIEW:{vt = View.VOLATILE_VIEW;break;}
        }
        testView = vsm.addExternalView(cameras, "Test", vt, 800, 600, false, true);
        testView.setBackgroundColor(Color.LIGHT_GRAY);
        testView.setEventHandler(eh);
        testView.setNotifyMouseMoved(true);
        vsm.getVirtualSpace("src").getCamera(0).setAltitude(50);

	AnimationManager am = vsm.getAnimationManager();

	final Glyph circle = new VCircle(0,200,0,30,Color.BLUE);
	vsm.addGlyph(circle, "src");

	final Glyph circle2 = new VCircle(60,200,0,30,Color.BLUE);
	vsm.addGlyph(circle2, "src");

	final Glyph circle3 = new VCircle(90,200,0,30,Color.BLUE);
	vsm.addGlyph(circle3, "src");

	final Glyph circle4 = new VCircle(120,200,0,30,Color.BLUE);
	vsm.addGlyph(circle4, "src");

	class ColorEndAction implements EndAction {
		public void execute(Object subject, 
				Animation.Dimension dimension){
			((Glyph)subject).setColor(Color.GREEN);
		}
	}

	Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(
				5000,
				circle,
				new LongPoint(0,-200),
				true,
				SlowInSlowOutInterpolator.getInstance(),
				new ColorEndAction());

	Animation anim2 = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(
				5000,
				circle2,
				new LongPoint(0,-200),
				true,
				SlowInSlowOutInterpolator.getInstance(),
				new ColorEndAction());

	Animation anim3 = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(
				5000,
				circle3,
				new LongPoint(0,-200),
				true,
				SlowInSlowOutInterpolator.getInstance(),
				new ColorEndAction());

	Animation anim4 = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(
				5000,
				circle4,
				new LongPoint(0,-200),
				true,
				SlowInSlowOutInterpolator.getInstance(),
				new ColorEndAction());

	Animation anim5 = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(
				5000,
				circle3,
				new LongPoint(0,-200),
				true,
				SlowInSlowOutInterpolator.getInstance(),
				new ColorEndAction());

	Animation anim6 = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(
				5000,
				circle4,
				new LongPoint(0,-200),
				true,
				SlowInSlowOutInterpolator.getInstance(),
				new ColorEndAction());

	am.startAnimation(anim, false);
	am.startAnimation(anim2, false);
	am.startAnimation(anim3, false);
	am.startAnimation(anim4, false);
	am.startAnimation(anim5, false);
	am.startAnimation(anim6, false);

	try{
	    Thread.sleep(1000);
	} catch(InterruptedException ie){
	    //do not act on interruption
	}

	am.stopAnimation(anim); //end action should execute (green circle)
	am.cancelAnimation(anim2); //end action should not execute (blue)
	am.stopAnimation(anim5); //stop unstarted animation
	am.cancelAnimation(anim6); //cancel unstarted animation
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
        new TestStopCancel().startAnim((args.length > 0) ? Short.parseShort(args[0]) : 0);
    }

    class MyEventHandler implements ViewEventHandler{
	TestStopCancel application;

	long lastX,lastY,lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)

	MyEventHandler(TestStopCancel appli){
	    application=appli;
	}

	long x1,x2,y1,y2;

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

	    v.setDrawDrag(true);
	    application.vsm.activeView.mouse.setSensitivity(false);
	    //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
	}

	public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	    application.vsm.getAnimationManager().setXspeed(0);
	    application.vsm.getAnimationManager().setYspeed(0);
	    application.vsm.getAnimationManager().setZspeed(0);
	    v.setDrawDrag(false);
	    application.vsm.activeView.mouse.setSensitivity(true);
	}

	public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){

	}

	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	    if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
		Camera c=application.vsm.getActiveCamera();
		float a=(c.focal+Math.abs(c.altitude))/c.focal;
		if (mod == META_SHIFT_MOD) {
		    application.vsm.getAnimationManager().setXspeed(0);
		    application.vsm.getAnimationManager().setYspeed(0);
		    application.vsm.getAnimationManager().setZspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
		    //50 is just a speed factor (too fast otherwise)
		}
		else {
		    application.vsm.getAnimationManager().setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/50.0f)) : (long)((jpx-lastJPX)/(a*50)));
		    application.vsm.getAnimationManager().setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
		    application.vsm.getAnimationManager().setZspeed(0);
		}
	    }
	}

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	    Camera c=application.vsm.getActiveCamera();
	    float a=(c.focal+Math.abs(c.altitude))/c.focal;
	    if (wheelDirection == WHEEL_UP){
		c.altitudeOffset(-a*5);
		application.vsm.repaintNow();
	    }
	    else {
		c.altitudeOffset(a*5);
		application.vsm.repaintNow();
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
}
