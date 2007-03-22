/*   FILE: Introduction.java
 *   DATE OF CREATION:   Dec 07 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2007. All Rights Reserved
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
 * $Id: Introduction.java,v 1.17 2006/05/29 08:52:08 epietrig Exp $
 */

package com.xerox.VTM.demo;


import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.util.Vector;

import javax.swing.ImageIcon;

import net.claribole.zvtm.glyphs.CGlyph;
import net.claribole.zvtm.glyphs.SGlyph;

import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.glyphs.BooleanOps;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VBoolShape;
import com.xerox.VTM.glyphs.VCbCurve;
import com.xerox.VTM.glyphs.VCircle;
import com.xerox.VTM.glyphs.VDiamond;
import com.xerox.VTM.glyphs.VDiamondOr;
import com.xerox.VTM.glyphs.VDiamondST;
import com.xerox.VTM.glyphs.VImage;
import com.xerox.VTM.glyphs.VImageOr;
import com.xerox.VTM.glyphs.VOctagon;
import com.xerox.VTM.glyphs.VOctagonOr;
import com.xerox.VTM.glyphs.VOctagonOrST;
import com.xerox.VTM.glyphs.VOctagonST;
import com.xerox.VTM.glyphs.VQdCurve;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.VRectangleOr;
import com.xerox.VTM.glyphs.VRectangleOrST;
import com.xerox.VTM.glyphs.VRectangleST;
import com.xerox.VTM.glyphs.VSegment;
import com.xerox.VTM.glyphs.VShape;
import com.xerox.VTM.glyphs.VShapeST;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VTriangle;
import com.xerox.VTM.glyphs.VTriangleOr;
import com.xerox.VTM.glyphs.VTriangleOrST;
import com.xerox.VTM.glyphs.VTriangleST;

import net.claribole.zvtm.engine.ViewEventHandler;
import net.claribole.zvtm.engine.TransitionManager;
import net.claribole.zvtm.engine.Location;

public class Introduction {

    static int PREFERRED_VIEW_WIDTH = 800;
    static int PREFERRED_VIEW_HEIGHT = 600;

    int viewWidth = PREFERRED_VIEW_WIDTH;
    int viewHeight = PREFERRED_VIEW_HEIGHT;

    static final Color BLANK_COLOR = Color.BLACK;
    static final Color MULTI_LAYER_BKG_COLOR = Color.WHITE;
    static final Color ANIM_BKG_COLOR = new Color(255,242,147);
    static final Color ANIM_BUTTON_COLOR = new Color(234,221,158);
    static final Color ANIM_SELECTED_BUTTON_COLOR = new Color(255,153,62);
    static final Color ANIM_OBJECT_COLOR = new Color(255,153,62);
    VirtualSpaceManager vsm;

    IntroPanel iPanel;

    ViewEventHandler eh;

    String animType = "orient";
    String animScheme = "sig";

    int camNb=0;

    boolean autoZoomEnabled = false;

    Introduction(){
	init();
    }

    public void init(){
	vsm=new VirtualSpaceManager(false);
	vsm.setMainFont(new Font("dialog", 0, 40));
	vsm.addVirtualSpace("vs1");
	vsm.addCamera("vs1");
	Vector vc1=new Vector();vc1.add(vsm.getVirtualSpace("vs1").getCamera(0));
	Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
	if (screenDimension.width < IntroPanel.PANEL_WIDTH + PREFERRED_VIEW_WIDTH){
	    viewWidth = screenDimension.width - IntroPanel.PANEL_WIDTH;
	}
	if (screenDimension.height < PREFERRED_VIEW_HEIGHT){
	    viewHeight = screenDimension.height;
	}
	vsm.addExternalView(vc1, "Demo", View.STD_VIEW, viewWidth, viewHeight, false, true).setBackgroundColor(Color.BLACK);
	vsm.getView("Demo").setLocation(IntroPanel.PANEL_WIDTH, 0);
	iPanel=new IntroPanel(this);
	//setAutoZoomEnabled(true);
    }

    void reveal(boolean center){
	if (center){
	    Camera c = vsm.getView("Demo").getCameraNumber(0);
	    Location l = vsm.getGlobalView(c);
	    c.posx = l.vx;
	    c.posy = l.vy;
	    c.updatePrecisePosition();
	    c.setAltitude(l.alt-c.getFocal());
	}
	TransitionManager.fadeIn(vsm.getView("Demo"), 500, vsm);
    }

