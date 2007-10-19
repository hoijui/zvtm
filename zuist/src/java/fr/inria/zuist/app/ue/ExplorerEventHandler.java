/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ExplorerEventHandler.java,v 1.23 2007/10/15 12:33:33 pietriga Exp $
 */

package fr.inria.zuist.app.ue;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

import java.util.Vector;

import com.xerox.VTM.engine.VCursor;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VText;
import net.claribole.zvtm.engine.ViewEventHandler;
import net.claribole.zvtm.engine.AnimationListener;

import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.TextDescription;

class ExplorerEventHandler implements ViewEventHandler, AnimationListener, ComponentListener {

    static final float MAIN_SPEED_FACTOR = 50.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;

    static final float CIRCULAR_ZOOMIN_FACTOR = 21.0f;
    static final float CIRCULAR_ZOOMOUT_FACTOR = 22.0f;
    
    static float WHEEL_MM_STEP = 1.0f;
    
    static final int DRAGGING_DISTANCE_THRESHOLD = 5;
    
    static final int NO_LENS = 0;
    static final int ZOOMIN_LENS = 1;
    static final int ZOOMOUT_LENS = -1;
    int lensType = NO_LENS;

    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    int pJPX, pJPY; // coordinates where mouse button was last pressed
    long lastVX, lastVY;
    int currentJPX, currentJPY;
    boolean cursorNearBorder = false;

    /* bounds of region in virtual space currently observed through mCamera */
    long[] wnes = new long[4];
    float oldCameraAltitude;

    boolean mCamStickedToMouse = false;

    UISTExplorer application;
    
    Glyph g;
    
    boolean dragging = false;
    
