/*   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *   $Id$
 */

package net.claribole.zgrviewer;

// import java.awt.Color;
// import java.awt.Toolkit;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
// import java.awt.event.KeyEvent;
// import java.io.File;
// import java.io.InputStream;
// import java.net.URL;
// import java.util.Vector;

// import javax.swing.JCheckBoxMenuItem;
// import javax.swing.JFileChooser;
// import javax.swing.JFrame;
// import javax.swing.JMenu;
import javax.swing.JMenuBar;
// import javax.swing.JMenuItem;
// import javax.swing.JOptionPane;
// import javax.swing.JPanel;
// import javax.swing.KeyStroke;

// import fr.inria.zvtm.engine.SwingWorker;
import fr.inria.zvtm.engine.View;
// import fr.inria.zvtm.glyphs.Glyph;
// import fr.inria.zvtm.widgets.PieMenu;
// import fr.inria.zvtm.widgets.PieMenuFactory;

// import javax.swing.ImageIcon;
// import javax.swing.SwingUtilities;

// import org.apache.xerces.dom.DOMImplementationImpl;
// import org.w3c.dom.Document;

public class WallZGRViewer extends ZGRViewer {

    public WallZGRViewer(boolean acc){
        super(acc);
    }

    public WallZGRViewer(){
        super();
    }

    void initGUI(boolean acc, boolean viewOnJPanel){
        exitVMonClose = !viewOnJPanel;
        cfgMngr.notifyPlugins(Plugin.NOTIFY_PLUGIN_GUI_INITIALIZING);
        Utils.initLookAndFeel();
        JMenuBar jmb = initViewMenu(acc);
        if (viewOnJPanel)
        {
        	_panelView = grMngr.createPanelView(grMngr.createZVTMelements(true), 100, 100);

        	//_panelView.setLocation(ConfigManager.mainViewX,ConfigManager.mainViewY);
        	_panelView.addComponentListener(grMngr);
        	_gp = new ZGRGlassPane(grMngr);

        	grMngr.gp = _gp;

            //((JFrame)_panelView.getFrame()).setGlassPane(gp);

        }
        else
        {
        	grMngr.createFrameView(grMngr.createZVTMelements(false), acc ? View.OPENGL_VIEW : View.STD_VIEW, jmb);
        }

        cfgMngr.notifyPlugins(Plugin.NOTIFY_PLUGIN_GUI_VIEW_CREATED);
        grMngr.parameterizeView(new ZgrvEvtHdlr(this, this.grMngr));
        cfgMngr.notifyPlugins(Plugin.NOTIFY_PLUGIN_GUI_INITIALIZED);
    }

}
