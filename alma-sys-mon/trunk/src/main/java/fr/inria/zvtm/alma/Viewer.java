/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Viewer.java 2771 2010-01-15 10:27:47Z epietrig $
 */

package fr.inria.zvtm.alma;

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
import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ListSelectionModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Scanner;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.EView;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.SwingWorker;
import fr.inria.zvtm.glyphs.IcePDFPageImg;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.widgets.TranslucentJList;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.RImage;

import org.icepdf.core.pobjects.Document;

public class Viewer {
    private static final int ERROR_LAYER = 1;
    /* screen dimensions, actual dimensions of windows */
    static int SCREEN_WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1024;  // 1400
    static int VIEW_MAX_H = 600;   // 1050
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /* dimensions of zoomable panel */
    int panelWidth, panelHeight;
    
    VirtualSpaceManager vsm;
    VirtualSpace bgSpace, errorSpace;
    EView mView;
    
    MainEventHandler eh;
    Navigation nm;
    
    VWGlassPane gp;

    private IcePDFPageImg backgroundPage;
    
    /* --------------- init ------------------*/

    public Viewer(String filename, boolean fullscreen, boolean opengl, boolean antialiased){
        init();
        initGUI(fullscreen, opengl, antialiased);
        loadDocument(filename);
        populateErrorSpace();
    }
    
    void init(){
        // parse properties
        Scanner sc = new Scanner(Viewer.class.getResourceAsStream("/properties")).useDelimiter("\\s*=\\s*");
        while (sc.hasNext()){
            String token = sc.next();
            if (token.equals("version")){
                Messages.VERSION = sc.next();
            }
        }
    }
    
    void initGUI(boolean fullscreen, boolean opengl, boolean antialiased){
        windowLayout();
        vsm = VirtualSpaceManager.INSTANCE;
        nm = new Navigation(this);
        bgSpace = vsm.addVirtualSpace(Messages.bgSpaceName);
        errorSpace = vsm.addVirtualSpace("errorSpace");
        Camera bgCamera = bgSpace.addCamera();
        Camera errorCamera = errorSpace.addCamera();
        nm.ovCamera = errorSpace.addCamera(); 
        errorCamera.stick(bgCamera); //stick background camera to main (error) camera
        Vector cameras = new Vector();
        cameras.add(bgCamera);
        cameras.add(errorCamera);
        nm.setCamera(errorCamera);
        mView = (EView)vsm.addFrameView(cameras, Messages.mViewName, (opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H,
                                        false, false, !fullscreen, (!fullscreen) ? Config.initMenu(this) : null);
        if (fullscreen){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        updatePanelSize();
		gp = new VWGlassPane(this);
		((JFrame)mView.getFrame()).setGlassPane(gp);
        eh = new MainEventHandler(this);
        mView.setListener(eh, 0);
        mView.setListener(eh, 1);
        mView.setAntialiasing(antialiased);
        mView.setBackgroundColor(Config.BACKGROUND_COLOR);
		mView.getPanel().addComponentListener(eh);
		ComponentAdapter ca0 = new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				updatePanelSize();
			}
		};
		mView.getFrame().addComponentListener(ca0);
        mView.setActiveLayer(ERROR_LAYER);
		nm.createOverview();
    }

