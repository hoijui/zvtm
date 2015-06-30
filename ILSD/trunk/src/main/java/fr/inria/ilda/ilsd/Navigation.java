/*   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file LICENSE.
 *
 * $Id: $
 */

package fr.inria.ilda.ilsd;


import java.awt.geom.Point2D;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

class Navigation {

    /* Navigation constants */
    static final int ANIM_MOVE_DURATION = 300;
    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;

    ILSD app;
    VirtualSpaceManager vsm;

    Navigation(ILSD app){
        this.app = app;
        vsm = VirtualSpaceManager.INSTANCE;
    }

    /* -------------- pan-zoom ------------------- */

    void getGlobalView(EndAction ea){
        app.sm.getGlobalView(app.mCamera, Navigation.ANIM_MOVE_DURATION, ea);
    }

    /* Higher view */
    void getHigherView(){
        Float alt = new Float(app.mCamera.getAltitude() + app.mCamera.getFocal());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(
                        Navigation.ANIM_MOVE_DURATION, app.mCamera, alt, true,
                        SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Lower view */
    void getLowerView(){
        Float alt = new Float(-(app.mCamera.getAltitude() + app.mCamera.getFocal())/2.0f);
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(
                        Navigation.ANIM_MOVE_DURATION, app.mCamera, alt, true,
                        SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Direction should be one of WorldExplorer.MOVE_* */
    void translateView(short direction){
        Point2D.Double trans;
        double[] rb = app.mView.getVisibleRegion(app.mCamera);
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
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(
                            Navigation.ANIM_MOVE_DURATION, app.mCamera, trans, true,
                            SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /** Camera zoom-in. Called e.g. when using the mouse wheel or circular gestures.
     *@param c camera to zoom in
     *@param idfactor input device factor
     *@param zcx center of zoom x-coord (in virtual space)
     *@param zcy center of zoom y-coord (in virtual space)
     */
    void czoomIn(Camera c, float idfactor, double zcx, double zcy){
        double a = (c.focal+Math.abs(c.altitude)) / c.focal;
        //wheelDirection == WHEEL_DOWN, zooming in
        if (c.getAltitude()-a*idfactor >= c.getZoomFloor()){
            // this test to prevent translation when camera is not actually zoming in
            c.move((zcx - c.vx) * idfactor / c.focal,
                   ((zcy - c.vy) * idfactor / c.focal));

        }
        c.altitudeOffset(-a*idfactor);
        c.getOwningView().repaint();
    }

    /** Camera zoom-out. Called e.g. when using the mouse wheel or circular gestures.
     *@param c camera to zoom in
     *@param idfactor input device factor
     *@param zcx center of zoom x-coord (in virtual space)
     *@param zcy center of zoom y-coord (in virtual space)
     */
    void czoomOut(Camera c, float idfactor, double zcx, double zcy){
        double a = (c.focal+Math.abs(c.altitude)) / c.focal;
        // zooming out
        c.move(-((zcx - c.vx) * idfactor / c.focal),
               -((zcy - c.vy) * idfactor / c.focal));
        c.altitudeOffset(a*idfactor);
        c.getOwningView().repaint();
    }

}
