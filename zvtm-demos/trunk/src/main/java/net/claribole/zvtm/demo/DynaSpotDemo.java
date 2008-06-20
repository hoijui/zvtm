

package net.claribole.zvtm.demo;

import java.awt.Color;
import java.util.Vector;

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
        demoView.setBackgroundColor(Color.LIGHT_GRAY);
        demoView.setEventHandler(eh);
        demoView.setNotifyMouseMoved(true);
        vsm.getVirtualSpace("src").getCamera(0).setAltitude(50);
		vsm.addGlyph(new VCircle(0,0,0,50,Color.WHITE), "src");
        vsm.repaintNow();
		// DynaSpot setup and activation
		demoView.getCursor().setDynaSpotMaxRadius(20);
		demoView.getCursor().setCutoffFrequencyParameters(3, 0.001);
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
