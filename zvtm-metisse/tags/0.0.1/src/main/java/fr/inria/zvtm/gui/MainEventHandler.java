/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: MainEventHandler.java 2769 2010-01-15 10:17:58Z epietrig $
 */

package fr.inria.zvtm.gui;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import fr.inria.zvtm.compositor.InputForwarder;
import fr.inria.zvtm.compositor.MetisseWindow;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.kernel.Main;

class MainEventHandler implements ViewListener, ComponentListener, KeyListener{

	 boolean MOVE_MODE = false;
	 float ZOOM_SPEED_COEF = 1.0f/50.0f;
	 double PAN_SPEED_COEF = 50.0;
	 final float WHEEL_ZOOMIN_COEF = 21.0f;
	 final float WHEEL_ZOOMOUT_COEF = 22.0f;
	 float WHEEL_MM_STEP = 1.0f;
	 long doubleclickdelay = 500;
	 long lastClickTime;

	//remember last mouse coords
	private int lastJPX,lastJPY;
	private Viewer application;


	private boolean pcameraStickedToMouse = false;
	private boolean regionStickedToMouse = false;
	boolean panning = false;

	// region selection
	boolean selectingRegion = false;
	double x1, y1, x2, y2;

	boolean cursorNearBorder = false;

	Glyph sticked = null;
	private int move_mode_key = KeyEvent.VK_WINDOWS;
	
	//scaling
	private boolean scaling = false;
	private boolean glyphMoving = false;
	private double lastScaleFactor;
	private MetisseWindow currentScaledWindow;
	private MetisseWindow currentMovedWindow;
	private double last_X;
	private double last_Y;

	MainEventHandler(Viewer app){
		this.application = app;
	}

	public void press1(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		if(!application.hasCursor())return;
		CursorHandler ch = application.getCursorHandler();
		ch.move(jpxx,jpyy,e);

		int jpx = ch.getX();
		int jpy = ch.getY();
		
		
		if(MOVE_MODE){

			lastJPX = jpx;
			lastJPY = jpy;
			if (mod == ALT_MOD){
				selectingRegion = true;
				x1 = application.getCursor().getVSXCoordinate();
				y1 = application.getCursor().getVSYCoordinate();
				v.setDrawRect(true);
			}
			else{
			
				lastJPX = jpx;
				lastJPY = jpy;
				last_X = application.getCursor().getVSXCoordinate();
				last_Y = application.getCursor().getVSYCoordinate();
				glyphMoving = true;
				currentMovedWindow = Main.compositor.get(InputForwarder.detectWindow(v, jpx, jpy));
			}
		}
		else{
			InputForwarder.press(mod,v,jpx,jpy,e);
		}
	}

	public void release1(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		if(!application.hasCursor())return;
		CursorHandler ch = application.getCursorHandler();
		ch.move(jpxx,jpyy, e);

		int jpx = ch.getX();
		int jpy = ch.getY();
		
		
		if(MOVE_MODE){
			regionStickedToMouse = false;
			pcameraStickedToMouse = false;
			panning = false;
			if (selectingRegion){
				v.setDrawRect(false);
				x2 = application.getCursor().getVSXCoordinate();
				y2 = application.getCursor().getVSYCoordinate();
				if ((Math.abs(x2-x1)>=4) && (Math.abs(y2-y1)>=4)){
					application.nm.mCamera.getOwningView().centerOnRegion(application.nm.mCamera, Config.ANIM_MOVE_LENGTH,
							x1, y1, x2, y2);
				}
				selectingRegion = false;
			}
			if(glyphMoving){
				glyphMoving = false;
			}
		}
		else{
			InputForwarder.release(mod,v,jpx,jpy,e);
		}
	}

	public void click1(ViewPanel v,int mod,int jpxx,int jpyy,int clickNumber, MouseEvent e){
		
		if(MOVE_MODE&& System.currentTimeMillis()-lastClickTime<doubleclickdelay){
			if(!application.hasCursor())return;
			CursorHandler ch = application.getCursorHandler();
			ch.move(jpxx,jpyy,e);

			int jpx = ch.getX();
			int jpy = ch.getY();
			MetisseWindow win = Main.compositor.get(InputForwarder.detectWindow(v, jpx, jpy));
			if(win!=null)
			if(win.isOnWall()){
				application.callBack(win);
			}
			else{
				application.teleport(win);
			}		
		}
		
		
		lastClickTime = System.currentTimeMillis();
	}

	public void press2(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		if(!application.hasCursor())return;
		CursorHandler ch = application.getCursorHandler();
		ch.move(jpxx,jpyy, e);

		int jpx = ch.getX();
		int jpy = ch.getY();
		
		if(MOVE_MODE){
			lastJPX = jpx;
			lastJPY = jpy;
			scaling = true;
			currentScaledWindow = Main.compositor.get(InputForwarder.detectWindow(v, jpx, jpy));
			if(currentScaledWindow==null)return;
			currentScaledWindow.isRescaling = true;
			lastScaleFactor = currentScaledWindow.scaleFactor;
		}
		else{
			InputForwarder.press(mod,v,jpx,jpy,e);
		}
	}

