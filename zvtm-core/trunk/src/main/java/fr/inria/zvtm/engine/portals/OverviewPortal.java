/*   FILE: OverviewPortal.java
 *   DATE OF CREATION:  Sat Jun 17 07:19:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.engine.portals;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.event.ObservedRegionListener;

/**A portal showing what is seen through a camera that serves as an overview. Shape: rectangular.
   The Camera should not be used in any other View or Portal.*/

public class OverviewPortal extends CameraPortal {

    Camera[] observedRegionCameras;
    View[] observedRegionViews;
    double[][] observedRegions;
    double[] orcoefs;


    Color observedRegionColor = Color.GREEN;
    Color[] observedRegionColors;

    /** For translucency of the rectangle representing the region observed through the main viewport (default is 0.5)*/
    AlphaComposite acST;
    float orBorderWidth = 1;
    float orHalfBorderWidth = 0.5f;
    BasicStroke orStroke = null;

    /** Alpha channel value. */
    float alpha = 0.5f;

    Timer borderTimer;

    boolean drawObservedRegionLocator = false;

    /** Builds a new portal displaying what is seen through a camera and the region seen through another camera.
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param pc camera associated with the portal (provinding the overview)
     *@param orc camera observing a region; this region is displayed as an overlay in the portal
     */
    public OverviewPortal(int x, int y, int w, int h, Camera pc, Camera orc){
        this(x, y, w, h, new Camera[]{pc}, new Camera[]{orc});
    }
    
    /** Builds a new portal displaying what is seen through a camera and the region seen through another camera.
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param pcs cameras associated with the portal (provinding the overview)
     *@param orc camera observing a region; this region is displayed as an overlay in the portal
     */
    public OverviewPortal(int x, int y, int w, int h, Camera[] pcs, Camera orc){
        this(x, y, w, h, pcs, new Camera[]{orc});
    }

    /** Builds a new portal displaying what is seen through a camera and the region seen through another camera.
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param pcs cameras associated with the portal (provinding the overview)
     *@param orc camera observing a region; this region is displayed as an overlay in the portal
     */
    public OverviewPortal(int x, int y, int w, int h, Vector<Camera> pcs, Camera orc){
        this(x, y, w, h, pcs.toArray(new Camera[pcs.size()]) , new Camera[]{orc});
    }
    
    /** Builds a new portal displaying what is seen through a camera and the region seen through another camera.
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param pcs cameras associated with the portal (provinding the overview)
     *@param orcs cameras observing a region; these regions are displayed as an overlay in the portal
     */
    public OverviewPortal(int x, int y, int w, int h, Vector<Camera> pcs, Vector<Camera> orcs){
        this(x, y, w, h, pcs.toArray(new Camera[pcs.size()]) , orcs.toArray(new Camera[orcs.size()]));
    }

    /** Builds a new portal displaying what is seen through a camera and the region seen through another camera.
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param pcs cameras associated with the portal (provinding the overview)
     *@param orcs cameras observing a region; these regions are displayed as an overlay in the portal
     */
    public OverviewPortal(int x, int y, int w, int h, Camera[] pcs, Camera[] orcs){
        super(x, y, w, h, pcs, 1f);
        this.observedRegionCameras = orcs;
        this.observedRegionViews = new View[orcs.length];
        this.observedRegions = new double[orcs.length][4];
        this.observedRegionColors = new Color[orcs.length];
        this.orcoefs = new double[orcs.length];
        for (int i=0;i<orcs.length;i++){
            this.observedRegionViews[i] = orcs[i].getOwningView();
            this.observedRegionColors[i] = Color.GREEN;
            usedCameras.add(orcs[i]);
            if (orcs[i].getOwningSpace() != null){
                usedSpaces.add(orcs[i].getOwningSpace());
            }
        }
        
        borderTimer = new Timer();
        // done only when a listener is set
        //borderTimer.scheduleAtFixedRate(new BorderTimer(this), 40, 40);
    }

    private class BorderTimer extends TimerTask {
        // FIXME: only for the first observed region
        OverviewPortal portal;
        double[] portalRegion = new double[4];
        double[] intersection = new double[4];

        BorderTimer(OverviewPortal p){
            super();
            this.portal = p;
        }

        public void run(){
            portal.getVisibleRegion(portalRegion);
            intersection[0] = portal.observedRegions[0][0] - portalRegion[0]; // west
            intersection[1] = portal.observedRegions[0][1] - portalRegion[1]; // north
            intersection[2] = portal.observedRegions[0][2] - portalRegion[2]; // east
            intersection[3] = portal.observedRegions[0][3] - portalRegion[3]; // south
            portal.observedRegionIntersects(intersection);
        }

    }

