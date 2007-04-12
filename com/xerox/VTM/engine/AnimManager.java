/*   FILE: AnimManager.java
 *   DATE OF CREATION:   Jul 12 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 *
 * $Id$
 */

package com.xerox.VTM.engine;

import java.awt.Point;
import java.util.Hashtable;
import java.util.Vector;

import net.claribole.zvtm.engine.AnimationListener;
import net.claribole.zvtm.engine.LensKillAction;
import net.claribole.zvtm.engine.PAnimation;
import net.claribole.zvtm.engine.PResize;
import net.claribole.zvtm.engine.PTransResize;
import net.claribole.zvtm.engine.PTranslation;
import net.claribole.zvtm.engine.PTranslucency;
import net.claribole.zvtm.engine.Portal;
import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.glyphs.DPath;
import net.claribole.zvtm.lens.FSLMaxMagRadii;
import net.claribole.zvtm.lens.FSLRadii;
import net.claribole.zvtm.lens.FixedSizeLens;
import net.claribole.zvtm.lens.LAnimation;
import net.claribole.zvtm.lens.LMaximumMagnification;
import net.claribole.zvtm.lens.Lens;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.Translucent;
import com.xerox.VTM.glyphs.VCbCurve;
import com.xerox.VTM.glyphs.VQdCurve;

  /**
   * The Animation Manager is in charge of running every standard glyph, camera and lens animations. It also manages motion of the active camera.
   * @author Emmanuel Pietriga
   **/

public class AnimManager implements Runnable{

    /**translation (pacing function=linear)*/
    public static final short GL_TRANS_LIN = 0;
    /**translation (pacing function=parabolic - slow in/fast out motion)*/
    public static final short GL_TRANS_PAR = 1;
    /**translation (pacing function=sigmoid - slow in/slow out motion)*/
    public static final short GL_TRANS_SIG = 2;
    /**resizing (pacing function=linear)*/
    public static final short GL_SZ_LIN = 3;
    /**resizing (pacing function=parabolic - slow in/fast out motion)*/
    public static final short GL_SZ_PAR = 4;
    /**resizing (pacing function=sigmoid - slow in/slow out motion)*/
    public static final short GL_SZ_SIG = 5;
    /**rotation (pacing function=linear)*/
    public static final short GL_ROT_LIN = 6;
    /**rotation (pacing function=parabolic - slow in/fast out motion)*/
    public static final short GL_ROT_PAR = 7;
    /**rotation (pacing function=sigmoid - slow in/slow out motion)*/
    public static final short GL_ROT_SIG = 8;
    /**coloration (pacing function=linear)*/
    public static final short GL_COLOR_LIN = 9;
    /**translation to absolute coordinates (pacing function=linear)*/
    public static final short GL_TRANS_LIN_ABS = 10;
    /**translation to absolute coordinates (pacing function=parabolic - slow in/fast out motion)*/
    public static final short GL_TRANS_PAR_ABS = 11;
    /**translation to absolute coordinates (pacing function=sigmoid - slow in/slow out motion)*/
    public static final short GL_TRANS_SIG_ABS = 12;
   

    /**control point translation (curve) (pacing function=linear)*/
    public static final short GL_CP_TRANS_LIN = 0;
    /**control point translation (curve) (pacing function=parabolic - slow in/fast out motion)*/
    public static final short GL_CP_TRANS_PAR = 1;
    /**control point translation (curve) (pacing function=sigmoid - slow in/slow out motion)*/
    public static final short GL_CP_TRANS_SIG = 2;

    /**lens radii value animation (pacing function=linear)*/
    public static final short LS_RD_LIN = 0;
    /**lens radii value animation (pacing function = parabolic - slow in/fast out motion)*/
    public static final short LS_RD_PAR = 1;
    /**lens radii value animation (pacing function = sigmoid - slow in/slow out motion)*/
    public static final short LS_RD_SIG = 2;
    /**lens max magnification factor animation (pacing function = linear)*/
    public static final short LS_MM_LIN = 3;
    /**lens max magnification factor animation (pacing function = parabolic - slow in/fast out motion)*/
    public static final short LS_MM_PAR = 4;
    /**lens max magnification factor animation (pacing function = sigmoid - slow in/slow out motion)*/
    public static final short LS_MM_SIG = 5;
    /**lens max magnification factor and radii value animation (pacing function = linear)*/
    public static final short LS_MM_RD_LIN = 6;
    /**lens max magnification factor and radii value animation (pacing function = parabolic - slow in/fast out motion)*/
    public static final short LS_MM_RD_PAR = 7;
    /**lens max magnification factor and radii value animation (pacing function = sigmoid - slow in/slow out motion)*/
    public static final short LS_MM_RD_SIG = 8;

    /**camera translation (pacing function=linear)*/
    public static final short CA_TRANS_LIN = 0;
    /**camera translation (pacing function=parabolic - slow in/fast out motion)*/
    public static final short CA_TRANS_PAR = 1;
    /**camera translation (pacing function=sigmoid - slow in/slow out motion)*/
    public static final short CA_TRANS_SIG = 2;
    /**camera altitude (pacing function=linear)*/
    public static final short CA_ALT_LIN = 3;
    /**camera altitude (pacing function=parabolic - slow in/fast out motion)*/
    public static final short CA_ALT_PAR = 4;
    /**camera altitude (pacing function=sigmoid - slow in/slow out motion)*/
    public static final short CA_ALT_SIG = 5;
    /**camera altitude and translation (pacing function=sigmoid - slow in/slow out motion)*/
    public static final short CA_ALT_TRANS_SIG = 6;
    /**camera altitude and translation (pacing function=linear)*/
    public static final short CA_ALT_TRANS_LIN = 7;

    /**portal translation (pacing function=linear)*/
    public static final short PT_TRANS_LIN = 0;
    /**portal translation (pacing function=parabolic - slow in/fast out motion)*/
    public static final short PT_TRANS_PAR = 1;
    /**portal translation (pacing function=sigmoid - slow in/slow out motion)*/
    public static final short PT_TRANS_SIG = 2;
    /**portal size (pacing function=linear)*/
    public static final short PT_SZ_LIN = 3;
    /**portal size (pacing function=parabolic - slow in/fast out motion)*/
    public static final short PT_SZ_PAR = 4;
    /**portal size (pacing function=sigmoid - slow in/slow out motion)*/
    public static final short PT_SZ_SIG = 5;
    /**portal size and translation (pacing function=sigmoid - slow in/slow out motion)*/
    public static final short PT_SZ_TRANS_SIG = 6;
    /**portal size and translation (pacing function=linear)*/
    public static final short PT_SZ_TRANS_LIN = 7;
    /**portal translucency (pacing function=linear)*/
    public static final short PT_ALPHA_LIN = 8;

    /**Interrupt a glyph translation*/
    public static final String GL_TRANS = "pos";
    /**Interrupt a glyph resizing*/
    public static final String GL_SZ="sz";
    /**Interrupt a glyph rotation*/
    public static final String GL_ROT = "or";
    /**Interrupt a glyph rotation*/
    public static final String GL_COLOR="col";
    /**Interrupt a curve control point animation*/
    public static final String GL_CTRL = "ctrl";

    /**Interrupt a camera translation*/
    public static final String CA_TRANS = "pos";
    /**Interrupt a camera altitude change*/
    public static final String CA_ALT = "sz";
    /**Interrupt a camera translation+altitude change*/
    public static final String CA_BOTH = "both";

    /**Interrupt a portal translation*/
    public static final String PT_TRANS = "pos";
    /**Interrupt a portal altitude change*/
    public static final String PT_SZ = "sz";
    /**Interrupt a portal translation+altitude change*/
    public static final String PT_BOTH = "both";
    /**Interrupt a portal translucency change*/
    public static final String PT_ALPHA = "alpha";

    /**Interrupt max magnification factor animation*/
    public static final String LS_MM = "mm";
    /**Interrupt radii value animation*/
    public static final String LS_RD = "rd";
    /**Interrupt max magnification factor and radii value animation*/
    public static final String LS_BOTH = "both";

    /**root VTM class*/
    VirtualSpaceManager vsm;

    Thread runAnim;

    boolean started=false;

    /**sets the time interval between to loops (sleep time)*/
    static int frameTime = 20;

    /**Holds a flag for each view telling whether it should be repainted or not.
     * (as a consequence of a change occuring through an animation)*/
    protected boolean[] repaintViews;
    /**Holds a flag telling wether all views should be repainted or not
     * (overrides any flag in repaintViews)*/
    protected boolean repaintAll = false;

    /**sets the steepness of the sigmoid (slow-in/slow-out animation pacing function)*/
    public static final int sigFactor = 4;

    Vector animCameraBag;  //animations to be executed: cameras
    Vector animGlyphBag;   //animations to be executed: glyphs
    Vector animLensBag;    //animations to be executed: lenses
    Vector animPortalBag;    //animations to be executed: portals

    //key is a glyph ID, value is another hashtable for which:
    //  keys are "pos","sz","or","col","ctrl" and value is a vector of animations waiting to be executed
    Hashtable pendingGAnims;  
    
    //key is a camera ID, value is another hashtable for which:
    //  keys are "pos","alt" - and value is a vector of animations waiting to be executed for this dimension
    Hashtable pendingCAnims;  

    //key is a lens ID, value is a vector of animations waiting to be executed for this dimension
    Hashtable pendingLAnims;

    //key is a portal ID, value is a vector of animations waiting to be executed for this dimension
    Hashtable pendingPAnims;
    
    //keys are IDs of glyph being animated right now - value is an array with four ordered integers whose value is 0 or 1.
    //  0=no anim running for this dimension, 1=anim running 
    //  index of dimensions in array: 0=position 1=size 2=orientation 3=color 4=control point (4 exists only for QdCurve and CbCurve)
    Hashtable animatedGlyphs; 

    //keys are IDs of cameras being animated right now - value is an array with two ordered integers whose value is 0 or 1.
    //  0=no anim running for this dimension, 1=anim running 
    //  index of dimensions in array: 0=position 1=altitude
    Hashtable animatedCameras;

    //keys are IDs of lenses being animated right now - value is an array with two ordered integers whose value is 0 or 1.
    //  0=no anim running for this dimension, 1=anim running 
    //  index of dimensions in array: 0=maximum magnification 1=radii (outer radius and inner radius)
    Hashtable animatedLenses;

    //keys are IDs of portals being animated right now - value is an array with two ordered integers whose value is 0 or 1.
    //  0=no anim running for this dimension, 1=anim running 
    //  index of dimensions in array: 0=position 1=size 2=translucency
    Hashtable animatedPortals;

    /**Animation listener.
     * Set by client application if it wants to be notified of camera animations
     * currently running  (for instance when the camera is moved) */
    AnimationListener animListener=null;
    /**Flag reset for each iteration telling whether camera has moved or not (x,y,alt).
     * Tell camera listeners if it has.*/
    private boolean notifyCameraAnim=false;
    /**Flag reset for each iteration telling whether camera has moved (x,y) or not.
     * Propagate move to dependant glyphs if true.*/
    private boolean cameraTranslated = false;

    /*convenience variables*/

    /**active camera translation speed*/
    public double Xspeed,Yspeed;
    /** active camera altitude speed*/
    public float Aspeed;
    /** active camera*/
    Camera activeCam;
    
    /*variables related to performance issues*/
    CAnimation vcan;
    LAnimation vlan;
    PAnimation vpan;
    int vi;


    /**@param parentVSM VTM class*/
    AnimManager(VirtualSpaceManager parentVSM){
	vsm=parentVSM;
	repaintViews = new boolean[0];
	animCameraBag=new Vector();
	animPortalBag=new Vector();
	animGlyphBag=new Vector();
	animLensBag = new Vector();
	pendingGAnims=new Hashtable();
	pendingCAnims=new Hashtable();
	pendingPAnims=new Hashtable();
	pendingLAnims = new Hashtable();
	animatedGlyphs=new Hashtable();
	animatedCameras=new Hashtable();
	animatedPortals=new Hashtable();
	animatedLenses = new Hashtable();
	Xspeed=0;
	Yspeed=0;
	Aspeed=0;
	//start();  //started externally when the first view is created (prevent unnecessary null pointer exception for active camera)
    }

    public void start(){
	started=true;
	runAnim=new Thread(this);
	runAnim.setPriority(Thread.NORM_PRIORITY);
	runAnim.start();
    }

    public synchronized void stop() {
	started=false;
	runAnim = null;
	notify();
    }

    public void run(){
	Thread me = Thread.currentThread();
	while (vsm.activeView==null){
	    try {
 		runAnim.sleep(500);   //wait for a view to become active - caused nullpointerex under Solaris
 	    } 
 	    catch (InterruptedException e) {
		if (vsm.debug){System.err.println("animmanager.run.sleep0 "+e);}
 		return;
 	    }
	}
	while (runAnim==me) {
	    try {
		activeCam=vsm.getActiveCamera();
		if (Xspeed != 0){
		    activeCam.dposx -= Xspeed;
		    activeCam.posx = Math.round(activeCam.dposx);
		    repaintViews[vsm.activeViewIndex] = true;
		    cameraTranslated = true;
		    notifyCameraAnim = true;
		}
		if (Yspeed != 0){
		    activeCam.dposy -= Yspeed;
		    activeCam.posy = Math.round(activeCam.dposy);
		    repaintViews[vsm.activeViewIndex] = true;
		    cameraTranslated = true;
		    notifyCameraAnim = true;
		}
		if (cameraTranslated){
		    activeCam.propagateMove(-Xspeed, -Yspeed);
		}
		if (Aspeed!=0){
		    activeCam.altitudeOffset(-Aspeed);
		    repaintViews[vsm.activeViewIndex] = true;
		    notifyCameraAnim = true;
		}
	    }
	    catch (NullPointerException e){if (vsm.debug){System.err.println("animmanager.run.activecam "+e);}}
	    for (int i=0;i<animCameraBag.size();i++) {
		try {
		    vcan = (CAnimation)(animCameraBag.get(i));
		    vcan.animate();
		    vi = vsm.getViewIndex(vcan.target.getOwningView().getName());
 		    if (vi != -1){
			repaintViews[vi] = true;
 		    }
		    notifyCameraAnim = true;
		}
		catch (NullPointerException e) {if (vsm.debug){System.err.println("animmanager.run.camera anim stopped "+e);}}
	    }
	    for (int i=0;i<animLensBag.size();i++){
		vlan = (LAnimation)(animLensBag.get(i));
		vlan.animate();
		vi = vsm.getViewIndex(vlan.target.getOwningView().getName());
		if (vi != -1){
		    repaintViews[vi] = true;
		}
	    }
	    for (int i=0;i<animPortalBag.size();i++) {
		try {
		    vpan = (PAnimation)(animPortalBag.get(i));
		    vpan.animate();
		    vi = vsm.getViewIndex(vpan.target.getOwningView().getName());
 		    if (vi != -1){
			repaintViews[vi] = true;
 		    }
		}
		catch (NullPointerException e) {if (vsm.debug){System.err.println("animmanager.run.camera anim stopped "+e);}}
	    }
	    for (int i=0;i<animGlyphBag.size();i++) {
		try {
		    if (((GAnimation)(animGlyphBag.get(i))).animate()){
			// XXX: repaint everything as we do not yet have an efficient way
			// of retrieving views specifically associated with a given glyph
			repaintAll = true;
		    }
		}
		catch (NullPointerException e) {
		    if (vsm.debug){
			System.err.println("animmanager.run.glyph anim stopped "+e);
			e.printStackTrace();
		    }
		}
	    }
	    if (repaintAll){
		vsm.repaintNow();
		repaintAll = false;
	    }
	    else {
		for (int i=0;i<repaintViews.length;i++){
		    if (repaintViews[i]){
			vsm.repaintNow(i);
			repaintViews[i] = false;
		    }
		}
	    }
	    if (animListener!=null && notifyCameraAnim){
		synchronized(this){
		    animListener.cameraMoved();
		    notifyCameraAnim=false;
		}
	    }
	    //either we do
 	    try {
 		runAnim.sleep(frameTime);   //sleep ... ms  
 	    } 
 	    catch (InterruptedException e) {
		if (vsm.debug){System.err.println("animmanager.run.sleep "+e);}
 		return;
 	    }
	    //or this   both seem to work well (have to test on several config)
	    //	    Thread.yield();
	}
    }
    
