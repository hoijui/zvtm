/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2013. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: TIVEventHandler.java 5421 2015-03-30 13:54:13Z epietrig $
 */

package fr.inria.zuist.viewer;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VCursor;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.event.CameraListener;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.engine.portals.DraggableCameraPortal;
import fr.inria.zvtm.event.PortalListener;
import fr.inria.zvtm.event.PickerListener;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.EndAction;

import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.TextDescription;

class TIVEventHandler implements ViewListener, ComponentListener, PortalListener, CameraListener, PickerListener {

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;

    static float WHEEL_MM_STEP = 1.0f;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    double lastVX, lastVY;
    int currentJPX, currentJPY;

    boolean mCamStickedToMouse = false;
    boolean regionStickedToMouse = false;
    boolean inPortal = false;
    OverviewPortal currentOP = null;
    DragMag currentDragMag = null;
    boolean dragging_dragmag = false;
    boolean dragging_dragmag_in = false;
    boolean resizeing_dragmag = false;
    boolean dragging_dragmag_vis = false;
    boolean do_force_exit_portal = false;

    TiledImageViewer application;
    TIVNavigationManager nm;

    boolean cursorNearBorder = false;

    boolean zero_order_dragging = false;
    boolean first_order_dragging = false;
    boolean translating = false;
    static final short ZERO_ORDER = 0;
    static final short FIRST_ORDER = 1;
    short navMode = ZERO_ORDER;

    // region selection
    boolean selectingRegion = false;
    double x1, y1, x2, y2;

    static final int DELAYED_UPDATE_FREQUENCY = 400;

    DelayedUpdateTimer dut;

