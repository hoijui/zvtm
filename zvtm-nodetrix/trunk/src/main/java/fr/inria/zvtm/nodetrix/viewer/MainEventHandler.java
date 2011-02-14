/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix.viewer;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.event.PortalListener;

import fr.inria.zvtm.nodetrix.Matrix;
import fr.inria.zvtm.nodetrix.NTNode;

class MainEventHandler implements ViewListener, ComponentListener, PortalListener {

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;
    static final float WHEEL_ZOOMIN_COEF = 21.0f;
    static final float WHEEL_ZOOMOUT_COEF = 22.0f;
    static float WHEEL_MM_STEP = 1.0f;
    
    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    double x1, y1, x2, y2;
    Viewer application;
    
    boolean pcameraStickedToMouse = false;
    boolean regionStickedToMouse = false;
    boolean inPortal = false;
    
    boolean panning = false;
    boolean selectingRegion = false;
    // draggedMatrix being non-null means that we are draggina a matrix
    Matrix draggedMatrix = null;
    
    // temp glyph, never assume it has been set correctly by another callback
    // for intra method use only
    Glyph g;
    
    MainEventHandler(Viewer app){
        this.application = app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
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
                x1 = v.getVCursor().vx;
                y1 = v.getVCursor().vy;
                v.setDrawRect(true);
		}
		else if ((g=v.lastGlyphEntered()) != null){
            if (g.getOwner() instanceof Matrix){
                draggedMatrix = (Matrix)g.getOwner();
            }
            else if (g.getOwner() instanceof NTNode){
                NTNode n = (NTNode)g.getOwner();
                if (n.isParentMatrixSingle()){
                    draggedMatrix = n.getMatrix();
                }
            }
		}
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	    regionStickedToMouse = false;
	    pcameraStickedToMouse = false;
	    draggedMatrix = null;
	    if (selectingRegion){
			v.setDrawRect(false);
			x2 = v.getVCursor().vx;
			y2 = v.getVCursor().vy;
			if ((Math.abs(x2-x1)>=4) && (Math.abs(y2-y1)>=4)){
				application.nm.mCamera.getOwningView().centerOnRegion(application.nm.mCamera, ConfigManager.ANIM_MOVE_LENGTH,
				                                                      x1, y1, x2, y2);
			}
			selectingRegion = false;
		}
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        panning = true;
        v.setDrawDrag(true);
    }

	public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	    application.nm.mCamera.setXspeed(0);
        application.nm.mCamera.setYspeed(0);
        application.nm.mCamera.setZspeed(0);
        v.setDrawDrag(false);
        panning = false;
	}

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        if (v.lastGlyphEntered() != null){
    		application.mView.centerOnGlyph(v.lastGlyphEntered(), v.cams[0], ConfigManager.ANIM_MOVE_LENGTH, true, 1.0f);				
		}
    }
        
    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (regionStickedToMouse){
			double a = (application.nm.ovCamera.focal+Math.abs(application.nm.ovCamera.altitude)) / application.nm.ovCamera.focal;
			application.nm.mCamera.move(a*(jpx-lastJPX), a*(lastJPY-jpy));
			lastJPX = jpx;
			lastJPY = jpy;
		}
		else if (pcameraStickedToMouse){
			double a = (application.nm.ovCamera.focal+Math.abs(application.nm.ovCamera.altitude))/application.nm.ovCamera.focal;
			application.nm.ovCamera.move(a*(lastJPX-jpx), a*(jpy-lastJPY));
			application.nm.mCamera.move(a*(lastJPX-jpx), a*(jpy-lastJPY));
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
                application.nm.mCamera.setXspeed(((jpx-lastJPX)*(a/PAN_SPEED_COEF)));
                application.nm.mCamera.setYspeed(((lastJPY-jpy)*(a/PAN_SPEED_COEF)));
                application.nm.mCamera.setZspeed(0);
            }		    
		}
		else if (draggedMatrix != null){
		    double a = (application.nm.mCamera.focal+Math.abs(application.nm.mCamera.altitude)) / application.nm.mCamera.focal;
		    draggedMatrix.move(a*(jpx-lastJPX), a*(lastJPY-jpy));
			lastJPX = jpx;
			lastJPY = jpy;
		}
    }

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
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

	public void enterGlyph(Glyph g){}

	public void exitGlyph(Glyph g){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code==KeyEvent.VK_PAGE_UP){application.nm.getHigherView();}
    	else if (code==KeyEvent.VK_PAGE_DOWN){application.nm.getLowerView();}
    	else if (code==KeyEvent.VK_HOME){application.nm.getGlobalView();}
    	else if (code==KeyEvent.VK_UP){application.nm.translateView(NavigationManager.MOVE_UP);}
    	else if (code==KeyEvent.VK_DOWN){application.nm.translateView(NavigationManager.MOVE_DOWN);}
    	else if (code==KeyEvent.VK_LEFT){application.nm.translateView(NavigationManager.MOVE_LEFT);}
    	else if (code==KeyEvent.VK_RIGHT){application.nm.translateView(NavigationManager.MOVE_RIGHT);}
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

	/* Overview Portal */
	public void enterPortal(Portal p){
		inPortal = true;
		((OverviewPortal)p).setBorder(ConfigManager.OV_INSIDE_BORDER_COLOR);
		VirtualSpaceManager.INSTANCE.repaint();
	}

	public void exitPortal(Portal p){
		inPortal = false;
		((OverviewPortal)p).setBorder(ConfigManager.OV_BORDER_COLOR);
		VirtualSpaceManager.INSTANCE.repaint();
	}
	
}
