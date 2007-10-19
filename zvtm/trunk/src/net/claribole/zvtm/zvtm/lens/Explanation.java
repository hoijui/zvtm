/*   FILE: Explanation.java
 *   DATE OF CREATION:  Sat Apr  1 18:48:03 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.lens;

import java.awt.Color;

import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;

public class Explanation {

    VirtualSpaceManager vsm;
    View mView;
    static final String mViewName = "mainView";
    static final int VIEW_WIDTH = 800;
    static final int VIEW_HEIGHT = 600;
    ExplanationEventHandler eeh;
    VirtualSpace mSpace;
    static final String mSpaceName = "mainSpace";
    Camera mCamera;

    VSegment lseg, cseg, rseg, caseg1, caseg2, caseg3, caseg4;
    VCbCurve lcur, rcur;
    static final Color SEG_COLOR = Color.black;
    static final Color CUR_COLOR = Color.black;
    static final Color CAM1_COLOR = Color.red;
    static final Color CAM2_COLOR = Color.blue;

    public Explanation(){
	vsm = new VirtualSpaceManager();
	init();
	initGlyphs();
    }

    void init(){
	mSpace = vsm.addVirtualSpace(mSpaceName);
	mCamera = vsm.addCamera(mSpaceName);
	Vector v = new Vector();
	v.add(mCamera);
	mView = vsm.addExternalView(v, mViewName, View.STD_VIEW, VIEW_WIDTH, VIEW_HEIGHT, false, true);
	eeh = new ExplanationEventHandler(this);
	mView.setEventHandler(eeh);
    }

    void initGlyphs(){
	caseg1 = new VSegment(0, 400, 0, CAM1_COLOR, -800, -400);
	caseg2 = new VSegment(0, 400, 0, CAM1_COLOR, 800, -400);
	vsm.addGlyph(caseg1, mSpace);
	vsm.addGlyph(caseg2, mSpace);
	mCamera.stick(caseg1);
	mCamera.stick(caseg2);
	caseg3 = new VSegment(0, 400, 0, CAM2_COLOR, 200, -400);
	caseg4 = new VSegment(0, 400, 0, CAM2_COLOR, 300, -400);
	vsm.addGlyph(caseg3, mSpace);
	vsm.addGlyph(caseg4, mSpace);
	mCamera.stick(caseg3);
	mCamera.stick(caseg4);
	lseg = new VSegment(-5000, 0, 0, SEG_COLOR, -150, 0);
	vsm.addGlyph(lseg, mSpace);
	cseg = new VSegment(-50, 0, 0, SEG_COLOR, 50, 0);
	vsm.addGlyph(cseg, mSpace);
	rseg = new VSegment(150, 0, 0, SEG_COLOR, 5000, 0);
	vsm.addGlyph(rseg, mSpace);
	lcur = new VCbCurve(-100, 0, 0, 50, CUR_COLOR, 0, 0, (float) (Math.PI/2.0f+Math.PI/4.0f), 0, (float) (3.0f*Math.PI/2.0f+Math.PI/4.0f));
	vsm.addGlyph(lcur, mSpace);
	rcur = new VCbCurve(100, 0, 0, 50, CUR_COLOR, 0, 0, (float) -(Math.PI/2.0f+Math.PI/4.0f), 0, (float) -(3.0f*Math.PI/2.0f+Math.PI/4.0f));
	vsm.addGlyph(rcur, mSpace);
    }

    void centerCamera(){
	mCamera.moveTo(0, mCamera.posy);
    }

    void moveLeft(){
	mCamera.move(20, 0);
    }

    void moveRight(){
	mCamera.move(-20, 0);	
    }

    boolean zooming = false;
    int ANIM_LENGTH = 500;

    void zoomin(){
	if (zooming){
	    zip2();
	}
	else {
	    zip1();
	}
	zooming = !zooming;
    }

    void zoomout(){
	if (zooming){
	    zop2();
	}
	else {
	    zop1();
	}
	zooming = !zooming;
    }

    void zip1(){
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, 100), cseg.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, 50), lcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_ROT_LIN,
					  new Float(-Math.PI/4.0f), lcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_SZ_LIN,
					  new Float(Math.sqrt(2)), lcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, 50), rcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_ROT_LIN,
					  new Float(Math.PI/4.0f), rcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_SZ_LIN,
					  new Float(Math.sqrt(2)), rcur.getID());
	curve();
    }

    void zip2(){
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, 100), lseg.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, 100), rseg.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, 50), lcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_ROT_LIN,
					  new Float(Math.PI/4.0f), lcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_SZ_LIN,
					  new Float(1/Math.sqrt(2)), lcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, 50), rcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_ROT_LIN,
					  new Float(-Math.PI/4.0f), rcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_SZ_LIN,
					  new Float(1/Math.sqrt(2)), rcur.getID());
	uncurve();
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, 100), caseg1.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, 100), caseg2.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, 100), caseg3.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, 100), caseg4.getID());
    }

    void zop1(){
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, -100), lseg.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, -100), rseg.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, -50), lcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_ROT_LIN,
					  new Float(-Math.PI/4.0f), lcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_SZ_LIN,
					  new Float(Math.sqrt(2)), lcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, -50), rcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_ROT_LIN,
					  new Float(Math.PI/4.0f), rcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_SZ_LIN,
					  new Float(Math.sqrt(2)), rcur.getID());
	curve();
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, -100), caseg1.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, -100), caseg2.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, -100), caseg3.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, -100), caseg4.getID());
    }

    void zop2(){
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, -100), cseg.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, -50), lcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_ROT_LIN,
					  new Float(Math.PI/4.0f), lcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_SZ_LIN,
					  new Float(1/Math.sqrt(2)), lcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_TRANS_LIN,
					  new LongPoint(0, -50), rcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_ROT_LIN,
					  new Float(-Math.PI/4.0f), rcur.getID());
	vsm.animator.createGlyphAnimation(ANIM_LENGTH, 0, AnimManager.GL_SZ_LIN,
					  new Float(1/Math.sqrt(2)), rcur.getID());
	uncurve();
    }

    void curve(){
	Vector vl = new Vector();
	vl.add(new PolarCoords(50, 0));
	vl.add(new PolarCoords(50, 0));
	vsm.animator.createCbCurveCtrlPtAnimation(ANIM_LENGTH, AnimManager.GL_CP_TRANS_LIN,
						  vl, lcur.getID(), null);
	Vector vr = new Vector();
	vr.add(new PolarCoords(50, 0));
	vr.add(new PolarCoords(50, 0));
	vsm.animator.createCbCurveCtrlPtAnimation(ANIM_LENGTH, AnimManager.GL_CP_TRANS_LIN,
						  vr, rcur.getID(), null);
    }

    void uncurve(){
	Vector vl = new Vector();
	vl.add(new PolarCoords(-50, 0));
	vl.add(new PolarCoords(-50, 0));
	vsm.animator.createCbCurveCtrlPtAnimation(ANIM_LENGTH, AnimManager.GL_CP_TRANS_LIN,
						  vl, lcur.getID(), null);
	Vector vr = new Vector();
	vr.add(new PolarCoords(-50, 0));
	vr.add(new PolarCoords(-50, 0));
	vsm.animator.createCbCurveCtrlPtAnimation(ANIM_LENGTH, AnimManager.GL_CP_TRANS_LIN,
						  vr, rcur.getID(), null);
    }

    public static void main(String[] args){
	new Explanation();
    }

}