/*   FILE: MorOverviewPortal.java
 *   DATE OF CREATION:  Sun Jun 13 21:09:59 2014
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Olivier Chapuis (chapuis@lri.fr)
 *   Copyright (c) INRIA, 2004-2010. Copyright (c) CNRS, 2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */ 

package fr.inria.zvtm.engine.portals;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Dimension;

import java.util.Timer;
import java.util.TimerTask;
import java.util.*;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.event.ObservedRegionListener;

/**A portal showing what is seen through a camera that serves as an overview. Shape: rectangular.
   The Camera should not be used in any other View or Portal.*/

public class MorOverviewPortal extends CameraPortal
{
    Camera[] observedRegionsCameras;
    View observedRegionView;  // only one valide view !!!
    double[][] observedRegions;
    double orcoef;

    Color[] observedRegionsColors;

    /** For translucency of the rectangle representing the region observed through the main viewport (default is 0.5)*/
    AlphaComposite acST;
    /** Alpha channel value. */
    float alpha = 0.2f;

    //Timer borderTimer;
    
    boolean drawObservedRegionLocator = false;

    /** Builds a new portal displaying what is seen through a camera and the region seen through another camera.
     *@param x top-left horizontal coordinate of portal, in parent's JPanel coordinates
     *@param y top-left vertical coordinate of portal, in parent's JPanel coordinates
     *@param w portal width
     *@param h portal height
     *@param pc camera associated with the portal (provinding the overview)
     *@param orc an array of cameras observing each a region; these regions are displayed as overlays in the portal
     */
    public MorOverviewPortal(int x, int y, int w, int h, Camera pc, Camera[] orcs, Color[] colors)
    {
	    super(x, y, w, h, pc);
	    this.observedRegionsCameras = new Camera[orcs.length];
	    this.observedRegionView = orcs[0].getOwningView();
	    this.observedRegions = new double[orcs.length][];
	    this.observedRegionsColors = new Color[orcs.length];
	    for(int i = 0; i < orcs.length; i++)
	    {
		    this.observedRegionsCameras[i] = orcs[i];
		    this.observedRegions[i] = new double[4];
		    if (colors != null &&  colors.length > i)
		    {
			    this.observedRegionsColors[i] = colors[i];
		    }
		    else
		    {
			    this.observedRegionsColors[i] = Color.GREEN;
		    }
	    }
	    // find a valid view !!
	    for(int i = 0; i < orcs.length; i++)
	    {
		    observedRegionView = orcs[i].getOwningView();
		    if (observedRegionView != null)
		    {
			    observedRegions[i] =
				    observedRegionView.getVisibleRegion(
					    observedRegionsCameras[i], observedRegions[i]);
			    if (observedRegions[i] != null)
			    {
				    borderColor = observedRegionsColors[i];
			    }
			    break;
		    }
	    }
	    
	    if (observedRegionView == null)
	    {
		    // dead !!!
		    System.out.print("MorOverviewPortal: None of the OR Cameras has a Valid View. Soon a Crash !!"); 
		    
	    }
	   
	    setObservedRegionTranslucency(alpha);

	    //borderTimer = new Timer();
	    //borderTimer.scheduleAtFixedRate(new BorderTimer(this), 40, 40);
    }

/*     private class BorderTimer extends TimerTask { */

/*         OverviewPortal portal; */
/*         double[] portalRegion = new double[4]; */
/*         double[] intersection = new double[4]; */

/*         BorderTimer(OverviewPortal p){ */
/*             super(); */
/*             this.portal = p; */
/*         } */

/*         public void run(){ */
/*             portal.getVisibleRegion(portalRegion); */
/*             intersection[0] = portal.observedRegion[0] - portalRegion[0]; // west */
/*             intersection[1] = portal.observedRegion[1] - portalRegion[1]; // north */
/*             intersection[2] = portal.observedRegion[2] - portalRegion[2]; // east */
/*             intersection[3] = portal.observedRegion[3] - portalRegion[3]; // south */
/*             portal.observedRegionIntersects(intersection); */
/*         } */

/*     } */


