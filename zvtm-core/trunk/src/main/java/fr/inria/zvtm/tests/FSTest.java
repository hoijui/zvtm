/*
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2011-2012.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Test.java 4913 2013-02-07 20:18:46Z epietrig $
 */

package fr.inria.zvtm.tests;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.DefaultTimingHandler;
import fr.inria.zvtm.animation.interpolation.ConstantAccInterpolator;
import fr.inria.zvtm.event.ViewAdapter;

import fr.inria.zvtm.glyphs.*;


public class FSTest  {

    static final float DEFAULT_MAX = 50;
    float MAX = DEFAULT_MAX;

    VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
    VirtualSpace mSpace;
    View mViewL, mViewR;
    Camera mCameraL, mCameraR;

    static final String CONFIG_BOTH = "both";
    static final String CONFIG_00 = "0";
    static final String CONFIG_01 = "1";

    public FSTest(String cfg){
        init(cfg);
        populate();
    }

    void init(String config){
        GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();

        GraphicsDevice[] devices = ge.getScreenDevices();
        if(devices.length == 0){
            throw new Error("no screen devices found");
        }
        Map<String, GraphicsDevice> devMap =
            new HashMap<String, GraphicsDevice>();

        System.out.print("available devices: ");
        for(int i=0;i<devices.length;i++) {
            devMap.put(devices[i].getIDstring(), devices[i]);
        }
        for(int i=0;i<devices.length-1;i++) {
            System.out.print(devices[i].getIDstring()+", ");
        }
        System.out.println(devices[devices.length-1].getIDstring());

        mSpace = vsm.addVirtualSpace(VirtualSpace.ANONYMOUS);
        mCameraL = mSpace.addCamera();
        mCameraR = mSpace.addCamera();
        Vector camerasL = new Vector(1);
        camerasL.add(mCameraL);
        Vector camerasR = new Vector(1);
        camerasR.add(mCameraR);

        if (config.equals(CONFIG_00) || config.equals(CONFIG_BOTH)){
            System.out.println("Creating view on 0");
            mViewL = vsm.addFrameView(camerasL,
                    "L", View.STD_VIEW, 100, 100,
                    false, false, false, null);
            FSListener fehL = new FSListener(this);
            mViewL.setListener(fehL, 0);
            GraphicsDevice device = devices[0];


            ((JFrame)mViewL.getFrame()).removeNotify();
            ((JFrame)mViewL.getFrame()).setUndecorated(true);
            device.setFullScreenWindow((JFrame)mViewL.getFrame());
            ((JFrame)mViewL.getFrame()).addNotify();
            mViewL.setBackgroundColor(Color.RED);
        }
        if (config.equals(CONFIG_01) || config.equals(CONFIG_BOTH)){
            System.out.println("Creating view on 1");
            mViewR = vsm.addFrameView(camerasR,
                    "R", View.STD_VIEW, 100, 100,
                    false, false, false, null);
            FSListener fehR = new FSListener(this);
            mViewR.setListener(fehR, 0);
            GraphicsDevice device = devices[1];
            ((JFrame)mViewR.getFrame()).removeNotify();
            ((JFrame)mViewR.getFrame()).setUndecorated(true);
            device.setFullScreenWindow((JFrame)mViewR.getFrame());
            ((JFrame)mViewR.getFrame()).addNotify();
            mViewR.setBackgroundColor(Color.BLUE);
        }
    }

    void populate(){
        Glyph[] glyphs = new Glyph[(int)(MAX*MAX)];
        for (int i=0;i<MAX;i++){
            for (int j=0;j<MAX;j++){
                VRectangle r = new VRectangle(i*20-MAX*10,j*20-MAX*10,0,20,20,Color.getHSBColor(i/MAX,j/MAX,1));
                r.setDrawBorder(false);
                glyphs[(int)(i*MAX+j)] = r;
            }
        }
        mSpace.addGlyphs(glyphs);
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
        String config = args[0];
        new FSTest(config);
    }

}

class FSListener extends ViewAdapter {

    FSTest application;

    int lastJPX, lastJPY;

    FSListener(FSTest app){
        this.application = app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        v.setDrawDrag(true);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        v.setDrawDrag(false);
    }

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
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
