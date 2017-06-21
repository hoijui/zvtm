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

import java.util.Vector;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.AlphaComposite;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Picker;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.glyphs.VText;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

/**A portal showing what is seen through a camera. Shape: rectangular.
   The Camera should not be used in any other View or Portal.*/

public class CameraPortal extends Portal {

    /** Double Buffering uses a BufferedImage as the back buffer. */
    BufferedImage backBuffer = null;
    int backBufferW = 0;
    int backBufferH = 0;
    int backBufferTX = 0;
    int backBufferTY = 0;
    Graphics2D backBufferGraphics = null;
    Graphics2D stableRefToBackBufferGraphics;
    GraphicsConfiguration gconf;
    Dimension oldSize = new Dimension(0,0);

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
    protected int borderWidthXYOff = 0;
    protected int borderWidthWHOff = 0;

    /** AlphaComposite used to paint glyph if not opaque. Set to null if glyph is opaque. */
    AlphaComposite alphaC;

    // Cameras used to render the portal
    Camera[] cameras;
    // space owning camera (optimization)
    VirtualSpace[] cameraSpaces;
    // camera indexes  (optimization)
    Integer[] cameraIndexes;

    // picking in camera portal
    Picker picker = new Picker();

    /*Graphics2d's original stroke. Passed to each glyph in case it needs to modifiy the stroke when painting itself*/
    Stroke standardStroke;

    /*Graphics2d's original affine transform. Passed to each glyph in case it needs to modifiy the affine transform when painting itself*/
    AffineTransform standardTransform;

    // utils when painting 
    double duncoef;
    Vector drawnGlyphs;
    Glyph[] gll;
    double viewWC, viewNC, viewEC, viewSC;

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

    /** Builds a portal displaying what is seen through a set of cameras
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param cams cameras vector associated with the portal
     */
    public CameraPortal(int x, int y, int w, int h, Vector<Camera> cams){
        this (x, y, w, h, cams.toArray(new Camera[cams.size()]), 1);
    }
    /** Builds a new possibly translucent portal displaying what is seen through a set of cameras
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param cams cameras vector associated with the portal
     *@param a alpha channel value (translucency). alpha ranges between 0.0 (fully transparent) and 1.0 (fully opaque)
     */
    public CameraPortal(int x, int y, int w, int h, Vector<Camera> cams, float a){
        this (x, y, w, h, cams.toArray(new Camera[cams.size()]), a);
    }

    /** Builds a new possibly translucent portal displaying what is seen through a set of cameras
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param cams cameras array associated with the portal
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
        this.cameraIndexes = new Integer[this.cameras.length];
        for (int i=0;i<cameras.length;i++){
            this.cameraSpaces[i] = this.cameras[i].getOwningSpace();
            this.cameraIndexes[i] = this.cameras[i].getIndex();
        }

        // need to duplicate
        this.usedCameras = new Vector<Camera>();
        for(Camera tc : cameras) { this.usedCameras.add(tc); }
        this.usedSpaces = new Vector<VirtualSpace>();
        for(VirtualSpace vs : cameraSpaces) { this.usedSpaces.add(vs); }
    
        setTranslucencyValue(a);
        setBorderWidth(1);
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
        repaint(true);
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
        if (v!=null) antialias = v.getAntialiasing();
    }

    /**Draw a border delimiting the portal.
     *@param bc color of the portal's border (pass null if none)*/
    public void setBorder(Color bc){
        this.borderColor = bc;
        repaint(true);
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
            borderWidthXYOff = 0;
            borderWidthWHOff = 1; 
        }
        else{
            stroke = new BasicStroke(bw);
            borderWidth = bw;
            halfBorderWidth = bw/2;
            borderWidthXYOff = (int)Math.floor(bw/2f);
            borderWidthWHOff =  2*borderWidthXYOff + (int)Math.ceil((bw/2f)-borderWidthXYOff);
        }
        repaint(true);
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
        repaint(true);
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

    // -----------------------------------------------------------------
    // painting

    protected double maxBufferWidthRatio = 1;
    protected double maxBufferHeightRatio = 1;
    public void setMaxBufferSizeRatios(double wr, double hr){
        if (wr < 1) { wr = 1; }
        if (hr < 1) { hr = 1; }
        maxBufferWidthRatio = wr;
        maxBufferHeightRatio = hr;
    }

