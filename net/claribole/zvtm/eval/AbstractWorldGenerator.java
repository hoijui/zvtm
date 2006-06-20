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

    static int TREE_DEPTH = 6; // we want subjects to navigate through 5 levels, includes root
    static final long SMALLEST_ELEMENT_WIDTH = 50;
    static final long MUL_FACTOR = 50;

    static long WORLD_WIDTH;
    static long WORLD_HEIGHT;
    static long HALF_WORLD_WIDTH;
    static long HALF_WORLD_HEIGHT;

    static long[] widthByLevel;
    static Color[] COLOR_BY_LEVEL;
    static {
	// compute size of all levels
	widthByLevel = new long[TREE_DEPTH];
	COLOR_BY_LEVEL = new Color[TREE_DEPTH];
	for (int i=0;i<TREE_DEPTH;i++){
	    widthByLevel[i] = SMALLEST_ELEMENT_WIDTH * Math.round(Math.pow(MUL_FACTOR, (TREE_DEPTH-i-1)));
	    COLOR_BY_LEVEL[i] = Color.getHSBColor(0, 0, (i)/((float)TREE_DEPTH));
	}
	WORLD_WIDTH = widthByLevel[0] * 2;
	WORLD_HEIGHT = WORLD_WIDTH / 2;
	HALF_WORLD_WIDTH = WORLD_WIDTH / 2;
	HALF_WORLD_HEIGHT = WORLD_HEIGHT / 2;
    }

    // card(NB_CHILDREN) gives the number of trials
    int[] DENSITIES = {5, 10, 20, 10, 5, 20, 5, 20 ,10};
    int trialCount;

    static final int ROUND_CORNER_RATIO = 5;

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
			  (widthByLevel[0]/2) + CSV_SEP +
			  ((int)widthByLevel[0]/ROUND_CORNER_RATIO) + CSV_SEP +
			  ((int)widthByLevel[0]/ROUND_CORNER_RATIO));
		bwt.newLine();
		populateRegion(0, 0, widthByLevel[0], widthByLevel[0]/2, depth+1);
	    }
	    bwt.flush();
	    bwt.close();
	}
	catch (IOException ex){ex.printStackTrace();}
	System.out.println(" done");
    }

    void populateRegion(long parentX, long parentY, long parentW, long parentH, int depth) throws IOException {
	bwt.write("# Level "+depth);
	bwt.newLine();
	// generate NB_CHILDREN-1 false targets
	long vx,vy;
	for (int i=0;i<DENSITIES[trialCount]-1;i++){
	    vx = Math.round(parentX + Math.random()*(parentW-widthByLevel[depth])*2-(parentW-widthByLevel[depth]));
	    vy = Math.round(parentY + Math.random()*(parentH-widthByLevel[depth])*2-(parentH-widthByLevel[depth]));
	    bwt.write(vx + CSV_SEP +
		      vy + CSV_SEP +
		      widthByLevel[depth] + CSV_SEP +
		      widthByLevel[depth]);
	    bwt.newLine();
	}
	// generate 1 target
	vx = Math.round(parentX + Math.random()*(parentW-widthByLevel[depth])*2-(parentW-widthByLevel[depth]));
	vy = Math.round(parentY + Math.random()*(parentH-widthByLevel[depth])*2-(parentH-widthByLevel[depth]));
	bwt.write(vx + CSV_SEP +
		  vy + CSV_SEP +
		  widthByLevel[depth] + CSV_SEP +
		  widthByLevel[depth] + CSV_SEP +
		  ((int)widthByLevel[depth]/ROUND_CORNER_RATIO) + CSV_SEP +
		  ((int)widthByLevel[depth]/ROUND_CORNER_RATIO));
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
