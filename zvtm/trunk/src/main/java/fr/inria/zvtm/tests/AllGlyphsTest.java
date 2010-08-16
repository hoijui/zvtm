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
        // reference frame
        for (int i=0;i<6;i++){
            for (int j=0;j<10;j++){
                vs.addGlyph(new VRectangle(i*40,j*40,0,20,20,Color.GRAY));
            }
        }
        // circles
        VCircle c = new VCircle(0,0,0,20,Color.WHITE);
        vs.addGlyph(c);
        c = new VCircle(40,0,0,10,Color.RED);
        c.sizeTo(20);
        vs.addGlyph(c);
        // rectangles
        VRectangle r = new VRectangle(0,40,0,20,10,Color.WHITE);
        vs.addGlyph(r);
        r = new VRectangle(40,40,0,10,5,Color.RED);
        r.sizeTo(22.36f);
        vs.addGlyph(r);
        r = new VRectangle(80,40,0,10,5,Color.BLUE);
        r.setWidth(20);
        r.setHeight(10);
        vs.addGlyph(r);
        FRectangle fr = new FRectangle(120,40,0,20,10,Color.GREEN,1f,0f);
        vs.addGlyph(fr);
        VRectangleOr or = new VRectangleOr(160,40,0,20,10,Color.YELLOW,(float)Math.PI/2f);
        vs.addGlyph(or);
        or = new VRectangleOr(160,40,0,10,5,Color.YELLOW,(float)Math.PI/2f);
        or.sizeTo(22.36f);
        vs.addGlyph(or);
        or = new VRectangleOr(200,40,0,10,5,Color.ORANGE,(float)Math.PI/2f);
        or.setWidth(20);
        or.setHeight(10);
        vs.addGlyph(or);
        // path
        DPath d = new DPath(0, 80, 0, Color.BLACK);
        d.addSegment(20, 20, false);
        d.addQdCurve(0, -40, -20, -20, false);
        d.addCbCurve(-40, 20, -40, 0, 0, 20, false);
        vs.addGlyph(d);
        // polygons
        LongPoint[] vertices = {new LongPoint(-20,120), new LongPoint(0,140), new LongPoint(20,120), new LongPoint(0,100)};
        FPolygon fp = new FPolygon(vertices, Color.WHITE);
        vs.addGlyph(fp);
        LongPoint[] vertices2 = {new LongPoint(0,0), new LongPoint(20,20), new LongPoint(40,0), new LongPoint(20,-20)};
        fp = new FPolygon(vertices2, Color.RED);
        fp.moveTo(40, 120);
        vs.addGlyph(fp);
        
        LongPoint[] vertices3 = {new LongPoint(60,120), new LongPoint(80,140), new LongPoint(100,120), new LongPoint(80,100)};
        VPolygon vp = new VPolygon(vertices3, 0, Color.BLUE);
        vs.addGlyph(vp);
        LongPoint[] vertices4 = {new LongPoint(0,0), new LongPoint(20,20), new LongPoint(40,0), new LongPoint(20,-20)};
        vp = new VPolygon(vertices4, 0, Color.GREEN);
        vp.moveTo(120, 120);
        vs.addGlyph(vp);
        LongPoint[] vertices5 = {new LongPoint(0,0), new LongPoint(10,10), new LongPoint(20,0), new LongPoint(10,-10)};
        vp = new VPolygon(vertices5, 0, Color.YELLOW);
        vp.moveTo(160, 120);
        vp.sizeTo(20);
        vs.addGlyph(vp);
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
