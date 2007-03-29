/*   FILE: InstructionsPanel.java
 *   DATE OF CREATION:  Tue Apr 25 13:15:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.eval;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;

import com.xerox.VTM.engine.SwingWorker;

class InstructionsPanel extends JFrame {

    static final Color SAY_BKG_COLOR = Color.GRAY;
    static final Color SAY_FRG_COLOR = Color.BLACK;
    static final Color WARN_BKG_COLOR = Color.BLACK;
    static final Color WARN_FRG_COLOR = Color.RED;

    static final Font MESSAGE_FONT = new Font("Arial", Font.PLAIN, 16);

    JPanel mainPanel;
    JLabel msgLb;
    
    InstructionsPanel(int x, int y, int w, int h){
	super();
	Container cpane = this.getContentPane();
	mainPanel = new JPanel();
	cpane.add(mainPanel);
	mainPanel.setBackground(Color.GRAY);
	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	mainPanel.setLayout(gridBag);
	constraints.fill=GridBagConstraints.NONE;
	constraints.anchor=GridBagConstraints.WEST;
	msgLb = new JLabel();
	msgLb.setFont(MESSAGE_FONT);
	buildConstraints(constraints, 0, 0, 1, 1, 100, 100);
	gridBag.setConstraints(msgLb,constraints);
	mainPanel.add(msgLb);
	this.setUndecorated(true);
	this.setSize(w, h);
	this.setLocation(x, y);
    }

    void say(String s){
	mainPanel.setBackground(SAY_BKG_COLOR);
	msgLb.setForeground(SAY_FRG_COLOR);
	msgLb.setText(s);
    }

    void warn(final String s1, final String s2, final int delay){
	mainPanel.setBackground(WARN_BKG_COLOR);
	msgLb.setForeground(WARN_FRG_COLOR);
	/* display error message for delay ms
	   and revert back to previous message */
	final SwingWorker worker=new SwingWorker(){
		public Object construct(){
		    msgLb.setText(s1);
		    sleep(delay);
		    InstructionsPanel.this.say(s2);
		    return null; 
		}
	    };
	worker.start();
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