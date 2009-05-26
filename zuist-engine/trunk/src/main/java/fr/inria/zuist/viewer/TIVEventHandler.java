/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: TIVExplorerEventHandler.java 1676 2009-04-10 14:56:31Z epietrig $
 */

package fr.inria.zuist.viewer;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VCursor;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VText;
import net.claribole.zvtm.engine.ViewEventHandler;
import net.claribole.zvtm.engine.CameraListener;
import net.claribole.zvtm.engine.Portal;
import net.claribole.zvtm.engine.OverviewPortal;
import net.claribole.zvtm.engine.PortalEventHandler;

import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.TextDescription;

class TIVExplorerEventHandler implements ViewEventHandler, CameraListener, ComponentListener, PortalEventHandler {

    static final float MAIN_SPEED_FACTOR = 50.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;
    
    static float WHEEL_MM_STEP = 1.0f;
    
	// update scene when panned from overview every 0.5s
	static final int DELAYED_UPDATE_FREQUENCY = 500;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    long lastVX, lastVY;
    int currentJPX, currentJPY;

    /* bounds of region in virtual space currently observed through mCamera */
    long[] wnes = new long[4];
    float oldCameraAltitude;

    boolean mCamStickedToMouse = false;
    boolean regionStickedToMouse = false;
    boolean inPortal = false;

    TiledImageViewer application;
    TIVNavigationManager nm;
    
    Glyph g;
    
    boolean cursorNearBorder = false;
    boolean panning = false;
    
	DelayedUpdateTimer dut;
	
	// region selection
	boolean selectingRegion = false;
	long x1, y1, x2, y2;

    TIVExplorerEventHandler(TiledImageViewer app){
        this.application = app;
        oldCameraAltitude = this.application.mCamera.getAltitude();
		initDelayedUpdateTimer();
    }

	void initDelayedUpdateTimer(){
		Timer timer = new Timer();
		dut = new DelayedUpdateTimer(this);
		timer.scheduleAtFixedRate(dut, DELAYED_UPDATE_FREQUENCY, DELAYED_UPDATE_FREQUENCY);
	}

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        lastVX = v.getVCursor().vx;
    	lastVY = v.getVCursor().vy;
		if (inPortal){
		    if (application.nm.ovPortal.coordInsideObservedRegion(jpx, jpy)){
				regionStickedToMouse = true;
		    }
			else {
				double a = (application.ovCamera.focal+Math.abs(application.ovCamera.altitude)) / application.ovCamera.focal;
				application.mCamera.moveTo(Math.round(a*(jpx-application.nm.ovPortal.x-application.nm.ovPortal.w/2)),
				                           Math.round(-a*(jpy-application.nm.ovPortal.y-application.nm.ovPortal.h/2)));
				cameraMoved(null, null, 0);
				// position camera where user has pressed, and then allow seamless dragging
				regionStickedToMouse = true;
			}
		}
        else if (mod == ALT_MOD){
            selectingRegion = true;
            x1 = v.getVCursor().vx;
            y1 = v.getVCursor().vy;
            v.setDrawRect(true);
        }
        else {
            if (nm.lensType != TIVNavigationManager.NO_LENS){
                nm.zoomInPhase2(lastVX, lastVY);
            }
            else {
                if (cursorNearBorder){
                    // do not activate the lens when cursor is near the border
                    return;
                }
                nm.zoomInPhase1(jpx, jpy);
            }
        }
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		regionStickedToMouse = false;
	    if (selectingRegion){
			v.setDrawRect(false);
			x2 = v.getVCursor().vx;
			y2 = v.getVCursor().vy;
			if ((Math.abs(x2-x1)>=4) && (Math.abs(y2-y1)>=4)){
				VirtualSpaceManager.INSTANCE.centerOnRegion(application.mCamera, TIVNavigationManager.ANIM_MOVE_DURATION, x1, y1, x2, y2);
			}
			selectingRegion = false;
		}
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        lastVX = v.getVCursor().vx;
    	lastVY = v.getVCursor().vy;
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        if (!inPortal){
            panning = true;
        }
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        panning = false;
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        lastVX = v.getVCursor().vx;
        lastVY = v.getVCursor().vy;
        if (nm.lensType != TIVNavigationManager.NO_LENS){
            nm.zoomOutPhase2();
        }
        else {
            if (cursorNearBorder){
                // do not activate the lens when cursor is near the border
                return;
            }
            nm.zoomOutPhase1(jpx, jpy, lastVX, lastVY);
        }
    }
        
    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
