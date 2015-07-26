/*   FILE: CameraPortal.java
 *   DATE OF CREATION:  Sat Jun 17 07:19:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.engine.portals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.AlphaComposite;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Picker;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.Translucent;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

/**A portal showing what is seen through a camera. Shape: rectangular.
   The Camera should not be used in any other View or Portal.*/

public class CameraPortal extends Portal {

    /**Draw a border delimiting the portal (null if no border).*/
    Color borderColor;
    /**Portal's background fill color (null if transparent).*/
    Color bkgColor;
    /** For internal use. Dot not tamper with. */
    protected BasicStroke stroke = null;
    /** For internal use. Dot not tamper with. */
    protected float halfBorderWidth = 0;
    /** For internal use. Dot not tamper with. */
    protected float borderWidth = 0;
    /** AlphaComposite used to paint glyph if not opaque. Set to null if glyph is opaque. */
    AlphaComposite alphaC;

    // Cameras used to render the portal
    Camera[] cameras;
    // space owning camera (optimization)
    VirtualSpace[] cameraSpaces;

    // picking in camera portal
    Picker picker = new Picker();

    /** Builds a new portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param cam camera associated with the portal
     */
    public CameraPortal(int x, int y, int w, int h, Camera cam){
        this(x, y, w, h, new Camera[]{cam}, 1f);
    }

    /** Builds a new possibly translucent portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param cam camera associated with the portal
     *@param a alpha channel value (translucency). alpha ranges between 0.0 (fully transparent) and 1.0 (fully opaque)
     */
    public CameraPortal(int x, int y, int w, int h, Camera cam, float a){
        this(x, y, w, h, new Camera[]{cam}, a);
    }

    /** Builds a new possibly translucent portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param cams cameras associated with the portal
     *@param a alpha channel value (translucency). alpha ranges between 0.0 (fully transparent) and 1.0 (fully opaque)
     */
    public CameraPortal(int x, int y, int w, int h, Camera[] cams, float a){
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        updateDimensions();
        this.cameras = cams;
        this.cameraSpaces = new VirtualSpace[this.cameras.length];
        for (int i=0;i<cameras.length;i++){
            this.cameraSpaces[i] = this.cameras[i].getOwningSpace();
        }
        setTranslucencyValue(a);
    }

    /**
     * Set alpha channel value (translucency).
     *@param alpha in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public void setTranslucencyValue(float alpha){
        if (alpha == 1.0f){
            alphaC = null;
        }
        else {
            alphaC = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        }
        VirtualSpaceManager.INSTANCE.repaint();
    }

    /** Get alpha channel value (translucency).
     *@return a value in [0;1.0]. 0 is fully transparent, 1 is opaque
     */
    public float getTranslucencyValue(){
        return (alphaC != null) ? alphaC.getAlpha() : 1.0f;
    }

    /** Get the main camera (at index 0). */
    public Camera getCamera(){
        return cameras[0];
    }

    /** Get all cameras through which this portal is observing. */
    public Camera[] getCameras(){
        return cameras;
    }

    /** Get picker associated with this camera portal.*/
    public Picker getPicker(){
        return picker;
    }

    /**CALLED INTERNALLY - NOT FOR PUBLIC USE*/
    public void setOwningView(View v){
        super.setOwningView(v);
        for (Camera c:cameras){
            c.setOwningView(v);
        }
    }

    /**Draw a border delimiting the portal.
     *@param bc color of the portal's border (pass null if none)*/
    public void setBorder(Color bc){
        this.borderColor = bc;
    }

    /**Get the color used to draw the border delimiting this portal.
     *@return color of the portal's border (null if none)*/
    public Color getBorder(){
	    return borderColor;
    }

    /** Set the border width of the portal (use SetBorder to draw the border)
     *@param bw portal border width
     */
    public void setBorderWidth(float bw){
        if (bw <= 0.0f){
            stroke=null;
            halfBorderWidth = 0.5f; borderWidth = 1.0f;
        }
        else{
            stroke = new BasicStroke(bw);
            borderWidth = bw;
            halfBorderWidth = bw/2;
        }
    }

    /** get the border width of the portal
     *@return null if default 1px-thick solid stroke
     */
    public float getBorderWidth(){
        return borderWidth;
    }

    /**Fill background with a color.
     *@param bc color of the border (pass null if none)*/
    public void setBackgroundColor(Color bc){
	    this.bkgColor = bc;
    }

    /**Get the color used to fill the background.
     *@return color of the border (null if none)*/
    public Color getBackgroundColor(){
	    return bkgColor;
    }

    /** Get the (unprojected) coordinates of point (jpx,jpy) in the virtual space
        to which the first camera associated with this portal belongs.
     *@param cx cursor x-coordinate (JPanel coordinates system)
     *@param cy cursor y-coordinate (JPanel coordinates system)
     */
    public Point2D.Double getVSCoordinates(int cx, int cy){
        return getVSCoordinates(cx, cy, cameras[0]);
    }

