/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.atc;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JFrame;
import javax.swing.JComboBox;

import java.util.Arrays;
import java.util.Vector;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Iterator;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.ClosedShape;
import com.xerox.VTM.glyphs.Translucent;
import net.claribole.zvtm.glyphs.DPathST;
import net.claribole.zvtm.lens.*;
import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.engine.PostAnimationAdapter;
import net.claribole.zvtm.engine.OverviewPortal;

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
    static final short L2_TWave = 22;
    static final short LInf_Step = 23;
    static final short L2_XGaussian = 24;
    static final short L2_HLinear = 25;
    short lensFamily = L2_Gaussian;
    
    static final float FLOOR_ALTITUDE = 100.0f;

    ATCExplorer application;

    NavigationManager(ATCExplorer app){
        this.application = app;
    }

	/* -------------- Overview ------------------- */
	
	static final int OVERVIEW_WIDTH = 200;
	static final int OVERVIEW_HEIGHT = 100;
	static final Color OBSERVED_REGION_COLOR = Color.GREEN;
	static final float OBSERVED_REGION_ALPHA = 0.5f;
	static final Color OV_BORDER_COLOR = Color.GREEN;
	static final Color OV_INSIDE_BORDER_COLOR = Color.WHITE;
	
	OverviewPortal ovPortal;
	
	void createOverview(){
		ovPortal = new OverviewPortal(application.panelWidth-OVERVIEW_WIDTH-1, application.panelHeight-OVERVIEW_HEIGHT-1, OVERVIEW_WIDTH, OVERVIEW_HEIGHT, application.ovCamera, application.mCamera);
		ovPortal.setPortalEventHandler(application.eh);
		ovPortal.setBackgroundColor(ATCExplorer.BACKGROUND_COLOR);
		ovPortal.setObservedRegionColor(OBSERVED_REGION_COLOR);
		ovPortal.setObservedRegionTranslucency(OBSERVED_REGION_ALPHA);
		application.vsm.addPortal(ovPortal, application.mView);
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
        application.vsm.repaintNow();
    }

    void zoomInPhase1(int x, int y){
        // create lens if it does not exist
        if (lens == null){
            lens = application.mView.setLens(getLensDefinition(x, y));
            lens.setBufferThreshold(1.5f);
        }
        application.vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
            lens.getID(), null);
        setLens(ZOOMIN_LENS);
    }
    
    void zoomInPhase2(long mx, long my){
        // compute camera animation parameters
        float cameraAbsAlt = application.mCamera.getAltitude()+application.mCamera.getFocal();
        long c2x = Math.round(mx - INV_MAG_FACTOR * (mx - application.mCamera.posx));
        long c2y = Math.round(my - INV_MAG_FACTOR * (my - application.mCamera.posy));
        Vector cadata = new Vector();
        // -(cameraAbsAlt)*(MAG_FACTOR-1)/MAG_FACTOR
        Float deltAlt = new Float((cameraAbsAlt)*(1-MAG_FACTOR)/MAG_FACTOR);
        if (cameraAbsAlt + deltAlt.floatValue() > FLOOR_ALTITUDE){
            cadata.add(deltAlt);
            cadata.add(new LongPoint(c2x-application.mCamera.posx, c2y-application.mCamera.posy));
            // animate lens and camera simultaneously (lens will die at the end)
            application.vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
                lens.getID(), new ZP2LensAction(this));
            application.vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
                cadata, application.mCamera.getID());
        }
        else {
            Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
            double ratio = actualDeltAlt.floatValue() / deltAlt.floatValue();
            cadata.add(actualDeltAlt);
            cadata.add(new LongPoint(Math.round((c2x-application.mCamera.posx)*ratio),
                Math.round((c2y-application.mCamera.posy)*ratio)));
            // animate lens and camera simultaneously (lens will die at the end)
            application.vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
                lens.getID(), new ZP2LensAction(this));
            application.vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
                cadata, application.mCamera.getID());
        }
    }

    void zoomOutPhase1(int x, int y, long mx, long my){
        application.sm.setUpdateLevel(false);
        // compute camera animation parameters
        float cameraAbsAlt = application.mCamera.getAltitude()+application.mCamera.getFocal();
        long c2x = Math.round(mx - MAG_FACTOR * (mx - application.mCamera.posx));
        long c2y = Math.round(my - MAG_FACTOR * (my - application.mCamera.posy));
        Vector cadata = new Vector();
        cadata.add(new Float(cameraAbsAlt*(MAG_FACTOR-1)));
        cadata.add(new LongPoint(c2x-application.mCamera.posx, c2y-application.mCamera.posy));
        // create lens if it does not exist
        if (lens == null){
            lens = application.mView.setLens(getLensDefinition(x, y));
            lens.setBufferThreshold(1.5f);
        }
        // animate lens and camera simultaneously
        application.vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
            lens.getID(), null);
        application.vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
            cadata, application.mCamera.getID(), null);
        setLens(ZOOMOUT_LENS);
    }

    void zoomOutPhase2(){
        // make lens disappear (killing anim)
        application.vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
            lens.getID(), new ZP2LensAction(this));
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
                    application.vsm.animator.createLensAnimation(WHEEL_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(magOffset),
                        lens.getID(), null);
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
                res = new TLinearLens(1.0f, 0.0f, 0.95f, LENS_R1, 50, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case LInf_TLinear:{
                res = new LInfTLinearLens(1.0f, 0.0f, 0.95f, LENS_R1, 100, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L3_TLinear:{
                res = new L3TLinearLens(1.0f, 0.0f, 0.95f, LENS_R1, 100, x - application.panelWidth/2, y - application.panelHeight/2);
                tLens = null;
                break;
            }
            case L2_Fading:{
                tLens = new TFadingLens(1.0f, 0.0f, 1.0f, LENS_R1, x - application.panelWidth/2, y - application.panelHeight/2);
                ((TFadingLens)tLens).setBoundaryColor(Color.RED);
                ((TFadingLens)tLens).setObservedRegionColor(Color.RED);
                res = (Lens)tLens;
                break;
            }
            case L2_DLinear:{
                tLens = new DLinearLens(1.0f, LENS_R1, LENS_R2, x - application.panelWidth/2, y - application.panelHeight/2);
                res = (Lens)tLens;
                ((FixedSizeLens)res).setInnerRadiusColor(Color.RED);
                ((FixedSizeLens)res).setOuterRadiusColor(Color.RED);
				break;
            }
            case LInf_Fading:{
                tLens = new LInfTFadingLens(1.0f, 0.0f, 0.98f, LENS_R1, x - application.panelWidth/2, y - application.panelHeight/2);
                ((TFadingLens)tLens).setBoundaryColor(Color.RED);
                ((TFadingLens)tLens).setObservedRegionColor(Color.RED);
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
            case L2_TWave:{
                res = new TWaveLens(1.0f, 0.0f, 0.95f, 200, 40, 10, x - application.panelWidth/2, y - application.panelHeight/2);
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

	/* ---------------------- Bring and go -----------------------------*/
	
	static final int BRING_ANIM_DURATION = 400;
	static final int FOLLOW_ANIM_DURATION = 1000;
	static final double BRING_DISTANCE_FACTOR = 1.5;
	
	static final float SECOND_BNG_STEP_TRANSLUCENCY = 0.2f;
	static final float OUTSIDE_BNG_SCOPE_TRANSLUCENCY = 0.05f;
	static final float[] FADE_IN_ANIM = {0,0,0,0,0,0,1-OUTSIDE_BNG_SCOPE_TRANSLUCENCY};
	static final float[] FADE_OUT_ANIM = {0,0,0,0,0,0,OUTSIDE_BNG_SCOPE_TRANSLUCENCY-1};
	
	static final Color BNG_SHAPE_FILL_COLOR = Color.RED;
	
	boolean isBringingAndGoing = false;
	
	Vector broughtStack = new Vector();
	
	HashMap brought2location = new HashMap();
	HashMap broughtnode2broughtby = new HashMap();
	
	Vector nodesOutsideScope;
	Vector arcsOutsideScope;
	
	void attemptToBring(Glyph g){
		LNode n = (LNode)g.getOwner();
		if (n == null){return;}
		int iis = broughtStack.indexOf(n);
		if (iis == -1){
			// entered a node that is not on the stack of nodes visited during this bring and go
			bringFor(n);
			// if there was a previous node in the stack, send back its brought nodes where they belong
			// (provided they are not brought by the new one we've just been working on)
			// do it only for previous node on stack; those before should have been taken care of when the
			// previous one was dealt with in here
			if (broughtStack.size() > 1){fadeStack((LNode)broughtStack.elementAt(broughtStack.size()-2), n);}
		}
		else {
			// entered a node previously visited during this bring and go
			// send back all nodes and arcs brought by subsequent steps of the bring and go
			for (int i=broughtStack.size()-1;i>=iis+1;i--){
				LNode n2 = (LNode)broughtStack.elementAt(i);
				n2.getShape().setColor(GraphManager.SHAPE_FILL_COLOR);
				broughtStack.remove(n2);
				sendBackFor(n2, n, n2.getArcLeadingTo(n) == null);
			}
		}
	}

	void bringFor(Glyph g){
		nodesOutsideScope = new Vector(Arrays.asList(application.grm.allNodes));
		arcsOutsideScope = new Vector(Arrays.asList(application.grm.allArcs));
		bringFor((LNode)g.getOwner());
		application.updateBreadCrumb();
	}
		
	void bringFor(LNode n){
		if (n == null){return;}
		isBringingAndGoing = true;
		broughtStack.add(n);
		ClosedShape thisEndShape = n.getShape();
		nodesOutsideScope.remove(n);
		thisEndShape.setColor(BNG_SHAPE_FILL_COLOR);
		double thisEndBoundingCircleRadius = thisEndShape.getSize();
		// distance between two rings
		double RING_STEP = 4 * thisEndBoundingCircleRadius;
		LEdge[] arcs = n.getAllArcs();
		// sort them according to distance from start node
		// (so as to try to keep the closest ones closer to the start node)
		Arrays.sort(arcs, new DistanceComparator(n));
		Hashtable node2bposition = new Hashtable();
		RingManager rm = new RingManager();
		Vector arcsToBring = new Vector();
		// compute the position of nodes to be brought
		for (int i=0;i<arcs.length;i++){
			if (arcs[i].isLoop()){continue;}
			LNode otherEnd = arcs[i].getOtherEnd(n);
			// do not bring nodes that are on the bring and go stack (these nodes are frozen during the whole bring and go)
			// otherwise it would be a mess (they would move when entering one of the nodes brought by them)
			if (broughtStack.contains(otherEnd)){continue;}
			ClosedShape otherEndShape = otherEnd.getShape();
			double d = Math.sqrt(Math.pow(otherEndShape.vx-thisEndShape.vx, 2) + Math.pow(otherEndShape.vy-thisEndShape.vy, 2));
			Ring ring = rm.getRing(Math.atan2(otherEndShape.vy-thisEndShape.vy, otherEndShape.vx-thisEndShape.vx), otherEndShape.getSize(), RING_STEP);
			double bd = ring.rank * RING_STEP;			
			double ratio = bd / d;
			long bx = thisEndShape.vx + Math.round(ratio * (otherEndShape.vx-thisEndShape.vx));
			long by = thisEndShape.vy + Math.round(ratio * (otherEndShape.vy-thisEndShape.vy));
			arcsToBring.add(arcs[i]);
			node2bposition.put(otherEnd, new LongPoint(bx, by));
		}
		// actually bring the arcs and nodes
		for (int i=0;i< arcsToBring.size();i++){
			LEdge e = (LEdge)arcsToBring.elementAt(i);
			LNode otherEnd = e.getOtherEnd(n);
			ClosedShape otherEndShape = otherEnd.getShape();
			bring(e, otherEnd, n, thisEndShape.vx, thisEndShape.vy, otherEndShape.vx, otherEndShape.vy, node2bposition);
		}
		// fade elements outside bring and go scope
		for (int i=0;i<arcsOutsideScope.size();i++){
			((LEdge)arcsOutsideScope.elementAt(i)).setTranslucency(OUTSIDE_BNG_SCOPE_TRANSLUCENCY);
		}
		for (int i=0;i<nodesOutsideScope.size();i++){
			((LNode)nodesOutsideScope.elementAt(i)).setTranslucency(OUTSIDE_BNG_SCOPE_TRANSLUCENCY);
		}
	}
	
	// n1 is the node for which we attempt to send back connected nodes
	// n2 is the new center of the bring and go, so we do not send back nodes connected to n1 that are also connected to n2
	void sendBackFor(LNode n1, LNode n2, boolean nodeItself){
		if (nodeItself){
			BroughtElement be = (BroughtElement)brought2location.get(n1);
			be.restorePreviousState(application.vsm.animator, BRING_ANIM_DURATION);
			// get the remembered location as the animation won't have finished before we need that
			// location to compute new edge end points
			updateEdges(n1, ((BroughtNode)be).previousLocations[0]);
			Vector nodesToSendBack = new Vector();
			synchronized(broughtnode2broughtby){
				Iterator it = broughtnode2broughtby.keySet().iterator();
				while (it.hasNext()){
					LNode n = (LNode)it.next();
					if (n != n2 && broughtnode2broughtby.get(n) == n1){
						// do not send back n2, obviously
						nodesToSendBack.add(n);
					}
				}
				for (int i=0;i<nodesToSendBack.size();i++){
					LNode n = (LNode)nodesToSendBack.elementAt(i);
					sendBack(n);
					sendBack(n.getArcLeadingTo(n1));
					broughtnode2broughtby.remove(n);				
				}
			}
		}
		else {
//			Vector nodesToSendBack = new Vector();
//			synchronized(broughtnode2broughtby){
//				Iterator it = broughtnode2broughtby.keySet().iterator();
//				while (it.hasNext()){
//					LNode n = (LNode)it.next();
//					if (n != n2 && broughtnode2broughtby.get(n) == n1){
//						// do not send back n2, obviously
//						nodesToSendBack.add(n);
//					}
//				}
//				for (int i=0;i<nodesToSendBack.size();i++){
//					LNode n = (LNode)nodesToSendBack.elementAt(i);
//					sendBack(n);
//					sendBack(n.getArcLeadingTo(n1));
//					broughtnode2broughtby.remove(n);				
//				}
//			}			
		}
	}
	
	void updateEdges(LNode n, LongPoint p){
		LEdge[] arcs = n.getAllArcs();
		for (int i=0;i<arcs.length;i++){
			DPathST spline = arcs[i].edgeSpline;
			LNode oe = arcs[i].getOtherEnd(n);
			LongPoint asp = spline.getStartPoint();
			LongPoint aep = spline.getEndPoint();
			LongPoint sp, ep;
			if (Math.sqrt(Math.pow(p.x-aep.x,2) + Math.pow(p.y-aep.y,2)) < Math.sqrt(Math.pow(p.x-asp.x,2) + Math.pow(p.y-asp.y,2))){
				sp = oe.getShape().getLocation();
				ep = p;
			}
			else {
				sp = p;
				ep = oe.getShape().getLocation();				
			}
			LongPoint[] splineCoords = DPathST.getFlattenedCoordinates(spline, sp, ep, true);
			application.vsm.animator.createPathAnimation(BRING_ANIM_DURATION, AnimManager.DP_TRANS_SIG_ABS,
			                                             splineCoords, spline.getID(), null);
		}
	}
	
	void endBringAndGo(Glyph g){
		//XXX:TBW if g is null, or not the latest node in the bring and go stack, go back to initial state
		//        else send all nodes and edges to their initial position, but also move camera to g
		isBringingAndGoing = false;
		if (g != null){
			LNode n = (LNode)g.getOwner();
			BroughtNode bn = (BroughtNode)brought2location.get(n);
			if (bn != null && broughtStack.indexOf(n) != 0){
				// do not translate if node in which we release button is the node
				// in which the bring and go was inititated (unlikely the user wants to move)
				//XXX:TBW add more tests to do this translation only if its worth it (far away enough)
				// translate camera to node in which button was released
				LongPoint lp = bn.previousLocations[0];
				LongPoint trans = new LongPoint((lp.x-application.mCamera.posx)/2, (lp.y-application.mCamera.posy)/2);
				Vector zoomout = new Vector();
				//XXX:TBW compute altitude offset to see everything on the path at apex, but not higher
				zoomout.add(new Float(1000));
				zoomout.add(trans);
				Vector zoomin = new Vector();
				//XXX:TBW compute altitude offset to see everything on the path at apex, but not higher
				zoomin.add(new Float(-1000));
				zoomin.add(trans);
				application.sm.setUpdateLevel(false);
				application.mView.setAntialiasing(false);			
				application.vsm.animator.createCameraAnimation(FOLLOW_ANIM_DURATION, AnimManager.CA_ALT_TRANS_LIN,
				                                               zoomout, application.mCamera.getID());
	   			application.vsm.animator.createCameraAnimation(FOLLOW_ANIM_DURATION, AnimManager.CA_ALT_TRANS_LIN,
	 														   zoomin, application.mCamera.getID(),
	                                                           new PostAnimationAdapter(){
		                                                            public void animationEnded(Object target, short type, String dimension){
			                                                            application.sm.setUpdateLevel(true);
			                                                            application.mView.setAntialiasing(true);
		                                                            }
	                                                           });
			}
		}
		if (!brought2location.isEmpty()){
			Iterator i = brought2location.keySet().iterator();
			while (i.hasNext()){
				sendBackNTU(i.next());
			}
			brought2location.clear();
		}
		synchronized(broughtnode2broughtby){
			if (!broughtnode2broughtby.isEmpty()){
				broughtnode2broughtby.clear();
			}
		}
		if (!broughtStack.isEmpty()){
			for (int i=0;i<broughtStack.size();i++){
				((LNode)broughtStack.elementAt(i)).getShape().setColor(GraphManager.SHAPE_FILL_COLOR);
			}
			broughtStack.clear();
		}
		application.updateBreadCrumb();
		for (int i=0;i<application.grm.allArcs.length;i++){
			application.grm.allArcs[i].setTranslucency(1.0f);
		}
		for (int i=0;i<application.grm.allNodes.length;i++){
			application.grm.allNodes[i].setTranslucency(1.0f);
		}
		nodesOutsideScope.clear();
		arcsOutsideScope.clear();
	}

	void bring(LEdge arc, LNode node, LNode broughtby, long sx, long sy, long ex, long ey, Hashtable node2bposition){
		synchronized(broughtnode2broughtby){
			if (brought2location.containsKey(node)){
				broughtnode2broughtby.put(node, broughtby);
			}
			else {
				brought2location.put(node, BroughtElement.rememberPreviousState(node));
				broughtnode2broughtby.put(node, broughtby);
			}
		}
		if (!brought2location.containsKey(arc)){
			brought2location.put(arc, BroughtElement.rememberPreviousState(arc));
		}
		ClosedShape nodeShape = node.getShape();
		application.bSpace.onTop(nodeShape);
		BText nodeLabel = node.getLabel();
		application.bSpace.onTop(nodeLabel);
		LongPoint bposition = (LongPoint)node2bposition.get(node);
		LongPoint translation = new LongPoint(bposition.x-nodeShape.vx, bposition.y-nodeShape.vy);
		application.vsm.animator.createGlyphAnimation(BRING_ANIM_DURATION, AnimManager.GL_TRANS_SIG, translation, nodeShape.getID());
		application.vsm.animator.createGlyphAnimation(BRING_ANIM_DURATION, AnimManager.GL_TRANS_SIG, translation, nodeLabel.getID());
		DPathST spline = arc.getSpline();
		LongPoint asp = spline.getStartPoint();
		LongPoint aep = spline.getEndPoint();
		LongPoint sp, ep;
		if (Math.sqrt(Math.pow(asp.x-ex,2) + Math.pow(asp.y-ey,2)) < Math.sqrt(Math.pow(asp.x-sx,2) + Math.pow(asp.y-sy,2))){
			sp = new LongPoint(bposition.x, bposition.y);
			ep = new LongPoint(sx, sy);
		}
		else {
			sp = new LongPoint(sx, sy);
			ep = new LongPoint(bposition.x, bposition.y);
		}
		LongPoint[] flatCoords = DPathST.getFlattenedCoordinates(spline, sp, ep, true);
		application.vsm.animator.createPathAnimation(BRING_ANIM_DURATION, AnimManager.DP_TRANS_SIG_ABS, flatCoords, spline.getID(), null);
		// brought elements should not be faded
		node.setTranslucency(1.0f);
		nodesOutsideScope.remove(node);
		arcsOutsideScope.remove(arc);
		LEdge[] otherArcs = node.getOtherArcs(arc);
		Glyph oe;
		for (int i=0;i<otherArcs.length;i++){
			if (!brought2location.containsKey(otherArcs[i])){
				brought2location.put(otherArcs[i], BroughtElement.rememberPreviousState(otherArcs[i]));
			}
			spline = otherArcs[i].getSpline();
			asp = spline.getStartPoint();
			aep = spline.getEndPoint();
			if (node2bposition.containsKey(otherArcs[i].getTail())
			    && node2bposition.containsKey(otherArcs[i].getHead())){
				sp = (LongPoint)node2bposition.get(otherArcs[i].getTail());
				ep = (LongPoint)node2bposition.get(otherArcs[i].getHead());
			}
			else {
				oe = otherArcs[i].getOtherEnd(node).getShape();
				if (Math.sqrt(Math.pow(asp.x-ex,2) + Math.pow(asp.y-ey,2)) <= Math.sqrt(Math.pow(aep.x-ex,2) + Math.pow(aep.y-ey,2))){
					sp = new LongPoint(bposition.x, bposition.y);
					ep = oe.getLocation();
				}
				else {
					sp = oe.getLocation();
					ep = new LongPoint(bposition.x, bposition.y);
				}
			}
			flatCoords = DPathST.getFlattenedCoordinates(spline, sp, ep, true);
			application.vsm.animator.createPathAnimation(BRING_ANIM_DURATION, AnimManager.DP_TRANS_SIG_ABS, flatCoords, spline.getID(), null);
			otherArcs[i].setTranslucency(SECOND_BNG_STEP_TRANSLUCENCY);
			// 2nd step brought elements should not be faded
			arcsOutsideScope.remove(otherArcs[i]);
		}		
	}
	
	void sendBackNTU(Object k){
		BroughtElement be = (BroughtElement)brought2location.get(k);
		be.restorePreviousState(application.vsm.animator, BRING_ANIM_DURATION);
	}

	void sendBack(LNode n){
		BroughtElement be = (BroughtElement)brought2location.get(n);
		be.restorePreviousState(application.vsm.animator, BRING_ANIM_DURATION);
		n.setTranslucency(OUTSIDE_BNG_SCOPE_TRANSLUCENCY);
	}

	void sendBack(LEdge e){
		BroughtElement be = (BroughtElement)brought2location.get(e);
		be.restorePreviousState(application.vsm.animator, BRING_ANIM_DURATION);
		e.setTranslucency(OUTSIDE_BNG_SCOPE_TRANSLUCENCY);
	}

	// n1 is the node for which we attempt to send back connected nodes
	// n2 is the new center of the bring and go, so we do not send back nodes connected to n1 that are also connected to n2
	void fadeStack(LNode n1, LNode n2){
		//System.out.println("Fading back "+n1.code);
		//XXX:TBW		
	}
	
	/* ----------------------  LinkSlider  -----------------------------*/
	
	
	/* ---------------------- Highlighting -----------------------------*/
	
	static final Color HIGHLIGHT_COLOR = Color.RED;
	Vector highlightedElements = new Vector();
	
	void highlight(Glyph g){
		LNode n = (LNode)g.getOwner();
		if (n != null){
			LEdge[] arcs = n.getAllArcs();
			Glyph g2;
			for (int i=0;i<arcs.length;i++){
				g2 = arcs[i].getSpline();
				g2.setColor(HIGHLIGHT_COLOR);
				highlightedElements.add(g2);
				g2 = arcs[i].getOtherEnd(n).getShape();
				g2.setColor(HIGHLIGHT_COLOR);
				highlightedElements.add(g2);
			}
		}
	}

	void unhighlight(Glyph g){
		for (int i=0;i<highlightedElements.size();i++){
			((Glyph)highlightedElements.elementAt(i)).setColor(GraphManager.SHAPE_FILL_COLOR);
		}
		highlightedElements.clear();
	}	
	
}

class ZP2LensAction implements PostAnimationAction {

    NavigationManager nm;
    
    ZP2LensAction(NavigationManager nm){
	    this.nm = nm;
    }
    
    public void animationEnded(Object target, short type, String dimension){
        if (type == PostAnimationAction.LENS){
            nm.application.vsm.getOwningView(((Lens)target).getID()).setLens(null);
            ((Lens)target).dispose();
            nm.setMagFactor(NavigationManager.DEFAULT_MAG_FACTOR);
            nm.lens = null;
            nm.setLens(NavigationManager.NO_LENS);
            nm.application.sm.setUpdateLevel(true);
        }
    }
    
}

class LensChooser extends JFrame implements ItemListener {

    // index of lenses should correspond to short value of associated lens type in NavigationManager
    static final String[] LENS_NAMES = {"L1 / Linear", "L1 / Inverse Cosine", "L1 / Manhattan",
        "L2 / Gaussian", "L2 / Linear", "L2 / Inverse Cosine", "L2 / Manhattan", "L2 / Translucence Linear", "L2 / Fading", "L2 / Dynamic Linear",
        "L3 / Gaussian", "L3 / Linear", "L3 / Inverse Cosine", "L3 / Manhattan", "L3 / Translucence Linear",
        "LInf / Gaussian", "LInf / Linear", "LInf / Inverse Cosine", "LInf / Manhattan", "LInf / Translucence Linear", "LInf / Fading",
        "L2 / Wave", "L2 / Translucent Wave", "LInf / Step", "L2 / XGaussian", "L2 / HLinear"};

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

class RingManager {
	
	Ring[] rings = new Ring[0];
	
	Ring getRing(double direction, double size, double ringStep){
		// normalize direction in [0,2Pi[
		if (direction < 0){direction = 2 * Math.PI + direction;}
		// look for a ring where the new object could be placed, starting with the innermost one
		for (int i=0;i<rings.length;i++){
			double a = Math.abs(Math.atan2(size, rings[i].rank * ringStep));
			if (!rings[i].intersectsConeOfInfluence(direction-a, direction+a)){
				rings[i].addNode(direction-a, direction+a);
				return rings[i];
			}
		}
		// if couldn't find any room, create a new ring
		Ring r = createNewRing();
		double a = Math.abs(Math.atan2(size, ringStep));
		r.addNode(direction-a, direction+a);
		return r;
	}
	
	private Ring createNewRing(){
		Ring[] tr = new Ring[rings.length+1];
		System.arraycopy(rings, 0, tr, 0, rings.length);
		tr[rings.length] = new Ring(tr.length);
		rings = tr;
		return rings[rings.length-1];
	}
	
}

class Ring {

	/* rank of this ring (starts at 1) */
	int rank;
	double[][] cones = new double[0][2];
	
	Ring(int r){
		this.rank = r;
	}
	
	void addNode(double a1, double a2){
		// compute its cone of influence
		double[][] tc = new double[cones.length+1][2];
		System.arraycopy(cones, 0, tc, 0, cones.length);
		// normalize angles in [0,2Pi[
		if (a1 < 0){a1 = 2 * Math.PI + a1;}
		if (a2 < 0){a2 = 2 * Math.PI + a2;}
		tc[cones.length][0] = Math.min(a1, a2);
		tc[cones.length][1] = Math.max(a1, a2);
		cones = tc;
	}
	
	boolean intersectsConeOfInfluence(double a1, double a2){
		for (int i=0;i<cones.length;i++){
			if (a2 > cones[i][0] && a1 < cones[i][1]){return true;}
		}
		return false;
	}
	
}

class DistanceComparator implements java.util.Comparator {

	LNode centerNode;
	Glyph centerShape;

	DistanceComparator(LNode cn){
		this.centerNode = cn;
		this.centerShape = cn.getShape();
	}
    
	public int compare(Object o1, Object o2){
		Glyph n1 = ((LEdge)o1).getOtherEnd(centerNode).getShape();
		Glyph n2 = ((LEdge)o2).getOtherEnd(centerNode).getShape();
		double d1 = Math.pow(centerShape.vx-n1.vx, 2) + Math.pow(centerShape.vy-n1.vy, 2);
		double d2 = Math.pow(centerShape.vx-n2.vx, 2) + Math.pow(centerShape.vy-n2.vy, 2);
		if (d1 < d2){
			return -1;
		}
		else if (d1 > d2){
			return 1;
		}
		else {
			return 0;
		}
	}
        
}
