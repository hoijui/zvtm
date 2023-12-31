/*   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 *
 */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;
import java.util.jar.JarInputStream;
import java.util.jar.JarEntry;
import java.util.Iterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.PRectangle;
import fr.inria.zvtm.glyphs.VText;

import fr.inria.zvtm.fits.Utils;

public class FilterVisualizer {

    VirtualSpaceManager vsm;
    VirtualSpace vs;
    ViewListener eh;
    View mView;

    FilterVisualizer(){
        vsm = VirtualSpaceManager.INSTANCE;
        eh = new FVListener(this);
        vs = vsm.addVirtualSpace("vs1");
        vs.addCamera();
        Vector cameras = new Vector();
        cameras.add(vsm.getVirtualSpace("vs1").getCamera(0));
        vsm.getVirtualSpace("vs1").getCamera(0).setZoomFloor(-90f);
        mView = vsm.addFrameView(cameras, "zvtm-fits - Available Filters", View.STD_VIEW, 800, 600, false, true, true, null);
        mView.setBackgroundColor(Color.BLACK);
        mView.getCursor().setColor(Color.WHITE);
        mView.getCursor().setHintColor(Color.WHITE);
        mView.setListener(eh);
        loadFilters();
        vsm.repaint();
    }

    void loadFilters(){
        try {
            Iterator<String> filterClasses = getFilterClasses().iterator();
            int y = 0;
            while (filterClasses.hasNext()){
                String className = filterClasses.next();
                Class c = ClassLoader.getSystemClassLoader().loadClass(className);
                ColorGradient cg = (ColorGradient)c.newInstance();
                vs.addGlyph(new PRectangle(0, y, 0, 400, 20, cg.getGradient(400), Color.WHITE));
                vs.addGlyph(new VText(300, y, 0, Color.WHITE, className.substring(FILTER_PACKAGE_NAME.length()+1, className.length()-6)));
                y -= 40;
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    static String JAR_NAME = "target/zvtm-fits-"+Utils.VERSION+".jar";
    static String FILTER_PACKAGE_NAME = "fr.inria.zvtm.fits.filters";

    public static List getFilterClasses(){
        ArrayList classes = new ArrayList();
        String packageName = FILTER_PACKAGE_NAME.replaceAll("\\." , "/");
        System.out.println("Jar " + JAR_NAME + " looking for " + FILTER_PACKAGE_NAME);
        try {
            JarInputStream jarFile = new JarInputStream(new FileInputStream(JAR_NAME));
            JarEntry jarEntry;
            while(true) {
                jarEntry=jarFile.getNextJarEntry();
                if (jarEntry == null){
                    break;
                }
                if ((jarEntry.getName().startsWith(packageName)) &&
                    (jarEntry.getName().endsWith("Filter.class"))){
                    String className = jarEntry.getName().replaceAll("/", "\\.");
                    classes.add(className.substring(0, className.length()-6));
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return classes;
    }

    public static void main(String[] args){
        new FilterVisualizer();
    }

}

class FVListener implements ViewListener {

    FilterVisualizer application;

    //remember last mouse coords to compute translation  (dragging)
    int lastJPX,lastJPY;

    FVListener(FilterVisualizer app){
        application = app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX=jpx;
        lastJPY=jpy;
        v.setDrawSegment(true);
        application.vsm.getActiveView().mouse.setSensitivity(false);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        v.cams[0].setXspeed(0);
        v.cams[0].setYspeed(0);
        v.cams[0].setZspeed(0);
        v.setDrawSegment(false);
        application.vsm.getActiveView().mouse.setSensitivity(true);
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (buttonNumber == 1){
            Camera c=application.vsm.getActiveCamera();
            double a=(c.focal+Math.abs(c.altitude))/c.focal;
            if (mod == SHIFT_MOD) {
                v.cams[0].setXspeed(0);
                v.cams[0].setYspeed(0);
                v.cams[0].setZspeed(((lastJPY-jpy)*(ZOOM_SPEED_COEF)));
                //50 is just a speed factor (too fast otherwise)
            }
            else {
                v.cams[0].setXspeed((c.altitude>0) ? ((jpx-lastJPX)*(a/PAN_SPEED_COEF)) : ((jpx-lastJPX)/(a*PAN_SPEED_COEF)));
                v.cams[0].setYspeed((c.altitude>0) ? ((lastJPY-jpy)*(a/PAN_SPEED_COEF)) : ((lastJPY-jpy)/(a*PAN_SPEED_COEF)));
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

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){

    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

}
