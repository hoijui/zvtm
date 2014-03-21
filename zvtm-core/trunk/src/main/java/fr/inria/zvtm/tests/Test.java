/*
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2011-2012.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.tests;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.DefaultTimingHandler;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.*;
import fr.inria.zvtm.event.ViewAdapter;

import fr.inria.zvtm.glyphs.*;
import fr.inria.zvtm.lens.*;


public class Test implements Java2DPainter {

    /* misc. lens settings */
    FixedSizeLens lens;
    static int LENS_R1 = 200;
    static int LENS_R2 = 50;
    static final int WHEEL_ANIM_TIME = 50;
    static final int LENS_ANIM_TIME = 300;
    static double DEFAULT_MAG_FACTOR = 1f;
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
    short lensFamily = L2_Gaussian;

    static final float FLOOR_ALTITUDE = 100.0f;

	/* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1600;
    static int VIEW_MAX_H = 1200;
    int VIEW_W, VIEW_H;

	static final Font FPS_FONT = new Font("Arial", Font.PLAIN, 12);

	static final float DEFAULT_MAX = 100;
	float MAX = DEFAULT_MAX;

	VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
	VirtualSpace mSpace,bSpace;
	View mView;
	Camera mCamera,bCamera;

	public Test(String vt, float nbObj){
		init(vt);
		MAX = nbObj;
		populate();
        mCamera.setLocation(new Location(0,0,0));
        bCamera.setLocation(new Location(0,0,0));
        mView.setActiveLayer(0);
        mView.setLayerVisibility(new boolean[]{true,false}, new boolean[]{false, true});
	}

    void init(String vt){
        windowLayout();
        mSpace = vsm.addVirtualSpace(VirtualSpace.ANONYMOUS);
        mCamera = mSpace.addCamera();
        bSpace = vsm.addVirtualSpace(VirtualSpace.ANONYMOUS);
        bCamera = bSpace.addCamera();
        Vector cameras = new Vector(2);
        cameras.add(mCamera);
        cameras.add(bCamera);
        System.out.println("Instantiating a regular Java2D view");
        mView = vsm.addFrameView(cameras, View.ANONYMOUS, View.STD_VIEW, VIEW_W, VIEW_H, true);
        mView.setBackgroundColor(Color.WHITE);
        mView.setListener(new MainListener(this), 0);
        mView.setRefreshRate(1);
        mView.setAntialiasing(true);
        mView.setJava2DPainter(this, Java2DPainter.FOREGROUND);
        mCamera.stick(bCamera);
    }

	void windowLayout(){
        VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
        VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }

	void populate(){

	    Glyph[] glyphs = new Glyph[(int)(MAX*MAX)];
        for (int i=0;i<MAX;i++){
            for (int j=0;j<MAX;j++){
                VRectangle r = new VRectangle(i*20,j*20,0,20,20, ((i*MAX+j) % 2 != 0) ? Color.RED : Color.BLUE);
                r.setDrawBorder(false);
                glyphs[(int)(i*MAX+j)] = r;
            }
        }
        mSpace.addGlyphs(glyphs);


        glyphs = new Glyph[(int)(MAX*MAX)];
        for (int i=0;i<MAX;i++){
            for (int j=0;j<MAX;j++){
                VRectangle r = new VRectangle(i*20,j*20,0,20,20, ((i*MAX+j) % 2 != 0) ? Color.WHITE : Color.BLACK);
                r.setDrawBorder(false);
                glyphs[(int)(i*MAX+j)] = r;
            }
        }
        bSpace.addGlyphs(glyphs);
	}

	void startAnim(){
	    double gvAlt = mView.getGlobalView(mCamera).getAltitude();
		animate(gvAlt);
	}

	void animate(final double gvAlt){
	    Animation cameraAlt = vsm.getAnimationManager().getAnimationFactory().createAnimation(
           2000, Animation.INFINITE, Animation.RepeatBehavior.REVERSE, mCamera, Animation.Dimension.ALTITUDE,
           new DefaultTimingHandler(){
               public void timingEvent(float fraction, Object subject, Animation.Dimension dim){
                   Camera c = (Camera)subject;
                   c.setAltitude(2*Double.valueOf(fraction*gvAlt).doubleValue());
               }
           },
           ConstantAccInterpolator.getInstance()
        );
        vsm.getAnimationManager().startAnimation(cameraAlt, false);
    }

    /* -------------- Sigma Lenses ------------------- */

    void moveLens(int x, int y){
        lens.setAbsolutePosition(x, y);
        VirtualSpaceManager.INSTANCE.repaint();
    }

    void zoomInPhase1(int x, int y){
        // create lens if it does not exist
        if (lens == null){
            lens = (FixedSizeLens)mView.setLens(getLensDefinition(x, y));
            lens.setBufferThreshold(1f);
        }
        Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
    }

    void setMagFactor(double m){
        MAG_FACTOR = m;
        INV_MAG_FACTOR = 1 / MAG_FACTOR;
    }

    Lens getLensDefinition(int x, int y){
        // Lens res = new FSGaussianLens(2.0f, LENS_R1, LENS_R2, x - VIEW_W/2, y - VIEW_H/2);
        Lens res = new BGaussianLens(1.0f, 0, 1, LENS_R1, LENS_R2, x - VIEW_W/2, y - VIEW_H/2);
        return res;
    }

    // void decLens(){
    //     Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(500, (FixedSizeLens)lens,
    //         -1, true, IdentityInterpolator.getInstance(), null);
    //     VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
    // }

    // void incLens(){
    //     Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(500, (FixedSizeLens)lens,
    //         1, true, IdentityInterpolator.getInstance(), null);
    //     VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
    // }


    void decLens(){
        BlendingLens bl = (BlendingLens)lens;
        bl.setFocusTranslucencyValue(bl.getFocusTranslucencyValue()-0.05f);
        System.out.println(bl.getFocusTranslucencyValue());
        // Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensRadiusAnim(1000, (FixedSizeLens)lens,
        //     -10, -10, true, IdentityInterpolator.getInstance(), null);
        // VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
    }

    void incLens(){
        BlendingLens bl = (BlendingLens)lens;
        bl.setFocusTranslucencyValue(bl.getFocusTranslucencyValue()+0.05f);
        System.out.println(bl.getFocusTranslucencyValue());
        // Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensRadiusAnim(1000, (FixedSizeLens)lens,
        //     10, 10, true, IdentityInterpolator.getInstance(), null);
        // VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
    }


	public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
	    float rr = 1000 / (float)(mView.getPanel().getDelay());
	    g2d.setColor(Color.BLACK);
	    g2d.setFont(FPS_FONT);
	    g2d.drawString(String.valueOf(rr), 10, 20);
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
        String vt = "";
        float nbObj = DEFAULT_MAX;
        for (String arg:args){
            if (arg.equals("ogl")){vt = arg;}
            else {
                try {
                    nbObj = Integer.parseInt(arg);
                }
                catch (NumberFormatException ex){
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
        }
	    if (vt.equals("ogl")){
	       System.setProperty("sun.java2d.opengl", "True");
	    }
        new Test(vt, nbObj);
    }

}

