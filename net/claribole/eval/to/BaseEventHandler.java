/*   FILE: BaseEventHandler.java
 *   DATE OF CREATION:  Thu Oct 12 12:08:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *
 * $Id:  $
 */ 

package net.claribole.eval.to;

import java.awt.Toolkit;
import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.engine.*;


abstract class BaseEventHandler implements ViewEventHandler {

    Eval application;

    public void press1(ViewPanel v, int mod, int jpx, int jpy, java.awt.event.MouseEvent e){}
    public void release1(ViewPanel v, int mod, int jpx, int jpy, java.awt.event.MouseEvent e){}
    public void click1(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, java.awt.event.MouseEvent e){}


    public void press2(ViewPanel v, int mod, int jpx, int jpy, java.awt.event.MouseEvent e){}
    public void release2(ViewPanel v, int mod, int jpx, int jpy, java.awt.event.MouseEvent e){}
    public void click2(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, java.awt.event.MouseEvent e){}

    public void press3(ViewPanel v, int mod, int jpx, int jpy, java.awt.event.MouseEvent e){}
    public void release3(ViewPanel v, int mod, int jpx, int jpy, java.awt.event.MouseEvent e){}
    public void click3(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, java.awt.event.MouseEvent e){}

    public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, java.awt.event.MouseEvent e){}
    public void mouseMoved(ViewPanel v, int jpx, int jpy, java.awt.event.MouseEvent e){}

    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, java.awt.event.MouseWheelEvent e){}

    public void enterGlyph(Glyph g){}
    public void exitGlyph(Glyph g){}

    public void Kpress(ViewPanel v, char c, int code, int mod, java.awt.event.KeyEvent e){}
           
    public void Krelease(ViewPanel v, char c, int code, int mod, java.awt.event.KeyEvent e){}
           
    public void Ktype(ViewPanel v, char c, int code, int mod, java.awt.event.KeyEvent e){}

    public void viewActivated(View v){}
    
    public void viewClosing(View v){}
           
    public void viewDeactivated(View v){}
           
    public void viewDeiconified(View v){}
           
    public void viewIconified(View v){}
    
}