    public void drawObservedRegionLocator(boolean b){
        drawObservedRegionLocator = b;
        repaint(true);
    }

    public boolean isDrawObservedRegionLocator(){
        return drawObservedRegionLocator;
    }

    /** Is the given point inside the observed region rectangle depicting what is seen through the main camera (viewfinder).
     *@param cx cursor x-coordinate (JPanel coordinates system)
     *@param cy cursor y-coordinate (JPanel coordinates system)
     */
    public boolean coordInsideObservedRegion(int cx, int cy){
	return (cx >= x-orHalfBorderWidth+w/2 + Math.round((observedRegions[0][0]-cameras[0].vx)*orcoefs[0]) &&
		cy >= y-orHalfBorderWidth+h/2 + Math.round((cameras[0].vy-observedRegions[0][1])*orcoefs[0]) &&
		cx <= x+orHalfBorderWidth+w/2 + Math.round((observedRegions[0][2]-cameras[0].vx)*orcoefs[0]) &&
		cy <= y+orHalfBorderWidth+h/2 + Math.round((cameras[0].vy-observedRegions[0][3])*orcoefs[0]));
    }
    /** Is the given point inside the ith observed region rectangle depicting what is seen through the main camera (viewfinder).
     *@param cx cursor x-coordinate (JPanel coordinates system)
     *@param cy cursor y-coordinate (JPanel coordinates system)
     *@param i observed region index
     */
    public boolean coordInsideObservedRegion(int cx, int cy, int i){
        if (i < 0 || i >= observedRegions.length) { return false; }
        double[] or = observedRegions[i];
        return (cx >= x-orHalfBorderWidth+w/2 + Math.round((or[0]-cameras[0].vx)*orcoefs[0]) &&
            cy >= y-orHalfBorderWidth+h/2 + Math.round((cameras[0].vy-or[1])*orcoefs[0]) &&
            cx <= x+orHalfBorderWidth+w/2 + Math.round((or[2]-cameras[0].vx)*orcoefs[0]) &&
            cy <= y+orHalfBorderWidth+h/2 + Math.round((cameras[0].vy-or[3])*orcoefs[0]));
    }
    /** Set the color of the rectangle depicting what is seen through the main observed region */
    public void setObservedRegionColor(Color c){
	    observedRegionColors[0] = c;
    }
    /** Set the color of the rectangle depicting what is seen through the ith observed region  */
    public void setObservedRegionColor(Color c, int i){
        if (i < 0 || i >= observedRegionColors.length) { return; }
        observedRegionColors[i] = c;
    }
    /** Set the colors of the rectangles depicting what is seen through the observed regions  */
    public void setObservedRegionColors(Vector<Color> colvect){
        for (int i = 0; i < observedRegionColors.length &&  i < colvect.size(); i++){
            observedRegionColors[i] = colvect.get(i);
        }
    }
    /** Set the colors of the rectangles depicting what is seen through the observed regions  */
    public void setObservedRegionColors(Color[] cols){
        for (int i = 0; i < observedRegionColors.length &&  i < cols.length; i++){
            observedRegionColors[i] = cols[i];
        }
    }
    /** Get color of rectangle depicting what is seen through the main camera. */
    public Color getObservedRegionColor(){
	    return observedRegionColor;
    }
    public Color getObservedRegionColor(int i){
        if (i < 0 || i >= observedRegionColors.length) { return null; }
        return observedRegionColors[i];
    }
    public Color[] getObservedRegionColors(){
        return observedRegionColors;
    }

