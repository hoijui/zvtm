/*   FILE: TrailingOverview.java
 *   DATE OF CREATION:  Mon Jul 10 14:07:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */

package net.claribole.zvtm.engine;

import java.awt.geom.Point2D;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.util.Timer;
import java.util.TimerTask;

import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.Transparent;

/**A portal behaving as a trailing widget and showing what is seen through a camera that serves as an overview. Shape: rectangular.
   The Camera should not be used in any other View or Portal.*/

public class TrailingOverviewInv extends TrailingOverview {

    /** Builds a new possibly translucent portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param pc camera associated with the portal (provinding the overview)
     *@param orc camera observing a region; this region is displayed as an overlay in the portal
     *@param a alpha channel value (translucency). alpha ranges between 0.0 (fully transparent) and 1.0 (fully opaque)
     *@param xo horizontal offset (in pixels) between cursor and portal (trailing widget)
     *@param yo vertical offset (in pixels) between cursor and portal (trailing widget)
     */
    public TrailingOverviewInv(int x, int y, int w, int h, Camera pc, Camera orc, float a, int xo, int yo){
	super(x, y, w, h, pc, orc, a, xo, yo);
    }

    public void updateWidgetLocation(){
	targetPos.setLocation(parentPos.getX() + xOffset, parentPos.getY() + yOffset);
	double distAway = targetPos.distance(currentPos);
	double maxDist = 2 * Math.abs(xOffset);
	double opacity = 1.0 - Math.min(1.0, distAway / maxDist);
 	filter.setCutOffFrequency(((1.0 - opacity) * cutoffParamA) + cutoffParamB);
	currentPos = filter.apply(targetPos, frequency);
	int tx = (int)Math.round(currentPos.getX());
	int ty = (int)Math.round(currentPos.getY());
	tx = Math.max(tx, w/2);
 	ty = Math.min(ty, owningView.getPanelSize().height - h/2);
	if (x != tx-w/2 || y != ty-h/2){// avoid unnecesarry repaint requests
	    this.moveTo(tx-w/2, ty-h/2);
	    // make the widget almost disappear when making big moves
	    setTransparencyValue(0.5f-(float)opacity/2.0f);
	    owningView.repaintNow();
	}
    }

    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setClip(x, y, w, h);
	g2d.setComposite(acST);
	if (bkgColor != null){
	    g2d.setColor(bkgColor);
	    g2d.fillRect(x, y, w, h);
	}
	standardStroke = g2d.getStroke();
	// be sure to call the translate instruction before getting the standard transform
	// as the latter's matrix is preconcatenated to the translation matrix of glyphs
	// that use AffineTransforms for translation
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
				gll[i].draw(g2d, w, h, camIndex, standardStroke, standardTransform, x, y);
			    }
			}
		    }
		}
	    }
	}
	// paint region observed through observedRegionCamera
	observedRegion = observedRegionView.getVisibleRegion(observedRegionCamera, observedRegion);
	g2d.setColor(Color.GREEN);
 	g2d.setComposite(orST);
	orcoef = (float)(camera.focal/(camera.focal+camera.altitude));
	g2d.fillRect(x+w/2 + Math.round((observedRegion[0]-camera.posx)*orcoef),
		     y+h/2 - Math.round((observedRegion[1]-camera.posy)*orcoef),
		     Math.round((observedRegion[2]-observedRegion[0])*orcoef),
		     Math.round((observedRegion[1]-observedRegion[3])*orcoef));
	g2d.setComposite(acST);
	g2d.drawRect(x+w/2 + Math.round((observedRegion[0]-camera.posx)*orcoef),
		     y+h/2 - Math.round((observedRegion[1]-camera.posy)*orcoef),
		     Math.round((observedRegion[2]-observedRegion[0])*orcoef),
		     Math.round((observedRegion[1]-observedRegion[3])*orcoef));
	// reset Graphics2D
	g2d.setClip(0, 0, viewWidth, viewHeight);
	if (borderColor != null){
	    g2d.setColor(borderColor);
	    g2d.drawRect(x, y, w, h);
	}
	g2d.setComposite(acO);
    }

}
