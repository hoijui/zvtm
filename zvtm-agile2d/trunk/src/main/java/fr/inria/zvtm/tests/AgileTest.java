/*
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2011.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.tests;

import java.awt.Color;

import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.AgilePanelType;
import fr.inria.zvtm.event.ViewAdapter;

import fr.inria.zvtm.glyphs.*;

public class AgileTest {
	
	VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
	VirtualSpace mSpace;
	View mView;
	Camera mCamera;
	
	public AgileTest(){
		init();
		populate();
	}
	
	void init(){
		View.registerViewPanelType(AgilePanelType.AGILE_VIEW, new AgilePanelType());
		mSpace = vsm.addVirtualSpace(VirtualSpace.ANONYMOUS);
		mCamera = mSpace.addCamera();
		Vector cameras = new Vector(1);
		cameras.add(mCamera);
		mView = vsm.addFrameView(cameras, View.ANONYMOUS, AgilePanelType.AGILE_VIEW, 800, 600, true);
		mView.setBackgroundColor(Color.LIGHT_GRAY);
		mView.setListener(new MainListener(this), 0);
	}
	
	void populate(){
		mSpace.addGlyph(new VCircle(0, 0, 0, 10, Color.WHITE));
		mSpace.addGlyph(new VRectangle(100, 0, 0, 50, 10, Color.RED));
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
        new AgileTest();
    }
    	
}

class MainListener extends ViewAdapter {
	
	AgileTest application;
	
	MainListener(AgileTest app){
		this.application = app;
	}
	
}
