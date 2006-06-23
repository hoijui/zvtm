/*   FILE: AbstractWorldGenerator.java
 *   DATE OF CREATION:  Tue Nov 22 09:36:06 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package net.claribole.zvtm.eval;

import java.awt.Color;

import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

class AbstractWorldGenerator {
    
    static final String CSV_SEP = ";";

    static int TREE_DEPTH = 4; // we want subjects to navigate through 5 levels, includes root
    static final long SMALLEST_ELEMENT_WIDTH = 500;
    static final long MUL_FACTOR = 50;

    static long WORLD_WIDTH;
    static long WORLD_HEIGHT;
    static long HALF_WORLD_WIDTH;
    static long HALF_WORLD_HEIGHT;

    static final Color DEEPEST_LEVEL_COLOR = Color.BLUE;
    static long[] widthByLevel;
    static Color[] COLOR_BY_LEVEL;
    static {
	// compute size of all levels
	widthByLevel = new long[TREE_DEPTH];
	COLOR_BY_LEVEL = new Color[TREE_DEPTH];
	for (int i=0;i<TREE_DEPTH;i++){
	    widthByLevel[i] = SMALLEST_ELEMENT_WIDTH * Math.round(Math.pow(MUL_FACTOR, (TREE_DEPTH-i-1)));
	    COLOR_BY_LEVEL[i] = Color.getHSBColor(0, 0, (i+1)/((float)TREE_DEPTH));
	}
	COLOR_BY_LEVEL[COLOR_BY_LEVEL.length-1] = DEEPEST_LEVEL_COLOR;
	WORLD_WIDTH = widthByLevel[0] * 2;
	WORLD_HEIGHT = WORLD_WIDTH;
	HALF_WORLD_WIDTH = WORLD_WIDTH / 2;
	HALF_WORLD_HEIGHT = WORLD_HEIGHT / 2;
    }

    // card(NB_CHILDREN) gives the number of trials
    int[] DENSITIES = {5, 10, 20, 10, 5, 20, 5, 20 ,10};
    int trialCount;

    static final double ROUND_CORNER_RATIO = 5;

    AbstractRegion root; // region at level 0

    BufferedWriter bwt;
    
    void generateWorld(String file){
	File output = new File(AbstractTaskLogManager.TRIAL_DIR + File.separator + file);
	System.out.println("Initializing file "+output.getAbsolutePath());
	try {
	    bwt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
	    System.out.print("Generating trials ");
	    for (int i=0;i<DENSITIES.length;i++){
		trialCount = i;
		bwt.write("# Trial="+ trialCount + CSV_SEP +
			  "Density=" + DENSITIES[trialCount]);
		bwt.newLine();
		System.out.print(".");
		int depth = 0;
		// init highest region
		bwt.write("# Level "+depth);
		bwt.newLine();
		bwt.write("0" + CSV_SEP +
			  "0" + CSV_SEP +
			  (widthByLevel[0]) + CSV_SEP +
			  (widthByLevel[0]) + CSV_SEP +
			  ((int)Math.round(widthByLevel[0]/ROUND_CORNER_RATIO)) + CSV_SEP +
			  ((int)Math.round(widthByLevel[0]/ROUND_CORNER_RATIO)));
		bwt.newLine();
		populateRegion(0, 0, widthByLevel[0], widthByLevel[0], depth+1);
	    }
	    bwt.flush();
	    bwt.close();
	}
	catch (IOException ex){ex.printStackTrace();}
	System.out.println(" done");
    }

    static long[][] addForbiddenRegion(long[][] wnes, long w, long n, long e, long s){
	long[][] tmpL = new long[wnes.length+1][4];
	System.arraycopy(wnes, 0, tmpL, 0, wnes.length);
	long[] nwnes = new long[4];
	nwnes[0] = w;
	nwnes[1] = n;
	nwnes[2] = e;
	nwnes[3] = s;
	tmpL[tmpL.length-1] = nwnes; 
	return tmpL;
    }

    static boolean inForbiddenRegion(long[][] wnes, long x, long y){
	for (int i=0;i<wnes.length;i++){
	    if (x >= wnes[i][0] && x <= wnes[i][2] && y >= wnes[i][3] && y <= wnes[i][1]){
		return true;
	    }
	}
	return false;
    }

    void populateRegion(long parentX, long parentY, long parentW, long parentH, int depth) throws IOException {
	long[][] fr = new long[0][4];
	bwt.write("# Level "+depth);
	bwt.newLine();
	// generate NB_CHILDREN-1 false targets
	long vx,vy;
	for (int i=0;i<DENSITIES[trialCount]-1;i++){
	    do {
		vx = Math.round(parentX + Math.random()*(parentW-widthByLevel[depth])*2-(parentW-widthByLevel[depth]));
		vy = Math.round(parentY + Math.random()*(parentH-widthByLevel[depth])*2-(parentH-widthByLevel[depth]));
	    } while(inForbiddenRegion(fr, vx, vy));
	    bwt.write(vx + CSV_SEP +
		      vy + CSV_SEP +
		      widthByLevel[depth] + CSV_SEP +
		      widthByLevel[depth]);
	    bwt.newLine();
	    fr = addForbiddenRegion(fr, vx-4*widthByLevel[depth], vy+4*widthByLevel[depth], vx+4*widthByLevel[depth], vy-4*widthByLevel[depth]);
	}
	// generate 1 target
	do {
	    vx = Math.round(parentX + Math.random()*(parentW-widthByLevel[depth])*2-(parentW-widthByLevel[depth]));
	    vy = Math.round(parentY + Math.random()*(parentH-widthByLevel[depth])*2-(parentH-widthByLevel[depth]));
	} while (inForbiddenRegion(fr, vx, vy));
	bwt.write(vx + CSV_SEP +
		  vy + CSV_SEP +
		  widthByLevel[depth] + CSV_SEP +
		  widthByLevel[depth] + CSV_SEP +
		  ((int)Math.round(widthByLevel[depth]/ROUND_CORNER_RATIO)) + CSV_SEP +
		  ((int)Math.round(widthByLevel[depth]/ROUND_CORNER_RATIO)));
	bwt.newLine();
	// populate target
	// (unless we have reached TREE_DEPTH)
	if (depth < TREE_DEPTH-1){
	    populateRegion(vx, vy, widthByLevel[depth], widthByLevel[depth], depth+1);
	}
    }

    public static void main(String[] args){
	if (args.length > 0){
	    (new AbstractWorldGenerator()).generateWorld(args[0]);
	}
	System.exit(0);
    }
    
    
}
