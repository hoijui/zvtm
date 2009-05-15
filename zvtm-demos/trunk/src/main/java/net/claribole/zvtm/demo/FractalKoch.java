/*   FILE: FractalKoch.java
 *   DATE OF CREATION:  Thu Dec 30 12:53:03 2004
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.demo;

import java.awt.Color;
import java.util.Vector;

import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.glyphs.VSegment;

public class FractalKoch extends FractalDemo {

    double R = 0.29;
    int N = 7;
    int progressIndex = 0;
    int totalNumberOfGlyphs = 0;
    ProgFrame pf;

    FractalKoch(int n){
	if (n > 0){
	    N = n;
	}
	    vsm = VirtualSpaceManager.INSTANCE;
	init();
    }

    public void init(){
	eh=new FractalEventHandler(this);
	vsm.addVirtualSpace(mainSpaceName);
	vsm.getVirtualSpace(mainSpaceName).getCamera(0).setZoomFloor(-90);
	buildKoch();
	vsm.addCamera(mainSpaceName);
	Vector cameras=new Vector();
	cameras.add(vsm.getVirtualSpace(mainSpaceName).getCamera(0));
	vsm.addExternalView(cameras, mainViewName, View.STD_VIEW, 800, 600, false, true);
	View v = vsm.getView(mainViewName);
	v.setEventHandler(eh);
	v.setBackgroundColor(Color.black);
	v.mouse.setColor(Color.white);
	vsm.getGlobalView(vsm.getVirtualSpace(mainSpaceName).getCamera(0), 500);
    }

    void reset(){
	vsm.destroyGlyphsInSpace(mainSpaceName);
	totalNumberOfGlyphs = 0;
	progressIndex = 0;
    }

    void buildKoch(){
	reset();
	totalNumberOfGlyphs = (int)Math.pow(4, N-1);
	pf = new ProgFrame("Computing "+totalNumberOfGlyphs+" segments...", "Kosh Fractal Demo");
	koch(1, 0, 200000, 300000, 200000);
	pf.destroy();
    }

    void buildKochCallback(){
	progressIndex+=1;
	pf.setPBValue((progressIndex * 100 / totalNumberOfGlyphs));
    }

    /* adapted from http://www.hewgill.com/chaos-and-fractals/c03_koch.html
       which was itself translated to Java from the Basic version of
       Chaos and Fractals by Peitgen, Jurgens, and Saupe */
    private void koch(int level, double x1, double y1, double x2, double y2){
	if (level < N) {
	    double nx = (2*x1+x2)/3;
	    double ny = (2*y1+y2)/3;
	    koch(level+1, x1, y1, nx, ny);
	    double ox = nx;
	    double oy = ny;
	    nx = (x1+x2)/2 - R*(y1-y2);
	    ny = (y1+y2)/2 + R*(x1-x2);
	    koch(level+1, ox, oy, nx, ny);
	    ox = nx; oy = ny;
	    nx = (x1+2*x2)/3;
	    ny = (y1+2*y2)/3;
	    koch(level+1, ox, oy, nx, ny);
	    koch(level+1, nx, ny, x2, y2);
	}
	else {
	    VSegment s = new VSegment((long)x1, (long)-y1, 0, Color.white, (long)x2, (long)-y2);
	    vsm.addGlyph(s, mainSpaceName);
	    buildKochCallback();
	}
    }

    public static void main(String[] args){
	int n = 0;
	for (int i=0;i<args.length;i++){
	    if (args[i].startsWith("-")){
		if (args[i].startsWith("-N") && args[i].length() > 2){
		    try {
			n = Integer.parseInt(args[i].substring(2));
		    }
		    catch (NumberFormatException ex){System.err.println("Invalid value for -N");}
		}
		else if (args[i].equals("-h") || args[i].indexOf("-help") != -1){
		    System.out.println("java [classpath_options] net.claribole.zvtm.demo.FractalKoch [options]");
		    System.out.println();
		    System.out.println("Options:");
		    System.out.println("    -Nxx  with xx a positive integer specifying a value for N (default is 6) (Koch algorithm)");
		    System.exit(0);
		}
	    }
	}
	new FractalKoch(n);
    }

}
