/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.viewer;

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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import java.awt.Container;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import java.util.Vector;

import java.io.File;
import java.io.IOException;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.Utilities;
import com.xerox.VTM.engine.SwingWorker;
import com.xerox.VTM.glyphs.Glyph;
import net.claribole.zvtm.glyphs.PieMenu;
import net.claribole.zvtm.glyphs.PieMenuFactory;

import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.Region;
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
    static final String mSpaceName = "Scene Space";
    static final String mnSpaceName = "PieMenu Space";
    VirtualSpace mSpace;
    Camera mCamera;
    static final String mViewName = "ZUIST Viewer";
    View mView;
    ViewerEventHandler eh;

    SceneManager sm;

	VWGlassPane gp;
	PieMenu mainPieMenu;
    
    public Viewer(boolean fullscreen, boolean antialiased, File xmlSceneFile){
		initGUI(fullscreen, antialiased);
        VirtualSpace[]  sceneSpaces = {mSpace};
        Camera[] sceneCameras = {mCamera};
        sm = new SceneManager(vsm, sceneSpaces, sceneCameras);
        sm.setSceneCameraBounds(mCamera, eh.wnes);
        if (xmlSceneFile != null){
			loadScene(xmlSceneFile);
			getGlobalView();
		}
    }

    void initGUI(boolean fullscreen, boolean antialiased){
        windowLayout();
        vsm = new VirtualSpaceManager();
        mSpace = vsm.addVirtualSpace(mSpaceName);
        vsm.addVirtualSpace(mnSpaceName);
        mCamera = vsm.addCamera(mSpace);
		vsm.addCamera(mnSpaceName).setAltitude(10);
        Vector cameras = new Vector();
        cameras.add(mCamera);
		cameras.add(vsm.getVirtualSpace(mnSpaceName).getCamera(0));
        mView = vsm.addExternalView(cameras, mViewName, View.STD_VIEW, VIEW_W, VIEW_H, false, false, !fullscreen, initMenu());
        if (fullscreen){
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        }
        else {
            mView.setVisible(true);
        }
        updatePanelSize();
		gp = new VWGlassPane(this);
		((JFrame)mView.getFrame()).setGlassPane(gp);
        eh = new ViewerEventHandler(this);
        mView.setEventHandler(eh, 0);
        mView.setEventHandler(eh, 1);
        mView.setNotifyMouseMoved(true);
        mView.setBackgroundColor(Color.WHITE);
		mView.setAntialiasing(antialiased);
        vsm.animator.setAnimationListener(eh);
    }

	private JMenuBar initMenu(){
		final JMenuItem openMI = new JMenuItem("Open...");
		openMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		final JMenuItem reloadMI = new JMenuItem("Reload");
		reloadMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		final JMenuItem exitMI = new JMenuItem("Exit");
		exitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		final JMenuItem aboutMI = new JMenuItem("About...");
		ActionListener a0 = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (e.getSource()==openMI){openFile();}
				else if (e.getSource()==reloadMI){reload();}
				else if (e.getSource()==exitMI){exit();}
				else if (e.getSource()==aboutMI){about();}
			}
		};
		JMenuBar jmb = new JMenuBar();
		JMenu fileM = new JMenu("File");
		JMenu helpM = new JMenu("Help");
		fileM.add(openMI);
		fileM.add(reloadMI);
		fileM.addSeparator();
		fileM.add(exitMI);
		helpM.add(aboutMI);
		jmb.add(fileM);
		jmb.add(helpM);
		openMI.addActionListener(a0);
		reloadMI.addActionListener(a0);
		exitMI.addActionListener(a0);
		aboutMI.addActionListener(a0);
		return jmb;
	}
	
	void displayMainPieMenu(boolean b){
		if (b){
			PieMenuFactory.setItemFillColor(ConfigManager.PIEMENU_FILL_COLOR);
			PieMenuFactory.setItemBorderColor(ConfigManager.PIEMENU_BORDER_COLOR);
			PieMenuFactory.setSelectedItemFillColor(ConfigManager.PIEMENU_INSIDE_COLOR);
			PieMenuFactory.setSelectedItemBorderColor(null);
			PieMenuFactory.setLabelColor(ConfigManager.PIEMENU_BORDER_COLOR);
			PieMenuFactory.setFont(ConfigManager.PIEMENU_FONT);
			PieMenuFactory.setTranslucency(0.7f);
			PieMenuFactory.setSensitivityRadius(0.5);
			PieMenuFactory.setAngle(-Math.PI/2.0);
			PieMenuFactory.setRadius(150);
			mainPieMenu = PieMenuFactory.createPieMenu(Messages.mainMenuLabels, Messages.mainMenuLabelOffsets, 0, mView, vsm);
			Glyph[] items = mainPieMenu.getItems();
			items[0].setType(Messages.PM_ENTRY);
			items[1].setType(Messages.PM_ENTRY);
			items[2].setType(Messages.PM_ENTRY);
			items[3].setType(Messages.PM_ENTRY);
		}
		else {
			mainPieMenu.destroy(0);
			mainPieMenu = null;
		}
	}

	void pieMenuEvent(Glyph menuItem){
		int index = mainPieMenu.getItemIndex(menuItem);
		if (index != -1){
			String label = mainPieMenu.getLabels()[index].getText();
			if (label == Messages.PM_BACK){moveBack();}
			else if (label == Messages.PM_GLOBALVIEW){getGlobalView();}
			else if (label == Messages.PM_OPEN){openFile();}
			else if (label == Messages.PM_RELOAD){reload();}
		}
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

	/*-------------  Scene management    -------------*/
	
	void reset(){
		sm.reset();
		vsm.destroyGlyphsInSpace(mSpaceName);
	}
	
	void openFile(){
		final JFileChooser fc = new JFileChooser(SCENE_FILE_DIR);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Find ZUIST Scene File");
		int returnVal= fc.showOpenDialog(mView.getFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION){
		    final SwingWorker worker = new SwingWorker(){
			    public Object construct(){
					reset();
					loadScene(fc.getSelectedFile());
					getGlobalView();
					return null; 
			    }
			};
		    worker.start();
		}
	}
	
	void reload(){
		if (SCENE_FILE==null){return;}
		final SwingWorker worker = new SwingWorker(){
		    public Object construct(){
				reset();
				loadScene(SCENE_FILE);
				return null; 
		    }
		};
	    worker.start();
	}

	void loadScene(File xmlSceneFile){
		gp.setValue(0);
		gp.setVisible(true);
		SCENE_FILE = xmlSceneFile;
	    SCENE_FILE_DIR = SCENE_FILE.getParentFile();
	    sm.loadScene(parseXML(SCENE_FILE), SCENE_FILE_DIR, gp);
	    gp.setVisible(false);
	    gp.setLabel(VWGlassPane.EMPTY_STRING);
        mCamera.setAltitude(0.0f);
        sm.updateLevel(mCamera.altitude);
        eh.cameraMoved();
	}
    
    /*-------------     Navigation       -------------*/

    void getGlobalView(){
		int l = 0;
		while (sm.getRegionsAtLevel(l) == null){
			l++;
			if (l > sm.getLevelCount()){
				l = -1;
				break;
			}
		}
		if (l > -1){
			long[] wnes = sm.getLevel(l).getBounds();
	        vsm.centerOnRegion(mCamera, Viewer.ANIM_MOVE_LENGTH, wnes[0], wnes[1], wnes[2], wnes[3]);		
		}
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

	void moveBack(){
		System.out.println("Moving back");
		
	}
	
    void altitudeChanged(){
        sm.updateLevel(mCamera.altitude);
    }
    
    void updatePanelSize(){
        Dimension d = mView.getPanel().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
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
    
    public void about(){
		JOptionPane.showMessageDialog(mView.getFrame(), Messages.ABOUT_MSG);
    }

    void exit(){
        System.exit(0);
    }

    public static void main(String[] args){
        File xmlSceneFile = null;
		boolean fs = false;
		boolean aa = true;
		for (int i=0;i<args.length;i++){
			if (args[i].startsWith("-")){
				if (args[i].substring(1).equals("fs")){fs = true;}
				else if (args[i].substring(1).equals("noaa")){aa = false;}
				else if (args[i].substring(1).equals("h") || args[i].substring(1).equals("--help")){Messages.printCmdLineHelp();System.exit(0);}
			}
            else {
                // the only other thing allowed as a cmd line param is a scene file
                File f = new File(args[i]);
                if (f.exists()){xmlSceneFile = f;}
            }
		}
        if (!fs && Utilities.osIsMacOS()){
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        System.out.println("--help for command line options");
        new Viewer(fs, aa, xmlSceneFile);
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

	static Color PIEMENU_FILL_COLOR = Color.BLACK;
	static Color PIEMENU_BORDER_COLOR = Color.WHITE;
	static Color PIEMENU_INSIDE_COLOR = Color.DARK_GRAY;
	
	static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12);

    static final Font PIEMENU_FONT = DEFAULT_FONT;

    static final Font GLASSPANE_FONT = new Font("Arial", Font.PLAIN, 12);

}