    public Camera[] getObservedRegionsCameras() {
	    return observedRegionsCameras;
    }

    public Color[] getObservedRegionsColors() {
	    return observedRegionsColors;
    }

    public void drawObservedRegionLocator(boolean b){
        drawObservedRegionLocator = b;
    }

    public boolean isDrawObservedRegionLocator(){
        return drawObservedRegionLocator;
    }

    /** Is the given point inside the observed region rectangle depicting what is seen through the main camera (viewfinder). 
     *@param cx cursor x-coordinate (JPanel coordinates system)
     *@param cy cursor y-coordinate (JPanel coordinates system) 
     */
    public boolean coordInsideObservedRegion(int cx, int cy, int i){
	return (cx >= x+w/2 + Math.round((observedRegions[i][0]-camera.vx)*orcoef) &&
		cy >= y+h/2 + Math.round((camera.vy-observedRegions[i][1])*orcoef) &&
		cx <= x+w/2 + Math.round((observedRegions[i][2]-camera.vx)*orcoef) &&
		cy <= y+h/2 + Math.round((camera.vy-observedRegions[i][3])*orcoef));
    }
    
    /** Set color of rectangle depicting what is seen through the main camera for the ith OR. */
     public void setObservedRegionColor(Color c, int i)
    {
	    if (i >= 0 && observedRegionsColors.length > i)
	    {
		    observedRegionsColors[i] = c;
	    }
    }

    public void setObservedRegionsColors(Color[] cs)
    {
	    for (int i = 0; i < observedRegionsColors.length && i < cs.length; i++)
	    {
		    observedRegionsColors[i] = cs[i];
	    }
    }

    /** Get color of rectangle depicting what is seen through the main camera. */
    public Color getObservedRegionColor(int i){
	    return observedRegionsColors[i];
    }