class MainListener extends ViewAdapter {

	Test application;

	int lastJPX, lastJPY;

	MainListener(Test app){
		this.application = app;
	}

	public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        v.setDrawDrag(true);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        application.mCamera.setXspeed(0);
        application.mCamera.setYspeed(0);
        application.mCamera.setZspeed(0);
        v.setDrawDrag(false);
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        application.zoomInPhase1(jpx, jpy);
    }

	static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        if (application.lens != null){
            application.moveLens(jpx, jpy);
        }
        java.awt.geom.Point2D.Double p = application.mView.fromPanelToVSCoordinates(jpx, jpy, application.mCamera, new java.awt.geom.Point2D.Double());
        System.out.println("---" + p);
        System.out.println(application.mView.fromVSToPanelCoordinates(p.getX(), p.getY(), application.mCamera, new java.awt.Point()));
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (buttonNumber == 1){
            Camera c = application.mCamera;
            double a = (c.focal+Math.abs(c.altitude)) / c.focal;
            if (mod == SHIFT_MOD) {
                application.mCamera.setXspeed(0);
                application.mCamera.setYspeed(0);
                application.mCamera.setZspeed(((lastJPY-jpy)*(ZOOM_SPEED_COEF)));
                //50 is just a speed factor (too fast otherwise)
            }
            else {
                application.mCamera.setXspeed((c.altitude>0) ? ((jpx-lastJPX)*(a/PAN_SPEED_COEF)) : ((jpx-lastJPX)/(a*PAN_SPEED_COEF)));
                application.mCamera.setYspeed((c.altitude>0) ? ((lastJPY-jpy)*(a/PAN_SPEED_COEF)) : ((lastJPY-jpy)/(a*PAN_SPEED_COEF)));
                application.mCamera.setZspeed(0);
            }
        }
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        Camera c = application.mCamera;
        double a = (c.focal+Math.abs(c.altitude)) / c.focal;
        if (wheelDirection == WHEEL_UP){
            c.altitudeOffset(-a*5);
            VirtualSpaceManager.INSTANCE.repaint();
        }
        else {
            //wheelDirection == WHEEL_DOWN
            c.altitudeOffset(a*5);
            VirtualSpaceManager.INSTANCE.repaint();
        }
    }

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code == KeyEvent.VK_Q){application.decLens();}
        if (code == KeyEvent.VK_W){application.incLens();}
    }

    public void enterGlyph(Glyph g){
        g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
        g.highlight(false, null);
    }

    public void viewClosing(View v){
        System.exit(0);
    }

}


class ZP2LensAction implements EndAction {

    Test app;

    ZP2LensAction(Test app){
        this.app = app;
    }

    public void execute(Object subject, Animation.Dimension dimension){
        (((Lens)subject).getOwningView()).setLens(null);
        ((Lens)subject).dispose();
        app.setMagFactor(Test.DEFAULT_MAG_FACTOR);
        app.lens = null;
    }

}
