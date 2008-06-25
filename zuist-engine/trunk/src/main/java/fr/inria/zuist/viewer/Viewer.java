/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.viewer;

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
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;

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
import com.xerox.VTM.glyphs.VImage;
import net.claribole.zvtm.engine.PostAnimationAction;
import net.claribole.zvtm.lens.*;

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

public class Viewer {
    
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
    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;
    
    /* ZVTM objects */
    VirtualSpaceManager vsm;
    static final String mSpaceName = "Main Space";
    VirtualSpace mSpace;
    Camera mCamera;
    static final String mViewName = "ZUIST Viewer";
    View mView;
    ViewerEventHandler eh;

    SceneManager sm;
    
    public Viewer(boolean fullscreen, String xmlSceneFile){
        if (xmlSceneFile != null){
        	SCENE_FILE = new File(xmlSceneFile);
            SCENE_FILE_DIR = SCENE_FILE.getParentFile();
        }
        initGUI(fullscreen);
        VirtualSpace[]  sceneSpaces = {mSpace};
        Camera[] sceneCameras = {mCamera};
        sm = new SceneManager(vsm, sceneSpaces, sceneCameras);
        sm.setSceneCameraBounds(mCamera, eh.wnes);
        sm.loadScene(parseXML(SCENE_FILE), SCENE_FILE_DIR);
        mCamera.setAltitude(10000.0f);
        eh.cameraMoved();
    }

    void initGUI(boolean fullscreen){
        windowLayout();
        vsm = new VirtualSpaceManager();
        mSpace = vsm.addVirtualSpace(mSpaceName);
        mCamera = vsm.addCamera(mSpace);
        Vector cameras = new Vector();
        cameras.add(mCamera);
        mView = vsm.addExternalView(cameras, mViewName, View.STD_VIEW, VIEW_W, VIEW_H, false, false, !fullscreen, null);
        if (fullscreen){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        eh = new ViewerEventHandler(this);
        mView.setEventHandler(eh, 0);
        mView.setNotifyMouseMoved(true);
        mView.setBackgroundColor(Color.WHITE);
        vsm.animator.setAnimationListener(eh);
        updatePanelSize();
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
        vsm.getGlobalView(mCamera, Viewer.ANIM_MOVE_LENGTH);
    }

    /* Higher view */
    void getHigherView(){
        Float alt = new Float(mCamera.getAltitude() + mCamera.getFocal());
        vsm.animator.createCameraAnimation(Viewer.ANIM_MOVE_LENGTH, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
    }

    /* Higher view */
    void getLowerView(){
        Float alt=new Float(-(mCamera.getAltitude() + mCamera.getFocal())/2.0f);
        vsm.animator.createCameraAnimation(Viewer.ANIM_MOVE_LENGTH, AnimManager.CA_ALT_SIG, alt, mCamera.getID());
    }

    /* Direction should be one of Viewer.MOVE_* */
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
        vsm.animator.createCameraAnimation(Viewer.ANIM_MOVE_LENGTH, AnimManager.CA_TRANS_SIG, trans, mCamera.getID());
    }
    
    void altitudeChanged(){
        sm.updateLevel(mCamera.altitude);
    }
    
    void updatePanelSize(){
        Dimension d = mView.getPanel().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
    }
    
    /* misc. lens settings */
    Lens lens;
    TemporalLens tLens;
    static int LENS_R1 = 200;
    static int LENS_R2 = 100;
    static final int WHEEL_ANIM_TIME = 50;
    static final int LENS_ANIM_TIME = 300;
    static double DEFAULT_MAG_FACTOR = 4.0;
    static double MAG_FACTOR = DEFAULT_MAG_FACTOR;
    static double INV_MAG_FACTOR = 1/MAG_FACTOR;
    /* LENS MAGNIFICATION */
    static float WHEEL_MM_STEP = 1.0f;
    static final float MAX_MAG_FACTOR = 12.0f;
    
    static final int NO_LENS = 0;
    static final int ZOOMIN_LENS = 1;
    static final int ZOOMOUT_LENS = -1;
    int lensType = NO_LENS;
    
    /* lens distance and drop-off functions */
    static final short L1_Linear = 0;
    static final short L1_InverseCosine = 1;
    static final short L1_Manhattan = 2;
    static final short L2_Gaussian = 3;
    static final short L2_Linear = 4;
    static final short L2_InverseCosine = 5;
    static final short L2_Manhattan = 6;
    static final short L2_TLinear = 7;
    static final short L2_Fading = 8;
    static final short L2_DLinear = 9;
    static final short L3_Gaussian = 10;
    static final short L3_Linear = 11;
    static final short L3_InverseCosine = 12;
    static final short L3_Manhattan = 13;
    static final short L3_TLinear = 14;
    static final short LInf_Gaussian = 15;
    static final short LInf_Linear = 16;
    static final short LInf_InverseCosine = 17;
    static final short LInf_Manhattan = 18;
    static final short LInf_TLinear = 19;
    static final short LInf_Fading = 20;
    static final short L2_Wave = 21;
    static final short L2_TWave = 22;
    static final short LInf_Step = 23;
    static final short L2_XGaussian = 24;
    static final short L2_HLinear = 25;
    short lensFamily = L2_Gaussian;
    
    static final float FLOOR_ALTITUDE = 100.0f;
    
    void setLens(int t){
        lensType = t;
    }

    void moveLens(int x, int y, long absTime){
        if (tLens != null){
            tLens.setAbsolutePosition(x, y, absTime);
        }
        else {
            lens.setAbsolutePosition(x, y);
        }
        vsm.repaintNow();
    }

    void zoomInPhase1(int x, int y){
        // create lens if it does not exist
        if (lens == null){
            lens = mView.setLens(getLensDefinition(x, y));
            lens.setBufferThreshold(1.5f);
        }
        vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
            lens.getID(), null);
        setLens(ZOOMIN_LENS);
    }
    