    void cameraDemo(){
	if (vsm.getView("Demo2")!=null){vsm.getView("Demo2").destroyView();}
	TransitionManager.fadeOut(vsm.getView("Demo"), 500, BLANK_COLOR, vsm,
				  new FadeOut(vsm.getView("Demo"), BLANK_COLOR, vsm.getVirtualSpace("vs1")){
				      public void animationEnded(Object target, short type, String dimension){
					  super.animationEnded(target, type, dimension);
					  cameraDemoActions();
				      }
				  });
    }

    void cameraDemoActions(){
	vsm.getView("Demo").setBlank(BLANK_COLOR);
	vsm.getView("Demo").setBackgroundColor(Color.WHITE);
	vsm.destroyGlyphsInSpace("vs1");
	eh=new CameraDemoEvtHdlr(this);
	vsm.getView("Demo").setEventHandler(eh);
	float h=0.8f;float s=1.0f;float v=1.0f;
	long randomX=0;
	long randomY=0;
	long randomS=0;
	float randomO=0;
	float randomSat=0;
	double shapeType=0;
	Glyph g;
	float[] vertices={1.0f,0.8f,1.0f,0.8f,1.0f,0.8f,1.0f,0.8f,1.0f,0.8f,1.0f,0.8f,1.0f,0.8f,1.0f,0.8f};
	for (int i=0;i<400;i++){
	    randomX=Math.round(Math.random()*6000);
	    randomY=Math.round(Math.random()*6000);
	    randomS=Math.round(Math.random()*199)+20;
	    randomO=(float)(Math.random()*2*Math.PI);
	    randomSat=(float)Math.random();
	    shapeType=Math.random();
	    if (shapeType<0.2){
		g=new VTriangleOr(randomX,randomY,0,randomS,Color.getHSBColor(0.66f,randomSat,0.8f),randomO);
	    }
	    else if (shapeType<0.4){
		g=new VDiamondOr(randomX,randomY,0,randomS,Color.getHSBColor(0.66f,randomSat,0.8f),randomO);
	    }
	    else if (shapeType<0.6){
		g=new VOctagonOr(randomX,randomY,0,randomS,Color.getHSBColor(0.66f,randomSat,0.8f), Color.BLACK,randomO);
	    }
	    else if (shapeType<0.8){
		g=new VRectangleOr(randomX,randomY,0,randomS,randomS,Color.getHSBColor(0.66f,randomSat,0.8f),randomO);
	    }
	    else {
		g=new VShape(randomX,randomY,0,randomS,vertices,Color.getHSBColor(0.66f,randomSat,0.8f),randomO);
	    }
	    vsm.addGlyph(g,"vs1");
	}
	reveal(true);
    }

    void objectFamilies(){
	TransitionManager.fadeOut(vsm.getView("Demo"), 500, BLANK_COLOR, vsm,
				  new FadeOut(vsm.getView("Demo"), BLANK_COLOR, vsm.getVirtualSpace("vs1")){
				      public void animationEnded(Object target, short type, String dimension){
					  super.animationEnded(target, type, dimension);
					  objectFamiliesActions();
				      }
				  });
    }

