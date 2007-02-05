/*   FILE: TrailingCameraPortalST.java
 *   DATE OF CREATION:  Wed Jul 05 15:00:06 2006
 *   AUTHOR :           Caroline Appert (appert@lri.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */

package net.claribole.zvtm.engine;

import com.xerox.VTM.engine.Camera;

/**A portal showing what is seen through a camera, with parameterable alpha channel (translucency).
   The portal behaves like a trailing widget.
   The Camera should not be used in any other View or Portal.*/

public class TrailingCameraPortalSTInv extends TrailingCameraPortalST {

    /** Builds a new possibly translucent portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param c camera associated with the portal
     *@param a alpha channel value (translucency). alpha ranges between 0.0 (fully transparent) and 1.0 (fully opaque)
     *@param xo horizontal offset (in pixels) between cursor and portal (trailing widget)
     *@param yo vertical offset (in pixels) between cursor and portal (trailing widget)
     */
    public TrailingCameraPortalSTInv(int x, int y, int w, int h, Camera c, float a, int xo, int yo){
	super(x, y, w, h, c, a, xo, yo);
    }
    public void updateWidgetLocation(){
	targetPos.setLocation(parentPos.getX() + xOffset, parentPos.getY() + yOffset);
	double distAway = targetPos.distance(currentPos);
	double maxDist = 2 * Math.abs(xOffset);
	double opacity = 1.0 - Math.min(1.0, distAway / maxDist);
 	filter.setCutOffFrequency(((1.0 - opacity) * 0.4) + 0.01);
// 	filter.setCutOffFrequency(((1.0 - opacity) * 0.4) + 0.1);
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

}