    /**set an animation listener - calls back method notify() each time the animation manager makes a change to a camera (possibly each time the thread runs through the loop) - set by client application if it wants to be notified of animations currently running  (for instance when the camera is moved)
     *@param al the class implementing interface AnimationListener  (set to null if you want to remove it)
     */
    public void setAnimationListener(AnimationListener al){
	this.animListener=al;
    }

    /**get the animation listener set by client application*/
    public AnimationListener getAnimationListener(){
	return this.animListener;
    }

    protected void registerView(){
	boolean[] tmpA = new boolean[repaintViews.length+1];
	System.arraycopy(repaintViews, 0, tmpA, 0, repaintViews.length);
	tmpA[repaintViews.length] = false;
	repaintViews = tmpA;
    }

    protected void unregisterView(int i){
	boolean[] tmpA = new boolean[repaintViews.length-1];
	if (tmpA.length > 0){
	    System.arraycopy(repaintViews, 0, tmpA, 0, i);
	    System.arraycopy(repaintViews, i+1, tmpA, i, repaintViews.length-i-1);
	}
	repaintViews = tmpA;
    }

    /* ----------------------- GLYPH ANIMATION ------------------------- */

    private void newGlyphAnim(long duration,short type,Object data,Long gID,int refresh, PostAnimationAction paa) throws ClassCastException {
	Glyph g=vsm.getGlyph(gID);
	switch(type){
	case GL_TRANS_LIN:{//translation - linear
	    if (animatedGlyphs.containsKey(gID) && (((int[])animatedGlyphs.get(gID))[0]==1)){
		putAsPendingGAnimation(gID,GL_TRANS,duration,type,data, paa);
	    }
	    else {
		if (animatedGlyphs.containsKey(gID)){((int[])animatedGlyphs.get(gID))[0]=1;}
		else {int [] tmpA={1,0,0,0};animatedGlyphs.put(gID,tmpA);}
		GTranslation an=(refresh==0) ? (new GTranslation(g,this,duration)) : (new GTranslationPRR(g,this,duration,refresh));
		an.setPostAnimationAction(paa);
		long x=g.vx;
		long y=g.vy;
		long tx=((LongPoint)data).x;    //data is a LongPoint representing the x and y offsets
		long ty=((LongPoint)data).y;
		double nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		double dx=tx/nbSteps;
		double dy=ty/nbSteps;
		an.steps=new LongPoint[(int)nbSteps];
		for (int i=0;i<nbSteps-1;){
		    an.steps[i]=new LongPoint((long)(x+i*dx),(long)(y+i*dy));
		    i++;
		}
		an.steps[(int)nbSteps-1]=new LongPoint(x+tx,y+ty);  //last point is assigned from source value in order to prevent precision error 
		animGlyphBag.add(an);
		an.start();
	    }
	    break;
	}	
	case GL_TRANS_PAR:{//translation - parabolic  (^4)
	    if (animatedGlyphs.containsKey(gID) && (((int[])animatedGlyphs.get(gID))[0]==1)){
		putAsPendingGAnimation(gID,GL_TRANS,duration,type,data, paa);
	    }
	    else {
		if (animatedGlyphs.containsKey(gID)){((int[])animatedGlyphs.get(gID))[0]=1;}
		else {int [] tmpA={1,0,0,0};animatedGlyphs.put(gID,tmpA);}
		GTranslation an=(refresh==0) ? (new GTranslation(g,this,duration)) : (new GTranslationPRR(g,this,duration,refresh));
		an.setPostAnimationAction(paa);
		long x=g.vx;
		long y=g.vy;
		long tx=((LongPoint)data).x;    //data is a LongPoint representing the x and y offsets
		long ty=((LongPoint)data).y;
		double nbSteps=Math.round((double)(duration/frameTime));     //number of steps
		an.steps=new LongPoint[(int)nbSteps];
		double stepValue;
		long dx,dy;
		for (int i=0;i<nbSteps-1;) {
		    stepValue=Math.pow((i+1)/nbSteps,4);
		    dx=(long)Math.round(tx*stepValue);
		    dy=(long)Math.round(ty*stepValue);
		    an.steps[i++]=new LongPoint(x+dx,y+dy);
		}
		an.steps[(int)nbSteps-1]=new LongPoint(x+tx,y+ty);   //last point is assigned from source value in order to prevent precision error
		animGlyphBag.add(an);
		an.start();
	    }
	    break;
	}
	case GL_TRANS_SIG:{//translation - sigmoid
	    if (animatedGlyphs.containsKey(gID) && (((int[])animatedGlyphs.get(gID))[0]==1)){
		putAsPendingGAnimation(gID,GL_TRANS,duration,type,data, paa);
	    }
	    else {
		if (animatedGlyphs.containsKey(gID)){((int[])animatedGlyphs.get(gID))[0]=1;}
		else {int [] tmpA={1,0,0,0};animatedGlyphs.put(gID,tmpA);}
		GTranslation an=(refresh==0) ? (new GTranslation(g,this,duration)) : (new GTranslationPRR(g,this,duration,refresh));
		an.setPostAnimationAction(paa);
		long x=g.vx;
		long y=g.vy;
		long tx=((LongPoint)data).x;    //data is a LongPoint representing the x and y offsets
		long ty=((LongPoint)data).y;
		double nbSteps=Math.round((double)(duration/frameTime));     //number of steps
		an.steps=new LongPoint[(int)nbSteps];
		double stepValue;
		long dx,dy;
		for (int i=0;i<nbSteps-1;) {
		    stepValue=computeSigmoid(sigFactor,(i+1)/nbSteps);
		    dx=(long)Math.round(tx*stepValue);
		    dy=(long)Math.round(ty*stepValue);
		    an.steps[i++]=new LongPoint(x+dx,y+dy);
		}
		an.steps[(int)nbSteps-1]=new LongPoint(x+tx,y+ty);   //last point is assigned from source value in order to prevent precision error
		/*for (int i=0;i<nbSteps;i++){
		    vsm.addGlyph(new VPoint(an.steps[i].x,an.steps[i].y,Color.black),"xtr");
		    }*/
		animGlyphBag.add(an);
		an.start();
	    }
	    break;
	}
	case GL_SZ_LIN:{//resizing - linear
	    if (animatedGlyphs.containsKey(gID) && (((int[])animatedGlyphs.get(gID))[1]==1)){
		putAsPendingGAnimation(gID,GL_SZ,duration,type,data, paa);
	    }
	    else {
		if (animatedGlyphs.containsKey(gID)){((int[])animatedGlyphs.get(gID))[1]=1;}
		else {int [] tmpA={0,1,0,0};animatedGlyphs.put(gID,tmpA);}
		GResize an=(refresh==0) ? (new GResize(g,this,duration)) : (new GResizePRR(g,this,duration,refresh));
		an.setPostAnimationAction(paa);
		float sz=g.getSize();
		float tsz=(((Float)data).floatValue())*sz;    //data is a float representing the resizing factor ; tsz is the final size
		float nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		float dsz=(tsz-sz)/nbSteps;
		an.steps=new float[(int)nbSteps];
		for (int i=0;i<nbSteps-1;) {
		    an.steps[i]=sz+i*dsz;
		    i++;
		}
		an.steps[(int)nbSteps-1]=tsz;  //last point is assigned from source value in order to prevent precision error 
		animGlyphBag.add(an);
		an.start();
	    }
	    break;
	}
	case GL_SZ_PAR:{//resizing - parabolic
	    if (animatedGlyphs.containsKey(gID) && (((int[])animatedGlyphs.get(gID))[1]==1)){
		putAsPendingGAnimation(gID,GL_SZ,duration,type,data, paa);
	    }
	    else {
		if (animatedGlyphs.containsKey(gID)){((int[])animatedGlyphs.get(gID))[1]=1;}
		else {int [] tmpA={0,1,0,0};animatedGlyphs.put(gID,tmpA);}
		GResize an=(refresh==0) ? (new GResize(g,this,duration)) : (new GResizePRR(g,this,duration,refresh));
		an.setPostAnimationAction(paa);
		float sz=g.getSize();
		float tsz=(((Float)data).floatValue())*sz;    //data is a float representing the resizing factor ; tsz is the final size
		float nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		float dsz;
		float stepValue;
		an.steps=new float[(int)nbSteps];
		for (int i=0;i<nbSteps-1;) {
		    stepValue=(float)Math.pow((i+1)/nbSteps,4);
		    dsz=(tsz-sz)*stepValue;
		    an.steps[i]=sz+dsz;
		    i++;
		}
		an.steps[(int)nbSteps-1]=tsz;  //last point is assigned from source value in order to prevent precision error 
		animGlyphBag.add(an);
		an.start();
	    }
	    break;
	}
	case GL_SZ_SIG:{//resizing - sigmoid
	    if (animatedGlyphs.containsKey(gID) && (((int[])animatedGlyphs.get(gID))[1]==1)){
		putAsPendingGAnimation(gID,GL_SZ,duration,type,data, paa);
	    }
	    else {
		if (animatedGlyphs.containsKey(gID)){((int[])animatedGlyphs.get(gID))[1]=1;}
		else {int [] tmpA={0,1,0,0};animatedGlyphs.put(gID,tmpA);}
		GResize an=(refresh==0) ? (new GResize(g,this,duration)) : (new GResizePRR(g,this,duration,refresh));
		an.setPostAnimationAction(paa);
		float sz=g.getSize();
		float tsz=(((Float)data).floatValue())*sz;    //data is a float representing the resizing factor ; tsz is the final size
		float nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		float dsz;
		float stepValue;
		an.steps=new float[(int)nbSteps];
		for (int i=0;i<nbSteps-1;) {
		    stepValue=(float)computeSigmoid(sigFactor,(i+1)/nbSteps);
		    dsz=(tsz-sz)*stepValue;
		    an.steps[i]=sz+dsz;
		    i++;
		}
		an.steps[(int)nbSteps-1]=tsz;  //last point is assigned from source value in order to prevent precision error 
		animGlyphBag.add(an);
		an.start();
	    }
	    break;
	}
	case GL_ROT_LIN:{//rotating - linear
	    if (animatedGlyphs.containsKey(gID) && (((int[])animatedGlyphs.get(gID))[2]==1)){
		putAsPendingGAnimation(gID,GL_ROT,duration,type,data, paa);
	    }
	    else {
		if (animatedGlyphs.containsKey(gID)){((int[])animatedGlyphs.get(gID))[2]=1;}
		else {int [] tmpA={0,0,1,0};animatedGlyphs.put(gID,tmpA);}
		GRotate an=(refresh==0) ? (new GRotate(g,this,duration)) : (new GRotatePRR(g,this,duration,refresh));
		an.setPostAnimationAction(paa);
		float or=g.getOrient();
		float tor=(((Float)data).floatValue())+or; //data is a float representing the rotation angle ; tor is the final orientation
		float nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		float dor=(tor-or)/nbSteps;
		an.steps=new float[(int)nbSteps];
		for (int i=0;i<nbSteps-1;) {
		    an.steps[i]=or+i*dor;
		    i++;
		}
		if (Math.abs(tor)>=2*Math.PI) {tor=tor%(2*(float)Math.PI);}  //angle belongs to [0:2*Pi[
		an.steps[(int)nbSteps-1]=tor;  //last point is assigned from source value in order to prevent precision error 
		animGlyphBag.add(an);
		an.start();
	    }
	    break;
	}
	case GL_ROT_PAR:{//rotating - parabolic
	    if (animatedGlyphs.containsKey(gID) && (((int[])animatedGlyphs.get(gID))[2]==1)){
		putAsPendingGAnimation(gID,GL_ROT,duration,type,data, paa);
	    }
	    else {
		if (animatedGlyphs.containsKey(gID)){((int[])animatedGlyphs.get(gID))[2]=1;}
		else {int [] tmpA={0,0,1,0};animatedGlyphs.put(gID,tmpA);}
		GRotate an=(refresh==0) ? (new GRotate(g,this,duration)) : (new GRotatePRR(g,this,duration,refresh));
		an.setPostAnimationAction(paa);
		float or=g.getOrient();
		float tor=(((Float)data).floatValue())+or; //data is a float representing the rotation angle ; tor is the final orientation
		float nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		float dor;
		float stepValue;
		an.steps=new float[(int)nbSteps];
		for (int i=0;i<nbSteps-1;) {
		    stepValue=(float)Math.pow((i+1)/nbSteps,4);
		    dor=(tor-or)*stepValue;
		    an.steps[i]=or+dor;
		    i++;
		}
		if (Math.abs(tor)>=2*Math.PI) {tor=tor%(2*(float)Math.PI);}  //angle belongs to [0:2*Pi[
		an.steps[(int)nbSteps-1]=tor;  //last point is assigned from source value in order to prevent precision error 
		animGlyphBag.add(an);
		an.start();
	    }
	    break;
	}
	case GL_ROT_SIG:{//rotating - sigmoid
	    if (animatedGlyphs.containsKey(gID) && (((int[])animatedGlyphs.get(gID))[2]==1)){
		putAsPendingGAnimation(gID,GL_ROT,duration,type,data, paa);
	    }
	    else {
		if (animatedGlyphs.containsKey(gID)){((int[])animatedGlyphs.get(gID))[2]=1;}
		else {int [] tmpA={0,0,1,0};animatedGlyphs.put(gID,tmpA);}
		GRotate an=(refresh==0) ? (new GRotate(g,this,duration)) : (new GRotatePRR(g,this,duration,refresh));
		an.setPostAnimationAction(paa);
		float or=g.getOrient();
		float tor=(((Float)data).floatValue())+or; //data is a float representing the rotation angle ; tor is the final orientation
		float nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		float dor;
		float stepValue;
		an.steps=new float[(int)nbSteps];
		for (int i=0;i<nbSteps-1;) {
		    stepValue=(float)computeSigmoid(sigFactor,(i+1)/nbSteps);
		    dor=(tor-or)*stepValue;
		    an.steps[i]=or+dor;
		    i++;
		}
		if (Math.abs(tor)>=2*Math.PI) {tor=tor%(2*(float)Math.PI);}  //angle belongs to [0:2*Pi[
		an.steps[(int)nbSteps-1]=tor;  //last point is assigned from source value in order to prevent precision error 
		animGlyphBag.add(an);
		an.start();
	    }
	    break;
	}
	case GL_COLOR_LIN:{//color - linear  DATA: an array of 6 floats (HSV components of fill color, HSV components of border color),
	    // or 7 floats (same + translucency)
	    float[] cdata;
	    // until 0.9.4, data was a vector with of Floats. Still supported for backward compatibility
	    if (data instanceof Vector){
		Vector v = (Vector)data;
		cdata = new float[v.size()];
		for (int i=0;i<cdata.length;i++){
		    cdata[i] = ((Float)v.elementAt(i)).floatValue();
		}
	    }
	    else {
		cdata = (float[])data;
	    }
	    if (animatedGlyphs.containsKey(gID) && (((int[])animatedGlyphs.get(gID))[3]==1)){
		putAsPendingGAnimation(gID,GL_COLOR,duration,type,data, paa);
	    }
	    else {
		if (animatedGlyphs.containsKey(gID)){((int[])animatedGlyphs.get(gID))[3]=1;}
		else {int [] tmpA={0,0,0,1};animatedGlyphs.put(gID,tmpA);}
		GColoration an = (refresh==0) ? (new GColoration(g,this,duration)) : (new GColorationPRR(g,this,duration,refresh));
		an.setPostAnimationAction(paa);
		double nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		an.nbSteps = (int)nbSteps;
		// fill color
		float[] thsv=new float[3];
		for (int i=0;i<3;i++){// init target HSV array
		    thsv[i] = cdata[i];
		}
		if (thsv[0] != 0 || thsv[1] != 0 || thsv[2] != 0){// fill color is indeed animated
		    float[] hsv;
		    hsv=g.getHSVColor();
		    double dh=thsv[0]/nbSteps;
		    double ds=thsv[1]/nbSteps;
		    double dv=thsv[2]/nbSteps;
		    an.steps=new float[an.nbSteps][3];
		    for (int i=0;i<nbSteps-1;) {
			an.steps[i][0]=hsv[0]+i*(float)dh;
			an.steps[i][1]=hsv[1]+i*(float)ds;
			an.steps[i][2]=hsv[2]+i*(float)dv;
			i++;
		    }
		    an.steps[(int)nbSteps-1][0]=hsv[0]+thsv[0];//last point is assigned from source value in order to prevent precision error
		    an.steps[(int)nbSteps-1][1]=hsv[1]+thsv[1];
		    an.steps[(int)nbSteps-1][2]=hsv[2]+thsv[2];
		}
		// border color
		thsv=new float[3];
		for (int i=0;i<3;i++){// init target HSV array
		    thsv[i] = cdata[i+3];
		}
		if (thsv[0] != 0 || thsv[1] != 0 || thsv[2] != 0){// border color is indeed animated
		    float[] hsv;
		    hsv=g.getHSVColor();
		    double dh=thsv[0]/nbSteps;
		    double ds=thsv[1]/nbSteps;
		    double dv=thsv[2]/nbSteps;
		    an.bsteps=new float[(int)nbSteps][3];
		    for (int i=0;i<nbSteps-1;) {
			an.bsteps[i][0]=hsv[0]+i*(float)dh;
			an.bsteps[i][1]=hsv[1]+i*(float)ds;
			an.bsteps[i][2]=hsv[2]+i*(float)dv;
			i++;
		    }
		    an.bsteps[(int)nbSteps-1][0]=hsv[0]+thsv[0];//last point is assigned from source value in order to prevent precision error
		    an.bsteps[(int)nbSteps-1][1]=hsv[1]+thsv[1];
		    an.bsteps[(int)nbSteps-1][2]=hsv[2]+thsv[2];
		}
		if (cdata.length == 7 && g instanceof Translucent){//deal with translucency animation (if specified)
		    float tav = cdata[6];
		    float sav = ((Translucent)g).getTranslucencyValue();
		    double dt = tav / nbSteps;
		    an.alphasteps=new float[an.nbSteps];
		    for (int i=0;i<nbSteps-1;i++){
			an.alphasteps[i] = sav + i * (float)dt;
		    }
		    //last point is assigned from source value in order to prevent precision error
		    an.alphasteps[(int)nbSteps-1] = sav + tav;
		}
		animGlyphBag.add(an);
		an.start();
	    }
	    break;
	}
	default:{
	    System.err.println("Error : AnimManager.createGlyphAnimation : unknown animation type");
	}
	}
    }

