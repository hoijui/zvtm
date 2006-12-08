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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;

/* Navigation Panel (directional arrows, plus zoom) */

class NavPanel extends JPanel implements ActionListener, ChangeListener {

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
    
    JCheckBox aaCb;
    
    JButton aboutBt;

    NavPanel(GraphicsManager gm, String initialSearchString){
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
	// search widgets
	SearchPanel p3 = new SearchPanel(grMngr, initialSearchString);
	buildConstraints(constraints,0,2,1,1,0,30);
	gridBag.setConstraints(p3, constraints);
	this.add(p3);
	// antialiasing checkbox
	aaCb = new JCheckBox("Antialiasing", grMngr.mainView.getAntialiasing());
	aaCb.setOpaque(false);
	buildConstraints(constraints,0,3,1,1,0,30);
	gridBag.setConstraints(aaCb, constraints);
	this.add(aaCb);
	aaCb.addChangeListener(this);
	// misc. widgets
	JPanel p4 = new JPanel();
	p4.setOpaque(false);
	aboutBt = new JButton("About...");
	aboutBt.setOpaque(false);
	aboutBt.addActionListener(this);
	p4.add(aboutBt);
	buildConstraints(constraints,0,4,1,1,0,38);
	gridBag.setConstraints(p4, constraints);
	this.add(p4);
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
	else if (o==aboutBt){grMngr.zapp.about();}
    }

    public void stateChanged(ChangeEvent e){
	Object o = e.getSource();
	if (o==aaCb){
	    grMngr.setAntialiasing(aaCb.isSelected());
	}
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

class SearchPanel extends JPanel implements ActionListener, KeyListener {

    GraphicsManager grMngr;
    JTextField findTf;
    JButton prevBt, nextBt;
    
    SearchPanel(GraphicsManager gm, String initialSearchString){
	this.setOpaque(false);
	this.grMngr = gm;
	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.CENTER;
	this.setLayout(gridBag);
	JLabel findLb = new JLabel("Find");
	NavPanel.buildConstraints(constraints,0,0,1,1,100,25);
	gridBag.setConstraints(findLb, constraints);
	this.add(findLb);
	findTf = new JTextField(initialSearchString);
	NavPanel.buildConstraints(constraints,0,1,1,1,100,25);
	gridBag.setConstraints(findTf, constraints);
	this.add(findTf);
	findTf.addKeyListener(this);
	prevBt = new JButton("Previous");
	prevBt.setOpaque(false);
	NavPanel.buildConstraints(constraints,0,2,1,1,100,25);
	gridBag.setConstraints(prevBt, constraints);
	this.add(prevBt);
	prevBt.addActionListener(this);
	nextBt = new JButton("Next");
	nextBt.setOpaque(false);
	NavPanel.buildConstraints(constraints,0,3,1,1,100,25);
	gridBag.setConstraints(nextBt, constraints);
	this.add(nextBt);
	nextBt.addActionListener(this);
    }
    
    public void actionPerformed(ActionEvent e){
	if (e.getSource() == prevBt){grMngr.search(findTf.getText(), -1);}
	else {grMngr.search(findTf.getText(), 1);}
    }

    public void keyPressed(KeyEvent e){
	if (e.getKeyCode()==KeyEvent.VK_ENTER){
	    grMngr.search(findTf.getText(), 1);
	}
    }

    public void keyReleased(KeyEvent e){}

    public void keyTyped(KeyEvent e){}

}