    /** Get the (unprojected) coordinates of point (jpx,jpy) in the virtual space
        to which the indicated camera associated with this portal belongs.
     *@param cx cursor x-coordinate (JPanel coordinates system)
     *@param cy cursor y-coordinate (JPanel coordinates system)
     *@param c camera to be considered
     */
    public Point2D.Double getVSCoordinates(int cx, int cy, Camera c){
        double uncoef = (c.focal+c.altitude) / c.focal;
        return new Point2D.Double((c.vx + (cx-x-w/2d)*uncoef), (c.vy - (cy-y-h/2d)*uncoef));
    }

    /** Get bounds of rectangular region of the VirtualSpace seen through this camera portal.
     *@param res array which will contain the result
     *@param c camera to be considered
     *@return boundaries in VirtualSpace coordinates {west,north,east,south}
     */
    public double[] getVisibleRegion(double[] res, Camera c){
        double uncoef = (c.focal+c.altitude) / c.focal;
        res[0] = c.vx - (w/2d)*uncoef;
        res[1] = c.vy + (h/2d)*uncoef;
        res[2] = c.vx + (w/2d)*uncoef;
        res[3] = c.vy - (h/2d)*uncoef;
        return res;
    }

    /** Get bounds of rectangular region of the VirtualSpace seen through this camera portal.
     *@param res array which will contain the result
     *@return boundaries in VirtualSpace coordinates {west,north,east,south}
     */
    public double[] getVisibleRegion(double[] res){
        return getVisibleRegion(res, cameras[0]);
    }

    /** Get bounds of rectangular region of the VirtualSpace seen through this camera portal.
     *@return boundaries in VirtualSpace coordinates {west,north,east,south}
     */
    public double[] getVisibleRegion(){
	    return getVisibleRegion(new double[4], cameras[0]);
    }

    /** Get the location from which a Camera in this Portal will see all glyphs visible in the associated virtual space.
     *@return the location to which the camera should go
     */
    public Location getGlobalView(Camera c){
        double[] wnes = c.getOwningSpace().findFarmostGlyphCoords();
        double dx = (wnes[2]+wnes[0])/2d;  //new coords where camera should go
        double dy = (wnes[1]+wnes[3])/2d;
        double[] regBounds = getVisibleRegion(new double[4], c);
        /*region that will be visible after translation, but before zoom/unzoom (need to
        compute zoom) ; we only take left and down because we only need horizontal and
        vertical ratios, which are equals for left and right, up and down*/
        double[] trRegBounds = {regBounds[0]+dx-c.vx, regBounds[3]+dy-c.vy};
        double currentAlt = c.getAltitude()+c.getFocal();
        double ratio = 0;
        //compute the mult factor for altitude to see all stuff on X
        if (trRegBounds[0]!=0){ratio = (dx-wnes[0])/((dx-trRegBounds[0]));}
        //same for Y ; take the max of both
        if (trRegBounds[1]!=0){
            double tmpRatio = (dy-wnes[3])/((dy-trRegBounds[1]));
            if (tmpRatio>ratio){ratio = tmpRatio;}
        }
        return new Location(dx, dy, currentAlt*Math.abs(ratio));
    }

    /** Get the location from which the Camera in this Portal will see all glyphs visible in the associated virtual space.
     *@return the location to which the camera should go
     */
    public Location getGlobalView(){
        return getGlobalView(cameras[0]);
    }

    /** Translates and (un)zooms this portal's camera in order to see everything visible in the associated virtual space.
     *@param d duration of the animation in ms
     *@return the final camera location
     */
    public Location getGlobalView(int d){
        return getGlobalView(d, cameras[0]);
    }

    /** Translates and (un)zooms this portal's camera in order to see everything visible in the associated virtual space.
     *@param d duration of the animation in ms
     *@return the final camera location
     */
    public Location getGlobalView(int d, Camera c){
        Location l = getGlobalView(c);
        Animation trans =
            VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory()
            .createCameraTranslation(d, c, new Point2D.Double(l.vx, l.vy), false,
            IdentityInterpolator.getInstance(),
            null);
        Animation altAnim =
            VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory()
            .createCameraAltAnim(d, c, l.alt, false,
            IdentityInterpolator.getInstance(),
            null);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(altAnim, false);
        return l;
    }

