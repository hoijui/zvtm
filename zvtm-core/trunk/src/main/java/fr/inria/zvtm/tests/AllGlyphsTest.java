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
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.ImageIcon;
import java.awt.geom.Point2D;

import java.util.Vector;

import fr.inria.zvtm.engine.*;
import fr.inria.zvtm.animation.*;
import fr.inria.zvtm.animation.interpolation.*;
import fr.inria.zvtm.glyphs.*;

public class AllGlyphsTest {

    VirtualSpaceManager vsm;
    VirtualSpace vs;
    ViewEventHandler eh;
    Camera mCam;

    View testView;
    
    AllGlyphsTest(short ogl){
        vsm = VirtualSpaceManager.INSTANCE;
        vsm.setDebug(true);
        initTest(ogl);
    }

    public void initTest(short ogl){
        eh = new TestEventHandler(this);
        vs = vsm.addVirtualSpace("s1");
        mCam = vs.addCamera();
        Vector cameras = new Vector();
        cameras.add(mCam);
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
        testView.getGlobalView(mCam, 500, 1.5f);
    }

    void populate(){
        // reference frame
        for (int i=0;i<6;i++){
            for (int j=0;j<15;j++){
                Glyph g = new VRectangle(i*40,j*40,0,40,40,Color.GRAY, Color.LIGHT_GRAY);
                g.setSensitivity(false);
                vs.addGlyph(g);
            }
        }
        // circles
        VCircle c = new VCircle(0,0,0,40,Color.WHITE);
        vs.addGlyph(c);
        c = new VCircle(40,0,0,20,Color.RED);
        c.sizeTo(40);
        vs.addGlyph(c);
        c = new VCircle(0,0,0,20,Color.BLUE);
        c.sizeTo(40);
        c.moveTo(80, 0);
        vs.addGlyph(c);
        // rectangles
        VRectangle r = new VRectangle(0,40,0,40,20,Color.WHITE);
        vs.addGlyph(r);
        r = new VRectangle(40,40,0,10,5,Color.RED);
        r.sizeTo(Math.sqrt(Math.pow(40,2)+Math.pow(20,2)));
        vs.addGlyph(r);
        r = new VRectangle(80,40,0,10,5,Color.BLUE);
        r.setWidth(40);
        r.setHeight(20);
        vs.addGlyph(r);
        FRectangle fr = new FRectangle(120,40,0,40,20,Color.GREEN,1f,0f);
        vs.addGlyph(fr);
        VRectangleOr or = new VRectangleOr(160,40,0,10,5,Color.YELLOW,Math.PI/2f);
        or.sizeTo(Math.sqrt(Math.pow(40,2)+Math.pow(20,2)));
        vs.addGlyph(or);
        or = new VRectangleOr(200,40,0,10,5,Color.ORANGE,Math.PI/2f);
        or.setWidth(40);
        or.setHeight(20);
        vs.addGlyph(or);
        // path
        DPath d = new DPath(0, 80, 0, Color.WHITE);
        d.addSegment(20, 20, false);
        d.addQdCurve(0, -40, -20, -20, false);
        d.addCbCurve(-40, 20, -40, 0, 0, 20, false);
        vs.addGlyph(d);
        // polygons
        Point2D.Double[] vertices = {new Point2D.Double(-20,120), new Point2D.Double(0,140), new Point2D.Double(20,120), new Point2D.Double(0,100)};
        FPolygon fp = new FPolygon(vertices, Color.WHITE);
        vs.addGlyph(fp);
        Point2D.Double[] vertices2 = {new Point2D.Double(0,0), new Point2D.Double(20,20), new Point2D.Double(40,0), new Point2D.Double(20,-20)};
        fp = new FPolygon(vertices2, Color.RED);
        fp.moveTo(40, 120);
        vs.addGlyph(fp);
        Point2D.Double[] vertices3 = {new Point2D.Double(60,120), new Point2D.Double(80,140), new Point2D.Double(100,120), new Point2D.Double(80,100)};
        VPolygon vp = new VPolygon(vertices3, 0, Color.BLUE);
        vs.addGlyph(vp);
        Point2D.Double[] vertices4 = {new Point2D.Double(0,0), new Point2D.Double(20,20), new Point2D.Double(40,0), new Point2D.Double(20,-20)};
        vp = new VPolygon(vertices4, 0, Color.GREEN);
        vp.moveTo(120, 120);
        vs.addGlyph(vp);
        Point2D.Double[] vertices5 = {new Point2D.Double(0,0), new Point2D.Double(10,10), new Point2D.Double(20,0), new Point2D.Double(10,-10)};
        vp = new VPolygon(vertices5, 0, Color.YELLOW);
        vp.moveTo(160, 120);
        vp.sizeTo(40);
        vs.addGlyph(vp);
        // point
        VPoint pt = new VPoint(0, 160, 0, Color.WHITE);
        vs.addGlyph(pt);
        pt = new VPoint(10, 10, 0, Color.RED);
        pt.moveTo(40, 160);
        vs.addGlyph(pt);
        // ellipses
        VEllipse e = new VEllipse(0, 280, 0, 20, 10, Color.WHITE);
        vs.addGlyph(e);
        e = new VEllipse(40, 280, 0, 10, 5, Color.RED);
        e.setWidth(20);
        e.setHeight(10);
        vs.addGlyph(e);
        e = new VEllipse(0, 0, 0, 10, 5, Color.BLUE);
        e.moveTo(80, 280);
        e.setWidth(20);
        e.setHeight(10);
        vs.addGlyph(e);
        // text
        String txt = "Test";
        VText tx = new VText(0, 302, 0, Color.WHITE, txt, VText.TEXT_ANCHOR_START);
        vs.addGlyph(tx);
        tx = new VText(0, 315, 0, Color.WHITE, txt, VText.TEXT_ANCHOR_MIDDLE);
        vs.addGlyph(tx);
        tx = new VText(0, 328, 0, Color.WHITE, txt, VText.TEXT_ANCHOR_END);
        vs.addGlyph(tx);
        tx = new VText(0, 302, 0, Color.RED, txt, VText.TEXT_ANCHOR_START);
        tx.moveTo(40, 302);
        vs.addGlyph(tx);
        tx = new VText(0, 315, 0, Color.RED, txt, VText.TEXT_ANCHOR_MIDDLE);
        tx.moveTo(40, 315);
        vs.addGlyph(tx);
        tx = new VText(0, 328, 0, Color.RED, txt, VText.TEXT_ANCHOR_END);
        tx.moveTo(40, 328);
        vs.addGlyph(tx);
        tx = new VTextOr(0, 320, 0, Color.BLUE, txt, (float)Math.PI/2f, VText.TEXT_ANCHOR_START);
        tx.moveTo(67, 302);
        vs.addGlyph(tx);
        tx = new VTextOr(0, 320, 0, Color.BLUE, txt, (float)Math.PI/2f, VText.TEXT_ANCHOR_MIDDLE);
        tx.moveTo(80, 315);
        vs.addGlyph(tx);
        tx = new VTextOr(0, 320, 0, Color.BLUE, txt, (float)Math.PI/2f, VText.TEXT_ANCHOR_END);
        tx.moveTo(93, 328);
        vs.addGlyph(tx);
        // shapes
        float[] vertices6 = {1f, .5f, 1f, .5f, 1f, .5f, 1f, .5f};
        VShape s = new VShape(0, 360, 0, 20, vertices6, Color.WHITE, 0);
        vs.addGlyph(s);
        s = new VShape(40, 360, 0, 20, vertices6, Color.RED, (float)Math.PI);
        vs.addGlyph(s);
        s = new VShape(0, 360, 0, 10, vertices6, Color.BLUE, (float)Math.PI);
        s.sizeTo(20);
        s.moveTo(80, 360);
        vs.addGlyph(s);
        // round rectangles
        VRoundRect rr = new VRoundRect(0, 400, 0, 20, 10, Color.WHITE, 4, 4);
        vs.addGlyph(rr);
        rr = new VRoundRect(40, 400, 0, 10, 5, Color.RED, 4, 4);
        rr.setWidth(20);
        rr.setHeight(10);
        vs.addGlyph(rr);
        rr = new VRoundRect(0, 0, 0, 10, 5, Color.BLUE, 4, 4);
        rr.moveTo(80, 400);
        rr.setWidth(20);
        rr.setHeight(10);
        vs.addGlyph(rr);
        // segments
        VSegment sg = new VSegment(-20, 420, 20, 460, 0, Color.WHITE);
        vs.addGlyph(sg);
        sg = new VSegment(-20, 420, 20, 460, 0, Color.RED);
        sg.moveTo(40, 440);
        vs.addGlyph(sg);
        sg = new VSegment(80, 440, 0, Color.BLUE, (float)Math.sqrt(Math.pow(20,2)+Math.pow(20,2)), (float)Math.PI/4f);
        vs.addGlyph(sg);
        sg = new VSegment(0, 0, 0, Color.GREEN, (float)Math.sqrt(Math.pow(20,2)+Math.pow(20,2)), (float)Math.PI/4f);
        sg.moveTo(120, 440);
        vs.addGlyph(sg);
        // slices and rings
        //XXX: this one is actually buggy (bad rendering)
        VSlice sl = new VSlice(-20, 460, 0, 40, Math.PI/2d, Math.PI/4d, Color.WHITE, Color.BLACK);
        vs.addGlyph(sl);
        sl = new VSlice(-20, 460, 0, 40, Math.PI/2d, Math.PI/4d, Color.RED, Color.BLACK);
        sl.moveTo(20, 460);
        vs.addGlyph(sl);
        VRing rg = new VRing(60, 460, 0, 40, Math.PI/2d, .2f, Math.PI/4d, Color.BLUE, Color.BLACK);
        vs.addGlyph(rg);
        rg = new VRing(60, 460, 0, 40, Math.PI/2d, .2f, Math.PI/4d, Color.GREEN, Color.BLACK);
        rg.moveTo(100, 460);
        vs.addGlyph(rg);
        // images
        String path_to_img = "src/main/resources/test.jpg";
        VImage im = new VImage(0, 520, 0, (new ImageIcon(path_to_img)).getImage());
        im.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
        vs.addGlyph(im);
        im = new VImage(0, 520, 0, (new ImageIcon(path_to_img)).getImage());
        im.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
        im.moveTo(40, 520);
        vs.addGlyph(im);
        im = new VImageOr(80, 520, 0, (new ImageIcon(path_to_img)).getImage(), (float)Math.PI/2f);
        im.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
        vs.addGlyph(im);
        im = new VImageOr(80, 520, 0, (new ImageIcon(path_to_img)).getImage(), (float)Math.PI/2f);
        im.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
        im.moveTo(120, 520);
        vs.addGlyph(im);
        im = new RImage(160, 520, 0, (new ImageIcon(path_to_img)).getImage(), 1f);
        im.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
        vs.addGlyph(im);
        im = new RImage(160, 520, 0, (new ImageIcon(path_to_img)).getImage(), 1f);
        im.setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
        im.moveTo(200, 520);
        vs.addGlyph(im);
    }
    
