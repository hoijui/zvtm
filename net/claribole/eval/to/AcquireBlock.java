/*   FILE: AcquireTrial.java
 *   DATE OF CREATION:  Fri Jan 19 15:35:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id:  $
 */

package net.claribole.eval.to;

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

    short[] direction;
    int[] ID;
    
    AcquireBlock(String blockLine){
	String[] data = blockLine.split(AcquireLogManager.INPUT_CSV_SEP);
	direction = new short[data.length];
	ID = new int[data.length];
	for (int i=0;i<data.length;i++){
	    direction[i] = getDirection(data[i].substring(0,2));
	    ID[i] = Integer.parseInt(data[i].substring(2));
	}
// 	printTrials();
    }
    
//     void printTrials(){
// 	for (int i=0;i<direction.length;i++){
// 	    System.out.println(getDirection(direction[i])+" "+ID[i]);
// 	}
//     }
    
}
