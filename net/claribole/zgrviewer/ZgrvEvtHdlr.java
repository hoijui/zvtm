/*   FILE: ZgrvEvtHdlr.java
 *   DATE OF CREATION:   Thu Jan 09 15:18:48 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *   $Id: ZgrvEvtHdlr.java,v 1.16 2006/06/15 06:54:24 epietrig Exp $
 */ 

package net.claribole.zgrviewer;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.util.Vector;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.VCursor;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VSegment;
import com.xerox.VTM.glyphs.VImage;
import com.xerox.VTM.svg.Metadata;

import net.claribole.zvtm.engine.ViewEventHandler;
import net.claribole.zvtm.engine.PortalEventHandler;
import net.claribole.zvtm.engine.Portal;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class ZgrvEvtHdlr implements ViewEventHandler, ComponentListener, PortalEventHandler {

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    ZGRViewer application;

    static final int NO_LENS = 0;
    static final int ZOOMIN_LENS = 1;
    static final int ZOOMOUT_LENS = -1;
    int lensType = NO_LENS;
    boolean cursorNearBorder = false;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    long lastVX, lastVY;
    long jpxD, jpyD;
    float tfactor;
    float cfactor=50.0f;
    long x1,y1,x2,y2;                     //remember last mouse coords to display selection rectangle (dragging)

    VSegment navSeg;

    Camera activeCam;

    boolean zoomingInRegion=false;
    boolean manualLeftButtonMove=false;
    boolean manualRightButtonMove=false;

    /*speed-dependant autozoom data*/
    boolean autoZooming = false;
    double dragValue;

    boolean toolPaletteIsActive = false;

    /* DragMag interaction */
    boolean inZoomWindow = false;
    boolean inMagWindow = false;
    boolean draggingMagWindow = false;
    boolean draggingZoomWindow = false;
    boolean draggingZoomWindowContent = false;

    ZgrvEvtHdlr(ZGRViewer app){
	this.application=app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (toolPaletteIsActive){return;}
	else {
	    lastJPX = jpx;
	    lastJPY = jpy;
	    if (inZoomWindow){
		if (application.dmPortal.coordInsideBar(jpx, jpy)){
		    draggingZoomWindow = true;
		}
		else {
		    draggingZoomWindowContent = true;
		}
	    }
	    else if (inMagWindow){
		application.vsm.stickToMouse(application.magWindow);
		draggingMagWindow = true;
	    }
	    else {
		application.rememberLocation(v.cams[0].getLocation());
		if (mod == NO_MODIFIER || mod == SHIFT_MOD || mod == META_MOD || mod == META_SHIFT_MOD){
		    manualLeftButtonMove=true;
		    lastJPX=jpx;
		    lastJPY=jpy;
		    //ZGRViewer.vsm.setActiveCamera(v.cams[0]);
		    v.setDrawDrag(true);
		    ZGRViewer.vsm.activeView.mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
		    activeCam=application.vsm.getActiveCamera();
		}
		else if (mod == ALT_MOD){
		    zoomingInRegion=true;
		    x1=v.getMouse().vx;
		    y1=v.getMouse().vy;
		    v.setDrawRect(true);
		}
	    }
	}
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (toolPaletteIsActive){return;}
	else {
	    draggingZoomWindow = false;
	    draggingZoomWindowContent = false;
	    if (draggingMagWindow){
		draggingMagWindow = false;
		application.vsm.unstickFromMouse();
	    }
	    if (zoomingInRegion){
		v.setDrawRect(false);
		x2=v.getMouse().vx;
		y2=v.getMouse().vy;
		if ((Math.abs(x2-x1)>=4) && (Math.abs(y2-y1)>=4)){
		    ZGRViewer.vsm.centerOnRegion(ZGRViewer.vsm.getActiveCamera(),ConfigManager.ANIM_MOVE_LENGTH,x1,y1,x2,y2);
		}
		zoomingInRegion=false;
	    }
	    else if (manualLeftButtonMove){
		ZGRViewer.vsm.animator.Xspeed=0;
		ZGRViewer.vsm.animator.Yspeed=0;
		ZGRViewer.vsm.animator.Aspeed=0;
		v.setDrawDrag(false);
		ZGRViewer.vsm.activeView.mouse.setSensitivity(true);
		if (autoZooming){unzoom(v);}
		manualLeftButtonMove=false;
	    }
	}
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (toolPaletteIsActive){
	    if (v.lastGlyphEntered() != null){application.tp.selectButton((VImage)v.lastGlyphEntered());}
	}
	else {
	    if (application.tp.isFadingLensNavMode() || application.tp.isProbingLensNavMode()){
		lastJPX = jpx;
		lastJPY = jpy;
		lastVX = v.getMouse().vx;
		lastVY = v.getMouse().vy;
		if (lensType != NO_LENS){
		    application.zoomInPhase2(lastVX, lastVY);
		}
		else {
		    if (cursorNearBorder){// do not activate the lens when cursor is near the border
			return;
		    }
		    application.zoomInPhase1(jpx, jpy);
		}
	    }
	    else if (application.tp.isDragMagNavMode()){
		application.triggerDM(jpx, jpy);
	    }
	    else {
		Glyph g=v.lastGlyphEntered();
		if (g!=null){
		    ZGRViewer.vsm.centerOnGlyph(g, v.cams[0], ConfigManager.ANIM_MOVE_LENGTH, true, ConfigManager.MAG_FACTOR);
		}
	    }
	}
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (toolPaletteIsActive){return;}
	Glyph g=v.lastGlyphEntered();
	if (g!=null){
	    if (g.getOwner()!=null){getAndDisplayURL((Metadata)g.getOwner());}
	}
	else {
	    attemptDisplayEdgeURL(v.getMouse(),v.cams[0]);
	}
    }

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (toolPaletteIsActive){return;}
	else {
	    if (application.tp.isFadingLensNavMode() || application.tp.isProbingLensNavMode()){
		lastJPX = jpx;
		lastJPY = jpy;
	    }
	    else {
		v.parent.setActiveLayer(1);
		application.displayMainPieMenu(true);
	    }
	}
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	if (toolPaletteIsActive){return;}
	else {
	    Glyph g = v.getMouse().lastGlyphEntered;
	    if (g != null && g.getType() == Messages.PM_ENTRY){
		application.pieMenuEvent(g);
	    }
	    if (application.mainPieMenu != null){
		application.displayMainPieMenu(false);
	    }
	    if (application.subPieMenu != null){
		application.displaySubMenu(null, false);
	    }
	    v.parent.setActiveLayer(0);
 	}
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	if (toolPaletteIsActive){return;}
	else {
	    if (application.tp.isFadingLensNavMode() || application.tp.isProbingLensNavMode()){
		lastJPX = jpx;
		lastJPY = jpy;
		lastVX = v.getMouse().vx;
		lastVY = v.getMouse().vy;
		if (lensType != NO_LENS){
		    application.zoomOutPhase2();
		}
		else {
		    if (cursorNearBorder){// do not activate the lens when cursor is near the border
			return;
		    }
		    application.zoomOutPhase1(jpx, jpy, lastVX, lastVY);
		}
	    }
	}
    }

    int lx, ly;
    
    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
	lx = jpx;
	ly = jpy;
	if ((jpx-ZGRViewer.LENS_R1) < 0){
	    lx = ZGRViewer.LENS_R1;
	    cursorNearBorder = true;
	}
	else if ((jpx+ZGRViewer.LENS_R1) > application.panelWidth){
	    lx = application.panelWidth - ZGRViewer.LENS_R1;
	    cursorNearBorder = true;
	}
	else {
	    cursorNearBorder = false;
	}
	if ((jpy-ZGRViewer.LENS_R1) < 0){
	    ly = ZGRViewer.LENS_R1;
	    cursorNearBorder = true;
	}
	else if ((jpy+ZGRViewer.LENS_R1) > application.panelHeight){
	    ly = application.panelHeight - ZGRViewer.LENS_R1;
	    cursorNearBorder = true;
	}
	if (lensType != 0 && application.lens != null){
	    application.moveLens(lx, ly, e.getWhen());
	}
	else {
	    if (application.tp.insidePaletteTriggerZone(jpx, jpy)){
		if (!application.tp.isShowing()){application.tp.show();}
	    }
	    else {
		if (application.tp.isShowing()){application.tp.hide();}
	    }
	}
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (toolPaletteIsActive){return;}
	if (mod != ALT_MOD && buttonNumber == 1){
	    if (draggingZoomWindow){
		application.dmPortal.move(jpx-lastJPX, jpy-lastJPY);
		lastJPX = jpx;
		lastJPY = jpy;
		application.vsm.repaintNow();
	    }
	    else if (draggingZoomWindowContent){
		tfactor = (application.dmCamera.focal+(application.dmCamera.altitude))/application.dmCamera.focal;
		synchronized(application.dmCamera){
		    application.dmCamera.move(Math.round(tfactor*(lastJPX-jpx)),
					      Math.round(tfactor*(jpy-lastJPY)));
		    lastJPX = jpx;
		    lastJPY = jpy;
		    application.updateMagWindow();
		}
	    }
	    else if (draggingMagWindow){
		application.updateZoomWindow();
	    }
	    else {
		tfactor=(activeCam.focal+Math.abs(activeCam.altitude))/activeCam.focal;
		if (mod == SHIFT_MOD || mod == META_SHIFT_MOD){
		    application.vsm.animator.Xspeed=0;
		    application.vsm.animator.Yspeed=0;
		    application.vsm.animator.Aspeed=(activeCam.altitude>0) ? (long)((lastJPY-jpy)*(tfactor/cfactor)) : (long)((lastJPY-jpy)/(tfactor*cfactor));
		    //50 is just a speed factor (too fast otherwise)
		}
		else {
		    jpxD = jpx-lastJPX;
		    jpyD = lastJPY-jpy;
		    application.vsm.animator.Xspeed=(activeCam.altitude>0) ? (long)(jpxD*(tfactor/cfactor)) : (long)(jpxD/(tfactor*cfactor));
		    application.vsm.animator.Yspeed=(activeCam.altitude>0) ? (long)(jpyD*(tfactor/cfactor)) : (long)(jpyD/(tfactor*cfactor));
		    application.vsm.animator.Aspeed=0;
		    if (application.cfgMngr.isSDZoomEnabled()){
			dragValue = Math.sqrt(Math.pow(jpxD, 2) + Math.pow(jpyD, 2));
			if (!autoZooming && dragValue > application.cfgMngr.SD_ZOOM_THRESHOLD){
			    autoZooming = true;
			    application.vsm.animator.createCameraAnimation(300, AnimManager.CA_ALT_LIN, new Float(application.cfgMngr.autoZoomFactor*(v.cams[0].getAltitude()+v.cams[0].getFocal())), v.cams[0].getID());
			}
		    }
		}
	    }
	}
	if (lensType != NO_LENS && application.lens != null){
	    application.moveLens(jpx, jpy, e.getWhen());
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	if (lensType != NO_LENS && application.lens != null){
	    if (wheelDirection  == ViewEventHandler.WHEEL_UP){
		application.magnifyFocus(application.WHEEL_MM_STEP, lensType, application.mainCamera);
	    }
	    else {
		application.magnifyFocus(-application.WHEEL_MM_STEP, lensType, application.mainCamera);
	    }
	}
	else if (inZoomWindow){
	    tfactor = (application.dmCamera.focal+Math.abs(application.dmCamera.altitude))/application.dmCamera.focal;
	    if (wheelDirection  == WHEEL_UP){// zooming in
		application.dmCamera.altitudeOffset(-tfactor*WHEEL_ZOOMIN_FACTOR);
	    }
	    else {// wheelDirection == WHEEL_DOWN, zooming out
		application.dmCamera.altitudeOffset(tfactor*WHEEL_ZOOMOUT_FACTOR);
	    }
	    application.updateMagWindow();
	    application.vsm.repaintNow();
	}
	else {
	    tfactor = (application.mainCamera.focal+Math.abs(application.mainCamera.altitude))/application.mainCamera.focal;
	    if (wheelDirection == WHEEL_UP){// zooming in
		application.mainCamera.altitudeOffset(tfactor*WHEEL_ZOOMIN_FACTOR);
		application.cameraMoved();
	    }
	    else {// wheelDirection == WHEEL_DOWN, zooming out
		application.mainCamera.altitudeOffset(-tfactor*WHEEL_ZOOMOUT_FACTOR);
		application.cameraMoved();
	    }
	}
    }

    public void enterGlyph(Glyph g){
	if (g == application.magWindow){
	    inMagWindow = true;
	    return;
	}
	if (g.mouseInsideFColor != null){g.color = g.mouseInsideFColor;}
	if (g.mouseInsideColor != null){g.borderColor = g.mouseInsideColor;}
	if (application.vsm.getActiveView().getActiveLayer() == 1){
	    VirtualSpace vs = application.vsm.getVirtualSpace(application.menuSpace);
	    vs.onTop(g);
	    int i = Utilities.indexOfGlyph(application.mainPieMenu.getItems(), g);
	    if (i != -1){
		vs.onTop(application.mainPieMenu.getLabels()[i]);
	    }
	    else {
		i = Utilities.indexOfGlyph(application.subPieMenu.getItems(), g);
		if (i != -1){
		    vs.onTop(application.subPieMenu.getLabels()[i]);
		}
	    }
	}
    }

    public void exitGlyph(Glyph g){
	if (g == application.magWindow){
	    inMagWindow = false;
	    return;
	}
	if (g.isSelected()){
	    g.borderColor = (g.selectedColor != null) ? g.selectedColor : g.bColor;
	}
	else {
	    if (g.mouseInsideFColor != null){g.color = g.fColor;}
	    if (g.mouseInsideColor != null){g.borderColor = g.bColor;}
	}
	if (application.vsm.getActiveView().getActiveLayer() == 1){
	    if (application.mainPieMenu != null && g == application.mainPieMenu.getBoundary()){
		Glyph lge = application.vsm.getActiveView().mouse.lastGlyphEntered;
		if (lge != null && lge.getType() == Messages.PM_SUBMN){
		    application.mainPieMenu.setSensitivity(false);
		    application.displaySubMenu(lge, true);
		}
	    }
	    else if (application.subPieMenu != null && g == application.subPieMenu.getBoundary()){
		application.displaySubMenu(null, false);
		application.mainPieMenu.setSensitivity(true);
	    }
	}
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	if(code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
	else if (code==KeyEvent.VK_HOME){application.getGlobalView();}
	else if (code==KeyEvent.VK_UP){application.translateView(ZGRViewer.MOVE_UP);}
	else if (code==KeyEvent.VK_DOWN){application.translateView(ZGRViewer.MOVE_DOWN);}
	else if (code==KeyEvent.VK_LEFT){application.translateView(ZGRViewer.MOVE_LEFT);}
	else if (code==KeyEvent.VK_RIGHT){application.translateView(ZGRViewer.MOVE_RIGHT);}
	else if (code==KeyEvent.VK_L || code==KeyEvent.VK_SPACE){
	    Glyph g=v.lastGlyphEntered();
	    if (g!=null){
		if (g.getOwner()!=null){getAndDisplayURL((Metadata)g.getOwner());}
	    }
	    else {
		attemptDisplayEdgeURL(v.getMouse(),v.cams[0]);
	    }
	}
	else if (code == KeyEvent.VK_R){
	    application.tp.show();
	}
	else if (code == KeyEvent.VK_T){
	    application.tp.hide();
	}
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){application.exit();}

    void attemptDisplayEdgeURL(VCursor mouse,Camera cam){
	Glyph g;
	Vector otherGlyphs=mouse.getIntersectingTexts(cam);
	if (otherGlyphs!=null && otherGlyphs.size()>0){
	    g=(Glyph)otherGlyphs.firstElement();
	    if (g.getOwner()!=null){getAndDisplayURL((Metadata)g.getOwner());}
	}
	else {
	    otherGlyphs=mouse.getIntersectingPaths(cam);
	    if (otherGlyphs!=null && otherGlyphs.size()>0){
		g=(Glyph)otherGlyphs.firstElement();
		if (g.getOwner()!=null){getAndDisplayURL((Metadata)g.getOwner());}
	    }
	}
    }

    void getAndDisplayURL(Metadata md){
	String url=md.getURL();
	if (url!=null && url.length()>0){
	    application.displayURLinBrowser(url);
	}
    }


    /*cancel a speed-dependant autozoom*/
    protected void unzoom(ViewPanel v){
	application.vsm.animator.createCameraAnimation(300, AnimManager.CA_ALT_LIN, new Float(application.cfgMngr.autoUnzoomFactor*(v.cams[0].getAltitude()+v.cams[0].getFocal())), v.cams[0].getID());
	autoZooming = false;
    }

    /*ComponentListener*/
    public void componentHidden(ComponentEvent e){}
    public void componentMoved(ComponentEvent e){}
    public void componentResized(ComponentEvent e){
	application.updatePanelSize();
    }
    public void componentShown(ComponentEvent e){}

    /**cursor enters portal*/
    public void enterPortal(Portal p){
	inZoomWindow = true;
    }

    /**cursor exits portal*/
    public void exitPortal(Portal p){
	inZoomWindow = false;
    }

    void resetDragMagInteraction(){
	inMagWindow = false;
	inZoomWindow = false;
	draggingZoomWindow = false;
	draggingZoomWindowContent = false;
    }

}
