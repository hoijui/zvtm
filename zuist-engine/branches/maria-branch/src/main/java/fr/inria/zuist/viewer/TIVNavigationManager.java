/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.viewer;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JFrame;
import javax.swing.JComboBox;
import java.awt.geom.Point2D;
import java.awt.BasicStroke;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.Map;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;


import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.SIRectangle;
import fr.inria.zvtm.glyphs.DPath;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.lens.*;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.engine.portals.CameraPortal;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;

import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.LensMenu;

class TIVNavigationManager {

    /* Navigation constants */
    static final int ANIM_MOVE_DURATION = 300;
    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;

    /* misc. lens settings */
    Lens lens;
    TemporalLens tLens;
    static int LENS_R1 = 200;
    static int LENS_R2 = 100;
    static final int WHEEL_ANIM_TIME = 50;
    static final int LENS_ANIM_TIME = 300;
    //static double DEFAULT_MAG_FACTOR = 4.0;
    static double DEFAULT_MAG_FACTOR = 1.0f;
    static double MAG_FACTOR = DEFAULT_MAG_FACTOR;
    static double INV_MAG_FACTOR = 1/MAG_FACTOR;
    /* LENS MAGNIFICATION */
    static float WHEEL_MM_STEP = 1.0f;
    static final float MAX_MAG_FACTOR = 12.0f;

    static final int NO_LENS = 0;
    static final int ZOOMIN_LENS = 1;
    static final int ZOOMOUT_LENS = -1;
    int lensType = NO_LENS;

    /* lens distance and drop-off functions */
    static final short L2_Gaussian = 0;
    static final short L2_SCB = 1;
    static final short Blense=2;
    static final short BInverseCosine=3;
    static final short BGaussianLens=4;
    static final short SCBGaussianLens=5;
    static final short SCFGaussianLens = 6;
    
    //short lensFamily = L2_SCB;
    //short lensFamily = BGaussianLens;
    short lensFamily = SCBGaussianLens;
    //short lensFamily = BGaussianLens;
    static boolean lense=false;
    static boolean pieMenu=false;

    static final float FLOOR_ALTITUDE = 100.0f;

    TiledImageViewer application;

    Camera mCamera;
    VirtualSpaceManager vsm;

    LensMenu lensMenu;

    static boolean [] contextVisibility = new boolean [6];
    static boolean [] lenseVisibility = new boolean [6];

    static String contextLayer;
    static String lenseLayer;
    static String temporaryContextLayer;
    static String temporaryLenseLayer;
    static int lensMenuRadius = 40;

    View fakeCursorView=null;
    public static File pathFile = null;

    TIVNavigationManager(TiledImageViewer app){
        this.application = app;
        this.vsm = VirtualSpaceManager.INSTANCE;
        mCamera = app.mCamera;
        ss = new ScreenSaver(this);
        ssTimer = new Timer();
    	ssTimer.scheduleAtFixedRate(ss, SCREEN_SAVER_INTERVAL, SCREEN_SAVER_INTERVAL);
    }

    void getGlobalView(EndAction ea){
		application.sm.getGlobalView(mCamera, TIVNavigationManager.ANIM_MOVE_DURATION, ea);
    }

