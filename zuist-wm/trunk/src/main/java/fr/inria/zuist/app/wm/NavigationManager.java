/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2009. All Rights Reserved
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

import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.lens.*;
import fr.inria.zvtm.engine.OverviewPortal;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;

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
    static final short L1_Linear = 0;
    static final short L1_InverseCosine = 1;
    static final short L1_Manhattan = 2;
    static final short L2_Gaussian = 3;
    static final short L2_Linear = 4;
    static final short L2_InverseCosine = 5;
    static final short L2_Manhattan = 6;
    static final short L2_TLinear = 7;
    static final short L2_Fading = 8;
    static final short L2_DLinear = 9;
    static final short L3_Gaussian = 10;
    static final short L3_Linear = 11;
    static final short L3_InverseCosine = 12;
    static final short L3_Manhattan = 13;
    static final short L3_TLinear = 14;
    static final short LInf_Gaussian = 15;
    static final short LInf_Linear = 16;
    static final short LInf_InverseCosine = 17;
    static final short LInf_Manhattan = 18;
    static final short LInf_TLinear = 19;
    static final short LInf_Fading = 20;
    static final short L2_Wave = 21;
    static final short LInf_Step = 22;
    static final short L2_XGaussian = 23;
    static final short L2_HLinear = 24;
    short lensFamily = L2_Gaussian;
    
    static final float FLOOR_ALTITUDE = 100.0f;

    WorldExplorer application;
    
    NavigationManager(WorldExplorer app){
        this.application = app;
    }

	/* -------------- Overview ------------------- */
	
	static final int OVERVIEW_WIDTH = 200;
	static final int OVERVIEW_HEIGHT = 100;
	static final Color OBSERVED_REGION_COLOR = Color.GREEN;
	static final float OBSERVED_REGION_ALPHA = 0.5f;
	static final Color OV_BORDER_COLOR = Color.WHITE;
	static final Color OV_INSIDE_BORDER_COLOR = Color.WHITE;
	
	OverviewPortal ovPortal;
	
	void createOverview(){
		ovPortal = new OverviewPortal(application.panelWidth-OVERVIEW_WIDTH-1, application.panelHeight-OVERVIEW_HEIGHT-1, OVERVIEW_WIDTH, OVERVIEW_HEIGHT, application.ovCamera, application.mCamera);
		ovPortal.setPortalEventHandler(application.eh);
		ovPortal.setBackgroundColor(WorldExplorer.BACKGROUND_COLOR);
		ovPortal.setObservedRegionColor(OBSERVED_REGION_COLOR);
		ovPortal.setObservedRegionTranslucency(OBSERVED_REGION_ALPHA);
		VirtualSpaceManager.INSTANCE.addPortal(ovPortal, application.mView);
		ovPortal.setBorder(Color.GREEN);
		updateOverview();
	}
	
	void updateOverview(){
		if (ovPortal != null){application.ovCamera.setLocation(ovPortal.getGlobalView());}
	}

    
	/* -------------- Sigma Lenses ------------------- */

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
//        VirtualSpaceManager.INSTANCE.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
//            lens.getID(), null);
        Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
        setLens(ZOOMIN_LENS);
    }
    
    void zoomInPhase2(long mx, long my){
        // compute camera animation parameters
        float cameraAbsAlt = application.mCamera.getAltitude()+application.mCamera.getFocal();
        long c2x = Math.round(mx - INV_MAG_FACTOR * (mx - application.mCamera.posx));
        long c2y = Math.round(my - INV_MAG_FACTOR * (my - application.mCamera.posy));
        //Vector cadata = new Vector();
        // -(cameraAbsAlt)*(MAG_FACTOR-1)/MAG_FACTOR
        Float deltAlt = new Float((cameraAbsAlt)*(1-MAG_FACTOR)/MAG_FACTOR);
        if (cameraAbsAlt + deltAlt.floatValue() > FLOOR_ALTITUDE){
            //	    cadata.add(deltAlt);
            //	    cadata.add(new LongPoint(c2x-application.mCamera.posx, c2y-application.mCamera.posy));
            // animate lens and camera simultaneously (lens will die at the end)
            //	    VirtualSpaceManager.INSTANCE.getAnimationManager().createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
            //					     lens.getID(), new ZP2LensAction(this));
            Animation al = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
                new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(), new ZP2LensAction(this));
            //	    VirtualSpaceManager.INSTANCE.getAnimationManager().createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
            //					       cadata, application.mCamera.getID(), null);
            Animation at = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, application.mCamera,
                new LongPoint(c2x-application.mCamera.posx, c2y-application.mCamera.posy), true, IdentityInterpolator.getInstance(), null);
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
            //	    cadata.add(new LongPoint(Math.round((c2x-application.mCamera.posx)*ratio),
            //				     Math.round((c2y-application.mCamera.posy)*ratio)));
            // animate lens and camera simultaneously (lens will die at the end)
            //	    VirtualSpaceManager.INSTANCE.getAnimationManager().createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
            //					     lens.getID(), new ZP2LensAction(this));
            Animation al = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
                new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(), new ZP2LensAction(this));
            //      VirtualSpaceManager.INSTANCE.getAnimationManager().createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
            //  				       cadata, application.mCamera.getID(), null);
            Animation at = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraTranslation(LENS_ANIM_TIME, application.mCamera,
                new LongPoint(Math.round((c2x-application.mCamera.posx)*ratio), Math.round((c2y-application.mCamera.posy)*ratio)), true, IdentityInterpolator.getInstance(), null);
            Animation aa = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createCameraAltAnim(LENS_ANIM_TIME, application.mCamera,
                actualDeltAlt, true, IdentityInterpolator.getInstance(), null);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(al, false);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(at, false);
            VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(aa, false);
        }
    }

    void zoomOutPhase1(int x, int y, long mx, long my){
        // compute camera animation parameters
        float cameraAbsAlt = application.mCamera.getAltitude()+application.mCamera.getFocal();
        long c2x = Math.round(mx - MAG_FACTOR * (mx - application.mCamera.posx));
        long c2y = Math.round(my - MAG_FACTOR * (my - application.mCamera.posy));
        //Vector cadata = new Vector();
        //cadata.add(new Float(cameraAbsAlt*(MAG_FACTOR-1)));
        //cadata.add(new LongPoint(c2x-application.mCamera.posx, c2y-application.mCamera.posy));
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
            new LongPoint(c2x-application.mCamera.posx, c2y-application.mCamera.posy), true, IdentityInterpolator.getInstance(), null);
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
                        float a1 = application.mCamera.getAltitude();
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
                        -- lensx1 is actually camera.posx1 + ((F+a1)/F) * lens.lx
                        -- lensx2 is actually camera.posx2 + ((F+a2)/F) * lens.lx
                        Given that (MAG_FACTOR + magOffset) / (F+a2) = MAG_FACTOR / (F+a1)
                        we eventually have:
                        Xoffset = (a1 - a2) / F * lens.lx   (lens.lx being the position of the lens's center in
                        JPanel coordinates w.r.t the view's center - see Lens.java)                */
                        application.mCamera.move(Math.round((a1-application.mCamera.getAltitude())/application.mCamera.getFocal()*lens.lx),
                        -Math.round((a1-application.mCamera.getAltitude())/application.mCamera.getFocal()*lens.ly));
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
            case L1_Linear:{
                res = new L1FSLinearLens(1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L1_InverseCosine:{
                res = new L1FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L1_Manhattan:{
                res = new L1FSManhattanLens(1.0f, LENS_R1, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L2_Gaussian:{
                res = new FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L2_Linear:{
                res = new FSLinearLens(1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L2_InverseCosine:{
                res = new FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L2_Manhattan:{
                res = new FSManhattanLens(1.0f, LENS_R1, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case LInf_Linear:{
                res = new LInfFSLinearLens(1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case LInf_InverseCosine:{
                res = new LInfFSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case LInf_Manhattan:{
                res = new LInfFSManhattanLens(1.0f, LENS_R1, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case LInf_Gaussian:{
                res = new LInfFSGaussianLens(1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L2_TLinear:{
                res = new BLinearLens(1.0f, 0.0f, 0.95f, LENS_R1, 50, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case LInf_TLinear:{
                res = new LInfBLinearLens(1.0f, 0.0f, 0.95f, LENS_R1, 100, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L3_TLinear:{
                res = new L3BLinearLens(1.0f, 0.0f, 0.95f, LENS_R1, 100, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L2_Fading:{
                tLens = new SCBLens(1.0f, 0.0f, 1.0f, LENS_R1, x - application.panelWidth/2, y - application.panelHeight/2);
                ((SCBLens)tLens).setBoundaryColor(Color.RED);
                ((SCBLens)tLens).setObservedRegionColor(Color.RED);
                res = (Lens)tLens;
                break;
            }
            case L2_DLinear:{
                tLens = new SCFLinearLens(1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                res = (Lens)tLens;
                ((FixedSizeLens)res).setInnerRadiusColor(Color.RED);
                ((FixedSizeLens)res).setOuterRadiusColor(Color.RED);
				break;
            }
            case LInf_Fading:{
                tLens = new LInfSCBLens(1.0f, 0.0f, 0.98f, LENS_R1, x - application.panelWidth/2, y - application.panelHeight/2);
                ((SCBLens)tLens).setBoundaryColor(Color.RED);
                ((SCBLens)tLens).setObservedRegionColor(Color.RED);
                res = (Lens)tLens;
                break;
            }
            case L3_Linear:{
                res = new L3FSLinearLens(1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L3_InverseCosine:{
                res = new L3FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L3_Manhattan:{
                res = new L3FSManhattanLens(1.0f, LENS_R1, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L3_Gaussian:{
                res = new L3FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L2_Wave:{
                res = new FSWaveLens(1.0f, LENS_R1, LENS_R2/2, 8, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case LInf_Step:{
                res = new LInfFSStepLens(1.0f, LENS_R1, LENS_R2, 1, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
        	case L2_XGaussian:{
        	    res = new XGaussianLens(1.0f, 0.2f, 1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
        	    tLens = null;
        	    break;
        	}
            case L2_HLinear:{
                res = new HLinearLens(1.0f, 0.0f, 0.95f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
        }
        return res;
    }
    
    void showLensChooser(){
        new LensChooser(this);
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

class LensChooser extends JFrame implements ItemListener {

    // index of lenses should correspond to short value of associated lens type in NavigationManager
    static final String[] LENS_NAMES = {"L1 / Linear", "L1 / Inverse Cosine", "L1 / Manhattan",
        "L2 / Gaussian", "L2 / Linear", "L2 / Inverse Cosine", "L2 / Manhattan", "L2 / Translucence Linear", "L2 / Fading", "L2 / Dynamic Linear",
        "L3 / Gaussian", "L3 / Linear", "L3 / Inverse Cosine", "L3 / Manhattan", "L3 / Translucence Linear",
        "LInf / Gaussian", "LInf / Linear", "LInf / Inverse Cosine", "LInf / Manhattan", "LInf / Translucence Linear", "LInf / Fading",
        "L2 / Wave", "LInf / Step", "L2 / XGaussian", "L2 / HLinear"};

    NavigationManager nm;

    JComboBox lensList;

    LensChooser(NavigationManager nm){
        super();
        this.nm = nm;
        initGUI();
        this.pack();
        this.setVisible(true);
    }
    
    void initGUI(){
        Container cp = getContentPane();
        lensList = new JComboBox(LENS_NAMES);
        lensList.setSelectedIndex(nm.lensFamily);
        lensList.addItemListener(this);
        cp.add(lensList);
    }
    
    public void itemStateChanged(ItemEvent e){
        if (e.getStateChange() == ItemEvent.SELECTED){
            Object src = e.getItem();
            for (int i=0;i<LENS_NAMES.length;i++){
                if (src == LENS_NAMES[i]){
                    nm.lensFamily = (short)i;
                    return;
                }
            }
        }
    }
    
}
