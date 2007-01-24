/*   FILE: AcquireTrial.java
 *   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id:  $
 */

package net.claribole.eval.to;

import com.xerox.VTM.engine.Camera;
import com.xerox.VTM.glyphs.Glyph;

public class AcquireBlock {

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
    int[] ID;
    float[] size;
    
    AcquireBlock(String blockLine){
	String[] data = blockLine.split(AcquireLogManager.INPUT_CSV_SEP);
	nbTrials = data.length;
	direction = new short[nbTrials];
	ID = new int[nbTrials];
	size = new float[nbTrials];
	for (int i=0;i<nbTrials;i++){
	    direction[i] = getDirection(data[i].substring(0,2));
	    ID[i] = Integer.parseInt(data[i].substring(2));
	    size[i] = (float)(AcquireEval.TARGET_DISTANCE / (Math.pow(2, ID[i]) - 1));
	}
    }

    void initTarget(Glyph g, int trialNumber){
	switch(direction[trialNumber]){// camera is at (0,0) at the start of a trial
	case DIRECTION_NW:{g.moveTo(-AcquireEval.TARGET_DISTANCE, AcquireEval.TARGET_DISTANCE);break;}
	case DIRECTION_NE:{g.moveTo(AcquireEval.TARGET_DISTANCE, AcquireEval.TARGET_DISTANCE);break;}
	case DIRECTION_SW:{g.moveTo(-AcquireEval.TARGET_DISTANCE, -AcquireEval.TARGET_DISTANCE);break;}
	case DIRECTION_SE:{g.moveTo(AcquireEval.TARGET_DISTANCE, -AcquireEval.TARGET_DISTANCE);break;}
	default:{System.err.println("Error: initializing target: unknown direction: "+direction[trialNumber]);}
	}
	g.sizeTo(size[trialNumber]);
    }

    void moveTarget(Glyph g, int trialNumber, Camera c){
	switch(direction[trialNumber]){
	case DIRECTION_NW:{g.moveTo(c.posx-AcquireEval.TARGET_DISTANCE, c.posy+AcquireEval.TARGET_DISTANCE);break;}
	case DIRECTION_NE:{g.moveTo(c.posx+AcquireEval.TARGET_DISTANCE, c.posy+AcquireEval.TARGET_DISTANCE);break;}
	case DIRECTION_SW:{g.moveTo(c.posx-AcquireEval.TARGET_DISTANCE, c.posy-AcquireEval.TARGET_DISTANCE);break;}
	case DIRECTION_SE:{g.moveTo(c.posx+AcquireEval.TARGET_DISTANCE, c.posy-AcquireEval.TARGET_DISTANCE);break;}
	default:{System.err.println("Error: moving target: unknown direction: "+direction[trialNumber]);}
	}
    }
    
}
