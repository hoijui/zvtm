/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Navigation.java 2769 2010-01-15 10:17:58Z epietrig $
 */

package fr.inria.zvtm.alma;

import java.awt.Color;
import java.awt.geom.Point2D;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.lens.*;

class Navigation {

    /* Navigation constants */

    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;
    
    /* lens */
    /* misc. lens settings */
    SCBLens lens;
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
    
    static final float FLOOR_ALTITUDE = 100.0f;
    
    /* main zvtm components */
    Viewer application;
    
    VirtualSpaceManager vsm;
    Camera mCamera;
    Camera ovCamera;
    
    Navigation(Viewer app){
        this.application = app;
        vsm = VirtualSpaceManager.INSTANCE;
    }
    
    void setCamera(Camera c){
        this.mCamera = c;
    }
    
    /*-------------     Navigation       -------------*/
    
    void getGlobalView(){
		application.mView.getGlobalView(mCamera, Config.ANIM_MOVE_LENGTH, 1.05f);
    }

    /* Higher view */
    void getHigherView(){
        Float alt = new Float(mCamera.getAltitude() + mCamera.getFocal());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Config.ANIM_MOVE_LENGTH, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Higher view */
    void getLowerView(){
        Float alt=new Float(-(mCamera.getAltitude() + mCamera.getFocal())/2.0f);
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Config.ANIM_MOVE_LENGTH, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Direction should be one of Viewer.MOVE_* */
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
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(Config.ANIM_MOVE_LENGTH, mCamera,
            trans, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }
    
    /* -------------- Overview ------------------- */
	
	OverviewPortal ovPortal;
	
	void createOverview(){
		ovPortal = new OverviewPortal(application.panelWidth-Config.OVERVIEW_WIDTH-1, application.panelHeight-Config.OVERVIEW_HEIGHT-1,
		                              Config.OVERVIEW_WIDTH, Config.OVERVIEW_HEIGHT, ovCamera, mCamera);
		ovPortal.setPortalListener(application.eh);
		ovPortal.setBackgroundColor(Config.BACKGROUND_COLOR);
		ovPortal.setObservedRegionColor(Config.OBSERVED_REGION_COLOR);
		ovPortal.setObservedRegionTranslucency(Config.OBSERVED_REGION_ALPHA);
		VirtualSpaceManager.INSTANCE.addPortal(ovPortal, application.mView);
		ovPortal.setBorder(Color.GREEN);
		updateOverview();
	}
	
	void updateOverview(){
		if (ovPortal != null){
		    ovCamera.setLocation(ovPortal.getGlobalView());
		}
	}
	
	void updateOverviewLocation(){
	    if (ovPortal != null){
	        ovPortal.moveTo(application.panelWidth-Config.OVERVIEW_WIDTH-1, application.panelHeight-Config.OVERVIEW_HEIGHT-1);
	    }
	}

    void toggleOverview(){
        ovPortal.setVisible(!ovPortal.isVisible());
        vsm.repaint(application.mView);
    }
    
    /* -------------- Sigma Lenses ------------------- */

    void setLens(int t){
        lensType = t;
    }

    void moveLens(int x, int y, long absTime){
        lens.setAbsolutePosition(x, y, absTime);
        VirtualSpaceManager.INSTANCE.repaint();
    }

    void zoomInPhase1(int x, int y){
        // create lens if it does not exist
        if (lens == null){
            lens = (SCBLens)application.mView.setLens(getLensDefinition(x, y));
            lens.setBufferThreshold(1.5f);
        }
        Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
        setLens(ZOOMIN_LENS);
    }

    void zoomInPhase2(double mx, double my){
        // compute camera animation parameters
        double cameraAbsAlt = mCamera.getAltitude()+mCamera.getFocal();
        double c2x = mx - INV_MAG_FACTOR * (mx - mCamera.vx);
        double c2y = my - INV_MAG_FACTOR * (my - mCamera.vy);
        //Vector cadata = new Vector();
        // -(cameraAbsAlt)*(MAG_FACTOR-1)/MAG_FACTOR
        Double deltAlt = new Double((cameraAbsAlt)*(1-MAG_FACTOR)/MAG_FACTOR);
        if (cameraAbsAlt + deltAlt.floatValue() > FLOOR_ALTITUDE){
            // animate lens and camera simultaneously (lens will die at the end)
            Animation al = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
                new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(), new ZP2LensAction(this));
            Animation at = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, mCamera,
                new Point2D.Double(c2x-mCamera.vx, c2y-mCamera.vy), true, IdentityInterpolator.getInstance(), null);
            Animation aa = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraAltAnim(LENS_ANIM_TIME, mCamera,
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
            Animation at = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, mCamera,
                new Point2D.Double(Math.round((c2x-mCamera.vx)*ratio), Math.round((c2y-mCamera.vy)*ratio)), true, IdentityInterpolator.getInstance(), null);
            Animation aa = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraAltAnim(LENS_ANIM_TIME, mCamera,
                actualDeltAlt, true, IdentityInterpolator.getInstance(), null);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(al, false);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(at, false);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(aa, false);
        }
    }

    void zoomOutPhase1(int x, int y, double mx, double my){
        // compute camera animation parameters
        double cameraAbsAlt = mCamera.getAltitude()+mCamera.getFocal();
        double c2x = mx - MAG_FACTOR * (mx - mCamera.vx);
        double c2y = my - MAG_FACTOR * (my - mCamera.vy);
        // create lens if it does not exist
        if (lens == null){
            lens = (SCBLens)application.mView.setLens(getLensDefinition(x, y));
            lens.setBufferThreshold(1.5f);
        }
        // animate lens and camera simultaneously
        Animation al = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
        Animation at = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, mCamera,
            new Point2D.Double(c2x-mCamera.vx, c2y-mCamera.vy), true, IdentityInterpolator.getInstance(), null);
        Animation aa = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraAltAnim(LENS_ANIM_TIME, mCamera,
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
                double a1 = mCamera.getAltitude();
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
                mCamera.altitudeOffset((a1+mCamera.getFocal())*magOffset/(MAG_FACTOR-magOffset));
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
                mCamera.move((a1-mCamera.getAltitude())/mCamera.getFocal()*lens.lx,
                -(a1-mCamera.getAltitude())/mCamera.getFocal()*lens.ly);
            }
            else {
                Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(WHEEL_ANIM_TIME, (FixedSizeLens)lens,
                    new Float(magOffset), true, IdentityInterpolator.getInstance(), null);
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
            }
        }
    }

    Lens getLensDefinition(int x, int y){
        SCBLens l = new SCBLens(1.0f, 0.0f, 1.0f, LENS_R1, x - application.panelWidth/2, y - application.panelHeight/2);
        l.setBoundaryColor(Color.RED);
        l.setObservedRegionColor(Color.RED);
        return l;
    }

}

class ZP2LensAction implements EndAction {

    Navigation nm;
    
    ZP2LensAction(Navigation nm){
	    this.nm = nm;
    }
    
    public void	execute(Object subject, Animation.Dimension dimension){
        (((Lens)subject).getOwningView()).setLens(null);
        ((Lens)subject).dispose();
        nm.setMagFactor(Navigation.DEFAULT_MAG_FACTOR);
        nm.lens = null;
        nm.setLens(Navigation.NO_LENS);
    }
    
}