    /** Position one of this portal's cameras so that it seamlessly integrates with the surrounding context.
     *@param contextCam camera observing the context (associated with the View containing the portal)
     *@return the final location of portalCam
     */
    public Location getSeamlessView(Camera contextCam){
        int hvw = contextCam.getOwningView().getFrame().getWidth() / 2;
        int hvh = contextCam.getOwningView().getFrame().getHeight() / 2;
        // get the region seen through the portal from the View's camera
        double uncoef = (contextCam.focal+contextCam.altitude) / (float)contextCam.focal;
        //XXX: FIXME works only when portal right under mouse cursor
        //     fix by taking the portal's visible region bounds in virtual space
        double[] wnes = {(contextCam.getOwningView().mouse.getVSXCoordinate() - w/2d*uncoef),
            contextCam.getOwningView().mouse.getVSYCoordinate() + h/2d*uncoef,
            contextCam.getOwningView().mouse.getVSXCoordinate() + w/2d*uncoef,
            contextCam.getOwningView().mouse.getVSYCoordinate() - h/2d*uncoef};
        // compute the portal camera's new (x,y) coordinates and altitude
        return new Location((wnes[2]+wnes[0]) / 2d, (wnes[1]+wnes[3]) / 2d, contextCam.focal * ((wnes[2]-wnes[0])/w));
    }

    /** Get the location from which this portal's camera will focus on a specific rectangular region.
	 *@param x1 x coord of first point
	 *@param y1 y coord of first point
	 *@param x2 x coord of opposite point
	 *@param y2 y coord of opposite point
	 *@return the final camera location
	 */
    public Location centerOnRegion(double x1, double y1, double x2, double y2){
        return centerOnRegion(x1, y1, x2, y2, cameras[0]);
    }

	/** Get the location from which this portal's camera will focus on a specific rectangular region.
	 *@param x1 x coord of first point
	 *@param y1 y coord of first point
	 *@param x2 x coord of opposite point
	 *@param y2 y coord of opposite point
     *@param c camera to be considered
	 *@return the final camera location
	 */
    public Location centerOnRegion(double x1, double y1, double x2, double y2, Camera c){
        double minX = Math.min(x1,x2);
		double minY = Math.min(y1,y2);
		double maxX = Math.max(x1,x2);
		double maxY = Math.max(y1,y2);
		double[] wnes = {minX,maxY,maxX,minY};  //wnes = west north east south
		double dx = (wnes[2]+wnes[0])/2d;  //new coords where camera should go
		double dy = (wnes[1]+wnes[3])/2d;
		double[] regBounds = getVisibleRegion(new double[4], c);
		// region that will be visible after translation, but before zoom/unzoom  (need to compute zoom) ;
		// we only take left and down because we only need horizontal and vertical ratios, which are equals for left and right, up and down
		double[] trRegBounds = {regBounds[0]+dx-c.vx, regBounds[3]+dy-c.vy};
		double currentAlt = c.getAltitude() + c.getFocal();
		double ratio = 0;
		//compute the mult factor for altitude to see all stuff on X
		if (trRegBounds[0] != 0){ratio = (dx-wnes[0]) / (dx-trRegBounds[0]);}
		//same for Y ; take the max of both
		if (trRegBounds[1] != 0){
			double tmpRatio = (dy-wnes[3]) / (dy-trRegBounds[1]);
			if (tmpRatio > ratio){ratio = tmpRatio;}
		}
		double newAlt = currentAlt * Math.abs(ratio);
		return new Location(dx, dy, newAlt);
    }

    /** Translates and (un)zooms this portal's camera in order to focus on a specific rectangular region
	 *@param d duration of the animation in ms (pass 0 to go there instantanesouly)
	 *@param x1 x coord of first point
	 *@param y1 y coord of first point
	 *@param x2 x coord of opposite point
	 *@param y2 y coord of opposite point
	 *@return the final camera location
	 */
    public Location centerOnRegion(int d, double x1, double y1, double x2, double y2){
        return centerOnRegion(d, x1, y1, x2, y2, cameras[0]);
    }

