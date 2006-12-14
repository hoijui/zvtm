
package net.claribole.eval.alphalens;

import java.awt.Toolkit;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.engine.*;


class BaseEventHandlerPointing implements ViewEventHandler, ComponentListener {

    static final float MAIN_SPEED_FACTOR = 50.0f;
    static final float LENS_SPEED_FACTOR = 5.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    EvalPointing application;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
//     int cjpx, cjpy;
    long lastVX, lastVY;

    float projCoef, alt, oldCameraAltitude; // for efficiency

    BaseEventHandlerPointing(EvalPointing app){
	this.application = app;
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}

    public void click1(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){
	//application.selectTarget(v.lastGlyphEntered());
	application.setLens(jpx, jpy);
    }

    public void press2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void release2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void click2(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void release3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void click3(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e){
	application.moveLens(jpx, jpy, System.currentTimeMillis());
// 	cjpx = jpx;
// 	cjpy = jpy;
    }

    public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, MouseEvent e){
// 	cjpx = jpx;
// 	cjpy = jpy;
    }

    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e){}

    public void enterGlyph(Glyph g){}
    public void exitGlyph(Glyph g){}

    public void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e){}
           
    public void Krelease(ViewPanel v, char c, int code, int mod, KeyEvent e){}
           
    public void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e){}

    public void viewActivated(View v){}
    
    public void viewClosing(View v){application.exit();}
           
    public void viewDeactivated(View v){}
           
    public void viewDeiconified(View v){}
           
    public void viewIconified(View v){}

    /* ComponentListener */
    public void componentHidden(ComponentEvent e){}

    public void componentMoved(ComponentEvent e){}

    public void componentResized(ComponentEvent e){
	application.updatePanelSize();
    }

    public void componentShown(ComponentEvent e){}

    void cameraMoved(){
	alt = application.mCamera.getAltitude();
	if (alt != oldCameraAltitude){
	    oldCameraAltitude = alt;
	}
    }

}