/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ViewerEventHandler.java 2008 2009-06-09 07:53:35Z epietrig $
 */

package fr.inria.wild.zuist;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.Color;

import java.util.Vector;

import java.io.File;
import java.net.InetAddress;

import com.illposed.osc.OSCPort;
import com.illposed.osc.OSCPortOut;
import com.illposed.osc.OSCMessage;

import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.engine.ViewEventHandler;
import fr.inria.zuist.engine.SceneManager;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class Controller {
    
    static final String MOVE_CAMERA = "/moveCam";
    
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
    
    static final Integer VALUE_NONE = new Integer(0);
    
    
    VirtualSpaceManager vsm;
    VirtualSpace mSpace;
    static final String mSpaceName = "Control Space";
    Camera mCamera;
    View mView;
    ControllerEventHandler eh;
    
    // following arrays have same length, viewport at index i is associated with sender at index i
    ViewPort[] viewports;
    OSCPortOut[] senders;
    
    WallConfiguration wc;
        
    public Controller(File configFile){
        wc = new WallConfiguration(configFile);
        initGUI();
        initOSC();
    }

    void initGUI(){
        vsm = VirtualSpaceManager.INSTANCE;
        eh = new ControllerEventHandler(this);
        mSpace = vsm.addVirtualSpace(mSpaceName);
        mCamera = vsm.addCamera(mSpace);
        Vector cameras = new Vector();
        cameras.add(mCamera);
        short vt = View.STD_VIEW;
        mView = vsm.addExternalView(cameras, "ZUIST4WILD Controller", vt, 800, 600, false, true);
        mView.setBackgroundColor(Color.LIGHT_GRAY);
        mView.setEventHandler(eh);
        mView.setNotifyMouseMoved(true);
        vsm.repaintNow();
    }
    
    void initOSC(){
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
    
    void sendToAll(String cmd, Integer value1, Integer value2){
        for (int i=0;i<senders.length;i++){
            sendMsg(senders[i], MOVE_CAMERA, cmd, value1, value2);
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
    
    public static void main(String[] args){
        new Controller(new File(args[0]));
    }
    
}

class ControllerEventHandler implements ViewEventHandler {

    Controller application;

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
//        if (code==KeyEvent.VK_PAGE_UP){application.getHigherView();}
//		else if (code==KeyEvent.VK_PAGE_DOWN){application.getLowerView();}
//		else if (code==KeyEvent.VK_HOME){application.getGlobalView();}
		if (code==KeyEvent.VK_UP){application.translate(Controller.MOVE_NORTH);}
		else if (code==KeyEvent.VK_DOWN){application.translate(Controller.MOVE_SOUTH);}
		else if (code==KeyEvent.VK_LEFT){application.translate(Controller.MOVE_WEST);}
		else if (code==KeyEvent.VK_RIGHT){application.translate(Controller.MOVE_EAST);}
    }

    public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

    public void viewActivated(View v){}

    public void viewDeactivated(View v){}

    public void viewIconified(View v){}

    public void viewDeiconified(View v){}

    public void viewClosing(View v){System.exit(0);}


}
