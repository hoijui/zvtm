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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;

import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortOut;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCListener;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.engine.ViewEventHandler;
import net.claribole.zvtm.engine.CameraListener;
import net.claribole.zvtm.animation.Animation;
import net.claribole.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.ProgressListener;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class Controller {
    
    static final int DEFAULT_VIEW_WIDTH = 1280;
    
    static final int DEFAULT_OSC_LISTENING_PORT = 57109;
    
    static final String MOVE_CAMERA = "/moveCam";
    static final String MOVE_META_CAMERA = "/moveMCam";
    
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
    
    static final Integer VALUE_NONE = new Integer(0);
    
    File SCENE_FILE, SCENE_FILE_DIR;
    
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;
    
    VirtualSpaceManager vsm;
    VirtualSpace mSpace;
    static final String mSpaceName = "Control Space";
    Camera mCamera;
    View mView;
    ControllerEventHandler eh;
    
    // following arrays have same length, viewport at index i is associated with sender at index i
    ViewPort[] viewports;
    OSCPortOut[] senders;
    OSCPortIn receiver;
    
    SceneManager sm;
    WallConfiguration wc;
    
    CTGlassPane gp;
        
    public Controller(File configFile, File zuistFile, int OSCin, boolean opengl, boolean antialiased){
        wc = new WallConfiguration(configFile);
        System.out.println("Wall size: "+wc.getSize().width+" x "+wc.getSize().height);
        initGUI(opengl, antialiased);
        initOSC(OSCin);
        VirtualSpace[]  sceneSpaces = {mSpace};
        Camera[] sceneCameras = {mCamera};
        sm = new SceneManager(sceneSpaces, sceneCameras);
        sm.setSceneCameraBounds(mCamera, eh.wnes);
        if (zuistFile != null){
			loadScene(zuistFile);
			//vsm.addGlyph(new net.claribole.zvtm.glyphs.CircleNR(0, 0, 0, 10, Color.WHITE), mSpace);
			getGlobalView();
		}
        mCamera.addListener(eh);
    }

    void initGUI(boolean opengl, boolean antialiased){
        vsm = VirtualSpaceManager.INSTANCE;
        eh = new ControllerEventHandler(this);
        mSpace = vsm.addVirtualSpace(mSpaceName);
        mCamera = vsm.addCamera(mSpace);
        Vector cameras = new Vector();
        cameras.add(mCamera);
        short vt = (opengl) ? View.OPENGL_VIEW : View.STD_VIEW;
        mView = vsm.addExternalView(cameras, "ZUIST4WILD Controller", vt,
            DEFAULT_VIEW_WIDTH, Math.round(DEFAULT_VIEW_WIDTH * wc.getSize().height/((float)wc.getSize().width)),
            false, true);
        mView.setBackgroundColor(Color.LIGHT_GRAY);
        mView.setEventHandler(eh);
        mView.setNotifyMouseMoved(true);
        mView.setAntialiasing(antialiased);
		ComponentAdapter ca0 = new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				updatePanelSize();
			}
		};
		mView.getFrame().addComponentListener(ca0);
		gp = new CTGlassPane(this);
		((JFrame)mView.getFrame()).setGlassPane(gp);
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
                    System.out.println("Creating out port "+p.getPort()+" for "+p.getNode().getHostName());
                    vp.add(p);
                    sd.add(new OSCPortOut(InetAddress.getByName(p.getNode().getHostName()), p.getPort()));
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
    
    /* ----------------- Scene --------------------*/
    
    void loadScene(File zuistFile){
		try {
			mView.setTitle(zuistFile.getCanonicalPath());			
		}
		catch (IOException ex){}
		gp.setValue(0);
		gp.setVisible(true);
		SCENE_FILE = zuistFile;
	    SCENE_FILE_DIR = SCENE_FILE.getParentFile();
	    sm.loadScene(Viewer.parseXML(SCENE_FILE), SCENE_FILE_DIR, true, gp);
	    gp.setVisible(false);
	    gp.setLabel(VWGlassPane.EMPTY_STRING);
        mCamera.setAltitude(0.0f);
        sm.updateLevel(mCamera.altitude);
        eh.cameraMoved(null, null, 0);
	}
    
    /* ------------------ Local Navigation ----------------- */    
    
    void getGlobalView(){
        sm.getGlobalView(mCamera, Viewer.ANIM_MOVE_LENGTH);
    }
    
    void altitudeChanged(){
        sm.updateLevel(mCamera.altitude);
    }
    
    void updatePanelSize(){
        Dimension d = mView.getPanel().getSize();
        panelWidth = d.width;
		panelHeight = d.height;
	}
	
    /* Higher view */
    void getHigherView(){
        Float alt = new Float(mCamera.getAltitude() + mCamera.getFocal());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Viewer.ANIM_MOVE_LENGTH, mCamera,
            alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /* Higher view */
    void getLowerView(){
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
    
    /* ------------------ Remote Navigation ----------------- */
    
    void updateMetaCam(long[] wnes){
        long rw = wnes[2] - wnes[0];
        long rh = wnes[1] - wnes[3];
        for (int i=0;i<viewports.length;i++){
            long[] vwnes = new long[4];
            vwnes[0] = Math.round(wnes[0] + rw * viewports[i].wnes[0]);
            vwnes[2] = Math.round(wnes[0] + rw * viewports[i].wnes[2]);
            vwnes[1] = Math.round(wnes[3] + rh * viewports[i].wnes[1]);
            vwnes[3] = Math.round(wnes[3] + rh * viewports[i].wnes[3]);
            sendMsg(senders[i], MOVE_CAMERA, CMD_CENTER_REGION, vwnes);
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
        sendToAll(CMD_TRANSLATION_SPEED, new Integer(x), new Integer(y));
    }
    
    void firstOrderZoom(int z){
        sendToAll(CMD_ZOOM_SPEED, new Integer(z), VALUE_NONE);
    }
    
    void stop(){
        sendToAll(CMD_STOP, VALUE_NONE, VALUE_NONE);
    }
    
    /* ------------------ OSC in  ----------------- */

    public void processIncomingMessage(OSCMessage message){
        //XXX:TBW
    }

    /* ------------------ OSC out ----------------- */
    
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
    
    /* ------------------ MAIN ----------------- */
    
    public static void main(String[] args){
        File configF = null;
        File zuistF = null;
        int OSCportIn = DEFAULT_OSC_LISTENING_PORT;
        boolean ogl = false;
        boolean aa = true;
		for (int i=0;i<args.length;i++){
			if (args[i].startsWith("-")){
				if (args[i].substring(1).startsWith("port")){OSCportIn = Integer.parseInt(args[i].substring(6));}
				else if (args[i].substring(1).equals("opengl")){ogl = true;}
				else if (args[i].substring(1).equals("noaa")){aa = false;}
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
        new Controller(configF, zuistF, OSCportIn, ogl, aa);
    }
    
    private static void printCmdLineHelp(){
        System.out.println("Usage:\n\tjava -Xmx1024M -Xms512M -cp target/zuist4wild-0.1.0-SNAPSHOT.jar fr.inria.wild.zuist.Controller <path_to_scene_dir> [options]");
        System.out.println("Options:\n\t-port=N: OSC commands listening port");
        System.out.println("\t-port=N: OSC commands listening port");
        System.out.println("\t-noaa: no antialiasing");
        System.out.println("\t-opengl: use Java2D OpenGL rendering pipeline (Java 6+Linux/Windows), requires that -Dsun.java2d.opengl=true be set on cmd line");
    }
    
}

class ControllerEventHandler implements ViewEventHandler, CameraListener {

    Controller application;
    
    /* bounds of region in virtual space currently observed through mCamera */
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
        v.setDrawDrag(false);
        application.stop();
    }

    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

    public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (buttonNumber == 1){
            Camera c=application.vsm.getActiveCamera();
            float a=(c.focal+Math.abs(c.altitude))/c.focal;
            if (mod == SHIFT_MOD) {
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
        else {
            if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
        	else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
        	else if (code==KeyEvent.VK_HOME){application.getGlobalView();}
        	else if (code==KeyEvent.VK_UP){application.translateView(Controller.MOVE_NORTH);}
        	else if (code==KeyEvent.VK_DOWN){application.translateView(Controller.MOVE_SOUTH);}
        	else if (code==KeyEvent.VK_LEFT){application.translateView(Controller.MOVE_WEST);}
        	else if (code==KeyEvent.VK_RIGHT){application.translateView(Controller.MOVE_EAST);}
        }
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}

    public void cameraMoved(Camera cam, LongPoint coord, float a){
        // region seen through camera
        application.mView.getVisibleRegion(application.mCamera, wnes);
        application.updateMetaCam(wnes);
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
