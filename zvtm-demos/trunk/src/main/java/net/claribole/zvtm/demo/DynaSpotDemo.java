/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.demo;

import java.awt.Color;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.Container;
import java.awt.GridLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;
import net.claribole.zvtm.glyphs.*;

public class DynaSpotDemo {

    VirtualSpaceManager vsm;
    VirtualSpace vs;
    ViewEventHandler eh;   //class that receives the events sent from views (include mouse click, entering object,...)

    View demoView;

    DynaSpotDemo(short ogl){
        vsm=new VirtualSpaceManager();
        vsm.setDebug(true);
        initTest(ogl);
    }

    public void initTest(short ogl){
        eh=new DynaSpotDemoEvtHdlr(this);
        vs = vsm.addVirtualSpace("src");
        vsm.setZoomLimit(-90);
        vsm.addCamera("src");
        Vector cameras=new Vector();
        cameras.add(vsm.getVirtualSpace("src").getCamera(0));
        short vt = View.STD_VIEW;
        switch(ogl){
            case View.OPENGL_VIEW:{vt = View.OPENGL_VIEW;break;}
        }
        demoView = vsm.addExternalView(cameras, "DynaSpot Demo", vt, 800, 600, false, true);
        demoView.setBackgroundColor(Color.WHITE);
        demoView.setEventHandler(eh);
        demoView.setNotifyMouseMoved(true);
        vsm.getVirtualSpace("src").getCamera(0).setAltitude(50);
		vsm.addGlyph(new VCircle(-300,0,0,4,Color.BLACK), "src");
		vsm.addGlyph(new VCircle(300,0,0,4,Color.BLACK), "src");
        vsm.repaintNow();
		demoView.getCursor().activateDynaSpot(true);
    }

	void setDynaSpotVisibility(short v){
		demoView.getCursor().setDynaSpotVisibility(v);
	}
    
    public static void main(String[] args){
        System.out.println("-----------------");
        System.out.println("General information");
        System.out.println("JVM version: "+System.getProperty("java.vm.vendor")+" "+System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version"));
        System.out.println("OS type: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+"/"+System.getProperty("os.arch")+" "+System.getProperty("sun.cpu.isalist"));
        System.out.println("-----------------");
        new DynaSpotDemo((args.length > 0) ? Short.parseShort(args[0]) : 0);
    }
    
}

