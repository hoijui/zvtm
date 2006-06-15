/*   FILE: LocateTask.java
 *   DATE OF CREATION:  Sat Apr 22 10:05:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: LocateTask2EventHandler.java,v 1.6 2006/06/03 11:22:31 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VRectangle;

import net.claribole.zvtm.engine.ViewEventHandler;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

class LocateTask2EventHandler implements ViewEventHandler {

    static final String DRAW_REGION_ERR = "Please draw the rectangle delimiting the region.";

    LocateTask2 application;
    long x1,y1,x2,y2;                     //remember last mouse coords to display selection rectangle (dragging)
    long nwx,nwy,sex,sey;

    VRectangle selectionRect;

    LocateTask2EventHandler(LocateTask2 appli){
	application = appli;
	selectionRect = new VRectangle(0, 0, 0, 1, 1, Color.RED);
	application.vsm.addGlyph(selectionRect, application.mainVS);
	selectionRect.setVisible(false);
	selectionRect.setFill(false);
	selectionRect.setBorderColor(Color.RED);
    }

    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){}
    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.sessionStarted){return;}
	if (application.noLocationSpecifiedYet){
	    selectionRect.setVisible(true);
	}
	x1 = v.getMouse().vx;
	y1 = v.getMouse().vy;
	x2 = x1 + 1;
	y2 = y1 + 1;
	nwx = Math.min(x1,x2);
	nwy = Math.max(y1,y2);
	sex = Math.max(x1,x2);
	sey = Math.min(y1,y2);
	selectionRect.vx = (nwx+sex)/2;
	selectionRect.vy = (nwy+sey)/2;
	selectionRect.setWidth((sex-nwx)/2);
	selectionRect.setHeight((nwy-sey)/2);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (!application.sessionStarted){return;}
	application.drewRegion(selectionRect.vx-selectionRect.getWidth(),
			       selectionRect.vy+selectionRect.getHeight(),
			       selectionRect.vx+selectionRect.getWidth(),
			       selectionRect.vy-selectionRect.getHeight());
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (buttonNumber == 3){
	    x2 = v.getMouse().vx;
	    y2 = v.getMouse().vy;
	    nwx = Math.min(x1,x2);
	    nwy = Math.max(y1,y2);
	    sex = Math.max(x1,x2);
	    sey = Math.min(y1,y2);
	    selectionRect.vx = (nwx+sex)/2;
	    selectionRect.vy = (nwy+sey)/2;
	    selectionRect.setWidth((sex-nwx)/2);
	    selectionRect.setHeight((nwy-sey)/2);
	}
    }

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
	    if (application.sessionStarted){
		if (!application.noLocationSpecifiedYet){
		    application.validateRegion();
		}
		else {
		    application.qpanel.warn(DRAW_REGION_ERR, application.questionText, LogManager.WARN_MSG_DELAY);
		}
	    }
	}
	else if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
	else if (code==KeyEvent.VK_HOME){application.getGlobalView();}
	else if (code==KeyEvent.VK_UP){application.translateView(ZLWorldTask.MOVE_UP);}
	else if (code==KeyEvent.VK_DOWN){application.translateView(ZLWorldTask.MOVE_DOWN);}
	else if (code==KeyEvent.VK_LEFT){application.translateView(ZLWorldTask.MOVE_LEFT);}
	else if (code==KeyEvent.VK_RIGHT){application.translateView(ZLWorldTask.MOVE_RIGHT);}
	else if (code==KeyEvent.VK_P){application.switchShowProgress();}
    }

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){}

}
