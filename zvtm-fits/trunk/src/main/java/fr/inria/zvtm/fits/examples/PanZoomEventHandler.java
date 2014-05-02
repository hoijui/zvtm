

package fr.inria.zvtm.fits.examples;

import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Glyph;


import java.awt.Cursor;
import java.awt.image.ImageFilter;
import java.awt.geom.Point2D;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;



class PanZoomEventHandler implements ViewListener {

    public static final String T_FILTER = "Fltr";

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;
    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;


    FitsExample app;


    //private double[] scaleBounds;
    private boolean dragLeft = false, dragRight = false;
    //private RangeSelection rs;

	private int lastJPX;
	private int lastJPY;

    boolean zero_order_dragging = false;
    boolean first_order_dragging = false;
    static final short ZERO_ORDER = 0;
    static final short FIRST_ORDER = 1;
    short navMode = ZERO_ORDER;


    PanZoomEventHandler(FitsExample app){
        this.app = app;
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

        zero_order_dragging = false;
        if (first_order_dragging){
            Camera c = app.mCamera;
            c.setXspeed(0);
            c.setYspeed(0);
            c.setZspeed(0);
            v.setDrawDrag(false);
            first_order_dragging = false;
        }
        
    }

	public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        //v.parent.setActiveLayer(2);
        /*
		lastJPX=jpx;
		lastJPY=jpy;
		v.setDrawDrag(true);
		app.vsm.getActiveView().mouse.setSensitivity(false);
        */
		//because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
	}

	public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        /*
		v.cams[0].setXspeed(0);
		v.cams[0].setYspeed(0);
		v.cams[0].setZspeed(0);
		v.setDrawDrag(false);
		app.vsm.getActiveView().mouse.setSensitivity(true);
        */
        //v.parent.setActiveLayer(0);
	}

	public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){

        /*
        app.setCursorCoords(v.getVCursor().getVSXCoordinate(), v.getVCursor().getVSYCoordinate());
        VirtualSpaceManager.INSTANCE.repaint();
        */

        //System.out.println(app.menu.BORDER_BOTTON_HISTOGRAM + " > " + jpy + " > " + app.menu.BORDER_TOP_HISTOGRAM);
        //System.out.println(app.menu.BORDER_LEFT_HISTOGRAM + " < " + jpx + " < " + app.menu.BORDER_RIGHT_HISTOGRAM);

        if((jpx < app.WIDTH_MENU && jpy > app.menu.BORDER_BOTTON_FILTER && jpy < app.menu.BORDER_TOP_FILTER) || (jpy > app.menu.BORDER_TOP_HISTOGRAM && jpy < app.menu.BORDER_BOTTON_HISTOGRAM && jpx > app.menu.BORDER_LEFT_HISTOGRAM && jpx < app.menu.BORDER_RIGHT_HISTOGRAM )){
            v.parent.setActiveLayer(2);
            v.parent.setCursorIcon(Cursor.DEFAULT_CURSOR);
        } else {
            v.parent.setActiveLayer(0);
            v.parent.setCursorIcon(Cursor.CUSTOM_CURSOR);
        }

    }

	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){

        Camera c = app.mCamera;
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

		if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
			Camera c = app.vsm.getActiveCamera();
			double a = (c.focal+Math.abs(c.altitude))/c.focal;
			if (mod == META_SHIFT_MOD) {
				v.cams[0].setXspeed(0);
				v.cams[0].setYspeed(0);
				v.cams[0].setZspeed((c.altitude>0) ? (lastJPY-jpy)*(a/4.0) : (lastJPY-jpy)/(a*4));

			}
			else {
				v.cams[0].setXspeed((c.altitude>0) ? (jpx-lastJPX)*(a/4.0) : (jpx-lastJPX)/(a*4));
				v.cams[0].setYspeed((c.altitude>0) ? (lastJPY-jpy)*(a/4.0) : (lastJPY-jpy)/(a*4));
				v.cams[0].setZspeed(0);
			}
		}
        */
	}

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        double a = (app.mCamera.focal+Math.abs(app.mCamera.altitude)) / app.mCamera.focal;
        double mvx = v.getVCursor().getVSXCoordinate();
        double mvy = v.getVCursor().getVSYCoordinate();
        if (wheelDirection  == WHEEL_UP){
            // zooming out
            app.mCamera.move(-((mvx - app.mCamera.vx) * WHEEL_ZOOMOUT_FACTOR / app.mCamera.focal),
                                     -((mvy - app.mCamera.vy) * WHEEL_ZOOMOUT_FACTOR / app.mCamera.focal));
            app.mCamera.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
            app.vsm.repaint();
        }
        else {
            //wheelDirection == WHEEL_DOWN, zooming in
            if (app.mCamera.getAltitude()-a*WHEEL_ZOOMIN_FACTOR >= app.mCamera.getZoomFloor()){
                // this test to prevent translation when camera is not actually zoming in
                app.mCamera.move((mvx - app.mCamera.vx) * WHEEL_ZOOMIN_FACTOR / app.mCamera.focal,
                                         ((mvy - app.mCamera.vy) * WHEEL_ZOOMIN_FACTOR / app.mCamera.focal));
            }
            app.mCamera.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
            app.vsm.repaint();
        }
    }

	public void enterGlyph(Glyph g){}

	public void exitGlyph(Glyph g){}

	public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if(c == '-'){
            app.scaleBounds[1] -= 100;
            app.hi.rescale(app.scaleBounds[0], app.scaleBounds[1], 1);
        } else if (c == '+'){
            app.scaleBounds[1] += 100;
            app.hi.rescale(app.scaleBounds[0], app.scaleBounds[1], 1);
        }
    }

	public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	}

	public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

	public void viewActivated(View v){}

	public void viewDeactivated(View v){}

	public void viewIconified(View v){}

	public void viewDeiconified(View v){}

	public void viewClosing(View v){System.exit(0);}

    void toggleNavMode(){
        switch(navMode){
            case FIRST_ORDER:{navMode = ZERO_ORDER;break;}
            case ZERO_ORDER:{navMode = FIRST_ORDER;break;}
        }
    }

}