    ExplorerEventHandler(UISTExplorer app){
	this.application = app;
	oldCameraAltitude = this.application.mCamera.getAltitude();
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        if (application.NAV_MODE == UISTExplorer.NAV_MODE_SS){
            application.toggleNavigationMode();
            return;
        }
        if (jpx <= MenuManager.MENU_ZONE_WIDTH && jpy <= MenuManager.MENU_ZONE_HEIGHT){
            return;
        }
        lastVX = v.getMouse().vx;
        lastVY = v.getMouse().vy;
        pJPX = jpx;
        pJPY = jpy;
        switch(application.NAV_MODE){
            case UISTExplorer.NAV_MODE_FISHEYE:{
                if (lensType != NO_LENS){
                    application.flattenLens();
                }
                else {
                    if (cursorNearBorder){
                        // do not activate the lens when cursor is near the border
                        return;
                    }
                    application.activateLens(jpx, jpy-UISTExplorer.LENS_R1);
                }
                // just in case free mode was left without this becoming false
                dragging = false;
                break;
            }
            case UISTExplorer.NAV_MODE_FISHEYE2:{
                if (lensType != NO_LENS){
                    application.flattenLens();
                }
                else {
                    if (cursorNearBorder){
                        // do not activate the lens when cursor is near the border
                        return;
                    }
                    application.activateLens(jpx, jpy-UISTExplorer.LENS_R1);
                }
                // just in case free mode was left without this becoming false
                dragging = false;
                break;
            }
            case UISTExplorer.NAV_MODE_DEFAULT:{
                dragging = true;
                break;
            }
        }
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        // if clicking in top left corner, means we are interacting with menu, activate menu layer
        if (jpx <= MenuManager.MENU_ZONE_WIDTH && jpy <= MenuManager.MENU_ZONE_HEIGHT){
			if (jpx < 2*MenuManager.NAV_MENU_ITEM_WIDTH){
	            v.parent.setActiveLayer(1);
				application.mm.expandNavMenu(true);
			}
			else if (jpx < 2*MenuManager.NAV_MENU_ITEM_WIDTH+2*MenuManager.HOME_BT_WIDTH){
				application.goHome();
			}
			else if (jpx < 2*MenuManager.NAV_MENU_ITEM_WIDTH+2*MenuManager.HOME_BT_WIDTH+2*MenuManager.UP_BT_WIDTH){
				application.goUp();
			}
			else if (jpx < 2*MenuManager.NAV_MENU_ITEM_WIDTH+2*MenuManager.HOME_BT_WIDTH+2*MenuManager.UP_BT_WIDTH+2*MenuManager.BACK_BT_WIDTH){
				application.moveBack();
			}
			else if (jpx < 2*MenuManager.NAV_MENU_ITEM_WIDTH+2*MenuManager.HOME_BT_WIDTH+2*MenuManager.UP_BT_WIDTH+2*MenuManager.BACK_BT_WIDTH+2*MenuManager.ABOUT_BT_WIDTH){
	            v.parent.setActiveLayer(2);
				application.about();
			}
            return;
        }
        switch(application.NAV_MODE){
            case UISTExplorer.NAV_MODE_FISHEYE:{
                if (lensType != NO_LENS){
                    application.flattenLens();
                }
                break;
            }
            case UISTExplorer.NAV_MODE_FISHEYE2:{
                if (lensType != NO_LENS){
                    application.flattenLens();
                }
                break;
            }
            case UISTExplorer.NAV_MODE_DEFAULT:{
                if (Math.sqrt(Math.pow(jpx-pJPX,2) + Math.pow(jpy-pJPY,2)) > DRAGGING_DISTANCE_THRESHOLD){
                    // if distance between press and release is superior to a few pixels,
                    // do not consider this as a click, user is actually panning
                    break;
                }
                Vector labels = v.getMouse().getIntersectingTexts(application.mCamera);
                if (labels != null){
                    // clicked on a text string, like an author name, or a paper title
                    int i = 0;
                    TextDescription td;
                    while (i<labels.size()){
                        // get first text string that is actually sensitive
                        // (all are returned by getIntersectingTexts, no matter their actual state)
                        // rely on its ObjectDescription's sensitivity setting, as the glyph's one
                        // is kept to false for efficiency reasons
                        td = (TextDescription)((VText)labels.elementAt(i)).getOwner();
                        if (td.isSensitive()){
                            clickedText(td);
                            return;
                        }
                        i++;
                    }
                    if ((g=v.lastGlyphEntered()) != null){
                        clickedObject(g);
                    }
                }
                else if ((g=v.lastGlyphEntered()) != null){
                    clickedObject(g);
                }
                // just in case free mode was left without this becoming false
                dragging = false;
                break;                
            }
        }
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastVX = v.getMouse().vx;
        lastVY = v.getMouse().vy;
        Vector labels = v.getMouse().getIntersectingTexts(application.mCamera);
        if (labels != null){
            // clicked on a text string, like an author name, or a paper title
            int i = 0;
            TextDescription td;
            while (i<labels.size()){
                // get first text string that is actually sensitive
                // (all are returned by getIntersectingTexts, no matter their actual state)
                // rely on its ObjectDescription's sensitivity setting, as the glyph's one
                // is kept to false for efficiency reasons
                td = (TextDescription)((VText)labels.elementAt(i)).getOwner();
                if (td.isSensitive()){
                    clickedText(td);
                    return;
                }
                i++;
            }
            if ((g=v.lastGlyphEntered()) != null){
                clickedObject(g);
            }
        }
        else if ((g=v.lastGlyphEntered()) != null){
            clickedObject(g);
        }
        // just in case free mode was left without this becoming false
        dragging = false;
    }

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	mCamStickedToMouse = false;
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        if (UISTExplorer.SHOW_STATUS_BAR){
            application.mView.setStatusBarText(v.getMouse().vx+" "+v.getMouse().vy
                +" "+application.mCamera.posx+" "+application.mCamera.posy+" "+application.mCamera.altitude);
        }
        currentJPX = jpx;
        currentJPY = jpy;
        if ((jpx-UISTExplorer.LENS_R1) < 0){
            jpx = UISTExplorer.LENS_R1;
            cursorNearBorder = true;
        }
        else if ((jpx+UISTExplorer.LENS_R1) > application.panelWidth){
            jpx = application.panelWidth - UISTExplorer.LENS_R1;
            cursorNearBorder = true;
        }
        else {
            cursorNearBorder = false;
        }
        if (jpy < 2*UISTExplorer.LENS_R1){
            jpy = 2*UISTExplorer.LENS_R1;
            cursorNearBorder = true;
        }
        // as the left is offset upward by a distance of LENS_R1, there can never be an intersection with the bottom border
       /*else if ((jpy) > application.panelHeight){
            jpy = application.panelHeight - UISTExplorer.LENS_R1;
            cursorNearBorder = true;
        }*/
        lastJPX = jpx;
        lastJPY = jpy;
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        currentJPX = jpx;
        currentJPY = jpy;
        if ((jpx-UISTExplorer.LENS_R1) < 0){
            jpx = UISTExplorer.LENS_R1;
            cursorNearBorder = true;
        }
        else if ((jpx+UISTExplorer.LENS_R1) > application.panelWidth){
            jpx = application.panelWidth - UISTExplorer.LENS_R1;
            cursorNearBorder = true;
        }
        else {
            cursorNearBorder = false;
        }
        if (jpy < 2*UISTExplorer.LENS_R1){
            jpy = 2*UISTExplorer.LENS_R1;
            cursorNearBorder = true;
        }
        switch(application.NAV_MODE){
            case UISTExplorer.NAV_MODE_FISHEYE:{
                if (lensType != NO_LENS && application.lens != null){
                    application.moveLens(jpx, jpy-UISTExplorer.LENS_R1);
                }
                break;
            }
            case UISTExplorer.NAV_MODE_FISHEYE2:{
                if (lensType != NO_LENS && application.lens != null){
                    application.moveLens(jpx, jpy-UISTExplorer.LENS_R1);
                }
                break;
            }
            case UISTExplorer.NAV_MODE_DEFAULT:{
                if (dragging){
                    float a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
                    synchronized(application.mCamera){
                        application.mCamera.move(Math.round(a*(lastJPX-jpx)), Math.round(a*(jpy-lastJPY)));
                        lastJPX = jpx;
                        lastJPY = jpy;
                        cameraMoved();
                    }
                }
                break;
            }
        }
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	float a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
	if (wheelDirection  == WHEEL_UP){// zooming in
	    application.mCamera.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
	    cameraMoved();
	    application.vsm.repaintNow();
	}
	else {//wheelDirection == WHEEL_DOWN, zooming out
	    application.mCamera.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
	    cameraMoved();
	    application.vsm.repaintNow();
	}
    }

    public void enterGlyph(Glyph g){
//        g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
//        g.highlight(false, null);
    }

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code == KeyEvent.VK_HOME){application.vsm.getGlobalView(application.mCamera, 500);}
        else if (code == KeyEvent.VK_M){application.toggleMemoryUsageDisplay();}
        else if (code == KeyEvent.VK_G){application.gc();}
        else if (code == KeyEvent.VK_A){application.toggleAntialiasing();}
        else if (code == KeyEvent.VK_OPEN_BRACKET){circularZoomIn();}
        else if (code == KeyEvent.VK_CLOSE_BRACKET){circularZoomOut();}
        else if (code == KeyEvent.VK_S){
            // also triggered when pressing middle mouse button
            application.toggleNavigationMode();
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
    

    public void cameraMoved(){
	// region seen through camera
	application.mView.getVisibleRegion(application.mCamera, wnes);
	float alt = application.mCamera.getAltitude();
	if (alt != oldCameraAltitude){// camera was an altitude change
	    application.altitudeChanged();
	    oldCameraAltitude = alt;
	}
	else {// camera movement was a simple translation
	    application.sm.updateVisibleRegions();
	}
    }
    
    void clickedText(TextDescription td){
	application.clickedText(td);
    }

    void clickedObject(Glyph gl){
        Object owner = gl.getOwner();
        if (owner != null){
            if (owner instanceof Region){
                Region r = (Region)owner;
                application.clickedOnRegion(r, gl, false);
            }
            else if (owner instanceof ObjectDescription){
                ObjectDescription od = (ObjectDescription)owner;
                application.clickedOnObject(od, gl, false);
            }
        }
    }
        
    public void circularZoomIn(){
        if (lensType != NO_LENS && application.lens != null){
            // if a lens is active, change the lens' mag factor
            application.magnifyFocus(-WHEEL_MM_STEP);
        }
        else {
            // otherwise change the camera's altitude
            float a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
            application.mCamera.altitudeOffset(a*CIRCULAR_ZOOMOUT_FACTOR);
            cameraMoved();
            application.vsm.repaintNow();            
        }
    }

    public void circularZoomOut(){
        if (lensType != NO_LENS && application.lens != null){
            // if a lens is active, change the lens' mag factor
            application.magnifyFocus(WHEEL_MM_STEP);
        }
        else {
            // otherwise change the camera's altitude
            float a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
            application.mCamera.altitudeOffset(-a*CIRCULAR_ZOOMIN_FACTOR);
            cameraMoved();
            application.vsm.repaintNow();            
        }
    }
    
}
