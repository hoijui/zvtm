/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.layout.jung;

import java.awt.Font;
import java.awt.Color;
import java.awt.Toolkit;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

class ConfigManager {
    
    /* Graph appearance */
    static final int GRAPH_SIZE_FACTOR = 20;
    static final int DEFAULT_NODE_SIZE = 10;
    static final Color DEFAULT_NODE_COLOR = Color.WHITE;
    static final Color DEFAULT_EDGE_COLOR = Color.BLACK;
    
    /* Fonts */
	static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12);
    static final Font GLASSPANE_FONT = new Font("Arial", Font.PLAIN, 12);
    static final Font SAY_MSG_FONT = new Font("Arial", Font.PLAIN, 24);
    static final Font MONOSPACE_ABOUT_FONT = new Font("Courier", Font.PLAIN, 8);
    
    /* Other colors */
    static final Color SAY_MSG_COLOR = Color.LIGHT_GRAY;
    static Color BACKGROUND_COLOR  = Color.LIGHT_GRAY;
    static final Color FADE_REGION_FILL = Color.BLACK;
    static final Color FADE_REGION_STROKE = Color.WHITE;
    
    /* Overview */
    static final int OVERVIEW_WIDTH = 200;
	static final int OVERVIEW_HEIGHT = 200;
	static final Color OBSERVED_REGION_COLOR = Color.GREEN;
	static final float OBSERVED_REGION_ALPHA = 0.5f;
	static final Color OV_BORDER_COLOR = Color.WHITE;
	static final Color OV_INSIDE_BORDER_COLOR = Color.WHITE;
    
    /* Durations/Animations */
    static final int ANIM_MOVE_LENGTH = 300;
    static final int SAY_DURATION = 500;

    /* External resources */
    static final String INSITU_LOGO_PATH = "/images/insitu.png";
    static final String INRIA_LOGO_PATH = "/images/inria.png";
 
    /* Swing Menu */
 	static JMenuBar initMenu(final Viewer app){
		final JMenuItem openMI = new JMenuItem("Open...");
		openMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		final JMenuItem exitMI = new JMenuItem("Exit");
		exitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		final JMenuItem aboutMI = new JMenuItem("About...");
		ActionListener a0 = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (e.getSource()==openMI){app.openFile();}
				if (e.getSource()==exitMI){app.exit();}
				else if (e.getSource()==aboutMI){app.ovm.showAbout();}
			}
		};
		JMenuBar jmb = new JMenuBar();
		JMenu fileM = new JMenu("File");
		JMenu helpM = new JMenu("Help");
		fileM.add(openMI);
		fileM.addSeparator();
		fileM.add(exitMI);
		helpM.add(aboutMI);
		jmb.add(fileM);
		jmb.add(helpM);
		openMI.addActionListener(a0);
		exitMI.addActionListener(a0);
		aboutMI.addActionListener(a0);
		return jmb;
	}
	
	/* Pie menu */
	
	static final Font PIEMENU_MAIN_FONT = new Font("Arial", 0, 10);
	static final Font PIEMENU_SUB_FONT = new Font("Arial", 0, 8);
	
	static Color PIEMENU_FILL_COLOR = Color.BLACK;
    static Color PIEMENU_BORDER_COLOR = Color.WHITE;
    static Color PIEMENU_INSIDE_COLOR = Color.DARK_GRAY;
	static final float PIEMENU_MAIN_ALPHA = 0.85f;
	static final float PIEMENU_SUB_ALPHA = 0.95f;
	static final int PIEMENU_MAIN_RADIUS = 75;
	static final int PIEMENU_SUB_RADIUS = 60;

}
