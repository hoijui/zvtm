/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package net.claribole.zvtm.demo;

import java.awt.Color;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner;
import javax.swing.JLabel;
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

	DynaSpotDemoControlPanel cp;

    DynaSpotDemo(short ogl){
        vsm=new VirtualSpaceManager();
        vsm.setDebug(true);
        initTest(ogl);
		cp = new DynaSpotDemoControlPanel(this);
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
        demoView.setBackgroundColor(Color.LIGHT_GRAY);
        demoView.setEventHandler(eh);
        demoView.setNotifyMouseMoved(true);
        vsm.getVirtualSpace("src").getCamera(0).setAltitude(50);
		vsm.addGlyph(new VCircle(-300,0,0,4,Color.BLACK), "src");
		vsm.addGlyph(new VCircle(300,0,0,4,Color.BLACK), "src");
        vsm.repaintNow();
		// DynaSpot setup and activation
		demoView.getCursor().setDynaSpotMaxRadius(20);
		demoView.getCursor().setCutoffFrequencyParameters(1.3, 0.01);
		demoView.getCursor().setOffsets(20, 20);
		demoView.getCursor().activateDynaSpot(true);
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

class DynaSpotDemoControlPanel extends JFrame implements ChangeListener {
	
	DynaSpotDemo application;
	JSpinner cutOffA, cutOffB, offset, maxRadius;
	
	DynaSpotDemoControlPanel(DynaSpotDemo app){
		this.application = app;
		Container cpane = getContentPane();
		cpane.setLayout(new GridLayout(8, 1));
		
		cutOffA = new JSpinner(new SpinnerNumberModel(application.demoView.getCursor().getCutoffFrequencyParameterA(), 0.1, 5.0, 0.1));
		cutOffB = new JSpinner(new SpinnerNumberModel(application.demoView.getCursor().getCutoffFrequencyParameterB(), 0.0001, 1, 0.001));
		offset = new JSpinner(new SpinnerNumberModel(application.demoView.getCursor().getDynaSpotMaxRadius(), 0, 50, 1));
		maxRadius = new JSpinner(new SpinnerNumberModel(application.demoView.getCursor().getOffsets().x, 1, 100, 1));
		
		cpane.add(new JLabel("Low-pass filter cutoff param A"));
		cpane.add(cutOffA);
		cpane.add(new JLabel("Low-pass filter cutoff param B"));
		cpane.add(cutOffB);
		cpane.add(new JLabel("Distance offset"));
		cpane.add(offset);
		cpane.add(new JLabel("DynaSpot maximum Radius"));
		cpane.add(maxRadius);
		cutOffA.addChangeListener(this);
		cutOffB.addChangeListener(this);
		offset.addChangeListener(this);
		maxRadius.addChangeListener(this);
		
		WindowListener w0 = new WindowAdapter(){
			public void windowClosing(WindowEvent e){System.exit(0);}
		    };
		this.addWindowListener(w0);
		this.setLocation(900, 0);
		this.setSize(400, 200);
		this.setTitle("DynaSpot Parameters");
		this.setVisible(true);
		
	}
	
	public void stateChanged(ChangeEvent e){
		Object o = e.getSource();
		if (o == cutOffA){application.demoView.getCursor().setCutoffFrequencyParameters(((Number)cutOffA.getValue()).doubleValue(), ((Number)cutOffB.getValue()).doubleValue());}
		else if (o == cutOffB){application.demoView.getCursor().setCutoffFrequencyParameters(((Number)cutOffA.getValue()).doubleValue(), ((Number)cutOffB.getValue()).doubleValue());}
		else if (o == offset){application.demoView.getCursor().setOffsets(((Number)offset.getValue()).intValue(), ((Number)offset.getValue()).intValue());}
		else if (o == maxRadius){application.demoView.getCursor().setDynaSpotMaxRadius(((Number)maxRadius.getValue()).intValue());}
	}
	
}
