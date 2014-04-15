/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2013. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
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
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Dimension;
import java.awt.Color;



import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VCursor;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VRing;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.event.CameraListener;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.event.PortalListener;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.lens.*;

import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.TextDescription;
import fr.inria.zuist.engine.LensMenu;



class TIVEventHandler implements ViewListener, ComponentListener, PortalListener, CameraListener {

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;

    static float WHEEL_MM_STEP = 1.0f;

    static float WHEEL_ALPHA_FACTOR=0.05f;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    double lastVX, lastVY;
    int currentJPX, currentJPY;
    int lastCenterX, lastCenterY;

    boolean mCamStickedToMouse = false;
    boolean regionStickedToMouse = false;
    boolean inPortal = false;

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
    VCircle lastPoint;

	static final int DELAYED_UPDATE_FREQUENCY = 400;
    List <String> lines  = new ArrayList <String> ();

    DelayedUpdateTimer dut;

    int enterPointPosition;
    int enterPointLens;
    int enterPointContext;
    int enterPointPieMenu;


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
		if (inPortal && application.mode !=0){
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
            if (navMode == FIRST_ORDER){
                first_order_dragging = true;
                v.setDrawDrag(true);
            }
            else {
                // ZERO_ORDER
                zero_order_dragging = true;
            }
        }
        translating = zero_order_dragging || first_order_dragging || regionStickedToMouse;
        if (translating){
            application.sm.enableRegionUpdater(false);
        }
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        //System.out.println("Realease 1");
		regionStickedToMouse = false;
		zero_order_dragging = false;
		if (translating){
            //System.out.print("Releaaaase 1.1");
    		translating = false;
    		application.sm.enableRegionUpdater(true);
		}
        if (first_order_dragging){
            //System.out.print("Releaaaase 1.2");
            Camera c = application.mCamera;
            c.setXspeed(0);
            c.setYspeed(0);
            c.setZspeed(0);
            v.setDrawDrag(false);
            first_order_dragging = false;
        }
	    if (selectingRegion){
            //System.out.print("Releaaaase 1.3");
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
        if(application.mode==application.none)
        {
            lines.add(lastVX+":"+lastVY);
            lastPoint = new VCircle(lastVX,lastVY,11,2,Color.PINK, Color.PINK);
            lastPoint.setType("PathPoint");
            application.mSpace.addGlyph(lastPoint);
        }


        if(application.mode==application.lenses || application.mode==application.routeLens)
        {
            if(!nm.lense)
            {
                //System.out.print("!nm.lense");
                nm.lense=true;
                if (!inPortal){
                    //System.out.print("HEREEEE");
                    if (nm.lensType != TIVNavigationManager.NO_LENS){
                        //System.out.println("Here 1");
                        nm.zoomInPhase2(lastVX, lastVY);
                    }
                    else {
                        if (cursorNearBorder){
                        // do not activate the lens when cursor is near the border
                            nm.lense=false;
                            return;
                        }
                        nm.zoomInPhase1(jpx, jpy);
                        //System.out.println("Here 2");
                    }
                }
            }
            else      
            {
                //System.out.print("nm.lense");
                nm.lense=false;
                if (nm.lensType != TIVNavigationManager.NO_LENS){
                nm.zoomOutPhase2();
                //System.out.println("Here 3");
                }
                else {
                    if (cursorNearBorder){
                    // do not activate the lens when cursor is near the border
                        return;
                    }
                 nm.zoomOutPhase1(jpx, jpy, lastVX, lastVY);
                 //System.out.println("Here 4");
                }

            }
        }
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
         lastJPX = jpx;
        lastJPY = jpy;
        lastVX = v.getVCursor().getVSXCoordinate();
        lastVY = v.getVCursor().getVSYCoordinate();
        String [] lenseOptions = {"Littoral", "Scan"};
        String [] contextOptions = {"Ortho", "Littoral"};
         v.parent.setActiveLayer(application.menuCamera);
         nm.showLensMenu(v,lastVX,lastVY);
        //LensMenu lensMenu = new LensMenu(lenseOptions,contextOptions, v.parent.getActiveCamera().getOwningSpace(),80,120, new Point2D.Double(lastVX,lastVY));
        
    }

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
         System.out.println("Release 2");
        
    }

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        //System.out.println(application.orthoView.getCursor().getStickedGlyphsNumber());
        //System.out.println(application.mView.getCursor().getStickedGlyphsNumber());
    }

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        //lastVX = v.getVCursor().getVSXCoordinate();
        //lastVY = v.getVCursor().getVSYCoordinate();
        if(application.mode==application.lenses)
        {
            lastCenterX = jpx;
            lastCenterY = jpy;
             v.parent.setActiveLayer(application.menuCamera);
                Camera c=v.parent.getActiveCamera();
                System.out.println(c.getOwningSpace().getName());
            if(!nm.lense) {
                application.displayMainPieMenu(true);
                nm.pieMenuCenter = new Point2D.Double(application.mView.mouse.getVSXCoordinate(),application.mView.mouse.getVSYCoordinate());
                }
            if (nm.lense) {
                
                nm.setLensRadius(nm.lensMenuRadius*2, nm.lensMenuRadius*2); 
                v.parent.setActiveLayer(application.menuCamera);
                nm.showLensMenu(v, application.mView.mouse.getVSXCoordinate(),application.mView.mouse.getVSYCoordinate());
            }
        }

    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            Glyph g = v.lastGlyphEntered();
            if (g != null && g.getType() == Messages.PM_ENTRY){
                //nm.saveLayerVisibility();
                application.updateLayer(g);
                application.pieMenuEvent(g);
            }
            if(g != null && (g.getType()==Messages.LENS_MENU_LENS || g.getType()==Messages.LENS_MENU_CONTEXT)) {
                nm.lenseLayer = nm.temporaryLenseLayer;
                nm.contextLayer = nm.temporaryContextLayer;
            }

            if (application.mainPieMenu != null){
                application.displayMainPieMenu(false);
            }
            if(nm.lense && application.mode == application.lenses && nm.lensMenu!=null)
            {
                nm.returnOriginalRadius();
                nm.lensMenu.hideLensMenu();
            }
            v.parent.setActiveLayer(application.mCamera);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        if(application.mode==application.none)
        {
            application.mSpace.removeGlyph(lastPoint);
            lines.remove(lines.size()-1);
        }
        
        //lastVX = v.getVCursor().getVSXCoordinate();
        //lastVY = v.getVCursor().getVSYCoordinate();
         /*if(application.mode == application.swipe || application.mode == application.alpha_swipe)
            {
                System.out.println("CLICK 3");
                if(nm.contextLayer==Messages.SCAN)
                {
                    System.out.println("Change to ORTHO");
                    nm.hideLayer(application.scanSpace);
                    nm.showLayer(application.orthoSpace);
                }
                else
                {
                    System.out.println("Change to SCAN");
                    nm.hideLayer(application.orthoSpace);
                    nm.showLayer(application.scanSpace);
                }
            }*/
        
    }

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
//        System.err.println(v.getVCursor().vx+" "+v.getVCursor().vy);
        //System.out.print("mouse: "+jpx+","+jpy);
        if(application.mode==application.covisualization2) {
            nm.moveFakeCursor(v,v.getVCursor().getVSXCoordinate(),v.getVCursor().getVSYCoordinate());
        }
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
            if(nm.rLens != null)
            {
                nm.rLens.moveLens(jpx, jpy);
            }
    	}
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	    Camera c = application.mCamera;
        //System.out.println("Mouse moved "+jpx + " "+ jpy);
        /*if(nm.lense && application.mainPieMenu!=null)
        {
            if((jpx-lastCenterX)*(jpx-lastCenterX)+(jpy-lastCenterY)*(jpy-lastCenterY)>=150*150)
            {
                boolean delete = false;
                if(delete)
                {
                nm.lense=false;
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
                application.displayMainPieMenu(false);
                }
                else
                {
                    lastCenterX = jpx;
                    lastCenterY = jpy;
                    nm.moveLens(jpx,jpy, e.getWhen());
                    application.displayMainPieMenu(false);
                    application.displayMainPieMenu(true);
                    nm.changeLayers(nm.lenseLayer);


                }
                //robot.mouseMove(lastInsideX,lastInsideY);
            }
        }*/
        if(nm.lense && nm.lensMenu!=null)
        {
            Glyph g = v.lastGlyphEntered();
            //System.out.println("Glyph in mouseDragged"+g);
            int pos = nm.lensMenu.positionInMenu(new Point2D.Double(application.mView.mouse.getVSXCoordinate(), application.mView.mouse.getVSYCoordinate()));
            if(g==null && pos!=-1) {enterPointPosition = pos;
                //System.out.println("Enter Point "+pos);
            }
            

            /*if((jpx-lastCenterX)*(jpx-lastCenterX)+(jpy-lastCenterY)*(jpy-lastCenterY)>=(nm.lensMenuRadius*3)*(nm.lensMenuRadius*3))
            {
                boolean delete = false;
                if(delete)
                {
                nm.lense=false;
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
                nm.lensMenu.hideLensMenu();
                }
                else
                {
                    lastCenterX = jpx;
                    lastCenterY = jpy;
                    nm.moveLens(jpx,jpy, e.getWhen());
                    nm.lensMenu.hideLensMenu();
                    nm.showLensMenu(v,application.mView.mouse.getVSXCoordinate(),application.mView.mouse.getVSYCoordinate());
                    nm.changeLayers(nm.lenseLayer);


                }
                //robot.mouseMove(lastInsideX,lastInsideY);
            }*/
        }
        if (application.mainPieMenu != null) {
             Glyph g = v.lastGlyphEntered();
            //System.out.println("Glyph in mouseDragged"+g);
            int pos = application.positionInPieMenu(new Point2D.Double(application.mView.mouse.getVSXCoordinate(), application.mView.mouse.getVSYCoordinate()));
            if(g==null) {
                if(pos!=-1 && pos!=1) {
                enterPointPosition=pos;
                }
            }
            else if(g.getType()==null) {
                if(pos!=-1 && pos!=1) {
                enterPointPosition=pos;
                }
            }
        }                                                                          
        if (zero_order_dragging){
            //System.out.println("Zero order");
            double a = (c.focal+Math.abs(c.altitude)) / c.focal;
            c.move(a*(lastJPX-jpx), a*(jpy-lastJPY));
            lastJPX = jpx;
            lastJPY = jpy;
		    if (nm.lensType != 0 && nm.lens != null){
			    nm.moveLens(jpx, jpy, e.getWhen());
		    }
        }
        else if (first_order_dragging){
            //System.out.println("First order");
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
	        double a = (application.ovCamera.focal+Math.abs(application.ovCamera.altitude)) / application.ovCamera.focal;
			c.move(a*(jpx-lastJPX), a*(lastJPY-jpy));
			lastJPX = jpx;
            lastJPY = jpy;
		}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        if (nm.lensType != 0 && nm.lens != null){
            if (wheelDirection  == ViewListener.WHEEL_UP){
                if (application.mode==application.lenses) {
                    //System.out.println("Update translucency");
                    nm.updateTranslucency(WHEEL_ALPHA_FACTOR, jpx, jpy);
                }
                else if(application.mode == application.routeLens)
                    nm.magnifyFocus(TIVNavigationManager.WHEEL_MM_STEP, nm.lensType, application.mCamera);
            }
            else {
                if(application.mode == application.lenses)
                    nm.updateTranslucency(-WHEEL_ALPHA_FACTOR, jpx, jpy);
                else if (application.mode==application.routeLens)
                    nm.magnifyFocus(-TIVNavigationManager.WHEEL_MM_STEP, nm.lensType, application.mCamera);
            }
            //nm.zoomInPhase1(jpx,jpy);
        }
        else {
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
    }

    public void enterGlyph(Glyph g ){
        //System.out.println("Enter Glyph " +g.getType());
        if(g.getType() == Messages.PM_ENTRY)
        {
            application.pieMenuEvent(g);
            enterPointPieMenu = enterPointPosition;
            //System.out.println("PM_ENTRY");
        }
        if(g.getType() == Messages.LENS_MENU_LENS)
        {
            nm.lensMenu.highlight(g);
            //System.out.println("GLYPH: "+nm.lensMenu.getLabel(g));
            //System.out.println("Enter Point in enterGlyph"+nm.lensMenu.positionInMenu(new Point2D.Double(application.mView.mouse.getVSXCoordinate(),application.mView.mouse.getVSYCoordinate())));
            nm.lensMenuChangeLayer(g);
            enterPointLens = enterPointPosition;
        }
         if(g.getType() == Messages.LENS_MENU_CONTEXT)
        {
            nm.lensMenu.highlight(g);
            //System.out.println("GLYPH: "+nm.lensMenu.getLabel(g));
            nm.lensMenuChangeLayer(g);
            enterPointContext = enterPointPosition;
            //System.out.println("Enter Point in enterGlyph"+nm.lensMenu.positionInMenu(new Point2D.Double(application.mView.mouse.getVSXCoordinate(),application.mView.mouse.getVSYCoordinate())));
        }
        //System.out.println("GLYPH " + g.getZindex());
//        g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
        //System.out.println("Exit Glyph "+g.getType());

        if(g.getType() == Messages.PM_ENTRY)
        {
                //System.out.println("IN IF, EXIT GLYPH")
            int cursorPosition = application.positionInPieMenu(new Point2D.Double(application.mView.mouse.getVSXCoordinate(), application.mView.mouse.getVSYCoordinate()));
            //System.out.println("Cursor_coordinates" + cursorPosition);
            if(cursorPosition != 1) {
                //System.out.println("application.positionInPieMenu "+ cursorPosition);
                //System.out.println("enterPointPieMenu "+enterPointPieMenu);
                if(cursorPosition == enterPointPieMenu)
                {
                    //System.out.println("Context Layer" + nm.contextLayer);
                    nm.changeLayers(nm.contextLayer);
                }
                else
                {
                    application.updateLayer(g);
                }
            }
        }
        //if(application.mView.getPanel().lastGlyphEntered()!=null)
            //{System.out.println("Last Glyph" + application.mView.getPanel().lastGlyphEntered());}
            //application.mView.getPanel().lastGlyphEntered().setColor(Color.YELLOW);

        if(g.getType() == Messages.LENS_MENU_LENS)   
        {
            //System.out.println("Enter Point "+enterPointLens);
            //System.out.println("Exit Point "+nm.lensMenu.positionInMenu(new Point2D.Double(application.mView.mouse.getVSXCoordinate(),application.mView.mouse.getVSYCoordinate())));
            nm.lensMenu.unHighlight(g);
            if(!nm.lensMenu.isInShowedMenu(new Point2D.Double(application.mView.mouse.getVSXCoordinate(),application.mView.mouse.getVSYCoordinate()))) {
                if(enterPointLens == nm.lensMenu.positionInMenu(new Point2D.Double(application.mView.mouse.getVSXCoordinate(),application.mView.mouse.getVSYCoordinate()))) {
                    
                    nm.changeLayerLense(nm.lenseLayer);
                    nm.temporaryLenseLayer = nm.lenseLayer;
                    
                }
                else {
                    //nm.lenseLayer = nm.temporaryLenseLayer;
                    nm.lenseLayer = nm.temporaryLenseLayer;
                    //nm.changeLensOptions();
                    //nm.lensMenu.hideLensMenu();
                }
            //nm.lensMenu.exitShowEvent(g,new Point2D.Double(application.mView.mouse.getVSXCoordinate(),application.mView.mouse.getVSYCoordinate()));
            }
        }
        if(g.getType() == Messages.LENS_MENU_CONTEXT)
        {
            nm.lensMenu.unHighlight(g);
            //System.out.println("Enter Point "+enterPointContext);
            //System.out.println("Exit Point "+nm.lensMenu.positionInMenu(new Point2D.Double(application.mView.mouse.getVSXCoordinate(),application.mView.mouse.getVSYCoordinate())));
            if(!nm.lensMenu.isInShowedMenu(new Point2D.Double(application.mView.mouse.getVSXCoordinate(),application.mView.mouse.getVSYCoordinate()))) {
                if(enterPointContext == nm.lensMenu.positionInMenu(new Point2D.Double(application.mView.mouse.getVSXCoordinate(),application.mView.mouse.getVSYCoordinate()))) {
                    nm.changeLayerContext(nm.contextLayer);
                    nm.temporaryContextLayer = nm.contextLayer;
                    //System.out.println("Enter Point "+enterPointPosition);
                    //System.out.println("Exit Point "+nm.lensMenu.positionInMenu(new Point2D.Double(application.mView.mouse.getVSXCoordinate(),application.mView.mouse.getVSYCoordinate())));
                }
                else {
                    //nm.lenseLayer = nm.temporaryLenseLayer;
                    nm.contextLayer = nm.temporaryContextLayer;
                    //nm.changeContextOptions();
                    //nm.lensMenu.hideLensMenu();
                }
            }
            //nm.lensMenu.exitShowEvent(g,new Point2D.Double(application.mView.mouse.getVSXCoordinate(),application.mView.mouse.getVSYCoordinate()));
        }
      

        /*if(nm.lensMenu.isInCenterMenu(new Point2D.Double(application.mView.mouse.getVSXCoordinate(),application.mView.mouse.getVSYCoordinate())) && nm.lensMenu.getLayerType(g)=="context"){
            nm.changeLayers(nm.contextLayer);
            nm.returnLensAndContext();
        }*/


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
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if(c=='t')
        {
            for (Glyph g : application.orthoSpace.getAllGlyphs())
        {
            g.setTranslucencyValue(0.5f);
        }
        }
        /*if(c=='b')
        {
            application.nm.lensFamily=application.nm.BInverseCosine;
        }
        if(c=='g')
        {
             application.nm.lensFamily=application.nm.BGaussianLens;
        }
        if(c=='r')
        {
            application.nm.lensFamily=application.nm.Blense;
        }*/

        if(c=='k')
        {
            if(application.mode == application.swipe)
            {

                if(nm.contextLayer==Messages.SCAN)
                {
                    System.out.println("Change to ORTHO");
                    nm.hideLayer(application.scanSpace);
                    nm.showLayer(application.orthoSpace);
                    //nm.changeLayerContext(Messages.ORTHO);
                    nm.contextLayer = Messages.ORTHO;
                }
                else
                {
                    System.out.println("Change to SCAN");
                    nm.hideLayer(application.orthoSpace);
                    nm.showLayer(application.scanSpace); 
                    //nm.changeLayerContext(Messages.SCAN);
                    nm.contextLayer = Messages.SCAN;
                }
            }
            if(application.mode == application.lenses)
            {
                application.nm.toggleLensType();
            }
        }

        if(c=='j')
        {
            if (application.mode == application.alpha_swipe) {
                nm.alphaLayer(application.scanSpace,1);
            }
            /*for (Glyph g: application.scanSpace.getAllGlyphs())
                application.scanSpace.hide(g);*/
        }

        if(c=='h')
        {
            if (application.mode == application.alpha_swipe) {
                nm.alphaLayer(application.scanSpace,0);
            }
        }

        if(c=='p')
        {
           if (application.mode == application.routeLens) {
                nm.rLens.setP(nm.rLens.getP()+1); 
                System.out.println("P: "+nm.rLens.getP());
            }
        }
        
        if(c=='o')
        {
            if(application.mode == application.none) {
                nm.writePath(lines);
            }
            if(application.mode == application.routeLens) {
                if(nm.rLens.getP()>0) {
                nm.rLens.setP(nm.rLens.getP()-1);
                }
                System.out.println("P: "+nm.rLens.getP());
            }

        }
        
        if(c=='q')
        {
            if(application.mode==application.none) {
                nm.readPath();
            }
        }
        if(c=='w')
        {
            if(application.mode==application.none) {
                lines.add("-----");
            }
        }
        if(c=='d')
        {
            v.parent.setActiveLayer(application.menuCamera);
            v.parent.getActiveCamera().getOwningSpace().addGlyph(new VRing(0, 0, 0, 100, Math.PI/2, 0.5f, Math.PI*3/4, Color.BLACK, Color.BLACK));
        }

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

	/* Overview Portal */
	public void enterPortal(Portal p){
		inPortal = true;
		((OverviewPortal)p).setBorder(TIVNavigationManager.OV_INSIDE_BORDER_COLOR);
		VirtualSpaceManager.INSTANCE.repaint();
        System.out.print("ENTER Portal");
	}

	public void exitPortal(Portal p){
		inPortal = false;
		((OverviewPortal)p).setBorder(TIVNavigationManager.OV_BORDER_COLOR);
		VirtualSpaceManager.INSTANCE.repaint();
        System.out.print("EXIT Portal");
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
