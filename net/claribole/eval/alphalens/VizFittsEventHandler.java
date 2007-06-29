/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: FittsEventHandler.java 700 2007-06-27 12:21:09Z epietrig $
 */

package net.claribole.eval.alphalens;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import net.claribole.zvtm.engine.ViewEventHandler;

import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.glyphs.Glyph;


class VizFittsEventHandler implements ViewEventHandler, ComponentListener {

    static final float MAIN_SPEED_FACTOR = 50.0f;
    static final float LENS_SPEED_FACTOR = 5.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    VizFitts application;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    int cjpx, cjpy;
    long lastVX, lastVY;

    float projCoef, alt, oldCameraAltitude; // for efficiency

    boolean cursorNearBorder = false;

    VizFittsEventHandler(VizFitts app){
	this.application = app;
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}

    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}

    public void click1(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void release2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void click2(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void release3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void click3(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e){ }

    public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, MouseEvent e){ }

    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e){}

    public void enterGlyph(Glyph g){}
    public void exitGlyph(Glyph g){}

    public void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e){ }
           
    public void Krelease(ViewPanel v, char c, int code, int mod, KeyEvent e){ }
           
    public void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e){}

    public void viewActivated(View v){}
    
    public void viewClosing(View v){}
           
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

    void cameraMoved(){ }

}