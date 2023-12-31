/*   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: FitsViewerEventHandler.java 5247 2014-12-02 20:22:41Z fdelcampo $
 */

package fr.inria.zuist.viewer;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;

import fr.inria.zvtm.engine.VCursor;

import java.awt.Cursor;

//import fr.inria.zvtm.engine.LongPoint;
import java.awt.geom.Point2D;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;

//import fr.inria.zvtm.engine.Utilities;
import fr.inria.zvtm.engine.Utils;

import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;

//import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.event.ViewListener;

//import fr.inria.zvtm.engine.CameraListener;
import fr.inria.zvtm.event.CameraListener;


import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.TextDescription;

import fr.inria.zvtm.glyphs.FitsImage;

//class FitsViewerEventHandler implements ViewEventHandler, ComponentListener, CameraListener {
class FitsViewerEventHandler implements ViewListener, ComponentListener, CameraListener {

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    //remember last mouse coords to compute translation  (dragging)
    int lastJPX,lastJPY;
    long lastVX, lastVY;

    FitsViewer application;

    Glyph g;

    double oldCameraAltitude;

    boolean zero_order_dragging = false;
    boolean first_order_dragging = false;
	static final short ZERO_ORDER = 0;
	static final short FIRST_ORDER = 1;
	short navMode = ZERO_ORDER;

	Glyph objectJustSelected = null;



    FitsViewerEventHandler(FitsViewer app){
        this.application = app;
        oldCameraAltitude = app.mCamera.getAltitude();

    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        if (navMode == FIRST_ORDER){
            first_order_dragging = true;
            v.setDrawDrag(true);
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
            v.setDrawDrag(false);
            first_order_dragging = false;
        }
    }

