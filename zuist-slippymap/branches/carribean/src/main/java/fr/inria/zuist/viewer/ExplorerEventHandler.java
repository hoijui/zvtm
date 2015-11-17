/*   Copyright (c) INRIA, 2015. All Rights Reserved
 * $Id: ExplorerEventHandler.java 5705 2015-08-11 12:38:56Z epietrig $
 */

package fr.inria.zuist.viewer;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;

import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;

import fr.inria.zvtm.engine.VCursor;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.event.CameraListener;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.event.PortalListener;

import fr.inria.zuist.engine.Region;
import fr.inria.zuist.od.ObjectDescription;
import fr.inria.zuist.od.TextDescription;

class ExplorerEventHandler implements ViewListener, CameraListener, ComponentListener, PortalListener {

    static final float MAIN_SPEED_FACTOR = 50.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    static float WHEEL_MM_STEP = 1.0f;

    // update scene when panned from overview every 0.5s
    static final int DELAYED_UPDATE_FREQUENCY = 500;

    //remember last mouse coords to compute translation  (dragging)
    int lastJPX,lastJPY;
    double lastVX, lastVY;

    int currentJPX, currentJPY;

    /* bounds of region in virtual space currently observed through bCamera */
    double[] wnes = new double[4];
    double oldCameraAltitude;

    boolean mCamStickedToMouse = false;
    boolean regionStickedToMouse = false;
    boolean inPortal = false;

    SlippyMapViewer application;
    NavigationManager nm;

    Glyph g;

    boolean panning = false;

    DelayedUpdateTimer dut;

    // region selection
    boolean selectingRegion = false;
    double x1, y1, x2, y2;

    ExplorerEventHandler(SlippyMapViewer app){
        this.application = app;
        this.nm = app.nm;
        oldCameraAltitude = this.application.mCamera.getAltitude();
        initDelayedUpdateTimer();
    }