    TIVEventHandler(TiledImageViewer app){
        this.application = app;
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
        lastVX = v.getVCursor().getVSXCoordinate();
        lastVY = v.getVCursor().getVSYCoordinate();
        if (currentDragMag != null){
            if(currentDragMag.coordInsideBar(jpx, jpy)){
                dragging_dragmag = true;
            }
            else{
                if (currentDragMag.isManatthan()){
                    dragging_dragmag = true;
                }
                else {
                    dragging_dragmag_in = true;
                }
            }
        }
        else if (currentOP != null){
            if (application.nm.ovPortal.coordInsideObservedRegion(jpx, jpy)){
                regionStickedToMouse = true;
            }
            else {
                double rw = (jpx-application.nm.ovPortal.x) / (double)application.nm.ovPortal.w;
                double rh = (jpy-application.nm.ovPortal.y) / (double)application.nm.ovPortal.h;
                application.mCamera.moveTo(rw*(application.nm.scene_bounds[2]-application.nm.scene_bounds[0]) + application.nm.scene_bounds[0],
                                           rh*(application.nm.scene_bounds[3]-application.nm.scene_bounds[1]) + application.nm.scene_bounds[1]);
                // position camera where user has pressed, and then allow seamless dragging
                regionStickedToMouse = true;
            }
        }
        else if (mod == ALT_MOD){
            selectingRegion = true;
            x1 = v.getVCursor().getVSXCoordinate();
            y1 = v.getVCursor().getVSYCoordinate();
            v.setDrawRect(true);
        }
        else {
            if (application.dmm != null)
            {
                currentDragMag = application.dmm.checkVis(jpx,jpy);
                if (currentDragMag != null){
                    dragging_dragmag_vis = true;
                }
            }
            if (currentDragMag == null){
                if (navMode == FIRST_ORDER){
                    first_order_dragging = true;
                    //v.setDrawDrag(true); // FIXME
                }
                else {
                    // ZERO_ORDER
                    zero_order_dragging = true;
                }
            }
        }
        translating = zero_order_dragging || first_order_dragging || regionStickedToMouse;
        if (translating){
            application.sm.enableRegionUpdater(false);
        }
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        regionStickedToMouse = false;
        zero_order_dragging = false;
        dragging_dragmag = false;
        dragging_dragmag_in = false;
        resizeing_dragmag = false;
        if (do_force_exit_portal && currentDragMag != null) {
            exitPortal(currentDragMag);
        }
        if (dragging_dragmag_vis){ currentDragMag = null; }
        dragging_dragmag_vis = false;
        if (translating){
            translating = false;
            application.sm.enableRegionUpdater(true);
        }
        if (first_order_dragging){
            Camera c = application.mCamera;
            c.setXspeed(0);
            c.setYspeed(0);
            c.setZspeed(0);
            // v.setDrawDrag(false); // FIXME
            first_order_dragging = false;
        }
        if (selectingRegion){
            v.setDrawRect(false);
            x2 = v.getVCursor().getVSXCoordinate();
            y2 = v.getVCursor().getVSYCoordinate();
            if ((Math.abs(x2-x1)>=4) && (Math.abs(y2-y1)>=4)){
                application.sm.setUpdateLevel(false);
                application.mCamera.getOwningView().centerOnRegion(application.mCamera, TIVNavigationManager.ANIM_MOVE_DURATION,
                    x1, y1, x2, y2,
                    new EndAction(){public void execute(Object subject, Animation.Dimension dimension){application.sm.setUpdateLevel(true);}});
            }
            selectingRegion = false;
        }
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        lastVX = v.getVCursor().getVSXCoordinate();
        lastVY = v.getVCursor().getVSYCoordinate();
        if (!inPortal && !resizeing_dragmag){
            if (nm.lensType != TIVNavigationManager.NO_LENS){
                nm.zoomInPhase2(lastVX, lastVY);
            }
            else {
                if (cursorNearBorder){
                    // do not activate the lens when cursor is near the border
                    return;
                }
                nm.zoomInPhase1(jpx, jpy);
            }
        }
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        if (currentDragMag != null){
            resizeing_dragmag = true;
        }
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        resizeing_dragmag = false;
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        lastVX = v.getVCursor().getVSXCoordinate();
        lastVY = v.getVCursor().getVSYCoordinate();
        if (resizeing_dragmag){ return; }
        if (nm.lensType != TIVNavigationManager.NO_LENS){
            nm.zoomOutPhase2();
        }
        else {
            if (cursorNearBorder){
                // do not activate the lens when cursor is near the border
                return;
            }
            nm.zoomOutPhase1(jpx, jpy, lastVX, lastVY);
        }
    }

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
//        System.err.println(v.getVCursor().vx+" "+v.getVCursor().vy);
        if ((jpx-TIVNavigationManager.LENS_R1) < 0){
            jpx = TIVNavigationManager.LENS_R1;
            cursorNearBorder = true;
        }
        else if ((jpx+TIVNavigationManager.LENS_R1) > application.panelWidth){
            jpx = application.panelWidth - TIVNavigationManager.LENS_R1;
            cursorNearBorder = true;
        }
        else {
            cursorNearBorder = false;
        }
        if ((jpy-TIVNavigationManager.LENS_R1) < 0){
            jpy = TIVNavigationManager.LENS_R1;
            cursorNearBorder = true;
        }
        else if ((jpy+TIVNavigationManager.LENS_R1) > application.panelHeight){
            jpy = application.panelHeight - TIVNavigationManager.LENS_R1;
            cursorNearBorder = true;
        }
        if (nm.lensType != 0 && nm.lens != null){
            nm.moveLens(jpx, jpy, e.getWhen());
        }
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        Camera c = application.mCamera;
        if (zero_order_dragging){
            double a = (c.focal+Math.abs(c.altitude)) / c.focal;
            c.move(a*(lastJPX-jpx), a*(jpy-lastJPY));
            lastJPX = jpx;
            lastJPY = jpy;
            if (nm.lensType != 0 && nm.lens != null){
                nm.moveLens(jpx, jpy, e.getWhen());
            }
        }
        else if (first_order_dragging){
            if (mod == SHIFT_MOD){
                c.setXspeed(0);
                c.setYspeed(0);
                c.setZspeed(((lastJPY-jpy)*(ZOOM_SPEED_COEF)));
            }
            else {
                double a = (c.focal+Math.abs(c.altitude)) / c.focal;
                c.setXspeed((c.altitude>0) ? (jpx-lastJPX)*(a/PAN_SPEED_COEF) : (jpx-lastJPX)/(a*PAN_SPEED_COEF));
                c.setYspeed((c.altitude>0) ? (lastJPY-jpy)*(a/PAN_SPEED_COEF) : (lastJPY-jpy)/(a*PAN_SPEED_COEF));
                c.setZspeed(0);
            }
            if (nm.lensType != 0 && nm.lens != null){
                nm.moveLens(jpx, jpy, e.getWhen());
            }
        }
        else if (regionStickedToMouse){
            double a = (application.overviewCamera.focal+Math.abs(application.overviewCamera.altitude)) / application.overviewCamera.focal;
            c.move(a*(jpx-lastJPX), a*(lastJPY-jpy));
            lastJPX = jpx;
            lastJPY = jpy;
        }
        else if (dragging_dragmag){
            currentDragMag.move(jpx-lastJPX, jpy-lastJPY);
            if (currentDragMag.isManatthan())
            {
                Camera dmc = currentDragMag.getCamera();
                double a = (c.focal+Math.abs(c.altitude)) / c.focal;
                dmc.move(a*(jpx-lastJPX), a*(lastJPY-jpy));
            }
            lastJPX = jpx;
            lastJPY = jpy;
        }
        else if (dragging_dragmag_vis){
            //currentDragMag.move(jpx-lastJPX, jpy-lastJPY);
            Camera dmc = currentDragMag.getCamera();
            double a = (c.focal+Math.abs(c.altitude)) / c.focal;
            dmc.move(a*(jpx-lastJPX), a*(lastJPY-jpy));
            currentDragMag.updateVis();
            lastJPX = jpx;
            lastJPY = jpy;
        }
        else if (dragging_dragmag_in){
            Camera dmc = currentDragMag.getCamera();
            double a = (dmc.focal+Math.abs(dmc.altitude)) / dmc.focal;
            if (mod == SHIFT_MOD)  a = (c.focal+Math.abs(c.altitude)) / c.focal;
            dmc.move(-a*(jpx-lastJPX), -a*(lastJPY-jpy));
            currentDragMag.updateVis();
            lastJPX = jpx;
            lastJPY = jpy;
        }
        else if (resizeing_dragmag){
            currentDragMag.resize(jpx-lastJPX, jpy-lastJPY);
            application.dmm.dragMagResized(currentDragMag);
            lastJPX = jpx;
            lastJPY = jpy;
        }
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        if (nm.lensType != 0 && nm.lens != null){
            if (wheelDirection  == ViewListener.WHEEL_UP){
                nm.magnifyFocus(TIVNavigationManager.WHEEL_MM_STEP, nm.lensType, application.mCamera);
            }
            else {
                nm.magnifyFocus(-TIVNavigationManager.WHEEL_MM_STEP, nm.lensType, application.mCamera);
            }
        }
        //else if (mod == SHIFT_MOD && currentDragMag != null && currentDragMag.isManatthan()){
            // update scaleFactor ...
        //}
        else if (currentDragMag != null && !currentDragMag.isManatthan())
        {
            Camera c = currentDragMag.getCamera();
            double a = (c.focal+Math.abs(c.altitude)) / c.focal;
            double mvx = c.vx;
            double mvy = c.vy;
            if (currentDragMag.getType() == DragMag.DM_TYPE_INDEPENDANT){ // centered zoom
                // FIXME... this does not work !!!
                System.out.print("center zoom "+mvx+" "+mvy+" ");
                mvx = mvx+ a*(lastJPX - currentDragMag.x - (double)currentDragMag.w/2.0);
                mvy = mvy+ a*(-lastJPY + currentDragMag.y + (double)currentDragMag.h/2.0);
                System.out.println("/ "+mvx+" "+mvy+" ");
            } 
            if (wheelDirection == WHEEL_UP){
                c.move(-((mvx - c.vx) * WHEEL_ZOOMOUT_FACTOR / c.focal),
                        -((mvy - c.vy) * WHEEL_ZOOMOUT_FACTOR / c.focal));
                c.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
            }
            else{
                if (c.getAltitude()-a*WHEEL_ZOOMIN_FACTOR >= 0){
                    c.move((mvx - c.vx) * WHEEL_ZOOMIN_FACTOR / c.focal,
                            (mvy - c.vy) * WHEEL_ZOOMIN_FACTOR / c.focal);
                    c.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
                }
            }
            currentDragMag.updateVis();
            application.vsm.repaint();
        }
        else {
            double a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
            double mvx = v.getVCursor().getVSXCoordinate();
            double mvy = v.getVCursor().getVSYCoordinate();
            if (currentDragMag != null && currentDragMag.isManatthan()){
                // zoom at the center of the dragmag
                double x = currentDragMag.x + currentDragMag.w/2;
                double y = currentDragMag.y + currentDragMag.h/2; 
                x = (x - ((double)application.panelWidth/2.0));
                y = (-y + ((double)application.panelHeight/2.0));
                Location cgl = application.mCamera.getLocation();
                mvx = cgl.getX()+ a*x;
                mvy = cgl.getY()+ a*y;
            }
            if (wheelDirection  == WHEEL_UP){
                // zooming out
                application.mCamera.move(-((mvx - application.mCamera.vx) * WHEEL_ZOOMOUT_FACTOR / application.mCamera.focal),
                                         -((mvy - application.mCamera.vy) * WHEEL_ZOOMOUT_FACTOR / application.mCamera.focal));
                application.mCamera.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
                application.vsm.repaint();
            }
            else {
                //wheelDirection == WHEEL_DOWN, zooming in
                if (application.mCamera.getAltitude()-a*WHEEL_ZOOMIN_FACTOR >= application.mCamera.getZoomFloor()){
                    // this test to prevent translation when camera is not actually zoming in
                    application.mCamera.move((mvx - application.mCamera.vx) * WHEEL_ZOOMIN_FACTOR / application.mCamera.focal,
                                             ((mvy - application.mCamera.vy) * WHEEL_ZOOMIN_FACTOR / application.mCamera.focal));
                }
                application.mCamera.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
                application.vsm.repaint();
            }
        }
    }

    public void enterGlyph(Glyph g){
//        g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
//        g.highlight(false, null);
    }

    int ci = 1180;

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code==KeyEvent.VK_PAGE_UP){application.nm.getHigherView();}
        else if (code==KeyEvent.VK_PAGE_DOWN){application.nm.getLowerView();}
        else if (code==KeyEvent.VK_HOME){application.nm.getGlobalView(null);}
        else if (code==KeyEvent.VK_UP){application.nm.translateView(TIVNavigationManager.MOVE_UP);}
        else if (code==KeyEvent.VK_DOWN){application.nm.translateView(TIVNavigationManager.MOVE_DOWN);}
        else if (code==KeyEvent.VK_LEFT){application.nm.translateView(TIVNavigationManager.MOVE_LEFT);}
        else if (code==KeyEvent.VK_RIGHT){application.nm.translateView(TIVNavigationManager.MOVE_RIGHT);}
        else if (code==KeyEvent.VK_N){toggleNavMode();}
        else if (code == KeyEvent.VK_F2){application.gc();}
        else if (code == KeyEvent.VK_L){application.nm.toggleLensType();}
        else if (code == KeyEvent.VK_U){application.toggleUpdateTiles();}
        else if (code == KeyEvent.VK_S){application.nm.toggleScreenSaver();}
        else if (code == KeyEvent.VK_Q && Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() == e.getModifiers()){application.exit();}
        else if (c == '?'){application.ovm.showAbout();}
        else if (code == KeyEvent.VK_A){application.toggleBenchAnim();}
        else if (code == KeyEvent.VK_D) {
            if (currentDragMag != null) {
                currentDragMag.setType(DragMag.DM_TYPE_DRAGMAG);
                application.vsm.repaint();
            }
            else { 
                application.dmm.addDragMag(DragMag.DM_TYPE_DRAGMAG);
            }
        }
        else if (code == KeyEvent.VK_M) {
            if (currentDragMag != null) {
                currentDragMag.setType(DragMag.DM_TYPE_MANATTHAN);
                application.vsm.repaint();
            }
            else { 
                application.dmm.addDragMag(DragMag.DM_TYPE_MANATTHAN);
            }
        }
        else if (code == KeyEvent.VK_I) {
            if (currentDragMag != null) {
                currentDragMag.setType(DragMag.DM_TYPE_INDEPENDANT);
                application.vsm.repaint();
            }
            else { 
                application.dmm.addDragMag(DragMag.DM_TYPE_INDEPENDANT);
            }
        }
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

    /* Overview Portal & DragMag */
    public void enterPortal(Portal p){
        if (dragging_dragmag || resizeing_dragmag || dragging_dragmag_vis || dragging_dragmag_in) {
            if (p == currentDragMag) { 
                do_force_exit_portal = false;
            }
            return; 
        }
        inPortal = true;
        if (p instanceof OverviewPortal){
            currentOP = (OverviewPortal)p;
            currentOP.setBorder(TIVNavigationManager.OV_INSIDE_BORDER_COLOR);
        }
        else if (p instanceof DragMag){
            currentDragMag = (DragMag)p;
            currentDragMag.setDragBarColor(TIVDragMagsManager.DDM_INSIDE_BAR_COLOR);
        }
        VirtualSpaceManager.INSTANCE.repaint();
    }

    public void exitPortal(Portal p){
        if (dragging_dragmag || resizeing_dragmag || dragging_dragmag_vis || dragging_dragmag_in) {
            if (p == currentDragMag) { 
                do_force_exit_portal = true;
            }
            return;
        }
        inPortal = false;
        if (currentOP != null){
            currentOP.setBorder(TIVNavigationManager.OV_BORDER_COLOR);
        }
        else if (currentDragMag != null){
            currentDragMag.setDragBarColor(TIVDragMagsManager.DDM_BAR_COLOR);
        }
        VirtualSpaceManager.INSTANCE.repaint();
        currentOP = null; currentDragMag = null;
    }

    void toggleNavMode(){
        switch(navMode){
            case FIRST_ORDER:{navMode = ZERO_ORDER;application.ovm.say(Messages.ZON);break;}
            case ZERO_ORDER:{navMode = FIRST_ORDER;application.ovm.say(Messages.FON);break;}
        }
    }

    public void cameraMoved(Camera cam, Point2D.Double coord, double a){
        if (translating){
            dut.requestUpdate();
        }
    }

    void cameraMoved(){
        application.sm.updateVisibleRegions();
    }

}

class DelayedUpdateTimer extends TimerTask {

    private boolean enabled = true;
    private boolean update = false;

    TIVEventHandler eh;

    DelayedUpdateTimer(TIVEventHandler eh){
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
