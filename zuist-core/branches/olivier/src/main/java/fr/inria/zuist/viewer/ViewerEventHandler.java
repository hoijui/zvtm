/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2013. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ViewerEventHandler.java 5421 2015-03-30 13:54:13Z epietrig $
 */

package fr.inria.zuist.viewer;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;

import java.util.Vector;

import fr.inria.zvtm.engine.VCursor;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.event.CameraListener;
import fr.inria.zvtm.event.PickerListener;
import fr.inria.zvtm.engine.VirtualSpaceManager;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.TextDescription;

class ViewerEventHandler implements ViewListener, ComponentListener, CameraListener, PickerListener {

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    static float WHEEL_MM_STEP = 1.0f;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    long lastVX, lastVY;

    Viewer application;

    Glyph g;

    boolean zero_order_dragging = false;
    boolean first_order_dragging = false;
    static final short ZERO_ORDER = 0;
    static final short FIRST_ORDER = 1;
    short navMode = ZERO_ORDER;

    Glyph objectJustSelected = null;

    ViewerEventHandler(Viewer app){
        this.application = app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        if (navMode == FIRST_ORDER){
            first_order_dragging = true;
            // v.setDrawDrag(true); /§/ FIXME
        }
        else {
            // ZERO_ORDER
            zero_order_dragging = true;
        }
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        zero_order_dragging = false;
        if (first_order_dragging){
            Camera c = application.mCamera;
            c.setXspeed(0);
            c.setYspeed(0);
            c.setZspeed(0);
            // v.setDrawDrag(false); // FIXME
            first_order_dragging = false;
        }
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        Vector gum = v.getVCursor().getPicker().getIntersectingGlyphs(v.cams[0]);
        if (gum == null){
            return;
        }
        Glyph g = (Glyph)gum.lastElement();
        if (objectJustSelected != null && g == objectJustSelected){
            // last click was on this object, already centered on it,
            // check if it takes somewhere and go there if it does
            Object owner = g.getOwner();
            if (owner != null && owner instanceof ObjectDescription){
                ObjectDescription od = (ObjectDescription)owner;
                String takesToID = od.takesTo();
                if (takesToID != null){
                    switch(od.takesToType()){
                        case SceneManager.TAKES_TO_OBJECT:{application.centerOnObject(takesToID);break;}
                        case SceneManager.TAKES_TO_REGION:{application.centerOnRegion(takesToID);break;}
                    }
                }
            }
        }
        else {
            // last click was not on this object, center on it
            application.rememberLocation(application.mCamera.getLocation());
            v.cams[0].getOwningView().centerOnGlyph(g, v.cams[0], Viewer.ANIM_MOVE_LENGTH, true, 1.2f);
            objectJustSelected = g;
        }
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        v.parent.setActiveLayer(1);
        application.displayMainPieMenu(true);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        Glyph g = v.lastGlyphEntered();
        if (g != null && g.getType() == Messages.PM_ENTRY){
            application.pieMenuEvent(g);
        }
        if (application.mainPieMenu != null){
            application.displayMainPieMenu(false);
        }
        v.parent.setActiveLayer(0);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        application.setCursorCoords(v.getVCursor().getVSXCoordinate(), v.getVCursor().getVSYCoordinate());
        VirtualSpaceManager.INSTANCE.repaint();
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        Camera c = application.mCamera;
        double a = (c.focal+Math.abs(c.altitude)) / c.focal;
        if (zero_order_dragging){
            c.move(a*(lastJPX-jpx), a*(jpy-lastJPY));
            lastJPX = jpx;
            lastJPY = jpy;
        }
        else if (first_order_dragging){
            if (mod == SHIFT_MOD){
                c.setXspeed(0);
                c.setYspeed(0);
                c.setZspeed(((lastJPY-jpy)*(ZOOM_SPEED_COEF)));
            }
            else {
                c.setXspeed((c.altitude>0) ? (jpx-lastJPX)*(a/PAN_SPEED_COEF) : (jpx-lastJPX)/(a*PAN_SPEED_COEF));
                c.setYspeed((c.altitude>0) ? (lastJPY-jpy)*(a/PAN_SPEED_COEF) : (lastJPY-jpy)/(a*PAN_SPEED_COEF));
                c.setZspeed(0);
            }
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

    public void enterGlyph(Glyph g){
        if (application.vsm.getActiveView().getActiveLayer() == 1){
            // interacting with pie menu
            g.highlight(true, null);
            VirtualSpace vs = application.vsm.getVirtualSpace(application.mnSpaceName);
            vs.onTop(g);
            int i = Utils.indexOfGlyph(application.mainPieMenu.getItems(), g);
            if (i != -1){
                vs.onTop(application.mainPieMenu.getLabels()[i]);
            }
        }
    }

    public void exitGlyph(Glyph g){
        if (application.vsm.getActiveView().getActiveLayer() == 1){
            g.highlight(false, null);
        }
    }

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
        else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
        else if (code==KeyEvent.VK_HOME){application.getGlobalView(null);}
        else if (code==KeyEvent.VK_UP){application.translateView(Viewer.MOVE_UP);}
        else if (code==KeyEvent.VK_DOWN){application.translateView(Viewer.MOVE_DOWN);}
        else if (code==KeyEvent.VK_LEFT){application.translateView(Viewer.MOVE_LEFT);}
        else if (code==KeyEvent.VK_RIGHT){application.translateView(Viewer.MOVE_RIGHT);}
        else if (code==KeyEvent.VK_N){toggleNavMode();}
        else if (code == KeyEvent.VK_F1){application.toggleMiscInfoDisplay();}
        else if (code == KeyEvent.VK_F7){application.gc();}
        else if (code == KeyEvent.VK_F2){application.ovm.toggleConsole();}
        else if (code == KeyEvent.VK_Q && Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() == e.getModifiers()){application.exit();}
        else if (code == KeyEvent.VK_A){application.toggleBenchAnim();}
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

    public void cameraMoved(Camera cam, Point2D.Double coord, double alt){
        application.altitudeChanged();
    }

    void toggleNavMode(){
        switch(navMode){
            case FIRST_ORDER:{navMode = ZERO_ORDER;break;}
            case ZERO_ORDER:{navMode = FIRST_ORDER;break;}
        }
    }

}
