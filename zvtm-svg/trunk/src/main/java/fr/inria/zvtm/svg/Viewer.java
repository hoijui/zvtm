/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.svg;

import java.awt.AlphaComposite;
import java.awt.Toolkit;
import java.awt.GraphicsEnvironment;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.GradientPaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Point2D;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.EView;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Utilities;
import fr.inria.zvtm.engine.SwingWorker;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.RImage;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import fr.inria.zvtm.engine.portals.PortalEventHandler;

public class Viewer {

    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1024;  // 1400
    static int VIEW_MAX_H = 768;   // 1050
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;
    
    VirtualSpaceManager vsm;
    VirtualSpace svgSpace, aboutSpace;
    EView mView;
    
    MainEventHandler eh;
    Navigation nm;
    Overlay ovm;
    
    VWGlassPane gp;
    
    File SCENE_FILE, SCENE_FILE_DIR;
    
    /* --------------- init ------------------*/

    public Viewer(File svgF, boolean fullscreen, boolean opengl, boolean antialiased){
        init();
        initGUI(fullscreen, opengl, antialiased);
        if (svgF != null){
            loadSVG(svgF);            
        }
    }
    
    void init(){
        // parse properties
        Scanner sc = new Scanner(Viewer.class.getResourceAsStream("/properties")).useDelimiter("\\s*=\\s*");
        while (sc.hasNext()){
            String token = sc.next();
            if (token.equals("version")){
                Messages.VERSION = sc.next();
            }
        }
    }
        
    void initGUI(boolean fullscreen, boolean opengl, boolean antialiased){
        windowLayout();
        Glyph.setDefaultCursorInsideHighlightColor(Config.HIGHLIGHT_COLOR);
        vsm = VirtualSpaceManager.INSTANCE;
        ovm = new Overlay(this);
        nm = new Navigation(this);
        svgSpace = vsm.addVirtualSpace(Messages.svgSpaceName);
        Camera mCamera = svgSpace.addCamera();
        mCamera.setZoomFloor(-99.0);
        nm.ovCamera = svgSpace.addCamera();
        aboutSpace = vsm.addVirtualSpace(Messages.aboutSpaceName);
		aboutSpace.addCamera();
        Vector cameras = new Vector();
        cameras.add(mCamera);
        nm.setCamera(mCamera);
        cameras.add(aboutSpace.getCamera(0));
        mView = (EView)vsm.addFrameView(cameras, Messages.mViewName, (opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H,
                                        false, false, !fullscreen, (!fullscreen) ? Config.initMenu(this) : null);
        if (fullscreen){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        updatePanelSize();
        ovm.init();
		gp = new VWGlassPane(this);
		((JFrame)mView.getFrame()).setGlassPane(gp);
        eh = new MainEventHandler(this);
        mView.setEventHandler(eh, 0);
        mView.setEventHandler(ovm, 1);
        mView.setNotifyMouseMoved(true);
        mView.setAntialiasing(antialiased);
        mView.setBackgroundColor(Config.BACKGROUND_COLOR);
		mView.getPanel().addComponentListener(eh);
		ComponentAdapter ca0 = new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				updatePanelSize();
			}
		};
		mView.getFrame().addComponentListener(ca0);
		nm.createOverview();
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
    
    void updatePanelSize(){
        Dimension d = mView.getPanel().getSize();
        panelWidth = d.width;
		panelHeight = d.height;
		nm.updateOverviewLocation();
	}
    
    /* --------------- SVG Parsing ------------------*/
    
	void reset(){
		svgSpace.removeAllGlyphs();
	}
	
	void openFile(){
		final JFileChooser fc = new JFileChooser(SCENE_FILE_DIR);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Find SVG File");
		int returnVal= fc.showOpenDialog(mView.getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION){
		    final SwingWorker worker = new SwingWorker(){
			    public Object construct(){
					reset();
					loadSVG(fc.getSelectedFile());
					return null; 
			    }
			};
		    worker.start();
		}
	}

	void reload(){
		if (SCENE_FILE == null){return;}
		final SwingWorker worker = new SwingWorker(){
		    public Object construct(){
				reset();
				loadSVG(SCENE_FILE);
				return null; 
		    }
		};
	    worker.start();
	}
    
    static final String LOAD_EXTERNAL_DTD_URL = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    
    void loadSVG(File svgF){
        gp.setVisible(true);
        gp.setValue(20);
        gp.setLabel(Messages.LOADING + svgF.getName());
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setAttribute(LOAD_EXTERNAL_DTD_URL, Boolean.FALSE);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            String svgURL = svgF.toURI().toURL().toString();
            Document xmlSVG = builder.parse(svgURL);
            gp.setValue(60);
            SVGReader.load(xmlSVG, svgSpace, true, svgURL);
            nm.getGlobalView();
            nm.updateOverview();
        }
        catch (FactoryConfigurationError e){e.printStackTrace();}
        catch (ParserConfigurationException e){e.printStackTrace();}
        catch (SAXException e){e.printStackTrace();}
        catch (MalformedURLException e){e.printStackTrace();}
        catch (IOException e){e.printStackTrace();}
		SCENE_FILE = svgF;
	    SCENE_FILE_DIR = SCENE_FILE.getParentFile();
	    mView.setTitle(Messages.mViewName + " - " + SCENE_FILE.getName());
        gp.setVisible(false);
    }
    