    /** Animate a glyph.
     *@param duration in milliseconds
     *@param type use one of (GL_TRANS_LIN, GL_TRANS_PAR, GL_TRANS_SIG, GL_SZ_LIN, GL_SZ_PAR, GL_SZ_SIG, GL_ROT_LIN, GL_ROT_PAR, GL_ROT_SIG, GL_COLOR_LIN)
     *@param data <br>- For translations, data is a LongPoint representing X and Y offsets<br>
     *            - For resize operations, data is a Float representing the resizing factor<br>
     *            - For rotations, data is a Float representing the rotation angle<br>
     *            - For coloration, data is a an array of 6 floats representing H,S and V offsets for fill and border color respectively. A 7th float can be appended to animate translucency (in which case the animation can only be applied to glyphs implementing the Translucent interface)
     *@param gID ID of glyph to be animated
     *@see #createGlyphAnimation(long duration, short type, Object data, Long gID, PostAnimationAction paa)
     *@see #createGlyphAnimation(long duration, int refresh, short type, Object data, Long gID)
     *@see #createGlyphAnimation(long duration, int refresh, short type, Object data, Long gID, PostAnimationAction paa)
     */
    public void createGlyphAnimation(long duration, short type, Object data, Long gID) throws ClassCastException {
	newGlyphAnim(duration, type, data, gID, 0, null);
    }

    /** Animate a glyph.
     *@param duration in milliseconds
     *@param type use one of (GL_TRANS_LIN, GL_TRANS_PAR, GL_TRANS_SIG, GL_SZ_LIN, GL_SZ_PAR, GL_SZ_SIG, GL_ROT_LIN, GL_ROT_PAR, GL_ROT_SIG, GL_COLOR_LIN)
     *@param data <br>- For translations, data is a LongPoint representing X and Y offsets<br>
     *            - For resize operations, data is a Float representing the resizing factor<br>
     *            - For rotations, data is a Float representing the rotation angle<br>
     *            - For coloration, data is a an array of 6 floats representing H,S and V offsets for fill and border color respectively. A 7th float can be appended to animate translucency (in which case the animation can only be applied to glyphs implementing the Translucent interface)
     *@param gID ID of glyph to be animated
     *@param paa action to perform after animation ends
     *@see #createGlyphAnimation(long duration, short type, Object data, Long gID)
     *@see #createGlyphAnimation(long duration, int refresh, short type, Object data, Long gID)
     *@see #createGlyphAnimation(long duration, int refresh, short type, Object data, Long gID, PostAnimationAction paa)
     */
    public void createGlyphAnimation(long duration, short type, Object data, Long gID, PostAnimationAction paa) throws ClassCastException {
	newGlyphAnim(duration, type, data, gID, 0, paa);
    }

    /** Animate a glyph.
     *@param duration in milliseconds
     *@param refresh refresh rate division factor. The animator manager runs at a rate as close as possible to 20ms. Some animations do not need to be refreshed that often. By setting a factor of N>1 for this animation, it is possible to tell the animator manager to apply the animation only every N iteration (default is 1). For instance, if the std refresh rate is indeed 20ms, and if refresh=3, then this animation will be executed only every 60 ms. The animation will still last the time indicated by duration.
     *@param type use one of (GL_TRANS_LIN, GL_TRANS_PAR, GL_TRANS_SIG, GL_SZ_LIN, GL_SZ_PAR, GL_SZ_SIG, GL_ROT_LIN, GL_ROT_PAR, GL_ROT_SIG, GL_COLOR_LIN)
     *@param data <br>- For translations, data is a LongPoint representing X and Y offsets<br>
     *            - For resize operations, data is a Float representing the resizing factor<br>
     *            - For rotations, data is a Float representing the rotation angle<br>
     *            - For coloration, data is a an array of 6 floats representing H,S and V offsets for fill and border color respectively. A 7th float can be appended to animate translucency (in which case the animation can only be applied to glyphs implementing the Translucent interface)
     *@param gID ID of glyph to be animated
     *@see #createGlyphAnimation(long duration, short type, Object data, Long gID)
     *@see #createGlyphAnimation(long duration, short type, Object data, Long gID, PostAnimationAction paa)
     *@see #createGlyphAnimation(long duration, int refresh, short type, Object data, Long gID, PostAnimationAction paa)
     */
    public void createGlyphAnimation(long duration, int refresh, short type, Object data, Long gID) throws ClassCastException {
	newGlyphAnim(duration, type, data, gID, refresh, null);
    }

    /** Animate a glyph.
     *@param duration in milliseconds
     *@param refresh refresh rate division factor. The animator manager runs at a rate as close as possible to 20ms. Some animations do not need to be refreshed that often. By setting a factor of N>1 for this animation, it is possible to tell the animator manager to apply the animation only every N iteration (default is 1). For instance, if the std refresh rate is indeed 20ms, and if refresh=3, then this animation will be executed only every 60 ms. The animation will still last the time indicated by duration.
     *@param type use one of (GL_TRANS_LIN, GL_TRANS_PAR, GL_TRANS_SIG, GL_SZ_LIN, GL_SZ_PAR, GL_SZ_SIG, GL_ROT_LIN, GL_ROT_PAR, GL_ROT_SIG, GL_COLOR_LIN)
     *@param data <br>- For translations, data is a LongPoint representing X and Y offsets<br>
     *            - For resize operations, data is a Float representing the resizing factor<br>
     *            - For rotations, data is a Float representing the rotation angle<br>
     *            - For coloration, data is a an array of 6 floats representing H,S and V offsets for fill and border color respectively. A 7th float can be appended to animate translucency (in which case the animation can only be applied to glyphs implementing the Translucent interface)
     *@param gID ID of glyph to be animated
     *@param paa action to perform after animation ends
     *@see #createGlyphAnimation(long duration, short type, Object data, Long gID)
     *@see #createGlyphAnimation(long duration, short type, Object data, Long gID, PostAnimationAction paa)
     *@see #createGlyphAnimation(long duration, int refresh, short type, Object data, Long gID)
     */
    public void createGlyphAnimation(long duration, int refresh, short type, Object data, Long gID, PostAnimationAction paa) throws ClassCastException {
	newGlyphAnim(duration, type, data, gID, refresh, paa);
    }

    private void putAsPendingGAnimation(Long gID,String dim,long duration,short type,Object data, PostAnimationAction paa){
	Vector pa;
	//look for other pending animations for this glyph and add this one to the end (FIFO)
	if (pendingGAnims.containsKey(gID)){
	    Hashtable animByDim=(Hashtable)pendingGAnims.get(gID);
	    if (animByDim.containsKey(dim)){pa=(Vector)animByDim.get(dim);}
	    else {pa=new Vector();}
	    pa.add(new AnimParams(duration,type,data, paa));
	    animByDim.put(dim,pa);
	}
	else {
	    pa=new Vector();
	    pa.add(new AnimParams(duration,type,data, paa));
	    Hashtable animByDim=new Hashtable();
	    animByDim.put(dim,pa);
	    pendingGAnims.put(gID,animByDim);
	}
    }

    /**kill a glyph animation - called by the animation itself when it stops*/
    synchronized void killGAnim(GAnimation gan,String dim){
	Long gID=gan.target.getID();   //get the glyph ID
	animGlyphBag.remove(gan);      //remove animation from bag of anims to be executed (it's over, kill it)
	if (animatedGlyphs.containsKey(gID)){//remove glyph from list of glyphs being animated
	    int[] animDims=(int[])animatedGlyphs.get(gID);
	    if (dim==GL_TRANS){animDims[0]=0;}
	    else if (dim==GL_SZ){animDims[1]=0;}
	    else if (dim==GL_ROT){animDims[2]=0;}
	    else if (dim==GL_COLOR){animDims[3]=0;}
	    if (allValuesEqualZero(animDims)){animatedGlyphs.remove(gID);}
	}
	gan.postAnimAction();
	if (pendingGAnims.containsKey(gID)){//look for first anim standing by whose target is this glyph
	    Hashtable pendingDims=(Hashtable)pendingGAnims.get(gID);
	    if (pendingDims.containsKey(dim)){
		Vector pa=(Vector)pendingDims.get(dim);
		AnimParams ap=(AnimParams)pa.elementAt(0);  //get its params
		pa.removeElementAt(0);  //remove the animation we're about to execute
		if (pa.isEmpty()) { //if there is no pending anim left, delete entry for this glyph
		    pendingDims.remove(dim);
		}
		//create the appropriate animation
		Glyph g = vsm.getGlyph(gID);
		if (g instanceof DPath)
		    this.createDPathAnimation(ap.duration, ap.type, (LongPoint[])ap.data, gID, ap.paa);
		else
		    this.newGlyphAnim(ap.duration, ap.type, ap.data, gID, 0, ap.paa);
		//remove entry for this glyph is there is no more anim in the queue
		if (pendingDims.isEmpty()){pendingGAnims.remove(gID);}
	    }
	}
    }

    /**
     * Interrupt an animation being executed
     *@param g glyph being animated
     *@param dim dimension animated (use one of GL_TRANS, GL_SZ, GL_ROT, GL_COLOR)
     *@param all also kill all animations waiting in the queue for this dimension (for this glyph) - has no effect if there is no animation waiting in the queue
     *@param finish true=put the glyph in its final state (i.e. the state in which it would be if the animation had not been interrupted) ; false=leave it in the current state (at the time when the animation is interrupted)
     */
    public void interruptGlyphAnimation(Glyph g,String dim,boolean all,boolean finish){
	GAnimation an=null;
	GAnimation tmpAn;
	for (int i=0;i<animGlyphBag.size();i++){
	    tmpAn=(GAnimation)animGlyphBag.elementAt(i);
	    if (tmpAn.target==g && tmpAn.type==dim){
		an=tmpAn;
		break;
	    }
	}
	if (an!=null){
	    if (all){//interrupt this animation and cancel pending animations for this attribute
		if (finish){//finish animation before killing it (assign final step to glyph)
		    an.conclude();
		}
		//else leave glyph in its current state
		if (pendingGAnims.containsKey(g.getID())){//cancel pending animations
		    Hashtable pendingDims=(Hashtable)pendingGAnims.get(g.getID());
		    if (pendingDims.containsKey(dim)){
			pendingDims.remove(dim);
			if (pendingDims.isEmpty()){pendingGAnims.remove(g.getID());}
		    }
		}
	    }
	    else {//interrupt this animation only, and take next animation
		if (finish){//finish animation before killing it (assign final step to glyph)
		    an.conclude();
		}
		//else leave glyph in its current state (nothing specific to do, just kill)
	    }
	    if (dim==AnimManager.GL_CTRL){killCurveAnim((GCurveCtrl)an);}
	    else {killGAnim(an,dim);}
	}
    }


    /* ----------------------- CURVE CONTROL POINT ANIMATION ------------------------- */

