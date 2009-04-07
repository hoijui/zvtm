/*   FILE: IViewContainer.java
 *   DATE OF CREATION:   Dec 27 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Fri Oct 11 10:18:07 2002 by Emmanuel Pietriga
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

/**
 *a JFrame that can contain AccIView and IView VTM windows (required to create such views)
 *@author Emmanuel Pietriga
 */

public class IViewContainer extends JFrame {

    /**parent desktop pane of IViews (JInternalFrames)*/
    public JDesktopPane dsk;

    /**
     *@param name title of the JFrame
     *@param w width of the main JFrame
     *@param h height of the main JFrame
     */
    protected IViewContainer(String name,int w,int h){
	super();
	dsk=new JDesktopPane();
	this.setContentPane(dsk);
	dsk.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
	this.pack();
	this.setLocation(0,0);
	this.setSize(w,h);
	//this.setVisible(true);
	this.init(name);
    }

    /**
     *width and height are set to max value
     *@param name title of the JFrame
     */
    protected IViewContainer(String name){
	super();
	dsk=new JDesktopPane();
	this.setContentPane(dsk);
	dsk.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
	this.pack();
	this.setLocation(0,0);
	Toolkit toolkit=Toolkit.getDefaultToolkit();
	Dimension screenSize=toolkit.getScreenSize();
	this.setSize(screenSize.width,screenSize.height);
	//this.setVisible(true);
	this.init(name);
    }

    void init(String n){
	this.setTitle(n);
	WindowListener w0=new WindowAdapter(){
		public void windowClosing(WindowEvent e){System.exit(0);}
	    };
	this.addWindowListener(w0);
    }

    /**register an IView in this IView container - used to retrieve IView from internal frame (key events)*/
    void addIView(IView iv,Integer layer){
	dsk.add(iv.frame,layer);
    }

    /**register an AccIView in this IView container - used to retrieve AccIView from internal frame (key events)*/
    void addIView(AccIView iv,Integer layer){
	dsk.add(iv.frame,layer);
    }

    void removeIView(IView iv){}//not implemented yet

    void removeIView(AccIView iv){}//not implemented yet

    public void addComponent(Component c,Integer layer){
	dsk.add(c,layer);
    }

    public void removeComponent(Component c){
	dsk.remove(c);
    }

}