    /* --------------- Main/exit ------------------*/
    
    void exit(){
        System.exit(0);
    }
    
    public static void main(String[] args){
		boolean fs = false;
		boolean ogl = false;
		boolean aa = true;
		File svgF = null;
		for (int i=0;i<args.length;i++){
			if (args[i].startsWith("-")){
				if (args[i].substring(1).equals("fs")){fs = true;}
				else if (args[i].substring(1).equals("opengl")){
				    System.setProperty("sun.java2d.opengl", "true");
				    ogl = true;
				}
				else if (args[i].substring(1).equals("noaa")){aa = false;}
				else if (args[i].substring(1).equals("h") || args[i].substring(1).equals("-help")){Messages.printCmdLineHelp();System.exit(0);}
			}
			else {
			    File f = new File(args[i]);
			    if (f.exists()){svgF = f;}
			}
		}
        if (!fs && Utilities.osIsMacOS()){
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        System.out.println(Messages.H_4_HELP);
        new Viewer(svgF, fs, ogl, aa);
    }
    
}

class VWGlassPane extends JComponent {
    
    static final int BAR_WIDTH = 200;
    static final int BAR_HEIGHT = 10;

    static final AlphaComposite GLASS_ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f);    
    static final Color MSG_COLOR = Color.DARK_GRAY;
    GradientPaint PROGRESS_GRADIENT = new GradientPaint(0, 0, Color.ORANGE, 0, BAR_HEIGHT, Color.BLUE);

    String msg = Messages.EMPTY_STRING;
    int msgX = 0;
    int msgY = 0;
    
    int completion = 0;
    int prX = 0;
    int prY = 0;
    int prW = 0;
    
    Viewer application;
    
    VWGlassPane(Viewer app){
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
        if (msg != Messages.EMPTY_STRING && msg.length() > 0){
            g2.setColor(MSG_COLOR);
            g2.setFont(Config.GLASSPANE_FONT);
            g2.drawString(msg, msgX, msgY);
        }
        g2.setPaint(PROGRESS_GRADIENT);
        g2.fillRect(prX, prY, prW, BAR_HEIGHT);
        g2.setColor(MSG_COLOR);
        g2.drawRect(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }
    
}

class Overlay implements ViewEventHandler {
    
    Viewer application;

    boolean showingAbout = false;
    VRectangle fadeAbout;
    VImage insituLogo, inriaLogo;
    VText[] aboutLines;

    VRectangle fadedRegion;
    VText sayGlyph;

    Overlay(Viewer app){
        this.application = app;
    }

    void init(){
        fadedRegion = new VRectangle(0, 0, 0, 10, 10, Config.FADE_REGION_FILL, Config.FADE_REGION_STROKE, 0.85f);
        application.aboutSpace.addGlyph(fadedRegion);
        fadedRegion.setVisible(false);
        sayGlyph = new VText(0, -10, 0, Config.SAY_MSG_COLOR, Messages.EMPTY_STRING, VText.TEXT_ANCHOR_MIDDLE);
        sayGlyph.setFont(Config.SAY_MSG_FONT);
        application.aboutSpace.addGlyph(sayGlyph);
        sayGlyph.setVisible(false);
    }
    