    /**animate a quadratic curve control point
     *@param duration in milliseconds
     *@param type use one of (GL_CP_TRANS_LIN, GL_CP_TRANS_PAR, GL_CP_TRANS_SIG)
     *@param data for translations, data is a PolarCoordinates representing distance and angle offsets
     *@param gID ID of glyph to be animated
     */
    public void createQdCurveCtrlPtAnimation(long duration,short type,Object data,Long gID, PostAnimationAction paa) {
	Glyph g=vsm.getGlyph(gID);
	//detect kind of curve and instanciate appropriate animation class
	if (g instanceof VQdCurve){
	    if ((animatedGlyphs.containsKey(gID)) && (((int[])animatedGlyphs.get(gID)).length<5)){//add two more dimensions in array of animated dimensions for this glyph since we are going to animate a control point
		animatedGlyphs.put(gID,addElementsToIntArray((int[])animatedGlyphs.get(gID),1));		
	    }
	    if (animatedGlyphs.containsKey(gID) && (((int[])animatedGlyphs.get(gID))[4]==1)){
		putAsPendingGAnimation(gID,GL_CTRL,duration,type,data, paa);
	    }
	    else {
		if (animatedGlyphs.containsKey(gID)){((int[])animatedGlyphs.get(gID))[4]=1;}
		else {int [] tmpA={0,0,0,0,0};tmpA[4]=1;animatedGlyphs.put(gID,tmpA);}
		GQdCurveCtrl an=new GQdCurveCtrl((VQdCurve)g,this,duration);
		an.setPostAnimationAction(paa);
		long rad=((VQdCurve)g).getCtrlPointRadius();
		float ang=((VQdCurve)g).getCtrlPointAngle();
		long trad=((PolarCoords)data).r;
		float tang=((PolarCoords)data).theta;
		double nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		an.steps=new PolarCoords[(int)nbSteps];
		switch(type){
		case GL_CP_TRANS_LIN:{//linear
		    double drad=trad/nbSteps;
		    double dang=tang/nbSteps;
		    for (int i=0;i<nbSteps-1;) {
			an.steps[i]=new PolarCoords((long)(rad+i*drad),(float)(ang+i*dang));
			i++;
		    }
		    an.steps[(int)nbSteps-1]=new PolarCoords(rad+trad,ang+tang);  //last point is assigned from source value in order to prevent precision error 
		    animGlyphBag.add(an);
		    an.start();   
		    break;
		}	
		case GL_CP_TRANS_PAR:{//parabolic  (^4)
		    double stepValue;
		    double drad,dang;
		    for (int i=0;i<nbSteps-1;) {
			stepValue=Math.pow((i+1)/nbSteps,4);
			an.steps[i++]=new PolarCoords(rad+(long)Math.round(trad*stepValue),(float)(ang+tang*stepValue));
		    }
		    an.steps[(int)nbSteps-1]=new PolarCoords(rad+trad,ang+tang);  //last point is assigned from source value in order to prevent precision error 
		    animGlyphBag.add(an);
		    an.start();   
		    break;
		}
		case GL_CP_TRANS_SIG:{//sigmoid
		    double stepValue;
		    double drad,dang;
		    for (int i=0;i<nbSteps-1;){
			stepValue=computeSigmoid(sigFactor,(i+1)/nbSteps);
			an.steps[i++]=new PolarCoords(rad+Math.round(trad*stepValue),(float)(ang+tang*stepValue));
		    }
		    an.steps[(int)nbSteps-1]=new PolarCoords(rad+trad,ang+tang);  //last point is assigned from source value in order to prevent precision error 
		    animGlyphBag.add(an);
		    an.start();   
		    break;
		}
		default:{
		    System.err.println("Error : AnimManager.createQdCurveCtrlPtAnimation : unknown animation type");
		}
		}
	    }
	}
	else {System.err.println("Error : AnimManager.createQdCurveCtrlPtAnimation : glyph is not a curve");}
    }
    
    /**animate a cubic curve control point
     *@param duration in milliseconds
     *@param type use one of (GL_CP_TRANS_LIN, GL_CP_TRANS_PAR, GL_CP_TRANS_SIG)
     *@param data for translations, data is a vector containing 2 PolarCoordinates representing distance and angle offsets for both control points
     *@param gID ID of glyph to be animated
     */
    public void createCbCurveCtrlPtAnimation(long duration,short type,Object data,Long gID, PostAnimationAction paa) {
	Glyph g=vsm.getGlyph(gID);
	//detect kind of curve and instanciate appropriate animation class
	if (g instanceof VCbCurve){
	    if ((animatedGlyphs.containsKey(gID)) && (((int[])animatedGlyphs.get(gID)).length<6)){//add two more dimensions in array of animated dimensions for this glyph since we are going to animate a control point
		animatedGlyphs.put(gID,addElementsToIntArray((int[])animatedGlyphs.get(gID),2));		
	    }
	    if (animatedGlyphs.containsKey(gID) && ((((int[])animatedGlyphs.get(gID))[4]==1) || (((int[])animatedGlyphs.get(gID))[5]==1))){//if at least one of the two control points is being animated
		putAsPendingGAnimation(gID,GL_CTRL,duration,type,data, paa);
	    }
	    else {
		if (animatedGlyphs.containsKey(gID)){((int[])animatedGlyphs.get(gID))[4]=1;}
		else {int [] tmpA={0,0,0,0,0};tmpA[4]=1;animatedGlyphs.put(gID,tmpA);}
		GCbCurveCtrl an=new GCbCurveCtrl((VCbCurve)g,this,duration);
		an.setPostAnimationAction(paa);
		long rad1=((VCbCurve)g).getCtrlPointRadius1();
		float ang1=((VCbCurve)g).getCtrlPointAngle1();
		long rad2=((VCbCurve)g).getCtrlPointRadius2();
		float ang2=((VCbCurve)g).getCtrlPointAngle2();
		long trad1=((PolarCoords)((Vector)data).elementAt(0)).r;
		float tang1=((PolarCoords)((Vector)data).elementAt(0)).theta;
		long trad2=((PolarCoords)((Vector)data).elementAt(1)).r;
		float tang2=((PolarCoords)((Vector)data).elementAt(1)).theta;
		double nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		switch(type){
		case GL_CP_TRANS_LIN:{//linear
		    if ((trad1!=0) || (tang1!=0)){
			an.steps=new PolarCoords[(int)nbSteps];
			double drad1=trad1/nbSteps;
			double dang1=tang1/nbSteps;
			for (int i=0;i<nbSteps-1;) {
			    an.steps[i]=new PolarCoords((long)(rad1+i*drad1),(float)(ang1+i*dang1));
			    i++;
			}
			an.steps[(int)nbSteps-1]=new PolarCoords(rad1+trad1,ang1+tang1);  //last point is assigned from source value in order to prevent precision error 
		    }
		    else {an.steps=null;}
		    if ((trad2!=0) || (tang2!=0)){
			an.steps2=new PolarCoords[(int)nbSteps];
			double drad2=trad2/nbSteps;
			double dang2=tang2/nbSteps;
			for (int i=0;i<nbSteps-1;) {
			    an.steps2[i]=new PolarCoords((long)(rad2+i*drad2),(float)(ang2+i*dang2));
			    i++;
			}
			an.steps2[(int)nbSteps-1]=new PolarCoords(rad2+trad2,ang2+tang2);  //last point is assigned from source value in order to prevent precision error 
		    }
		    else {an.steps2=null;}
		    animGlyphBag.add(an);
		    an.start();   
		    break;
		}	
		case GL_CP_TRANS_PAR:{//parabolic  (^4)
		    if ((trad1!=0) || (tang1!=0)){
			an.steps=new PolarCoords[(int)nbSteps];
			double stepValue1;
			for (int i=0;i<nbSteps-1;) {
			    stepValue1=Math.pow((i+1)/nbSteps,4);
			    an.steps[i++]=new PolarCoords(rad1+(long)Math.round(trad1*stepValue1),ang1+(float)(tang1*stepValue1));
			}
			an.steps[(int)nbSteps-1]=new PolarCoords(rad1+trad1,ang1+tang1);  //last point is assigned from source value in order to prevent precision error
		    }
		    else {an.steps=null;}
		    if ((trad2!=0) || (tang2!=0)){
			an.steps2=new PolarCoords[(int)nbSteps];
			double stepValue2;
			for (int i=0;i<nbSteps-1;) {
			    stepValue2=Math.pow((i+1)/nbSteps,4);
			    an.steps2[i++]=new PolarCoords(rad2+(long)Math.round(trad2*stepValue2),(float)(ang2+tang2*stepValue2));
			}
			an.steps2[(int)nbSteps-1]=new PolarCoords(rad2+trad2,ang2+tang2);  //last point is assigned from source value in order to prevent precision error
		    }
		    else {an.steps2=null;}
		    animGlyphBag.add(an);
		    an.start();   
		    break;
		}
		case GL_CP_TRANS_SIG:{//sigmoid
		    if ((trad1!=0) || (tang1!=0)){
			an.steps=new PolarCoords[(int)nbSteps];
			double stepValue1;
			for (int i=0;i<nbSteps-1;){
			    stepValue1=computeSigmoid(sigFactor,(i+1)/nbSteps);
			    an.steps[i++]=new PolarCoords(rad1+Math.round(trad1*stepValue1),(float)(ang1+tang1*stepValue1));
			}
			an.steps[(int)nbSteps-1]=new PolarCoords(rad1+trad1,ang1+tang1);  //last point is assigned from source value in order to prevent precision error
		    }
		    else {an.steps=null;}
		    if ((trad2!=0) || (tang2!=0)){
			an.steps2=new PolarCoords[(int)nbSteps];
			double stepValue2;
			for (int i=0;i<nbSteps-1;){
			    stepValue2=computeSigmoid(sigFactor,(i+1)/nbSteps);
			    an.steps2[i++]=new PolarCoords(rad2+Math.round(trad2*stepValue2),(float)(ang2+tang2*stepValue2));
			}
			an.steps2[(int)nbSteps-1]=new PolarCoords(rad2+trad2,ang2+tang2);  //last point is assigned from source value in order to prevent precision error
		    }
		    else {an.steps2=null;}
		    animGlyphBag.add(an);
		    an.start();   
		    break;
		}
		default:{
		    System.err.println("Error : AnimManager.createCbCurveCtrlPtAnimation : unknown animation type");
		}
		}
	    }
	}
	else {System.err.println("Error : AnimManager.createCbCurveCtrlPtAnimation : glyph is not a curve");}
    }



    /**kill a curve control point animation - called by the animation itself when it stops*/
    void killCurveAnim(GCurveCtrl gan){
	Long gID=gan.target.getID();   //get the glyph ID
	animGlyphBag.remove(gan);      //remove animation from bag of anims to be executed (it's over, kill it)
	if (animatedGlyphs.containsKey(gID)) {//remove glyph from list of glyphs being animated
	    int[] animDims=(int[])animatedGlyphs.get(gID);
	    animDims[4]=0;
	    if (allValuesEqualZero(animDims)){animatedGlyphs.remove(gID);}
	}
	gan.postAnimAction();
	if (pendingGAnims.containsKey(gID)) {  //look for first anim standing by whose target is this glyph	    
	    Hashtable pendingDims=(Hashtable)pendingGAnims.get(gID);
	    if (pendingDims.containsKey(GL_CTRL)){
		Vector pa=(Vector)pendingDims.get(GL_CTRL);
		AnimParams ap=(AnimParams)pa.elementAt(0);  //get its params
		pa.removeElementAt(0);  //remove the animation we're about to execute
		if (pa.isEmpty()) { //if there is no pending anim left, delete entry for this glyph
		    pendingDims.remove(GL_CTRL);
		}
		//create the appropriate animation
		Glyph curve=vsm.getGlyph(gID);
		if (curve instanceof VQdCurve){this.createQdCurveCtrlPtAnimation(ap.duration,ap.type,ap.data,gID, ap.paa);}
		else if (curve instanceof VCbCurve){this.createCbCurveCtrlPtAnimation(ap.duration,ap.type,ap.data,gID, ap.paa);}
	    }
	}
    }


    /* ----------------------- DPath ANIMATION ------------------------- */
    /** animate DPath transformation
     * @param duration duration of the animation in ms
     * @param type use on of the relative (GL_TRANS_LIN, GL_TRANS_PAR, GL_TRANS_SIG) or absolute (GL_TRANS_LIN_ABS, GL_TRANS_PAR_ABS, GL_TRANS_SIG_ABS)
     * @param data relative or absolute (depending on type) coordinates for each point of DPath
     * @param gID ID of DPath to be animated
     * @param paa action to be performed after animation finished
     */
    public void createDPathAnimation(long duration, short type, LongPoint[] data, Long gID, PostAnimationAction paa){
	Glyph g=vsm.getGlyph(gID);
	//detect kind of glyph and instanciate appropriate animation class
	if (g instanceof DPath){
	    if (animatedGlyphs.containsKey(gID) && (((int[])animatedGlyphs.get(gID))[0]==1)){
		putAsPendingGAnimation(gID, GL_TRANS, duration, type, data, paa);
	    }
	    else {
		if (animatedGlyphs.containsKey(gID)){((int[])animatedGlyphs.get(gID))[0]=1;}
		else {int [] tmpA={0,0,0,0,0};tmpA[4]=1;animatedGlyphs.put(gID,tmpA);}
		
		DPTransformation an = new DPTransformation(g, this, duration);
		an.setPostAnimationAction(paa);
		LongPoint[] currentPoints = ((DPath)g).getAllPointsCoordinates();
		double nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		
		// if data should be interpreted as absolute coordinates then convert them to relative and proceed
		if (type == GL_TRANS_LIN_ABS || type == GL_TRANS_PAR_ABS || type == GL_TRANS_SIG_ABS){
		    for (int i = 0; i < data.length; i++){
			data[i].x = data[i].x - currentPoints[i].x;
			data[i].y = data[i].y - currentPoints[i].y;			
		    }
		    switch(type){
		    case GL_TRANS_LIN_ABS: { type = GL_TRANS_LIN; break;}
		    case GL_TRANS_PAR_ABS: { type = GL_TRANS_PAR; break;}
		    case GL_TRANS_SIG_ABS: { type = GL_TRANS_SIG; break;}
		    }
		}
		
		switch(type){
		case GL_TRANS_LIN:{//linear		    
		    an.steps = new LongPoint[(int)nbSteps][data.length];
		    for (int step=0; step < nbSteps - 1; step++){
			LongPoint[] stepCoordinates = new LongPoint[data.length];
			for (int i=0; i < data.length; i++){
			    stepCoordinates[i] = new LongPoint(currentPoints[i].x + (step+1)*data[i].x/nbSteps, currentPoints[i].y + (step+1)*data[i].y/nbSteps);
			}
			an.steps[step] = stepCoordinates;
		    }
		    LongPoint[] stepCoordinates = new LongPoint[data.length];
		    for (int i=0; i < data.length; i++){
			stepCoordinates[i] = new LongPoint(currentPoints[i].x + data[i].x, currentPoints[i].y + data[i].y); //last point is assigned from source value in order to prevent precision error 
                    }
		    an.steps[(int)nbSteps - 1] = stepCoordinates;
		    animGlyphBag.add(an);
		    an.start();   
		    break;
		}
		case GL_TRANS_PAR:{//parabolic  (^4)
		    an.steps = new LongPoint[(int)nbSteps][data.length];
		    for (int step=0; step < nbSteps - 1; step++){
			LongPoint[] stepCoordinates = new LongPoint[data.length];
			double stepValue=Math.pow((step+1)/nbSteps,4);
                        for (int i=0; i < data.length; i++){
			    long dx=(long)Math.round(data[i].x*stepValue);
                            long dy=(long)Math.round(data[i].y*stepValue);
                            stepCoordinates[i] = new LongPoint(currentPoints[i].x + dx, currentPoints[i].y + dy);
                        }
			an.steps[step] = stepCoordinates;
		    }
		    LongPoint[] stepCoordinates = new LongPoint[data.length];
                    for (int i=0; i < data.length; i++){
                        stepCoordinates[i] = new LongPoint(currentPoints[i].x + data[i].x, currentPoints[i].y + data[i].y);
                    }
		    an.steps[(int)nbSteps - 1] = stepCoordinates;
		    animGlyphBag.add(an);
		    an.start();		    
		    break;
		}
		case GL_TRANS_SIG:{//sigmoid
		    an.steps = new LongPoint[(int)nbSteps][data.length];
		    for (int step=0; step < nbSteps - 1; step++){
			LongPoint[] stepCoordinates = new LongPoint[data.length];
			double stepValue=computeSigmoid(sigFactor,(step+1)/nbSteps);
                        for (int i=0; i < data.length; i++){
			    long dx=(long)Math.round(data[i].x*stepValue);
                            long dy=(long)Math.round(data[i].y*stepValue);
                            stepCoordinates[i] = new LongPoint(currentPoints[i].x + dx, currentPoints[i].y + dy);
                        }
			an.steps[step] = stepCoordinates;
		    }
		    LongPoint[] stepCoordinates = new LongPoint[data.length];
                    for (int i=0; i < data.length; i++){
                        stepCoordinates[i] = new LongPoint(currentPoints[i].x + data[i].x, currentPoints[i].y + data[i].y);
                    }
		    an.steps[(int)nbSteps - 1] = stepCoordinates;
		    animGlyphBag.add(an);
		    an.start();
		    
		    break;
		}
		
		default:{
		    System.err.println("Error : AnimManager.createCbCurveCtrlPtAnimation : unknown animation type");
		}
		}
	    }
	}
	else {System.err.println("Error : AnimManager.createCbCurveCtrlPtAnimation : glyph is not a curve");}
    }
    
    
    /* ----------------------- CAMERA ANIMATION ------------------------- */

