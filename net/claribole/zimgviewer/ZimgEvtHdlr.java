/*   FILE: ZimgEvtHdlr.java
 *   DATE OF CREATION:   Thu May 29 16:12:27 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.zimgviewer;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VSegment;

import net.claribole.zvtm.engine.ViewEventHandler;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class ZimgEvtHdlr implements ViewEventHandler {

    ZImgViewer application;

    long lastX,lastY,lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    float tfactor;
    float cfactor=50.0f;
    long x1,y1,x2,y2;                     //remember last mouse coords to display selection rectangle (dragging)

    VSegment navSeg;

    Camera activeCam;

    boolean zoomingInRegion=false;
    boolean manualLeftButtonMove=false;
    boolean manualRightButtonMove=false;

    ZimgEvtHdlr(ZImgViewer app){
	this.application=app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	Glyph g=v.lastGlyphEntered();
	if (g!=null){
	    ZImgViewer.vsm.getVirtualSpace(ZImgViewer.mainSpace).onTop(g);
	    ZImgViewer.vsm.stickToMouse(v.lastGlyphEntered());
	}
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	ZImgViewer.vsm.unstickFromMouse();
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){

    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (v.lastGlyphEntered()!=null){
	    ZImgViewer.vsm.stickToMouse(v.lastGlyphEntered());
	}
    }
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){ZImgViewer.vsm.unstickFromMouse();}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	//application.vsm.setSync(false);
	lastJPX=jpx;
	lastJPY=jpy;
	//application.vsm.animator.setActiveCam(v.cams[0]);
	v.setDrawDrag(true);
	application.vsm.activeView.mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.animator.Xspeed=0;
	application.vsm.animator.Yspeed=0;
	application.vsm.animator.Aspeed=0;
	v.setDrawDrag(false);
	application.vsm.activeView.mouse.setSensitivity(true);
	/*Camera c=v.cams[0];
	  application.vsm.animator.createCameraAnimation(500,2,new LongPoint(lastX-application.vsm.mouse.vx,lastY-application.vsm.mouse.vy),c.getID());*/
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}
    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
	    Camera c=application.vsm.getActiveCamera();
	    float a=(c.focal+Math.abs(c.altitude))/c.focal;
	    if (mod == META_SHIFT_MOD) {
		application.vsm.animator.Xspeed=0;
		application.vsm.animator.Yspeed=0;
 		application.vsm.animator.Aspeed=(c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50));  //50 is just a speed factor (too fast otherwise)
	    }
	    else {
		application.vsm.animator.Xspeed=(c.altitude>0) ? (long)((jpx-lastJPX)*(a/50.0f)) : (long)((jpx-lastJPX)/(a*50));
		application.vsm.animator.Yspeed=(c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50));
		application.vsm.animator.Aspeed=0;
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){}

    public void enterGlyph(Glyph g){
	if (g.mouseInsideFColor != null){g.color = g.mouseInsideFColor;}
	if (g.mouseInsideColor != null){g.borderColor = g.mouseInsideColor;}
	if (g.getOwner()!=null){
	    ImageInfo ii=(ImageInfo)g.getOwner();
	    ZImgViewer.mainView.setStatusBarText(ii.getFileName());
	}
    }

    public void exitGlyph(Glyph g){
	if (g.isSelected()){
	    g.borderColor = (g.selectedColor != null) ? g.selectedColor : g.bColor;
	}
	else {
	    if (g.mouseInsideFColor != null){g.color = g.fColor;}
	    if (g.mouseInsideColor != null){g.borderColor = g.bColor;}
	}
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
	else if (code==KeyEvent.VK_HOME){application.getGlobalView();}
	else if (code==KeyEvent.VK_UP){application.translateView(ZImgViewer.MOVE_UP);}
	else if (code==KeyEvent.VK_DOWN){application.translateView(ZImgViewer.MOVE_DOWN);}
	else if (code==KeyEvent.VK_LEFT){application.translateView(ZImgViewer.MOVE_LEFT);}
	else if (code==KeyEvent.VK_RIGHT){application.translateView(ZImgViewer.MOVE_RIGHT);}
	else if (code==KeyEvent.VK_R){
	    Glyph g=v.lastGlyphEntered();
	    if (g!=null){
		application.rotateGlyph(g, new Float(0.2f));
	    }
	}
	else if (code==KeyEvent.VK_T){
	    Glyph g=v.lastGlyphEntered();
	    if (g!=null){
		application.rotateGlyph(g, new Float(-0.2f));
	    }
	}
	else if (code==KeyEvent.VK_S){
	    Glyph g=v.lastGlyphEntered();
	    if (g!=null){
		application.resizeGlyph(g, new Float(0.8333f));
	    }
	}
	else if (code==KeyEvent.VK_D){
	    Glyph g=v.lastGlyphEntered();
	    if (g!=null){
		application.resizeGlyph(g, new Float(1.2f));
	    }
	}
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){application.exit();}

}
