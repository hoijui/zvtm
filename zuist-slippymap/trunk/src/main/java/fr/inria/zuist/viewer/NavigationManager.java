/*   Copyright (c) INRIA, 2015. All Rights Reserved
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

class NavigationManager {

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
    short lensFamily = L2_SCB;

    static final float FLOOR_ALTITUDE = 100.0f;

    SlippyMapViewer application;
    VirtualSpaceManager vsm;

    NavigationManager(SlippyMapViewer app){
        this.application = app;
        vsm = VirtualSpaceManager.INSTANCE;
    }

    /* -------------- pan-zoom ------------------- */

    void getGlobalView(EndAction ea){
        // application.mView.getGlobalView(application.mCamera, NavigationManager.ANIM_MOVE_DURATION);
        application.sm.getGlobalView(application.mCamera, NavigationManager.ANIM_MOVE_DURATION, ea);
    }

    /* Higher view */
    void getHigherView(){
        Float alt = new Float(application.mCamera.getAltitude() + application.mCamera.getFocal());
        //vsm.animator.createCameraAnimation(NavigationManager.ANIM_MOVE_DURATION, AnimManager.CA_ALT_SIG, alt, bCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(NavigationManager.ANIM_MOVE_DURATION, application.mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Higher view */
    void getLowerView(){
        Float alt=new Float(-(application.mCamera.getAltitude() + application.mCamera.getFocal())/2.0f);
        //vsm.animator.createCameraAnimation(NavigationManager.ANIM_MOVE_DURATION, AnimManager.CA_ALT_SIG, alt, bCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(NavigationManager.ANIM_MOVE_DURATION, application.mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Direction should be one of WorldExplorer.MOVE_* */
    void translateView(short direction){
        Point2D.Double trans;
        double[] rb = application.mView.getVisibleRegion(application.mCamera);
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
        //vsm.animator.createCameraAnimation(NavigationManager.ANIM_MOVE_DURATION, AnimManager.CA_TRANS_SIG, trans, bCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(NavigationManager.ANIM_MOVE_DURATION, application.mCamera,
            trans, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
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

    void createOverview(){
        int ow = MAX_OVERVIEW_WIDTH;
        int oh = MAX_OVERVIEW_HEIGHT;
        Region[] rootRegions = application.sm.getRegionsAtLevel(0);
        if (rootRegions != null){
            float ar = (float)(rootRegions[0].getWidth() / rootRegions[0].getHeight());
            if (ar > 1){
                // wider than high
                oh = Math.round(ow/ar);
            }
            else {
                // higher than wide
                ow = Math.round(oh*ar);
            }
        }
        else {
            ow = 1;
            oh = 1;
        }
        ovPortal = new OverviewPortal(application.panelWidth-ow-1, application.panelHeight-oh-1, ow, oh, application.ovCamera, application.mCamera);
        ovPortal.setPortalListener(application.eh);
        ovPortal.setBackgroundColor(SlippyMapViewer.BACKGROUND_COLOR);
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
                ovPortal.centerOnRegion(NavigationManager.ANIM_MOVE_DURATION, scene_bounds[0], scene_bounds[1], scene_bounds[2], scene_bounds[3]);
            }
        }
    }

}
