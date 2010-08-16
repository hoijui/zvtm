/*
 * AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.tests;

import java.awt.Color;

import java.util.Vector;

import fr.inria.zvtm.engine.*;
import fr.inria.zvtm.animation.*;
import fr.inria.zvtm.animation.interpolation.*;
import fr.inria.zvtm.glyphs.*;

public class AllGlyphsTest {

    VirtualSpaceManager vsm;
    VirtualSpace vs;
    ViewEventHandler eh;

    View testView;
    
    AllGlyphsTest(short ogl){
        vsm = VirtualSpaceManager.INSTANCE;
        vsm.setDebug(true);
        initTest(ogl);
    }

    public void initTest(short ogl){
        eh = new TestEventHandler(this);
        vs = vsm.addVirtualSpace("s1");
        Vector cameras = new Vector();
        cameras.add(vs.addCamera());
        vs.getCamera(0).setZoomFloor(-90f);
        short vt = View.STD_VIEW;
        switch(ogl){
            case View.OPENGL_VIEW:{vt = View.OPENGL_VIEW;break;}
        }
        testView = vsm.addFrameView(cameras, "All Glyphs Test", vt, 1024, 768, false, true, true, null);
        testView.setBackgroundColor(Color.LIGHT_GRAY);
        testView.setEventHandler(eh);
        testView.setNotifyMouseMoved(true);
        vs.getCamera(0).setAltitude(0);
        populate();
        vsm.repaintNow();
    }

    void populate(){
        // circles
        VCircle c = new VCircle(0,0,0,20,Color.WHITE);
        vs.addGlyph(c);
        c = new VCircle(40,0,0,10,Color.WHITE);
        c.sizeTo(20);
        vs.addGlyph(c);
        // rectangles
        VRectangle r = new VRectangle(0,40,0,20,10,Color.WHITE);
        vs.addGlyph(r);
        r = new VRectangle(40,40,0,10,5,Color.WHITE);
        r.sizeTo(22.36f);
        vs.addGlyph(r);
        r = new VRectangle(80,40,0,10,5,Color.WHITE);
        r.setWidth(20);
        r.setHeight(10);
        vs.addGlyph(r);
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
        new AllGlyphsTest((args.length > 0) ? Short.parseShort(args[0]) : 0);
    }
    
}

class TestEventHandler extends DefaultEventHandler {
    
    AllGlyphsTest test;
    
    TestEventHandler(AllGlyphsTest t){
        this.test = t;
    }
    
    
}
