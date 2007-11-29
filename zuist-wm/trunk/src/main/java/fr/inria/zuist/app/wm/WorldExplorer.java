/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.wm;

import java.io.File;
import java.io.IOException;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;

import java.util.Vector;

import java.io.File;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.glyphs.VSegment;

import fr.inria.zuist.engine.SceneManager;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Emmanuel Pietriga
 */

public class WorldExplorer {
    
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
    
    /* Navigation constants */
    static final int ANIM_MOVE_LENGTH = 300;
    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;
    
    /* ZVTM objects */
    VirtualSpaceManager vsm;
    static final String mSpaceName = "World Space";
    VirtualSpace mSpace;
    Camera mCamera;
    static final String mViewName = "World Explorer";
    View mView;
    ExplorerEventHandler eh;

    SceneManager sm;

    public WorldExplorer(boolean fullscreen, String dir){
        if (dir != null){
            PATH_TO_HIERARCHY = dir;
            PATH_TO_SCENE = PATH_TO_HIERARCHY + "/wm_scene.xml";
            SCENE_FILE = new File(PATH_TO_SCENE);
        }
        initGUI(fullscreen);
        sm = new SceneManager(vsm, mSpace, mCamera);
        sm.setSceneCameraBounds(eh.wnes);
        sm.loadScene(parseXML(SCENE_FILE), PATH_TO_HIERARCHY);
    }

    void initGUI(boolean fullscreen){
        windowLayout();
        vsm = new VirtualSpaceManager();
        mSpace = vsm.addVirtualSpace(mSpaceName);
        mCamera = vsm.addCamera(mSpace);
        Vector cameras = new Vector();
        cameras.add(mCamera);
        mView = vsm.addExternalView(cameras, mViewName, View.STD_VIEW, VIEW_W, VIEW_H, true, false, false, null);
        if (fullscreen){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        eh = new ExplorerEventHandler(this);
        mView.setEventHandler(eh, 0);
        mView.setNotifyMouseMoved(true);
        mView.setBackgroundColor(Color.GRAY);
        vsm.animator.setAnimationListener(eh);
        updatePanelSize();
        vsm.addGlyph(new com.xerox.VTM.glyphs.VSegment(-45000, 0, 0, Color.BLACK, 45000, 0), mSpace);
        vsm.addGlyph(new com.xerox.VTM.glyphs.VSegment(0, -25000, 0, Color.BLACK, 0, 25000), mSpace);
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
    
    /*-------------     Navigation       -------------*/

    void getGlobalView(){
        vsm.getGlobalView(mCamera, WorldExplorer.ANIM_MOVE_LENGTH);
    }

    /* Higher view */
    void getHigherView(){
        Float alt = new Float(mCamera.getAltitude() + mCamera.getFocal());
        vsm.animator.createCameraAnimation(WorldExplorer.ANIM_MOVE_LENGTH, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
    }

    /* Higher view */
    void getLowerView(){
        Float alt=new Float(-(mCamera.getAltitude() + mCamera.getFocal())/2.0f);
        vsm.animator.createCameraAnimation(WorldExplorer.ANIM_MOVE_LENGTH, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
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
        vsm.animator.createCameraAnimation(WorldExplorer.ANIM_MOVE_LENGTH, AnimManager.CA_TRANS_SIG, trans, mCamera.getID());
    }
    
    void altitudeChanged(){
        sm.updateLevel(mCamera.altitude);
    }
    
    void updatePanelSize(){
        Dimension d = mView.getPanel().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
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
        boolean fs = (args.length > 0) ? Boolean.parseBoolean(args[0]) : false;
        String dir = (args.length > 1) ? args[1] : null;
        new WorldExplorer(fs, dir);
    }

}
