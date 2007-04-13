/*   FILE: BehaviorTrial.java
 *   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id$
 */

package net.claribole.eval.to;

import com.xerox.VTM.engine.Camera;

public class BehaviorBlock {

    static final String BEHAVIOR_FT = "FT";
    static final String BEHAVIOR_IT = "IT";
    static final String BEHAVIOR_DT = "DT";
    static final float BEHAVIOR_FT_A = 0;
    static final float BEHAVIOR_FT_B = 0.3f;
    static final float BEHAVIOR_IT_A = 2.0f;
    static final float BEHAVIOR_IT_B = -1.3f;
    static final float BEHAVIOR_DT_A = -0.5f;
    static final float BEHAVIOR_DT_B = 0.5f;

    static final String TARGET_MAIN_VIEWPORT = "MV";
    static final String TARGET_TRAILING_WIDGET = "TW";

    static final String DIRECTION_NW_STR = "NW";   // north west
    static final String DIRECTION_NE_STR = "NE";   // north east
    static final String DIRECTION_SE_STR = "SE";   // south east
    static final String DIRECTION_SW_STR = "SW";   // south west
    static final String DIRECTION_TW_STR = "TW";   // in trailing widget

    static final String RADIUS_R1 = "R1";   // radius 1 
    static final String RADIUS_R2 = "R2";   // radius 2
    static final String RADIUS_R3 = "R3";   // radius 3
    static final String RADIUS_RW = "RT";   // no radius, trailing widget

    static final long C_NE_X = 0;
    static final long C_NE_Y = 0;
    static final long C_SW_X = -3000;
    static final long C_SW_Y = -4600;
    static final long C_SE_X = 2800;
    static final long C_SE_Y = -1300;
    static final long C_NW_X = -3420;
    static final long C_NW_Y = -2500;

    int nbTrials = 0;
    String[] direction;
    String[] radius;
    long[] timeToAcquire;
    
    /* trials when target is in main viewport, either NW, NE, SE or SW */
    BehaviorBlock(String blockLine, String tl){
	String[] data = blockLine.split(BehaviorLogManager.INPUT_CSV_SEP);
	nbTrials = data.length;
	direction = new String[nbTrials];
	radius = new String[nbTrials];
	timeToAcquire = new long[nbTrials];
	if (tl.equals(TARGET_MAIN_VIEWPORT)){
	    for (int i=0;i<nbTrials;i++){
		direction[i] = data[i].substring(0,2);
		radius[i] = data[i].substring(2,4);
	    }
	}
	else {// tl.equals(TARGET_TRAILING_WIDGET)
	    for (int i=0;i<nbTrials;i++){
		direction[i] = data[i].substring(0,2);
		radius[i] = RADIUS_RW;
	    }
	}
    }

    void moveCamera(int trial, Camera c){
	if (direction[trial].equals(DIRECTION_NW_STR)){c.moveTo(C_NW_X, C_NW_Y);}
	else if (direction[trial].equals(DIRECTION_NE_STR)){c.moveTo(C_NE_X, C_NE_Y);}
	else if (direction[trial].equals(DIRECTION_SE_STR)){c.moveTo(C_SE_X, C_SE_Y);}
	else if (direction[trial].equals(DIRECTION_SW_STR)){c.moveTo(C_SW_X, C_SW_Y);}
	else {System.err.println("Error: unknown direction: "+direction[trial]);}
    }
    
}
