/*   FILE: RadarEvtHdlr.java
 *   DATE OF CREATION:  Wed Nov 24 09:41:02 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zgrviewer;

import java.awt.event.KeyEvent;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
//import fr.inria.zvtm.engine.AnimManager;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.animation.Animation;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class RadarEvtHdlr implements ViewEventHandler {

    GraphicsManager grMngr;

    private boolean draggingRegionRect=false;

    RadarEvtHdlr(GraphicsManager gm){
	this.grMngr = gm;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	    v.getVCursor().stickGlyph(grMngr.observedRegion);  //necessarily observedRegion glyph (there is no other glyph)
	    grMngr.vsm.activeView.mouse.setSensitivity(false);
	    draggingRegionRect=true;
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (draggingRegionRect){
	    grMngr.vsm.activeView.mouse.setSensitivity(true);
	    v.getVCursor().unstickLastGlyph();
	    draggingRegionRect=false;
	}
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
		LongPoint lp = v.getVCursor().getLocation();
		Camera c = grMngr.mSpace.getCamera(0);
//		grMngr.vsm.animator.createCameraAnimation(, AnimManager.CA_TRANS_SIG, new LongPoint(lp.x-c.posx, lp.y-c.posy), c.getID());
		Animation a = grMngr.vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(ConfigManager.ANIM_MOVE_LENGTH, c,
            new LongPoint(lp.x-c.posx, lp.y-c.posy), true, SlowInSlowOutInterpolator.getInstance(), null);
        grMngr.vsm.getAnimationManager().startAnimation(a, false);
	}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	grMngr.rView.getGlobalView(grMngr.mSpace.getCamera(1),500);
	grMngr.cameraMoved(null, null, 0);
    }
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		v.getVCursor().stickGlyph(grMngr.observedRegion);  //necessarily observedRegion glyph (there is no other glyph)
		grMngr.vsm.activeView.mouse.setSensitivity(false);
		draggingRegionRect=true;
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (draggingRegionRect){
	    grMngr.vsm.activeView.mouse.setSensitivity(true);
	    v.getVCursor().unstickLastGlyph();
	    draggingRegionRect=false;
	}
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (draggingRegionRect){
	    grMngr.updateMainViewFromRadar();
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	Camera c=grMngr.mSpace.getCamera(0);
	float a=(c.focal+Math.abs(c.altitude))/c.focal;
	if (wheelDirection == WHEEL_UP){
	    c.altitudeOffset(a*10);
	    grMngr.cameraMoved(null, null, 0);
	}
	else {//wheelDirection == WHEEL_DOWN
	    c.altitudeOffset(-a*10);
	    grMngr.cameraMoved(null, null, 0);
	}
    }

    public void enterGlyph(Glyph g){}

    public void exitGlyph(Glyph g){}

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code == KeyEvent.VK_PAGE_UP){grMngr.getHigherView();}
	else if (code == KeyEvent.VK_PAGE_DOWN){grMngr.getLowerView();}
	else if (code == KeyEvent.VK_HOME){grMngr.getGlobalView();}
	else if (code == KeyEvent.VK_UP){grMngr.translateView(GraphicsManager.MOVE_UP);}
	else if (code == KeyEvent.VK_DOWN){grMngr.translateView(GraphicsManager.MOVE_DOWN);}
	else if (code == KeyEvent.VK_LEFT){grMngr.translateView(GraphicsManager.MOVE_LEFT);}
	else if (code == KeyEvent.VK_RIGHT){grMngr.translateView(GraphicsManager.MOVE_RIGHT);}
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

	public void viewClosing(View v){
		grMngr.rView.destroyView();
		grMngr.rView=null;
	}

}
