/* $Id:$ */

package fr.inria.zvtm.misc;

import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
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
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.SpeedCoupling;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.glyphs.ZPDFPageImg;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.lens.*;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

public class PDFLens implements ComponentListener, Java2DPainter {
	
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

    PDFLens(String pdfFilePath, float df, float mm, short prec_tech, short vb){
	    setMagFactor(mm);
		VirtualSpaceManager.INSTANCE.setDebug(true);
		initGUI();
		load(new File(pdfFilePath), df);
		vs.addGlyph(new VRectangle(0, 0, 0, 1, 1, Color.RED));
		motor_precision = prec_tech;
		visual_behavior = vb;
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
		pdfView.setJava2DPainter(this, AFTER_LENSES);
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
		    else if (f.getName().toLowerCase().endsWith(".png12")){
		        /// 4 5 6
                VImage im = new VImage(0, 0, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1-12/map5/H/map5_p1.png")).getImage(), 12.4);
                vs.addGlyph(im);

                im = new VImage(-2000, 0, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1-12/map4/H/map4_p1.png")).getImage(), 12.4);
                vs.addGlyph(im);

                im = new VImage(2000, 0, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1-12/map6/H/map6_p1.png")).getImage(), 12.4);
                vs.addGlyph(im);
                
                // 1 2 3
                im = new VImage(0, 2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1-12/map2/H/map2_p1.png")).getImage(), 12.4);
                vs.addGlyph(im);

                im = new VImage(-2000, 2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1-12/map1/H/map1_p1.png")).getImage(), 12.4);
                vs.addGlyph(im);

                im = new VImage(2000, 2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1-12/map3/H/map3_p1.png")).getImage(), 12.4);
                vs.addGlyph(im);
                
                // 7 8 9
                im = new VImage(0, -2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1-12/map8/H/map8_p1.png")).getImage(), 12.4);
                vs.addGlyph(im);

                im = new VImage(-2000, -2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1-12/map7/H/map7_p1.png")).getImage(), 12.4);
                vs.addGlyph(im);

                im = new VImage(2000, -2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1-12/map9/H/map9_p1.png")).getImage(), 12.4);
                vs.addGlyph(im);
                
                // A B C
                im = new VImage(-4000, 2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1-12/mapA/H/mapA_p1.png")).getImage(), 12.4);
                vs.addGlyph(im);
                
                im = new VImage(-4000, 0, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1-12/mapB/H/mapB_p1.png")).getImage(), 12.4);
                vs.addGlyph(im);
                
                im = new VImage(-4000, -2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1-12/mapC/H/mapC_p1.png")).getImage(), 12.4);
                vs.addGlyph(im);

                // I J K
                im = new VImage(4000, 2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1-12/mapK/H/mapK_p1.png")).getImage(), 12.4);
                vs.addGlyph(im);
                
                im = new VImage(4000, 0, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1-12/mapJ/H/mapJ_p1.png")).getImage(), 12.4);
                vs.addGlyph(im);
                
                im = new VImage(4000, -2000, 0, (new ImageIcon("/Users/epietrig/projects/zvtm_misc/hpl/images/1-12/mapI/H/mapI_p1.png")).getImage(), 12.4);
                vs.addGlyph(im);

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
    SpeedCoupling speedCoupling;

    static int LENS_R1 = 150;
    static int LENS_R2 = 75;
    static final int WHEEL_ANIM_TIME = 50;
    static final int LENS_ANIM_TIME = 300;
    static double DEFAULT_MAG_FACTOR = 12.0;
    static double MAG_FACTOR = DEFAULT_MAG_FACTOR;
    static double INV_MAG_FACTOR = 1/MAG_FACTOR;
    /* LENS MAGNIFICATION */
    static float WHEEL_MM_STEP = 1.0f;
    static final float MAX_MAG_FACTOR = 12.0f;
        
    /* lens distance and drop-off functions */
    static final short L2_Gaussian = 0;
    static final short L2_SCB = 1;
    static final short L2_FLAT = 2;
    short visual_behavior = L2_Gaussian; //L2_FLAT,L2_SCB L2_Gaussian;
    
    static final short MP_NONE = 0;
    static final short MP_CONTINUOUS = 1;
    static final short MP_RING = 2;
    static final short MP_SHIFT = 3;
    short motor_precision = MP_RING; //MP_CONTINUOUS;
	
	void toggleLensType(){}
	
	void goFocusControl(boolean b){
	    lens.setFocusControlled(b, FixedSizeLens.CONSTANT);
	}

    void moveLens(int x, int y, long absTime){
	if (lens == null)
	    return;
	int lX = lens.lx + panelWidth / 2;
	int lY = lens.ly + panelHeight / 2;

	//System.out.println("x: " + x + ", y:" + y + ", lX: " + lX + ", lY: " + lY +
	//		   ", panel: " + panelWidth + ", " + panelHeight);
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

		//System.out.println("x: " + x + ", y:" + y + ", lX: " + lX + ", lY: " + lY +
		//		   ", panel: " + panelWidth + ", " + panelHeight);
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

    int _lastX,_lastY;
    static int NUM_PSTOCK = 1;
    double[] _pX = new double[NUM_PSTOCK];
    double[] _pY = new double[NUM_PSTOCK];

    double getRadius()
    {
	double Rad = (double)lens.getActualRingRadius();
	// getActualRingRadius();  
	return Rad;
    }

    void pstock(double x, double y)
    {
	for (int i = NUM_PSTOCK-1; i > 0; i--)
	{
	    _pX[i] = _pX[i-1];
	    _pY[i] = _pY[i-1];
	}
	_pX[0] = x;
	_pY[0] = y;
    }

    void ringMoveLens(int x, int y, long absTime)
    {
	int lX = lens.lx + panelWidth / 2;
	int lY = lens.ly + panelHeight / 2;

	if (x == _lastX && y == _lastY)
	{
	    return;
	}

	int lensX = lX;
	int lensY = lY;
	double d = Math.sqrt((lensX-x)*(lensX-x) + (lensY-y)*(lensY-y));
	double Rad2 = getRadius();
	if (d < Rad2)
	{
	    _lastX = x; _lastY = y;
 	    pstock((double)x, (double)y);

	    return;
	}

	double x2 =   (double)x - _pX[NUM_PSTOCK-1]; //_lastX;
	double y2 = - (double)y + _pY[NUM_PSTOCK-1]; //_lastY;
	double xg =   (double)lensX - _pX[NUM_PSTOCK-1]; //_lastX;
	double yg = - (double)lensY + _pY[NUM_PSTOCK-1]; //_lastY;
	double Gx,Gy;
	double norm = Math.sqrt((x2 - xg)*(x2 - xg) + (y2 -yg)*(y2 -yg));
	double r =  Math.sqrt((xg*xg) + (yg*yg));
	r = Rad2;
	Gx = x2 - r*((x2-xg)/norm);
	Gy = y2 - r*((y2-yg)/norm);
	Gx = Gx + (double)_pX[NUM_PSTOCK-1]; //_lastX;
	Gy = - Gy + (double)_pY[NUM_PSTOCK-1];//_lastY;

	moveLens((int)Gx, (int)Gy, absTime);

	_lastX = x; _lastY = y;
	pstock(x,y);
    }


    void activateLens(int x, int y){
	updatePanelSize();
	System.out.println(
	    "ACTIVATE LENS x: " + x + ", y:" + y + ", panel: " + 
	    panelWidth + ", " + panelHeight);
	
        if (lens != null){return;}
        lens = (FixedSizeLens)pdfView.setLens(getLensDefinition(x, y));
        lens.setBufferThreshold(1.5f);
        
	if (visual_behavior != L2_SCB)
	{
	    Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(LENS_ANIM_TIME, (FixedSizeLens)lens,
														     new Float(MAG_FACTOR-1), true, IdentityInterpolator.getInstance(), null);
	    VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
	}
	else
	{
	    
	}
        
        /* motor precison: continuous */
	//motor_precision = MP_CONTINUOUS;
	if (motor_precision == MP_CONTINUOUS)
	{
	    lens.setFocusControlled(true, FixedSizeLens.SPEED_DEPENDENT_LINEAR);
        }

        /* motor precison: key */
        //motor_precision = MP_SHIFT;
        

	 /* motor precison: ring */
        //motor_precision = MP_RING;
        
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
		if (visual_behavior != L2_SCB)
		{
		    Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createLensMagAnim(WHEEL_ANIM_TIME, (FixedSizeLens)lens,
															     new Float(magOffset), true, IdentityInterpolator.getInstance(), null);
		    VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
		}
		else
		{
		    
		}
	    }
        }
    }

    FixedSizeLens getLensDefinition(int x, int y){
        FixedSizeLens res = null;
        switch (visual_behavior){
            case L2_Gaussian:{
                res = new FSGaussianLens(1f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
		res.setOuterRadiusColor(Color.BLACK);
		res.setInnerRadiusColor(Color.BLACK);
                tLens = null;
                break;
            }
            case L2_SCB:{
                SCBLens scbLens = new SCBLens((float)MAG_FACTOR, 0.0f, 1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);
                scbLens.setBoundaryColor(Color.BLACK);
                scbLens.setObservedRegionColor(Color.BLACK);

		speedCoupling = new SpeedCoupling();
		// lag time (100ms bd) and reduc time (500ms bd)
		speedCoupling.setTimeParameters(300, 400);
		// min speed (200 p/s bd)
		speedCoupling.setSpeedParameters(100);
		// coef_inc = 0.05 bd , coef_dec = 0.0 bd
		speedCoupling.setCoefParameters(0.1f, 0.01f);
		if (motor_precision ==  MP_RING)
		{
		    speedCoupling.setCoefParameters(0.1f, 0.005f);
		    // should put this func in TemporalLens ?
		    scbLens.setDoRing(true);
		    //scbLens.setDoDrawRing(true);
		}
		// should put this func in TemporalLens ?
		scbLens.setSpeedCoupling(speedCoupling);

		tLens = (TemporalLens)scbLens;
                res = (FixedSizeLens)tLens;
                break;
            }
	    case L2_FLAT:{
		SCFGaussianLens SCFLens = new SCFGaussianLens(1f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);

		speedCoupling = new SpeedCoupling();
		// lag time (100ms bd) and reduc time (500ms bd)
		speedCoupling.setTimeParameters(200, 400);
		// min speed (200 p/s bd)
		speedCoupling.setSpeedParameters(100);
		// coef_inc = 0.05 bd , coef_dec = 0.0 bd
		speedCoupling.setCoefParameters(0.1f, 0.00f);
		if (motor_precision ==  MP_RING)
		{
		    SCFLens.setSpeedBlendRadii(false, false);
		    // should put this func in TemporalLens ?
		    SCFLens.setDoRing(true);
		    //SCFLens.setDoDrawRing(true);
		}
		// should put this func in TemporalLens ?
		SCFLens.setSpeedCoupling(speedCoupling);

		tLens = (TemporalLens)SCFLens;
		res = (FixedSizeLens)tLens;
		res.setOuterRadiusColor(Color.BLACK);
		res.setInnerRadiusColor(Color.BLACK);
                break;
	    }
        }
        return res;
    }
    
    static final AlphaComposite acO = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
    static final AlphaComposite acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .7f);
    
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
        if (lens == null){return;}
        g2d.setColor(Color.BLACK);
        g2d.setComposite(acST);
        g2d.fillRect(0, 0, 50, 20);
        g2d.setComposite(acO);
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.valueOf(Math.round(MAG_FACTOR))+"x", 15, 15);
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
		System.setProperty("sun.java2d.opengl","true");
		new PDFLens((args.length > 0) ? args[0] : null,
		            (args.length > 1) ? Float.parseFloat(args[1]) : 1,
		            (args.length > 2) ? Float.parseFloat(args[2]) : 12,
			    (args.length > 3) ? Short.parseShort(args[3]) : 0,
			    (args.length > 4) ? Short.parseShort(args[4]) : 0);
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
		application.ringMoveLens(jpx, jpy, e.getWhen());
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
