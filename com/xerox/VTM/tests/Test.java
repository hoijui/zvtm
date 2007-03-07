/*   FILE: Test.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
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

package com.xerox.VTM.tests;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.*;
import java.awt.geom.QuadCurve2D;

import java.util.Vector;

import javax.swing.*;

import net.claribole.zvtm.glyphs.*;
import net.claribole.zvtm.lens.*;
import net.claribole.zvtm.engine.*;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;

public class Test extends JFrame {

    VirtualSpaceManager vsm;

    ViewEventHandler eh1,eh2;   //class that receives the events sent from views (include mouse click, entering object,...)

    View v1,v2;

    Test(short ogl){
	super();
	vsm=new VirtualSpaceManager();
	vsm.setDebug(true);
	//vsm.setDefaultMultiFills(true);
	initTest(ogl);
    }

    JTabbedPane tp;

    public void initTest(short ogl){

	eh1=new EventHandlerTest(this);
	eh2=new EventHandlerTest(this);
	vsm.addVirtualSpace("vs1");
	vsm.addVirtualSpace("vs2");
	vsm.addCamera("vs1");
	vsm.addCamera("vs2");
	Vector cameras=new Vector();
	cameras.add(vsm.getVirtualSpace("vs1").getCamera(0));




	Container c = this.getContentPane();
	tp = new JTabbedPane();
	tp.add("Test1", vsm.addPanelView(cameras, "v1", 700, 500));
	cameras.clear();
	cameras.add(vsm.getVirtualSpace("vs2").getCamera(0));
	tp.add("Test2", vsm.addPanelView(cameras, "v2", 700, 500));

	c.add(tp);

	v1 = vsm.getView("v1");
	v2 = vsm.getView("v2");
	v1.setEventHandler(eh1);
	v2.setEventHandler(eh2);

	setSize(800,600);
	setVisible(true);



// 	short vt = View.STD_VIEW;
// 	switch(ogl){
// 	case View.OPENGL_VIEW:{vt = View.OPENGL_VIEW;break;}
// 	case View.VOLATILE_VIEW:{vt = View.VOLATILE_VIEW;break;}
// 	}
// 	testView = vsm.addExternalView(cameras, "Test", vt, 800, 600, false, true);
// 	testView.setEventHandler(eh);
// 	testView.setNotifyMouseMoved(true);
// 	vsm.getVirtualSpace("src").getCamera(0).setAltitude(50);
// 	vsm.getVirtualSpace("src").getCamera(0).moveTo(50,50);
	
	for (int i=0;i<10;i++){
	    for (int j=0;j<5;j++){
		vsm.addGlyph(new VRectangle(i*20,j*20,0,10,10,Color.getHSBColor(i/10.0f,j/10.0f,1)), "vs1");
	    }
	}

	for (int i=0;i<5;i++){
	    for (int j=0;j<10;j++){
		vsm.addGlyph(new VRectangle(i*20,j*20,0,10,10,Color.getHSBColor(i/10.0f,1,1)), "vs2");
	    }
	}

	

// 	vsm.repaintNow();
    }


    public static void main(String[] args){
	System.out.println("-----------------");
	System.out.println("General information");
	System.out.println("JVM version: "+System.getProperty("java.vm.vendor")+" "+System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version"));
	System.out.println("OS type: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+"/"+System.getProperty("os.arch")+" "+System.getProperty("sun.cpu.isalist"));
	System.out.println("-----------------");
	System.out.println("Directory information");
	System.out.println("Java Classpath: "+System.getProperty("java.class.path"));	
	System.out.println("Java directory: "+System.getProperty("java.home"));
	System.out.println("Launching from: "+System.getProperty("user.dir"));
	System.out.println("-----------------");
	System.out.println("User informations");
	System.out.println("User name: "+System.getProperty("user.name"));
	System.out.println("User home directory: "+System.getProperty("user.home"));
	System.out.println("-----------------");
	Test appli=new Test((args.length > 0) ? Short.parseShort(args[0]) : 0);
    }
    
}
