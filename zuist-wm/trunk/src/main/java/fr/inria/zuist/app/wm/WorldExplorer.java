/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.wm;

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
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.ImageIcon;
import java.awt.geom.Point2D;

import java.util.Vector;

import java.io.File;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.widgets.TranslucentTextArea;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.EndAction;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.ProgressListener;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.geotools.factory.GeoTools;

import org.geonames.Toponym;
import org.geonames.InsufficientStyleException;

/**
 * @author Emmanuel Pietriga
 */

public class WorldExplorer implements Java2DPainter {
    
    String PATH_TO_HIERARCHY = "data/tgt";
    String PATH_TO_SCENE = PATH_TO_HIERARCHY + "/wm_scene.xml";
    File SCENE_FILE = new File(PATH_TO_SCENE);
        
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
    
    boolean SHOW_MEMORY_USAGE = false;
    
    boolean UPDATE_MAPS = true;
    
    /* ZVTM objects */
    VirtualSpaceManager vsm;
    static final String mSpaceName = "BMNG Layer";
    static final String bSpaceName = "Boundary Layer";
    VirtualSpace mSpace, bSpace;
    Camera mCamera, bCamera, ovCamera;
    static final String mViewName = "World Explorer";
    View mView;
    ExplorerEventHandler eh;

    SceneManager sm;
    GeoToolsManager gm;
    NavigationManager nm;
    AirTrafficManager ga;
    
    WEGlassPane gp;
    
    TranslucentTextArea console;
    
    boolean antialiasing = false;
    
    public WorldExplorer(boolean queryGN, short lad, boolean air,
                         boolean fullscreen, boolean opengl, boolean aa, File xmlSceneFile){
        VirtualSpaceManager.INSTANCE.getAnimationManager().setResolution(80);
        nm = new NavigationManager(this);
        initGUI(fullscreen, opengl, aa);
        gp = new WEGlassPane(this);
        ((JFrame)mView.getFrame()).setGlassPane(gp);
        gp.setValue(0);
        gp.setVisible(true);
        VirtualSpace[]  sceneSpaces = {mSpace};
        Camera[] sceneCameras = {mCamera};
		sm = new SceneManager(sceneSpaces, sceneCameras);
		if (xmlSceneFile != null){
            gp.setLabel("Loading "+xmlSceneFile.getName());
            sm.loadScene(parseXML(xmlSceneFile), xmlSceneFile.getParentFile(), true, gp);
		}
        gm = new GeoToolsManager(this, queryGN, lad);
        ga = new AirTrafficManager(this, air);
        gp.setVisible(false);
        gp.setLabel(WEGlassPane.EMPTY_STRING);
        mCamera.setAltitude(9000.0f);
        EndAction ea  = new EndAction(){
               public void execute(Object subject, Animation.Dimension dimension){
                   sm.setUpdateLevel(true);
                   sm.enableRegionUpdater(true);
               }
           };
        nm.getGlobalView(ea);
        eh.cameraMoved(mCamera, null, 0);
		nm.createOverview();
		nm.updateOverview();
        console.setVisible(true);
    }

