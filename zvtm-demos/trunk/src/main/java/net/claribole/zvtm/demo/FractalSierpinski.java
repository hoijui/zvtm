/*   FILE: FractalSierpinski.java
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
import com.xerox.VTM.glyphs.VPoint;

public class FractalSierpinski extends FractalDemo {

    int N = 8;
    int progressIndex = 0;
    long totalNumberOfGlyphs = 0;
    ProgFrame pf;

    FractalSierpinski(int n){
	if (n > 0){
	    N = n;
	}
	    vsm = VirtualSpaceManager.INSTANCE;
	init();
    }

    public void init(){
	eh=new FractalEventHandler(this);
	vsm.addVirtualSpace(mainSpaceName);
	buildSierpinski();
	vsm.addCamera(mainSpaceName);
	vsm.getVirtualSpace(mainSpaceName).getCamera(0).setZoomFloor(-90);
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

    /* adapted from http://www.hewgill.com/chaos-and-fractals/c02_sierpinski.html
       which was itself translated to Java from the Basic version of
       Chaos and Fractals by Peitgen, Jurgens, and Saupe */
    void buildSierpinski(){
	reset();
	long n = Math.round(Math.pow(2, N));
	totalNumberOfGlyphs = n * n;
	pf = new ProgFrame("Computing points...", "Sierpinski Fractal Demo");
	for (int y = 0; y < n; y++) {
	    pf.setPBValue((int)(y * 100 / n));
	    for (int x = 0; x < n; x++) {
		if ((x & (y-x)) == 0) {
		    vsm.addGlyph(new VPoint(x+15800-y/2,3000-y,Color.white), mainSpaceName);
		    progressIndex += 1;
		}
	    }
	}
	System.out.println("Computed "+progressIndex+" points");
	pf.destroy();
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
		    System.out.println("java [classpath_options] net.claribole.zvtm.demo.FractalSierpinski [options]");
		    System.out.println();
		    System.out.println("Options:");
		    System.out.println("    -Nxx  with xx a positive integer specifying a value for N (default is 8) (Sierpinski algorithm)");
		    System.exit(0);
		}
	    }
	}
	new FractalSierpinski(n);
    }

}