    void objectFamiliesActions(){
	vsm.getView("Demo").setBlank(BLANK_COLOR);
	vsm.getView("Demo").setBackgroundColor(Color.LIGHT_GRAY);
	vsm.destroyGlyphsInSpace("vs1");
	eh=new CameraDemoEvtHdlr(this);
	vsm.getView("Demo").setEventHandler(eh);
	VRectangle r1=new VRectangle(-600,400,0,100,50,Color.black);
	VRectangle r2=new VRectangle(-200,400,0,50,50,Color.black);
	VRectangleOr r3=new VRectangleOr(200,400,0,30,100,Color.black,0.707f);
	VRectangleST r4=new VRectangleST(600,400,0,100,75,Color.black);
	r2.setDashed(true);r3.setFilled(false);
	vsm.addGlyph(r1,"vs1");vsm.addGlyph(r2,"vs1");vsm.addGlyph(r3,"vs1");vsm.addGlyph(r4,"vs1");
	r1.setHSVColor(0.5f,0.9f,0.6f);r2.setHSVColor(0.5f,0.9f,0.6f);r3.setHSVColor(0.5f,0.9f,0.6f);r4.setHSVColor(0.5f,0.9f,0.6f);
	VTriangleST t1=new VTriangleST(-600,200,0,50,Color.black);
	VTriangle t2=new VTriangle(-200,200,0,50,Color.black);
	VTriangle t3=new VTriangle(200,200,0,50,Color.black);
	VTriangleOrST t4=new VTriangleOrST(600,200,0,75,Color.black, Color.BLACK, 0.5f, 0.707f);
	t1.setDashed(true);t3.setFilled(false);t3.setDashed(true);
	vsm.addGlyph(t1,"vs1");vsm.addGlyph(t2,"vs1");vsm.addGlyph(t3,"vs1");vsm.addGlyph(t4,"vs1");
	t1.setHSVColor(0.66f,0.5f,0.5f);t2.setHSVColor(0.66f,0.5f,0.5f);t3.setHSVColor(0.66f,0.5f,0.5f);t4.setHSVColor(0.66f,0.5f,0.5f);
	VDiamondST d1=new VDiamondST(-600,0,0,50,Color.black, Color.BLACK, 0.5f);
	VDiamond d2=new VDiamond(-200,0,0,45,Color.black);
	VOctagon o3=new VOctagon(200,0,0,50,Color.black);
	VOctagonOrST o4=new VOctagonOrST(600,0,0,75,Color.black,Color.BLACK, 0.5f,0.5f);
	d1.setDashed(true);d2.setFilled(false);o3.setDashed(true);
	vsm.addGlyph(d1,"vs1");vsm.addGlyph(d2,"vs1");vsm.addGlyph(o3,"vs1");vsm.addGlyph(o4,"vs1");
	d1.setHSVColor(0.0f,0.8f,0.8f);d2.setHSVColor(0.0f,0.8f,0.8f);o3.setHSVColor(0.0f,0.8f,0.8f);o4.setHSVColor(0.0f,0.8f,0.8f);
	VCircle x1=new VCircle(-600,-200,0,50,Color.black);
	VSegment x2=new VSegment(-200,-200,0,50,100,Color.black);
	BooleanOps[] barray={new BooleanOps(0,-20,20,40,1,2),new BooleanOps(0,25,20,40,2,1)};
	VBoolShape x3=new VBoolShape(200,-200,0,100,50,2,barray,Color.black);
	VText x4=new VText(600,-200,0,Color.black,"text object", VText.TEXT_ANCHOR_MIDDLE);
	x2.setDashed(true);
	vsm.addGlyph(x1,"vs1");vsm.addGlyph(x2,"vs1");vsm.addGlyph(x3,"vs1");vsm.addGlyph(x4,"vs1");
	x1.setHSVColor(0.2f,0.55f,0.95f);x2.setHSVColor(0.2f,0.55f,0.95f);x3.setHSVColor(0.2f,0.55f,0.95f);x4.setHSVColor(0.2f,0.55f,0.95f);
	VImage i1=new VImage(0,-400,0,(new ImageIcon(this.getClass().getResource("/images/logo-futurs-small.png"))).getImage());i1.setDrawBorderPolicy(VImage.DRAW_BORDER_MOUSE_INSIDE);
	vsm.addGlyph(i1,"vs1");
	float[] vs={0.1f,0.5f,0.3f,0.5f,1.0f,0.5f,1.0f,0.5f};
	VShapeST s1=new VShapeST(-600,-400,0,100,vs,Color.gray,0);vsm.addGlyph(s1,"vs1");
	float[] vs2={1.0f,0.8f,1.0f,0.8f,1.0f,0.8f,1.0f,0.8f,1.0f,0.8f,1.0f,0.8f,1.0f,0.8f,1.0f,0.8f};
	VShape s2=new VShape(600,-400,0,100,vs2,Color.gray,0);vsm.addGlyph(s2,"vs1");
	VQdCurve qd1=new VQdCurve(-600,-600,0,100,Color.black,0,50,(float)Math.PI/2);
	vsm.addGlyph(qd1,"vs1");
	VQdCurve qd2=new VQdCurve(0,-600,0,100,Color.black,0.707f,100,(float)Math.PI/3);
	vsm.addGlyph(qd2,"vs1");
	VCbCurve cb1=new VCbCurve(600,-600,0,100,Color.black,0,50,(float)Math.PI/2,100,(float)-Math.PI/2);
	vsm.addGlyph(cb1,"vs1");
	qd2.setHSVColor(0.0f,0.8f,0.8f);
	cb1.setHSVColor(0.66f,0.5f,0.5f);
	//will be the primary glyph of a CGlyph
	VRectangleOr cg1=new VRectangleOr(0,-900,0,400,100,Color.white,0);
	//and 4 secondary glyphs (init coordinates of secondary glyphs do not matter as they will be changed to match the position offset defined in the associated SGlyph)
	VTriangleOr cg2=new VTriangleOr(0,-800,0,50,Color.red,(float)(-Math.PI/4.0f));
	VTriangleOr cg3=new VTriangleOr(0,-800,0,50,Color.red,(float)(Math.PI/4.0f));
	VTriangleOr cg4=new VTriangleOr(0,-800,0,50,Color.red,(float)(-5*Math.PI/4.0f));
	VTriangleOr cg5=new VTriangleOr(0,-800,0,50,Color.red,(float)(-3*Math.PI/4.0f));
	vsm.addGlyph(cg1,"vs1");vsm.addGlyph(cg2,"vs1");vsm.addGlyph(cg3,"vs1");vsm.addGlyph(cg4,"vs1");vsm.addGlyph(cg5,"vs1");
	cg1.setHSVColor(0.66f,0.5f,0.5f);
	SGlyph[] sgs={
	    new SGlyph(cg2,400,100,SGlyph.FULL_ROTATION,SGlyph.RESIZE),
	    new SGlyph(cg3,-400,100,SGlyph.FULL_ROTATION,SGlyph.RESIZE),
	    new SGlyph(cg4,-400,-100,SGlyph.FULL_ROTATION,SGlyph.RESIZE),
	    new SGlyph(cg5,400,-100,SGlyph.FULL_ROTATION,SGlyph.RESIZE)
	};
	CGlyph cg=new CGlyph(cg1,sgs);
	vsm.addCGlyph(cg,"vs1");  //use addCGlyph, not addGlyph
	reveal(true);
    }