    void initGUI(boolean fullscreen, boolean opengl, boolean aa){
        windowLayout();
        antialiasing = aa;
        vsm = VirtualSpaceManager.INSTANCE;
        mSpace = vsm.addVirtualSpace(mSpaceName);
        bSpace = vsm.addVirtualSpace(bSpaceName);
        mCamera = mSpace.addCamera();
        ovCamera = mSpace.addCamera();
		bCamera = bSpace.addCamera();
        Vector cameras = new Vector();
        cameras.add(mCamera);
        cameras.add(bCamera);
        //mCamera.stick(bCamera, true);
        mView = vsm.addFrameView(cameras, mViewName, (opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H, false, false, !fullscreen, null);
        if (fullscreen && GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
		mView.setAntialiasing(antialiasing);
        eh = new ExplorerEventHandler(this);
        mCamera.addListener(eh);
        mView.setListener(eh, 0);
        mView.setListener(eh, 1);
        mView.setBackgroundColor(BACKGROUND_COLOR);
		mView.getCursor().setColor(Color.WHITE);
		mView.getCursor().setHintColor(Color.WHITE);
		mView.getCursor().setDynaSpotColor(Color.WHITE);
        mView.getCursor().setDynaSpotLagTime(200);
		mView.getCursor().activateDynaSpot(true);
        mView.setJava2DPainter(this, Java2DPainter.AFTER_PORTALS);
        updatePanelSize();
        // console
        JLayeredPane lp = ((JFrame)mView.getFrame()).getRootPane().getLayeredPane();
        console = new TranslucentTextArea(2, 80);
        console.setForeground(Color.WHITE);
        console.setBackground(Color.BLACK);
        lp.add(console, (Integer)(JLayeredPane.DEFAULT_LAYER+50));
        console.setBounds(20, panelHeight-100, panelWidth-250, 96);
        console.setMargin(new java.awt.Insets(5,5,5,5));
        console.setVisible(false);
        mView.setActiveLayer(1);
        mView.getPanel().addComponentListener(eh);
    }

    void windowLayout(){
        if (Utils.osIsWindows()){
            VIEW_X = VIEW_Y = 0;
        }
        else if (Utils.osIsMacOS()){
            VIEW_X = 80;
            SCREEN_WIDTH -= 80;
        }
        VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
        VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }
    
    /* tells whether antialising was requested at launch time or not */
    boolean isAAEnabled(){
        return antialiasing;
    }
    
        
    ClosedShape selectedFeature = null;
    
    void displayFeatureInfo(Toponym feature, Glyph g){
        if (feature != null){
            String t = feature.getName()+", "+feature.getCountryName()+
                       "\nLatitude: "+feature.getLatitude()+
                       "\nLongitude: "+feature.getLongitude();
            try {
                if (feature.getPopulation() != null){
                    t += "\nPopulation: "+feature.getPopulation();
                }
            }
            catch (InsufficientStyleException e){}
            unselectOldFeature();
            selectedFeature = (ClosedShape)g;
            selectedFeature.setColor(GeoNamesParser.SELECTED_FEATURE_COLOR);
            console.setText(t);
        }
        else {
            console.setText(null);
            unselectOldFeature();
        }
    }
    
    void unselectOldFeature(){
        if (selectedFeature != null){
            selectedFeature.setColor(GeoNamesParser.FEATURE_COLOR);
            selectedFeature = null;
        }
    }
    
    /*-------------     Navigation       -------------*/
    
    boolean isDynaspotEnabled(){
        return true;
    }

    
    void altitudeChanged(){}
    
    void updatePanelSize(){
        Dimension d = mView.getPanel().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
        if (nm != null && nm.ovPortal != null){
            nm.ovPortal.moveTo(panelWidth-nm.ovPortal.getDimensions().width-1, panelHeight-nm.ovPortal.getDimensions().height-1);            
        }
        if (console != null){
            console.setBounds(20, panelHeight-100, panelWidth-250, 96);
        }
    }
    
    void toggleMemoryUsageDisplay(){
        SHOW_MEMORY_USAGE = !SHOW_MEMORY_USAGE;
        vsm.repaint();
    }

    void toggleUpdateMaps(){
        UPDATE_MAPS = !UPDATE_MAPS;
        sm.setUpdateLevel(UPDATE_MAPS);
    }
    
    void gc(){
        System.gc();
        if (SHOW_MEMORY_USAGE){
            vsm.repaint();
        }
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

    static void printCmdLineHelp(){
        System.out.println("Usage:\n\tjava -Xmx1024M -Xms512M -jar target/zuist-wm-X.X.X.jar <path_to_scene_dir> [options]");
		System.out.println("\n\t-qgn: query geonames.org Web service");
		System.out.println("\t-ladN: display N-th level administrative division boundaries (countries, states, provinces, ...)");
		System.out.println("\t-air[N]: display air traffic data, optional N integer value represents min route weight to be displayed");
		System.out.println("\t-fs: run application fullscreen");
		System.out.println("\t-opengl: use Java2D OpenGL pipeline for rendering");
        System.out.println("\t-aa: enable antialiasing");
		System.exit(0);
    }

    public static void main(String[] args){
        File xmlSceneFile = null;
		boolean fs = false;
		boolean ogl = false;
		boolean aa = false;
		boolean queryGN = false;
		short lad = -1;
		boolean air = false;
		for (int i=0;i<args.length;i++){
			if (args[i].startsWith("-")){
				if (args[i].substring(1).equals("fs")){fs = true;}
				else if (args[i].substring(1).equals("opengl")){ogl = true;}
				else if (args[i].substring(1).equals("aa")){aa = true;}
				else if (args[i].substring(1).equals("qgn")){queryGN = true;}
				else if (args[i].substring(1).startsWith("lad")){lad = Short.parseShort(args[i].substring(4));}
				else if (args[i].substring(1).startsWith("air")){
				    air = true;
				    if (args[i].length() > 4){AirTrafficManager.MIN_WEIGHT = Integer.parseInt(args[i].substring(4));}
				}
				else if (args[i].substring(1).equals("h") || args[i].substring(1).equals("--help")){WorldExplorer.printCmdLineHelp();System.exit(0);}
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
        if (!fs && Utils.osIsMacOS()){
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        System.out.println("--help for command line options");
        System.out.println("Using GeoTools v" + GeoTools.getVersion());
        new WorldExplorer(queryGN, lad, air, fs, ogl, aa, xmlSceneFile);
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
    
    WorldExplorer application;
    
    static final Font GLASSPANE_FONT = new Font("Arial", Font.PLAIN, 12);

    WEGlassPane(WorldExplorer app){
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
