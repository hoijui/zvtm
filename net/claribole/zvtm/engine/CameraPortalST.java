/*   FILE: CameraPortalST.java
 *   DATE OF CREATION:  Sat Jun 17 13:58:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.engine;

import java.awt.Graphics2D;
import java.awt.AlphaComposite;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.Transparent;

/**A portal showing what is seen through a camera, with parameterable alpha channel (translucency).
   The Camera should not be used in any other View or Portal.*/

public class CameraPortalST extends CameraPortal implements Transparent {

    /** for translucency (default is 0.5)*/
    AlphaComposite acST;

    /** alpha channel*/
    float alpha = 0.5f;

    /** Builds a new possibly translucent portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param c camera associated with the portal
     *@param a alpha channel value (translucency). alpha ranges between 0.0 (fully transparent) and 1.0 (fully opaque)
     */
    public CameraPortalST(int x, int y, int w, int h, Camera c, float a){
	super(x, y, w, h, c);
	this.alpha = a;
	acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.alpha);
    }

    /**
     *set alpha channel value (transparency)
     *@param a [0;1.0] 0 is fully transparent, 1 is opaque
     */
    public void setTransparencyValue(float a){
	try {
	    acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a);  //transparency set to alpha
	    alpha = a;
	}
	catch (IllegalArgumentException ex){
	    if (VirtualSpaceManager.debugModeON()){System.err.println("Error animating translucency of "+this.toString());}
	}
    }

    /**get alpha value (transparency) for this glyph*/
    public float getTransparencyValue(){
	return alpha;
    }

    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setClip(x, y, w, h);
	g2d.setComposite(acST);
	if (bkgColor != null){
	    g2d.setColor(bkgColor);
	    g2d.fillRect(x, y, w, h);
	}
	g2d.translate(x, y);
	standardStroke = g2d.getStroke();
	standardTransform = g2d.getTransform();
	drawnGlyphs = cameraSpace.getDrawnGlyphs(camIndex);
	synchronized(drawnGlyphs){
	    drawnGlyphs.removeAllElements();
	    uncoef = (float)((camera.focal+camera.altitude) / camera.focal);
	    //compute region seen from this view through camera
	    viewWC = (long)(camera.posx - (w/2)*uncoef);
	    viewNC = (long)(camera.posy + (h/2)*uncoef);
	    viewEC = (long)(camera.posx + (w/2)*uncoef);
	    viewSC = (long)(camera.posy - (h/2)*uncoef);
	    gll = cameraSpace.getVisibleGlyphList();
	    for (int i=0;i<gll.length;i++){
		if (gll[i] != null){
		    synchronized(gll[i]){
			if (gll[i].visibleInRegion(viewWC, viewNC, viewEC, viewSC, camIndex)){
			    //if glyph is at least partially visible in the reg. seen from this view, display
			    gll[i].project(camera, size); // an invisible glyph should still be projected
			    if (gll[i].isVisible()){      // as it can be sensitive
				gll[i].draw(g2d, w, h, camIndex, standardStroke, standardTransform);
			    }
			}
		    }
		}
	    }
	}
	g2d.translate(-x, -y);
	g2d.setClip(0, 0, viewWidth, viewHeight);
	if (borderColor != null){
	    g2d.setColor(borderColor);
	    g2d.drawRect(x, y, w, h);
	}
	g2d.setComposite(acO);
    }

}
