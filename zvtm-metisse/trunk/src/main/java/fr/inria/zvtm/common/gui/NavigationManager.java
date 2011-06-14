package fr.inria.zvtm.common.gui;

/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Navigation.java 2769 2010-01-15 10:17:58Z epietrig $
 */



import java.awt.geom.Point2D;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;

public class NavigationManager {

    /* Navigation constants */

    public static final short MOVE_UP = 0;
    public static final short MOVE_DOWN = 1;
    public static final short MOVE_LEFT = 2;
    public static final short MOVE_RIGHT = 3;
	public final double WHEEL_ZOOMOUT_COEF = 5;
	public final double PAN_SPEED_COEF = 50.0;
        
    private Viewer application;
    
    private VirtualSpaceManager vsm;
    private Camera mCamera;
    
    public NavigationManager(Viewer app){
        this.application = app;
        vsm = VirtualSpaceManager.INSTANCE;
    }
    
    /*-------------     Navigation       -------------*/
    
    public void getGlobalView(){
		application.getView().getGlobalView(getCamera(), Config.ANIM_MOVE_LENGTH, 1.05f);
    }

    /* Higher view */
    public void getHigherView(){
        Float alt = new Float(getCamera().getAltitude() + getCamera().getFocal());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Config.ANIM_MOVE_LENGTH, getCamera(),
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Higher view */
    public void getLowerView(){
        Float alt=new Float(-(getCamera().getAltitude() + getCamera().getFocal())/2.0f);
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Config.ANIM_MOVE_LENGTH, getCamera(),
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Direction should be one of Viewer.MOVE_* */
    public void translateView(short direction){
        Point2D.Double trans;
        double[] rb = application.getView().getVisibleRegion(getCamera());
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
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(Config.ANIM_MOVE_LENGTH, getCamera(),
            trans, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

	public Camera getCamera() {
		return mCamera;
	}
	
	public void setCamera(Camera mCamera) {
		this.mCamera = mCamera;
	}
}
