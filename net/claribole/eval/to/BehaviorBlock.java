/*   FILE: BehaviorTrial.java
 *   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id:  $
 */

package net.claribole.eval.to;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.glyphs.ZCircle;

public class BehaviorBlock {

    static final String BEHAVIOR_FT = "FT";
    static final String BEHAVIOR_IT = "IT";
    static final String BEHAVIOR_DT = "DT";
    static final float BEHAVIOR_FT_A = 0;
    static final float BEHAVIOR_FT_B = 0.3f;
    static final float BEHAVIOR_IT_A = 1.666f;
    static final float BEHAVIOR_IT_B = -0.666f;
    static final float BEHAVIOR_DT_A = -0.5f;
    static final float BEHAVIOR_DT_B = 0.5f;

    static final String TARGET_MAIN_VIEWPORT = "MV";
    static final String TARGET_TRAILING_WIDGET = "TW";

    static final String DIRECTION_NW_STR = "NW";   // north west
    static final String DIRECTION_NE_STR = "NE";   // north east
    static final String DIRECTION_SE_STR = "SE";   // south east
    static final String DIRECTION_SW_STR = "SW";   // south west
    static final String DIRECTION_TW_STR = "TW";   // in trailing widget

    int nbTrials = 0;
    String[] direction;
    long[] timeToAcquire;
    
    /* trials when target is in main viewport, either NW, NE, SE or SW */
    BehaviorBlock(String blockLine){
	String[] data = blockLine.split(BehaviorLogManager.INPUT_CSV_SEP);
	nbTrials = data.length;
	direction = new String[nbTrials];
	timeToAcquire = new long[nbTrials];
	for (int i=0;i<nbTrials;i++){
	    direction[i] = data[i].substring(0,2);
	}
    }
    
    /* trials when target is in trailing widget */
    BehaviorBlock(int trialCount){
	nbTrials = trialCount;
	direction = new String[nbTrials];
	timeToAcquire = new long[nbTrials];
	for (int i=0;i<nbTrials;i++){
	    direction[i] = DIRECTION_TW_STR;
	}
    }

}