    void showAbout(){
        if (!showingAbout){
            fadeAbout = new VRectangle(0, 0, 0, Math.round(application.panelWidth/1.05), Math.round(application.panelHeight/1.5),
                Config.FADE_REGION_FILL, Config.FADE_REGION_STROKE, 0.85f);
            aboutLines = new VText[4];
			aboutLines[0] = new VText(0, 150, 0, Color.WHITE, Messages.APP_NAME, VText.TEXT_ANCHOR_MIDDLE, 4.0f);
            aboutLines[1] = new VText(0, 110, 0, Color.WHITE, Messages.V+Messages.VERSION, VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            aboutLines[2] = new VText(0, 40, 0, Color.WHITE, Messages.AUTHORS, VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            RImage.setReflectionHeight(0.7f);
            inriaLogo = new RImage(-150, -40, 0, (new ImageIcon(this.getClass().getResource(Config.INRIA_LOGO_PATH))).getImage(), 1.0f);
            insituLogo = new RImage(200, -40, 0, (new ImageIcon(this.getClass().getResource(Config.INSITU_LOGO_PATH))).getImage(), 1.0f);
            aboutLines[3] = new VText(0, -200, 0, Color.WHITE, Messages.ABOUT_DEPENDENCIES, VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            aboutLines[3].setFont(Config.MONOSPACE_ABOUT_FONT);
            application.aboutSpace.addGlyph(fadeAbout);
            application.aboutSpace.addGlyph(inriaLogo);
            application.aboutSpace.addGlyph(insituLogo);
			for (int i=0;i<aboutLines.length;i++){
	            application.aboutSpace.addGlyph(aboutLines[i]);				
			}
            showingAbout = true;
        }
		application.mView.setActiveLayer(1);
		if (application.nm.ovPortal.isVisible()){application.nm.toggleOverview();}
    }

    void hideAbout(){
        if (showingAbout){
            showingAbout = false;
            if (insituLogo != null){
                application.aboutSpace.removeGlyph(insituLogo);
                insituLogo = null;
            }
            if (inriaLogo != null){
                application.aboutSpace.removeGlyph(inriaLogo);
                inriaLogo = null;
            }
            if (fadeAbout != null){
                application.aboutSpace.removeGlyph(fadeAbout);
                fadeAbout = null;
            }
			for (int i=0;i<aboutLines.length;i++){
	            if (aboutLines[i] != null){
	                application.aboutSpace.removeGlyph(aboutLines[i]);
	                aboutLines[i] = null;
	            }				
			}
		}
		application.mView.setActiveLayer(0);
		application.nm.ovPortal.setVisible(true);
	}

    void say(final String msg){
    	final SwingWorker worker = new SwingWorker(){
    		public Object construct(){
    		    showMessage(msg);
    		    sleep(Config.SAY_DURATION);
    		    hideMessage();
    		    return null;
    		}
    	    };
    	worker.start();
    }

    void showMessage(String msg){
        synchronized(this){
            fadedRegion.setWidth(application.panelWidth/2-1);
            fadedRegion.setHeight(50);
            sayGlyph.setText(msg);
            fadedRegion.setVisible(true);
            sayGlyph.setVisible(true);
        }
    }

    void hideMessage(){
        synchronized(this){
            fadedRegion.setVisible(false);
            sayGlyph.setVisible(false);
        }
    }

	public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	}

	public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
		hideAbout();
	}

	public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){}

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){}

	public void enterGlyph(Glyph g){}

	public void exitGlyph(Glyph g){}

	public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
		hideAbout();
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
}

class MainEventHandler implements ViewEventHandler, ComponentListener, PortalEventHandler {

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;
    static final float WHEEL_ZOOMIN_COEF = 21.0f;
    static final float WHEEL_ZOOMOUT_COEF = 22.0f;
    static float WHEEL_MM_STEP = 1.0f;
    
    //remember last mouse coords
    int lastJPX,lastJPY;
    long lastVX, lastVY;
    
    Viewer application;
    
    boolean pcameraStickedToMouse = false;
    boolean regionStickedToMouse = false;
    boolean inPortal = false;
    
    boolean panning = false;
    
    // region selection
	boolean selectingRegion = false;
	double x1, y1, x2, y2;
	
