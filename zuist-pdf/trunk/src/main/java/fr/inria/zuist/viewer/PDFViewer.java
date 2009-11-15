/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.viewer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import java.awt.Container;
import javax.swing.KeyStroke;

import java.util.Vector;
import java.util.HashMap;

import java.net.URL;
import java.io.File;
import java.io.IOException;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.Utilities;
import fr.inria.zvtm.engine.SwingWorker;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.CameraListener;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.glyphs.ZPDFPageImg;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.Level;
import fr.inria.zuist.engine.RegionListener;
import fr.inria.zuist.engine.LevelListener;
import fr.inria.zuist.engine.ProgressListener;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.PDFResourceHandler;

import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFFile;

/**
 * @author Emmanuel Pietriga
 */

public class PDFViewer {
        
    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1024;  // 1400
    static int VIEW_MAX_H = 768;   // 1050
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;
    
    /* Navigation constants */
    static final int ANIM_MOVE_LENGTH = 300;
    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;
    
    /* ZVTM objects */
    VirtualSpaceManager vsm;
    static final String mSpaceName = "Scene Space";
    VirtualSpace mSpace;
    Camera mCamera;
    static final String mViewName = "ZUIST PDF Viewer";
    View mView;
    PDFViewerEventHandler eh;

    SceneManager sm;

	VWGlassPane gp;
    
    public PDFViewer(boolean fullscreen, boolean opengl, File pdfFile){
		initGUI(fullscreen, opengl);
        VirtualSpace[]  sceneSpaces = {mSpace};
        Camera[] sceneCameras = {mCamera};
        sm = new SceneManager(sceneSpaces, sceneCameras);
        sm.setResourceHandler("pdf", new PDFResourceHandler());
        sm.setSceneCameraBounds(mCamera, eh.wnes);
		if (pdfFile != null){
			loadPDF(pdfFile);
			//getGlobalView();
		}
        mCamera.addListener(eh);
    }

