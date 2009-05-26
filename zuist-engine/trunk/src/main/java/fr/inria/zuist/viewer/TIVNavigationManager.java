/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: TIVNavigationManager.java 1780 2009-05-12 12:36:30Z epietrig $
 */

package fr.inria.zuist.viewer;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JFrame;
import javax.swing.JComboBox;

import java.util.Vector;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import net.claribole.zvtm.lens.*;
import net.claribole.zvtm.engine.OverviewPortal;
import net.claribole.zvtm.animation.EndAction;
import net.claribole.zvtm.animation.Animation;
import net.claribole.zvtm.animation.interpolation.IdentityInterpolator;

import fr.inria.zuist.engine.Region;

class TIVNavigationManager {

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
    
    static final float FLOOR_ALTITUDE = 0.0f;

    TiledImageViewer application;
    
    TIVNavigationManager(TiledImageViewer app){
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
	
	void createOverview(Region rootRegion){
	    int ow, oh;
	    float ar = rootRegion.getWidth()/(float)rootRegion.getHeight();
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
		    //application.ovCamera.setLocation(ovPortal.getGlobalView());
		    int l = 0;
    		while (application.sm.getRegionsAtLevel(l) == null){
    			l++;
    			if (l > application.sm.getLevelCount()){
    				l = -1;
    				break;
    			}
    		}
    		if (l > -1){
    			long[] wnes = application.sm.getLevel(l).getBounds();
    	        ovPortal.centerOnRegion(TiledImageViewer.ANIM_MOVE_DURATION, wnes[0], wnes[1], wnes[2], wnes[3]);		
    		}
		}
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
            // animate lens and camera simultaneously (lens will die at the end)
            Animation al = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
                new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(), new ZP2LensAction(this));
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
            // animate lens and camera simultaneously (lens will die at the end)
            Animation al = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
                new Float(-MAG_FACTOR+1), true, IdentityInterpolator.getInstance(), new ZP2LensAction(this));
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
        // create lens if it does not exist
        if (lens == null){
            lens = application.mView.setLens(getLensDefinition(x, y));
            lens.setBufferThreshold(1.5f);
        }
        // animate lens and camera simultaneously
        Animation al = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
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
    
    void showLensChooser(){
        new LensChooser(this);
    }

}

class ZP2LensAction implements EndAction {

    TIVNavigationManager nm;
    
    ZP2LensAction(TIVNavigationManager nm){
	    this.nm = nm;
    }
    
    public void	execute(Object subject, Animation.Dimension dimension){
        VirtualSpaceManager.INSTANCE.getOwningView(((Lens)subject).getID()).setLens(null);
        ((Lens)subject).dispose();
        nm.setMagFactor(TIVNavigationManager.DEFAULT_MAG_FACTOR);
        nm.lens = null;
        nm.setLens(TIVNavigationManager.NO_LENS);
        nm.application.sm.setUpdateLevel(true);
    }
    
}

class LensChooser extends JFrame implements ItemListener {

    // index of lenses should correspond to short value of associated lens type in TIVNavigationManager
    static final String[] LENS_NAMES = {"Fisheye Lens", "Speed-coupled Blending Lens"};

    TIVNavigationManager nm;

    JComboBox lensList;

    LensChooser(TIVNavigationManager nm){
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
