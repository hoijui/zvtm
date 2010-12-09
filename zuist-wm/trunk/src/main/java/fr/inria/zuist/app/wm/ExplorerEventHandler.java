/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.wm;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;

import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;

import fr.inria.zvtm.engine.VCursor;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.event.CameraListener;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.event.PortalListener;

import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.TextDescription;

import org.geonames.Toponym;

class ExplorerEventHandler implements ViewListener, CameraListener, ComponentListener, PortalListener {

    static final float MAIN_SPEED_FACTOR = 50.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;
    
    static float WHEEL_MM_STEP = 1.0f;
    
	// update scene when panned from overview every 0.5s
	static final int DELAYED_UPDATE_FREQUENCY = 500;

    //remember last mouse coords to compute translation  (dragging)
    int lastJPX,lastJPY;
    double lastVX, lastVY;
    
    int currentJPX, currentJPY;

    /* bounds of region in virtual space currently observed through mCamera */
    double[] wnes = new double[4];
    double oldCameraAltitude;

    boolean mCamStickedToMouse = false;
    boolean regionStickedToMouse = false;
    boolean inPortal = false;

    WorldExplorer application;
    NavigationManager nm;
    
    Glyph g;
    
    boolean cursorNearBorder = false;
    boolean panning = false;
    
	DelayedUpdateTimer dut;
	
	// region selection
	boolean selectingRegion = false;
	double x1, y1, x2, y2;

