/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.wm;

import java.io.File;
import java.io.IOException;

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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.ImageIcon;

import java.util.Vector;

import java.io.File;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.glyphs.VSegment;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.ClosedShape;
import com.xerox.VTM.glyphs.VImage;
import net.claribole.zvtm.engine.Java2DPainter;
import net.claribole.zvtm.widgets.TranslucentTextArea;
import net.claribole.zvtm.animation.Animation;
import net.claribole.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

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
    
    /* Navigation constants */
    static final int ANIM_MOVE_DURATION = 300;
    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;
    
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
    
    WEGlassPane gp;
    
    TranslucentTextArea console;

    public WorldExplorer(boolean queryGN, boolean fullscreen, boolean grid, boolean opengl, String dir){
        if (dir != null){
            PATH_TO_HIERARCHY = dir;
            PATH_TO_SCENE = PATH_TO_HIERARCHY + "/wm_scene.xml";
            SCENE_FILE = new File(PATH_TO_SCENE);
        }
        nm = new NavigationManager(this);
        initGUI(fullscreen, opengl);
        gp = new WEGlassPane(this);
        ((JFrame)mView.getFrame()).setGlassPane(gp);
        gp.setValue(0);
        gp.setVisible(true);
        VirtualSpace[]  sceneSpaces = {mSpace, bSpace};
        Camera[] sceneCameras = {mCamera, bCamera};
		vsm.addGlyph(new VImage(0, 0, 0, (new ImageIcon(PATH_TO_HIERARCHY+"/0-0-0-0-0.jpg")).getImage(), 20), mSpace);
        sm = new SceneManager(sceneSpaces, sceneCameras);
        sm.setSceneCameraBounds(mCamera, eh.wnes);
        sm.setSceneCameraBounds(bCamera, eh.wnes);
        sm.loadScene(parseXML(SCENE_FILE), new File(PATH_TO_HIERARCHY), gp);
        if (grid){buildGrid();}
        gm = new GeoToolsManager(this, queryGN);
        gp.setVisible(false);
        gp.setLabel(WEGlassPane.EMPTY_STRING);
        mCamera.setAltitude(9000.0f);
        vsm.getGlobalView(mCamera, ANIM_MOVE_DURATION);
        eh.cameraMoved(null, null, 0);
		nm.createOverview();
        console.setVisible(true);
        mCamera.addListener(eh);
    }

    void initGUI(boolean fullscreen, boolean opengl){
        windowLayout();
        vsm = VirtualSpaceManager.INSTANCE;
        mSpace = vsm.addVirtualSpace(mSpaceName);
        bSpace = vsm.addVirtualSpace(bSpaceName);
        mCamera = vsm.addCamera(mSpace);
        ovCamera = vsm.addCamera(mSpace);
		bCamera = vsm.addCamera(bSpace);
        Vector cameras = new Vector();
        cameras.add(mCamera);
        cameras.add(bCamera);
        mCamera.stick(bCamera, true);
        mView = vsm.addExternalView(cameras, mViewName, (opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H, false, false, true, null);
        if (fullscreen){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        eh = new ExplorerEventHandler(this);
        mView.setEventHandler(eh, 0);
        mView.setEventHandler(eh, 1);
        mView.setNotifyMouseMoved(true);
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
    
    static final int GRID_STEP = 2160;
    
    void buildGrid(){
        for (int i=-43200;i<=43200;){
            VSegment s = new VSegment(i, 0, 0, 0, 21600, Color.RED);
            s.setSensitivity(false);
            vsm.addGlyph(s, bSpace);
            i += GRID_STEP;
        }
        for (int i=-21600;i<=21600;){
            VSegment s = new VSegment(0, i, 0, 43200, 0, Color.RED);
            s.setSensitivity(false);
            vsm.addGlyph(s, bSpace);
            i += GRID_STEP;
        }
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

    void getGlobalView(){
        vsm.getGlobalView(mCamera, WorldExplorer.ANIM_MOVE_DURATION);
    }

    /* Higher view */
    void getHigherView(){
        Float alt = new Float(mCamera.getAltitude() + mCamera.getFocal());
        //vsm.animator.createCameraAnimation(WorldExplorer.ANIM_MOVE_DURATION, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(WorldExplorer.ANIM_MOVE_DURATION, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Higher view */
    void getLowerView(){
        Float alt=new Float(-(mCamera.getAltitude() + mCamera.getFocal())/2.0f);
        //vsm.animator.createCameraAnimation(WorldExplorer.ANIM_MOVE_DURATION, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(WorldExplorer.ANIM_MOVE_DURATION, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Direction should be one of WorldExplorer.MOVE_* */
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
        //vsm.animator.createCameraAnimation(WorldExplorer.ANIM_MOVE_DURATION, AnimManager.CA_TRANS_SIG, trans, mCamera.getID());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(WorldExplorer.ANIM_MOVE_DURATION, mCamera,
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
    
    void toggleMemoryUsageDisplay(){
        SHOW_MEMORY_USAGE = !SHOW_MEMORY_USAGE;
        vsm.repaintNow();
    }

    void toggleUpdateMaps(){
        UPDATE_MAPS = !UPDATE_MAPS;
        sm.setUpdateLevel(UPDATE_MAPS);
    }
    
    void gc(){
        System.gc();
        if (SHOW_MEMORY_USAGE){
            vsm.repaintNow();
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

    public static void main(String[] args){
        String dir = (args.length > 0) ? args[0] : null;
        boolean queryGN = (args.length > 1) ? Boolean.parseBoolean(args[1]) : false;
        boolean fs = (args.length > 2) ? Boolean.parseBoolean(args[2]) : false;
        boolean grid = (args.length > 3) ? Boolean.parseBoolean(args[3]) : false;
        boolean ogl = (args.length > 4) ? Boolean.parseBoolean(args[3]) : false;
        System.out.println("Using GeoTools v" + GeoTools.getVersion() );
		if (dir == null){
			System.out.println("Usage:\n\tjava -Xmx1024M -Xms512M -jar target/zuist-wm-X.X.X.jar <path_to_scene_dir> [queryGN] [fs] [grid] [opengl]");
			System.out.println("\n\tqgn: query geonames.org Web service: true or false");
			System.out.println("\tfs: fullscreen: true or false");
			System.out.println("\tgrid: draw a grid on top of the map: true or false");
			System.out.println("\topengl: use OpenGL: true or false");
			System.exit(0);
		}
        new WorldExplorer(queryGN, fs, grid, ogl, dir);
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
