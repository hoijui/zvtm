/*   FILE: Introduction.java
 *   DATE OF CREATION:   Dec 07 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
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

package fr.inria.zvtm.demo;


import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import fr.inria.zvtm.glyphs.CGlyph;
import fr.inria.zvtm.glyphs.SGlyph;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.BooleanOps;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VBoolShape;
import fr.inria.zvtm.glyphs.VCbCurve;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VDiamond;
import fr.inria.zvtm.glyphs.VDiamondOr;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VImageOr;
import fr.inria.zvtm.glyphs.VQdCurve;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VRectangleOr;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.VShape;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VTriangle;
import fr.inria.zvtm.glyphs.VTriangleOr;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.ConstantAccInterpolator;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.engine.TransitionManager;
import fr.inria.zvtm.engine.Location;

import org.jdesktop.animation.timing.interpolation.Interpolator;

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
    volatile String animScheme = "sig";

    int camNb=0;

    boolean autoZoomEnabled = false;
    
    static final String VS_1 = "vs1";
    static final String VS_2 = "vs2";
    
    static final float[] OCTAGON_VERTICES = {1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f};
    static final float[] STAR1_VERTICES={1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f};
    static final float[] STAR2_VERTICES = {1f,0.76f,1f,0.76f,1f,0.76f,1f,0.76f,1f,0.76f,1f,0.76f,1f,0.76f,1f,0.76f};
	
    

    Introduction(){
	init();
    }

    public void init(){
	    vsm = VirtualSpaceManager.INSTANCE;
	VText.setMainFont(new Font("dialog", 0, 40));
	vsm.addVirtualSpace(VS_1);
	vsm.addCamera(VS_1);
	Vector vc1=new Vector();vc1.add(vsm.getVirtualSpace(VS_1).getCamera(0));
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
	    c.setAltitude(l.alt);
	}
	TransitionManager.fadeIn(vsm.getView("Demo"), 500, vsm);
    }

    void cameraDemo(){
	if (vsm.getView("Demo2")!=null){vsm.getView("Demo2").destroyView();}
	TransitionManager.fadeOut(vsm.getView("Demo"), 500, BLANK_COLOR, vsm,
				  new EndAction(){
				      public void execute(Object subject,
						   Animation.Dimension dimension){
					  cameraDemoActions();
				      }
				  }
				  );
    }

    void cameraDemoActions(){
	vsm.getView("Demo").setBlank(BLANK_COLOR);
	vsm.getView("Demo").setBackgroundColor(Color.WHITE);
	vsm.destroyGlyphsInSpace(VS_1);
	eh=new CameraDemoEvtHdlr(this);
	vsm.getView("Demo").setEventHandler(eh);
	long randomX=0;
	long randomY=0;
	long randomS=0;
	float randomOr = 0;
	float randomSat=0;
	double shapeType=0;
	Glyph g;
	for (int i=0;i<400;i++){
	    randomX=Math.round(Math.random()*6000);
	    randomY=Math.round(Math.random()*6000);
	    randomS=Math.round(Math.random()*199)+20;
	    randomOr=(float)(Math.random()*2*Math.PI);
	    randomSat=(float)Math.random();
	    shapeType=Math.random();
	    if (shapeType<0.2){
		g=new VTriangleOr(randomX,randomY,0,randomS,Color.getHSBColor(0.66f,randomSat,0.8f),randomOr);
	    }
	    else if (shapeType<0.4){
		g=new VDiamondOr(randomX,randomY,0,randomS,Color.getHSBColor(0.66f,randomSat,0.8f),randomOr);
	    }
	    else if (shapeType<0.6){
		    g = new VShape(randomX, randomY, 0, randomS, OCTAGON_VERTICES, Color.getHSBColor(0.66f,randomSat,0.8f), Color.BLACK, randomOr);
	    }
	    else if (shapeType<0.8){
		g=new VRectangleOr(randomX,randomY,0,randomS,randomS,Color.getHSBColor(0.66f,randomSat,0.8f),randomOr);
	    }
	    else {
		g = new VShape(randomX, randomY, 0, randomS, STAR1_VERTICES, Color.getHSBColor(0.66f,randomSat,0.8f), randomOr);
	    }
	    vsm.addGlyph(g,VS_1);
	}
	reveal(true);
    }

    void objectFamilies(){
	TransitionManager.fadeOut(vsm.getView("Demo"), 500, BLANK_COLOR, vsm,
				  new EndAction(){
				      public void execute(Object subject,
							  Animation.Dimension dimension){
					  objectFamiliesActions();
				      }
				  }
				  );
    }

    void objectFamiliesActions(){
	vsm.getView("Demo").setBlank(BLANK_COLOR);
	vsm.getView("Demo").setBackgroundColor(Color.LIGHT_GRAY);
	vsm.destroyGlyphsInSpace(VS_1);
	eh=new CameraDemoEvtHdlr(this);
	vsm.getView("Demo").setEventHandler(eh);
	VRectangle r1=new VRectangle(-600,400,0,100,50,Color.black);
	VRectangle r2=new VRectangle(-200,400,0,50,50,Color.black);
	VRectangleOr r3=new VRectangleOr(200,400,0,30,100,Color.black,0.707f);
	VRectangle r4=new VRectangle(600,400,0,100,75,Color.WHITE, Color.BLACK, 0.5f);
	r2.setDashed(true);r3.setFilled(false);
	vsm.addGlyph(r1,VS_1);vsm.addGlyph(r2,VS_1);vsm.addGlyph(r3,VS_1);vsm.addGlyph(r4,VS_1);
	r1.setHSVColor(0.5f,0.9f,0.6f);r2.setHSVColor(0.5f,0.9f,0.6f);r3.setHSVColor(0.5f,0.9f,0.6f);r4.setHSVColor(0.5f,0.9f,0.6f);
	VTriangle t1=new VTriangle(-600,200,0,50,Color.BLACK, Color.BLACK, 0.5f);
	VTriangle t2=new VTriangle(-200,200,0,50,Color.black);
	VTriangle t3=new VTriangle(200,200,0,50,Color.black);
	VTriangleOr t4=new VTriangleOr(600,200,0,75,Color.black, Color.BLACK, 0.707f, 0.5f);
	t1.setDashed(true);t3.setFilled(false);t3.setDashed(true);
	vsm.addGlyph(t1,VS_1);vsm.addGlyph(t2,VS_1);vsm.addGlyph(t3,VS_1);vsm.addGlyph(t4,VS_1);
	t1.setHSVColor(0.66f,0.5f,0.5f);t2.setHSVColor(0.66f,0.5f,0.5f);t3.setHSVColor(0.66f,0.5f,0.5f);t4.setHSVColor(0.66f,0.5f,0.5f);
	VDiamond d1=new VDiamond(-600,0,0,50,Color.black, Color.BLACK, 0.5f);
	VDiamond d2=new VDiamond(-200,0,0,45,Color.black);
	d1.setDashed(true);d2.setFilled(false);
	vsm.addGlyph(d1,VS_1);vsm.addGlyph(d2,VS_1);
	d1.setHSVColor(0.0f,0.8f,0.8f);d2.setHSVColor(0.0f,0.8f,0.8f);
	VCircle x1=new VCircle(-600,-200,0,50,Color.black);
	VSegment x2=new VSegment(-200,-200,0,50,100,Color.black);
	BooleanOps[] barray={new BooleanOps(0,-20,20,40,1,2),new BooleanOps(0,25,20,40,2,1)};
	VBoolShape x3=new VBoolShape(200,-200,0,100,50,2,barray,Color.black);
	VText x4=new VText(600,-200,0,Color.black,"text object", VText.TEXT_ANCHOR_MIDDLE);
	x2.setDashed(true);
	vsm.addGlyph(x1,VS_1);vsm.addGlyph(x2,VS_1);vsm.addGlyph(x3,VS_1);vsm.addGlyph(x4,VS_1);
	x1.setHSVColor(0.2f,0.55f,0.95f);x2.setHSVColor(0.2f,0.55f,0.95f);x3.setHSVColor(0.2f,0.55f,0.95f);x4.setHSVColor(0.2f,0.55f,0.95f);
	VImage i1=new VImage(0,-400,0,(new ImageIcon(this.getClass().getResource("/images/logo-futurs-small.png"))).getImage());i1.setDrawBorderPolicy(VImage.DRAW_BORDER_MOUSE_INSIDE);
	vsm.addGlyph(i1,VS_1);
	float[] vs={0.1f,0.5f,0.3f,0.5f,1f,0.5f,1f,0.5f};
	VShape s1=new VShape(-600,-400,0,100,vs,Color.gray,Color.BLACK, 0, 0.5f);vsm.addGlyph(s1,VS_1);
	float[] vs2={1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f};
	VShape s2=new VShape(600,-400,0,100,vs2,Color.gray,0);vsm.addGlyph(s2,VS_1);
	VQdCurve qd1=new VQdCurve(-600,-600,0,100,Color.black,0,50,(float)Math.PI/2);
	vsm.addGlyph(qd1,VS_1);
	VQdCurve qd2=new VQdCurve(0,-600,0,100,Color.black,0.707f,100,(float)Math.PI/3);
	vsm.addGlyph(qd2,VS_1);
	VCbCurve cb1=new VCbCurve(600,-600,0,100,Color.black,0,50,(float)Math.PI/2,100,(float)-Math.PI/2);
	vsm.addGlyph(cb1,VS_1);
	qd2.setHSVColor(0.0f,0.8f,0.8f);
	cb1.setHSVColor(0.66f,0.5f,0.5f);
	//will be the primary glyph of a CGlyph
	VRectangleOr cg1=new VRectangleOr(0,-900,0,400,100,Color.white,0);
	//and 4 secondary glyphs (init coordinates of secondary glyphs do not matter as they will be changed to match the position offset defined in the associated SGlyph)
	VTriangleOr cg2=new VTriangleOr(0,-800,0,50,Color.red,(float)(-Math.PI/4.0f));
	VTriangleOr cg3=new VTriangleOr(0,-800,0,50,Color.red,(float)(Math.PI/4.0f));
	VTriangleOr cg4=new VTriangleOr(0,-800,0,50,Color.red,(float)(-5*Math.PI/4.0f));
	VTriangleOr cg5=new VTriangleOr(0,-800,0,50,Color.red,(float)(-3*Math.PI/4.0f));
	vsm.addGlyph(cg1,VS_1);vsm.addGlyph(cg2,VS_1);vsm.addGlyph(cg3,VS_1);vsm.addGlyph(cg4,VS_1);vsm.addGlyph(cg5,VS_1);
	cg1.setHSVColor(0.66f,0.5f,0.5f);
	SGlyph[] sgs={
	    new SGlyph(cg2,400,100,SGlyph.FULL_ROTATION,SGlyph.RESIZE),
	    new SGlyph(cg3,-400,100,SGlyph.FULL_ROTATION,SGlyph.RESIZE),
	    new SGlyph(cg4,-400,-100,SGlyph.FULL_ROTATION,SGlyph.RESIZE),
	    new SGlyph(cg5,400,-100,SGlyph.FULL_ROTATION,SGlyph.RESIZE)
	};
	CGlyph cg=new CGlyph(cg1,sgs);
	vsm.addCGlyph(cg,VS_1);  //use addCGlyph, not addGlyph
	reveal(true);
    }

    void objectAnim(){
	TransitionManager.fadeOut(vsm.getView("Demo"), 500, BLANK_COLOR, vsm,
				  new EndAction(){
				      public void execute(Object subject,
							  Animation.Dimension dimension){
					  objectAnimActions();
				      }
				  }
				  );
    }

    void objectAnimActions(){
	vsm.getView("Demo").setBlank(BLANK_COLOR);
	vsm.getView("Demo").setBackgroundColor(ANIM_BKG_COLOR);
	vsm.destroyGlyphsInSpace(VS_1);
	vsm.destroyVirtualSpace(VS_2);
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
	vsm.addGlyph(orG,VS_1);vsm.addGlyph(szG,VS_1);vsm.addGlyph(clG,VS_1);vsm.addGlyph(trG,VS_1);
	vsm.addGlyph(orT,VS_1);vsm.addGlyph(szT,VS_1);vsm.addGlyph(clT,VS_1);vsm.addGlyph(trT,VS_1);
	orG.setColor(Introduction.ANIM_SELECTED_BUTTON_COLOR);szG.setColor(Introduction.ANIM_BUTTON_COLOR);clG.setColor(Introduction.ANIM_BUTTON_COLOR);trG.setColor(Introduction.ANIM_BUTTON_COLOR);
	VSegment sep=new VSegment(700,0,0,1,300,Color.black);
	vsm.addGlyph(sep,VS_1);
	VRectangle linG=new VRectangle(1000,200,0,25,25,Color.black);
	linG.setType("lin");
	VText linT = new VText(1000, 100, 0, Color.black, "Linear", VText.TEXT_ANCHOR_MIDDLE);
	VRectangle expG=new VRectangle(1000,0,0,25,25,Color.black);
	expG.setType("exp");
	VText expT = new VText(1000, -100, 0, Color.black, "Slow-in/Fast-out", VText.TEXT_ANCHOR_MIDDLE);
	VRectangle sigG=new VRectangle(1000,-200,0,25,25,Color.black);
	sigG.setType("sig");
	VText sigT = new VText(1000, -300, 0, Color.black, "Slow-in/Slow-out", VText.TEXT_ANCHOR_MIDDLE);
	vsm.addGlyph(linG,VS_1);vsm.addGlyph(expG,VS_1);vsm.addGlyph(sigG,VS_1);
	vsm.addGlyph(linT,VS_1);vsm.addGlyph(expT,VS_1);vsm.addGlyph(sigT,VS_1);
	linG.setColor(Introduction.ANIM_BUTTON_COLOR);expG.setColor(Introduction.ANIM_BUTTON_COLOR);sigG.setColor(Introduction.ANIM_SELECTED_BUTTON_COLOR);
	eh=new AnimationEvtHdlr(this,orG,szG,clG,trG,linG,expG,sigG);
	vsm.getView("Demo").setEventHandler(eh);

	VCircle c1=new VCircle(-400,900,0,100,Color.black);c1.setType("an");	
	VTriangleOr t1=new VTriangleOr(-400,600,0,100,Color.black,0);t1.setType("an");
	VShape o1 = new VShape(-400, 300, 0, 100, OCTAGON_VERTICES, Color.BLACK, 0);o1.setType("an");
	VRectangleOr r1=new VRectangleOr(-400,0,0,100,50,Color.black,0);r1.setType("an");
	VDiamondOr d1=new VDiamondOr(-400,-300,0,100,Color.black,0);d1.setType("an");
	vsm.addGlyph(c1,VS_1);vsm.addGlyph(t1,VS_1);vsm.addGlyph(r1,VS_1);vsm.addGlyph(d1,VS_1);vsm.addGlyph(o1,VS_1);
	c1.setColor(Introduction.ANIM_OBJECT_COLOR);
	o1.setColor(Introduction.ANIM_OBJECT_COLOR);
    t1.setColor(Introduction.ANIM_OBJECT_COLOR);
	r1.setColor(Introduction.ANIM_OBJECT_COLOR);
	d1.setColor(Introduction.ANIM_OBJECT_COLOR);
	VImageOr i1=new VImageOr(-400,-600,0,(new ImageIcon(this.getClass().getResource("/images/xrce.gif"))).getImage(),0.0f);i1.setDrawBorderPolicy(VImage.DRAW_BORDER_MOUSE_INSIDE);
	vsm.addGlyph(i1,VS_1);i1.setType("an");
	i1.sizeTo(200);
	float[] vs={1f,0.4f,1f,0.4f,0.8f,0.5f,0.3f,1f};
	VShape s1=new VShape(-400,-900,0,100,vs,ANIM_OBJECT_COLOR,0);vsm.addGlyph(s1,VS_1);s1.setType("an");

	//will be the primary glyph of a CGlyph
	VRectangleOr cg1=new VRectangleOr(-400,-1200,0,200,100,Color.black,0);
	//and 4 secondary glyphs (init coordinates of secondary glyphs do not matter as they will be changed to match the position offset defined in the associated SGlyph)
	VTriangleOr cg2=new VTriangleOr(0,0,0,50,Color.black,0);
	VRectangleOr cg3=new VRectangleOr(0,0,0,50,50,Color.black,Color.BLACK,0.404f, 0.5f);
	VTriangleOr cg4=new VTriangleOr(0,0,0,50,Color.black,Color.BLACK,0.404f, 0.5f);
	VRectangleOr cg5=new VRectangleOr(0,0,0,50,50,Color.black,0);
	vsm.addGlyph(cg1,VS_1);vsm.addGlyph(cg2,VS_1);vsm.addGlyph(cg3,VS_1);vsm.addGlyph(cg4,VS_1);vsm.addGlyph(cg5,VS_1);
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
	vsm.addCGlyph(cg,VS_1);  //use addCGlyph, not addGlyph


	//will be the primary glyph of a CGlyph
	cg1=new VRectangleOr(-400,-1600,0,200,100,Color.black,0);
	//and 4 secondary glyphs (init coordinates of secondary glyphs do not matter as they will be changed to match the position offset defined in the associated SGlyph)
	cg2=new VTriangleOr(0,0,0,50,Color.black,0);
	cg3=new VRectangleOr(0,0,0,50,50,Color.black,Color.BLACK,0.404f, 0.5f);
	cg4=new VTriangleOr(0,0,0,50,Color.black,Color.BLACK, 0.404f, 0.5f);
	cg5=new VRectangleOr(0,0,0,50,50,Color.black,0);
	vsm.addGlyph(cg1,VS_1);vsm.addGlyph(cg2,VS_1);vsm.addGlyph(cg3,VS_1);vsm.addGlyph(cg4,VS_1);vsm.addGlyph(cg5,VS_1);
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
	vsm.addCGlyph(cg,VS_1);  //use addCGlyph, not addGlyph
	reveal(true);
    }

    void animate(Glyph g){
	Interpolator inter = SlowInSlowOutInterpolator.getInstance();
 	if (animScheme.equals("lin")){inter = IdentityInterpolator.getInstance();}
 	else if (animScheme.equals("exp")){inter = ConstantAccInterpolator.getInstance();}
 	else if (animScheme.equals("sig")){inter = SlowInSlowOutInterpolator.getInstance();}

	if (animType.equals("orient")){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphOrientationAnim(1200, g, 10f, true, inter, null);
	    vsm.getAnimationManager().startAnimation(anim, true);
	}
	else if (animType.equals("size")){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphSizeAnim(3000, g, 200f, true, inter, null);
	    vsm.getAnimationManager().startAnimation(anim, true);
	}
	else if (animType.equals("col")){
	    float[] fill = {0, -0.8f, -0.6f};
	    float[] fill2 = {0, 0.8f, 0.6f};
	    
	     Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphFillColorAnim(2000, g, fill, true, inter, null);
	     Animation anim2 = vsm.getAnimationManager().getAnimationFactory()
		 .createGlyphFillColorAnim(2000, g, fill2, true, inter, null);
	     vsm.getAnimationManager().startAnimation(anim, false);
	     vsm.getAnimationManager().startAnimation(anim2, false);
	}
	else if (animType.equals("trans")){
	    Animation anim = vsm.getAnimationManager().getAnimationFactory()
		.createGlyphTranslation(1000, g, new LongPoint(200,100), true, inter, null);
	    vsm.getAnimationManager().startAnimation(anim, true);
	}
    }

    void multiLayer(){
	TransitionManager.fadeOut(vsm.getView("Demo"), 500, BLANK_COLOR, vsm,
				  new EndAction(){
				      public void execute(Object subject,
							  Animation.Dimension dimension){
					  multiLayerActions();
				      }
				  }
				  );
    }
    
    void multiLayerActions(){
	vsm.getView("Demo").setBlank(BLANK_COLOR);
	if (vsm.getView("Demo2")!=null){vsm.getView("Demo2").destroyView();}
	vsm.destroyGlyphsInSpace(VS_1);
	vsm.getVirtualSpace(VS_1).removeCamera(1);
	vsm.getView("Demo").destroyView();
	vsm.addVirtualSpace(VS_2);
	vsm.addCamera(VS_2);
	Vector vc1=new Vector();vc1.add(vsm.getVirtualSpace(VS_1).getCamera(0));vc1.add(vsm.getVirtualSpace(VS_2).getCamera(0));
	vsm.addExternalView(vc1, "Demo", View.STD_VIEW, viewWidth, viewHeight, false, true).setBackgroundColor(MULTI_LAYER_BKG_COLOR);
	vsm.getView("Demo").setLocation(IntroPanel.PANEL_WIDTH, 0);
	eh=new MultiLayerEvtHdlr(this);
	vsm.getView("Demo").setEventHandler(eh, 0);
	vsm.getView("Demo").setEventHandler(eh, 1);
	VRectangle g1=new VRectangle(-2000,0,0,500,500,Color.blue, Color.BLACK, 0.5f);
	VTriangle g2=new VTriangle(2000,0,0,500,Color.blue, Color.BLACK, 0.5f);
	VDiamond g3=new VDiamond(0,-2000,0,500,Color.blue, Color.BLACK, 0.5f);
	VShape g4 = new VShape(0, 2000, 0, 500, OCTAGON_VERTICES, Color.BLUE, Color.BLACK, (float)(2*Math.PI/16.0f), 0.5f);
	VShape g5 = new VShape(0, 0, 0, 200, STAR2_VERTICES, Color.blue, Color.BLACK, 0, 0.5f);
	VCbCurve cb1=new VCbCurve(0,1000,0,300,Color.black,(float)Math.PI/2,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	VCbCurve cb2=new VCbCurve(1000,0,0,300,Color.black,0,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	VCbCurve cb3=new VCbCurve(0,-1000,0,300,Color.black,(float)Math.PI/2,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	VCbCurve cb4=new VCbCurve(-1000,0,0,300,Color.black,0,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	vsm.addGlyph(g1,VS_2);vsm.addGlyph(g2,VS_2);vsm.addGlyph(g3,VS_2);vsm.addGlyph(g4, VS_2);vsm.addGlyph(g5,VS_2);
	vsm.addGlyph(cb1,VS_2);vsm.addGlyph(cb2,VS_2);vsm.addGlyph(cb3,VS_2);vsm.addGlyph(cb4,VS_2);
	VCircle c1=new VCircle(-2000,0,0,500,Color.yellow);
	VCircle c2=new VCircle(2000,0,0,500,Color.yellow);
	VCircle c3=new VCircle(0,-2000,0,500,Color.yellow);
	VCircle c4=new VCircle(0,2000,0,500,Color.yellow);
	VShape c5 = new VShape(0, 0, 0, 200, STAR1_VERTICES, Color.yellow, Color.BLACK, 0, 0.5f);
	cb1=new VCbCurve(0,1000,0,300,Color.black,(float)Math.PI/2,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	cb2=new VCbCurve(1000,0,0,300,Color.black,0,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	cb3=new VCbCurve(0,-1000,0,300,Color.black,(float)Math.PI/2,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	cb4=new VCbCurve(-1000,0,0,300,Color.black,0,200,(float)Math.PI/2,200,(float)-Math.PI/2);
	vsm.addGlyph(c1,VS_1);vsm.addGlyph(c2,VS_1);vsm.addGlyph(c3,VS_1);vsm.addGlyph(c4,VS_1);vsm.addGlyph(c5,VS_1);
	vsm.addGlyph(cb1,VS_1);vsm.addGlyph(cb2,VS_1);vsm.addGlyph(cb3,VS_1);vsm.addGlyph(cb4,VS_1);
	vsm.getVirtualSpace(VS_1).getCamera(0).posx=0;
	vsm.getVirtualSpace(VS_1).getCamera(0).posy=0;
	vsm.getVirtualSpace(VS_1).getCamera(0).setAltitude(800.0f);
	vsm.getVirtualSpace(VS_2).getCamera(0).posx=0;
	vsm.getVirtualSpace(VS_2).getCamera(0).posy=0;
	vsm.getVirtualSpace(VS_2).getCamera(0).setAltitude(800.0f);
	reveal(false);
    }

    void multiView(){
	TransitionManager.fadeOut(vsm.getView("Demo"), 500, BLANK_COLOR, vsm,
				  new EndAction(){
				      public void execute(Object subject,
							  Animation.Dimension dimension){
					  multiViewActions();
				      }
				  }
				  );
    }

    void multiViewActions(){
	vsm.getView("Demo").setBlank(BLANK_COLOR);
	vsm.getView("Demo").setBackgroundColor(Color.WHITE);
	vsm.destroyGlyphsInSpace(VS_1);
	vsm.destroyVirtualSpace(VS_2);
	eh=new CameraDemoEvtHdlr(this);
	ViewEventHandler eh2=new CameraDemoEvtHdlr(this);
	vsm.addCamera(VS_1);
	camNb++;  //keep track of how many cameras have been created in the virtual space
	Vector vc1=new Vector();
	vc1.add(vsm.getVirtualSpace(VS_1).getCamera(camNb));
	vsm.addExternalView(vc1, "Demo2", View.STD_VIEW, 300, 200, false, true);
	vsm.getView("Demo").setEventHandler(eh);
	vsm.getView("Demo2").setEventHandler(eh2);
	vsm.getView("Demo2").setLocation(0,350);
	vsm.getView("Demo2").setBackgroundColor(Color.WHITE);
	VRectangle g1=new VRectangle(200,-200,0,100,50,Color.yellow);
	VRectangle g2=new VRectangle(200,200,0,100,50,Color.green);
	VRectangle g3=new VRectangle(-200,-200,0,100,50,Color.red);
	VRectangle g4=new VRectangle(-200,200,0,100,50,Color.blue);
	vsm.addGlyph(g1,VS_1);vsm.addGlyph(g2,VS_1);vsm.addGlyph(g3,VS_1);vsm.addGlyph(g4,VS_1);
	vsm.getGlobalView(vsm.getVirtualSpace(VS_1).getCamera(camNb),200);
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
        new Introduction();
    }
    
}