    /**animate a camera
     *@param duration in milliseconds
     *@param type use one of (CA_TRANS_LIN, CA_TRANS_PAR, CA_TRANS_SIG, CA_ALT_LIN, CA_ALT_PAR, CA_ALT_SIG, CA_ALT_TRANS_LIN, CA_ALT_TRANS_SIG)
     *@param data for translations, data is LongPoint representing X and Y offsets<br>
     *            for zoom operations, data is an altitude offset (Float)<br>
     *            for zoom+translation, data is a a vector containing an altitude offset (Float) plus a LongPoint representing the x and y offsets
     *@param cID ID of camera to be animated
     */
    public void createCameraAnimation(long duration,short type,Object data,Integer cID) {
	createCameraAnimation(duration, type, data, cID, null);
    }

    /**animate a camera
     *@param duration in milliseconds
     *@param type use one of (CA_TRANS_LIN, CA_TRANS_PAR, CA_TRANS_SIG, CA_ALT_LIN, CA_ALT_PAR, CA_ALT_SIG, CA_ALT_TRANS_LIN, CA_ALT_TRANS_SIG)
     *@param data for translations, data is LongPoint representing X and Y offsets<br>
     *            for zoom operations, data is an altitude offset (Float)<br>
     *            for zoom+translation, data is a a vector containing an altitude offset (Float) plus a LongPoint representing the x and y offsets
     *@param cID ID of camera to be animated
     *@param paa action to perform after animation ends
     */
    public void createCameraAnimation(long duration,short type,Object data,Integer cID, PostAnimationAction paa) {
	Camera c=vsm.getCamera(cID);
	switch(type){
	case CA_TRANS_LIN:{//translation - linear
	    if (animatedCameras.containsKey(cID) && (((int[])animatedCameras.get(cID))[0]==1)){
		putAsPendingCAnimation(cID,CA_TRANS,duration,type,data, paa);
	    }
	    else {
		if (animatedCameras.containsKey(cID)){((int[])animatedCameras.get(cID))[0]=1;}
		else {int[] tmpA={1,0};animatedCameras.put(cID,tmpA);}
		double nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    CTranslation an=new CTranslation(c,this,duration);
		    an.setPostAnimationAction(paa);
		    long x=c.posx;
		    long y=c.posy;
		    long tx=((LongPoint)data).x;    //data is a LongPoint representing the x and y offsets
		    long ty=((LongPoint)data).y;
		    double dx=tx/nbSteps;
		    double dy=ty/nbSteps;
		    //System.out.println("["+x+" "+y+" "+tx+" "+ty+"/"+nbSteps+" "+dx+" "+dy+"]");
		    an.steps=new LongPoint[(int)nbSteps];
		    for (int i=0;i<nbSteps-1;) {
			an.steps[i]=new LongPoint((long)(x+i*dx),(long)(y+i*dy));
			//System.out.print(an.steps[i]);
			i++;
		    }
		    an.steps[(int)nbSteps-1]=new LongPoint(x+tx,y+ty);  //last point is assigned from source value in order to prevent precision error
		    animCameraBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case CA_TRANS_PAR:{//translation - parabolic  (^4)
	    if (animatedCameras.containsKey(cID) && (((int[])animatedCameras.get(cID))[0]==1)){
		putAsPendingCAnimation(cID,CA_TRANS,duration,type,data, paa);
	    }
	    else {
		if (animatedCameras.containsKey(cID)){((int[])animatedCameras.get(cID))[0]=1;}
		else {int [] tmpA={1,0};animatedCameras.put(cID,tmpA);}
		double nbSteps=Math.round((double)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    CTranslation an=new CTranslation(c,this,duration);
		    an.setPostAnimationAction(paa);
		    long x=c.posx;
		    long y=c.posy;
		    long tx=((LongPoint)data).x;    //data is a LongPoint representing the x and y offsets
		    long ty=((LongPoint)data).y;
		    an.steps=new LongPoint[(int)nbSteps];
		    double stepValue;
		    long dx,dy;
		    for (int i=0;i<nbSteps-1;) {
			stepValue=Math.pow((i+1)/nbSteps,4);
			dx=(long)Math.round(tx*stepValue);
			dy=(long)Math.round(ty*stepValue);
			//System.out.println("("+dx+","+dy+")");
			an.steps[i++]=new LongPoint(x+dx,y+dy);
		    }
		    an.steps[(int)nbSteps-1]=new LongPoint(x+tx,y+ty);   //last point is assigned from source value in order to prevent precision error
		    animCameraBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case CA_TRANS_SIG:{//translation - sigmoid
	    if (animatedCameras.containsKey(cID) && (((int[])animatedCameras.get(cID))[0]==1)){
		putAsPendingCAnimation(cID,CA_TRANS,duration,type,data, paa);
	    }
	    else {
		if (animatedCameras.containsKey(cID)){((int[])animatedCameras.get(cID))[0]=1;}
		else {int [] tmpA={1,0};animatedCameras.put(cID,tmpA);}
		double nbSteps=Math.round((double)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    CTranslation an=new CTranslation(c,this,duration);
		    an.setPostAnimationAction(paa);
		    long x=c.posx;
		    long y=c.posy;
		    long tx=((LongPoint)data).x;    //data is a LongPoint representing the x and y offsets
		    long ty=((LongPoint)data).y;
		    an.steps=new LongPoint[(int)nbSteps];
		    double stepValue;
		    long dx,dy;
		    for (int i=0;i<nbSteps-1;) {
			stepValue=computeSigmoid(sigFactor,(i+1)/nbSteps);
			dx=(long)Math.round(tx*stepValue);
			dy=(long)Math.round(ty*stepValue);
			an.steps[i++]=new LongPoint(x+dx,y+dy);
		    }
		    an.steps[(int)nbSteps-1]=new LongPoint(x+tx,y+ty);   //last point is assigned from source value in order to prevent precision error
		    animCameraBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case CA_ALT_LIN:{//zoom - linear
	    if (animatedCameras.containsKey(cID) && (((int[])animatedCameras.get(cID))[1]==1)){
		putAsPendingCAnimation(cID,CA_ALT,duration,type,data, paa);
	    }
	    else {
		if (animatedCameras.containsKey(cID)){((int[])animatedCameras.get(cID))[1]=1;}
		else {int [] tmpA={0,1};animatedCameras.put(cID,tmpA);}
		int nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    CZoom an=new CZoom(c,this,duration);
		    an.setPostAnimationAction(paa);
		    float alt=c.altitude;
		    float ta=((Float)data).floatValue();    //data is an altitude offset
		    double da=ta/nbSteps;
		    an.steps=new float[nbSteps];
		    float step;
		    for (int i=0;i<nbSteps-1;) {
			step=(float)(alt+i*da);
			if (step<vsm.zoomFloor){step=vsm.zoomFloor;}  //negative altitudes are the source of a JVM crash (related to the painting)
			an.steps[i]=step;
			//System.out.print("("+an.steps[i]+")");
			i++;
		    }
		    step=alt+ta;
		    if (step<vsm.zoomFloor){step=vsm.zoomFloor;}
		    an.steps[(int)nbSteps-1]=step;  //last point is assigned from source value in order to prevent precision error
		    animCameraBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case CA_ALT_PAR:{//zoom - parabolic  (^4)
	    if (animatedCameras.containsKey(cID) && (((int[])animatedCameras.get(cID))[1]==1)){
		putAsPendingCAnimation(cID,CA_ALT,duration,type,data, paa);
	    }
	    else {
		if (animatedCameras.containsKey(cID)){((int[])animatedCameras.get(cID))[1]=1;}
		else {int [] tmpA={0,1};animatedCameras.put(cID,tmpA);}
		double nbSteps=Math.round((double)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    CZoom an=new CZoom(c,this,duration);
		    an.setPostAnimationAction(paa);
		    float alt=c.altitude;
		    float ta=((Float)data).floatValue();    //data is an altitude offset
		    an.steps=new float[(int)nbSteps];
		    double stepValue;
		    float da;
		    float step;
		    for (int i=0;i<nbSteps-1;) {
			stepValue=Math.pow((i+1)/nbSteps,4);
			da=(float)ta*(float)stepValue;
			//System.out.print("("+da+")");
			step=alt+da;
			if (step<vsm.zoomFloor){step=vsm.zoomFloor;}
			an.steps[i++]=step;
		    }
		    step=alt+ta;
		    if (step<vsm.zoomFloor){step=vsm.zoomFloor;}
		    an.steps[(int)nbSteps-1]=step;   //last point is assigned from source value in order to prevent precision error
		    animCameraBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case CA_ALT_SIG:{//zoom - sigmoid
	    if (animatedCameras.containsKey(cID) && (((int[])animatedCameras.get(cID))[1]==1)){
		putAsPendingCAnimation(cID,CA_ALT,duration,type,data, paa);
	    }
	    else {
		if (animatedCameras.containsKey(cID)){((int[])animatedCameras.get(cID))[1]=1;}
		else {int [] tmpA={0,1};animatedCameras.put(cID,tmpA);}
		double nbSteps=Math.round((double)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    CZoom an=new CZoom(c,this,duration);
		    an.setPostAnimationAction(paa);
		    float alt=c.altitude;
		    float ta=((Float)data).floatValue();    //data is an altitude offset
		    an.steps=new float[(int)nbSteps];
		    double stepValue;
		    float da;
		    float step;
		    for (int i=0;i<nbSteps-1;) {
			stepValue=computeSigmoid(sigFactor,(i+1)/nbSteps);
			da=(float)ta*(float)stepValue;
			//System.out.print("("+da+")");
			step=alt+da;
			if (step<vsm.zoomFloor){step=vsm.zoomFloor;} //negative altitudes are the source of a JVM crash (related to the painting)
			an.steps[i++]=step;
		    }
		    step=alt+ta;
		    if (step<vsm.zoomFloor){step=vsm.zoomFloor;}
		    an.steps[(int)nbSteps-1]=step;   //last point is assigned from source value in order to prevent precision error
		    animCameraBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case CA_ALT_TRANS_SIG:{//trans+zoom - sigmoid
	    if (animatedCameras.containsKey(cID) && ((((int[])animatedCameras.get(cID))[0]==1) || (((int[])animatedCameras.get(cID))[1]==1))){
		putAsPendingCAnimation(cID,CA_BOTH,duration,type,data, paa);
	    }
	    else {
		if (animatedCameras.containsKey(cID)){((int[])animatedCameras.get(cID))[0]=1;((int[])animatedCameras.get(cID))[1]=1;}
		else {int [] tmpA={1,1};animatedCameras.put(cID,tmpA);}
		double nbSteps=Math.round((double)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    CTransZoom an=new CTransZoom(c,this,duration);
		    an.setPostAnimationAction(paa);
		    float alt=c.altitude;
		    long x=c.posx;
		    long y=c.posy;
		    float ta=((Float)((Vector)data).elementAt(0)).floatValue();    //data is a vector containing an altitude offset
		    long tx=((LongPoint)((Vector)data).elementAt(1)).x;                 //plus a LongPoint representing the x and y offsets
		    long ty=((LongPoint)((Vector)data).elementAt(1)).y;
		    an.zsteps=new float[(int)nbSteps];
		    an.tsteps=new LongPoint[(int)nbSteps];
		    double stepValue;
		    float da;
		    long dx,dy;
		    float step;
		    for (int i=0;i<nbSteps-1;) {
			stepValue=computeSigmoid(sigFactor,(i+1)/nbSteps);
			da=(float)ta*(float)stepValue;
			dx=(long)Math.round(tx*stepValue);
			dy=(long)Math.round(ty*stepValue);
			step=alt+da;
			if (step<vsm.zoomFloor){step=vsm.zoomFloor;} //negative altitudes are the source of a JVM crash (related to the painting)
			an.zsteps[i]=step;
			an.tsteps[i++]=new LongPoint(x+dx,y+dy);
		    }
		    step=alt+ta;if (step<vsm.zoomFloor){step=vsm.zoomFloor;}
		    an.zsteps[(int)nbSteps-1]=step;   //last point is assigned from source value in order to prevent precision error
		    an.tsteps[(int)nbSteps-1]=new LongPoint(x+tx,y+ty);
		    animCameraBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case CA_ALT_TRANS_LIN:{//trans+zoom - linear
	    if (animatedCameras.containsKey(cID) && ((((int[])animatedCameras.get(cID))[0]==1) || (((int[])animatedCameras.get(cID))[1]==1))){
		putAsPendingCAnimation(cID,CA_BOTH,duration,type,data, paa);
	    }
	    else {
		if (animatedCameras.containsKey(cID)){((int[])animatedCameras.get(cID))[0]=1;((int[])animatedCameras.get(cID))[1]=1;}
		else {int [] tmpA={1,1};animatedCameras.put(cID,tmpA);}
		double nbSteps = Math.round((double)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    CTransZoom an = new CTransZoom(c,this,duration);
		    an.setPostAnimationAction(paa);
		    float alt = c.altitude;
		    long x = c.posx;
		    long y = c.posy;
		    float ta = ((Float)((Vector)data).elementAt(0)).floatValue();    //data is a vector containing an altitude offset
		    long tx = ((LongPoint)((Vector)data).elementAt(1)).x;            //plus a LongPoint representing the x and y offsets
		    long ty = ((LongPoint)((Vector)data).elementAt(1)).y;
		    an.zsteps = new float[(int)nbSteps];
		    an.tsteps = new LongPoint[(int)nbSteps];
		    double da=ta/nbSteps;
		    double dx = tx/nbSteps;
		    double dy = ty/nbSteps;
		    float step;
		    for (int i=0;i<nbSteps-1;i++) {
			step = (float)(alt+i*da);
			if (step<vsm.zoomFloor){step = vsm.zoomFloor;} //negative altitudes are the source of a JVM crash (related to the painting)
			an.zsteps[i] = step;
			an.tsteps[i] = new LongPoint((long)(x+i*dx),(long)(y+i*dy));
		    }
		    step = alt+ta;
		    if (step < vsm.zoomFloor){step = vsm.zoomFloor;}
		    an.zsteps[(int)nbSteps-1] = step;   //last point is assigned from source value in order to prevent precision error
		    an.tsteps[(int)nbSteps-1] = new LongPoint(x+tx,y+ty);
		    animCameraBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	default:{
	    System.err.println("Error : AnimManager.createCameraAnimation : unknown animation type");
	}
	}
    }

    void putAsPendingCAnimation(Integer cID,String dim,long duration,short type,Object data, PostAnimationAction paa){
	Vector pa;
	//look for other pending animations for this camera and add this one to the end (FIFO)
	if (pendingCAnims.containsKey(cID)){
	    Hashtable animByDim=(Hashtable)pendingCAnims.get(cID);
	    if (animByDim.containsKey(dim)){pa=(Vector)animByDim.get(dim);}
	    else {pa=new Vector();}
	    pa.add(new AnimParams(duration,type,data, paa));
	    animByDim.put(dim,pa);
	}
	else {
	    pa=new Vector();
	    pa.add(new AnimParams(duration,type,data, paa));
	    Hashtable animByDim=new Hashtable();
	    animByDim.put(dim,pa);
	    pendingCAnims.put(cID,animByDim);
	}	
    }

    void killCAnim(CAnimation can,String dim){
	Integer cID=can.target.getID();   //get the camera ID
	animCameraBag.remove(can);      //remove animation from bag of anims to be executed (it's over, kill it)
	if (animatedCameras.containsKey(cID)){//remove camera from list of cameras being animated
	    int[] animDims=(int[])animatedCameras.get(cID);
	    if (dim.equals(CA_TRANS)){animDims[0]=0;}
	    else if (dim.equals(CA_ALT)){animDims[1]=0;}
	    else if (dim.equals(CA_BOTH)){animDims[0]=0;animDims[1]=0;}
	    if (allValuesEqualZero(animDims)){animatedCameras.remove(cID);}
	}
	can.postAnimAction();
	if (pendingCAnims.containsKey(cID)) {  //look for first anim standing by whose target is this camera	    
	    Hashtable pendingDims=(Hashtable)pendingCAnims.get(cID);
	    if (pendingDims.containsKey(dim)){
		Vector pa=(Vector)pendingDims.get(dim);
		AnimParams ap=(AnimParams)pa.elementAt(0);  //get its params
		pa.removeElementAt(0);  //remove the animation we're about to execute
		if (pa.isEmpty()) { //if there is no pending anim left, delete entry for this glyph
		    pendingDims.remove(dim);
		    if (pendingDims.isEmpty()){pendingCAnims.remove(cID);}
		}
		this.createCameraAnimation(ap.duration,ap.type,ap.data,cID, ap.paa);    //create the appropriate animation
	    }
	}
    }

    /**
     * Interrupt a camera animation being executed
     *@param c camera being animated
     *@param dim dimension animated (use one of CA_TRANS, CA_ALT, CA_BOTH)
     *@param all also kill all animations waiting in the queue for this dimension (for this camera) - has no effect if there is no animation waiting in the queue
     *@param finish true=put the camera in its final state (i.e. the state in which it would be if the animation had not been interrupted) ; false=leave it in the current state (at the time when the animation is interrupted)
     */
    public void interruptCameraAnimation(Camera c,String dim,boolean all,boolean finish){
	CAnimation an=null;
	CAnimation tmpAn;
	for (int i=0;i<animCameraBag.size();i++){
	    tmpAn=(CAnimation)animCameraBag.elementAt(i);
	    if (tmpAn.target==c && tmpAn.type==dim){
		an=tmpAn;
		break;
	    }
	}
	if (an!=null){
	    if (all){//interrupt this animation and cancel pending animations for this attribute
		if (finish){//finish animation before killing it (assign final step to glyph)
		    an.conclude();
		}
		//else leave glyph in its current state
		if (pendingCAnims.containsKey(c.getID())){//cancel pending animations
		    Hashtable pendingDims=(Hashtable)pendingCAnims.get(c.getID());
		    if (pendingDims.containsKey(dim)){
			pendingDims.remove(dim);
			if (pendingDims.isEmpty()){pendingCAnims.remove(c.getID());}
		    }
		}
	    }
	    else {//interrupt this animation only, and take next animation
		if (finish){//finish animation before killing it (assign final step to glyph)
		    an.conclude();
		}
		//else leave camera in its current state (nothing specific to do, just kill)
	    }
	    killCAnim(an,dim);
	}
    }


    /* ----------------------- LENS ANIMATION ------------------------- */

    /**animate a lens
     *@param duration in milliseconds
     *@param type use one of (LS_MM_LIN, LS_MM_PAR, LS_MM_SIG, LS_RD_LIN, LS_RD_PAR, LS_RD_SIG, LS_MM_RD_LIN, LS_MM_RD_PAR, LS_MM_RD_SIG)
     *@param data for radii, data is an array of int[2] ; the first value is the outer radius offset, the second value is the inner radius offset<br>
     *            for maximum magnification factor, data is a factor offset (Float)<br>
     *            for max magnification and radii, data is a vector containing a Float (factor offset) and an array of int[2] representing outer and inner radius offsets
     *@param lID ID of lens to be animated
     *@param kill true if lens should be removed from view after this animation ends
     */
    public void createLensAnimation(long duration,short type,Object data,Integer lID, boolean kill){
	if (kill){
	    createLensAnimation(duration, type, data, lID, new LensKillAction(vsm));
	}
	else {
	    createLensAnimation(duration, type, data, lID, null);
	}
    }

    /**animate a lens
     *@param duration in milliseconds
     *@param type use one of (LS_MM_LIN, LS_MM_PAR, LS_MM_SIG, LS_RD_LIN, LS_RD_PAR, LS_RD_SIG, LS_MM_RD_LIN, LS_MM_RD_PAR, LS_MM_RD_SIG)
     *@param data for radii, data is an array of int[2] ; the first value is the outer radius offset, the second value is the inner radius offset<br>
     *            for maximum magnification factor, data is a factor offset (Float)<br>
     *            for max magnification and radii, data is a vector containing a Float (factor offset) and an array of int[2] representing outer and inner radius offsets
     *@param lID ID of lens to be animated
     *@param paa action to perform after animation ends
     */
    public void createLensAnimation(long duration,short type,Object data,Integer lID, PostAnimationAction paa){
	Lens l = vsm.getLens(lID);
	if (l == null){return;}
	switch(type){
	case LS_RD_LIN:{//radii - linear
	    if (animatedLenses.containsKey(lID) && (((int[])animatedLenses.get(lID))[1] == 1)){
		putAsPendingLAnimation(lID,LS_RD,duration,type,data, paa);
	    }
	    else {
		if (animatedLenses.containsKey(lID)){((int[])animatedLenses.get(lID))[1]=1;}
		else {int[] tmpA = {0,1};animatedLenses.put(lID,tmpA);}
		double nbSteps = Math.round((float)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    int r1 = ((FixedSizeLens)l).getOuterRadius();
		    int r2 = ((FixedSizeLens)l).getInnerRadius();
		    FSLRadii an = new FSLRadii((FixedSizeLens)l, this, duration);
		    an.setPostAnimationAction(paa);
		    int[] offsets = (int[])data;
		    int tr1 = offsets[0];    //data is an array of int[2] representing offsets for outer and inner radii
		    int tr2 = offsets[1];
		    double dr1 = tr1/nbSteps;
		    double dr2 = tr2/nbSteps;
		    an.steps = new int[(int)nbSteps][2];
		    for (int i=0;i<nbSteps-1;) {
			an.steps[i][0] = (int)Math.round(r1 + i * dr1);
			an.steps[i][1] = (int)Math.round(r2 + i * dr2);
			i++;
		    }
		    an.steps[(int)nbSteps-1][0] = r1 + tr1;
		    an.steps[(int)nbSteps-1][1] = r2 + tr2;
		    // change magnification buffer size
		    if (an.steps[an.steps.length-1][0] > an.steps[0][0]){// before animation if it gets bigger
			l.setMagRasterDimensions(Math.round(2*l.getMaximumMagnification()*an.steps[an.steps.length-1][0]));
		    }
		    else {// after animation if it gets smaller
			an.setFinalRasterSize(Math.round(2*l.getMaximumMagnification()*an.steps[an.steps.length-1][0]));
		    }
		    animLensBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case LS_RD_PAR:{//radii - parabolic (^4)
	    if (animatedLenses.containsKey(lID) && (((int[])animatedLenses.get(lID))[1] == 1)){
		putAsPendingLAnimation(lID,LS_RD,duration,type,data, paa);
	    }
	    else {
		if (animatedLenses.containsKey(lID)){((int[])animatedLenses.get(lID))[1]=1;}
		else {int[] tmpA = {0,1};animatedLenses.put(lID,tmpA);}
		double nbSteps = Math.round((float)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    FSLRadii an = new FSLRadii((FixedSizeLens)l,this,duration);
		    an.setPostAnimationAction(paa);
		    int r1 = ((FixedSizeLens)l).getOuterRadius();
		    int r2 = ((FixedSizeLens)l).getInnerRadius();
		    int[] offsets = (int[])data;
		    int tr1 = offsets[0];    //data is an array of int[2] representing offsets for outer and inner radii
		    int tr2 = offsets[1];
		    double stepValue;
		    an.steps=new int[(int)nbSteps][2];
		    for (int i=0;i<nbSteps-1;) {
			stepValue=Math.pow((i+1)/nbSteps,4);
			an.steps[i][0] = (int)Math.round(r1 + tr1*stepValue);
			an.steps[i][1] = (int)Math.round(r2 + tr2*stepValue);
			i++;
		    }
		    an.steps[(int)nbSteps-1][0] = r1 + tr1;
		    an.steps[(int)nbSteps-1][1] = r2 + tr2;
		    // change magnification buffer size
		    if (an.steps[an.steps.length-1][0] > an.steps[0][0]){// before animation if it gets bigger
			l.setMagRasterDimensions(Math.round(2*l.getMaximumMagnification()*an.steps[an.steps.length-1][0]));
		    }
		    else {// after animation if it gets smaller
			an.setFinalRasterSize(Math.round(2*l.getMaximumMagnification()*an.steps[an.steps.length-1][0]));
		    }
		    animLensBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case LS_RD_SIG:{//radii - sigmoid
	    if (animatedLenses.containsKey(lID) && (((int[])animatedLenses.get(lID))[1] == 1)){
		putAsPendingLAnimation(lID,LS_RD,duration,type,data, paa);
	    }
	    else {
		if (animatedLenses.containsKey(lID)){((int[])animatedLenses.get(lID))[1]=1;}
		else {int[] tmpA = {0,1};animatedLenses.put(lID,tmpA);}
		double nbSteps = Math.round((float)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    FSLRadii an = new FSLRadii((FixedSizeLens)l,this,duration);
		    an.setPostAnimationAction(paa);
		    int r1 = ((FixedSizeLens)l).getOuterRadius();
		    int r2 = ((FixedSizeLens)l).getInnerRadius();
		    int[] offsets = (int[])data;
		    int tr1 = offsets[0];    //data is an array of int[2] representing offsets for outer and inner radii
		    int tr2 = offsets[1];
		    double stepValue;
		    an.steps=new int[(int)nbSteps][2];
		    for (int i=0;i<nbSteps-1;) {
			stepValue = computeSigmoid(sigFactor,(i+1)/nbSteps);
			an.steps[i][0] = (int)Math.round(r1 + tr1*stepValue);
			an.steps[i][1] = (int)Math.round(r2 + tr2*stepValue);
			i++;
		    }
		    an.steps[(int)nbSteps-1][0] = r1 + tr1;
		    an.steps[(int)nbSteps-1][1] = r2 + tr2;
		    // change magnification buffer size
		    if (an.steps[an.steps.length-1][0] > an.steps[0][0]){// before animation if it gets bigger
			l.setMagRasterDimensions(Math.round(2*l.getMaximumMagnification()*an.steps[an.steps.length-1][0]));
		    }
		    else {// after animation if it gets smaller
			an.setFinalRasterSize(Math.round(2*l.getMaximumMagnification()*an.steps[an.steps.length-1][0]));
		    }
		    animLensBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case LS_MM_LIN:{//maximum magnification - linear
	    if (animatedLenses.containsKey(lID) && (((int[])animatedLenses.get(lID))[0] == 1)){
		putAsPendingLAnimation(lID,LS_MM,duration,type,data, paa);
	    }
	    else {
		if (animatedLenses.containsKey(lID)){((int[])animatedLenses.get(lID))[0]=1;}
		else {int[] tmpA = {1,0};animatedLenses.put(lID,tmpA);}
		double nbSteps = Math.round((float)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    LMaximumMagnification an = new LMaximumMagnification(l,this,duration);
		    an.setPostAnimationAction(paa);
		    float mm = l.getMaximumMagnification();
		    float tmm = ((Float)data).floatValue();    //data is an magnification offset
		    double dmm = tmm/nbSteps;
		    an.steps=new float[(int)nbSteps];
		    float step;
		    for (int i=0;i<nbSteps-1;) {
			step=(float)(mm+i*dmm);
			if (step < Lens.MM_FLOOR){step = Lens.MM_FLOOR;}
			an.steps[i]=step;
			i++;
		    }
		    step=mm+tmm;
		    if (step < Lens.MM_FLOOR){step = Lens.MM_FLOOR;}
		    an.steps[(int)nbSteps-1]=step;  //last point is assigned from source value in order to prevent precision error
		    // change magnification buffer size
		    if (step > an.steps[0]){// before animation if it gets bigger
			l.setMagRasterDimensions(Math.round(2*step*l.getRadius()));
		    }
		    else {// after animation if it gets smaller
			an.setFinalRasterSize(Math.round(2*step*l.getRadius()));
		    }
		    animLensBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case LS_MM_PAR:{//maximum magnification - parabolic  (^4)
	    if (animatedLenses.containsKey(lID) && (((int[])animatedLenses.get(lID))[0] == 1)){
		putAsPendingLAnimation(lID,LS_MM,duration,type,data, paa);
	    }
	    else {
		if (animatedLenses.containsKey(lID)){((int[])animatedLenses.get(lID))[0]=1;}
		else {int[] tmpA = {1,0};animatedLenses.put(lID,tmpA);}
		double nbSteps = Math.round((float)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    LMaximumMagnification an = new LMaximumMagnification(l,this,duration);
		    an.setPostAnimationAction(paa);
		    float mm = l.getMaximumMagnification();
		    float tmm = ((Float)data).floatValue();    //data is an magnification offset
		    an.steps=new float[(int)nbSteps];
		    double stepValue;
		    float dmm;
		    float step;
		    for (int i=0;i<nbSteps-1;) {
			stepValue = Math.pow((i+1)/nbSteps,4);
			dmm = (float)tmm*(float)stepValue;
			step = mm+dmm;
			if (step < Lens.MM_FLOOR){step = Lens.MM_FLOOR;}
			an.steps[i++]=step;
		    }
		    step = mm+tmm;
		    if (step < Lens.MM_FLOOR){step = Lens.MM_FLOOR;}
		    an.steps[(int)nbSteps-1] = step;  //last point is assigned from source value in order to prevent precision error
		    // change magnification buffer size
		    if (step > an.steps[0]){// before animation if it gets bigger
			l.setMagRasterDimensions(Math.round(2*step*l.getRadius()));
		    }
		    else {// after animation if it gets smaller
			an.setFinalRasterSize(Math.round(2*step*l.getRadius()));
		    }
		    animLensBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case LS_MM_SIG:{//maximum magnification - sigmoid
	    if (animatedLenses.containsKey(lID) && (((int[])animatedLenses.get(lID))[0] == 1)){
		putAsPendingLAnimation(lID,LS_MM,duration,type,data, paa);
	    }
	    else {
		if (animatedLenses.containsKey(lID)){((int[])animatedLenses.get(lID))[0]=1;}
		else {int[] tmpA = {1,0};animatedLenses.put(lID,tmpA);}
		double nbSteps = Math.round((float)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    LMaximumMagnification an = new LMaximumMagnification(l,this,duration);
		    an.setPostAnimationAction(paa);
		    float mm = l.getMaximumMagnification();
		    float tmm = ((Float)data).floatValue();    //data is an magnification offset
		    an.steps=new float[(int)nbSteps];
		    double stepValue;
		    float dmm;
		    float step;
		    for (int i=0;i<nbSteps-1;) {
			stepValue=computeSigmoid(sigFactor,(i+1)/nbSteps);
			dmm = (float)tmm*(float)stepValue;
			step = mm+dmm;
			if (step < Lens.MM_FLOOR){step = Lens.MM_FLOOR;}
			an.steps[i++]=step;
		    }
		    step = mm+tmm;
		    if (step < Lens.MM_FLOOR){step = Lens.MM_FLOOR;}
		    an.steps[(int)nbSteps-1] = step;  //last point is assigned from source value in order to prevent precision error
		    // change magnification buffer size
		    if (step > an.steps[0]){// before animation if it gets bigger
			l.setMagRasterDimensions(Math.round(2*step*l.getRadius()));
		    }
		    else {// after animation if it gets smaller
			an.setFinalRasterSize(Math.round(2*step*l.getRadius()));
		    }
		    animLensBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case LS_MM_RD_LIN:{//radii + maximu magnification - linear
	    if (animatedLenses.containsKey(lID) && (((int[])animatedLenses.get(lID))[0] == 1)){
		putAsPendingLAnimation(lID,LS_BOTH,duration,type,data, paa);
	    }
	    else {
		if (animatedLenses.containsKey(lID)){((int[])animatedLenses.get(lID))[0]=1;}
		else {int[] tmpA = {1,0};animatedLenses.put(lID,tmpA);}
		double nbSteps = Math.round((float)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    FSLMaxMagRadii an = new FSLMaxMagRadii((FixedSizeLens)l,this,duration);
		    an.setPostAnimationAction(paa);
		    int r1 = ((FixedSizeLens)l).getOuterRadius();
		    int r2 = ((FixedSizeLens)l).getInnerRadius();
		    int[] offsets = (int[])(((Vector)data).elementAt(1));
		    int tr1 = offsets[0];    //data is an array of int[2] representing offsets for outer and inner radii
		    int tr2 = offsets[1];
		    double dr1 = tr1/nbSteps;
		    double dr2 = tr2/nbSteps;
		    an.rsteps=new int[(int)nbSteps][2];
		    for (int i=0;i<nbSteps-1;) {
			an.rsteps[i][0] = (int)Math.round(r1 + i * dr1);
			an.rsteps[i][1] = (int)Math.round(r2 + i * dr2);
			i++;
		    }
		    an.rsteps[(int)nbSteps-1][0] = r1 + tr1;
		    an.rsteps[(int)nbSteps-1][1] = r2 + tr2;
		    float mm = l.getMaximumMagnification();
		    float tmm = ((Float)(((Vector)data).elementAt(0))).floatValue();    //data is an magnification offset
		    double dmm = tmm/nbSteps;
		    an.mmsteps = new float[(int)nbSteps];
		    float step;
		    for (int i=0;i<nbSteps-1;) {
			step = (float)(mm+i*dmm);
			if (step < Lens.MM_FLOOR){step = Lens.MM_FLOOR;}
			an.mmsteps[i] = step;
			i++;
		    }
		    step = mm+tmm;
		    if (step < Lens.MM_FLOOR){step = Lens.MM_FLOOR;}
		    an.mmsteps[(int)nbSteps-1] = step;  //last point is assigned from source value in order to prevent precision error
		    // change magnification buffer size
		    if (an.rsteps[an.rsteps.length-1][0] * an.mmsteps[an.mmsteps.length-1] > an.rsteps[0][0] * an.mmsteps[0]){// before animation if it gets bigger
			l.setMagRasterDimensions(Math.round(2*an.rsteps[an.rsteps.length-1][0] * an.mmsteps[an.mmsteps.length-1]));
		    }
		    else {// after animation if it gets smaller
			an.setFinalRasterSize(Math.round(2*an.rsteps[an.rsteps.length-1][0] * an.mmsteps[an.mmsteps.length-1]));
		    }
		    animLensBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case LS_MM_RD_PAR:{//radii + maximu magnification - parabolic (^4)
	    if (animatedLenses.containsKey(lID) && (((int[])animatedLenses.get(lID))[0] == 1)){
		putAsPendingLAnimation(lID,LS_BOTH,duration,type,data, paa);
	    }
	    else {
		if (animatedLenses.containsKey(lID)){((int[])animatedLenses.get(lID))[0]=1;}
		else {int[] tmpA = {1,0};animatedLenses.put(lID,tmpA);}
		double nbSteps = Math.round((float)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    FSLMaxMagRadii an = new FSLMaxMagRadii((FixedSizeLens)l,this,duration);
		    an.setPostAnimationAction(paa);
		    int r1 = ((FixedSizeLens)l).getOuterRadius();
		    int r2 = ((FixedSizeLens)l).getInnerRadius();
		    int[] offsets = (int[])(((Vector)data).elementAt(1));
		    int tr1 = offsets[0];    //data is an array of int[2] representing offsets for outer and inner radii
		    int tr2 = offsets[1];
		    double stepValue;
		    an.rsteps=new int[(int)nbSteps][2];
		    for (int i=0;i<nbSteps-1;) {
			stepValue=Math.pow((i+1)/nbSteps,4);
			an.rsteps[i][0] = (int)Math.round(r1 + tr1*stepValue);
			an.rsteps[i][1] = (int)Math.round(r2 + tr2*stepValue);
			i++;
		    }
		    an.rsteps[(int)nbSteps-1][0] = r1 + tr1;
		    an.rsteps[(int)nbSteps-1][1] = r2 + tr2;
		    float mm = l.getMaximumMagnification();
		    float tmm = ((Float)(((Vector)data).elementAt(0))).floatValue();    //data is an magnification offset
		    float dmm, step;
		    an.mmsteps = new float[(int)nbSteps];
		    for (int i=0;i<nbSteps-1;) {
			stepValue = Math.pow((i+1)/nbSteps,4);
			dmm = (float)tmm*(float)stepValue;
			step = mm+dmm;
			if (step < Lens.MM_FLOOR){step = Lens.MM_FLOOR;}
			an.mmsteps[i++]=step;
		    }
		    step = mm+tmm;
		    if (step < Lens.MM_FLOOR){step = Lens.MM_FLOOR;}
		    an.mmsteps[(int)nbSteps-1] = step;  //last point is assigned from source value in order to prevent precision error
		    // change magnification buffer size
		    if (an.rsteps[an.rsteps.length-1][0] * an.mmsteps[an.mmsteps.length-1] > an.rsteps[0][0] * an.mmsteps[0]){// before animation if it gets bigger
			l.setMagRasterDimensions(Math.round(2*an.rsteps[an.rsteps.length-1][0] * an.mmsteps[an.mmsteps.length-1]));
		    }
		    else {// after animation if it gets smaller
			an.setFinalRasterSize(Math.round(2*an.rsteps[an.rsteps.length-1][0] * an.mmsteps[an.mmsteps.length-1]));
		    }
		    animLensBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case LS_MM_RD_SIG:{//radii + maximu magnification - sigmoid
	    if (animatedLenses.containsKey(lID) && (((int[])animatedLenses.get(lID))[0] == 1)){
		putAsPendingLAnimation(lID,LS_BOTH,duration,type,data, paa);
	    }
	    else {
		if (animatedLenses.containsKey(lID)){((int[])animatedLenses.get(lID))[0]=1;}
		else {int[] tmpA = {1,0};animatedLenses.put(lID,tmpA);}
		double nbSteps = Math.round((float)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    FSLMaxMagRadii an = new FSLMaxMagRadii((FixedSizeLens)l,this,duration);
		    an.setPostAnimationAction(paa);
		    int r1 = ((FixedSizeLens)l).getOuterRadius();
		    int r2 = ((FixedSizeLens)l).getInnerRadius();
		    int[] offsets = (int[])(((Vector)data).elementAt(1));
		    int tr1 = offsets[0];    //data is an array of int[2] representing offsets for outer and inner radii
		    int tr2 = offsets[1];
		    double stepValue;
		    an.rsteps=new int[(int)nbSteps][2];
		    for (int i=0;i<nbSteps-1;) {
			stepValue = computeSigmoid(sigFactor,(i+1)/nbSteps);
			an.rsteps[i][0] = (int)Math.round(r1 + tr1*stepValue);
			an.rsteps[i][1] = (int)Math.round(r2 + tr2*stepValue);
			i++;
		    }
		    an.rsteps[(int)nbSteps-1][0] = r1 + tr1;
		    an.rsteps[(int)nbSteps-1][1] = r2 + tr2;
		    float mm = l.getMaximumMagnification();
		    float tmm = ((Float)(((Vector)data).elementAt(0))).floatValue();    //data is an magnification offset
		    float dmm, step;
		    an.mmsteps = new float[(int)nbSteps];
		    for (int i=0;i<nbSteps-1;) {
			stepValue=computeSigmoid(sigFactor,(i+1)/nbSteps);
			dmm = (float)tmm*(float)stepValue;
			step = mm+dmm;
			if (step < Lens.MM_FLOOR){step = Lens.MM_FLOOR;}
			an.mmsteps[i++]=step;
		    }
		    step = mm+tmm;
		    if (step < Lens.MM_FLOOR){step = Lens.MM_FLOOR;}
		    an.mmsteps[(int)nbSteps-1] = step;  //last point is assigned from source value in order to prevent precision error
		    // change magnification buffer size
		    if (an.rsteps[an.rsteps.length-1][0] * an.mmsteps[an.mmsteps.length-1] > an.rsteps[0][0] * an.mmsteps[0]){// before animation if it gets bigger
			l.setMagRasterDimensions(Math.round(2*an.rsteps[an.rsteps.length-1][0] * an.mmsteps[an.mmsteps.length-1]));
		    }
		    else {// after animation if it gets smaller
			an.setFinalRasterSize(Math.round(2*an.rsteps[an.rsteps.length-1][0] * an.mmsteps[an.mmsteps.length-1]));
		    }
		    animLensBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	default:{
	    System.err.println("Error : AnimManager.createLensAnimation : unknown animation type");
	}
	}
    }

    //XXX: should probably add finalRasterSize data to the set of parameters remembered
    void putAsPendingLAnimation(Integer lID,String dim,long duration,short type,Object data, PostAnimationAction paa){
	Vector pa;
	//look for other pending animations for this lens and add this one to the end (FIFO)
	if (pendingLAnims.containsKey(lID)){
	    Hashtable animByDim=(Hashtable)pendingLAnims.get(lID);
	    if (animByDim.containsKey(dim)){pa=(Vector)animByDim.get(dim);}
	    else {pa=new Vector();}
	    pa.add(new AnimParams(duration,type,data, paa));
	    animByDim.put(dim,pa);
	}
	else {
	    pa=new Vector();
	    pa.add(new AnimParams(duration,type,data, paa));
	    Hashtable animByDim=new Hashtable();
	    animByDim.put(dim,pa);
	    pendingLAnims.put(lID,animByDim);
	}
    }

    public void killLAnim(LAnimation lan,String dim){
	lan.updateRaster();
	Integer lID=lan.target.getID();   //get the lens ID
	animLensBag.remove(lan);      //remove animation from bag of anims to be executed (it's over, kill it)
	if (animatedLenses.containsKey(lID)){//remove lens from list of lenses being animated
	    int[] animDims=(int[])animatedLenses.get(lID);
	    if (dim.equals(LS_MM)){animDims[0]=0;}
	    else if (dim.equals(LS_RD)){animDims[1]=0;}
	    else if (dim.equals(LS_BOTH)){animDims[0]=0;animDims[1]=0;}
	    if (allValuesEqualZero(animDims)){animatedLenses.remove(lID);}
	}
	lan.postAnimAction();
	if (pendingLAnims.containsKey(lID)){  //look for first anim standing by whose target is this lens	    
	    Hashtable pendingDims=(Hashtable)pendingLAnims.get(lID);
	    if (pendingDims.containsKey(dim)){
		Vector pa=(Vector)pendingDims.get(dim);
		AnimParams ap=(AnimParams)pa.elementAt(0);  //get its params
		pa.removeElementAt(0);  //remove the animation we're about to execute
		if (pa.isEmpty()) { //if there is no pending anim left, delete entry for this glyph
		    pendingDims.remove(dim);
		    if (pendingDims.isEmpty()){pendingLAnims.remove(lID);}
		}
		this.createLensAnimation(ap.duration,ap.type,ap.data,lID,ap.paa);  //create the appropriate animation
	    }
	}
    }

    /* ----------------------- PORTAL ANIMATION ------------------------- */

    /**animate a portal
     *@param duration in milliseconds
     *@param type use one of (PT_TRANS_LIN, PT_TRANS_SIG, PT_SZ_LIN, PT_SZ_SIG, PT_SZ_TRANS_LIN, PT_SZ_TRANS_SIG, PT_ALPHA_LIN)
     *@param data for translations, data is java.awt.Point representing X and Y offsets<br>
     *            for size, data is a java.awt.Point offset <br>
     *            for size + translation, data is an array of 2 Points (Point[]) representing w,h offsets (size) and x,y offsets (position)<br>
     *            for translucency (alpha channel) data is a Float (alpha channel offset)
     *@param pID ID of portal to be animated
     *@param paa action to perform after animation ends
     */
    public void createPortalAnimation(long duration, short type, Object data, Integer pID, PostAnimationAction paa) {
	Portal p = vsm.getPortal(pID);
	switch(type){
	case PT_TRANS_LIN:{//translation - linear
	    if (animatedPortals.containsKey(pID) && (((int[])animatedPortals.get(pID))[0]==1)){
		putAsPendingPAnimation(pID,PT_TRANS,duration,type,data, paa);
	    }
	    else {
		if (animatedPortals.containsKey(pID)){((int[])animatedPortals.get(pID))[0]=1;}
		else {int[] tmpA={1,0,0};animatedPortals.put(pID,tmpA);}
		float nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    PTranslation an=new PTranslation(p,this,duration);
		    an.setPostAnimationAction(paa);
		    int x = p.x;
		    int y = p.y;
		    int tx=((Point)data).x;    //data is a Point representing the x and y offsets
		    int ty=((Point)data).y;
		    float dx=tx/nbSteps;
		    float dy=ty/nbSteps;
		    an.steps=new Point[(int)nbSteps];
		    for (int i=0;i<nbSteps-1;) {
			an.steps[i]=new Point(Math.round(x+i*dx), Math.round(y+i*dy));
			i++;
		    }
		    an.steps[(int)nbSteps-1]=new Point(x+tx,y+ty);  //last point is assigned from source value in order to prevent precision error
		    animPortalBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case PT_TRANS_SIG:{//translation - sigmoid
	    if (animatedPortals.containsKey(pID) && (((int[])animatedPortals.get(pID))[0]==1)){
		putAsPendingPAnimation(pID,PT_TRANS,duration,type,data, paa);
	    }
	    else {
		if (animatedPortals.containsKey(pID)){((int[])animatedPortals.get(pID))[0]=1;}
		else {int [] tmpA={1,0,0};animatedPortals.put(pID,tmpA);}
		float nbSteps=Math.round((duration/(float)frameTime));     //number of steps
		if (nbSteps>0){
		    PTranslation an=new PTranslation(p,this,duration);
		    an.setPostAnimationAction(paa);
		    int x = p.x;
		    int y = p.y;
		    int tx=((Point)data).x;    //data is a Point representing the x and y offsets
		    int ty=((Point)data).y;
		    an.steps=new Point[(int)nbSteps];
		    float stepValue;
		    int dx,dy;
		    for (int i=0;i<nbSteps-1;) {
			stepValue=(float)computeSigmoid(sigFactor,(i+1)/nbSteps);
			dx=Math.round(tx*stepValue);
			dy=Math.round(ty*stepValue);
			an.steps[i++]=new Point(x+dx,y+dy);
		    }
		    an.steps[(int)nbSteps-1]=new Point(x+tx,y+ty);   //last point is assigned from source value in order to prevent precision error
		    animPortalBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case PT_SZ_LIN:{//size - linear
	    if (animatedPortals.containsKey(pID) && (((int[])animatedPortals.get(pID))[1]==1)){
		putAsPendingPAnimation(pID,PT_SZ,duration,type,data, paa);
	    }
	    else {
		if (animatedPortals.containsKey(pID)){((int[])animatedPortals.get(pID))[1]=1;}
		else {int[] tmpA={0,1,0};animatedPortals.put(pID,tmpA);}
		float nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    PResize an=new PResize(p,this,duration);
		    an.setPostAnimationAction(paa);
		    int w = p.w;
		    int h = p.h;
		    int tw=((Point)data).x;    //data is a Point representing the w and h offsets
		    int th=((Point)data).y;
		    float dw=tw/nbSteps;
		    float dh=th/nbSteps;
		    an.steps=new Point[(int)nbSteps];
		    for (int i=0;i<nbSteps-1;) {
			an.steps[i]=new Point(Math.round(w+i*dw), Math.round(h+i*dh));
			i++;
		    }
		    an.steps[(int)nbSteps-1]=new Point(w+tw,h+th);  //last point is assigned from source value in order to prevent precision error
		    animPortalBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case PT_SZ_SIG:{//size - sigmoid
	    if (animatedPortals.containsKey(pID) && (((int[])animatedPortals.get(pID))[1]==1)){
		putAsPendingPAnimation(pID,PT_SZ,duration,type,data, paa);
	    }
	    else {
		if (animatedPortals.containsKey(pID)){((int[])animatedPortals.get(pID))[1]=1;}
		else {int [] tmpA={0,1,0};animatedPortals.put(pID,tmpA);}
		float nbSteps=Math.round((duration/(float)frameTime));     //number of steps
		if (nbSteps>0){
		    PResize an=new PResize(p,this,duration);
		    an.setPostAnimationAction(paa);
		    int w = p.w;
		    int h = p.h;
		    int tw=((Point)data).x;    //data is a Point representing the w and h offsets
		    int th=((Point)data).y;
		    an.steps=new Point[(int)nbSteps];
		    float stepValue;
		    int dw,dh;
		    for (int i=0;i<nbSteps-1;) {
			stepValue=(float)computeSigmoid(sigFactor,(i+1)/nbSteps);
			dw=Math.round(tw*stepValue);
			dh=Math.round(th*stepValue);
			an.steps[i++]=new Point(w+dw,h+dh);
		    }
		    an.steps[(int)nbSteps-1]=new Point(w+tw,h+th);   //last point is assigned from source value in order to prevent precision error
		    animPortalBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case PT_SZ_TRANS_LIN:{//size and translation - linear
	    if (animatedPortals.containsKey(pID) && (((int[])animatedPortals.get(pID))[0]==1 || ((int[])animatedPortals.get(pID))[1]==1)){
		putAsPendingPAnimation(pID,PT_BOTH,duration,type,data, paa);
	    }
	    else {
		if (animatedPortals.containsKey(pID)){((int[])animatedPortals.get(pID))[0]=1;((int[])animatedPortals.get(pID))[1]=1;}
		else {int[] tmpA={1,1,0};animatedPortals.put(pID,tmpA);}
		float nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    PTransResize an=new PTransResize(p,this,duration);
		    an.setPostAnimationAction(paa);
		    int x = p.x;
		    int y = p.y;
		    int tx=((Point[])data)[1].x;    //data is a Point representing the x and y offsets
		    int ty=((Point[])data)[1].y;
		    float dx=tx/nbSteps;
		    float dy=ty/nbSteps;
		    int w = p.w;
		    int h = p.h;
		    int tw=((Point[])data)[0].x;    //data is a Point representing the w and h offsets
		    int th=((Point[])data)[0].y;
		    float dw=tw/nbSteps;
		    float dh=th/nbSteps;
		    an.tsteps=new Point[(int)nbSteps];
		    an.ssteps=new Point[(int)nbSteps];
		    for (int i=0;i<nbSteps-1;i++) {
			an.tsteps[i]=new Point(Math.round(x+i*dx), Math.round(y+i*dy));
			an.ssteps[i]=new Point(Math.round(w+i*dw), Math.round(h+i*dh));
		    }
		    an.tsteps[(int)nbSteps-1]=new Point(x+tx,y+ty);  //last point is assigned from source value in order to prevent precision error
		    an.ssteps[(int)nbSteps-1]=new Point(w+tw,h+th);
		    animPortalBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case PT_SZ_TRANS_SIG:{//size and translation - sigmoid
	    if (animatedPortals.containsKey(pID) && (((int[])animatedPortals.get(pID))[0]==1 || ((int[])animatedPortals.get(pID))[1]==1)){
		putAsPendingPAnimation(pID,PT_BOTH,duration,type,data, paa);
	    }
	    else {
		if (animatedPortals.containsKey(pID)){((int[])animatedPortals.get(pID))[0]=1;((int[])animatedPortals.get(pID))[1]=1;}
		else {int[] tmpA={1,1,0};animatedPortals.put(pID,tmpA);}
		float nbSteps=Math.round((duration/(float)frameTime));     //number of steps
		if (nbSteps>0){
		    PTransResize an=new PTransResize(p,this,duration);
		    an.setPostAnimationAction(paa);
		    int x = p.x;
		    int y = p.y;
		    int tx=((Point[])data)[1].x;    //data is a Point[] representing the w,h and x,y offsets
		    int ty=((Point[])data)[1].y;
		    int w = p.w;
		    int h = p.h;
		    int tw=((Point[])data)[0].x;
		    int th=((Point[])data)[0].y;
		    an.tsteps=new Point[(int)nbSteps];
		    an.ssteps=new Point[(int)nbSteps];
		    float stepValue;
		    int dx,dy;
		    int dw,dh;
		    for (int i=0;i<nbSteps-1;i++) {
			stepValue=(float)computeSigmoid(sigFactor,(i+1)/nbSteps);
			dx=Math.round(tx*stepValue);
			dy=Math.round(ty*stepValue);
			dw=Math.round(tw*stepValue);
			dh=Math.round(th*stepValue);
			an.tsteps[i]=new Point(x+dx,y+dy);
			an.ssteps[i]=new Point(w+dw,h+dh);
		    }
		    an.tsteps[(int)nbSteps-1]=new Point(x+tx,y+ty);   //last point is assigned from source value in order to prevent precision error
		    an.ssteps[(int)nbSteps-1]=new Point(w+tw,h+th);
		    animPortalBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	case PT_ALPHA_LIN:{//translucency - linear
	    if (animatedPortals.containsKey(pID) && ((int[])animatedPortals.get(pID))[2]==1){
		putAsPendingPAnimation(pID,PT_ALPHA,duration,type,data, paa);
	    }
	    else {
		if (animatedPortals.containsKey(pID)){((int[])animatedPortals.get(pID))[2]=1;}
		else {int[] tmpA={0,0,1};animatedPortals.put(pID,tmpA);}
		float nbSteps=Math.round((float)(duration/frameTime));     //number of steps
		if (nbSteps>0){
		    PTranslucency an=new PTranslucency(p,this,duration);
		    an.setPostAnimationAction(paa);
		    float tav = ((Float)data).floatValue();
		    float sav = ((Translucent)p).getTranslucencyValue();
		    double dt = tav / nbSteps;
		    an.steps=new float[(int)nbSteps];
		    for (int i=0;i<nbSteps-1;i++){
			an.steps[i] = sav + i * (float)dt;
		    }
		    //last point is assigned from source value in order to prevent precision error
		    an.steps[(int)nbSteps-1] = sav + tav;
		    animPortalBag.add(an);
		    an.start();
		}
	    }
	    break;
	}
	default:{
	    System.err.println("Error : AnimManager.createPortalAnimation : unknown animation type");
	}
	}
    }

    void putAsPendingPAnimation(Integer pID,String dim,long duration,short type,Object data, PostAnimationAction paa){
	Vector pa;
	//look for other pending animations for this portal and add this one to the end (FIFO)
	if (pendingPAnims.containsKey(pID)){
	    Hashtable animByDim=(Hashtable)pendingPAnims.get(pID);
	    if (animByDim.containsKey(dim)){pa=(Vector)animByDim.get(dim);}
	    else {pa=new Vector();}
	    pa.add(new AnimParams(duration,type,data, paa));
	    animByDim.put(dim,pa);
	}
	else {
	    pa=new Vector();
	    pa.add(new AnimParams(duration,type,data, paa));
	    Hashtable animByDim=new Hashtable();
	    animByDim.put(dim,pa);
	    pendingPAnims.put(pID,animByDim);
	}	
    }

    /**FOR INTERNAL USE ONLY*/
    public void killPAnim(PAnimation pan,String dim){
	Integer pID=pan.target.getID();   //get the portal ID
	animPortalBag.remove(pan);      //remove animation from bag of anims to be executed (it's over, kill it)
	if (animatedPortals.containsKey(pID)){//remove portal from list of portals being animated
	    int[] animDims=(int[])animatedPortals.get(pID);
	    if (dim.equals(PT_TRANS)){animDims[0]=0;}
	    else if (dim.equals(PT_SZ)){animDims[1]=0;}
	    else if (dim.equals(PT_BOTH)){animDims[0]=0;animDims[1]=0;}
	    else if (dim.equals(PT_ALPHA)){animDims[2]=0;}
	    if (allValuesEqualZero(animDims)){animatedPortals.remove(pID);}
	}
	pan.postAnimAction();
	if (pendingPAnims.containsKey(pID)) {  //look for first anim standing by whose target is this portal	    
	    Hashtable pendingDims=(Hashtable)pendingPAnims.get(pID);
	    if (pendingDims.containsKey(dim)){
		Vector pa=(Vector)pendingDims.get(dim);
		AnimParams ap=(AnimParams)pa.elementAt(0);  //get its params
		pa.removeElementAt(0);  //remove the animation we're about to execute
		if (pa.isEmpty()) { //if there is no pending anim left, delete entry for this glyph
		    pendingDims.remove(dim);
		    if (pendingDims.isEmpty()){pendingPAnims.remove(pID);}
		}
		this.createPortalAnimation(ap.duration,ap.type,ap.data,pID, ap.paa);    //create the appropriate animation
	    }
	}
    }

    /**
     * Interrupt a portal animation being executed
     *@param p portal being animated
     *@param dim dimension animated (use one of PT_TRANS, PT_SZ, PT_BOTH)
     *@param all also kill all animations waiting in the queue for this dimension (for this portal) - has no effect if there is no animation waiting in the queue
     *@param finish true=put the portal in its final state (i.e. the state in which it would be if the animation had not been interrupted) ; false=leave it in the current state (at the time when the animation is interrupted)
     */
    public void interruptPortalAnimation(Portal p,String dim,boolean all,boolean finish){
	PAnimation an=null;
	PAnimation tmpAn;
	for (int i=0;i<animPortalBag.size();i++){
	    tmpAn=(PAnimation)animPortalBag.elementAt(i);
	    if (tmpAn.target==p && tmpAn.type==dim){
		an=tmpAn;
		break;
	    }
	}
	if (an!=null){
	    if (all){//interrupt this animation and cancel pending animations for this attribute
		if (finish){//finish animation before killing it (assign final step to glyph)
		    an.conclude();
		}
		//else leave glyph in its current state
		if (pendingPAnims.containsKey(p.getID())){//cancel pending animations
		    Hashtable pendingDims=(Hashtable)pendingPAnims.get(p.getID());
		    if (pendingDims.containsKey(dim)){
			pendingDims.remove(dim);
			if (pendingDims.isEmpty()){pendingPAnims.remove(p.getID());}
		    }
		}
	    }
	    else {//interrupt this animation only, and take next animation
		if (finish){//finish animation before killing it (assign final step to glyph)
		    an.conclude();
		}
		//else leave portal in its current state (nothing specific to do, just kill)
	    }
	    killPAnim(an,dim);
	}
    }

    /* ------------------ Misc. Functions ---------------- */

    /**
     *@param n determines the steepness of the function
     *@param x should be between 0 and 1 for our purpose
     */
    static double computeSigmoid(int n,double x){
	return (Math.atan(n*(2*x-1))/Math.atan(n)+1)/(2.0f);	
    }

    //returns true if all values inside an array of int are equal to zero
    boolean allValuesEqualZero(int[] ar){
	for (int i=0;i<ar.length;i++){if (ar[i]!=0){return false;}}
	return true;
    }

    //add nb elements to an array of int, value of new elems=0
    int[] addElementsToIntArray(int[] ar,int nb){
	int[] res=new int[ar.length+nb];
	for (int i=0;i<ar.length;i++){
	    res[i]=ar[i];
	}
	for (int i=ar.length;i<res.length;i++){
	    res[i]=0;
	}
	return res;
    }

}


