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

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.lens.*;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

import fr.inria.zuist.engine.Region;

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
    static int LENS_R1 = 100;
    static int LENS_R2 = 50;
    static final int WHEEL_ANIM_TIME = 50;
    static final int LENS_ANIM_TIME = 300;
    static double DEFAULT_MAG_FACTOR = 4.0;
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
    short lensFamily = L2_Gaussian;
    
    static final float FLOOR_ALTITUDE = 100.0f;

    TiledImageViewer application;
    
    Camera mCamera;
    VirtualSpaceManager vsm;
    
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
            double qt = Math.round((rb[1]-rb[3])/4.0);
            trans = new Point2D.Double(0,qt);
        }
        else if (direction==MOVE_DOWN){
            double qt = Math.round((rb[3]-rb[1])/4.0);
            trans = new Point2D.Double(0,qt);
        }
        else if (direction==MOVE_RIGHT){
            double qt = Math.round((rb[2]-rb[0])/4.0);
            trans = new Point2D.Double(qt,0);
        }
        else {
            // direction==MOVE_LEFT
            double qt = Math.round((rb[0]-rb[2])/4.0);
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
		ovPortal = new OverviewPortal(application.panelWidth-ow-1, application.panelHeight-oh-1, ow, oh, application.ovCamera, application.mCamera);
		ovPortal.setPortalEventHandler(application.eh);
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
        vsm.repaintNow(application.mView);
    }
    
	/* -------------- Sigma Lenses ------------------- */
	
	void toggleLensType(){
	    if (lensFamily == L2_Gaussian){
	        lensFamily = L2_SCB;
	        application.ovm.say(Messages.SCB);
	    }
	    else {
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
        VirtualSpaceManager.INSTANCE.repaintNow();
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
        double c2x = mx - INV_MAG_FACTOR * (mx - application.mCamera.posx);
        double c2y = my - INV_MAG_FACTOR * (my - application.mCamera.posy);
        //Vector cadata = new Vector();
        // -(cameraAbsAlt)*(MAG_FACTOR-1)/MAG_FACTOR
        Double deltAlt = new Double((cameraAbsAlt)*(1-MAG_FACTOR)/MAG_FACTOR);
        if (cameraAbsAlt + deltAlt.floatValue() > FLOOR_ALTITUDE){
            // animate lens and camera simultaneously (lens will die at the end)
            Animation al = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
                new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(), new ZP2LensAction(this));
            Animation at = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, application.mCamera,
                new Point2D.Double(c2x-application.mCamera.posx, c2y-application.mCamera.posy), true, IdentityInterpolator.getInstance(), null);
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
                new Point2D.Double(Math.round((c2x-application.mCamera.posx)*ratio), Math.round((c2y-application.mCamera.posy)*ratio)), true, IdentityInterpolator.getInstance(), null);
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
        double c2x = mx - MAG_FACTOR * (mx - application.mCamera.posx);
        double c2y = my - MAG_FACTOR * (my - application.mCamera.posy);
        // create lens if it does not exist
        if (lens == null){
            lens = application.mView.setLens(getLensDefinition(x, y));
            lens.setBufferThreshold(1.5f);
        }
        // animate lens and camera simultaneously
        Animation al = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
        Animation at = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, application.mCamera,
            new Point2D.Double(c2x-application.mCamera.posx, c2y-application.mCamera.posy), true, IdentityInterpolator.getInstance(), null);
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
			       -- lensx1 is actually camera.posx1 + ((F+a1)/F) * lens.lx
			       -- lensx2 is actually camera.posx2 + ((F+a2)/F) * lens.lx
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
                tLens = new SCBLens(1.0f, 0.0f, 1.0f, LENS_R1, x - application.panelWidth/2, y - application.panelHeight/2);
                ((SCBLens)tLens).setBoundaryColor(Color.RED);
                ((SCBLens)tLens).setObservedRegionColor(Color.RED);
                res = (Lens)tLens;
                break;
            }
        }
        return res;
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
