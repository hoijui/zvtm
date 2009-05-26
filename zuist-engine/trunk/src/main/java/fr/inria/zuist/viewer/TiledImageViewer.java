/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.viewer;

import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.GradientPaint;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.ImageIcon;

import java.util.Vector;

import java.io.File;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.SwingWorker;
import com.xerox.VTM.glyphs.VText;
import com.xerox.VTM.glyphs.VRectangleST;
import com.xerox.VTM.glyphs.VImage;
import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.engine.ViewEventHandler;
import net.claribole.zvtm.glyphs.RImage;
import net.claribole.zvtm.animation.Animation;
import net.claribole.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import net.claribole.zvtm.engine.RepaintListener;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.ProgressListener;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Emmanuel Pietriga
 */

public class TiledImageViewer {
    
    File SCENE_FILE, SCENE_FILE_DIR;
        
    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1024;  // 1400
    static int VIEW_MAX_H = 768;   // 1050
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;

	static final Color BACKGROUND_COLOR = Color.BLACK;
    
    boolean UPDATE_TILES = true;
    
    /* ZVTM objects */
    VirtualSpaceManager vsm;
    static final String mSpaceName = "Image Layer";
    static final String aboutSpaceName = "About layer";
    VirtualSpace mSpace, aboutSpace;
    Camera mCamera, ovCamera;
    static final String mViewName = "ZUIST Tiled Image Viewer";
    View mView;
    TIVExplorerEventHandler eh;

    SceneManager sm;
    TIVNavigationManager nm;
    Overlay ovm;
    
    WEGlassPane gp;
    
    public TiledImageViewer(boolean fullscreen, boolean opengl, boolean antialiased, File xmlSceneFile){
        ovm = new Overlay(this);
        initGUI(fullscreen, opengl, antialiased);
        nm = new TIVNavigationManager(this);
        eh.nm = this.nm;
        gp = new WEGlassPane(this);
        ((JFrame)mView.getFrame()).setGlassPane(gp);
        VirtualSpace[]  sceneSpaces = {mSpace};
        Camera[] sceneCameras = {mCamera};
        sm = new SceneManager(sceneSpaces, sceneCameras);
        sm.setSceneCameraBounds(mCamera, eh.wnes);
        eh.cameraMoved(null, null, 0);
        if (xmlSceneFile != null){
			loadScene(xmlSceneFile);
			nm.getGlobalView();
		}
        eh.dut.setEnabled(true);
		nm.createOverview(sm.getRegionsAtLevel(0)[0]);
        eh.cameraMoved(null, null, 0);
        mCamera.addListener(eh);
		nm.updateOverview();
    }
    
