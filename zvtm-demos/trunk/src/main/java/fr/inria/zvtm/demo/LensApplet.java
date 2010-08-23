/*   FILE: LensApplet.java
 *   DATE OF CREATION:  Tue Nov 16 15:39:23 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package fr.inria.zvtm.demo;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import java.util.Vector;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.lens.FixedSizeLens;
import fr.inria.zvtm.lens.FSLinearLens;
import fr.inria.zvtm.lens.Lens;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.engine.ViewEventHandler;


public class LensApplet extends JApplet {

    static VirtualSpaceManager vsm;
    static LensAppletEvtHdlr evt;

    static Color backgroundColor = new Color(221, 221, 221);

    int viewWidth=400;
    int viewHeight=400;

    static String demoVS="demovs";
    static String zvtmView="Lens Applet Demo";
    
    VirtualSpace vs;
    View view;

    Lens lens;

    JCheckBox lensCB;
    
    public LensApplet() {
        getRootPane().putClientProperty("defeatSystemEventQueueCheck",Boolean.TRUE);
    }

    public void init() {
	Container cp = getContentPane();
	try {
	    int w=Integer.parseInt(getParameter("width"));
	    int h=Integer.parseInt(getParameter("height"));
	    if (w>0){viewWidth=w;}
	    if (h>0){viewHeight=h;}
	}
	catch (Exception ex){}
	cp.setBackground(backgroundColor);
	cp.setLayout(new FlowLayout());
	((JPanel)cp).setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black,2)," Lens demo "));
	vsm = VirtualSpaceManager.INSTANCE;
	vs = vsm.addVirtualSpace(demoVS);
	vs.addCamera();
	vsm.getVirtualSpace(demoVS).getCamera(0).setZoomFloor(0);
	Vector cams=new Vector();
	cams.add(vsm.getVirtualSpace(demoVS).getCamera(0));
	this.setSize(viewWidth-10,viewHeight-10);
	cp.setSize(viewWidth,viewHeight);
	JPanel zvtmV=vsm.addPanelView(cams,zvtmView,viewWidth-10,viewHeight-10);
 	zvtmV.setPreferredSize(new Dimension(viewWidth-10,viewHeight-80));
	view = vsm.getView(zvtmView);
	evt = new LensAppletEvtHdlr(this);
	view.setEventHandler(evt);
	view.setBackgroundColor(backgroundColor);
	cp.add(zvtmV);
	ActionListener a0 = new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    if (lensCB.isSelected()){
			LensApplet.this.view.repaintNow();
			LensApplet.this.lens = view.setLens(new FSLinearLens(1.0f,100,20));
			LensApplet.this.lens.setBufferThreshold(1.5f);
			
			Animation lensAnim = vsm.getAnimationManager().getAnimationFactory()
			    .createLensMagAnim(1000, (FixedSizeLens)LensApplet.this.lens, 
					       1f, true, 
					       IdentityInterpolator.getInstance(), null);
			vsm.getAnimationManager().startAnimation(lensAnim, true);
		    }
		    else {
			//XXX remove lens from view? (view.setLens(null))
			Animation lensAnim = vsm.getAnimationManager().getAnimationFactory()
			    .createLensMagAnim(1000, (FixedSizeLens)LensApplet.this.lens, 
					       -1f, true, 
					       IdentityInterpolator.getInstance(), null);
			vsm.getAnimationManager().startAnimation(lensAnim, true);

			LensApplet.this.lens = null;
		    }
		}
	    };
	lensCB = new JCheckBox("Linear lens");
	lensCB.setBackground(backgroundColor);
	lensCB.addActionListener(a0);
	cp.add(lensCB);
	for (int i=-200;i<=200;i+=40){
	    VSegment s = new VSegment(i,0,0,0,200,Color.black);
	    VSegment s2 = new VSegment(0,i,0,200,0,Color.black);
	    vs.addGlyph(s);
	    vs.addGlyph(s2);
	}
	VImage i1=new VImage(0,0,0,(new ImageIcon(this.getClass().getResource("/images/logo-futurs-small.png"))).getImage());
	i1.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	vs.addGlyph(i1);
	vsm.repaintNow();
	view.getGlobalView(vs.getCamera(0),500);
	vsm.repaintNow();
    }

}

class LensAppletEvtHdlr implements ViewEventHandler {

    LensApplet application;

    long lastX,lastY,lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    float tfactor;
    float cfactor=50.0f;
    long x1,y1,x2,y2;                     //remember last mouse coords to display selection rectangle (dragging)

    VSegment navSeg;

    Camera activeCam;
    
    boolean lensActivated = false;

    LensAppletEvtHdlr(LensApplet app){
	this.application=app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	lastJPX=jpx;
	lastJPY=jpy;
	v.setDrawDrag(true);
	LensApplet.vsm.activeView.mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
	activeCam=LensApplet.vsm.getActiveCamera();
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	LensApplet.vsm.getAnimationManager().setXspeed(0);
	LensApplet.vsm.getAnimationManager().setYspeed(0);
	LensApplet.vsm.getAnimationManager().setZspeed(0);
	v.setDrawDrag(false);
	LensApplet.vsm.activeView.mouse.setSensitivity(true);
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	lastJPX=jpx;
	lastJPY=jpy;
	v.setDrawDrag(true);
	LensApplet.vsm.activeView.mouse.setSensitivity(false);  //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
	activeCam=LensApplet.vsm.getActiveCamera();
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	LensApplet.vsm.getAnimationManager().setXspeed(0);
	LensApplet.vsm.getAnimationManager().setYspeed(0);
	LensApplet.vsm.getAnimationManager().setZspeed(0);
	v.setDrawDrag(false);
	LensApplet.vsm.activeView.mouse.setSensitivity(true);
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
	if (buttonNumber==1 || buttonNumber==3){
	    Camera c=application.vsm.getActiveCamera();
	    double a=(c.focal+Math.abs(c.altitude))/c.focal;
	    if (mod == SHIFT_MOD || mod == META_SHIFT_MOD) {
		application.vsm.getAnimationManager().setXspeed(0);
		application.vsm.getAnimationManager().setYspeed(0);
 		application.vsm.getAnimationManager().setZspeed((c.altitude>0) ? (lastJPY-jpy)*(a/50.0f) : (lastJPY-jpy)/(a*50));  //50 is just a speed factor (too fast otherwise)
	    }
	    else {
		application.vsm.getAnimationManager().setXspeed((c.altitude>0) ? (jpx-lastJPX)*(a/50.0f) : (jpx-lastJPX)/(a*50));
		application.vsm.getAnimationManager().setYspeed((c.altitude>0) ? (lastJPY-jpy)*(a/50.0f) : (lastJPY-jpy)/(a*50));
		application.vsm.getAnimationManager().setZspeed(0);
	    }
	}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	Camera c=application.vsm.getActiveCamera();
	double a=(c.focal+Math.abs(c.altitude))/c.focal;
	if (wheelDirection == WHEEL_UP){
	    c.altitudeOffset(-a*5);
	    application.vsm.repaintNow();
	}
	else {//wheelDirection == WHEEL_DOWN
	    c.altitudeOffset(a*5);
	    application.vsm.repaintNow();
	}
    }

    public void enterGlyph(Glyph g){
	g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
	g.highlight(false, null);
    }

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	System.err.println(c);
	switch(c){
	case 'l':{
	    int[] b = new int[2];
	    if (lensActivated){
		lensActivated = false;
		b[0] = -20; b[1] = -20;
		
		Animation animLens = application.vsm.getAnimationManager().getAnimationFactory()
		    .createLensRadiusAnim(1000, (FixedSizeLens)application.lens, -20, -20, true,
					  IdentityInterpolator.getInstance(), null);
		application.vsm.getAnimationManager().startAnimation(animLens, true);
	    }
	    else {
		lensActivated = true;
		b[0] = 20; b[1] = 20;
		
		Animation animLens = application.vsm.getAnimationManager().getAnimationFactory()
		    .createLensRadiusAnim(1000, (FixedSizeLens)application.lens, 20, 20, true,
					  IdentityInterpolator.getInstance(), null);
		application.vsm.getAnimationManager().startAnimation(animLens, true);
	    }
	    break;
	}
	}
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){}

}
