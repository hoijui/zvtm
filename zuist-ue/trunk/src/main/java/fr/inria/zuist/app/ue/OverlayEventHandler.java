/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
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

class OverlayEventHandler implements ViewEventHandler {
    
    UISTExplorer application;

    Glyph g = null;

    OverlayEventHandler(UISTExplorer app){
        this.application = app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        if (application.ovm.showingLinks){
            Vector labels = v.getMouse().getIntersectingTexts(application.ovCamera);
            boolean clickedOnLink = false;
            if (labels != null){
                g = (Glyph)labels.elementAt(0);
                v.parent.setActiveLayer(0);
                String type = g.getType();
                application.ovm.hideLinks(false);
                if (type != null){
                    if (type.startsWith("O")){
                        application.rememberLocation();
                        application.goToObject(type.substring(1), true, UISTExplorer.AUTHOR_CAMERA_ALTITUDE);
                    }
                    else {
                        // starts with "R"
                        application.rememberLocation();
                        application.goToRegion(type.substring(1));
                    }
                }
                clickedOnLink = true;
            }
            if (!clickedOnLink){
                // if not clicking on a label
                // dismiss the overlay, not matter whether user clicked on faded rect or empty region
                application.ovm.hideLinks(true);
                v.parent.setActiveLayer(0);            
            }
        }
        else if (application.ovm.showingAbout){
            application.ovm.hideAbout();
            v.parent.setActiveLayer(0);
        }
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){

    }

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){System.err.println("a");
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){

    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){

    }

    public void enterGlyph(Glyph g){}

    public void exitGlyph(Glyph g){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){

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

}