    void translate(){
        Point2D.Double translation = new Point2D.Double(400, -200);
        AnimationFactory af = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory();
        for (Glyph g:vs.getAllGlyphs()){
            Animation a = af.createGlyphTranslation(1000, g, translation, true, SlowInSlowOutInterpolator.getInstance(), null);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, true);
        }
    }
    
    void rotate(){
        AnimationFactory af = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory();
        for (Glyph g:vs.getAllGlyphs()){
            Animation a = af.createGlyphOrientationAnim(1000, g, (float)Math.PI, true, SlowInSlowOutInterpolator.getInstance(), null);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, true);
        }
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
    
    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;
    
    AllGlyphsTest test;
    
    int lastJPX,lastJPY;
    
    TestEventHandler(AllGlyphsTest t){
        this.test = t;
    }
    
    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        v.setDrawDrag(true);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed(0);
        VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed(0);
        v.setDrawDrag(false);
    }
    
    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (buttonNumber == 1){
            Camera c = test.mCam;
            double a = (c.focal+Math.abs(c.altitude)) / c.focal;
            VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/PAN_SPEED_COEF)) : (long)((jpx-lastJPX)/(a*PAN_SPEED_COEF)));
            VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/PAN_SPEED_COEF)) : (long)((lastJPY-jpy)/(a*PAN_SPEED_COEF)));
        }
    }
    
    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e){
        Camera c = test.mCam;
        double a = (c.focal+Math.abs(c.altitude)) / c.focal;
        if (wheelDirection == WHEEL_DOWN){
            c.altitudeOffset(-a*5);
            VirtualSpaceManager.INSTANCE.repaintNow();
        }
        else {
            //wheelDirection == WHEEL_UP
            c.altitudeOffset(a*5);
            VirtualSpaceManager.INSTANCE.repaintNow();
        }
    }
    
    public void enterGlyph(Glyph g){
        g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
        g.highlight(false, null);
    }
    
    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (c == 't'){test.translate();}
        else if (c == 'r'){test.rotate();}
    }
    
    public void viewClosing(View v){
        System.exit(0);
    }
    
}
