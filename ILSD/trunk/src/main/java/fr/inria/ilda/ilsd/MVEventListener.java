/*   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file LICENSE.
 *
 * $Id: $
 */

package fr.inria.ilda.ilsd;


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
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.event.CameraListener;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.event.PortalListener;
import fr.inria.zvtm.event.PickerListener;

import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.TextDescription;

import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.VirtualSpaceManager;

class MVEventListener implements ViewListener, CameraListener, ComponentListener, PickerListener {

    static final float MAIN_SPEED_FACTOR = 50.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    static float WHEEL_MM_STEP = 1.0f;

    //remember last mouse coords to compute translation  (dragging)
    int lastJPX,lastJPY;
    double lastVX, lastVY;

    int currentJPX, currentJPY;

    ILSD app;

    // last glyph entered
    Glyph lge;

    boolean panning = false;

    MVEventListener(ILSD app){
        this.app = app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        lge = app.dSpacePicker.lastGlyphEntered();
        if (lge != null){
            // interacting with a Glyph in data space (could be a FITS image, a PDF page, etc.)
            app.dSpacePicker.stickGlyph(lge);
        }
        else {
            // pressed button in empty space (or background ZUIST image)
            panning = true;
        }
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        panning = false;
        lge = app.dSpacePicker.lastGlyphEntered();
        if (lge != null){
            app.dSpacePicker.unstickLastGlyph();
        }
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        app.meh.displayMainPieMenu();
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click3(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e){
        updateDataSpacePicker(jpx, jpy);
    }

    public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, MouseEvent e){
        updateDataSpacePicker(jpx, jpy);
        if (panning){
            Camera c = app.mCamera;
            double a = (c.focal+Math.abs(c.altitude)) / c.focal;
            synchronized(c){
                c.move(a*(lastJPX-jpx), a*(jpy-lastJPY));
                lastJPX = jpx;
                lastJPY = jpy;
                cameraMoved(c, null, 0);
            }
        }
    }

    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e){
        double mvx = v.getVCursor().getVSXCoordinate();
        double mvy = v.getVCursor().getVSYCoordinate();
        if (wheelDirection  == WHEEL_UP){
            app.nav.czoomOut(app.mCamera, WHEEL_ZOOMOUT_FACTOR, mvx, mvy);
        }
        else {
            //wheelDirection == WHEEL_DOWN, zooming in
            app.nav.czoomIn(app.mCamera, WHEEL_ZOOMIN_FACTOR, mvx, mvy);
        }
    }

    public void enterGlyph(Glyph g){}

    public void exitGlyph(Glyph g){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code==KeyEvent.VK_PAGE_UP){app.nav.getHigherView();}
        else if (code==KeyEvent.VK_PAGE_DOWN){app.nav.getLowerView();}
        else if (code==KeyEvent.VK_HOME){app.nav.getGlobalView(null);}
        else if (code==KeyEvent.VK_UP){app.nav.translateView(Navigation.MOVE_UP);}
        else if (code==KeyEvent.VK_DOWN){app.nav.translateView(Navigation.MOVE_DOWN);}
        else if (code==KeyEvent.VK_LEFT){app.nav.translateView(Navigation.MOVE_LEFT);}
        else if (code==KeyEvent.VK_RIGHT){app.nav.translateView(Navigation.MOVE_RIGHT);}
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){
        app.exit();
    }

    /*ComponentListener*/
    public void componentHidden(ComponentEvent e){}
    public void componentMoved(ComponentEvent e){}
    public void componentResized(ComponentEvent e){
        app.updatePanelSize();
    }
    public void componentShown(ComponentEvent e){}

    public void cameraMoved(Camera cam, Point2D.Double coord, double a){}

    Point2D.Double vsCoords = new Point2D.Double();

    void updateDataSpacePicker(int jpx, int jpy){
        app.mView.fromPanelToVSCoordinates(jpx, jpy, app.dCamera, vsCoords);
        app.dSpacePicker.setVSCoordinates(vsCoords.x, vsCoords.y);
        app.dSpacePicker.computePickedGlyphList(app.dCamera);
    }

}
