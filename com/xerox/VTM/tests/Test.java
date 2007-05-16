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
 *
 * $Id:$
 */

package com.xerox.VTM.tests;

import java.awt.Color;
import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;
import net.claribole.zvtm.glyphs.*;

public class Test {

    VirtualSpaceManager vsm;

    ViewEventHandler eh;   //class that receives the events sent from views (include mouse click, entering object,...)

    View testView;

    Test(short ogl){
	vsm=new VirtualSpaceManager();
	vsm.setDebug(true);
	//vsm.setDefaultMultiFills(true);
	initTest(ogl);
    }

    public void initTest(short ogl){

	eh=new EventHandlerTest(this);
	vsm.addVirtualSpace("src");
	vsm.setZoomLimit(-90);
	vsm.addCamera("src");
	Vector cameras=new Vector();
	cameras.add(vsm.getVirtualSpace("src").getCamera(0));
	short vt = View.STD_VIEW;
	switch(ogl){
	case View.OPENGL_VIEW:{vt = View.OPENGL_VIEW;break;}
	case View.VOLATILE_VIEW:{vt = View.VOLATILE_VIEW;break;}
	}
	testView = vsm.addExternalView(cameras, "Test", vt, 800, 600, false, true);
	testView.setEventHandler(eh);
	testView.setNotifyMouseMoved(true);
	vsm.getVirtualSpace("src").getCamera(0).setAltitude(50);

	
	vsm.addGlyph(new VRectangle(0,0,0,100,100,Color.white),"src");

	vsm.repaintNow();
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
	new Test((args.length > 0) ? Short.parseShort(args[0]) : 0);
    }
    
}
