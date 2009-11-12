/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster.examples;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import fr.inria.zvtm.cluster.ClusteredView;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VRectangle;

import java.awt.Color;
import java.util.Vector;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Sample master application.
 */
public class CenterOnRegion {
    //shortcut
    private VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE; 

    CenterOnRegion(){
        vsm.setMaster("CenterOnRegion");
        VirtualSpace vs = vsm.addVirtualSpace("testSpace");
        Camera cam = vs.addCamera();
        Vector<Camera> cameras = new Vector<Camera>();
        cameras.add(cam);	
        ClusteredView cv = 
            new ClusteredView(3, //origin (block number)
                    2760, 
                    1740,
                    4, 
                    8, 	
                    3, 
                    4, 
                    cameras);
        vsm.addClusteredView(cv);

        Camera cam2 = vs.addCamera();
        Vector<Camera> cameras2 = new Vector<Camera>();
        cameras2.add(cam2);
        ClusteredView cv2 = 
            new ClusteredView(27, //origin (block number)
                    2760, 
                    1740,
                    4, 
                    8, 	
                    4, 
                    2, 
                    cameras2);
        vsm.addClusteredView(cv2);

        long north = 30000;
        long south = -5000;
        long west = -90000;
        long east = -20000;
        long radius = 1700; //radius (circle) or half width/height (rect)

        VRectangle northWest = new VRectangle(west,north,0,radius,radius,Color.GREEN); 
        VCircle    northEast = new VCircle(east,north,0,radius,Color.YELLOW);
        VCircle    southWest = new VCircle(west,south,0,radius,Color.RED);
        VRectangle southEast = new VRectangle(east,south,0,radius,radius,Color.BLUE);

        vs.addGlyph(northWest);
        vs.addGlyph(northEast);
        vs.addGlyph(southWest);
        vs.addGlyph(southEast);

        Location loc = cv.centerOnRegion(cam, west-radius, north+radius, east+radius, south-radius);
        Location loc2 = cv2.centerOnRegion(cam2, west-radius, north+radius, east+radius, south-radius);
        cam.setLocation(loc);
        cam2.setLocation(loc2);
    }

    public static void main(String[] args){
        new CenterOnRegion();
    }
}

