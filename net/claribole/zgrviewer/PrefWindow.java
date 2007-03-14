/*   FILE: PrefWindow.java
 *   DATE OF CREATION:   Thu Jan 09 15:47:07 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *   $Id: PrefWindow.java,v 1.12 2005/10/21 09:08:53 epietrig Exp $
 */

package net.claribole.zgrviewer;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JScrollPane;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class PrefWindow extends JFrame implements ActionListener {

    ZGRViewer application;
    GraphicsManager grMngr;
    
    JTabbedPane tabbedPane;

    JButton okPrefs,savePrefs;

    //Misc prefs
    JCheckBox saveWindowLayoutCb;
    JCheckBox antialiascb, silentCb; //set antialias rendering, -q option
    JTextField cmdLOptsTf;
    JCheckBox sdZoomCb;
    JSlider sdZoomSlider;
    JSpinner mFactorSpinner;

    //directory panel
    JButton brw1,brw2,brw3,brw3b,brw3c,brw4,brw5;
    JTextField tf1,tf2,tf3,tf3b,tf3c,tf4,tf5;
    JCheckBox cb1;

    //web browser panel
    JRadioButton detectBrowserBt,specifyBrowserBt;
    JTextField browserPathTf,browserOptsTf;
    JButton brw6,webHelpBt;
    JLabel pathLb,optLb;

    //proxy/firewall
    JCheckBox useProxyCb;
    JLabel proxyHostLb,proxyPortLb;
    JTextField proxyHostTf,proxyPortTf;
    JButton proxyHelpBt;

    PrefWindow(ZGRViewer app, GraphicsManager gm){
	this.application=app;
	this.grMngr = gm;
	tabbedPane = new JTabbedPane();

	//misc panel
	JPanel miscPane=new JPanel();
	GridBagLayout gridBag0=new GridBagLayout();
	GridBagConstraints constraints0=new GridBagConstraints();
	constraints0.fill=GridBagConstraints.HORIZONTAL;
	constraints0.anchor=GridBagConstraints.WEST;
	miscPane.setLayout(gridBag0);

	//save window layout checkbox
	saveWindowLayoutCb=new JCheckBox("Save/Restore Window Layout at Startup",ConfigManager.SAVE_WINDOW_LAYOUT);
	buildConstraints(constraints0,0,0,2,1,100,10);
	gridBag0.setConstraints(saveWindowLayoutCb,constraints0);
	miscPane.add(saveWindowLayoutCb);
	//antialiasing
	antialiascb=new JCheckBox("Antialiasing",ConfigManager.ANTIALIASING);
	antialiascb.addActionListener(this);
	buildConstraints(constraints0,0,1,2,1,100,10);
	gridBag0.setConstraints(antialiascb,constraints0);
	miscPane.add(antialiascb);
	// -q option
	silentCb = new JCheckBox("GraphViz programs should not issue warnings (v1.10 and above)", ConfigManager.FORCE_SILENT);
	silentCb.addActionListener(this);
	buildConstraints(constraints0, 0, 2, 2, 1, 100, 10);
	gridBag0.setConstraints(silentCb,constraints0);
	miscPane.add(silentCb);
	//command line options
	JLabel cmdLOptsLb=new JLabel("dot/neato command line options (-T will be ignored)");
	buildConstraints(constraints0,0,3,2,1,100,10);
	gridBag0.setConstraints(cmdLOptsLb,constraints0);
	miscPane.add(cmdLOptsLb);
	cmdLOptsTf=new JTextField(ConfigManager.CMD_LINE_OPTS);
	buildConstraints(constraints0,0,4,2,1,100,10);
	gridBag0.setConstraints(cmdLOptsTf,constraints0);
	miscPane.add(cmdLOptsTf);
	sdZoomCb = new JCheckBox("Enable speed-dependent automatic zooming");
	buildConstraints(constraints0,0,5,2,1,100,10);
	gridBag0.setConstraints(sdZoomCb,constraints0);
	miscPane.add(sdZoomCb);
	sdZoomCb.setSelected(application.cfgMngr.isSDZoomEnabled());
	ActionListener a31 = new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    boolean b = PrefWindow.this.sdZoomCb.isSelected();
		    PrefWindow.this.application.cfgMngr.setSDZoomEnabled(b);
		    PrefWindow.this.sdZoomSlider.setEnabled(b);
		}
	    };
	sdZoomCb.addActionListener(a31);
	sdZoomSlider = new JSlider(2, 10, (int)application.cfgMngr.getSDZoomFactor());
	sdZoomSlider.setLabelTable(sdZoomSlider.createStandardLabels(1));
	sdZoomSlider.setPaintLabels(true);
	sdZoomSlider.setPaintTicks(true);
	sdZoomSlider.setSnapToTicks(true);
	sdZoomSlider.setPaintTrack(true);
	sdZoomSlider.setEnabled(application.cfgMngr.isSDZoomEnabled());
	buildConstraints(constraints0,0,6,2,1,100,10);
	gridBag0.setConstraints(sdZoomSlider,constraints0);
	miscPane.add(sdZoomSlider);
	ChangeListener cl0 = new ChangeListener(){
		public void stateChanged(ChangeEvent e){
		    PrefWindow.this.application.cfgMngr.setSDZoomFactor((double)PrefWindow.this.sdZoomSlider.getValue());
		}
	    };
	sdZoomSlider.addChangeListener(cl0);
	JLabel mFactorLabel = new JLabel("Magnification factor when focusing on a node");
	buildConstraints(constraints0, 0, 7, 1, 1, 60, 10);
	gridBag0.setConstraints(mFactorLabel, constraints0);
	miscPane.add(mFactorLabel);
	mFactorSpinner = new JSpinner(new SpinnerNumberModel((float)ConfigManager.MAG_FACTOR, 0.1, 10, 0.1));
	buildConstraints(constraints0, 1, 7, 1, 1, 40, 0);
	gridBag0.setConstraints(mFactorSpinner, constraints0);
	miscPane.add(mFactorSpinner);

	//blank panel to fill remaining part of the tab
	JPanel p1=new JPanel();
	buildConstraints(constraints0,0,7,1,1,100,50);
	gridBag0.setConstraints(p1,constraints0);
	miscPane.add(p1);
	//add tab to panel
	tabbedPane.addTab("Misc.",miscPane);

	//directories panel
	FocusListener fl0=new FocusListener(){
		public void focusGained(FocusEvent e){}
		public void focusLost(FocusEvent e){
		    Object src = e.getSource();
		    if (src == tf1){
			File fl = new File(tf1.getText().trim());
			if (fl.exists()){
			    if (fl.isDirectory()){
				ConfigManager.m_TmpDir = fl;
			    }
			    else {
				javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.notADirectory + tf1.getText());
			    }
			}
			else {
			    javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.fileDoesNotExist + tf1.getText());
			}
		    }
		    else if (src == tf2){
			File fl = new File(tf2.getText().trim());
			if (fl.exists()){
			    if (fl.isDirectory()){
				ConfigManager.m_PrjDir = fl;
			    }
			    else {
				javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.notADirectory + tf2.getText());
			    }
			}
			else {
			    javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.fileDoesNotExist + tf2.getText());
			}
		    }
		    else if (src == tf3){
			File fl = new File(tf3.getText().trim());
			if (fl.exists()){
			    if (fl.isFile()){
				ConfigManager.m_NeatoPath = fl;
			    }
			    else {
				javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.notADirectory + tf3.getText());
			    }
			}
			else {
			    javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.fileDoesNotExist + tf3.getText());
			}
		    }
		    else if (src == tf3b){
			File fl = new File(tf3b.getText().trim());
			if (fl.exists()){
			    if (fl.isFile()){
				ConfigManager.m_CircoPath = fl;
			    }
			    else {
				javax.swing.JOptionPane.showMessageDialog(PrefWindow.this, Messages.notADirectory + tf3b.getText());
			    }
			}
			else {
			    javax.swing.JOptionPane.showMessageDialog(PrefWindow.this, Messages.fileDoesNotExist + tf3b.getText());
			}
		    }
		    else if (src == tf3c){
			File fl = new File(tf3c.getText().trim());
			if (fl.exists()){
			    if (fl.isFile()){
				ConfigManager.m_TwopiPath = fl;
			    }
			    else {
				javax.swing.JOptionPane.showMessageDialog(PrefWindow.this, Messages.notADirectory + tf3c.getText());
			    }
			}
			else {
			    javax.swing.JOptionPane.showMessageDialog(PrefWindow.this, Messages.fileDoesNotExist + tf3c.getText());
			}
		    }
		    else if (src == tf4){
			File fl = new File(tf4.getText().trim());
			if (fl.exists()){
			    if (fl.isFile()){
				ConfigManager.m_DotPath = fl;
			    }
			    else {
				javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.notAFile + tf4.getText());
			    }
			}
			else {
			    javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.fileDoesNotExist + tf4.getText());
			}
		    }
		    else if (src == tf5){
			File fl = new File(tf5.getText().trim());
			if (fl.exists()){
			    if (fl.isDirectory()){
				ConfigManager.m_GraphVizFontDir = fl;
			    }
			    else {
				javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.notADirectory + tf5.getText());
			    }
			}
			else {
			    javax.swing.JOptionPane.showMessageDialog(PrefWindow.this,Messages.fileDoesNotExist + tf5.getText());
			}
		    }
		}
	    };
	JPanel dirPane=new JPanel();
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	constraints.fill=GridBagConstraints.HORIZONTAL;
	constraints.anchor=GridBagConstraints.WEST;
	dirPane.setLayout(gridBag);
	JLabel l1=new JLabel("Temporary directory");
	buildConstraints(constraints,0,0,1,1,60,10);
	gridBag.setConstraints(l1,constraints);
	dirPane.add(l1);
	cb1=new JCheckBox("Delete temp files on exit");
	buildConstraints(constraints,1,0,1,1,30,0);
	gridBag.setConstraints(cb1,constraints);
	if (ConfigManager.DELETE_TEMP_FILES){cb1.setSelected(true);} else {cb1.setSelected(false);}
	cb1.addActionListener(this);
	dirPane.add(cb1);
	brw1=new JButton("Browse...");
	buildConstraints(constraints,2,0,1,1,10,0);
	gridBag.setConstraints(brw1,constraints);
	brw1.addActionListener(this);
	dirPane.add(brw1);
	tf1=new JTextField(ConfigManager.m_TmpDir.toString());
	buildConstraints(constraints,0,1,3,1,100,10);
	gridBag.setConstraints(tf1,constraints);
	dirPane.add(tf1);
	tf1.addFocusListener(fl0);
	JLabel l2=new JLabel("DOT files directory");
	buildConstraints(constraints,0,2,2,1,90,10);
	gridBag.setConstraints(l2,constraints);
	dirPane.add(l2);
	brw2=new JButton("Browse...");
	buildConstraints(constraints,2,2,1,1,10,0);
	gridBag.setConstraints(brw2,constraints);
	brw2.addActionListener(this);
	dirPane.add(brw2);
	tf2=new JTextField(ConfigManager.m_PrjDir.toString());
	buildConstraints(constraints,0,3,3,1,100,10);
	gridBag.setConstraints(tf2,constraints);
	dirPane.add(tf2);
	tf2.addFocusListener(fl0);
	JLabel l4=new JLabel("GraphViz/dot executable");
	buildConstraints(constraints,0,4,2,1,90,10);
	gridBag.setConstraints(l4,constraints);
	dirPane.add(l4);
	brw4=new JButton("Browse...");
	buildConstraints(constraints,2,4,1,1,10,0);
	gridBag.setConstraints(brw4,constraints);
	brw4.addActionListener(this);
	dirPane.add(brw4);
	tf4=new JTextField(ConfigManager.m_DotPath.toString());
	buildConstraints(constraints,0,5,3,1,100,10);
	gridBag.setConstraints(tf4,constraints);
	dirPane.add(tf4);
	tf4.addFocusListener(fl0);
	JLabel l3=new JLabel("GraphViz/neato executable");
	buildConstraints(constraints,0,6,2,1,90,10);
	gridBag.setConstraints(l3,constraints);
	dirPane.add(l3);
	brw3=new JButton("Browse...");
	buildConstraints(constraints,2,6,1,1,10,0);
	gridBag.setConstraints(brw3,constraints);
	brw3.addActionListener(this);
	dirPane.add(brw3);
	tf3=new JTextField(ConfigManager.m_NeatoPath.toString());
	buildConstraints(constraints,0,7,3,1,100,10);
	gridBag.setConstraints(tf3,constraints);
	dirPane.add(tf3);
	tf3.addFocusListener(fl0);
	JLabel l3b=new JLabel("GraphViz/circo executable");
	buildConstraints(constraints,0,8,2,1,90,10);
	gridBag.setConstraints(l3b,constraints);
	dirPane.add(l3b);
	brw3b=new JButton("Browse...");
	buildConstraints(constraints,2,8,1,1,10,0);
	gridBag.setConstraints(brw3b,constraints);
	brw3b.addActionListener(this);
	dirPane.add(brw3b);
	tf3b=new JTextField(ConfigManager.m_CircoPath.toString());
	buildConstraints(constraints,0,9,3,1,100,10);
	gridBag.setConstraints(tf3b,constraints);
	dirPane.add(tf3b);
	tf3b.addFocusListener(fl0);
	JLabel l3c=new JLabel("GraphViz/twopi executable");
	buildConstraints(constraints,0,10,2,1,90,10);
	gridBag.setConstraints(l3c,constraints);
	dirPane.add(l3c);
	brw3c=new JButton("Browse...");
	buildConstraints(constraints,2,10,1,1,10,0);
	gridBag.setConstraints(brw3c,constraints);
	brw3c.addActionListener(this);
	dirPane.add(brw3c);
	tf3c=new JTextField(ConfigManager.m_TwopiPath.toString());
	buildConstraints(constraints,0,11,3,1,100,10);
	gridBag.setConstraints(tf3c,constraints);
	dirPane.add(tf3c);
	tf3c.addFocusListener(fl0);
	JLabel l5=new JLabel("GraphViz font directory (optional)");
	buildConstraints(constraints,0,12,2,1,90,10);
	gridBag.setConstraints(l5,constraints);
	dirPane.add(l5);
	brw5=new JButton("Browse...");
	buildConstraints(constraints,2,12,1,1,10,0);
	gridBag.setConstraints(brw5,constraints);
	brw5.addActionListener(this);
	dirPane.add(brw5);
	tf5=new JTextField(ConfigManager.m_GraphVizFontDir.toString());
	buildConstraints(constraints,0,13,3,1,100,10);
	gridBag.setConstraints(tf5,constraints);
	dirPane.add(tf5);
	tf5.addFocusListener(fl0);
	JScrollPane dirSP = new JScrollPane(dirPane);
	dirSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	dirSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	tabbedPane.addTab("Directories", dirSP);
	//web browser panel
	JPanel webPane=new JPanel();
	GridBagLayout gridBag2=new GridBagLayout();
	GridBagConstraints constraints2=new GridBagConstraints();
	constraints2.fill=GridBagConstraints.HORIZONTAL;
	constraints2.anchor=GridBagConstraints.WEST;
	webPane.setLayout(gridBag2);
	ButtonGroup bg2=new ButtonGroup();
	detectBrowserBt=new JRadioButton("Automatically Detect Default Browser");
	buildConstraints(constraints2,0,0,3,1,100,1);
	gridBag2.setConstraints(detectBrowserBt,constraints2);
	detectBrowserBt.addActionListener(this);
	bg2.add(detectBrowserBt);
	webPane.add(detectBrowserBt);
	specifyBrowserBt=new JRadioButton("Specify Browser:");
	buildConstraints(constraints2,0,1,3,1,100,1);
	gridBag2.setConstraints(specifyBrowserBt,constraints2);
	specifyBrowserBt.addActionListener(this);
	bg2.add(specifyBrowserBt);
	webPane.add(specifyBrowserBt);
	JPanel p7=new JPanel();
	buildConstraints(constraints2,0,2,1,1,10,1);
	gridBag2.setConstraints(p7,constraints2);
	webPane.add(p7);
	pathLb=new JLabel("Path");
	buildConstraints(constraints2,1,2,1,1,80,0);
	gridBag2.setConstraints(pathLb,constraints2);
	webPane.add(pathLb);
	brw6=new JButton("Browse...");
	buildConstraints(constraints2,2,2,1,1,10,0);
	gridBag2.setConstraints(brw6,constraints2);
	brw6.addActionListener(this);
	webPane.add(brw6);
	browserPathTf=new JTextField(ConfigManager.browserPath.toString());
	buildConstraints(constraints2,1,3,2,1,90,1);
	gridBag2.setConstraints(browserPathTf,constraints2);
	webPane.add(browserPathTf);
	optLb=new JLabel("Command Line Options");
	buildConstraints(constraints2,1,4,2,1,90,1);
	gridBag2.setConstraints(optLb,constraints2);
	webPane.add(optLb);
	browserOptsTf=new JTextField(ConfigManager.browserOptions);
	buildConstraints(constraints2,1,5,2,1,90,1);
	gridBag2.setConstraints(browserOptsTf,constraints2);
	webPane.add(browserOptsTf);
	//fill out empty space
	JPanel p8=new JPanel();
	buildConstraints(constraints2,0,6,3,1,100,92);
	gridBag2.setConstraints(p8,constraints2);
	webPane.add(p8);
	webHelpBt=new JButton("Help");
	buildConstraints(constraints2,2,7,1,1,10,1);
	gridBag2.setConstraints(webHelpBt,constraints2);
	webHelpBt.addActionListener(this);
	webPane.add(webHelpBt);
	if (ConfigManager.autoDetectBrowser){detectBrowserBt.doClick();} //select and fire event
	else {specifyBrowserBt.doClick();} //so that fields get enabled/disabled as is approriate
	tabbedPane.addTab("Web Browser",webPane);

	//proxy panel
	JPanel proxyPane=new JPanel();
	GridBagLayout gridBag5=new GridBagLayout();
	GridBagConstraints constraints5=new GridBagConstraints();
	constraints5.fill=GridBagConstraints.HORIZONTAL;
	constraints5.anchor=GridBagConstraints.WEST;
	proxyPane.setLayout(gridBag5);
	useProxyCb=new JCheckBox("Use Proxy Server");
	buildConstraints(constraints5,0,0,2,1,100,1);
	gridBag5.setConstraints(useProxyCb,constraints5);
	useProxyCb.setSelected(ConfigManager.useProxy);
	useProxyCb.addActionListener(this);
	proxyPane.add(useProxyCb);
	proxyHostLb=new JLabel("Hostname:");
	proxyHostLb.setEnabled(ConfigManager.useProxy);
	buildConstraints(constraints5,0,1,1,1,80,1);
	gridBag5.setConstraints(proxyHostLb,constraints5);
	proxyPane.add(proxyHostLb);
	proxyPortLb=new JLabel("Port:");
	proxyPortLb.setEnabled(ConfigManager.useProxy);
	buildConstraints(constraints5,1,1,1,1,20,1);
	gridBag5.setConstraints(proxyPortLb,constraints5);
	proxyPane.add(proxyPortLb);
	proxyHostTf=new JTextField(ConfigManager.proxyHost);
	proxyHostTf.setEnabled(ConfigManager.useProxy);
	buildConstraints(constraints5,0,2,1,1,80,1);
	gridBag5.setConstraints(proxyHostTf,constraints5);
	proxyPane.add(proxyHostTf);
	proxyPortTf=new JTextField(ConfigManager.proxyPort);
	proxyPortTf.setEnabled(ConfigManager.useProxy);
	buildConstraints(constraints5,1,2,1,1,20,1);
	gridBag5.setConstraints(proxyPortTf,constraints5);
	proxyPane.add(proxyPortTf);
	constraints5.fill=GridBagConstraints.BOTH;
	constraints5.anchor=GridBagConstraints.CENTER;
	//fill out empty space
	JPanel p1000=new JPanel();
	buildConstraints(constraints5,0,5,2,1,100,90);
	gridBag5.setConstraints(p1000,constraints5);
	proxyPane.add(p1000);
	constraints5.fill=GridBagConstraints.NONE;
	constraints5.anchor=GridBagConstraints.EAST;
	proxyHelpBt=new JButton("Help");
	buildConstraints(constraints5,1,6,1,1,20,1);
	gridBag5.setConstraints(proxyHelpBt,constraints5);
	proxyHelpBt.addActionListener(this);
	proxyPane.add(proxyHelpBt);
	tabbedPane.addTab("Proxy",proxyPane);

	//plugin panel
	tabbedPane.addTab("Plugins", initPluginPane());

	//main panel (tabbed panes + OK/Save buttons)
	Container cpane=this.getContentPane();
	GridBagLayout gridBag3=new GridBagLayout();
	GridBagConstraints constraints3=new GridBagConstraints();
	constraints3.fill=GridBagConstraints.BOTH;
	constraints3.anchor=GridBagConstraints.WEST;
	cpane.setLayout(gridBag3);
	buildConstraints(constraints3,0,0,3,1,100,90);
	gridBag3.setConstraints(tabbedPane,constraints3);
	cpane.add(tabbedPane);
	JPanel tmp=new JPanel();
	buildConstraints(constraints3,0,1,1,1,70,10);
	gridBag3.setConstraints(tmp,constraints3);
	cpane.add(tmp);
	constraints3.fill=GridBagConstraints.HORIZONTAL;
	constraints3.anchor=GridBagConstraints.CENTER;
	okPrefs=new JButton("Apply & Close");
	//okPrefs.setPreferredSize(new Dimension(60,25));
	buildConstraints(constraints3,1,1,1,1,15,10);
	gridBag3.setConstraints(okPrefs,constraints3);
	okPrefs.addActionListener(this);
	cpane.add(okPrefs);
	constraints3.fill=GridBagConstraints.HORIZONTAL;
	constraints3.anchor=GridBagConstraints.CENTER;
	savePrefs=new JButton("Save");
	//savePrefs.setPreferredSize(new Dimension(60,35));
	buildConstraints(constraints3,2,1,1,1,15,10);
	gridBag3.setConstraints(savePrefs,constraints3);
	savePrefs.addActionListener(this);
	cpane.add(savePrefs);

	tabbedPane.setSelectedIndex(0);
	//window
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){}
	    };
	this.addWindowListener(w0);
	this.setTitle("Preferences");
	this.pack();
	this.setSize(400,300);
    }

    private JComponent initPluginPane(){
	JPanel pluginPane = new JPanel();
	int nbPlugins = application.cfgMngr.plugins.length;

	GridBagLayout gridBag = new GridBagLayout();
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.fill=GridBagConstraints.NONE;
	pluginPane.setLayout(gridBag);
	JButton b65;
	JLabel l65;
	ActionListener a65;
	MouseListener m65;
	int row = 0;
	for (int i=0;i<nbPlugins;i++){
	    row = i * 2;
	    constraints.anchor = GridBagConstraints.WEST;
	    final String url = (application.cfgMngr.plugins[i].getURL() != null) ? application.cfgMngr.plugins[i].getURL().toString() : "";
	    l65 = new JLabel("<html><a href=\""+url+"\">"+application.cfgMngr.plugins[i].getName()+"</a></html>");
	    buildConstraints(constraints, 0, row, 1, 1, 70, 5);
	    gridBag.setConstraints(l65, constraints);
	    pluginPane.add(l65);
	    m65 = new MouseListener(){
		    public void mouseClicked(MouseEvent e){
			if (url != null && url.length() > 0){
			    application.displayURLinBrowser(url);
			}
		    }
		    public void mouseEntered(MouseEvent e){}
		    public void mouseExited(MouseEvent e){}
		    public void mousePressed(MouseEvent e){}
		    public void mouseReleased(MouseEvent e){}
		};
	    l65.addMouseListener(m65);
	    constraints.anchor = GridBagConstraints.EAST;
	    b65 = new JButton("Settings...");
	    buildConstraints(constraints, 1, row, 1, 1, 30, 0);
	    gridBag.setConstraints(b65, constraints);
	    pluginPane.add(b65);
	    final Plugin pg65 = application.cfgMngr.plugins[i];
	    a65 = new ActionListener(){
		    public void actionPerformed(ActionEvent e){
			pg65.showSettings();
		    }
		};
	    b65.addActionListener(a65);
	    constraints.anchor = GridBagConstraints.WEST;
	    l65 = new JLabel("Author: "+application.cfgMngr.plugins[i].getAuthor());
	    buildConstraints(constraints, 0, row + 1, 1, 1, 0, 5);
	    gridBag.setConstraints(l65, constraints);
	    pluginPane.add(l65);
	    constraints.anchor = GridBagConstraints.EAST;
	    l65 = new JLabel("Version: "+application.cfgMngr.plugins[i].getVersion());
	    buildConstraints(constraints, 0, row + 1, 1, 1, 0, 0);
	    gridBag.setConstraints(l65, constraints);
	    pluginPane.add(l65);
	}
	JPanel p47 = new JPanel();
	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.WEST;
	buildConstraints(constraints, 0, row + 2, 2, 1, 100, 90);
	gridBag.setConstraints(p47, constraints);
	pluginPane.add(p47);
	return new JScrollPane(pluginPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    public void actionPerformed(ActionEvent e){
	JFileChooser fc;
	int returnVal;
	Object o=e.getSource();
	if (o==brw1){//tmp directory browse button
	    fc=new JFileChooser(ConfigManager.m_TmpDir);
 	    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //does not work well with JVM 1.3.x (works fine in 1.4)
	    returnVal= fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION){
		ConfigManager.m_TmpDir=fc.getSelectedFile();
		tf1.setText(ConfigManager.m_TmpDir.toString());
	    }
	}
	else if (o==brw2){
	    fc=new JFileChooser(ConfigManager.m_PrjDir);
 	    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //does not work well with JVM 1.3.x (works fine in 1.4)
	    returnVal= fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION){
		ConfigManager.m_PrjDir=fc.getSelectedFile();
		tf2.setText(ConfigManager.m_PrjDir.toString());
	    }
	}
	else if (o==brw4){
	    fc=new JFileChooser(ConfigManager.m_DotPath);
	    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    returnVal= fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION){
		ConfigManager.m_DotPath=fc.getSelectedFile();
		tf4.setText(ConfigManager.m_DotPath.toString());
	    }
	}
	else if (o==brw3){
	    fc=new JFileChooser(ConfigManager.m_NeatoPath);
	    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    returnVal= fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION){
		ConfigManager.m_NeatoPath=fc.getSelectedFile();
		tf3.setText(ConfigManager.m_NeatoPath.toString());
	    }
	}
	else if (o == brw3b){
	    fc = new JFileChooser(ConfigManager.m_CircoPath);
	    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    returnVal = fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION){
		ConfigManager.m_CircoPath = fc.getSelectedFile();
		tf3b.setText(ConfigManager.m_CircoPath.toString());
	    }
	}
	else if (o==brw3c){
	    fc = new JFileChooser(ConfigManager.m_TwopiPath);
	    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    returnVal = fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION){
		ConfigManager.m_TwopiPath = fc.getSelectedFile();
		tf3c.setText(ConfigManager.m_TwopiPath.toString());
	    }
	}
	else if (o==brw5){
	    fc=new JFileChooser(ConfigManager.m_GraphVizFontDir);
 	    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //does not work well with JVM 1.3.x (works fine in 1.4)
	    returnVal= fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION){
		ConfigManager.m_GraphVizFontDir=fc.getSelectedFile();
		tf5.setText(ConfigManager.m_GraphVizFontDir.toString());
	    }
	}
	else if (o==cb1){
	    if (cb1.isSelected()){ConfigManager.DELETE_TEMP_FILES=true;}
	    else {ConfigManager.DELETE_TEMP_FILES=false;}
	}
	else if (o==detectBrowserBt){
	    if (detectBrowserBt.isSelected()){//automatically detect browser
		ConfigManager.autoDetectBrowser=true;
		browserPathTf.setEnabled(false);
		brw6.setEnabled(false);
		browserOptsTf.setEnabled(false);
		pathLb.setEnabled(false);
		optLb.setEnabled(false);
	    }
	}
	else if (o==specifyBrowserBt){
	    if (specifyBrowserBt.isSelected()){//specify browser
		ConfigManager.autoDetectBrowser=false;
		browserPathTf.setEnabled(true);
		brw6.setEnabled(true);
		browserOptsTf.setEnabled(true);
		pathLb.setEnabled(true);
		optLb.setEnabled(true);
	    }
	}
	else if (o==brw6){
	    fc=new JFileChooser(ConfigManager.browserPath);
	    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	    returnVal= fc.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION){
		ConfigManager.browserPath=fc.getSelectedFile();
		browserPathTf.setText(ConfigManager.browserPath.toString());
	    }
	}
	else if (o==webHelpBt){
	    Dimension screenSize=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	    TextViewer help=new TextViewer(new StringBuffer(Messages.webBrowserHelpText),"Web Browser Configuration",0,(screenSize.width-400)/2,(screenSize.height-300)/2,400,300,false);
	}
	else if (o==useProxyCb){
	    proxyHostLb.setEnabled(useProxyCb.isSelected());
	    proxyPortLb.setEnabled(useProxyCb.isSelected());
	    proxyHostTf.setEnabled(useProxyCb.isSelected());
	    proxyPortTf.setEnabled(useProxyCb.isSelected());
	}
	else if (o==proxyHelpBt){
	    Dimension screenSize=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	    TextViewer help=new TextViewer(new StringBuffer(Messages.proxyHelpText),"Proxy Configuration",0,(screenSize.width-400)/2,(screenSize.height-300)/2,400,300,false);
	}
	else if (o==okPrefs){updateVars();this.dispose();}
	else if (o==savePrefs){updateVars();application.saveConfiguration();}
	else if (o==antialiascb){
	    if (antialiascb.isSelected()){javax.swing.JOptionPane.showMessageDialog(this,Messages.antialiasingWarning);}
	    grMngr.setAntialiasing(antialiascb.isSelected());
	}
    }

    void updateVars(){
	ConfigManager.SAVE_WINDOW_LAYOUT=saveWindowLayoutCb.isSelected();
	ConfigManager.FORCE_SILENT = silentCb.isSelected();
	ConfigManager.MAG_FACTOR = ((Double)mFactorSpinner.getValue()).floatValue();
	ConfigManager.CMD_LINE_OPTS=cmdLOptsTf.getText();
	ConfigManager.browserPath=new File(browserPathTf.getText());
	ConfigManager.browserOptions=browserOptsTf.getText();
	ConfigManager.updateProxy(useProxyCb.isSelected(),proxyHostTf.getText(),proxyPortTf.getText());
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
