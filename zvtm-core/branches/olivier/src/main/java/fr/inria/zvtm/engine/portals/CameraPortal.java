/*   FILE: CameraPortal.java
 *   DATE OF CREATION:  Sat Jun 17 07:19:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2014. All Rights Reserved
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

    // Camera used to render the portal
    Camera camera;
    // space owning camera (optimization)
    VirtualSpace cameraSpace;

    //list of Camera objects used in this portal
    Vector<Camera> cameras;
    Vector<VirtualSpace> cameraSpaces;
    Vector<Integer> camIndexs;

    // Region of virtual space seen through camera
    double viewWC, viewNC, viewEC, viewSC;

    // inverse of projection coef
    double duncoef;

    Vector drawnGlyphs;
    Glyph[] gll;
    // camera's index in parent virtual space
    int camIndex;

    // picking in camera portal
    Picker picker = new Picker();

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
        this(x, y, w, h, c, 1f);
    }

    /** Builds a new possibly translucent portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param c camera associated with the portal
     *@param a alpha channel value (translucency). alpha ranges between 0.0 (fully transparent) and 1.0 (fully opaque)
     */
    public CameraPortal(int x, int y, int w, int h, Camera c, float a){
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        updateDimensions();
        this.camera = c;
        this.cameraSpace = this.camera.getOwningSpace();
        this.camIndex = this.camera.getIndex();
        setTranslucencyValue(a);
        this.cameras = new Vector<Camera>();
        this.cameras.add(this.camera);
        this.cameraSpaces = new Vector<VirtualSpace>();
        this.cameraSpaces.add(this.cameraSpace);
        this.camIndexs = new Vector<Integer>();
        this.camIndexs.add(this.camIndex);

    }

   /** Builds a new portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param cvect vector of cameras associated with the portal
     */
    public CameraPortal(int x, int y, int w, int h, Vector<Camera> cvect){
        this(x, y, w, h, cvect, 1f);
    }
    /** Builds a new possibly translucent portal displaying what is seen through a camera
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param cvect camera associated with the portal
     *@param a alpha channel value (translucency). alpha ranges between 0.0 (fully transparent) and 1.0 (fully opaque)
     */
    public CameraPortal(int x, int y, int w, int h, Vector<Camera> cvect, float a){
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        updateDimensions();
        this.cameras = cvect;
        this.camera = cvect.elementAt(0);
        this.cameraSpace = this.camera.getOwningSpace();
        this.camIndex = this.camera.getIndex();
        this.cameraSpaces = new Vector<VirtualSpace>();
        this.camIndexs = new Vector<Integer>();
        for(Camera cam : this.cameras){
            cameraSpaces.add(cam.getOwningSpace());
            camIndexs.add(cam.getIndex());
        }
        setTranslucencyValue(a);
    }

    /** AlphaComposite used to paint glyph if not opaque. Set to null if glyph is opaque. */
    AlphaComposite alphaC;

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

    public Camera getCamera(){
        return camera;
    }

    public Vector<Camera> getCameras(){
        return cameras;
    }

    /** Get picker associated with this camera portal.*/
    public Picker getPicker(){
        return picker;
    }

    /**CALLED INTERNALLY - NOT FOR PUBLIC USE*/
    public void setOwningView(View v){
        super.setOwningView(v);
        camera.setOwningView(v);
        // vect FIXME !
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

     /** For internal use. Dot not tamper with. */
    protected BasicStroke stroke = null;
    /** For internal use. Dot not tamper with. */
    protected float halfBorderWidth = 0;
    /** For internal use. Dot not tamper with. */
    protected float borderWidth = 0;

    /** Set the border width of the portal (use SetBorder to draw the border)
     *@param w  width of the border
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

    /** Get the (unprojected) coordinates of point (jpx,jpy) in the virtual space to which the camera associated with this portal belongs.
     *@param cx cursor x-coordinate (JPanel coordinates system)
     *@param cy cursor y-coordinate (JPanel coordinates system)
     */
    public Point2D.Double getVSCoordinates(int cx, int cy){
        double uncoef = (camera.focal+camera.altitude) / camera.focal;
        return new Point2D.Double((camera.vx + (cx-x-w/2d)*uncoef), (camera.vy - (cy-y-h/2d)*uncoef));
    }

    /** Get bounds of rectangular region of the VirtualSpace seen through this camera portal.
     *@param res array which will contain the result
     *@return boundaries in VirtualSpace coordinates {west,north,east,south}
     */
    public double[] getVisibleRegion(double[] res){
        double uncoef = (camera.focal+camera.altitude) / camera.focal;
        res[0] = camera.vx - (w/2d)*uncoef;
        res[1] = camera.vy + (h/2d)*uncoef;
        res[2] = camera.vx + (w/2d)*uncoef;
        res[3] = camera.vy - (h/2d)*uncoef;
        return res;
    }

    /** Get bounds of rectangular region of the VirtualSpace seen through this camera portal.
     *@return boundaries in VirtualSpace coordinates {west,north,east,south}
     */
    public double[] getVisibleRegion(){
	    return getVisibleRegion(new double[4]);
    }

    /** Get the location from which this portal's camera will see all glyphs visible in the associated virtual space.
     *@return the location to which the camera should go
     */
    public Location getGlobalView(){
        double[] wnes = cameraSpace.findFarmostGlyphCoords();
        double dx = (wnes[2]+wnes[0])/2d;  //new coords where camera should go
        double dy = (wnes[1]+wnes[3])/2d;
        double[] regBounds = getVisibleRegion();
        /*region that will be visible after translation, but before zoom/unzoom (need to
        compute zoom) ; we only take left and down because we only need horizontal and
        vertical ratios, which are equals for left and right, up and down*/
        double[] trRegBounds = {regBounds[0]+dx-camera.vx, regBounds[3]+dy-camera.vy};
        double currentAlt = camera.getAltitude()+camera.getFocal();
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

    /** Translates and (un)zooms this portal's camera in order to see everything visible in the associated virtual space.
     *@param d duration of the animation in ms
     *@return the final camera location
     */
    public Location getGlobalView(int d){
        Location l = getGlobalView();
        Animation trans =
            VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory()
            .createCameraTranslation(d, camera, new Point2D.Double(l.vx, l.vy), false,
            IdentityInterpolator.getInstance(),
            null);
        Animation altAnim =
            VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory()
            .createCameraAltAnim(d, camera, l.alt, false,
            IdentityInterpolator.getInstance(),
            null);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(trans, false);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(altAnim, false);
        return l;
    }

    /** Position this portal's camera so that it seamlessly integrates with the surrounding context.
     *@param c camera observing the context (associated with the View containing the portal)
     *@return the final camera location
     */
    public Location getSeamlessView(Camera c){
        int hvw = c.getOwningView().getFrame().getWidth() / 2;
        int hvh = c.getOwningView().getFrame().getHeight() / 2;
        // get the region seen through the portal from the View's camera
        double uncoef = (c.focal+c.altitude) / (float)c.focal;
        //XXX: FIXME works only when portal right under mouse cursor
        //     fix by taking the portal's visible region bounds in virtual space
        double[] wnes = {(c.getOwningView().mouse.getVSXCoordinate() - w/2d*uncoef),
            c.getOwningView().mouse.getVSYCoordinate() + h/2d*uncoef,
            c.getOwningView().mouse.getVSXCoordinate() + w/2d*uncoef,
            c.getOwningView().mouse.getVSYCoordinate() - h/2d*uncoef};
        // compute the portal camera's new (x,y) coordinates and altitude
        return new Location((wnes[2]+wnes[0]) / 2d, (wnes[1]+wnes[3]) / 2d, camera.focal * ((wnes[2]-wnes[0])/w));
    }

	/** Get the location from which this portal's camera will focus on a specific rectangular region.
	 *@param x1 x coord of first point
	 *@param y1 y coord of first point
	 *@param x2 x coord of opposite point
	 *@param y2 y coord of opposite point
	 *@return the final camera location
	 */
    public Location centerOnRegion(double x1, double y1, double x2, double y2){
        double minX = Math.min(x1,x2);
		double minY = Math.min(y1,y2);
		double maxX = Math.max(x1,x2);
		double maxY = Math.max(y1,y2);
		double[] wnes = {minX,maxY,maxX,minY};  //wnes = west north east south
		double dx = (wnes[2]+wnes[0])/2d;  //new coords where camera should go
		double dy = (wnes[1]+wnes[3])/2d;
		double[] regBounds = getVisibleRegion();
		// region that will be visible after translation, but before zoom/unzoom  (need to compute zoom) ;
		// we only take left and down because we only need horizontal and vertical ratios, which are equals for left and right, up and down
		double[] trRegBounds = {regBounds[0]+dx-camera.vx, regBounds[3]+dy-camera.vy};
		double currentAlt = camera.getAltitude() + camera.getFocal();
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
        Location l = centerOnRegion(x1, y1, x2, y2);
		Animation trans =
		    VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
		    createCameraTranslation(d, camera, l.getPosition(), false,
					    SlowInSlowOutInterpolator.getInstance(),
					    null);
		Animation altAnim =
		    VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().
		    createCameraAltAnim(d, camera, l.getAltitude(), false,
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
        standardStroke = g2d.getStroke();
        // be sure to call the translate instruction before getting the standard transform
        // as the latter's matrix is preconcatenated to the translation matrix of glyphs
        // that use AffineTransforms for translation
        standardTransform = g2d.getTransform();
        for (int u=0; u < cameras.size(); u++){
            Camera cam = cameras.elementAt(u);
            VirtualSpace spa = cameraSpaces.elementAt(u);
            int idx = camIndexs.elementAt(u);
            drawnGlyphs = spa.getDrawnGlyphs(idx);
            //synchronized(drawnGlyphs){
                drawnGlyphs.removeAllElements();
                duncoef = (cam.focal+cam.altitude) / cam.focal;
                //compute region seen from this view through camera
                viewWC = cam.vx - (w/2d) * duncoef;
                viewNC = cam.vy + (h/2d) * duncoef;
                viewEC = cam.vx + (w/2d) * duncoef;
                viewSC = cam.vy - (h/2d) * duncoef;
                //gll = cameraSpace.getDrawingList();
                gll = spa.getDrawingList();
                for (int i=0;i<gll.length;i++){
                    if (gll[i] != null){
                        //synchronized(gll[i]){
                            if (gll[i].visibleInViewport(viewWC, viewNC, viewEC, viewSC, cam)){
                                //if glyph is at least partially visible in the reg. seen from this view, display
                                gll[i].project(cam, size); // an invisible glyph should still be projected
                                if (gll[i].isVisible()){      // as it can be sensitive
                                    gll[i].draw(g2d, w, h, idx, standardStroke, standardTransform, x, y);
                                }
                                spa.drewGlyph(gll[i], idx);
                            }
                        //}
                    }
                }
            //}
        }
        g2d.setClip(0, 0, viewWidth, viewHeight);
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
        double uncoef = (camera.focal+camera.altitude) / camera.focal;
        double pvx = (camera.vx + (cx-x-w/2d)*uncoef);
        double pvy = (camera.vy - (cy-y-h/2d)*uncoef);
        picker.setVSCoordinates(pvx, pvy);
        picker.computePickedGlyphList(camera);
    }

}