    public void setObservedRegionTranslucency(float a){
        if (a == 1.0f){
            acST = null;
        }
        else {
            acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a);
        }
    }

    public double getObservedRegionX(int i) {
        orcoef = camera.focal/(camera.focal+camera.altitude);
        return x+ (double)w/2.0 + (observedRegions[i][0]-camera.vx)*orcoef;
    }
    public double getObservedRegionY(int i) {
        orcoef = camera.focal/(camera.focal+camera.altitude);
        return y+ h/2.0 - (observedRegions[i][1]-camera.vy)*orcoef;
    }
    public double getObservedRegionW(int i) {
        orcoef = camera.focal/(camera.focal+camera.altitude);
        return (observedRegions[i][2]-observedRegions[i][0])*orcoef;
    }
    public double getObservedRegionH(int i) {
        orcoef = camera.focal/(camera.focal+camera.altitude);
        return (observedRegions[i][1]-observedRegions[i][3])*orcoef;
    }
    public double getObservedRegionCX(int i) {
        return getObservedRegionX(i) + getObservedRegionW(i)/2.0;
    }
    public double getObservedRegionCY(int i) {
        return getObservedRegionY(i) + getObservedRegionH(i)/2.0;
    }
    
    /**
     *@return null if translucency is 1.0f (opaque).
     */
    public AlphaComposite getObservedRegionTranslucency(){
	    return acST;
    }

    @Override
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
        if (!visible){return;}
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
            duncoef = (camera.focal+camera.altitude) / camera.focal;
            //compute region seen from this view through camera
            viewWC = camera.vx - (w/2d)*duncoef;
            viewNC = camera.vy + (h/2d)*duncoef;
            viewEC = camera.vx + (w/2d)*duncoef;
            viewSC = camera.vy - (h/2d)*duncoef;
            gll = cameraSpace.getDrawingList();
            for (int i=0;i<gll.length;i++){
                if (gll[i] != null){
                    synchronized(gll[i]){
                        if (gll[i].visibleInViewport(viewWC, viewNC, viewEC, viewSC, camera)){
                            //if glyph is at least partially visible in the reg. seen from this view, display
                            gll[i].project(camera, size); // an invisible glyph should still be projected
                            if (gll[i].isVisible()){      // as it can be sensitive
                                gll[i].draw(g2d, w, h, camIndex, standardStroke, standardTransform, x, y);
                            }
                            cameraSpace.drewGlyph(gll[i], camIndex);
                        }
                    }
                }
            }
        }

        // paint region observed through observedRegionsCameras
	orcoef = (float)(camera.focal/(camera.focal+camera.altitude));
	g2d.setStroke(standardStroke);
	double uncoef;
	Dimension panelSize = observedRegionView.getPanel().getComponent().getSize();
	for(int i = 0; i < observedRegionsCameras.length; i++)
	{
		//System.out.println("PAINT " + i + " " + observedRegionsCameras.length);

		//observedRegions[i] = observedRegionViews[i].getVisibleRegion(observedRegionsCameras[i], observedRegions[i]);
		uncoef = (observedRegionsCameras[i].focal+observedRegionsCameras[i].altitude) 
			/ observedRegionsCameras[i].focal;
		observedRegions[i][0] = observedRegionsCameras[i].vx-(panelSize.width/2-0)*uncoef;
		observedRegions[i][1] = observedRegionsCameras[i].vy+(panelSize.height/2-0)*uncoef;
		observedRegions[i][2] = observedRegionsCameras[i].vx+(panelSize.width/2-0)*uncoef;
		observedRegions[i][3] = observedRegionsCameras[i].vy-(panelSize.height/2-0)*uncoef;
		//System.out.println(
		//	" observedRegions("+i+"):" +
		//	observedRegions[i][0]+" "+observedRegions[i][1]+" "+
		//	observedRegions[i][2]+" "+observedRegions[i][3]);

		g2d.setColor(observedRegionsColors[i]);
       
		int nwx = (int)(x+w/2d + Math.round((observedRegions[i][0]-camera.vx)*orcoef));
		int nwy = (int)(y+h/2d - Math.round((observedRegions[i][1]-camera.vy)*orcoef));
		int orw = (int)Math.round((observedRegions[i][2]-observedRegions[i][0])*orcoef);
		int orh = (int)Math.round((observedRegions[i][1]-observedRegions[i][3])*orcoef);
		if (acST != null){
			g2d.setComposite(acST);
			g2d.fillRect(nwx, nwy, orw, orh);
			g2d.setComposite(Translucent.acO);
		}
		//g2d.setStroke(standardStroke);
		g2d.drawRect(nwx, nwy, orw, orh);
		if (drawObservedRegionLocator){
			// west
			g2d.drawRect(x, nwy+orh/2, nwx-x, 1);
			// // north
			g2d.drawRect(nwx+orw/2, y, 1, nwy-y);
			// east
			g2d.drawRect(nwx+orw, nwy+orh/2, x+w-(nwx+orw), 1);
			// south
			g2d.drawRect(nwx+orw/2, nwy+orh, 1, y+h-(nwy+orh));
		}
	}

	// reset Graphics2D
	g2d.setClip(0, 0, viewWidth, viewHeight);
	if (borderColor != null){
		g2d.setColor(borderColor);
		g2d.drawRect(x, y, w, h);
	}
    }


    //public void dispose(){
	    //borderTimer.cancel();
    //}

    //ObservedRegionListener observedRegionListener;

    //public void setObservedRegionListener(ObservedRegionListener orl){
//	    this.observedRegionListener = orl;
//    }

//    void observedRegionIntersects(double[] wnes){
//        if (observedRegionListener != null){
//            observedRegionListener.intersectsParentRegion(wnes);
//        }
//    }

}
