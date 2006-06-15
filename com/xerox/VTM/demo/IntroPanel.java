/*   FILE: IntroPanel.java
 *   DATE OF CREATION:   Dec 07 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 *
 * $Id: IntroPanel.java,v 1.7 2006/04/05 06:21:57 epietrig Exp $
 */

package com.xerox.VTM.demo;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.claribole.zvtm.lens.FSInverseCosineLens;
import net.claribole.zvtm.lens.FSLinearLens;
import net.claribole.zvtm.lens.Lens;

import com.xerox.VTM.engine.AnimManager;

public class IntroPanel  extends JFrame {

    static int PANEL_WIDTH = 300;
    static int INIT_PANEL_HEIGHT = 200;
    static int PANEL_HEIGHT = 300;

    Introduction application;

    Container cpane;

    static String NO_LENS = "No lens";
    static String LINEAR_LENS = "Linear lens";
    static String INVERSE_COSINE_LENS = "Inverse Cosine Lens";

    Lens lens;

    IntroPanel(Introduction app){
	application=app;
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	cpane=this.getContentPane();
	cpane.setLayout(gridBag);

	JPanel pn1=new JPanel();
	pn1.setLayout(new FlowLayout());
	JLabel l=new JLabel();
	l.setText(msgIntro);
	pn1.add(l);

	JPanel pn2=new JPanel();
	pn2.setLayout(new FlowLayout());
	JButton okB=new JButton("OK");
	ActionListener a0=new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    demo0();
		}
	    };
	okB.addActionListener(a0);
	pn2.add(okB);

	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor=GridBagConstraints.CENTER;
	buildConstraints(constraints,0,0,1,1,100,80);
	gridBag.setConstraints(pn1,constraints);
	cpane.add(pn1);
	buildConstraints(constraints,0,1,1,1,100,0);
	gridBag.setConstraints(pn2,constraints);
	cpane.add(pn2);

	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){System.exit(0);}
	    };
	this.addWindowListener(w0);
	this.setLocation(0,0);
	this.setSize(PANEL_WIDTH, INIT_PANEL_HEIGHT);
	this.setTitle("Introduction");
	this.setVisible(true);
    }

    void demo0(){
	cpane.removeAll();
	this.setSize(PANEL_WIDTH, PANEL_HEIGHT);
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	cpane.setLayout(gridBag);
	JTextArea ta=new JTextArea(msg0);
	ta.setLineWrap(true);
	ta.setWrapStyleWord(true);
	ta.setEditable(false);
	JScrollPane sp = new JScrollPane(ta);
	sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	sp.setPreferredSize(new Dimension(300,200));
	JPanel pn1 = initLensPanel();
	JPanel pn2=new JPanel();
	pn2.setLayout(new GridLayout(1,2));
	final JButton prev=new JButton(new ImageIcon(this.getClass().getResource("/images/Back16b.gif")));   
	final JButton next=new JButton(new ImageIcon(this.getClass().getResource("/images/Forward16b.gif")));
	pn2.add(prev);
	pn2.add(next);
	ActionListener a0=new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    JButton b=(JButton)e.getSource();
		    if (b==prev){demo4();}
		    else if (b==next){demo1();}
		}
	    };
	prev.addActionListener(a0);
	next.addActionListener(a0);
	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor=GridBagConstraints.CENTER;
	buildConstraints(constraints,0,0,1,1,100,98);
	gridBag.setConstraints(sp,constraints);
	cpane.add(sp);
	buildConstraints(constraints,0,1,1,1,100,1);
	gridBag.setConstraints(pn1,constraints);
	cpane.add(pn1);
	buildConstraints(constraints,0,2,1,1,100,1);
	gridBag.setConstraints(pn2,constraints);
	cpane.add(pn2);
	this.validate();
	application.cameraDemo();
    }

    void demo1(){
	cpane.removeAll();
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	cpane.setLayout(gridBag);
	JTextArea ta=new JTextArea(msg1);
	ta.setLineWrap(true);
	ta.setWrapStyleWord(true);
	ta.setEditable(false);
	JScrollPane sp = new JScrollPane(ta);
	sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	sp.setPreferredSize(new Dimension(300,200));
	JPanel pn1 = initLensPanel();
	JPanel pn2=new JPanel();
	pn2.setLayout(new GridLayout(1,2));
	final JButton prev=new JButton(new ImageIcon(this.getClass().getResource("/images/Back16b.gif")));   
	final JButton next=new JButton(new ImageIcon(this.getClass().getResource("/images/Forward16b.gif")));
	pn2.add(prev);
	pn2.add(next);
	ActionListener a0=new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    JButton b=(JButton)e.getSource();
		    if (b==prev){demo0();}
		    else if (b==next){demo2();}
		}
	    };
	prev.addActionListener(a0);
	next.addActionListener(a0);
	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor=GridBagConstraints.CENTER;
	buildConstraints(constraints,0,0,1,1,100,98);
	gridBag.setConstraints(sp,constraints);
	cpane.add(sp);
	buildConstraints(constraints,0,1,1,1,100,1);
	gridBag.setConstraints(pn1,constraints);
	cpane.add(pn1);
	buildConstraints(constraints,0,2,1,1,100,1);
	gridBag.setConstraints(pn2,constraints);
	cpane.add(pn2);
	this.validate();
	application.objectFamilies();
    }

    void demo2(){
	cpane.removeAll();
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	cpane.setLayout(gridBag);
	JTextArea ta=new JTextArea(msg2);
	ta.setLineWrap(true);
	ta.setWrapStyleWord(true);
	ta.setEditable(false);
	JScrollPane sp = new JScrollPane(ta);
	sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	sp.setPreferredSize(new Dimension(300,200));
	JPanel pn1 = initLensPanel();
	JPanel pn2=new JPanel();
	pn2.setLayout(new GridLayout(1,2));
	final JButton prev=new JButton(new ImageIcon(this.getClass().getResource("/images/Back16b.gif")));   
	final JButton next=new JButton(new ImageIcon(this.getClass().getResource("/images/Forward16b.gif")));
	pn2.add(prev);
	pn2.add(next);
	ActionListener a0=new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    JButton b=(JButton)e.getSource();
		    if (b==prev){demo1();}
		    else if (b==next){demo3();}
		}
	    };
	prev.addActionListener(a0);
	next.addActionListener(a0);
	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor=GridBagConstraints.CENTER;
	buildConstraints(constraints,0,0,1,1,100,98);
	gridBag.setConstraints(sp,constraints);
	cpane.add(sp);
	buildConstraints(constraints,0,1,1,1,100,1);
	gridBag.setConstraints(pn1,constraints);
	cpane.add(pn1);
	buildConstraints(constraints,0,2,1,1,100,1);
	gridBag.setConstraints(pn2,constraints);
	cpane.add(pn2);
	this.validate();
	application.objectAnim();
    }

    void demo3(){
	cpane.removeAll();
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	cpane.setLayout(gridBag);
	JTextArea ta=new JTextArea(msg3);
	ta.setLineWrap(true);
	ta.setWrapStyleWord(true);
	ta.setEditable(false);
	JScrollPane sp = new JScrollPane(ta);
	sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	sp.setPreferredSize(new Dimension(300,200));
	JPanel pn1 = initLensPanel();
	JPanel pn2=new JPanel();
	pn2.setLayout(new GridLayout(1,2));
	final JButton prev=new JButton(new ImageIcon(this.getClass().getResource("/images/Back16b.gif")));   
	final JButton next=new JButton(new ImageIcon(this.getClass().getResource("/images/Forward16b.gif")));
	pn2.add(prev);
	pn2.add(next);
	ActionListener a0=new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    JButton b=(JButton)e.getSource();
		    if (b==prev){demo2();}
		    else if (b==next){demo4();}
		}
	    };
	prev.addActionListener(a0);
	next.addActionListener(a0);
	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor=GridBagConstraints.CENTER;
	buildConstraints(constraints,0,0,1,1,100,98);
	gridBag.setConstraints(sp,constraints);
	cpane.add(sp);
	buildConstraints(constraints,0,1,1,1,100,1);
	gridBag.setConstraints(pn1,constraints);
	cpane.add(pn1);
	buildConstraints(constraints,0,2,1,1,100,1);
	gridBag.setConstraints(pn2,constraints);
	cpane.add(pn2);
	this.validate();
	application.multiLayer();
    }

    void demo4(){
	cpane.removeAll();
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	cpane.setLayout(gridBag);
	JTextArea ta=new JTextArea(msg4);
	ta.setLineWrap(true);
	ta.setWrapStyleWord(true);
	ta.setEditable(false);
	JScrollPane sp = new JScrollPane(ta);
	sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	sp.setPreferredSize(new Dimension(300,200));
	JPanel pn1 = initLensPanel();
	JPanel pn2=new JPanel();
	pn2.setLayout(new GridLayout(1,2));
	final JButton prev=new JButton(new ImageIcon(this.getClass().getResource("/images/Back16b.gif")));   
	final JButton next=new JButton(new ImageIcon(this.getClass().getResource("/images/Forward16b.gif")));
	pn2.add(prev);
	pn2.add(next);
	ActionListener a0=new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    JButton b=(JButton)e.getSource();
		    if (b==prev){demo3();}
		    else if (b==next){demo0();}
		}
	    };
	prev.addActionListener(a0);
	next.addActionListener(a0);
	constraints.fill=GridBagConstraints.BOTH;
	constraints.anchor=GridBagConstraints.CENTER;
	buildConstraints(constraints,0,0,1,1,100,98);
	gridBag.setConstraints(sp,constraints);
	cpane.add(sp);
	buildConstraints(constraints,0,1,1,1,100,1);
	gridBag.setConstraints(pn1,constraints);
	cpane.add(pn1);
	buildConstraints(constraints,0,2,1,1,100,1);
	gridBag.setConstraints(pn2,constraints);
	cpane.add(pn2);
	this.validate();
	application.multiView();
    }

    JPanel initLensPanel(){
	JPanel pn1 = new JPanel();
	pn1.setLayout(new GridLayout(2,1));
	JLabel lb1 = new JLabel("View distortion");
	pn1.add(lb1);
	Vector lenses = new Vector();
	lenses.addElement(NO_LENS);
	lenses.addElement(LINEAR_LENS);
	lenses.addElement(INVERSE_COSINE_LENS);
	final JComboBox lenscbb = new JComboBox(lenses);
	ItemListener il1 = new ItemListener(){
		public void itemStateChanged(ItemEvent e){
		    if (e.getStateChange() == ItemEvent.SELECTED){
			setLens((String)e.getItem());
		    }
		}
	    };
	pn1.add(lenscbb);
	if (lens != null){
	    if (lens instanceof FSLinearLens){
		lenscbb.setSelectedItem(LINEAR_LENS);
	    }
	    else if (lens instanceof FSInverseCosineLens){
		lenscbb.setSelectedItem(INVERSE_COSINE_LENS);
	    }
	}
	lenscbb.addItemListener(il1);
	return pn1;
    }

    static int LENS_ANIM_TIME = 500;
    static float LENS_MM = 1.0f;
    static int LENS_R1 = 100;
    static int LENS_R2 = 50;

    void setLens(String s){
	if (s == NO_LENS && lens != null){
	    application.vsm.animator.createLensAnimation(LENS_ANIM_TIME,AnimManager.LS_MM_LIN,
							 new Float(-LENS_MM),
							 lens.getID(),true);
	    lens = null;
	}
	else if (s == LINEAR_LENS){
	    lens = application.vsm.getView("Demo").setLens(new FSLinearLens(1.0f,LENS_R1,1));
	    application.vsm.animator.createLensAnimation(LENS_ANIM_TIME,AnimManager.LS_MM_LIN,
							 new Float(LENS_MM),
							 lens.getID(),false);
	}
	else if (s == INVERSE_COSINE_LENS){
	    lens = application.vsm.getView("Demo").setLens(new FSInverseCosineLens(1.0f,LENS_R1, LENS_R2));
	    application.vsm.animator.createLensAnimation(LENS_ANIM_TIME,AnimManager.LS_MM_LIN,
							 new Float(LENS_MM),
							 lens.getID(),false);
	}
    }

    void buildConstraints(GridBagConstraints gbc, int gx,int gy,int gw,int gh,int wx,int wy){
	gbc.gridx=gx;
	gbc.gridy=gy;
	gbc.gridwidth=gw;
	gbc.gridheight=gh;
	gbc.weightx=wx;
	gbc.weighty=wy;
    }

    static final String msg0 = "All graphical objects belong to infinite virtual spaces. These objects are observed through cameras which provide views of regions of the virtual spaces. Cameras can be moved inside the virtual space and it is also possible to change their altitude (zoom). In this example, we have created a camera in a virtual space containing 400 objects. To pan the view, drag the mouse using the right mouse button. To change altitude, press SHIFT and drag the mouse vertically with the right mouse button.";

    static final String msg1 = "It is possible to create different kinds of graphical objects : circles, rectangles, triangles, octagons, diamonds, segments, points, text, polygons, boolean shapes, bitmap images, quadratic and cubic curves, general paths and composite objects (made of one or more shapes). Graphical operations are defined for all types of objects in a polymorphic manner: rotation, translation, resizing, color modification. All graphical objects can be semi-transparent or empty and can have a dashed and/or thick contour.";

    static final String msg2 = "Graphical object transformations and camera movements can be animated. Available transformations are Orientation, Resize, Translation, Coloration (HSV color space) and Translucency. The animation's pacing function can be linear, exponential (speed increases during animation) or slow-in/slow-out (if animation's duration is T, speed smoothly increases from 0 to T/2 and smoothly decreases from T/2 to T). Cameras can be animated in the same way (position and altitude).\nSelect an animation type and a pacing function by clicking on the green boxes and then click on one object to animate it.\n";

    static final String msg3 = "It is possible to create several virtual spaces which coexist simultaneously. Each virtual space can contain more than one camera. A view is made of at least one camera. If a view is composed of more than one camera, a layer is created for each camera and all layers are superimposed in the view (hence the translucency feature of glyphs). All layers are independent and only one is active at a time (meaning that only the glyphs observed by the camera attached to the active layer can be manipulated).\nUse the right mouse button to navigate in the active layer, left mouse button to move objects belonging to the active layer. Press key 's', or click the mouse's middle button to switch between layers.";

    static final String msg4 = "Multiple views can coexist, possibly made of cameras observing the same virtual space. Here, we have two views, each one containing a single camera. Both cameras belong to the same virtual space, and therefore can potentially show the same objects.";

    static final String msgIntro = "<html><p>This little demo introduces<br>the capabilities of the VTM.<br>Click on the arrows to<br> navigate between demos</p><p><i>In all demos objects can be<br>moved using left mouse button <br>and keystroke 'c' centers the view.</i></p></html>";


}
