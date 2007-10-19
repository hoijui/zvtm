/*   FILE: MapMonitor.java
 *   DATE OF CREATION:  Wed Apr  5 12:07:02 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JPanel;


class MapMonitor extends JFrame {

    ZLWorldTask application;

    JPanel panel;

    MapMonitor(String frameTitle, int x, int y, int width, int height, ZLWorldTask app){
	super();
	this.application = app;
	Container cpane=this.getContentPane();
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor=GridBagConstraints.CENTER;
	cpane.setLayout(gridBag);
	panel = new MonitorPanel(this);
	buildConstraints(constraints,0,0,1,1,100,100);
	gridBag.setConstraints(panel,constraints);
	cpane.add(panel);
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){dispose();}
	    };
	this.addWindowListener(w0);
	this.setTitle(frameTitle);
	this.setLocation(x,y);
	this.setSize(width,height);
	this.setVisible(true);
    }

    void updateMaps(){
	repaint();
    }

    void buildConstraints(GridBagConstraints gbc, int gx,int gy,int gw,int gh,int wx,int wy){
	gbc.gridx=gx;
	gbc.gridy=gy;
	gbc.gridwidth=gw;
	gbc.gridheight=gh;
	gbc.weightx=wx;
	gbc.weighty=wy;
    }

}

class MonitorPanel extends JPanel {

    static final Color U_COLOR = Color.black;
    static final Color L_COLOR = Color.green;

    static final Color P_COLOR = Color.orange;
    static final Color W_COLOR = Color.red;

    MapMonitor mm;
    ZLWorldTaskMapManager ewmm;

    int panelWidth;
    int xi;

    static final int HSBB = 5; // horizontal space between bars
    static final int VSBB = 40; // vertical space between bars
    static final int BH = 10; // bar height
    static final int BW = 2; // bar height
    static final int SBE = 5; // space between elements of a bar
    
    
    static final int L0y = 10;
    static final int L1y = L0y + VSBB;
    static final int L1ly = L1y - SBE;
    static final int L1uy = L1y + BH + SBE - 2;
    static final int L2y = L1y + VSBB;
    static final int L2ly = L2y - SBE;
    static final int L2uy = L2y + BH + SBE - 2;
    static final int L3y = L2y + VSBB;
    static final int L3ly = L3y - SBE;
    static final int L3uy = L3y + BH + SBE - 2;

    MonitorPanel(MapMonitor mm){
	this.mm = mm;
	this.ewmm = mm.application.ewmm;
	this.setBackground(Color.DARK_GRAY);
    }

    public void paint(Graphics g){
	super.paint(g);
	panelWidth = this.getWidth();
	// main map
	if (ewmm.mainMap == null){g.setColor(U_COLOR);}
	else {g.setColor(L_COLOR);}
	g.fillRect(HSBB, L0y, BW, BH);
	// M1N00
	for (int i=0;i<ewmm.M1N00im.length;i++){
	    g.setColor((ewmm.M1N00im[i] == null) ? U_COLOR : L_COLOR);
	    xi = HSBB*(i+1);
	    g.fillRect(xi, L1y, BW, BH);
	    if (ewmm.M1N00lrq[i] != null){
		g.setColor((ewmm.M1N00lrqs[i]) ? P_COLOR : W_COLOR);
		g.fillRect(xi, L1ly, BW, 2);
	    }
	    if (ewmm.M1N00urq[i] != null){
		g.setColor((ewmm.M1N00urqs[i]) ? P_COLOR : W_COLOR);
		g.fillRect(xi, L1uy, BW, 2);		
	    }
	}
	// M1NN0
	for (int i=0;i<ewmm.M1NN0im.length;i++){
	    g.setColor((ewmm.M1NN0im[i] == null) ? U_COLOR : L_COLOR);
	    xi = HSBB*(i+1);
	    g.fillRect(xi, L2y, BW, BH);
	    if (ewmm.M1NN0lrq[i] != null){
		g.setColor((ewmm.M1NN0lrqs[i]) ? P_COLOR : W_COLOR);
		g.fillRect(xi, L2ly, BW, 2);
	    }
	    if (ewmm.M1NN0urq[i] != null){
		g.setColor((ewmm.M1NN0urqs[i]) ? P_COLOR : W_COLOR);
		g.fillRect(xi, L2uy, BW, 2);		
	    }
	}
	// M1NNN
	for (int i=0;i<ewmm.M1NNNim.length;i++){
	    g.setColor((ewmm.M1NNNim[i] == null) ? U_COLOR : L_COLOR);
	    xi = HSBB*(i+1);
	    g.fillRect(xi, L3y, BW, BH);
	    if (ewmm.M1NNNlrq[i] != null){
		g.setColor((ewmm.M1NNNlrqs[i]) ? P_COLOR : W_COLOR);
		g.fillRect(xi, L3ly, BW, 2);
	    }
	    if (ewmm.M1NNNurq[i] != null){
		g.setColor((ewmm.M1NNNurqs[i]) ? P_COLOR : W_COLOR);
		g.fillRect(xi, L3uy, BW, 2);		
	    }
	}
    }
    
}