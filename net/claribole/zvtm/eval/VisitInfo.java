/*   FILE: VisitInfo.java
 *   DATE OF CREATION:  Wed May 24 08:51:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: VisitInfo.java,v 1.4 2006/06/02 09:05:42 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

class VisitInfo {

    static final int FVCIIL = 10; // FIRST_VISIT_COLUMN_INDEX_IN_LOGS

    int[] visitSeq;
    String technique;
    String trial;

    VisitInfo(String[] items, int t){
	visitSeq = new int[(items.length-FVCIIL)/2];
	int j = 0;
	for (int i=FVCIIL;i<items.length;){
	    visitSeq[j] = Integer.parseInt(items[i]);
	    j++;
	    i+=2;
	}
	technique = items[2];
	trial = String.valueOf(t);
    }

}