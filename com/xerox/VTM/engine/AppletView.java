/*   FILE: AppletView.java
 *   DATE OF CREATION:   Dec 27 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Thu Feb 20 16:31:33 2003 by Emmanuel Pietriga
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import net.claribole.zvtm.engine.ViewEventHandler;

  /**
   * An applet view is a panel and can be composed of one or several cameras superimposed (uses a standard JFrame)
   * @author Emmanuel Pietriga
   **/

public class AppletView extends View implements KeyListener{

    //JFrame frame;

    /**
     *@param v list of cameras
     *@param t view name
     *@param panelWidth width of window in pixels
     *@param panelHeight height of window in pixels
     *@param vsm root VTM class
     */
    protected AppletView(Vector v,String t,int panelWidth,int panelHeight,VirtualSpaceManager vsm){
	//frame=new JFrame();
	//frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	mouse=new VCursor(this);
	name=t;
	parent=vsm;
	detectMultipleFullFills=vsm.defaultMultiFill;
	initCameras(v);   //vector -> cast elements as "Camera"
// 	GridBagLayout gridBag=new GridBagLayout();
// 	GridBagConstraints constraints=new GridBagConstraints();
// 	Container cpane=ja.getContentPane();
// 	cpane.setLayout(gridBag);
// 	buildConstraints(constraints,0,0,1,1,100,90);
// 	constraints.fill=GridBagConstraints.BOTH;
// 	constraints.anchor=GridBagConstraints.CENTER;
	panel=new AppletViewPanel(v,this);
	panel.setSize(panelWidth,panelHeight);
// 	gridBag.setConstraints(panel,constraints);
// 	cpane.add(panel);
// 	WindowListener l=new WindowAdapter(){
// 		public void windowClosing(WindowEvent e){close();}
// 		public void windowActivated(WindowEvent e){activate();}
// 		public void windowDeactivated(WindowEvent e){deactivate();}
// 		public void windowIconified(WindowEvent e){iconify();}
// 		public void windowDeiconified(WindowEvent e){deiconify();}
// 	    };
// 	frame.addWindowListener(l);
// 	frame.addKeyListener(this);
// 	frame.pack();
// 	frame.setSize(panelWidth,panelHeight);
// 	if (visible){frame.setVisible(true);}
    }


    /**get the java.awt.Container for this view*/
    public Container getFrame(){return null;}

    /**tells whether this frame is selected or not - not used*/
    public boolean isSelected(){
	return false;
    } 

    /**set the window location*/
    public void setLocation(int x,int y){}

    /**set the window title*/
    public void setTitle(String t){}

    /**set the window size*/
    public void setSize(int x,int y){}

    /**can the window be resized or not (no effect)*/
    public void setResizable(boolean b){}

    /**Shows or hides this view*/
    public void setVisible(boolean b){
    }

    /**Brings this window to the front. Places this window at the top of the stacking order and shows it in front of any other windows*/
    public void toFront(){}

    /**Sends this window to the back. Places this window at the bottom of the stacking order and makes the corresponding adjustment to other visible windows*/
    public void toBack(){}

    /**destroy this view*/
    public void destroyView(){
	panel.stop();
	//parent.destroyView(this.name);
	//frame.dispose();
    }

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

    /**used only in Internal Views to get focus in view for key events (called automatically when the mouse enters the (Acc)IView)*/
    public void requestFocus(){}

}
