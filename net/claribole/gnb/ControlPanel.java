/*   FILE: ControlPanel.java
 *   DATE OF CREATION:  Mon Oct 24 08:46:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */ 

package net.claribole.gnb;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JComboBox;

class ControlPanel extends JFrame implements ItemListener {

    GeonamesBrowser application;

    JComboBox magLensCbb;

    ControlPanel(GeonamesBrowser app, int x, int y, int w, int h){
	super();
	this.application = app;
	Container cpane = this.getContentPane();
	cpane.setLayout(new GridLayout(3,1));
	initFisheyeLensSelector(cpane);
	initFresnelMapLensSelector(cpane);
	initFresnelDetailLensSelector(cpane);
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

    }

    void initFresnelDetailLensSelector(Container parent){

    }

    void selectMagnificationLens(short lensID){// lensID is one of GeonamesBrowser.L{2,Inf}_*
	application.lensFamily = lensID;
    }

    public void itemStateChanged(ItemEvent e){
	if (e.getSource() == magLensCbb){
	    if (e.getStateChange() == ItemEvent.SELECTED){
		selectMagnificationLens((short)Utils.getItemIndex(Messages.MAG_LENS_NAMES, e.getItem()));
	    }
	}
    }

}