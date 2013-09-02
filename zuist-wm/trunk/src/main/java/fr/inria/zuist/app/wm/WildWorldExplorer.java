/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.wm;

import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;

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
import java.awt.GraphicsDevice;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.ImageIcon;
import java.awt.geom.Point2D;

import java.util.Vector;

import java.io.File;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.widgets.TranslucentTextArea;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.ProgressListener;

import fr.inria.zvtm.cluster.ClusterGeometry;
import fr.inria.zvtm.cluster.ClusteredView;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.geotools.factory.GeoTools;

import org.geonames.Toponym;
import org.geonames.InsufficientStyleException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * @author Emmanuel Pietriga
 */

public class WildWorldExplorer extends WorldExplorer {

    public WildWorldExplorer(WEOptions options, File xmlSceneFile){
        super(options, xmlSceneFile);
    }

    void initGUI(WEOptions options){
        AirTrafficManager.EDGE_STROKE_WIDTH = 4f;
        AirTrafficManager.MIN_ALPHA = .3f;
        windowLayout();
        vsm = VirtualSpaceManager.INSTANCE;
        vsm.setMaster("WildWorldExplorer");
        mSpace = vsm.addVirtualSpace(mSpaceName);
        bSpace = vsm.addVirtualSpace(bSpaceName);
        mCamera = mSpace.addCamera();
        ovCamera = mSpace.addCamera();
        bCamera = bSpace.addCamera();
        Vector cameras = new Vector();
        cameras.add(mCamera);
        cameras.add(bCamera);
        //mCamera.stick(bCamera, true);
        mView = vsm.addFrameView(cameras, mViewName, (options.opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H, false, false, !options.fullscreen, null);
        if (options.fullscreen && GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().isFullScreenSupported()){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        mView.setAntialiasing(!options.noaa);
        eh = new ExplorerEventHandler(this);
        mCamera.addListener(eh);
        mView.setListener(eh, 0);
        mView.setListener(eh, 1);
        mView.setBackgroundColor(BACKGROUND_COLOR);
        mView.getCursor().setColor(Color.WHITE);
        mView.getCursor().setHintColor(Color.WHITE);
        //mView.getCursor().setDynaSpotColor(Color.WHITE);
        //mView.getCursor().setDynaSpotLagTime(200);
        //mView.getCursor().activateDynaSpot(true);
        mView.setJava2DPainter(this, Java2DPainter.AFTER_PORTALS);
        updatePanelSize();

        ClusterGeometry cg = new ClusterGeometry(options.blockWidth, options.blockHeight, options.numCols, options.numRows);
        Vector ccameras = new Vector();
        ccameras.add(mCamera);
        ccameras.add(bCamera);
        ClusteredView cv = new ClusteredView(cg, options.numRows-1, options.numCols, options.numRows, ccameras);
        vsm.addClusteredView(cv);

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
        mView.getPanel().getComponent().addComponentListener(eh);
    }

    boolean isDynaspotEnabled(){
        return false;
    }

    public static void main(String[] args){
        WEOptions options = new WEOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
        } catch(CmdLineException ex){
            System.err.println(ex.getMessage());
            parser.printUsage(System.err);
            return;
        }
        File xmlSceneFile = (options.path_to_zuist_map != null) ? new File(options.path_to_zuist_map) : null;
        if (!options.fullscreen && Utils.osIsMacOS()){
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        System.out.println("--help for command line options");
        System.out.println("Using GeoTools v" + GeoTools.getVersion());
        new WildWorldExplorer(options, xmlSceneFile);
    }

}