	public void release2(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		if(!application.hasCursor())return;
		CursorHandler ch = application.getCursorHandler();
		ch.move(jpxx,jpyy, e);

		int jpx = ch.getX();
		int jpy = ch.getY();
		
		if(MOVE_MODE){
			if(currentScaledWindow == null)return;
			scaling = false;
			lastScaleFactor = currentScaledWindow.scaleFactor;
			currentScaledWindow.endRescale();
		}
		else{
			InputForwarder.release(mod,v,jpx,jpy,e);
		}
	}

	public void click2(ViewPanel v,int mod,int jpxx,int jpyy,int clickNumber, MouseEvent e){
		if(!application.hasCursor())return;
		CursorHandler ch = application.getCursorHandler();
		ch.move(jpxx,jpyy, e);

		int jpx = ch.getX();
		int jpy = ch.getY();
		
		if(MOVE_MODE){
			if(currentScaledWindow!=null)currentScaledWindow.resetTransform();
			else if(currentMovedWindow!=null)currentMovedWindow.resetTransform();
		}
		else
		InputForwarder.click(mod,jpx,jpy,clickNumber,  e);
	}

	public void press3(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		if(!application.hasCursor())return;
		CursorHandler ch = application.getCursorHandler();
		ch.move(jpxx,jpyy, e);

		int jpx = ch.getX();
		int jpy = ch.getY();
		
		
		if(MOVE_MODE){
			lastJPX = jpx;
			lastJPY = jpy;
			panning = true;
		//	v.setDrawDrag(true);
		}
		else{
			InputForwarder.press(mod,v,jpx,jpy,e);
		}
	}

	public void release3(ViewPanel v,int mod,int jpxx,int jpyy, MouseEvent e){
		if(!application.hasCursor())return;
		CursorHandler ch = application.getCursorHandler();
		ch.move(jpxx,jpyy, e);

		int jpx = ch.getX();
		int jpy = ch.getY();
		
		if(MOVE_MODE){
			application.nm.mCamera.setXspeed(0);
			application.nm.mCamera.setYspeed(0);
			application.nm.mCamera.setZspeed(0);
			v.setDrawDrag(false);
			panning = false;
		}
		else{
			InputForwarder.release(mod,v,jpx,jpy,e);
		}
	}

	public void click3(ViewPanel v,int mod,int jpxx,int jpyy,int clickNumber, MouseEvent e){
		if(!application.hasCursor())return;
		CursorHandler ch = application.getCursorHandler();
		ch.move(jpxx,jpyy, e);

		int jpx = ch.getX();
		int jpy = ch.getY();
		
		if(MOVE_MODE){
			if (v.lastGlyphEntered() != null){
				application.mView.centerOnGlyph(v.lastGlyphEntered(), v.cams[0], Config.ANIM_MOVE_LENGTH, true, 1.0f);				
			}
		}
		else{
			InputForwarder.click(mod,jpx,jpy,clickNumber,  e);
		}
	}

	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
		CursorHandler ch = application.getCursorHandler();
		if(ch==null)return;
		ch.move(jpx,jpy, e);
		InputForwarder.move(v,ch.getX(),ch.getY(),e);
	}


	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpxx,int jpyy, MouseEvent e){
		if(!application.hasCursor())return;
		CursorHandler ch = application.getCursorHandler();
		ch.move(jpxx,jpyy, e);

		int jpx = ch.getX();
		int jpy = ch.getY();
		
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
			else if(glyphMoving){
				double x = application.getCursor().getVSXCoordinate();
				double y = application.getCursor().getVSYCoordinate();
				currentMovedWindow.moveGlyphOf(x-last_X,y-last_Y);
				lastJPX = jpx;
				lastJPY = jpy;
				last_X = x;
				last_Y = y;
			}
		}
		else{
			InputForwarder.move(v,jpx,jpy,e);
		}
	}

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpxx,int jpyy, MouseWheelEvent e){		
		
		if(MOVE_MODE){
			double a = ((application.nm.mCamera.focal+Math.abs(application.nm.mCamera.altitude))/ application.nm.mCamera.focal);
			double c = Math.pow(Math.min(1,(application.nm.mCamera.altitude+application.nm.mCamera.focal)/100),1.5);
			if (wheelDirection  == WHEEL_UP){
				// zooming in
				application.nm.mCamera.altitudeOffset(a*(WHEEL_ZOOMOUT_COEF)*Math.pow(c, 1.5));
			}
			else {
				//wheelDirection == WHEEL_DOWN, zooming out
				application.nm.mCamera.altitudeOffset((-a)*(WHEEL_ZOOMIN_COEF)*Math.pow(c, 1.5));
			}     
			application.refresh();
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
			else if (code==KeyEvent.VK_R){application.resetCursorPosition();}
			else if (code==KeyEvent.VK_C){application.changeColor();}
			else if (code==KeyEvent.VK_ALT){application.getCursorHandler().deactivate();};
		}

	}

	public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){
		

	}

	public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
		
		if (code==move_mode_key){
			MOVE_MODE = false;
			}
		if(MOVE_MODE){
		}
		if (code==KeyEvent.VK_ALT){application.getCursorHandler().activate();};
	
		
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

	@Override
	public void keyPressed(KeyEvent e) {
		if(!MOVE_MODE){
			InputForwarder.Kpress(e);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(!MOVE_MODE){
			InputForwarder.Kreles(e);
		}
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
