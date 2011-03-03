/*   FILE: Introduction.java
 *   DATE OF CREATION:   Dec 07 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2011. All Rights Reserved
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
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.awt.geom.Point2D;

import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VImageOr;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VRectangleOr;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.VShape;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.ConstantAccInterpolator;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.engine.Transitions;
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

    ViewListener eh;

    String animType = "orient";
    volatile String animScheme = "sig";

    int camNb=0;

    boolean autoZoomEnabled = false;
    
    static final String VS_1 = "vs1";
    VirtualSpace vs1,vs2;
    static final String VS_2 = "vs2";
    
    static final float[] TRIANGLE_VERTICES = {1f, 1f, 1f};
    static final float[] DIAMOND_VERTICES = {1f, 1f, 1f, 1f};
    static final float[] OCTAGON_VERTICES = {1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f};
    static final float[] STAR1_VERTICES={1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f};
    static final float[] STAR2_VERTICES = {1f,0.76f,1f,0.76f,1f,0.76f,1f,0.76f,1f,0.76f,1f,0.76f,1f,0.76f,1f,0.76f};
	
    View demoView;

    Introduction(){
	init();
    }

    public void init(){
	    vsm = VirtualSpaceManager.INSTANCE;
	VText.setMainFont(new Font("dialog", 0, 40));
	vs1 = vsm.addVirtualSpace(VS_1);
	vs1.addCamera();
	Vector vc1=new Vector();vc1.add(vs1.getCamera(0));
	Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
	if (screenDimension.width < IntroPanel.PANEL_WIDTH + PREFERRED_VIEW_WIDTH){
	    viewWidth = screenDimension.width - IntroPanel.PANEL_WIDTH;
	}
	if (screenDimension.height < PREFERRED_VIEW_HEIGHT){
	    viewHeight = screenDimension.height;
	}
	demoView = vsm.addFrameView(vc1, "Demo", View.STD_VIEW, viewWidth, viewHeight, true);
	demoView.setBackgroundColor(Color.BLACK);
	demoView.setLocation(IntroPanel.PANEL_WIDTH, 0);
	iPanel=new IntroPanel(this);
	//setAutoZoomEnabled(true);
    }

    void reveal(boolean center){
	if (center){
	    Camera c = demoView.getCameraNumber(0);
	    Location l = demoView.getGlobalView(c);
	    c.vx = l.vx;
	    c.vy = l.vy;
	    c.setAltitude(l.alt);
	}
	Transitions.fadeIn(demoView, 500);
    }

    void cameraDemo(){
	if (vsm.getView("Demo2")!=null){vsm.getView("Demo2").destroyView();}
	Transitions.fadeOut(demoView, 500, BLANK_COLOR,
				  new EndAction(){
				      public void execute(Object subject,
						   Animation.Dimension dimension){
					  cameraDemoActions();
				      }
				  }
				  );
    }

    void cameraDemoActions(){
        demoView.setBlank(BLANK_COLOR);
        demoView.setBackgroundColor(Color.WHITE);
        vs1.removeAllGlyphs();
        eh=new CameraDemoEvtHdlr(this);
        demoView.setListener(eh);
        double randomX=0;
        double randomY=0;
        double randomS=0;
        double randomOr = 0;
        float randomSat=0;
        double shapeType=0;
        Glyph g;
        for (int i=0;i<400;i++){
            randomX=Math.random()*6000;
            randomY=Math.random()*6000;
            randomS=Math.random()*199+20;
            randomOr=Math.random()*2*Math.PI;
            randomSat=(float)Math.random();
            shapeType=Math.random();
            if (shapeType<0.2){
                g = new VShape(randomX, randomY, 0, randomS, TRIANGLE_VERTICES, Color.getHSBColor(0.66f,randomSat,0.8f), randomOr);
            }
            else if (shapeType<0.4){
                g = new VShape(randomX, randomY, 0, randomS, DIAMOND_VERTICES, Color.getHSBColor(0.66f,randomSat,0.8f), randomOr);
            }
            else if (shapeType<0.6){
                g = new VShape(randomX, randomY, 0, randomS, OCTAGON_VERTICES, Color.getHSBColor(0.66f,randomSat,0.8f), randomOr);
            }
            else if (shapeType<0.8){
                g=new VRectangleOr(randomX,randomY,0,randomS,randomS,Color.getHSBColor(0.66f,randomSat,0.8f),randomOr);
            }
            else {
                g = new VShape(randomX, randomY, 0, randomS, STAR1_VERTICES, Color.getHSBColor(0.66f,randomSat,0.8f), randomOr);
            }
            vs1.addGlyph(g);
        }
        reveal(true);
    }

    void objectFamilies(){
        Transitions.fadeOut(demoView, 500, BLANK_COLOR,
        new EndAction(){
            public void execute(Object subject,
            Animation.Dimension dimension){
                objectFamiliesActions();
            }
        }
        );
    }

    void objectFamiliesActions(){
        demoView.setBlank(BLANK_COLOR);
        demoView.setBackgroundColor(Color.LIGHT_GRAY);
        vs1.removeAllGlyphs();
        eh=new CameraDemoEvtHdlr(this);
        demoView.setListener(eh);
        VRectangle r1=new VRectangle(-600,400,0,200,100,Color.black);
        VRectangle r2=new VRectangle(-200,400,0,100,100,Color.black);
        VRectangleOr r3=new VRectangleOr(200,400,0,60,200,Color.black,0.707f);
        VRectangle r4=new VRectangle(600,400,0,200,150,Color.WHITE, Color.BLACK, 0.5f);
        r2.setDashed(true);r3.setFilled(false);
        vs1.addGlyph(r1);vs1.addGlyph(r2);vs1.addGlyph(r3);vs1.addGlyph(r4);
        r1.setHSVColor(0.5f,0.9f,0.6f);r2.setHSVColor(0.5f,0.9f,0.6f);r3.setHSVColor(0.5f,0.9f,0.6f);r4.setHSVColor(0.5f,0.9f,0.6f);
        VShape t1=new VShape(-600,200,0,100, TRIANGLE_VERTICES, Color.BLACK, Color.BLACK, 0, 0.5f);
        VShape t2=new VShape(-200,200,0,100, TRIANGLE_VERTICES,Color.BLACK, 0);
        VShape t3=new VShape(200,200,0,100, TRIANGLE_VERTICES,Color.BLACK, 0);
        VShape t4=new VShape(600,200,0,150, TRIANGLE_VERTICES,Color.BLACK, Color.BLACK, 0.707f, 0.5f);
        t1.setDashed(true);t3.setFilled(false);t3.setDashed(true);
        vs1.addGlyph(t1);vs1.addGlyph(t2);vs1.addGlyph(t3);vs1.addGlyph(t4);
        t1.setHSVColor(0.66f,0.5f,0.5f);t2.setHSVColor(0.66f,0.5f,0.5f);t3.setHSVColor(0.66f,0.5f,0.5f);t4.setHSVColor(0.66f,0.5f,0.5f);
        VShape d1=new VShape(-600,0,0,100, DIAMOND_VERTICES, Color.BLACK, Color.BLACK, 0.5f);
        VShape d2=new VShape(-200,0,0,90, DIAMOND_VERTICES, Color.BLACK, 0);
        d1.setDashed(true);d2.setFilled(false);
        vs1.addGlyph(d1);vs1.addGlyph(d2);
        d1.setHSVColor(0.0f,0.8f,0.8f);d2.setHSVColor(0.0f,0.8f,0.8f);
        VCircle x1=new VCircle(-600,-200,0,50,Color.black);
        VSegment x2=new VSegment(-200,-200,0,50,100,Color.black);
        VText x4=new VText(600,-200,0,Color.black,"text object", VText.TEXT_ANCHOR_MIDDLE);
        x2.setDashed(true);
        vs1.addGlyph(x1);vs1.addGlyph(x2);vs1.addGlyph(x4);
        x1.setHSVColor(0.2f,0.55f,0.95f);x2.setHSVColor(0.2f,0.55f,0.95f);x4.setHSVColor(0.2f,0.55f,0.95f);
        VImage i1=new VImage(0,-400,0,(new ImageIcon(this.getClass().getResource("/images/logo-futurs-small.png"))).getImage());
        vs1.addGlyph(i1);
        float[] vs={0.1f,0.5f,0.3f,0.5f,1f,0.5f,1f,0.5f};
        VShape s1=new VShape(-600,-400,0,100,vs,Color.gray,Color.BLACK, 0, 0.5f);vs1.addGlyph(s1);
        float[] vs2={1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f,1f,0.8f};
        VShape s2=new VShape(600,-400,0,100,vs2,Color.gray,0);vs1.addGlyph(s2);
        //VQdCurve qd1=new VQdCurve(-600,-600,0,100,Color.black,0,50,(float)Math.PI/2);
        //vs1.addGlyph(qd1);
        //VQdCurve qd2=new VQdCurve(0,-600,0,100,Color.black,0.707f,100,(float)Math.PI/3);
        //vs1.addGlyph(qd2);
        //VCbCurve cb1=new VCbCurve(600,-600,0,100,Color.black,0,50,(float)Math.PI/2,100,(float)-Math.PI/2);
        //vs1.addGlyph(cb1);
        //qd2.setHSVColor(0.0f,0.8f,0.8f);
        //cb1.setHSVColor(0.66f,0.5f,0.5f);
        ////will be the primary glyph of a CGlyph
        //VRectangleOr cg1=new VRectangleOr(0,-900,0,400,100,Color.white,0);
        ////and 4 secondary glyphs (init coordinates of secondary glyphs do not matter as they will be changed to match the position offset defined in the associated SGlyph)
        //VTriangleOr cg2=new VTriangleOr(0,-800,0,50,Color.red,(float)(-Math.PI/4.0f));
        //VTriangleOr cg3=new VTriangleOr(0,-800,0,50,Color.red,(float)(Math.PI/4.0f));
        //VTriangleOr cg4=new VTriangleOr(0,-800,0,50,Color.red,(float)(-5*Math.PI/4.0f));
        //VTriangleOr cg5=new VTriangleOr(0,-800,0,50,Color.red,(float)(-3*Math.PI/4.0f));
        //vs1.addGlyph(cg1);vs1.addGlyph(cg2);vs1.addGlyph(cg3);vs1.addGlyph(cg4);vs1.addGlyph(cg5);
        //cg1.setHSVColor(0.66f,0.5f,0.5f);
        //SGlyph[] sgs={
            //    new SGlyph(cg2,400,100,SGlyph.FULL_ROTATION,SGlyph.RESIZE),
            //    new SGlyph(cg3,-400,100,SGlyph.FULL_ROTATION,SGlyph.RESIZE),
            //    new SGlyph(cg4,-400,-100,SGlyph.FULL_ROTATION,SGlyph.RESIZE),
            //    new SGlyph(cg5,400,-100,SGlyph.FULL_ROTATION,SGlyph.RESIZE)
            //};
            //CGlyph cg=new CGlyph(cg1,sgs);
            reveal(true);
    }

    void objectAnim(){
	Transitions.fadeOut(demoView, 500, BLANK_COLOR,
				  new EndAction(){
				      public void execute(Object subject,
							  Animation.Dimension dimension){
					  objectAnimActions();
				      }
				  }
				  );
    }

    void objectAnimActions(){
	demoView.setBlank(BLANK_COLOR);
	demoView.setBackgroundColor(ANIM_BKG_COLOR);
	vs1.removeAllGlyphs();
	vsm.destroyVirtualSpace(VS_2);
	VRectangle orG=new VRectangle(400,300,0,50,50,Color.black);
	orG.setType("orient");
	VText orT = new VText(400, 200, 0, Color.black, "Orientation", VText.TEXT_ANCHOR_MIDDLE);
	VRectangle szG=new VRectangle(400,100,0,50,50,Color.black);
	szG.setType("size");
	VText szT = new VText(400, 0, 0, Color.black, "Size", VText.TEXT_ANCHOR_MIDDLE);
	VRectangle clG=new VRectangle(400,-100,0,50,50,Color.black);
	clG.setType("col");
	VText clT = new VText(400, -200, 0, Color.black, "Color", VText.TEXT_ANCHOR_MIDDLE);
	VRectangle trG=new VRectangle(400,-300,0,50,50,Color.black);
	trG.setType("pos");
	VText trT = new VText(400, -400, 0, Color.black, "Translation", VText.TEXT_ANCHOR_MIDDLE);
	vs1.addGlyph(orG);vs1.addGlyph(szG);vs1.addGlyph(clG);vs1.addGlyph(trG);
	vs1.addGlyph(orT);vs1.addGlyph(szT);vs1.addGlyph(clT);vs1.addGlyph(trT);
	orG.setColor(Introduction.ANIM_SELECTED_BUTTON_COLOR);szG.setColor(Introduction.ANIM_BUTTON_COLOR);clG.setColor(Introduction.ANIM_BUTTON_COLOR);trG.setColor(Introduction.ANIM_BUTTON_COLOR);
	VSegment sep=new VSegment(700,0,0,1,300,Color.black);
	vs1.addGlyph(sep);
	VRectangle linG=new VRectangle(1000,200,0,50,50,Color.black);
	linG.setType("lin");
	VText linT = new VText(1000, 100, 0, Color.black, "Linear", VText.TEXT_ANCHOR_MIDDLE);
	VRectangle expG=new VRectangle(1000,0,0,50,50,Color.black);
	expG.setType("exp");
	VText expT = new VText(1000, -100, 0, Color.black, "Slow-in/Fast-out", VText.TEXT_ANCHOR_MIDDLE);
	VRectangle sigG=new VRectangle(1000,-200,0,50,50,Color.black);
	sigG.setType("sig");
	VText sigT = new VText(1000, -300, 0, Color.black, "Slow-in/Slow-out", VText.TEXT_ANCHOR_MIDDLE);
	vs1.addGlyph(linG);vs1.addGlyph(expG);vs1.addGlyph(sigG);
	vs1.addGlyph(linT);vs1.addGlyph(expT);vs1.addGlyph(sigT);
	linG.setColor(Introduction.ANIM_BUTTON_COLOR);expG.setColor(Introduction.ANIM_BUTTON_COLOR);sigG.setColor(Introduction.ANIM_SELECTED_BUTTON_COLOR);
	eh=new AnimationEvtHdlr(this,orG,szG,clG,trG,linG,expG,sigG);
	demoView.setListener(eh);

	VCircle c1=new VCircle(-400,900,0,200,Color.black);c1.setType("an");	
	VShape t1=new VShape(-400,600,0,200, TRIANGLE_VERTICES, Color.black,0);t1.setType("an");
	VShape o1 = new VShape(-400, 300, 0, 200, OCTAGON_VERTICES, Color.BLACK, 0);o1.setType("an");
	VRectangleOr r1=new VRectangleOr(-400,0,0,200,100,Color.BLACK,0);r1.setType("an");
	VShape d1=new VShape(-400,-300,0,200, DIAMOND_VERTICES, Color.BLACK,0);d1.setType("an");
	vs1.addGlyph(c1);vs1.addGlyph(t1);vs1.addGlyph(r1);vs1.addGlyph(d1);vs1.addGlyph(o1);
	c1.setColor(Introduction.ANIM_OBJECT_COLOR);
	o1.setColor(Introduction.ANIM_OBJECT_COLOR);
    t1.setColor(Introduction.ANIM_OBJECT_COLOR);
	r1.setColor(Introduction.ANIM_OBJECT_COLOR);
	d1.setColor(Introduction.ANIM_OBJECT_COLOR);
	VImageOr i1=new VImageOr(-400,-600,0,(new ImageIcon(this.getClass().getResource("/images/xrce.gif"))).getImage(),0.0f);
	vs1.addGlyph(i1);i1.setType("an");
	i1.sizeTo(200);
	float[] vs={1f,0.4f,1f,0.4f,0.8f,0.5f,0.3f,1f};
	VShape s1=new VShape(-400,-900,0,100,vs,ANIM_OBJECT_COLOR,0);vs1.addGlyph(s1);s1.setType("an");

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
		.createGlyphTranslation(1000, g, new Point2D.Double(200,100), true, inter, null);
	    vsm.getAnimationManager().startAnimation(anim, true);
	}
    }

    void multiLayer(){
	Transitions.fadeOut(demoView, 500, BLANK_COLOR,
				  new EndAction(){
				      public void execute(Object subject,
							  Animation.Dimension dimension){
					  multiLayerActions();
				      }
				  }
				  );
    }
    
    void multiLayerActions(){
        demoView.setBlank(BLANK_COLOR);
        if (vsm.getView("Demo2")!=null){vsm.getView("Demo2").destroyView();}
        vs1.removeAllGlyphs();
        vs1.removeCamera(1);
        demoView.destroyView();
        vs2 = vsm.addVirtualSpace(VS_2);
        vs2.addCamera();
        Vector vc1=new Vector();vc1.add(vs1.getCamera(0));vc1.add(vsm.getVirtualSpace(VS_2).getCamera(0));
        vsm.addFrameView(vc1, "Demo", View.STD_VIEW, viewWidth, viewHeight, true).setBackgroundColor(MULTI_LAYER_BKG_COLOR);
        demoView = vsm.getView("Demo");
        demoView.setLocation(IntroPanel.PANEL_WIDTH, 0);
        eh=new MultiLayerEvtHdlr(this);
        demoView.setListener(eh, 0);
        demoView.setListener(eh, 1);
        VRectangle g1=new VRectangle(-2000,0,0,1000,1000,Color.blue, Color.BLACK, 0.5f);
        VShape g2=new VShape(2000,0,0,1000, TRIANGLE_VERTICES, Color.BLUE, Color.BLACK, 0.5f);
        VShape g3=new VShape(0,-2000,0,1000, DIAMOND_VERTICES, Color.BLUE, Color.BLACK, 0.5f);
        VShape g4 = new VShape(0, 2000, 0, 1000, OCTAGON_VERTICES, Color.BLUE, Color.BLACK, (float)(2*Math.PI/16.0f), 0.5f);
        VShape g5 = new VShape(0, 0, 0, 400, STAR2_VERTICES, Color.BLUE, Color.BLACK, 0, 0.5f);
        vs2.addGlyph(g1);vs2.addGlyph(g2);vs2.addGlyph(g3);vs2.addGlyph(g4);vs2.addGlyph(g5);
        VCircle c1=new VCircle(-2000,0,0,1000,Color.yellow);
        VCircle c2=new VCircle(2000,0,0,1000,Color.yellow);
        VCircle c3=new VCircle(0,-2000,0,1000,Color.yellow);
        VCircle c4=new VCircle(0,2000,0,1000,Color.yellow);
        VShape c5 = new VShape(0, 0, 0, 400, STAR1_VERTICES, Color.YELLOW, Color.BLACK, 0, 0.5f);
        vs1.addGlyph(c1);vs1.addGlyph(c2);vs1.addGlyph(c3);vs1.addGlyph(c4);vs1.addGlyph(c5);
        vs1.getCamera(0).vx=0;
        vs1.getCamera(0).vy=0;
        vs1.getCamera(0).setAltitude(800.0f);
        vsm.getVirtualSpace(VS_2).getCamera(0).vx=0;
        vsm.getVirtualSpace(VS_2).getCamera(0).vy=0;
        vsm.getVirtualSpace(VS_2).getCamera(0).setAltitude(800.0f);
        reveal(false);
    }

    void multiView(){
	Transitions.fadeOut(demoView, 500, BLANK_COLOR,
				  new EndAction(){
				      public void execute(Object subject,
							  Animation.Dimension dimension){
					  multiViewActions();
				      }
				  }
				  );
    }

    void multiViewActions(){
	demoView.setBlank(BLANK_COLOR);
	demoView.setBackgroundColor(Color.WHITE);
	vs1.removeAllGlyphs();
	vsm.destroyVirtualSpace(VS_2);
	eh=new CameraDemoEvtHdlr(this);
	ViewListener eh2=new CameraDemoEvtHdlr(this);
	vs1.addCamera();
	camNb++;  //keep track of how many cameras have been created in the virtual space
	Vector vc1=new Vector();
	vc1.add(vs1.getCamera(camNb));
	vsm.addFrameView(vc1, "Demo2", View.STD_VIEW, 300, 200, true);
	demoView.setListener(eh);
	vsm.getView("Demo2").setListener(eh2);
	vsm.getView("Demo2").setLocation(0,350);
	vsm.getView("Demo2").setBackgroundColor(Color.WHITE);
	VRectangle g1=new VRectangle(200,-200,0,200,100,Color.yellow);
	VRectangle g2=new VRectangle(200,200,0,200,100,Color.green);
	VRectangle g3=new VRectangle(-200,-200,0,200,100,Color.red);
	VRectangle g4=new VRectangle(-200,200,0,200,100,Color.blue);
	vs1.addGlyph(g1);vs1.addGlyph(g2);vs1.addGlyph(g3);vs1.addGlyph(g4);
	vsm.getView("Demo2").getGlobalView(vs1.getCamera(camNb),200);
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

class AnimationEvtHdlr implements ViewListener {

    Introduction application;

    double lastX,lastY;
    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    double tfactor;
    float cfactor=40.0f;

    VSegment navSeg;

    Camera activeCam;

    Glyph orientation;Glyph size;Glyph color;Glyph translation; //type of animation
    Glyph linear;Glyph exponential;Glyph sigmoid; //temporal scheme

    AnimationEvtHdlr(Introduction appli,Glyph o,Glyph s,Glyph c,Glyph t,Glyph l,Glyph e,Glyph sg){
	application=appli;
	orientation=o;size=s;color=c;translation=t;
	linear=l;exponential=e;sigmoid=sg;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	try {
	    Glyph g=v.lastGlyphEntered();
	    if (g!=null && g.getType().equals("an")){
		    v.getVCursor().stickGlyph(g);
	    }
	}
	catch (NullPointerException ex){}
	application.vsm.getActiveView().mouse.setSensitivity(false);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.getActiveView().mouse.setSensitivity(true);
	v.getVCursor().unstickLastGlyph();
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	Glyph a;
	if ((a=v.lastGlyphEntered())!=null){
	    String t=a.getType();
	    if (t.equals("an")){
		    application.animate(a);
	    }
	    else if (t.equals("orient")){
		if (application.animType.equals("orient")){
		    application.animType="";
		    orientation.setColor(Introduction.ANIM_BUTTON_COLOR);
		}
		else{
		    application.animType="orient";
		    orientation.setColor(Introduction.ANIM_SELECTED_BUTTON_COLOR);
		    color.setColor(Introduction.ANIM_BUTTON_COLOR);
		    size.setColor(Introduction.ANIM_BUTTON_COLOR);
		    translation.setColor(Introduction.ANIM_BUTTON_COLOR);
		}
	    }
	    else if (t.equals("size")){
		if (application.animType.equals("size")){
		    application.animType="";
		    size.setColor(Introduction.ANIM_BUTTON_COLOR);
		}
		else{
		    application.animType="size";
		    orientation.setColor(Introduction.ANIM_BUTTON_COLOR);
		    color.setColor(Introduction.ANIM_BUTTON_COLOR);
		    size.setColor(Introduction.ANIM_SELECTED_BUTTON_COLOR);
		    translation.setColor(Introduction.ANIM_BUTTON_COLOR);
		}
	    }
	    else if (t.equals("col")){
		if (application.animType.equals("col")){
		    application.animType="";
		    a.setColor(Introduction.ANIM_BUTTON_COLOR);
		}
		else{
		    application.animType="col";
		    orientation.setColor(Introduction.ANIM_BUTTON_COLOR);
		    color.setColor(Introduction.ANIM_SELECTED_BUTTON_COLOR);
		    size.setColor(Introduction.ANIM_BUTTON_COLOR);
		    translation.setColor(Introduction.ANIM_BUTTON_COLOR);
		}
	    }
	    else if (t.equals("pos")){
		if (application.animType.equals("trans")){
		    application.animType="";
		    a.setColor(Introduction.ANIM_BUTTON_COLOR);
		}
		else{
		    application.animType="trans";
		    orientation.setColor(Introduction.ANIM_BUTTON_COLOR);
		    color.setColor(Introduction.ANIM_BUTTON_COLOR);
		    size.setColor(Introduction.ANIM_BUTTON_COLOR);
		    translation.setColor(Introduction.ANIM_SELECTED_BUTTON_COLOR);
		}
	    }
	    else if (t.equals("lin")){
		if (application.animScheme.equals("lin")){
		    application.animScheme="";
		    linear.setColor(Introduction.ANIM_BUTTON_COLOR);
		}
		else{
		    application.animScheme="lin";
		    exponential.setColor(Introduction.ANIM_BUTTON_COLOR);
		    sigmoid.setColor(Introduction.ANIM_BUTTON_COLOR);
		    linear.setColor(Introduction.ANIM_SELECTED_BUTTON_COLOR);
		}
	    }
	    else if (t.equals("exp")){
		if (application.animScheme.equals("exp")){
		    application.animScheme="";
		    exponential.setColor(Introduction.ANIM_BUTTON_COLOR);
		}
		else{
		    application.animScheme="exp";
		    linear.setColor(Introduction.ANIM_BUTTON_COLOR);
		    sigmoid.setColor(Introduction.ANIM_BUTTON_COLOR);
		    exponential.setColor(Introduction.ANIM_SELECTED_BUTTON_COLOR);
		}
	    }
	    else if (t.equals("sig")){
		if (application.animScheme.equals("sig")){
		    application.animScheme="";
		    sigmoid.setColor(Introduction.ANIM_BUTTON_COLOR);
		}
		else{
		    application.animScheme="sig";
		    exponential.setColor(Introduction.ANIM_BUTTON_COLOR);
		    linear.setColor(Introduction.ANIM_BUTTON_COLOR);
		    sigmoid.setColor(Introduction.ANIM_SELECTED_BUTTON_COLOR);
		}
	    }
	    application.vsm.repaint();
	}
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	lastJPX=jpx;
	lastJPY=jpy;
	//application.vsm.setActiveCamera(v.cams[0]);
	v.setDrawDrag(true);
	application.vsm.getActiveView().mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
	activeCam=v.cams[0];
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	v.cams[0].setXspeed(0);
	v.cams[0].setYspeed(0);
	v.cams[0].setZspeed(0);
	v.setDrawDrag(false);
	application.vsm.getActiveView().mouse.setSensitivity(true);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
	    tfactor=(activeCam.focal+Math.abs(activeCam.altitude))/activeCam.focal;
	    if (mod == META_SHIFT_MOD) {
		v.cams[0].setXspeed(0);
		v.cams[0].setYspeed(0);
 		v.cams[0].setZspeed((activeCam.altitude>0) ? (lastJPY-jpy)*(tfactor/cfactor) : (lastJPY-jpy)/(tfactor*cfactor));  //50 is just a speed factor (too fast otherwise)
	    }
	    else {
		v.cams[0].setXspeed((activeCam.altitude>0) ? (jpx-lastJPX)*(tfactor/cfactor) : (jpx-lastJPX)/(tfactor*cfactor));
		v.cams[0].setYspeed((activeCam.altitude>0) ? (lastJPY-jpy)*(tfactor/cfactor) : (lastJPY-jpy)/(tfactor*cfactor));
		v.cams[0].setZspeed(0);
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e){
	Camera c = application.vsm.getActiveCamera();
	double a = (c.focal+Math.abs(c.altitude)) / c.focal;
	if (wheelDirection == WHEEL_UP){
	    c.altitudeOffset(-a*5);
	    application.vsm.repaint();
	}
	else {//wheelDirection == WHEEL_DOWN
	    c.altitudeOffset(a*5);
	    application.vsm.repaint();
	}
    }

    public void enterGlyph(Glyph g){
	g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
	g.highlight(false, null);
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){
	switch(c){
	case 'c':{
	    application.demoView.getGlobalView(application.vsm.getActiveCamera(),200);
	    break;
	}
	}
    }

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

    public String toString(){return "CameraDemoEvtHdlr";}

}

class CameraDemoEvtHdlr implements ViewListener {

    Introduction application;

    long lastX,lastY,lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    double tfactor;
    float cfactor=40.0f;

    Camera activeCam;

    VSegment navSeg;

    CameraDemoEvtHdlr(Introduction appli){
	application=appli;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	try {
	    Glyph g=v.lastGlyphEntered();
	    if (g!=null){
		    v.getVCursor().stickGlyph(g);
		    application.vsm.getVirtualSpace("vs1").onTop(g);
	    }
	}
	catch (NullPointerException ex){}
	application.vsm.getActiveView().mouse.setSensitivity(false);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.getActiveView().mouse.setSensitivity(true);
	v.getVCursor().unstickLastGlyph();
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	lastJPX=jpx;
	lastJPY=jpy;
	//application.vsm.setActiveCamera(v.cams[0]);
	v.setDrawSegment(true);
	application.vsm.getActiveView().mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
	activeCam=v.cams[0];
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	v.cams[0].setXspeed(0);
	v.cams[0].setYspeed(0);
	v.cams[0].setZspeed(0);
	v.setDrawSegment(false);
	application.vsm.getActiveView().mouse.setSensitivity(true);
	if (autoZoomed){
	    Animation anim = application.vsm.getAnimationManager().getAnimationFactory()
		.createCameraAltAnim(300, v.cams[0], -2*v.cams[0].getAltitude()/3.0f, true,
				     IdentityInterpolator.getInstance(), null);
	    application.vsm.getAnimationManager().startAnimation(anim, true);
	    
	    autoZoomed = false;
	}
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    double drag;
    boolean autoZoomed = false;

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
	    tfactor=(activeCam.focal+Math.abs(activeCam.altitude))/activeCam.focal;
	    if (mod == META_SHIFT_MOD) {
		v.cams[0].setXspeed(0);
		v.cams[0].setYspeed(0);
 		v.cams[0].setZspeed((activeCam.altitude>0) ? (lastJPY-jpy)*(tfactor/cfactor) : (lastJPY-jpy)/(tfactor*cfactor));  //50 is just a speed factor (too fast otherwise)
	    }
	    else {
		v.cams[0].setXspeed((activeCam.altitude>0) ? (jpx-lastJPX)*(tfactor/cfactor) : (jpx-lastJPX)/(tfactor*cfactor));
		v.cams[0].setYspeed((activeCam.altitude>0) ? (lastJPY-jpy)*(tfactor/cfactor) : (lastJPY-jpy)/(tfactor*cfactor));
		v.cams[0].setZspeed(0);
		if (application.isAutoZoomEnabled()){
		    drag = Math.sqrt(Math.pow(jpx-lastJPX, 2) + Math.pow(jpy-lastJPY, 2));
		    if (!autoZoomed && drag > 300.0f){
			autoZoomed = true;
		
			Animation anim = application.vsm.getAnimationManager().getAnimationFactory()
			    .createCameraAltAnim(300, v.cams[0], 2*v.cams[0].getAltitude(), true,
						 IdentityInterpolator.getInstance(), null);
			application.vsm.getAnimationManager().startAnimation(anim, true);
		    }
		}
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e){
	Camera c = application.vsm.getActiveCamera();
	double a = (c.focal+Math.abs(c.altitude)) / c.focal;
	if (wheelDirection == WHEEL_UP){
	    c.altitudeOffset(-a*5);
	    application.vsm.repaint();
	}
	else {//wheelDirection == WHEEL_DOWN
	    c.altitudeOffset(a*5);
	    application.vsm.repaint();
	}
    }

    public void enterGlyph(Glyph g){
	g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
	g.highlight(false, null);
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){
	switch(c){
	case 'c':{
	    application.demoView.getGlobalView(application.vsm.getActiveCamera(),200);
	    break;
	}
	case 'f':{
	    fr.inria.zvtm.glyphs.GlyphFactory.getGlyphFactoryDialog((java.awt.Frame)v.parent.getFrame());
	    break;
	}
	}
    }

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

    public String toString(){return "CameraDemoEvtHdlr";}

}

class MultiLayerEvtHdlr implements ViewListener {

    Introduction application;

    double lastX,lastY;
    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    double tfactor;
    float cfactor=40.0f;

    VSegment navSeg;

    Camera activeCam;

    int currentLayer=0;

    MultiLayerEvtHdlr(Introduction appli){
	application=appli;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	try {v.getVCursor().stickGlyph(v.lastGlyphEntered());application.vsm.getVirtualSpace("vs1").onTop(v.lastGlyphEntered());}
	catch (NullPointerException ex){}
	application.vsm.getActiveView().mouse.setSensitivity(false);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	application.vsm.getActiveView().mouse.setSensitivity(true);
	v.getVCursor().unstickLastGlyph();
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){if (application.vsm.getActiveView().getActiveLayer()==0){application.vsm.getActiveView().setActiveLayer(1);} else {application.vsm.getActiveView().setActiveLayer(0);}}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	lastJPX=jpx;
	lastJPY=jpy;
	//application.vsm.setActiveCamera(v.cams[0]);
	v.setDrawDrag(true);
	application.vsm.getActiveView().mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
	activeCam=application.vsm.getActiveCamera();
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        application.vsm.getActiveCamera().setXspeed(0);
        application.vsm.getActiveCamera().setYspeed(0);
        application.vsm.getActiveCamera().setZspeed(0);
        v.setDrawDrag(false);
        application.vsm.getActiveView().mouse.setSensitivity(true);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        //activeCam has been initialized in press3()
        if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
            tfactor=(activeCam.focal+Math.abs(activeCam.altitude))/activeCam.focal;
            if (mod == META_SHIFT_MOD) {
                application.vsm.getActiveCamera().setXspeed(0);
                application.vsm.getActiveCamera().setYspeed(0);
                application.vsm.getActiveCamera().setZspeed((activeCam.altitude>0) ? (lastJPY-jpy)*(tfactor/cfactor) : (lastJPY-jpy)/(tfactor*cfactor));
                //50 is just a speed factor (too fast otherwise)
            }
            else {
                application.vsm.getActiveCamera().setXspeed((activeCam.altitude>0) ? (jpx-lastJPX)*(tfactor/cfactor) : (jpx-lastJPX)/(tfactor*cfactor));
                application.vsm.getActiveCamera().setYspeed((activeCam.altitude>0) ? (lastJPY-jpy)*(tfactor/cfactor) : (lastJPY-jpy)/(tfactor*cfactor));
                application.vsm.getActiveCamera().setZspeed(0);
            }
        }
    }

    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e){
	Camera c = application.vsm.getActiveCamera();
	double a = (c.focal+Math.abs(c.altitude)) / c.focal;
	if (wheelDirection == WHEEL_UP){
	    c.altitudeOffset(-a*5);
	    application.vsm.repaint();
	}
	else {//wheelDirection == WHEEL_DOWN
	    c.altitudeOffset(a*5);
	    application.vsm.repaint();
	}
    }

    public void enterGlyph(Glyph g){
	g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
	g.highlight(false, null);
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){
	switch(c){
	case 's':{
	    if (currentLayer==0){
		currentLayer=1;
	    }
	    else {
		currentLayer=0;
	    }
	    v.parent.setActiveLayer(currentLayer);
	    break;
	}
	}
    }

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}
    
    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

    public String toString(){return "MultiLayerEvtHdlr";}

}
