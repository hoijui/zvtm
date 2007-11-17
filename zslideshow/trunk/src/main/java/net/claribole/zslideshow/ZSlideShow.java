/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zslideshow;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
//import java.awt.Graphics2D;
import java.awt.Font;
//import java.awt.AlphaComposite;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
//import javax.swing.SwingUtilities;
//
import java.util.Vector;
//import java.util.Hashtable;
//import java.util.Enumeration;
//
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
//import java.io.BufferedWriter;
//import java.io.FileOutputStream;
//import java.io.OutputStreamWriter;
//
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.SwingWorker;
//import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.glyphs.Glyph;
//import com.xerox.VTM.glyphs.Translucent;
import com.xerox.VTM.glyphs.VImage;
//import com.xerox.VTM.glyphs.VRectangle;
//import com.xerox.VTM.glyphs.VRectangleST;
//import net.claribole.zvtm.engine.PostAnimationAdapter;
//import net.claribole.zvtm.engine.TransitionManager;
//import net.claribole.zvtm.engine.Java2DPainter;       
//import net.claribole.zvtm.engine.RepaintAdapter;
//import net.claribole.zvtm.engine.RepaintListener;
//import net.claribole.zvtm.engine.GlyphKillAction;
//import net.claribole.zvtm.engine.Location;
import net.claribole.zvtm.glyphs.RImage;
//import net.claribole.zvtm.lens.*;
//
//import fr.inria.zuist.engine.SceneManager;
//import fr.inria.zuist.engine.Region;
//import fr.inria.zuist.engine.ObjectDescription;
//import fr.inria.zuist.engine.TextDescription;
//import fr.inria.zuist.engine.ProgressListener;
//import fr.inria.zuist.engine.LevelListener;
//import fr.inria.zuist.engine.RegionListener;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.FactoryConfigurationError;
//import javax.xml.parsers.ParserConfigurationException;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;
//
public class ZSlideShow {

    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1024;
    static int VIEW_MAX_H = 768;
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;
    
    /* ZVTM objects */
    VirtualSpaceManager vsm;
    static final String mSpaceName = "Slide Show Space";
    static final String mnSpaceName = "Menu Space";
    VirtualSpace mSpace, mnSpace;
    Camera mCamera, mnCamera;
    static final String mViewName = "ZSlideShow";
    View mView;
    ZSSEventHandler eh;
    ZSSMenuEventHandler mneh;

    static final float SLIDESHOW_CAMERA_ALTITUDE = 1000.0f;

    static final Font MAIN_FONT = new Font("Arial", Font.PLAIN, 10);
    
    static final Color BACKGROUND_COLOR = new Color(10,10,10);
    
    public ZSlideShow(short fullscreen){
        initGUI(fullscreen);
        System.gc();
    }

    void initGUI(short fullscreen){
        windowLayout();
        vsm = new VirtualSpaceManager();
        vsm.setMainFont(MAIN_FONT);
        mSpace = vsm.addVirtualSpace(mSpaceName);
        mCamera = vsm.addCamera(mSpace);
        mnSpace = vsm.addVirtualSpace(mnSpaceName);
        mnCamera = vsm.addCamera(mnSpace);
        Vector cameras = new Vector();
        cameras.add(mCamera);
        cameras.add(mnCamera);
        mView = vsm.addExternalView(cameras, mViewName, View.STD_VIEW, VIEW_W, VIEW_H, false, false, false, null);
        if (fullscreen == FULL_SCREEN){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        eh = new ZSSEventHandler(this);
        mView.setEventHandler(eh, 0);
        mneh = new ZSSMenuEventHandler(this);
        mView.setEventHandler(mneh, 1);
        mView.setBackgroundColor(BACKGROUND_COLOR);
        RImage.setReflectionHeight(0.5f);
        RImage.setReflectionMaskEndPoints(0.15f, 0.0f);
        updatePanelSize();
        mCamera.setAltitude(SLIDESHOW_CAMERA_ALTITUDE);
        mnCamera.setAltitude(0);
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
    
    void updatePanelSize(){
        Dimension d = mView.getPanel().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
    }
    
    /* List of picture files in the current directory. */
    File[] contents = new File[0];
    VImage[] images = new VImage[0];
    int currentIndex = -1;
        
    void reset(){
        //XXX:TBW: remove glyphs from virtual space, reset hooks to glyphs
        if (currentIndex != -1){
            hidePicture(currentIndex);
        }
        contents = new File[0];
        images = new VImage[0];
    }
    
    void selectDirectory(){
        final JFileChooser fc = new JFileChooser(System.getProperty("user.home"));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Browse Directory...");
        int returnVal= fc.showOpenDialog(mView.getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION){
            openDirectory(fc.getSelectedFile());
        }
    }
    
    void openDirectory(File dir){
        reset();
        contents = dir.listFiles(new ImageFileFilter());
        images = new VImage[contents.length];
        if (contents.length > 0){
            displayPicture(0);
        }
        else {
            //XXX: SIGNAL NO FILE THAT CAN BE DISPLAYED IN SLIDESHOW
            System.out.println("Directory " + dir.getAbsolutePath() + "does not contain any file that can be displayed");
            
        }
    }
    
    void displayPicture(final int i){
        final SwingWorker worker=new SwingWorker(){
		    public Object construct(){
			    doDisplayPicture(i);
			    if (i<images.length-1 && images[i+1] == null){
			        preload(i+1);
			    }
			    if (i>0 && images[i-1] == null){
			        preload(i-1);
			    }
			    return null; 
		    }
		};
	    worker.start();
    }
    
    void displayPreviousPicture(){
        if (currentIndex > 0){
            displayPicture(currentIndex-1);
        }
    }

    void displayNextPicture(){
        if (currentIndex < contents.length-1){
            displayPicture(currentIndex+1);
        }        
    }
    
    void doDisplayPicture(int i){
        if (currentIndex != -1){
            hidePicture(currentIndex);
        }
        currentIndex = i;
        if (images[i] == null){
            System.out.println("loading "+i);
            
            images[i] = new RImage(0, 0, 0, RImage.getBufferedImageFromFile(contents[i]), 2.0f, 1.0f);
            images[i].setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
            images[i].setBorderColor(Color.WHITE);
        }
        vsm.addGlyph(images[i], mSpace);
    }
    
    void preload(int i){
        System.out.println("preloading "+i);
        
        images[i] = new RImage(0, 0, 0, RImage.getBufferedImageFromFile(contents[i]), 2.0f, 1.0f);
        images[i].setDrawBorderPolicy(VImage.DRAW_BORDER_ALWAYS);
        images[i].setBorderColor(Color.WHITE);
    }
    
    void hidePicture(int i){
        //XXX:TBW animate destruction
        if (images[i] != null){
            mSpace.destroyGlyph(images[i]);
        }
    }
    
    void exit(){
        System.exit(0);
    }

    static final short FULL_SCREEN = 1;

    public static void main(String[] args){
        final short fs = (args.length > 0) ? Short.parseShort(args[0]) : 0;
        new ZSlideShow(fs);
    }
    
}

class ImageFileFilter implements FilenameFilter {
    
    static final String[] EXTENSIONS = {".png", ".jpg", ".jpeg", ".gif"};
    
    ImageFileFilter(){}
    
    public boolean accept(File dir, String name){
        for (int i=0;i<EXTENSIONS.length;i++){
            if (name.endsWith(EXTENSIONS[i])){
                return true;
            }
        }
        return false;
    }
    
}
