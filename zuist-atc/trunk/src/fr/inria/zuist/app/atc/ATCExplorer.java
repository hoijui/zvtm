/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.atc;

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
import javax.swing.ImageIcon;

import java.util.Vector;

import java.io.File;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VSegment;
import com.xerox.VTM.glyphs.VImage;
import net.claribole.zvtm.engine.Java2DPainter;
import net.claribole.zvtm.widgets.TranslucentWidget;

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

public class ATCExplorer implements Java2DPainter {
    
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

	static final Color BACKGROUND_COLOR = Color.GRAY;
	
	static final Font GRAPH_FONT = new Font("Arial", Font.PLAIN, 8);
    
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
    NavigationManager nm;
	GraphManager grm;
    
    WEGlassPane gp;

    public ATCExplorer(boolean fullscreen, boolean grid, String dir){
        if (dir != null){
            PATH_TO_HIERARCHY = dir;
            PATH_TO_SCENE = PATH_TO_HIERARCHY + "/wm_scene.xml";
            SCENE_FILE = new File(PATH_TO_SCENE);
        }
        nm = new NavigationManager(this);
        initGUI(fullscreen);
        gp = new WEGlassPane(this);
        ((JFrame)mView.getFrame()).setGlassPane(gp);
        gp.setValue(0);
        gp.setVisible(true);
        VirtualSpace[]  sceneSpaces = {mSpace, bSpace};
        Camera[] sceneCameras = {mCamera, bCamera};
		vsm.addGlyph(new VImage(0, 0, 0, (new ImageIcon(PATH_TO_HIERARCHY+"/0-0-0-0-0.jpg")).getImage(), 20), mSpace);
        sm = new SceneManager(vsm, sceneSpaces, sceneCameras);
        sm.setSceneCameraBounds(mCamera, eh.wnes);
        sm.setSceneCameraBounds(bCamera, eh.wnes);
        sm.loadScene(parseXML(SCENE_FILE), new File(PATH_TO_HIERARCHY), gp);
        if (grid){buildGrid();}
		grm = new GraphManager(this);
		grm.loadAirTraffic();
        gp.setVisible(false);
        gp.setLabel(WEGlassPane.EMPTY_STRING);
        mCamera.setAltitude(9000.0f);
        vsm.getGlobalView(mCamera, ANIM_MOVE_DURATION);
        eh.cameraMoved();
		nm.createOverview();
    }