    ExplorerEventHandler(WorldExplorer app){
        this.application = app;
        this.nm = app.nm;
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
		if (inPortal){
		    if (application.nm.ovPortal.coordInsideObservedRegion(jpx, jpy)){
				regionStickedToMouse = true;
		    }
			else {
				double a = (application.ovCamera.focal+Math.abs(application.ovCamera.altitude)) / application.ovCamera.focal;
				application.mCamera.moveTo(a*(jpx-application.nm.ovPortal.x-application.nm.ovPortal.w/2),
				                           -a*(jpy-application.nm.ovPortal.y-application.nm.ovPortal.h/2));
				cameraMoved(application.mCamera, null, 0);
				// position camera where user has pressed, and then allow seamless dragging
				regionStickedToMouse = true;
			}
		}
        else if (mod == SHIFT_MOD){
            lastVX = v.getVCursor().vx;
            lastVY = v.getVCursor().vy;
            if (nm.lensType != NavigationManager.NO_LENS){
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
		else if ((g = v.lastGlyphEntered()) != null){
		    if (g.getType().equals(AirTrafficManager.AIRP)){
		        if (mode == MODE_BRINGANDGO){
        		    application.ga.highlight(g);
        			application.ga.bringFor(g);		            
		        }
		        else if (mode == MODE_HIGHLIGHTING){
        		    application.ga.highlight(g);
        		}
		    }
		}
		else {
		    if (application.isDynaspotEnabled() && v.getVCursor().getDynaSpotRadius() > 0){return;}
		    selectingRegion = true;
			x1 = v.getVCursor().vx;
			y1 = v.getVCursor().vy;
			v.setDrawRect(true);
		}
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        if (application.ga.isBringingAndGoing){
		    application.ga.unhighlight(g);
			application.ga.endBringAndGo(v.lastGlyphEntered());
		}
		if (application.ga.isHighlighting){
		    application.ga.unhighlight(g);
		}
        if (application.isDynaspotEnabled() && !inPortal && !v.getVCursor().isDynaSpotActivated()){v.getVCursor().activateDynaSpot(true);}
		regionStickedToMouse = false;
	    if (selectingRegion){
			v.setDrawRect(false);
			x2 = v.getVCursor().vx;
			y2 = v.getVCursor().vy;
			if ((Math.abs(x2-x1)>=4) && (Math.abs(y2-y1)>=4)){
				application.mCamera.getOwningView().centerOnRegion(application.mCamera, NavigationManager.ANIM_MOVE_DURATION, x1, y1, x2, y2);
			}
			selectingRegion = false;
		}
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        lastVX = v.getVCursor().vx;
    	lastVY = v.getVCursor().vy;
    	if (application.isDynaspotEnabled()){
        	Glyph g = v.getVCursor().dynaPick(application.bCamera);
        	if (g != null){
        	    if (g.getType().equals(GeoToolsManager.CITY)){
            	    application.displayFeatureInfo((g != null) ? (Toponym)g.getOwner() : null, g);
            	}
            	else if (mode == MODE_HIGHLIGHTING && g.getType().equals(AirTrafficManager.AIRP)){
        		    application.ga.highlight(g);
            	}     	    
        	}
    	}
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
        if (application.isDynaspotEnabled() && !v.getVCursor().isDynaSpotActivated()){v.getVCursor().activateDynaSpot(true);}
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        lastVX = v.getVCursor().vx;
        lastVY = v.getVCursor().vy;
        if (mod == META_SHIFT_MOD){
            if (nm.lensType != NavigationManager.NO_LENS){
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
        else {
            Glyph g;
            if ((g=v.lastGlyphEntered()) != null){
                if (g instanceof VPolygon){
                    application.mView.centerOnGlyph(g, application.mCamera, NavigationManager.ANIM_MOVE_DURATION);
                }
            }            
        }
    }
        
    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
//        System.err.println(v.getVCursor().vx+" "+v.getVCursor().vy);
    	if ((jpx-NavigationManager.LENS_R1) < 0){
    	    jpx = NavigationManager.LENS_R1;
    	    cursorNearBorder = true;
    	}
    	else if ((jpx+NavigationManager.LENS_R1) > application.panelWidth){
    	    jpx = application.panelWidth - NavigationManager.LENS_R1;
    	    cursorNearBorder = true;
    	}
    	else {
    	    cursorNearBorder = false;
    	}
    	if ((jpy-NavigationManager.LENS_R1) < 0){
    	    jpy = NavigationManager.LENS_R1;
    	    cursorNearBorder = true;
    	}
    	else if ((jpy+NavigationManager.LENS_R1) > application.panelHeight){
    	    jpy = application.panelHeight - NavigationManager.LENS_R1;
    	    cursorNearBorder = true;
    	}
    	if (nm.lensType != 0 && nm.lens != null){
    	    nm.moveLens(jpx, jpy, e.getWhen());
    	}
    	if (application.isDynaspotEnabled()){
        	v.getVCursor().dynaPick(application.bCamera);    	    
    	}
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (application.ga.isBringingAndGoing){return;}
        if (panning){
            if (application.isDynaspotEnabled() && v.getVCursor().isDynaSpotActivated()){v.getVCursor().activateDynaSpot(false);}
            double a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
            synchronized(application.mCamera){
                application.mCamera.move(a*(lastJPX-jpx), a*(jpy-lastJPY));
                lastJPX = jpx;
                lastJPY = jpy;
				cameraMoved(application.mCamera, null, 0);
            }
            if (nm.lensType != 0 && nm.lens != null){
        	    nm.moveLens(jpx, jpy, e.getWhen());
        	}
        }
		else if (regionStickedToMouse){
			double a = (application.ovCamera.focal+Math.abs(application.ovCamera.altitude)) / application.ovCamera.focal;
			application.mCamera.move(a*(jpx-lastJPX), a*(lastJPY-jpy));
			dut.requestUpdate();
			lastJPX = jpx;
			lastJPY = jpy;
		}
		else if (selectingRegion){
		    if (application.isDynaspotEnabled() && v.getVCursor().isDynaSpotActivated()){v.getVCursor().activateDynaSpot(false);}
		}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        if (nm.lensType != 0 && nm.lens != null){
            if (wheelDirection  == WHEEL_DOWN){
                nm.magnifyFocus(NavigationManager.WHEEL_MM_STEP, nm.lensType, application.mCamera);
            }
            else {
                nm.magnifyFocus(-NavigationManager.WHEEL_MM_STEP, nm.lensType, application.mCamera);
            }
        }
        else {
            double a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
            if (wheelDirection  == WHEEL_UP){
                // zooming in
                application.mCamera.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
                dut.requestUpdate();
                application.vsm.repaint();
            }
            else {
                //wheelDirection == WHEEL_DOWN, zooming out
                application.mCamera.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
                dut.requestUpdate();
                application.vsm.repaint();
            }
    	}
    }

    public void enterGlyph(Glyph g){
		if (application.ga.isBringingAndGoing && g.getType().equals(AirTrafficManager.AIRP)){
			application.ga.attemptToBring(g);
		}
        //else if (g instanceof VPolygon){
        //    ((VPolygon)g).setFilled(true);
        //    g.setTranslucencyValue(.1f);
        //}

    }

    public void exitGlyph(Glyph g){
        //if (g instanceof VPolygon){
        //    ((VPolygon)g).setFilled(false);
        //    g.setTranslucencyValue(1f);
        //}
    }

    int ci = 1180;

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code==KeyEvent.VK_PAGE_UP){application.nm.getHigherView();}
    	else if (code==KeyEvent.VK_PAGE_DOWN){application.nm.getLowerView();}
    	else if (code==KeyEvent.VK_HOME){application.nm.getGlobalView(null);}
    	else if (code==KeyEvent.VK_UP){application.nm.translateView(NavigationManager.MOVE_UP);}
    	else if (code==KeyEvent.VK_DOWN){application.nm.translateView(NavigationManager.MOVE_DOWN);}
    	else if (code==KeyEvent.VK_LEFT){application.nm.translateView(NavigationManager.MOVE_LEFT);}
    	else if (code==KeyEvent.VK_RIGHT){application.nm.translateView(NavigationManager.MOVE_RIGHT);}
        else if (code == KeyEvent.VK_F1){application.toggleMemoryUsageDisplay();}
        else if (code == KeyEvent.VK_F2){application.gc();}
        else if (code == KeyEvent.VK_B){application.gm.toggleCountryDisplay();}
        else if (code == KeyEvent.VK_L){application.nm.toggleLensType();}
        else if (code == KeyEvent.VK_U){application.toggleUpdateMaps();}
        else if (code == KeyEvent.VK_A){application.ga.toggleTraffic();}
        else if (code == KeyEvent.VK_N){toggleTopoNav();}
        else if (code == KeyEvent.VK_W){application.ga.setTranslucencyByWeight();}        
        else if (code == KeyEvent.VK_T){new Preferences(application);}        
    }
    
    static final short MODE_HIGHLIGHTING = 0;
	static final short MODE_BRINGANDGO = 1;
	//static final short MODE_LINKSLIDER = 2;
	short mode = MODE_BRINGANDGO;
	
	void toggleTopoNav(){
		switch(mode){
			case MODE_HIGHLIGHTING:{mode = MODE_BRINGANDGO;break;}
			case MODE_BRINGANDGO:{mode = MODE_HIGHLIGHTING;break;}
			//case MODE_BRINGANDGO:{mode = MODE_LINKSLIDER;break;}
			//case MODE_LINKSLIDER:{mode = MODE_HIGHLIGHTING;break;}
		}
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

    public void cameraMoved(Camera cam, Point2D.Double coord, double a){
        application.bCamera.setAltitude(cam.getAltitude());
        application.bCamera.moveTo(cam.vx, cam.vy);
        dut.requestUpdate();
    }
    
    void cameraMoved(){
        // region seen through camera
        application.mView.getVisibleRegion(application.mCamera, wnes);
        double alt = application.mCamera.getAltitude();
        if (alt != oldCameraAltitude){
            oldCameraAltitude = alt;
        }
        else {
            // camera movement was a simple translation
			//dut.cancelUpdate();
        }        
    }

	/* Overview Portal */
	public void enterPortal(Portal p){
	    if (application.isDynaspotEnabled()){
    	    application.mView.getCursor().activateDynaSpot(false);	        
	    }
		inPortal = true;
		((OverviewPortal)p).setBorder(NavigationManager.OV_INSIDE_BORDER_COLOR);
		application.vsm.repaint();
	}

	public void exitPortal(Portal p){
	    if (application.isDynaspotEnabled()){
    	    application.mView.getCursor().activateDynaSpot(true);	        
	    }
		inPortal = false;
		((OverviewPortal)p).setBorder(NavigationManager.OV_BORDER_COLOR);
		application.vsm.repaint();
	}
	
}

class DelayedUpdateTimer extends TimerTask {

    private boolean enabled = true;
	private boolean update = false;
	
	ExplorerEventHandler eh;

	DelayedUpdateTimer(ExplorerEventHandler eh){
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
