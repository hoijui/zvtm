/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ViewerEventHandler.java 2008 2009-06-09 07:53:35Z epietrig $
 */

package fr.inria.wild.zuist;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Font;
import javax.swing.JComponent;
import javax.swing.JFrame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Dimension;

import java.util.Vector;
import java.util.HashMap;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;

import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortOut;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCListener;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.engine.CameraListener;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.ProgressListener;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class Controller implements Java2DPainter {
    
    static final int DEFAULT_VIEW_WIDTH = 1280;
    
    static final int DEFAULT_OSC_LISTENING_PORT = 57109;
    
    static final String MOVE_CAMERA = "/moveCam";
    static final String MOVE_META_CAMERA = "/moveMCam";

    static final float SPEED_FACTOR = 50f;
        
    static final short MOVE_WEST = 0;
    static final short MOVE_NORTH = 1;
    static final short MOVE_EAST = 2;
    static final short MOVE_SOUTH = 3;
    static final String CMD_MOVE_WEST = "west";
    static final String CMD_MOVE_NORTH = "north";
    static final String CMD_MOVE_EAST = "east";
    static final String CMD_MOVE_SOUTH = "south";
    static final String CMD_TRANSLATION_SPEED = "xyspeed";
    static final String CMD_ZOOM_SPEED = "zspeed";
    static final String CMD_STOP = "stop";
    static final String CMD_CENTER_REGION = "creg";
    static final String CMD_GC = "gc";
    static final String CMD_INFO = "info";
    static final String CMD_CONSOLE = "cs";
    static final String CMD_QUIT = "quit";
    
    static final String IN_CMD_STOP = CMD_STOP;
    static final String IN_CMD_PAN1 = "pan";
    static final String IN_CMD_ZOOM1 = "zoom";
    static final String IN_CMD_PAN0 = "pan0";
    static final String IN_CMD_ZOOM0 = "zoom0";
    static final String IN_CMD_GGV = "ggv";
    /* zoom origin */
    static final String IN_CMD_ZORIG_ENABLED = "zoe";
    static final String IN_CMD_ZORIG_DISABLED = "zod";
    static final String IN_CMD_ZORIG = "zo";
    
    static final Integer VALUE_NONE = new Integer(0);
    
    static final float VIEWFINDER_OPACITY = .3f;
    static final float VIEWPORT_OPACITY = .3f;
    
    File SCENE_FILE, SCENE_FILE_DIR;
    
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;
    
    VirtualSpaceManager vsm;

    static final String OVERVIEW_TITLE = "ZUIST4WILD Controller [Overview]";
    static final String CONTROL_VIEW_TITLE = "ZUIST4WILD Controller [Control]";
    static final String zSpaceName = "ZUIST Space";
    static final String rSpaceName = "Regions Space";

    VirtualSpace zSpace, rSpace;

    // control camera / view
    Camera cCamera;
    View cView;
    // overview camera / view
    Camera oCamera, rCamera;
    View oView;
    VRectangle viewFinder;
    VRectangle[] viewportFinders;

    ControllerEventHandler ceh;
    OverviewEventHandler oeh;
    
    // following arrays have same length, viewport at index i is associated with sender at index i
    ViewPort[] viewports;
    OSCPortOut[] senders;
    OSCPortIn receiver;
    
    SceneManager sm;
    
    WallConfiguration wc;
    boolean pixelsBehindBezels = false;
    
    CTGlassPane gp;
        
    public Controller(File configFile, File zuistFile, int OSCin, boolean opengl, boolean antialiased, boolean pbb){
        this.pixelsBehindBezels = pbb;
        wc = new WallConfiguration(configFile, pixelsBehindBezels);
        System.out.println("Wall size: "+wc.getSize().width+" x "+wc.getSize().height);
        initGUI(opengl, antialiased);
        initOSC(OSCin);
        initViewFinders();
        VirtualSpace[]  sceneSpaces = {zSpace};
        Camera[] sceneCameras = {cCamera};
        sm = new SceneManager(sceneSpaces, sceneCameras);
        if (zuistFile != null){
            sm.enableRegionUpdater(false);
			loadScene(zuistFile);
			EndAction ea  = new EndAction(){
                   public void execute(Object subject, Animation.Dimension dimension){
                       sm.setUpdateLevel(true);
                       sm.enableRegionUpdater(true);
                   }
               };
			getGlobalView(cCamera);
			getGlobalView(oCamera);
		}
        cCamera.addListener(ceh);
    }
    
    void initGUI(boolean opengl, boolean antialiased){
        vsm = VirtualSpaceManager.INSTANCE;
        // control view
        zSpace = vsm.addVirtualSpace(zSpaceName);
        cCamera = zSpace.addCamera();
        Vector cameras = new Vector();
        cameras.add(cCamera);
        short vt = (opengl) ? View.OPENGL_VIEW : View.STD_VIEW;
        cView = vsm.addFrameView(cameras, CONTROL_VIEW_TITLE, vt,
            DEFAULT_VIEW_WIDTH, Math.round(DEFAULT_VIEW_WIDTH * wc.getSize().height/((float)wc.getSize().width)),
            false, true, true, null);
        cView.setBackgroundColor(Color.WHITE);
        ceh = new ControllerEventHandler(this);
        cView.setEventHandler(ceh);
        cView.setNotifyMouseMoved(true);
        cView.setAntialiasing(antialiased);
		ComponentAdapter ca0 = new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				updatePanelSize();
			}
		};
		cView.getFrame().addComponentListener(ca0);
		gp = new CTGlassPane(this);
		((JFrame)cView.getFrame()).setGlassPane(gp);
        // overview
        rSpace = vsm.addVirtualSpace(rSpaceName);
        oCamera = zSpace.addCamera();
        rCamera = rSpace.addCamera();
        cameras = new Vector();
        cameras.add(oCamera);
        cameras.add(rCamera);
        oView = vsm.addFrameView(cameras, OVERVIEW_TITLE, vt,
            1440, 850,
            false, true);
        oView.setBackgroundColor(Color.WHITE);
        oeh = new OverviewEventHandler(this);
        oView.setEventHandler(oeh, 0);
        oView.setEventHandler(oeh, 1);
        oView.setNotifyMouseMoved(true);
        oView.setAntialiasing(antialiased);
        oView.setJava2DPainter(this, Java2DPainter.FOREGROUND);
        oCamera.stick(rCamera);
        vsm.repaintNow();
    }
    
    void initOSC(int in){
        // OSC senders (control of cameras)
        try {
            Vector vp = new Vector();
            Vector sd = new Vector();
            ClusterNode[] nodes = wc.getNodes();
            for (int i=0;i<nodes.length;i++){
                for (int j=0;j<nodes[i].getViewPorts().length;j++){
                    ViewPort p = nodes[i].getViewPorts()[j];
                    vp.add(p);
                    try {
                        sd.add(new OSCPortOut(InetAddress.getByName(p.getNode().getHostName()), p.getPort()));
                        System.out.println("Creating out port "+p.getPort()+" for "+p.getNode().getHostName());
                    }
                    catch (java.net.UnknownHostException ex){System.err.println("Error: could not connect to host "+p.getNode().getHostName());}
                }
            }
            if (vp.size() != sd.size()){
                System.out.println("Error while initializing OSC senders: viewport count does not match osc sender count");
            }
            viewports = (ViewPort[])vp.toArray(new ViewPort[vp.size()]);
            senders = (OSCPortOut[])sd.toArray(new OSCPortOut[sd.size()]);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        // OSC receiver (control of meta camera)
        try {
            System.out.println("Initializing OSC receiver");
            receiver = new OSCPortIn(in);
            OSCListener listener = new OSCListener() {
                public void acceptMessage(java.util.Date time, OSCMessage message){
                    processIncomingMessage(message);
                }
            };
            receiver.addListener(Controller.MOVE_META_CAMERA, listener);
            receiver.startListening();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
    
    void initViewFinders(){
        // viewfinders
        viewFinder = new VRectangle(0, 0, 1000, 10, 10, Color.GREEN, Color.GREEN, VIEWFINDER_OPACITY);
        rSpace.addGlyph(viewFinder);
        viewportFinders = new VRectangle[viewports.length];
        for (int i=0;i<viewportFinders.length;i++){
            viewportFinders[i] = new VRectangle(0, 0, 1000, 10, 10, Color.RED, Color.RED, VIEWPORT_OPACITY);
            rSpace.addGlyph(viewportFinders[i]);
        }
    }
    
    /* ----------------- Scene --------------------*/
    
    void loadScene(File zuistFile){
		try {
			cView.setTitle(CONTROL_VIEW_TITLE + zuistFile.getCanonicalPath());
			oView.setTitle(OVERVIEW_TITLE + zuistFile.getCanonicalPath());
		}
		catch (IOException ex){}
		gp.setValue(0);
		gp.setVisible(true);
		SCENE_FILE = zuistFile;
	    SCENE_FILE_DIR = SCENE_FILE.getParentFile();
	    sm.loadScene(Viewer.parseXML(SCENE_FILE), SCENE_FILE_DIR, true, gp);
	    HashMap sceneAttributes = sm.getSceneAttributes();
	    if (sceneAttributes.containsKey(SceneManager._background)){
	        cView.setBackgroundColor((Color)sceneAttributes.get(SceneManager._background));
	        oView.setBackgroundColor((Color)sceneAttributes.get(SceneManager._background));
	    }
	    gp.setVisible(false);
	    gp.setLabel(VWGlassPane.EMPTY_STRING);
	    
	    System.out.println(sm.getLevelCount()+" "+sm.getObjectCount());
	    for (int i=0;i<sm.getLevelCount();i++){
	        System.out.println(sm.getLevel(i));
	    }
        cCamera.setAltitude(10.0f);
        ceh.cameraMoved(null, null, 0);
	}
    
    void togglePixelsBehindBezels(){
        pixelsBehindBezels = !pixelsBehindBezels;
        wc.computeDimensions(pixelsBehindBezels);
        ceh.cameraMoved(null, null, 0);
    }
    
    /* ------------------ Local Navigation ----------------- */

    void getGlobalView(Camera c){
        sm.getGlobalView(c, Viewer.ANIM_MOVE_LENGTH, null);
    }
    
    void altitudeChanged(){}
    
    void updatePanelSize(){
        Dimension d = cView.getPanel().getSize();
        panelWidth = d.width;
		panelHeight = d.height;
	}
	
    /* Higher view */
    void getHigherView(){
        Float alt = new Float(cCamera.getAltitude() + cCamera.getFocal())/2f;
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Viewer.ANIM_MOVE_LENGTH, cCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Higher view */
    void getLowerView(){
        Float alt=new Float(-(cCamera.getAltitude() + cCamera.getFocal())/4.0f);
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Viewer.ANIM_MOVE_LENGTH, cCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Direction should be one of Viewer.MOVE_* */
    void translateView(short direction){
        LongPoint trans;
        long[] rb = cView.getVisibleRegion(cCamera);
        if (direction == Controller.MOVE_NORTH){
            long qt = Math.round((rb[1]-rb[3])/8.0);
            trans = new LongPoint(0,qt);
        }
        else if (direction == Controller.MOVE_SOUTH){
            long qt = Math.round((rb[3]-rb[1])/8.0);
            trans = new LongPoint(0,qt);
        }
        else if (direction == Controller.MOVE_EAST){
            long qt = Math.round((rb[2]-rb[0])/8.0);
            trans = new LongPoint(qt,0);
        }
        else {
            // direction == Controller.MOVE_WEST
            long qt = Math.round((rb[0]-rb[2])/8.0);
            trans = new LongPoint(qt,0);
        }
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(Viewer.ANIM_MOVE_LENGTH, cCamera,
            trans, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }
    
    /* ------------------ Remote Navigation ----------------- */
    
    boolean isBeyondLimit = false;
    
    void setBeyondLimit(boolean b){
        isBeyondLimit = b;
    }
    
    void updateCameraGroup(long[] wnes){
        // update VRect representing overall observed region
        viewFinder.moveTo((wnes[0]+wnes[2])/2, (wnes[1]+wnes[3])/2);
        viewFinder.setWidth((wnes[2]-wnes[0])/2);
        viewFinder.setHeight((wnes[1]-wnes[3])/2);
        // compute regions to observe for each viewport on the wall
        long rw = wnes[2] - wnes[0];
        long rh = wnes[1] - wnes[3];
        for (int i=0;i<viewports.length;i++){
            long[] vwnes = new long[4];
            vwnes[0] = Math.round(wnes[0] + rw * viewports[i].wnes[0]);
            vwnes[2] = Math.round(wnes[0] + rw * viewports[i].wnes[2]);
            vwnes[1] = Math.round(wnes[1] - rh * viewports[i].wnes[1]);
            vwnes[3] = Math.round(wnes[1] - rh * viewports[i].wnes[3]);
            if (i<senders.length){sendMsg(senders[i], MOVE_CAMERA, CMD_CENTER_REGION, vwnes);}
            viewportFinders[i].moveTo((vwnes[0]+vwnes[2])/2, (vwnes[1]+vwnes[3])/2);
            viewportFinders[i].setWidth((vwnes[2]-vwnes[0])/2);
            viewportFinders[i].setHeight((vwnes[1]-vwnes[3])/2);
        }
        
    }

    void translate(short direction){
        switch(direction){
            case MOVE_WEST:{sendToAll(CMD_MOVE_WEST, VALUE_NONE, VALUE_NONE);break;}
            case MOVE_NORTH:{sendToAll(CMD_MOVE_NORTH, VALUE_NONE, VALUE_NONE);break;}
            case MOVE_EAST:{sendToAll(CMD_MOVE_EAST, VALUE_NONE, VALUE_NONE);break;}
            case MOVE_SOUTH:{sendToAll(CMD_MOVE_SOUTH, VALUE_NONE, VALUE_NONE);break;}
        }
    }
    
    void firstOrderTranslate(int x, int y){
        float a = (cCamera.focal+Math.abs(cCamera.altitude)) / cCamera.focal;
        vsm.getAnimationManager().setXspeed((rCamera.altitude>0) ? (long)(x*(a/SPEED_FACTOR)) : (long)(x/(a*SPEED_FACTOR)));
        vsm.getAnimationManager().setYspeed((rCamera.altitude>0) ? (long)(y*(a/SPEED_FACTOR)) : (long)(y/(a*SPEED_FACTOR)));
        vsm.getAnimationManager().setZspeed(0);
    }
    
    void firstOrderZoom(float z){
        float a = (cCamera.focal+Math.abs(cCamera.altitude)) / cCamera.focal;
        vsm.getAnimationManager().setXspeed(0);
        vsm.getAnimationManager().setYspeed(0);
        vsm.getAnimationManager().setZspeed((rCamera.altitude>0) ? (long)(z*(a/SPEED_FACTOR)) : (long)(z/(a*SPEED_FACTOR)));
    }
    
    void stop(){
        vsm.getAnimationManager().setXspeed(0);
        vsm.getAnimationManager().setYspeed(0);
        vsm.getAnimationManager().setZspeed(0);
        sendToAll(CMD_STOP, VALUE_NONE, VALUE_NONE);
    }
    
    boolean zoomInvariantEnabled = false;
    long zilX,zilY;

    /* providing virtual space coordinates */
    void setZoomInvariantLocationW(long x, long y){
        double a = (cCamera.focal+Math.abs(cCamera.altitude)) / cCamera.focal;
        setZoomInvariantLocation(cCamera.posx + Math.round(a*x), cCamera.posy + Math.round(a*y));
    }
        
    /* providing virtual space coordinates */
    void setZoomInvariantLocation(long x, long y){
        this.zilX = x;
        this.zilY = y;
        vsm.getAnimationManager().setZoomInvariantLocation(zilX, zilY);
    }
    
    void enableZoomInvariant(boolean b){
        this.zoomInvariantEnabled = b;
        vsm.getAnimationManager().enableCustomZoomInvariantLocation(zoomInvariantEnabled);
        System.out.println("ZO " + ((zoomInvariantEnabled) ? "enabled" : "disabled"));
    }
    
    /* ------------------ OSC in  ----------------- */

    public void processIncomingMessage(OSCMessage msg){
        Object[] params = msg.getArguments();
        String cmd = (String)params[0];
        if (cmd.equals(IN_CMD_PAN1)){
            //firstOrderTranslate(((Integer)params[1]).intValue(), ((Integer)params[2]).intValue());
            //System.out.println("pan "+((Integer)params[1]).intValue()+" "+((Integer)params[2]).intValue());
        }
        else if (cmd.equals(IN_CMD_ZOOM1)){
            //firstOrderZoom(((Float)params[1]).floatValue());
            //System.out.println("zoom "+((Float)params[1]).floatValue());
        }
        else if (cmd.equals(IN_CMD_STOP)){
            stop();
            System.out.println("stop");
        }
        else if (cmd.equals(IN_CMD_ZORIG)){
            setZoomInvariantLocationW(((Integer)params[1]).intValue(), ((Integer)params[2]).intValue());
            System.out.println("set zoom origin "+((Integer)params[1]).intValue()+" "+((Integer)params[2]).intValue());
        }
        else if (cmd.equals(IN_CMD_ZORIG_ENABLED)){
            enableZoomInvariant(true);
            System.out.println("Mode: zoom to origin");
        }        
        else if (cmd.equals(IN_CMD_ZORIG_DISABLED)){
            enableZoomInvariant(false);
            System.out.println("Mode: zoom to center of screen");
        }
        else if (cmd.equals(IN_CMD_GGV)){
            getGlobalView(cCamera);
            System.out.println("ggv");
        }
    }

    /* ------------------ OSC out ----------------- */
    
    void gcNodes(){
        sendToAll(CMD_GC, VALUE_NONE, VALUE_NONE);
    }
    
    void infoNodes(){
        sendToAll(CMD_INFO, VALUE_NONE, VALUE_NONE);
    }

    void consoleNodes(){
        sendToAll(CMD_CONSOLE, VALUE_NONE, VALUE_NONE);
    }

    void quitNodes(){
        sendToAll(CMD_QUIT, VALUE_NONE, VALUE_NONE);
    }
    
    void sendToAll(String cmd, Integer value1, Integer value2){
        for (int i=0;i<senders.length;i++){
            sendMsg(senders[i], MOVE_CAMERA, cmd, value1, value2);
        }
    }

    void sendMsg(OSCPortOut sender, String listener, String cmd, long[] wnes){
        Object args[] = new Object[5];
        args[0] = cmd;
        args[1] = String.valueOf(wnes[0]);
        args[2] = String.valueOf(wnes[1]);
        args[3] = String.valueOf(wnes[2]);
        args[4] = String.valueOf(wnes[3]);
        OSCMessage msg = new OSCMessage(listener, args);
        try {
            sender.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    void sendMsg(OSCPortOut sender, String listener, String cmd, Integer value1, Integer value2){
        Object args[] = new Object[3];
        args[0] = cmd;
        args[1] = value1;
        args[2] = value2;
        OSCMessage msg = new OSCMessage(listener, args);
        try {
            sender.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /* ------------------ Overlaid info ----------------- */    
    
    static final Font OVERLAY_FONT = new Font("Dialog", Font.PLAIN, 10);
    String mCoordsStr = "";
    
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight){
        if (isBeyondLimit){
            g2d.setColor(Color.RED);
            g2d.fillRect(5, 5, 10, 10);
        }
        g2d.setColor(Color.BLACK);
        g2d.setFont(OVERLAY_FONT);
        g2d.drawString(mCoordsStr, 20, 15);
    }
    
    /* ------------------ MAIN ----------------- */
    
    public static void main(String[] args){
        File configF = null;
        File zuistF = null;
        int OSCportIn = DEFAULT_OSC_LISTENING_PORT;
        boolean ogl = false;
        boolean aa = true;
        // pixels behind bezels
        boolean pbb = true;
		for (int i=0;i<args.length;i++){
			if (args[i].startsWith("-")){
				if (args[i].substring(1).startsWith("port")){OSCportIn = Integer.parseInt(args[i].substring(6));}
				else if (args[i].substring(1).equals("opengl")){ogl = true;}
				else if (args[i].substring(1).equals("noaa")){aa = false;}
				else if (args[i].substring(1).equals("nopbb")){pbb = false;}
				else if (args[i].substring(1).equals("h") || args[i].substring(1).equals("--help")){Controller.printCmdLineHelp();System.exit(0);}
				else if (args[i].equals("-c")){
				    configF = new File(args[++i]);
				}
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
                            zuistF = new File(f, xmlFiles[0]);
                        }
                    }
                    else {
                        zuistF = f;                        
                    }
                }
            }
		}
		if (ogl){
		    System.setProperty("sun.java2d.opengl", "True");
		}
		System.out.println("Loading config from "+configF);
		System.out.println("Loading scene from "+zuistF);
		System.out.println("Listening for OSC commands from port "+OSCportIn);
		System.out.println("OpenGL pipeline is "+((ogl) ? "ON" : "OFF"));
		System.out.println("Antialiasing is "+((aa) ? "ON" : "OFF"));
        new Controller(configF, zuistF, OSCportIn, ogl, aa, pbb);
    }
    
    private static void printCmdLineHelp(){
        System.out.println("Usage:\n\tjava -Xmx1024M -Xms512M -cp target/zuist4wild-0.1.0-SNAPSHOT.jar fr.inria.wild.zuist.Controller <path_to_scene_dir> [options]");
        System.out.println("Options:\n\t-port=N: OSC commands listening port");
        System.out.println("\t-port=N: OSC commands listening port");
        System.out.println("\t-noaa: no antialiasing");
        System.out.println("\t-opengl: use Java2D OpenGL rendering pipeline (Java 6+Linux/Windows), requires that -Dsun.java2d.opengl=true be set on cmd line");
        System.out.println("\t-nopbb: configure viewports so that there are no pixels behind bezels (distorts image, but shows all pixels)");
    }
    
}

class ControllerEventHandler implements ViewEventHandler, CameraListener {

    Controller application;
    
    /* bounds of region in virtual space currently observed through cCamera */
    long[] wnes = new long[4];
    float oldCameraAltitude;

    //remember last mouse coords to compute translation  (dragging)
    int lastJPX,lastJPY;

    ControllerEventHandler(Controller appli){
        application = appli;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        v.setDrawDrag(true);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        application.vsm.getAnimationManager().setXspeed(0);
        application.vsm.getAnimationManager().setYspeed(0);
        application.vsm.getAnimationManager().setZspeed(0);
        v.setDrawDrag(false);
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        v.setDrawDrag(true);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        v.setDrawDrag(false);
        application.stop();
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        application.setZoomInvariantLocation(v.getVCursor().vx, v.getVCursor().vy);
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (buttonNumber == 1){
            if (mod == SHIFT_MOD){
                application.firstOrderZoom(lastJPY-jpy);
            }
            else {
                application.firstOrderTranslate(jpx-lastJPX, lastJPY-jpy);
            }
        }
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){}

    public void enterGlyph(Glyph g){
        g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
        g.highlight(false, null);
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (mod == SHIFT_MOD){
    		if (code==KeyEvent.VK_UP){application.translate(Controller.MOVE_NORTH);}
    		else if (code==KeyEvent.VK_DOWN){application.translate(Controller.MOVE_SOUTH);}
    		else if (code==KeyEvent.VK_LEFT){application.translate(Controller.MOVE_WEST);}
    		else if (code==KeyEvent.VK_RIGHT){application.translate(Controller.MOVE_EAST);}            
        }
        else if (mod == CTRL_MOD){
            if (code==KeyEvent.VK_Q){application.quitNodes();}
        }
        else {
            if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
        	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
        	else if (code==KeyEvent.VK_HOME){application.getGlobalView(application.cCamera);}
        	else if (code==KeyEvent.VK_UP){application.translateView(Controller.MOVE_NORTH);}
        	else if (code==KeyEvent.VK_DOWN){application.translateView(Controller.MOVE_SOUTH);}
        	else if (code==KeyEvent.VK_LEFT){application.translateView(Controller.MOVE_WEST);}
        	else if (code==KeyEvent.VK_RIGHT){application.translateView(Controller.MOVE_EAST);}
        	else if (code==KeyEvent.VK_G){application.gcNodes();}
        	else if (code==KeyEvent.VK_I){application.infoNodes();}
        	else if (code==KeyEvent.VK_C){application.consoleNodes();}
        	else if (code==KeyEvent.VK_P){System.out.println(application.panelWidth+" "+application.panelHeight);}
        	else if (code==KeyEvent.VK_B){application.togglePixelsBehindBezels();}
        	else if (code==KeyEvent.VK_Z){application.enableZoomInvariant(!application.zoomInvariantEnabled);}
        }
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

    public void cameraMoved(Camera cam, LongPoint coord, float a){
        application.cView.getVisibleRegion(application.cCamera, wnes);
        // tests when zooming beyond max res limit (beyond this limit camera positions on cluster nodes are just crazy, have to fix it eventually)
        application.setBeyondLimit((wnes[2]-wnes[0]) < application.wc.size.width || (wnes[1]-wnes[3]) < application.wc.size.height);
        // update camera group
        application.updateCameraGroup(wnes);
    }

}

class OverviewEventHandler implements ViewEventHandler {

    Controller application;
    
    //remember last mouse coords to compute translation  (dragging)
    int lastJPX,lastJPY;

    OverviewEventHandler(Controller appli){
        application = appli;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        v.setDrawDrag(true);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        application.vsm.getAnimationManager().setXspeed(0);
        application.vsm.getAnimationManager().setYspeed(0);
        application.vsm.getAnimationManager().setZspeed(0);
        v.setDrawDrag(false);
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        v.setDrawDrag(true);
    }

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        v.setDrawDrag(false);
        application.stop();
    }

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){
        application.mCoordsStr = v.getVCursor().vx + ", " + v.getVCursor().vy;
        application.oView.repaintNow();
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (buttonNumber == 1){
            if (mod == SHIFT_MOD){
                application.firstOrderZoom(lastJPY-jpy);
            }
            else {
                application.firstOrderTranslate(jpx-lastJPX, lastJPY-jpy);
            }
        }
    }

    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){}

    public void enterGlyph(Glyph g){
        g.highlight(true, null);
    }

    public void exitGlyph(Glyph g){
        g.highlight(false, null);
    }

    public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (mod == SHIFT_MOD){
    		if (code==KeyEvent.VK_UP){application.translate(Controller.MOVE_NORTH);}
    		else if (code==KeyEvent.VK_DOWN){application.translate(Controller.MOVE_SOUTH);}
    		else if (code==KeyEvent.VK_LEFT){application.translate(Controller.MOVE_WEST);}
    		else if (code==KeyEvent.VK_RIGHT){application.translate(Controller.MOVE_EAST);}            
        }
        else if (mod == CTRL_MOD){
            if (code==KeyEvent.VK_Q){application.quitNodes();}
        }
        else {
            if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
        	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
        	else if (code==KeyEvent.VK_HOME){application.getGlobalView(application.cCamera);}
        	else if (code==KeyEvent.VK_UP){application.translateView(Controller.MOVE_NORTH);}
        	else if (code==KeyEvent.VK_DOWN){application.translateView(Controller.MOVE_SOUTH);}
        	else if (code==KeyEvent.VK_LEFT){application.translateView(Controller.MOVE_WEST);}
        	else if (code==KeyEvent.VK_RIGHT){application.translateView(Controller.MOVE_EAST);}
        	else if (code==KeyEvent.VK_G){application.gcNodes();}
        	else if (code==KeyEvent.VK_I){application.infoNodes();}
        	else if (code==KeyEvent.VK_C){application.consoleNodes();}
        	else if (code==KeyEvent.VK_B){application.togglePixelsBehindBezels();}
        }
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

}

class CTGlassPane extends JComponent implements ProgressListener {
    
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
    
    Controller application;
    
    CTGlassPane(Controller app){
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