    void objectAnim(){
	TransitionManager.fadeOut(vsm.getView("Demo"), 500, BLANK_COLOR, vsm,
				  new FadeOut(vsm.getView("Demo"), BLANK_COLOR, vsm.getVirtualSpace("vs1")){
				      public void animationEnded(Object target, short type, String dimension){
					  super.animationEnded(target, type, dimension);
					  objectAnimActions();
				      }
				  });
    }

    void objectAnimActions(){
	vsm.getView("Demo").setBlank(BLANK_COLOR);
	vsm.getView("Demo").setBackgroundColor(ANIM_BKG_COLOR);
	vsm.destroyGlyphsInSpace("vs1");
	vsm.destroyVirtualSpace("vs2");
	VRectangle orG=new VRectangle(400,300,0,25,25,Color.black);
	orG.setType("orient");
	VText orT = new VText(400, 200, 0, Color.black, "Orientation", VText.TEXT_ANCHOR_MIDDLE);
	VRectangle szG=new VRectangle(400,100,0,25,25,Color.black);
	szG.setType("size");
	VText szT = new VText(400, 0, 0, Color.black, "Size", VText.TEXT_ANCHOR_MIDDLE);
	VRectangle clG=new VRectangle(400,-100,0,25,25,Color.black);
	clG.setType("col");
	VText clT = new VText(400, -200, 0, Color.black, "Color", VText.TEXT_ANCHOR_MIDDLE);
	VRectangle trG=new VRectangle(400,-300,0,25,25,Color.black);
	trG.setType("pos");
	VText trT = new VText(400, -400, 0, Color.black, "Translation", VText.TEXT_ANCHOR_MIDDLE);
	vsm.addGlyph(orG,"vs1");vsm.addGlyph(szG,"vs1");vsm.addGlyph(clG,"vs1");vsm.addGlyph(trG,"vs1");
	vsm.addGlyph(orT,"vs1");vsm.addGlyph(szT,"vs1");vsm.addGlyph(clT,"vs1");vsm.addGlyph(trT,"vs1");
	orG.setColor(Introduction.ANIM_SELECTED_BUTTON_COLOR);szG.setColor(Introduction.ANIM_BUTTON_COLOR);clG.setColor(Introduction.ANIM_BUTTON_COLOR);trG.setColor(Introduction.ANIM_BUTTON_COLOR);
	VSegment sep=new VSegment(700,0,0,1,300,Color.black);
	vsm.addGlyph(sep,"vs1");
	VRectangle linG=new VRectangle(1000,200,0,25,25,Color.black);
	linG.setType("lin");
	VText linT = new VText(1000, 100, 0, Color.black, "Linear", VText.TEXT_ANCHOR_MIDDLE);
	VRectangle expG=new VRectangle(1000,0,0,25,25,Color.black);
	expG.setType("exp");
	VText expT = new VText(1000, -100, 0, Color.black, "Slow-in/Fast-out", VText.TEXT_ANCHOR_MIDDLE);
	VRectangle sigG=new VRectangle(1000,-200,0,25,25,Color.black);
	sigG.setType("sig");
	VText sigT = new VText(1000, -300, 0, Color.black, "Slow-in/Slow-out", VText.TEXT_ANCHOR_MIDDLE);
	vsm.addGlyph(linG,"vs1");vsm.addGlyph(expG,"vs1");vsm.addGlyph(sigG,"vs1");
	vsm.addGlyph(linT,"vs1");vsm.addGlyph(expT,"vs1");vsm.addGlyph(sigT,"vs1");
	linG.setColor(Introduction.ANIM_BUTTON_COLOR);expG.setColor(Introduction.ANIM_BUTTON_COLOR);sigG.setColor(Introduction.ANIM_SELECTED_BUTTON_COLOR);
	eh=new AnimationEvtHdlr(this,orG,szG,clG,trG,linG,expG,sigG);
	vsm.getView("Demo").setEventHandler(eh);

	VCircle c1=new VCircle(-400,900,0,100,Color.black);c1.setType("an");	
	VTriangleOr t1=new VTriangleOr(-400,600,0,100,Color.black,0);t1.setType("an");
	VOctagonOr o1=new VOctagonOr(-400,300,0,100,Color.black,Color.BLACK,0);o1.setType("an");
	VRectangleOr r1=new VRectangleOr(-400,0,0,100,50,Color.black,0);r1.setType("an");
	VDiamondOr d1=new VDiamondOr(-400,-300,0,100,Color.black,0);d1.setType("an");
	vsm.addGlyph(c1,"vs1");vsm.addGlyph(t1,"vs1");vsm.addGlyph(o1,"vs1");vsm.addGlyph(r1,"vs1");vsm.addGlyph(d1,"vs1");
	c1.setColor(Introduction.ANIM_OBJECT_COLOR);t1.setColor(Introduction.ANIM_OBJECT_COLOR);o1.setColor(Introduction.ANIM_OBJECT_COLOR);r1.setColor(Introduction.ANIM_OBJECT_COLOR);d1.setColor(Introduction.ANIM_OBJECT_COLOR);
	VImageOr i1=new VImageOr(-400,-600,0,(new ImageIcon(this.getClass().getResource("/images/xrce.gif"))).getImage(),0.0f);i1.setDrawBorderPolicy(VImage.DRAW_BORDER_MOUSE_INSIDE);
	vsm.addGlyph(i1,"vs1");i1.setType("an");
	i1.sizeTo(200);
	float[] vs={1.0f,0.4f,1.0f,0.4f,0.8f,0.5f,0.3f,1.0f};
	VShape s1=new VShape(-400,-900,0,100,vs,ANIM_OBJECT_COLOR,0);vsm.addGlyph(s1,"vs1");s1.setType("an");

	//will be the primary glyph of a CGlyph
	VRectangleOr cg1=new VRectangleOr(-400,-1200,0,200,100,Color.black,0);
	//and 4 secondary glyphs (init coordinates of secondary glyphs do not matter as they will be changed to match the position offset defined in the associated SGlyph)
	VTriangleOr cg2=new VTriangleOr(0,0,0,50,Color.black,0);
	VRectangleOrST cg3=new VRectangleOrST(0,0,0,50,50,Color.black,Color.BLACK, 0.5f,0.404f);
	VTriangleOrST cg4=new VTriangleOrST(0,0,0,50,Color.black,Color.BLACK, 0.5f,0.404f);
	VRectangleOr cg5=new VRectangleOr(0,0,0,50,50,Color.black,0);
	vsm.addGlyph(cg1,"vs1");vsm.addGlyph(cg2,"vs1");vsm.addGlyph(cg3,"vs1");vsm.addGlyph(cg4,"vs1");vsm.addGlyph(cg5,"vs1");
	cg1.setType("an");cg2.setType("an");cg3.setType("an");cg4.setType("an");cg5.setType("an");
	cg1.setColor(Introduction.ANIM_OBJECT_COLOR);
	cg2.setColor(Introduction.ANIM_OBJECT_COLOR);
	cg3.setColor(Introduction.ANIM_BUTTON_COLOR);
	cg4.setColor(Introduction.ANIM_OBJECT_COLOR);
	cg5.setColor(Introduction.ANIM_SELECTED_BUTTON_COLOR);
	SGlyph[] sgs={
	    new SGlyph(cg2,200,0,SGlyph.FULL_ROTATION,SGlyph.RESIZE),
	    new SGlyph(cg3,0,100,SGlyph.FULL_ROTATION,SGlyph.RESIZE),
	    new SGlyph(cg4,-200,0,SGlyph.FULL_ROTATION,SGlyph.RESIZE),
	    new SGlyph(cg5,0,-100,SGlyph.FULL_ROTATION,SGlyph.RESIZE)
	};
	CGlyph cg=new CGlyph(cg1,sgs);
	vsm.addCGlyph(cg,"vs1");  //use addCGlyph, not addGlyph


	//will be the primary glyph of a CGlyph
	cg1=new VRectangleOr(-400,-1600,0,200,100,Color.black,0);
	//and 4 secondary glyphs (init coordinates of secondary glyphs do not matter as they will be changed to match the position offset defined in the associated SGlyph)
	cg2=new VTriangleOr(0,0,0,50,Color.black,0);
	cg3=new VRectangleOrST(0,0,0,50,50,Color.black,Color.BLACK, 0.5f,0.404f);
	cg4=new VTriangleOrST(0,0,0,50,Color.black,Color.BLACK, 0.5f,0.404f);
	cg5=new VRectangleOr(0,0,0,50,50,Color.black,0);
	vsm.addGlyph(cg1,"vs1");vsm.addGlyph(cg2,"vs1");vsm.addGlyph(cg3,"vs1");vsm.addGlyph(cg4,"vs1");vsm.addGlyph(cg5,"vs1");
	cg1.setType("an");cg2.setType("an");cg3.setType("an");cg4.setType("an");cg5.setType("an");
	cg1.setColor(Introduction.ANIM_OBJECT_COLOR);
	cg2.setColor(Introduction.ANIM_OBJECT_COLOR);
	cg3.setColor(Introduction.ANIM_BUTTON_COLOR);
	cg4.setColor(Introduction.ANIM_OBJECT_COLOR);
	cg5.setColor(Introduction.ANIM_SELECTED_BUTTON_COLOR);
	SGlyph[] sgs2={
	    new SGlyph(cg2,200,0,SGlyph.ROTATION_POSITION_ONLY,SGlyph.RESIZE),
	    new SGlyph(cg3,0,100,SGlyph.ROTATION_POSITION_ONLY,SGlyph.RESIZE),
	    new SGlyph(cg4,-200,0,SGlyph.ROTATION_POSITION_ONLY,SGlyph.RESIZE),
	    new SGlyph(cg5,0,-100,SGlyph.ROTATION_POSITION_ONLY,SGlyph.RESIZE)
	};
	cg=new CGlyph(cg1,sgs2);
	vsm.addCGlyph(cg,"vs1");  //use addCGlyph, not addGlyph
	reveal(true);
    }

