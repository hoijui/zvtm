/*   FILE: BiManSettings.java
 *   DATE OF CREATION:  Mon May 30 08:27:25 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: BiManSettings.java,v 1.3 2005/05/31 07:03:56 epietrig Exp $
 */

package net.claribole.zgrviewer.mpd;

import java.util.Vector;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import domino.Socket;

public class BiManSettings extends JFrame implements ChangeListener {

    static Dictionary tickLabels;
    static {
	tickLabels = new Hashtable();
	tickLabels.put(new Integer(5), new JLabel("0.5"));
	tickLabels.put(new Integer(10), new JLabel("1.0"));
	tickLabels.put(new Integer(20), new JLabel("2.0"));
    }

    BiManPlugin plugin;
    JComboBox devicesCbb;
    JSlider sensitivitySld;

    BiManSettings(BiManPlugin bmp){
	super();
	this.plugin = bmp;
	setLayout(new GridLayout(4,1));
	// init device list
	add(new JLabel("Pointing device controlling camera altitude"));
	devicesCbb = new JComboBox(getAvailablePointingDeviceNames());
	add(devicesCbb);
	ItemListener il1 = new ItemListener(){
		public void itemStateChanged(ItemEvent e){
		    if (e.getStateChange() == ItemEvent.SELECTED){
			plugin.setNonDominantHandDevice((String)(e.getItem()));
		    }
		}
	    };
	devicesCbb.addItemListener(il1);
	// init sensitivity
	add(new JLabel("Pointing device sensitivity"));
	sensitivitySld = new JSlider(5,20,10);
	sensitivitySld.setPaintTicks(true);
	sensitivitySld.setPaintTrack(true);
	sensitivitySld.setPaintLabels(true);
	sensitivitySld.setMajorTickSpacing(5);
	sensitivitySld.setMinorTickSpacing(1);
	sensitivitySld.setLabelTable(tickLabels);
	add(sensitivitySld);
	sensitivitySld.setValueIsAdjusting(true);
	sensitivitySld.setValue(Math.round(plugin.NON_DOMINANT_HAND_DEVICE_SENSITIVITY*10));
	sensitivitySld.addChangeListener(this);
	//window
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){
		    BiManSettings.this.setVisible(false);
		    BiManSettings.this.dispose();
		}
	    };
	this.addWindowListener(w0);
	this.setTitle("Bi-manual Interaction Plug-in Preferences");
	this.pack();
	this.setSize(400, 190);
	this.setLocation(((JFrame)plugin.application.grMngr.mainView.getFrame()).getLocation());
	this.setVisible(true);
    }
    
    Vector getAvailablePointingDeviceNames(){
	Vector res = new Vector();
	Socket[] sockets = plugin.mpdMngr.getAvailablePointingDevices();
	if (sockets != null){
	    for (int i = 0; i < sockets.length; i++) {
		res.add(sockets[i].getDeviceDescriptor().getName());
	    }
	}
	return res;
    }
    
    public void stateChanged(ChangeEvent e){
	plugin.NON_DOMINANT_HAND_DEVICE_SENSITIVITY = sensitivitySld.getValue()/10.0f;
    }

}
