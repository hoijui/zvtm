/*   FILE: CameraPortal.java
 *   DATE OF CREATION:  Sat Jun 17 07:19:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.engine;

import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import java.util.Vector;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.LongPoint;
import net.claribole.zvtm.engine.Location;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.glyphs.Glyph;

/**A portal showing what is seen through a camera. Shape: rectangular.
   The Camera should not be used in any other View or Portal.*/

public class CameraPortal extends Portal {

    /**Draw a border delimiting the portal (null if no border).*/
    Color borderColor;

    /**Portal's background fill color (null if transparent).*/
    Color bkgColor;

    // Camera used to render the portal
    Camera camera;
    // space owning camera (optimization)
    VirtualSpace cameraSpace;

    // Region of virtual space seen through camera 
    long viewWC, viewNC, viewEC, viewSC;

    // inverse of projection coef
    float uncoef;

    Vector drawnGlyphs;
    Glyph[] gll;
    // camera's index in parent virtual space
    int camIndex;

    /*Graphics2d's original stroke. Passed to each glyph in case it needs to modifiy the stroke when painting itself*/
    Stroke standardStroke;

    /*Graphics2d's original affine transform. Passed to each glyph in case it needs to modifiy the affine transform when painting itself*/
    AffineTransform standardTransform;

    /** Builds a new portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param c camera associated with the portal
     */
    public CameraPortal(int x, int y, int w, int h, Camera c){
	this.x = x;
	this.y = y;
	this.w = w;
	this.h = h;
	updateDimensions();
	this.camera = c;
	this.cameraSpace = this.camera.getOwningSpace();
	this.camIndex = this.camera.getIndex();
    }

    /**CALLED INTERNALLY - NOT FOR PUBLIC USE*/
    public void setOwningView(View v){
	super.setOwningView(v);
	camera.setOwningView(v);
    }

    /**Draw a border delimiting the portal.
     *@param bc color of the border (null if none)*/    
    public void setBorder(Color bc){
	this.borderColor = bc;
    }

    /**Get the color used to draw the border delimiting this portal.
     *@return color of the border (null if none)*/    
    public Color getBorder(){
	return borderColor;
    }

    /**Fill background with a color.
     *@param bc color of the border (null if none)*/    
    public void setBackgroundColor(Color bc){
	this.bkgColor = bc;
    }

    /**Get the color used to fill the background.
     *@return color of the border (null if none)*/    
    public Color getBackgroundColor(){
	return bkgColor;
    }

    /**returns bounds of rectangle representing virtual space's region seen through camera c [west,north,east,south]*/
    public long[] getVisibleRegion(){
	float uncoef = (float)((camera.focal+camera.altitude) / camera.focal);
	long[] res = {(long)(camera.posx - (w/2)*uncoef),
		      (long)(camera.posy + (h/2)*uncoef),
		      (long)(camera.posx + (w/2)*uncoef),
		      (long)(camera.posy - (h/2)*uncoef)};
	return res;
    }

    /**returns the location from which this portal's camera will see everything visible in the associated virtual space
     *@return the location to which the camera should go
     */
    public Location getGlobalView(){
	long[] wnes = cameraSpace.findFarmostGlyphCoords();
	long dx = (wnes[2]+wnes[0])/2;  //new coords where camera should go
	long dy = (wnes[1]+wnes[3])/2;
	long[] regBounds = getVisibleRegion();
	/*region that will be visible after translation, but before zoom/unzoom (need to
	  compute zoom) ; we only take left and down because we only need horizontal and
	  vertical ratios, which are equals for left and right, up and down*/
	long[] trRegBounds = {regBounds[0]+dx-camera.posx, regBounds[3]+dy-camera.posy};
	float currentAlt = camera.getAltitude()+camera.getFocal();
	float ratio = 0;
	//compute the mult factor for altitude to see all stuff on X
	if (trRegBounds[0]!=0){ratio = (dx-wnes[0])/((float)(dx-trRegBounds[0]));}
	//same for Y ; take the max of both
	if (trRegBounds[1]!=0){
	    float tmpRatio = (dy-wnes[3])/((float)(dy-trRegBounds[1]));
	    if (tmpRatio>ratio){ratio = tmpRatio;}
	}
	return new Location(dx, dy, currentAlt*Math.abs(ratio));
    }

    /**translates and (un)zooms this portal's camera in order to see everything visible in the associated virtual space
     *@param d duration of the animation in ms
     *@return the final camera location
     */
    public Location getGlobalView(int d, VirtualSpaceManager vsm){
	Location l = getGlobalView();
	float dAlt = l.alt - camera.getAltitude() - camera.getFocal();
	Vector prms=new Vector();
	prms.add(new Float(dAlt));prms.add(new LongPoint(l.vx-camera.posx, l.vy-camera.posy));
	vsm.animator.createCameraAnimation(d, AnimManager.CA_ALT_TRANS_SIG, prms, camera.getID());
	return l;
    }

    /** Position this portal's camera so that it seamlessly integrates with the surrounding context
     *@param c camera observing the context (associated with the View)
     */
    public Location getSeamlessView(Camera c){
	int hvw = c.getOwningView().getFrame().getWidth() / 2;
	int hvh = c.getOwningView().getFrame().getHeight() / 2;
	// get the region seen through the portal from the View's camera
	float uncoef = (float)((c.focal+c.altitude) / (float)c.focal);
	//XXX: FIXME works only when portal right under mouse cursor
	//     fix by taking the portal's visible region bounds in virtual space
	long[] wnes = {(long) (c.getOwningView().mouse.vx - w/2*uncoef),
		       (long) (c.getOwningView().mouse.vy + h/2*uncoef),
		       (long) (c.getOwningView().mouse.vx + w/2*uncoef),
		       (long) (c.getOwningView().mouse.vy - h/2*uncoef)};
	// compute the portal camera's new (x,y) coordinates and altitude
	return new Location((wnes[2]+wnes[0]) / 2, (wnes[1]+wnes[3]) / 2, camera.focal * ((wnes[2]-wnes[0])/((float)w)));
    }

    /**detects whether the given point is inside this portal or not 
     *@param cx horizontal cursor coordinate (JPanel)
     *@param cy vertical cursor coordinate (JPanel)
     */
    public boolean coordInside(int cx, int cy){
	return ((cx >= x) && (cx <= x+w) && 
		(cy >= y) && (cy <= y+h));
    }

    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setClip(x, y, w, h);
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
	g2d.setClip(0, 0, viewWidth, viewHeight);
	if (borderColor != null){
	    g2d.setColor(borderColor);
	    g2d.drawRect(x, y, w, h);
	}
    }

}
