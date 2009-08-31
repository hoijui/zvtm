/* $Id:$ */

package fr.inria.zvtm.misc;

import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelEvent;
import javax.swing.ImageIcon;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Utilities;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.glyphs.ZPDFPageImg;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.lens.*;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

public class PDFLens implements ComponentListener {
	
    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 800;
    static int VIEW_MAX_H = 450;
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;
	
	static final int NAV_ANIM_DURATION = 300;
	
	static final short DIRECT_G2D_RENDERING = 0;
	static final short OFFSCREEN_IMAGE_RENDERING = 1;
	short rendering_technique = OFFSCREEN_IMAGE_RENDERING;

	VirtualSpace vs;
	static final String spaceName = "pdfSpace";
	ViewEventHandler eh;
	Camera mCamera;

	View pdfView;

	PDFLens(String pdfFilePath, float df){
		VirtualSpaceManager.INSTANCE.setDebug(true);
		initGUI();
		load(new File(pdfFilePath), df);
		
		
		vs.addGlyph(new VRectangle(0, 0, 0, 1, 1, Color.RED));
		
	}
	
	void windowLayout(){
        if (Utilities.osIsWindows()){
            VIEW_X = VIEW_Y = 0;
        }
        else if (Utilities.osIsMacOS()){
            VIEW_X = 80;
            SCREEN_WIDTH -= 80;
        }
        VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
        VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }
    
    /*ComponentListener*/
    public void componentHidden(ComponentEvent e){}
    public void componentMoved(ComponentEvent e){}
    public void componentResized(ComponentEvent e){updatePanelSize();}
    public void componentShown(ComponentEvent e){}
    
    void updatePanelSize(){
        Dimension d = pdfView.getPanel().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
    }

	public void initGUI(){
	    this.windowLayout();
		vs = VirtualSpaceManager.INSTANCE.addVirtualSpace(spaceName);
		mCamera = VirtualSpaceManager.INSTANCE.addCamera(spaceName);
		Vector cameras = new Vector();
		cameras.add(mCamera);
		pdfView = VirtualSpaceManager.INSTANCE.addExternalView(cameras, "High precision lenses on PDF", View.STD_VIEW, VIEW_W, VIEW_H, false, true, false, null);
		pdfView.setBackgroundColor(Color.WHITE);
		pdfView.getPanel().addComponentListener(this);
		eh = new PDFLensEventHandler(this);
		pdfView.setEventHandler(eh);
		pdfView.setAntialiasing(true);
		mCamera.setAltitude(1100);
		VirtualSpaceManager.INSTANCE.repaintNow();
	}

