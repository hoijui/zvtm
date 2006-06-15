/*   FILE: AccIView.java
 *   DATE OF CREATION:   Jun 08 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:              Tue Oct 12 09:15:51 2004 by Emmanuel Pietriga
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
 */ 


package com.xerox.VTM.engine;

import java.awt.Container;
import java.awt.DisplayMode;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import net.claribole.zvtm.engine.ViewEventHandler;

  /**
   * An internal view is a window and can be composed of one or several cameras superimposed (uses a JInternalFrame) - <br>
   * this one is hardware accelerated (at least under Win32) using VolatileImage available since JDK 1.4.0 (does not accelerate bitmaps)
   * @author Emmanuel Pietriga
   **/

public class AccIView extends View implements InternalFrameListener,KeyListener/*,MouseListener*/{

    JInternalFrame frame;
    IViewContainer ivc;
    
    /**
     *@param v list of cameras
     *@param t view name
     *@param panelWidth width of window in pixels
     *@param panelHeight height of window in pixels
     *@param bar true -&gt; add a status bar to this view (below main panel)
     *@param visible should the view be made visible automatically or not
     *@param vsm root VTM class
     *@param i desktop pane containing the internal frame (can be null)
     */
    public AccIView(Vector v,String t,int panelWidth,int panelHeight,boolean bar,boolean visible,VirtualSpaceManager vsm,IViewContainer i,Integer layer){
	ivc=i;
	frame=new JInternalFrame(t,true,true,true,true);
	frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	mouse=new VCursor(this);
	name=t;
	parent=vsm;
	detectMultipleFullFills=vsm.defaultMultiFill;
	initCameras(v);   //vector -> cast elements as "Camera"
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	Container cpane=frame.getContentPane();
	cpane.setLayout(gridBag);
	if (bar){
	    buildConstraints(constraints,0,0,1,1,100,90);
	    constraints.fill=GridBagConstraints.BOTH;
	    constraints.anchor=GridBagConstraints.CENTER;
	    panel=new AccViewPanel(v,this);
	    panel.setSize(panelWidth,panelHeight);
	    gridBag.setConstraints(panel,constraints);
	    cpane.add(panel);
	    buildConstraints(constraints,0,1,1,1,0,0);
	    constraints.anchor=GridBagConstraints.WEST;
	    statusBar=new JLabel(" ");
	    gridBag.setConstraints(statusBar,constraints);
	    cpane.add(statusBar);
	}
	else {
	    buildConstraints(constraints,0,0,1,1,100,90);
	    constraints.fill=GridBagConstraints.BOTH;
	    constraints.anchor=GridBagConstraints.CENTER;
	    panel=new AccViewPanel(v,this);
	    panel.setSize(panelWidth,panelHeight);
	    gridBag.setConstraints(panel,constraints);
	    cpane.add(panel);
	}
	frame.addInternalFrameListener(this);
	frame.pack();
	frame.setSize(panelWidth,panelHeight);
	if (visible){frame.setVisible(true);}
	//frame.addMouseListener(this);
	if (ivc != null){ivc.addIView(this,layer);}
	frame.addKeyListener(this);
	try {
	    frame.setSelected(true);
	} catch (java.beans.PropertyVetoException e) {}
    }

    /**
     *@param v list of cameras
     *@param t view name
     *@param panelWidth width of window in pixels
     *@param panelHeight height of window in pixels
     *@param bar true -&gt; add a status bar to this view (below main panel)
     *@param visible should the view be made visible automatically or not
     *@param vsm root VTM class
     *@param i desktop pane containing the internal frame (can be null)
     *@param mnb a menu bar, already configured with actionListeners already attached to items (it is just added to the view)
     */
    public AccIView(Vector v,String t,int panelWidth,int panelHeight,boolean bar,boolean visible,VirtualSpaceManager vsm,IViewContainer i,JMenuBar mnb,Integer layer){
	ivc=i;
	frame=new JInternalFrame(t,true,true,true,true);
	frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	frame.setJMenuBar(mnb);
	mouse=new VCursor(this);
	name=t;
	parent=vsm;
	initCameras(v);   //vector -> cast elements as "Camera"
	GridBagLayout gridBag=new GridBagLayout();
	GridBagConstraints constraints=new GridBagConstraints();
	Container cpane=frame.getContentPane();
	cpane.setLayout(gridBag);
	if (bar){
	    buildConstraints(constraints,0,0,1,1,100,90);
	    constraints.fill=GridBagConstraints.BOTH;
	    constraints.anchor=GridBagConstraints.CENTER;
	    panel=new AccViewPanel(v,this);
	    panel.setSize(panelWidth,panelHeight);
	    gridBag.setConstraints(panel,constraints);
	    cpane.add(panel);
	    buildConstraints(constraints,0,1,1,1,0,0);
	    constraints.anchor=GridBagConstraints.WEST;
	    statusBar=new JLabel(" ");
	    gridBag.setConstraints(statusBar,constraints);
	    cpane.add(statusBar);
	}
	else {
	    buildConstraints(constraints,0,0,1,1,100,90);
	    constraints.fill=GridBagConstraints.BOTH;
	    constraints.anchor=GridBagConstraints.CENTER;
	    panel=new AccViewPanel(v,this);
	    panel.setSize(panelWidth,panelHeight);
	    gridBag.setConstraints(panel,constraints);
	    cpane.add(panel);
	}
	frame.addInternalFrameListener(this);
	frame.pack();
	frame.setSize(panelWidth,panelHeight);
	if (visible){frame.setVisible(true);}
	//frame.addMouseListener(this);
	if (ivc != null){ivc.addIView(this,layer);}
	frame.addKeyListener(this);
	try {
	    frame.setSelected(true);
	} catch (java.beans.PropertyVetoException e) {}
    }