    void initDelayedUpdateTimer(){
        Timer timer = new Timer();
        dut = new DelayedUpdateTimer(this);
        timer.scheduleAtFixedRate(dut, DELAYED_UPDATE_FREQUENCY, DELAYED_UPDATE_FREQUENCY);
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        if (inPortal){
            if (application.nm.ovPortal.coordInsideObservedRegion(jpx, jpy)){
                regionStickedToMouse = true;
            }
            else {
                double a = (application.ovCamera.focal+Math.abs(application.ovCamera.altitude)) / application.ovCamera.focal;
                application.mCamera.moveTo(a*(jpx-application.nm.ovPortal.x-application.nm.ovPortal.w/2),
                                           -a*(jpy-application.nm.ovPortal.y-application.nm.ovPortal.h/2));
                cameraMoved(application.mCamera, null, 0);
                // position camera where user has pressed, and then allow seamless dragging
                regionStickedToMouse = true;
            }
        }
        else if ((g = v.lastGlyphEntered()) != null){
        }
        else {
            selectingRegion = true;
            x1 = v.getVCursor().getVSXCoordinate();
            y1 = v.getVCursor().getVSYCoordinate();
            v.setDrawRect(true);
        }
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        regionStickedToMouse = false;
        if (selectingRegion){
            v.setDrawRect(false);
            x2 = v.getVCursor().getVSXCoordinate();
            y2 = v.getVCursor().getVSYCoordinate();
            if ((Math.abs(x2-x1)>=4) && (Math.abs(y2-y1)>=4)){
                application.mCamera.getOwningView().centerOnRegion(application.mCamera, NavigationManager.ANIM_MOVE_DURATION, x1, y1, x2, y2);
            }
            selectingRegion = false;
        }
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        lastVX = v.getVCursor().getVSXCoordinate();
        lastVY = v.getVCursor().getVSYCoordinate();
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        if (!inPortal){
            panning = true;
        }
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        panning = false;
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        lastVX = v.getVCursor().getVSXCoordinate();
        lastVY = v.getVCursor().getVSYCoordinate();
        Glyph g;
        if ((g=v.lastGlyphEntered()) != null){
            if (g instanceof VPolygon){
                application.mView.centerOnGlyph(g, application.mCamera, NavigationManager.ANIM_MOVE_DURATION);
            }
        }
    }

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        // System.out.println(v.getVCursor().getVSXCoordinate()+" "+v.getVCursor().getVSYCoordinate());
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (panning){
            double a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
            synchronized(application.mCamera){
                application.mCamera.move(a*(lastJPX-jpx), a*(jpy-lastJPY));
                lastJPX = jpx;
                lastJPY = jpy;
                cameraMoved(application.mCamera, null, 0);
            }
        }
        else if (regionStickedToMouse){
            double a = (application.ovCamera.focal+Math.abs(application.ovCamera.altitude)) / application.ovCamera.focal;
            application.mCamera.move(a*(jpx-lastJPX), a*(lastJPY-jpy));
            dut.requestUpdate();
            lastJPX = jpx;
            lastJPY = jpy;
        }
        else if (selectingRegion){
        }
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        double a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
        double mvx = v.getVCursor().getVSXCoordinate();
        double mvy = v.getVCursor().getVSYCoordinate();
        if (wheelDirection  == WHEEL_UP){
            // zooming out
            application.mCamera.move(-((mvx - application.mCamera.vx) * WHEEL_ZOOMOUT_FACTOR / application.mCamera.focal),
                                     -((mvy - application.mCamera.vy) * WHEEL_ZOOMOUT_FACTOR / application.mCamera.focal));
            application.mCamera.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
        }
        else {
            //wheelDirection == WHEEL_DOWN, zooming in
            if (application.mCamera.getAltitude()-a*WHEEL_ZOOMIN_FACTOR >= application.mCamera.getZoomFloor()){
                // this test to prevent translation when camera is not actually zoming in
                application.mCamera.move((mvx - application.mCamera.vx) * WHEEL_ZOOMIN_FACTOR / application.mCamera.focal,
                                         ((mvy - application.mCamera.vy) * WHEEL_ZOOMIN_FACTOR / application.mCamera.focal));
            }
            application.mCamera.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
        }
        dut.requestUpdate();
        application.vsm.repaint();
    }

    public void enterGlyph(Glyph g){}

    public void exitGlyph(Glyph g){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code==KeyEvent.VK_PAGE_UP){application.nm.getHigherView();}
        else if (code==KeyEvent.VK_PAGE_DOWN){application.nm.getLowerView();}
        else if (code==KeyEvent.VK_UP){application.nm.translateView(NavigationManager.MOVE_UP);}
        else if (code==KeyEvent.VK_DOWN){application.nm.translateView(NavigationManager.MOVE_DOWN);}
        else if (code==KeyEvent.VK_LEFT){application.nm.translateView(NavigationManager.MOVE_LEFT);}
        else if (code==KeyEvent.VK_RIGHT){application.nm.translateView(NavigationManager.MOVE_RIGHT);}
        else if (code == KeyEvent.VK_F1){application.toggleMemoryUsageDisplay();}
        else if (code == KeyEvent.VK_F2){application.gc();}
        else if (code == KeyEvent.VK_U){application.toggleUpdateMaps();}
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){
        application.exit();
    }

    /*ComponentListener*/
    public void componentHidden(ComponentEvent e){}
    public void componentMoved(ComponentEvent e){}
    public void componentResized(ComponentEvent e){
        application.updatePanelSize();
    }
    public void componentShown(ComponentEvent e){}

    public void cameraMoved(Camera cam, Point2D.Double coord, double a){
        application.bCamera.setAltitude(cam.getAltitude());
        application.bCamera.moveTo(cam.vx, cam.vy);
        dut.requestUpdate();
    }

    void cameraMoved(){
        //application.mView.getVisibleRegion(application.mCamera, wnes);
        // region seen through camera
        double alt = application.mCamera.getAltitude();
        if (alt != oldCameraAltitude){
            oldCameraAltitude = alt;
        }
        else {
            // camera movement was a simple translation
            //dut.cancelUpdate();
        }
    }

    /* Overview Portal */
    public void enterPortal(Portal p){
        inPortal = true;
        ((OverviewPortal)p).setBorder(NavigationManager.OV_INSIDE_BORDER_COLOR);
        application.vsm.repaint();
    }

    public void exitPortal(Portal p){
        inPortal = false;
        ((OverviewPortal)p).setBorder(NavigationManager.OV_BORDER_COLOR);
        application.vsm.repaint();
    }

}

class DelayedUpdateTimer extends TimerTask {

    private boolean enabled = true;
    private boolean update = false;

    ExplorerEventHandler eh;

    DelayedUpdateTimer(ExplorerEventHandler eh){
        super();
        this.eh = eh;
    }

    public void setEnabled(boolean b){
        enabled = b;
    }

    public boolean isEnabled(){
        return enabled;
    }

    public void run(){
        if (enabled && update){
            eh.cameraMoved();
            update = false;
        }
    }

    void requestUpdate(){
        update = true;
    }

    void cancelUpdate(){
        update = false;
    }

}