    void animate(Glyph g){
	if (animType.equals("orient")){
	    if (animScheme.equals("lin")){vsm.animator.createGlyphAnimation(5000,AnimManager.GL_ROT_LIN,new Float(10.0f),g.getID());}
	    else if (animScheme.equals("exp")){vsm.animator.createGlyphAnimation(5000,AnimManager.GL_ROT_PAR,new Float(10.0f),g.getID());}
	    else if (animScheme.equals("sig")){vsm.animator.createGlyphAnimation(5000,AnimManager.GL_ROT_SIG,new Float(10.0f),g.getID());}
	}
	else if (animType.equals("size")){
	    if (animScheme.equals("lin")){vsm.animator.createGlyphAnimation(3000,AnimManager.GL_SZ_LIN,new Float(2.0f),g.getID());}
	    else if (animScheme.equals("exp")){vsm.animator.createGlyphAnimation(3000,AnimManager.GL_SZ_PAR,new Float(2.0f),g.getID());}
	    else if (animScheme.equals("sig")){vsm.animator.createGlyphAnimation(3000,AnimManager.GL_SZ_SIG,new Float(2.0f),g.getID());}
	}
	else if (animType.equals("col")){
	    Vector v=new Vector();
	    v.add(new Float(0));v.add(new Float(-0.8f));v.add(new Float(-0.6f));
	    v.add(new Float(0));v.add(new Float(0));v.add(new Float(0));
	    vsm.animator.createGlyphAnimation(2000,AnimManager.GL_COLOR_LIN,v,g.getID());
	    v=new Vector();
	    v.add(new Float(0));v.add(new Float(0.8f));v.add(new Float(0.6f));
	    v.add(new Float(0));v.add(new Float(0));v.add(new Float(0));
	    vsm.animator.createGlyphAnimation(2000,AnimManager.GL_COLOR_LIN,v,g.getID());
	}
	else if (animType.equals("trans")){
	    if (animScheme.equals("lin")){vsm.animator.createGlyphAnimation(1000,AnimManager.GL_TRANS_LIN,new LongPoint(200,100),g.getID());}
	    else if (animScheme.equals("exp")){vsm.animator.createGlyphAnimation(1000,AnimManager.GL_TRANS_PAR,new LongPoint(200,100),g.getID());}
	    else if (animScheme.equals("sig")){vsm.animator.createGlyphAnimation(1000,AnimManager.GL_TRANS_SIG,new LongPoint(200,100),g.getID());}
	}
    }

