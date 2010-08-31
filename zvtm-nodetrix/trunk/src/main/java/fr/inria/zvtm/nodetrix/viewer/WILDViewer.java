/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package fr.inria.zvtm.nodetrix.viewer;

import java.awt.AlphaComposite;
import java.awt.Toolkit;
import java.awt.GraphicsEnvironment;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.GradientPaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;

import java.util.HashMap;
import java.util.Vector;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.EView;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.SwingWorker;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.RImage;

import fr.inria.zvtm.nodetrix.NodeTrixViz;
import fr.inria.zvtm.nodetrix.NTNode;

import fr.inria.zvtm.cluster.ClusterGeometry;
import fr.inria.zvtm.cluster.ClusteredView;


public class WILDViewer extends Viewer {

    public WILDViewer(boolean fullscreen, boolean opengl, boolean antialiased, File inputFile){
        super(fullscreen, opengl, antialiased, inputFile);
    }
    
    void initGUI(boolean fullscreen, boolean opengl, boolean antialiased){
        windowLayout();
        vsm = VirtualSpaceManager.INSTANCE;
        vsm.setMaster("WILDViewer");
        ovm = new Overlay(this);
        nm = new NavigationManager(this);
        mSpace = vsm.addVirtualSpace(Messages.mSpaceName);
        Camera mCamera = mSpace.addCamera();
        nm.ovCamera = mSpace.addCamera();
        aboutSpace = vsm.addVirtualSpace(Messages.aboutSpaceName);
		aboutSpace.addCamera();
        Vector cameras = new Vector();
        cameras.add(mCamera);
        nm.setCamera(mCamera);
        cameras.add(aboutSpace.getCamera(0));
        mView = (EView)vsm.addFrameView(cameras, Messages.mViewName, (opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H,
                                        false, false, !fullscreen, (!fullscreen) ? ConfigManager.initMenu(this) : null);
        ClusterGeometry cg = new ClusterGeometry(2680, 1700, 8, 4);
        Vector ccameras = new Vector();
        ccameras.add(mCamera);
        ClusteredView cv = new ClusteredView(cg, 3, 8, 4, ccameras);
        vsm.addClusteredView(cv);
        if (fullscreen){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        updatePanelSize();
        ovm.init();
		gp = new VWGlassPane(this);
		((JFrame)mView.getFrame()).setGlassPane(gp);
        eh = new MainEventHandler(this);
        mView.setListener(eh, 0);
        mView.setListener(ovm, 1);
        mView.setAntialiasing(antialiased);
        mView.setBackgroundColor(ConfigManager.BACKGROUND_COLOR);
		mView.getPanel().addComponentListener(eh);
		ComponentAdapter ca0 = new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				updatePanelSize();
			}
		};
		mView.getFrame().addComponentListener(ca0);
		nm.createOverview();
	}
    
    public static void main(String[] args){
        File inputFile = null;
		boolean fs = false;
		boolean ogl = false;
		boolean aa = true;
		for (int i=0;i<args.length;i++){
			if (args[i].startsWith("-")){
				if (args[i].substring(1).equals("fs")){fs = true;}
				else if (args[i].substring(1).equals("opengl")){
				    System.setProperty("sun.java2d.opengl", "true");
				    ogl = true;
				}
				else if (args[i].substring(1).equals("noaa")){aa = false;}
				else if (args[i].substring(1).equals("h") || args[i].substring(1).equals("--help")){Messages.printCmdLineHelp();System.exit(0);}
			}
            else {
                // the only other thing allowed as a cmd line param is a scene file
                File f = new File(args[i]);
                if (f.exists()){
                    inputFile = f;                        
                }
            }
		}
        if (!fs && Utils.osIsMacOS()){
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        System.out.println(Messages.H_4_HELP);
        new WILDViewer(fs, ogl, aa, inputFile);
    }
    
}