//        System.err.println(v.getVCursor().vx+" "+v.getVCursor().vy);
    	if ((jpx-TIVNavigationManager.LENS_R1) < 0){
    	    jpx = TIVNavigationManager.LENS_R1;
    	    cursorNearBorder = true;
    	}
    	else if ((jpx+TIVNavigationManager.LENS_R1) > application.panelWidth){
    	    jpx = application.panelWidth - TIVNavigationManager.LENS_R1;
    	    cursorNearBorder = true;
    	}
    	else {
    	    cursorNearBorder = false;
    	}
    	if ((jpy-TIVNavigationManager.LENS_R1) < 0){
    	    jpy = TIVNavigationManager.LENS_R1;
    	    cursorNearBorder = true;
    	}
    	else if ((jpy+TIVNavigationManager.LENS_R1) > application.panelHeight){
    	    jpy = application.panelHeight - TIVNavigationManager.LENS_R1;
    	    cursorNearBorder = true;
    	}
    	if (nm.lensType != 0 && nm.lens != null){
    	    nm.moveLens(jpx, jpy, e.getWhen());
    	}
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (panning){
            float a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
            synchronized(application.mCamera){
                application.mCamera.move(Math.round(a*(lastJPX-jpx)), Math.round(a*(jpy-lastJPY)));
                lastJPX = jpx;
                lastJPY = jpy;
                cameraMoved(null, null, 0);
            }
            if (nm.lensType != 0 && nm.lens != null){
        	    nm.moveLens(jpx, jpy, e.getWhen());
        	}
        }
		else if (regionStickedToMouse){
			float a = (application.ovCamera.focal+Math.abs(application.ovCamera.altitude)) / application.ovCamera.focal;
			application.mCamera.move(Math.round(a*(jpx-lastJPX)), Math.round(a*(lastJPY-jpy)));
			dut.requestUpdate();
			lastJPX = jpx;
			lastJPY = jpy;
		}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        if (nm.lensType != 0 && nm.lens != null){
            if (wheelDirection  == ViewEventHandler.WHEEL_UP){
                nm.magnifyFocus(TIVNavigationManager.WHEEL_MM_STEP, nm.lensType, application.mCamera);
            }
            else {
                nm.magnifyFocus(-TIVNavigationManager.WHEEL_MM_STEP, nm.lensType, application.mCamera);
            }
        }
        else {
            float a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
            if (wheelDirection  == WHEEL_UP){
                // zooming in
                application.mCamera.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
                dut.requestUpdate();
                VirtualSpaceManager.INSTANCE.repaintNow();
            }
            else {
                //wheelDirection == WHEEL_DOWN, zooming out
                application.mCamera.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
                dut.requestUpdate();
                VirtualSpaceManager.INSTANCE.repaintNow();
            }
    	}
    }

    public void enterGlyph(Glyph g){
//        g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
//        g.highlight(false, null);
    }

    int ci = 1180;

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code==KeyEvent.VK_PAGE_UP){application.nm.getHigherView();}
    	else if (code==KeyEvent.VK_PAGE_DOWN){application.nm.getLowerView();}
    	else if (code==KeyEvent.VK_HOME){application.nm.getGlobalView();}
    	else if (code==KeyEvent.VK_UP){application.nm.translateView(TIVNavigationManager.MOVE_UP);}
    	else if (code==KeyEvent.VK_DOWN){application.nm.translateView(TIVNavigationManager.MOVE_DOWN);}
    	else if (code==KeyEvent.VK_LEFT){application.nm.translateView(TIVNavigationManager.MOVE_LEFT);}
    	else if (code==KeyEvent.VK_RIGHT){application.nm.translateView(TIVNavigationManager.MOVE_RIGHT);}
        else if (code == KeyEvent.VK_F2){application.gc();}
        else if (code == KeyEvent.VK_L){application.nm.toggleLensType();}
        else if (code == KeyEvent.VK_U){application.toggleUpdateTiles();}
        else if (code == KeyEvent.VK_O){application.nm.updateOverview();}
        else if (c == '?'){application.ovm.showAbout();}
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

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

    public void cameraMoved(Camera cam, LongPoint coord, float a){
        dut.requestUpdate();
    }
    
    void cameraMoved(){
        // region seen through camera
        application.mView.getVisibleRegion(application.mCamera, wnes);
        float alt = application.mCamera.getAltitude();
        if (alt != oldCameraAltitude){
            // camera was an altitude change
            application.nm.altitudeChanged();
            oldCameraAltitude = alt;
        }
        else {
            // camera movement was a simple translation
			//dut.cancelUpdate();
            application.sm.updateVisibleRegions();
        }        
    }

	/* Overview Portal */
	public void enterPortal(Portal p){
		inPortal = true;
		((OverviewPortal)p).setBorder(TIVNavigationManager.OV_INSIDE_BORDER_COLOR);
		VirtualSpaceManager.INSTANCE.repaintNow();
	}

	public void exitPortal(Portal p){
		inPortal = false;
		((OverviewPortal)p).setBorder(TIVNavigationManager.OV_BORDER_COLOR);
		VirtualSpaceManager.INSTANCE.repaintNow();
	}
	
}

class DelayedUpdateTimer extends TimerTask {

    private boolean enabled = true;
	private boolean update = false;
	
	TIVExplorerEventHandler eh;

	DelayedUpdateTimer(TIVExplorerEventHandler eh){
		super();
		this.eh = eh;
	}

	public void setEnabled(boolean b){
		enabled = b;
	}

	public boolean isEnabled(){
		return enabled;
	}

	public void run(){		
		if (enabled && update){
			eh.cameraMoved();
			update = false;
		}
	}
	
	void requestUpdate(){
		update = true;
	}
	
	void cancelUpdate(){
		update = false;
	}

}