    void multiLayer(){
	TransitionManager.fadeOut(vsm.getView("Demo"), 500, BLANK_COLOR, vsm,
				  new FadeOut(vsm.getView("Demo"), BLANK_COLOR, vsm.getVirtualSpace("vs1")){
				      public void animationEnded(Object target, short type, String dimension){
					  super.animationEnded(target, type, dimension);
					  multiLayerActions();
				      }
				  });
    }

    void multiLayerActions(){
	vsm.getView("Demo").setBlank(BLANK_COLOR);
	if (vsm.getView("Demo2")!=null){vsm.getView("Demo2").destroyView();}
	vsm.destroyGlyphsInSpace("vs1");
	vsm.getVirtualSpace("vs1").removeCamera(1);
	vsm.getView("Demo").destroyView();
	vsm.addVirtualSpace("vs2");
	vsm.addCamera("vs2");
	Vector vc1=new Vector();vc1.add(vsm.getVirtualSpace("vs1").getCamera(0));vc1.add(vsm.getVirtualSpace("vs2").getCamera(0));
	vsm.addExternalView(vc1, "Demo", View.STD_VIEW, viewWidth, viewHeight, false, true).setBackgroundColor(MULTI_LAYER_BKG_COLOR);
	vsm.getView("Demo").setLocation(IntroPanel.PANEL_WIDTH, 0);
	eh=new MultiLayerEvtHdlr(this);
	vsm.getView("Demo").setEventHandler(eh);
	VRectangleST g1=new VRectangleST(-2000,0,0,500,500,Color.blue);
	VTriangleST g2=new VTriangleST(2000,0,0,500,Color.blue);
	VDiamondST g3=new VDiamondST(0,-2000,0,500,Color.blue, Color.BLACK, 0.5f);
	VOctagonST g4=new VOctagonST(0,2000,0,500,Color.blue, Color.BLACK, 0.5f);
	float[] vertices={1.0f,0.76f,1.0f,0.76f,1.0f,0.76f,1.0f,0.76f,1.0f,0.76f,1.0f,0.76f,1.0f,0.76f,1.0f,0.76f};
	VShapeST g5=new VShapeST(0,0,0,200,vertices,Color.blue,0);
	VCbCurve cb1=new VCbCurve(0,1000,0,300,Color.black,(float)Math.PI/2,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	VCbCurve cb2=new VCbCurve(1000,0,0,300,Color.black,0,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	VCbCurve cb3=new VCbCurve(0,-1000,0,300,Color.black,(float)Math.PI/2,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	VCbCurve cb4=new VCbCurve(-1000,0,0,300,Color.black,0,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	vsm.addGlyph(g1,"vs2");vsm.addGlyph(g2,"vs2");vsm.addGlyph(g3,"vs2");vsm.addGlyph(g4,"vs2");vsm.addGlyph(g5,"vs2");
	vsm.addGlyph(cb1,"vs2");vsm.addGlyph(cb2,"vs2");vsm.addGlyph(cb3,"vs2");vsm.addGlyph(cb4,"vs2");
	VCircle c1=new VCircle(-2000,0,0,500,Color.yellow);
	VCircle c2=new VCircle(2000,0,0,500,Color.yellow);
	VCircle c3=new VCircle(0,-2000,0,500,Color.yellow);
	VCircle c4=new VCircle(0,2000,0,500,Color.yellow);
	VShapeST c5=new VShapeST(0,0,0,200,vertices,Color.yellow,0);
	cb1=new VCbCurve(0,1000,0,300,Color.black,(float)Math.PI/2,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	cb2=new VCbCurve(1000,0,0,300,Color.black,0,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	cb3=new VCbCurve(0,-1000,0,300,Color.black,(float)Math.PI/2,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	cb4=new VCbCurve(-1000,0,0,300,Color.black,0,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	vsm.addGlyph(c1,"vs1");vsm.addGlyph(c2,"vs1");vsm.addGlyph(c3,"vs1");vsm.addGlyph(c4,"vs1");vsm.addGlyph(c5,"vs1");
	vsm.addGlyph(cb1,"vs1");vsm.addGlyph(cb2,"vs1");vsm.addGlyph(cb3,"vs1");vsm.addGlyph(cb4,"vs1");
	vsm.getVirtualSpace("vs1").getCamera(0).posx=0;
	vsm.getVirtualSpace("vs1").getCamera(0).posy=0;
	vsm.getVirtualSpace("vs1").getCamera(0).setAltitude(800.0f);
	vsm.getVirtualSpace("vs2").getCamera(0).posx=0;
	vsm.getVirtualSpace("vs2").getCamera(0).posy=0;
	vsm.getVirtualSpace("vs2").getCamera(0).setAltitude(800.0f);
	reveal(false);
    }

    void multiView(){
	TransitionManager.fadeOut(vsm.getView("Demo"), 500, BLANK_COLOR, vsm,
				  new FadeOut(vsm.getView("Demo"), BLANK_COLOR, vsm.getVirtualSpace("vs1")){
				      public void animationEnded(Object target, short type, String dimension){
					  super.animationEnded(target, type, dimension);
					  multiViewActions();
				      }
				  });
    }

    void multiViewActions(){
	vsm.getView("Demo").setBlank(BLANK_COLOR);
	vsm.getView("Demo").setBackgroundColor(Color.WHITE);
	vsm.destroyGlyphsInSpace("vs1");
	vsm.destroyVirtualSpace("vs2");
	eh=new CameraDemoEvtHdlr(this);
	ViewEventHandler eh2=new CameraDemoEvtHdlr(this);
	vsm.addCamera("vs1");
	camNb++;  //keep track of how many cameras have been created in the virtual space
	Vector vc1=new Vector();
	vc1.add(vsm.getVirtualSpace("vs1").getCamera(camNb));
	vsm.addExternalView(vc1, "Demo2", View.STD_VIEW, 300, 200, false, true);
	vsm.getView("Demo").setEventHandler(eh);
	vsm.getView("Demo2").setEventHandler(eh2);
	vsm.getView("Demo2").setLocation(0,350);
	vsm.getView("Demo2").setBackgroundColor(Color.WHITE);
	VRectangle g1=new VRectangle(200,-200,0,100,50,Color.yellow);
	VRectangle g2=new VRectangle(200,200,0,100,50,Color.green);
	VRectangle g3=new VRectangle(-200,-200,0,100,50,Color.red);
	VRectangle g4=new VRectangle(-200,200,0,100,50,Color.blue);
	vsm.addGlyph(g1,"vs1");vsm.addGlyph(g2,"vs1");vsm.addGlyph(g3,"vs1");vsm.addGlyph(g4,"vs1");
	vsm.getGlobalView(vsm.getVirtualSpace("vs1").getCamera(camNb),200);
	reveal(true);
    }

    protected boolean isAutoZoomEnabled(){
	return autoZoomEnabled;
    }

    protected void setAutoZoomEnabled(boolean b){
	autoZoomEnabled = b;
    }

    public static void main(String[] args){
	System.out.println("-----------------");
	System.out.println("General information");
	System.out.println("JVM version: "+System.getProperty("java.vm.vendor")+" "+System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version"));
	System.out.println("OS type: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+"/"+System.getProperty("os.arch")+" "+System.getProperty("sun.cpu.isalist"));
	System.out.println("-----------------");
	System.out.println("Directory information");
	System.out.println("Java Classpath: "+System.getProperty("java.class.path"));	
	System.out.println("Java directory: "+System.getProperty("java.home"));
	System.out.println("Launching from: "+System.getProperty("user.dir"));
	System.out.println("-----------------");
	System.out.println("User informations");
	System.out.println("User name: "+System.getProperty("user.name"));
	System.out.println("User home directory: "+System.getProperty("user.home"));
	System.out.println("-----------------");
	Introduction appli=new Introduction();
    }
    
}

class FadeOut extends net.claribole.zvtm.engine.FadeOut {
    
    View view;
    Color blankColor;
    VirtualSpace spaceOwningFadeRect;
    
    FadeOut(View v, Color c, VirtualSpace vs){
	super(v, c, vs);
    }

    public void animationEnded(Object target, short type, String dimension){
	super.animationEnded(target, type, dimension);
    }

}
