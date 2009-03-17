/*   FILE: TestCircle.java
 *   DATE OF CREATION:   Jul 11 2000
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
 */

package net.claribole.zvtm.animation.examples;

import java.awt.*;
import javax.swing.*;

import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;
import net.claribole.zvtm.glyphs.*;
import net.claribole.zvtm.widgets.*;

import net.claribole.zvtm.animation.*;
import org.jdesktop.animation.timing.*;

public class TestCircle {

    VirtualSpaceManager vsm;
    VirtualSpace vs;
    ViewEventHandler eh;   //class that receives the events sent from views (include mouse click, entering object,...)

    View testView;

    TestCircle(short ogl){
        vsm=new VirtualSpaceManager();
        vsm.setDebug(true);
        //vsm.setDefaultMultiFills(true);
        initTest(ogl);
    }

    public void initTest(short ogl){
        eh=new EventHandlerTest(this);
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
	final Glyph circle = new VCircle(100,0,0,40,Color.WHITE);
        vsm.getVirtualSpace("src").getCamera(0).setAltitude(50);
	vsm.addGlyph(circle, "src");
        vsm.repaintNow();

	AnimationManager am = new AnimationManager();
	for(int i=0; i<4; ++i){
	    Animation anim = am.createAnimation(2000, //2 seconds
						1.0,
						Animator.RepeatBehavior.LOOP,
						circle,
						Animation.Dimension.POSITION,
						new TimingHandler(){
						    public void begin(Object subject, Animation.Dimension dim){}
						    public void end(Object subject, Animation.Dimension dim){}
						    public void repeat(Object subject, Animation.Dimension dim){}
    public void timingEvent(float fraction, 
			    Object subject, Animation.Dimension dim){
	Glyph g = (Glyph)subject;
	g.moveTo(100 - Float.valueOf(100*fraction).longValue(), 0);
    }
						});
	    am.addAnimation(anim);
	    am.startAnimation(anim, false);
	}

	 Animation anim = am.createAnimation(8000, 
						1.0,
						Animator.RepeatBehavior.LOOP,
						circle,
						Animation.Dimension.FILLCOLOR,
						new TimingHandler(){
						    public void begin(Object subject, Animation.Dimension dim){}
						    public void end(Object subject, Animation.Dimension dim){}
						    public void repeat(Object subject, Animation.Dimension dim){}
    public void timingEvent(float fraction, 
			    Object subject, Animation.Dimension dim){
	Glyph g = (Glyph)subject;
	g.setColor(new Color(0,
			     0,
			     Float.valueOf(255*fraction).intValue()));
    }
						});
	    am.addAnimation(anim);
	    am.startAnimation(anim, false);

	 Animation anim2 = am.createAnimation(4000, 
					      1.0,
					      Animator.RepeatBehavior.LOOP,
					      circle,
					      Animation.Dimension.SIZE,
					      new TimingHandler(){
						 public void begin(Object subject, Animation.Dimension dim){}
						 public void end(Object subject, Animation.Dimension dim){}
						 public void repeat(Object subject, Animation.Dimension dim){}
    public void timingEvent(float fraction, 
			    Object subject, Animation.Dimension dim){
	Glyph g = (Glyph)subject;
	g.sizeTo(40+60*fraction);
    }
					      });
	 am.addAnimation(anim2);
	    am.startAnimation(anim2, false);

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
        new TestCircle((args.length > 0) ? Short.parseShort(args[0]) : 0);
    }
    
}
