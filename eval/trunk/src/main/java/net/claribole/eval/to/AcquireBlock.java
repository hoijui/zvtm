/*   FILE: AcquireTrial.java
 *   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id$
 */

package net.claribole.eval.to;

import com.xerox.VTM.glyphs.ZCircle;

public class AcquireBlock {

    static final String START_LOC_TL_STR = "TL";   // top left
    static final String START_LOC_TR_STR = "TR";   // top right
    static final String START_LOC_BL_STR = "BL";   // bottom left
    static final String START_LOC_BR_STR = "BR";   // bottom right
    static final short START_LOC_TL = 0;
    static final short START_LOC_TR = 1;
    static final short START_LOC_BL = 2;
    static final short START_LOC_BR = 3;

    static short getStartLocation(String d){
	if (d.equals(START_LOC_TL_STR)){return START_LOC_TL;}
	else if (d.equals(START_LOC_TR_STR)){return START_LOC_TR;}
	else if (d.equals(START_LOC_BL_STR)){return START_LOC_BL;}
	else if (d.equals(START_LOC_BR_STR)){return START_LOC_BR;}
	else {System.err.println("Error parsing input trials: unknown start location: "+d);return -1;}
    }

    static String getStartLocation(short s){
	switch (s){
	case START_LOC_TL:{return START_LOC_TL_STR;}
	case START_LOC_TR:{return START_LOC_TR_STR;}
	case START_LOC_BL:{return START_LOC_BL_STR;}
	case START_LOC_BR:{return START_LOC_BR_STR;}
	default:{return "";}
	}
    }

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
    short[] startlocation;
    short[] direction;
    int[] ID;
    long[] size;
    long[] timeToAcquire;
    long[] timeToAcquireOR;
    long[] timeToCoarseCentering;
    
    AcquireBlock(String blockLine){
	String[] data = blockLine.split(AcquireLogManager.INPUT_CSV_SEP);
	nbTrials = data.length;
	startlocation = new short[nbTrials];
	direction = new short[nbTrials];
	timeToAcquire = new long[nbTrials];
	timeToAcquireOR = new long[nbTrials];
	timeToCoarseCentering = new long[nbTrials];
	ID = new int[nbTrials];
	size = new long[nbTrials];
	for (int i=0;i<nbTrials;i++){
	    startlocation[i] = getStartLocation(data[i].substring(0,2));
	    direction[i] = getDirection(data[i].substring(2,4));
	    ID[i] = Integer.parseInt(data[i].substring(4));
	    size[i] = Math.round((AcquireEval.TARGET_DISTANCE / (Math.pow(2, ID[i]) - 1)));
	}
    }

    ZCircle moveTarget(int trialNumber, long origX, long origY){
	ZCircle res;
	switch(direction[trialNumber]){
	case DIRECTION_NW:{res = new ZCircle(origX-AcquireEval.TARGET_DISTANCE, origY+AcquireEval.TARGET_DISTANCE, 0, size[trialNumber], AcquireEval.TARGET_COLOR);break;}
	case DIRECTION_NE:{res = new ZCircle(origX+AcquireEval.TARGET_DISTANCE, origY+AcquireEval.TARGET_DISTANCE, 0, size[trialNumber], AcquireEval.TARGET_COLOR);break;}
	case DIRECTION_SW:{res = new ZCircle(origX-AcquireEval.TARGET_DISTANCE, origY-AcquireEval.TARGET_DISTANCE, 0, size[trialNumber], AcquireEval.TARGET_COLOR);break;}
	case DIRECTION_SE:{res = new ZCircle(origX+AcquireEval.TARGET_DISTANCE, origY-AcquireEval.TARGET_DISTANCE, 0, size[trialNumber], AcquireEval.TARGET_COLOR);break;}
	default:{System.err.println("Error: moving target: unknown direction: "+direction[trialNumber]);res = null;}
	}
	res.setMinimumProjectedSize(AcquireEval.TARGET_MIN_PROJ_SIZE);
	return res;
    }

    double getStdDeviationOfTimeToAcquireForLastTrials(int lastTrialIndex, int notttia){// number of trials to take into account
	double N = (notttia < lastTrialIndex) ? notttia : lastTrialIndex+1;
	int firstTrialIndex = (lastTrialIndex != notttia) ? lastTrialIndex-(int)N+1 : lastTrialIndex-(int)N+2;
	// compute variance and get standard deviation from it (sqrt)
	double sum = 0;
	for (int i=firstTrialIndex;i<=lastTrialIndex;i++){
	    sum += timeToAcquire[i];
	}
	double mean = sum/N;
	sum = 0;
	for (int i=firstTrialIndex;i<=lastTrialIndex;i++){
	    sum += Math.pow(timeToAcquire[i]-mean,2);
	}
	double res = Math.sqrt(sum / N);
	return res;
    }
    
}
