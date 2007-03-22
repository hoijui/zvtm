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
import java.awt.Dimension;
import java.awt.geom.QuadCurve2D;

import java.util.Vector;

import javax.swing.ImageIcon;

import net.claribole.zvtm.glyphs.*;
import net.claribole.zvtm.lens.*;
import net.claribole.zvtm.engine.*;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;

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

    VImageOrST im;

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
	vsm.addGlyph(new VCircle(0,0,0,50,Color.RED), "src");
	im = new VImageOrST(0,0,0,(new ImageIcon("images/logo-futurs-small.png")).getImage(), (float)(Math.PI/4.0), 1.0f);
	vsm.addGlyph(im,"src");

	vsm.repaintNow();
    }

    void bob(float f){
	Vector data =new Vector();
	data.add(new Float(0));
	data.add(new Float(0));
	data.add(new Float(0));
	data.add(new Float(0));
	data.add(new Float(0));
	data.add(new Float(0));
	data.add(new Float(f));

	vsm.animator.createGlyphAnimation(300, AnimManager.GL_COLOR_LIN, data,
					  im.getID(), null);
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