    private void loadDocument(String filename){
      if(!new File(filename).exists()){
        System.err.println("No such file: " + filename);
        return;
      }
      Document doc = new Document();
      try{
        doc.setFile(filename);
      } catch (Exception ex){
        ex.printStackTrace();
        return;
      }
      backgroundPage =  new IcePDFPageImg(0, 0, 0, doc, 0, 2f, 1);
      backgroundPage.setInterpolationMethod(java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      bgSpace.addGlyph(backgroundPage);
      doc.dispose();
      nm.updateOverview();
    }

    class ErrorRepr {
        String msg;
        VRectangle box;
        VText textMsg;
        public String toString(){
            return msg;
        }
    }

    // Demo: place random-like error boxes over the system schematics
    private void populateErrorSpace(){
      if(backgroundPage == null){
        System.err.println("oops");
        return;
      }

      final double[][] rects = new double[][]{
          {71,285,336,162},
          {305,-686,705,-828},
          {1364, -610, 1655, -680}
      }; 
      final String[] errorMessages = new String[]{
          "Power supply fault",
          "Wrong clock reference",
          "Lost master clock"
      };
      final ArrayList<ErrorRepr> errors = new ArrayList<ErrorRepr>();
      for(int i=0; i<rects.length; ++i){
        ErrorRepr er = new ErrorRepr();
        VRectangle rect = makeRect(rects[i][0],
                rects[i][1], rects[i][2], rects[i][3]);
        errorSpace.addGlyph(rect);
        VText text = new VText(rects[i][0], rects[i][1]+20, 0, Color.RED, errorMessages[i]);
        text.setBorderColor(Color.BLACK);
        text.setDrawBorder(true);
        text.setScale(3f);
        errorSpace.addGlyph(text);
        er.msg = errorMessages[i];
        er.box = rect;
        er.textMsg = text;
        errors.add(er);
      }

      JFrame frm = (JFrame)(mView.getFrame());
      JLayeredPane lp = frm.getRootPane().getLayeredPane();
      JList lst = new TranslucentJList(new AbstractListModel(){
          public int getSize() { return errors.size(); }
          public Object getElementAt(int index) { return errors.get(index); }
      });
      lst.addListSelectionListener(new ListSelectionListener(){
          public void valueChanged(ListSelectionEvent lse){
              if(!lse.getValueIsAdjusting()) return;
              JList lst = (JList)lse.getSource();
              ErrorRepr er = (ErrorRepr)lst.getSelectedValue();
              if(er == null) return;
              Glyph g = er.box;
              mView.centerOnGlyph(g, nm.mCamera, 500);
          }
      });
      lst.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      lp.add(lst, (Integer)(JLayeredPane.DEFAULT_LAYER+50));
      lst.setBounds(0,0,Config.OVERVIEW_WIDTH,200);

      nm.updateOverview();
    }

    private static VRectangle makeRect(double x1, double y1, 
            double x2, double y2){
        double w = Math.abs(x1 - x2);
        double h = Math.abs(y1 - y2);
        double cx = Math.min(x1, x2) + (w/2.);
        double cy = Math.min(y1, y2) + (h/2.);
        return new VRectangle(cx, cy, 0, w, h, Color.RED, Color.BLACK, 0.7f);
    }

    void windowLayout(){
        if (Utils.osIsWindows()){
            VIEW_X = VIEW_Y = 0;
        }
        else if (Utils.osIsMacOS()){
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
		nm.updateOverviewLocation();
	}
    
    /* --------------- Main/exit ------------------*/
    
    void exit(){
        System.exit(0);
    }
    
    public static void main(String[] args){
		boolean fs = false;
		boolean ogl = false;
		boolean aa = true;
    String filename = "";
		for (int i=0;i<args.length;i++){
			if (args[i].startsWith("-")){
				if (args[i].substring(1).equals("fs")){fs = true;}
				else if (args[i].substring(1).equals("opengl")){
				    System.setProperty("sun.java2d.opengl", "true");
				    ogl = true;
				}
				else if (args[i].substring(1).equals("noaa")){aa = false;}
				else if (args[i].substring(1).equals("h") || args[i].substring(1).equals("-help")){Messages.printCmdLineHelp();System.exit(0);}
			} else {
        filename = args[i];
      }
		}
        if (!fs && Utils.osIsMacOS()){
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        System.out.println(Messages.H_4_HELP);
        new Viewer(filename, fs, ogl, aa);
    }
    
}

class VWGlassPane extends JComponent {
    
    static final int BAR_WIDTH = 200;
    static final int BAR_HEIGHT = 10;

    static final AlphaComposite GLASS_ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f);    
    static final Color MSG_COLOR = Color.DARK_GRAY;
    GradientPaint PROGRESS_GRADIENT = new GradientPaint(0, 0, Color.ORANGE, 0, BAR_HEIGHT, Color.BLUE);

    String msg = Messages.EMPTY_STRING;
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
        if (msg != Messages.EMPTY_STRING && msg.length() > 0){
            g2.setColor(MSG_COLOR);
            g2.setFont(Config.GLASSPANE_FONT);
            g2.drawString(msg, msgX, msgY);
        }
        g2.setPaint(PROGRESS_GRADIENT);
        g2.fillRect(prX, prY, prW, BAR_HEIGHT);
        g2.setColor(MSG_COLOR);
        g2.drawRect(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }
    
}