	boolean cursorNearBorder = false;
    
    Glyph sticked = null;
    
    MainEventHandler(Viewer app){
        this.application = app;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        if (inPortal){
		    if (application.nm.ovPortal.coordInsideObservedRegion(jpx, jpy)){
				regionStickedToMouse = true;
		    }
		    else {
				pcameraStickedToMouse = true;
		    }
		}
		else if (mod == ALT_MOD){
            selectingRegion = true;
            x1 = v.getVCursor().vx;
            y1 = v.getVCursor().vy;
            v.setDrawRect(true);
        }
        else {
            lastJPX = jpx;
            lastJPY = jpy;
            panning = true;
            v.setDrawDrag(true);
        }
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	    regionStickedToMouse = false;
	    pcameraStickedToMouse = false;
	    if (selectingRegion){
			v.setDrawRect(false);
			x2 = v.getVCursor().vx;
			y2 = v.getVCursor().vy;
			if ((Math.abs(x2-x1)>=4) && (Math.abs(y2-y1)>=4)){
				application.nm.mCamera.getOwningView().centerOnRegion(application.nm.mCamera, Config.ANIM_MOVE_LENGTH,
				                                                      x1, y1, x2, y2);
			}
			selectingRegion = false;
		}
        else if (panning){	    
            application.vsm.getAnimationManager().setXspeed(0);
            application.vsm.getAnimationManager().setYspeed(0);
            application.vsm.getAnimationManager().setZspeed(0);
            v.setDrawDrag(false);
            panning = false;
        }
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
        if (v.lastGlyphEntered() != null){
    		application.mView.centerOnGlyph(v.lastGlyphEntered(), v.cams[0], Config.ANIM_MOVE_LENGTH, true, 1.0f);				
		}
    }

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}
        
    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (regionStickedToMouse){
		    double a = (application.nm.ovCamera.focal+Math.abs(application.nm.ovCamera.altitude)) / application.nm.ovCamera.focal;
			application.nm.mCamera.move(Math.round(a*(jpx-lastJPX)), Math.round(a*(lastJPY-jpy)));
			lastJPX = jpx;
			lastJPY = jpy;
		}
		else if (pcameraStickedToMouse){
			double a = (application.nm.ovCamera.focal+Math.abs(application.nm.ovCamera.altitude))/application.nm.ovCamera.focal;
			application.nm.ovCamera.move(Math.round(a*(lastJPX-jpx)), Math.round(a*(jpy-lastJPY)));
			application.nm.mCamera.move(Math.round(a*(lastJPX-jpx)), Math.round(a*(jpy-lastJPY)));
			lastJPX = jpx;
			lastJPY = jpy;
		}
		else if (panning){
            Camera c = v.cams[0];
            double a = (c.focal+Math.abs(c.altitude))/c.focal;
            if (mod == META_SHIFT_MOD) {
                application.vsm.getAnimationManager().setXspeed(0);
                application.vsm.getAnimationManager().setYspeed(0);
                application.vsm.getAnimationManager().setZspeed(((lastJPY-jpy)*(ZOOM_SPEED_COEF)));
            }
            else {
                application.vsm.getAnimationManager().setXspeed((long)((jpx-lastJPX)*(a/PAN_SPEED_COEF)));
                application.vsm.getAnimationManager().setYspeed((long)((lastJPY-jpy)*(a/PAN_SPEED_COEF)));
                application.vsm.getAnimationManager().setZspeed(0);
            }		    
		}
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
        double a = (application.nm.mCamera.focal+Math.abs(application.nm.mCamera.altitude)) / application.nm.mCamera.focal;
        if (wheelDirection  == WHEEL_UP){
            // zooming in
            application.nm.mCamera.altitudeOffset(a*WHEEL_ZOOMOUT_COEF);
            VirtualSpaceManager.INSTANCE.repaintNow();
        }
        else {
            //wheelDirection == WHEEL_DOWN, zooming out
            application.nm.mCamera.altitudeOffset(-a*WHEEL_ZOOMIN_COEF);
            VirtualSpaceManager.INSTANCE.repaintNow();
        }            
    }

	public void enterGlyph(Glyph g){
	    g.highlight(true, null);
	}

	public void exitGlyph(Glyph g){
	    g.highlight(false, null);
	}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (code==KeyEvent.VK_PAGE_UP){application.nm.getHigherView();}
    	else if (code==KeyEvent.VK_PAGE_DOWN){application.nm.getLowerView();}
    	else if (code==KeyEvent.VK_HOME){application.nm.getGlobalView();}
    	else if (code==KeyEvent.VK_UP){application.nm.translateView(Navigation.MOVE_UP);}
    	else if (code==KeyEvent.VK_DOWN){application.nm.translateView(Navigation.MOVE_DOWN);}
    	else if (code==KeyEvent.VK_LEFT){application.nm.translateView(Navigation.MOVE_LEFT);}
    	else if (code==KeyEvent.VK_RIGHT){application.nm.translateView(Navigation.MOVE_RIGHT);}
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

	/* Overview Portal */
	public void enterPortal(Portal p){
		inPortal = true;
		((OverviewPortal)p).setBorder(Config.OV_INSIDE_BORDER_COLOR);
		VirtualSpaceManager.INSTANCE.repaintNow();
	}

	public void exitPortal(Portal p){
		inPortal = false;
		((OverviewPortal)p).setBorder(Config.OV_BORDER_COLOR);
		VirtualSpaceManager.INSTANCE.repaintNow();
	}
	
}

