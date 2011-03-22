/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: MainEventHandler.java 2769 2010-01-15 10:17:58Z epietrig $
 */

package fr.inria.zvtm.alma;

import java.awt.geom.Point2D;
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

class MainEventHandler implements ViewListener, ComponentListener, PortalListener {

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;
    static final float WHEEL_ZOOMIN_COEF = 21.0f;
    static final float WHEEL_ZOOMOUT_COEF = 22.0f;
    static float WHEEL_MM_STEP = 1.0f;
    
    //remember last mouse coords
    int lastJPX,lastJPY;
    double lastVX, lastVY;
    
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
    
    MainEventHandler(Viewer app){
        this.application = app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        Point2D spcCoords = v.viewToSpaceCoords(application.nm.mCamera, 
                jpx, jpy);
        System.out.println("error space coords: " + spcCoords.getX() + ", " + spcCoords.getY());
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

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
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

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        if (mod == SHIFT_MOD){
            lastVX = v.getVCursor().getVSXCoordinate();
            lastVY = v.getVCursor().getVSXCoordinate();
            if (!inPortal){
                if (application.nm.lensType != Navigation.NO_LENS){
                    application.nm.zoomInPhase2(lastVX, lastVY);
                }
                else {
                    if (cursorNearBorder){
                        // do not activate the lens when cursor is near the border
                        return;
                    }
                    application.nm.zoomInPhase1(jpx, jpy);
                }            
            }
        }
        else {
            Glyph[] pickList = v.getVCursor().getPicker().getPickedGlyphList();
            if (pickList.length == 0){
                return;
            }
            Glyph pickedGlyph = pickList[pickList.length - 1];
            v.parent.centerOnGlyph(pickedGlyph, application.nm.mCamera, 500);            
        }
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
	    application.bgCamera.setXspeed(0);
        application.bgCamera.setYspeed(0);
        application.bgCamera.setZspeed(0);
        v.setDrawDrag(false);
        panning = false;
	}

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        if (mod == SHIFT_MOD || mod == META_SHIFT_MOD){
            lastVX = v.getVCursor().getVSXCoordinate();
            lastVY = v.getVCursor().getVSYCoordinate();
            if (application.nm.lensType != Navigation.NO_LENS){
                application.nm.zoomOutPhase2();
            }
            else {
                if (cursorNearBorder){
                    // do not activate the lens when cursor is near the border
                    return;
                }
                application.nm.zoomOutPhase1(jpx, jpy, lastVX, lastVY);
            }
        }
        else {
            if (v.lastGlyphEntered() != null){
        		application.mView.centerOnGlyph(v.lastGlyphEntered(), v.cams[0], Config.ANIM_MOVE_LENGTH, true, 1.0f);				
    		}            
        }
    }
        
    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        if ((jpx-Navigation.LENS_R1) < 0){
    	    jpx = Navigation.LENS_R1;
    	    cursorNearBorder = true;
    	}
    	else if ((jpx+Navigation.LENS_R1) > application.panelWidth){
    	    jpx = application.panelWidth - Navigation.LENS_R1;
    	    cursorNearBorder = true;
    	}
    	else {
    	    cursorNearBorder = false;
    	}
    	if ((jpy-Navigation.LENS_R1) < 0){
    	    jpy = Navigation.LENS_R1;
    	    cursorNearBorder = true;
    	}
    	else if ((jpy+Navigation.LENS_R1) > application.panelHeight){
    	    jpy = application.panelHeight - Navigation.LENS_R1;
    	    cursorNearBorder = true;
    	}
    	if (application.nm.lensType != 0 && application.nm.lens != null){
    	    application.nm.moveLens(jpx, jpy, e.getWhen());
    	}
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
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
            Camera c = application.nm.mCamera;
            double a = (c.focal+Math.abs(c.altitude))/c.focal;
            if (mod == META_SHIFT_MOD) {
                c.setXspeed(0);
                c.setYspeed(0);
                c.setZspeed(((lastJPY-jpy)*(ZOOM_SPEED_COEF)));
            }
            else {
                c.setXspeed((long)((jpx-lastJPX)*(a/PAN_SPEED_COEF)));
                c.setYspeed((long)((lastJPY-jpy)*(a/PAN_SPEED_COEF)));
                c.setZspeed(0);
            }		    
		}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        if (application.nm.lensType != 0 && application.nm.lens != null){
            if (wheelDirection  == ViewListener.WHEEL_UP){
                application.nm.magnifyFocus(Navigation.WHEEL_MM_STEP, application.nm.lensType, application.nm.mCamera);
            }
            else {
                application.nm.magnifyFocus(-Navigation.WHEEL_MM_STEP, application.nm.lensType, application.nm.mCamera);
            }
        }
        else {
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
        if (code==KeyEvent.VK_PAGE_UP){application.nm.getHigherView();}
    	else if (code==KeyEvent.VK_PAGE_DOWN){application.nm.getLowerView();}
    	else if (code==KeyEvent.VK_HOME){application.nm.getGlobalView();}
    	else if (code==KeyEvent.VK_UP){application.nm.translateView(Navigation.MOVE_UP);}
    	else if (code==KeyEvent.VK_DOWN){application.nm.translateView(Navigation.MOVE_DOWN);}
    	else if (code==KeyEvent.VK_LEFT){application.nm.translateView(Navigation.MOVE_LEFT);}
    	else if (code==KeyEvent.VK_RIGHT){application.nm.translateView(Navigation.MOVE_RIGHT);}
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
		((OverviewPortal)p).setBorder(Config.OV_INSIDE_BORDER_COLOR);
		VirtualSpaceManager.INSTANCE.repaint();
	}

	public void exitPortal(Portal p){
		inPortal = false;
		((OverviewPortal)p).setBorder(Config.OV_BORDER_COLOR);
		VirtualSpaceManager.INSTANCE.repaint();
	}
	
}
