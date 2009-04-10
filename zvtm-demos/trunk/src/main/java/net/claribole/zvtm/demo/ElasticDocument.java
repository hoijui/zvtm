/*   FILE: ElasticDocument.java
 *   DATE OF CREATION:  Sat Jun 10 18:06:19 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.demo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.text.Style;

import net.claribole.zvtm.animation.Animation;
import net.claribole.zvtm.animation.EndAction;
import net.claribole.zvtm.animation.interpolation.IdentityInterpolator;
import net.claribole.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import net.claribole.zvtm.engine.Java2DPainter;
import net.claribole.zvtm.engine.Location;
import net.claribole.zvtm.lens.FixedSizeLens;
import net.claribole.zvtm.lens.FSGaussianLens;
import net.claribole.zvtm.lens.FSInverseCosineLens;
import net.claribole.zvtm.lens.FSLinearLens;
import net.claribole.zvtm.lens.FSManhattanLens;
import net.claribole.zvtm.lens.FSScramblingLens;
import net.claribole.zvtm.lens.L1FSInverseCosineLens;
import net.claribole.zvtm.lens.L1FSLinearLens;
import net.claribole.zvtm.lens.L1FSManhattanLens;
import net.claribole.zvtm.lens.LInfFSInverseCosineLens;
import net.claribole.zvtm.lens.LInfFSLinearLens;
import net.claribole.zvtm.lens.LInfFSManhattanLens;
import net.claribole.zvtm.lens.Lens;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.VImage;

public class ElasticDocument implements Java2DPainter {

    /* screen dimensions */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;

    /* actual dimensions of windows on screen */
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;

    static boolean SHOW_MEMORY_USAGE = false;

    /*translation constants*/
    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;
    static int ANIM_MOVE_LENGTH = 300;

    /*dimensions of zoomable panel*/
    int panelWidth, panelHeight;

    /* ZVTM */
    VirtualSpaceManager vsm;

    /* main view event handler */
    ElasticDocumentEventHandler eh;

    /* main view*/
    View demoView;
    Camera demoCamera;
    VirtualSpace mainVS;
    static String mainVSname = "mainSpace";

    /* misc. lens settings */
    Lens lens;
    static int LENS_R1 = 100;
    static int LENS_R2 = 50;
    static final int WHEEL_ANIM_TIME = 50;
    static final int LENS_ANIM_TIME = 300;
    static float DEFAULT_MAG_FACTOR = 4f;
    static float MAG_FACTOR = DEFAULT_MAG_FACTOR;
    static float INV_MAG_FACTOR = 1/MAG_FACTOR;

    /* lens distance and drop-off functions */
    static final short L1_Linear = 0;
    static final short L1_InverseCosine = 1;
    static final short L1_Manhattan = 2;
    static final short L2_Gaussian = 3;
    static final short L2_Linear = 4;
    static final short L2_InverseCosine = 5;
    static final short L2_Manhattan = 6;
    static final short L2_Scrambling = 7;
    static final short LInf_Linear = 8;
    static final short LInf_InverseCosine = 9;
    static final short LInf_Manhattan = 10;
    short lensFamily = L2_Gaussian;
    static final String View_Title_Prefix = "Probing Lens Demo - ";
    static final String L1_Linear_Title = View_Title_Prefix + "L1 / Linear";
    static final String L1_InverseCosine_Title = View_Title_Prefix + "L1 / Inverse Cosine";
    static final String L1_Manhattan_Title = View_Title_Prefix + "L1 / Manhattan";
    static final String L2_Gaussian_Title = View_Title_Prefix + "L2 / Gaussian";
    static final String L2_Linear_Title = View_Title_Prefix + "L2 / Linear";
    static final String L2_InverseCosine_Title = View_Title_Prefix + "L2 / Inverse Cosine";
    static final String L2_Manhattan_Title = View_Title_Prefix + "L2 / Manhattan";
    static final String L2_Scrambling_Title = View_Title_Prefix + "L2 / Scrambling (for fun)";
    static final String LInf_Linear_Title = View_Title_Prefix + "LInf / Linear";
    static final String LInf_InverseCosine_Title = View_Title_Prefix + "LInf / Inverse Cosine";
    static final String LInf_Manhattan_Title = View_Title_Prefix + "LInf / Manhattan";

    /* LENS MAGNIFICATION */
    static float WHEEL_MM_STEP = 1.0f;
    static final float MAX_MAG_FACTOR = 12.0f;

    static final Color HCURSOR_COLOR = new Color(200,48,48);

    static final float START_ALTITUDE = 10000;
    static final float FLOOR_ALTITUDE = 100.0f;

    ElasticDocument(){
	vsm = new VirtualSpaceManager();
	init();
    }

    public void init(){
	eh = new ElasticDocumentEventHandler(this);
	windowLayout();
	mainVS = vsm.addVirtualSpace(mainVSname);
	vsm.setZoomLimit(0);
	demoCamera = vsm.addCamera(mainVSname);
	Vector cameras=new Vector();
	cameras.add(demoCamera);
	demoView = vsm.addExternalView(cameras, L2_Gaussian_Title, View.STD_VIEW, VIEW_W, VIEW_H, false, true, true, null);
	demoView.mouse.setHintColor(HCURSOR_COLOR);
	demoView.setLocation(VIEW_X, VIEW_Y);
	updatePanelSize();
	demoView.setEventHandler(eh);
	demoView.getPanel().addComponentListener(eh);
	demoView.setNotifyMouseMoved(true);
 	demoView.setJava2DPainter(this, Java2DPainter.AFTER_LENSES);
	demoCamera.setAltitude(START_ALTITUDE);
	loadDocument();
	System.gc();
    }

    void windowLayout(){
	if (Utilities.osIsWindows()){
	    VIEW_X = VIEW_Y = 0;
	    SCREEN_HEIGHT -= 30;
	}
	else if (Utilities.osIsMacOS()){
	    VIEW_X = 80;
	    SCREEN_WIDTH -= 80;
	}
	VIEW_W = SCREEN_WIDTH;
	VIEW_H = SCREEN_HEIGHT;
    }

    VImage page1,page2,page3,page4,page5;
    static final long PAGE1_X = 0;
    static final long PAGE1_Y = 0;
    static final String PAGE1_PATH = "images/pdf/p1.png";
    static final long PAGE2_X = 0;
    static final long PAGE2_Y = -50000;
    static final String PAGE2_PATH = "images/pdf/p2.png";
    static final long PAGE3_X = 0;
    static final long PAGE3_Y = -100000;
    static final String PAGE3_PATH = "images/pdf/p3.png";
    static final long PAGE4_X = 0;
    static final long PAGE4_Y = -150000;
    static final String PAGE4_PATH = "images/pdf/p4.png";
    static final long PAGE5_X = 0;
    static final long PAGE5_Y = -200000;
    static final String PAGE5_PATH = "images/pdf/p5.png";
    static final double PAGE_SCALE = 8.0;

    void loadDocument(){
	page1 = new VImage(PAGE1_X, PAGE1_Y, 0,
			   (new ImageIcon(PAGE1_PATH)).getImage(),
			   PAGE_SCALE);
	page1.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	vsm.addGlyph(page1, mainVSname);

	page2 = new VImage(PAGE2_X, PAGE2_Y, 0,
			   (new ImageIcon(PAGE2_PATH)).getImage(),
			   PAGE_SCALE);
	page2.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	vsm.addGlyph(page2, mainVSname);

	page3 = new VImage(PAGE3_X, PAGE3_Y, 0,
			   (new ImageIcon(PAGE3_PATH)).getImage(),
			   PAGE_SCALE);
	page3.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	vsm.addGlyph(page3, mainVSname);

	page4 = new VImage(PAGE4_X, PAGE4_Y, 0,
			   (new ImageIcon(PAGE4_PATH)).getImage(),
			   PAGE_SCALE);
	page4.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	vsm.addGlyph(page4, mainVSname);

	page5 = new VImage(PAGE5_X, PAGE5_Y, 0,
			   (new ImageIcon(PAGE5_PATH)).getImage(),
			   PAGE_SCALE);
	page5.setDrawBorderPolicy(VImage.DRAW_BORDER_NEVER);
	vsm.addGlyph(page5, mainVSname);
    }

    void setLens(int t){
	eh.lensType = t;
    }

    void moveLens(int x, int y, boolean write){
	lens.setAbsolutePosition(x, y);
	vsm.repaintNow();
    }

    void zoomInPhase1(int x, int y){
	// create lens if it does not exist
	if (lens == null){
 	    lens = demoView.setLens(getLensDefinition(x, y));
	    lens.setBufferThreshold(1.5f);
	}

	Animation lensAnim = vsm.getAnimationManager().getAnimationFactory()
	    .createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens, MAG_FACTOR-1, true, 
			       IdentityInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(lensAnim, true);

	setLens(ElasticDocumentEventHandler.ZOOMIN_LENS);
    }
    
    void zoomInPhase2(long mx, long my){
	// compute camera animation parameters
	float cameraAbsAlt = demoCamera.getAltitude()+demoCamera.getFocal();
	long c2x = Math.round(mx - INV_MAG_FACTOR * (mx - demoCamera.posx));
	long c2y = Math.round(my - INV_MAG_FACTOR * (my - demoCamera.posy));
	// -(cameraAbsAlt)*(MAG_FACTOR-1)/MAG_FACTOR
	float deltAlt = (cameraAbsAlt)*(1-MAG_FACTOR)/MAG_FACTOR;
	if (cameraAbsAlt + deltAlt > FLOOR_ALTITUDE){
	    Animation lensAnim = vsm.getAnimationManager().getAnimationFactory()
		.createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens, -MAG_FACTOR+1,
				   true, IdentityInterpolator.getInstance(),
				   new Zp2LensAction()); 
	    Animation camAltAnim = vsm.getAnimationManager().getAnimationFactory()
		.createCameraAltAnim(LENS_ANIM_TIME, demoCamera, cameraAbsAlt, false,
				     IdentityInterpolator.getInstance(), null);
	    Animation camTransAnim = vsm.getAnimationManager().getAnimationFactory()
		.createCameraTranslation(LENS_ANIM_TIME, demoCamera, new LongPoint(c2x, c2y), false,
					 IdentityInterpolator.getInstance(), null);

	    vsm.getAnimationManager().startAnimation(lensAnim, true);
	    vsm.getAnimationManager().startAnimation(camAltAnim, true);
	    vsm.getAnimationManager().startAnimation(camTransAnim, true);
	}
	else {
	    float actualDeltAlt = FLOOR_ALTITUDE - cameraAbsAlt;
	    double ratio = actualDeltAlt / deltAlt;
// 	    cadata.add(actualDeltAlt);
// 	    cadata.add(new LongPoint(Math.round((c2x-demoCamera.posx)*ratio),
// 				     Math.round((c2y-demoCamera.posy)*ratio)));

	    Animation lensAnim = vsm.getAnimationManager().getAnimationFactory()
		.createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens, -MAG_FACTOR+1,
				   true, IdentityInterpolator.getInstance(),
				   new Zp2LensAction()); 
	    Animation camAltAnim = vsm.getAnimationManager().getAnimationFactory()
		.createCameraAltAnim(LENS_ANIM_TIME, demoCamera, actualDeltAlt, true,
				     IdentityInterpolator.getInstance(), null);
	    Animation camTransAnim = vsm.getAnimationManager().getAnimationFactory()
		.createCameraTranslation(LENS_ANIM_TIME, demoCamera, new LongPoint(c2x, c2y), false,
					 IdentityInterpolator.getInstance(), null);

	    vsm.getAnimationManager().startAnimation(lensAnim, true);
	    vsm.getAnimationManager().startAnimation(camAltAnim, true);
	    vsm.getAnimationManager().startAnimation(camTransAnim, true);
	}
    }

    void zoomOutPhase1(int x, int y, long mx, long my){
	// compute camera animation parameters
	float cameraAbsAlt = demoCamera.getAltitude()+demoCamera.getFocal();
	long c2x = Math.round(mx - MAG_FACTOR * (mx - demoCamera.posx));
	long c2y = Math.round(my - MAG_FACTOR * (my - demoCamera.posy));
	
	// create lens if it does not exist
	if (lens == null){
	    lens = demoView.setLens(getLensDefinition(x, y));
	    lens.setBufferThreshold(1.5f);
	}
	// animate lens and camera simultaneously
	Animation lensAnim = vsm.getAnimationManager().getAnimationFactory()
	    .createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens, MAG_FACTOR-1,
			       true, IdentityInterpolator.getInstance(),
				   null);
	Animation camAltAnim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraAltAnim(LENS_ANIM_TIME, demoCamera, cameraAbsAlt*(MAG_FACTOR-1), false,
				 IdentityInterpolator.getInstance(), null);
	Animation camTransAnim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraTranslation(LENS_ANIM_TIME, demoCamera, new LongPoint(c2x, c2y), false,
				     IdentityInterpolator.getInstance(), null);

	vsm.getAnimationManager().startAnimation(lensAnim, true);
	vsm.getAnimationManager().startAnimation(camAltAnim, true);
	vsm.getAnimationManager().startAnimation(camTransAnim, true);

	setLens(ElasticDocumentEventHandler.ZOOMOUT_LENS);
    }

    void zoomOutPhase2(){
	// make lens disappear (killing anim)
	Animation anim = vsm.getAnimationManager().getAnimationFactory()
	    .createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens, -MAG_FACTOR+1,
			       true, IdentityInterpolator.getInstance(),
			       new Zp2LensAction());
	vsm.getAnimationManager().startAnimation(anim, true);
    }

    void setMagFactor(float m){
	MAG_FACTOR = m;
	INV_MAG_FACTOR = 1 / MAG_FACTOR;
    }

    synchronized void magnifyFocus(float magOffset, int zooming, Camera ca){
	synchronized (lens){
	    float nmf = MAG_FACTOR + magOffset;
	    if (nmf <= MAX_MAG_FACTOR && nmf > 1.0f){
		setMagFactor(nmf);
		if (zooming == ElasticDocumentEventHandler.ZOOMOUT_LENS){
		    /* if unzooming, we want to keep the focus point stable, and unzoom the context
		       this means that camera altitude must be adjusted to keep altitude + lens mag
		       factor constant in the lens focus region. The camera must also be translated
		       to keep the same region of the virtual space under the focus region */
		    float a1 = demoCamera.getAltitude();
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
		    demoCamera.altitudeOffset((float)((a1+demoCamera.getFocal())*magOffset/(MAG_FACTOR-magOffset)));
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
		       JPanel coordinates w.r.t the view's center - see Lens.java)
		    */
		    demoCamera.move(Math.round((a1-demoCamera.getAltitude())/demoCamera.getFocal()*lens.lx),
				    -Math.round((a1-demoCamera.getAltitude())/demoCamera.getFocal()*lens.ly));
		}
		else {
		    Animation anim = vsm.getAnimationManager().getAnimationFactory()
			.createLensMagAnim(WHEEL_ANIM_TIME, (FixedSizeLens)lens, magOffset,
					   true, IdentityInterpolator.getInstance(),
					   null);
		    vsm.getAnimationManager().startAnimation(anim, true);
		}
	    }
	}
    }

    Lens getLensDefinition(int x, int y){
	Lens res = null;
	switch (lensFamily){
	case L1_Linear:{res = new L1FSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);break;}
	case L1_InverseCosine:{res = new L1FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);break;}
	case L1_Manhattan:{res = new L1FSManhattanLens(1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);break;}
	case L2_Gaussian:{res = new FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);break;}
	case L2_Linear:{res = new FSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);break;}
	case L2_InverseCosine:{res = new FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);break;}
	case L2_Manhattan:{res = new FSManhattanLens(1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);break;}
	case L2_Scrambling:{res = new FSScramblingLens(1.0f, LENS_R1, 1, x - panelWidth/2, y - panelHeight/2);break;}
	case LInf_Linear:{res = new LInfFSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);break;}
	case LInf_InverseCosine:{res = new LInfFSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);break;}
	case LInf_Manhattan:{res = new LInfFSManhattanLens(1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);break;}
	}
	return res;
    }

    void getGlobalView(){
	Location l=vsm.getGlobalView(demoCamera,ANIM_MOVE_LENGTH);
    }

    /*higher view */
    void getHigherView(){
	Camera c=demoView.getCameraNumber(0);
	float alt=c.getAltitude()+c.getFocal();

	Animation anim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraAltAnim(ANIM_MOVE_LENGTH, c, alt,
			       true, SlowInSlowOutInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(anim, true);
    }

    /*lower view */
    void getLowerView(){
	Camera c=demoView.getCameraNumber(0);
	float alt=-(c.getAltitude()+c.getFocal())/2.0f;

	Animation anim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraAltAnim(ANIM_MOVE_LENGTH, c, alt,
			       true, SlowInSlowOutInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(anim, true);
    }

    /*direction should be one of ZGRViewer.MOVE_* */
    void translateView(short direction){
	Camera c=demoView.getCameraNumber(0);
	LongPoint trans;
	long[] rb=demoView.getVisibleRegion(c);
	if (direction==MOVE_UP){
	    long qt=Math.round((rb[1]-rb[3])/2.4);
	    trans=new LongPoint(0,qt);
	}
	else if (direction==MOVE_DOWN){
	    long qt=Math.round((rb[3]-rb[1])/2.4);
	    trans=new LongPoint(0,qt);
	}
	else if (direction==MOVE_RIGHT){
	    long qt=Math.round((rb[2]-rb[0])/2.4);
	    trans=new LongPoint(qt,0);
	}
	else {// direction==MOVE_LEFT
	    long qt=Math.round((rb[0]-rb[2])/2.4);
	    trans=new LongPoint(qt,0);
	}
	Animation anim = vsm.getAnimationManager().getAnimationFactory()
	    .createCameraTranslation(ANIM_MOVE_LENGTH, c, trans,
				     true, SlowInSlowOutInterpolator.getInstance(), null);
	vsm.getAnimationManager().startAnimation(anim, true);
    }

    void updatePanelSize(){
	Dimension d = demoView.getPanel().getSize();
	panelWidth = d.width;
	panelHeight = d.height;
    }

    long maxMem = Runtime.getRuntime().maxMemory();
    int totalMemRatio, usedMemRatio;

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	if (SHOW_MEMORY_USAGE){showMemoryUsage(g2d, viewWidth, viewHeight);}
    }

    void showMemoryUsage(Graphics2D g2d, int viewWidth, int viewHeight){
	totalMemRatio = (int)(Runtime.getRuntime().totalMemory() * 100 / maxMem);
	usedMemRatio = (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * 100 / maxMem);
	g2d.setColor(Color.green);
	g2d.fillRect(20,
		     viewHeight - 40,
		     200,
		     15);
	g2d.setColor(Color.orange);
	g2d.fillRect(20,
		     viewHeight - 40,
		     totalMemRatio * 2,
		     15);
	g2d.setColor(Color.red);
	g2d.fillRect(20,
		     viewHeight - 40,
		     usedMemRatio * 2,
		     15);
	g2d.setColor(Color.black);
	g2d.drawRect(20,
		     viewHeight - 40,
		     200,
		     15);
	g2d.drawString(usedMemRatio + "%", 50, viewHeight - 28);
	g2d.drawString(totalMemRatio + "%", 100, viewHeight - 28);
	g2d.drawString(maxMem/1048576 + " Mb", 170, viewHeight - 28);	
    }

    public void writeOnConsole(String s){}

    public void writeOnConsole(String s, Style st){}

    void gc(){
	System.gc();
	if (SHOW_MEMORY_USAGE){vsm.repaintNow();}
    }

    public static void main(String[] args){
	new ElasticDocument();
    }

    class Zp2LensAction implements EndAction{
	public void execute(Object subject,
			    Animation.Dimension dimension){
	    vsm.getOwningView(((Lens)subject).getID()).setLens(null);
    	    ((Lens)subject).dispose();
    	    setMagFactor(ElasticDocument.DEFAULT_MAG_FACTOR);
    	    lens = null;
    	    setLens(ElasticDocumentEventHandler.NO_LENS);
	}
    }
}