class Navigation {

    /* Navigation constants */

    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;
        
    Viewer application;
    
    VirtualSpaceManager vsm;
    Camera mCamera;
    Camera ovCamera;
    
    Navigation(Viewer app){
        this.application = app;
        vsm = VirtualSpaceManager.INSTANCE;
    }
    
    void setCamera(Camera c){
        this.mCamera = c;
    }
    
    /*-------------     Navigation       -------------*/
    
    void getGlobalView(){
		application.mView.getGlobalView(mCamera, Config.ANIM_MOVE_LENGTH, 1.05f);
    }

    /* Higher view */
    void getHigherView(){
        Float alt = new Float(mCamera.getAltitude() + mCamera.getFocal());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Config.ANIM_MOVE_LENGTH, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Higher view */
    void getLowerView(){
        Float alt=new Float(-(mCamera.getAltitude() + mCamera.getFocal())/2.0f);
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Config.ANIM_MOVE_LENGTH, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Direction should be one of Viewer.MOVE_* */
    void translateView(short direction){
        Point2D.Double trans;
        double[] rb = application.mView.getVisibleRegion(mCamera);
        if (direction==MOVE_UP){
            double qt = (rb[1]-rb[3])/4.0;
            trans = new Point2D.Double(0, qt);
        }
        else if (direction==MOVE_DOWN){
            double qt = (rb[3]-rb[1])/4.0;
            trans = new Point2D.Double(0,qt);
        }
        else if (direction==MOVE_RIGHT){
            double qt = (rb[2]-rb[0])/4.0;
            trans = new Point2D.Double(qt,0);
        }
        else {
            // direction==MOVE_LEFT
            double qt = (rb[0]-rb[2])/4.0;
            trans = new Point2D.Double(qt,0);
        }
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(Config.ANIM_MOVE_LENGTH, mCamera,
            trans, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }
    
    /* -------------- Overview ------------------- */
	
	OverviewPortal ovPortal;
	
	void createOverview(){
		ovPortal = new OverviewPortal(application.panelWidth-Config.OVERVIEW_WIDTH-1, application.panelHeight-Config.OVERVIEW_HEIGHT-1,
		                              Config.OVERVIEW_WIDTH, Config.OVERVIEW_HEIGHT, ovCamera, mCamera);
		ovPortal.setPortalEventHandler(application.eh);
		ovPortal.setBackgroundColor(Config.BACKGROUND_COLOR);
		ovPortal.setObservedRegionColor(Config.OBSERVED_REGION_COLOR);
		ovPortal.setObservedRegionTranslucency(Config.OBSERVED_REGION_ALPHA);
		VirtualSpaceManager.INSTANCE.addPortal(ovPortal, application.mView);
		ovPortal.setBorder(Color.GREEN);
		updateOverview();
	}
	
	void updateOverview(){
		if (ovPortal != null){
		    ovCamera.setLocation(ovPortal.getGlobalView());
		}
	}
	
	void updateOverviewLocation(){
	    if (ovPortal != null){
	        ovPortal.moveTo(application.panelWidth-Config.OVERVIEW_WIDTH-1, application.panelHeight-Config.OVERVIEW_HEIGHT-1);
	    }
	}

    void toggleOverview(){
        ovPortal.setVisible(!ovPortal.isVisible());
        vsm.repaintNow(application.mView);
    }
    
}

class Messages {

	static final String EMPTY_STRING = "";

	static final String V = "v";
	static String VERSION;
    static final String AUTHORS = "Author: Emmanuel Pietriga";
    static final String APP_NAME = "ZVTM SVG Viewer";
    static final String CREDITS_NAMES = "Based on: ZVTM";
    static final String ABOUT_DEPENDENCIES = "Based upon: ZVTM (http://zvtm.sf.net)";

    static final String H_4_HELP = "--help for command line options";
    
    static final String LOAD_FILE = "Load file";
    static final String LOADING = "Loading ";
    
    static final String PROCESSING = "Processing ";
    
    static final String svgSpaceName = "SVG";
    static final String aboutSpaceName = "About layer";
    static final String mViewName = "SVG Viewer";
    
    protected static void printCmdLineHelp(){
		System.out.println("Usage:\n\tjava -jar target/zvtm-svg-"+VERSION+".jar <path_to_file> [options]");
        System.out.println("Options:\n\t-fs: fullscreen mode");
        System.out.println("\t-noaa: no antialiasing");
		System.out.println("\t-opengl: use Java2D OpenGL rendering pipeline (Java 6+Linux/Windows), requires that -Dsun.java2d.opengl=true be set on cmd line");
    }
    
}

class Config {
    
    /* Fonts */
	static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12);
    static final Font GLASSPANE_FONT = new Font("Arial", Font.PLAIN, 12);
    static final Font SAY_MSG_FONT = new Font("Arial", Font.PLAIN, 24);
    static final Font MONOSPACE_ABOUT_FONT = new Font("Courier", Font.PLAIN, 8);
    
    /* Other colors */
    static final Color SAY_MSG_COLOR = Color.LIGHT_GRAY;
    static Color BACKGROUND_COLOR  = Color.WHITE;
    static final Color FADE_REGION_FILL = Color.BLACK;
    static final Color FADE_REGION_STROKE = Color.WHITE;
    static final Color HIGHLIGHT_COLOR = Color.RED;
    
    /* Overview */
    static final int OVERVIEW_WIDTH = 200;
	static final int OVERVIEW_HEIGHT = 200;
	static final Color OBSERVED_REGION_COLOR = Color.GREEN;
	static final float OBSERVED_REGION_ALPHA = 0.5f;
	static final Color OV_BORDER_COLOR = Color.BLACK;
	static final Color OV_INSIDE_BORDER_COLOR = Color.BLACK;
    
    /* Durations/Animations */
    static final int ANIM_MOVE_LENGTH = 300;
    static final int SAY_DURATION = 500;

    /* External resources */
    static final String INSITU_LOGO_PATH = "/images/insitu.png";
    static final String INRIA_LOGO_PATH = "/images/inria.png";
 
 	static JMenuBar initMenu(final Viewer app){
		final JMenuItem exitMI = new JMenuItem("Exit");
		exitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		final JMenuItem openMI = new JMenuItem("Open...");
		openMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		final JMenuItem reloadMI = new JMenuItem("Reload");
		reloadMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		final JMenuItem aboutMI = new JMenuItem("About...");
		ActionListener a0 = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (e.getSource()==exitMI){app.exit();}
				else if (e.getSource()==openMI){app.openFile();}
				else if (e.getSource()==reloadMI){app.reload();}
				else if (e.getSource()==aboutMI){app.ovm.showAbout();}
			}
		};
		JMenuBar jmb = new JMenuBar();
		JMenu fileM = new JMenu("File");
		JMenu helpM = new JMenu("Help");
		fileM.add(openMI);
		fileM.add(reloadMI);
		fileM.addSeparator();
		fileM.add(exitMI);
		helpM.add(aboutMI);
		jmb.add(fileM);
		jmb.add(helpM);
		openMI.addActionListener(a0);
		reloadMI.addActionListener(a0);
		exitMI.addActionListener(a0);
		aboutMI.addActionListener(a0);
		return jmb;
	}
	
}
