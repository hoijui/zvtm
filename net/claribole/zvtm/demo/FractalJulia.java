/*   FILE: FractalJulia.java
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
import com.xerox.VTM.glyphs.VSegment;

public class FractalJulia extends FractalDemo {

    static final int left = 20;
    static final int w = 300000;
    static final int s = w/3;
    static final int orig = left + w/2;
    static final double xc = -1;
    static final double yc = 0.1;

    ProgFrame pf;
    int N = 10000;

    FractalJulia(int n){
	if (n > 0){
	    N = n;
	}
	vsm=new VirtualSpaceManager();
	init();
    }

    public void init(){
	eh=new FractalEventHandler(this);
	vsm.addVirtualSpace(mainSpaceName);
	vsm.setZoomLimit(-90);
	buildJulia();
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
    }

    /* adapted from http://www.hewgill.com/chaos-and-fractals/c13_julia.html
       which was itself translated to Java from the Basic version of
       Chaos and Fractals by Peitgen, Jurgens, and Saupe */
    void buildJulia(){
	reset();
	pf = new ProgFrame("Computing "+N+" points...", "Julia Fractal Demo");
	vsm.addGlyph(new VSegment(left, -left-w/2, 0, Color.gray, left+w, -left-w/2), mainSpaceName);
	vsm.addGlyph(new VSegment(left+w/2, -left, 0, Color.gray, left+w/2, -left-w), mainSpaceName);
	double xn = 0.25;
	double yn = 0;
	for (int i = 0; i < N; i++) {
	    double a = xn - xc;
	    double b = yn - yc;
	    if (a == 0) {
		xn = Math.sqrt(Math.abs(b)/2);
		if (xn > 0) {
		    yn = b/(2*xn);
		} else {
		    yn = 0;
		}
	    } else if (a > 0) {
		xn = Math.sqrt((Math.sqrt(a*a + b*b) + a)/2);
		yn = b / (2 * xn);
	    } else {
		yn = Math.sqrt((Math.sqrt(a*a + b*b) - a)/2);
		xn = b / (2 * yn);
	    }
	    if (i == 0) {
		xn += 0.5;
	    }
	    if (Math.random() >= 0.5) {
		xn = -xn;
		yn = -yn;
	    }
	    vsm.addGlyph(new VPoint((int)(xn*s + orig), (int)(yn*s - orig), Color.white), mainSpaceName);
	    if (i % 10 == 0){//don't refresh too often: it is time consuming
		pf.setPBValue(i * 100 / N);
	    }
	}
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
		    System.out.println("java [classpath_options] net.claribole.zvtm.demo.FractalJulia [options]");
		    System.out.println();
		    System.out.println("Options:");
		    System.out.println("    -Nxx  with xx a positive integer specifying a value for N (default is 10000) (Julia algorithm)");
		    System.exit(0);
		}
	    }
	}
	new FractalJulia(n);
    }

}
