/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package net.claribole.zvtm.layout.tests;

import java.awt.*;
import javax.swing.*;

import java.util.Vector;

import com.xerox.VTM.engine.*;
import com.xerox.VTM.glyphs.*;
import net.claribole.zvtm.engine.*;
import net.claribole.zvtm.glyphs.*;
import net.claribole.zvtm.widgets.*;
import net.claribole.zvtm.layout.*;

public class Test {

    VirtualSpaceManager vsm;
	static final String mSpaceName = "Main space";
    VirtualSpace vs;
    ViewEventHandler eh;   //class that receives the events sent from views (include mouse click, entering object,...)
	static final String mViewName = "Test";
    View mView;
	Camera mCamera;

    Test(){
        vsm = new VirtualSpaceManager();
        vsm.setDebug(true);
        initTest();
    }

    public void initTest(){
        eh = new EventHandlerTest(this);
        vs = vsm.addVirtualSpace(mSpaceName);
        vsm.setZoomLimit(-90);
        mCamera = vsm.addCamera(mSpaceName);
        Vector cameras = new Vector();
        cameras.add(mCamera);
        mView = vsm.addExternalView(cameras, mViewName, View.STD_VIEW, 800, 600, false, true);
        mView.setBackgroundColor(Color.LIGHT_GRAY);
        mView.setEventHandler(eh);
        mView.setNotifyMouseMoved(true);
        mCamera.setAltitude(0);
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
        new Test();
    }
    
}
