/*   FILE: AbstractTrialInfo.java
 *   DATE OF CREATION:  Fri Apr 21 16:00:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: AbstractTrialInfo.java,v 1.4 2006/05/23 14:36:00 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import java.awt.Color;

import java.util.Vector;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.ZRoundRect;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;
import com.xerox.VTM.engine.LongPoint;

class AbstractTrialInfo {
    
    // trial # in the block
    int trialNumber;
    // number of objects the subject has to visit before
    // he finds the actual target, per level
    int targetIndex;
    // incremented each time the subject visits a target,
    // per level (three levels)
    int nbTargetsVisited = 0;
    LongPoint initialCameraPos;
    
    String visitSequence;
    
    AbstractTrialInfo(int tn, String[] tis){
	trialNumber = tn;
	targetIndex = Integer.parseInt(tis[0]);
	initialCameraPos = new LongPoint(Long.parseLong(tis[1]), Long.parseLong(tis[2]));
	visitSequence = "";
    }

    void newVisit(long time, String id){
	nbTargetsVisited++;
	visitSequence += AbstractTaskLogManager.OUTPUT_CSV_SEP + id + AbstractTaskLogManager.OUTPUT_CSV_SEP + time;
    }

    static String getHeader(){
	String res = "";
	for (int i=0;i<ZLAbstractTask.DENSITY*ZLAbstractTask.DENSITY*2;i++){
	    res += AbstractTaskLogManager.OUTPUT_CSV_SEP + "Visit " + (i+1) + " (Object)" +
		AbstractTaskLogManager.OUTPUT_CSV_SEP + "Visit " + (i+1) + " (Time)";
	}
	return res;
    }

    String getVisitSummary(){
	return visitSequence;
    }

}