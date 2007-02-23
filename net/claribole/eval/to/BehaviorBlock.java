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

    static final String BEHAVIOR_FT_STR = "FT";
    static final String BEHAVIOR_IT_STR = "IT";
    static final String BEHAVIOR_DT_STR = "DT";
    static final float BEHAVIOR_FT_A = 0;
    static final float BEHAVIOR_FT_B = 0.3f;
    static final float BEHAVIOR_IT_A = 1.666f;
    static final float BEHAVIOR_IT_B = -0.666f;
    static final float BEHAVIOR_DT_A = -0.5f;
    static final float BEHAVIOR_DT_B = 0.5f;

    static final String DIRECTION_NW_STR = "NW";   // north west
    static final String DIRECTION_NE_STR = "NE";   // north east
    static final String DIRECTION_SE_STR = "SE";   // south east
    static final String DIRECTION_SW_STR = "SW";   // south west
    static final short DIRECTION_NW = 0;           // north west
    static final short DIRECTION_NE = 1;           // north east
    static final short DIRECTION_SE = 2;           // south east
    static final short DIRECTION_SW = 3;           // south west

    static short getDirection(String d){
	if (d.equals(DIRECTION_NW_STR)){return DIRECTION_NW;}
	else if (d.equals(DIRECTION_NE_STR)){return DIRECTION_NE;}
	else if (d.equals(DIRECTION_SW_STR)){return DIRECTION_SW;}
	else if (d.equals(DIRECTION_SE_STR)){return DIRECTION_SE;}
	else {System.err.println("Error parsing input trials: unknown direction: "+d);return -1;}
    }

    static String getDirection(short s){
	switch (s){
	case DIRECTION_NW:{return DIRECTION_NW_STR;}
	case DIRECTION_NE:{return DIRECTION_NE_STR;}
	case DIRECTION_SW:{return DIRECTION_SW_STR;}
	case DIRECTION_SE:{return DIRECTION_SE_STR;}
	default:{return "";}
	}
    }

    int nbTrials = 0;
    short[] direction;
    long[] timeToAcquire;
    
    BehaviorBlock(String blockLine){
	String[] data = blockLine.split(BehaviorLogManager.INPUT_CSV_SEP);
	nbTrials = data.length;
	direction = new short[nbTrials];
	timeToAcquire = new long[nbTrials];
	for (int i=0;i<nbTrials;i++){
	    direction[i] = getDirection(data[i].substring(0,2));
	}
    }
    
}
