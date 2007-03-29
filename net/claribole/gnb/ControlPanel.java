/*   FILE: ControlPanel.java
 *   DATE OF CREATION:  Mon Oct 24 08:46:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.gnb;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;

import fr.inria.jfresnel.jena.JenaLens;

class ControlPanel extends JFrame implements ItemListener, ActionListener {

    GeonamesBrowser application;

    JComboBox magLensCbb;
    
    JComboBox layoutLensCbb;
    JTextArea layoutLensTa;

    JComboBox detailLensCbb;
    JTextArea detailLensTa;
    
    JCheckBox adaptMapsCb;

    ControlPanel(GeonamesBrowser app, int x, int y, int w, int h){
	super();
	this.application = app;
	Container cpane = this.getContentPane();
	cpane.setLayout(new GridLayout(7,1));
	initFisheyeLensSelector(cpane);
	cpane.add(new HSepPanel(2, true, Color.BLACK));
	initFresnelMapLensSelector(cpane);
	cpane.add(new HSepPanel(2, true, Color.BLACK));
	initFresnelDetailLensSelector(cpane);
	cpane.add(new HSepPanel(2, true, Color.BLACK));
	adaptMapsCb = new JCheckBox(Messages.ADAPT_MAPS_CHECKBOX, true);
	cpane.add(adaptMapsCb);
	adaptMapsCb.addActionListener(this);
	this.setLocation(x,y);
	this.setSize(w, h);
	this.setVisible(true);
    }

    void initFisheyeLensSelector(Container parent){
	Container c = new Container();
	c.setLayout(new GridLayout(2,1));
	c.add(new JLabel(Messages.CP_FISHEYE_PANEL_TITLE));
	magLensCbb = new JComboBox(Messages.MAG_LENS_NAMES);
	magLensCbb.addItemListener(this);
	c.add(magLensCbb);
	parent.add(c);
    }

    void initFresnelMapLensSelector(Container parent){
	Container c = new Container();
	c.setLayout(new GridLayout(3,1));
	c.add(new JLabel(Messages.CP_LAYOUT_PANEL_TITLE));
	layoutLensCbb = new JComboBox(application.fm.layoutLenses);
	layoutLensCbb.addItemListener(this);
	c.add(layoutLensCbb);
	layoutLensTa = new JTextArea(application.fm.layoutLenses[0].getComment());
	layoutLensTa.setLineWrap(true);
	layoutLensTa.setWrapStyleWord(true);
	c.add(layoutLensTa);
	parent.add(c);
    }

    void initFresnelDetailLensSelector(Container parent){
	Container c = new Container();
	c.setLayout(new GridLayout(3,1));
	c.add(new JLabel(Messages.CP_DETAIL_PANEL_TITLE));
	detailLensCbb = new JComboBox(application.fm.detailLenses);
	detailLensCbb.addItemListener(this);
	c.add(detailLensCbb);
	detailLensTa = new JTextArea(application.fm.detailLenses[0].getComment());
	detailLensTa.setLineWrap(true);
	detailLensTa.setWrapStyleWord(true);
	c.add(detailLensTa);
	parent.add(c);
    }

    void selectMagnificationLens(short lensID){// lensID is one of GeonamesBrowser.L{2,Inf}_*
	application.lensFamily = lensID;
    }
    
    void selectLayoutLens(JenaLens lens){
	application.fm.selectedLayoutLens = lens;
	layoutLensTa.setText(lens.getComment());
    }

    void selectDetailLens(JenaLens lens){
	application.fm.selectedDetailLens = lens;
	detailLensTa.setText(lens.getComment());
    }

    public void itemStateChanged(ItemEvent e){
	if (e.getSource() == magLensCbb){
	    if (e.getStateChange() == ItemEvent.SELECTED){
		selectMagnificationLens((short)Utils.getItemIndex(Messages.MAG_LENS_NAMES, e.getItem()));
	    }
	}
	else if (e.getSource() == detailLensCbb){
	    if (e.getStateChange() == ItemEvent.SELECTED){
		selectDetailLens((JenaLens)e.getItem());
	    }
	}
	else if (e.getSource() == layoutLensCbb){
	    if (e.getStateChange() == ItemEvent.SELECTED){
		selectLayoutLens((JenaLens)e.getItem());
	    }
	}
    }

    public void actionPerformed(ActionEvent e){
	if (e.getSource() == adaptMapsCb){
	    application.mm.switchAdaptMaps();
	}
    }

}