    /**get the java.awt.Container for this view*/
    public Container getFrame(){return frame;}

    /**tells whether this frame is selected or not - not used*/
    public boolean isSelected(){
	return frame.isSelected();
    }
    
    /**set the window location*/
    public void setLocation(int x,int y){frame.setLocation(x,y);}
    
    /**set the window title*/
    public void setTitle(String t){frame.setTitle(t);}

    /**set the window size*/
    public void setSize(int x,int y){frame.setSize(x,y);}

    /**can the window be resized or not*/
    public void setResizable(boolean b){frame.setResizable(b);}

    /**Shows or hides this view*/
    public void setVisible(boolean b){
	frame.setVisible(b);
	if (b){this.activate();}
	else {this.deactivate();}
    }
    
    /**Brings this window to the front. Places this window at the top of the stacking order and shows it in front of any other windows*/
    public void toFront(){frame.toFront();}
    
    /**Sends this window to the back. Places this window at the bottom of the stacking order and makes the corresponding adjustment to other visible windows*/
    public void toBack(){frame.toBack();}
    
    /**destroy this view*/
    public void destroyView(){
	if (ivc != null){ivc.removeIView(this);}
	panel.stop();
	parent.destroyView(this.name);
	frame.dispose();
    }
    
    public void internalFrameClosing(InternalFrameEvent e) {close();}
    
    public void internalFrameClosed(InternalFrameEvent e) {}
    
    public void internalFrameOpened(InternalFrameEvent e) {}
    
    public void internalFrameIconified(InternalFrameEvent e) {iconify();}
    
    public void internalFrameDeiconified(InternalFrameEvent e) {deiconify(); }
    
    public void internalFrameActivated(InternalFrameEvent e) {activate();}
    
    public void internalFrameDeactivated(InternalFrameEvent e) {deactivate();}
    
    /**detect key typed and send to application event handler*/
    public void keyTyped(KeyEvent e){
	if (e.isShiftDown()) {
	    if (e.isControlDown()) {panel.evH.Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_SHIFT_MOD, e);}
	    else {panel.evH.Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.SHIFT_MOD, e);}
	}
	else {
	    if (e.isControlDown()) {panel.evH.Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_MOD, e);}
	    else {panel.evH.Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.NO_MODIFIER, e);}
	}
    }

    /**detect key pressed and send to application event handler*/
    public void keyPressed(KeyEvent e){
	if (e.isShiftDown()) {
	    if (e.isControlDown()) {panel.evH.Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_SHIFT_MOD, e);}
	    else {panel.evH.Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.SHIFT_MOD, e);}
	}
	else {
	    if (e.isControlDown()) {panel.evH.Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_MOD, e);}
	    else {panel.evH.Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.NO_MODIFIER, e);}
	}
    }

    /**detect key released and send to application event handler*/
    public void keyReleased(KeyEvent e) {
	if (e.isShiftDown()) {
	    if (e.isControlDown()) {panel.evH.Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_SHIFT_MOD, e);}
	    else {panel.evH.Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.SHIFT_MOD, e);}
	}
	else {
	    if (e.isControlDown()) {panel.evH.Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_MOD, e);}
	    else {panel.evH.Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.NO_MODIFIER, e);}
	}
    }
    
    /**used only in Internal Views to get focus in view for key events.
     *(NO LONGER called automatically when the mouse enters the IView)*/
    public void requestFocus(){
	frame.requestFocus();
    }

}
