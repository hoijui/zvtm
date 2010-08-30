/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix.viewer;

import java.awt.Color;
import java.awt.geom.Point2D;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

class NavigationManager {

    /* Navigation constants */

    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;
    
    Viewer application;
    
    VirtualSpaceManager vsm;
    Camera mCamera;
    Camera ovCamera;
    
    NavigationManager(Viewer app){
        this.application = app;
        vsm = VirtualSpaceManager.INSTANCE;
    }
    
    void setCamera(Camera c){
        this.mCamera = c;
    }
    
    /*-------------     Navigation       -------------*/
    
    void getGlobalView(){
		application.mView.getGlobalView(mCamera, ConfigManager.ANIM_MOVE_LENGTH, 1.05f);
    }

    /* Higher view */
    void getHigherView(){
        Float alt = new Float(mCamera.getAltitude() + mCamera.getFocal());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(ConfigManager.ANIM_MOVE_LENGTH, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Higher view */
    void getLowerView(){
        Float alt=new Float(-(mCamera.getAltitude() + mCamera.getFocal())/2.0f);
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(ConfigManager.ANIM_MOVE_LENGTH, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

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
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(ConfigManager.ANIM_MOVE_LENGTH, mCamera,
            trans, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }
    
    /* -------------- Overview ------------------- */
	
	OverviewPortal ovPortal;
	
	void createOverview(){
		ovPortal = new OverviewPortal(application.panelWidth-ConfigManager.OVERVIEW_WIDTH-1, application.panelHeight-ConfigManager.OVERVIEW_HEIGHT-1,
		                              ConfigManager.OVERVIEW_WIDTH, ConfigManager.OVERVIEW_HEIGHT, ovCamera, mCamera);
		ovPortal.setPortalListener(application.eh);
		ovPortal.setBackgroundColor(ConfigManager.BACKGROUND_COLOR);
		ovPortal.setObservedRegionColor(ConfigManager.OBSERVED_REGION_COLOR);
		ovPortal.setObservedRegionTranslucency(ConfigManager.OBSERVED_REGION_ALPHA);
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
	        ovPortal.moveTo(application.panelWidth-ConfigManager.OVERVIEW_WIDTH-1, application.panelHeight-ConfigManager.OVERVIEW_HEIGHT-1);
	    }
	}

    void toggleOverview(){
        ovPortal.setVisible(!ovPortal.isVisible());
        vsm.repaint(application.mView);
    }
    
}