    /* Higher view */
    void getHigherView(){
        Float alt = new Float(mCamera.getAltitude() + mCamera.getFocal());
        //vsm.animator.createCameraAnimation(TIVNavigationManager.ANIM_MOVE_DURATION, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(TIVNavigationManager.ANIM_MOVE_DURATION, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Higher view */
    void getLowerView(){
        Float alt=new Float(-(mCamera.getAltitude() + mCamera.getFocal())/2.0f);
        //vsm.animator.createCameraAnimation(TIVNavigationManager.ANIM_MOVE_DURATION, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(TIVNavigationManager.ANIM_MOVE_DURATION, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Direction should be one of TiledImageViewer.MOVE_* */
    void translateView(short direction){
        Point2D.Double trans;
        double[] rb = application.mView.getVisibleRegion(mCamera);
        if (direction==MOVE_UP){
            double qt = (rb[1]-rb[3])/4.0;
            trans = new Point2D.Double(0,qt);
        }
        else if (direction==MOVE_DOWN){
            double qt = (rb[3]-rb[1])/4.0;
            trans = new Point2D.Double(0,qt);
        }
        else if (direction==MOVE_RIGHT){
            double qt = (rb[2]-rb[0])/4.0;
            trans = new Point2D.Double(qt,0);
        }
        else {
            // direction==MOVE_LEFT
            double qt = (rb[0]-rb[2])/4.0;
            trans = new Point2D.Double(qt,0);
        }
        //vsm.animator.createCameraAnimation(TIVNavigationManager.ANIM_MOVE_DURATION, AnimManager.CA_TRANS_SIG, trans, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(TIVNavigationManager.ANIM_MOVE_DURATION, mCamera,
            trans, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    void altitudeChanged(){

    }

	/* -------------- Overview ------------------- */

	static final int MAX_OVERVIEW_WIDTH = 200;
	static final int MAX_OVERVIEW_HEIGHT = 200;
	static final Color OBSERVED_REGION_COLOR = Color.GREEN;
	static final float OBSERVED_REGION_ALPHA = 0.5f;
	static final Color OV_BORDER_COLOR = Color.WHITE;
	static final Color OV_INSIDE_BORDER_COLOR = Color.WHITE;

	OverviewPortal ovPortal;
    

	double[] scene_bounds = {0, 0, 0, 0};

	void createOverview(Region rootRegion){
	    int ow, oh;
	    float ar = (float) (rootRegion.getWidth() / (float)rootRegion.getHeight());
	    if (ar > 1){
	        // wider than high
	        ow = MAX_OVERVIEW_WIDTH;
	        oh = Math.round(ow/ar);
	    }
	    else {
	        // higher than wide
	        oh = MAX_OVERVIEW_HEIGHT;
	        ow = Math.round(oh*ar);
	    }
		ovPortal = new OverviewPortal(application.panelWidth-ow-1, application.panelHeight-oh-1, ow, oh, application.ovCamera, application.scanCamera);
		ovPortal.setPortalListener(application.eh);
		ovPortal.setBackgroundColor(TiledImageViewer.BACKGROUND_COLOR);
		ovPortal.setObservedRegionColor(OBSERVED_REGION_COLOR);
		ovPortal.setObservedRegionTranslucency(OBSERVED_REGION_ALPHA);
		VirtualSpaceManager.INSTANCE.addPortal(ovPortal, application.mView);
		ovPortal.setBorder(Color.GREEN);
		updateOverview();

        


	}

	void updateOverview(){
		if (ovPortal != null){
		    int l = 0;
    		while (application.sm.getRegionsAtLevel(l) == null){
    			l++;
    			if (l > application.sm.getLevelCount()){
    				l = -1;
    				break;
    			}
    		}
    		if (l > -1){
    			scene_bounds = application.sm.getLevel(l).getBounds();
    	        ovPortal.centerOnRegion(TIVNavigationManager.ANIM_MOVE_DURATION, scene_bounds[0], scene_bounds[1], scene_bounds[2], scene_bounds[3]);
    		}
		}
	}

    void showOverview(boolean b){
        if (b == ovPortal.isVisible()){return;}
        ovPortal.setVisible(b);
        vsm.repaint(application.mView);
    }

    /* -------------- Modes -------------------------- */


    CameraPortal cPortal=null;
    CameraPortal mPortal=null;

    Glyph fakeCursorH = null;
    Glyph fakeCursorV=null;

    Vector <DPath> paths = new Vector <DPath>();
    void loadMode(int mode)
    {

        if(mode==application.lenses)
        {
            saveLayerVisibility();

        }
        if(mode==application.routeLens)
        {
            lensFamily = L2_Gaussian;
            DEFAULT_MAG_FACTOR = 3.0f;
            LENS_R1 = 150;
            LENS_R2 = 100;

            //Camera that will be magnified in the lense
            application.magnifyCamera = application.mCamera;
            application.loadLenseSpace();
            readPath();

        }

        if(mode==application.covisualization2)
        {
            Color c = new Color(137,60,255);
            fakeCursorH = new SIRectangle(0,0,0,20,3,c);
            fakeCursorV = new SIRectangle(0,0,0,3,20,c);
        }
    }

    void loadCovisualization()
    {
        cPortal = new CameraPortal(application.panelWidth/2,0,application.panelWidth/2, application.panelHeight,application.orthoCamera);
        mPortal = new CameraPortal(0,0,application.panelWidth/2, application.panelHeight, application.scanCamera);
        //mPortal = new CameraPortal(0,0,application.panelWidth/2, application.panelHeight, application.littCamera);
        cPortal.setPortalListener(application.eh);
        mPortal.setPortalListener(application.eh);
        //cPortal.setPortalListener(application.eh);
        cPortal.setBackgroundColor(Color.BLACK);
        mPortal.setBackgroundColor(Color.BLACK);
        VirtualSpaceManager.INSTANCE.addPortal(cPortal, application.mView);
        VirtualSpaceManager.INSTANCE.addPortal(mPortal, application.mView);


    }

    void resizeCovisualization()
    {
        if(cPortal!=null)
            VirtualSpaceManager.INSTANCE.destroyPortal(cPortal);
        if(mPortal != null)
            VirtualSpaceManager.INSTANCE.destroyPortal(mPortal);
        loadCovisualization();
    }

    //Switching layers. Only for mode=swipe.
    void hideLayer(VirtualSpace vs)
    {
        System.out.println(vs.getName());
        //for (Glyph g:vs.getAllGlyphs())
        for(Glyph g:application.scanSpace.getAllGlyphs())
        {
            vs.hide(g);
        }
    } 

    void showLayer(VirtualSpace vs)
    {
        for(Glyph g:vs.getAllGlyphs())
        {
            vs.show(g);
        }
    }

    //Mixing representations with translucency. Only for mode = alpha_swipe
    public void alphaLayer (VirtualSpace vs, int i)
    {
        if(i==1)
        {
            if(vs.getVisibleGlyphsList()[0].getTranslucencyValue()>0.1){
                for (Glyph g : vs.getAllGlyphs()){
                    g.setTranslucencyValue(g.getTranslucencyValue()-0.1f);
                }
            }
                
            else {
                for (Glyph g : vs.getAllGlyphs()){
                    g.setTranslucencyValue(0f);
                }
            }
        } 
        if(i==0)
        {
            if(vs.getVisibleGlyphsList()[0].getTranslucencyValue()<0.9){
                for (Glyph g : vs.getAllGlyphs()){
                    g.setTranslucencyValue(g.getTranslucencyValue()+0.1f);}
            }
            else{
                for (Glyph g : vs.getAllGlyphs()){
                    g.setTranslucencyValue(1f);}
            }
        }   
    }

    //Moves the cursor in the inactive screen, only in mode=covisualization
    public void moveFakeCursor(ViewPanel v, double jpx, double jpy)
    {
        if (fakeCursorView == null || fakeCursorView == v.parent || !(application.covisHash.get(fakeCursorView).contains(fakeCursorH)))
        {
            if (fakeCursorView !=null && (application.covisHash.get(fakeCursorView).contains(fakeCursorH)) )
            {
                application.covisHash.get(fakeCursorView).removeGlyph(fakeCursorH);
                application.covisHash.get(fakeCursorView).removeGlyph(fakeCursorV);
            }

            if(application.covisHash.keySet().toArray()[0]==v.parent) {
                fakeCursorView = (View)(application.covisHash.keySet().toArray()[1]);}
            else {
                fakeCursorView = (View)(application.covisHash.keySet().toArray()[0]);}
            application.covisHash.get(fakeCursorView).addGlyph(fakeCursorH);
            application.covisHash.get(fakeCursorView).addGlyph(fakeCursorV);
        }

        if(fakeCursorV !=null && fakeCursorH !=null)
        {
            application.covisHash.get(fakeCursorView).onTop(fakeCursorV);
            application.covisHash.get(fakeCursorView).onTop(fakeCursorH);
            fakeCursorV.moveTo(jpx,jpy);
            fakeCursorH.moveTo(jpx,jpy);
        }
    }

    public void changeLayers(String vsName)
    {
        if(lense)
        {
            changeLayerLense(vsName);
        
        }
        else
        {
            changeLayerContext(vsName);
        }
    }

    public void loadSavedLayers()
    {
        application.mView.setLayerVisibility(contextVisibility, lenseVisibility);
    }

    public void saveLayerVisibility()
    {
        System.arraycopy(application.mView.getLayerVisibility()[0],0,contextVisibility,0, application.mView.getLayerVisibility()[0].length);
        System.arraycopy(application.mView.getLayerVisibility()[1],0,lenseVisibility,0, application.mView.getLayerVisibility()[1].length);
    }

    //Change representations in lenses mode.
    public void changeLayerContext(String name)
    {
        for (String key : application.layersIndex.keySet())
        {

            if(key==name)
            {
                System.out.println("CONTEXT LAYER: "+name);
                application.mView.getLayerVisibility()[0][application.layersIndex.get(key)]=true;
                application.mView.setLayerVisibility(application.mView.getLayerVisibility()[0], application.mView.getLayerVisibility()[1]);
            }
            else
            {
                application.mView.getLayerVisibility()[0][application.layersIndex.get(key)]=false;
                application.mView.setLayerVisibility(application.mView.getLayerVisibility()[0], application.mView.getLayerVisibility()[1]);
            }
        }

    }

    public void changeLayerLense(String name)
    {
        for (String key : application.layersIndex.keySet())
        {
            if(key==name)
            {
                System.out.println("LENSE LAYER: "+name);
                application.mView.getLayerVisibility()[1][application.layersIndex.get(key)]=true;
                application.mView.setLayerVisibility(application.mView.getLayerVisibility()[0], application.mView.getLayerVisibility()[1]);
            }
            else
            {
                application.mView.getLayerVisibility()[1][application.layersIndex.get(key)]=false;
                application.mView.setLayerVisibility(application.mView.getLayerVisibility()[0], application.mView.getLayerVisibility()[1]);
            }
        }
    }

    //For creating paths clicking, only in mode=none
    public void writePath(List <String> lines)
    {
        try{
        File f = new File ("points4.txt");
        OutputStream outputStream = new FileOutputStream(f);
        PrintStream ps = new PrintStream(f);
        for (String line : lines)
        {
            ps.println(line);
        }
        ps.close();
        }
        catch(Exception e)
        {
            System.out.println("Not able to write file");
        }
    }

    public DPath readPath()
    {
        DPath path = null;
        if(pathFile!=null){
            try {
            BufferedReader br = new BufferedReader(new FileReader(pathFile));
            String line;
            Double x;
            Double y;
            path = new DPath();
            while ((line = br.readLine()) != null) {
                if(line.equals("-----"))
                {
                    paths.add(path);
                    String firstPoint=null;
                    if(br.readLine() != null) {
                    firstPoint=br.readLine();
                    String[] coordinates = firstPoint.split(":");
                    x = Double.parseDouble(coordinates[0]);
                    y = Double.parseDouble(coordinates[1]);
                    path=new DPath(x,y,11,Color.CYAN);
                    }
                    
                }
                else
                {
                String[] coordinates = line.split(":");
                x = Double.parseDouble(coordinates[0]);
                y = Double.parseDouble(coordinates[1]);
                path.addSegment(x,y,true);
                }
               
            }
            br.close();
            }
            catch(Exception e) {System.out.println("Not able to read file");}
            for (DPath dp : paths)
            {
            BasicStroke s = new BasicStroke(7f);
            dp.setStroke(s);
            application.lenseSpace.addGlyph(dp);
            application.mSpace.addGlyph(dp);
            }
        }
        return path;

    }

    public void showLensMenu(ViewPanel v, double centerX, double centerY)
    {
        Vector <String> lenseOptions= new Vector <String> ();
        Vector <String> contextOptions= new Vector <String> ();
        for (String key : application.layersIndex.keySet())
        {
            if (key != lenseLayer)
            {
                lenseOptions.add(key);
            }
        } 
        for (String key : application.layersIndex.keySet())
        {
            if (key != contextLayer)
            {
                contextOptions.add(key);
            }
        }  
        v.parent.setActiveLayer(application.menuCamera);
        lensMenu = new LensMenu(lenseOptions,contextOptions, v.parent.getActiveCamera().getOwningSpace(),2*lensMenuRadius,3*lensMenuRadius, new Point2D.Double(centerX,centerY));
        lensMenu.drawLensMenu(new Point2D.Double(centerX,centerY));
        //application.mView.centerOnGlyph(lensMenu.getOuterItems().get(0),v.parent.getActiveCamera(),0);
        System.out.println("CONTEXT LAYER in showLensMenu "+contextLayer);
        System.out.println("LENS LAYER in showLensMenu "+lenseLayer);
        temporaryLenseLayer = lenseLayer;
        temporaryContextLayer = contextLayer;
    }

    public void changeLensOptions()
    {
        Vector <String> lenseOptions= new Vector <String> ();
        for (String key : application.layersIndex.keySet())
        {
            if (key != lenseLayer)
            {
                lenseOptions.add(key);
            }
        } 
        lensMenu.changeLabelsInner(lenseOptions);
    }

    public void changeContextOptions()
    {
        Vector <String> contextOptions= new Vector <String> ();
        for (String key : application.layersIndex.keySet())
        {
            if (key != contextLayer)
            {
                contextOptions.add(key);
            }
        } 
        lensMenu.changeLabelsOuter(contextOptions);
    }

    public void lensMenuChangeLayer(Glyph g)
    {
        String label = lensMenu.getLabel(g);
        if(g.getType()== Messages.LENS_MENU_LENS) {
            changeLayerLense(label);
            temporaryLenseLayer = label;
        }
        if(g.getType()== Messages.LENS_MENU_CONTEXT) {
            changeLayerContext(label);
            temporaryContextLayer = label;
        }
    }

    public void returnLensAndContext()
    {
        System.out.println("CONTEXT LAYER in returnLensAndContext "+contextLayer);
        System.out.println("LENS LAYER in returnLensAndContext "+lenseLayer);
        changeLayerContext(contextLayer);
        changeLayerLense(lenseLayer);
    }


	/* -------------- Sigma Lenses ------------------- */
    RouteLens rLens = null;

	void toggleLensType(){
        if(lensFamily == SCBGaussianLens)
        {
            lensFamily = BGaussianLens;
            if(tLens != null) {
                tLens = null;
            }
        }
        else if (lensFamily == BGaussianLens)
        {
            lensFamily = SCBGaussianLens;
        }
	    else if (lensFamily == L2_Gaussian){
	        lensFamily = L2_SCB;
	        application.ovm.say(Messages.SCB);
	    }
	    else if(lensFamily == L2_SCB) {
	        lensFamily = L2_Gaussian;
	        application.ovm.say(Messages.FISHEYE);
	    }
	}

    void setLens(int t){
        lensType = t;
    }

    void moveLens(int x, int y, long absTime){
        if (tLens != null){
            tLens.setAbsolutePosition(x, y, absTime);
        }
        else {
            lens.setAbsolutePosition(x, y);
        }
        VirtualSpaceManager.INSTANCE.repaint();
    }

    void zoomInPhase1(int x, int y){
        // create lens if it does not exist
        if (lens == null){
            lens = application.mView.setLens(getLensDefinition(x, y));
            lens.setBufferThreshold(1.5f);
        }
        Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
        setLens(ZOOMIN_LENS);
    }

    void zoomInPhase2(double mx, double my){
        // compute camera animation parameters
        double cameraAbsAlt = application.mCamera.getAltitude()+application.mCamera.getFocal();
        double c2x = mx - INV_MAG_FACTOR * (mx - application.mCamera.vx);
        double c2y = my - INV_MAG_FACTOR * (my - application.mCamera.vy);
        //Vector cadata = new Vector();
        // -(cameraAbsAlt)*(MAG_FACTOR-1)/MAG_FACTOR
        Double deltAlt = new Double((cameraAbsAlt)*(1-MAG_FACTOR)/MAG_FACTOR);
        if (cameraAbsAlt + deltAlt.floatValue() > FLOOR_ALTITUDE){
            // animate lens and camera simultaneously (lens will die at the end)
            Animation al = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
                new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(), new ZP2LensAction(this));
            Animation at = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, application.mCamera,
                new Point2D.Double(c2x-application.mCamera.vx, c2y-application.mCamera.vy), true, IdentityInterpolator.getInstance(), null);
            Animation aa = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraAltAnim(LENS_ANIM_TIME, application.mCamera,
                deltAlt, true, IdentityInterpolator.getInstance(), null);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(al, false);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(at, false);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(aa, false);
        }
        else {
            Double actualDeltAlt = new Double(FLOOR_ALTITUDE - cameraAbsAlt);
            double ratio = actualDeltAlt.doubleValue() / deltAlt.doubleValue();
            // animate lens and camera simultaneously (lens will die at the end)
            Animation al = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
                new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(), new ZP2LensAction(this));
            Animation at = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, application.mCamera,
                new Point2D.Double(Math.round((c2x-application.mCamera.vx)*ratio), Math.round((c2y-application.mCamera.vy)*ratio)), true, IdentityInterpolator.getInstance(), null);
            Animation aa = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraAltAnim(LENS_ANIM_TIME, application.mCamera,
                actualDeltAlt, true, IdentityInterpolator.getInstance(), null);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(al, false);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(at, false);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(aa, false);
        }
    }

    void zoomOutPhase1(int x, int y, double mx, double my){
        // compute camera animation parameters
        double cameraAbsAlt = application.mCamera.getAltitude()+application.mCamera.getFocal();
        double c2x = mx - MAG_FACTOR * (mx - application.mCamera.vx);
        double c2y = my - MAG_FACTOR * (my - application.mCamera.vy);
        // create lens if it does not exist
        if (lens == null){
            lens = application.mView.setLens(getLensDefinition(x, y));
            lens.setBufferThreshold(1.5f);
        }
        // animate lens and camera simultaneously
        Animation al = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
        Animation at = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, application.mCamera,
            new Point2D.Double(c2x-application.mCamera.vx, c2y-application.mCamera.vy), true, IdentityInterpolator.getInstance(), null);
        Animation aa = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraAltAnim(LENS_ANIM_TIME, application.mCamera,
            new Double(cameraAbsAlt*(MAG_FACTOR-1)), true, IdentityInterpolator.getInstance(), null);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(al, false);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(at, false);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(aa, false);
        setLens(ZOOMOUT_LENS);
    }

    void zoomOutPhase2(){
        // make lens disappear (killing anim)
        Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(), new ZP2LensAction(this));
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
    }

    void setMagFactor(double m){
        MAG_FACTOR = m;
        INV_MAG_FACTOR = 1 / MAG_FACTOR;
    }

    void magnifyFocus(double magOffset, int zooming, Camera ca){
	    double nmf = MAG_FACTOR + magOffset;
	    if (nmf <= MAX_MAG_FACTOR && nmf > 1.0f){
		    setMagFactor(nmf);
		    if (zooming == ZOOMOUT_LENS){
			    /* if unzooming, we want to keep the focus point stable, and unzoom the context
			       this means that camera altitude must be adjusted to keep altitude + lens mag
			       factor constant in the lens focus region. The camera must also be translated
			       to keep the same region of the virtual space under the focus region */
			    double a1 = application.mCamera.getAltitude();
			    lens.setMaximumMagnification((float)nmf, true);
			    /* explanation for the altitude offset computation: the projected size of an object
			       in the focus region (in lens space) should remain the same before and after the
			       change of magnification factor. The size of an object is a function of the
			       projection coefficient (see any Glyph.projectForLens() method). This means that
			       the following equation must be true, where F is the camera's focal distance, a1
			       is the camera's altitude before the move and a2 is the camera altitude after the
move:
MAG_FACTOR * F / (F + a1) = MAG_FACTOR + magOffset * F / (F + a2)
From this we can get the altitude difference (a2 - a1)                       */
			    application.mCamera.altitudeOffset((a1+application.mCamera.getFocal())*magOffset/(MAG_FACTOR-magOffset));
			    /* explanation for the X offset computation: the position in X of an object in the
			       focus region (lens space) should remain the same before and after the change of
			       magnification factor. This means that the following equation must be true (taken
			       by simplifying pc[i].lcx computation in a projectForLens() method):
			       (vx-(lensx1))*coef1 = (vx-(lensx2))*coef2
			       -- coef1 is actually MAG_FACTOR * F/(F+a1)
			       -- coef2 is actually (MAG_FACTOR + magOffset) * F/(F+a2)
			       -- lensx1 is actually camera.vx1 + ((F+a1)/F) * lens.lx
			       -- lensx2 is actually camera.vx2 + ((F+a2)/F) * lens.lx
			       Given that (MAG_FACTOR + magOffset) / (F+a2) = MAG_FACTOR / (F+a1)
			       we eventually have:
			       Xoffset = (a1 - a2) / F * lens.lx   (lens.lx being the position of the lens's center in
			       JPanel coordinates w.r.t the view's center - see Lens.java)                */
			    application.mCamera.move((a1-application.mCamera.getAltitude())/application.mCamera.getFocal()*lens.lx,
					    -(a1-application.mCamera.getAltitude())/application.mCamera.getFocal()*lens.ly);
		    }
		    else {
			    Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(WHEEL_ANIM_TIME, (FixedSizeLens)lens,
					    new Float(magOffset), true, IdentityInterpolator.getInstance(), null);
			    VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
		    }
	    }
    }

    Lens getLensDefinition(int x, int y){
        Lens res = null;
        switch (lensFamily){
            case L2_Gaussian:{
                res = new FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L2_SCB:{
                tLens = new SCBLens(1.0f, 0.5f, 1.0f, LENS_R1, x - application.panelWidth/2, y - application.panelHeight/2);
                ((SCBLens)tLens).setBoundaryColor(Color.RED);
                ((SCBLens)tLens).setObservedRegionColor(Color.RED);
                ((SCBLens)tLens).setRadii(200, 100);
                res = (Lens)tLens;
                break;
            }
            case Blense:{
                res=new BLinearLens(1.0f, 0, 1, LENS_R1, LENS_R2, x - application.VIEW_W/2, y - application.VIEW_H/2);
                break;
            }
            case BInverseCosine:{
                res=new BInverseCosineLens(1.0f, 0, 1, LENS_R1, LENS_R2, x - application.VIEW_W/2, y - application.VIEW_H/2);
                break;
            }
            case BGaussianLens:{
                res=new BGaussianLens(1.0f, 0, 1, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                break;
            }
            case SCBGaussianLens:{
                tLens = new SCBGaussianLens(1.0f, 0.0f, 1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                //((SCBGaussianLens)tLens).setBoundaryColor(Color.RED);
                //((SCBGaussianLens)tLens).setObservedRegionColor(Color.RED);
                res = (Lens)tLens;
                break;
            }
            case SCFGaussianLens:{
                tLens = new SCFGaussianLens(2.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                //((SCFGaussianLens)tLens).setBoundaryColor(Color.RED);
                //((SCFGaussianLens)tLens).setObservedRegionColor(Color.RED);
                res = (Lens)tLens;
                break;    
            }
        }
        if(application.mode == application.routeLens)
        {
            //Paths are in lenseSpace
            rLens= new RouteLens(res, application.lenseCamera);
            rLens.setP(6);
            for (DPath dp : paths)
                rLens.addRoute(dp);
        }
        return res;
    }

    void updateTranslucency(float f, int x, int y)
    {
        //System.out.println("updateTranslucency");
        float minAlpha=0.3f;
        float alpha=((BlendingLens) lens).getFocusTranslucencyValue()+f;
        //System.out.println("Alpha"+alpha);
        if(alpha<=1 && alpha>=minAlpha)
        {
            
            ((BlendingLens) lens).setFocusTranslucencyValue(alpha);
            if (lensFamily == SCBGaussianLens) {
                ((SCBGaussianLens) lens).setMaxTranslucency(alpha);
            }
            //System.out.println("BlendingLens" +((BlendingLens) lens).getFocusTranslucencyValue());
        }
        else
        {
            if(alpha>=1) ((BlendingLens) lens).setFocusTranslucencyValue(1f);
            if(alpha<=minAlpha) ((BlendingLens) lens).setFocusTranslucencyValue(minAlpha);
        }
        //System.out.println("alpha "+alpha);
        //lens.dispose();
        //zoomInPhase1(x,y);

        /*lens = application.mView.setLens(getLensDefinition(x, y));
        lens.setBufferThreshold(1.5f);*/
        Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(50, (FixedSizeLens)lens,
            new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
        setLens(ZOOMIN_LENS);
    }

    public void incrementLensRadius(int r1, int r2)
    {
        ((FixedSizeLens) lens).setMMandRadii(1, getLensOuterRadius()+r1, getLensInnerRadius()+r2,true);
    }
    
    public void reduceLensRadius(int r1, int r2)
    {
        ((FixedSizeLens) lens).setMMandRadii(1, getLensOuterRadius()-r1, getLensInnerRadius()-r2,true);
    }

    public void returnOriginalRadius()
    {
        ((FixedSizeLens) lens).setMMandRadii(1, LENS_R1, LENS_R2,true);
    }
    
    public int getLensInnerRadius()
    {
        return ((FixedSizeLens)lens).getInnerRadius();
    }
    
    public int getLensOuterRadius()
    {
        return ((FixedSizeLens)lens).getOuterRadius();
    }

    public void setLensRadius(int r1, int r2)
    {
        ((FixedSizeLens) lens).setMMandRadii(1, r1, r2,true);
    }

    /* ---------------- Screen saver ---------------------- */

    boolean screensaverEnabled = false;

    int SCREEN_SAVER_INTERVAL = 2000;

    ScreenSaver ss;
	Timer ssTimer;

    void toggleScreenSaver(){
        screensaverEnabled = !screensaverEnabled;
        ss.setEnabled(screensaverEnabled);
        application.ovm.say((screensaverEnabled) ? "Screen Saver Mode" : "Viewer Mode");
    }

}

class ScreenSaver extends TimerTask {

	TIVNavigationManager nm;
    boolean enabled = false;

	ScreenSaver(TIVNavigationManager nm){
		super();
        this.nm = nm;
	}

    void setEnabled(boolean b){
        enabled = b;
    }

	public void run(){
		if (enabled){
		    move();
		}
	}

	void move(){
	    int r = (int)Math.round(Math.random()*20);
	    if (r < 6){
	        nm.getGlobalView(null);
	    }
	    else if (r < 8){
	        nm.getHigherView();
	    }
	    else if (r < 16){
	        nm.getLowerView();
	    }
	    else if (r < 17){
	        nm.translateView(TIVNavigationManager.MOVE_UP);
	    }
	    else if (r < 18){
	        nm.translateView(TIVNavigationManager.MOVE_DOWN);
	    }
	    else if (r < 19){
	        nm.translateView(TIVNavigationManager.MOVE_LEFT);
	    }
	    else if (r <= 20){
	        nm.translateView(TIVNavigationManager.MOVE_RIGHT);
	    }
	}

}

class ZP2LensAction implements EndAction {

    TIVNavigationManager nm;

    ZP2LensAction(TIVNavigationManager nm){
	    this.nm = nm;
    }

    public void	execute(Object subject, Animation.Dimension dimension){
        (((Lens)subject).getOwningView()).setLens(null);
        ((Lens)subject).dispose();
        nm.setMagFactor(TIVNavigationManager.DEFAULT_MAG_FACTOR);
        nm.lens = null;
        nm.setLens(TIVNavigationManager.NO_LENS);
        nm.application.sm.setUpdateLevel(true);
    }

}
