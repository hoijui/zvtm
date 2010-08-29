/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.wm;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JFrame;
import javax.swing.JComboBox;
import java.awt.geom.Point2D;

import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.lens.*;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zuist.engine.Region;

class NavigationManager {

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
    short lensFamily = L2_SCB;
    
    static final float FLOOR_ALTITUDE = 100.0f;

    WorldExplorer application;
    
    NavigationManager(WorldExplorer app){
        this.application = app;
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
	    float ar = (float)(rootRegion.getWidth() / rootRegion.getHeight());
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
		ovPortal.setPortalListener(application.eh);
		ovPortal.setBackgroundColor(WorldExplorer.BACKGROUND_COLOR);
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
    	        ovPortal.centerOnRegion(WorldExplorer.ANIM_MOVE_DURATION, scene_bounds[0], scene_bounds[1], scene_bounds[2], scene_bounds[3]);		
    		}
		}
	}
	
	/* -------------- Sigma Lenses ------------------- */

    void toggleLensType(){
	    if (lensFamily == L2_Gaussian){
	        lensFamily = L2_SCB;
	        //application.ovm.say(Messages.SCB);
	    }
	    else {
	        lensFamily = L2_Gaussian;
	        //application.ovm.say(Messages.FISHEYE);
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
//        VirtualSpaceManager.INSTANCE.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
//            lens.getID(), null);
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
        Float deltAlt = new Float((cameraAbsAlt)*(1-MAG_FACTOR)/MAG_FACTOR);
        if (cameraAbsAlt + deltAlt.floatValue() > FLOOR_ALTITUDE){
            //	    cadata.add(deltAlt);
            //	    cadata.add(new LongPoint(c2x-application.mCamera.vx, c2y-application.mCamera.vy));
            // animate lens and camera simultaneously (lens will die at the end)
            //	    VirtualSpaceManager.INSTANCE.getAnimationManager().createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
            //					     lens.getID(), new ZP2LensAction(this));
            Animation al = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
                new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(), new ZP2LensAction(this));
            //	    VirtualSpaceManager.INSTANCE.getAnimationManager().createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
            //					       cadata, application.mCamera.getID(), null);
            Animation at = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, application.mCamera,
                new Point2D.Double(c2x-application.mCamera.vx, c2y-application.mCamera.vy), true, IdentityInterpolator.getInstance(), null);
            Animation aa = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraAltAnim(LENS_ANIM_TIME, application.mCamera,
                deltAlt, true, IdentityInterpolator.getInstance(), null);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(al, false);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(at, false);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(aa, false);
        }
        else {
            Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
            double ratio = actualDeltAlt.floatValue() / deltAlt.floatValue();
            //	    cadata.add(actualDeltAlt);
            //	    cadata.add(new LongPoint(Math.round((c2x-application.mCamera.vx)*ratio),
            //				     Math.round((c2y-application.mCamera.vy)*ratio)));
            // animate lens and camera simultaneously (lens will die at the end)
            //	    VirtualSpaceManager.INSTANCE.getAnimationManager().createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
            //					     lens.getID(), new ZP2LensAction(this));
            Animation al = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
                new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(), new ZP2LensAction(this));
            //      VirtualSpaceManager.INSTANCE.getAnimationManager().createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
            //  				       cadata, application.mCamera.getID(), null);
            Animation at = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, application.mCamera,
                new Point2D.Double((c2x-application.mCamera.vx)*ratio, (c2y-application.mCamera.vy)*ratio), true, IdentityInterpolator.getInstance(), null);
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
        //Vector cadata = new Vector();
        //cadata.add(new Float(cameraAbsAlt*(MAG_FACTOR-1)));
        //cadata.add(new LongPoint(c2x-application.mCamera.vx, c2y-application.mCamera.vy));
        // create lens if it does not exist
        if (lens == null){
            lens = application.mView.setLens(getLensDefinition(x, y));
            lens.setBufferThreshold(1.5f);
        }
        // animate lens and camera simultaneously
        //        VirtualSpaceManager.INSTANCE.getAnimationManager().createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
        //            lens.getID(), null);
        Animation al = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
        //        VirtualSpaceManager.INSTANCE.getAnimationManager().createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
        //            cadata, application.mCamera.getID(), null);
        Animation at = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, application.mCamera,
            new Point2D.Double(c2x-application.mCamera.vx, c2y-application.mCamera.vy), true, IdentityInterpolator.getInstance(), null);
        Animation aa = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraAltAnim(LENS_ANIM_TIME, application.mCamera,
            new Float(cameraAbsAlt*(MAG_FACTOR-1)), true, IdentityInterpolator.getInstance(), null);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(al, false);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(at, false);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(aa, false);
        setLens(ZOOMOUT_LENS);
    }

    void zoomOutPhase2(){
        // make lens disappear (killing anim)
        //        VirtualSpaceManager.INSTANCE.getAnimationManager().createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
        //            lens.getID(), new ZP2LensAction(this));
        Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(), new ZP2LensAction(this));
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
    }

    void setMagFactor(double m){
        MAG_FACTOR = m;
        INV_MAG_FACTOR = 1 / MAG_FACTOR;
    }

    synchronized void magnifyFocus(double magOffset, int zooming, Camera ca){
        synchronized (lens){
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
                        application.mCamera.altitudeOffset((float)((a1+application.mCamera.getFocal())*magOffset/(MAG_FACTOR-magOffset)));
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
//                    VirtualSpaceManager.INSTANCE.animator.createLensAnimation(WHEEL_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(magOffset),
//                        lens.getID(), null);
                    Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(WHEEL_ANIM_TIME, (FixedSizeLens)lens,
                        new Float(magOffset), true, IdentityInterpolator.getInstance(), null);
                    VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
                }
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
    
}

class ZP2LensAction implements EndAction {

    NavigationManager nm;
    
    ZP2LensAction(NavigationManager nm){
	    this.nm = nm;
    }
    
    public void	execute(Object subject, Animation.Dimension dimension){
        ((Lens)subject).getOwningView().setLens(null);
        ((Lens)subject).dispose();
        nm.setMagFactor(NavigationManager.DEFAULT_MAG_FACTOR);
        nm.lens = null;
        nm.setLens(NavigationManager.NO_LENS);
        nm.application.sm.setUpdateLevel(true);
    }
    
}
