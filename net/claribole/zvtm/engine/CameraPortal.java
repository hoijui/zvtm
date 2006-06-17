/*   FILE: Portal.java
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
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.glyphs.Glyph;

/**A portal showing what is seen through a camera.
   The Camera should not be used in any other View or Portal.*/

public class CameraPortal extends Portal {

    /**Draw a border delimiting the portal.*/
    Color borderColor;

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
	this.d = new Dimension(w, h);
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

    /**returns bounds of rectangle representing virtual space's region seen through camera c [west,north,east,south]*/
    public long[] getVisibleRegion(){
	float uncoef = (float)((camera.focal+camera.altitude) / camera.focal);
	long[] res = {(long)(camera.posx - (d.width/2)*uncoef),
		      (long)(camera.posy + (d.height/2)*uncoef),
		      (long)(camera.posx + (d.width/2)*uncoef),
		      (long)(camera.posy - (d.height/2)*uncoef)};
	return res;
    }

    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	g2d.setClip(x,y,d.width,d.height);
	standardStroke = g2d.getStroke();
	standardTransform = g2d.getTransform();
	drawnGlyphs = cameraSpace.getDrawnGlyphs(camIndex);
	synchronized(drawnGlyphs){
	    drawnGlyphs.removeAllElements();
	    uncoef = (float)((camera.focal+camera.altitude) / camera.focal);
	    //compute region seen from this view through camera
	    viewWC = (long)(camera.posx - (d.width/2)*uncoef);
	    viewNC = (long)(camera.posy + (d.height/2)*uncoef);
	    viewEC = (long)(camera.posx + (d.width/2)*uncoef);
	    viewSC = (long)(camera.posy - (d.height/2)*uncoef);
	    gll = cameraSpace.getVisibleGlyphList();
	    for (int i=0;i<gll.length;i++){
		if (gll[i] != null){
		    synchronized(gll[i]){
			if (gll[i].visibleInRegion(viewWC, viewNC, viewEC, viewSC, camIndex)){
			    //if glyph is at least partially visible in the reg. seen from this view, display
			    gll[i].project(camera, d); // an invisible glyph should still be projected
			    if (gll[i].isVisible()){      // as it can be sensitive
				gll[i].draw(g2d, d.width, d.height, camIndex, standardStroke, standardTransform);
			    }
			}
		    }
		}
	    }
	}
	g2d.setClip(0, 0, viewWidth, viewHeight);
	if (borderColor != null){
	    g2d.setColor(borderColor);
	    g2d.drawRect(x, y, d.width, d.height);
	}
    }

}
