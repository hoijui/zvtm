/*   FILE: AppletView.java/PView.java
 *   DATE OF CREATION:   Dec 27 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Thu Feb 20 16:31:33 2003 by Emmanuel Pietriga
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2009. All Rights Reserved
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
 * $Id$
 */

package fr.inria.zvtm.engine;

import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import fr.inria.zvtm.engine.ViewEventHandler;

  /**
   * A PView (formerly AppletView) is based on a JPanel. It can be inserted anywhere in a Swing component hierarchy.
   * It can be composed of one or several cameras superimposed.
   * @author Emmanuel Pietriga
   **/

public class PView extends View implements KeyListener {

	/**
		*@param v list of cameras
		*@param t view name
		*@param panelWidth width of window in pixels
		*@param panelHeight height of window in pixels
		*/
	protected PView(Vector v,String t,int panelWidth,int panelHeight){
		mouse=new VCursor(this);
		name=t;
		detectMultipleFullFills = VirtualSpaceManager.INSTANCE.defaultMultiFill;
		initCameras(v);   //vector -> cast elements as "Camera"
		panel = new StdViewPanel(v, this, true);
		panel.setSize(panelWidth,panelHeight);
		panel.addKeyListener(this);
	}

    /** Get the java.awt.Container for this view*/
    @Override
    public Container getFrame(){return panel;}

    /** Always returns false as this is not a window. */
    @Override
    public boolean isSelected(){
	    return false;
    } 

    /** No effect. */
    @Override
    public void setLocation(int x,int y){}

    /** No effect. */
    @Override
    public void setTitle(String t){}

    /** No effect. */
    @Override
    public void setSize(int width, int height){}

    /** No effect. */
    @Override
    public void setResizable(boolean b){}

    /** No effect. */
    @Override
    public void setVisible(boolean b){}

    @Override
    public void destroyView(){
        panel.stop();
        VirtualSpaceManager.INSTANCE.destroyView(this.name);
    }

    @Override
    public void keyTyped(KeyEvent e){
        if (e.isShiftDown()) {
            if (e.isControlDown()) {panel.evHs[panel.activeLayer].Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_SHIFT_MOD, e);}
            else {panel.evHs[panel.activeLayer].Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.SHIFT_MOD, e);}
        }
        else {
            if (e.isControlDown()) {panel.evHs[panel.activeLayer].Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_MOD, e);}
            else {panel.evHs[panel.activeLayer].Ktype(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.NO_MODIFIER, e);}
        }
    }

    @Override
    public void keyPressed(KeyEvent e){
        if (e.isShiftDown()) {
            if (e.isControlDown()) {panel.evHs[panel.activeLayer].Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_SHIFT_MOD, e);}
            else {panel.evHs[panel.activeLayer].Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.SHIFT_MOD, e);}
        }
        else {
            if (e.isControlDown()) {panel.evHs[panel.activeLayer].Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_MOD, e);}
            else {panel.evHs[panel.activeLayer].Kpress(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.NO_MODIFIER, e);}
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.isShiftDown()) {
            if (e.isControlDown()) {panel.evHs[panel.activeLayer].Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_SHIFT_MOD, e);}
            else {panel.evHs[panel.activeLayer].Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.SHIFT_MOD, e);}
        }
        else {
            if (e.isControlDown()) {panel.evHs[panel.activeLayer].Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.CTRL_MOD, e);}
            else {panel.evHs[panel.activeLayer].Krelease(panel,e.getKeyChar(),e.getKeyCode(),ViewEventHandler.NO_MODIFIER, e);}
        }
    }

}