    public void click1(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){
        /*
        Vector gum = v.getVCursor().getIntersectingGlyphs(v.cams[0]);
		if (gum == null){
			return;
		}
		Glyph g = (Glyph)gum.lastElement();
        */
        System.out.println("click1: ");
        System.out.println(v.viewToSpaceCoords(application.mCamera, jpx, jpy));

        Glyph g = v.lastGlyphEntered();

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
			v.cams[0].getOwningView().centerOnGlyph(g, v.cams[0], FitsViewer.ANIM_MOVE_LENGTH, true, 1.2f);
			objectJustSelected = g;
		}
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        //toggleColorFilter();
        //toggleTransferFun();
    }

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		v.parent.setActiveLayer(application.LAYER_PIEMENU);
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
		v.parent.setActiveLayer(application.LAYER_SCENE);
	}

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){

        if((jpx < application.menu.WIDTH_MENU && jpy < application.menu.BORDER_BOTTON_FILTER && jpy > application.menu.BORDER_TOP_FILTER) ||
          (jpy > application.menu.BORDER_TOP_HISTOGRAM && jpy < application.menu.BORDER_BOTTON_HISTOGRAM && jpx > application.menu.BORDER_LEFT_HISTOGRAM && jpx < application.menu.BORDER_RIGHT_HISTOGRAM )){
            v.parent.setActiveLayer(application.LAYER_MENU);
            v.parent.setCursorIcon(Cursor.DEFAULT_CURSOR);
        }

/*
        Camera c = application.mCamera;
        double a = (c.focal+Math.abs(c.altitude)) / c.focal;
        double vx = c.vx;
        double vy = c.vy;
        //System.out.println("VirtualSpace: Camera("+vx+","+vy+") (" + jpx + ", " + jpy + ")");
        //System.out.println("(" + (vx + a*(jpx-application.VIEW_W/2) ) + ", " + (vy + a*(jpy-application.VIEW_H/2) ) + ")");
        //System.out.println("VIEW_W: " + application.VIEW_W + " - VIEW_H: " + application.VIEW_H);
        Point2D.Double cur = new Point2D.Double(v.getVCursor().getVSXCoordinate(), v.getVCursor().getVSYCoordinate());

        FitsImage fi = (FitsImage)(application.getGlyphOnPoint(cur.getX(), cur.getY() ).firstElement());
        

        double x = (cur.getX()-fi.getLocation().getX());
        double y = (cur.getY()-fi.getLocation().getY());

        System.out.println("cursor-fits:");
        System.out.println(x + " " + y);


        Point2D.Double wcs = fi.pix2wcs(x, y);

        System.out.println(wcs);
        //System.out.println(wcs2);
        //System.out.println(wcs3);
*/
        
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        Camera c = application.mCamera;
        double a = (c.focal+Math.abs(c.altitude)) / c.focal;
        if (zero_order_dragging){
            c.move(Math.round(a*(lastJPX-jpx)), Math.round(a*(jpy-lastJPY)));
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
                c.setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/PAN_SPEED_COEF)) : (long)((jpx-lastJPX)/(a*PAN_SPEED_COEF)));
                c.setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/PAN_SPEED_COEF)) : (long)((lastJPY-jpy)/(a*PAN_SPEED_COEF)));
                c.setZspeed(0);
            }
        }
    }

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
		double a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
		//if (wheelDirection  == WHEEL_UP){
        if (wheelDirection  == WHEEL_UP){
			// zooming in
			application.mCamera.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
			//application.vsm.repaintNow();
            application.vsm.repaint();
		}
		else {
			//wheelDirection == WHEEL_DOWN, zooming out
			application.mCamera.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
			//application.vsm.repaintNow();
            application.vsm.repaint();
		}
	}

	public void enterGlyph(Glyph g){
		if (application.vsm.getActiveView().getActiveLayer() == application.LAYER_PIEMENU){
			// interacting with pie menu
			g.highlight(true, null);
			VirtualSpace vs = application.vsm.getVirtualSpace(application.mnSpaceName);
			vs.onTop(g);
			//int i = Utilities.indexOfGlyph(application.mainPieMenu.getItems(), g);
            int i = Utils.indexOfGlyph(application.mainPieMenu.getItems(), g);
			if (i != -1){
				vs.onTop(application.mainPieMenu.getLabels()[i]);
			}
		}

	}

	public void exitGlyph(Glyph g){
		if (application.vsm.getActiveView().getActiveLayer() == application.LAYER_PIEMENU){
			g.highlight(false, null);
		}
    }

    int direction = 1;
    double angle = 0;
    double diffangle = Math.PI/180;

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
    	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
    	else if (code==KeyEvent.VK_HOME){application.getGlobalView(null);}
    	else if (code==KeyEvent.VK_UP){application.translateView(FitsViewer.MOVE_UP);}
    	else if (code==KeyEvent.VK_DOWN){application.translateView(FitsViewer.MOVE_DOWN);}
    	else if (code==KeyEvent.VK_LEFT){application.translateView(FitsViewer.MOVE_LEFT);}
    	else if (code==KeyEvent.VK_RIGHT){application.translateView(FitsViewer.MOVE_RIGHT);}
        else if (code==KeyEvent.VK_L){ application.rescaleGlobal(false);}
        else if (code==KeyEvent.VK_G){ application.rescaleGlobal(true);}
        else if (code==KeyEvent.VK_1){  application.showLayer(application.LAYER_SCENE_KS, 1f);application.hideLayer(application.LAYER_SCENE_J); application.hideLayer(application.LAYER_SCENE_H);}
        else if (code==KeyEvent.VK_2){  application.showLayer(application.LAYER_SCENE_H, 0.5f);application.showLayer(application.LAYER_SCENE_KS, 1f);application.hideLayer(application.LAYER_SCENE_J);}
        else if (code==KeyEvent.VK_3){  application.showLayer(application.LAYER_SCENE_H, 1f);application.hideLayer(application.LAYER_SCENE_J); application.hideLayer(application.LAYER_SCENE_KS);}
        else if (code==KeyEvent.VK_4){  application.showLayer(application.LAYER_SCENE_H, 1f);application.showLayer(application.LAYER_SCENE_J, 0.5f);application.hideLayer(application.LAYER_SCENE_KS);}
        else if (code==KeyEvent.VK_5){  application.showLayer(application.LAYER_SCENE_J, 1f);application.hideLayer(application.LAYER_SCENE_KS); application.hideLayer(application.LAYER_SCENE_H);}
    	else if (code==KeyEvent.VK_P) {
            System.out.println("change projections");
            //application.moveToCoordinatesWCS();
            //application.orientTo(-1.716612658);
        }
        else if (code==KeyEvent.VK_R){
            if(direction > 0)
                application.orientTo(angle+=diffangle);
            else
                application.orientTo(angle-=diffangle);
            System.out.println("rotation: "+ angle);
        }
        else if (code==KeyEvent.VK_D){
            if(direction > 0)
                direction = -1;
            else
                direction = 1;
        }
        else if (code==KeyEvent.VK_N){
            System.out.println("toggleNavMode()");
            toggleNavMode();}
        else if (code==KeyEvent.VK_C){
            System.out.println("toggleColorFilter();");
            toggleColorFilter();}
        else if (c == 't'){
            System.out.println("toggleTransferFun()");
            toggleTransferFun();}
    }

    private void toggleColorFilter(){
        application.toggleColorFilter();
    }

    private void toggleTransferFun(){
        application.toggleTransferFun();
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){
        System.out.println("ktype: "+ c);
    }

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

    //public void cameraMoved(Camera cam, LongPoint coord, float alt){
    /*
    public void cameraMoved(Camera cam, Point2D.Double coord, float alt){
        application.altitudeChanged();
    }
    */

    public void cameraMoved(Camera cam, Point2D.Double coord, double a){
        //application.bCamera.setAltitude(cam.getAltitude());
        //application.bCamera.moveTo(cam.vx, cam.vy);
        //dut.requestUpdate();
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

    void toggleNavMode(){
        switch(navMode){
            case FIRST_ORDER:{navMode = ZERO_ORDER;break;}
            case ZERO_ORDER:{navMode = FIRST_ORDER;break;}
        }
    }

}
