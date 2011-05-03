/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: MainEventHandler.java 2769 2010-01-15 10:17:58Z epietrig $
 */

package ZVTMViewer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Vector;

import kernel.Main;

import Compositors.BasicAdapter;
import Compositors.FrameManager;
import Protocol.InputForwarder;
import Protocol.rfbAgent;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.event.PortalListener;

class MainEventHandler implements ViewListener, ComponentListener, PortalListener , KeyListener{

	static boolean MOVE_MODE = false;
	static float ZOOM_SPEED_COEF = 1.0f/50.0f;
	static double PAN_SPEED_COEF = 50.0;
	static final float WHEEL_ZOOMIN_COEF = 21.0f;
	static final float WHEEL_ZOOMOUT_COEF = 22.0f;
	static float WHEEL_MM_STEP = 1.0f;

	//remember last mouse coords
	int lastJPX,lastJPY;
	long lastVX, lastVY;

	Viewer application;

	boolean pcameraStickedToMouse = false;
	boolean regionStickedToMouse = false;
	boolean inPortal = false;

	boolean panning = false;

	// region selection
	boolean selectingRegion = false;
	double x1, y1, x2, y2;

	boolean cursorNearBorder = false;

	Glyph sticked = null;
	private int move_mode_key = KeyEvent.VK_WINDOWS;
	
	//scaling
	private boolean scaling = false;
	private double lastScaleFactor;
	private MetisseWindow currentScaledWindow;

	MainEventHandler(Viewer app){
		this.application = app;
	}

	public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

