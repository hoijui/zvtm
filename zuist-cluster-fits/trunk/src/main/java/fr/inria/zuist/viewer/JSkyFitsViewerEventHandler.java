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
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Stroke;

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
import fr.inria.zvtm.glyphs.VCircle;

//import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.event.ViewListener;

//import fr.inria.zvtm.engine.CameraListener;
import fr.inria.zvtm.event.CameraListener;


import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.TextDescription;

import fr.inria.zvtm.glyphs.JSkyFitsImage;

import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.animation.EndAction;

//class FitsViewerEventHandler implements ViewEventHandler, ComponentListener, CameraListener {
class JSkyFitsViewerEventHandler implements ViewListener {

    static final BasicStroke SEL_STROKE = new BasicStroke(2f);
    static final float SEL_ALPHA = .5f;

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;
    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    JSkyFitsViewer app;
    AnimationManager am = VirtualSpaceManager.INSTANCE.getAnimationManager();

    //private double[] scaleBounds;
    //private boolean dragLeft = false, dragRight = false;
    //private RangeSelection rs;

    private int lastJPX;
    private int lastJPY;

    Point2D.Double rightClickPress;
    VCircle rightClickSelectionG = new VCircle(0, 0, 1000, 1, Color.BLACK, Color.RED, SEL_ALPHA);
    Point2D.Double coordClickPress;

    boolean panning = false;
    boolean selectingForQuery = false;

    JSkyFitsViewerEventHandler(JSkyFitsViewer app){
        this.app = app;
        rightClickSelectionG.setFilled(false);
        rightClickSelectionG.setStroke(SEL_STROKE);
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        /*
        Point2D.Double cursorPos = viewToSpace(vsm.getActiveCamera(), jpx, jpy);
        if(app.rs.overLeftTick(cursorPos.x, cursorPos.y)){
            dragLeft = true;
        } else if(app.rs.overRightTick(cursorPos.x, cursorPos.y)){
            dragRight = true;
        }
        */
        lastJPX = jpx;
        lastJPY = jpy;
        panning = true;
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        /*
        dragLeft = false;
        dragRight = false;
        double min = hi.getUnderlyingImage().getHistogram().getMin();
        double max = hi.getUnderlyingImage().getHistogram().getMax();
        app.hi.rescale(min + app.rs.getLeftValue()*(max - min),
                min + app.rs.getRightValue()*(max - min),
                1);
        */
        //v.parent.setActiveLayer(0);

        panning = false;
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        selectingForQuery = true;

        coordClickPress = v.getVCursor().getVSCoordinates(app.mCamera);

        // first point (start dragging) defines the center of the query zone
        rightClickPress = v.getVCursor().getVSCoordinates(app.cursorCamera);
        rightClickSelectionG.moveTo(rightClickPress.x, rightClickPress.y);
        rightClickSelectionG.sizeTo(1);
        app.cursorSpace.addGlyph(rightClickSelectionG);


        //VCircle test = new VCircle(v.getVCursor().getVSCoordinates(app.mnCamera).x, v.getVCursor().getVSCoordinates(app.mnCamera).y, 1000, 100, Color.BLACK, Color.RED);
        //app.mnSpace.addGlyph(test);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

        // second point (end dragging) defines the radius of the query zone
        //Point2D.Double rightClickRelease = v.getVCursor().getVSCoordinates(app.cursorCamera);
        Point2D.Double coordClickRelease = v.getVCursor().getVSCoordinates(app.mCamera);

        // make query
        app.querySimbad(coordClickPress, coordClickRelease);
        selectingForQuery = false;
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        if (app.mnCamera.isEnabled() && app.menu.cursorInside(jpx, jpy)){
            v.parent.setActiveLayer(app.LAYER_MENU);
            v.parent.setCursorIcon(Cursor.DEFAULT_CURSOR);
        }
        if(app.rPicker != null){
            //System.out.println("rPicker.setVSCoordinates");
            app.rPicker.setVSCoordinates(v.getVCursor().getVSXCoordinate(), v.getVCursor().getVSYCoordinate());
        }
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (panning){
            Camera c = app.mCamera;
            pan(c, lastJPX-jpx, jpy-lastJPY);
            lastJPX = jpx;
            lastJPY = jpy;
        }
        else if (selectingForQuery){
            Point2D.Double p = v.getVCursor().getVSCoordinates(app.mnCamera);
            rightClickSelectionG.sizeTo(2*Math.sqrt((p.x-rightClickPress.x)*(p.x-rightClickPress.x)+(p.y-rightClickPress.y)*(p.y-rightClickPress.y)));
        }
        /*

        if(buttonNumber == 1){
            /*
            if(dragLeft) {
                //rs.setLeftTickPos(viewToSpace(vsm.getActiveCamera(), jpx, jpy).x);
            } else if(dragRight){
                //rs.setRightTickPos(viewToSpace(vsm.getActiveCamera(), jpx, jpy).x);
            }
            *
        }
        */
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        Camera c = app.mCamera;
        double mvx = v.getVCursor().getVSXCoordinate();
        double mvy = v.getVCursor().getVSYCoordinate();
        zoom(c, mvx, mvy, wheelDirection);
    }

