/*   FILE: RadarEvtHdlr.java
 *   DATE OF CREATION:  Wed Nov 24 09:41:02 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: RadarEvtHdlr.java,v 1.5 2006/05/28 16:19:48 epietrig Exp $
 */ 

package net.claribole.zgrviewer;

import java.awt.event.KeyEvent;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.glyphs.Glyph;

import net.claribole.zvtm.engine.ViewEventHandler;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class RadarEvtHdlr implements ViewEventHandler {

    ZGRViewer application;

    private boolean draggingRegionRect=false;

    RadarEvtHdlr(ZGRViewer app){
	this.application=app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	    ZGRViewer.vsm.stickToMouse(application.observedRegion);  //necessarily observedRegion glyph (there is no other glyph)
	    ZGRViewer.vsm.activeView.mouse.setSensitivity(false);
	    draggingRegionRect=true;
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (draggingRegionRect){
	    ZGRViewer.vsm.activeView.mouse.setSensitivity(true);
	    ZGRViewer.vsm.unstickFromMouse();
	    draggingRegionRect=false;
	}
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	ZGRViewer.vsm.getGlobalView(ZGRViewer.vsm.getVirtualSpace(ZGRViewer.mainSpace).getCamera(1),500);
	application.cameraMoved();
    }
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	ZGRViewer.vsm.stickToMouse(application.observedRegion);  //necessarily observedRegion glyph (there is no other glyph)
	ZGRViewer.vsm.activeView.mouse.setSensitivity(false);
	draggingRegionRect=true;
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (draggingRegionRect){
	    ZGRViewer.vsm.activeView.mouse.setSensitivity(true);
	    ZGRViewer.vsm.unstickFromMouse();
	    draggingRegionRect=false;
	}
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (draggingRegionRect){
	    application.updateMainViewFromRadar();
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	Camera c=application.mSpace.getCamera(0);
	float a=(c.focal+Math.abs(c.altitude))/c.focal;
	if (wheelDirection == WHEEL_UP){
	    c.altitudeOffset(a*10);
	    application.cameraMoved();
	}
	else {//wheelDirection == WHEEL_DOWN
	    c.altitudeOffset(-a*10);
	    application.cameraMoved();
	}
    }

    public void enterGlyph(Glyph g){}

    public void exitGlyph(Glyph g){}

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code == KeyEvent.VK_PAGE_UP){application.getHigherView();}
	else if (code == KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
	else if (code == KeyEvent.VK_HOME){application.getGlobalView();}
	else if (code == KeyEvent.VK_UP){application.translateView(ZGRViewer.MOVE_UP);}
	else if (code == KeyEvent.VK_DOWN){application.translateView(ZGRViewer.MOVE_DOWN);}
	else if (code == KeyEvent.VK_LEFT){application.translateView(ZGRViewer.MOVE_LEFT);}
	else if (code == KeyEvent.VK_RIGHT){application.translateView(ZGRViewer.MOVE_RIGHT);}
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){
	ZGRViewer.vsm.getView(ZGRViewer.radarView).destroyView();
	ZGRViewer.rView=null;
    }

}