		if(MOVE_MODE){

			lastJPX = jpx;
			lastJPY = jpy;
			if (inPortal){
				if (application.nm.ovPortal.coordInsideObservedRegion(jpx, jpy)){
					regionStickedToMouse = true;
				}
				else {
					pcameraStickedToMouse = true;
				}
			}
			else if (mod == ALT_MOD){
				selectingRegion = true;
				x1 = v.getVCursor().getVSXCoordinate();
				y1 = v.getVCursor().getVSYCoordinate();
				v.setDrawRect(true);
			}
		}
		else{
			Main.conn.infw.press(mod,v,jpx,jpy,e);
		}
	}

	public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

		if(MOVE_MODE){
			regionStickedToMouse = false;
			pcameraStickedToMouse = false;
			panning = false;
			if (selectingRegion){
				v.setDrawRect(false);
				x2 = v.getVCursor().getVSXCoordinate();
				y2 = v.getVCursor().getVSYCoordinate();
				if ((Math.abs(x2-x1)>=4) && (Math.abs(y2-y1)>=4)){
					application.nm.mCamera.getOwningView().centerOnRegion(application.nm.mCamera, Config.ANIM_MOVE_LENGTH,
							x1, y1, x2, y2);
				}
				selectingRegion = false;
			}
		}
		else{
			Main.conn.infw.release(mod,v,jpx,jpy,e);
		}
	}

	public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	//	Main.conn.infw.click(mod,jpx,jpy,clickNumber,  e);
	}

	public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		if(MOVE_MODE){
			lastJPX = jpx;
			lastJPY = jpy;
			scaling = true;
			currentScaledWindow = ((BasicAdapter)Main.renderer).winman.windows.get(InputForwarder.detectWindow(v, jpx, jpy));
			currentScaledWindow.beingRescaled = true;
			lastScaleFactor = currentScaledWindow.scaleFactor;
		}
		else{
			Main.conn.infw.press(mod,v,jpx,jpy,e);
		}
	}

	public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

		if(MOVE_MODE){
			scaling = false;
			lastScaleFactor = currentScaledWindow.scaleFactor;
			currentScaledWindow.endRescale();
		}
		else{
			Main.conn.infw.release(mod,v,jpx,jpy,e);
		}
	}

	public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
		Main.conn.infw.click(mod,jpx,jpy,clickNumber,  e);
	}

	public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		if(MOVE_MODE){
			lastJPX = jpx;
			lastJPY = jpy;
			panning = true;
			v.setDrawDrag(true);
		}
		else{
			Main.conn.infw.press(mod,v,jpx,jpy,e);
		}
	}

	public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		if(MOVE_MODE){
			application.nm.mCamera.setXspeed(0);
			application.nm.mCamera.setYspeed(0);
			application.nm.mCamera.setZspeed(0);
			v.setDrawDrag(false);
			panning = false;
		}
		else{
			Main.conn.infw.release(mod,v,jpx,jpy,e);
		}
	}

	public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
		if(MOVE_MODE){
			if (v.lastGlyphEntered() != null){
				application.mView.centerOnGlyph(v.lastGlyphEntered(), v.cams[0], Config.ANIM_MOVE_LENGTH, true, 1.0f);				
			}
		}
		else{
			Main.conn.infw.click(mod,jpx,jpy,clickNumber,  e);
		}
	}

	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
	 	Main.conn.infw.move(v,jpx,jpy,e);
	}

	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
		if(MOVE_MODE){
			if (regionStickedToMouse){
				double a = (application.nm.ovCamera.focal+Math.abs(application.nm.ovCamera.altitude)) / application.nm.ovCamera.focal;
				application.nm.mCamera.move(Math.round(a*(jpx-lastJPX)), Math.round(a*(lastJPY-jpy)));
				lastJPX = jpx;
				lastJPY = jpy;
			}
			else if (pcameraStickedToMouse){
				double a = (application.nm.ovCamera.focal+Math.abs(application.nm.ovCamera.altitude))/application.nm.ovCamera.focal;
				application.nm.ovCamera.move(Math.round(a*(lastJPX-jpx)), Math.round(a*(jpy-lastJPY)));
				application.nm.mCamera.move(Math.round(a*(lastJPX-jpx)), Math.round(a*(jpy-lastJPY)));
				lastJPX = jpx;
				lastJPY = jpy;
			}
			else if (panning){
				Camera c = v.cams[0];
				double a = (c.focal+Math.abs(c.altitude))/c.focal;
				if (mod == META_SHIFT_MOD) {
					application.nm.mCamera.setXspeed(0);
					application.nm.mCamera.setYspeed(0);
					application.nm.mCamera.setZspeed(((lastJPY-jpy)*(ZOOM_SPEED_COEF)));
				}
				else {
					application.nm.mCamera.setXspeed((long)((jpx-lastJPX)*(a/PAN_SPEED_COEF)));
					application.nm.mCamera.setYspeed((long)((lastJPY-jpy)*(a/PAN_SPEED_COEF)));
					application.nm.mCamera.setZspeed(0);
				}		    
			}
			else if(scaling){
				currentScaledWindow.setScaleFactor(lastScaleFactor+(jpx-lastJPX)*1./100);
			}
		}
		else{
	//		Main.viewer.dragging  = true;
			Main.conn.infw.move(v,jpx,jpy,e);
		}
	}

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
		if(MOVE_MODE){
			double a = (application.nm.mCamera.focal+Math.abs(application.nm.mCamera.altitude)) / application.nm.mCamera.focal;
			if (wheelDirection  == WHEEL_UP){
				// zooming in
				application.nm.mCamera.altitudeOffset(a*WHEEL_ZOOMOUT_COEF);
				VirtualSpaceManager.INSTANCE.repaint();
			}
			else {
				//wheelDirection == WHEEL_DOWN, zooming out
				application.nm.mCamera.altitudeOffset(-a*WHEEL_ZOOMIN_COEF);
				VirtualSpaceManager.INSTANCE.repaint();
			}      
		}
	}

	public void enterGlyph(Glyph g){
		g.highlight(true, null);
	}

	public void exitGlyph(Glyph g){
		g.highlight(false, null);
	}

	public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
		if (code==move_mode_key){MOVE_MODE = true;}
		if(MOVE_MODE){
			if (code==KeyEvent.VK_PAGE_UP){application.nm.getHigherView();}
			else if (code==KeyEvent.VK_PAGE_DOWN){application.nm.getLowerView();}
			else if (code==KeyEvent.VK_HOME){application.nm.getGlobalView();}
			else if (code==KeyEvent.VK_UP){application.nm.translateView(Navigation.MOVE_UP);}
			else if (code==KeyEvent.VK_DOWN){application.nm.translateView(Navigation.MOVE_DOWN);}
			else if (code==KeyEvent.VK_LEFT){application.nm.translateView(Navigation.MOVE_LEFT);}
			else if (code==KeyEvent.VK_RIGHT){application.nm.translateView(Navigation.MOVE_RIGHT);}
		}

	}

	public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){
		

	}

	public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
		if (code==move_mode_key){MOVE_MODE = false;}
		if(MOVE_MODE){
		
		}
	
		
	}

	public void viewActivated(View v){}

	public void viewDeactivated(View v){}

	public void viewIconified(View v){}

	public void viewDeiconified(View v){}

	public void viewClosing(View v){
		application.exit();
	}

	/*ComponentListener*/
	public void componentHidden(ComponentEvent e){}
	public void componentMoved(ComponentEvent e){}
	public void componentResized(ComponentEvent e){
		application.updatePanelSize();
	}
	public void componentShown(ComponentEvent e){}    

	/* Overview Portal */
	public void enterPortal(Portal p){
		inPortal = true;
		((OverviewPortal)p).setBorder(Config.OV_INSIDE_BORDER_COLOR);
		VirtualSpaceManager.INSTANCE.repaint();
	}

	public void exitPortal(Portal p){
		inPortal = false;
		((OverviewPortal)p).setBorder(Config.OV_BORDER_COLOR);
		VirtualSpaceManager.INSTANCE.repaint();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(!MOVE_MODE){
			Main.conn.infw.Kpress(e);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(!MOVE_MODE){
			Main.conn.infw.Kreles(e);
		}
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
