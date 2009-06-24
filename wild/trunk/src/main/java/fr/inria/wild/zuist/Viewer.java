/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.wild.zuist;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Insets;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import java.awt.Container;

import java.util.Vector;
import java.util.HashMap;

import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.Utilities;
import fr.inria.zvtm.engine.SwingWorker;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.glyphs.PieMenu;
import fr.inria.zvtm.glyphs.PieMenuFactory;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.widgets.TranslucentTextArea;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.Level;
import fr.inria.zuist.engine.RegionListener;
import fr.inria.zuist.engine.LevelListener;
import fr.inria.zuist.engine.ProgressListener;
import fr.inria.zuist.engine.ObjectDescription;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;

/**
 * @author Emmanuel Pietriga
 */

public class Viewer implements Java2DPainter, RegionListener, LevelListener {
    
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
    
    /* Navigation constants */
    static final int ANIM_MOVE_LENGTH = 300;
    
    /* ZVTM objects */
    VirtualSpaceManager vsm;
    static final String mSpaceName = "Scene Space";
    VirtualSpace mSpace;
    Camera mCamera;
    String mCameraAltStr = Messages.ALTITUDE + "0";
    String levelStr = Messages.LEVEL + "0";
    static final String mViewName = "ZUIST4WILD Viewer";
    View mView;
    ViewerEventHandler eh;

    SceneManager sm;

	VWGlassPane gp;
	
	long cameraXOffset = 0;
	long cameraYOffset = 0;
	
	OSCPortIn receiver;
	int oscPort = 0;
    
    public Viewer(short screen, long cx, long cy, boolean opengl, boolean antialiased, File xmlSceneFile, int port){
		this.cameraXOffset = cx;
		this.cameraYOffset = cy;
		initGUI(screen, opengl, antialiased);
		initOSCListener(port);
        VirtualSpace[]  sceneSpaces = {mSpace};
        Camera[] sceneCameras = {mCamera};
        sm = new SceneManager(sceneSpaces, sceneCameras);
        sm.setSceneCameraBounds(mCamera, eh.wnes);
        sm.setRegionListener(this);
        sm.setLevelListener(this);
		previousLocations = new Vector();
        if (xmlSceneFile != null){
			loadScene(xmlSceneFile);
		}
        mCamera.addListener(eh);
    }

    void initGUI(short screen, boolean opengl, boolean antialiased){
        windowLayout();
        vsm = VirtualSpaceManager.INSTANCE;
        mSpace = vsm.addVirtualSpace(mSpaceName);
        mCamera = vsm.addCamera(mSpace);
        Vector cameras = new Vector();
        cameras.add(mCamera);
        mView = vsm.addExternalView(cameras, mViewName + " [" + oscPort + "]",
            (opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H, false, false, (screen == -1), null);
        if (screen != -1){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[screen].setFullScreenWindow((JFrame)mView.getFrame());            
        }
        else {
            mView.setVisible(true);
        }
        updatePanelSize();
		gp = new VWGlassPane(this);
		((JFrame)mView.getFrame()).setGlassPane(gp);
        eh = new ViewerEventHandler(this);
        mView.setEventHandler(eh, 0);
        mView.setNotifyMouseMoved(true);
        mView.setBackgroundColor(Color.WHITE);
		mView.setAntialiasing(antialiased);
		mView.setJava2DPainter(this, Java2DPainter.AFTER_PORTALS);
		mView.getPanel().addComponentListener(eh);
		ComponentAdapter ca0 = new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				updatePanelSize();
			}
		};
		mView.getFrame().addComponentListener(ca0);
		initConsole();
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
    