    void zoomInPhase2(long mx, long my){
        // compute camera animation parameters
        float cameraAbsAlt = mCamera.getAltitude()+mCamera.getFocal();
        long c2x = Math.round(mx - INV_MAG_FACTOR * (mx - mCamera.posx));
        long c2y = Math.round(my - INV_MAG_FACTOR * (my - mCamera.posy));
        Vector cadata = new Vector();
        // -(cameraAbsAlt)*(MAG_FACTOR-1)/MAG_FACTOR
        Float deltAlt = new Float((cameraAbsAlt)*(1-MAG_FACTOR)/MAG_FACTOR);
        if (cameraAbsAlt + deltAlt.floatValue() > FLOOR_ALTITUDE){
            cadata.add(deltAlt);
            cadata.add(new LongPoint(c2x-mCamera.posx, c2y-mCamera.posy));
            // animate lens and camera simultaneously (lens will die at the end)
            vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
                lens.getID(), new ZP2LensAction(this));
            vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
                cadata, mCamera.getID());
        }
        else {
            Float actualDeltAlt = new Float(FLOOR_ALTITUDE - cameraAbsAlt);
            double ratio = actualDeltAlt.floatValue() / deltAlt.floatValue();
            cadata.add(actualDeltAlt);
            cadata.add(new LongPoint(Math.round((c2x-mCamera.posx)*ratio),
                Math.round((c2y-mCamera.posy)*ratio)));
            // animate lens and camera simultaneously (lens will die at the end)
            vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
                lens.getID(), new ZP2LensAction(this));
            vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
                cadata, mCamera.getID());
        }
    }

    void zoomOutPhase1(int x, int y, long mx, long my){
        sm.setUpdateLevel(false);
        // compute camera animation parameters
        float cameraAbsAlt = mCamera.getAltitude()+mCamera.getFocal();
        long c2x = Math.round(mx - MAG_FACTOR * (mx - mCamera.posx));
        long c2y = Math.round(my - MAG_FACTOR * (my - mCamera.posy));
        Vector cadata = new Vector();
        cadata.add(new Float(cameraAbsAlt*(MAG_FACTOR-1)));
        cadata.add(new LongPoint(c2x-mCamera.posx, c2y-mCamera.posy));
        // create lens if it does not exist
        if (lens == null){
            lens = mView.setLens(getLensDefinition(x, y));
            lens.setBufferThreshold(1.5f);
        }
        // animate lens and camera simultaneously
        vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(MAG_FACTOR-1),
            lens.getID(), null);
        vsm.animator.createCameraAnimation(LENS_ANIM_TIME, AnimManager.CA_ALT_TRANS_LIN,
            cadata, mCamera.getID(), null);
        setLens(ZOOMOUT_LENS);
    }

    void zoomOutPhase2(){
        // make lens disappear (killing anim)
        vsm.animator.createLensAnimation(LENS_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(-MAG_FACTOR+1),
            lens.getID(), new ZP2LensAction(this));
    }

    void setMagFactor(double m){
        MAG_FACTOR = m;
        INV_MAG_FACTOR = 1 / MAG_FACTOR;
    }

    synchronized void magnifyFocus(double magOffset, int zooming, Camera ca){
        synchronized (lens){
            double nmf = MAG_FACTOR + magOffset;
            if (nmf <= MAX_MAG_FACTOR && nmf > 1.0f){
                setMagFactor(nmf);
                if (zooming == ZOOMOUT_LENS){
                    /* if unzooming, we want to keep the focus point stable, and unzoom the context
                        this means that camera altitude must be adjusted to keep altitude + lens mag
                        factor constant in the lens focus region. The camera must also be translated
                        to keep the same region of the virtual space under the focus region */
                        float a1 = mCamera.getAltitude();
                    lens.setMaximumMagnification((float)nmf, true);
                    /* explanation for the altitude offset computation: the projected size of an object
                        in the focus region (in lens space) should remain the same before and after the
                        change of magnification factor. The size of an object is a function of the
                        projection coefficient (see any Glyph.projectForLens() method). This means that
                        the following equation must be true, where F is the camera's focal distance, a1
                        is the camera's altitude before the move and a2 is the camera altitude after the
                        move:
                        MAG_FACTOR * F / (F + a1) = MAG_FACTOR + magOffset * F / (F + a2)
                        From this we can get the altitude difference (a2 - a1)                       */
                        mCamera.altitudeOffset((float)((a1+mCamera.getFocal())*magOffset/(MAG_FACTOR-magOffset)));
                    /* explanation for the X offset computation: the position in X of an object in the
                        focus region (lens space) should remain the same before and after the change of
                        magnification factor. This means that the following equation must be true (taken
                        by simplifying pc[i].lcx computation in a projectForLens() method):
                        (vx-(lensx1))*coef1 = (vx-(lensx2))*coef2
                        -- coef1 is actually MAG_FACTOR * F/(F+a1)
                        -- coef2 is actually (MAG_FACTOR + magOffset) * F/(F+a2)
                        -- lensx1 is actually camera.posx1 + ((F+a1)/F) * lens.lx
                        -- lensx2 is actually camera.posx2 + ((F+a2)/F) * lens.lx
                        Given that (MAG_FACTOR + magOffset) / (F+a2) = MAG_FACTOR / (F+a1)
                        we eventually have:
                        Xoffset = (a1 - a2) / F * lens.lx   (lens.lx being the position of the lens's center in
                        JPanel coordinates w.r.t the view's center - see Lens.java)                */
                        mCamera.move(Math.round((a1-mCamera.getAltitude())/mCamera.getFocal()*lens.lx),
                        -Math.round((a1-mCamera.getAltitude())/mCamera.getFocal()*lens.ly));
                }
                else {
                    vsm.animator.createLensAnimation(WHEEL_ANIM_TIME, AnimManager.LS_MM_LIN, new Float(magOffset),
                        lens.getID(), null);
                }
            }
        }
    }

    Lens getLensDefinition(int x, int y){
        Lens res = null;
        switch (lensFamily){
            case L1_Linear:{
                res = new L1FSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case L1_InverseCosine:{
                res = new L1FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case L1_Manhattan:{
                res = new L1FSManhattanLens(1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case L2_Gaussian:{
                res = new FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case L2_Linear:{
                res = new FSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case L2_InverseCosine:{
                res = new FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case L2_Manhattan:{
                res = new FSManhattanLens(1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case LInf_Linear:{
                res = new LInfFSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case LInf_InverseCosine:{
                res = new LInfFSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case LInf_Manhattan:{
                res = new LInfFSManhattanLens(1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case LInf_Gaussian:{
                res = new LInfFSGaussianLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case L2_TLinear:{
                res = new TLinearLens(1.0f, 0.0f, 0.95f, LENS_R1, 10, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case LInf_TLinear:{
                res = new LInfTLinearLens(1.0f, 0.0f, 0.95f, LENS_R1, 100, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case L3_TLinear:{
                res = new L3TLinearLens(1.0f, 0.0f, 0.95f, LENS_R1, 100, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case L2_Fading:{
                tLens = new TFadingLens(1.0f, 0.0f, 1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);
                ((TFadingLens)tLens).setBoundaryColor(Color.RED);
                ((TFadingLens)tLens).setObservedRegionColor(Color.RED);
                res = (Lens)tLens;
                break;
            }
            case L2_DLinear:{
                tLens = new DLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                res = (Lens)tLens;
                ((FixedSizeLens)res).setInnerRadiusColor(Color.RED);
                ((FixedSizeLens)res).setOuterRadiusColor(Color.RED);
				break;
            }
            case LInf_Fading:{
                tLens = new LInfTFadingLens(1.0f, 0.0f, 0.98f, LENS_R1, x - panelWidth/2, y - panelHeight/2);
                ((TFadingLens)tLens).setBoundaryColor(Color.RED);
                ((TFadingLens)tLens).setObservedRegionColor(Color.RED);
                res = (Lens)tLens;
                break;
            }
            case L3_Linear:{
                res = new L3FSLinearLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case L3_InverseCosine:{
                res = new L3FSInverseCosineLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case L3_Manhattan:{
                res = new L3FSManhattanLens(1.0f, LENS_R1, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case L3_Gaussian:{
                res = new L3FSGaussianLens(1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case L2_Wave:{
                res = new FSWaveLens(1.0f, LENS_R1, LENS_R2/2, 8, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case L2_TWave:{
                res = new TWaveLens(1.0f, 0.0f, 0.95f, 200, 40, 10, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
            case LInf_Step:{
                res = new LInfFSStepLens(1.0f, LENS_R1, LENS_R2, 1, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
        	case L2_XGaussian:{
        	    res = new XGaussianLens(1.0f, 0.2f, 1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
        	    tLens = null;
        	    break;
        	}
            case L2_HLinear:{
                res = new HLinearLens(1.0f, 0.4f, 1.0f, LENS_R1, LENS_R2, x - panelWidth/2, y - panelHeight/2);
                tLens = null;
                break;
            }
        }
        return res;
    }
    
    void showLensChooser(){
        new LensChooser(this);
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
        String xmlSceneFile = (args.length > 0) ? args[0] : null;
		if (xmlSceneFile == null){
			System.out.println(Messages.USAGE);
			System.exit(0);
		}
        boolean fs = (args.length > 1) ? Boolean.parseBoolean(args[1]) : false;
        new Viewer(fs, xmlSceneFile);
    }

}


class ZP2LensAction implements PostAnimationAction {

    Viewer vw;
    
    ZP2LensAction(Viewer vw){
	    this.vw = vw;
    }
    
    public void animationEnded(Object target, short type, String dimension){
        if (type == PostAnimationAction.LENS){
            vw.vsm.getOwningView(((Lens)target).getID()).setLens(null);
            ((Lens)target).dispose();
            vw.setMagFactor(Viewer.DEFAULT_MAG_FACTOR);
            vw.lens = null;
            vw.setLens(Viewer.NO_LENS);
            vw.sm.setUpdateLevel(true);
        }
    }
    
}

class LensChooser extends JFrame implements ItemListener {

    // index of lenses should correspond to short value of associated lens type in Viewer
    static final String[] LENS_NAMES = {"L1 / Linear", "L1 / Inverse Cosine", "L1 / Manhattan",
        "L2 / Gaussian", "L2 / Linear", "L2 / Inverse Cosine", "L2 / Manhattan", "L2 / Translucence Linear", "L2 / Fading", "L2 / Dynamic Linear",
        "L3 / Gaussian", "L3 / Linear", "L3 / Inverse Cosine", "L3 / Manhattan", "L3 / Translucence Linear",
        "LInf / Gaussian", "LInf / Linear", "LInf / Inverse Cosine", "LInf / Manhattan", "LInf / Translucence Linear", "LInf / Fading",
        "L2 / Wave", "L2 / Translucent Wave", "LInf / Step", "L2 / XGaussian", "L2 / HLinear"};

    Viewer vw;

    JComboBox lensList;

    LensChooser(Viewer vw){
        super();
        this.vw = vw;
        initGUI();
        this.pack();
        this.setVisible(true);
    }
    
    void initGUI(){
        Container cp = getContentPane();
        lensList = new JComboBox(LENS_NAMES);
        lensList.setSelectedIndex(vw.lensFamily);
        lensList.addItemListener(this);
        cp.add(lensList);
    }
    
    public void itemStateChanged(ItemEvent e){
        if (e.getStateChange() == ItemEvent.SELECTED){
            Object src = e.getItem();
            for (int i=0;i<LENS_NAMES.length;i++){
                if (src == LENS_NAMES[i]){
                    vw.lensFamily = (short)i;
                    return;
                }
            }
        }
    }
    
}