    void initGUI(boolean fullscreen){
        windowLayout(fullscreen);
        vsm = new VirtualSpaceManager();
		vsm.setMainFont(GRAPH_FONT);
        mSpace = vsm.addVirtualSpace(mSpaceName);
        bSpace = vsm.addVirtualSpace(bSpaceName);
        mCamera = vsm.addCamera(mSpace);
        ovCamera = vsm.addCamera(mSpace);
		bCamera = vsm.addCamera(bSpace);
        Vector cameras = new Vector();
        cameras.add(mCamera);
        cameras.add(bCamera);
        mCamera.stick(bCamera, true);
        mView = vsm.addExternalView(cameras, mViewName, View.STD_VIEW, VIEW_W, VIEW_H, false, false, false, null);
        if (fullscreen){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        eh = new ExplorerEventHandler(this);
        mView.setEventHandler(eh, 1);
        mView.setNotifyMouseMoved(true);
        mView.setBackgroundColor(BACKGROUND_COLOR);
        mView.setJava2DPainter(this, Java2DPainter.AFTER_PORTALS);
		mView.setAntialiasing(true);
        vsm.animator.setAnimationListener(eh);
        updatePanelSize();
		mView.setActiveLayer(1);
    }

	void windowLayout(boolean fullscreen){
		if (fullscreen){
			VIEW_W = SCREEN_WIDTH;
			VIEW_H = SCREEN_HEIGHT;
		}
		else {
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
	}
    
    static final int GRID_STEP = 2160;
    
    void buildGrid(){
        for (int i=-43200;i<=43200;){
            vsm.addGlyph(new VSegment(i, 0, 0, 0, 21600, Color.RED), bSpace);
            i += GRID_STEP;
        }
        for (int i=-21600;i<=21600;){
            vsm.addGlyph(new VSegment(0, i, 0, 43200, 0, Color.RED), bSpace);
            i += GRID_STEP;
        }
    }
    
    /*-------------     Navigation       -------------*/

    void getGlobalView(){
        vsm.getGlobalView(mCamera, ATCExplorer.ANIM_MOVE_DURATION);
    }

    /* Higher view */
    void getHigherView(){
        Float alt = new Float(mCamera.getAltitude() + mCamera.getFocal());
        vsm.animator.createCameraAnimation(ATCExplorer.ANIM_MOVE_DURATION, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
    }

    /* Higher view */
    void getLowerView(){
        Float alt=new Float(-(mCamera.getAltitude() + mCamera.getFocal())/2.0f);
        vsm.animator.createCameraAnimation(ATCExplorer.ANIM_MOVE_DURATION, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
    }

    /* Direction should be one of ATCExplorer.MOVE_* */
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
        vsm.animator.createCameraAnimation(ATCExplorer.ANIM_MOVE_DURATION, AnimManager.CA_TRANS_SIG, trans, mCamera.getID());
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

	boolean SHOW_BREADCRUMB = true;

    /*Java2DPainter interface*/
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
        if (SHOW_MEMORY_USAGE){showMemoryUsage(g2d, viewWidth, viewHeight);}
		if (SHOW_BREADCRUMB){showBreadCrumb(g2d, viewWidth, viewHeight);}
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

	static final Font PATH_FONT = new Font("Arial", Font.PLAIN, 10);
	static final Color PATH_BG_COLOR = Color.BLACK;
	static final Color PATH_FG_COLOR = Color.WHITE;
	static final String EMPTY_STRING = "";
	static final String BREADCRUMB_SEP = "> ";
	String bcStr = BREADCRUMB_SEP;

    void showBreadCrumb(Graphics2D g2d, int viewWidth, int viewHeight){
		g2d.setComposite(TranslucentWidget.AB_08);
		g2d.setColor(PATH_BG_COLOR);
		g2d.fillRect(1, 1, viewWidth-2, 15);
		g2d.setComposite(TranslucentWidget.AB_10);
		g2d.setColor(PATH_FG_COLOR);
		g2d.drawRect(0, 0, viewWidth-1, 16);
		if (bcStr.length() > 0){
			g2d.setFont(PATH_FONT);
			g2d.drawString(bcStr, 8, 12);
		}
	}
	
	void updateBreadCrumb(){
		if (nm.broughtStack.size() > 0){
			bcStr = EMPTY_STRING;
			for (int i=0;i<nm.broughtStack.size();i++){
				bcStr += BREADCRUMB_SEP + ((LNode)nm.broughtStack.elementAt(i)).name;
			}
		}
		else {
			Glyph g = mView.getPanel().lastGlyphEntered();
			if (g != null && g.getOwner() != null){
				bcStr = BREADCRUMB_SEP + ((LNode)g.getOwner()).name;
			}
			else {
				bcStr = EMPTY_STRING;
			}
		}
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
		org.apache.log4j.BasicConfigurator.configure();
        String dir = (args.length > 0) ? args[0] : null;
        boolean fs = (args.length > 1) ? Boolean.parseBoolean(args[1]) : false;
        boolean grid = (args.length > 2) ? Boolean.parseBoolean(args[2]) : false;
		if (args.length > 3){GraphManager.MAX_WEIGHT = Integer.parseInt(args[3]);}
		if (dir == null){
			System.out.println("Usage:\n\tjava -Xmx1024M -Xms512M -jar target/zuist-atc.jar <path_to_scene_dir> [fs] [grid] [weight]");
			System.out.println("\n\tfs: fullscreen: true or false");
			System.out.println("\tgrid: draw a grid on top of the map: true or false");
			System.out.println("\tweight: display only flight routes with a weight superior to this value");
			System.out.println();
			System.exit(0);
		}
		new ATCExplorer(fs, grid, dir);
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
    
    ATCExplorer application;
    
    static final Font GLASSPANE_FONT = new Font("Arial", Font.PLAIN, 12);

    WEGlassPane(ATCExplorer app){
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
