/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.eval;

import java.awt.Color;
import java.awt.Point;

import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;
import net.claribole.zvtm.glyphs.*;

public class EvalDebugger {

    VirtualSpaceManager vsm;
    VirtualSpace vs;
    ViewEventHandler eh;   //class that receives the events sent from views (include mouse click, entering object,...)

    View testView;

    EvalDebugger(short ogl){
        vsm=new VirtualSpaceManager();
        vsm.setDebug(true);
        //vsm.setDefaultMultiFills(true);
        initEvalDebugger(ogl);
    }

    public void initEvalDebugger(short ogl){
        eh=new EvalDebuggerEventHandler(this);
        vs = vsm.addVirtualSpace("src");
        vsm.setZoomLimit(-90);
        vsm.addCamera("src");
        Vector cameras=new Vector();
        cameras.add(vsm.getVirtualSpace("src").getCamera(0));
        short vt = View.STD_VIEW;
        switch(ogl){
            case View.OPENGL_VIEW:{vt = View.OPENGL_VIEW;break;}
            case View.VOLATILE_VIEW:{vt = View.VOLATILE_VIEW;break;}
        }
        testView = vsm.addExternalView(cameras, "Test", vt, 1280, 800, false, true);
        testView.setBackgroundColor(Color.WHITE);
        testView.setEventHandler(eh);
        testView.setNotifyMouseMoved(true);
        vsm.getVirtualSpace("src").getCamera(0).setAltitude(0);
		testView.setAntialiasing(true);
		generateScene();
        vsm.repaintNow();
		testView.getCursor().setDynaSpotMaxRadius(20);
		testView.getCursor().setCutoffFrequencyParameters(0.8, 0.1);
		testView.getCursor().activateDynaSpot(true);
		testView.getCursor().setDynaSpotAreaVisible(true);
    }

	void generateScene(){
		int w = 10;
		int ox = 0;
		int oy = 0;
		float dir = 0;
		DistractorGenerator.setParameters(300, w, 1.0f, 20);
		DistractorGenerator.setTranslation(ox, oy);
		DistractorGenerator.setDirection(dir);
		vsm.addGlyph(new VCircle(ox, oy, 0, w/2, Color.RED), "src");
		Point[] coords = DistractorGenerator.generate();
		// 1st coords are the target
		vsm.addGlyph(new VCircle(coords[0].x, coords[0].y, 0, w/2, Color.GREEN), "src");
		// other are distractors
		VCircle c;
		for (int i=1;i<coords.length;i++){
			c = new VCircle(coords[i].x, coords[i].y, 0, w/2, Color.DARK_GRAY, Color.DARK_GRAY);
			c.setFilled(false);
			vsm.addGlyph(c, "src");
		}
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
        new EvalDebugger((args.length > 0) ? Short.parseShort(args[0]) : 0);
    }
    
}
