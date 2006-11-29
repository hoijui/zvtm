/*   FILE: ZGRApplet.java
 *   DATE OF CREATION:   Fri May 09 09:52:34 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.zgrviewer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;

/* Navigation Panel (directional arrows, plus zoom) */

class NavPanel extends JPanel implements ActionListener {

    GraphicsManager grMngr;

    // pan buttons: NW, N, NE, W, H, E, SW, S, SE
    JButton[] panBts = new JButton[9];
    // icons for these buttons
    final ImageIcon[] icons = {new ImageIcon(this.getClass().getResource("/images/zgrv/m_nw.png")),
			       new ImageIcon(this.getClass().getResource("/images/zgrv/m_n.png")),
			       new ImageIcon(this.getClass().getResource("/images/zgrv/m_ne.png")),
			       new ImageIcon(this.getClass().getResource("/images/zgrv/m_w.png")),
			       new ImageIcon(this.getClass().getResource("/images/zgrv/m_home.png")),
			       new ImageIcon(this.getClass().getResource("/images/zgrv/m_e.png")),
			       new ImageIcon(this.getClass().getResource("/images/zgrv/m_sw.png")),
			       new ImageIcon(this.getClass().getResource("/images/zgrv/m_s.png")),
			       new ImageIcon(this.getClass().getResource("/images/zgrv/m_se.png")),
			       new ImageIcon(this.getClass().getResource("/images/zgrv/zm_i.png")),
			       new ImageIcon(this.getClass().getResource("/images/zgrv/zm_o.png"))};
    // rollover icons for these buttons
    final ImageIcon[] r_icons = {new ImageIcon(this.getClass().getResource("/images/zgrv/m_nw_h.png")),
				 new ImageIcon(this.getClass().getResource("/images/zgrv/m_n_h.png")),
				 new ImageIcon(this.getClass().getResource("/images/zgrv/m_ne_h.png")),
				 new ImageIcon(this.getClass().getResource("/images/zgrv/m_w_h.png")),
				 new ImageIcon(this.getClass().getResource("/images/zgrv/m_home_h.png")),
				 new ImageIcon(this.getClass().getResource("/images/zgrv/m_e_h.png")),
				 new ImageIcon(this.getClass().getResource("/images/zgrv/m_sw_h.png")),
				 new ImageIcon(this.getClass().getResource("/images/zgrv/m_s_h.png")),
				 new ImageIcon(this.getClass().getResource("/images/zgrv/m_se_h.png")),
				 new ImageIcon(this.getClass().getResource("/images/zgrv/zm_i_h.png")),
				 new ImageIcon(this.getClass().getResource("/images/zgrv/zm_o_h.png"))};
    // zoom buttons: zoom in, zoom out
    JButton[] zoomBts = new JButton[2];

    NavPanel(GraphicsManager gm){
	super();
	this.setOpaque(false);
	this.grMngr = gm;


	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	this.setLayout(gridBag);

	//translation buttons in a 3x3 grid
	JPanel p1 = new JPanel();
	p1.setLayout(new GridLayout(3,3));
	p1.setOpaque(false);
	for (int i=0;i<panBts.length;i++){
	    panBts[i] = new JButton(icons[i]);
	    panBts[i].setBorder(BorderFactory.createEmptyBorder());
	    panBts[i].setContentAreaFilled(false);
	    panBts[i].setBorderPainted(false);
	    panBts[i].setFocusPainted(false);
	    panBts[i].setRolloverIcon(r_icons[i]);
	    panBts[i].addActionListener(this);
	    p1.add(panBts[i]);
	}

	buildConstraints(constraints,0,0,1,1,100,1);
	gridBag.setConstraints(p1, constraints);
	this.add(p1);

	//zoom buttons
	JPanel p2 = new JPanel();
	p2.setLayout(new GridLayout(2,1));
	p2.setOpaque(false);
	for (int i=0;i<zoomBts.length;i++){
	    zoomBts[i] = new JButton(icons[i+panBts.length]);
	    zoomBts[i].setBorder(BorderFactory.createEmptyBorder());
	    zoomBts[i].setContentAreaFilled(false);
	    zoomBts[i].setBorderPainted(false);
	    zoomBts[i].setFocusPainted(false);
	    zoomBts[i].setRolloverIcon(r_icons[i+panBts.length]);
	    zoomBts[i].addActionListener(this);
	    p2.add(zoomBts[i]);
	}
	buildConstraints(constraints,0,1,1,1,0,1);
	gridBag.setConstraints(p2, constraints);
	this.add(p2);

	JPanel p3 = new JPanel();
	p3.setOpaque(false);
	buildConstraints(constraints,0,2,1,1,0,98);
	gridBag.setConstraints(p3, constraints);
	this.add(p3);
    }

    public void actionPerformed(ActionEvent e){
	Object o = e.getSource();
	if (o==zoomBts[0]){grMngr.getLowerView();}
	else if (o==zoomBts[1]){grMngr.getHigherView();}
	else if (o==panBts[4]){grMngr.getGlobalView();}
	else if (o==panBts[1]){grMngr.translateView(GraphicsManager.MOVE_UP);}
	else if (o==panBts[7]){grMngr.translateView(GraphicsManager.MOVE_DOWN);}
	else if (o==panBts[5]){grMngr.translateView(GraphicsManager.MOVE_RIGHT);}
	else if (o==panBts[3]){grMngr.translateView(GraphicsManager.MOVE_LEFT);}
	else if (o==panBts[0]){grMngr.translateView(GraphicsManager.MOVE_UP_LEFT);}
	else if (o==panBts[2]){grMngr.translateView(GraphicsManager.MOVE_UP_RIGHT);}
	else if (o==panBts[6]){grMngr.translateView(GraphicsManager.MOVE_DOWN_LEFT);}
	else if (o==panBts[8]){grMngr.translateView(GraphicsManager.MOVE_DOWN_RIGHT);}
    }

    static void buildConstraints(GridBagConstraints gbc, int gx,int gy,int gw,int gh,int wx,int wy){
	gbc.gridx = gx;
	gbc.gridy = gy;
	gbc.gridwidth = gw;
	gbc.gridheight = gh;
	gbc.weightx = wx;
	gbc.weighty = wy;
    }

}