    public void setObservedRegionTranslucency(float a){
        if (a == 1.0f){
            acST = null;
        }
        else {
            acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a);
        }
        repaint(true);
    }

    /** Set the border width of the rectangle depicting what is seen through the main camera. **/
    public void setObservedRegionBorderWidth(float bw){
        if (bw <= 0.0f){
            orStroke=null;
            orHalfBorderWidth = 0.5f; orBorderWidth = 1.0f;
        }
        else{
            orStroke = new BasicStroke(bw);
            orBorderWidth = bw;
            orHalfBorderWidth = bw/2;
        }
        repaint(true);
    }

    /** Get the border width of the rectangle depicting what is seen through the main camera. **/
    public float setObservedRegionBorderWidth(){
        return orBorderWidth;
    }

    public double getObservedRegionX() {
        orcoefs[0] = cameras[0].focal/(cameras[0].focal+cameras[0].altitude);
        return x+ (double)w/2.0 + (observedRegions[0][0]-cameras[0].vx)*orcoefs[0];
    }
    public double getObservedRegionY() {
        orcoefs[0] = cameras[0].focal/(cameras[0].focal+cameras[0].altitude);
        return y+ h/2.0 - (observedRegions[0][1]-cameras[0].vy)*orcoefs[0];
    }
    public double getObservedRegionW() {
        orcoefs[0] = cameras[0].focal/(cameras[0].focal+cameras[0].altitude);
        return (observedRegions[0][2]-observedRegions[0][0])*orcoefs[0];
    }
    public double getObservedRegionH() {
        orcoefs[0] = cameras[0].focal/(cameras[0].focal+cameras[0].altitude);
        return (observedRegions[0][1]-observedRegions[0][3])*orcoefs[0];
    }
    public double getObservedRegionCX() {
        return getObservedRegionX() + getObservedRegionW()/2.0;
    }
    public double getObservedRegionCY() {
        return getObservedRegionY() + getObservedRegionH()/2.0;
    }

    public double getObservedRegionX(int i) {
        if (i < 0 || i >= observedRegions.length) { return 0; }
        double[] or = observedRegions[i];
        orcoefs[0] = cameras[0].focal/(cameras[0].focal+cameras[0].altitude);
        return x+ (double)w/2.0 + (or[0]-cameras[0].vx)*orcoefs[0];
    }
    public double getObservedRegionY(int i) {
        if (i < 0 || i >= observedRegions.length) { return 0; }
        double[] or = observedRegions[i];
        orcoefs[0] = cameras[0].focal/(cameras[0].focal+cameras[0].altitude);
        return y+ h/2.0 - (or[1]-cameras[0].vy)*orcoefs[0];
    }
    public double getObservedRegionW(int i) {
        if (i < 0 || i >= observedRegions.length) { return 0; }
        double[] or = observedRegions[i];
        orcoefs[0] = cameras[0].focal/(cameras[0].focal+cameras[0].altitude);
        return (or[2]-or[0])*orcoefs[0];
    }
    public double getObservedRegionH(int i) {
        if (i < 0 || i >= observedRegions.length) { return 0; }
        double[] or = observedRegions[i];
        orcoefs[0] = cameras[0].focal/(cameras[0].focal+cameras[0].altitude);
        return (or[1]-or[3])*orcoefs[0];
    }
    public double getObservedRegionCX(int i) {
        if (i < 0 || i >= observedRegions.length) { return 0; }
        return getObservedRegionX(i) + getObservedRegionW(i)/2.0;
    }
    public double getObservedRegionCY(int i) {
        if (i < 0 || i >= observedRegions.length) { return 0; }
        return getObservedRegionY(i) + getObservedRegionH(i)/2.0;
    }
    /**
     *@return null if translucency is 1.0f (opaque).
     */
    public AlphaComposite getObservedRegionTranslucency(){
	    return acST;
    }

    public Camera getObservedRegionCamera(){
        return observedRegionCameras[0];
    }
    public Camera getObservedRegionCamera(int i){
        if (i < 0 || i >= observedRegionCameras.length) { return null; }
        return observedRegionCameras[i];
    }
    public Camera[] getObservedRegionCameras(){
        return observedRegionCameras;
    }

    // for zvtm-cluster (observedRegionView is not correct)
    protected int obsViewWidth = 0;
    protected int obsViewHeight = 0;
    protected double obsViewX = 0;
    protected double obsViewY = 0;
    /**CALLED INTERNALLY (zvtm-cluster) - NOT FOR PUBLIC USE*/
    public void setObservedViewLocationAndSize(double ox, double oy, int evw, int evh){
        obsViewWidth = evw;
        obsViewHeight = evh;
        obsViewX = ox;
        obsViewY = oy;
    }

    protected double[] getViewVisibleRegion(){
       if (obsViewWidth == 0 || obsViewHeight == 0){
            return observedRegionViews[0].getVisibleRegion(observedRegionCameras[0], observedRegions[0]);
        }
        Camera c = observedRegionCameras[0];
        double uncoef = (c.focal+c.altitude) / c.focal;
        observedRegions[0][0] = (c.vx - obsViewX*uncoef) - (obsViewWidth/2)*uncoef;
        observedRegions[0][1] = (c.vy - obsViewY*uncoef) + (obsViewHeight/2)*uncoef;
        observedRegions[0][2] = (c.vx - obsViewX*uncoef) + (obsViewWidth/2)*uncoef;
        observedRegions[0][3] = (c.vy - obsViewY*uncoef) - (obsViewHeight/2)*uncoef;
        return observedRegions[0];
    }
    protected double[] getViewVisibleRegion(int i){
        if (i < 0 || i >= observedRegionCameras.length) { return null; }
        if (obsViewWidth == 0 || obsViewHeight == 0){
            return observedRegionViews[i].getVisibleRegion(
                observedRegionCameras[i], observedRegions[i]);
        }
        Camera c = observedRegionCameras[i];
        double uncoef = (c.focal+c.altitude) / c.focal;
        double[] or =  observedRegions[i];
        or[0] = (c.vx - obsViewX*uncoef) - (obsViewWidth/2)*uncoef;
        or[1] = (c.vy - obsViewY*uncoef) + (obsViewHeight/2)*uncoef;
        or[2] = (c.vx - obsViewX*uncoef) + (obsViewWidth/2)*uncoef;
        or[3] = (c.vy - obsViewY*uncoef) - (obsViewHeight/2)*uncoef;
        return or;
    }

    // FIXME: introduce an additional buffer for the obs region !
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
                //System.out.println("draw portal.... "+x+" "+y+" "+w+" "+h+" "+buffx+" "+backBufferW);
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

    @Override
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
            if (alphaC.getAlpha() == 0){
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

        // g2d.setClip(x-tx, y-ty, w, h);
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
            g2d.fillRect(x-tx, y-ty, w, h);
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
        // paint region observed through observedRegionCamera
        if (orStroke!=null){
            g2d.setStroke(orStroke);
        }
        else{
            g2d.setStroke(standardStroke);
        }
        //System.out.println("observedRegions num: " + observedRegionCameras.size());
        for (int i = 0; i < observedRegionCameras.length; i++){
            observedRegions[i] = getViewVisibleRegion(i);
            //observedRegion = observedRegionView.getVisibleRegion(observedRegionCamera, observedRegion);
            g2d.setColor(observedRegionColors[i]);
            orcoefs[0] = (float)(cameras[0].focal/(cameras[0].focal+cameras[0].altitude));
            double[] or = observedRegions[i];
            int nwx = (int)(x-tx+backBufferTX+w/2d + Math.round((or[0]-cameras[0].vx)*orcoefs[0]));
            int nwy = (int)(y-ty+backBufferTY+h/2d - Math.round((or[1]-cameras[0].vy)*orcoefs[0]));
            int orw = (int)Math.round((or[2]-or[0])*orcoefs[0]);
            int orh = (int)Math.round((or[1]-or[3])*orcoefs[0]);
            //System.out.println("observedRegions "+i+" "+nwx+" "+nwy+" "+orw+" "+orh);
            if (acST != null){
                g2d.setComposite(acST);
                g2d.fillRect(nwx, nwy, orw, orh);
                if (alphaC != null){
                    g2d.setComposite(alphaC);
                }
                else{
                    g2d.setComposite(Translucent.acO);
                }
                g2d.drawRect(nwx, nwy, orw, orh);
            }
            else{
                g2d.fillRect(nwx, nwy, orw, orh);
            }
            if (drawObservedRegionLocator){
                // west
                g2d.drawRect(x-tx+backBufferTX, nwy+orh/2, nwx-x+tx-backBufferTX, 1);
                // north
                g2d.drawRect(nwx+orw/2, y-ty+backBufferTY, 1, nwy-y+ty-backBufferTY);
                // east
                g2d.drawRect(nwx+orw, nwy+orh/2, x+w-(nwx+orw), 1);
                // south
                g2d.drawRect(nwx+orw/2, nwy+orh, 1, y+h-(nwy+orh));
            }
        }
        // reset Graphics2D
        g2d.setStroke(standardStroke);
        paintPortalFrame(g2d, viewWidth, viewHeight, tx, ty);
    }

    public void dispose(){
        if (observedRegionListener != null)
	       borderTimer.cancel();
    }

    ObservedRegionListener observedRegionListener;

    public void setObservedRegionListener(ObservedRegionListener orl){
        borderTimer.scheduleAtFixedRate(new BorderTimer(this), 40, 40);
	    this.observedRegionListener = orl;
    }

    void observedRegionIntersects(double[] wnes){
        if (observedRegionListener != null){
            observedRegionListener.intersectsParentRegion(wnes);
        }
    }

}
