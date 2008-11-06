/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: IDSequence.java 744 2007-07-27 07:08:00Z epietrig $
 */
 
package net.claribole.eval.alphalens;

import com.xerox.VTM.engine.Camera;

class LabelSequence {

	int[] MMs;
	String[] labels;

	LabelSequence(){
		this.MMs = new int[0];
		this.labels = new String[0];
	}

	int length(){
		return MMs.length;
	}

	void addSequence(String[] idseq){
		int[] tmpSeq = new int[MMs.length + idseq.length];
		float[] tmpSeqA = new float[labels.length + idseq.length];
		System.arraycopy(MMs, 0, tmpSeq, 0, MMs.length);
		System.arraycopy(TAs, 0, tmpSeqA, 0, TAs.length);
		for (int i=0;i<idseq.length;i++){
			tmpSeq[i+MMs.length] = Integer.parseInt(idseq[i]);
		}
		MMs = tmpSeq;
		labels = tmpSeqA;
	}

	public String toString(){
		String res = "" + MMs[0];
		for (int i=1;i<MMs.length;i++){
			res += ", " + MMs[i];
		}
		res += "\n" + labels[0];
		for (int i=1;i<labels.length;i++){
			res += ", " + labels[i];
		}
		return res;
	}

}