    void initOSCListener(int port){
        oscPort = port;
        try {
            receiver = new OSCPortIn(port);
            OSCListener listener = new OSCListener() {
                public void acceptMessage(java.util.Date time, OSCMessage message){
                    processMessage(message);
                }
            };
            receiver.addListener(Controller.MOVE_CAMERA, listener);
            receiver.startListening();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /* -------------- Console ------------------------*/
    
    TranslucentTextArea console;

	// west, east and south margins of console (north bound depends on frame's height)
	static final int[] consoleMarginsWES = {10, 10, 10};
	static final int[] consolePaddingWNES = {5, 5, 5, 5};

	void initConsole(){
		JFrame f = (JFrame)mView.getFrame();
		JLayeredPane lp = f.getRootPane().getLayeredPane();
		console = new TranslucentTextArea("");
		console.setEditable(false);
		console.setMargin(new Insets(consolePaddingWNES[1], consolePaddingWNES[0], consolePaddingWNES[3], consolePaddingWNES[2]));
		lp.add(console, (Integer)(JLayeredPane.DEFAULT_LAYER+33));
		updateConsoleBounds();
		console.setVisible(false);
	}
	
	void updateConsoleBounds(){
	    if (console == null){return;}
		console.setBounds(consoleMarginsWES[0], Math.round(panelHeight*.8f),
		                  panelWidth-consoleMarginsWES[1]-consoleMarginsWES[0], Math.round(panelHeight*.05f-consoleMarginsWES[2]));
	}
	
	void toggleConsole(){
		console.setVisible(!console.isVisible());
	}
	
	void sayInConsole(String text){
		console.setText(text);
	}
	

	/*-------------  Scene management    -------------*/
	
	void reset(){
		sm.reset();
		vsm.removeGlyphsFromSpace(mSpaceName);
	}
	
	void loadScene(File xmlSceneFile){
		try {
			mView.setTitle(mViewName + " - " + xmlSceneFile.getCanonicalPath() + " [" + oscPort + "]");			
		}
		catch (IOException ex){}
		gp.setValue(0);
		gp.setVisible(true);
		SCENE_FILE = xmlSceneFile;
	    SCENE_FILE_DIR = SCENE_FILE.getParentFile();
	    sm.loadScene(parseXML(SCENE_FILE), SCENE_FILE_DIR, true, gp);
	    HashMap sceneAttributes = sm.getSceneAttributes();
	    if (sceneAttributes.containsKey(SceneManager._background)){
	        mView.setBackgroundColor((Color)sceneAttributes.get(SceneManager._background));
	    }
		MAX_NB_REQUESTS = sm.getObjectCount() / 100;
	    gp.setVisible(false);
	    gp.setLabel(VWGlassPane.EMPTY_STRING);
        mCamera.setAltitude(0.0f);
        sm.updateLevel(mCamera.altitude);
        eh.cameraMoved(null, null, 0);
	}
    
    /*-------------     Navigation       -------------*/
    
    void processMessage(OSCMessage msg){
        Object[] params = msg.getArguments();
        String cmd = (String)params[0];
        if (cmd.equals(Controller.CMD_CENTER_REGION)){
            centerOnRegion(Long.parseLong((String)params[1]), Long.parseLong((String)params[2]), Long.parseLong((String)params[3]), Long.parseLong((String)params[4]));
        }
        else if (cmd.equals(Controller.CMD_MOVE_WEST)){
            translateView(Controller.MOVE_WEST);
        }
        else if (cmd.equals(Controller.CMD_MOVE_NORTH)){
            translateView(Controller.MOVE_NORTH);
        }
        else if (cmd.equals(Controller.CMD_MOVE_EAST)){
            translateView(Controller.MOVE_EAST);
        }
        else if (cmd.equals(Controller.CMD_MOVE_SOUTH)){
            translateView(Controller.MOVE_SOUTH);
        }
        else if (cmd.equals(Controller.CMD_TRANSLATION_SPEED)){
            firstOrderTranslate(((Integer)params[1]).intValue(), ((Integer)params[2]).intValue());
        }
        else if (cmd.equals(Controller.CMD_ZOOM_SPEED)){
            firstOrderZoom(((Integer)params[1]).intValue());
        }
        else if (cmd.equals(Controller.CMD_STOP)){
            stop();
        }
        else if (cmd.equals(Controller.CMD_GC)){
            gc();
        }
        else if (cmd.equals(Controller.CMD_INFO)){
            toggleMiscInfoDisplay();
        }
        else if (cmd.equals(Controller.CMD_CONSOLE)){
            toggleConsole();
        }
        else if (cmd.equals(Controller.CMD_QUIT)){
            System.exit(0);
        }
    }
    
    void centerOnRegion(long w, long n, long e, long s){
        vsm.centerOnRegion(mCamera, 0, w, n, e, s);
    }
    
    void firstOrderTranslate(int dragX, int dragY){
        float a = (mCamera.focal + Math.abs(mCamera.altitude)) / mCamera.focal;
        vsm.getAnimationManager().setXspeed((mCamera.altitude>0) ? (long)((dragX)*(a/50.0f)) : (long)((dragX)/(a*50)));
        vsm.getAnimationManager().setYspeed((mCamera.altitude>0) ? (long)((dragY)*(a/50.0f)) : (long)((dragY)/(a*50)));
    }
    
    void firstOrderZoom(int dragZ){
        float a = (mCamera.focal + Math.abs(mCamera.altitude)) / mCamera.focal;
        vsm.getAnimationManager().setZspeed((mCamera.altitude>0) ? (long)((dragZ)*(a/50.0f)) : (long)((dragZ)/(a*50)));
    }
    
    void stop(){
        vsm.getAnimationManager().setXspeed(0);
        vsm.getAnimationManager().setYspeed(0);
        vsm.getAnimationManager().setZspeed(0);
    }

    void getGlobalView(){
        sm.getGlobalView(mCamera, Viewer.ANIM_MOVE_LENGTH);
    }

    /* Higher view */
    void getHigherView(){
		rememberLocation(mCamera.getLocation());
        Float alt = new Float(mCamera.getAltitude() + mCamera.getFocal());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Viewer.ANIM_MOVE_LENGTH, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Higher view */
    void getLowerView(){
		rememberLocation(mCamera.getLocation());
        Float alt=new Float(-(mCamera.getAltitude() + mCamera.getFocal())/2.0f);
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Viewer.ANIM_MOVE_LENGTH, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Direction should be one of Viewer.MOVE_* */
    void translateView(short direction){
        LongPoint trans;
        long[] rb = mView.getVisibleRegion(mCamera);
        if (direction == Controller.MOVE_NORTH){
            long qt = Math.round((rb[1]-rb[3])/4.0);
            trans = new LongPoint(0,qt);
        }
        else if (direction == Controller.MOVE_SOUTH){
            long qt = Math.round((rb[3]-rb[1])/4.0);
            trans = new LongPoint(0,qt);
        }
        else if (direction == Controller.MOVE_EAST){
            long qt = Math.round((rb[2]-rb[0])/4.0);
            trans = new LongPoint(qt,0);
        }
        else {
            // direction == Controller.MOVE_WEST
            long qt = Math.round((rb[0]-rb[2])/4.0);
            trans = new LongPoint(qt,0);
        }
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(Viewer.ANIM_MOVE_LENGTH, mCamera,
            trans, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

	void centerOnObject(String id){
		ObjectDescription od = sm.getObject(id);
		if (od != null){
			Glyph g = od.getGlyph();
			if (g != null){
				rememberLocation(mCamera.getLocation());
				vsm.centerOnGlyph(g, mCamera, Viewer.ANIM_MOVE_LENGTH, true, 1.2f);				
			}
		}
	}

	void centerOnRegion(String id){
		Region r = sm.getRegion(id);
		if (r != null){
			Glyph g = r.getBounds();
			if (g != null){
				rememberLocation(mCamera.getLocation());
				vsm.centerOnGlyph(g, mCamera, Viewer.ANIM_MOVE_LENGTH, true, 1.2f);				
			}
		}		
	}

	Vector previousLocations;
	static final int MAX_PREV_LOC = 100;
	
	void rememberLocation(){
	    rememberLocation(mCamera.getLocation());
    }
    
	void rememberLocation(Location l){
		if (previousLocations.size() >= MAX_PREV_LOC){
			// as a result of release/click being undifferentiated)
			previousLocations.removeElementAt(0);
		}
		if (previousLocations.size()>0){
			if (!Location.equals((Location)previousLocations.lastElement(),l)){
                previousLocations.add(l);
            }
		}
		else {previousLocations.add(l);}
	}
	
	void moveBack(){		
		if (previousLocations.size()>0){
			Vector animParams = Location.getDifference(mSpace.getCamera(0).getLocation(), (Location)previousLocations.lastElement());
			sm.setUpdateLevel(false);
            class LevelUpdater implements EndAction {
                public void execute(Object subject, Animation.Dimension dimension){
                    sm.setUpdateLevel(true);
                    sm.updateLevel(mCamera.altitude);
                }
            }
            Animation at = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(Viewer.ANIM_MOVE_LENGTH, mSpace.getCamera(0),
                (LongPoint)animParams.elementAt(1), true, SlowInSlowOutInterpolator.getInstance(), null);
            Animation aa = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Viewer.ANIM_MOVE_LENGTH, mSpace.getCamera(0),
                (Float)animParams.elementAt(0), true, SlowInSlowOutInterpolator.getInstance(), new LevelUpdater());
            vsm.getAnimationManager().startAnimation(at, false);
            vsm.getAnimationManager().startAnimation(aa, false);
			previousLocations.removeElementAt(previousLocations.size()-1);
		}
	}
	
    void altitudeChanged(){
        sm.updateLevel(mCamera.altitude);
        mCameraAltStr = Messages.ALTITUDE + String.valueOf(mCamera.altitude);
    }
    
    void updatePanelSize(){
        Dimension d = mView.getPanel().getSize();
        panelWidth = d.width;
		panelHeight = d.height;
		updateConsoleBounds();
	}

	/* ---- Debug information ----*/
	
	public void enteredRegion(Region r){}

	public void exitedRegion(Region r){}

	public void enteredLevel(int depth){
	    levelStr = Messages.LEVEL + String.valueOf(depth);
	}

	public void exitedLevel(int depth){}
	
    long maxMem = Runtime.getRuntime().maxMemory();
    int totalMemRatio, usedMemRatio;	
    boolean SHOW_MISC_INFO = true;

    void toggleMiscInfoDisplay(){
        SHOW_MISC_INFO = !SHOW_MISC_INFO;
        vsm.repaintNow();
    }

	static final AlphaComposite acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
	static final Color MID_DARK_GRAY = new Color(64,64,64);

    void showMemoryUsage(Graphics2D g2d, int viewWidth, int viewHeight){
        totalMemRatio = (int)(Runtime.getRuntime().totalMemory() * 100 / maxMem);
        usedMemRatio = (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * 100 / maxMem);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(20,
            3,
            200,
            13);
        g2d.setColor(Viewer.MID_DARK_GRAY);
        g2d.fillRect(20,
            3,
            totalMemRatio * 2,
            13);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(20,
            3,
            usedMemRatio * 2,
            13);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(20,
            3,
            200,
            13);
        g2d.drawString(usedMemRatio + "%", 50, 14);
        g2d.drawString(totalMemRatio + "%", 100, 14);
        g2d.drawString(maxMem/1048576 + " Mb", 160, 14);	
    }

    // consider 1000 as the maximum number of requests that can be in the queue at any given time
    // 1000 is the default value ; adapt for each scene depending on the number of objects
    // as this could vary dramatically from one scene to another - see loadScene()
    float MAX_NB_REQUESTS = 1000;
    static final int REQ_QUEUE_BAR_WIDTH = 100;
    static final int REQ_QUEUE_BAR_HEIGHT = 6;
    
    static final Font INFO_FONT = new Font("Dialog", Font.PLAIN, 10);
    
    void showReqQueueStatus(Graphics2D g2d, int viewWidth, int viewHeight){
        float ratio = sm.getPendingRequestQueueSize()/(MAX_NB_REQUESTS);
        if (ratio > 1.0f){
            // do not go over gauge boundary, even if actual number of requests goes beyond MAX_NB_REQUESTS
            ratio = 1.0f;
        }
        g2d.setColor(Color.GRAY);
        g2d.fillRect(viewWidth-Math.round(REQ_QUEUE_BAR_WIDTH * ratio)-10, 7, Math.round(REQ_QUEUE_BAR_WIDTH * ratio), REQ_QUEUE_BAR_HEIGHT);
        g2d.drawRect(viewWidth-REQ_QUEUE_BAR_WIDTH-10, 7, REQ_QUEUE_BAR_WIDTH, REQ_QUEUE_BAR_HEIGHT);
    }
    
    void showAltitude(Graphics2D g2d, int viewWidth, int viewHeight){        
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(240,
            3,
            190,
            13);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(240,
            3,
            190,
            13);
        g2d.drawString(levelStr, 250, 14);
        g2d.drawString(mCameraAltStr, 310, 14);
    }

    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
        if (!SHOW_MISC_INFO){return;}
        g2d.setFont(INFO_FONT);
		g2d.setComposite(acST);
		showMemoryUsage(g2d, viewWidth, viewHeight);
		showReqQueueStatus(g2d, viewWidth, viewHeight);
		showAltitude(g2d, viewWidth, viewHeight);
		g2d.setComposite(Translucent.acO);
    }

	void gc(){
		System.gc();
		if (SHOW_MISC_INFO){
			vsm.repaintNow();
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
        File xmlSceneFile = null;
        short screen = 0;
		boolean ogl = false;
		boolean aa = true;
		long camX = 0;
		long camY = 0;
		// default port is supposed to be 57110
		int oscPort = OSCPortIn.defaultSCOSCPort();
		for (int i=0;i<args.length;i++){
			if (args[i].startsWith("-")){
			    // -screen=N with N in [0..X] where X is the number of displays (graphics device)
				if (args[i].substring(1).startsWith("screen")){screen = Short.parseShort(args[i].substring(8));}
				else if (args[i].substring(1).startsWith("port")){oscPort = Integer.parseInt(args[i].substring(6));}
				else if (args[i].substring(1).startsWith("camera")){
				    String[] coords = args[i].substring(8).split(",");
				    camX = Long.parseLong(coords[0]);
				    camY = Long.parseLong(coords[1]);
				}
				else if (args[i].substring(1).equals("opengl")){ogl = true;}
				else if (args[i].substring(1).equals("noaa")){aa = false;}
				else if (args[i].substring(1).equals("h") || args[i].substring(1).equals("--help")){Viewer.printCmdLineHelp();System.exit(0);}
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
        System.out.println("--help for command line options");
        System.out.println("Display set to screen "+screen);
        System.out.println("Listening for OSC instructions on port "+oscPort);
		System.out.println("OpenGL pipeline is "+((ogl) ? "ON" : "OFF"));
		System.out.println("Antialiasing is "+((aa) ? "ON" : "OFF"));
        new Viewer(screen, camX, camY, ogl, aa, xmlSceneFile, oscPort);
    }
    
    private static void printCmdLineHelp(){
        System.out.println("Usage:\n\tjava -Xmx1024M -Xms512M -jar target/zuist4wild-0.1.0-SNAPSHOT.jar <path_to_scene_dir> [options]");
        System.out.println("Options:\n\t-screen=N: N in [0..X] where X is the number of displays (graphics device)");
        System.out.println("\t-port=N: OSC commands listening port");
        System.out.println("\t-camera=x,y: relative coords w.r.t meta camera center in virtual space");
        System.out.println("\t-noaa: no antialiasing");
        System.out.println("\t-opengl: use Java2D OpenGL rendering pipeline (Java 6+Linux/Windows), requires that -Dsun.java2d.opengl=true be set on cmd line");
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

class Messages {
    
    static final String LEVEL = "Level ";
    
    static final String ALTITUDE = "Altitude ";
    
}
