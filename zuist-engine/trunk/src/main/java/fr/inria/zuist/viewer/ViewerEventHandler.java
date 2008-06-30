/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.viewer;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

import java.util.Vector;

import com.xerox.VTM.engine.VCursor;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VText;
import net.claribole.zvtm.engine.ViewEventHandler;
import net.claribole.zvtm.engine.AnimationListener;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.TextDescription;

class ViewerEventHandler implements ViewEventHandler, AnimationListener, ComponentListener {

    static final float MAIN_SPEED_FACTOR = 50.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;
    
    static float WHEEL_MM_STEP = 1.0f;
    
    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    long lastVX, lastVY;
    int currentJPX, currentJPY;

    /* bounds of region in virtual space currently observed through mCamera */
    long[] wnes = new long[4];
    float oldCameraAltitude;

    boolean mCamStickedToMouse = false;

    Viewer application;
    
    Glyph g;
    
    boolean cursorNearBorder = false;
    boolean dragging = false;

	Glyph objectJustSelected = null;
    
    ViewerEventHandler(Viewer app){
        this.application = app;
        oldCameraAltitude = this.application.mCamera.getAltitude();
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        dragging = true;
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        dragging = false;
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
		Vector gum = v.getMouse().getIntersectingGlyphs(v.cams[0]);
		if (gum == null){
			return;
		}
		Glyph g = (Glyph)gum.lastElement();
		if (objectJustSelected != null && g == objectJustSelected){
			// last click was on this object, already centered on it,
			// check if it takes somewhere and go there if it does
			Object owner = g.getOwner();
			if (owner != null && owner instanceof ObjectDescription){
				ObjectDescription od = (ObjectDescription)owner;
				String takesToID = od.takesTo();
				if (takesToID != null){
					switch(od.takesToType()){
						case SceneManager.TAKES_TO_OBJECT:{application.centerOnObject(takesToID);break;}
						case SceneManager.TAKES_TO_REGION:{application.centerOnRegion(takesToID);break;}
					}
				}
			}				
		}
		else {
			// last click was not on this object, center on it
			application.rememberLocation(application.mCamera.getLocation());
			application.vsm.centerOnGlyph(g, v.cams[0], Viewer.ANIM_MOVE_LENGTH, true, 1.2f);				
			objectJustSelected = g;
		}
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		v.parent.setActiveLayer(1);
		application.displayMainPieMenu(true);
	}

	public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		Glyph g = v.lastGlyphEntered();
		if (g != null && g.getType() == Messages.PM_ENTRY){
			application.pieMenuEvent(g);
		}
		if (application.mainPieMenu != null){
			application.displayMainPieMenu(false);
		}
		v.parent.setActiveLayer(0);
	}

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}
        
    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (dragging){
            float a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
            synchronized(application.mCamera){
                application.mCamera.move(Math.round(a*(lastJPX-jpx)), Math.round(a*(jpy-lastJPY)));
                lastJPX = jpx;
                lastJPY = jpy;
                cameraMoved();
            }
        }
    }

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
		float a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
		if (wheelDirection  == WHEEL_UP){
			// zooming in
			application.mCamera.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
			cameraMoved();
			application.vsm.repaintNow();
		}
		else {
			//wheelDirection == WHEEL_DOWN, zooming out
			application.mCamera.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
			cameraMoved();
			application.vsm.repaintNow();
		}
	}

	public void enterGlyph(Glyph g){
		if (application.vsm.getActiveView().getActiveLayer() == 1){
			// interacting with pie menu
			g.highlight(true, null);
			VirtualSpace vs = application.vsm.getVirtualSpace(application.mnSpaceName);
			vs.onTop(g);
			int i = Utilities.indexOfGlyph(application.mainPieMenu.getItems(), g);
			if (i != -1){
				vs.onTop(application.mainPieMenu.getLabels()[i]);
			}
		}
	}

	public void exitGlyph(Glyph g){
		if (application.vsm.getActiveView().getActiveLayer() == 1){
			g.highlight(false, null);
		}
    }

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
    	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
    	else if (code==KeyEvent.VK_HOME){application.getGlobalView();}
    	else if (code==KeyEvent.VK_UP){application.translateView(Viewer.MOVE_UP);}
    	else if (code==KeyEvent.VK_DOWN){application.translateView(Viewer.MOVE_DOWN);}
    	else if (code==KeyEvent.VK_LEFT){application.translateView(Viewer.MOVE_LEFT);}
    	else if (code==KeyEvent.VK_RIGHT){application.translateView(Viewer.MOVE_RIGHT);}
		else if (code == KeyEvent.VK_F1){application.toggleMiscInfoDisplay();}
        else if (code == KeyEvent.VK_F2){application.gc();}
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
        if (alt != oldCameraAltitude){
            // camera was an altitude change
            application.altitudeChanged();
            oldCameraAltitude = alt;
        }
        else {
            // camera movement was a simple translation
            application.sm.updateVisibleRegions();
        }
    }

}
