/*   FILE: LocateTaskQPanel.java
 *   DATE OF CREATION:  Sun Apr 23 13:05:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.eval;

import java.awt.Container;
import java.awt.Color;
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

class LocateTaskQPanel extends JFrame {

    static final String YES = "Yes";
    static final String NO = "No";

    static final Color SAY_BKG_COLOR = Color.GRAY;
    static final Color SAY_FRG_COLOR = Color.BLACK;
    static final Color ASK_BKG_COLOR = Color.GRAY;
    static final Color ASK_FRG_COLOR = Color.BLACK;

    static final Font MESSAGE_FONT = new Font("Arial", Font.PLAIN, 12);

    LocateTask application;

    LocateTaskQPanel(int x, int y, int w, int h, LocateTask app){
	super();
	this.application = app;
	this.setUndecorated(true);
	this.setSize(w, h);
	this.setLocation(x, y);
	this.setVisible(true);
    }

    void ask(String s){
	Container cpane = this.getContentPane();
	cpane.removeAll();
	validate();
	JPanel mainPanel = new JPanel();
	cpane.add(mainPanel);
	mainPanel.setBackground(ASK_BKG_COLOR);
	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	mainPanel.setLayout(gridBag);
	constraints.fill=GridBagConstraints.HORIZONTAL;
	constraints.anchor=GridBagConstraints.CENTER;
	JLabel l0 = new JLabel(s);
	l0.setForeground(ASK_FRG_COLOR);
	l0.setFont(MESSAGE_FONT);
	InstructionsPanel.buildConstraints(constraints,0,0,1,1,70,100);
	gridBag.setConstraints(l0,constraints);
	mainPanel.add(l0);
	final JButton yesBt = new JButton(YES);
	InstructionsPanel.buildConstraints(constraints,1,0,1,1,15,0);
	gridBag.setConstraints(yesBt,constraints);
	mainPanel.add(yesBt);
	final JButton noBt = new JButton(NO);
	InstructionsPanel.buildConstraints(constraints,2,0,1,1,15,0);
	gridBag.setConstraints(noBt,constraints);
	mainPanel.add(noBt);
	ActionListener a1=new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    if (e.getSource() == yesBt){
			answerYes();
		    }
		    else if (e.getSource() == noBt){
			answerNo();
		    }
		}
	    };
	yesBt.addActionListener(a1);
	noBt.addActionListener(a1);
	validate();
    }

    void answerYes(){
	application.aboutToLocateCity();
    }

    void answerNo(){
	application.doesNotKnow();
    }

    void say(String s){
	Container cpane = this.getContentPane();
	cpane.removeAll();
	validate();
	JPanel mainPanel = new JPanel();
	mainPanel.setBackground(SAY_BKG_COLOR);
	cpane.add(mainPanel);
	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	mainPanel.setLayout(gridBag);
	constraints.fill=GridBagConstraints.HORIZONTAL;
	constraints.anchor=GridBagConstraints.CENTER;
	JLabel l0 = new JLabel(s);
	l0.setForeground(SAY_FRG_COLOR);
	l0.setFont(MESSAGE_FONT);
	InstructionsPanel.buildConstraints(constraints,0,0,1,1,100,100);
	gridBag.setConstraints(l0,constraints);
	mainPanel.add(l0);
	validate();
    }

}