	void load(File f, float detailFactor){
		try {
		    System.out.println("Loading file...");
		    if (f.getName().toLowerCase().endsWith(".pdf")){
    			RandomAccessFile raf = new RandomAccessFile(f, "r");
    			FileChannel channel = raf.getChannel();
    			ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
    			PDFFile pdfFile = new PDFFile(buf);
    			int page_width = (int)pdfFile.getPage(0).getBBox().getWidth();
    			System.out.println("page width "+page_width);
    			for (int i=0;i<pdfFile.getNumPages();i++){
                    try {
                        ZPDFPageImg p = new ZPDFPageImg(i*Math.round(page_width*1.1f*detailFactor), i*Math.round(page_width*1.1f*detailFactor), 0, pdfFile.getPage(i+1), detailFactor, 1);
                        p.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        vs.addGlyph(p);
                    }
                    catch ( Exception e) {
                        e.printStackTrace();
                    }
    			}		        
		    }
		    else {
		        /// 4 5 6
                VImage im = new VImage(0, 0, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1/map5/H/map5_p1.png")).getImage());
                im.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                vs.addGlyph(im);

                im = new VImage(-2000, 0, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1/map4/H/map4_p1.png")).getImage());
                im.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                vs.addGlyph(im);

                im = new VImage(2000, 0, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1/map6/H/map6_p1.png")).getImage());
                im.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                vs.addGlyph(im);
                
                // 1 2 3
                im = new VImage(0, 2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1/map2/H/map2_p1.png")).getImage());
                im.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                vs.addGlyph(im);

                im = new VImage(-2000, 2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1/map1/H/map1_p1.png")).getImage());
                im.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                vs.addGlyph(im);

                im = new VImage(2000, 2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1/map3/H/map3_p1.png")).getImage());
                im.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                vs.addGlyph(im);
                
                // 7 8 9
                im = new VImage(0, -2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1/map8/H/map8_p1.png")).getImage());
                im.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                vs.addGlyph(im);

                im = new VImage(-2000, -2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1/map7/H/map7_p1.png")).getImage());
                im.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                vs.addGlyph(im);

                im = new VImage(2000, -2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1/map9/H/map9_p1.png")).getImage());
                im.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                vs.addGlyph(im);
                
                // A B C
                im = new VImage(-4000, 2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1/mapA/H/mapA_p1.png")).getImage());
                im.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                vs.addGlyph(im);
                
                im = new VImage(-4000, 0, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1/mapB/H/mapB_p1.png")).getImage());
                im.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                vs.addGlyph(im);
                
                im = new VImage(-4000, -2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1/mapC/H/mapC_p1.png")).getImage());
                im.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                vs.addGlyph(im);

                // I J K
                im = new VImage(4000, 2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1/mapK/H/mapK_p1.png")).getImage());
                im.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                vs.addGlyph(im);
                
                im = new VImage(4000, 0, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1/mapJ/H/mapJ_p1.png")).getImage());
                im.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                vs.addGlyph(im);
                
                im = new VImage(4000, -2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1/mapI/H/mapI_p1.png")).getImage());
                im.setInterpolationMethod(RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                vs.addGlyph(im);


		    }
			System.out.println("done");
		}
		catch ( Exception e) { 
			e.printStackTrace();
		}
	}
	
	/* -------------- Lenses ------------------- */
		
    /* misc. lens settings */
    FixedSizeLens lens;
    TemporalLens tLens;
    static int LENS_R1 = 150;
    static int LENS_R2 = 75;
    static final int WHEEL_ANIM_TIME = 50;
    static final int LENS_ANIM_TIME = 300;
    static double DEFAULT_MAG_FACTOR = 12.0;
    static double MAG_FACTOR = DEFAULT_MAG_FACTOR;
    static double INV_MAG_FACTOR = 1/MAG_FACTOR;
    /* LENS MAGNIFICATION */
    static float WHEEL_MM_STEP = 1.0f;
    static final float MAX_MAG_FACTOR = 20.0f;
        
    /* lens distance and drop-off functions */
    static final short L2_Gaussian = 0;
    static final short L2_SCB = 1;
    short visual_behavior = L2_Gaussian;
    
    static final short MP_NONE = 0;
    static final short MP_CONTINUOUS = 1;
    static final short MP_RING = 2;
    static final short MP_SHIFT = 3;
    short motor_precision = MP_CONTINUOUS;
	
	void toggleLensType(){}
	
	void goFocusControl(boolean b){
	    lens.setFocusControlled(b, FixedSizeLens.CONSTANT);
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
    
    void relativeMoveLens(int x, int y, long absTime){
		int lX = lens.lx + panelWidth / 2;
		int lY = lens.ly + panelHeight / 2;

		if (lX == x && lY == y)
			return;

		// hack (disabled if accel = 1)
		int accel = 1;
		int dx = x - lX;
		int dy = y - lY;
		if (dx >= 4)
		    dx = dx*accel;
		if (dy >= 4)
		    dy = dy*accel;

		lens.moveLensBy(dx, dy, absTime);

		VirtualSpaceManager.INSTANCE.repaintNow();
	}
	

    void activateLens(int x, int y){
        if (lens != null){return;}
        lens = (FixedSizeLens)pdfView.setLens(getLensDefinition(x, y));
        lens.setBufferThreshold(1.5f);
        
        Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
            new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
        
        /* motor precison: continuous */
        //lens.setFocusControlled(true, FixedSizeLens.SPEED_DEPENDENT_LINEAR);
        //motor_precision = MP_CONTINUOUS;
        
        /* motor precison: key */
        motor_precision = MP_SHIFT;
        
        
    }

    void setMagFactor(double m){
        MAG_FACTOR = m;
        INV_MAG_FACTOR = 1 / MAG_FACTOR;
        System.out.println(MAG_FACTOR);
    }

    synchronized void magnifyFocus(double magOffset){
        synchronized (lens){
            double nmf = MAG_FACTOR + magOffset;
            if (nmf <= MAX_MAG_FACTOR && nmf > 1.0f){
                setMagFactor(nmf);
                Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(WHEEL_ANIM_TIME, (FixedSizeLens)lens,
                    new Float(magOffset), true, IdentityInterpolator.getInstance(), null);
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
            }
        }
    }

    FixedSizeLens getLensDefinition(int x, int y){
        FixedSizeLens res = null;
        switch (visual_behavior){
            case L2_Gaussian:{
                res = new FSGaussianLens(1f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case L2_SCB:{
                tLens = new SCBLens(1f, 0.0f, 1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);
                ((SCBLens)tLens).setBoundaryColor(Color.RED);
                ((SCBLens)tLens).setObservedRegionColor(Color.RED);
                res = (FixedSizeLens)tLens;
                break;
            }
        }
        return res;
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
		new PDFLens((args.length > 0) ? args[0] : null, (args.length > 1) ? Float.parseFloat(args[1]) : 1);
	}

}

class PDFLensEventHandler implements ViewEventHandler {

	PDFLens application;

	long lastJPX,lastJPY;
	int cjpx,cjpy;

    boolean SHIFT_PRESSED = false;

    boolean cursorNearBorder = false;

	PDFLensEventHandler(PDFLens appli){
		application = appli;
	}

	boolean dragging = false;

	public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	    if (mod == CTRL_MOD){
	        VirtualSpaceManager.INSTANCE.stickToMouse(v.lastGlyphEntered());
	    }
        else {
            lastJPX=jpx;
            lastJPY=jpy;
            v.setDrawDrag(true);
            VirtualSpaceManager.INSTANCE.activeView.mouse.setSensitivity(false);	        
        }
    }

	public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	    VirtualSpaceManager.INSTANCE.unstickFromMouse();
		VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed(0);
        VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed(0);
        VirtualSpaceManager.INSTANCE.getAnimationManager().setZspeed(0);
		v.setDrawDrag(false);
		VirtualSpaceManager.INSTANCE.activeView.mouse.setSensitivity(true);
	}

	public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
		Glyph g = v.lastGlyphEntered();
		if (g != null){
			VirtualSpaceManager.INSTANCE.centerOnGlyph(g, application.mCamera, PDFLens.NAV_ANIM_DURATION);
		}
		else {
			VirtualSpaceManager.INSTANCE.getGlobalView(application.mCamera, PDFLens.NAV_ANIM_DURATION);
		}
	}

	public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){

	}

	public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	}

	public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
	}

	public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	    application.activateLens(jpx, jpy);
	}

	public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	}

	public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
	    if ((jpx-application.LENS_R1/2) < 0){
    	    cjpx = application.LENS_R1/2;
    	}
    	else if ((jpx+application.LENS_R1/2) > application.panelWidth){
    	    cjpx = application.panelWidth - application.LENS_R1/2;
    	}
    	else {
    	    cjpx = jpx;
    	}
    	if ((jpy-application.LENS_R1/2) < 0){
    	    cjpy = application.LENS_R1/2;
    	}
    	else if ((jpy+application.LENS_R1/2) > application.panelHeight){
    	    cjpy = application.panelHeight - application.LENS_R1/2;
    	}
    	else {
    	    cjpy = jpy;
    	}
    	if (application.lens != null){
    	    if (application.motor_precision == PDFLens.MP_CONTINUOUS ||
    	        application.motor_precision == PDFLens.MP_SHIFT && this.SHIFT_PRESSED){
    	        application.relativeMoveLens(jpx, jpy, e.getWhen());
    	    }
    	    else if (application.motor_precision == PDFLens.MP_RING){
    	    }
    	    else {
        	    application.moveLens(jpx, jpy, e.getWhen());
    	    }
    	}
	    
	}

	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
		if (buttonNumber == 1){
			Camera c = VirtualSpaceManager.INSTANCE.getActiveCamera();
			float a = (c.focal+Math.abs(c.altitude))/c.focal;
			if (mod == SHIFT_MOD) {
			    VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed(0);
                VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed(0);
                VirtualSpaceManager.INSTANCE.getAnimationManager().setZspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
				//50 is just a speed factor (too fast otherwise)
			}
			else {
			    VirtualSpaceManager.INSTANCE.getAnimationManager().setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/50.0f)) : (long)((jpx-lastJPX)/(a*50)));
                VirtualSpaceManager.INSTANCE.getAnimationManager().setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
                VirtualSpaceManager.INSTANCE.getAnimationManager().setZspeed(0);
			}
		}
	}

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
	    if (application.lens != null){
	        if (wheelDirection  == ViewEventHandler.WHEEL_UP){
                application.magnifyFocus(PDFLens.WHEEL_MM_STEP);
            }
            else {
                application.magnifyFocus(-PDFLens.WHEEL_MM_STEP);
            }
	    }
	    else {
    		Camera c = VirtualSpaceManager.INSTANCE.getActiveCamera();
    		float a = (c.focal+Math.abs(c.altitude))/c.focal;
    		if (wheelDirection == WHEEL_UP){
    			c.altitudeOffset(-a*5);
    			VirtualSpaceManager.INSTANCE.repaintNow();
    		}
    		else {
    			//wheelDirection == WHEEL_DOWN
    			c.altitudeOffset(a*5);
    			VirtualSpaceManager.INSTANCE.repaintNow();
    		}	        
	    }
	}

	public void enterGlyph(Glyph g){
	}

	public void exitGlyph(Glyph g){
	}

	public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

	public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
	    if (code == KeyEvent.VK_L){
	        application.toggleLensType();
	    }
	    else if (code == KeyEvent.VK_SHIFT){
	        SHIFT_PRESSED = true;
	        if (application.motor_precision == application.MP_SHIFT){application.goFocusControl(true);}
	    }
	}

	public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){
	    if (code == KeyEvent.VK_SHIFT){
	        SHIFT_PRESSED = false;
	        if (application.motor_precision == application.MP_SHIFT){application.goFocusControl(false);}
	    }
	}

	public void viewActivated(View v){}

	public void viewDeactivated(View v){}

	public void viewIconified(View v){}

	public void viewDeiconified(View v){}

	public void viewClosing(View v){System.exit(0);}

}