    void initGUI(boolean fullscreen, boolean opengl, boolean antialiased){
        windowLayout();
        vsm = VirtualSpaceManager.INSTANCE;
        mSpace = vsm.addVirtualSpace(mSpaceName);
        mCamera = vsm.addCamera(mSpace);
        ovCamera = vsm.addCamera(mSpace);
        aboutSpace = vsm.addVirtualSpace(aboutSpaceName);
		vsm.addCamera(aboutSpaceName);
        Vector cameras = new Vector();
        cameras.add(mCamera);
        cameras.add(aboutSpace.getCamera(0));
        mView = vsm.addExternalView(cameras, mViewName, (opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H, false, false, !fullscreen, (!fullscreen) ? initMenu() : null);
        if (fullscreen && GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        eh = new TIVExplorerEventHandler(this);
        mView.setEventHandler(eh, 0);
        mView.setEventHandler(ovm, 1);
        mView.setNotifyMouseMoved(true);
        mView.setBackgroundColor(BACKGROUND_COLOR);
		mView.setAntialiasing(antialiased);
		mView.getCursor().setColor(Color.WHITE);
		mView.getCursor().setHintColor(Color.WHITE);
		mView.getCursor().setDynaSpotColor(Color.WHITE);
        mView.getCursor().setDynaSpotLagTime(200);
        updatePanelSize();
        mView.setActiveLayer(0);
    }

	private JMenuBar initMenu(){
		final JMenuItem openMI = new JMenuItem("Open...");
		openMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		final JMenuItem reloadMI = new JMenuItem("Reload");
		reloadMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		final JMenuItem exitMI = new JMenuItem("Exit");
		exitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		final JMenuItem aboutMI = new JMenuItem("About...");
		ActionListener a0 = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (e.getSource()==openMI){openFile();}
				else if (e.getSource()==reloadMI){reload();}
				else if (e.getSource()==exitMI){exit();}
				else if (e.getSource()==aboutMI){ovm.showAbout();}
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
	
	void reset(){
		sm.reset();
		vsm.removeGlyphsFromSpace(mSpaceName);
	}
	
	void openFile(){
		final JFileChooser fc = new JFileChooser(SCENE_FILE_DIR);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Find ZUIST Scene File");
		int returnVal= fc.showOpenDialog(mView.getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION){
		    final SwingWorker worker = new SwingWorker(){
			    public Object construct(){
					reset();
					loadScene(fc.getSelectedFile());
					nm.getGlobalView();
					nm.updateOverview();
					return null; 
			    }
			};
		    worker.start();
		}
	}

	void reload(){
		if (SCENE_FILE==null){return;}
		final SwingWorker worker = new SwingWorker(){
		    public Object construct(){
				reset();
				loadScene(SCENE_FILE);
				nm.getGlobalView();
				return null; 
		    }
		};
	    worker.start();
	}

	void loadScene(File xmlSceneFile){
		try {
			mView.setTitle(mViewName + " - " + xmlSceneFile.getCanonicalPath());			
		}
		catch (IOException ex){}
		gp.setValue(0);
		gp.setVisible(true);
		SCENE_FILE = xmlSceneFile;
	    SCENE_FILE_DIR = SCENE_FILE.getParentFile();
	    sm.loadScene(parseXML(SCENE_FILE), SCENE_FILE_DIR, gp);
	    gp.setVisible(false);
	    gp.setLabel(WEGlassPane.EMPTY_STRING);
        mCamera.setAltitude(0.0f);
        sm.updateLevel(mCamera.altitude);
        eh.cameraMoved(null, null, 0);
	}
    
    void updatePanelSize(){
        Dimension d = mView.getPanel().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
    }

    void toggleUpdateTiles(){
        UPDATE_TILES = !UPDATE_TILES;
        sm.setUpdateLevel(UPDATE_TILES);
    }
    
    void gc(){
        System.gc();
    }
    
    static Document parseXML(File f){ 
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", new Boolean(false));
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document res = builder.parse(f);
            return res;
        }
        catch (FactoryConfigurationError e){e.printStackTrace();return null;}
        catch (ParserConfigurationException e){e.printStackTrace();return null;}
        catch (SAXException e){e.printStackTrace();return null;}
        catch (IOException e){e.printStackTrace();return null;}
    }
    
    void exit(){
        System.exit(0);
    }

    public static void main(String[] args){
        File xmlSceneFile = null;
		boolean fs = false;
		boolean ogl = false;
		boolean aa = true;
		for (int i=0;i<args.length;i++){
			if (args[i].startsWith("-")){
				if (args[i].substring(1).equals("fs")){fs = true;}
				else if (args[i].substring(1).equals("opengl")){fs = true;}
				else if (args[i].substring(1).equals("noaa")){aa = false;}
				else if (args[i].substring(1).equals("h") || args[i].substring(1).equals("--help")){TiledImageViewer.printCmdLineHelp();System.exit(0);}
			}
            else {
                // the only other thing allowed as a cmd line param is a scene file
                File f = new File(args[i]);
                if (f.exists()){
                    if (f.isDirectory()){
                        // if arg is a directory, take first xml file we find in that directory
                        String[] xmlFiles = f.list(new FilenameFilter(){
                                                public boolean accept(File dir, String name){return name.endsWith(".xml");}
                                            });
                        if (xmlFiles.length > 0){
                            xmlSceneFile = new File(f, xmlFiles[0]);
                        }
                    }
                    else {
                        xmlSceneFile = f;                        
                    }
                }
            }
		}
		if (ogl){
		    System.setProperty("sun.java2d.opengl", "True");
		}
        if (!fs && Utilities.osIsMacOS()){
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        System.out.println("--help for command line options");
        new TiledImageViewer(fs, ogl, aa, xmlSceneFile);
    }
    
    private static void printCmdLineHelp(){
		System.out.println("Usage:\n\tjava -Xmx1024M -Xms512M -cp target/timingframework-1.0.jar:zuist-engine-0.2.0-SNAPSHOT.jar:target/:target/:target/zvtm-0.10.0-SNAPSHOT.jar <path_to_scene_dir> [-fs] [-opengl]");
		System.out.println("\n\t-fs: fullscreen mode");
		System.out.println("\t-opengl: use Java2D OpenGL rendering pipeline (Java 6+Linux/Windows), requires that -Dsun.java2d.opengl=true be set on cmd line");
    }

}

class WEGlassPane extends JComponent implements ProgressListener {
    
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
    
    TiledImageViewer application;
    
    static final Font GLASSPANE_FONT = new Font("Arial", Font.PLAIN, 12);

    WEGlassPane(TiledImageViewer app){
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
            g2.setFont(GLASSPANE_FONT);
            g2.drawString(msg, msgX, msgY);
        }
        g2.setPaint(PROGRESS_GRADIENT);
        g2.fillRect(prX, prY, prW, BAR_HEIGHT);
        g2.setColor(MSG_COLOR);
        g2.drawRect(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }
    
}

class Overlay implements ViewEventHandler {
    
    static final Color FADE_REGION_FILL = Color.BLACK;
    static final Color FADE_REGION_STROKE = Color.WHITE;

    static final String INSITU_LOGO_PATH = "/images/insitu.png";
    static final String INRIA_LOGO_PATH = "/images/inria.png";

    TiledImageViewer application;

    Overlay(TiledImageViewer app){
        this.application = app;
    }

    boolean showingAbout = false;
    VRectangleST fadeAbout;
    VImage insituLogo, inriaLogo;
    VText[] aboutLines;
    
    void showAbout(){
        if (!showingAbout){
            fadeAbout = new VRectangleST(0, 0, 0, Math.round(application.panelWidth/2.1), Math.round(application.panelHeight/3),
                FADE_REGION_FILL, FADE_REGION_STROKE, 0.85f);
            aboutLines = new VText[5];
			aboutLines[0] = new VText(0, 150, 0, Color.WHITE, "ZUIST Tiled Image Viewer", VText.TEXT_ANCHOR_MIDDLE, 4.0f);
            aboutLines[1] = new VText(0, 110, 0, Color.WHITE, "v"+Messages.VERSION, VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            aboutLines[2] = new VText(0, 0, 0, Color.WHITE, "By Emmanuel Pietriga (INRIA) & Michel Beaudouin-Lafon (Universit\u00E9 Paris-Sud)", VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            RImage.setReflectionHeight(0.7f);
            inriaLogo = new RImage(-150, -70, 0, (new ImageIcon(this.getClass().getResource(INRIA_LOGO_PATH))).getImage(), 1.0f);
            insituLogo = new RImage(200, -70, 0, (new ImageIcon(this.getClass().getResource(INSITU_LOGO_PATH))).getImage(), 1.0f);
            aboutLines[3] = new VText(0, -170, 0, Color.WHITE, "Based on the ZVTM toolkit", VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            aboutLines[4] = new VText(0, -200, 0, Color.WHITE, "http://zvtm.sf.net", VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            VirtualSpaceManager.INSTANCE.addGlyph(fadeAbout, application.aboutSpace);
            VirtualSpaceManager.INSTANCE.addGlyph(inriaLogo, application.aboutSpace);
            VirtualSpaceManager.INSTANCE.addGlyph(insituLogo, application.aboutSpace);
			for (int i=0;i<aboutLines.length;i++){
	            VirtualSpaceManager.INSTANCE.addGlyph(aboutLines[i], application.aboutSpace);				
			}
            showingAbout = true;
        }
		application.mView.setActiveLayer(1);
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
