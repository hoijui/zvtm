/*   FILE: ZgrvBiManHdlr.java
 *   DATE OF CREATION:  Thu May 26 18:53:59 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ZgrvBiManHdlr.java,v 1.3 2005/06/02 14:01:02 skbo Exp $
 */

package net.claribole.zgrviewer.mpd;

import net.claribole.zgrviewer.ZGRViewer;
import net.claribole.zgrviewer.GraphicsManager;
import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;

import net.claribole.zvtm.mpd.MPDAppEventHandler;

import domino.Socket;

public class ZgrvBiManHdlr implements MPDAppEventHandler {

    GraphicsManager grMngr;
    private BiManPlugin plugin;

    public ZgrvBiManHdlr(GraphicsManager gm, BiManPlugin plugin){
	this.grMngr = gm;
	this.plugin = plugin;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, Socket pd){}

    public void release1(ViewPanel v,int mod,int jpx,int jpy, Socket pd){
        grMngr.vsm.getGlobalView(grMngr.vsm.getActiveCamera(), 300);
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, Socket pd){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, Socket pd){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, Socket pd){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, Socket pd){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, Socket pd){}

    public void release3(ViewPanel v,int mod,int jpx,int jpy, Socket pd){}

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, Socket pd){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, Socket pd){
        Camera c = grMngr.vsm.getActiveCamera();
        float a=(c.focal+Math.abs(c.altitude))/c.focal;
            c.altitudeOffset(Math.signum(jpy)*a*10*plugin.NON_DOMINANT_HAND_DEVICE_SENSITIVITY);
            grMngr.cameraMoved();
        
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, Socket pd){}

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, Socket pd){}

}
