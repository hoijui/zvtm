/*   FILE: LocateTask.java
 *   DATE OF CREATION:  Sat Apr 22 10:05:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.eval;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.Glyph;

import net.claribole.zvtm.engine.ViewEventHandler;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

class LocateTaskEventHandler implements ViewEventHandler {

    LocateTask application;

    LocateTaskEventHandler(LocateTask appli){
	application = appli;
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
	if (application.locatingCity){
	    VCursor c = v.getMouse();
	    application.locatingCity(c.vx, c.vy);
	}
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){}

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){}

    public void enterGlyph(Glyph g){}
    
    public void exitGlyph(Glyph g){}

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code == KeyEvent.VK_Q && mod == CTRL_MOD){System.exit(0);}
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code==KeyEvent.VK_S){application.startSession();}
	else if (code==KeyEvent.VK_SPACE){
	    if (application.trialCount >= 0){
		if (!application.noLocationSpecifiedYet){
		    application.locatedCity();
		}
	    }
	    else {
		application.nextStep();
	    }
	}
	else if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
    }

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){}

}