    void initGUI(boolean fullscreen, boolean opengl){
        windowLayout();
        vsm = VirtualSpaceManager.INSTANCE;
        mSpace = vsm.addVirtualSpace(mSpaceName);
        mCamera = mSpace.addCamera();
        Vector cameras = new Vector();
        cameras.add(mCamera);
        mView = vsm.addFrameView(cameras, mViewName, (opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H, false, false, !fullscreen, null);
        if (fullscreen){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        updatePanelSize();
		gp = new VWGlassPane(this);
		((JFrame)mView.getFrame()).setGlassPane(gp);
        eh = new PDFViewerEventHandler(this);
        mView.setEventHandler(eh, 0);
        mView.setNotifyMouseMoved(true);
        mView.setBackgroundColor(Color.WHITE);
		mView.getPanel().addComponentListener(eh);
		ComponentAdapter ca0 = new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				updatePanelSize();
			}
		};
		mView.getFrame().addComponentListener(ca0);		
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

	/*-------------  Scene management    -------------*/
	
	static final short[] TRANSITIONS = {Region.APPEAR, Region.APPEAR, Region.DISAPPEAR, Region.DISAPPEAR};
	
	void loadPDF(File pdfFile){
		try {
			mView.setTitle(mViewName + " - " + pdfFile.getCanonicalPath());
		}
		catch (IOException ex){}
		gp.setValue(0);
		gp.setVisible(true);
		try {
    		URL pdfURL = pdfFile.toURI().toURL();
    		PDFFile pf = PDFResourceHandler.getPDF(pdfURL);
    		float[] alts = new float[pf.getNumPages()];
    		Rectangle2D bbox = PDFResourceHandler.getPage(pdfURL, 0).getBBox();
    		alts[0] = 0;
    		Region prevRegion = null;
    		// all but last level
    		for (int i=0;i<pf.getNumPages()-1;i++){
    		    int depth = pf.getNumPages() - i - 1;
    		    alts[i+1] = Camera.DEFAULT_FOCAL * (float)Math.pow(2, i+1) - Camera.DEFAULT_FOCAL;
                sm.createLevel(depth, alts[i+1], alts[i]);
                Region r = sm.createRegion(0, 0, Math.round(bbox.getWidth()*(i+1)), Math.round(bbox.getHeight()*(i+1)), depth, depth,
                                "R"+String.valueOf(depth), "Page "+String.valueOf(depth+1),
                                0, TRANSITIONS, Region.ORDERING_ARRAY, true, null, Color.BLACK);
                sm.createTextDescription(0, 0, "P"+String.valueOf(depth), 0, r, (float)Math.pow(2,i+1), "Page "+String.valueOf(depth+1),
                                         VText.TEXT_ANCHOR_START, Color.BLACK,
                                         "Arial", Font.PLAIN, 24, false);
                if (prevRegion != null){
                    prevRegion.setContainingRegion(r);
                    r.addContainedRegion(prevRegion);
                }
                prevRegion = r;
    		}
    		// last level
    		sm.createLevel(0, Camera.DEFAULT_FOCAL * (float)Math.pow(2, pf.getNumPages()) - Camera.DEFAULT_FOCAL, alts[pf.getNumPages()-1]);
    		Region r = sm.createRegion(0, 0, Math.round(bbox.getWidth()*(pf.getNumPages())), Math.round(bbox.getHeight()*(pf.getNumPages())), 0, 0,
                                       "R0", "Page 1", 0, TRANSITIONS, Region.ORDERING_ARRAY, true, null, Color.BLACK);
            if (prevRegion != null){
                r.addContainedRegion(prevRegion);
                prevRegion.setContainingRegion(r);
            }
                            
		}
		catch (java.net.MalformedURLException ex){ex.printStackTrace();}
		// important SM init calls
        sm.enableGlyphLoader(true);
        sm.setUpdateLevel(true);
	    gp.setVisible(false);
	    gp.setLabel(VWGlassPane.EMPTY_STRING);
        mCamera.setAltitude(0.0f);
        sm.updateLevel(mCamera.altitude);
        eh.cameraMoved(null, null, 0);
	}
    
    /*-------------     Navigation       -------------*/
    
    void getGlobalView(){
		int l = 0;
		while (sm.getRegionsAtLevel(l) == null){
			l++;
			if (l > sm.getLevelCount()){
				l = -1;
				break;
			}
		}
		if (l > -1){
			long[] wnes = sm.getLevel(l).getBounds();
	        mCamera.getOwningView().centerOnRegion(mCamera, PDFViewer.ANIM_MOVE_LENGTH, wnes[0], wnes[1], wnes[2], wnes[3]);		
		}
    }

    /* Higher view */
    void getHigherView(){
        Float alt = new Float(mCamera.getAltitude() + mCamera.getFocal());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(PDFViewer.ANIM_MOVE_LENGTH, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Higher view */
    void getLowerView(){
        Float alt=new Float(-(mCamera.getAltitude() + mCamera.getFocal())/2.0f);
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(PDFViewer.ANIM_MOVE_LENGTH, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Direction should be one of Viewer.MOVE_* */
    void translateView(short direction){
        LongPoint trans;
        long[] rb = mView.getVisibleRegion(mCamera);
        if (direction==MOVE_UP){
            long qt = Math.round((rb[1]-rb[3])/4.0);
            trans = new LongPoint(0,qt);
        }
        else if (direction==MOVE_DOWN){
            long qt = Math.round((rb[3]-rb[1])/4.0);
            trans = new LongPoint(0,qt);
        }
        else if (direction==MOVE_RIGHT){
            long qt = Math.round((rb[2]-rb[0])/4.0);
            trans = new LongPoint(qt,0);
        }
        else {
            // direction==MOVE_LEFT
            long qt = Math.round((rb[0]-rb[2])/4.0);
            trans = new LongPoint(qt,0);
        }
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(PDFViewer.ANIM_MOVE_LENGTH, mCamera,
            trans, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }
	
    void altitudeChanged(){
        sm.updateLevel(mCamera.altitude);
    }
    
    void updatePanelSize(){
        Dimension d = mView.getPanel().getSize();
        panelWidth = d.width;
		panelHeight = d.height;
	}

    /* ----- Misc  ------*/
    
    void exit(){
        System.exit(0);
    }

    public static void main(String[] args){
        File pdfFile = null;
		boolean fs = false;
		boolean ogl = false;
		boolean aa = true;
		for (int i=0;i<args.length;i++){
			if (args[i].startsWith("-")){
				if (args[i].substring(1).equals("fs")){fs = true;}
				else if (args[i].substring(1).equals("opengl")){ogl = true;}
				else if (args[i].substring(1).equals("h") || args[i].substring(1).equals("--help")){PDFViewer.printCmdLineHelp();System.exit(0);}
			}
            else {
                // the only other thing allowed as a cmd line param is a scene file
                File f = new File(args[i]);
                if (f.exists()){
                    pdfFile = f;                        
                }
            }
		}
        if (!fs && Utilities.osIsMacOS()){
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        System.out.println("--help for command line options");
        new PDFViewer(fs, ogl, pdfFile);
    }
    
    private static void printCmdLineHelp(){
        System.out.println("Usage:\n\tjava -Xmx1024M -Xms512M -jar target/zuist-pdf-X.X.X.jar <path_to_pdf> [options]");
        System.out.println("Options:\n\t-fs: fullscreen mode");
        System.out.println("Options:\n\t-opengl: OpenGL pipeline");
    }
    
}

class VWGlassPane extends JComponent implements ProgressListener {
    
    static final int BAR_WIDTH = 200;
    static final int BAR_HEIGHT = 10;

    static final AlphaComposite GLASS_ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f);    
    static final Color MSG_COLOR = Color.DARK_GRAY;
    GradientPaint PROGRESS_GRADIENT = new GradientPaint(0, 0, Color.ORANGE, 0, BAR_HEIGHT, Color.BLUE);

    static final String EMPTY_STRING = "";
    String msg = EMPTY_STRING;
    int msgX = 0;
    int msgY = 0;
    
    int completion = 0;
    int prX = 0;
    int prY = 0;
    int prW = 0;
    
    PDFViewer application;
    
    VWGlassPane(PDFViewer app){
        super();
        this.application = app;
        addMouseListener(new MouseAdapter(){});
        addMouseMotionListener(new MouseMotionAdapter(){});
        addKeyListener(new KeyAdapter(){});
    }
    
    public void setValue(int c){
        completion = c;
        prX = application.panelWidth/2-BAR_WIDTH/2;
        prY = application.panelHeight/2-BAR_HEIGHT/2;
        prW = (int)(BAR_WIDTH * ((float)completion) / 100.0f);
        PROGRESS_GRADIENT = new GradientPaint(0, prY, Color.LIGHT_GRAY, 0, prY+BAR_HEIGHT, Color.DARK_GRAY);
        repaint(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }
    
    public void setLabel(String m){
        msg = m;
        msgX = application.panelWidth/2-BAR_WIDTH/2;
        msgY = application.panelHeight/2-BAR_HEIGHT/2 - 10;
        repaint(msgX, msgY-50, 400, 70);
    }
    
    protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D)g;
        Rectangle clip = g.getClipBounds();
        g2.setComposite(GLASS_ALPHA);
        g2.setColor(Color.WHITE);
        g2.fillRect(clip.x, clip.y, clip.width, clip.height);
        g2.setComposite(AlphaComposite.Src);
        if (msg != EMPTY_STRING){
            g2.setColor(MSG_COLOR);
            g2.setFont(ConfigManager.GLASSPANE_FONT);
            g2.drawString(msg, msgX, msgY);
        }
        g2.setPaint(PROGRESS_GRADIENT);
        g2.fillRect(prX, prY, prW, BAR_HEIGHT);
        g2.setColor(MSG_COLOR);
        g2.drawRect(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }
    
}

class ConfigManager {

	static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12);

    static final Font GLASSPANE_FONT = new Font("Arial", Font.PLAIN, 12);

}

class PDFViewerEventHandler implements ViewEventHandler, CameraListener, ComponentListener {

    static final float MAIN_SPEED_FACTOR = 50.0f;

    static final float WHEEL_ZOOMIN_FACTOR = 21.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 22.0f;
    
    static float WHEEL_MM_STEP = 1.0f;
    
    int lastJPX,lastJPY;    //remember last mouse coords to compute translation  (dragging)
    long lastVX, lastVY;
    int currentJPX, currentJPY;

    /* bounds of region in virtual space currently observed through mCamera */
    long[] wnes = new long[4];
    float oldCameraAltitude;

    PDFViewer application;
    
    PDFViewerEventHandler(PDFViewer app){
        this.application = app;
        oldCameraAltitude = this.application.mCamera.getAltitude();
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
		if (v.lastGlyphEntered() != null){
    		application.mView.centerOnGlyph(v.lastGlyphEntered(), v.cams[0], PDFViewer.ANIM_MOVE_LENGTH, true, 1.2f);				
		}
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}
        
    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){}

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
		float a = (application.mCamera.focal+Math.abs(application.mCamera.altitude)) / application.mCamera.focal;
		if (wheelDirection  == WHEEL_UP){
			// zooming in
			application.mCamera.altitudeOffset(a*WHEEL_ZOOMOUT_FACTOR);
			cameraMoved(null, null, 0);
			VirtualSpaceManager.INSTANCE.repaintNow();
		}
		else {
			//wheelDirection == WHEEL_DOWN, zooming out
			application.mCamera.altitudeOffset(-a*WHEEL_ZOOMIN_FACTOR);
			cameraMoved(null, null, 0);
			VirtualSpaceManager.INSTANCE.repaintNow();
		}
	}

	public void enterGlyph(Glyph g){}

	public void exitGlyph(Glyph g){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
    	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
    	else if (code==KeyEvent.VK_HOME){application.getGlobalView();}
    	else if (code==KeyEvent.VK_UP){application.translateView(Viewer.MOVE_UP);}
    	else if (code==KeyEvent.VK_DOWN){application.translateView(Viewer.MOVE_DOWN);}
    	else if (code==KeyEvent.VK_LEFT){application.translateView(Viewer.MOVE_LEFT);}
    	else if (code==KeyEvent.VK_RIGHT){application.translateView(Viewer.MOVE_RIGHT);}
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}
    
    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){
        application.exit();
    }

    /*ComponentListener*/
    public void componentHidden(ComponentEvent e){}
    public void componentMoved(ComponentEvent e){}
    public void componentResized(ComponentEvent e){
        application.updatePanelSize();
    }
    public void componentShown(ComponentEvent e){}
    
    public void cameraMoved(Camera cam, LongPoint coord, float a){
        // region seen through camera
        application.mView.getVisibleRegion(application.mCamera, wnes);
        float alt = application.mCamera.getAltitude();
        if (alt != oldCameraAltitude){
            // camera was an altitude change
            application.altitudeChanged();
            oldCameraAltitude = alt;
        }
        else {
            // camera movement was a simple translation
            application.sm.updateVisibleRegions();
        }
    }

}