    public void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code == KeyEvent.VK_M){
            app.toggleMenu();
        }
        else if (code == KeyEvent.VK_F1){
            app.menu.selectPreviousColorMapping();
        }
        else if (code == KeyEvent.VK_F2){
            app.menu.selectNextColorMapping();
        }
        else if (code == KeyEvent.VK_F3){
            app.menu.selectPreviousScale();
        }
        else if (code == KeyEvent.VK_F4){
            app.menu.selectNextScale();
        }
        else if (code==KeyEvent.VK_L){
            app.rescaleGlobal(false);
        }
        else if (code==KeyEvent.VK_G){
            app.rescaleGlobal(true);
        }
        else if(code==KeyEvent.VK_1){
            app.showLayer(FitsViewer.LAYER_SCENE_KS, 1.f);
            app.hideLayer(FitsViewer.LAYER_SCENE_H);
            app.hideLayer(FitsViewer.LAYER_SCENE_J);
        }
        else if(code==KeyEvent.VK_2){
            app.showLayer(FitsViewer.LAYER_SCENE_KS, 1.f);
            app.showLayer(FitsViewer.LAYER_SCENE_H, 0.5f);
            app.hideLayer(FitsViewer.LAYER_SCENE_J);
        }
        else if(code==KeyEvent.VK_3){
            app.hideLayer(FitsViewer.LAYER_SCENE_KS);
            app.showLayer(FitsViewer.LAYER_SCENE_H, 1.f);
            app.hideLayer(FitsViewer.LAYER_SCENE_J);
        }
        else if(code==KeyEvent.VK_4){
            app.hideLayer(FitsViewer.LAYER_SCENE_KS);
            app.showLayer(FitsViewer.LAYER_SCENE_H, 1.f);
            app.showLayer(FitsViewer.LAYER_SCENE_J, 0.5f);
        }
        else if(code==KeyEvent.VK_5){
            app.hideLayer(FitsViewer.LAYER_SCENE_KS);
            app.hideLayer(FitsViewer.LAYER_SCENE_H);
            app.showLayer(FitsViewer.LAYER_SCENE_J, 1.f);
        }
        else if(code==KeyEvent.VK_6){
            app.showLayer(FitsViewer.LAYER_SCENE_KS, 1.f);
            app.showLayer(FitsViewer.LAYER_SCENE_H, 0.66f);
            app.showLayer(FitsViewer.LAYER_SCENE_J, 0.33f);
        }
        // else if (code == KeyEvent.VK_MINUS){
        //     //app.scaleBounds[1] -= 100;
        //     //app.img.rescale(app.scaleBounds[0], app.scaleBounds[1], 1);
        // }
        // else if (code == KeyEvent.VK_PLUS){
        //     //app.scaleBounds[1] += 100;
        //     //app.img.rescale(app.scaleBounds[0], app.scaleBounds[1], 1);
        // }
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}



    void pan(Camera c, int dx, int dy){
        synchronized(c){
            double a = (c.focal+Math.abs(c.altitude)) / c.focal;
            c.move(a*dx, a*dy);
        }
    }

    void zoom(Camera c, double vx, double vy, short direction){
        double a = (c.focal+Math.abs(c.altitude)) / c.focal;
        if (direction  == WHEEL_UP){
            // zooming out
            c.move(-((vx - c.vx) * WHEEL_ZOOMOUT_FACTOR / c.focal),
                   -((vy - c.vy) * WHEEL_ZOOMOUT_FACTOR / c.focal));
            c.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
        }
        else {
            // direction == WHEEL_DOWN, zooming in
            if (c.getAltitude()-a*WHEEL_ZOOMIN_FACTOR >= c.getZoomFloor()){
                // this test to prevent translation when camera is not actually zoming in
                c.move((vx - c.vx) * WHEEL_ZOOMIN_FACTOR / c.focal,
                       ((vy - c.vy) * WHEEL_ZOOMIN_FACTOR / c.focal));
            }
            c.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
        }
    }


    void fadeOutRightClickSelection(){
        Animation a = am.getAnimationFactory().createTranslucencyAnim(300,
                            rightClickSelectionG, 0f, false, IdentityInterpolator.getInstance(),
                            new EndAction(){
                                public void execute(Object subject, Animation.Dimension dimension){
                                    app.cursorSpace.removeGlyph(rightClickSelectionG);
                                    rightClickSelectionG.setTranslucencyValue(SEL_ALPHA);
                                }
                            });
        am.startAnimation(a, true);
    }

}


/*
class JSkyFitsViewerEventHandler implements ViewListener, ComponentListener, CameraListener {

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    //remember last mouse coords to compute translation  (dragging)
    int lastJPX,lastJPY;
    long lastVX, lastVY;

    JSkyFitsViewer application;

    Glyph g;

    double oldCameraAltitude;

    boolean zero_order_dragging = false;
    boolean first_order_dragging = false;
	static final short ZERO_ORDER = 0;
	static final short FIRST_ORDER = 1;
	short navMode = ZERO_ORDER;

	Glyph objectJustSelected = null;



    JSkyFitsViewerEventHandler(JSkyFitsViewer app){
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
        *
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

        if (app.mnCamera.isEnabled() && app.menu.cursorInside(jpx, jpy)){
            v.parent.setActiveLayer(app.LAYER_MENU);
            v.parent.setCursorIcon(Cursor.DEFAULT_CURSOR);
        }
        /*
        if((jpx < application.menu.WIDTH_MENU && jpy < application.menu.BORDER_BOTTOM_FILTER && jpy > application.menu.BORDER_TOP_FILTER) ||
          (jpy > application.menu.BORDER_TOP_HISTOGRAM && jpy < application.menu.BORDER_BOTTOM_HISTOGRAM && jpx > application.menu.BORDER_LEFT_HISTOGRAM && jpx < application.menu.BORDER_RIGHT_HISTOGRAM )){
            v.parent.setActiveLayer(application.LAYER_MENU);
            v.parent.setCursorIcon(Cursor.DEFAULT_CURSOR);
        }
        */

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
*
        
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

    /*ComponentListener*
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
    *

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
*/