    protected void updateOffscreenBuffer(int viewWidth, int viewHeight){
        // limit the buffer size to the size of the view... we never need more
        // do not do an intersection because we do not want to update the size to ofthen !!!
        buffx = x;
        buffy = y;
        int bw = w;
        int bh = h;
        if (w > viewWidth*maxBufferWidthRatio){
            bw = (int)Math.ceil(viewWidth*maxBufferWidthRatio);
            if (x < 0){
                buffx = w + x - bw;
                if (buffx > 0){
                    buffx = 0;
                }
            }
        }
        if (h > viewHeight*maxBufferHeightRatio){
            bh = (int)Math.ceil(viewHeight*maxBufferHeightRatio);
            if (y < 0){
                buffy = h + y - bh;
                if (buffy > 0){
                    buffy = 0;
                }
            }
        }
        int btx = (x - buffx);
        int bty = (y - buffy);
        if(btx !=  backBufferTX || bty != backBufferTY){
            backBufferTX = btx;
            backBufferTY = bty;
            repaintASAP=true;
        }
        boolean updateAntialias = false;
        if (bw != oldSize.width || bh != oldSize.height || backBufferW != bw || backBufferH != bh) {
            //System.out.println("UPDATE PORTAL BACK BUFFER");
            backBuffer = null;
            if (backBufferGraphics != null) {
                backBufferGraphics.dispose();
                backBufferGraphics = null;
            }
            oldSize.width = bw;
            oldSize.height = bh;
            updateAntialias=true;
            updateFont=true;
            repaintASAP=true;
        }
        if (backBuffer == null){
            gconf = owningView.getPanel().getComponent().getGraphicsConfiguration();
            // assign minimal size of 1
            backBuffer = gconf.createCompatibleImage((bw > 0) ? bw: 1, (bh > 0) ? bh : 1, BufferedImage.TYPE_INT_ARGB);
            backBufferW = backBuffer.getWidth();
            backBufferH = backBuffer.getHeight();
            if (backBufferGraphics != null){
                backBufferGraphics.dispose();
                backBufferGraphics = null;
            }
            repaintASAP=true;
            //System.out.println(
            //    "UPDATE PORTAL BACK BUFFER "+ w+" "+h+" "+ backBufferW+" "+backBufferH);
        }
        if (backBufferGraphics == null) {
            backBufferGraphics = backBuffer.createGraphics();
            updateAntialias=true;
            updateFont=true;
            repaintASAP=true;
        }
        if (updateFont){
            backBufferGraphics.setFont(VText.getMainFont());
            updateFont = false;
            repaintASAP=true;
        }
        if (owningView!=null && antialias != owningView.getAntialiasing()) { updateAntialias=true; }
        if (updateAntialias){
            //System.out.println(
            //    "UPDATE PORTAL  ANTIALIAS "+ antialias+" "+owningView.getAntialiasing());
            if (antialias){
                backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            else {
                backBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
            antialias = owningView.getAntialiasing();
            repaintASAP=true;
        }
        stableRefToBackBufferGraphics = backBufferGraphics;
    }

    protected boolean bufferDraw = false;
    protected boolean wasOutOfView = true;

    @Override
    public BufferedImage getBufferImage(){
        return backBuffer;
    }

    @Override
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
        bufferDraw = owningView.getDrawPortalOffScreen();
        if (bufferDraw){
            //System.out.println("draw portal.... ? "+ repaintASAP);
            updateOffscreenBuffer(viewWidth, viewHeight);
            if (wasOutOfView &&
                !(x+w < 0 || y+h < 0 || x >= viewWidth || y >= viewHeight)){
                repaintASAP=true;
                wasOutOfView=false;
            }
            if (repaintASAP){
                repaintASAP=false;
                // System.out.println("draw portal.... "+x+" "+y+" "+w+" "+h+" "+buffx+" "+backBufferW);
                try {
                    paintOnBack(stableRefToBackBufferGraphics, viewWidth, viewHeight, x, y);
                }
                catch (NullPointerException ex0){
                    //ex0.printStackTrace(); 
                }
            }
        }
        else{
            paintOnBack(g2d, viewWidth, viewHeight, 0, 0);
        }
    }

    protected void paintOnBack(Graphics2D g2d, int viewWidth, int viewHeight, int tx, int ty){
        if (!visible){return;}
        //Check if the portal is out of the view
        if (x+w < 0 || y+h < 0 || x >= viewWidth || y >= viewHeight){
            // clear the buffer ???
            wasOutOfView = true;
            return;
        }
        wasOutOfView=false;
        int bw = w;
        int bh = h;
        if (bufferDraw){
            bw = backBufferW;
            bh = backBufferH;
        }
        if (alphaC != null){
            // portal is not opaque
            if (alphaC.getAlpha() == 0){
                // portal is totally transparent
                if (bufferDraw){
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
                    g2d.fillRect(0, 0, w, h);
                    g2d.setComposite(Translucent.acO);
                }
                return;
            }
            if (!bufferDraw){
                g2d.setComposite(alphaC);
            }
        }
         
        g2d.setClip(x-tx, y-ty, bw, bh);  // rounded     g2d.setClip(clippingShape);
        doSetClip(g2d, bw, bh, tx, ty);
        if (bufferDraw){
            if (bkgColor == null){
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
                g2d.fillRect(0, 0, bw, bh);
                g2d.setComposite(Translucent.acO);
            }
        }
        if (bkgColor != null){
            g2d.setColor(bkgColor);
            g2d.fillRect(x-tx, y-ty, bw, bh);
        }
        standardStroke = g2d.getStroke();
        // be sure to call the translate instruction before getting the standard transform
        // as the latter's matrix is preconcatenated to the translation matrix of glyphs
        // that use AffineTransforms for translation
        standardTransform = g2d.getTransform();
        for (int u=0; u < cameras.length; u++){
            Camera cam = cameras[u];
            VirtualSpace spa = cameraSpaces[u];
            int idx = cameraIndexes[u];
            drawnGlyphs = spa.getDrawnGlyphs(idx);
            synchronized(drawnGlyphs){
                drawnGlyphs.removeAllElements();
                duncoef = (cam.focal+cam.altitude) / cam.focal;
                //compute region seen from this view through camera
                viewWC = cam.vx - (w/2d) * duncoef - (double)(backBufferTX)* duncoef ;
                viewNC = cam.vy + (h/2d) * duncoef + (double)(backBufferTY)* duncoef;
                viewEC =  viewWC + (bw/1d)  * duncoef;
                viewSC = viewNC - (bh/1d) * duncoef;
                gll = spa.getDrawingList();
                for (int i=0;i<gll.length;i++){
                    if (gll[i] != null){
                        synchronized(gll[i]){
                            if (gll[i].visibleInViewport(viewWC, viewNC, viewEC, viewSC, cam)){
                                //if glyph is at least partially visible in the reg. seen from this view, display
                                gll[i].project(cam, size); // an invisible glyph should still be projected
                                if (gll[i].isVisible()){      // as it can be sensitive
                                    gll[i].draw(g2d, bw, bh, idx, standardStroke, standardTransform,
                                        x-tx+backBufferTX, y-ty+backBufferTY);
                                }
                                spa.drewGlyph(gll[i], idx);
                            }
                        }
                    }
                }
            }
        }
        paintPortalFrame(g2d, viewWidth, viewHeight, tx, ty);
    }

    void doSetClip(Graphics2D g2d, int bw, int bh, int tx, int ty){
        g2d.setClip(x-tx, y-ty, bw, bh); 
    }

    void paintPortalFrame(Graphics2D g2d, int viewWidth, int viewHeight, int tx, int ty){
        if (borderColor != null){
            g2d.setColor(borderColor);
            if (stroke != null){
                g2d.setStroke(stroke);
            }
            g2d.drawRect(x-tx+backBufferTX+borderWidthXYOff, y-ty+backBufferTY+borderWidthXYOff,
                w-borderWidthWHOff, h-borderWidthWHOff);
            g2d.setStroke(standardStroke);
        }
        g2d.setClip(0, 0, viewWidth, viewHeight);
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