	/** Translates and (un)zooms this portal's camera in order to focus on a specific rectangular region
	 *@param d duration of the animation in ms (pass 0 to go there instantanesouly)
	 *@param x1 x coord of first point
	 *@param y1 y coord of first point
	 *@param x2 x coord of opposite point
	 *@param y2 y coord of opposite point
	 *@return the final camera location
	 */
    public Location centerOnRegion(int d, double x1, double y1, double x2, double y2, Camera c){
        Location l = centerOnRegion(x1, y1, x2, y2, c);
		Animation trans =
		    VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
		    createCameraTranslation(d, c, l.getPosition(), false,
					    SlowInSlowOutInterpolator.getInstance(),
					    null);
		Animation altAnim =
		    VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
		    createCameraAltAnim(d, c, l.getAltitude(), false,
					SlowInSlowOutInterpolator.getInstance(),
					null);
		VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);
		VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(altAnim, false);
		return l;
    }

    @Override
    public boolean coordInside(int cx, int cy){
	   //return ((cx >= x) && (cx <= x+w) && (cy >= y) && (cy <= y+h));
        return ((cx >= x-halfBorderWidth) && (cx <= x+w+halfBorderWidth) &&
            (cy >= y-halfBorderWidth) && (cy <= y+h+halfBorderWidth));
    }

    public boolean coordInsideBorder(int cx, int cy){
    return (((cx >= x-halfBorderWidth) && (cx <= x+halfBorderWidth) &&
             (cy >= y-halfBorderWidth) && (cy <= y+h+halfBorderWidth)) ||
            ((cx >= x-halfBorderWidth) && (cx <= x+w+halfBorderWidth) &&
             (cy >= y-halfBorderWidth) && (cy <= y+halfBorderWidth)) ||
            ((cx >= x+w-halfBorderWidth) && (cx <= x+w+halfBorderWidth) &&
             (cy >= y-halfBorderWidth) && (cy <= y+h+halfBorderWidth)) ||
            ((cx >= x-halfBorderWidth) && (cx <= x+w+halfBorderWidth) &&
             (cy >= y+h-halfBorderWidth) && (cy <= y+h+halfBorderWidth)));
    }

    @Override
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
        if (!visible){return;}
        //Check if the portal is out of the view
        if (x+w+halfBorderWidth < 0 || y+h+halfBorderWidth < 0 ||
            x-halfBorderWidth >= viewWidth || y-halfBorderWidth >= viewHeight){
            return;
        }
        if (alphaC != null){
            // portal is not opaque
            if (alphaC.getAlpha() == 0){
                // portal is totally transparent
                return;
            }
            g2d.setComposite(alphaC);
        }
        g2d.setClip(x, y, w, h);
        if (bkgColor != null){
            g2d.setColor(bkgColor);
            g2d.fillRect(x, y, w, h);
        }
        /*Graphics2d's original stroke. Passed to each glyph in case it needs to modifiy the stroke when painting itself*/
        Stroke standardStroke = g2d.getStroke();
        /*Graphics2d's original affine transform. Passed to each glyph in case it needs to modifiy the affine transform when painting itself*/
        AffineTransform standardTransform= g2d.getTransform();
        // be sure to call the translate instruction before getting the standard transform
        // as the latter's matrix is preconcatenated to the translation matrix of glyphs
        // that use AffineTransforms for translation
        double[] wnes;
        double duncoef;
        for (int j=0;j<cameras.length;j++){
            Camera c = cameras[j];
            VirtualSpace vs = cameraSpaces[j];
            Vector<Glyph> drawnGlyphs = vs.getDrawnGlyphs(c.getIndex());
            synchronized(drawnGlyphs){
                drawnGlyphs.removeAllElements();
                duncoef = (c.focal+c.altitude) / c.focal;
                //compute region seen from this view through camera
                wnes = new double[]{c.vx - (w/2d) * duncoef,
                                    c.vy + (h/2d) * duncoef,
                                    c.vx + (w/2d) * duncoef,
                                    c.vy - (h/2d) * duncoef};
                Glyph[] gll = vs.getDrawingList();
                for (int i=0;i<gll.length;i++){
                    if (gll[i] != null){
                        synchronized(gll[i]){
                            if (gll[i].visibleInViewport(wnes[0], wnes[1], wnes[2], wnes[3], c)){
                                //if glyph is at least partially visible in the reg. seen from this view, display
                                gll[i].project(c, size); // an invisible glyph should still be projected
                                if (gll[i].isVisible()){      // as it can be sensitive
                                    gll[i].draw(g2d, w, h, c.getIndex(), standardStroke, standardTransform, x, y);
                                }
                                vs.drewGlyph(gll[i], c.getIndex());
                            }
                        }
                    }
                }
            }
        }
        g2d.setClip(0, 0, viewWidth, viewHeight);
        paintPortalFrame(g2d, standardStroke);
    }

    void paintPortalFrame(Graphics2D g2d, Stroke standardStroke){
        if (borderColor != null){
            g2d.setColor(borderColor);
            if (stroke != null){
                g2d.setStroke(stroke);
            }
            g2d.drawRect(x, y, w, h);
            g2d.setStroke(standardStroke);
        }
        if (alphaC != null){
            g2d.setComposite(Translucent.acO);
        }
    }

    public void pick(int cx, int cy){
        picker.setJPanelCoordinates(cx-x, cy-y);
        double uncoef = (cameras[0].focal+cameras[0].altitude) / cameras[0].focal;
        double pvx = (cameras[0].vx + (cx-x-w/2d)*uncoef);
        double pvy = (cameras[0].vy - (cy-y-h/2d)*uncoef);
        picker.setVSCoordinates(pvx, pvy);
        picker.computePickedGlyphList(cameras[0]);
    }

}
